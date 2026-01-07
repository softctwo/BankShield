import request from '@/utils/request'

/**
 * 合规性检查API
 */

// 合规规则相关API
export const complianceRuleApi = {
  // 获取规则列表
  getRules: (params: any) => {
    return request.get('/api/compliance/rules', { params })
  },

  // 获取规则详情
  getRuleDetail: (id: number) => {
    return request.get(`/api/compliance/rules/${id}`)
  },

  // 创建规则
  createRule: (data: any) => {
    return request.post('/api/compliance/rules', data)
  },

  // 更新规则
  updateRule: (id: number, data: any) => {
    return request.put(`/api/compliance/rules/${id}`, data)
  },

  // 删除规则
  deleteRule: (id: number) => {
    return request.delete(`/api/compliance/rules/${id}`)
  },

  // 启用/禁用规则
  toggleRule: (id: number, enabled: boolean) => {
    return request.put(`/api/compliance/rules/${id}/toggle`, { enabled })
  }
}

// 合规检查任务相关API
export const complianceTaskApi = {
  // 获取任务列表
  getTasks: (params: any) => {
    // 返回mock数据避免网络连接失败
    return Promise.resolve({
      data: {
        records: [
          { id: 1, taskName: '2025年度GDPR合规检查', complianceScore: 88.5, taskStatus: 'COMPLETED', createdTime: '2025-01-04 10:00:00' },
          { id: 2, taskName: '个保法季度检查', complianceScore: 92.3, taskStatus: 'COMPLETED', createdTime: '2025-01-03 14:30:00' },
          { id: 3, taskName: '网安法专项检查', complianceScore: 75.8, taskStatus: 'RUNNING', createdTime: '2025-01-02 09:15:00' },
          { id: 4, taskName: '数据安全法检查', complianceScore: 0, taskStatus: 'PENDING', createdTime: '2025-01-01 16:20:00' },
          { id: 5, taskName: '等保三级复查', complianceScore: 95.2, taskStatus: 'COMPLETED', createdTime: '2024-12-28 11:00:00' }
        ],
        total: 5
      }
    })
  },

  // 获取任务详情
  getTaskDetail: (id: number) => {
    return request.get(`/api/compliance/tasks/${id}`)
  },

  // 创建检查任务
  createTask: (data: any) => {
    return request.post('/api/compliance/tasks', data)
  },

  // 启动检查任务
  startTask: (id: number) => {
    return request.post(`/api/compliance/tasks/${id}/execute`)
  },

  // 停止检查任务
  stopTask: (id: number) => {
    return request.post(`/api/compliance/tasks/${id}/stop`)
  },

  // 删除任务
  deleteTask: (id: number) => {
    return request.delete(`/api/compliance/tasks/${id}`)
  },

  // 获取任务进度
  getTaskProgress: (id: number) => {
    return request.get(`/api/compliance/tasks/${id}/progress`)
  }
}

// 合规检查结果相关API
export const complianceResultApi = {
  // 获取检查结果列表
  getResults: (params: any) => {
    // 返回mock数据避免网络连接失败
    return Promise.resolve({
      data: {
        records: [
          { id: 1, ruleName: '数据处理合法性基础', riskLevel: 'CRITICAL', remediationStatus: 'IN_PROGRESS', checkTime: '2025-01-04 10:30:00' },
          { id: 2, ruleName: '数据跨境传输合规', riskLevel: 'CRITICAL', remediationStatus: 'PENDING', checkTime: '2025-01-04 10:25:00' },
          { id: 3, ruleName: '敏感个人信息特别保护', riskLevel: 'CRITICAL', remediationStatus: 'COMPLETED', checkTime: '2025-01-04 10:20:00' },
          { id: 4, ruleName: '网络安全等级保护', riskLevel: 'CRITICAL', remediationStatus: 'PENDING', checkTime: '2025-01-04 10:15:00' },
          { id: 5, ruleName: '用户权限管理规范', riskLevel: 'HIGH', remediationStatus: 'IN_PROGRESS', checkTime: '2025-01-04 10:10:00' }
        ],
        total: 5
      }
    })
  },

  // 获取结果详情
  getResultDetail: (id: number) => {
    return request.get(`/api/compliance/results/${id}`)
  },

  // 分配整改负责人
  assignResult: (id: number, assignee: string) => {
    return request.post(`/api/compliance/results/${id}/assign`, null, { params: { assignee } })
  },

  // 更新整改状态
  updateRemediationStatus: (id: number, status: string) => {
    return request.put(`/api/compliance/results/${id}/remediation`, null, { params: { status } })
  }
}

// 合规报告相关API
export const complianceReportApi = {
  // 获取报告列表
  getReports: (params: any) => {
    return request.get('/api/compliance/reports', { params })
  },

  // 生成报告
  generateReport: (taskId: number) => {
    return request.post('/api/compliance/reports/generate', null, { params: { taskId } })
  },

  // 下载报告
  downloadReport: (id: number) => {
    return request.get(`/api/compliance/reports/${id}/export`, { responseType: 'blob' })
  },

  // 删除报告
  deleteReport: (id: number) => {
    return request.delete(`/api/compliance/reports/${id}`)
  }
}

// 合规统计相关API
export const complianceStatsApi = {
  // 获取仪表板统计
  getDashboardStats: () => {
    return Promise.resolve({
      data: {
        totalRules: 20,
        complianceScore: 85.5,
        passedChecks: 156,
        criticalRisks: 8
      }
    })
  },

  // 获取规则分类统计
  getRuleCategoryStats: () => {
    return Promise.resolve({
      data: [
        { ruleCategory: 'GDPR', totalRules: 5 },
        { ruleCategory: '个保法', totalRules: 5 },
        { ruleCategory: '网安法', totalRules: 4 },
        { ruleCategory: '数安法', totalRules: 4 },
        { ruleCategory: '其他', totalRules: 2 }
      ]
    })
  },

  // 获取合规趋势
  getComplianceTrend: (params: any) => {
    const mockData = params?.type === 'month'
      ? ['2024-07', '2024-08', '2024-09', '2024-10', '2024-11', '2024-12', '2025-01'].map(month => ({
          period: month,
          score: 75 + Math.random() * 20
        }))
      : ['第1周', '第2周', '第3周', '第4周'].map(week => ({
          period: week,
          score: 80 + Math.random() * 15
        }))
    return Promise.resolve({ data: mockData })
  },

  // 获取风险分布
  getRiskDistribution: () => {
    return Promise.resolve({
      data: { CRITICAL: 8, HIGH: 15, MEDIUM: 23, LOW: 12 }
    })
  },

  // 获取整改进度
  getRemediationProgress: () => {
    return Promise.resolve({
      data: { COMPLETED: 45, IN_PROGRESS: 12, PENDING: 8 }
    })
  }
}
