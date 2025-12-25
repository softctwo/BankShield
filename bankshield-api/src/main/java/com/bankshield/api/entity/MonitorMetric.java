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
 * 监控指标实体类
 * 对应数据库表：monitor_metric
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("monitor_metric")
public class MonitorMetric implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 指标ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 指标名称
     */
    @TableField("metric_name")
    private String metricName;

    /**
     * 指标类型：SYSTEM/SECURITY/DATABASE/SERVICE
     */
    @TableField("metric_type")
    private String metricType;

    /**
     * 指标值
     */
    @TableField("metric_value")
    private Double metricValue;

    /**
     * 指标单位
     */
    @TableField("metric_unit")
    private String metricUnit;

    /**
     * 阈值
     */
    private Double threshold;

    /**
     * 状态：NORMAL/WARNING/CRITICAL
     */
    private String status;

    /**
     * 采集时间
     */
    @TableField("collect_time")
    private LocalDateTime collectTime;

    /**
     * 描述
     */
    private String description;

    /**
     * 关联资源
     */
    @TableField("related_resource")
    private String relatedResource;
}