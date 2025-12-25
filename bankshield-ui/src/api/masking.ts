import request from '@/utils/request'
import type { PageResult } from '@/types/common'

/**
 * 脱敏规则相关接口
 */

export interface DataMaskingRule {
  id?: number
  ruleName: string
  sensitiveDataType: string
  maskingAlgorithm: string
  algorithmParams?: string
  applicableScenarios?: string
  enabled?: boolean
  createTime?: string
  updateTime?: string
  createdBy?: string
  description?: string
}

export interface MaskingTestRequest {
  testData: string
  sensitiveDataType: string
  maskingAlgorithm: string
  algorithmParams?: string
}

export interface MaskingTestResponse {
  originalData: string
  maskedData: string
  sensitiveDataType: string
  maskingAlgorithm: string
  algorithmParams?: string
}

/**
 * 分页查询脱敏规则
 */
export function getMaskingRulePage(params: {
  pageNum: number
  pageSize: number
  ruleName?: string
  sensitiveDataType?: string
  scenario?: string
  enabled?: boolean
}) {
  return request.get('/api/masking/rule/page', { params })
}

/**
 * 获取规则详情
 */
export function getMaskingRuleDetail(id: number) {
  return request.get(`/api/masking/rule/${id}`)
}

/**
 * 创建脱敏规则
 */
export function createMaskingRule(data: DataMaskingRule) {
  return request.post('/api/masking/rule', data)
}

/**
 * 更新脱敏规则
 */
export function updateMaskingRule(data: DataMaskingRule) {
  return request.put('/api/masking/rule', data)
}

/**
 * 删除脱敏规则
 */
export function deleteMaskingRule(id: number) {
  return request.delete(`/api/masking/rule/${id}`)
}

/**
 * 更新规则状态
 */
export function updateMaskingRuleStatus(id: number, enabled: boolean) {
  return request.put(`/api/masking/rule/${id}/status`, null, { params: { enabled } })
}

/**
 * 测试脱敏规则
 */
export function testMaskingRule(data: MaskingTestRequest) {
  return request.post('/api/masking/test', data)
}

/**
 * 获取支持的脱敏算法列表
 */
export function getMaskingAlgorithms() {
  return request.get('/api/masking/algorithms')
}

/**
 * 获取敏感数据类型列表
 */
export function getSensitiveDataTypes() {
  return request.get('/api/masking/sensitive-types')
}

/**
 * 获取适用场景列表
 */
export function getMaskingScenarios() {
  return request.get('/api/masking/scenarios')
}