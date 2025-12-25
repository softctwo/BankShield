import request from '@/utils/request'

// AI智能安全分析模块API接口

/**
 * 异常行为检测
 * @param {Object} behavior - 用户行为数据
 * @returns {Promise} 异常分数
 */
export function detectAnomaly(behavior) {
  return request({
    url: '/api/ai/behavior/detect',
    method: 'post',
    data: behavior
  })
}

/**
 * 详细异常行为检测
 * @param {Object} behavior - 用户行为数据
 * @returns {Promise} 异常检测结果详情
 */
export function detectAnomalyDetail(behavior) {
  return request({
    url: '/api/ai/behavior/detect-detail',
    method: 'post',
    data: behavior
  })
}

/**
 * 批量异常行为检测
 * @param {Array} behaviors - 用户行为数据列表
 * @returns {Promise} 异常检测结果列表
 */
export function detectAnomalies(behaviors) {
  return request({
    url: '/api/ai/behavior/detect-batch',
    method: 'post',
    data: behaviors
  })
}

/**
 * 学习用户行为模式
 * @param {Number} userId - 用户ID
 * @param {Array} behaviors - 用户行为数据列表
 * @returns {Promise} 学习结果
 */
export function learnUserBehaviorPattern(userId, behaviors) {
  return request({
    url: '/api/ai/behavior/learn-pattern',
    method: 'post',
    params: { userId },
    data: behaviors
  })
}

/**
 * 获取用户行为模式
 * @param {Number} userId - 用户ID
 * @returns {Promise} 用户行为模式信息
 */
export function getUserBehaviorPattern(userId) {
  return request({
    url: `/api/ai/behavior/pattern/${userId}`,
    method: 'get'
  })
}

/**
 * 获取异常行为统计
 * @param {Number} userId - 用户ID
 * @param {String} startTime - 开始时间
 * @param {String} endTime - 结束时间
 * @returns {Promise} 异常行为统计信息
 */
export function getAnomalyStatistics(userId, startTime, endTime) {
  return request({
    url: '/api/ai/behavior/statistics',
    method: 'get',
    params: { userId, startTime, endTime }
  })
}

/**
 * 智能告警分类
 * @param {Number} alertId - 告警ID
 * @returns {Promise} 告警分类结果
 */
export function classifyAlert(alertId) {
  return request({
    url: '/api/ai/alert/smart-classify',
    method: 'get',
    params: { alertId }
  })
}

/**
 * 资源使用预测
 * @returns {Promise} 资源使用预测结果
 */
export function predictResourceUsage() {
  return request({
    url: '/api/ai/prediction/resource',
    method: 'get'
  })
}

/**
 * 特定资源类型预测
 * @param {String} resourceType - 资源类型
 * @param {Number} days - 预测天数
 * @returns {Promise} 资源使用预测结果
 */
export function predictSpecificResource(resourceType, days = 7) {
  return request({
    url: `/api/ai/prediction/resource/${resourceType}`,
    method: 'get',
    params: { days }
  })
}

/**
 * 威胁预测
 * @param {Number} days - 预测天数
 * @returns {Promise} 威胁预测结果
 */
export function predictThreats(days = 7) {
  return request({
    url: '/api/ai/prediction/threat',
    method: 'get',
    params: { days }
  })
}

/**
 * 获取AI模型信息
 * @returns {Promise} AI模型信息
 */
export function getModelInfo() {
  return request({
    url: '/api/ai/model/info',
    method: 'get'
  })
}

/**
 * 健康检查
 * @returns {Promise} 健康状态
 */
export function healthCheck() {
  return request({
    url: '/api/ai/health',
    method: 'get'
  })
}

/**
 * 获取异常行为雷达图数据
 * @returns {Promise} 雷达图数据
 */
export function getAnomalyRadarData() {
  return request({
    url: '/api/ai/charts/anomaly-radar',
    method: 'get'
  })
}

/**
 * 获取告警降噪效果数据
 * @returns {Promise} 降噪效果数据
 */
export function getAlertPrecisionData() {
  return request({
    url: '/api/ai/charts/alert-precision',
    method: 'get'
  })
}

/**
 * 获取预测趋势数据
 * @returns {Promise} 预测趋势数据
 */
export function getPredictionData() {
  return request({
    url: '/api/ai/charts/prediction-trend',
    method: 'get'
  })
}

/**
 * 获取最新异常行为
 * @param {Object} params - 查询参数
 * @returns {Promise} 最新异常行为列表
 */
export function getRecentAnomalies(params) {
  return request({
    url: '/api/ai/anomalies/recent',
    method: 'get',
    params
  })
}

/**
 * 获取AI模型状态
 * @returns {Promise} AI模型状态列表
 */
export function getModelStatus() {
  return request({
    url: '/api/ai/models/status',
    method: 'get'
  })
}

/**
 * 获取仪表板统计数据
 * @returns {Promise} 仪表板统计数据
 */
export function getDashboardStats() {
  return request({
    url: '/api/ai/dashboard/stats',
    method: 'get'
  })
}

