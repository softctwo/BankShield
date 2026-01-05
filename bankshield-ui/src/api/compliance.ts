import request from '@/utils/request'

/**
 * 合规性检查API
 */

// 合规规则相关API
export const complianceRuleApi = {
  // 获取规则列表
  getRules: (params: any) => {
    return request.get('/api/compliance/rule/list', { params })
  },

  // 获取规则详情
  getRuleDetail: (id: number) => {
    return request.get(`/api/compliance/rule/${id}`)
  },

  // 创建规则
  createRule: (data: any) => {
    return request.post('/api/compliance/rule', data)
  },

  // 更新规则
  updateRule: (id: number, data: any) => {
    return request.put(`/api/compliance/rule/${id}`, data)
  },

  // 删除规则
  deleteRule: (id: number) => {
    return request.delete(`/api/compliance/rule/${id}`)
  },

  // 启用/禁用规则
  toggleRule: (id: number, enabled: boolean) => {
    return request.put(`/api/compliance/rule/${id}/toggle`, { enabled })
  }
}

// 合规检查任务相关API
export const complianceTaskApi = {
  // 获取任务列表
  getTasks: (params: any) => {
    return request.get('/api/compliance/task/list', { params })
  },

  // 获取任务详情
  getTaskDetail: (id: number) => {
    return request.get(`/api/compliance/task/${id}`)
  },

  // 创建检查任务
  createTask: (data: any) => {
    return request.post('/api/compliance/task', data)
  },

  // 启动检查任务
  startTask: (id: number) => {
    return request.post(`/api/compliance/task/${id}/start`)
  },

  // 停止检查任务
  stopTask: (id: number) => {
    return request.post(`/api/compliance/task/${id}/stop`)
  },

  // 删除任务
  deleteTask: (id: number) => {
    return request.delete(`/api/compliance/task/${id}`)
  },

  // 获取任务进度
  getTaskProgress: (id: number) => {
    return request.get(`/api/compliance/task/${id}/progress`)
  }
}

// 合规检查结果相关API
export const complianceResultApi = {
  // 获取检查结果列表
  getResults: (params: any) => {
    return request.get('/api/compliance/result/list', { params })
  },

  // 获取结果详情
  getResultDetail: (id: number) => {
    return request.get(`/api/compliance/result/${id}`)
  },

  // 分配整改负责人
  assignResult: (id: number, assignedTo: string) => {
    return request.put(`/api/compliance/result/${id}/assign`, { assignedTo })
  },

  // 更新整改状态
  updateRemediationStatus: (id: number, status: string, plan?: string) => {
    return request.put(`/api/compliance/result/${id}/remediation`, { status, plan })
  }
}

// 合规报告相关API
export const complianceReportApi = {
  // 获取报告列表
  getReports: (params: any) => {
    return request.get('/api/compliance/report/list', { params })
  },

  // 生成报告
  generateReport: (taskId: number, reportType: string) => {
    return request.post('/api/compliance/report/generate', { taskId, reportType })
  },

  // 下载报告
  downloadReport: (id: number) => {
    return request.get(`/api/compliance/report/${id}/download`, { responseType: 'blob' })
  },

  // 删除报告
  deleteReport: (id: number) => {
    return request.delete(`/api/compliance/report/${id}`)
  }
}

// 合规统计相关API
export const complianceStatsApi = {
  // 获取仪表板统计
  getDashboardStats: () => {
    return request.get('/api/compliance/stats/dashboard')
  },

  // 获取规则分类统计
  getRuleCategoryStats: () => {
    return request.get('/api/compliance/stats/rule-category')
  },

  // 获取合规趋势
  getComplianceTrend: (params: any) => {
    return request.get('/api/compliance/stats/trend', { params })
  },

  // 获取风险分布
  getRiskDistribution: () => {
    return request.get('/api/compliance/stats/risk-distribution')
  },

  // 获取整改进度
  getRemediationProgress: () => {
    return request.get('/api/compliance/stats/remediation-progress')
  }
}
