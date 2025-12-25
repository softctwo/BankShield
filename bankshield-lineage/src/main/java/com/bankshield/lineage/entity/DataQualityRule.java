package com.bankshield.lineage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 数据质量规则实体
 *
 * @author BankShield
 * @since 2024-01-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("data_quality_rule")
public class DataQualityRule {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 规则名称
     */
    @TableField("rule_name")
    private String ruleName;

    /**
     * 规则类型：COMPLETENESS/ACCURACY/CONSISTENCY/TIMELINESS/UNIQUENESS/VALIDITY
     */
    @TableField("rule_type")
    private String ruleType;

    /**
     * 规则描述
     */
    @TableField("description")
    private String description;

    /**
     * 检查SQL
     */
    @TableField("check_sql")
    private String checkSql;

    /**
     * 阈值（百分比）
     */
    @TableField("threshold")
    private Double threshold;

    /**
     * 权重
     */
    @TableField("weight")
    private Double weight;

    /**
     * 严重程度：HIGH/MEDIUM/LOW
     */
    @TableField("severity")
    private String severity;

    /**
     * 目标表ID
     */
    @TableField("target_table_id")
    private Long targetTableId;

    /**
     * 目标字段ID
     */
    @TableField("target_column_id")
    private Long targetColumnId;

    /**
     * 是否启用
     */
    @TableField("enabled")
    private Boolean enabled;

    /**
     * 执行频率（cron表达式）
     */
    @TableField("execution_cron")
    private String executionCron;

    /**
     * 最后执行时间
     */
    @TableField("last_execution_time")
    private LocalDateTime lastExecutionTime;

    /**
     * 最后执行结果
     */
    @TableField("last_execution_result")
    private String lastExecutionResult;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 更新人
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;

    /**
     * 版本号
     */
    @Version
    @TableField("version")
    private Integer version;
}