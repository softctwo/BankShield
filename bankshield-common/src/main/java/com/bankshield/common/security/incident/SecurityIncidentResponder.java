package com.bankshield.common.security.incident;

import com.bankshield.common.security.audit.SecurityEventLogger;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 安全事件响应器
 * 用于自动响应和处理各种安全事件，包括自动阻断、告警通知、应急响应等
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Slf4j
@Component
public class SecurityIncidentResponder {

    // 事件类型常量
    public static final String BRUTE_FORCE = "BRUTE_FORCE";
    public static final String PRIVILEGE_ESCALATION = "PRIVILEGE_ESCALATION";
    public static final String DATA_BREACH = "DATA_BREACH";
    public static final String MALICIOUS_REQUEST = "MALICIOUS_REQUEST";
    public static final String UNAUTHORIZED_ACCESS = "UNAUTHORIZED_ACCESS";
    public static final String DATA_LEAK = "DATA_LEAK";
    public static final String INJECTION_ATTACK = "INJECTION_ATTACK";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SecurityEventLogger eventLogger;

    @Value("${bankshield.security.incident.response.enabled:true}")
    private boolean responseEnabled;

    @Value("${bankshield.security.incident.response.auto-block:true}")
    private boolean autoBlockEnabled;

    @Value("${bankshield.security.incident.response.notification.enabled:true}")
    private boolean notificationEnabled;
    
    // 事件严重级别
    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    // 事件状态
    public enum IncidentStatus {
        DETECTED,       // 已检测
        ANALYZING,      // 分析中
        RESPONDING,     // 响应中
        CONTAINED,      // 已控制
        RESOLVED,       // 已解决
        CLOSED          // 已关闭
    }
    
    // 响应动作
    public enum ResponseAction {
        LOG,            // 记录日志
        ALERT,          // 发送告警
        BLOCK_IP,       // 阻断IP
        THROTTLE,       // 限速
        ISOLATE,        // 隔离
        DISABLE_ACCOUNT, // 禁用账户
        FORCE_LOGOUT,   // 强制登出
        NOTIFY_ADMIN,   // 通知管理员
        ESCALATE        // 升级处理
    }
    
    /**
     * 处理安全事件
     * 
     * @param incident 安全事件
     */
    public void handleIncident(SecurityIncident incident) {
        if (!responseEnabled) {
            log.info("安全事件响应已禁用，跳过事件处理: {}", incident.getTitle());
            return;
        }
        
        log.info("开始处理安全事件: {} - 级别: {}", incident.getTitle(), incident.getSeverity());
        
        try {
            // 创建事件记录
            createIncidentRecord(incident);
            
            // 根据严重程度执行响应动作
            List<ResponseAction> actions = determineResponseActions(incident);
            
            for (ResponseAction action : actions) {
                executeResponseAction(incident, action);
            }
            
            // 更新事件状态
            updateIncidentStatus(incident, IncidentStatus.RESPONDING);
            
            // 记录响应日志
            logIncidentResponse(incident, actions);
            
        } catch (Exception e) {
            log.error("处理安全事件失败: {}", e.getMessage(), e);
            // 记录失败事件
            logFailedIncident(incident, e);
        }
    }
    
    /**
     * 确定响应动作
     */
    private List<ResponseAction> determineResponseActions(SecurityIncident incident) {
        List<ResponseAction> actions = new ArrayList<>();
        
        // 所有事件都记录日志
        actions.add(ResponseAction.LOG);
        
        switch (incident.getSeverity()) {
            case CRITICAL:
                actions.add(ResponseAction.ALERT);
                actions.add(ResponseAction.BLOCK_IP);
                actions.add(ResponseAction.ISOLATE);
                actions.add(ResponseAction.NOTIFY_ADMIN);
                actions.add(ResponseAction.ESCALATE);
                break;
                
            case HIGH:
                actions.add(ResponseAction.ALERT);
                actions.add(ResponseAction.BLOCK_IP);
                actions.add(ResponseAction.NOTIFY_ADMIN);
                break;
                
            case MEDIUM:
                actions.add(ResponseAction.ALERT);
                actions.add(ResponseAction.THROTTLE);
                actions.add(ResponseAction.NOTIFY_ADMIN);
                break;
                
            case LOW:
                actions.add(ResponseAction.LOG);
                break;
        }
        
        // 根据事件类型添加特定动作
        switch (incident.getType()) {
            case BRUTE_FORCE:
                if (incident.getSeverity() == Severity.HIGH || incident.getSeverity() == Severity.CRITICAL) {
                    actions.add(ResponseAction.DISABLE_ACCOUNT);
                }
                break;
                
            case PRIVILEGE_ESCALATION:
                actions.add(ResponseAction.FORCE_LOGOUT);
                actions.add(ResponseAction.DISABLE_ACCOUNT);
                break;
                
            case DATA_BREACH:
                actions.add(ResponseAction.ISOLATE);
                actions.add(ResponseAction.ESCALATE);
                break;
        }
        
        return actions;
    }
    
