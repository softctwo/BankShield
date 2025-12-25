package com.bankshield.mpc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

/**
 * 隐私求交结果DTO
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Data
@AllArgsConstructor
public class PsiResult {
    
    /**
     * 交集大小
     */
    private Integer intersectionSize;
    
    /**
     * 交集数据（可选，根据配置返回）
     */
    private Set<String> intersection;
    
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
    
    public PsiResult(Integer intersectionSize, Set<String> intersection) {
        this.intersectionSize = intersectionSize;
        this.intersection = intersection;
    }
}