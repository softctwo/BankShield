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
 * 影响分析结果实体类
 * 对应数据库表：data_impact_analysis
 * 用于记录数据变更的影响分析结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("data_impact_analysis")
public class DataImpactAnalysis implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分析名称
     */
    private String analysisName;

    /**
     * 分析类型: COLUMN_CHANGE, TABLE_CHANGE, SCHEMA_CHANGE
     */
    private String analysisType;

    /**
     * 影响对象类型: TABLE, COLUMN, SCHEMA
     */
    @TableField("impact_object_type")
    private String impactObjectType;

    /**
     * 影响对象名称
     */
    @TableField("impact_object_name")
    private String impactObjectName;

    /**
     * 分析目标（JSON格式）
     */
    @TableField("analysis_target")
    private String analysisTarget;

    /**
     * 影响范围（JSON格式）
     */
    @TableField("impact_scope")
    private String impactScope;

    /**
     * 影响路径数量
     */
    @TableField("impact_path_count")
    private Integer impactPathCount;

    /**
     * 影响资产数量
     */
    @TableField("impact_asset_count")
    private Integer impactAssetCount;

    /**
     * 风险等级: HIGH, MEDIUM, LOW
     */
    @TableField("risk_level")
    private String riskLevel;

    /**
     * 分析状态: COMPLETED, FAILED, RUNNING
     */
    private String status;

    /**
     * 分析结果详情（JSON格式）
     */
    private String result;

    /**
     * 分析报告文件路径
     */
    @TableField("report_path")
    private String reportPath;

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
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 创建人ID
     */
    @TableField("create_by")
    private Long createBy;
}