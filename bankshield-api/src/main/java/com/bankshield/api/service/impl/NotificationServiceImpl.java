package com.bankshield.api.service.impl;

import com.bankshield.api.entity.AlertRecord;
import com.bankshield.api.entity.NotificationConfig;
import com.bankshield.api.enums.AlertLevel;
import com.bankshield.api.enums.NotifyType;
import com.bankshield.api.mapper.NotificationConfigMapper;
import com.bankshield.api.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 告警通知服务实现类
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationConfigMapper notificationConfigMapper;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private RestTemplate restTemplate;

    // 通知频率控制缓存
    private final Map<String, LocalDateTime> notificationCache = new ConcurrentHashMap<>();

    // 通知频率控制时间间隔（分钟）
    private static final Map<String, Integer> NOTIFICATION_INTERVALS = new HashMap<>();
    
    static {
        NOTIFICATION_INTERVALS.put(AlertLevel.INFO.getCode(), 30);
        NOTIFICATION_INTERVALS.put(AlertLevel.WARNING.getCode(), 15);
        NOTIFICATION_INTERVALS.put(AlertLevel.CRITICAL.getCode(), 5);
        NOTIFICATION_INTERVALS.put(AlertLevel.EMERGENCY.getCode(), 1);
    }

    @Override
    public void sendAlertNotification(AlertRecord alertRecord) {
        if (alertRecord == null) {
            log.warn("告警记录为空，无法发送通知");
            return;
        }

        try {
            log.info("开始发送告警通知: {}", alertRecord.getAlertTitle());
            
            // 根据告警级别发送通知
            sendNotificationByLevel(alertRecord);
            
            // 更新通知状态
            alertRecord.setNotifyStatus("NOTIFIED");
            log.info("告警通知发送完成: {}", alertRecord.getAlertTitle());
        } catch (Exception e) {
            log.error("发送告警通知失败: {}", alertRecord.getAlertTitle(), e);
            alertRecord.setNotifyStatus("FAILED");
        }
    }

    @Override
    public void sendNotificationByLevel(AlertRecord alertRecord) {
        String alertLevel = alertRecord.getAlertLevel();

        // 获取所有启用的通知配置，不再限制类型
        List<NotificationConfig> configs = notificationConfigMapper.selectEnabledConfigs();
        
        for (NotificationConfig config : configs) {
            try {
                if (!canSendNotification(alertLevel, config.getNotifyType())) {
                    log.info("通知频率控制，跳过发送: level={}, type={}", alertLevel, config.getNotifyType());
                    continue;
                }

                switch (config.getNotifyType()) {
                    case "EMAIL":
                        sendEmailNotification(alertRecord, config);
                        break;
                    case "SMS":
                        sendSmsNotification(alertRecord, config);
                        break;
                    case "WEBHOOK":
                        sendWebhookNotification(alertRecord, config);
                        break;
                    case "DINGTALK":
                        sendDingTalkNotification(alertRecord, config);
                        break;
                    case "WECHAT":
                        sendWeChatNotification(alertRecord, config);
                        break;
                    default:
                        log.warn("不支持的通知类型: {}", config.getNotifyType());
                }
            } catch (Exception e) {
                log.error("发送{}通知失败", config.getNotifyType(), e);
            }
        }
    }

    @Override
    public void sendEmailNotification(AlertRecord alertRecord, NotificationConfig config) {
        try {
            String[] recipients = config.getRecipients().split(",");
            String subject = renderTemplate(config.getNotifyTemplate(), alertRecord);
            String content = buildEmailContent(alertRecord);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipients);
            message.setSubject(subject);
            message.setText(content);
            message.setFrom("noreply@bankshield.com");

            mailSender.send(message);
            log.info("邮件通知发送成功: {}", alertRecord.getAlertTitle());
            
            // 更新通知缓存
            updateNotificationCache(alertRecord.getAlertLevel(), NotifyType.EMAIL.getCode());
        } catch (Exception e) {
            log.error("邮件通知发送失败: {}", alertRecord.getAlertTitle(), e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }

    @Override
    public void sendSmsNotification(AlertRecord alertRecord, NotificationConfig config) {
        // 这里简化处理，实际应该调用短信服务商API
        try {
            log.info("发送短信通知: {}", alertRecord.getAlertTitle());
            // 模拟短信发送
            TimeUnit.SECONDS.sleep(1);
            log.info("短信通知发送成功: {}", alertRecord.getAlertTitle());
            
            updateNotificationCache(alertRecord.getAlertLevel(), NotifyType.SMS.getCode());
        } catch (Exception e) {
            log.error("短信通知发送失败: {}", alertRecord.getAlertTitle(), e);
            throw new RuntimeException("短信发送失败", e);
        }
    }

    @Override
    public void sendWebhookNotification(AlertRecord alertRecord, NotificationConfig config) {
        try {
            String webhookUrl = extractWebhookUrl(config.getConfigParams());
            if (webhookUrl == null || webhookUrl.isEmpty()) {
                log.warn("Webhook URL为空");
                return;
            }

            Map<String, Object> payload = buildWebhookPayload(alertRecord);
            
            restTemplate.postForObject(webhookUrl, payload, String.class);
            log.info("Webhook通知发送成功: {}", alertRecord.getAlertTitle());
            
            updateNotificationCache(alertRecord.getAlertLevel(), NotifyType.WEBHOOK.getCode());
        } catch (Exception e) {
            log.error("Webhook通知发送失败: {}", alertRecord.getAlertTitle(), e);
            throw new RuntimeException("Webhook发送失败", e);
        }
    }

    @Override
    public void sendDingTalkNotification(AlertRecord alertRecord, NotificationConfig config) {
        try {
            log.info("发送钉钉通知: {}", alertRecord.getAlertTitle());
            // 这里应该调用钉钉机器人API
            // 简化处理，模拟发送
            TimeUnit.SECONDS.sleep(1);
            log.info("钉钉通知发送成功: {}", alertRecord.getAlertTitle());
            
            updateNotificationCache(alertRecord.getAlertLevel(), NotifyType.DINGTALK.getCode());
        } catch (Exception e) {
            log.error("钉钉通知发送失败: {}", alertRecord.getAlertTitle(), e);
            throw new RuntimeException("钉钉发送失败", e);
        }
    }

    @Override
    public void sendWeChatNotification(AlertRecord alertRecord, NotificationConfig config) {
        try {
            log.info("发送企业微信通知: {}", alertRecord.getAlertTitle());
            // 这里应该调用企业微信API
            // 简化处理，模拟发送
            TimeUnit.SECONDS.sleep(1);
            log.info("企业微信通知发送成功: {}", alertRecord.getAlertTitle());
            
            updateNotificationCache(alertRecord.getAlertLevel(), NotifyType.WECHAT.getCode());
        } catch (Exception e) {
            log.error("企业微信通知发送失败: {}", alertRecord.getAlertTitle(), e);
            throw new RuntimeException("企业微信发送失败", e);
        }
    }

    @Override
    public boolean testNotificationConfig(NotificationConfig config) {
        try {
            // 创建测试告警记录
            AlertRecord testAlert = AlertRecord.builder()
                    .alertLevel(AlertLevel.INFO.getCode())
                    .alertTitle("测试告警通知")
                    .alertContent("这是一条测试告警通知，用于验证通知配置是否有效。")
                    .alertTime(LocalDateTime.now())
                    .build();

            switch (config.getNotifyType()) {
                case "EMAIL":
                    sendEmailNotification(testAlert, config);
                    break;
                case "SMS":
                    sendSmsNotification(testAlert, config);
                    break;
                case "WEBHOOK":
                    sendWebhookNotification(testAlert, config);
                    break;
                case "DINGTALK":
                    sendDingTalkNotification(testAlert, config);
                    break;
                case "WECHAT":
                    sendWeChatNotification(testAlert, config);
                    break;
                default:
                    log.warn("不支持的通知类型: {}", config.getNotifyType());
                    return false;
            }
            
            return true;
        } catch (Exception e) {
            log.error("测试通知配置失败", e);
            return false;
        }
    }

    @Override
    public boolean canSendNotification(String alertLevel, String notifyType) {
        String key = buildNotificationCacheKey(alertLevel, notifyType);
        LocalDateTime lastSentTime = notificationCache.get(key);
        
        if (lastSentTime == null) {
            return true;
        }

        Integer intervalMinutes = NOTIFICATION_INTERVALS.get(alertLevel);
        if (intervalMinutes == null) {
            intervalMinutes = 30; // 默认30分钟
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextAllowedTime = lastSentTime.plusMinutes(intervalMinutes);
        
        return now.isAfter(nextAllowedTime);
    }

    @Override
    public String renderTemplate(String template, AlertRecord alertRecord) {
        if (template == null || alertRecord == null) {
            return "";
        }

        String rendered = template;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        // 替换模板变量
        rendered = rendered.replace("{{title}}", alertRecord.getAlertTitle() != null ? alertRecord.getAlertTitle() : "");
        rendered = rendered.replace("{{content}}", alertRecord.getAlertContent() != null ? alertRecord.getAlertContent() : "");
        rendered = rendered.replace("{{level}}", alertRecord.getAlertLevel() != null ? alertRecord.getAlertLevel() : "");
        rendered = rendered.replace("{{time}}", alertRecord.getAlertTime() != null ? alertRecord.getAlertTime().format(formatter) : "");
        
        return rendered;
    }

    /**
     * 构建邮件内容
     */
    private String buildEmailContent(AlertRecord alertRecord) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        return String.format(
            "告警标题: %s\n" +
            "告警级别: %s\n" +
            "告警时间: %s\n" +
            "告警内容: %s\n" +
            "请及时处理此告警。",
            alertRecord.getAlertTitle(),
            alertRecord.getAlertLevel(),
            alertRecord.getAlertTime().format(formatter),
            alertRecord.getAlertContent()
        );
    }

    /**
     * 构建Webhook载荷
     */
    private Map<String, Object> buildWebhookPayload(AlertRecord alertRecord) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", alertRecord.getAlertTitle());
        payload.put("content", alertRecord.getAlertContent());
        payload.put("level", alertRecord.getAlertLevel());
        payload.put("time", alertRecord.getAlertTime().toString());
        payload.put("timestamp", System.currentTimeMillis());
        return payload;
    }

    /**
     * 提取Webhook URL
     * 支持两种格式：
     * 1. JSON格式：{"url": "https://xxx.com/webhook", "secret": "xxx"}
     * 2. 直接URL字符串（兼容旧数据）
     */
    private String extractWebhookUrl(String configParams) {
        if (configParams == null || configParams.trim().isEmpty()) {
            return null;
        }

        try {
            // 尝试解析JSON
            JSONObject jsonParams = JSON.parseObject(configParams);
            if (jsonParams != null && jsonParams.containsKey("url")) {
                String url = jsonParams.getString("url");
                if (url != null && !url.trim().isEmpty()) {
                    return url.trim();
                }
            }
        } catch (Exception e) {
            // 不是JSON格式，作为普通URL处理
            log.debug("Webhook配置参数不是有效的JSON格式，作为直接URL处理");
        }

        // 如果是直接URL，检查是否为有效URL格式
        if (configParams.contains("http://") || configParams.contains("https://")) {
            return configParams.trim();
        }

        log.warn("Webhook URL格式无效: {}", configParams);
        return null;
    }

    /**
     * 构建通知缓存键
     */
    private String buildNotificationCacheKey(String alertLevel, String notifyType) {
        return alertLevel + ":" + notifyType;
    }

    /**
     * 更新通知缓存
     */
    private void updateNotificationCache(String alertLevel, String notifyType) {
        String key = buildNotificationCacheKey(alertLevel, notifyType);
        notificationCache.put(key, LocalDateTime.now());
    }
}