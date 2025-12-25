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
 * 告警记录实体类
 * 对应数据库表：alert_record
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("alert_record")
public class AlertRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 告警ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 告警规则ID
     */
    @TableField("rule_id")
    private Long ruleId;

    /**
     * 告警级别
     */
    @TableField("alert_level")
    private String alertLevel;

    /**
     * 告警标题
     */
    @TableField("alert_title")
    private String alertTitle;

    /**
     * 告警内容
     */
    @TableField("alert_content")
    private String alertContent;

    /**
     * 告警时间
     */
    @TableField("alert_time")
    private LocalDateTime alertTime;

    /**
     * 告警状态：UNRESOLVED/RESOLVED/IGNORED
     */
    @TableField("alert_status")
    private String alertStatus;

    /**
     * 处理人
     */
    private String handler;

    /**
     * 处理时间
     */
    @TableField("handle_time")
    private LocalDateTime handleTime;

    /**
     * 处理备注
     */
    @TableField("handle_remark")
    private String handleRemark;

    /**
     * 通知状态
     */
    @TableField("notify_status")
    private String notifyStatus;
}