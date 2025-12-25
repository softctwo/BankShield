import request from '@/utils/request';
import {
  MonitorMetric,
  DashboardStats,
  AlertTrend,
  AlertDistribution,
  ResourceUsage,
  DatabaseStatus,
  ApplicationStatus,
  MetricQueryParams,
  AlertRecord
} from '@/types/monitor';

/**
 * 监控数据API
 */

// 获取当前监控指标
export function getCurrentMetrics() {
  return request.get<ResourceUsage & DatabaseStatus & ApplicationStatus>('/api/monitor/metrics/current');
}

// 获取历史监控数据
export function getHistoryMetrics(metricType?: string, metricName?: string, hours: number = 24) {
  return request.get<MonitorMetric[]>('/api/monitor/metrics/history', {
    params: {
      metricType,
      metricName,
      hours
    }
  });
}

// 获取Dashboard统计数据
export function getDashboardStats() {
  return request.get<DashboardStats>('/api/monitor/dashboard/stats');
}

// 获取24小时告警趋势
export function get24HourAlertTrend() {
  return request.get<AlertTrend[]>('/api/monitor/dashboard/alert-trend');
}

// 获取告警类型分布
export function getAlertDistribution() {
  return request.get<AlertDistribution[]>('/api/monitor/dashboard/alert-distribution');
}

// 获取最近告警
export function getRecentAlerts(limit: number = 10) {
  return request.get<AlertRecord[]>('/api/monitor/dashboard/recent-alerts', {
    params: { limit }
  });
}

// 手动触发监控数据采集
export function collectMetrics() {
  return request.post<string>('/api/monitor/metrics/collect');
}

// 获取系统健康度
export function getSystemHealth() {
  return request.get<{
    healthScore: number;
    status: string;
    checkTime: string;
  }>('/api/monitor/system/health');
}

// 获取系统资源使用情况
export function getSystemResourceUsage() {
  return request.get<ResourceUsage>('/api/monitor/metrics/current');
}

// 获取数据库状态
export function getDatabaseStatus() {
  return request.get<DatabaseStatus>('/api/monitor/metrics/current');
}

// 获取应用状态
export function getApplicationStatus() {
  return request.get<ApplicationStatus>('/api/monitor/metrics/current');
}

// 获取监控指标趋势
export function getMetricTrend(metricType: string, metricName: string, hours: number = 24) {
  return request.get<{
    time: string;
    value: number;
    status: string;
  }[]>('/api/monitor/metrics/history', {
    params: {
      metricType,
      metricName,
      hours
    }
  });
}