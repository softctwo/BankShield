import request from '@/utils/request'
import type { Result } from '@/types/result'

// 数据血缘图谱相关接口

/**
 * 构建血缘图谱
 */
export const getLineageGraph = (assetId: number, depth: number): Promise<Result<LineageGraph>> => {
  return request({
    url: `/lineage/api/lineage/graph/${assetId}`,
    method: 'get',
    params: { depth }
  })
}

/**
 * 分析影响
 */
export const analyzeImpact = (assetId: number): Promise<Result<ImpactAnalysis>> => {
  return request({
    url: `/lineage/api/lineage/impact/${assetId}`,
    method: 'get'
  })
}

/**
 * 分析字段影响
 */
export const analyzeFieldImpact = (fieldId: number): Promise<Result<ImpactAnalysis>> => {
  return request({
    url: `/lineage/api/lineage/impact/field/${fieldId}`,
    method: 'get'
  })
}

/**
 * 分析表影响
 */
export const analyzeTableImpact = (tableId: number): Promise<Result<ImpactAnalysis>> => {
  return request({
    url: `/lineage/api/lineage/impact/table/${tableId}`,
    method: 'get'
  })
}

/**
 * 自动发现血缘关系
 */
export const autoDiscoverLineage = (): Promise<Result<string>> => {
  return request({
    url: '/lineage/api/lineage/auto-discover',
    method: 'post'
  })
}

/**
 * 提取SQL血缘
 */
export const extractSqlLineage = (sql: string, dbType: string = 'mysql'): Promise<Result<LineageInfo>> => {
  return request({
    url: '/lineage/api/lineage/extract-sql',
    method: 'post',
    params: { sql, dbType }
  })
}

/**
 * 查询节点详情
 */
export const getNodeById = (nodeId: number): Promise<Result<DataLineageNode>> => {
  return request({
    url: `/lineage/api/lineage/node/${nodeId}`,
    method: 'get'
  })
}

/**
 * 分页查询节点
 */
export const getNodes = (
  page: number,
  size: number,
  nodeType?: string,
  nodeName?: string,
  dataSourceId?: number
): Promise<Result<PageResult<DataLineageNode>>> => {
  return request({
    url: '/lineage/api/lineage/nodes',
    method: 'get',
    params: { page, size, nodeType, nodeName, dataSourceId }
  })
}

/**
 * 查询上游节点
 */
export const getUpstreamNodes = (nodeId: number): Promise<Result<DataLineageNode[]>> => {
  return request({
    url: `/lineage/api/lineage/upstream/${nodeId}`,
    method: 'get'
  })
}

/**
 * 查询下游节点
 */
export const getDownstreamNodes = (nodeId: number): Promise<Result<DataLineageNode[]>> => {
  return request({
    url: `/lineage/api/lineage/downstream/${nodeId}`,
    method: 'get'
  })
}

/**
 * 查询血缘路径
 */
export const getLineagePath = (
  sourceNodeId: number,
  targetNodeId: number,
  maxDepth: number = 10
): Promise<Result<LineageEdge[]>> => {
  return request({
    url: '/lineage/api/lineage/path',
    method: 'get',
    params: { sourceNodeId, targetNodeId, maxDepth }
  })
}

/**
 * 计算复杂性
 */
export const calculateComplexity = (nodeId: number): Promise<Result<number>> => {
  return request({
    url: `/lineage/api/lineage/complexity/${nodeId}`,
    method: 'get'
  })
}

/**
 * 验证血缘关系
 */
export const validateLineage = (sourceNodeId: number, targetNodeId: number): Promise<Result<boolean>> => {
  return request({
    url: '/lineage/api/lineage/validate',
    method: 'get',
    params: { sourceNodeId, targetNodeId }
  })
}

/**
 * 导入血缘数据
 */
export const importLineageData = (lineageData: string): Promise<Result<string>> => {
  return request({
    url: '/lineage/api/lineage/import',
    method: 'post',
    data: lineageData,
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

/**
 * 导出血缘数据
 */
export const exportLineageData = (nodeId: number, depth: number = 3): Promise<Result<string>> => {
  return request({
    url: `/lineage/api/lineage/export/${nodeId}`,
    method: 'get',
    params: { depth }
  })
}

// 类型定义
export interface LineageGraph {
  graphId: string
  centerNodeId: number
  depth: number
  nodes: LineageNode[]
  links: LineageEdge[]
  statistics: GraphStatistics
}

export interface LineageNode {
  id: number
  name: string
  type: string
  category: number
  symbolSize: number
  qualityScore?: number
  importanceLevel?: string
  properties?: any
  fixed?: boolean
  x?: number
  y?: number
}

export interface LineageEdge {
  source: number
  target: number
  relationshipType: string
  transformation?: string
  impactWeight: number
  lineStyle?: number
  label?: string
}

export interface GraphStatistics {
  totalNodes: number
  totalEdges: number
  tableNodes: number
  columnNodes: number
  viewNodes: number
  averageQualityScore: number
  maxQualityScore: number
  minQualityScore: number
}

export interface ImpactAnalysis {
  assetId: number
  assetName: string
  assetType: string
  directImpact: ImpactStatistics
  indirectImpact: ImpactStatistics
  downstreamTables: DownstreamTable[]
  impactPaths: ImpactPath[]
  complexity: ComplexityAnalysis
}

export interface ImpactStatistics {
  tableCount: number
  columnCount: number
  viewCount: number
  reportCount: number
  applicationCount: number
  etlCount: number
  highRiskCount: number
  mediumRiskCount: number
  lowRiskCount: number
}

export interface DownstreamTable {
  tableId: number
  tableName: string
  databaseName: string
  impactLevel: number
  lineagePath: string
  impactType: string
  riskLevel: string
  importanceLevel: string
}

export interface ImpactPath {
  pathId: string
  pathDescription: string
  pathLength: number
  nodes: PathNode[]
}

export interface PathNode {
  nodeId: number
  nodeName: string
  nodeType: string
  relationshipType?: string
}

export interface ComplexityAnalysis {
  cyclomaticComplexity: number
  dependencyCount: number
  stabilityScore: number
  maintenanceScore: number
  complexityLevel: string
}

export interface LineageInfo {
  sourceTables: TableInfo[]
  targetTables: TableInfo[]
  targetColumns: ColumnInfo[]
  statementType: string
  relationshipType: string
  transformation?: string
  impactWeight?: number
}

export interface TableInfo {
  tableName: string
  databaseName?: string
  schemaName?: string
  nodeType: string
  alias?: string
  description?: string
}

export interface ColumnInfo {
  columnName: string
  tableName: string
  databaseName?: string
  nodeType: string
  dataType?: string
  expression?: string
  description?: string
}

export interface DataLineageNode {
  id: number
  nodeType: string
  nodeName: string
  nodeCode: string
  dataSourceId?: number
  databaseName?: string
  schemaName?: string
  tableName?: string
  columnName?: string
  dataType?: string
  description?: string
  properties?: string
  qualityScore?: number
  importanceLevel?: string
  enabled: boolean
  createTime: string
  updateTime: string
  createBy: string
  updateBy: string
  deleted: boolean
  version: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  orders: any[]
  optimizeCountSql: boolean
  searchCount: boolean
  optimizeJoinOfCountSql: boolean
  maxLimit?: number
  pages: number
}