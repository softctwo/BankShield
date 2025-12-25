package com.bankshield.encrypt.service.impl;

import com.alibaba.excel.EasyExcel;
import com.bankshield.api.service.SecureKeyManagementService;
import com.bankshield.common.result.PageResult;
import com.bankshield.common.result.Result;
import com.bankshield.common.result.ResultCode;
import com.bankshield.encrypt.entity.EncryptionKey;
import com.bankshield.encrypt.entity.KeyRotationHistory;
import com.bankshield.encrypt.entity.KeyUsageAudit;
import com.bankshield.encrypt.enums.KeyStatus;
import com.bankshield.encrypt.enums.KeyType;
import com.bankshield.encrypt.enums.KeyUsage;
import com.bankshield.encrypt.mapper.EncryptionKeyMapper;
import com.bankshield.encrypt.mapper.KeyRotationHistoryMapper;
import com.bankshield.encrypt.mapper.KeyUsageAuditMapper;
import com.bankshield.encrypt.service.KeyGenerationService;
import com.bankshield.encrypt.service.KeyManagementService;
import com.bankshield.encrypt.service.KeyStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 密钥管理服务实现类
 */
@Slf4j
@Service
public class KeyManagementServiceImpl implements KeyManagementService {
    
    @Autowired
    private EncryptionKeyMapper encryptionKeyMapper;
    
    @Autowired
    private KeyRotationHistoryMapper keyRotationHistoryMapper;
    
    @Autowired
    private KeyUsageAuditMapper keyUsageAuditMapper;
    
    @Autowired
    private KeyGenerationService keyGenerationService;
    
    @Autowired
    private KeyStorageService keyStorageService;
    
    @Autowired(required = false)
    private SecureKeyManagementService secureKeyManagementService;
    
    @Value("${bankshield.encrypt.vault-enabled:false}")
    private boolean vaultEnabled;
    
    @Override
    @Transactional
    public Result<EncryptionKey> generateKey(String keyName, KeyType keyType, KeyUsage keyUsage,
                                            Integer keyLength, Integer expireDays, Integer rotationCycle,
                                            String description, String createdBy) {
        try {
            // 检查密钥名称是否已存在
            if (isKeyNameExists(keyName)) {
                return Result.error(ResultCode.PARAMETER_ERROR.getMessage());
            }
            
            // 生成密钥
            String keyMaterial;
            String keyFingerprint;
            
            if (vaultEnabled && secureKeyManagementService != null) {
                // 使用Vault集成的安全密钥生成
                log.info("使用Vault集成的安全密钥生成服务");
                Result<String> keyGenerationResult = secureKeyManagementService.generateEncryptionKey(
                        keyType.name(), keyLength);
                
                if (!keyGenerationResult.isSuccess()) {
                    throw new RuntimeException("密钥生成失败: " + keyGenerationResult.getMessage());
                }
                
                keyMaterial = keyGenerationResult.getData();
                keyFingerprint = keyGenerationService.calculateKeyFingerprint(keyMaterial);
                
                log.info("使用Vault成功生成加密密钥");
            } else {
                // 使用传统密钥生成服务
                log.info("使用传统密钥生成服务");
                if (keyType == KeyType.SM2 || keyType == KeyType.RSA) {
                    // 非对称加密，生成密钥对
                    KeyGenerationService.KeyPair keyPair = keyGenerationService.generateKeyPair(keyType, keyUsage, keyLength);
                    keyMaterial = keyPair.getPrivateKey() + "|" + keyPair.getPublicKey();
                    keyFingerprint = keyGenerationService.calculateKeyFingerprint(keyPair.getPublicKey());
                } else {
                    // 对称加密，生成单个密钥
                    keyMaterial = keyGenerationService.generateKey(keyType, keyUsage, keyLength);
                    keyFingerprint = keyGenerationService.calculateKeyFingerprint(keyMaterial);
                }
            }
            
            // 加密密钥材料
            String encryptedKeyMaterial = keyStorageService.encryptKeyMaterial(keyMaterial);
            
            // 创建密钥实体
            EncryptionKey encryptionKey = new EncryptionKey();
            encryptionKey.setKeyName(keyName);
            encryptionKey.setKeyType(keyType.getCode());
            encryptionKey.setKeyLength(keyLength);
            encryptionKey.setKeyUsage(keyUsage.getCode());
            encryptionKey.setKeyStatus(KeyStatus.ACTIVE.getCode());
            encryptionKey.setKeyFingerprint(keyFingerprint);
            encryptionKey.setKeyMaterial(encryptedKeyMaterial);
            encryptionKey.setCreatedBy(createdBy);
            encryptionKey.setExpireTime(LocalDateTime.now().plusDays(expireDays != null ? expireDays : 365));
            encryptionKey.setRotationCycle(rotationCycle != null ? rotationCycle : 90);
            encryptionKey.setRotationCount(0);
            encryptionKey.setDescription(description);
            
            // 保存到数据库
            encryptionKeyMapper.insert(encryptionKey);
            
            // 记录使用审计
            recordKeyUsage(encryptionKey.getId(), "GENERATE", createdBy, "生成新密钥");
            
            log.info("密钥生成成功，ID：{}，名称：{}，类型：{}", 
                    encryptionKey.getId(), keyName, keyType.getCode());
            
            return Result.success(encryptionKey);
            
        } catch (Exception e) {
            log.error("生成密钥失败", e);
            return Result.error("生成密钥失败：" + e.getMessage());
        }
    }
    
