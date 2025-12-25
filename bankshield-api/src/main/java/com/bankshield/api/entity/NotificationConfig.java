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
 * 通知配置实体类
 * 对应数据库表：notification_config
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("notification_config")
public class NotificationConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知配置ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 通知类型：EMAIL/SMS/WEBHOOK/DINGTALK/WECHAT
     */
    @TableField("notify_type")
    private String notifyType;

    /**
     * 接收人
     */
    private String recipients;

    /**
     * 通知模板
     */
    @TableField("notify_template")
    private String notifyTemplate;

    /**
     * 是否启用
     */
    private Integer enabled;

    /**
     * 配置参数（JSON格式）
     */
    @TableField("config_params")
    private String configParams;

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