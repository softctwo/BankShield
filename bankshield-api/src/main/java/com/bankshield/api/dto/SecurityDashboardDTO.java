package com.bankshield.api.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 安全态势仪表板数据DTO
 */
@Data
public class SecurityDashboardDTO {
    
    // 威胁统计
    private ThreatStats threatStats;
    
    // 攻击类型分布
    private List<Map<String, Object>> attackTypeDistribution;
    
    // 安全事件趋势（24小时）
    private List<Map<String, Object>> eventTrend;
    
    // 攻击来源地理分布
    private List<Map<String, Object>> geoDistribution;
    
    // 实时安全事件流
    private List<SecurityEventDTO> realtimeEvents;
    
    // 系统健康状态
    private SystemHealth systemHealth;
    
    // TOP10攻击源IP
    private List<Map<String, Object>> topAttackIPs;
    
    // 综合安全评分
    private Integer securityScore;
    
    // 安全等级
    private String securityLevel;
    
    @Data
    public static class ThreatStats {
        private Integer critical;
        private Integer high;
        private Integer medium;
        private Integer low;
    }
    
    @Data
    public static class SystemHealth {
        private Integer cpu;
        private Integer memory;
        private Integer disk;
        private Integer network;
    }
}
