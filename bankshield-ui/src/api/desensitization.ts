import { request } from '@/utils/request'

// ==================== 脱敏规则相关接口 ====================

/**
 * 分页查询脱敏规则
 */
export function getRules(params: {
  current: number
  size: number
  ruleName?: string
  dataType?: string
  status?: string
}) {
  return request({
    url: '/api/desensitization/rules',
    method: 'get',
    params
  })
}

/**
 * 根据ID查询脱敏规则
 */
export function getRuleById(id: number) {
  return request({
    url: `/api/desensitization/rules/${id}`,
    method: 'get'
  })
}

/**
 * 新增脱敏规则
 */
export function createRule(data: any) {
  return request({
    url: '/api/desensitization/rules',
    method: 'post',
    data
  })
}

/**
 * 更新脱敏规则
 */
export function updateRule(id: number, data: any) {
  return request({
    url: `/api/desensitization/rules/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除脱敏规则
 */
export function deleteRule(id: number) {
  return request({
    url: `/api/desensitization/rules/${id}`,
    method: 'delete'
  })
}

/**
 * 更新脱敏规则状态
 */
export function updateRuleStatus(id: number, status: string) {
  return request({
    url: `/api/desensitization/rules/${id}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 测试脱敏规则
 */
export function testRule(id: number, testData: string) {
  return request({
    url: `/api/desensitization/rules/${id}/test`,
    method: 'post',
    params: { testData }
  })
}

/**
 * 查询所有启用的脱敏规则
 */
export function getEnabledRules() {
  return request({
    url: '/api/desensitization/rules/enabled',
    method: 'get'
  })
}

// ==================== 脱敏模板相关接口 ====================

/**
 * 分页查询脱敏模板
 */
export function getTemplates(params: {
  current: number
  size: number
  templateName?: string
  templateType?: string
  status?: string
}) {
  return request({
    url: '/api/desensitization/templates',
    method: 'get',
    params
  })
}

/**
 * 根据ID查询脱敏模板
 */
export function getTemplateById(id: number) {
  return request({
    url: `/api/desensitization/templates/${id}`,
    method: 'get'
  })
}

/**
 * 新增脱敏模板
 */
export function createTemplate(data: any) {
  return request({
    url: '/api/desensitization/templates',
    method: 'post',
    data
  })
}

/**
 * 更新脱敏模板
 */
export function updateTemplate(id: number, data: any) {
  return request({
    url: `/api/desensitization/templates/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除脱敏模板
 */
export function deleteTemplate(id: number) {
  return request({
    url: `/api/desensitization/templates/${id}`,
    method: 'delete'
  })
}

/**
 * 更新脱敏模板状态
 */
export function updateTemplateStatus(id: number, status: string) {
  return request({
    url: `/api/desensitization/templates/${id}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 应用脱敏模板
 */
export function applyTemplate(id: number, scheduleTime?: string) {
  return request({
    url: `/api/desensitization/templates/${id}/apply`,
    method: 'post',
    params: { scheduleTime }
  })
}

// ==================== 脱敏日志相关接口 ====================

/**
 * 分页查询脱敏日志
 */
export function getLogs(params: {
  current: number
  size: number
  userName?: string
  targetTable?: string
  logType?: string
  status?: string
  startTime?: string
  endTime?: string
}) {
  return request({
    url: '/api/desensitization/logs',
    method: 'get',
    params
  })
}

/**
 * 根据ID查询脱敏日志详情
 */
export function getLogById(id: number) {
  return request({
    url: `/api/desensitization/logs/${id}`,
    method: 'get'
  })
}

/**
 * 查询脱敏日志统计
 */
export function getLogStatistics(params?: {
  startTime?: string
  endTime?: string
}) {
  return request({
    url: '/api/desensitization/logs/statistics',
    method: 'get',
    params
  })
}

/**
 * 导出脱敏日志
 */
export function exportLogs(params?: {
  userName?: string
  targetTable?: string
  logType?: string
  status?: string
  startTime?: string
  endTime?: string
}) {
  return request({
    url: '/api/desensitization/logs/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// ==================== 脱敏测试相关接口 ====================

/**
 * 单条数据脱敏测试
 */
export function testSingle(ruleCode: string, testData: string) {
  return request({
    url: '/api/desensitization/test/single',
    method: 'post',
    params: { ruleCode, testData }
  })
}

/**
 * 批量数据脱敏测试
 */
export function testBatch(ruleCode: string, testDataList: string[]) {
  return request({
    url: '/api/desensitization/test/batch',
    method: 'post',
    params: { ruleCode },
    data: testDataList
  })
}

/**
 * 快捷脱敏测试
 */
export function quickTest(dataType: string, testData: string) {
  return request({
    url: '/api/desensitization/quick-test',
    method: 'post',
    params: { dataType, testData }
  })
}
