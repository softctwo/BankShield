package com.bankshield.api.dto.report;

import lombok.Data;

/**
 * 密钥管理数据DTO
 */
@Data
public class KeyManagementData {
    
    /**
     * 密钥统计
     */
    private Long totalKeyCount;
    private Long activeKeyCount;
    private Long expiredKeyCount;
    private Long keysNeedRotation;
    
    /**
     * 密钥轮换统计
     */
    private Long rotationCountInLastMonth;
    private Long rotationCountInLastQuarter;
    
    /**
     * 密钥类型分布
     */
    private Long sm2KeyCount;
    private Long sm4KeyCount;
    private Long aesKeyCount;
    private Long rsaKeyCount;
    
    /**
     * 密钥合规性
     */
    private Boolean keyManagementCompliance;
    
    /**
     * 密钥存储安全性
     */
    private Boolean keyStorageSecurity;
}