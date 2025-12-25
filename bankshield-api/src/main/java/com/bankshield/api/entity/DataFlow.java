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
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 数据流关系实体类
 * 对应数据库表：data_flow
 * 用于记录详细的数据流转关系
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("data_flow")
public class DataFlow implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 源表名
     */
    @TableField("source_table")
    private String sourceTable;

    /**
     * 源字段名
     */
    @TableField("source_column")
    private String sourceColumn;

    /**
     * 目标表名
     */
    @TableField("target_table")
    private String targetTable;

    /**
     * 目标字段名
     */
    @TableField("target_column")
    private String targetColumn;

    /**
     * 流转类型: DIRECT, INDIRECT, TRANSFORMATION
     */
    @TableField("flow_type")
    private String flowType;

    /**
     * 置信度 (0-1)
     */
    private BigDecimal confidence;

    /**
     * 发现时间
     */
    @TableField("discovery_time")
    private LocalDateTime discoveryTime;

    /**
     * 最后更新时间
     */
    @TableField("last_updated")
    private LocalDateTime lastUpdated;

    /**
     * 转换逻辑描述
     */
    private String transformation;

    /**
     * 数据源ID
     */
    @TableField("data_source_id")
    private Long dataSourceId;

    /**
     * 发现方法: SQL_PARSE, LOG_ANALYSIS, METADATA, ML
     */
    @TableField("discovery_method")
    private String discoveryMethod;
}