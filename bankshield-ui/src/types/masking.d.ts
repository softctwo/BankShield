/**
 * 数据脱敏相关类型定义
 */

// 敏感数据类型枚举
export enum SensitiveDataType {
  PHONE = 'PHONE',
  ID_CARD = 'ID_CARD',
  BANK_CARD = 'BANK_CARD',
  NAME = 'NAME',
  EMAIL = 'EMAIL',
  ADDRESS = 'ADDRESS'
}

// 脱敏算法枚举
export enum MaskingAlgorithm {
  PARTIAL_MASK = 'PARTIAL_MASK',
  FULL_MASK = 'FULL_MASK',
  HASH = 'HASH',
  SYMMETRIC_ENCRYPT = 'SYMMETRIC_ENCRYPT',
  FORMAT_PRESERVING = 'FORMAT_PRESERVING'
}

// 脱敏场景枚举
export enum MaskingScenario {
  DISPLAY = 'DISPLAY',
  EXPORT = 'EXPORT',
  QUERY = 'QUERY',
  TRANSFER = 'TRANSFER'
}

// 脱敏规则接口
export interface DataMaskingRule {
  id?: number
  ruleName: string
  sensitiveDataType: SensitiveDataType | string
  maskingAlgorithm: MaskingAlgorithm | string
  algorithmParams?: string
  applicableScenarios?: string
  enabled?: boolean
  createTime?: string
  updateTime?: string
  createdBy?: string
  description?: string
}

// 脱敏算法参数接口
export interface MaskingAlgorithmParams {
  // 部分掩码参数
  keepPrefix?: number
  keepSuffix?: number
  maskChar?: string
  maskLength?: number
  
  // 哈希算法参数
  hashAlgorithm?: 'SM3' | 'SHA256'
  
  // 加密参数
  encryptKeyId?: string
  
  // 格式保留加密参数
  formatPreserveLength?: number
}

// 脱敏测试请求接口
export interface MaskingTestRequest {
  testData: string
  sensitiveDataType: SensitiveDataType | string
  maskingAlgorithm: MaskingAlgorithm | string
  algorithmParams?: string
}

// 脱敏测试响应接口
export interface MaskingTestResponse {
  originalData: string
  maskedData: string
  sensitiveDataType: SensitiveDataType | string
  maskingAlgorithm: MaskingAlgorithm | string
  algorithmParams?: string
}

// 脱敏规则查询参数接口
export interface MaskingRuleQueryParams {
  pageNum: number
  pageSize: number
  ruleName?: string
  sensitiveDataType?: SensitiveDataType | string
  scenario?: MaskingScenario | string
  enabled?: boolean
}

// 脱敏规则分页结果接口
export interface MaskingRulePageResult {
  records: DataMaskingRule[]
  total: number
  size: number
  current: number
  pages: number
}