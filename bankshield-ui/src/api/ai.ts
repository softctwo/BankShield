import request, { type ResponseData } from '@/utils/request'
import type { AI } from '@/types/api'

/**
 * AI智能安全分析API
 */

// 异常行为检测
export function detectAnomaly(data: AI.UserBehavior): Promise<ResponseData<number>> {
  return request({
    url: '/api/ai/behavior/detect',
    method: 'post',
    data
  })
}

// 详细异常行为检测
export function detectAnomalyDetail(data: AI.UserBehavior): Promise<ResponseData<AI.AnomalyResult>> {
  return request({
    url: '/api/ai/behavior/detect-detail',
    method: 'post',
    data
  })
}

// 批量异常行为检测
export function detectAnomalies(data: any) {
  return request({
    url: '/api/ai/behavior/detect-batch',
    method: 'post',
    data
  })
}

// 学习用户行为模式
export function learnUserBehaviorPattern(userId: number, data: any) {
  return request({
    url: '/api/ai/behavior/learn-pattern',
    method: 'post',
    params: { userId },
    data
  })
}

// 获取用户行为模式
export function getUserBehaviorPattern(userId: number) {
  return request({
    url: `/api/ai/behavior/pattern/${userId}`,
    method: 'get'
  })
}

// 获取异常行为统计
export function getAnomalyStatistics(params: any) {
  return request({
    url: '/api/ai/behavior/statistics',
    method: 'get',
    params
  })
}

// 智能告警分类
export function classifyAlert(alertId: number) {
  return request({
    url: '/api/ai/alert/smart-classify',
    method: 'get',
    params: { alertId }
  })
}

// 资源使用预测
export function predictResourceUsage() {
  return request({
    url: '/api/ai/prediction/resource',
    method: 'get'
  })
}

// 特定资源类型预测
export function predictSpecificResource(resourceType: string, days: number = 7) {
  return request({
    url: `/api/ai/prediction/resource/${resourceType}`,
    method: 'get',
    params: { days }
  })
}

// 威胁预测
export function predictThreats(days: number = 7): Promise<ResponseData<AI.ThreatPrediction>> {
  return request({
    url: '/api/ai/prediction/threat',
    method: 'get',
    params: { days }
  })
}

// 获取AI模型信息
export function getModelInfo(): Promise<ResponseData<AI.ModelInfo>> {
  return request({
    url: '/api/ai/model/info',
    method: 'get'
  })
}

// 健康检查
export function healthCheck() {
  return request({
    url: '/api/ai/health',
    method: 'get'
  })
}
