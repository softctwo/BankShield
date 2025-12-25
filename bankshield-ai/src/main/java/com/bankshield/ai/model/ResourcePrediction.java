package com.bankshield.ai.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 资源预测数据传输对象
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Data
@ApiModel(value = "ResourcePrediction对象", description = "资源预测结果")
public class ResourcePrediction implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "预测ID")
    private Long predictionId;

    @ApiModelProperty(value = "资源类型：CPU/内存/磁盘/网络")
    private String resourceType;

    @ApiModelProperty(value = "预测类型：usage/trend/alert")
    private String predictionType;

    @ApiModelProperty(value = "预测开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "预测结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "预测时间间隔（小时）")
    private Integer intervalHours;

    @ApiModelProperty(value = "预测置信度")
    private Double confidence;

    @ApiModelProperty(value = "当前值")
    private Double currentValue;

    @ApiModelProperty(value = "预测值列表")
    private List<PredictionPoint> predictedValues;

    @ApiModelProperty(value = "历史平均值")
    private Double historicalAverage;

    @ApiModelProperty(value = "预测趋势：上升/下降/平稳")
    private String trend;

    @ApiModelProperty(value = "风险等级：低/中/高")
    private String riskLevel;

    @ApiModelProperty(value = "告警阈值")
    private Double alertThreshold;

    @ApiModelProperty(value = "严重阈值")
    private Double criticalThreshold;

    @ApiModelProperty(value = "是否需要告警")
    private Boolean needAlert;

    @ApiModelProperty(value = "告警信息")
    private String alertMessage;

    @ApiModelProperty(value = "建议措施")
    private List<String> recommendations;

    @ApiModelProperty(value = "额外信息")
    private Map<String, Object> extraInfo;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 预测点数据
     */
    @Data
    public static class PredictionPoint implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "时间点")
        private LocalDateTime timestamp;

        @ApiModelProperty(value = "预测值")
        private Double predictedValue;

        @ApiModelProperty(value = "置信区间下限")
        private Double confidenceLower;

        @ApiModelProperty(value = "置信区间上限")
        private Double confidenceUpper;

        @ApiModelProperty(value = "概率分布")
        private Map<String, Double> probabilityDistribution;
    }
}