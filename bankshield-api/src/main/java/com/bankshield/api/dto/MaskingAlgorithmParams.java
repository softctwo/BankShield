package com.bankshield.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 脱敏算法参数DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaskingAlgorithmParams {
    
    /**
     * 保留前缀长度（部分掩码用）
     */
    private Integer keepPrefix;
    
    /**
     * 保留后缀长度（部分掩码用）
     */
    private Integer keepSuffix;
    
    /**
     * 掩码字符（部分掩码用）
     */
    private String maskChar;
    
    /**
     * 哈希算法（哈希算法用）：SHA256/SM3
     */
    private String hashAlgorithm;
    
    /**
     * 加密密钥ID（加密算法用）
     */
    private String encryptKeyId;
    
    /**
     * 格式保留长度（格式保留加密用）
     */
    private Integer formatPreserveLength;
    
    /**
     * 掩码长度（可选，默认为自动计算）
     */
    private Integer maskLength;
}