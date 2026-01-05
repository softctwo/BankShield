/**
 * API类型定义
 */

// 分页请求参数
export interface PageParams {
  page?: number
  pageSize?: number
  current?: number
  size?: number
}

// 分页响应数据
export interface PageData<T> {
  total: number
  list: T[]
  pageNum?: number
  pageSize?: number
}

// 区块链存证相关类型
export namespace Blockchain {
  // 存证记录类型
  export enum RecordType {
    AUDIT_LOG = 'AUDIT_LOG',
    KEY_EVENT = 'KEY_EVENT',
    COMPLIANCE_CHECK = 'COMPLIANCE_CHECK'
  }

  // 存证状态
  export enum AnchorStatus {
    PENDING = 'PENDING',
    SUCCESS = 'SUCCESS',
    FAILED = 'FAILED'
  }

  // 存证记录
  export interface AnchorRecord {
    id: number
    recordId: string
    recordType: RecordType
    dataHash: string
    txHash: string
    blockNumber: number
    timestamp: number
    status: AnchorStatus
    metadata?: Record<string, any>
  }

  // 批量存证请求
  export interface BatchAnchorRequest {
    records: Array<{
      recordId: string
      recordType: RecordType
      data: any
    }>
  }

  // 网络状态
  export interface NetworkStatus {
    networkName: string
    channelCount: number
    peerCount: number
    blockHeight: number
    status: 'ACTIVE' | 'INACTIVE'
  }

  // 统计信息
  export interface Statistics {
    totalAnchors: number
    successRate: number
    avgResponseTime: number
    todayAnchors: number
  }
}

// MPC相关类型
export namespace MPC {
  // 任务类型
  export enum JobType {
    PSI = 'PSI',
    SECURE_SUM = 'SECURE_SUM',
    JOINT_QUERY = 'JOINT_QUERY'
  }

  // 任务状态
  export enum JobStatus {
    PENDING = 'PENDING',
    RUNNING = 'RUNNING',
    COMPLETED = 'COMPLETED',
    FAILED = 'FAILED',
    CANCELLED = 'CANCELLED'
  }

  // 任务信息
  export interface Job {
    id: number
    jobType: JobType
    status: JobStatus
    partyIds: number[]
    result?: any
    createTime: number
    updateTime: number
    errorMessage?: string
  }

  // PSI请求
  export interface PSIRequest {
    partyIds: number[]
    dataSet: string[]
    protocol: string
  }

  // 安全求和请求
  export interface SecureSumRequest {
    partyIds: number[]
    localValue: number
  }

  // 参与方信息
  export interface Party {
    id: number
    name: string
    endpoint: string
    publicKey: string
    status: 'ACTIVE' | 'INACTIVE'
  }

  // 统计信息
  export interface Statistics {
    totalJobs: number
    runningJobs: number
    completedJobs: number
    successRate: number
  }
}

// AI相关类型
export namespace AI {
  // 用户行为
  export interface UserBehavior {
    userId: string
    loginTime: string
    loginIp: string
    operationType: string
    dataVolume: number
    duration?: number
  }

  // 异常检测结果
  export interface AnomalyResult {
    score: number
    isAnomaly: boolean
    factors?: string[]
    confidence: number
  }

  // 威胁预测结果
  export interface ThreatPrediction {
    probability: number
    riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
    factors: string[]
    recommendations: string[]
  }

  // 模型信息
  export interface ModelInfo {
    name: string
    version: string
    accuracy: number
    lastTrainTime: number
    status: 'ACTIVE' | 'TRAINING' | 'INACTIVE'
  }
}

// 数据血缘相关类型
export namespace Lineage {
  // 节点类型
  export interface Node {
    id: string
    name: string
    type: 'table' | 'view' | 'task' | 'file'
    metadata?: Record<string, any>
  }

  // 边
  export interface Edge {
    source: string
    target: string
    type?: string
  }

  // 血缘图
  export interface LineageGraph {
    nodes: Node[]
    edges: Edge[]
    depth: number
  }

  // 影响分析结果
  export interface ImpactAnalysis {
    impactedAssets: number
    impactLevel: 'LOW' | 'MEDIUM' | 'HIGH'
    details: Array<{
      assetId: string
      assetName: string
      impactType: string
    }>
  }
}

// 监控相关类型
export namespace Monitor {
  // 指标数据
  export interface Metric {
    name: string
    value: number
    unit: string
    timestamp: number
  }

  // 告警级别
  export enum AlertLevel {
    INFO = 'INFO',
    WARNING = 'WARNING',
    ERROR = 'ERROR',
    CRITICAL = 'CRITICAL'
  }

  // 告警信息
  export interface Alert {
    id: number
    level: AlertLevel
    message: string
    source: string
    timestamp: number
    resolved: boolean
  }

  // 系统健康状态
  export interface HealthStatus {
    status: 'UP' | 'DOWN' | 'DEGRADED'
    components: Record<string, 'UP' | 'DOWN'>
    timestamp: number
  }
}
