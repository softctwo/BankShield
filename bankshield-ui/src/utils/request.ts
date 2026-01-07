import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 定义统一响应格式
export interface ResponseData<T = any> {
  code: number
  message: string
  data: T
  success?: boolean
}

// 扩展AxiosRequestConfig
interface CustomAxiosRequestConfig extends AxiosRequestConfig {
  skipErrorHandler?: boolean
}

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000, // 增加超时时间到30秒
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const data = response.data as ResponseData
    const config = response.config as CustomAxiosRequestConfig

    // 假设后端返回格式：{ code: number, data: any, message: string }
    if (data.code === 200) {
      return response
    } else {
      // 如果设置了跳过错误处理，则不显示错误消息
      if (!config.skipErrorHandler) {
        ElMessage.error(data.message || '请求失败')
      }
      return Promise.reject(new Error(data.message || '请求失败'))
    }
  },
  (error) => {
    const response = error.response
    const config = error.config as CustomAxiosRequestConfig
    
    // GET请求默认静默处理网络错误（允许页面降级到模拟数据）
    const isGetRequest = config?.method?.toLowerCase() === 'get'
    const shouldSkipError = config?.skipErrorHandler || isGetRequest
    
    // 如果设置了跳过错误处理或是GET请求，则静默失败
    if (shouldSkipError) {
      return Promise.reject(error)
    }

    if (response) {
      switch (response.status) {
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          localStorage.removeItem('token')
          // 跳转到登录页
          router.push('/login')
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(response.data?.message || '网络错误')
      }
    } else {
      ElMessage.error('网络连接失败')
    }

    return Promise.reject(error)
  }
)

// 封装请求方法，提供更好的类型推断
export function request<T = any>(config: CustomAxiosRequestConfig): Promise<ResponseData<T>> {
  return service.request<ResponseData<T>>(config).then(res => res.data)
}

export default service
