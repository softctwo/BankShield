// 公共类型定义

export interface PageParams {
  pageNum: number;
  pageSize: number;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  page: number;
  size: number;
}
