package com.bankshield.api.service.impl;

import com.bankshield.api.dto.SecurityDashboardDTO;
import com.bankshield.api.dto.SecurityEventDTO;
import com.bankshield.api.mapper.SecurityThreatMapper;
import com.bankshield.api.service.SecurityDashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 安全态势仪表板服务实现
 */
@Slf4j
@Service
public class SecurityDashboardServiceImpl implements SecurityDashboardService {
    
    @Autowired
    private SecurityThreatMapper threatMapper;
    
    private final Random random = new Random();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    @Override
    public SecurityDashboardDTO getDashboardData() {
        SecurityDashboardDTO dashboard = new SecurityDashboardDTO();
        
        // 威胁统计
        dashboard.setThreatStats(getThreatStats());
        
        // 攻击类型分布
        dashboard.setAttackTypeDistribution(threatMapper.getAttackTypeDistribution());
        
        // 安全事件趋势
        dashboard.setEventTrend(threatMapper.getEventTrend24h());
        
        // 地理位置分布
        dashboard.setGeoDistribution(threatMapper.getGeoDistribution());
        
        // 实时安全事件流
        dashboard.setRealtimeEvents(getRealtimeEvents());
        
        // 系统健康状态
        dashboard.setSystemHealth(getSystemHealth());
        
        // TOP10攻击源IP
        dashboard.setTopAttackIPs(threatMapper.getTopAttackIPs());
        
        // 综合安全评分
        dashboard.setSecurityScore(calculateSecurityScore());
        
        // 安全等级
        dashboard.setSecurityLevel(getSecurityLevel());
        
        return dashboard;
    }
    
    /**
     * 获取威胁统计
     */
    private SecurityDashboardDTO.ThreatStats getThreatStats() {
        SecurityDashboardDTO.ThreatStats stats = new SecurityDashboardDTO.ThreatStats();
        
        List<Map<String, Object>> threatStats = threatMapper.getThreatStats();
        
        stats.setCritical(0);
        stats.setHigh(0);
        stats.setMedium(0);
        stats.setLow(0);
        
        for (Map<String, Object> stat : threatStats) {
            String severity = (String) stat.get("severity");
            Long count = (Long) stat.get("count");
            int countValue = count != null ? count.intValue() : 0;
            
            switch (severity) {
                case "CRITICAL":
                    stats.setCritical(countValue);
                    break;
                case "HIGH":
                    stats.setHigh(countValue);
                    break;
                case "MEDIUM":
                    stats.setMedium(countValue);
                    break;
                case "LOW":
                    stats.setLow(countValue);
                    break;
            }
        }
        
        return stats;
    }
    
    /**
     * 获取实时安全事件流
     */
    private List<SecurityEventDTO> getRealtimeEvents() {
        List<SecurityEventDTO> events = new ArrayList<>();
        List<Map<String, Object>> recentEvents = threatMapper.getRecentEvents();
        
        for (Map<String, Object> event : recentEvents) {
            SecurityEventDTO dto = new SecurityEventDTO();
            dto.setId((Long) event.get("id"));
            dto.setType((String) event.get("threat_type"));
            dto.setSource((String) event.get("source_ip"));
            dto.setTarget((String) event.get("target_ip"));
            dto.setStatus((String) event.get("status"));
            dto.setLevel(((String) event.get("severity")).toLowerCase());
            
            LocalDateTime detectTime = (LocalDateTime) event.get("detect_time");
            dto.setTime(detectTime != null ? detectTime.format(timeFormatter) : "");
            
            events.add(dto);
        }
        
        return events;
    }
    
    /**
     * 获取系统健康状态
     */
    private SecurityDashboardDTO.SystemHealth getSystemHealth() {
        SecurityDashboardDTO.SystemHealth health = new SecurityDashboardDTO.SystemHealth();
        
        // 模拟系统健康数据（实际应该从监控系统获取）
        health.setCpu(random.nextInt(30) + 40);
        health.setMemory(random.nextInt(25) + 50);
        health.setDisk(random.nextInt(20) + 60);
        health.setNetwork(random.nextInt(35) + 30);
        
        return health;
    }
    
    @Override
    public Integer calculateSecurityScore() {
        SecurityDashboardDTO.ThreatStats stats = getThreatStats();
        
        // 计算威胁总分（权重：严重10分，高危5分，中危2分，低危1分）
        int threatScore = stats.getCritical() * 10 + 
                         stats.getHigh() * 5 + 
                         stats.getMedium() * 2 + 
                         stats.getLow();
        
        // 基础分100分，减去威胁分数
        int score = 100 - Math.min(threatScore, 100);
        
        return Math.max(score, 0);
    }
    
    @Override
    public String getSecurityLevel() {
        Integer score = calculateSecurityScore();
        
        if (score >= 80) {
            return "正常";
        } else if (score >= 60) {
            return "警告";
        } else {
            return "高危";
        }
    }
}
