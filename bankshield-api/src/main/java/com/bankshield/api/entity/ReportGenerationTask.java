package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 报表生成任务实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("report_generation_task")
public class ReportGenerationTask {
    
    /**
     * 任务ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 模板ID
     */
    private Long templateId;
    
    /**
     * 生成状态（PENDING/RUNNING/SUCCESS/FAILED）
     */
    private String status;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 报表文件路径
     */
    private String reportFilePath;
    
    /**
     * 创建人
     */
    private String createdBy;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 报表周期
     */
    private String reportPeriod;
    
    /**
     * 报表数据（JSON格式）
     */
    private String reportData;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}