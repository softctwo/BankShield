export interface WatermarkTemplate {
  id: number
  templateName: string
  watermarkType: 'TEXT' | 'IMAGE' | 'DATABASE'
  watermarkContent: string
  watermarkPosition?: 'TOP_LEFT' | 'TOP_RIGHT' | 'BOTTOM_LEFT' | 'BOTTOM_RIGHT' | 'CENTER' | 'FULLSCREEN' | null
  transparency?: number
  fontSize?: number
  fontColor?: string
  fontFamily?: string
  enabled: boolean
  createTime?: string
  updateTime?: string
  createdBy?: string
  description?: string
}

export interface WatermarkTask {
  id: number
  taskName: string
  taskType: 'FILE' | 'DATABASE'
  templateId: number
  dataSourceId?: number
  status: 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED'
  startTime?: string
  endTime?: string
  processCount: number
  outputFilePath?: string
  createdBy: string
  errorMessage?: string
  createTime: string
}

export interface WatermarkExtractLog {
  id: number
  watermarkContent: string
  extractTime: string
  extractSource: string
  extractResult: 'SUCCESS' | 'FAIL'
  operator: string
  fileName?: string
  fileType?: string
  fileSize?: number
  extractDuration?: number
  errorMessage?: string
}

export interface WatermarkTemplateParams {
  page?: number
  size?: number
  templateName?: string
  watermarkType?: string
  enabled?: number
}

export interface WatermarkTemplateResponse {
  list: WatermarkTemplate[]
  total: number
  page: number
  size: number
}

export interface WatermarkTaskParams {
  page?: number
  size?: number
  taskName?: string
  taskType?: string
  status?: string
  startTime?: string
  endTime?: string
}

export interface WatermarkTaskResponse {
  list: WatermarkTask[]
  total: number
  page: number
  size: number
}

export interface WatermarkExtractParams {
  page?: number
  size?: number
  watermarkContent?: string
  extractResult?: string
  operator?: string
  startTime?: string
  endTime?: string
}

export interface WatermarkExtractResponse {
  list: WatermarkExtractLog[]
  total: number
  page: number
  size: number
}

export interface WatermarkExtractRequest {
  file: File
  operator: string
}

export interface WatermarkTaskCreateRequest {
  taskName: string
  taskType: 'FILE' | 'DATABASE'
  templateId: number
  dataSourceId?: number
  createdBy: string
  remark?: string
}

export interface WatermarkTemplateCreateRequest {
  templateName: string
  watermarkType: 'TEXT' | 'IMAGE' | 'DATABASE'
  watermarkContent: string
  watermarkPosition?: 'TOP_LEFT' | 'TOP_RIGHT' | 'BOTTOM_LEFT' | 'BOTTOM_RIGHT' | 'CENTER' | 'FULLSCREEN'
  transparency?: number
  fontSize?: number
  fontColor?: string
  fontFamily?: string
  description?: string
  createdBy: string
}

export interface WatermarkExtractStatistics {
  totalExtracts: number
  successCount: number
  failCount: number
  successRate: number
  dailyStats: {
    date: string
    total: number
    success: number
    fail: number
  }[]
}

export interface WatermarkPositionOption {
  label: string
  value: string
}

export interface WatermarkTypeOption {
  label: string
  value: string
}

export interface TaskStatusOption {
  label: string
  value: string
  type: 'info' | 'warning' | 'success' | 'danger'
}

export interface ExtractResultOption {
  label: string
  value: string
  type: 'success' | 'danger'
}