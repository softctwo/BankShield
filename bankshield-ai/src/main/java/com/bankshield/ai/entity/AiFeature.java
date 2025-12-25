package com.bankshield.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI特征实体类
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ai_feature")
@ApiModel(value = "AiFeature对象", description = "AI特征表")
public class AiFeature implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "特征向量JSON")
    @TableField("feature_vector")
    private String featureVector;

    @ApiModelProperty(value = "行为类型")
    @TableField("behavior_type")
    private String behaviorType;

    @ApiModelProperty(value = "用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "会话ID")
    @TableField("session_id")
    private String sessionId;

    @ApiModelProperty(value = "IP地址")
    @TableField("ip_address")
    private String ipAddress;

    @ApiModelProperty(value = "地理位置")
    @TableField("location")
    private String location;

    @ApiModelProperty(value = "异常分数")
    @TableField("anomaly_score")
    private Double anomalyScore;

    @ApiModelProperty(value = "是否异常")
    @TableField("is_anomaly")
    private Boolean isAnomaly;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}