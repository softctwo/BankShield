package com.bankshield.lineage.dto;

import lombok.Data;

/**
 * 质量统计信息 DTO
 *
 * @author BankShield
 * @since 2024-01-24
 */
@Data
public class QualityStatistics {
    /**
     * 总检查次数
     */
    private Long totalChecks;

    /**
     * 平均质量评分
     */
    private Double averageScore;

    /**
     * 最高质量评分
     */
    private Double maxScore;

    /**
     * 最低质量评分
     */
    private Double minScore;

    /**
     * 通过次数
     */
    private Long passCount;

    /**
     * 失败次数
     */
    private Long failCount;

    /**
     * 警告次数
     */
    private Long warningCount;

    /**
     * 通过率
     */
    private Double passRate;

    /**
     * 通过检查次数
     */
    private Long passedChecks;

    /**
     * 失败检查次数
     */
    private Long failedChecks;

    /**
     * 最后检查时间
     */
    private Long lastCheckTime;
}