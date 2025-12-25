import request from '@/utils/request'

/**
 * 密钥管理API
 */

// 密钥相关接口
export interface Key {
  id: number
  keyName: string
  keyType: string
  keyLength: number
  keyUsage: string
  keyStatus: string
  keyFingerprint: string
  keyMaterial?: string
  createdBy: string
  createTime: string
  updateTime: string
  expireTime: string
  rotationCycle: number
  lastRotationTime?: string
  rotationCount: number
  description?: string
  dataSourceId?: number
}

// 密钥状态枚举
export enum KeyStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  EXPIRED = 'EXPIRED',
  REVOKED = 'REVOKED',
  DESTROYED = 'DESTROYED'
}

// 密钥类型枚举
export enum KeyType {
  SM2 = 'SM2',
  SM3 = 'SM3',
  SM4 = 'SM4',
  AES = 'AES',
  RSA = 'RSA'
}

// 密钥用途枚举
export enum KeyUsage {
  ENCRYPT = 'ENCRYPT',
  DECRYPT = 'DECRYPT',
  SIGN = 'SIGN',
  VERIFY = 'VERIFY'
}

// 生成密钥请求
export interface GenerateKeyRequest {
  keyName: string
  keyType: KeyType
  keyUsage: KeyUsage
  keyLength?: number
  expireDays?: number
  rotationCycle?: number
  description?: string
  createdBy: string
}

// 查询参数
export interface KeyQueryParams {
  pageNum?: number
  pageSize?: number
  keyName?: string
  keyType?: string
  keyStatus?: string
  keyUsage?: string
}

// 密钥统计
export interface KeyStatistics {
  totalKeys: number
  activeKeys: number
  inactiveKeys: number
  expiringKeys: number
}

// 密钥使用统计
export interface KeyUsageStatistics {
  statistics: Array<{
    operationType: string
    count: number
    totalSize: number
  }>
  dailyUsage: Array<{
    date: string
    count: number
    totalSize: number
  }>
  topOperators: Array<{
    operator: string
    count: number
  }>
}

/**
 * 获取密钥列表
 */
export function getKeyList(params: KeyQueryParams) {
  return request({
    url: '/api/key/page',
    method: 'get',
    params
  })
}

/**
 * 获取密钥详情
 */
export function getKeyDetail(id: number) {
  return request({
    url: `/api/key/${id}`,
    method: 'get'
  })
}

/**
 * 生成新密钥
 */
export function generateKey(data: GenerateKeyRequest) {
  return request({
    url: '/api/key/generate',
    method: 'post',
    data
  })
}

/**
 * 轮换密钥
 */
export function rotateKey(id: number, rotationReason: string, operator: string) {
  return request({
    url: `/api/key/rotate/${id}`,
    method: 'post',
    params: {
      rotationReason,
      operator
    }
  })
}

/**
 * 更新密钥状态
 */
export function updateKeyStatus(id: number, status: KeyStatus, operator: string) {
  return request({
    url: `/api/key/status/${id}`,
    method: 'put',
    params: {
      status,
      operator
    }
  })
}

/**
 * 销毁密钥
 */
export function destroyKey(id: number, operator: string) {
  return request({
    url: `/api/key/${id}`,
    method: 'delete',
    params: {
      operator
    }
  })
}

/**
 * 导出密钥信息
 */
export function exportKeyInfo(keyIds?: number[]) {
  return request({
    url: '/api/key/export',
    method: 'post',
    data: keyIds,
    responseType: 'blob'
  })
}

/**
 * 获取支持的密钥类型
 */
export function getSupportedKeyTypes() {
  return request({
    url: '/api/key/types',
    method: 'get'
  })
}

/**
 * 获取密钥使用统计
 */
export function getKeyUsageStatistics(id: number, startTime: string, endTime: string) {
  return request({
    url: `/api/key/usage/${id}`,
    method: 'get',
    params: {
      startTime,
      endTime
    }
  })
}

/**
 * 获取密钥统计信息
 */
export function getKeyStatistics() {
  return request({
    url: '/api/key/statistics',
    method: 'get'
  })
}

/**
 * 获取密钥状态颜色
 */
export function getKeyStatusColor(status: KeyStatus): string {
  switch (status) {
    case KeyStatus.ACTIVE:
      return 'success'
    case KeyStatus.INACTIVE:
      return 'info'
    case KeyStatus.EXPIRED:
      return 'warning'
    case KeyStatus.REVOKED:
      return 'error'
    case KeyStatus.DESTROYED:
      return 'error'
    default:
      return 'info'
  }
}

/**
 * 获取密钥状态标签
 */
export function getKeyStatusLabel(status: KeyStatus): string {
  switch (status) {
    case KeyStatus.ACTIVE:
      return '活跃'
    case KeyStatus.INACTIVE:
      return '禁用'
    case KeyStatus.EXPIRED:
      return '已过期'
    case KeyStatus.REVOKED:
      return '已撤销'
    case KeyStatus.DESTROYED:
      return '已销毁'
    default:
      return status
  }
}

/**
 * 获取密钥类型标签
 */
export function getKeyTypeLabel(type: KeyType): string {
  switch (type) {
    case KeyType.SM2:
      return '国密SM2'
    case KeyType.SM3:
      return '国密SM3'
    case KeyType.SM4:
      return '国密SM4'
    case KeyType.AES:
      return 'AES'
    case KeyType.RSA:
      return 'RSA'
    default:
      return type
  }
}

/**
 * 获取密钥用途标签
 */
export function getKeyUsageLabel(usage: KeyUsage): string {
  switch (usage) {
    case KeyUsage.ENCRYPT:
      return '加密'
    case KeyUsage.DECRYPT:
      return '解密'
    case KeyUsage.SIGN:
      return '签名'
    case KeyUsage.VERIFY:
      return '验签'
    default:
      return usage
  }
}