package com.bankshield.api.controller.security;

import com.bankshield.common.result.Result;
import com.bankshield.common.security.compliance.SecurityBaselineChecker;
import com.bankshield.common.security.compliance.SecurityBaselineChecker.BaselineCheckResult;
import com.bankshield.common.security.ratelimit.AdvancedRateLimiter;
import com.bankshield.common.security.audit.SecurityEventLogger;
import com.bankshield.common.security.incident.SecurityIncidentResponder;
import com.bankshield.common.security.incident.SecurityIncidentResponder.SecurityIncident;
import com.bankshield.common.security.incident.SecurityIncidentResponder.Severity;
import com.bankshield.common.security.incident.SecurityIncidentResponder.IncidentStatus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 安全加固控制器
 * 提供安全功能的管理和监控接口
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/security")
@Api(tags = "安全加固管理")
public class SecurityHardeningController {
    
    @Autowired
    private SecurityBaselineChecker baselineChecker;
    
    @Autowired
    private AdvancedRateLimiter rateLimiter;
    
    @Autowired
    private SecurityEventLogger eventLogger;
    
    @Autowired
    private SecurityIncidentResponder incidentResponder;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 执行安全基线检查
     */
    @GetMapping("/baseline/check")
    @ApiOperation("执行安全基线检查")
    public Result<BaselineCheckResult> performBaselineCheck() {
        try {
            log.info("执行安全基线检查");
            
            BaselineCheckResult result = baselineChecker.performFullCheck();
            
            // 记录审计日志
            eventLogger.logSecurityEvent(
                SecurityEventLogger.EventType.SYSTEM_CONFIG_CHANGE,
                SecurityEventLogger.EventLevel.LOW,
                getCurrentUserId(),
                getClientIp(),
                "执行安全基线检查",
                Map.of("passRate", result.getPassRate(), "status", result.getOverallStatus())
            );
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("执行安全基线检查失败: {}", e.getMessage(), e);
            return Result.error("执行安全基线检查失败");
        }
    }
    
    /**
     * 获取安全统计信息
     */
    @GetMapping("/stats")
    @ApiOperation("获取安全统计信息")
    public Result<Map<String, Object>> getSecurityStats(
            @ApiParam("日期 (格式: yyyyMMdd)") @RequestParam(required = false) String date) {
        try {
            if (date == null) {
                date = getCurrentDateKey();
            }
            
            Map<String, Object> stats = new HashMap<>();
            
            // 获取安全事件统计
            Map<String, Object> eventStats = eventLogger.getSecurityStats(date);
            stats.put("eventStats", eventStats);
            
            // 获取限流统计
            String userId = getCurrentUserId();
            Map<String, Object> rateLimitStats = rateLimiter.getRateLimitStats(userId);
            stats.put("rateLimitStats", rateLimitStats);
            
            // 获取系统安全状态
            Map<String, Object> systemStatus = getSystemSecurityStatus();
            stats.put("systemStatus", systemStatus);
            
            // 获取威胁情报
            Map<String, Object> threatIntel = getThreatIntelligence();
            stats.put("threatIntelligence", threatIntel);
            
            stats.put("timestamp", System.currentTimeMillis());
            stats.put("date", date);
            
            return Result.success(stats);
            
        } catch (Exception e) {
            log.error("获取安全统计信息失败: {}", e.getMessage(), e);
            return Result.error("获取安全统计信息失败");
        }
    }
    
    /**
     * 获取系统安全状态
     */
    private Map<String, Object> getSystemSecurityStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // 检查被阻断的IP数量
        String blockedListKey = "ip:blocked:list:" + getCurrentDateKey();
        Long blockedIpCount = redisTemplate.opsForList().size(blockedListKey);
        status.put("blockedIpCount", blockedIpCount != null ? blockedIpCount : 0);
        
        // 检查活跃事件数量
        String activeListKey = "incident:active:" + getCurrentDateKey();
        Long activeIncidentCount = redisTemplate.opsForList().size(activeListKey);
        status.put("activeIncidentCount", activeIncidentCount != null ? activeIncidentCount : 0);
        
