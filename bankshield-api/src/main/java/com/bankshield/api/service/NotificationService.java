package com.bankshield.api.service;

import com.bankshield.api.entity.AlertRecord;
import com.bankshield.api.entity.NotificationConfig;

import java.util.List;

/**
 * 告警通知服务接口
 */
public interface NotificationService {

    /**
     * 发送告警通知
     */
    void sendAlertNotification(AlertRecord alertRecord);

    /**
     * 根据告警级别发送通知
     */
    void sendNotificationByLevel(AlertRecord alertRecord);

    /**
     * 发送邮件通知
     */
    void sendEmailNotification(AlertRecord alertRecord, NotificationConfig config);

    /**
     * 发送短信通知
     */
    void sendSmsNotification(AlertRecord alertRecord, NotificationConfig config);

    /**
     * 发送Webhook通知
     */
    void sendWebhookNotification(AlertRecord alertRecord, NotificationConfig config);

    /**
     * 发送钉钉通知
     */
    void sendDingTalkNotification(AlertRecord alertRecord, NotificationConfig config);

    /**
     * 发送企业微信通知
     */
    void sendWeChatNotification(AlertRecord alertRecord, NotificationConfig config);

    /**
     * 测试通知配置
     */
    boolean testNotificationConfig(NotificationConfig config);

    /**
     * 获取通知频率控制
     */
    boolean canSendNotification(String alertLevel, String notifyType);

    /**
     * 渲染通知模板
     */
    String renderTemplate(String template, AlertRecord alertRecord);
}