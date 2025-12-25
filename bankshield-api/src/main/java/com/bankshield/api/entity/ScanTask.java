package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 资产扫描任务实体类
 * 对应数据库表：scan_task
 * 用于管理数据资产的扫描任务
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("scan_task")
public class ScanTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 数据源ID
     */
    @TableField("data_source_id")
    private Long dataSourceId;

    /**
     * 扫描类型: FULL, INCREMENTAL
     */
    private String scanType;

    /**
     * 扫描配置(JSON)
     */
    private String scanConfig;

    /**
     * 任务状态: 0:待执行, 1:执行中, 2:完成, 3:失败
     */
    private Integer taskStatus;

    /**
     * 进度百分比
     */
    private Integer progressPercent;

    /**
     * 开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}