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
 * AI模型实体类
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ai_model")
@ApiModel(value = "AiModel对象", description = "AI模型表")
public class AiModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "模型名称")
    @TableField("model_name")
    private String modelName;

    @ApiModelProperty(value = "模型类型：异常检测/告警分类/预测")
    @TableField("model_type")
    private String modelType;

    @ApiModelProperty(value = "模型子类型")
    @TableField("model_subtype")
    private String modelSubtype;

    @ApiModelProperty(value = "模型文件路径")
    @TableField("model_path")
    private String modelPath;

    @ApiModelProperty(value = "模型版本")
    @TableField("model_version")
    private String modelVersion;

    @ApiModelProperty(value = "准确率")
    @TableField("accuracy")
    private Double accuracy;

    @ApiModelProperty(value = "精确率")
    @TableField("precision_rate")
    private Double precisionRate;

    @ApiModelProperty(value = "召回率")
    @TableField("recall_rate")
    private Double recallRate;

    @ApiModelProperty(value = "F1分数")
    @TableField("f1_score")
    private Double f1Score;

    @ApiModelProperty(value = "训练样本数量")
    @TableField("training_samples")
    private Integer trainingSamples;

    @ApiModelProperty(value = "特征数量")
    @TableField("feature_count")
    private Integer featureCount;

    @ApiModelProperty(value = "算法名称")
    @TableField("algorithm")
    private String algorithm;

    @ApiModelProperty(value = "超参数")
    @TableField("hyperparameters")
    private String hyperparameters;

    @ApiModelProperty(value = "模型状态：active/inactive/training")
    @TableField("status")
    private String status;

    @ApiModelProperty(value = "模型描述")
    @TableField("description")
    private String description;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "训练完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("training_time")
    private LocalDateTime trainingTime;

    @ApiModelProperty(value = "最后使用时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("last_used_time")
    private LocalDateTime lastUsedTime;

    @ApiModelProperty(value = "使用次数")
    @TableField("usage_count")
    private Integer usageCount;

    @ApiModelProperty(value = "是否删除")
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
}