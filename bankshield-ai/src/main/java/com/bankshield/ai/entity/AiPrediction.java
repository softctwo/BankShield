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
 * AI预测结果实体类
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ai_prediction")
@ApiModel(value = "AiPrediction对象", description = "AI预测结果表")
public class AiPrediction implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "模型ID")
    @TableField("model_id")
    private Long modelId;

    @ApiModelProperty(value = "模型名称")
    @TableField("model_name")
    private String modelName;

    @ApiModelProperty(value = "预测类型：异常检测/告警分类/资源预测")
    @TableField("prediction_type")
    private String predictionType;

    @ApiModelProperty(value = "输入数据")
    @TableField("input_data")
    private String inputData;

    @ApiModelProperty(value = "预测结果")
    @TableField("prediction_result")
    private String predictionResult;

    @ApiModelProperty(value = "预测概率/置信度")
    @TableField("confidence")
    private Double confidence;

    @ApiModelProperty(value = "预测分数")
    @TableField("prediction_score")
    private Double predictionScore;

    @ApiModelProperty(value = "预测标签")
    @TableField("prediction_label")
    private String predictionLabel;

    @ApiModelProperty(value = "用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "资源类型（资源预测时使用）")
    @TableField("resource_type")
    private String resourceType;

    @ApiModelProperty(value = "预测时间范围（资源预测时使用）")
    @TableField("prediction_range")
    private String predictionRange;

    @ApiModelProperty(value = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("start_time")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("end_time")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "预测时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("prediction_time")
    private LocalDateTime predictionTime;

    @ApiModelProperty(value = "是否准确")
    @TableField("is_accurate")
    private Boolean isAccurate;

    @ApiModelProperty(value = "验证时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("validation_time")
    private LocalDateTime validationTime;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}