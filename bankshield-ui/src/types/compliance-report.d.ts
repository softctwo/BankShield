/**
 * 合规报表相关类型定义
 */

// 报表模板
export interface ReportTemplate {
  id?: number
  templateName: string
  reportType: '等保' | 'PCI-DSS' | 'GDPR' | '自定义'
  templateFilePath?: string
  generationFrequency: 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'QUARTERLY'
  enabled: boolean
  description?: string
  templateConfig?: string
  templateParams?: string
  createTime?: string
  updateTime?: string
}

// 报表生成任务
export interface ReportGenerationTask {
  id?: number
  templateId: number
  status: 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED'
  startTime?: string
  endTime?: string
  reportFilePath?: string
  createdBy?: string
  errorMessage?: string
  reportPeriod?: string
  reportData?: string
  createTime?: string
  updateTime?: string
  // 关联数据
  templateName?: string
  templateType?: string
}

// 合规检查项
export interface ComplianceCheckItem {
  id?: number
  checkItemName: string
  complianceStandard: string
  checkType: string
  passStatus: 'PASS' | 'FAIL' | 'UNKNOWN' | 'NOT_APPLICABLE'
  checkResult: string
  checkTime?: string
  nextCheckTime?: string
  responsiblePerson?: string
  enabled?: boolean
  checkDescription?: string
  remediation?: string
  createTime?: string
  updateTime?: string
}

// 合规检查历史
export interface ComplianceCheckHistory {
  id?: number
  complianceStandard: string
  checkResult: string
  complianceScore: number
  checkTime?: string
  checker?: string
  reportPath?: string
  createTime?: string
  updateTime?: string
}

// 合规检查结果
export interface ComplianceCheckResult {
  standard: string
  checkTime: string
  complianceScore: number
  passCount: number
  failCount: number
  totalCount: number
  nonCompliances: ComplianceCheckItem[]
  allItems: ComplianceCheckItem[]
}

// 报表统计数据
export interface ReportStats {
  // 模板统计
  totalTemplates: number
  enabledTemplates: number
  // 任务统计
  totalTasks: number
  pendingTasks: number
  runningTasks: number
  successTasks: number
  failedTasks: number
  // 合规趋势
  dengBaoTrend: ComplianceTrendItem[]
  pciDssTrend: ComplianceTrendItem[]
  gdprTrend: ComplianceTrendItem[]
  // 最近不合规项
  recentNonCompliances: ComplianceCheckItem[]
}

// 合规趋势项
export interface ComplianceTrendItem {
  checkTime: string
  complianceScore: number
}

// 任务查询参数
export interface TaskQueryParams {
  templateId?: number
  status?: string
  createdBy?: string
  startTime?: string
  endTime?: string
}

// 模板查询参数
export interface TemplateQueryParams {
  templateName?: string
  reportType?: string
  enabled?: boolean
}

// 合规检查查询参数
export interface ComplianceQueryParams {
  checkItemName?: string
  complianceStandard?: string
  passStatus?: string
  startTime?: string
  endTime?: string
}

// 报表生成参数
export interface ReportGenerationParams {
  templateId: number
  reportPeriod: string
  parameters?: Record<string, any>
}

// 报表文件信息
export interface ReportFileInfo {
  taskId: number
  fileName: string
  filePath: string
  fileSize: number
  downloadUrl: string
}

// 合规标准配置
export interface ComplianceStandardConfig {
  standard: string
  label: string
  description: string
  requirements: ComplianceRequirement[]
}

// 合规要求
export interface ComplianceRequirement {
  id: string
  name: string
  description: string
  checkItems: string[]
}

// 报表模板配置
export interface ReportTemplateConfig {
  sections: ReportSection[]
  charts: ReportChart[]
  metrics: ReportMetric[]
}

// 报表章节
export interface ReportSection {
  id: string
  title: string
  type: 'table' | 'chart' | 'text' | 'metrics'
  dataSource: string
  description?: string
}

// 报表图表
export interface ReportChart {
  id: string
  title: string
  type: 'pie' | 'bar' | 'line' | 'gauge'
  dataSource: string
  options?: Record<string, any>
}

// 报表指标
export interface ReportMetric {
  id: string
  title: string
  value: number | string
  unit?: string
  trend?: 'up' | 'down' | 'stable'
  description?: string
}

// 任务状态映射
export type TaskStatusMap = {
  [key in 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED']: {
    text: string
    color: 'default' | 'processing' | 'success' | 'error'
  }
}

// 合规状态映射
export type ComplianceStatusMap = {
  [key in 'PASS' | 'FAIL' | 'UNKNOWN' | 'NOT_APPLICABLE']: {
    text: string
    color: 'success' | 'error' | 'warning' | 'default'
  }
}