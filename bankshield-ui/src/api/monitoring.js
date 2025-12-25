import request from '@/utils/request'

/**
 * 获取系统健康状态
 */
export function getSystemHealth() {
  return request({
    url: '/monitoring/health',
    method: 'get'
  })
}

/**
 * 获取监控指标
 */
export function getMetrics(params) {
  return request({
    url: '/monitoring/metrics',
    method: 'get',
    params
  })
}

/**
 * 获取活跃告警
 */
export function getActiveAlerts() {
  return request({
    url: '/monitoring/alerts',
    method: 'get'
  })
}

/**
 * 创建自定义告警
 */
export function createCustomAlert(data) {
  return request({
    url: '/monitoring/alerts',
    method: 'post',
    data
  })
}

/**
 * 静默告警
 */
export function silenceAlert(data) {
  return request({
    url: '/monitoring/alerts/silence',
    method: 'post',
    data
  })
}

/**
 * 获取Dashboard数据
 */
export function getDashboardData() {
  return request({
    url: '/monitoring/dashboard',
    method: 'get'
  })
}

/**
 * 获取服务健康状态
 */
export function getServiceHealth(service) {
  return request({
    url: `/monitoring/services/${service}/health`,
    method: 'get'
  })
}

/**
 * 获取服务指标
 */
export function getServiceMetrics(service, params) {
  return request({
    url: `/monitoring/services/${service}/metrics`,
    method: 'get',
    params
  })
}

/**
 * 获取业务指标
 */
export function getBusinessMetrics(params) {
  return request({
    url: '/monitoring/business/metrics',
    method: 'get',
    params
  })
}

/**
 * 获取安全指标
 */
export function getSecurityMetrics(params) {
  return request({
    url: '/monitoring/security/metrics',
    method: 'get',
    params
  })
}

/**
 * 获取性能指标
 */
export function getPerformanceMetrics(params) {
  return request({
    url: '/monitoring/performance/metrics',
    method: 'get',
    params
  })
}

/**
 * 获取合规指标
 */
export function getComplianceMetrics(params) {
  return request({
    url: '/monitoring/compliance/metrics',
    method: 'get',
    params
  })
}

/**
 * 获取告警历史
 */
export function getAlertHistory(params) {
  return request({
    url: '/monitoring/alerts/history',
    method: 'get',
    params
  })
}

/**
 * 获取告警统计
 */
export function getAlertStatistics(params) {
  return request({
    url: '/monitoring/alerts/statistics',
    method: 'get',
    params
  })
}

/**
 * 获取系统拓扑
 */
export function getSystemTopology() {
  return request({
    url: '/monitoring/topology',
    method: 'get'
  })
}

/**
 * 获取日志分析
 */
export function getLogAnalysis(params) {
  return request({
    url: '/monitoring/logs/analysis',
    method: 'get',
    params
  })
}

/**
 * 导出监控数据
 */
export function exportMonitoringData(params) {
  return request({
    url: '/monitoring/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

/**
 * 获取Prometheus查询结果
 */
export function queryPrometheus(query, time) {
  return request({
    url: '/monitoring/prometheus/query',
    method: 'get',
    params: { query, time }
  })
}

/**
 * 获取Prometheus范围查询结果
 */
export function queryPrometheusRange(query, start, end, step) {
  return request({
    url: '/monitoring/prometheus/query_range',
    method: 'get',
    params: { query, start, end, step }
  })
}