package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 联邦学习训练轮次记录
 */
@Data
@TableName("federated_round")
public class FederatedRound {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联任务ID
     */
    private Long jobId;

    /**
     * 轮次编号
     */
    private Integer roundNumber;

    /**
     * 轮次状态（TRAINING/UPLOADING/AGGREGATING/COMPLETED/FAILED）
     */
    private String status;

    /**
     * 参与方数量
     */
    private Integer participantCount;

    /**
     * 已完成参与方数量
     */
    private Integer completedCount;

    /**
     * 本轮精度
     */
    private Double accuracy;

    /**
     * 本轮损失
     */
    private Double loss;

    /**
     * 本轮指标（JSON）
     */
    private String metrics;

    /**
     * 聚合模型路径
     */
    private String aggregatedModelPath;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 耗时（秒）
     */
    private Long duration;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
