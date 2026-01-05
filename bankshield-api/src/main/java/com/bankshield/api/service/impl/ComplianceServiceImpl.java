package com.bankshield.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bankshield.api.dto.ComplianceStatisticsDTO;
import com.bankshield.api.entity.*;
import com.bankshield.api.mapper.*;
import com.bankshield.api.service.ComplianceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 合规性检查服务实现
 */
@Slf4j
@Service
public class ComplianceServiceImpl extends ServiceImpl<ComplianceRuleMapper, ComplianceRule> implements ComplianceService {
    
    @Autowired
    private ComplianceRuleMapper ruleMapper;
    
    @Autowired
    private ComplianceCheckTaskMapper taskMapper;
    
    @Autowired
    private ComplianceCheckResultMapper resultMapper;
    
    @Autowired
    private ComplianceReportMapper reportMapper;
    
    // ==================== 合规规则管理 ====================
    
    @Override
    public IPage<ComplianceRule> getRules(Page<ComplianceRule> page, String category, String standard, String status) {
        LambdaQueryWrapper<ComplianceRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(category), ComplianceRule::getCategory, category)
               .eq(StringUtils.hasText(standard), ComplianceRule::getStandard, standard)
               .eq(StringUtils.hasText(status), ComplianceRule::getStatus, status)
               .orderByDesc(ComplianceRule::getCreateTime);
        return ruleMapper.selectPage(page, wrapper);
    }
    
    @Override
    public ComplianceRule getRuleById(Long id) {
        return ruleMapper.selectById(id);
    }
    
    @Override
    @Transactional
    public boolean createRule(ComplianceRule rule) {
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());
        return ruleMapper.insert(rule) > 0;
    }
    
    @Override
    @Transactional
    public boolean updateRule(ComplianceRule rule) {
        rule.setUpdateTime(LocalDateTime.now());
        return ruleMapper.updateById(rule) > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteRule(Long id) {
        return ruleMapper.deleteById(id) > 0;
    }
    
    // ==================== 检查任务管理 ====================
    
    @Override
    public IPage<ComplianceCheckTask> getTasks(Page<ComplianceCheckTask> page, String taskType, String standard, String status) {
        LambdaQueryWrapper<ComplianceCheckTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(taskType), ComplianceCheckTask::getTaskType, taskType)
               .eq(StringUtils.hasText(standard), ComplianceCheckTask::getStandard, standard)
               .eq(StringUtils.hasText(status), ComplianceCheckTask::getStatus, status)
               .orderByDesc(ComplianceCheckTask::getCreateTime);
        return taskMapper.selectPage(page, wrapper);
    }
    
    @Override
    public ComplianceCheckTask getTaskById(Long id) {
        return taskMapper.selectById(id);
    }
    
    @Override
    @Transactional
    public boolean createTask(ComplianceCheckTask task) {
        task.setStatus("PENDING");
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        return taskMapper.insert(task) > 0;
    }
    
    @Override
    @Transactional
    public boolean executeTask(Long taskId) {
        ComplianceCheckTask task = taskMapper.selectById(taskId);
        if (task == null) {
            return false;
        }
        
        // 更新任务状态为运行中
        task.setStatus("RUNNING");
        task.setStartTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);
        
        // 异步执行检查任务（这里简化处理，实际应该使用线程池异步执行）
        try {
            executeComplianceCheck(task);
        } catch (Exception e) {
            log.error("执行合规检查任务失败: {}", e.getMessage(), e);
            task.setStatus("FAILED");
            task.setEndTime(LocalDateTime.now());
            taskMapper.updateById(task);
            return false;
        }
        
        return true;
    }
    
    /**
     * 执行合规检查
     */
    private void executeComplianceCheck(ComplianceCheckTask task) {
        // 获取相关规则
        LambdaQueryWrapper<ComplianceRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ComplianceRule::getStandard, task.getStandard())
               .eq(ComplianceRule::getStatus, "ACTIVE");
        List<ComplianceRule> rules = ruleMapper.selectList(wrapper);
        
        int totalRules = rules.size();
        int passedRules = 0;
        int failedRules = 0;
        
        // 执行每条规则的检查
        for (ComplianceRule rule : rules) {
            ComplianceCheckResult result = new ComplianceCheckResult();
            result.setTaskId(task.getId());
            result.setRuleId(rule.getId());
            result.setCheckTime(LocalDateTime.now());
            result.setCreateTime(LocalDateTime.now());
            result.setUpdateTime(LocalDateTime.now());
            
            // 模拟检查结果（实际应该执行checkScript）
            boolean passed = Math.random() > 0.3;
            
            if (passed) {
                result.setCheckStatus("PASSED");
                passedRules++;
            } else {
                result.setCheckStatus("FAILED");
                result.setRiskLevel(rule.getSeverity());
                result.setFindings("检测到不符合规则: " + rule.getRuleName());
                result.setRemediationStatus("PENDING");
                failedRules++;
            }
            
            resultMapper.insert(result);
        }
        
        // 更新任务统计
        task.setTotalRules(totalRules);
        task.setPassedRules(passedRules);
        task.setFailedRules(failedRules);
        task.setComplianceScore((int) ((double) passedRules / totalRules * 100));
        task.setStatus("COMPLETED");
        task.setEndTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);
    }
    
    @Override
    @Transactional
    public boolean stopTask(Long taskId) {
        ComplianceCheckTask task = taskMapper.selectById(taskId);
        if (task == null) {
            return false;
        }
        
        task.setStatus("STOPPED");
        task.setEndTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        return taskMapper.updateById(task) > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteTask(Long id) {
        // 删除任务相关的结果
        LambdaQueryWrapper<ComplianceCheckResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ComplianceCheckResult::getTaskId, id);
        resultMapper.delete(wrapper);
        
        // 删除任务
        return taskMapper.deleteById(id) > 0;
    }
    
    // ==================== 检查结果管理 ====================
    
    @Override
    public IPage<ComplianceCheckResult> getResults(Page<ComplianceCheckResult> page, Long taskId, String checkStatus, String riskLevel) {
        LambdaQueryWrapper<ComplianceCheckResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(taskId != null, ComplianceCheckResult::getTaskId, taskId)
               .eq(StringUtils.hasText(checkStatus), ComplianceCheckResult::getCheckStatus, checkStatus)
               .eq(StringUtils.hasText(riskLevel), ComplianceCheckResult::getRiskLevel, riskLevel)
               .orderByDesc(ComplianceCheckResult::getCheckTime);
        return resultMapper.selectPage(page, wrapper);
    }
    
    @Override
    public ComplianceCheckResult getResultById(Long id) {
        return resultMapper.selectById(id);
    }
    
    @Override
    @Transactional
    public boolean assignRemediation(Long resultId, String assignee) {
        ComplianceCheckResult result = resultMapper.selectById(resultId);
        if (result == null) {
            return false;
        }
        
        result.setAssignee(assignee);
        result.setRemediationStatus("IN_PROGRESS");
        result.setUpdateTime(LocalDateTime.now());
        return resultMapper.updateById(result) > 0;
    }
    
    @Override
    @Transactional
    public boolean updateRemediationStatus(Long resultId, String status) {
        ComplianceCheckResult result = resultMapper.selectById(resultId);
        if (result == null) {
            return false;
        }
        
        result.setRemediationStatus(status);
        if ("COMPLETED".equals(status)) {
            result.setRemediationTime(LocalDateTime.now());
        }
        result.setUpdateTime(LocalDateTime.now());
        return resultMapper.updateById(result) > 0;
    }
    
    // ==================== 合规报告管理 ====================
    
    @Override
    public IPage<ComplianceReport> getReports(Page<ComplianceReport> page, String standard, String status) {
        LambdaQueryWrapper<ComplianceReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(standard), ComplianceReport::getStandard, standard)
               .eq(StringUtils.hasText(status), ComplianceReport::getStatus, status)
               .orderByDesc(ComplianceReport::getGenerateTime);
        return reportMapper.selectPage(page, wrapper);
    }
    
    @Override
    public ComplianceReport getReportById(Long id) {
        return reportMapper.selectById(id);
    }
    
    @Override
    @Transactional
    public boolean generateReport(Long taskId) {
        ComplianceCheckTask task = taskMapper.selectById(taskId);
        if (task == null) {
            return false;
        }
        
        // 统计风险等级
        LambdaQueryWrapper<ComplianceCheckResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ComplianceCheckResult::getTaskId, taskId)
               .eq(ComplianceCheckResult::getCheckStatus, "FAILED");
        List<ComplianceCheckResult> failedResults = resultMapper.selectList(wrapper);
        
        int criticalRisks = 0;
        int highRisks = 0;
        int mediumRisks = 0;
        int lowRisks = 0;
        
        for (ComplianceCheckResult result : failedResults) {
            String riskLevel = result.getRiskLevel();
            if ("CRITICAL".equals(riskLevel)) {
                criticalRisks++;
            } else if ("HIGH".equals(riskLevel)) {
                highRisks++;
            } else if ("MEDIUM".equals(riskLevel)) {
                mediumRisks++;
            } else if ("LOW".equals(riskLevel)) {
                lowRisks++;
            }
        }
        
        // 创建报告
        ComplianceReport report = new ComplianceReport();
        report.setTaskId(taskId);
        report.setReportName(task.getTaskName() + " - 合规报告");
        report.setReportType("COMPREHENSIVE");
        report.setStandard(task.getStandard());
        report.setComplianceScore(task.getComplianceScore());
        report.setTotalRules(task.getTotalRules());
        report.setPassedRules(task.getPassedRules());
        report.setFailedRules(task.getFailedRules());
        report.setCriticalRisks(criticalRisks);
        report.setHighRisks(highRisks);
        report.setMediumRisks(mediumRisks);
        report.setLowRisks(lowRisks);
        report.setSummary("本次合规检查共检查 " + task.getTotalRules() + " 条规则，通过 " + task.getPassedRules() + " 条，未通过 " + task.getFailedRules() + " 条。");
        report.setRecommendations("建议优先处理 " + criticalRisks + " 个严重风险和 " + highRisks + " 个高危风险。");
        report.setStatus("GENERATED");
        report.setGenerateTime(LocalDateTime.now());
        report.setCreateTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());
        
        return reportMapper.insert(report) > 0;
    }
    
    @Override
    public String exportReport(Long reportId) {
        ComplianceReport report = reportMapper.selectById(reportId);
        if (report == null) {
            return null;
        }
        
        // 实际应该生成PDF或Excel文件
        String filePath = "/reports/compliance_report_" + reportId + ".pdf";
        report.setFilePath(filePath);
        reportMapper.updateById(report);
        
        return filePath;
    }
    
    // ==================== 统计分析 ====================
    
    @Override
    public ComplianceStatisticsDTO getStatistics() {
        ComplianceStatisticsDTO statistics = new ComplianceStatisticsDTO();
        
        // 总规则数
        LambdaQueryWrapper<ComplianceRule> ruleWrapper = new LambdaQueryWrapper<>();
        ruleWrapper.eq(ComplianceRule::getStatus, "ACTIVE");
        Long totalRulesCount = ruleMapper.selectCount(ruleWrapper);
        statistics.setTotalRules(totalRulesCount != null ? totalRulesCount.intValue() : 0);
        
        // 获取最新的合规评分
        LambdaQueryWrapper<ComplianceCheckTask> taskWrapper = new LambdaQueryWrapper<>();
        taskWrapper.eq(ComplianceCheckTask::getStatus, "COMPLETED")
                   .orderByDesc(ComplianceCheckTask::getEndTime)
                   .last("LIMIT 1");
        ComplianceCheckTask latestTask = taskMapper.selectOne(taskWrapper);
        statistics.setComplianceScore(latestTask != null ? latestTask.getComplianceScore() : 0);
        
        // 通过的检查数
        LambdaQueryWrapper<ComplianceCheckResult> passedWrapper = new LambdaQueryWrapper<>();
        passedWrapper.eq(ComplianceCheckResult::getCheckStatus, "PASSED");
        Long passedCount = resultMapper.selectCount(passedWrapper);
        statistics.setPassedChecks(passedCount != null ? passedCount.intValue() : 0);
        
        // 严重风险数
        LambdaQueryWrapper<ComplianceCheckResult> criticalWrapper = new LambdaQueryWrapper<>();
        criticalWrapper.eq(ComplianceCheckResult::getCheckStatus, "FAILED")
                       .eq(ComplianceCheckResult::getRiskLevel, "CRITICAL")
                       .ne(ComplianceCheckResult::getRemediationStatus, "COMPLETED");
        Long criticalCount = resultMapper.selectCount(criticalWrapper);
        statistics.setCriticalRiskCount(criticalCount != null ? criticalCount.intValue() : 0);
        
        // 合规趋势
        statistics.setComplianceTrend(taskMapper.getComplianceTrend());
        
        // 规则分类分布
        statistics.setRuleCategoryDistribution(ruleMapper.getRuleCategoryStats());
        
        // 风险等级分布
        statistics.setRiskLevelDistribution(resultMapper.getRiskLevelDistribution());
        
        // 整改进度
        statistics.setRemediationProgress(resultMapper.getRemediationProgress());
        
        // 任务状态统计
        statistics.setTaskStatusStats(taskMapper.getTaskStatusStats());
        
        // 最近任务
        LambdaQueryWrapper<ComplianceCheckTask> recentWrapper = new LambdaQueryWrapper<>();
        recentWrapper.orderByDesc(ComplianceCheckTask::getCreateTime).last("LIMIT 10");
        statistics.setRecentTasks(taskMapper.selectMaps(recentWrapper));
        
        // 高危风险项
        statistics.setCriticalRiskList(resultMapper.getCriticalRisks());
        
        return statistics;
    }
}
