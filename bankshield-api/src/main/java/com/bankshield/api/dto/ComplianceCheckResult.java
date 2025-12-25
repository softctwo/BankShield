package com.bankshield.api.dto;

import com.bankshield.api.entity.ComplianceCheckItem;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 合规检查结果DTO
 */
@Data
public class ComplianceCheckResult {
    
    /**
     * 合规标准
     */
    private String standard;
    
    /**
     * 检查时间
     */
    private LocalDateTime checkTime;
    
    /**
     * 合规评分（0-100）
     */
    private Integer complianceScore;
    
    /**
     * 通过数量
     */
    private Integer passCount;
    
    /**
     * 失败数量
     */
    private Integer failCount;
    
    /**
     * 总检查项数量
     */
    private Integer totalCount;
    
    /**
     * 不合规项列表
     */
    private List<ComplianceCheckItem> nonCompliances;
    
    /**
     * 所有检查项
     */
    private List<ComplianceCheckItem> allItems;
    
    public ComplianceCheckResult() {
        this.nonCompliances = new ArrayList<>();
        this.allItems = new ArrayList<>();
        this.passCount = 0;
        this.failCount = 0;
        this.totalCount = 0;
        this.complianceScore = 0;
    }
    
    /**
     * 添加不合规项
     */
    public void addNonCompliance(ComplianceCheckItem item) {
        this.nonCompliances.add(item);
    }
    
    /**
     * 计算合规评分
     */
    public void calculateScore() {
        this.totalCount = allItems.size();
        this.passCount = (int) allItems.stream()
                .filter(item -> "PASS".equals(item.getPassStatus()))
                .count();
        this.failCount = (int) allItems.stream()
                .filter(item -> "FAIL".equals(item.getPassStatus()))
                .count();
        
        if (totalCount > 0) {
            this.complianceScore = (int) ((passCount * 100.0) / totalCount);
        } else {
            this.complianceScore = 0;
        }
    }
}