/**
 * 获取异常行为列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 异常行为列表
 */
export function getAnomalyList(params) {
  return request({
    url: '/api/ai/anomalies',
    method: 'get',
    params
  })
}

/**
 * 获取异常行为详情
 * @param {Number} id - 异常行为ID
 * @returns {Promise} 异常行为详情
 */
export function getAnomalyDetail(id) {
  return request({
    url: `/api/ai/anomalies/${id}`,
    method: 'get'
  })
}

/**
 * 导出异常行为数据
 * @param {Object} params - 导出参数
 * @returns {Promise} 导出文件
 */
export function exportAnomalies(params) {
  return request({
    url: '/api/ai/anomalies/export',
    method: 'post',
    data: params,
    responseType: 'blob'
  })
}

/**
 * 训练AI模型
 * @param {Object} params - 训练参数
 * @returns {Promise} 训练结果
 */
export function trainModel(params) {
  return request({
    url: '/api/ai/models/train',
    method: 'post',
    data: params
  })
}

/**
 * 获取模型训练日志
 * @param {Object} params - 查询参数
 * @returns {Promise} 训练日志列表
 */
export function getTrainingLogs(params) {
  return request({
    url: '/api/ai/models/training-logs',
    method: 'get',
    params
  })
}

/**
 * 更新模型状态
 * @param {Number} modelId - 模型ID
 * @param {Object} params - 更新参数
 * @returns {Promise} 更新结果
 */
export function updateModelStatus(modelId, params) {
  return request({
    url: `/api/ai/models/${modelId}/status`,
    method: 'put',
    data: params
  })
}

/**
 * 删除模型
 * @param {Number} modelId - 模型ID
 * @returns {Promise} 删除结果
 */
export function deleteModel(modelId) {
  return request({
    url: `/api/ai/models/${modelId}`,
    method: 'delete'
  })
}

/**
 * 批量检测用户行为
 * @param {Array} userBehaviors - 用户行为数据列表
 * @returns {Promise} 批量检测结果
 */
export function batchDetectAnomalies(userBehaviors) {
  return request({
    url: '/api/ai/behavior/batch-detect',
    method: 'post',
    data: userBehaviors
  })
}

/**
 * 获取用户行为分析
 * @param {Number} userId - 用户ID
 * @param {Object} params - 分析参数
 * @returns {Promise} 行为分析结果
 */
export function getUserBehaviorAnalysis(userId, params) {
  return request({
    url: `/api/ai/behavior/analysis/${userId}`,
    method: 'get',
    params
  })
}

/**
 * 获取系统异常趋势
 * @param {Object} params - 查询参数
 * @returns {Promise} 异常趋势数据
 */
export function getAnomalyTrends(params) {
  return request({
    url: '/api/ai/anomalies/trends',
    method: 'get',
    params
  })
}

/**
 * 获取预测性维护建议
 * @param {Object} params - 查询参数
 * @returns {Promise} 维护建议
 */
export function getMaintenanceRecommendations(params) {
  return request({
    url: '/api/ai/maintenance/recommendations',
    method: 'get',
    params
  })
}

/**
 * 获取威胁情报
 * @param {Object} params - 查询参数
 * @returns {Promise} 威胁情报数据
 */
export function getThreatIntelligence(params) {
  return request({
    url: '/api/ai/threats/intelligence',
    method: 'get',
    params
  })
}

/**
 * 提交威胁反馈
 * @param {Object} feedback - 威胁反馈数据
 * @returns {Promise} 提交结果
 */
export function submitThreatFeedback(feedback) {
  return request({
    url: '/api/ai/threats/feedback',
    method: 'post',
    data: feedback
  })
}

/**
 * 获取AI配置信息
 * @returns {Promise} AI配置信息
 */
export function getAIConfig() {
  return request({
    url: '/api/ai/config',
    method: 'get'
  })
}

/**
 * 更新AI配置
 * @param {Object} config - AI配置数据
 * @returns {Promise} 更新结果
 */
export function updateAIConfig(config) {
  return request({
    url: '/api/ai/config',
    method: 'put',
    data: config
  })
}

export default {
  detectAnomaly,
  detectAnomalyDetail,
  detectAnomalies,
  learnUserBehaviorPattern,
  getUserBehaviorPattern,
  getAnomalyStatistics,
  classifyAlert,
  predictResourceUsage,
  predictSpecificResource,
  predictThreats,
  getModelInfo,
  healthCheck,
  getAnomalyRadarData,
  getAlertPrecisionData,
  getPredictionData,
  getRecentAnomalies,
  getModelStatus,
  getDashboardStats,
  getAnomalyList,
  getAnomalyDetail,
  exportAnomalies,
  trainModel,
  getTrainingLogs,
  updateModelStatus,
  deleteModel,
  batchDetectAnomalies,
  getUserBehaviorAnalysis,
  getAnomalyTrends,
  getMaintenanceRecommendations,
  getThreatIntelligence,
  submitThreatFeedback,
  getAIConfig,
  updateAIConfig
}