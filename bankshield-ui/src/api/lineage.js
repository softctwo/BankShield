import request from '@/utils/request'

// 血缘发现相关API
export function getDiscoveryTasks(params) {
  return request({
    url: '/api/lineage/enhanced/discovery/tasks/recent',
    method: 'get',
    params
  })
}

export function createDiscoveryTask(data) {
  return request({
    url: '/api/lineage/enhanced/discovery/task',
    method: 'post',
    params: {
      taskName: data.taskName,
      dataSourceId: data.dataSourceId,
      discoveryStrategy: data.discoveryStrategy
    },
    data: data.config
  })
}

export function cancelDiscoveryTask(taskId) {
  return request({
    url: `/api/lineage/enhanced/discovery/task/${taskId}/cancel`,
    method: 'post'
  })
}

export function getDiscoveryTaskStatus(taskId) {
  return request({
    url: `/api/lineage/enhanced/discovery/task/${taskId}`,
    method: 'get'
  })
}

export function getDiscoveryStatistics() {
  return request({
    url: '/api/lineage/enhanced/discovery/statistics',
    method: 'get'
  })
}

// 影响分析相关API
export function getImpactAnalyses(params) {
  return request({
    url: '/api/lineage/enhanced/impact-analysis/recent',
    method: 'get',
    params
  })
}

export function createImpactAnalysis(data) {
  return request({
    url: '/api/lineage/enhanced/impact-analysis',
    method: 'post',
    params: {
      analysisName: data.analysisName,
      analysisType: data.analysisType,
      impactObjectType: data.impactObjectType,
      impactObjectName: data.impactObjectName,
      createBy: data.createBy
    },
    data: data.analysisTarget
  })
}

export function getImpactAnalysisResult(analysisId) {
  return request({
    url: `/api/lineage/enhanced/impact-analysis/${analysisId}`,
    method: 'get'
  })
}

export function getImpactAnalysisStatistics() {
  return request({
    url: '/api/lineage/enhanced/impact-analysis/statistics',
    method: 'get'
  })
}

// 数据地图相关API
export function generateGlobalDataMap() {
  return request({
    url: '/api/lineage/enhanced/data-map/global',
    method: 'post'
  })
}

export function generateBusinessDomainMap(businessDomain) {
  return request({
    url: '/api/lineage/enhanced/data-map/business-domain',
    method: 'post',
    params: { businessDomain }
  })
}

export function generateDataSourceMap(dataSourceId) {
  return request({
    url: `/api/lineage/enhanced/data-map/data-source/${dataSourceId}`,
    method: 'post'
  })
}

export function generateCustomDataMap(data) {
  return request({
    url: '/api/lineage/enhanced/data-map/custom',
    method: 'post',
    params: {
      mapName: data.mapName,
      includedTables: data.includedTables,
      includedDataSources: data.includedDataSources
    },
    data: data.layoutConfig
  })
}

export function getDataMap(mapId) {
  return request({
    url: `/api/lineage/enhanced/data-map/${mapId}`,
    method: 'get'
  })
}

export function getActiveDataMaps() {
  return request({
    url: '/api/lineage/enhanced/data-map/active',
    method: 'get'
  })
}

export function getDefaultDataMap() {
  return request({
    url: '/api/lineage/enhanced/data-map/default',
    method: 'get'
  })
}

export function setDefaultDataMap(mapId) {
  return request({
    url: `/api/lineage/enhanced/data-map/${mapId}/default`,
    method: 'put'
  })
}

export function updateDataMap(mapId, mapName, config) {
  return request({
    url: `/api/lineage/enhanced/data-map/${mapId}`,
    method: 'put',
    params: { mapName },
    data: config
  })
}

export function deleteDataMap(mapId) {
  return request({
    url: `/api/lineage/enhanced/data-map/${mapId}`,
    method: 'delete'
  })
}

export function getDataMapStatistics() {
  return request({
    url: '/api/lineage/enhanced/data-map/statistics',
    method: 'get'
  })
}

// 可视化相关API
export function generateLineageGraph(tableName, columnName, depth) {
  return request({
    url: '/api/lineage/enhanced/visualization/lineage-graph',
    method: 'get',
    params: { tableName, columnName, depth }
  })
}

export function generateImpactAnalysisGraph(tableName, columnName) {
  return request({
    url: '/api/lineage/enhanced/visualization/impact-analysis',
    method: 'get',
    params: { tableName, columnName }
  })
}

export function generate3DDataMap(mapId) {
  return request({
    url: `/api/lineage/enhanced/visualization/3d-data-map/${mapId}`,
    method: 'get'
  })
}

export function generateTraceabilityGraph(sourceTable, sourceColumn, targetTable, targetColumn) {
  return request({
    url: '/api/lineage/enhanced/visualization/traceability',
    method: 'get',
    params: { sourceTable, sourceColumn, targetTable, targetColumn }
  })
}

export function generateDataFlowGraph(tableName, startTime, endTime) {
  return request({
    url: '/api/lineage/enhanced/visualization/data-flow',
    method: 'get',
    params: { tableName, startTime, endTime }
  })
}

export function exportVisualizationToHtml(chartType, chartData, title) {
  return request({
    url: '/api/lineage/enhanced/visualization/export-html',
    method: 'post',
    params: { chartType, title },
    data: chartData
  })
}

// 数据流查询API
export function getDataFlows(params) {
  return request({
    url: '/api/lineage/enhanced/flows',
    method: 'get',
    params
  })
}

export function getDataFlowStatistics() {
  return request({
    url: '/api/lineage/enhanced/flows/statistics',
    method: 'get'
  })
}