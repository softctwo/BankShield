package com.bankshield.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.dto.KeyStatisticsDTO;
import com.bankshield.api.dto.KeyUsageStatisticsDTO;
import com.bankshield.api.entity.SecurityKey;
import com.bankshield.api.mapper.SecurityKeyMapper;
import com.bankshield.api.service.AuditService;
import com.bankshield.api.service.KeyManagementService;
import com.bankshield.api.service.SecureKeyManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 密钥管理服务实现
 */
@Slf4j
@Service
public class KeyManagementServiceImpl implements KeyManagementService {

    @Autowired
    private SecurityKeyMapper securityKeyMapper;

    @Autowired
    private SecureKeyManagementService secureKeyManagementService;

    @Autowired
    private AuditService auditService;

    @Override
    public IPage<SecurityKey> getKeyPage(Page<SecurityKey> page, String keyName, String keyType, 
                                         String keyStatus, String keyUsage) {
        LambdaQueryWrapper<SecurityKey> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SecurityKey::getDeleted, 0);
        
        if (StringUtils.hasText(keyName)) {
            wrapper.like(SecurityKey::getKeyName, keyName);
        }
        if (StringUtils.hasText(keyType)) {
            wrapper.eq(SecurityKey::getKeyType, keyType);
        }
        if (StringUtils.hasText(keyStatus)) {
            wrapper.eq(SecurityKey::getStatus, Integer.parseInt(keyStatus));
        }
        if (StringUtils.hasText(keyUsage)) {
            wrapper.eq(SecurityKey::getKeyUsage, keyUsage);
        }
        
