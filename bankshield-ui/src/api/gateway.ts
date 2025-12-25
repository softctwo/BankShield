import request from '@/utils/request'

// 限流规则接口
export interface RateLimitRule {
  id?: number
  ruleName: string
  limitDimension: 'IP' | 'USER' | 'API' | 'GLOBAL'
  limitThreshold: number
  limitWindow: number
  enabled: boolean
  description?: string
  createTime?: string
  updateTime?: string
}

// IP黑名单接口
export interface BlacklistIp {
  id?: number
  ipAddress: string
  blockReason: string
  blockTime?: string
  unblockTime?: string
  blockStatus: 'BLOCKED' | 'UNBLOCKED' | 'EXPIRED'
  operator?: string
  version?: number
}

// API访问日志接口
export interface ApiAccessLog {
  id?: number
  requestPath: string
  requestMethod: string
  requestParams?: string
  requestHeaders?: string
  responseStatus?: number
  responseContent?: string
  executeTime?: number
  ipAddress?: string
  userId?: number
  accessTime: string
  accessResult: 'SUCCESS' | 'FAIL'
  errorMessage?: string
  userAgent?: string
  requestBodySize?: number
  responseBodySize?: number
}

// API路由配置接口
export interface ApiRouteConfig {
  id?: number
  routeId: string
  routePath: string
  targetService: string
  requireAuth: boolean
  requiredRoles?: string
  rateLimitThreshold: number
  signatureVerificationEnabled: boolean
  signatureSecret?: string
  enabled: boolean
  description?: string
  createTime?: string
  updateTime?: string
  version?: number
}

// 通用分页参数
export interface PageParams {
  page?: number
  size?: number
  [key: string]: any
}

// 通用分页结果
export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  numberOfElements: number
  first: boolean
  last: boolean
}

// 限流规则管理
export const getRateLimitRules = (params: PageParams & {
  ruleName?: string
  limitDimension?: string
  enabled?: boolean
}) => {
  return request<PageResult<RateLimitRule>>({
    url: '/gateway/rate-limit/rules',
    method: 'get',
    params
  })
}

export const getRateLimitRule = (id: number) => {
  return request<RateLimitRule>({
    url: `/gateway/rate-limit/rule/${id}`,
    method: 'get'
  })
}

export const createRateLimitRule = (data: RateLimitRule) => {
  return request<string>({
    url: '/gateway/rate-limit/rule',
    method: 'post',
    data
  })
}

export const updateRateLimitRule = (id: number, data: RateLimitRule) => {
  return request<string>({
    url: `/gateway/rate-limit/rule/${id}`,
    method: 'put',
    data
  })
}

export const deleteRateLimitRule = (id: number) => {
  return request<string>({
    url: `/gateway/rate-limit/rule/${id}`,
    method: 'delete'
  })
}

export const updateRateLimitRuleStatus = (id: number, enabled: boolean) => {
  return request<string>({
    url: `/gateway/rate-limit/rule/${id}/status`,
    method: 'put',
    params: { enabled }
  })
}

export const getEnabledRateLimitRules = () => {
  return request<RateLimitRule[]>({
    url: '/gateway/rate-limit/rules/enabled',
    method: 'get'
  })
}

export const getRateLimitStatistics = () => {
  return request<{
    totalRules: number
    enabledRules: number
    disabledRules: number
    dimensionStats: Record<string, number>
  }>({
    url: '/gateway/rate-limit/statistics',
    method: 'get'
  })
}

// IP黑名单管理
export const getBlacklistIps = (params: PageParams & {
  ipAddress?: string
  blockStatus?: string
}) => {
  return request<PageResult<BlacklistIp>>({
    url: '/gateway/blacklist/ips',
    method: 'get',
    params
  })
}

export const getBlacklistIp = (id: number) => {
  return request<BlacklistIp>({
    url: `/gateway/blacklist/ip/${id}`,
    method: 'get'
  })
}

export const addToBlacklist = (data: {
  ipAddress: string
  blockReason: string
  blockDuration?: number
  operator: string
}) => {
  return request<string>({
    url: '/gateway/blacklist/ip',
    method: 'post',
    data
  })
}

export const addToBlacklistBatch = (data: {
  ipAddresses: string[]
  blockReason: string
  blockDuration?: number
  operator: string
}) => {
  return request<string>({
    url: '/gateway/blacklist/ips/batch',
    method: 'post',
    data
  })
}

export const removeFromBlacklist = (id: number, operator: string) => {
  return request<string>({
    url: `/gateway/blacklist/ip/${id}/unblock`,
    method: 'put',
    params: { operator }
  })
}

