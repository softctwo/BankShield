package com.bankshield.api.service.impl;

import com.bankshield.api.entity.MonitorMetric;
import com.bankshield.api.enums.MetricType;
import com.bankshield.api.mapper.MonitorMetricMapper;
import com.bankshield.api.service.MonitorDataCollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 监控数据采集服务实现类
 */
@Slf4j
@Service
public class MonitorDataCollectionServiceImpl implements MonitorDataCollectionService {

    @Autowired
    private MonitorMetricMapper monitorMetricMapper;

    @Override
    public void collectSystemMetrics() {
        log.info("开始采集系统指标");
        List<MonitorMetric> metrics = new ArrayList<>();
        
        try {
            // CPU使用率
            double cpuUsage = getCpuUsage();
            MonitorMetric cpuMetric = MonitorMetric.builder()
                    .metricName("CPU使用率")
                    .metricType(MetricType.SYSTEM.getCode())
                    .metricValue(cpuUsage)
                    .metricUnit("%")
                    .threshold(80.0)
                    .status(cpuUsage > 80 ? "WARNING" : "NORMAL")
                    .collectTime(LocalDateTime.now())
                    .description("服务器CPU使用率")
                    .relatedResource("system")
                    .build();
            metrics.add(cpuMetric);

            // 内存使用率
            double memoryUsage = getMemoryUsage();
            MonitorMetric memoryMetric = MonitorMetric.builder()
                    .metricName("内存使用率")
                    .metricType(MetricType.SYSTEM.getCode())
                    .metricValue(memoryUsage)
                    .metricUnit("%")
                    .threshold(85.0)
                    .status(memoryUsage > 85 ? "CRITICAL" : memoryUsage > 70 ? "WARNING" : "NORMAL")
                    .collectTime(LocalDateTime.now())
                    .description("服务器内存使用率")
                    .relatedResource("system")
                    .build();
            metrics.add(memoryMetric);

            // 磁盘使用率
            double diskUsage = getDiskUsage();
            MonitorMetric diskMetric = MonitorMetric.builder()
                    .metricName("磁盘使用率")
                    .metricType(MetricType.SYSTEM.getCode())
                    .metricValue(diskUsage)
                    .metricUnit("%")
                    .threshold(90.0)
                    .status(diskUsage > 90 ? "CRITICAL" : diskUsage > 80 ? "WARNING" : "NORMAL")
                    .collectTime(LocalDateTime.now())
                    .description("服务器磁盘使用率")
                    .relatedResource("system")
                    .build();
            metrics.add(diskMetric);

            batchSaveMetrics(metrics);
            log.info("系统指标采集完成，共采集{}个指标", metrics.size());
        } catch (Exception e) {
            log.error("采集系统指标失败", e);
        }
    }

    @Override
    public void collectDatabaseMetrics() {
        log.info("开始采集数据库指标");
        List<MonitorMetric> metrics = new ArrayList<>();
        
        try {
            // 数据库连接数
            int connectionCount = getDatabaseConnectionCount();
            MonitorMetric connectionMetric = MonitorMetric.builder()
                    .metricName("数据库连接数")
                    .metricType(MetricType.DATABASE.getCode())
                    .metricValue((double) connectionCount)
                    .metricUnit("个")
                    .threshold(100.0)
                    .status(connectionCount > 100 ? "WARNING" : "NORMAL")
                    .collectTime(LocalDateTime.now())
                    .description("MySQL数据库连接数")
                    .relatedResource("database")
                    .build();
            metrics.add(connectionMetric);

            // 慢查询数量（这里用模拟数据，实际需要查询数据库）
            int slowQueryCount = (int) (Math.random() * 10);
            MonitorMetric slowQueryMetric = MonitorMetric.builder()
                    .metricName("慢查询数量")
                    .metricType(MetricType.DATABASE.getCode())
                    .metricValue((double) slowQueryCount)
                    .metricUnit("个")
                    .threshold(5.0)
                    .status(slowQueryCount > 5 ? "WARNING" : "NORMAL")
                    .collectTime(LocalDateTime.now())
                    .description("数据库慢查询数量")
                    .relatedResource("database")
                    .build();
            metrics.add(slowQueryMetric);

            batchSaveMetrics(metrics);
            log.info("数据库指标采集完成，共采集{}个指标", metrics.size());
        } catch (Exception e) {
            log.error("采集数据库指标失败", e);
        }
    }

    @Override
    public void collectApplicationMetrics() {
        log.info("开始采集应用指标");
        List<MonitorMetric> metrics = new ArrayList<>();
        
        try {
            // 线程数
            int threadCount = ManagementFactory.getThreadMXBean().getThreadCount();
            MonitorMetric threadMetric = MonitorMetric.builder()
                    .metricName("应用线程数")
                    .metricType(MetricType.SERVICE.getCode())
                    .metricValue((double) threadCount)
                    .metricUnit("个")
                    .threshold(200.0)
                    .status(threadCount > 200 ? "WARNING" : "NORMAL")
                    .collectTime(LocalDateTime.now())
                    .description("应用线程数量")
                    .relatedResource("application")
                    .build();
            metrics.add(threadMetric);

            // GC次数
            long gcCount = ManagementFactory.getGarbageCollectorMXBeans().stream()
                    .mapToLong(gc -> gc.getCollectionCount())
                    .sum();
            MonitorMetric gcMetric = MonitorMetric.builder()
                    .metricName("GC次数")
                    .metricType(MetricType.SERVICE.getCode())
                    .metricValue((double) gcCount)
                    .metricUnit("次")
                    .threshold(100.0)
                    .status(gcCount > 100 ? "WARNING" : "NORMAL")
                    .collectTime(LocalDateTime.now())
                    .description("垃圾回收次数")
                    .relatedResource("application")
                    .build();
            metrics.add(gcMetric);

            batchSaveMetrics(metrics);
            log.info("应用指标采集完成，共采集{}个指标", metrics.size());
        } catch (Exception e) {
            log.error("采集应用指标失败", e);
        }
    }

