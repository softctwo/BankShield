package com.bankshield.api.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 合规统计数据DTO
 */
@Data
public class ComplianceStatisticsDTO {
    
    private Integer totalRules;
    
    private Integer complianceScore;
    
    private Integer passedChecks;
    
    private Integer criticalRiskCount;
    
    private List<Map<String, Object>> complianceTrend;
    
    private List<Map<String, Object>> ruleCategoryDistribution;
    
    private List<Map<String, Object>> riskLevelDistribution;
    
    private List<Map<String, Object>> remediationProgress;
    
    private List<Map<String, Object>> taskStatusStats;
    
    private List<Map<String, Object>> recentTasks;
    
    private List<Map<String, Object>> criticalRiskList;
}
