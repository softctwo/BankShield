/**
 * 安全扫描模块类型定义
 */

// 扫描任务状态
export type TaskStatus = 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED' | 'PARTIAL'

// 扫描类型
export type ScanType = 'VULNERABILITY' | 'CONFIG' | 'WEAK_PASSWORD' | 'ANOMALY' | 'ALL'

// 风险级别
export type RiskLevel = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'INFO'

// 风险类型
export type RiskType = 
  // 漏洞扫描相关
  | 'SQL_INJECTION' | 'XSS' | 'CSRF' | 'DIRECTORY_TRAVERSAL' | 'COMMAND_INJECTION' | 'FILE_UPLOAD'
  // 配置检查相关
  | 'PASSWORD_POLICY' | 'SESSION_TIMEOUT' | 'ENCRYPTION_CONFIG' | 'FILE_UPLOAD_LIMIT' | 'CORS_CONFIG' | 'SENSITIVE_INFO_LEAK'
  // 弱密码检测相关
  | 'WEAK_PASSWORD' | 'DEFAULT_PASSWORD' | 'EXPIRED_PASSWORD'
  // 异常行为检测相关
  | 'ABNORMAL_LOGIN_TIME' | 'ABNORMAL_IP' | 'HIGH_FREQUENCY_OPERATION' | 'PRIVILEGE_ESCALATION' | 'SESSION_ANOMALY'
  // 其他
  | 'CONFIG_ISSUE' | 'OPEN_PORT' | 'SERVICE_VULNERABILITY' | 'ABNORMAL_ACCESS' | 'ABNORMAL_DATA_ACCESS' | 'ABNORMAL_SYSTEM_BEHAVIOR'

// 修复状态
export type FixStatus = 'UNFIXED' | 'RESOLVED' | 'WONT_FIX'

// 合规标准
export type ComplianceStandard = 'GB_LEVEL3' | 'PCI_DSS' | 'OWASP_TOP10' | 'ISO27001' | 'CUSTOM'

// 检查类型
export type CheckType = 'AUTH' | 'SESSION' | 'ENCRYPTION' | 'PASSWORD' | 'ACCESS_CONTROL' | 'INPUT_VALIDATION' | 'NETWORK' | 'MALWARE' | 'COMPONENT' | 'AUDIT' | 'KEY_MANAGEMENT'

// 扫描任务
export interface SecurityScanTask {
  id?: number
  taskName: string
  scanType: ScanType
  scanTarget: string
  status?: TaskStatus
  scanConfig?: string
  description?: string
  startTime?: string
  endTime?: string
  riskCount?: number
  reportPath?: string
  createdBy?: string
  errorMessage?: string
  progress?: number
  createTime?: string
  updateTime?: string
}

// 扫描结果
export interface SecurityScanResult {
  id: number
  taskId: number
  riskLevel: RiskLevel
  riskType: RiskType
  riskDescription: string
  impactScope: string
  remediationAdvice: string
  discoveredTime: string
  fixStatus: FixStatus
  fixTime?: string
  fixBy?: string
  verifyResult?: string
  riskDetails?: string
  cveId?: string
  cvssScore?: number
  assetInfo?: string
  createTime: string
  updateTime: string
}

// 安全基线
export interface SecurityBaseline {
  id: number
  checkItemName: string
  complianceStandard: ComplianceStandard
  checkType: CheckType
  riskLevel: RiskLevel
  passStatus: 'PASS' | 'FAIL' | 'UNKNOWN' | 'NOT_APPLICABLE'
  checkResult?: string
  checkTime: string
  nextCheckTime?: string
  responsiblePerson?: string
  enabled: boolean
  builtin: boolean
  description: string
  remedyAdvice: string
  createdBy: string
  createTime: string
  updateTime: string
}

// 扫描任务查询参数
export interface ScanTaskQueryParams {
  page: number
  size: number
  taskName?: string
  scanType?: ScanType
  status?: TaskStatus
}

// 扫描结果查询参数
export interface ScanResultQueryParams {
  page: number
  size: number
  taskId: number
  riskLevel?: RiskLevel
  fixStatus?: FixStatus
}

// 任务统计信息
export interface TaskStatistics {
  totalTasks: number
  todayTasks: number
  weekTasks: number
  monthTasks: number
  statusCounts: Record<TaskStatus, number>
  trend: Array<{
    date: string
    count: number
  }>
}

// 基线统计信息
export interface BaselineStatistics {
  complianceStandard: string
  totalCount: number
  enabledCount: number
  passCount: number
  failCount: number
}

// 风险级别颜色映射
export const RISK_LEVEL_COLORS: Record<RiskLevel, string> = {
  CRITICAL: '#FF0000',
  HIGH: '#FF6600',
  MEDIUM: '#FF9900',
  LOW: '#FFCC00',
  INFO: '#0099FF'
}

// 任务状态颜色映射
export const TASK_STATUS_COLORS: Record<TaskStatus, string> = {
  PENDING: '#909399',
  RUNNING: '#409EFF',
  SUCCESS: '#67C23A',
  FAILED: '#F56C6C',
  PARTIAL: '#E6A23C'
}

// 扫描类型标签映射
export const SCAN_TYPE_LABELS: Record<ScanType, { label: string; icon: string }> = {
  VULNERABILITY: { label: '漏洞扫描', icon: '🔴' },
  CONFIG: { label: '配置检查', icon: '🟡' },
  WEAK_PASSWORD: { label: '弱密码检测', icon: '🟠' },
  ANOMALY: { label: '异常行为检测', icon: '🔵' },
  ALL: { label: '全面扫描', icon: '🟣' }
}

// 风险类型描述映射
export const RISK_TYPE_DESCRIPTIONS: Record<RiskType, string> = {
  SQL_INJECTION: 'SQL注入漏洞',
  XSS: '跨站脚本攻击',
  CSRF: '跨站请求伪造',
  DIRECTORY_TRAVERSAL: '目录遍历',
  COMMAND_INJECTION: '命令注入',
  FILE_UPLOAD: '文件上传漏洞',
  PASSWORD_POLICY: '密码策略',
  SESSION_TIMEOUT: '会话超时',
  ENCRYPTION_CONFIG: '加密配置',
  FILE_UPLOAD_LIMIT: '文件上传限制',
  CORS_CONFIG: 'CORS配置',
  SENSITIVE_INFO_LEAK: '敏感信息泄露',
  WEAK_PASSWORD: '弱密码',
  DEFAULT_PASSWORD: '默认密码',
  EXPIRED_PASSWORD: '过期密码',
  ABNORMAL_LOGIN_TIME: '异常登录时间',
  ABNORMAL_IP: '异常IP地址',
  HIGH_FREQUENCY_OPERATION: '高频操作',
  PRIVILEGE_ESCALATION: '权限提升',
  SESSION_ANOMALY: '会话异常',
  CONFIG_ISSUE: '配置问题',
  OPEN_PORT: '开放端口',
  SERVICE_VULNERABILITY: '服务漏洞',
  ABNORMAL_ACCESS: '异常访问',
  ABNORMAL_DATA_ACCESS: '异常数据访问',
  ABNORMAL_SYSTEM_BEHAVIOR: '异常系统行为'
}