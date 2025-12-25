package com.bankshield.lineage.dto;

import lombok.Data;

/**
 * 质量规则模板 DTO
 *
 * @author BankShield
 * @since 2024-01-24
 */
@Data
public class QualityRuleTemplate {
    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 规则类型
     */
    private String ruleType;

    /**
     * 描述
     */
    private String description;

    /**
     * 检查SQL模板
     */
    private String checkSqlTemplate;

    /**
     * 默认阈值
     */
    private Double defaultThreshold;

    /**
     * 默认权重
     */
    private Double defaultWeight;

    /**
     * 默认严重程度
     */
    private String defaultSeverity;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * SQL模板
     */
    private String sqlTemplate;
}