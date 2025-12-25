// Axios响应类型定义
export interface AxiosResponse<T = any> {
  data: T;
  code: number;
  message: string;
}
