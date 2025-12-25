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
 * 安全事件实体类
 * 对应数据库表：security_event
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("security_event")
public class SecurityEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 事件类型：ATTACK/VULNERABILITY/ANOMALY/POLICY_VIOLATION
     */
    @TableField("event_type")
    private String eventType;

    /**
     * 风险级别：HIGH/MEDIUM/LOW
     */
    @TableField("risk_level")
    private String riskLevel;

    /**
     * 事件来源
     */
    @TableField("event_source")
    private String eventSource;

    /**
     * 事件标题
     */
    @TableField("event_title")
    private String eventTitle;

    /**
     * 事件详情
     */
    @TableField("event_detail")
    private String eventDetail;

    /**
     * 发生时间
     */
    @TableField("occur_time")
    private LocalDateTime occurTime;

    /**
     * 发现时间
     */
    @TableField("discover_time")
    private LocalDateTime discoverTime;

    /**
     * 处理状态：UNPROCESSED/PROCESSING/RESOLVED/IGNORED
     */
    @TableField("process_status")
    private String processStatus;

    /**
     * 处理人
     */
    @TableField("handler")
    private String handler;

    /**
     * 处理时间
     */
    @TableField("process_time")
    private LocalDateTime processTime;

    /**
     * 处理备注
     */
    @TableField("process_remark")
    private String processRemark;

    /**
     * 影响资产ID
     */
    @TableField("asset_id")
    private Long assetId;

    /**
     * 影响资产名称
     */
    @TableField("asset_name")
    private String assetName;

    /**
     * 威胁指标
     */
    @TableField("threat_indicators")
    private String threatIndicators;

    /**
     * 地理位置
     */
    @TableField("location")
    private String location;

    /**
     * IP地址
     */
    @TableField("ip_address")
    private String ipAddress;
}