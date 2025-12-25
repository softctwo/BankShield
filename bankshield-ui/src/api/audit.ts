import request from '@/utils/request'
import type { 
  OperationAudit, 
  LoginAudit, 
  OperationAuditQuery, 
  LoginAuditQuery, 
  AuditPageResponse 
} from '@/types/audit'

/**
 * 审计管理相关API
 */

/**
 * 获取操作审计列表
 */
export function getOperationAuditList(params: OperationAuditQuery) {
  return request<AuditPageResponse<OperationAudit>>({
    url: '/api/audit/operation/page',
    method: 'get',
    params
  })
}

/**
 * 获取操作审计详情
 */
export function getOperationAuditDetail(id: number) {
  return request<OperationAudit>({
    url: `/api/audit/operation/${id}`,
    method: 'get'
  })
}

/**
 * 获取登录审计列表
 */
export function getLoginAuditList(params: LoginAuditQuery) {
  return request<AuditPageResponse<LoginAudit>>({
    url: '/api/audit/login/page',
    method: 'get',
    params
  })
}

/**
 * 获取登录审计详情
 */
export function getLoginAuditDetail(id: number) {
  return request<LoginAudit>({
    url: `/api/audit/login/${id}`,
    method: 'get'
  })
}

/**
 * 导出操作审计Excel
 */
export function exportOperationAudit(params: OperationAuditQuery) {
  return request<Blob>({
    url: '/api/audit/export/operation',
    method: 'post',
    data: params,
    responseType: 'blob'
  })
}

/**
 * 导出登录审计Excel
 */
export function exportLoginAudit(params: LoginAuditQuery) {
  return request<Blob>({
    url: '/api/audit/export/login',
    method: 'post',
    data: params,
    responseType: 'blob'
  })
}