package com.bankshield.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审计完整性统计数据
 * 
 * @author BankShield
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditIntegrityStatistics {
    
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
     * 最新区块号
     */
    private Long latestBlockNumber;
    
    /**
     * 最新区块时间
     */
    private String latestBlockTime;
}