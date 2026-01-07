package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI训练任务实体
 */
@Data
@TableName("ai_training_job")
public class AITrainingJob {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务名称
     */
    private String jobName;

    /**
     * 关联模型ID
     */
    private Long modelId;

    /**
     * 模型类型
     */
    private String modelType;

    /**
     * 算法类型
     */
    private String algorithm;

    /**
     * 任务状态（PENDING/RUNNING/COMPLETED/FAILED/CANCELLED）
     */
    private String status;

    /**
     * 进度（0-100）
     */
    private Integer progress;

    /**
     * 训练数据源
     */
    private String dataSource;

    /**
     * 训练数据量
     */
    private Long dataSize;

    /**
     * 训练参数（JSON）
     */
    private String trainingParams;

    /**
     * 训练结果指标（JSON）
     */
    private String metrics;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 创建者
     */
    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
