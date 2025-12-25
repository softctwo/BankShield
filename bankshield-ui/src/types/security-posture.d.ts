/**
 * 安全态势感知相关类型定义
 */

// 安全事件
export interface SecurityEvent {
  id: number;
  eventType: 'ATTACK' | 'VULNERABILITY' | 'ANOMALY' | 'POLICY_VIOLATION';
  riskLevel: 'HIGH' | 'MEDIUM' | 'LOW';
  eventSource: string;
  eventTitle: string;
  eventDetail: string;
  occurTime: string;
  discoverTime: string;
  processStatus: 'UNPROCESSED' | 'PROCESSING' | 'RESOLVED' | 'IGNORED';
  handler?: string;
  processTime?: string;
  processRemark?: string;
  assetId?: number;
  assetName?: string;
  threatIndicators?: string;
  location?: string;
  ipAddress?: string;
}

// 威胁情报
export interface ThreatIntelligence {
  id: number;
  threatType: 'MALWARE' | 'PHISHING' | 'DDOS' | 'INTRUSION' | 'DATA_LEAK';
  threatDescription: string;
  threatLevel: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  impactScope: string;
  discoverTime: string;
  threatSource: string;
  iocIndicators?: string;
  cveCodes?: string;
  referenceUrls?: string;
  recommendations?: string;
  confidenceLevel: 'HIGH' | 'MEDIUM' | 'LOW';
  status: 'ACTIVE' | 'RESOLVED' | 'EXPIRED';
  tags?: string;
  geoLocation?: string;
  targetIndustry?: string;
  attackTechniques?: string;
}

// 安全评分
export interface SecurityScore {
  securityScore: number;
  unprocessedAlertCount: number;
  unprocessedEventCount: number;
  activeThreatCount: number;
  systemHealth: number;
}

// 威胁统计
export interface ThreatStatistics {
  activeCount: number;
  todayCount: number;
  typeDistribution: Array<{
    threat_type: string;
    count: number;
  }>;
  levelDistribution: Array<{
    threat_level: string;
    count: number;
  }>;
  weekTrend: Array<{
    date: string;
    count: number;
  }>;
  geoDistribution: Array<{
    geo_location: string;
    count: number;
  }>;
  industryDistribution: Array<{
    target_industry: string;
    count: number;
  }>;
}

// 实时安全态势数据
export interface SecurityPostureData {
  // 核心安全指标
  securityScore: number;
  unprocessedAlertCount: number;
  unprocessedEventCount: number;
  onlineUserCount: number;

  // 威胁情报数据
  activeThreatCount: number;
  todayThreatCount: number;
  recentThreats: ThreatIntelligence[];

  // 安全事件数据
  recentEvents: SecurityEvent[];
  todayEventCount: number;
  riskDistribution: Array<{
    risk_level: string;
    count: number;
  }>;

  // 监控数据
  systemHealth: number;
  alertDistribution: Array<{
    alert_level: string;
    count: number;
  }>;
  alertTrend: Array<{
    time_label: string;
    count: number;
  }>;

  // 资产数据
  assetHealth: {
    total: number;
    healthy: number;
    unhealthy: number;
    healthRate: number;
  };
  sensitiveDataCount: number;

  // 密钥数据
  keyStatus: {
    activeCount: number;
    expiredCount: number;
    destroyedCount: number;
    expiringSoonCount: number;
  };

  // 权限数据
  roleViolationCount: number;

  // 24小时趋势数据
  event24HourTrend: Array<{
    time_label: string;
    count: number;
  }>;
  threat7DayTrend: Array<{
    date: string;
    count: number;
  }>;

  // 地理位置分布
  locationDistribution: Array<{
    location: string;
    count: number;
  }>;
  threatGeoDistribution: Array<{
    geo_location: string;
    count: number;
  }>;
}

// 分页结果
export interface PageResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

// 风险分布数据
export interface RiskDistribution {
  name: string;
  value: number;
  color?: string;
}

// 趋势数据
export interface TrendData {
  time: string;
  value: number;
}

// 地理位置数据
export interface GeoData {
  name: string;
  value: number;
  coord?: [number, number];
}

// 事件统计
export interface EventStatistics {
  totalEvents: number;
  highRiskEvents: number;
  unprocessedEvents: number;
  todayEvents: number;
}

// 威胁地图数据
export interface ThreatMapData {
  geoCoordMap: Record<string, [number, number]>;
  threatData: Array<{
    fromName: string;
    toName: string;
    coords: [[number, number], [number, number]];
    threatType: string;
    threatLevel: string;
  }>;
}

// 实时监控数据
export interface RealTimeData {
  timestamp: string;
  securityScore: number;
  threatCount: number;
  eventCount: number;
  alertCount: number;
}

// 大屏配置
export interface DashboardConfig {
  autoRefresh: boolean;
  refreshInterval: number;
  theme: 'dark' | 'light';
  layout: 'default' | 'compact';
  showAnimations: boolean;
  soundEnabled: boolean;
}