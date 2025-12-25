import request from '@/utils/request'
import type { MetricsData, ChartData } from '@/types/dashboard'

/**
 * 仪表板相关 API
 */

/**
 * 获取仪表板指标数据
 */
export const getMetrics = () => {
  return request.get<MetricsData>('/api/dashboard/metrics')
}

/**
 * 获取图表数据
 */
export const getCharts = (chartTypes: string[]) => {
  return request.get<ChartData>('/api/dashboard/charts', {
    params: { types: chartTypes.join(',') }
  })
}

/**
 * 获取仪表板配置
 */
export const getConfig = () => {
  return request.get('/api/dashboard/config')
}

/**
 * 保存仪表板配置
 */
export const saveConfig = (config: any) => {
  return request.put('/api/dashboard/config', config)
}

/**
 * 获取告警统计
 */
export const getAlertStats = () => {
  return request.get('/api/dashboard/alert-stats')
}

/**
 * 获取安全态势评分
 */
export const getSecurityScore = () => {
  return request.get('/api/dashboard/security-score')
}

// 导出 dashboardApi 对象以兼容 import { dashboardApi }
export const dashboardApi = {
  getMetrics,
  getCharts,
  getConfig,
  saveConfig,
  getAlertStats,
  getSecurityScore
}

export default dashboardApi