    @Override
    public PageResult<EncryptionKey> getKeyList(Integer pageNum, Integer pageSize, String keyName,
                                               String keyType, String keyStatus, String keyUsage) {
        try {
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<EncryptionKey> page = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
            
            IPage<EncryptionKey> resultPage = encryptionKeyMapper.selectKeyPage(page, keyName, keyType, keyStatus, keyUsage);
            
            // 脱敏处理：不返回密钥材料
            resultPage.getRecords().forEach(key -> key.setKeyMaterial(null));

            // 清理密钥材料引用
            for (EncryptionKey key : resultPage.getRecords()) {
                key.setKeyMaterial(null);
            }
            
            return PageResult.success(resultPage.getRecords(), resultPage.getTotal(), pageNum, pageSize);
            
        } catch (Exception e) {
            log.error("查询密钥列表失败", e);
            return PageResult.error("查询密钥列表失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<EncryptionKey> getKeyDetail(Long keyId) {
        try {
            EncryptionKey encryptionKey = encryptionKeyMapper.selectById(keyId);
            if (encryptionKey == null) {
                return Result.error("密钥不存在");
            }
            
            // 脱敏处理：不返回密钥材料
            encryptionKey.setKeyMaterial(null);
            
            return Result.success(encryptionKey);
            
        } catch (Exception e) {
            log.error("获取密钥详情失败，ID：{}", keyId, e);
            return Result.error("获取密钥详情失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Result<Void> updateKeyStatus(Long keyId, KeyStatus status, String operator) {
        try {
            EncryptionKey encryptionKey = encryptionKeyMapper.selectById(keyId);
            if (encryptionKey == null) {
                return Result.error("密钥不存在");
            }
            
            // 检查状态转换是否合法
            if (!isValidStatusTransition(encryptionKey.getKeyStatus(), status.getCode())) {
                return Result.error("无效的状态转换：从 " + encryptionKey.getKeyStatus() + " 到 " + status.getCode());
            }
            
            // 更新状态
            encryptionKeyMapper.updateKeyStatus(keyId, status.getCode());
            
            // 记录使用审计
            String operationType = "UPDATE_STATUS_" + status.getCode();
            recordKeyUsage(keyId, operationType, operator, "更新密钥状态为" + status.getDescription());
            
            log.info("密钥状态更新成功，ID：{}，新状态：{}", keyId, status.getCode());
            
            return Result.success();
            
        } catch (Exception e) {
            log.error("更新密钥状态失败，ID：{}", keyId, e);
            return Result.error("更新密钥状态失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Result<EncryptionKey> rotateKey(Long keyId, String rotationReason, String operator) {
        try {
            EncryptionKey oldKey = encryptionKeyMapper.selectById(keyId);
            if (oldKey == null) {
                return Result.error("密钥不存在");
            }
            
            if (!KeyStatus.ACTIVE.getCode().equals(oldKey.getKeyStatus())) {
                return Result.error("只能轮换活跃的密钥");
            }
            
            // 生成新密钥
            KeyType keyType = KeyType.fromCode(oldKey.getKeyType());
            KeyUsage keyUsage = KeyUsage.fromCode(oldKey.getKeyUsage());
            
            String newKeyMaterial;
            String newKeyFingerprint;
            
            if (keyType == KeyType.SM2 || keyType == KeyType.RSA) {
                // 非对称加密，生成密钥对
                KeyGenerationService.KeyPair keyPair = keyGenerationService.generateKeyPair(keyType, keyUsage, oldKey.getKeyLength());
                newKeyMaterial = keyPair.getPrivateKey() + "|" + keyPair.getPublicKey();
                newKeyFingerprint = keyGenerationService.calculateKeyFingerprint(keyPair.getPublicKey());
            } else {
                // 对称加密，生成单个密钥
                newKeyMaterial = keyGenerationService.generateKey(keyType, keyUsage, oldKey.getKeyLength());
                newKeyFingerprint = keyGenerationService.calculateKeyFingerprint(newKeyMaterial);
            }
            
            // 加密新密钥材料
            String encryptedNewKeyMaterial = keyStorageService.encryptKeyMaterial(newKeyMaterial);
            
            // 创建新密钥
            EncryptionKey newKey = new EncryptionKey();
            newKey.setKeyName(oldKey.getKeyName() + "_ROTATED_" + System.currentTimeMillis());
            newKey.setKeyType(oldKey.getKeyType());
            newKey.setKeyLength(oldKey.getKeyLength());
            newKey.setKeyUsage(oldKey.getKeyUsage());
            newKey.setKeyStatus(KeyStatus.ACTIVE.getCode());
            newKey.setKeyFingerprint(newKeyFingerprint);
            newKey.setKeyMaterial(encryptedNewKeyMaterial);
            newKey.setCreatedBy(operator);
            newKey.setExpireTime(oldKey.getExpireTime());
            newKey.setRotationCycle(oldKey.getRotationCycle());
            newKey.setRotationCount(0);
            newKey.setDescription("轮换自密钥：" + oldKey.getKeyName());
            newKey.setDataSourceId(oldKey.getDataSourceId());
            
            // 保存新密钥
            encryptionKeyMapper.insert(newKey);
            
            // 更新旧密钥状态
            oldKey.setKeyStatus(KeyStatus.EXPIRED.getCode());
            oldKey.setLastRotationTime(LocalDateTime.now());
            oldKey.setRotationCount(oldKey.getRotationCount() + 1);
            encryptionKeyMapper.updateById(oldKey);
            
            // 记录轮换历史
            KeyRotationHistory rotationHistory = new KeyRotationHistory();
            rotationHistory.setOldKeyId(oldKey.getId());
            rotationHistory.setNewKeyId(newKey.getId());
            rotationHistory.setRotationReason(rotationReason);
            rotationHistory.setRotatedBy(operator);
            rotationHistory.setRotationStatus("SUCCESS");
            keyRotationHistoryMapper.insert(rotationHistory);
            
            // 记录使用审计
            recordKeyUsage(oldKey.getId(), "ROTATE_OLD", operator, "轮换旧密钥");
            recordKeyUsage(newKey.getId(), "ROTATE_NEW", operator, "生成新密钥");
            
            log.info("密钥轮换成功，旧密钥ID：{}，新密钥ID：{}", oldKey.getId(), newKey.getId());
            
            // 脱敏处理
            newKey.setKeyMaterial(null);
            return Result.success(newKey);
            
        } catch (Exception e) {
            log.error("密钥轮换失败，ID：{}", keyId, e);
            return Result.error("密钥轮换失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Result<Void> destroyKey(Long keyId, String operator) {
        try {
            EncryptionKey encryptionKey = encryptionKeyMapper.selectById(keyId);
            if (encryptionKey == null) {
                return Result.error("密钥不存在");
            }
            
            // 安全删除密钥材料
            keyStorageService.secureDeleteKeyMaterial(encryptionKey.getKeyMaterial());
            
            // 更新密钥状态为已销毁
            encryptionKey.setKeyStatus(KeyStatus.DESTROYED.getCode());
            encryptionKeyMapper.updateById(encryptionKey);
            
            // 记录使用审计
            recordKeyUsage(keyId, "DESTROY", operator, "销毁密钥");
            
            log.info("密钥销毁成功，ID：{}", keyId);
            
            return Result.success();
            
        } catch (Exception e) {
            log.error("销毁密钥失败，ID：{}", keyId, e);
            return Result.error("销毁密钥失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<Map<String, Object>> getKeyStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 基本统计
            statistics.put("totalKeys", encryptionKeyMapper.countAllKeys());
            statistics.put("activeKeys", encryptionKeyMapper.countActiveKeys());
            statistics.put("inactiveKeys", encryptionKeyMapper.countInactiveKeys());
            
            // 即将过期的密钥数量（30天内）
            LocalDateTime expireTime = LocalDateTime.now().plusDays(30);
            statistics.put("expiringKeys", encryptionKeyMapper.countExpiringKeys(expireTime));
            
            return Result.success(statistics);
            
        } catch (Exception e) {
            log.error("获取密钥统计信息失败", e);
            return Result.error("获取密钥统计信息失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<List<Map<String, String>>> getSupportedKeyTypes() {
        try {
            List<Map<String, String>> keyTypes = Arrays.stream(KeyType.values())
                .map(type -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("code", type.getCode());
                    map.put("description", type.getDescription());
                    return map;
                })
                .collect(Collectors.toList());
            
            return Result.success(keyTypes);
            
        } catch (Exception e) {
            log.error("获取支持的密钥类型失败", e);
            return Result.error("获取支持的密钥类型失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<Map<String, Object>> getKeyUsageStatistics(Long keyId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            // 获取基本统计
            List<Map<String, Object>> statistics = keyUsageAuditMapper.selectUsageStatistics(keyId, startTime, endTime);
            
            // 获取每日使用量
            List<Map<String, Object>> dailyUsage = keyUsageAuditMapper.selectDailyUsage(keyId, startTime, endTime);
            
            // 获取最活跃的操作员
            List<Map<String, Object>> topOperators = keyUsageAuditMapper.selectTopOperators(keyId, startTime, endTime, 10);
            
            Map<String, Object> result = new HashMap<>();
            result.put("statistics", statistics);
            result.put("dailyUsage", dailyUsage);
            result.put("topOperators", topOperators);
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("获取密钥使用统计失败，密钥ID：{}", keyId, e);
            return Result.error("获取密钥使用统计失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<String> exportKeyInfo(List<Long> keyIds) {
        // TODO: 实现Excel导出功能
        return Result.error("导出功能暂未实现");
    }
    
    /**
     * 检查密钥名称是否已存在
     */
    private boolean isKeyNameExists(String keyName) {
        try {
            // 使用现有方法查询密钥列表，然后筛选
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<EncryptionKey> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
            IPage<EncryptionKey> resultPage = encryptionKeyMapper.selectKeyPage(page, keyName, null, null, null);
            return resultPage.getRecords() != null && !resultPage.getRecords().isEmpty();
        } catch (Exception e) {
            log.error("检查密钥名称是否存在时发生错误", e);
            return true; // 发生错误时认为已存在，避免重复创建
        }
    }
    
    /**
     * 验证状态转换是否合法
     */
    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        // 已销毁的密钥不能进行任何状态转换
        if (KeyStatus.DESTROYED.getCode().equals(currentStatus)) {
            return false;
        }

        // 状态不变是允许的
        if (currentStatus.equals(newStatus)) {
            return true;
        }

        // ACTIVE 可以转换到 INACTIVE, EXPIRED, REVOKED, DESTROYED
        if (KeyStatus.ACTIVE.getCode().equals(currentStatus)) {
            return KeyStatus.INACTIVE.getCode().equals(newStatus) ||
                   KeyStatus.EXPIRED.getCode().equals(newStatus) ||
                   KeyStatus.REVOKED.getCode().equals(newStatus) ||
                   KeyStatus.DESTROYED.getCode().equals(newStatus);
        }

        // INACTIVE 可以转换到 ACTIVE, DESTROYED
        if (KeyStatus.INACTIVE.getCode().equals(currentStatus)) {
            return KeyStatus.ACTIVE.getCode().equals(newStatus) ||
                   KeyStatus.DESTROYED.getCode().equals(newStatus);
        }

        // EXPIRED 只能转换到 DESTROYED
        if (KeyStatus.EXPIRED.getCode().equals(currentStatus)) {
            return KeyStatus.DESTROYED.getCode().equals(newStatus);
        }

        // REVOKED 只能转换到 DESTROYED
        if (KeyStatus.REVOKED.getCode().equals(currentStatus)) {
            return KeyStatus.DESTROYED.getCode().equals(newStatus);
        }

        // 默认不允许转换
        return false;
    }
    
    /**
     * 记录密钥使用审计
     */
    private void recordKeyUsage(Long keyId, String operationType, String operator, String description) {
        try {
            KeyUsageAudit audit = new KeyUsageAudit();
            audit.setKeyId(keyId);
            audit.setOperationType(operationType);
            audit.setOperator(operator);
            audit.setOperationResult("SUCCESS");
            audit.setDescription(description);
            keyUsageAuditMapper.insert(audit);
        } catch (Exception e) {
            log.error("记录密钥使用审计失败", e);
        }
    }
}