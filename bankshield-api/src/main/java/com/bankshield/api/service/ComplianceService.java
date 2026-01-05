package com.bankshield.api.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.dto.ComplianceStatisticsDTO;
import com.bankshield.api.entity.*;

/**
 * 合规性检查服务接口
 */
public interface ComplianceService {
    
    // ==================== 合规规则管理 ====================
    
    /**
     * 分页查询合规规则
     */
    IPage<ComplianceRule> getRules(Page<ComplianceRule> page, String category, String standard, String status);
    
    /**
     * 获取规则详情
     */
    ComplianceRule getRuleById(Long id);
    
    /**
     * 创建合规规则
     */
    boolean createRule(ComplianceRule rule);
    
    /**
     * 更新合规规则
     */
    boolean updateRule(ComplianceRule rule);
    
    /**
     * 删除合规规则
     */
    boolean deleteRule(Long id);
    
    // ==================== 检查任务管理 ====================
    
    /**
     * 分页查询检查任务
     */
    IPage<ComplianceCheckTask> getTasks(Page<ComplianceCheckTask> page, String taskType, String standard, String status);
    
    /**
     * 获取任务详情
     */
    ComplianceCheckTask getTaskById(Long id);
    
    /**
     * 创建检查任务
     */
    boolean createTask(ComplianceCheckTask task);
    
    /**
     * 执行检查任务
     */
    boolean executeTask(Long taskId);
    
    /**
     * 停止检查任务
     */
    boolean stopTask(Long taskId);
    
    /**
     * 删除检查任务
     */
    boolean deleteTask(Long id);
    
    // ==================== 检查结果管理 ====================
    
    /**
     * 分页查询检查结果
     */
    IPage<ComplianceCheckResult> getResults(Page<ComplianceCheckResult> page, Long taskId, String checkStatus, String riskLevel);
    
    /**
     * 获取结果详情
     */
    ComplianceCheckResult getResultById(Long id);
    
    /**
     * 分配整改任务
     */
    boolean assignRemediation(Long resultId, String assignee);
    
    /**
     * 更新整改状态
     */
    boolean updateRemediationStatus(Long resultId, String status);
    
    // ==================== 合规报告管理 ====================
    
    /**
     * 分页查询合规报告
     */
    IPage<ComplianceReport> getReports(Page<ComplianceReport> page, String standard, String status);
    
    /**
     * 获取报告详情
     */
    ComplianceReport getReportById(Long id);
    
    /**
     * 生成合规报告
     */
    boolean generateReport(Long taskId);
    
    /**
     * 导出合规报告
     */
    String exportReport(Long reportId);
    
    // ==================== 统计分析 ====================
    
    /**
     * 获取合规统计数据
     */
    ComplianceStatisticsDTO getStatistics();
}
