package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * MPC多方计算任务实体
 */
@Data
@TableName("mpc_job")
public class MpcJob {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务名称
     */
    private String jobName;

    /**
     * 任务类型（PSI/SECURE_SUM/JOINT_QUERY/SECURE_COMPARE）
     */
    private String jobType;

    /**
     * 任务状态（PENDING/RUNNING/COMPLETED/FAILED/CANCELLED）
     */
    private String status;

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
     * 任务参数（JSON）
     */
    private String jobParams;

    /**
     * 结果摘要
     */
    private String resultSummary;

    /**
     * 结果数据路径
     */
    private String resultPath;

    /**
     * 安全协议
     */
    private String protocol;

    /**
     * 安全级别
     */
    private String securityLevel;

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
