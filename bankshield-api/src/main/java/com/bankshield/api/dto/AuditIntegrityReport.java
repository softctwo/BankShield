package com.bankshield.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 审计完整性报告
 * 
 * @author BankShield
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditIntegrityReport {
    
    /**
     * 总审计日志数量
     */
    private Long totalAudits;
    
    /**
     * 已上链的审计日志数量
     */
    private Long anchoredAudits;
    
    /**
     * 上链率（百分比）
     */
    private Double anchoringRate;
    
    /**
     * 总区块数量
     */
    private Long totalBlocks;
    
    /**
     * 验证结果：1-完整，0-不完整
     */
    private Integer verificationResult;
    
    /**
     * 完整性问题列表
     */
    private List<String> integrityIssues;
}