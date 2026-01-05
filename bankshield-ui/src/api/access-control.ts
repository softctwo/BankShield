import request from '@/utils/request'

// ==================== 访问策略管理 ====================

export interface AccessPolicy {
  id?: number
  policyCode: string
  policyName: string
  policyType: string
  description?: string
  priority: number
  effect: string
  status: string
  createdBy?: string
  createdTime?: string
  updatedBy?: string
  updatedTime?: string
}

export interface AccessRule {
  id?: number
  policyId: number
  ruleCode: string
  ruleName: string
  ruleType: string
  subjectCondition?: string
  resourceCondition?: string
  actionCondition?: string
  environmentCondition?: string
  mfaRequired: boolean
  priority: number
  status: string
  createdTime?: string
  updatedTime?: string
}

export interface MfaConfig {
  id?: number
  userId: number
  username: string
  mfaType: string
  mfaEnabled: boolean
  phone?: string
  email?: string
  totpSecret?: string
  backupCodes?: string
  lastVerifiedTime?: string
  createdTime?: string
  updatedTime?: string
}

export interface TemporaryPermission {
  id?: number
  userId: number
  username: string
  permissionCode: string
  permissionName: string
  resourceType?: string
  resourceId?: string
  grantedBy: string
  grantReason?: string
  validFrom: string
  validTo: string
  status: string
  createdTime?: string
}

export interface IpWhitelist {
  id?: number
  ipAddress: string
  ipRange?: string
  description?: string
  applyTo: string
  targetId?: number
  status: string
  createdBy?: string
  createdTime?: string
  updatedTime?: string
}

export interface IpBlacklist {
  id?: number
  ipAddress: string
  ipRange?: string
  blockReason: string
  blockType: string
  severity: string
  blockedBy?: string
  blockedTime?: string
  expireTime?: string
  status: string
  createdTime?: string
}

// 访问策略API
export function getPolicies(params: any) {
  return request({
    url: '/api/access-control/policies',
    method: 'get',
    params
  })
}

export function getPolicyById(id: number) {
  return request({
    url: `/api/access-control/policies/${id}`,
    method: 'get'
  })
}

export function createPolicy(data: AccessPolicy) {
  return request({
    url: '/api/access-control/policies',
    method: 'post',
    data
  })
}

export function updatePolicy(id: number, data: AccessPolicy) {
  return request({
    url: `/api/access-control/policies/${id}`,
    method: 'put',
    data
  })
}

export function deletePolicy(id: number) {
  return request({
    url: `/api/access-control/policies/${id}`,
    method: 'delete'
  })
}

export function updatePolicyStatus(id: number, status: string) {
  return request({
    url: `/api/access-control/policies/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export function getPolicyRules(id: number) {
  return request({
    url: `/api/access-control/policies/${id}/rules`,
    method: 'get'
  })
}

// 访问规则API
export function getRules(params: any) {
  return request({
    url: '/api/access-control/rules',
    method: 'get',
    params
  })
}

export function createRule(data: AccessRule) {
  return request({
    url: '/api/access-control/rules',
    method: 'post',
    data
  })
}

export function updateRule(id: number, data: AccessRule) {
  return request({
    url: `/api/access-control/rules/${id}`,
    method: 'put',
    data
  })
}

export function deleteRule(id: number) {
  return request({
    url: `/api/access-control/rules/${id}`,
    method: 'delete'
  })
}

// MFA管理API
export function configureMfa(data: MfaConfig) {
  return request({
    url: '/api/access-control/mfa/config',
    method: 'post',
    data
  })
}

export function toggleMfa(userId: number, mfaType: string, enabled: boolean) {
  return request({
    url: '/api/access-control/mfa/toggle',
    method: 'put',
    params: { userId, mfaType, enabled }
  })
}

export function verifyMfa(userId: number, mfaType: string, code: string) {
  return request({
    url: '/api/access-control/mfa/verify',
    method: 'post',
    params: { userId, mfaType, code }
  })
}

export function generateTotpSecret() {
  return request({
    url: '/api/access-control/mfa/totp-secret',
    method: 'get'
  })
}

export function generateBackupCodes() {
  return request({
    url: '/api/access-control/mfa/backup-codes',
    method: 'get'
  })
}

// 临时权限API
export function grantTemporaryPermission(data: TemporaryPermission) {
  return request({
    url: '/api/access-control/temp-permissions',
    method: 'post',
    data
  })
}

export function revokeTemporaryPermission(id: number) {
  return request({
    url: `/api/access-control/temp-permissions/${id}`,
    method: 'delete'
  })
}

export function getUserPermissions(userId: number) {
  return request({
    url: `/api/access-control/temp-permissions/user/${userId}`,
    method: 'get'
  })
}

// IP访问控制API
export function addIpWhitelist(data: IpWhitelist) {
  return request({
    url: '/api/access-control/ip/whitelist',
    method: 'post',
    data
  })
}

export function removeIpWhitelist(id: number) {
  return request({
    url: `/api/access-control/ip/whitelist/${id}`,
    method: 'delete'
  })
}

export function addIpBlacklist(data: IpBlacklist) {
  return request({
    url: '/api/access-control/ip/blacklist',
    method: 'post',
    data
  })
}

export function removeIpBlacklist(id: number) {
  return request({
    url: `/api/access-control/ip/blacklist/${id}`,
    method: 'delete'
  })
}

export function checkIpStatus(ipAddress: string, userId?: number) {
  return request({
    url: '/api/access-control/ip/check',
    method: 'post',
    params: { ipAddress, userId }
  })
}

// 访问日志API
export function getAccessLogs(days: number = 7) {
  return request({
    url: '/api/access-control/logs',
    method: 'get',
    params: { days }
  })
}
