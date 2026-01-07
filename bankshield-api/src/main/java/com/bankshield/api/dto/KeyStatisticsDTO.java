package com.bankshield.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 密钥统计信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyStatisticsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总密钥数
     */
    private Integer totalKeys;

    /**
     * 活跃密钥数
     */
    private Integer activeKeys;

    /**
     * 禁用密钥数
     */
    private Integer inactiveKeys;

    /**
     * 即将过期密钥数（30天内）
     */
    private Integer expiringKeys;

    /**
     * 已过期密钥数
     */
    private Integer expiredKeys;

    /**
     * SM2密钥数
     */
    private Integer sm2Keys;

    /**
     * SM3密钥数
     */
    private Integer sm3Keys;

    /**
     * SM4密钥数
     */
    private Integer sm4Keys;

    /**
     * AES密钥数
     */
    private Integer aesKeys;

    /**
     * RSA密钥数
     */
    private Integer rsaKeys;

    /**
     * 加密用途密钥数
     */
    private Integer encryptKeys;

    /**
     * 解密用途密钥数
     */
    private Integer decryptKeys;

    /**
     * 签名用途密钥数
     */
    private Integer signKeys;

    /**
     * 验签用途密钥数
     */
    private Integer verifyKeys;

    /**
     * 本月新增密钥数
     */
    private Integer monthlyNewKeys;

    /**
     * 本月轮换密钥数
     */
    private Integer monthlyRotatedKeys;
}
