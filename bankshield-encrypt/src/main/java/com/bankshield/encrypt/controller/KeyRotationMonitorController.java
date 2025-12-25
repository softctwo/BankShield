package com.bankshield.encrypt.controller;

import com.bankshield.common.result.Result;
import com.bankshield.encrypt.entity.EncryptionKey;
import com.bankshield.encrypt.entity.KeyRotationMonitor;
import com.bankshield.encrypt.entity.KeyRotationPlan;
import com.bankshield.encrypt.enums.KeyRotationStatus;
import com.bankshield.encrypt.mapper.EncryptionKeyMapper;
import com.bankshield.encrypt.mapper.KeyRotationHistoryMapper;
import com.bankshield.encrypt.service.SmoothKeyRotationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 密钥轮换监控控制器
 * 提供密钥轮换状态的实时监控和管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/key/rotation")
public class KeyRotationMonitorController {
    
    @Autowired
    private SmoothKeyRotationService rotationService;
    
    @Autowired
    private EncryptionKeyMapper keyMapper;
    
    @Autowired
    private KeyRotationHistoryMapper rotationHistoryMapper;
    
    /**
     * 获取密钥轮换监控数据
     */
    @GetMapping("/monitor")
    @PreAuthorize("hasAuthority('KEY_VIEW')")
    public Result<KeyRotationMonitor> getRotationMonitor() {
        try {
            KeyRotationMonitor monitor = new KeyRotationMonitor();
            
            // 查询处于不同轮换状态的密钥数量
            monitor.setKeysInWarmUp(countKeysByRotationStatus(KeyRotationStatus.WARMING_UP.name()));
            monitor.setKeysInDualActive(countKeysByRotationStatus(KeyRotationStatus.DUAL_ACTIVE.name()));
            monitor.setKeysInDecryptOnly(countKeysByRotationStatus(KeyRotationStatus.DECRYPT_ONLY.name()));
            
            // 查询即将过期的密钥
            monitor.setKeysExpiringSoon(countExpiringKeys(30));
            
            // 查询总的活跃密钥数量
            monitor.setTotalActiveKeys(countActiveKeys());
            
            // 计算轮换健康分数
            monitor.setRotationHealthScore(calculateRotationHealthScore());
            
            // 查询最近的轮换历史
            monitor.setRecentRotations(getRecentRotations(10));
            
            log.info("密钥轮换监控数据获取成功: warmingUp={}, dualActive={}, decryptOnly={}, expiringSoon={}", 
                    monitor.getKeysInWarmUp(), monitor.getKeysInDualActive(), 
                    monitor.getKeysInDecryptOnly(), monitor.getKeysExpiringSoon());
            
            return Result.success(monitor);
            
        } catch (Exception e) {
            log.error("获取密钥轮换监控数据失败", e);
            return Result.error(500, "获取监控数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 强制轮换密钥
     */
    @PostMapping("/{keyId}/force-rotate")
    @PreAuthorize("hasAuthority('KEY_ROTATE')")
    public Result<KeyRotationPlan> forceRotate(@PathVariable Long keyId, 
                                               @RequestBody Map<String, String> request) {
        try {
            String reason = request.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return Result.error(400, "轮换原因不能为空");
            }
            
            log.info("强制轮换密钥: keyId={}, reason={}", keyId, reason);
            
            Result<KeyRotationPlan> result = rotationService.executeSmoothRotation(keyId, reason);
            
            if (result.isSuccess()) {
                log.info("强制轮换密钥成功: keyId={}, planId={}", keyId, result.getData().hashCode());
            } else {
                log.error("强制轮换密钥失败: keyId={}, message={}", keyId, result.getMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("强制轮换密钥异常: keyId=" + keyId, e);
            return Result.error(500, "强制轮换密钥失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取密钥轮换状态
     */
    @GetMapping("/{keyId}/status")
    @PreAuthorize("hasAuthority('KEY_VIEW')")
    public Result<Map<String, Object>> getKeyRotationStatus(@PathVariable Long keyId) {
        try {
            Result<String> statusResult = rotationService.checkRotationStatus(keyId);
            
            if (!statusResult.isSuccess()) {
                return Result.error(statusResult.getCode(), statusResult.getMessage());
            }
            
            Map<String, Object> statusInfo = new HashMap<>();
            statusInfo.put("keyId", keyId);
            statusInfo.put("rotationStatus", statusResult.getData());
            statusInfo.put("statusDescription", getStatusDescription(statusResult.getData()));
            statusInfo.put("canEncrypt", KeyRotationStatus.fromCode(statusResult.getData()).canEncrypt());
            statusInfo.put("canDecrypt", KeyRotationStatus.fromCode(statusResult.getData()).canDecrypt());
            
            // 获取密钥基本信息
            EncryptionKey key = keyMapper.selectById(keyId);
            if (key != null) {
                statusInfo.put("keyName", key.getKeyName());
                statusInfo.put("keyType", key.getKeyType());
                statusInfo.put("keyStatus", key.getKeyStatus());
                statusInfo.put("expireTime", key.getExpireTime());
                statusInfo.put("rotationStartTime", key.getRotationStartTime());
                statusInfo.put("rotationCompleteTime", key.getRotationCompleteTime());
            }
            
            return Result.success(statusInfo);
            
        } catch (Exception e) {
            log.error("获取密钥轮换状态失败: keyId=" + keyId, e);
            return Result.error(500, "获取轮换状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取活跃的轮换计划
     */
    @GetMapping("/active-plans")
    @PreAuthorize("hasAuthority('KEY_VIEW')")
    public Result<List<KeyRotationPlan>> getActiveRotationPlans() {
        try {
            Result<List<KeyRotationPlan>> result = rotationService.getActiveRotationPlans();
            
            if (result.isSuccess()) {
                log.info("获取活跃轮换计划成功: count={}", result.getData().size());
            } else {
                log.error("获取活跃轮换计划失败: {}", result.getMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("获取活跃轮换计划异常", e);
            return Result.error(500, "获取活跃轮换计划失败: " + e.getMessage());
        }
    }
    
    /**
     * 取消轮换计划
     */
    @PostMapping("/cancel/{planId}")
    @PreAuthorize("hasAuthority('KEY_ROTATE')")
    public Result<Void> cancelRotation(@PathVariable String planId, 
                                      @RequestBody Map<String, String> request) {
        try {
            String reason = request.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return Result.error(400, "取消原因不能为空");
            }
            
            log.info("取消轮换计划: planId={}, reason={}", planId, reason);
            
            Result<Void> result = rotationService.cancelRotation(planId, reason);
            
            if (result.isSuccess()) {
                log.info("取消轮换计划成功: planId={}", planId);
            } else {
                log.error("取消轮换计划失败: planId={}, message={}", planId, result.getMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("取消轮换计划异常: planId=" + planId, e);
            return Result.error(500, "取消轮换计划失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取轮换统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('KEY_VIEW')")
    public Result<Map<String, Object>> getRotationStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 总轮换次数
            Long totalRotations = rotationHistoryMapper.selectCount(null);
            statistics.put("totalRotations", totalRotations.intValue());
            
            // 成功轮换次数
            Long successfulRotations = rotationHistoryMapper.selectCount(
                new QueryWrapper<com.bankshield.encrypt.entity.KeyRotationHistory>()
                    .eq("rotation_status", "SUCCESS"));
            statistics.put("successfulRotations", successfulRotations.intValue());
            
            // 失败轮换次数
            Long failedRotations = rotationHistoryMapper.selectCount(
                new QueryWrapper<com.bankshield.encrypt.entity.KeyRotationHistory>()
                    .eq("rotation_status", "FAILED"));
            statistics.put("failedRotations", failedRotations.intValue());
            
            // 成功率
            double successRate = totalRotations > 0 ? 
                (successfulRotations * 100.0 / totalRotations) : 0;
            statistics.put("successRate", String.format("%.2f%%", successRate));
            
            // 本月轮换次数
            LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            Long monthlyRotations = rotationHistoryMapper.selectCount(
                new QueryWrapper<com.bankshield.encrypt.entity.KeyRotationHistory>()
                    .ge("rotation_time", startOfMonth));
            statistics.put("monthlyRotations", monthlyRotations.intValue());
            
            log.info("获取轮换统计信息成功: total={}, success={}, failed={}, rate={}", 
                    totalRotations, successfulRotations, failedRotations, successRate);
            
            return Result.success(statistics);
            
        } catch (Exception e) {
            log.error("获取轮换统计信息失败", e);
            return Result.error(500, "获取统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 统计指定轮换状态的密钥数量
     */
    private long countKeysByRotationStatus(String status) {
        return keyMapper.selectCount(
            new QueryWrapper<EncryptionKey>().eq("rotation_status", status));
    }
    
    /**
     * 统计即将过期的密钥数量
     */
    private long countExpiringKeys(int daysAhead) {
        return keyMapper.selectCount(
            new QueryWrapper<EncryptionKey>()
                .le("expire_time", LocalDateTime.now().plusDays(daysAhead))
                .eq("key_status", "ACTIVE"));
    }
    
    /**
     * 统计活跃密钥数量
     */
    private long countActiveKeys() {
        return keyMapper.selectCount(
            new QueryWrapper<EncryptionKey>().eq("key_status", "ACTIVE"));
    }
    
    /**
     * 计算轮换健康分数
     */
    private double calculateRotationHealthScore() {
        try {
            long totalKeys = countActiveKeys();
            long expiringKeys = countExpiringKeys(30);
            long warmingUpKeys = countKeysByRotationStatus(KeyRotationStatus.WARMING_UP.name());
            long dualActiveKeys = countKeysByRotationStatus(KeyRotationStatus.DUAL_ACTIVE.name());
            
            if (totalKeys == 0) {
                return 100.0; // 没有密钥时认为是健康的
            }
            
            // 健康分数计算逻辑
            double expiringScore = Math.max(0, 100 - (expiringKeys * 10.0 / totalKeys));
            double rotationScore = (warmingUpKeys + dualActiveKeys) * 5.0 / totalKeys;
            
            return Math.min(100.0, expiringScore + rotationScore);
            
        } catch (Exception e) {
            log.error("计算轮换健康分数失败", e);
            return 0.0;
        }
    }
    
    /**
     * 获取最近的轮换历史
     */
    private List<Map<String, Object>> getRecentRotations(int limit) {
        // 这里应该实现查询最近轮换历史的逻辑
        // 为了简化实现，返回空列表
        return java.util.Collections.emptyList();
    }
    
    /**
     * 获取状态描述
     */
    private String getStatusDescription(String status) {
        try {
            KeyRotationStatus rotationStatus = KeyRotationStatus.fromCode(status);
            return rotationStatus.getDescription();
        } catch (Exception e) {
            return "未知状态";
        }
    }
}