export const removeFromBlacklistBatch = (data: {
  ids: number[]
  operator: string
}) => {
  return request<string>({
    url: '/gateway/blacklist/ips/batch/unblock',
    method: 'put',
    data
  })
}

export const checkIpInBlacklist = (ipAddress: string) => {
  return request<{
    ipAddress: string
    isBlacklisted: boolean
    blockReason?: string
    blockTime?: string
    unblockTime?: string
    blockStatus?: string
  }>({
    url: '/gateway/blacklist/ip/check',
    method: 'get',
    params: { ipAddress }
  })
}

export const getBlacklistStatistics = () => {
  return request<{
    totalCount: number
    activeCount: number
    blockedCount: number
    unblockedCount: number
    expiredCount: number
  }>({
    url: '/gateway/blacklist/statistics',
    method: 'get'
  })
}

export const getActiveBlacklists = () => {
  return request<BlacklistIp[]>({
    url: '/gateway/blacklist/ips/active',
    method: 'get'
  })
}

export const processExpiredBlacklists = () => {
  return request<string>({
    url: '/gateway/blacklist/ips/process-expired',
    method: 'put'
  })
}

// API审计日志管理
export const getApiAccessLogs = (params: PageParams & {
  requestPath?: string
  ipAddress?: string
  userId?: number
  accessResult?: string
  startTime?: string
  endTime?: string
}) => {
  return request<PageResult<ApiAccessLog>>({
    url: '/gateway/api/access/logs',
    method: 'get',
    params
  })
}

export const getApiAccessLog = (id: number) => {
  return request<ApiAccessLog>({
    url: `/gateway/api/access/log/${id}`,
    method: 'get'
  })
}

export const getSlowQueries = (params: PageParams & {
  executeTime?: number
}) => {
  return request<PageResult<ApiAccessLog>>({
    url: '/gateway/api/slow-queries',
    method: 'get',
    params
  })
}

export const getAccessStatistics = (params?: {
  startTime?: string
  endTime?: string
}) => {
  return request<{
    totalAccessCount: number
    errorRate: string
    averageExecuteTime: string
    startTime: string
    endTime: string
  }>({
    url: '/gateway/api/statistics',
    method: 'get',
    params
  })
}

export const getTopRequestPaths = (params?: {
  startTime?: string
  endTime?: string
  limit?: number
}) => {
  return request<Array<[string, number]>>({
    url: '/gateway/api/top-paths',
    method: 'get',
    params
  })
}

export const getErrorRateStatistics = (params?: {
  startTime?: string
  endTime?: string
}) => {
  return request<{
    errorRate: number
    totalAccessCount: number
    errorCount: number
    startTime: string
    endTime: string
  }>({
    url: '/gateway/api/error-rate',
    method: 'get',
    params
  })
}

export const deleteApiAccessLog = (id: number) => {
  return request<string>({
    url: `/gateway/api/access/log/${id}`,
    method: 'delete'
  })
}

export const deleteApiAccessLogsBatch = (ids: number[]) => {
  return request<string>({
    url: '/gateway/api/access/logs/batch',
    method: 'delete',
    data: { ids }
  })
}

export const clearApiAccessLogs = () => {
  return request<string>({
    url: '/gateway/api/access/logs/clear',
    method: 'delete'
  })
}

export const getRealtimeStats = () => {
  return request<{
    hourStats: {
      totalAccessCount: number
      errorRate: number
      averageExecuteTime: number
    }
    dayStats: {
      totalAccessCount: number
      errorRate: number
      averageExecuteTime: number
    }
  }>({
    url: '/gateway/api/realtime-stats',
    method: 'get'
  })
}

// API路由配置管理
export const getApiRoutes = (params: PageParams & {
  routeId?: string
  targetService?: string
}) => {
  return request<PageResult<ApiRouteConfig>>({
    url: '/gateway/api/routes',
    method: 'get',
    params
  })
}

export const getApiRoute = (id: number) => {
  return request<ApiRouteConfig>({
    url: `/gateway/api/route/${id}`,
    method: 'get'
  })
}

export const createApiRoute = (data: ApiRouteConfig) => {
  return request<string>({
    url: '/gateway/api/route',
    method: 'post',
    data
  })
}

export const updateApiRoute = (id: number, data: ApiRouteConfig) => {
  return request<string>({
    url: `/gateway/api/route/${id}`,
    method: 'put',
    data
  })
}

export const deleteApiRoute = (id: number) => {
  return request<string>({
    url: `/gateway/api/route/${id}`,
    method: 'delete'
  })
}

export const updateApiRouteStatus = (id: number, enabled: boolean) => {
  return request<string>({
    url: `/gateway/api/route/${id}/status`,
    method: 'put',
    params: { enabled }
  })
}