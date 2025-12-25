package com.bankshield.common.security.audit;

import cn.hutool.core.util.StrUtil;
import com.bankshield.common.result.ResultCode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 安全事件日志记录器
 * 用于记录各种安全相关事件，支持异步记录和实时告警
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Slf4j
@Component
public class SecurityEventLogger {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String SECURITY_EVENT_KEY = "security:event:";
    private static final String SECURITY_ALERT_KEY = "security:alert:";
    private static final String SECURITY_STAT_KEY = "security:stat:";
    
    // 事件级别定义
    public enum EventLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    // 事件类型定义
    public enum EventType {
        LOGIN_FAILURE,          // 登录失败
        LOGIN_SUCCESS,          // 登录成功
        LOGOUT,                 // 登出
        PASSWORD_CHANGE,        // 密码修改
        PRIVILEGE_ESCALATION,   // 权限提升
        ACCESS_DENIED,          // 访问拒绝
        SQL_INJECTION,          // SQL注入攻击
        XSS_ATTACK,             // XSS攻击
        CSRF_ATTACK,            // CSRF攻击
        BRUTE_FORCE,            // 暴力破解
        RATE_LIMIT_EXCEEDED,    // 限流触发
        SIGNATURE_VERIFICATION_FAILED, // 签名验证失败
        MALICIOUS_REQUEST,      // 恶意请求
        SUSPICIOUS_BEHAVIOR,    // 可疑行为
        SYSTEM_CONFIG_CHANGE,   // 系统配置变更
        KEY_ROTATION,           // 密钥轮换
        DATA_EXPORT,            // 数据导出
        AUDIT_LOG_ACCESS,       // 审计日志访问
        EMERGENCY_OPERATION     // 紧急操作
    }
    
