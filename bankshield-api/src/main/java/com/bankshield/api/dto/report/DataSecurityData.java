package com.bankshield.api.dto.report;

import lombok.Data;

/**
 * 数据安全数据DTO
 */
@Data
public class DataSecurityData {
    
    /**
     * 敏感数据访问统计
     */
    private Long sensitiveDataAccessCount;
    private Long maskedDataCount;
    private Long encryptedDataCount;
    
    /**
     * 数据分类统计
     */
    private Long totalDataAssets;
    private Long classifiedDataAssets;
    private Long sensitiveDataAssets;
    
    /**
     * 加密配置统计
     */
    private Long totalEncryptConfigs;
    private Long activeEncryptConfigs;
    private Long inactiveEncryptConfigs;
    
    /**
     * 数据完整性检查
     */
    private Boolean dataIntegrityCheck;
    private Double dataIntegrityScore;
    
    /**
     * 数据传输安全
     */
    private Boolean encryptedTransmission;
    private Boolean secureProtocolEnabled;
    
    /**
     * 数据备份安全
     */
    private Boolean backupEncryptionEnabled;
    private Long backupCount;
    private Long encryptedBackupCount;
}