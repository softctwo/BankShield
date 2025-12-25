package com.bankshield.api.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.*;
import com.bankshield.api.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 安全态势感知服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityPostureService {

    private final SecurityEventMapper securityEventMapper;
    private final ThreatIntelligenceMapper threatIntelligenceMapper;
    private final AlertRecordMapper alertRecordMapper;
    private final UserMapper userMapper;
    private final MonitorMetricMapper monitorMetricMapper;
    private final DataAssetMapper dataAssetMapper;

    /**
     * 获取实时安全态势数据
     */
    public Map<String, Object> getRealTimeSecurityPosture() {
        Map<String, Object> postureData = new HashMap<>();
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0);
            LocalDateTime oneHourAgo = now.minusHours(1);
            LocalDateTime oneDayAgo = now.minusDays(1);

            // 1. 核心安全指标
            postureData.put("securityScore", calculateSecurityScore());
            postureData.put("unprocessedAlertCount", getUnprocessedAlertCount());
            postureData.put("unprocessedEventCount", getUnprocessedEventCount());
            postureData.put("onlineUserCount", getOnlineUserCount());

            // 2. 威胁情报数据
            postureData.put("activeThreatCount", getActiveThreatCount());
            postureData.put("todayThreatCount", getTodayThreatCount());
            postureData.put("recentThreats", getRecentThreats(5));

            // 3. 安全事件数据
            postureData.put("recentEvents", getRecentSecurityEvents(10));
            postureData.put("todayEventCount", getTodayEventCount());
            postureData.put("riskDistribution", getRiskDistribution(oneDayAgo));

            // 4. 监控数据
            postureData.put("systemHealth", getSystemHealth());
            postureData.put("alertDistribution", getAlertDistribution(oneDayAgo));
            postureData.put("alertTrend", getAlertTrend(24));

            // 5. 资产数据
            postureData.put("assetHealth", getAssetHealth());
            postureData.put("sensitiveDataCount", getSensitiveDataCount());

            // 6. 密钥数据
            postureData.put("keyStatus", getKeyStatus());

            // 7. 权限数据
            postureData.put("roleViolationCount", getRoleViolationCount());

            // 8. 24小时趋势数据
            postureData.put("event24HourTrend", getEvent24HourTrend());
            postureData.put("threat7DayTrend", getThreat7DayTrend());

            // 9. 地理位置分布
            postureData.put("locationDistribution", getLocationDistribution(oneDayAgo));
            postureData.put("threatGeoDistribution", getThreatGeoDistribution());

            log.info("安全态势数据获取成功");
        } catch (Exception e) {
            log.error("获取安全态势数据失败", e);
            throw new RuntimeException("获取安全态势数据失败", e);
        }

        return postureData;
    }

    /**
     * 计算安全评分
     */
    private int calculateSecurityScore() {
        int score = 100;
        
        // 未处理告警扣分
        int unprocessedAlerts = getUnprocessedAlertCount();
        score -= Math.min(unprocessedAlerts * 2, 20);
        
        // 未处理事件扣分
        int unprocessedEvents = getUnprocessedEventCount();
        score -= Math.min(unprocessedEvents * 3, 15);
        
        // 活跃威胁扣分
        int activeThreats = getActiveThreatCount();
        score -= Math.min(activeThreats * 1, 10);
        
        // 系统健康度影响
        Double systemHealth = getSystemHealth();
        if (systemHealth != null && systemHealth < 80) {
            score -= (80 - systemHealth.intValue()) / 2;
        }
        
        // 密钥状态影响
        Map<String, Object> keyStatus = getKeyStatus();
        Integer expiredKeys = (Integer) keyStatus.getOrDefault("expiredCount", 0);
        score -= Math.min(expiredKeys * 1, 5);
        
        return Math.max(score, 0);
    }

    /**
     * 获取未处理告警数量
     */
    private int getUnprocessedAlertCount() {
        return alertRecordMapper.countUnprocessedAlerts();
    }

    /**
     * 获取未处理事件数量
     */
    private int getUnprocessedEventCount() {
        return securityEventMapper.countUnprocessedEvents();
    }

    /**
     * 获取在线用户数量
     */
    private int getOnlineUserCount() {
        // 这里简化处理，实际应该通过会话管理获取
        return userMapper.selectCount(null) > 0 ? 15 : 0; // 模拟数据
    }

    /**
     * 获取活跃威胁数量
     */
    private int getActiveThreatCount() {
        return (int) (long) threatIntelligenceMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ThreatIntelligence>()
                .eq("status", "ACTIVE")
        );
    }

    /**
     * 获取今日威胁数量
     */
    private int getTodayThreatCount() {
        return threatIntelligenceMapper.countTodayThreats();
    }

    /**
     * 获取最近威胁情报
     */
    private List<ThreatIntelligence> getRecentThreats(int limit) {
        return threatIntelligenceMapper.selectActiveThreats(limit);
    }

    /**
     * 获取最近安全事件
     */
    private List<SecurityEvent> getRecentSecurityEvents(int limit) {
        return securityEventMapper.selectRecentEvents(limit);
    }

    /**
     * 获取今日事件数量
     */
    private int getTodayEventCount() {
        return securityEventMapper.countTodayEvents();
    }

    /**
     * 获取风险分布
     */
    private List<Map<String, Object>> getRiskDistribution(LocalDateTime startTime) {
        return securityEventMapper.countByRiskLevel(startTime);
    }

    /**
     * 获取系统健康度
     */
    private Double getSystemHealth() {
        // 从监控指标中获取最新的系统健康度
        List<MonitorMetric> metrics = monitorMetricMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<MonitorMetric>()
                .eq("metric_type", "SYSTEM_HEALTH")
                .orderByDesc("collect_time")
                .last("LIMIT 1")
        );
        
        if (!metrics.isEmpty()) {
            return metrics.get(0).getMetricValue();
        }
        
        return 95.0; // 默认健康度
    }

    /**
     * 获取告警分布
     */
    private List<Map<String, Object>> getAlertDistribution(LocalDateTime startTime) {
        return alertRecordMapper.countByAlertLevel(startTime);
    }

    /**
     * 获取告警趋势
     */
    private List<Map<String, Object>> getAlertTrend(int hours) {
        return alertRecordMapper.getHourlyTrend(hours);
    }

    /**
     * 获取资产健康度
     */
    private Map<String, Object> getAssetHealth() {
        Map<String, Object> healthData = new HashMap<>();

        int totalAssets = (int) (long) dataAssetMapper.selectCount(null);
        int healthyAssets = (int) (long) dataAssetMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataAsset>()
                .eq("asset_status", "HEALTHY")
        );

        healthData.put("total", totalAssets);
        healthData.put("healthy", healthyAssets);
        healthData.put("unhealthy", totalAssets - healthyAssets);
        healthData.put("healthRate", totalAssets > 0 ? (healthyAssets * 100 / totalAssets) : 100);

        return healthData;
    }

    /**
     * 获取敏感数据数量
     */
    private int getSensitiveDataCount() {
        return (int) (long) dataAssetMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataAsset>()
                .in("sensitivity_level", "HIGH", "CRITICAL")
        );
    }

    /**
     * 获取密钥状态
     */
    private Map<String, Object> getKeyStatus() {
        Map<String, Object> keyData = new HashMap<>();
        
        // 这里应该从密钥管理模块获取数据，暂时模拟
        keyData.put("activeCount", 120);
        keyData.put("expiredCount", 5);
        keyData.put("destroyedCount", 3);
        keyData.put("expiringSoonCount", 12);
        
        return keyData;
    }

    /**
     * 获取权限冲突数量
     */
    private int getRoleViolationCount() {
        // 这里应该从三权分立模块获取数据，暂时模拟
        return 2;
    }

    /**
     * 获取事件24小时趋势
     */
    private List<Map<String, Object>> getEvent24HourTrend() {
        return securityEventMapper.get24HourTrend();
    }

    /**
     * 获取威胁7天趋势
     */
    private List<Map<String, Object>> getThreat7DayTrend() {
        return threatIntelligenceMapper.get7DayTrend();
    }

    /**
     * 获取地理位置分布
     */
    private List<Map<String, Object>> getLocationDistribution(LocalDateTime startTime) {
        return securityEventMapper.getLocationDistribution(startTime);
    }

    /**
     * 获取威胁地理位置分布
     */
    private List<Map<String, Object>> getThreatGeoDistribution() {
        return threatIntelligenceMapper.getGeoDistribution();
    }

    /**
     * 分页查询安全事件
     */
    public IPage<SecurityEvent> getSecurityEventPage(Page<SecurityEvent> page, String eventType, 
                                                       String riskLevel, String processStatus,
                                                       LocalDateTime startTime, LocalDateTime endTime) {
        return securityEventMapper.selectSecurityEventPage(page, eventType, riskLevel, 
                                                         processStatus, startTime, endTime);
    }

    /**
     * 获取威胁情报列表
     */
    public IPage<ThreatIntelligence> getThreatIntelligencePage(Page<ThreatIntelligence> page, 
                                                                String threatType, String threatLevel,
                                                                String status, LocalDateTime startTime, 
                                                                LocalDateTime endTime) {
        return threatIntelligenceMapper.selectThreatIntelligencePage(page, threatType, threatLevel,
                                                                   status, startTime, endTime);
    }
}