    /**
     * 记录安全事件
     * 
     * @param type 事件类型
     * @param level 事件级别
     * @param userId 用户ID
     * @param ip 客户端IP
     * @param description 事件描述
     * @param details 详细数据
     */
    @Async
    public void logSecurityEvent(EventType type, EventLevel level, String userId, 
                               String ip, String description, Map<String, Object> details) {
        try {
            SecurityEvent event = new SecurityEvent();
            event.setId(UUID.randomUUID().toString());
            event.setType(type);
            event.setLevel(level);
            event.setUserId(userId);
            event.setIp(ip);
            event.setDescription(description);
            event.setDetails(details);
            event.setTimestamp(LocalDateTime.now());
            event.setCreateTime(new Date());
            
            // 保存事件到Redis
            saveSecurityEvent(event);
            
            // 根据级别处理告警
            handleAlert(event);
            
            // 更新统计信息
            updateStatistics(event);
            
            // 记录到应用日志
            logSecurityEvent(event);
            
        } catch (Exception e) {
            log.error("记录安全事件失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 记录登录事件
     */
    public void logLoginEvent(String username, boolean success, String ip, String reason) {
        EventType type = success ? EventType.LOGIN_SUCCESS : EventType.LOGIN_FAILURE;
        EventLevel level = success ? EventLevel.LOW : EventLevel.MEDIUM;
        
        Map<String, Object> details = new HashMap<>();
        details.put("username", username);
        details.put("success", success);
        if (!success) {
            details.put("reason", reason);
        }
        
        logSecurityEvent(type, level, username, ip, 
                success ? "用户登录成功" : "用户登录失败: " + reason, details);
    }
    
    /**
     * 记录攻击事件
     */
    public void logAttackEvent(EventType attackType, String ip, String evidence, String path) {
        Map<String, Object> details = new HashMap<>();
        details.put("evidence", evidence);
        details.put("path", path);
        details.put("attack_type", attackType);
        
        logSecurityEvent(attackType, EventLevel.HIGH, "anonymous", ip,
                String.format("检测到%s攻击: %s", attackType, evidence), details);
    }
    
    /**
     * 记录限流事件
     */
    public void logRateLimitEvent(String userId, String ip, String path, String ruleType) {
        Map<String, Object> details = new HashMap<>();
        details.put("path", path);
        details.put("rule_type", ruleType);
        
        logSecurityEvent(EventType.RATE_LIMIT_EXCEEDED, EventLevel.MEDIUM, userId, ip,
                String.format("请求被限流: %s", path), details);
    }
    
    /**
     * 记录签名验证失败
     */
    public void logSignatureVerificationFailed(String ip, String reason, String path) {
        Map<String, Object> details = new HashMap<>();
        details.put("reason", reason);
        details.put("path", path);
        
        logSecurityEvent(EventType.SIGNATURE_VERIFICATION_FAILED, EventLevel.HIGH, "anonymous", ip,
                String.format("API签名验证失败: %s", reason), details);
    }
    
    /**
     * 记录数据访问事件
     */
    public void logDataAccessEvent(String userId, String ip, String dataType, String operation, boolean success) {
        EventType type = success ? EventType.DATA_EXPORT : EventType.ACCESS_DENIED;
        EventLevel level = success ? EventLevel.LOW : EventLevel.MEDIUM;
        
        Map<String, Object> details = new HashMap<>();
        details.put("data_type", dataType);
        details.put("operation", operation);
        details.put("success", success);
        
        logSecurityEvent(type, level, userId, ip,
                String.format("数据访问: %s %s", operation, dataType), details);
    }
    
    /**
     * 保存安全事件
     */
    private void saveSecurityEvent(SecurityEvent event) {
        try {
            String key = SECURITY_EVENT_KEY + event.getId();
            redisTemplate.opsForHash().putAll(key, beanToMap(event));
            
            // 设置过期时间（30天）
            redisTemplate.expire(key, 30, TimeUnit.DAYS);
            
            // 添加到事件列表
            String listKey = SECURITY_EVENT_KEY + "list:" + getDateKey();
            redisTemplate.opsForList().rightPush(listKey, event.getId());
            redisTemplate.expire(listKey, 30, TimeUnit.DAYS);
            
        } catch (Exception e) {
            log.error("保存安全事件失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理告警
     */
    private void handleAlert(SecurityEvent event) {
        // 高级别事件立即告警
        if (event.getLevel() == EventLevel.CRITICAL || event.getLevel() == EventLevel.HIGH) {
            sendAlert(event);
        }
        
        // 检查是否需要聚合告警
        if (shouldAggregateAlert(event)) {
            aggregateAlert(event);
        }
    }
    
    /**
     * 发送告警
     */
    private void sendAlert(SecurityEvent event) {
        try {
            SecurityAlert alert = new SecurityAlert();
            alert.setId(UUID.randomUUID().toString());
            alert.setEventId(event.getId());
            alert.setType(event.getType());
            alert.setLevel(event.getLevel());
            alert.setTitle(buildAlertTitle(event));
            alert.setContent(buildAlertContent(event));
            alert.setTimestamp(event.getTimestamp());
            alert.setCreateTime(new Date());
            
            // 保存告警
            String alertKey = SECURITY_ALERT_KEY + alert.getId();
            redisTemplate.opsForHash().putAll(alertKey, beanToMap(alert));
            redisTemplate.expire(alertKey, 7, TimeUnit.DAYS);
            
            // 添加到待处理告警列表
            String pendingListKey = SECURITY_ALERT_KEY + "pending:" + getDateKey();
            redisTemplate.opsForList().rightPush(pendingListKey, alert.getId());
            redisTemplate.expire(pendingListKey, 7, TimeUnit.DAYS);
            
            log.warn("安全告警 - {}: {}", alert.getTitle(), alert.getContent());
            
        } catch (Exception e) {
            log.error("发送安全告警失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 更新统计信息
     */
    private void updateStatistics(SecurityEvent event) {
        try {
            String dateKey = getDateKey();
            
            // 按类型统计
            String typeStatKey = SECURITY_STAT_KEY + "type:" + event.getType() + ":" + dateKey;
            redisTemplate.opsForValue().increment(typeStatKey);
            redisTemplate.expire(typeStatKey, 30, TimeUnit.DAYS);
            
            // 按级别统计
            String levelStatKey = SECURITY_STAT_KEY + "level:" + event.getLevel() + ":" + dateKey;
            redisTemplate.opsForValue().increment(levelStatKey);
            redisTemplate.expire(levelStatKey, 30, TimeUnit.DAYS);
            
            // 按IP统计
            if (StrUtil.isNotBlank(event.getIp())) {
                String ipStatKey = SECURITY_STAT_KEY + "ip:" + event.getIp() + ":" + dateKey;
                redisTemplate.opsForValue().increment(ipStatKey);
                redisTemplate.expire(ipStatKey, 30, TimeUnit.DAYS);
            }
            
        } catch (Exception e) {
            log.error("更新统计信息失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 记录到应用日志
     */
    private void logSecurityEvent(SecurityEvent event) {
        String logMessage = String.format("[%s] %s - %s (用户: %s, IP: %s, 描述: %s)",
                event.getLevel(), event.getType(), event.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                event.getUserId(), event.getIp(), event.getDescription());
        
        switch (event.getLevel()) {
            case CRITICAL:
                log.error(logMessage);
                break;
            case HIGH:
                log.warn(logMessage);
                break;
            case MEDIUM:
                log.info(logMessage);
                break;
            case LOW:
                log.debug(logMessage);
                break;
        }
    }
    
    /**
     * 构建告警标题
     */
    private String buildAlertTitle(SecurityEvent event) {
        return String.format("[%s] %s", event.getLevel(), event.getType());
    }
    
    /**
     * 构建告警内容
     */
    private String buildAlertContent(SecurityEvent event) {
        StringBuilder content = new StringBuilder();
        content.append("事件描述: ").append(event.getDescription()).append("\n");
        content.append("用户: ").append(event.getUserId()).append("\n");
        content.append("IP: ").append(event.getIp()).append("\n");
        content.append("时间: ").append(event.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        if (event.getDetails() != null && !event.getDetails().isEmpty()) {
            content.append("\n详细信息:\n");
            event.getDetails().forEach((key, value) -> 
                content.append("  ").append(key).append(": ").append(value).append("\n"));
        }
        
        return content.toString();
    }
    
    /**
     * 检查是否需要聚合告警
     */
    private boolean shouldAggregateAlert(SecurityEvent event) {
        // 暴力破解攻击需要聚合
        return event.getType() == EventType.BRUTE_FORCE ||
               event.getType() == EventType.SQL_INJECTION ||
               event.getType() == EventType.XSS_ATTACK;
    }
    
    /**
     * 聚合告警
     */
    private void aggregateAlert(SecurityEvent event) {
        // 实现告警聚合逻辑
        String aggregateKey = SECURITY_ALERT_KEY + "aggregate:" + event.getType() + ":" + event.getIp() + ":" + getDateKey();
        Long count = redisTemplate.opsForValue().increment(aggregateKey);
        redisTemplate.expire(aggregateKey, 1, TimeUnit.DAYS);
        
        // 如果达到一定阈值，发送聚合告警
        if (count != null && count >= 10) {
            log.warn("检测到大量{}攻击来自IP: {} ({}次)", event.getType(), event.getIp(), count);
        }
    }
    
    /**
     * 获取日期键
     */
    private String getDateKey() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
    
    /**
     * 对象转Map
     */
    private Map<String, Object> beanToMap(Object obj) {
        Map<String, Object> map = new HashMap<>();
        if (obj instanceof SecurityEvent) {
            SecurityEvent event = (SecurityEvent) obj;
            map.put("id", event.getId());
            map.put("type", event.getType().name());
            map.put("level", event.getLevel().name());
            map.put("userId", event.getUserId());
            map.put("ip", event.getIp());
            map.put("description", event.getDescription());
            map.put("details", event.getDetails() != null ? event.getDetails().toString() : "");
            map.put("timestamp", event.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            map.put("createTime", event.getCreateTime().getTime());
        } else if (obj instanceof SecurityAlert) {
            SecurityAlert alert = (SecurityAlert) obj;
            map.put("id", alert.getId());
            map.put("eventId", alert.getEventId());
            map.put("type", alert.getType().name());
            map.put("level", alert.getLevel().name());
            map.put("title", alert.getTitle());
            map.put("content", alert.getContent());
            map.put("timestamp", alert.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            map.put("createTime", alert.getCreateTime().getTime());
        }
        return map;
    }
    
    /**
     * 获取安全事件统计
     */
    public Map<String, Object> getSecurityStats(String dateKey) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 按类型统计
            Map<String, Long> typeStats = new HashMap<>();
            for (EventType type : EventType.values()) {
                String key = SECURITY_STAT_KEY + "type:" + type + ":" + dateKey;
                Long count = (Long) redisTemplate.opsForValue().get(key);
                if (count != null) {
                    typeStats.put(type.name(), count);
                }
            }
            stats.put("typeStats", typeStats);
            
            // 按级别统计
            Map<String, Long> levelStats = new HashMap<>();
            for (EventLevel level : EventLevel.values()) {
                String key = SECURITY_STAT_KEY + "level:" + level + ":" + dateKey;
                Long count = (Long) redisTemplate.opsForValue().get(key);
                if (count != null) {
                    levelStats.put(level.name(), count);
                }
            }
            stats.put("levelStats", levelStats);
            
        } catch (Exception e) {
            log.error("获取安全统计失败: {}", e.getMessage(), e);
        }
        
        return stats;
    }
    
    /**
     * 安全事件
     */
    @Data
    public static class SecurityEvent {
        private String id;
        private EventType type;
        private EventLevel level;
        private String userId;
        private String ip;
        private String description;
        private Map<String, Object> details;
        private LocalDateTime timestamp;
        private Date createTime;
    }
    
    /**
     * 安全告警
     */
    @Data
    public static class SecurityAlert {
        private String id;
        private String eventId;
        private EventType type;
        private EventLevel level;
        private String title;
        private String content;
        private LocalDateTime timestamp;
        private Date createTime;
    }
}