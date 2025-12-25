import request from '@/utils/request';
import { 
  SecurityPostureData, 
  SecurityEvent, 
  ThreatIntelligence, 
  SecurityScore,
  ThreatStatistics,
  PageResult
} from '@/types/security-posture';

/**
 * 获取实时安全态势数据
 */
export function getRealTimeSecurityPosture() {
  return request.get<SecurityPostureData>('/api/security/posture/realtime');
}

/**
 * 分页查询安全事件
 */
export function getSecurityEventPage(params: {
  page: number;
  size: number;
  eventType?: string;
  riskLevel?: string;
  processStatus?: string;
  startTime?: string;
  endTime?: string;
}) {
  return request.get<PageResult<SecurityEvent>>('/api/security/events/page', { params });
}

/**
 * 获取威胁情报列表
 */
export function getThreatIntelligencePage(params: {
  page: number;
  size: number;
  threatType?: string;
  threatLevel?: string;
  status?: string;
  startTime?: string;
  endTime?: string;
}) {
  return request.get<PageResult<ThreatIntelligence>>('/api/security/threats', { params });
}

/**
 * 获取活跃威胁情报
 */
export function getActiveThreats(limit: number = 10) {
  return request.get<ThreatIntelligence[]>('/api/security/threats/active', {
    params: { limit }
  });
}

/**
 * 获取威胁情报统计
 */
export function getThreatStatistics() {
  return request.get<ThreatStatistics>('/api/security/threats/statistics');
}

/**
 * 根据ID获取威胁情报详情
 */
export function getThreatById(id: number) {
  return request.get<ThreatIntelligence>(`/api/security/threats/${id}`);
}

/**
 * 保存威胁情报
 */
export function saveThreatIntelligence(data: ThreatIntelligence) {
  return request.post('/api/security/threats', data);
}

/**
 * 更新威胁情报状态
 */
export function updateThreatStatus(id: number, status: string) {
  return request.put(`/api/security/threats/${id}/status`, null, {
    params: { status }
  });
}

/**
 * 删除威胁情报
 */
export function deleteThreatIntelligence(id: number) {
  return request.delete(`/api/security/threats/${id}`);
}

/**
 * 获取安全评分
 */
export function getSecurityScore() {
  return request.get<SecurityScore>('/api/security/score');
}

/**
 * 获取威胁类型选项
 */
export function getThreatTypeOptions() {
  return [
    { label: '恶意软件', value: 'MALWARE' },
    { label: '钓鱼攻击', value: 'PHISHING' },
    { label: '拒绝服务', value: 'DDOS' },
    { label: '网络入侵', value: 'INTRUSION' },
    { label: '数据泄露', value: 'DATA_LEAK' }
  ];
}

/**
 * 获取威胁等级选项
 */
export function getThreatLevelOptions() {
  return [
    { label: '严重', value: 'CRITICAL', color: '#F56C6C' },
    { label: '高危', value: 'HIGH', color: '#E6A23C' },
    { label: '中危', value: 'MEDIUM', color: '#E6A23C' },
    { label: '低危', value: 'LOW', color: '#67C23A' }
  ];
}

/**
 * 获取事件类型选项
 */
export function getEventTypeOptions() {
  return [
    { label: '攻击事件', value: 'ATTACK' },
    { label: '漏洞事件', value: 'VULNERABILITY' },
    { label: '异常行为', value: 'ANOMALY' },
    { label: '策略违规', value: 'POLICY_VIOLATION' }
  ];
}

/**
 * 获取风险级别选项
 */
export function getRiskLevelOptions() {
  return [
    { label: '高风险', value: 'HIGH', color: '#F56C6C' },
    { label: '中风险', value: 'MEDIUM', color: '#E6A23C' },
    { label: '低风险', value: 'LOW', color: '#67C23A' }
  ];
}

/**
 * 获取处理状态选项
 */
export function getProcessStatusOptions() {
  return [
    { label: '未处理', value: 'UNPROCESSED', color: '#909399' },
    { label: '处理中', value: 'PROCESSING', color: '#409EFF' },
    { label: '已解决', value: 'RESOLVED', color: '#67C23A' },
    { label: '已忽略', value: 'IGNORED', color: '#C0C4CC' }
  ];
}

/**
 * 获取威胁情报状态选项
 */
export function getThreatStatusOptions() {
  return [
    { label: '活跃', value: 'ACTIVE', color: '#F56C6C' },
    { label: '已解决', value: 'RESOLVED', color: '#67C23A' },
    { label: '已过期', value: 'EXPIRED', color: '#C0C4CC' }
  ];
}