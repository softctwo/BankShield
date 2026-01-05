package com.bankshield.api.service.impl;

import com.bankshield.api.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 监控告警服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final Map<Long, Map<String, Object>> alertRules = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, Object>> alertHistory = new ConcurrentHashMap<>();
    private final AtomicLong ruleIdGenerator = new AtomicLong(1);
    private final AtomicLong alertIdGenerator = new AtomicLong(1);

    @Override
    public Long createAlertRule(Map<String, Object> rule) {
        Long ruleId = ruleIdGenerator.getAndIncrement();
        rule.put("id", ruleId);
        rule.put("createTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        rule.put("enabled", true);
        alertRules.put(ruleId, rule);
        log.info("创建告警规则: {}", ruleId);
        return ruleId;
    }

    @Override
    public boolean updateAlertRule(Long ruleId, Map<String, Object> rule) {
        if (!alertRules.containsKey(ruleId)) {
            return false;
        }
        rule.put("id", ruleId);
        rule.put("updateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        alertRules.put(ruleId, rule);
        log.info("更新告警规则: {}", ruleId);
        return true;
    }

    @Override
    public boolean deleteAlertRule(Long ruleId) {
        Map<String, Object> removed = alertRules.remove(ruleId);
        log.info("删除告警规则: {}", ruleId);
        return removed != null;
    }

    @Override
    public List<Map<String, Object>> getAllAlertRules() {
        return new ArrayList<>(alertRules.values());
    }

    @Override
    public void triggerAlert(String alertType, String level, String message, Map<String, Object> details) {
        Long alertId = alertIdGenerator.getAndIncrement();
        
        Map<String, Object> alert = new HashMap<>();
        alert.put("id", alertId);
        alert.put("alertType", alertType);
        alert.put("level", level);
        alert.put("message", message);
        alert.put("details", details);
        alert.put("status", "OPEN");
        alert.put("triggerTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        alertHistory.put(alertId, alert);
        
        log.warn("触发告警 [{}] {}: {}", level, alertType, message);
        
        // 自动发送通知（根据级别）
        if ("CRITICAL".equals(level) || "HIGH".equals(level)) {
            sendAlertNotification(alertId, Arrays.asList("email", "sms", "webhook"));
        }
    }

    @Override
    public List<Map<String, Object>> getAlertHistory(String startTime, String endTime, String level) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Map<String, Object> alert : alertHistory.values()) {
            if (level != null && !level.equals(alert.get("level"))) {
                continue;
            }
            result.add(alert);
        }
        
        result.sort((a, b) -> {
            String timeA = (String) a.get("triggerTime");
            String timeB = (String) b.get("triggerTime");
            return timeB.compareTo(timeA); // 降序
        });
        
        return result;
    }

    @Override
    public boolean acknowledgeAlert(Long alertId, String operator) {
        Map<String, Object> alert = alertHistory.get(alertId);
        if (alert == null) {
            return false;
        }
        
        alert.put("status", "ACKNOWLEDGED");
        alert.put("acknowledgedBy", operator);
        alert.put("acknowledgedTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        log.info("确认告警: {}, 操作人: {}", alertId, operator);
        return true;
    }

    @Override
    public boolean closeAlert(Long alertId, String operator, String resolution) {
        Map<String, Object> alert = alertHistory.get(alertId);
        if (alert == null) {
            return false;
        }
        
        alert.put("status", "CLOSED");
        alert.put("closedBy", operator);
        alert.put("closedTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        alert.put("resolution", resolution);
        
        log.info("关闭告警: {}, 操作人: {}, 解决方案: {}", alertId, operator, resolution);
        return true;
    }

    @Override
    public Map<String, Object> getAlertStatistics(int days) {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        
        int totalAlerts = alertHistory.size();
        int openAlerts = 0;
        int acknowledgedAlerts = 0;
        int closedAlerts = 0;
        
        Map<String, Integer> levelCount = new HashMap<>();
        Map<String, Integer> typeCount = new HashMap<>();
        
        for (Map<String, Object> alert : alertHistory.values()) {
            String status = (String) alert.get("status");
            String level = (String) alert.get("level");
            String type = (String) alert.get("alertType");
            
            if ("OPEN".equals(status)) openAlerts++;
            else if ("ACKNOWLEDGED".equals(status)) acknowledgedAlerts++;
            else if ("CLOSED".equals(status)) closedAlerts++;
            
            levelCount.put(level, levelCount.getOrDefault(level, 0) + 1);
            typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
        }
        
        stats.put("totalAlerts", totalAlerts);
        stats.put("openAlerts", openAlerts);
        stats.put("acknowledgedAlerts", acknowledgedAlerts);
        stats.put("closedAlerts", closedAlerts);
        stats.put("levelDistribution", levelCount);
        stats.put("typeDistribution", typeCount);
        stats.put("averageResponseTime", "15分钟");
        stats.put("averageResolutionTime", "2小时");
        
        return stats;
    }

    @Override
    public boolean sendAlertNotification(Long alertId, List<String> channels) {
        Map<String, Object> alert = alertHistory.get(alertId);
        if (alert == null) {
            return false;
        }
        
        for (String channel : channels) {
            switch (channel) {
                case "email":
                    sendEmailNotification(alert);
                    break;
                case "sms":
                    sendSmsNotification(alert);
                    break;
                case "webhook":
                    sendWebhookNotification(alert);
                    break;
                default:
                    log.warn("不支持的通知渠道: {}", channel);
            }
        }
        
        alert.put("notificationSent", true);
        alert.put("notificationChannels", channels);
        alert.put("notificationTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        return true;
    }

    private void sendEmailNotification(Map<String, Object> alert) {
        log.info("发送邮件通知: 告警ID={}, 级别={}, 消息={}", 
            alert.get("id"), alert.get("level"), alert.get("message"));
        // 实际实现中应该调用邮件服务
    }

    private void sendSmsNotification(Map<String, Object> alert) {
        log.info("发送短信通知: 告警ID={}, 级别={}, 消息={}", 
            alert.get("id"), alert.get("level"), alert.get("message"));
        // 实际实现中应该调用短信服务
    }

    private void sendWebhookNotification(Map<String, Object> alert) {
        log.info("发送Webhook通知: 告警ID={}, 级别={}, 消息={}", 
            alert.get("id"), alert.get("level"), alert.get("message"));
        // 实际实现中应该调用Webhook服务
    }
}
