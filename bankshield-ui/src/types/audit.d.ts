/**
 * 审计模块相关类型定义
 */

/**
 * 操作审计记录
 */
export interface OperationAudit {
  id: number
  userId: number
  username: string
  operationType: string
  operationModule: string
  operationContent: string
  requestUrl: string
  requestMethod: string
  requestParams: string
  responseResult: string
  ipAddress: string
  location: string
  status: number // 0-失败, 1-成功
  errorMessage: string
  createTime: string
}

/**
 * 登录审计记录
 */
export interface LoginAudit {
  id: number
  userId?: number
  username: string
  loginTime: string
  loginIp: string
  loginLocation: string
  browser: string
  os: string
  status: number // 0-失败, 1-成功
  failureReason?: string
  logoutTime?: string
  sessionDuration?: number // 会话时长(秒)
}

/**
 * 操作审计查询参数
 */
export interface OperationAuditQuery {
  page?: number
  size?: number
  username?: string
  operationType?: string
  operationModule?: string
  status?: number
  startTime?: string
  endTime?: string
}

/**
 * 登录审计查询参数
 */
export interface LoginAuditQuery {
  page?: number
  size?: number
  username?: string
  status?: number
  startTime?: string
  endTime?: string
}

/**
 * 审计分页响应
 */
export interface AuditPageResponse<T> {
  success: boolean
  message: string
  code: number
  result: {
    records: T[]
    total: number
    size: number
    current: number
    pages: number
  }
  timestamp: number
}

/**
 * 操作类型枚举
 */
export const OperationTypeEnum = {
  QUERY: '查询',
  VIEW: '查看详情',
  ADD: '新增',
  EDIT: '修改',
  DELETE: '删除',
  LOGIN: '登录',
  LOGOUT: '退出',
  OTHER: '其他操作'
} as const

/**
 * 操作模块枚举
 */
export const OperationModuleEnum = {
  USER: '用户管理',
  ROLE: '角色管理',
  DEPT: '部门管理',
  MENU: '菜单管理',
  DATA_ASSET: '数据资产管理',
  DATA_SOURCE: '数据源管理',
  SENSITIVE_DATA: '敏感数据管理',
  CLASSIFICATION: '数据分类分级',
  ENCRYPT: '加密管理',
  MONITOR: '监控管理',
  OTHER: '其他模块'
} as const

/**
 * 操作结果枚举
 */
export const OperationStatusEnum = {
  SUCCESS: 1,
  FAILURE: 0
} as const

/**
 * 登录结果枚举
 */
export const LoginStatusEnum = {
  SUCCESS: 1,
  FAILURE: 0
} as const