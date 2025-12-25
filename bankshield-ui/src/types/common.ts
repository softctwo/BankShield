export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

// 分页参数
export interface PageParams {
  page?: number;
  pageSize?: number;
  size?: number;
}

// 分页结果
export interface PageResult<T> {
  records: T[];
  total: number;
  page: number;
  pageSize: number;
  size: number;
  pages: number;
}

// 通用列表响应
export interface ListResponse<T> {
  list: T[];
  total: number;
}

// 通用响应包装
export interface Result<T = any> {
  code: number;
  success: boolean;
  message: string;
  data: T;
}
