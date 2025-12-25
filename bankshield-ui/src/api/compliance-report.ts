import request from '@/utils/request'
import type { PageParams, PageResult } from '@/types/common'
import type { 
  ReportTemplate, 
  ReportGenerationTask, 
  ComplianceCheckResult, 
  ComplianceCheckItem,
  ComplianceCheckHistory
} from '@/types/compliance-report'

// 报表模板管理
export const getReportTemplates = (params: PageParams & {
  templateName?: string
  reportType?: string
  enabled?: boolean
}) => {
  return request<PageResult<ReportTemplate>>({
    url: '/api/report/template/page',
    method: 'get',
    params
  })
}

export const createReportTemplate = (data: ReportTemplate) => {
  return request<string>({
    url: '/api/report/template',
    method: 'post',
    data
  })
}

export const updateReportTemplate = (id: number, data: ReportTemplate) => {
  return request<string>({
    url: `/api/report/template/${id}`,
    method: 'put',
    data
  })
}

export const deleteReportTemplate = (id: number) => {
  return request<string>({
    url: `/api/report/template/${id}`,
    method: 'delete'
  })
}

export const getReportTemplateDetail = (id: number) => {
  return request<ReportTemplate>({
    url: `/api/report/template/${id}`,
    method: 'get'
  })
}

// 报表生成任务管理
export const createReportTask = (data: { 
  templateId: number
  reportPeriod: string 
}) => {
  return request<string>({
    url: '/api/report/task',
    method: 'post',
    data
  })
}

export const getTaskStatus = (taskId: number) => {
  return request<ReportGenerationTask>({
    url: `/api/report/task/${taskId}`,
    method: 'get'
  })
}

export const getReportTasks = (params: PageParams & {
  templateId?: number
  status?: string
  createdBy?: string
}) => {
  return request<PageResult<ReportGenerationTask>>({
    url: '/api/report/task/page',
    method: 'get',
    params
  })
}

// 报表下载
export const downloadReport = (taskId: number) => {
  return request<Blob>({
    url: `/api/report/download/${taskId}`,
    method: 'get',
    responseType: 'blob'
  })
}

// 合规检查管理
export const performComplianceCheck = (standard: string) => {
  return request<ComplianceCheckResult>({
    url: '/api/report/check',
    method: 'post',
    data: { standard }
  })
}

export const getComplianceCheckItems = (params: PageParams & {
  checkItemName?: string
  complianceStandard?: string
  passStatus?: string
}) => {
  return request<PageResult<ComplianceCheckItem>>({
    url: '/api/report/check/item/page',
    method: 'get',
    params
  })
}

export const getComplianceScore = (standard: string) => {
  return request<{
    standard: string
    score: number
    nonComplianceCount: number
    nonCompliances: ComplianceCheckItem[]
  }>({
    url: '/api/report/check/score',
    method: 'get',
    params: { standard }
  })
}

export const getComplianceCheckHistory = (params: PageParams & {
  complianceStandard?: string
  checker?: string
  startTime?: string
  endTime?: string
}) => {
  return request<PageResult<ComplianceCheckHistory>>({
    url: '/api/report/check/history/page',
    method: 'get',
    params
  })
}

// 统计信息
export const getReportStats = () => {
  return request<{
    totalTemplates: number
    enabledTemplates: number
    totalTasks: number
    pendingTasks: number
    runningTasks: number
    successTasks: number
    failedTasks: number
    dengBaoTrend: Array<{ checkTime: string; complianceScore: number }>
    pciDssTrend: Array<{ checkTime: string; complianceScore: number }>
    gdprTrend: Array<{ checkTime: string; complianceScore: number }>
    recentNonCompliances: ComplianceCheckItem[]
  }>({
    url: '/api/report/stats',
    method: 'get'
  })
}

// 导出报表URL生成
export const generateReportExportUrl = (taskId: number) => {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
  // 移除末尾的斜杠，避免 URL 重复拼接
  const normalizedBaseUrl = baseUrl.replace(/\/$/, '')
  return `${normalizedBaseUrl}/api/report/download/${taskId}`
}

// 批量操作
export const batchCreateReportTasks = (templateIds: number[], reportPeriod: string) => {
  const promises = templateIds.map(templateId => 
    createReportTask({ templateId, reportPeriod })
  )
  return Promise.all(promises)
}

// 获取支持的合规标准
export const getSupportedStandards = () => {
  return [
    { value: '等保', label: '等级保护' },
    { value: '等保二级', label: '等级保护二级' },
    { value: '等保三级', label: '等级保护三级' },
    { value: 'PCI-DSS', label: 'PCI-DSS' },
    { value: 'GDPR', label: 'GDPR' }
  ]
}

// 获取支持的报表类型
export const getSupportedReportTypes = () => {
  return [
    { value: '等保', label: '等级保护' },
    { value: 'PCI-DSS', label: 'PCI-DSS' },
    { value: 'GDPR', label: 'GDPR' },
    { value: '自定义', label: '自定义' }
  ]
}

// 获取支持的生成频率
export const getSupportedFrequencies = () => {
  return [
    { value: 'DAILY', label: '每日' },
    { value: 'WEEKLY', label: '每周' },
    { value: 'MONTHLY', label: '每月' },
    { value: 'QUARTERLY', label: '每季度' }
  ]
}

// 获取任务状态映射
export const getTaskStatusMap = () => {
  return {
    PENDING: { text: '待处理', color: 'default' },
    RUNNING: { text: '运行中', color: 'processing' },
    SUCCESS: { text: '成功', color: 'success' },
    FAILED: { text: '失败', color: 'error' }
  }
}

// 获取合规状态映射
export const getComplianceStatusMap = () => {
  return {
    PASS: { text: '通过', color: 'success' },
    FAIL: { text: '未通过', color: 'error' },
    UNKNOWN: { text: '未知', color: 'warning' },
    NOT_APPLICABLE: { text: '不适用', color: 'default' }
  }
}