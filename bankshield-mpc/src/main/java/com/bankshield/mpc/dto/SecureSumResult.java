package com.bankshield.mpc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;

/**
 * 安全求和结果DTO
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Data
@AllArgsConstructor
public class SecureSumResult {
    
    /**
     * 求和结果
     */
    private BigInteger sum;
    
    /**
     * 参与方数量
     */
    private Integer partyCount;
    
    /**
     * 执行时间（毫秒）
     */
    private Long executionTime;
    
    /**
     * 结果哈希
     */
    private String resultHash;
}