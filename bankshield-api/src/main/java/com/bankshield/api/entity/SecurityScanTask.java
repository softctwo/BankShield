package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 安全扫描任务实体类
 * @author BankShield
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("security_scan_task")
public class SecurityScanTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 扫描类型：VULNERABILITY/CONFIG/WEAK_PASSWORD/ANOMALY/ALL
     */
    private String scanType;

    /**
     * 扫描目标（JSON格式存储多个目标）
     */
    private String scanTarget;

    /**
     * 执行状态：PENDING/RUNNING/SUCCESS/FAILED/PARTIAL
     */
    private String status;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 发现风险数
     */
    private Integer riskCount;

    /**
     * 扫描报告路径
     */
    private String reportPath;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 扫描进度（0-100）
     */
    private Integer progress;

    /**
     * 错误信息
     */
    private String errorMessage;
}