    /**
     * 执行响应动作
     */
    private void executeResponseAction(SecurityIncident incident, ResponseAction action) {
        try {
            switch (action) {
                case LOG:
                    logIncident(incident);
                    break;
                    
                case ALERT:
                    sendAlert(incident);
                    break;
                    
                case BLOCK_IP:
                    if (autoBlockEnabled && incident.getSourceIp() != null) {
                        blockIpAddress(incident.getSourceIp(), incident);
                    }
                    break;
                    
                case THROTTLE:
                    if (incident.getSourceIp() != null) {
                        throttleIpAddress(incident.getSourceIp(), incident);
                    }
                    break;
                    
                case ISOLATE:
                    isolateSystem(incident);
                    break;
                    
                case DISABLE_ACCOUNT:
                    if (incident.getUserId() != null) {
                        disableUserAccount(incident.getUserId(), incident);
                    }
                    break;
                    
                case FORCE_LOGOUT:
                    if (incident.getUserId() != null) {
                        forceUserLogout(incident.getUserId(), incident);
                    }
                    break;
                    
                case NOTIFY_ADMIN:
                    notifyAdministrators(incident);
                    break;
                    
                case ESCALATE:
                    escalateIncident(incident);
                    break;
            }
        } catch (Exception e) {
            log.error("执行响应动作 {} 失败: {}", action, e.getMessage(), e);
        }
    }
    
    /**
     * 创建事件记录
     */
    private void createIncidentRecord(SecurityIncident incident) {
        incident.setId(UUID.randomUUID().toString());
        incident.setStatus(IncidentStatus.DETECTED);
        incident.setDetectedTime(LocalDateTime.now());
        incident.setCreateTime(new Date());
        
        // 保存到Redis
        String incidentKey = "incident:" + incident.getId();
        redisTemplate.opsForHash().putAll(incidentKey, incidentToMap(incident));
        redisTemplate.expire(incidentKey, 30, TimeUnit.DAYS);
        
        // 添加到活跃事件列表
        String activeListKey = "incident:active:" + getDateKey();
        redisTemplate.opsForList().rightPush(activeListKey, incident.getId());
        redisTemplate.expire(activeListKey, 7, TimeUnit.DAYS);
        
        log.info("创建安全事件记录: {} - {}", incident.getId(), incident.getTitle());
    }
    
