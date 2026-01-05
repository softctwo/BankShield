package com.bankshield.api.service;

import java.util.List;
import java.util.Map;

/**
 * 数据质量检查服务接口
 */
public interface DataQualityService {

    /**
     * 执行数据质量检查
     *
     * @param tableName 表名
     * @param checkRules 检查规则列表
     * @return 检查结果
     */
    Map<String, Object> executeQualityCheck(String tableName, List<String> checkRules);

    /**
     * 检查数据完整性
     *
     * @param tableName 表名
     * @return 完整性检查结果
     */
    Map<String, Object> checkDataCompleteness(String tableName);

    /**
     * 检查数据一致性
     *
     * @param tableName 表名
     * @param referenceTable 参照表
     * @return 一致性检查结果
     */
    Map<String, Object> checkDataConsistency(String tableName, String referenceTable);

    /**
     * 检查数据准确性
     *
     * @param tableName 表名
     * @param validationRules 验证规则
     * @return 准确性检查结果
     */
    Map<String, Object> checkDataAccuracy(String tableName, Map<String, Object> validationRules);

    /**
     * 检查数据唯一性
     *
     * @param tableName 表名
     * @param columns 列名列表
     * @return 唯一性检查结果
     */
    Map<String, Object> checkDataUniqueness(String tableName, List<String> columns);

    /**
     * 检查数据时效性
     *
     * @param tableName 表名
     * @param timeColumn 时间列名
     * @param thresholdDays 阈值天数
     * @return 时效性检查结果
     */
    Map<String, Object> checkDataTimeliness(String tableName, String timeColumn, int thresholdDays);

    /**
     * 检查数据规范性
     *
     * @param tableName 表名
     * @param formatRules 格式规则
     * @return 规范性检查结果
     */
    Map<String, Object> checkDataConformity(String tableName, Map<String, String> formatRules);

    /**
     * 检查空值和缺失值
     *
     * @param tableName 表名
     * @return 空值检查结果
     */
    Map<String, Object> checkNullValues(String tableName);

    /**
     * 检查异常值和离群点
     *
     * @param tableName 表名
     * @param columnName 列名
     * @return 异常值检查结果
     */
    Map<String, Object> checkOutliers(String tableName, String columnName);

    /**
     * 生成数据质量报告
     *
     * @param tableName 表名
     * @return 质量报告
     */
    Map<String, Object> generateQualityReport(String tableName);

    /**
     * 获取数据质量评分
     *
     * @param tableName 表名
     * @return 质量评分（0-100）
     */
    int getQualityScore(String tableName);

    /**
     * 获取数据质量趋势
     *
     * @param tableName 表名
     * @param days 天数
     * @return 质量趋势数据
     */
    List<Map<String, Object>> getQualityTrend(String tableName, int days);

    /**
     * 修复数据质量问题
     *
     * @param tableName 表名
     * @param issueType 问题类型
     * @param fixStrategy 修复策略
     * @return 修复结果
     */
    Map<String, Object> fixQualityIssues(String tableName, String issueType, String fixStrategy);
}
