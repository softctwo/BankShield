import request from '@/utils/request';
import { 
  AlertRule, 
  AlertRecord, 
  AlertQueryParams,
  AlertLevel,
  AlertStatus,
  TriggerCondition
} from '@/types/monitor';

/**
 * 告警规则与记录API
 */

// 告警规则相关API

// 分页查询告警规则
export function getAlertRulePage(params: {
  page?: number;
  size?: number;
  ruleName?: string;
  ruleType?: string;
  alertLevel?: AlertLevel;
  enabled?: number;
}) {
  return request.get<{
    records: AlertRule[];
    total: number;
    size: number;
    current: number;
  }>('/api/alert/rule/page', { params });
}

// 获取告警规则详情
export function getAlertRule(id: number) {
  return request.get<AlertRule>(`/api/alert/rule/${id}`);
}

// 创建告警规则
export function createAlertRule(data: AlertRule) {
  return request.post<string>('/api/alert/rule', data);
}

// 更新告警规则
export function updateAlertRule(id: number, data: AlertRule) {
  return request.put<string>(`/api/alert/rule/${id}`, data);
}

// 删除告警规则
export function deleteAlertRule(id: number) {
  return request.delete<string>(`/api/alert/rule/${id}`);
}

// 启用/禁用告警规则
export function toggleAlertRule(id: number, enabled: number) {
  return request.put<string>(`/api/alert/rule/${id}/enable`, null, {
    params: { enabled }
  });
}

// 批量启用/禁用告警规则
export function batchToggleAlertRules(ids: number[], enabled: number) {
  return request.put<string>('/api/alert/rule/batch/enable', null, {
    params: { ids, enabled }
  });
}

// 测试告警规则
export function testAlertRule(id: number) {
  return request.post<string>(`/api/alert/rule/${id}/test`);
}

// 获取所有启用的告警规则
export function getEnabledAlertRules() {
  return request.get<AlertRule[]>('/api/alert/rule/enabled');
}

// 告警记录相关API

// 分页查询告警记录
export function getAlertRecordPage(params: AlertQueryParams) {
  return request.get<{
    records: AlertRecord[];
    total: number;
    size: number;
    current: number;
  }>('/api/alert/record/page', { params });
}

// 获取告警详情
export function getAlertRecord(id: number) {
  return request.get<AlertRecord>(`/api/alert/record/${id}`);
}

// 处理告警
export function resolveAlert(id: number, handler: string, handleRemark?: string) {
  return request.put<string>(`/api/alert/record/${id}/resolve`, null, {
    params: { handler, handleRemark }
  });
}

// 忽略告警
export function ignoreAlert(id: number, handler: string, handleRemark?: string) {
  return request.put<string>(`/api/alert/record/${id}/ignore`, null, {
    params: { handler, handleRemark }
  });
}

// 批量处理告警
export function batchResolveAlerts(ids: number[], handler: string, handleRemark?: string) {
  return request.post<string>('/api/alert/record/batch/resolve', null, {
    params: { ids, handler, handleRemark }
  });
}

// 批量忽略告警
export function batchIgnoreAlerts(ids: number[], handler: string, handleRemark?: string) {
  return request.post<string>('/api/alert/record/batch/ignore', null, {
    params: { ids, handler, handleRemark }
  });
}

// 获取未处理告警数
export function getUnresolvedAlertCount() {
  return request.get<{
    count: number;
    timestamp: string;
  }>('/api/alert/record/unresolved/count');
}

// 获取最近未处理告警
export function getRecentUnresolvedAlerts(limit: number = 10) {
  return request.get<AlertRecord[]>('/api/alert/record/unresolved/recent', {
    params: { limit }
  });
}

// 获取今日告警统计
export function getTodayAlertStats() {
  return request.get<{
    totalCount: number;
    resolvedCount: number;
    unresolvedCount: number;
    levelDistribution: {
      alertLevel: AlertLevel;
      count: number;
    }[];
  }>('/api/alert/record/stats/today');
}

// 获取告警分布统计
export function getAlertDistribution() {
  return request.get<{
    levelDistribution: {
      alertLevel: AlertLevel;
      count: number;
      color: string;
      name: string;
    }[];
    trend: {
      timeLabel: string;
      count: number;
    }[];
  }>('/api/alert/record/stats/distribution');
}

// 辅助函数

// 获取告警级别颜色
export function getAlertLevelColor(level: AlertLevel): string {
  const colorMap = {
    [AlertLevel.INFO]: '#909399',
    [AlertLevel.WARNING]: '#E6A23C',
    [AlertLevel.CRITICAL]: '#F56C6C',
    [AlertLevel.EMERGENCY]: '#FF0000'
  };
  return colorMap[level];
}

// 获取告警状态标签
export function getAlertStatusLabel(status: AlertStatus): string {
  const labelMap = {
    [AlertStatus.UNRESOLVED]: '未处理',
    [AlertStatus.RESOLVED]: '已处理',
    [AlertStatus.IGNORED]: '已忽略'
  };
  return labelMap[status];
}

// 获取触发条件标签
export function getTriggerConditionLabel(condition: TriggerCondition): string {
  const labelMap = {
    [TriggerCondition.GREATER_THAN]: '大于',
    [TriggerCondition.LESS_THAN]: '小于',
    [TriggerCondition.EQUAL]: '等于',
    [TriggerCondition.GREATER_EQUAL]: '大于等于',
    [TriggerCondition.LESS_EQUAL]: '小于等于',
    [TriggerCondition.NOT_EQUAL]: '不等于'
  };
  return labelMap[condition];
}