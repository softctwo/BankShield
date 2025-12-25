import request from '@/utils/request'
import type {
  WatermarkTemplate,
  WatermarkTemplateParams,
  WatermarkTemplateResponse,
  WatermarkTask,
  WatermarkTaskParams,
  WatermarkTaskResponse,
  WatermarkExtractLog,
  WatermarkExtractParams,
  WatermarkExtractResponse,
  WatermarkTaskCreateRequest,
  WatermarkTemplateCreateRequest,
  WatermarkExtractStatistics
} from '@/types/watermark'

// 水印模板管理

// 获取水印模板列表
export const getWatermarkTemplateList = (params: WatermarkTemplateParams) => {
  return request({
    url: '/watermark/template/page',
    method: 'get',
    params
  }) as Promise<WatermarkTemplateResponse>
}

// 创建水印模板
export const createWatermarkTemplate = (data: WatermarkTemplateCreateRequest) => {
  return request({
    url: '/watermark/template',
    method: 'post',
    data
  })
}

// 更新水印模板
export const updateWatermarkTemplate = (data: WatermarkTemplate) => {
  return request({
    url: '/watermark/template',
    method: 'put',
    data
  })
}

// 删除水印模板
export const deleteWatermarkTemplate = (id: number) => {
  return request({
    url: `/watermark/template/${id}`,
    method: 'delete'
  })
}

// 启用水印模板
export const enableWatermarkTemplate = (id: number, enabled: number) => {
  return request({
    url: `/watermark/template/${id}/enable`,
    method: 'put',
    params: { enabled }
  })
}

// 水印任务管理

// 获取水印任务列表
export const getWatermarkTaskList = (params: WatermarkTaskParams) => {
  return request({
    url: '/watermark/task/page',
    method: 'get',
    params
  }) as Promise<WatermarkTaskResponse>
}

// 创建水印任务
export const createWatermarkTask = (data: WatermarkTaskCreateRequest) => {
  return request({
    url: '/watermark/task',
    method: 'post',
    data
  })
}

// 获取水印任务详情
export const getWatermarkTaskDetail = (id: number) => {
  return request({
    url: `/watermark/task/${id}`,
    method: 'get'
  }) as Promise<WatermarkTask>
}

// 获取水印任务进度
export const getWatermarkTaskProgress = (id: number) => {
  return request({
    url: `/watermark/task/${id}/progress`,
    method: 'get'
  }) as Promise<number>
}

// 取消水印任务
export const cancelWatermarkTask = (id: number) => {
  return request({
    url: `/watermark/task/${id}/cancel`,
    method: 'put'
  })
}

// 重试水印任务
export const retryWatermarkTask = (id: number) => {
  return request({
    url: `/watermark/task/${id}/retry`,
    method: 'put'
  })
}

// 下载水印任务结果文件
export const downloadWatermarkTaskResult = (id: number) => {
  return request({
    url: `/watermark/task/${id}/download`,
    method: 'get',
    responseType: 'blob'
  })
}

// 水印提取溯源

// 提取文件中的水印
export const extractWatermarkFromFile = (file: File, operator: string) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('operator', operator)
  
  return request({
    url: '/watermark/extract',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }) as Promise<WatermarkExtractLog>
}

// 批量提取文件中的水印
export const batchExtractWatermarkFromFiles = (files: File[], operator: string) => {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  formData.append('operator', operator)
  
  return request({
    url: '/watermark/extract/batch',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }) as Promise<WatermarkExtractLog[]>
}

// 验证文件是否包含水印
export const verifyWatermarkInFile = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/watermark/extract/verify',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }) as Promise<boolean>
}

// 获取水印提取日志列表
export const getWatermarkExtractLogList = (params: WatermarkExtractParams) => {
  return request({
    url: '/watermark/extract/log/page',
    method: 'get',
    params
  }) as Promise<WatermarkExtractResponse>
}

// 获取水印提取统计信息
export const getWatermarkExtractStatistics = (days: number = 7) => {
  return request({
    url: '/watermark/extract/statistics',
    method: 'get',
    params: { days }
  }) as Promise<WatermarkExtractStatistics>
}