package com.bankshield.api.service;

import com.bankshield.api.entity.MonitorMetric;

import java.util.List;

/**
 * 监控数据采集服务接口
 */
public interface MonitorDataCollectionService {

    /**
     * 采集系统指标
     */
    void collectSystemMetrics();

    /**
     * 采集数据库指标
     */
    void collectDatabaseMetrics();

    /**
     * 采集应用指标
     */
    void collectApplicationMetrics();

    /**
     * 采集安全指标
     */
    void collectSecurityMetrics();

    /**
     * 采集所有指标
     */
    void collectAllMetrics();

    /**
     * 保存监控指标
     */
    void saveMetric(MonitorMetric metric);

    /**
     * 批量保存监控指标
     */
    void batchSaveMetrics(List<MonitorMetric> metrics);

    /**
     * 获取当前系统CPU使用率
     */
    double getCpuUsage();

    /**
     * 获取当前系统内存使用率
     */
    double getMemoryUsage();

    /**
     * 获取当前系统磁盘使用率
     */
    double getDiskUsage();

    /**
     * 获取数据库连接数
     */
    int getDatabaseConnectionCount();

    /**
     * 获取活跃用户数
     */
    int getActiveUserCount();
}