    /**
     * 阻断IP地址
     */
    private void blockIpAddress(String ip, SecurityIncident incident) {
        String blockKey = "ip:blocked:" + ip;
        Map<String, Object> blockInfo = new HashMap<>();
        blockInfo.put("ip", ip);
        blockInfo.put("reason", "安全事件响应");
        blockInfo.put("incidentId", incident.getId());
        blockInfo.put("blockedTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        blockInfo.put("expiryTime", LocalDateTime.now().plusHours(24).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        redisTemplate.opsForHash().putAll(blockKey, blockInfo);
        redisTemplate.expire(blockKey, 24, TimeUnit.HOURS);
        
        // 添加到阻断列表
        String blockListKey = "ip:blocked:list:" + getDateKey();
        redisTemplate.opsForList().rightPush(blockListKey, ip);
        redisTemplate.expire(blockListKey, 7, TimeUnit.DAYS);
        
        log.warn("IP地址 {} 被阻断 - 事件: {}", ip, incident.getTitle());
        
        // 记录阻断事件
        eventLogger.logSecurityEvent(
            SecurityEventLogger.EventType.MALICIOUS_REQUEST,
            SecurityEventLogger.EventLevel.HIGH,
            "system",
            ip,
            String.format("IP地址被自动阻断: %s", incident.getTitle()),
            Map.of("incidentId", incident.getId(), "reason", incident.getDescription())
        );
    }
    
    /**
     * 限速IP地址
     */
    private void throttleIpAddress(String ip, SecurityIncident incident) {
        String throttleKey = "ip:throttled:" + ip;
        redisTemplate.opsForValue().set(throttleKey, "1", 1, TimeUnit.HOURS);
        
        log.warn("IP地址 {} 被限速 - 事件: {}", ip, incident.getTitle());
    }
    
    /**
     * 隔离系统
     */
    private void isolateSystem(SecurityIncident incident) {
        // 设置系统隔离状态
        String isolationKey = "system:isolated";
        Map<String, Object> isolationInfo = new HashMap<>();
        isolationInfo.put("incidentId", incident.getId());
        isolationInfo.put("reason", incident.getTitle());
        isolationInfo.put("isolatedTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        isolationInfo.put("status", "isolated");
        
        redisTemplate.opsForHash().putAll(isolationKey, isolationInfo);
        
        log.error("系统进入隔离状态 - 事件: {}", incident.getTitle());
        
        // 发送紧急通知
        sendEmergencyNotification(incident);
    }
    
    /**
     * 禁用用户账户
     */
    private void disableUserAccount(String userId, SecurityIncident incident) {
        String disableKey = "user:disabled:" + userId;
        Map<String, Object> disableInfo = new HashMap<>();
        disableInfo.put("userId", userId);
        disableInfo.put("reason", "安全事件响应");
        disableInfo.put("incidentId", incident.getId());
        disableInfo.put("disabledTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        redisTemplate.opsForHash().putAll(disableKey, disableInfo);
        redisTemplate.expire(disableKey, 24, TimeUnit.HOURS);
        
        log.warn("用户账户 {} 被禁用 - 事件: {}", userId, incident.getTitle());
    }
    
    /**
     * 强制用户登出
     */
    private void forceUserLogout(String userId, SecurityIncident incident) {
        String logoutKey = "user:logout:forced:" + userId;
        redisTemplate.opsForValue().set(logoutKey, incident.getId(), 1, TimeUnit.HOURS);
        
        log.warn("用户 {} 被强制登出 - 事件: {}", userId, incident.getTitle());
    }
    
    /**
     * 发送告警
     */
    private void sendAlert(SecurityIncident incident) {
        // 实现告警发送逻辑
        log.warn("发送安全告警 - {}: {}", incident.getTitle(), incident.getDescription());
    }
    
    /**
     * 通知管理员
     */
    private void notifyAdministrators(SecurityIncident incident) {
        if (notificationEnabled) {
            // 实现管理员通知逻辑
            log.info("通知管理员 - {}: {}", incident.getTitle(), incident.getDescription());
        }
    }
    
    /**
     * 发送紧急通知
     */
    private void sendEmergencyNotification(SecurityIncident incident) {
        log.error("!!! 紧急安全通知 !!! - {}: {}", incident.getTitle(), incident.getDescription());
        // 实现紧急通知逻辑（短信、邮件、电话等）
    }
    
    /**
     * 升级事件处理
     */
    private void escalateIncident(SecurityIncident incident) {
        log.warn("升级安全事件处理 - {}: {}", incident.getTitle(), incident.getDescription());
        // 实现事件升级逻辑
    }
    
    /**
     * 记录事件
     */
    private void logIncident(SecurityIncident incident) {
        eventLogger.logSecurityEvent(
            SecurityEventLogger.EventType.SUSPICIOUS_BEHAVIOR,
            convertSeverity(incident.getSeverity()),
            incident.getUserId() != null ? incident.getUserId() : "system",
            incident.getSourceIp() != null ? incident.getSourceIp() : "unknown",
            incident.getDescription(),
            Map.of("incidentId", incident.getId(), "type", incident.getType())
        );
    }
    
    /**
     * 记录事件响应
     */
    private void logIncidentResponse(SecurityIncident incident, List<ResponseAction> actions) {
        log.info("安全事件响应完成 - 事件: {}, 执行动作: {}", incident.getTitle(), actions);
    }
    
    /**
     * 记录失败事件
     */
    private void logFailedIncident(SecurityIncident incident, Exception e) {
        log.error("安全事件处理失败 - 事件: {}, 错误: {}", incident.getTitle(), e.getMessage(), e);
    }
    
    /**
     * 更新事件状态
     */
    private void updateIncidentStatus(SecurityIncident incident, IncidentStatus status) {
        incident.setStatus(status);
        incident.setUpdateTime(LocalDateTime.now());
        
        // 更新Redis中的事件记录
        String incidentKey = "incident:" + incident.getId();
        redisTemplate.opsForHash().put(incidentKey, "status", status.name());
        redisTemplate.opsForHash().put(incidentKey, "updateTime", 
                incident.getUpdateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
    
    /**
     * 获取日期键
     */
    private String getDateKey() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
    
    /**
     * 事件转Map
     */
    private Map<String, Object> incidentToMap(SecurityIncident incident) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", incident.getId());
        map.put("title", incident.getTitle());
        map.put("description", incident.getDescription());
        map.put("type", incident.getType());
        map.put("severity", incident.getSeverity().name());
        map.put("userId", incident.getUserId());
        map.put("sourceIp", incident.getSourceIp());
        map.put("status", incident.getStatus().name());
        map.put("detectedTime", incident.getDetectedTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        map.put("createTime", incident.getCreateTime().getTime());
        return map;
    }
    
    /**
     * 转换严重程度
     */
    private SecurityEventLogger.EventLevel convertSeverity(Severity severity) {
        switch (severity) {
            case CRITICAL:
                return SecurityEventLogger.EventLevel.CRITICAL;
            case HIGH:
                return SecurityEventLogger.EventLevel.HIGH;
            case MEDIUM:
                return SecurityEventLogger.EventLevel.MEDIUM;
            case LOW:
                return SecurityEventLogger.EventLevel.LOW;
            default:
                return SecurityEventLogger.EventLevel.MEDIUM;
        }
    }
    
    /**
     * 安全事件
     */
    @Data
    public static class SecurityIncident {
        private String id;
        private String title;
        private String description;
        private String type;
        private Severity severity;
        private String userId;
        private String sourceIp;
        private IncidentStatus status;
        private LocalDateTime detectedTime;
        private LocalDateTime updateTime;
        private Date createTime;
        private Map<String, Object> metadata;
    }
}