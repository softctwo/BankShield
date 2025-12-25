package com.bankshield.lineage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 数据质量检查结果实体
 *
 * @author BankShield
 * @since 2024-01-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("data_quality_result")
public class DataQualityResult {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 规则ID
     */
    @TableField("rule_id")
    private Long ruleId;

    /**
     * 目标表ID
     */
    @TableField("table_id")
    private Long tableId;

    /**
     * 目标字段ID
     */
    @TableField("column_id")
    private Long columnId;

    /**
     * 质量评分
     */
    @TableField("quality_score")
    private Double qualityScore;

    /**
     * 检查结果
     */
    @TableField("check_result")
    private String checkResult;

    /**
     * 检查状态：SUCCESS/FAILURE/WARNING
     */
    @TableField("check_status")
    private String checkStatus;

    /**
     * 错误数量
     */
    @TableField("error_count")
    private Long errorCount;

    /**
     * 总数量
     */
    @TableField("total_count")
    private Long totalCount;

    /**
     * 错误样本
     */
    @TableField("error_samples")
    private String errorSamples;

    /**
     * 执行时间
     */
    @TableField("execution_time")
    private Long executionTime;

    /**
     * 检查时间
     */
    @TableField(value = "check_time", fill = FieldFill.INSERT)
    private LocalDateTime checkTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
}