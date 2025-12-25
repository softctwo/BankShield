import request from '@/utils/request'
import type { SecurityScanTask, SecurityScanResult, SecurityBaseline } from '@/types/security-scan'

/**
 * 安全扫描API
 */

// 扫描任务相关API
export const scanTaskApi = {
  // 创建扫描任务
  createTask: (task: Partial<SecurityScanTask>) => {
    return request.post('/api/security-scan/task', task)
  },

  // 执行扫描任务
  executeTask: (taskId: number) => {
    return request.post(`/api/security-scan/task/${taskId}/execute`)
  },

  // 停止扫描任务
  stopTask: (taskId: number) => {
    return request.post(`/api/security-scan/task/${taskId}/stop`)
  },

  // 获取扫描任务列表
  getTasks: (params: {
    page: number
    size: number
    taskName?: string
    scanType?: string
    status?: string
  }) => {
    return request.get('/api/security-scan/task', { params })
  },

  // 获取扫描任务详情
  getTaskDetail: (taskId: number) => {
    return request.get(`/api/security-scan/task/${taskId}`)
  },

  // 获取扫描任务进度
  getTaskProgress: (taskId: number) => {
    return request.get(`/api/security-scan/task/${taskId}/progress`)
  },

  // 删除扫描任务
  deleteTask: (taskId: number) => {
    return request.delete(`/api/security-scan/task/${taskId}`)
  },

  // 批量删除扫描任务
  batchDeleteTasks: (taskIds: number[]) => {
    return request.delete('/api/security-scan/task/batch', { data: taskIds })
  },

  // 获取扫描任务统计信息
  getTaskStatistics: () => {
    return request.get('/api/security-scan/task/statistics')
  },

  // 获取最近扫描任务
  getRecentTasks: (limit: number = 10) => {
    return request.get('/api/security-scan/task/recent', { params: { limit } })
  },

  // 获取任务执行日志
  getTaskExecutionLog: (taskId: number) => {
    return request.get(`/api/security-scan/task/${taskId}/logs`)
  }
}

// 扫描结果相关API
export const scanResultApi = {
  // 获取扫描结果列表
  getResults: (params: {
    page: number
    size: number
    taskId: number
    riskLevel?: string
    fixStatus?: string
  }) => {
    return request.get(`/api/security-scan/task/${params.taskId}/results`, { params })
  },

  // 标记风险为已修复
  markAsFixed: (resultId: number, fixRemark: string) => {
    return request.put(`/api/security-scan/result/${resultId}/fix`, fixRemark)
  },

  // 批量标记风险为已修复
  batchMarkAsFixed: (resultIds: number[], fixRemark: string) => {
    return request.put('/api/security-scan/result/batch-fix', { resultIds, fixRemark })
  }
}

// 扫描报告相关API
export const scanReportApi = {
  // 生成扫描报告
  generateReport: (taskId: number) => {
    return request.post(`/api/security-scan/report/${taskId}`)
  }
}

// 安全基线相关API
export const securityBaselineApi = {
  // 获取所有安全基线
  getAllBaselines: () => {
    return request.get('/api/security-scan/baseline/all')
  },

  // 获取启用的安全基线
  getEnabledBaselines: () => {
    return request.get('/api/security-scan/baseline/enabled')
  },

  // 更新安全基线
  updateBaseline: (id: number, baseline: Partial<SecurityBaseline>) => {
    return request.put(`/api/security-scan/baseline/${id}`, baseline)
  },

  // 批量更新基线启用状态
  batchUpdateBaselineEnabled: (ids: number[], enabled: boolean) => {
    return request.put('/api/security-scan/baseline/batch-enabled', { ids, enabled })
  }
}