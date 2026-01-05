package com.bankshield.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.*;
import com.bankshield.api.mapper.*;
import com.bankshield.api.scanner.ScanEngine;
import com.bankshield.api.service.SecurityScanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class SecurityScanServiceImpl implements SecurityScanService {

    @Autowired
    private SecurityScanTaskMapper taskMapper;

    @Autowired
    private VulnerabilityRecordMapper vulnerabilityMapper;

    @Autowired
    private ScanRuleMapper ruleMapper;

    @Autowired
    private RemediationPlanMapper planMapper;

    @Autowired
    private DependencyComponentMapper componentMapper;

    @Autowired
    private ScanStatisticsMapper statisticsMapper;

    @Autowired
    private ScanEngine scanEngine;

    // ==================== 扫描任务管理 ====================

    @Override
    public IPage<SecurityScanTask> pageTasks(Page<SecurityScanTask> page, String taskType, String status) {
        LambdaQueryWrapper<SecurityScanTask> wrapper = new LambdaQueryWrapper<>();
        if (taskType != null && !taskType.isEmpty()) {
            wrapper.eq(SecurityScanTask::getScanType, taskType);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(SecurityScanTask::getStatus, status);
        }
        wrapper.orderByDesc(SecurityScanTask::getCreateTime);
        return taskMapper.selectPage(page, wrapper);
    }

    @Override
    public SecurityScanTask getTaskById(Long id) {
        return taskMapper.selectById(id);
    }

    @Override
    @Transactional
    public SecurityScanTask createTask(SecurityScanTask task) {
        task.setStatus("PENDING");
        task.setProgress(0);
        task.setCreateTime(LocalDateTime.now());
        taskMapper.insert(task);
        log.info("创建扫描任务: {} (ID: {})", task.getTaskName(), task.getId());
        return task;
    }

    @Override
    public void startScan(Long taskId) {
        SecurityScanTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("扫描任务不存在: " + taskId);
        }
        
        if (!"PENDING".equals(task.getStatus()) && !"FAILED".equals(task.getStatus())) {
            throw new RuntimeException("任务状态不允许启动: " + task.getStatus());
        }
        
        log.info("启动扫描任务: {} (ID: {})", task.getTaskName(), taskId);
        scanEngine.executeScan(task);
    }

    @Override
    public void stopScan(Long taskId) {
        SecurityScanTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("扫描任务不存在: " + taskId);
        }
        
        task.setStatus("CANCELLED");
        task.setEndTime(LocalDateTime.now());
        taskMapper.updateById(task);
        log.info("停止扫描任务: {} (ID: {})", task.getTaskName(), taskId);
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        SecurityScanTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("扫描任务不存在: " + taskId);
        }
        
        // 删除关联的漏洞记录
        LambdaQueryWrapper<VulnerabilityRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VulnerabilityRecord::getTaskId, taskId);
        vulnerabilityMapper.delete(wrapper);
        
        // 删除任务
        taskMapper.deleteById(taskId);
        log.info("删除扫描任务: {} (ID: {})", task.getTaskName(), taskId);
    }

    @Override
    public List<SecurityScanTask> getRecentTasks(int limit) {
        return taskMapper.selectRecentTasks(limit);
    }

    // ==================== 漏洞管理 ====================

    @Override
    public IPage<VulnerabilityRecord> pageVulnerabilities(Page<VulnerabilityRecord> page, 
                                                          String severity, String status, String vulnType) {
        LambdaQueryWrapper<VulnerabilityRecord> wrapper = new LambdaQueryWrapper<>();
        if (severity != null && !severity.isEmpty()) {
            wrapper.eq(VulnerabilityRecord::getSeverity, severity);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(VulnerabilityRecord::getStatus, status);
        }
        if (vulnType != null && !vulnType.isEmpty()) {
            wrapper.eq(VulnerabilityRecord::getVulnType, vulnType);
        }
        wrapper.orderByDesc(VulnerabilityRecord::getCreatedTime);
        return vulnerabilityMapper.selectPage(page, wrapper);
    }

    @Override
    public VulnerabilityRecord getVulnerabilityDetail(Long id) {
        return vulnerabilityMapper.selectById(id);
    }

    @Override
    public void updateVulnerabilityStatus(Long id, String status) {
        VulnerabilityRecord vuln = vulnerabilityMapper.selectById(id);
        if (vuln == null) {
            throw new RuntimeException("漏洞不存在: " + id);
        }
        
        vuln.setStatus(status);
        vuln.setUpdatedTime(LocalDateTime.now());
        vulnerabilityMapper.updateById(vuln);
        log.info("更新漏洞状态: {} -> {}", id, status);
    }

    @Override
    public void assignVulnerability(Long id, String assignedTo) {
        VulnerabilityRecord vuln = vulnerabilityMapper.selectById(id);
        if (vuln == null) {
            throw new RuntimeException("漏洞不存在: " + id);
        }
        
        vuln.setAssignedTo(assignedTo);
        vuln.setStatus("IN_PROGRESS");
        vuln.setUpdatedTime(LocalDateTime.now());
        vulnerabilityMapper.updateById(vuln);
        log.info("分配漏洞: {} -> {}", id, assignedTo);
    }

    @Override
    public void resolveVulnerability(Long id, String resolutionNotes, String resolvedBy) {
        VulnerabilityRecord vuln = vulnerabilityMapper.selectById(id);
        if (vuln == null) {
            throw new RuntimeException("漏洞不存在: " + id);
        }
        
        vuln.setStatus("RESOLVED");
        vuln.setResolutionNotes(resolutionNotes);
        vuln.setResolvedBy(resolvedBy);
        vuln.setResolvedTime(LocalDateTime.now());
        vuln.setUpdatedTime(LocalDateTime.now());
        vulnerabilityMapper.updateById(vuln);
        log.info("解决漏洞: {} by {}", id, resolvedBy);
    }

    @Override
    public void markAsFalsePositive(Long id, String reason) {
        VulnerabilityRecord vuln = vulnerabilityMapper.selectById(id);
        if (vuln == null) {
            throw new RuntimeException("漏洞不存在: " + id);
        }
        
        vuln.setStatus("FALSE_POSITIVE");
        vuln.setResolutionNotes("误报: " + reason);
        vuln.setUpdatedTime(LocalDateTime.now());
        vulnerabilityMapper.updateById(vuln);
        log.info("标记为误报: {}", id);
    }

    @Override
    public List<VulnerabilityRecord> getVulnerabilitiesByTask(Long taskId) {
        return vulnerabilityMapper.selectByTaskId(taskId);
    }

    // ==================== 扫描规则管理 ====================

    @Override
    public IPage<ScanRule> pageRules(Page<ScanRule> page, String ruleType) {
        LambdaQueryWrapper<ScanRule> wrapper = new LambdaQueryWrapper<>();
        if (ruleType != null && !ruleType.isEmpty()) {
            wrapper.eq(ScanRule::getRuleType, ruleType);
        }
        wrapper.orderByDesc(ScanRule::getCreatedTime);
        return ruleMapper.selectPage(page, wrapper);
    }

    @Override
    public ScanRule getRuleById(Long id) {
        return ruleMapper.selectById(id);
    }

    @Override
    public ScanRule createRule(ScanRule rule) {
        rule.setEnabled(true);
        rule.setCreatedTime(LocalDateTime.now());
        ruleMapper.insert(rule);
        log.info("创建扫描规则: {}", rule.getRuleName());
        return rule;
    }

    @Override
    public void updateRule(ScanRule rule) {
        rule.setUpdatedTime(LocalDateTime.now());
        ruleMapper.updateById(rule);
        log.info("更新扫描规则: {}", rule.getId());
    }

    @Override
    public void deleteRule(Long id) {
        ruleMapper.deleteById(id);
        log.info("删除扫描规则: {}", id);
    }

    @Override
    public void toggleRule(Long id, Boolean enabled) {
        ScanRule rule = ruleMapper.selectById(id);
        if (rule == null) {
            throw new RuntimeException("规则不存在: " + id);
        }
        
        rule.setEnabled(enabled);
        rule.setUpdatedTime(LocalDateTime.now());
        ruleMapper.updateById(rule);
        log.info("切换规则状态: {} -> {}", id, enabled);
    }

    @Override
    public List<ScanRule> getEnabledRules(String ruleType) {
        if (ruleType != null && !ruleType.isEmpty()) {
            return ruleMapper.selectEnabledByType(ruleType);
        }
        return ruleMapper.selectAllEnabled();
    }

    // ==================== 修复计划管理 ====================

    @Override
    public IPage<RemediationPlan> pagePlans(Page<RemediationPlan> page, String status) {
        LambdaQueryWrapper<RemediationPlan> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(RemediationPlan::getPlanStatus, status);
        }
        wrapper.orderByDesc(RemediationPlan::getCreatedTime);
        return planMapper.selectPage(page, wrapper);
    }

    @Override
    public RemediationPlan getPlanById(Long id) {
        return planMapper.selectById(id);
    }

    @Override
    public RemediationPlan createPlan(RemediationPlan plan) {
        plan.setPlanStatus("DRAFT");
        plan.setCreatedTime(LocalDateTime.now());
        planMapper.insert(plan);
        log.info("创建修复计划: {}", plan.getPlanName());
        return plan;
    }

    @Override
    public void updatePlan(RemediationPlan plan) {
        plan.setUpdatedTime(LocalDateTime.now());
        planMapper.updateById(plan);
        log.info("更新修复计划: {}", plan.getId());
    }

    @Override
    public void deletePlan(Long id) {
        planMapper.deleteById(id);
        log.info("删除修复计划: {}", id);
    }

    @Override
    public void updatePlanStatus(Long id, String status) {
        RemediationPlan plan = planMapper.selectById(id);
        if (plan == null) {
            throw new RuntimeException("修复计划不存在: " + id);
        }
        
        plan.setPlanStatus(status);
        plan.setUpdatedTime(LocalDateTime.now());
        planMapper.updateById(plan);
        log.info("更新计划状态: {} -> {}", id, status);
    }

    @Override
    public void approvePlan(Long id, String approvedBy) {
        RemediationPlan plan = planMapper.selectById(id);
        if (plan == null) {
            throw new RuntimeException("修复计划不存在: " + id);
        }
        
        plan.setPlanStatus("APPROVED");
        plan.setApprovedBy(approvedBy);
        plan.setApprovedTime(LocalDateTime.now());
        plan.setUpdatedTime(LocalDateTime.now());
        planMapper.updateById(plan);
        log.info("审批修复计划: {} by {}", id, approvedBy);
    }

    @Override
    public void completePlan(Long id, String completionNotes) {
        RemediationPlan plan = planMapper.selectById(id);
        if (plan == null) {
            throw new RuntimeException("修复计划不存在: " + id);
        }
        
        plan.setPlanStatus("COMPLETED");
        plan.setCompletionNotes(completionNotes);
        plan.setCompletedTime(LocalDateTime.now());
        plan.setUpdatedTime(LocalDateTime.now());
        planMapper.updateById(plan);
        log.info("完成修复计划: {}", id);
    }

    // ==================== 依赖组件管理 ====================

    @Override
    public IPage<DependencyComponent> pageComponents(Page<DependencyComponent> page, String componentType) {
        LambdaQueryWrapper<DependencyComponent> wrapper = new LambdaQueryWrapper<>();
        if (componentType != null && !componentType.isEmpty()) {
            wrapper.eq(DependencyComponent::getComponentType, componentType);
        }
        wrapper.orderByDesc(DependencyComponent::getVulnerabilityCount);
        return componentMapper.selectPage(page, wrapper);
    }

    @Override
    public List<DependencyComponent> getVulnerableComponents() {
        return componentMapper.selectWithVulnerabilities();
    }

    // ==================== 统计分析 ====================

    @Override
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 任务统计
        LambdaQueryWrapper<SecurityScanTask> taskWrapper = new LambdaQueryWrapper<>();
        stats.put("totalTasks", taskMapper.selectCount(taskWrapper));
        
        taskWrapper.eq(SecurityScanTask::getStatus, "RUNNING");
        stats.put("runningTasks", taskMapper.selectCount(taskWrapper));
        
        // 漏洞统计
        LambdaQueryWrapper<VulnerabilityRecord> vulnWrapper = new LambdaQueryWrapper<>();
        stats.put("totalVulnerabilities", vulnerabilityMapper.selectCount(vulnWrapper));
        
        vulnWrapper.eq(VulnerabilityRecord::getStatus, "OPEN");
        stats.put("openVulnerabilities", vulnerabilityMapper.selectCount(vulnWrapper));
        
        vulnWrapper.clear();
        vulnWrapper.in(VulnerabilityRecord::getSeverity, "CRITICAL", "HIGH");
        stats.put("highRiskVulnerabilities", vulnerabilityMapper.selectCount(vulnWrapper));
        
        // 按严重程度统计
        List<Map<String, Object>> severityStats = vulnerabilityMapper.countBySeverity();
        stats.put("severityDistribution", severityStats);
        
        // 组件统计
        stats.put("vulnerableComponents", componentMapper.countVulnerableComponents());
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getVulnerabilityTrend(int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        return statisticsMapper.selectByDateRange(startDate, endDate)
                .stream()
                .map(stat -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", stat.getStatDate());
                    map.put("total", stat.getTotalVulnerabilities());
                    map.put("critical", stat.getCriticalCount());
                    map.put("high", stat.getHighCount());
                    map.put("medium", stat.getMediumCount());
                    map.put("low", stat.getLowCount());
                    return map;
                })
                .toList();
    }

    @Override
    public List<Map<String, Object>> getTopVulnerabilityTypes() {
        return vulnerabilityMapper.countByType();
    }

    @Override
    public List<Map<String, Object>> getSeverityDistribution() {
        return vulnerabilityMapper.countBySeverity();
    }

    @Override
    public Map<String, Object> getScanTaskStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        List<SecurityScanTaskMapper.TaskStatusCount> statusCounts = taskMapper.countTasksByStatus();
        stats.put("statusDistribution", statusCounts);
        
        // 最近7天的扫描任务数
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        int recentTaskCount = taskMapper.countTasksByTimeRange(sevenDaysAgo, LocalDateTime.now());
        stats.put("recentTaskCount", recentTaskCount);
        
        return stats;
    }
}
