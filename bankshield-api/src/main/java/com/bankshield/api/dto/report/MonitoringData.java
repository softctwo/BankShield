package com.bankshield.api.dto.report;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 监控数据DTO
 */
@Data
public class MonitoringData {
    
    /**
     * 告警统计
     */
    private Long totalAlertCount;
    private Long unresolvedAlertCount;
    private Long criticalAlertCount;
    private Long warningAlertCount;
    private Long infoAlertCount;
    
    /**
     * 告警趋势（每日告警数量）
     */
    private List<Map<String, Object>> alertTrend;
    
    /**
     * 系统监控指标
     */
    private Double cpuUsage;
    private Double memoryUsage;
    private Double diskUsage;
    private Double networkUsage;
    
    /**
     * 安全事件统计
     */
    private Long totalSecurityEvents;
    private Long highRiskEvents;
    private Long mediumRiskEvents;
    private Long lowRiskEvents;
    
    /**
     * 监控覆盖率
     */
    private Double monitoringCoverage;
}