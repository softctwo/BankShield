package com.bankshield.api.service;

import java.util.List;
import java.util.Map;

/**
 * 监控Dashboard服务接口
 */
public interface MonitorDashboardService {

    /**
     * 获取Dashboard统计数据
     */
    Map<String, Object> getDashboardStats();

    /**
     * 获取今日告警统计
     */
    Map<String, Object> getTodayAlertStats();

    /**
     * 获取系统健康度
     */
    double getSystemHealthScore();

    /**
     * 获取未处理告警数
     */
    long getUnresolvedAlertCount();

    /**
     * 获取24小时告警趋势
     */
    List<Map<String, Object>> get24HourAlertTrend();

    /**
     * 获取监控指标趋势
     */
    List<Map<String, Object>> getMetricTrend(String metricType, String metricName, int hours);

    /**
     * 获取告警类型分布
     */
    List<Map<String, Object>> getAlertTypeDistribution();

    /**
     * 获取最近告警
     */
    List<Map<String, Object>> getRecentAlerts(int limit);

    /**
     * 获取系统资源使用情况
     */
    Map<String, Object> getSystemResourceUsage();

    /**
     * 获取数据库状态
     */
    Map<String, Object> getDatabaseStatus();

    /**
     * 获取应用状态
     */
    Map<String, Object> getApplicationStatus();
}