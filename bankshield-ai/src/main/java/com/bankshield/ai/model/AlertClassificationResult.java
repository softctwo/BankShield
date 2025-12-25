package com.bankshield.ai.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 智能告警分类结果数据传输对象
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Data
@ApiModel(value = "AlertClassificationResult对象", description = "智能告警分类结果")
public class AlertClassificationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "分类结果ID")
    private Long classificationId;

    @ApiModelProperty(value = "告警ID")
    private Long alertId;

    @ApiModelProperty(value = "告警类型")
    private String alertType;

    @ApiModelProperty(value = "原始告警级别")
    private String originalLevel;

    @ApiModelProperty(value = "AI分类结果")
    private String classificationResult;

    @ApiModelProperty(value = "分类置信度")
    private Double classificationConfidence;

    @ApiModelProperty(value = "是否为真正威胁")
    private Boolean isRealThreat;

    @ApiModelProperty(value = "威胁等级：低/中/高/严重")
    private String threatLevel;

    @ApiModelProperty(value = "威胁类型")
    private String threatType;

    @ApiModelProperty(value = "分类时间")
    private LocalDateTime classificationTime;

    @ApiModelProperty(value = "分类模型")
    private String classificationModel;

    @ApiModelProperty(value = "分类特征")
    private Map<String, Object> classificationFeatures;

    @ApiModelProperty(value = "特征重要性")
    private Map<String, Double> featureImportance;

    @ApiModelProperty(value = "相似历史告警")
    private List<SimilarAlert> similarAlerts;

    @ApiModelProperty(value = "处理建议")
    private List<String> handlingSuggestions;

    @ApiModelProperty(value = "是否需要人工审核")
    private Boolean needManualReview;

    @ApiModelProperty(value = "审核优先级")
    private String reviewPriority;

    @ApiModelProperty(value = "预计误报概率")
    private Double falsePositiveProbability;

    @ApiModelProperty(value = "预计漏报概率")
    private Double falseNegativeProbability;

    @ApiModelProperty(value = "分类说明")
    private String classificationExplanation;

    @ApiModelProperty(value = "额外信息")
    private Map<String, Object> extraInfo;

    /**
     * 相似告警数据
     */
    @Data
    public static class SimilarAlert implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "告警ID")
        private Long alertId;

        @ApiModelProperty(value = "告警时间")
        private LocalDateTime alertTime;

        @ApiModelProperty(value = "告警类型")
        private String alertType;

        @ApiModelProperty(value = "相似度")
        private Double similarity;

        @ApiModelProperty(value = "处理结果")
        private String handlingResult;

        @ApiModelProperty(value = "是否为真正威胁")
        private Boolean wasRealThreat;
    }

    /**
     * 分类结果枚举
     */
    public enum ClassificationResult {
        REAL_THREAT("真正威胁"),
        FALSE_POSITIVE("误报"),
        POTENTIAL_THREAT("潜在威胁"),
        NEEDS_INVESTIGATION("需要调查"),
        LOW_RISK("低风险"),
        MEDIUM_RISK("中风险"),
        HIGH_RISK("高风险"),
        CRITICAL_RISK("严重风险");

        private final String description;

        ClassificationResult(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 威胁类型枚举
     */
    public enum ThreatType {
        MALWARE("恶意软件"),
        INTRUSION("入侵攻击"),
        DATA_LEAKAGE("数据泄露"),
        PRIVILEGE_ESCALATION("权限提升"),
        UNAUTHORIZED_ACCESS("未授权访问"),
        DDOS_ATTACK("DDoS攻击"),
        PHISHING("钓鱼攻击"),
        INSIDER_THREAT("内部威胁"),
        NETWORK_ANOMALY("网络异常"),
        SYSTEM_VULNERABILITY("系统漏洞"),
        SUSPICIOUS_BEHAVIOR("可疑行为"),
        OTHER("其他");

        private final String description;

        ThreatType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}