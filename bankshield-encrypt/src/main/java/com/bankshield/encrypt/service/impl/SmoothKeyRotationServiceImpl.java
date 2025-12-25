package com.bankshield.encrypt.service.impl;

import com.bankshield.common.result.Result;
import com.bankshield.encrypt.entity.EncryptionKey;
import com.bankshield.encrypt.entity.KeyRotationHistory;
import com.bankshield.encrypt.entity.KeyRotationPlan;
import com.bankshield.encrypt.enums.KeyRotationStatus;
import com.bankshield.encrypt.enums.KeyStatus;
import com.bankshield.encrypt.enums.KeyType;
import com.bankshield.encrypt.enums.KeyUsage;
import com.bankshield.encrypt.mapper.EncryptionKeyMapper;
import com.bankshield.encrypt.mapper.KeyRotationHistoryMapper;
import com.bankshield.encrypt.service.KeyManagementService;
import com.bankshield.encrypt.service.SmoothKeyRotationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 平滑密钥轮换服务实现类
 * 实现四阶段平滑轮换：预热期→双密钥期→仅解密期→过期
 */
@Slf4j
@Service
public class SmoothKeyRotationServiceImpl implements SmoothKeyRotationService {
    
    @Autowired
    private EncryptionKeyMapper encryptionKeyMapper;
    
    @Autowired
    private KeyRotationHistoryMapper rotationHistoryMapper;
    
    @Autowired
    private KeyManagementService keyManagementService;
    
    // 轮换配置
    @Value("${key.rotation.warm-up-days:3}")
    private int warmUpDays;
    
    @Value("${key.rotation.dual-active-days:7}")
    private int dualActiveDays;
    
    @Value("${key.rotation.decrypt-only-days:30}")
    private int decryptOnlyDays;
    
    @Value("${key.rotation.auto-schedule:true}")
    private boolean autoSchedule;
    
