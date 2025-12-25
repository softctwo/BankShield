package com.bankshield.ai.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 异常检测结果数据传输对象
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Data
@ApiModel(value = "AnomalyResult对象", description = "异常检测结果")
public class AnomalyResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "检测结果ID")
    private Long resultId;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "会话ID")
    private String sessionId;

    @ApiModelProperty(value = "行为类型")
    private String behaviorType;

    @ApiModelProperty(value = "异常分数")
    private Double anomalyScore;

    @ApiModelProperty(value = "是否异常")
    private Boolean isAnomaly;

    @ApiModelProperty(value = "异常等级：低/中/高")
    private String anomalyLevel;

    @ApiModelProperty(value = "异常阈值")
    private Double anomalyThreshold;

    @ApiModelProperty(value = "检测时间")
    private LocalDateTime detectionTime;

    @ApiModelProperty(value = "检测模型")
    private String detectionModel;

    @ApiModelProperty(value = "特征向量")
    private List<Double> featureVector;

    @ApiModelProperty(value = "特征重要性")
    private Map<String, Double> featureImportance;

    @ApiModelProperty(value = "异常类型列表")
    private List<String> anomalyTypes;

    @ApiModelProperty(value = "异常描述")
    private String anomalyDescription;

    @ApiModelProperty(value = "建议措施")
    private List<String> recommendations;

    @ApiModelProperty(value = "风险评分")
    private Double riskScore;

    @ApiModelProperty(value = "置信度")
    private Double confidence;

    @ApiModelProperty(value = "是否需要告警")
    private Boolean needAlert;

    @ApiModelProperty(value = "告警信息")
    private String alertMessage;

    @ApiModelProperty(value = "IP地址")
    private String ipAddress;

    @ApiModelProperty(value = "地理位置")
    private String location;

    @ApiModelProperty(value = "额外信息")
    private Map<String, Object> extraInfo;

    /**
     * 异常类型枚举
     */
    public enum AnomalyType {
        LOGIN_TIME_ANOMALY("登录时间异常"),
        LOCATION_ANOMALY("地理位置异常"),
        FREQUENCY_ANOMALY("操作频率异常"),
        PERMISSION_ANOMALY("权限使用异常"),
        DATA_ACCESS_ANOMALY("数据访问异常"),
        BEHAVIOR_PATTERN_ANOMALY("行为模式异常"),
        DEVICE_ANOMALY("设备异常"),
        NETWORK_ANOMALY("网络异常");

        private final String description;

        AnomalyType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 异常等级枚举
     */
    public enum AnomalyLevel {
        LOW("低", 0.3),
        MEDIUM("中", 0.6),
        HIGH("高", 0.9);

        private final String description;
        private final double threshold;

        AnomalyLevel(String description, double threshold) {
            this.description = description;
            this.threshold = threshold;
        }

        public String getDescription() {
            return description;
        }

        public double getThreshold() {
            return threshold;
        }

        public static AnomalyLevel fromScore(double score) {
            if (score >= HIGH.threshold) {
                return HIGH;
            } else if (score >= MEDIUM.threshold) {
                return MEDIUM;
            } else {
                return LOW;
            }
        }
    }
}