package com.bankshield.api.dto.report;

import lombok.Data;

import java.util.Date;

/**
 * 报表数据DTO
 */
@Data
public class ReportData {
    
    /**
     * 报表标题
     */
    private String title;
    
    /**
     * 报表周期
     */
    private String period;
    
    /**
     * 生成时间
     */
    private Date generationTime;
    
    /**
     * 合规评分
     */
    private Integer complianceScore;
    
    /**
     * 审计数据
     */
    private AuditData auditData;
    
    /**
     * 密钥管理数据
     */
    private KeyManagementData keyManagementData;
    
    /**
     * 监控数据
     */
    private MonitoringData monitoringData;
    
    /**
     * 数据安全数据
     */
    private DataSecurityData dataSecurityData;
    
    /**
     * 系统信息
     */
    private String systemName;
    private String systemVersion;
    private String organizationName;
}