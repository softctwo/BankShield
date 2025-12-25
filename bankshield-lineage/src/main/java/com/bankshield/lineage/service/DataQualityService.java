package com.bankshield.lineage.service;

import com.bankshield.lineage.dto.QualityRuleTemplate;
import com.bankshield.lineage.dto.QualityStatistics;
import com.bankshield.lineage.dto.QualityTestResult;
import com.bankshield.lineage.entity.DataQualityRule;
import com.bankshield.lineage.entity.DataQualityResult;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 数据质量服务接口
 *
 * @author BankShield
 * @since 2024-01-24
 */
public interface DataQualityService {

    /**
     * 执行质量检查
     */
    void executeQualityChecks();

    /**
     * 执行单个规则检查
     *
     * @param ruleId 规则ID
     * @return 检查结果
     */
    DataQualityResult executeQualityCheck(Long ruleId);

    /**
     * 计算表质量评分
     *
     * @param tableId 表ID
     * @return 质量评分
     */
    Double calculateTableQuality(Long tableId);

    /**
     * 计算字段质量评分
     *
     * @param columnId 字段ID
     * @return 质量评分
     */
    Double calculateColumnQuality(Long columnId);

    /**
     * 查询质量规则
     *
     * @param page 分页参数
     * @param ruleType 规则类型
     * @param ruleName 规则名称
     * @param enabled 是否启用
     * @return 规则分页
     */
    IPage<DataQualityRule> getQualityRules(Page<DataQualityRule> page, String ruleType, String ruleName, Boolean enabled);

    /**
     * 创建质量规则
     *
     * @param rule 规则信息
     * @return 规则ID
     */
    Long createQualityRule(DataQualityRule rule);

    /**
     * 更新质量规则
     *
     * @param rule 规则信息
     * @return 是否成功
     */
    boolean updateQualityRule(DataQualityRule rule);

    /**
     * 删除质量规则
     *
     * @param ruleId 规则ID
     * @return 是否成功
     */
    boolean deleteQualityRule(Long ruleId);

    /**
     * 查询质量检查结果
     *
     * @param page 分页参数
     * @param ruleId 规则ID
     * @param tableId 表ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 结果分页
     */
    IPage<DataQualityResult> getQualityResults(Page<DataQualityResult> page, Long ruleId, Long tableId, String startTime, String endTime);

    /**
     * 获取质量统计信息
     *
     * @param tableId 表ID
     * @return 统计信息
     */
    QualityStatistics getQualityStatistics(Long tableId);

    /**
     * 测试质量规则
     *
     * @param ruleId 规则ID
     * @return 测试结果
     */
    QualityTestResult testQualityRule(Long ruleId);

    /**
     * 批量创建质量规则
     *
     * @param rules 规则列表
     * @return 创建结果
     */
    boolean batchCreateQualityRules(List<DataQualityRule> rules);

    /**
     * 获取规则模板
     *
     * @param ruleType 规则类型
     * @return 规则模板
     */
    List<QualityRuleTemplate> getRuleTemplates(String ruleType);

    /**
     * 质量告警
     *
     * @param result 检查结果
     */
    void triggerQualityAlert(DataQualityResult result);
}