        wrapper.orderByDesc(SecurityKey::getCreateTime);
        return securityKeyMapper.selectPage(page, wrapper);
    }

    @Override
    public SecurityKey getKeyById(Long id) {
        return securityKeyMapper.selectById(id);
    }

    @Override
    @Transactional
    public SecurityKey generateKey(String keyName, String keyType, String keyUsage, 
                                  Integer keyLength, Integer expireDays, String description, String createdBy) {
        log.info("生成密钥: keyName={}, keyType={}, keyUsage={}", keyName, keyType, keyUsage);
        
        // 使用安全密钥管理服务生成密钥材料
        String encryptedKeyMaterial = secureKeyManagementService.generateEncryptionKey(keyType, keyLength).getData();
        
        // 创建密钥实体
        SecurityKey key = SecurityKey.builder()
                .keyName(keyName)
                .keyType(keyType)
                .keyUsage(keyUsage)
                .keyLength(keyLength)
                .status(1) // 1-有效
                .keyMaterial(encryptedKeyMaterial)
                .keyFingerprint(generateFingerprint(encryptedKeyMaterial))
                .createBy(createdBy)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .expireTime(LocalDateTime.now().plusDays(expireDays != null ? expireDays : 365))
                .rotationCycle(90) // 默认90天轮换周期
                .rotationCount(0)
                .description(description)
                .deleted(0)
                .build();
        
        securityKeyMapper.insert(key);
        
        // 记录审计日志
        auditService.logKeyGeneration(keyType, keyLength, "SUCCESS", "Generated key: " + keyName);
        
        log.info("密钥生成成功: id={}", key.getId());
        return key;
    }

    @Override
    @Transactional
    public SecurityKey rotateKey(Long id, String rotationReason, String operator) {
        log.info("轮换密钥: id={}, reason={}", id, rotationReason);
        
        SecurityKey oldKey = securityKeyMapper.selectById(id);
        if (oldKey == null) {
            throw new RuntimeException("密钥不存在");
        }
        
        // 生成新的密钥材料
        String newKeyMaterial = secureKeyManagementService.generateEncryptionKey(
                oldKey.getKeyType(), oldKey.getKeyLength()).getData();
        
        // 更新密钥
        oldKey.setKeyMaterial(newKeyMaterial);
        oldKey.setKeyFingerprint(generateFingerprint(newKeyMaterial));
        oldKey.setLastRotationTime(LocalDateTime.now());
        oldKey.setRotationCount(oldKey.getRotationCount() + 1);
        oldKey.setUpdateTime(LocalDateTime.now());
        
        securityKeyMapper.updateById(oldKey);
        
        // 记录审计日志
        auditService.logKeyRotation(String.valueOf(id), oldKey.getKeyName(), rotationReason, operator);
        
        log.info("密钥轮换成功: id={}, rotationCount={}", id, oldKey.getRotationCount());
        return oldKey;
    }

    @Override
    @Transactional
    public void updateKeyStatus(Long id, Integer status, String operator) {
        log.info("更新密钥状态: id={}, status={}", id, status);
        
        SecurityKey key = securityKeyMapper.selectById(id);
        if (key == null) {
            throw new RuntimeException("密钥不存在");
        }
        
        key.setStatus(status);
        key.setUpdateTime(LocalDateTime.now());
        securityKeyMapper.updateById(key);
        
        // 记录审计日志
        String statusDesc = status == 1 ? "ACTIVE" : (status == 0 ? "INACTIVE" : "EXPIRED");
        log.info("密钥状态变更: {} -> {}, operator: {}", key.getKeyName(), statusDesc, operator);
        
        log.info("密钥状态更新成功: id={}", id);
    }

    @Override
    @Transactional
    public void destroyKey(Long id, String operator) {
        log.info("销毁密钥: id={}", id);
        
        SecurityKey key = securityKeyMapper.selectById(id);
        if (key == null) {
            throw new RuntimeException("密钥不存在");
        }
        
        // 逻辑删除
        key.setDeleted(1);
        key.setStatus(0); // 0-无效
        key.setUpdateTime(LocalDateTime.now());
        securityKeyMapper.updateById(key);
        
        // 记录审计日志
        auditService.logKeyDestruction(String.valueOf(id), key.getKeyName(), operator);
        
        log.info("密钥销毁成功: id={}", id);
    }

    @Override
    public void exportKeyInfo(List<Long> keyIds, HttpServletResponse response) {
        log.info("导出密钥信息: keyIds={}", keyIds);
        
        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=keys_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv");
            
            PrintWriter writer = response.getWriter();
            writer.println("ID,密钥名称,密钥类型,密钥用途,密钥长度,状态,创建时间,过期时间");
            
            List<SecurityKey> keys;
            if (keyIds != null && !keyIds.isEmpty()) {
                keys = securityKeyMapper.selectBatchIds(keyIds);
            } else {
                LambdaQueryWrapper<SecurityKey> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SecurityKey::getDeleted, 0);
                keys = securityKeyMapper.selectList(wrapper);
            }
            
            for (SecurityKey key : keys) {
                writer.println(String.format("%d,%s,%s,%s,%d,%d,%s,%s",
                        key.getId(), key.getKeyName(), key.getKeyType(), key.getKeyUsage(),
                        key.getKeyLength(), key.getStatus(),
                        key.getCreateTime(), key.getExpireTime()));
            }
            
            writer.flush();
            log.info("密钥信息导出成功");
        } catch (IOException e) {
            log.error("导出密钥信息失败", e);
            throw new RuntimeException("导出失败", e);
        }
    }

    @Override
    public List<String> getSupportedKeyTypes() {
        return Arrays.asList("SM2", "SM3", "SM4", "AES", "RSA");
    }

    @Override
    public KeyUsageStatisticsDTO getKeyUsageStatistics(Long keyId, String startTime, String endTime) {
        log.info("获取密钥使用统计: keyId={}", keyId);
        
        SecurityKey key = securityKeyMapper.selectById(keyId);
        if (key == null) {
            throw new RuntimeException("密钥不存在");
        }
        
        // 模拟统计数据（实际应从审计日志中统计）
        return KeyUsageStatisticsDTO.builder()
                .keyId(keyId)
                .keyName(key.getKeyName())
                .totalUsageCount(1250L)
                .encryptCount(680L)
                .decryptCount(420L)
                .signCount(100L)
                .verifyCount(50L)
                .operationStats(new ArrayList<>())
                .dailyUsageStats(new ArrayList<>())
                .topOperators(new ArrayList<>())
                .build();
    }

    @Override
    public KeyStatisticsDTO getKeyStatistics() {
        log.info("获取密钥统计信息");
        
        LambdaQueryWrapper<SecurityKey> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SecurityKey::getDeleted, 0);
        
        List<SecurityKey> allKeys = securityKeyMapper.selectList(wrapper);
        
        int totalKeys = allKeys.size();
        int activeKeys = (int) allKeys.stream().filter(k -> k.getStatus() == 1).count();
        int inactiveKeys = (int) allKeys.stream().filter(k -> k.getStatus() == 0).count();
        int expiredKeys = (int) allKeys.stream().filter(k -> k.getStatus() == 2).count();
        
        // 即将过期（30天内）
        LocalDateTime thirtyDaysLater = LocalDateTime.now().plusDays(30);
        int expiringKeys = (int) allKeys.stream()
                .filter(k -> k.getExpireTime() != null && 
                        k.getExpireTime().isAfter(LocalDateTime.now()) && 
                        k.getExpireTime().isBefore(thirtyDaysLater))
                .count();
        
        // 按类型统计
        int sm2Keys = (int) allKeys.stream().filter(k -> "SM2".equals(k.getKeyType())).count();
        int sm3Keys = (int) allKeys.stream().filter(k -> "SM3".equals(k.getKeyType())).count();
        int sm4Keys = (int) allKeys.stream().filter(k -> "SM4".equals(k.getKeyType())).count();
        int aesKeys = (int) allKeys.stream().filter(k -> "AES".equals(k.getKeyType())).count();
        int rsaKeys = (int) allKeys.stream().filter(k -> "RSA".equals(k.getKeyType())).count();
        
        // 按用途统计
        int encryptKeys = (int) allKeys.stream().filter(k -> "ENCRYPT".equals(k.getKeyUsage())).count();
        int decryptKeys = (int) allKeys.stream().filter(k -> "DECRYPT".equals(k.getKeyUsage())).count();
        int signKeys = (int) allKeys.stream().filter(k -> "SIGN".equals(k.getKeyUsage())).count();
        int verifyKeys = (int) allKeys.stream().filter(k -> "VERIFY".equals(k.getKeyUsage())).count();
        
        // 本月统计
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        int monthlyNewKeys = (int) allKeys.stream()
                .filter(k -> k.getCreateTime() != null && k.getCreateTime().isAfter(monthStart))
                .count();
        int monthlyRotatedKeys = (int) allKeys.stream()
                .filter(k -> k.getLastRotationTime() != null && k.getLastRotationTime().isAfter(monthStart))
                .count();
        
        return KeyStatisticsDTO.builder()
                .totalKeys(totalKeys)
                .activeKeys(activeKeys)
                .inactiveKeys(inactiveKeys)
                .expiringKeys(expiringKeys)
                .expiredKeys(expiredKeys)
                .sm2Keys(sm2Keys)
                .sm3Keys(sm3Keys)
                .sm4Keys(sm4Keys)
                .aesKeys(aesKeys)
                .rsaKeys(rsaKeys)
                .encryptKeys(encryptKeys)
                .decryptKeys(decryptKeys)
                .signKeys(signKeys)
                .verifyKeys(verifyKeys)
                .monthlyNewKeys(monthlyNewKeys)
                .monthlyRotatedKeys(monthlyRotatedKeys)
                .build();
    }

    /**
     * 生成密钥指纹
     */
    private String generateFingerprint(String keyMaterial) {
        // 简化实现，实际应使用SHA256
        return "FP:" + Integer.toHexString(keyMaterial.hashCode()).toUpperCase();
    }
}
