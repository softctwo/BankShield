package com.bankshield.lineage.dto;

import lombok.Data;
import java.util.List;

/**
 * 质量测试结果 DTO
 *
 * @author BankShield
 * @since 2024-01-24
 */
@Data
public class QualityTestResult {
    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 质量评分
     */
    private Double qualityScore;

    /**
     * 错误数量
     */
    private Long errorCount;

    /**
     * 总数量
     */
    private Long totalCount;

    /**
     * 执行时间
     */
    private Long executionTime;

    /**
     * 错误样本
     */
    private List<String> errorSamples;

    /**
     * 消息
     */
    private String message;

    /**
     * 规则ID
     */
    private Long ruleId;

    /**
     * 测试时间
     */
    private Long testTime;

    /**
     * 样本数量
     */
    private Long sampleCount;

    /**
     * 通过样本数量
     */
    private Long passedSampleCount;

    /**
     * 失败样本数量
     */
    private Long failedSampleCount;
}