        // 检查系统隔离状态
        String isolationKey = "system:isolated";
        Boolean isIsolated = redisTemplate.hasKey(isolationKey);
        status.put("isSystemIsolated", isIsolated != null && isIsolated);
        
        return status;
    }
    
    /**
     * 获取威胁情报
     */
    private Map<String, Object> getThreatIntelligence() {
        Map<String, Object> intel = new HashMap<>();
        
        // 获取今日攻击趋势
        String date = getCurrentDateKey();
        Map<String, Object> attackTrends = getAttackTrends(date);
        intel.put("attackTrends", attackTrends);
        
        // 获取攻击来源分布
        Map<String, Object> attackSources = getAttackSources(date);
        intel.put("attackSources", attackSources);
        
        // 获取攻击类型分布
        Map<String, Object> attackTypes = getAttackTypes(date);
        intel.put("attackTypes", attackTypes);
        
        return intel;
    }
    
    /**
     * 获取攻击趋势
     */
    private Map<String, Object> getAttackTrends(String date) {
        Map<String, Object> trends = new HashMap<>();
        
        // 获取不同类型的事件数量
        for (SecurityEventLogger.EventType type : SecurityEventLogger.EventType.values()) {
            String key = "security:stat:type:" + type + ":" + date;
            Long count = (Long) redisTemplate.opsForValue().get(key);
            if (count != null && count > 0) {
                trends.put(type.name(), count);
            }
        }
        
        return trends;
    }
    
    /**
     * 获取攻击来源分布
     */
    private Map<String, Object> getAttackSources(String date) {
        Map<String, Object> sources = new HashMap<>();
        
        // 获取按IP统计的攻击数量
        Set<String> keys = redisTemplate.keys("security:stat:ip:*:" + date);
        if (keys != null) {
            for (String key : keys) {
                String ip = key.substring(key.indexOf(":ip:") + 4, key.lastIndexOf(":"));
                Long count = (Long) redisTemplate.opsForValue().get(key);
                if (count != null && count > 0) {
                    sources.put(ip, count);
                }
            }
        }
        
        return sources;
    }
    
    /**
     * 获取攻击类型分布
     */
    private Map<String, Object> getAttackTypes(String date) {
        Map<String, Object> types = new HashMap<>();
        
        // 获取按级别统计的事件数量
        for (SecurityEventLogger.EventLevel level : SecurityEventLogger.EventLevel.values()) {
            String key = "security:stat:level:" + level + ":" + date;
            Long count = (Long) redisTemplate.opsForValue().get(key);
            if (count != null && count > 0) {
                types.put(level.name(), count);
            }
        }
        
        return types;
    }
    
    /**
     * 获取限流状态
     */
    @GetMapping("/rate-limit/status")
    @ApiOperation("获取限流状态")
    public Result<Map<String, Object>> getRateLimitStatus(
            @ApiParam("用户ID") @RequestParam String userId,
            @ApiParam("请求路径") @RequestParam String path) {
        try {
            Map<String, Object> status = new HashMap<>();
            
            // 检查是否被限流
            boolean isAllowed = rateLimiter.tryAcquire(path, userId);
            status.put("isAllowed", isAllowed);
            
            // 获取剩余令牌数
            long remainingTokens = rateLimiter.getRemainingTokens(path, userId);
            status.put("remainingTokens", remainingTokens);
            
            // 获取限流规则信息
            Map<String, Object> ruleInfo = getRateLimitRuleInfo(path);
            status.put("ruleInfo", ruleInfo);
            
            status.put("userId", userId);
            status.put("path", path);
            status.put("timestamp", System.currentTimeMillis());
            
            return Result.success(status);
            
        } catch (Exception e) {
            log.error("获取限流状态失败: {}", e.getMessage(), e);
            return Result.error("获取限流状态失败");
        }
    }
    
    /**
     * 重置限流
     */
    @PostMapping("/rate-limit/reset")
    @ApiOperation("重置限流")
    public Result<Void> resetRateLimit(
            @ApiParam("用户ID") @RequestParam String userId,
            @ApiParam("请求路径") @RequestParam String path) {
        try {
            rateLimiter.resetRateLimit(path, userId);
            
            // 记录审计日志
            eventLogger.logSecurityEvent(
                SecurityEventLogger.EventType.SYSTEM_CONFIG_CHANGE,
                SecurityEventLogger.EventLevel.LOW,
                getCurrentUserId(),
                getClientIp(),
                "重置限流",
                Map.of("userId", userId, "path", path)
            );
            
            return Result.success();
            
        } catch (Exception e) {
            log.error("重置限流失败: {}", e.getMessage(), e);
            return Result.error("重置限流失败");
        }
    }
    
    /**
     * 获取限流规则信息
     */
    private Map<String, Object> getRateLimitRuleInfo(String path) {
        Map<String, Object> ruleInfo = new HashMap<>();
        
        // 这里可以根据路径返回相应的规则信息
        if (path.contains("/api/user/login")) {
            ruleInfo.put("type", "login");
            ruleInfo.put("rate", 10);
            ruleInfo.put("capacity", 50);
            ruleInfo.put("period", "1s");
        } else if (path.contains("/api/key/")) {
            ruleInfo.put("type", "key");
            ruleInfo.put("rate", 20);
            ruleInfo.put("capacity", 100);
            ruleInfo.put("period", "1s");
        } else {
            ruleInfo.put("type", "default");
            ruleInfo.put("rate", 100);
            ruleInfo.put("capacity", 200);
            ruleInfo.put("period", "1s");
        }
        
        return ruleInfo;
    }
    
    /**
     * 获取被阻断的IP列表
     */
    @GetMapping("/blocked-ips")
    @ApiOperation("获取被阻断的IP列表")
    public Result<List<Map<String, Object>>> getBlockedIps(
            @ApiParam("日期 (格式: yyyyMMdd)") @RequestParam(required = false) String date) {
        try {
            if (date == null) {
                date = getCurrentDateKey();
            }
            
            List<Map<String, Object>> blockedIps = new ArrayList<>();
            
            String blockListKey = "ip:blocked:list:" + date;
            List<Object> ips = redisTemplate.opsForList().range(blockListKey, 0, -1);
            
            if (ips != null) {
                for (Object ip : ips) {
                    String blockKey = "ip:blocked:" + ip;
                    Map<Object, Object> blockInfo = redisTemplate.opsForHash().entries(blockKey);
                    if (!blockInfo.isEmpty()) {
                        Map<String, Object> ipInfo = new HashMap<>();
                        ipInfo.put("ip", ip);
                        ipInfo.put("blockInfo", blockInfo);
                        blockedIps.add(ipInfo);
                    }
                }
            }
            
            return Result.success(blockedIps);
            
        } catch (Exception e) {
            log.error("获取被阻断IP列表失败: {}", e.getMessage(), e);
            return Result.error("获取被阻断IP列表失败");
        }
    }
    
    /**
     * 解除IP阻断
     */
    @PostMapping("/unblock-ip")
    @ApiOperation("解除IP阻断")
    public Result<Void> unblockIp(@ApiParam("IP地址") @RequestParam String ip) {
        try {
            String blockKey = "ip:blocked:" + ip;
            redisTemplate.delete(blockKey);
            
            // 记录审计日志
            eventLogger.logSecurityEvent(
                SecurityEventLogger.EventType.SYSTEM_CONFIG_CHANGE,
                SecurityEventLogger.EventLevel.LOW,
                getCurrentUserId(),
                getClientIp(),
                "解除IP阻断",
                Map.of("ip", ip)
            );
            
            log.info("IP地址 {} 已解除阻断", ip);
            return Result.success();
            
        } catch (Exception e) {
            log.error("解除IP阻断失败: {}", e.getMessage(), e);
            return Result.error("解除IP阻断失败");
        }
    }
    
    /**
     * 创建安全事件
     */
    @PostMapping("/incidents")
    @ApiOperation("创建安全事件")
    public Result<String> createIncident(@RequestBody CreateIncidentRequest request) {
        try {
            SecurityIncident incident = new SecurityIncident();
            incident.setTitle(request.getTitle());
            incident.setDescription(request.getDescription());
            incident.setType(request.getType());
            incident.setSeverity(Severity.valueOf(request.getSeverity()));
            incident.setUserId(request.getUserId());
            incident.setSourceIp(request.getSourceIp());
            incident.setMetadata(request.getMetadata());
            
            incidentResponder.handleIncident(incident);
            
            return Result.success(incident.getId());
            
        } catch (Exception e) {
            log.error("创建安全事件失败: {}", e.getMessage(), e);
            return Result.error("创建安全事件失败");
        }
    }
    
    /**
     * 获取安全事件列表
     */
    @GetMapping("/incidents")
    @ApiOperation("获取安全事件列表")
    public Result<List<Map<String, Object>>> getIncidents(
            @ApiParam("严重程度") @RequestParam(required = false) String severity,
            @ApiParam("状态") @RequestParam(required = false) String status,
            @ApiParam("日期 (格式: yyyyMMdd)") @RequestParam(required = false) String date) {
        try {
            if (date == null) {
                date = getCurrentDateKey();
            }
            
            List<Map<String, Object>> incidents = new ArrayList<>();
            
            String activeListKey = "incident:active:" + date;
            List<Object> incidentIds = redisTemplate.opsForList().range(activeListKey, 0, -1);
            
            if (incidentIds != null) {
                for (Object incidentId : incidentIds) {
                    String incidentKey = "incident:" + incidentId;
                    Map<Object, Object> incidentData = redisTemplate.opsForHash().entries(incidentKey);
                    
                    if (!incidentData.isEmpty()) {
                        // 过滤条件
                        if (severity != null && !severity.equals(incidentData.get("severity"))) {
                            continue;
                        }
                        if (status != null && !status.equals(incidentData.get("status"))) {
                            continue;
                        }
                        
                        Map<String, Object> incident = new HashMap<>();
                        incidentData.forEach((key, value) -> incident.put(key.toString(), value));
                        incidents.add(incident);
                    }
                }
            }
            
            return Result.success(incidents);
            
        } catch (Exception e) {
            log.error("获取安全事件列表失败: {}", e.getMessage(), e);
            return Result.error("获取安全事件列表失败");
        }
    }
    
    /**
     * 更新事件状态
     */
    @PutMapping("/incidents/{id}/status")
    @ApiOperation("更新事件状态")
    public Result<Void> updateIncidentStatus(
            @PathVariable String id,
            @ApiParam("新状态") @RequestParam String status) {
        try {
            String incidentKey = "incident:" + id;
            Map<Object, Object> incidentData = redisTemplate.opsForHash().entries(incidentKey);
            
            if (incidentData.isEmpty()) {
                return Result.error("事件不存在");
            }
            
            // 更新状态
            redisTemplate.opsForHash().put(incidentKey, "status", status);
            redisTemplate.opsForHash().put(incidentKey, "updateTime", 
                    LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            // 记录审计日志
            eventLogger.logSecurityEvent(
                SecurityEventLogger.EventType.SYSTEM_CONFIG_CHANGE,
                SecurityEventLogger.EventLevel.LOW,
                getCurrentUserId(),
                getClientIp(),
                "更新安全事件状态",
                Map.of("incidentId", id, "newStatus", status)
            );
            
            return Result.success();
            
        } catch (Exception e) {
            log.error("更新事件状态失败: {}", e.getMessage(), e);
            return Result.error("更新事件状态失败");
        }
    }
    
    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        // 从安全上下文中获取当前用户ID
        return "admin"; // 临时实现
    }
    
    /**
     * 获取客户端IP
     */
    private String getClientIp() {
        // 从请求中获取客户端IP
        return "127.0.0.1"; // 临时实现
    }
    
    /**
     * 获取当前日期键
     */
    private String getCurrentDateKey() {
        return LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
    
    /**
     * 创建事件请求
     */
    @Data
    public static class CreateIncidentRequest {
        private String title;
        private String description;
        private String type;
        private String severity;
        private String userId;
        private String sourceIp;
        private Map<String, Object> metadata;
    }
}