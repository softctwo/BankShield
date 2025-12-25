package com.bankshield.monitor.controller;

import com.bankshield.monitor.service.AlertingService;
import com.bankshield.monitor.service.HealthCheckService;
import com.bankshield.monitor.service.MetricsService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 监控控制器
 *
 * @author BankShield Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private HealthCheckService healthCheckService;

    @Autowired
    private AlertingService alertingService;

    @Autowired
    private MeterRegistry meterRegistry;

    /**
     * 获取系统健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Timer.Sample timer = metricsService.startApiTimer();
        
        try {
            Map<String, Object> health = new HashMap<>();
            
            // 获取服务健康状态
            Map<String, HealthCheckService.ServiceHealth> serviceHealth = healthCheckService.getAllServiceHealth();
            double overallHealthScore = healthCheckService.getOverallHealthScore();
            
            health.put("services", serviceHealth);
            health.put("overallHealthScore", overallHealthScore);
            health.put("status", overallHealthScore >= 80 ? "HEALTHY" : overallHealthScore >= 50 ? "DEGRADED" : "UNHEALTHY");
            health.put("timestamp", new Date());

            metricsService.recordApiResponseTime(timer, "GET", "/api/monitoring/health", "200");
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            metricsService.recordApiResponseTime(timer, "GET", "/api/monitoring/health", "500");
            throw e;
        }
    }

    /**
     * 获取监控指标
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics(@RequestParam(required = false) String metric) {
        Timer.Sample timer = metricsService.startApiTimer();
        
        try {
            Map<String, Object> metrics = new HashMap<>();
            
            if (metric != null && !metric.isEmpty()) {
                // 获取指定指标
                metrics.put(metric, getMetricValue(metric));
            } else {
                // 获取所有关键指标
                metrics.put("systemHealthScore", getMetricValue("bankshield_system_health_score"));
                metrics.put("onlineUsers", getMetricValue("bankshield_online_users"));
                metrics.put("securityAlerts", getMetricValue("bankshield_security_alerts_total"));
                metrics.put("dataProcessing", getMetricValue("bankshield_data_processing_total"));
                metrics.put("encryptionOperations", getMetricValue("bankshield_encryption_operations_total"));
                metrics.put("auditLogs", getMetricValue("bankshield_audit_logs_total"));
            }

            metricsService.recordApiResponseTime(timer, "GET", "/api/monitoring/metrics", "200");
            return ResponseEntity.ok(metrics);
            
        } catch (Exception e) {
            metricsService.recordApiResponseTime(timer, "GET", "/api/monitoring/metrics", "500");
            throw e;
        }
    }

    /**
     * 获取活跃告警
     */
    @GetMapping("/alerts")
    public ResponseEntity<List<AlertingService.Alert>> getActiveAlerts() {
        Timer.Sample timer = metricsService.startApiTimer();
        
        try {
            List<AlertingService.Alert> alerts = alertingService.getActiveAlerts();
            
            metricsService.recordApiResponseTime(timer, "GET", "/api/monitoring/alerts", "200");
            return ResponseEntity.ok(alerts);
            
        } catch (Exception e) {
            metricsService.recordApiResponseTime(timer, "GET", "/api/monitoring/alerts", "500");
            throw e;
        }
    }

    /**
     * 创建自定义告警
     */
    @PostMapping("/alerts")
    public ResponseEntity<Map<String, String>> createAlert(@RequestBody CreateAlertRequest request) {
        Timer.Sample timer = metricsService.startApiTimer();
        
        try {
            alertingService.sendAlert(
                    request.getAlertName(),
                    request.getSeverity(),
                    request.getSummary(),
                    request.getDescription(),
                    request.getLabels()
            );

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Alert created successfully");

            metricsService.recordApiResponseTime(timer, "POST", "/api/monitoring/alerts", "200");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            metricsService.recordApiResponseTime(timer, "POST", "/api/monitoring/alerts", "500");
            throw e;
        }
    }

    /**
     * 静默告警
     */
    @PostMapping("/alerts/silence")
    public ResponseEntity<Map<String, String>> silenceAlert(@RequestBody SilenceAlertRequest request) {
        Timer.Sample timer = metricsService.startApiTimer();
        
        try {
            alertingService.silenceAlert(
                    request.getAlertName(),
                    request.getDuration(),
                    request.getComment()
            );

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Alert silenced successfully");

            metricsService.recordApiResponseTime(timer, "POST", "/api/monitoring/alerts/silence", "200");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            metricsService.recordApiResponseTime(timer, "POST", "/api/monitoring/alerts/silence", "500");
            throw e;
        }
    }

    /**
     * 获取Dashboard数据
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Timer.Sample timer = metricsService.startApiTimer();
        
        try {
            Map<String, Object> dashboard = new HashMap<>();
            
            // 系统概览
            dashboard.put("systemHealthScore", healthCheckService.getOverallHealthScore());
            dashboard.put("activeServices", getActiveServiceCount());
            dashboard.put("totalServices", 4);
            dashboard.put("activeAlerts", alertingService.getActiveAlerts().size());
            
            // 关键指标
            dashboard.put("securityAlerts", getMetricValue("bankshield_security_alerts_total"));
            dashboard.put("dataProcessingRate", getMetricValue("bankshield_data_processing_total"));
            dashboard.put("encryptionOperations", getMetricValue("bankshield_encryption_operations_total"));
            dashboard.put("onlineUsers", getMetricValue("bankshield_online_users"));
            
            // 服务状态
            dashboard.put("serviceHealth", healthCheckService.getAllServiceHealth());
            
            // 告警统计
            dashboard.put("alertsBySeverity", getAlertsBySeverity());
            
            // 最近24小时趋势数据（这里可以集成时序数据库查询）
            dashboard.put("trends", getTrends());

            metricsService.recordApiResponseTime(timer, "GET", "/api/monitoring/dashboard", "200");
            return ResponseEntity.ok(dashboard);
            
        } catch (Exception e) {
            metricsService.recordApiResponseTime(timer, "GET", "/api/monitoring/dashboard", "500");
            throw e;
        }
    }

    /**
     * 获取指标值
     */
    private double getMetricValue(String metricName) {
        try {
            return meterRegistry.find(metricName).counter() != null ? 
                   meterRegistry.find(metricName).counter().count() : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 获取活跃服务数量
     */
    private long getActiveServiceCount() {
        return healthCheckService.getAllServiceHealth().values().stream()
                .filter(HealthCheckService.ServiceHealth::isHealthy)
                .count();
    }

    /**
     * 按严重级别统计告警
     */
    private Map<String, Long> getAlertsBySeverity() {
        Map<String, Long> alertsBySeverity = new HashMap<>();
        alertsBySeverity.put("critical", 0L);
        alertsBySeverity.put("warning", 0L);
        alertsBySeverity.put("info", 0L);
        
        // 这里可以集成实际的告警统计逻辑
        return alertsBySeverity;
    }

    /**
     * 获取趋势数据
     */
    private Map<String, Object> getTrends() {
        Map<String, Object> trends = new HashMap<>();
        
        // 模拟趋势数据，实际应该从时序数据库获取
        List<Object> cpuTrend = new ArrayList<>();
        cpuTrend.add(createTrendPoint("00:00", 45.2));
        cpuTrend.add(createTrendPoint("06:00", 52.1));
        cpuTrend.add(createTrendPoint("12:00", 68.5));
        cpuTrend.add(createTrendPoint("18:00", 73.2));
        cpuTrend.add(createTrendPoint("24:00", 58.9));

        List<Object> memoryTrend = new ArrayList<>();
        memoryTrend.add(createTrendPoint("00:00", 62.3));
        memoryTrend.add(createTrendPoint("06:00", 65.7));
        memoryTrend.add(createTrendPoint("12:00", 71.2));
        memoryTrend.add(createTrendPoint("18:00", 78.4));
        memoryTrend.add(createTrendPoint("24:00", 69.1));

        List<Object> alertTrend = new ArrayList<>();
        alertTrend.add(createTrendPoint("00:00", 2));
        alertTrend.add(createTrendPoint("06:00", 5));
        alertTrend.add(createTrendPoint("12:00", 8));
        alertTrend.add(createTrendPoint("18:00", 12));
        alertTrend.add(createTrendPoint("24:00", 6));
        
        trends.put("cpu", cpuTrend);
        trends.put("memory", memoryTrend);
        trends.put("alerts", alertTrend);
        
        return trends;
    }

    /**
     * 创建趋势数据点
     */
    private Map<String, Object> createTrendPoint(String time, Object value) {
        Map<String, Object> point = new HashMap<>();
        point.put("time", time);
        point.put("value", value);
        return point;
    }

    /**
     * 创建告警请求
     */
    public static class CreateAlertRequest {
        private String alertName;
        private String severity;
        private String summary;
        private String description;
        private Map<String, String> labels;

        // Getters and Setters
        public String getAlertName() { return alertName; }
        public void setAlertName(String alertName) { this.alertName = alertName; }
        
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Map<String, String> getLabels() { return labels; }
        public void setLabels(Map<String, String> labels) { this.labels = labels; }
    }

    /**
     * 静默告警请求
     */
    public static class SilenceAlertRequest {
        private String alertName;
        private String duration;
        private String comment;

        // Getters and Setters
        public String getAlertName() { return alertName; }
        public void setAlertName(String alertName) { this.alertName = alertName; }
        
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
        
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }
}