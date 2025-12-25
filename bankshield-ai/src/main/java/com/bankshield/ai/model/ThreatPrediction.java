package com.bankshield.ai.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 威胁预测数据传输对象
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Data
@ApiModel(value = "ThreatPrediction对象", description = "威胁预测结果")
public class ThreatPrediction implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "预测ID")
    private Long predictionId;

    @ApiModelProperty(value = "预测开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "预测结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "预测天数")
    private Integer predictionDays;

    @ApiModelProperty(value = "预测置信度")
    private Double confidence;

    @ApiModelProperty(value = "威胁列表")
    private List<Threat> threats;

    @ApiModelProperty(value = "威胁统计信息")
    private ThreatStatistics statistics;

    @ApiModelProperty(value = "建议措施")
    private List<String> recommendations;

    @ApiModelProperty(value = "额外信息")
    private Map<String, Object> extraInfo;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 威胁信息
     */
    @Data
    @ApiModel(value = "Threat对象", description = "威胁信息")
    public static class Threat implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "威胁ID")
        private String threatId;

        @ApiModelProperty(value = "威胁类型")
        private String threatType;

        @ApiModelProperty(value = "威胁等级：低/中/高/严重")
        private String threatLevel;

        @ApiModelProperty(value = "威胁描述")
        private String description;

        @ApiModelProperty(value = "预测发生时间")
        private LocalDateTime predictedTime;

        @ApiModelProperty(value = "概率")
        private Double probability;

        @ApiModelProperty(value = "影响范围")
        private String impactScope;

        @ApiModelProperty(value = "可能的影响")
        private List<String> potentialImpacts;

        @ApiModelProperty(value = "建议的防护措施")
        private List<String> recommendedMeasures;
    }

    /**
     * 威胁统计信息
     */
    @Data
    @ApiModel(value = "ThreatStatistics对象", description = "威胁统计信息")
    public static class ThreatStatistics implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "总威胁数量")
        private Integer totalThreats;

        @ApiModelProperty(value = "高风险威胁数量")
        private Integer highRiskThreats;

        @ApiModelProperty(value = "中风险威胁数量")
        private Integer mediumRiskThreats;

        @ApiModelProperty(value = "低风险威胁数量")
        private Integer lowRiskThreats;

        @ApiModelProperty(value = "按威胁类型统计")
        private Map<String, Integer> threatsByType;

        @ApiModelProperty(value = "平均威胁概率")
        private Double averageThreatProbability;

        @ApiModelProperty(value = "最高威胁概率")
        private Double maxThreatProbability;
    }
}