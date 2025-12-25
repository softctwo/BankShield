/**
 * API 类型导出文件
 */
export type { PageParams, PageResult, ListResponse } from '@/types/common'
export type { Result } from '@/types/common'
export type { ApiResponse } from '@/types/common'

// 重新导出常用的类型
export interface PaginationParams {
  page?: number
  pageSize?: number
  size?: number
}
