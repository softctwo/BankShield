export interface Result<T = any> {
  code: number;
  success: boolean;
  message: string;
  data: T;
}
