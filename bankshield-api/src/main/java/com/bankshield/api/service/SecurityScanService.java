package com.bankshield.api.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.*;

import java.util.List;
import java.util.Map;

public interface SecurityScanService {

    // 扫描任务管理
    IPage<SecurityScanTask> pageTasks(Page<SecurityScanTask> page, String taskType, String status);
    
    SecurityScanTask getTaskById(Long id);
    
    SecurityScanTask createTask(SecurityScanTask task);
    
    void startScan(Long taskId);
    
    void stopScan(Long taskId);
    
    void deleteTask(Long taskId);
    
    List<SecurityScanTask> getRecentTasks(int limit);

    // 漏洞管理
    IPage<VulnerabilityRecord> pageVulnerabilities(Page<VulnerabilityRecord> page, 
                                                    String severity, String status, String vulnType);
    
    VulnerabilityRecord getVulnerabilityDetail(Long id);
    
    void updateVulnerabilityStatus(Long id, String status);
    
    void assignVulnerability(Long id, String assignedTo);
    
    void resolveVulnerability(Long id, String resolutionNotes, String resolvedBy);
    
    void markAsFalsePositive(Long id, String reason);
    
    List<VulnerabilityRecord> getVulnerabilitiesByTask(Long taskId);

    // 扫描规则管理
    IPage<ScanRule> pageRules(Page<ScanRule> page, String ruleType);
    
    ScanRule getRuleById(Long id);
    
    ScanRule createRule(ScanRule rule);
    
    void updateRule(ScanRule rule);
    
    void deleteRule(Long id);
    
    void toggleRule(Long id, Boolean enabled);
    
    List<ScanRule> getEnabledRules(String ruleType);

    // 修复计划管理
    IPage<RemediationPlan> pagePlans(Page<RemediationPlan> page, String status);
    
    RemediationPlan getPlanById(Long id);
    
    RemediationPlan createPlan(RemediationPlan plan);
    
    void updatePlan(RemediationPlan plan);
    
    void deletePlan(Long id);
    
    void updatePlanStatus(Long id, String status);
    
    void approvePlan(Long id, String approvedBy);
    
    void completePlan(Long id, String completionNotes);

    // 依赖组件管理
    IPage<DependencyComponent> pageComponents(Page<DependencyComponent> page, String componentType);
    
    List<DependencyComponent> getVulnerableComponents();

    // 统计分析
    Map<String, Object> getDashboardStatistics();
    
    List<Map<String, Object>> getVulnerabilityTrend(int days);
    
    List<Map<String, Object>> getTopVulnerabilityTypes();
    
    List<Map<String, Object>> getSeverityDistribution();
    
    Map<String, Object> getScanTaskStatistics();
}