    /**
     * 执行平滑密钥轮换
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<KeyRotationPlan> executeSmoothRotation(Long oldKeyId, String rotationReason) {
        log.info("开始执行平滑密钥轮换: oldKeyId={}, reason={}", oldKeyId, rotationReason);
        
        try {
            // 1. 获取旧密钥
            EncryptionKey oldKey = encryptionKeyMapper.selectById(oldKeyId);
            if (oldKey == null) {
                return Result.error(404, "密钥不存在");
            }
            
            // 检查密钥状态
            if (!KeyStatus.ACTIVE.name().equals(oldKey.getKeyStatus())) {
                return Result.error(400, "只有活跃状态的密钥才能轮换");
            }
            
            // 检查是否已有活跃的轮换计划
            if (hasActiveRotationPlan(oldKeyId)) {
                return Result.error(400, "该密钥已有活跃的轮换计划");
            }
            
            // 2. 生成新密钥
            // 将String类型转换为枚举类型
            KeyType keyType;
            KeyUsage keyUsage;
            try {
                keyType = KeyType.fromCode(oldKey.getKeyType());
                keyUsage = KeyUsage.fromCode(oldKey.getKeyUsage());
            } catch (IllegalArgumentException e) {
                log.error("密钥类型或用途转换失败: keyType={}, keyUsage={}", 
                         oldKey.getKeyType(), oldKey.getKeyUsage(), e);
                return Result.error(400, "密钥类型或用途无效: " + e.getMessage());
            }
            
            // 调用generateKey方法，使用正确的参数列表
            String newKeyName = oldKey.getKeyName() + "_rotated_" + System.currentTimeMillis();
            String newKeyDescription = "通过轮换生成的密钥，原密钥ID: " + oldKeyId;
            Integer rotationCycle = null; // 轮换周期使用默认值
            
            Result<EncryptionKey> newKeyResult = keyManagementService.generateKey(
                newKeyName,
                keyType,
                keyUsage,
                oldKey.getKeyLength(),
                Integer.valueOf(90),  // expireDays
                rotationCycle,
                newKeyDescription,
                "SYSTEM"
            );
            if (!newKeyResult.isSuccess()) {
                return Result.error(500, "新密钥生成失败: " + newKeyResult.getMessage());
            }
            
            EncryptionKey newKey = newKeyResult.getData();
            Long newKeyId = newKey.getId();
            
            // 3. 设置轮换关系
            LocalDateTime now = LocalDateTime.now();
            
            oldKey.setRotationStatus(KeyRotationStatus.WARMING_UP.name());
            oldKey.setRotationStartTime(now);
            oldKey.setNextKeyId(newKeyId);
            oldKey.setRotationCompleteTime(now.plusDays(warmUpDays + dualActiveDays + decryptOnlyDays));
            
            newKey.setPrevKeyId(oldKeyId);
            newKey.setRotationStatus(KeyRotationStatus.WARMING_UP.name());
            
            encryptionKeyMapper.updateById(oldKey);
            encryptionKeyMapper.updateById(newKey);
            
            // 4. 创建轮换计划
            KeyRotationPlan plan = createRotationPlan(oldKey, newKey, rotationReason);
            
            // 5. 记录轮换历史
            KeyRotationHistory history = new KeyRotationHistory();
            history.setOldKeyId(oldKeyId);
            history.setNewKeyId(newKeyId);
            history.setRotationTime(now);
            history.setRotationReason(rotationReason);
            history.setRotatedBy(getCurrentUsername());
            history.setRotationStatus("IN_PROGRESS");
            history.setFailureReason("计划创建成功，等待执行");
            
            rotationHistoryMapper.insert(history);
            
            // 6. 启动轮换定时任务
            if (autoSchedule) {
                scheduleRotationPhases(plan);
            }
            
            log.info("平滑密钥轮换计划创建成功: oldKeyId={}, newKeyId={}, planId={}", 
                     oldKeyId, newKeyId, plan.hashCode());
            
            return Result.success(plan);
            
        } catch (Exception e) {
            log.error("执行平滑密钥轮换失败: oldKeyId=" + oldKeyId, e);
            return Result.error(500, "执行平滑密钥轮换失败: " + e.getMessage());
        }
    }
    
    /**
     * 进入双密钥活跃期
     */
    @Override
    @Transactional
    public void enterDualActivePhase(KeyRotationPlan plan) {
        log.info("进入双密钥活跃期: oldKey={}, newKey={}", plan.getOldKeyId(), plan.getNewKeyId());
        
        try {
            // 更新旧密钥状态
            EncryptionKey oldKey = encryptionKeyMapper.selectById(plan.getOldKeyId());
            oldKey.setRotationStatus(KeyRotationStatus.DUAL_ACTIVE.name());
            encryptionKeyMapper.updateById(oldKey);
            
            // 更新新密钥状态
            EncryptionKey newKey = encryptionKeyMapper.selectById(plan.getNewKeyId());
            newKey.setRotationStatus(KeyRotationStatus.DUAL_ACTIVE.name());
            newKey.setKeyStatus(KeyStatus.ACTIVE.name()); // 新密钥正式激活
            encryptionKeyMapper.updateById(newKey);
            
            // 更新轮换历史
            updateRotationHistory(plan, "进入双密钥活跃期");
            
            log.info("双密钥活跃期开始: 新旧密钥并行运行");
            
        } catch (Exception e) {
            log.error("进入双密钥活跃期失败", e);
            handleRotationFailure(plan, "进入双密钥活跃期失败: " + e.getMessage());
        }
    }
    
    /**
     * 进入仅解密期
     */
    @Override
    @Transactional
    public void enterDecryptOnlyPhase(KeyRotationPlan plan) {
        log.info("进入仅解密期: oldKey={}, newKey={}", plan.getOldKeyId(), plan.getNewKeyId());
        
        try {
            // 更新旧密钥状态
            EncryptionKey oldKey = encryptionKeyMapper.selectById(plan.getOldKeyId());
            oldKey.setRotationStatus(KeyRotationStatus.DECRYPT_ONLY.name());
            oldKey.setKeyStatus(KeyStatus.INACTIVE.name()); // 旧密钥标记为不活跃
            encryptionKeyMapper.updateById(oldKey);
            
            // 新密钥保持活跃
            EncryptionKey newKey = encryptionKeyMapper.selectById(plan.getNewKeyId());
            newKey.setRotationStatus(KeyRotationStatus.ACTIVE.name());
            encryptionKeyMapper.updateById(newKey);
            
            // 更新轮换历史
            updateRotationHistory(plan, "进入仅解密期");
            
            log.info("仅解密期开始: 旧密钥仅用于解密历史数据");
            
        } catch (Exception e) {
            log.error("进入仅解密期失败", e);
            handleRotationFailure(plan, "进入仅解密期失败: " + e.getMessage());
        }
    }
    
