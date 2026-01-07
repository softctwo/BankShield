package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI模型实体
 */
@Data
@TableName("ai_model")
public class AIModel {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型类型（ANOMALY_DETECTION/THREAT_PREDICTION/BEHAVIOR_ANALYSIS/RISK_SCORING）
     */
    private String modelType;

    /**
     * 算法类型（ISOLATION_FOREST/LSTM/XGBOOST/RANDOM_FOREST/DNN）
     */
    private String algorithm;

    /**
     * 模型版本
     */
    private String version;

    /**
     * 模型状态（TRAINING/TRAINED/DEPLOYED/DEPRECATED）
     */
    private String status;

    /**
     * 模型精度
     */
    private Double accuracy;

    /**
     * 模型召回率
     */
    private Double recall;

    /**
     * 模型F1分数
     */
    private Double f1Score;

    /**
     * 模型文件路径
     */
    private String modelPath;

    /**
     * 模型格式（ONNX/PMML/PICKLE/H5）
     */
    private String modelFormat;

    /**
     * 模型大小（字节）
     */
    private Long modelSize;

    /**
     * 训练数据量
     */
    private Long trainingDataSize;

    /**
     * 训练参数（JSON）
     */
    private String trainingParams;

    /**
     * 模型描述
     */
    private String description;

    /**
     * 使用次数
     */
    private Long usageCount;

    /**
     * 最后使用时间
     */
    private LocalDateTime lastUsedTime;

    /**
     * 创建者
     */
    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
