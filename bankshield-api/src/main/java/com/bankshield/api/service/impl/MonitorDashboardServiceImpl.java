package com.bankshield.api.service.impl;

import com.bankshield.api.entity.MonitorMetric;
import com.bankshield.api.enums.AlertLevel;
import com.bankshield.api.mapper.AlertRecordMapper;
import com.bankshield.api.mapper.MonitorMetricMapper;
import com.bankshield.api.mapper.NotificationConfigMapper;
import com.bankshield.api.service.MonitorDashboardService;
import com.bankshield.api.service.MonitorDataCollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 监控Dashboard服务实现类
 */
@Slf4j
@Service
public class MonitorDashboardServiceImpl implements MonitorDashboardService {

    @Autowired
    private MonitorMetricMapper monitorMetricMapper;

    @Autowired
    private AlertRecordMapper alertRecordMapper;

    @Autowired
    private NotificationConfigMapper notificationConfigMapper;

    @Autowired
    private MonitorDataCollectionService monitorDataCollectionService;

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0);
            
            // 今日告警统计
            Map<String, Object> todayAlerts = alertRecordMapper.countAlerts(startOfDay, now);
            stats.put("todayAlertCount", todayAlerts.get("totalCount"));
            stats.put("todayResolvedCount", todayAlerts.get("resolvedCount"));
            
            // 未处理告警数
            long unresolvedCount = getUnresolvedAlertCount();
            stats.put("unresolvedAlertCount", unresolvedCount);
            
            // 系统健康度
            double healthScore = getSystemHealthScore();
            stats.put("systemHealthScore", healthScore);
            
            // 当前监控指标
            MonitorMetric cpuMetric = monitorMetricMapper.selectLatestMetric("SYSTEM", "CPU使用率");
            MonitorMetric memoryMetric = monitorMetricMapper.selectLatestMetric("SYSTEM", "内存使用率");
            
            stats.put("currentCpuUsage", cpuMetric != null ? cpuMetric.getMetricValue() : 0);
            stats.put("currentMemoryUsage", memoryMetric != null ? memoryMetric.getMetricValue() : 0);
            
            // 活跃通知配置数
            long activeNotificationCount = notificationConfigMapper.selectEnabledConfigs().size();
            stats.put("activeNotificationCount", activeNotificationCount);
            
        } catch (Exception e) {
            log.error("获取Dashboard统计数据失败", e);
            // 返回默认值
            stats.put("todayAlertCount", 0);
            stats.put("todayResolvedCount", 0);
            stats.put("unresolvedAlertCount", 0);
            stats.put("systemHealthScore", 100.0);
            stats.put("currentCpuUsage", 0);
            stats.put("currentMemoryUsage", 0);
            stats.put("activeNotificationCount", 0);
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getTodayAlertStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0);
            
            // 总体统计
            Map<String, Object> totalStats = alertRecordMapper.countAlerts(startOfDay, now);
            stats.putAll(totalStats);
            
            // 各级别告警统计
            List<Map<String, Object>> levelStats = alertRecordMapper.countAlertsByLevel(startOfDay, now);
            Map<String, Integer> levelCountMap = new HashMap<>();
            for (Map<String, Object> levelStat : levelStats) {
                String level = (String) levelStat.get("alertLevel");
                Integer count = ((Number) levelStat.get("count")).intValue();
                levelCountMap.put(level, count);
            }
            
            stats.put("levelStats", levelCountMap);
            
        } catch (Exception e) {
            log.error("获取今日告警统计失败", e);
            stats.put("totalCount", 0);
            stats.put("resolvedCount", 0);
            stats.put("unresolvedCount", 0);
            stats.put("levelStats", new HashMap<>());
        }
        
        return stats;
    }

    @Override
    public double getSystemHealthScore() {
        try {
            double score = 100.0;
            
            // CPU使用率扣分
            MonitorMetric cpuMetric = monitorMetricMapper.selectLatestMetric("SYSTEM", "CPU使用率");
            if (cpuMetric != null && cpuMetric.getMetricValue() != null) {
                double cpuUsage = cpuMetric.getMetricValue();
                if (cpuUsage > 90) {
                    score -= 30;
                } else if (cpuUsage > 80) {
                    score -= 20;
                } else if (cpuUsage > 70) {
                    score -= 10;
                }
            }
            
            // 内存使用率扣分
            MonitorMetric memoryMetric = monitorMetricMapper.selectLatestMetric("SYSTEM", "内存使用率");
            if (memoryMetric != null && memoryMetric.getMetricValue() != null) {
                double memoryUsage = memoryMetric.getMetricValue();
                if (memoryUsage > 90) {
                    score -= 30;
                } else if (memoryUsage > 80) {
                    score -= 20;
                } else if (memoryUsage > 70) {
                    score -= 10;
                }
            }
            
            // 未处理告警扣分
            long unresolvedCount = getUnresolvedAlertCount();
            if (unresolvedCount > 10) {
                score -= 25;
            } else if (unresolvedCount > 5) {
                score -= 15;
            } else if (unresolvedCount > 0) {
                score -= 5;
            }
            
            // 确保分数在0-100之间
            return Math.max(0, Math.min(100, score));
            
        } catch (Exception e) {
            log.error("计算系统健康度失败", e);
            return 100.0; // 默认健康
        }
    }

    @Override
    public long getUnresolvedAlertCount() {
        try {
            // 查询未处理告警数
            List<com.bankshield.api.entity.AlertRecord> unresolvedAlerts = alertRecordMapper.selectUnresolvedAlerts(1);
            return unresolvedAlerts.size();
        } catch (Exception e) {
            log.error("获取未处理告警数失败", e);
            return 0;
        }
    }

    @Override
    public List<Map<String, Object>> get24HourAlertTrend() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime = now.minusHours(24);
            
            // 获取告警趋势数据
            List<Map<String, Object>> trendData = alertRecordMapper.getAlertTrend(startTime, now, "HOUR");
            
            // 补充缺失的时间点
            Map<String, Map<String, Object>> trendMap = new HashMap<>();
            for (Map<String, Object> data : trendData) {
                String timeLabel = (String) data.get("timeLabel");
                trendMap.put(timeLabel, data);
            }
            
            // 生成完整的时间序列
            List<Map<String, Object>> completeTrend = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            
            for (int i = 23; i >= 0; i--) {
                LocalDateTime timePoint = now.minusHours(i);
                String timeLabel = timePoint.format(formatter);
                
                Map<String, Object> data = trendMap.getOrDefault(timeLabel, new HashMap<>());
                data.put("timeLabel", timeLabel);
                if (!data.containsKey("count")) {
                    data.put("count", 0);
                }
                
                completeTrend.add(data);
            }
            
            return completeTrend;
            
        } catch (Exception e) {
            log.error("获取24小时告警趋势失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getMetricTrend(String metricType, String metricName, int hours) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime = now.minusHours(hours);
            
            // 获取指标历史数据
            List<MonitorMetric> metrics = monitorMetricMapper.selectMetricsByTimeRange(
                    metricType, metricName, startTime, now);
            
            // 转换为趋势数据
            List<Map<String, Object>> trend = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");
            
            for (MonitorMetric metric : metrics) {
                Map<String, Object> point = new HashMap<>();
                point.put("time", metric.getCollectTime().format(formatter));
                point.put("value", metric.getMetricValue());
                point.put("status", metric.getStatus());
                trend.add(point);
            }
            
            return trend;
            
        } catch (Exception e) {
            log.error("获取指标趋势失败: metricType={}, metricName={}", metricType, metricName, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getAlertTypeDistribution() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime = now.minusDays(7); // 最近7天
            
            // 获取告警类型分布
            List<Map<String, Object>> distribution = alertRecordMapper.countAlertsByLevel(startTime, now);
            
            // 添加颜色信息
            for (Map<String, Object> item : distribution) {
                String level = (String) item.get("alertLevel");
                AlertLevel alertLevel = AlertLevel.valueOf(level);
                item.put("color", alertLevel.getColor());
                item.put("name", alertLevel.getDescription());
            }
            
            return distribution;
            
        } catch (Exception e) {
            log.error("获取告警类型分布失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getRecentAlerts(int limit) {
        try {
            List<com.bankshield.api.entity.AlertRecord> alertRecords = alertRecordMapper.getRecentAlerts(limit);
            // 转换为 Map 列表
            List<Map<String, Object>> result = new ArrayList<>();
            for (com.bankshield.api.entity.AlertRecord record : alertRecords) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", record.getId());
                map.put("alertLevel", record.getAlertLevel());
                map.put("alertStatus", record.getAlertStatus());
                map.put("alertTitle", record.getAlertTitle());
                map.put("alertContent", record.getAlertContent());
                map.put("alertTime", record.getAlertTime());
                result.add(map);
            }
            return result;
        } catch (Exception e) {
            log.error("获取最近告警失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> getSystemResourceUsage() {
        Map<String, Object> usage = new HashMap<>();
        
        try {
            // CPU使用率
            MonitorMetric cpuMetric = monitorMetricMapper.selectLatestMetric("SYSTEM", "CPU使用率");
            if (cpuMetric != null) {
                Map<String, Object> cpu = new HashMap<>();
                cpu.put("value", cpuMetric.getMetricValue());
                cpu.put("status", cpuMetric.getStatus());
                cpu.put("unit", cpuMetric.getMetricUnit());
                usage.put("cpu", cpu);
            }
            
            // 内存使用率
            MonitorMetric memoryMetric = monitorMetricMapper.selectLatestMetric("SYSTEM", "内存使用率");
            if (memoryMetric != null) {
                Map<String, Object> memory = new HashMap<>();
                memory.put("value", memoryMetric.getMetricValue());
                memory.put("status", memoryMetric.getStatus());
                memory.put("unit", memoryMetric.getMetricUnit());
                usage.put("memory", memory);
            }
            
            // 磁盘使用率
            MonitorMetric diskMetric = monitorMetricMapper.selectLatestMetric("SYSTEM", "磁盘使用率");
            if (diskMetric != null) {
                Map<String, Object> disk = new HashMap<>();
                disk.put("value", diskMetric.getMetricValue());
                disk.put("status", diskMetric.getStatus());
                disk.put("unit", diskMetric.getMetricUnit());
                usage.put("disk", disk);
            }
            
        } catch (Exception e) {
            log.error("获取系统资源使用情况失败", e);
        }
        
        return usage;
    }

    @Override
    public Map<String, Object> getDatabaseStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // 数据库连接数
            MonitorMetric connectionMetric = monitorMetricMapper.selectLatestMetric("DATABASE", "数据库连接数");
            if (connectionMetric != null) {
                Map<String, Object> connection = new HashMap<>();
                connection.put("value", connectionMetric.getMetricValue());
                connection.put("status", connectionMetric.getStatus());
                connection.put("unit", connectionMetric.getMetricUnit());
                status.put("connectionCount", connection);
            }
            
            // 慢查询数量
            MonitorMetric slowQueryMetric = monitorMetricMapper.selectLatestMetric("DATABASE", "慢查询数量");
            if (slowQueryMetric != null) {
                Map<String, Object> slowQuery = new HashMap<>();
                slowQuery.put("value", slowQueryMetric.getMetricValue());
                slowQuery.put("status", slowQueryMetric.getStatus());
                slowQuery.put("unit", slowQueryMetric.getMetricUnit());
                status.put("slowQueryCount", slowQuery);
            }
            
        } catch (Exception e) {
            log.error("获取数据库状态失败", e);
        }
        
        return status;
    }

    @Override
    public Map<String, Object> getApplicationStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // 应用线程数
            MonitorMetric threadMetric = monitorMetricMapper.selectLatestMetric("SERVICE", "应用线程数");
            if (threadMetric != null) {
                Map<String, Object> thread = new HashMap<>();
                thread.put("value", threadMetric.getMetricValue());
                thread.put("status", threadMetric.getStatus());
                thread.put("unit", threadMetric.getMetricUnit());
                status.put("threadCount", thread);
            }
            
            // GC次数
            MonitorMetric gcMetric = monitorMetricMapper.selectLatestMetric("SERVICE", "GC次数");
            if (gcMetric != null) {
                Map<String, Object> gc = new HashMap<>();
                gc.put("value", gcMetric.getMetricValue());
                gc.put("status", gcMetric.getStatus());
                gc.put("unit", gcMetric.getMetricUnit());
                status.put("gcCount", gc);
            }
            
        } catch (Exception e) {
            log.error("获取应用状态失败", e);
        }
        
        return status;
    }
}