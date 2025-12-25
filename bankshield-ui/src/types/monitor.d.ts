/**
 * 监控相关类型定义
 */

// 监控指标类型
export interface MonitorMetric {
  id: number;
  metricName: string;
  metricType: MetricType;
  metricValue: number;
  metricUnit?: string;
  threshold?: number;
  status: MetricStatus;
  collectTime: string;
  description?: string;
  relatedResource?: string;
}

// 告警规则类型
export interface AlertRule {
  id: number;
  ruleName: string;
  ruleType: string;
  monitorMetric: string;
  triggerCondition: TriggerCondition;
  threshold: number;
  alertLevel: AlertLevel;
  enabled: number;
  createTime: string;
  updateTime: string;
  description?: string;
}

// 告警记录类型
export interface AlertRecord {
  id: number;
  ruleId: number;
  alertLevel: AlertLevel;
  alertTitle: string;
  alertContent: string;
  alertTime: string;
  alertStatus: AlertStatus;
  handler?: string;
  handleTime?: string;
  handleRemark?: string;
  notifyStatus: NotifyStatus;
}

// 通知配置类型
export interface NotificationConfig {
  id: number;
  notifyType: NotifyType;
  recipients: string;
  notifyTemplate?: string;
  enabled: number;
  configParams?: string;
  createTime: string;
  updateTime: string;
  description?: string;
}

// 监控指标类型枚举
export enum MetricType {
  SYSTEM = 'SYSTEM',
  SECURITY = 'SECURITY',
  DATABASE = 'DATABASE',
  SERVICE = 'SERVICE'
}

// 指标状态枚举
export enum MetricStatus {
  NORMAL = 'NORMAL',
  WARNING = 'WARNING',
  CRITICAL = 'CRITICAL'
}

// 告警级别枚举
export enum AlertLevel {
  INFO = 'INFO',
  WARNING = 'WARNING',
  CRITICAL = 'CRITICAL',
  EMERGENCY = 'EMERGENCY'
}

// 告警状态枚举
export enum AlertStatus {
  UNRESOLVED = 'UNRESOLVED',
  RESOLVED = 'RESOLVED',
  IGNORED = 'IGNORED'
}

// 通知状态枚举
export enum NotifyStatus {
  UNNOTIFIED = 'UNNOTIFIED',
  NOTIFIED = 'NOTIFIED',
  FAILED = 'FAILED'
}

// 通知类型枚举
export enum NotifyType {
  EMAIL = 'EMAIL',
  SMS = 'SMS',
  WEBHOOK = 'WEBHOOK',
  DINGTALK = 'DINGTALK',
  WECHAT = 'WECHAT'
}

// 触发条件枚举
export enum TriggerCondition {
  GREATER_THAN = '>',
  LESS_THAN = '<',
  EQUAL = '=',
  GREATER_EQUAL = '>=',
  LESS_EQUAL = '<=',
  NOT_EQUAL = '!='
}

// 系统健康状态枚举
export enum HealthStatus {
  HEALTHY = 'HEALTHY',
  WARNING = 'WARNING',
  CRITICAL = 'CRITICAL',
  EMERGENCY = 'EMERGENCY'
}

// Dashboard统计数据类型
export interface DashboardStats {
  todayAlertCount: number;
  todayResolvedCount: number;
  unresolvedAlertCount: number;
  systemHealthScore: number;
  currentCpuUsage: number;
  currentMemoryUsage: number;
  activeNotificationCount: number;
}

// 告警趋势数据类型
export interface AlertTrend {
  timeLabel: string;
  count: number;
}

// 告警分布数据类型
export interface AlertDistribution {
  alertLevel: AlertLevel;
  count: number;
  color: string;
  name: string;
}

// 监控资源使用情况类型
export interface ResourceUsage {
  cpu: ResourceMetric;
  memory: ResourceMetric;
  disk: ResourceMetric;
}

// 单个资源指标类型
export interface ResourceMetric {
  value: number;
  status: MetricStatus;
  unit: string;
}

// 数据库状态类型
export interface DatabaseStatus {
  connectionCount: ResourceMetric;
  slowQueryCount: ResourceMetric;
}

// 应用状态类型
export interface ApplicationStatus {
  threadCount: ResourceMetric;
  gcCount: ResourceMetric;
}

// 告警查询参数类型
export interface AlertQueryParams {
  page?: number;
  size?: number;
  startTime?: string;
  endTime?: string;
  alertLevel?: AlertLevel;
  alertStatus?: AlertStatus;
  keyword?: string;
}

// 通知配置查询参数类型
export interface NotificationQueryParams {
  page?: number;
  size?: number;
  notifyType?: NotifyType;
  enabled?: number;
}

// WebSocket消息类型
export interface WebSocketMessage {
  type: string;
  content: string;
  timestamp: number;
}

// 监控指标查询参数类型
export interface MetricQueryParams {
  metricType?: MetricType;
  metricName?: string;
  status?: MetricStatus;
  startTime?: string;
  endTime?: string;
  page?: number;
  size?: number;
}