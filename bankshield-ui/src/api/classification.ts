import request from '@/utils/request'

// 数据资产管理相关API

/**
 * 获取资产列表
 */
export const getAssetList = (params: any) => {
  return request({
    url: '/api/asset/list',
    method: 'get',
    params
  })
}

/**
 * 获取资产详情
 */
export const getAssetById = (id: number) => {
  return request({
    url: `/api/asset/${id}`,
    method: 'get'
  })
}

/**
 * 人工标注资产分级
 */
export const classifyAsset = (assetId: number, manualLevel: number, operatorId: number) => {
  return request({
    url: `/api/asset/${assetId}/classify`,
    method: 'put',
    params: { manualLevel, operatorId }
  })
}

/**
 * 提交审核
 */
export const submitReview = (assetId: number, finalLevel: number, reason?: string) => {
  return request({
    url: `/api/asset/${assetId}/review`,
    method: 'post',
    params: { finalLevel, reason }
  })
}

/**
 * 审核资产
 */
export const reviewAsset = (assetId: number, approved: boolean, comment?: string, reviewerId?: number) => {
  return request({
    url: `/api/asset/${assetId}/approve`,
    method: 'put',
    params: { approved, comment, reviewerId }
  })
}

/**
 * 批量审核通过
 */
export const batchApproveAssets = (assetIds: number[], reviewerId: number) => {
  return request({
    url: '/api/asset/batch-approve',
    method: 'post',
    data: { assetIds, reviewerId }
  })
}

/**
 * 批量审核拒绝
 */
export const batchRejectAssets = (assetIds: number[], comment: string, reviewerId: number) => {
  return request({
    url: '/api/asset/batch-reject',
    method: 'post',
    data: { assetIds, comment, reviewerId }
  })
}

/**
 * 获取待审核资产列表
 */
export const getPendingReviewAssets = (page: number, size: number) => {
  return request({
    url: '/api/asset/pending-review',
    method: 'get',
    params: { page, size }
  })
}

/**
 * 获取风险资产清单
 */
export const getRiskAssets = (riskLevel?: string) => {
  return request({
    url: '/api/asset/risk-assets',
    method: 'get',
    params: { riskLevel }
  })
}

/**
 * 获取资产地图概览
 */
export const getAssetOverview = () => {
  return request({
    url: '/api/asset/map/overview',
    method: 'get'
  })
}

/**
 * 资产下钻查询
 */
export const drillDown = (data: any) => {
  return request({
    url: '/api/asset/map/drill-down',
    method: 'get',
    params: data
  })
}

/**
 * 获取业务条线分布
 */
export const getBusinessLineDistribution = () => {
  return request({
    url: '/api/asset/business-line-distribution',
    method: 'get'
  })
}

/**
 * 获取存储位置分布
 */
export const getStorageDistribution = () => {
  return request({
    url: '/api/asset/storage-distribution',
    method: 'get'
  })
}

/**
 * 导出资产清单
 */
export const exportAssetList = (exportType: string) => {
  return request({
    url: '/api/asset/export',
    method: 'get',
    params: { exportType }
  })
}

/**
 * 启动资产发现任务
 */
export const discoverAssets = (dataSourceId: number) => {
  return request({
    url: '/api/asset/discover',
    method: 'post',
    params: { dataSourceId }
  })
}

/**
 * 获取扫描进度
 */
export const getScanProgress = (taskId: number) => {
  return request({
    url: `/api/asset/scan-progress/${taskId}`,
    method: 'get'
  })
}

/**
 * 停止扫描任务
 */
export const stopScanTask = (taskId: number) => {
  return request({
    url: `/api/asset/scan-stop/${taskId}`,
    method: 'post'
  })
}

// 数据源管理相关API

/**
 * 获取数据源列表
 */
export const getDataSourceList = (params: any) => {
  return request({
    url: '/api/data-source/page',
    method: 'get',
    params
  })
}

/**
 * 添加数据源
 */
export const addDataSource = (data: any) => {
  return request({
    url: '/api/data-source',
    method: 'post',
    data
  })
}

/**
 * 更新数据源
 */
export const updateDataSource = (data: any) => {
  return request({
    url: '/api/data-source',
    method: 'put',
    data
  })
}

/**
 * 删除数据源
 */
export const deleteDataSource = (id: number) => {
  return request({
    url: `/api/data-source/${id}`,
    method: 'delete'
  })
}

/**
 * 测试数据源连接
 */
export const testDataSourceConnection = (id: number) => {
  return request({
    url: `/api/data-source/${id}/test-connection`,
    method: 'post'
  })
}

/**
 * 启动数据源扫描
 */
export const startDataSourceScan = (id: number) => {
  return request({
    url: `/api/data-source/${id}/scan`,
    method: 'post'
  })
}

/**
 * 重新扫描数据源
 */
export const rescanDataSource = (id: number) => {
  return request({
    url: `/api/data-source/${id}/rescan`,
    method: 'post'
  })
}

// 数据血缘分析相关API

/**
 * 解析SQL血缘
 */
export const parseLineage = (sql: string, dataSource: string) => {
  return request({
    url: '/api/lineage/parse',
    method: 'post',
    data: { sql, dataSource }
  })
}

/**
 * 查询字段血缘链路
 */
export const getFieldLineage = (tableName: string, fieldName: string) => {
  return request({
    url: '/api/lineage/field',
    method: 'get',
    params: { tableName, fieldName }
  })
}

/**
 * 查询表的上游血缘
 */
export const getUpstreamLineage = (tableName: string) => {
  return request({
    url: '/api/lineage/upstream',
    method: 'get',
    params: { tableName }
  })
}

/**
 * 查询表的下游血缘
 */
export const getDownstreamLineage = (tableName: string) => {
  return request({
    url: '/api/lineage/downstream',
    method: 'get',
    params: { tableName }
  })
}

/**
 * 构建血缘图谱
 */
export const buildLineageGraph = (assetId: number) => {
  return request({
    url: `/api/lineage/graph/${assetId}`,
    method: 'get'
  })
}

/**
 * 分析传输风险
 */
export const analyzeTransferRisk = (assetId: number) => {
  return request({
    url: `/api/lineage/transfer-risk/${assetId}`,
    method: 'get'
  })
}

// 敏感数据模板相关API

/**
 * 获取敏感数据模板列表
 */
export const getTemplateList = (params: any) => {
  return request({
    url: '/api/template/page',
    method: 'get',
    params
  })
}

/**
 * 添加敏感数据模板
 */
export const addTemplate = (data: any) => {
  return request({
    url: '/api/template',
    method: 'post',
    data
  })
}

/**
 * 更新敏感数据模板
 */
export const updateTemplate = (data: any) => {
  return request({
    url: '/api/template',
    method: 'put',
    data
  })
}

/**
 * 删除敏感数据模板
 */
export const deleteTemplate = (id: number) => {
  return request({
    url: `/api/template/${id}`,
    method: 'delete'
  })
}

/**
 * 按安全等级获取模板
 */
export const getTemplatesBySecurityLevel = (securityLevel: number) => {
  return request({
    url: '/api/template/by-security-level',
    method: 'get',
    params: { securityLevel }
  })
}

/**
 * 获取金融行业标准模板
 */
export const getFinancialStandardTemplates = () => {
  return request({
    url: '/api/template/financial-standard',
    method: 'get'
  })
}