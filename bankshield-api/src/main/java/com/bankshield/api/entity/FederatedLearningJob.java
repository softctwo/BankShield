package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 联邦学习任务实体
 */
@Data
@TableName("federated_learning_job")
public class FederatedLearningJob {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务名称
     */
    private String jobName;

    /**
     * 任务类型（HORIZONTAL_FL/VERTICAL_FL/TRANSFER_FL）
     * - HORIZONTAL_FL: 横向联邦学习（样本对齐，特征相同）
     * - VERTICAL_FL: 纵向联邦学习（特征对齐，样本相同）
     * - TRANSFER_FL: 迁移联邦学习
     */
    private String jobType;

    /**
     * 模型类型（LR/DNN/GBDT/TREE）
     */
    private String modelType;

    /**
     * 任务状态（PENDING/INITIALIZING/TRAINING/AGGREGATING/COMPLETED/FAILED）
     */
    private String status;

    /**
     * 当前轮次
     */
    private Integer currentRound;

    /**
     * 总轮次
     */
    private Integer totalRounds;

    /**
     * 进度（0-100）
     */
    private Integer progress;

    /**
     * 发起方ID
     */
    private String initiatorId;

    /**
     * 参与方ID列表（JSON数组）
     */
    private String participantIds;

    /**
     * 聚合策略（FED_AVG/FED_PROX/SCAFFOLD）
     */
    private String aggregationStrategy;

    /**
     * 差分隐私参数（JSON）
     */
    private String privacyParams;

    /**
     * 训练参数（JSON）
     */
    private String trainingParams;

    /**
     * 全局模型路径
     */
    private String globalModelPath;

    /**
     * 当前模型精度
     */
    private Double accuracy;

    /**
     * 当前模型损失
     */
    private Double loss;

    /**
     * 模型指标（JSON）
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
     * 描述
     */
    private String description;

    /**
     * 创建者
     */
    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