    /**
     * 完成轮换
     */
    @Override
    @Transactional
    public void completeRotation(KeyRotationPlan plan) {
        log.info("完成密钥轮换: oldKey={}, newKey={}", plan.getOldKeyId(), plan.getNewKeyId());
        
        try {
            // 旧密钥过期
            EncryptionKey oldKey = encryptionKeyMapper.selectById(plan.getOldKeyId());
            oldKey.setKeyStatus(KeyStatus.EXPIRED.name());
            oldKey.setExpireTime(LocalDateTime.now());
            oldKey.setRotationStatus(KeyRotationStatus.EXPIRED.name());
            encryptionKeyMapper.updateById(oldKey);
            
            // 新密钥完全激活
            EncryptionKey newKey = encryptionKeyMapper.selectById(plan.getNewKeyId());
            newKey.setRotationStatus(KeyRotationStatus.ACTIVE.name());
            newKey.setPrevKeyId(plan.getOldKeyId());
            encryptionKeyMapper.updateById(newKey);
            
            // 更新轮换历史
            updateRotationHistory(plan, "轮换完成");
            
            plan.setStatus("SUCCESS");
            plan.setCompleteTime(LocalDateTime.now());
            
            log.info("密钥轮换完成: 旧密钥已安全下线，新密钥完全接管");
            
        } catch (Exception e) {
            log.error("完成密钥轮换失败", e);
            handleRotationFailure(plan, "完成密钥轮换失败: " + e.getMessage());
        }
    }
    
