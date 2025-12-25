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
 * 数据血缘自动发现任务实体类
 * 对应数据库表：data_lineage_discovery_task
 * 用于管理数据血缘的自动发现任务
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("data_lineage_discovery_task")
public class DataLineageAutoDiscovery implements Serializable {

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
     * 发现策略: SQL_PARSE, LOG_ANALYSIS, METADATA_CRAWL, ML_INFERENCE
     */
    private String discoveryStrategy;

    /**
     * 任务状态: RUNNING, SUCCESS, FAILED, PENDING
     */
    private String status;

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
     * 发现的血缘关系数量
     */
    @TableField("discovered_flows_count")
    private Integer discoveredFlowsCount;

    /**
     * 任务配置(JSON格式)
     */
    private String config;

    /**
     * 错误信息
     */
    @TableField("error_message")
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