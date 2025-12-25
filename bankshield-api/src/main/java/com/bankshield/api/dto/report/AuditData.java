package com.bankshield.api.dto.report;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 审计数据DTO
 */
@Data
public class AuditData {
    
    /**
     * 操作审计统计
     */
    private Long totalOperationCount;
    private Long failedOperationCount;
    private List<Map<String, Object>> topOperationUsers;
    
    /**
     * 登录审计统计
     */
    private Long totalLoginCount;
    private Long failedLoginCount;
    private List<Map<String, Object>> topLoginIps;
    
    /**
     * 数据访问审计统计
     */
    private Long totalDataAccessCount;
    private Long sensitiveDataAccessCount;
    private Long maskedDataCount;
    
    /**
     * 审计日志完整性
     */
    private Double auditLogIntegrity;
    
    /**
     * 审计日志存储天数
     */
    private Integer auditLogRetentionDays;
}