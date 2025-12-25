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
 * 告警规则实体类
 * 对应数据库表：alert_rule
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("alert_rule")
public class AlertRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 规则ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 规则名称
     */
    @TableField("rule_name")
    private String ruleName;

    /**
     * 规则类型
     */
    @TableField("rule_type")
    private String ruleType;

    /**
     * 监控指标
     */
    @TableField("monitor_metric")
    private String monitorMetric;

    /**
     * 触发条件：> < =
     */
    @TableField("trigger_condition")
    private String triggerCondition;

    /**
     * 阈值
     */
    private Double threshold;

    /**
     * 告警级别：INFO/WARNING/CRITICAL/EMERGENCY
     */
    @TableField("alert_level")
    private String alertLevel;

    /**
     * 是否启用
     */
    private Integer enabled;

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

    /**
     * 描述
     */
    private String description;
}