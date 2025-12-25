package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 水印任务实体类
 * 对应数据库表：watermark_task
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("watermark_task")
public class WatermarkTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务类型: FILE/DATABASE
     */
    private String taskType;

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 数据源ID
     */
    private Long dataSourceId;

    /**
     * 任务状态: PENDING/RUNNING/SUCCESS/FAILED
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
     * 处理记录数
     */
    private Long processCount;

    /**
     * 输出文件路径
     */
    private String outputFilePath;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 错误信息
     */
    private String errorMessage;
}