    /**
     * 取消轮换
     */
    @Override
    @Transactional
    public Result<Void> cancelRotation(String planId, String reason) {
        try {
            log.info("取消密钥轮换: planId={}, reason={}", planId, reason);
            
            // 这里应该实现具体的取消逻辑
            // 包括恢复密钥状态、清理计划等
            
            return Result.success();
        } catch (Exception e) {
            log.error("取消密钥轮换失败", e);
            return Result.error(500, "取消密钥轮换失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取轮换计划详情
     */
    @Override
    public Result<KeyRotationPlan> getRotationPlan(String planId) {
        // 这里应该从持久化存储中获取轮换计划
        // 为了简化实现，返回空结果
        return Result.error(404, "轮换计划不存在");
    }
    
    /**
     * 检查轮换状态
     */
    @Override
    public Result<String> checkRotationStatus(Long keyId) {
        try {
            EncryptionKey key = encryptionKeyMapper.selectById(keyId);
            if (key == null) {
                return Result.error(404, "密钥不存在");
            }
            
            String rotationStatus = key.getRotationStatus();
            if (rotationStatus == null) {
                rotationStatus = KeyRotationStatus.ACTIVE.name();
            }
            
            return Result.success(rotationStatus);
        } catch (Exception e) {
            log.error("检查轮换状态失败", e);
            return Result.error(500, "检查轮换状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取活跃的轮换计划
     */
    @Override
    public Result<List<KeyRotationPlan>> getActiveRotationPlans() {
        // 这里应该查询数据库中的活跃轮换计划
        // 为了简化实现，返回空列表
        return Result.success(java.util.Collections.emptyList());
    }
    
    /**
     * 创建轮换计划
     */
    private KeyRotationPlan createRotationPlan(EncryptionKey oldKey, EncryptionKey newKey, String rotationReason) {
        KeyRotationPlan plan = new KeyRotationPlan();
        
        LocalDateTime now = LocalDateTime.now();
        
        // 基本信息
        plan.setOldKeyId(oldKey.getId());
        plan.setNewKeyId(newKey.getId());
        plan.setStatus("IN_PROGRESS");
        plan.setRotationReason(rotationReason);
        plan.setCreateTime(now);
        
        // 阶段1: 预热期（默认3天）
        plan.setWarmUpStartTime(now);
        plan.setWarmUpEndTime(now.plusDays(warmUpDays));
        plan.setWarmUpDurationDays(warmUpDays);
        
        // 阶段2: 双密钥活跃期（默认7天）
        plan.setDualActiveStartTime(plan.getWarmUpEndTime());
        plan.setDualActiveEndTime(plan.getDualActiveStartTime().plusDays(dualActiveDays));
        plan.setDualActiveDurationDays(dualActiveDays);
        
        // 阶段3: 仅解密期（默认30天）
        plan.setDecryptOnlyStartTime(plan.getDualActiveEndTime());
        plan.setDecryptOnlyEndTime(plan.getDecryptOnlyStartTime().plusDays(decryptOnlyDays));
        plan.setDecryptOnlyDurationDays(decryptOnlyDays);
        
        // 阶段4: 完成时间
        plan.setCompleteTime(plan.getDecryptOnlyEndTime());
        
        // 初始化进度信息
        plan.setProgressPercentage(0);
        plan.setCurrentPhase("预热期");
        plan.setEstimatedRemainingHours((warmUpDays + dualActiveDays + decryptOnlyDays) * 24);
        
        return plan;
    }
    
    /**
     * 调度轮换的各个阶段
     */
    private void scheduleRotationPhases(KeyRotationPlan plan) {
        try {
            // 阶段1: 预热期结束（启用双密钥）
            scheduleTask(() -> enterDualActivePhase(plan), plan.getWarmUpEndTime());
            
            // 阶段2: 双密钥期结束（旧密钥只用于解密）
            scheduleTask(() -> enterDecryptOnlyPhase(plan), plan.getDualActiveEndTime());
            
            // 阶段3: 仅解密期结束（旧密钥过期）
            scheduleTask(() -> completeRotation(plan), plan.getDecryptOnlyEndTime());
            
            log.info("已调度密钥轮换任务: warmUp={}, dualActive={}, decryptOnly={}", 
                     plan.getWarmUpEndTime(), plan.getDualActiveEndTime(), plan.getDecryptOnlyEndTime());
            
        } catch (Exception e) {
            log.error("调度轮换阶段失败", e);
            handleRotationFailure(plan, "调度轮换阶段失败: " + e.getMessage());
        }
    }
    
    /**
     * 调度任务
     */
    private void scheduleTask(Runnable task, LocalDateTime executeTime) {
        long delay = Duration.between(LocalDateTime.now(), executeTime).toMillis();
        
        if (delay > 0) {
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.schedule(() -> {
                try {
                    task.run();
                } catch (Exception e) {
                    log.error("执行调度任务失败", e);
                }
            }, delay, TimeUnit.MILLISECONDS);
            scheduler.shutdown();
        } else {
            // 如果执行时间已过，立即执行
            task.run();
        }
    }
    
    /**
     * 检查是否有活跃的轮换计划
     */
    private boolean hasActiveRotationPlan(Long oldKeyId) {
        EncryptionKey key = encryptionKeyMapper.selectById(oldKeyId);
        if (key == null) {
            return false;
        }
        
        String rotationStatus = key.getRotationStatus();
        return rotationStatus != null && 
               (KeyRotationStatus.WARMING_UP.name().equals(rotationStatus) ||
                KeyRotationStatus.DUAL_ACTIVE.name().equals(rotationStatus) ||
                KeyRotationStatus.DECRYPT_ONLY.name().equals(rotationStatus));
    }
    
    /**
     * 更新轮换历史
     */
    private void updateRotationHistory(KeyRotationPlan plan, String phaseDescription) {
        try {
            QueryWrapper<KeyRotationHistory> wrapper = new QueryWrapper<>();
            wrapper.eq("old_key_id", plan.getOldKeyId())
                   .eq("new_key_id", plan.getNewKeyId())
                   .eq("rotation_status", "IN_PROGRESS");
            
            KeyRotationHistory history = rotationHistoryMapper.selectOne(wrapper);
            if (history != null) {
                history.setFailureReason(phaseDescription);
                rotationHistoryMapper.updateById(history);
            }
        } catch (Exception e) {
            log.error("更新轮换历史失败", e);
        }
    }
    
    /**
     * 处理轮换失败
     */
    private void handleRotationFailure(KeyRotationPlan plan, String failureReason) {
        try {
            log.error("密钥轮换失败: {}", failureReason);
            
            plan.setStatus("FAILED");
            
            // 更新轮换历史
            updateRotationHistory(plan, "轮换失败: " + failureReason);
            
            // 可以在这里添加失败通知逻辑
            
        } catch (Exception e) {
            log.error("处理轮换失败时发生错误", e);
        }
    }
    
    /**
     * 获取当前用户名
     */
    private String getCurrentUsername() {
        try {
            org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            return auth != null ? auth.getName() : "system";
        } catch (Exception e) {
            return "system";
        }
    }
}