package com.bankshield.api.controller;

import com.bankshield.common.result.Result;
import com.bankshield.api.entity.MonitorMetric;
import com.bankshield.api.service.MonitorDashboardService;
import com.bankshield.api.service.MonitorDataCollectionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 监控数据接口控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor")
@Api(tags = "监控管理")
public class MonitorController {

    @Autowired
    private MonitorDataCollectionService monitorDataCollectionService;

    @Autowired
    private MonitorDashboardService monitorDashboardService;

    @GetMapping("/metrics/current")
    @ApiOperation("获取当前监控指标")
    public Result<Map<String, Object>> getCurrentMetrics() {
        try {
            Map<String, Object> metrics = monitorDashboardService.getSystemResourceUsage();
            metrics.putAll(monitorDashboardService.getDatabaseStatus());
            metrics.putAll(monitorDashboardService.getApplicationStatus());
            
            return Result.success(metrics);
        } catch (Exception e) {
            log.error("获取当前监控指标失败", e);
            return Result.error("获取当前监控指标失败");
        }
    }

    @GetMapping("/metrics/history")
    @ApiOperation("获取历史监控数据")
    public Result<List<Map<String, Object>>> getHistoryMetrics(
            @ApiParam("指标类型") @RequestParam(required = false) String metricType,
            @ApiParam("指标名称") @RequestParam(required = false) String metricName,
            @ApiParam("时间范围(小时)") @RequestParam(defaultValue = "24") int hours) {
        
        try {
            List<Map<String, Object>> trend = monitorDashboardService.getMetricTrend(metricType, metricName, hours);
            return Result.success(trend);
        } catch (Exception e) {
            log.error("获取历史监控数据失败", e);
            return Result.error("获取历史监控数据失败");
        }
    }

    @GetMapping("/dashboard/stats")
    @ApiOperation("获取Dashboard统计数据")
    public Result<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> stats = monitorDashboardService.getDashboardStats();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取Dashboard统计数据失败", e);
            return Result.error("获取Dashboard统计数据失败");
        }
    }

    @GetMapping("/dashboard/alert-trend")
    @ApiOperation("获取24小时告警趋势")
    public Result<List<Map<String, Object>>> get24HourAlertTrend() {
        try {
            List<Map<String, Object>> trend = monitorDashboardService.get24HourAlertTrend();
            return Result.success(trend);
        } catch (Exception e) {
            log.error("获取24小时告警趋势失败", e);
            return Result.error("获取24小时告警趋势失败");
        }
    }

    @GetMapping("/dashboard/alert-distribution")
    @ApiOperation("获取告警类型分布")
    public Result<List<Map<String, Object>>> getAlertDistribution() {
        try {
            List<Map<String, Object>> distribution = monitorDashboardService.getAlertTypeDistribution();
            return Result.success(distribution);
        } catch (Exception e) {
            log.error("获取告警类型分布失败", e);
            return Result.error("获取告警类型分布失败");
        }
    }

    @GetMapping("/dashboard/recent-alerts")
    @ApiOperation("获取最近告警")
    public Result<List<Map<String, Object>>> getRecentAlerts(
            @ApiParam("数量限制") @RequestParam(defaultValue = "10") int limit) {
        
        try {
            List<Map<String, Object>> alerts = monitorDashboardService.getRecentAlerts(limit);
            return Result.success(alerts);
        } catch (Exception e) {
            log.error("获取最近告警失败", e);
            return Result.error("获取最近告警失败");
        }
    }

    @PostMapping("/metrics/collect")
    @ApiOperation("手动触发监控数据采集")
    public Result<String> collectMetrics() {
        try {
            monitorDataCollectionService.collectAllMetrics();
            return Result.success("监控数据采集任务已触发");
        } catch (Exception e) {
            log.error("触发监控数据采集失败", e);
            return Result.error("触发监控数据采集失败");
        }
    }

    @GetMapping("/system/health")
    @ApiOperation("获取系统健康度")
    public Result<Map<String, Object>> getSystemHealth() {
        try {
            Map<String, Object> health = new java.util.HashMap<>();
            health.put("healthScore", monitorDashboardService.getSystemHealthScore());
            health.put("status", getHealthStatus(monitorDashboardService.getSystemHealthScore()));
            health.put("checkTime", LocalDateTime.now());
            
            return Result.success(health);
        } catch (Exception e) {
            log.error("获取系统健康度失败", e);
            return Result.error("获取系统健康度失败");
        }
    }

    /**
     * 根据健康分数获取状态
     */
    private String getHealthStatus(double healthScore) {
        if (healthScore >= 90) {
            return "HEALTHY";
        } else if (healthScore >= 70) {
            return "WARNING";
        } else if (healthScore >= 50) {
            return "CRITICAL";
        } else {
            return "EMERGENCY";
        }
    }
}