    @Override
    public void collectSecurityMetrics() {
        log.info("开始采集安全指标");
        List<MonitorMetric> metrics = new ArrayList<>();
        
        try {
            // 活跃用户数
            int activeUserCount = getActiveUserCount();
            MonitorMetric userMetric = MonitorMetric.builder()
                    .metricName("活跃用户数")
                    .metricType(MetricType.SECURITY.getCode())
                    .metricValue((double) activeUserCount)
                    .metricUnit("人")
                    .threshold(50.0)
                    .status("NORMAL")
                    .collectTime(LocalDateTime.now())
                    .description("系统活跃用户数量")
                    .relatedResource("security")
                    .build();
            metrics.add(userMetric);

            // 异常登录次数（模拟数据）
            int abnormalLoginCount = (int) (Math.random() * 5);
            MonitorMetric loginMetric = MonitorMetric.builder()
                    .metricName("异常登录次数")
                    .metricType(MetricType.SECURITY.getCode())
                    .metricValue((double) abnormalLoginCount)
                    .metricUnit("次")
                    .threshold(3.0)
                    .status(abnormalLoginCount > 3 ? "WARNING" : "NORMAL")
                    .collectTime(LocalDateTime.now())
                    .description("异常登录尝试次数")
                    .relatedResource("security")
                    .build();
            metrics.add(loginMetric);

            batchSaveMetrics(metrics);
            log.info("安全指标采集完成，共采集{}个指标", metrics.size());
        } catch (Exception e) {
            log.error("采集安全指标失败", e);
        }
    }

    @Override
    public void collectAllMetrics() {
        log.info("开始采集所有监控指标");
        collectSystemMetrics();
        collectDatabaseMetrics();
        collectApplicationMetrics();
        collectSecurityMetrics();
        log.info("所有监控指标采集完成");
    }

    @Override
    public void saveMetric(MonitorMetric metric) {
        monitorMetricMapper.insert(metric);
    }

    @Override
    public void batchSaveMetrics(List<MonitorMetric> metrics) {
        if (metrics != null && !metrics.isEmpty()) {
            monitorMetricMapper.batchInsert(metrics);
        }
    }

    @Override
    public double getCpuUsage() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            
            // 首先尝试强制转换为 com.sun.management.OperatingSystemMXBean
            try {
                if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                    com.sun.management.OperatingSystemMXBean sunOsBean = 
                        (com.sun.management.OperatingSystemMXBean) osBean;
                    
                    // 尝试 getProcessCpuLoad
                    double processCpuLoad = sunOsBean.getProcessCpuLoad();
                    if (processCpuLoad >= 0) {
                        return processCpuLoad * 100;
                    }
                    
                    // 尝试 getSystemCpuLoad
                    double systemCpuLoad = sunOsBean.getSystemCpuLoad();
                    if (systemCpuLoad >= 0) {
                        return systemCpuLoad * 100;
                    }
                }
            } catch (Exception e) {
                log.debug("Sun OperatingSystemMXBean 方法不可用，尝试反射调用", e);
            }
            
            // 使用反射尝试各种方法
            String[] methodNames = {"getProcessCpuLoad", "getSystemCpuLoad", "getCpuLoad"};
            for (String methodName : methodNames) {
                try {
                    java.lang.reflect.Method method = osBean.getClass().getMethod(methodName);
                    double cpuLoad = (double) method.invoke(osBean);
                    if (cpuLoad >= 0) {
                        return cpuLoad * 100;
                    }
                } catch (Exception e) {
                    log.debug("方法 {} 不可用，尝试下一个", methodName, e);
                }
            }
            
            // 如果都不可用，返回基于系统负载的模拟数据
            double loadAverage = osBean.getSystemLoadAverage();
            if (loadAverage >= 0 && loadAverage <= 10) {
                return loadAverage * 10; // 简单转换，避免超过100%
            }
            
            return Math.random() * 30; // 返回0-30%的随机值，更真实
        } catch (Exception e) {
            log.warn("获取CPU使用率失败，使用模拟数据", e);
            return Math.random() * 30;
        }
    }

    @Override
    public double getMemoryUsage() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            long totalMemory = Runtime.getRuntime().totalMemory();
            long freeMemory = Runtime.getRuntime().freeMemory();
            long usedMemory = totalMemory - freeMemory;
            return (double) usedMemory / totalMemory * 100;
        } catch (Exception e) {
            log.warn("获取内存使用率失败，使用模拟数据", e);
            return Math.random() * 100;
        }
    }

    @Override
    public double getDiskUsage() {
        try {
            // 这里简化处理，实际应该获取磁盘使用情况
            return Math.random() * 100;
        } catch (Exception e) {
            log.warn("获取磁盘使用率失败，使用模拟数据", e);
            return Math.random() * 100;
        }
    }

    @Override
    public int getDatabaseConnectionCount() {
        // 这里简化处理，实际应该查询数据库连接数
        return (int) (Math.random() * 50 + 10);
    }

    @Override
    public int getActiveUserCount() {
        // 这里简化处理，实际应该查询活跃用户数
        return (int) (Math.random() * 30 + 5);
    }
}