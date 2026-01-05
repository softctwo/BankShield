package com.bankshield.api.service;

import java.util.List;
import java.util.Map;

/**
 * 性能监控服务接口
 */
public interface PerformanceMonitorService {

    /**
     * 获取系统性能指标
     *
     * @return 性能指标
     */
    Map<String, Object> getSystemMetrics();

    /**
     * 获取应用性能指标
     *
     * @return 应用指标
     */
    Map<String, Object> getApplicationMetrics();

    /**
     * 获取数据库性能指标
     *
     * @return 数据库指标
     */
    Map<String, Object> getDatabaseMetrics();

    /**
     * 获取缓存性能指标
     *
     * @return 缓存指标
     */
    Map<String, Object> getCacheMetrics();

    /**
     * 获取API性能统计
     *
     * @param timeRange 时间范围（小时）
     * @return API性能统计
     */
    List<Map<String, Object>> getApiPerformanceStats(int timeRange);

    /**
     * 获取慢查询列表
     *
     * @param threshold 阈值（毫秒）
     * @param limit 数量限制
     * @return 慢查询列表
     */
    List<Map<String, Object>> getSlowQueries(long threshold, int limit);

    /**
     * 获取性能趋势
     *
     * @param metric 指标名称
     * @param days 天数
     * @return 趋势数据
     */
    Map<String, Object> getPerformanceTrend(String metric, int days);

    /**
     * 记录性能指标
     *
     * @param metricName 指标名称
     * @param value 指标值
     * @param tags 标签
     */
    void recordMetric(String metricName, double value, Map<String, String> tags);

    /**
     * 获取性能告警
     *
     * @return 告警列表
     */
    List<Map<String, Object>> getPerformanceAlerts();

    /**
     * 生成性能报告
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 报告内容
     */
    Map<String, Object> generatePerformanceReport(String startTime, String endTime);
}
