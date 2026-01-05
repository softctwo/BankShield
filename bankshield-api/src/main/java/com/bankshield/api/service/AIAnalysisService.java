package com.bankshield.api.service;

import java.util.List;
import java.util.Map;

/**
 * AI智能分析服务接口
 */
public interface AIAnalysisService {

    /**
     * 威胁预测
     * 基于历史数据预测未来可能的安全威胁
     *
     * @param days 预测天数
     * @return 预测结果
     */
    Map<String, Object> predictThreats(int days);

    /**
     * 异常检测
     * 检测系统中的异常行为和模式
     *
     * @param dataType 数据类型（audit_log/security_threat/access_log）
     * @param timeRange 时间范围（小时）
     * @return 异常检测结果
     */
    List<Map<String, Object>> detectAnomalies(String dataType, int timeRange);

    /**
     * 风险评分
     * 对用户、系统或数据进行风险评分
     *
     * @param targetType 目标类型（user/system/data）
     * @param targetId 目标ID
     * @return 风险评分结果
     */
    Map<String, Object> calculateRiskScore(String targetType, String targetId);

    /**
     * 智能推荐
     * 基于当前安全态势推荐改进措施
     *
     * @return 推荐列表
     */
    List<Map<String, Object>> getRecommendations();

    /**
     * 行为分析
     * 分析用户或系统的行为模式
     *
     * @param entityType 实体类型（user/system）
     * @param entityId 实体ID
     * @param days 分析天数
     * @return 行为分析结果
     */
    Map<String, Object> analyzeBehavior(String entityType, String entityId, int days);

    /**
     * 关联分析
     * 分析安全事件之间的关联关系
     *
     * @param eventIds 事件ID列表
     * @return 关联分析结果
     */
    Map<String, Object> analyzeCorrelation(List<Long> eventIds);

    /**
     * 趋势预测
     * 预测安全指标的未来趋势
     *
     * @param metric 指标名称
     * @param days 预测天数
     * @return 趋势预测结果
     */
    Map<String, Object> predictTrend(String metric, int days);

    /**
     * 根因分析
     * 分析安全事件的根本原因
     *
     * @param eventId 事件ID
     * @return 根因分析结果
     */
    Map<String, Object> analyzeRootCause(Long eventId);
}
