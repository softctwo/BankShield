package com.bankshield.mpc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;

/**
 * 联合查询结果DTO
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Data
@AllArgsConstructor
public class JointQueryResult {
    
    /**
     * 查询类型
     */
    private String queryType;
    
    /**
     * 查询结果
     */
    private BigInteger result;
    
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