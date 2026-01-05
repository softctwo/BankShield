package com.bankshield.api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.dto.ComplianceStatisticsDTO;
import com.bankshield.api.entity.*;
import com.bankshield.api.service.ComplianceService;
import com.bankshield.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 合规性检查控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/compliance")
public class ComplianceController {
    
    @Autowired
    private ComplianceService complianceService;
    
    // ==================== 合规规则管理 ====================
    
    /**
     * 分页查询合规规则
     */
    @GetMapping("/rules")
    public Result<IPage<ComplianceRule>> getRules(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String standard,
            @RequestParam(required = false) String status) {
        try {
            Page<ComplianceRule> pageParam = new Page<>(page, size);
            IPage<ComplianceRule> result = complianceService.getRules(pageParam, category, standard, status);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询合规规则失败: {}", e.getMessage(), e);
            return Result.error("查询合规规则失败");
        }
    }
    
    /**
     * 获取规则详情
     */
    @GetMapping("/rules/{id}")
    public Result<ComplianceRule> getRuleById(@PathVariable Long id) {
        try {
            ComplianceRule rule = complianceService.getRuleById(id);
            return Result.success(rule);
        } catch (Exception e) {
            log.error("获取规则详情失败: {}", e.getMessage(), e);
            return Result.error("获取规则详情失败");
        }
    }
    
    /**
     * 创建合规规则
     */
    @PostMapping("/rules")
    public Result<Void> createRule(@RequestBody ComplianceRule rule) {
        try {
            boolean success = complianceService.createRule(rule);
            return success ? Result.success() : Result.error("创建规则失败");
        } catch (Exception e) {
            log.error("创建合规规则失败: {}", e.getMessage(), e);
            return Result.error("创建合规规则失败");
        }
    }
    
    /**
     * 更新合规规则
     */
    @PutMapping("/rules/{id}")
    public Result<Void> updateRule(@PathVariable Long id, @RequestBody ComplianceRule rule) {
        try {
            rule.setId(id);
            boolean success = complianceService.updateRule(rule);
            return success ? Result.success() : Result.error("更新规则失败");
        } catch (Exception e) {
            log.error("更新合规规则失败: {}", e.getMessage(), e);
            return Result.error("更新合规规则失败");
        }
    }
    
    /**
     * 删除合规规则
     */
    @DeleteMapping("/rules/{id}")
    public Result<Void> deleteRule(@PathVariable Long id) {
        try {
            boolean success = complianceService.deleteRule(id);
            return success ? Result.success() : Result.error("删除规则失败");
        } catch (Exception e) {
            log.error("删除合规规则失败: {}", e.getMessage(), e);
            return Result.error("删除合规规则失败");
        }
    }
    
    // ==================== 检查任务管理 ====================
    
    /**
     * 分页查询检查任务
     */
    @GetMapping("/tasks")
    public Result<IPage<ComplianceCheckTask>> getTasks(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) String standard,
            @RequestParam(required = false) String status) {
        try {
            Page<ComplianceCheckTask> pageParam = new Page<>(page, size);
            IPage<ComplianceCheckTask> result = complianceService.getTasks(pageParam, taskType, standard, status);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询检查任务失败: {}", e.getMessage(), e);
            return Result.error("查询检查任务失败");
        }
    }
    
    /**
     * 获取任务详情
     */
    @GetMapping("/tasks/{id}")
    public Result<ComplianceCheckTask> getTaskById(@PathVariable Long id) {
        try {
            ComplianceCheckTask task = complianceService.getTaskById(id);
            return Result.success(task);
        } catch (Exception e) {
            log.error("获取任务详情失败: {}", e.getMessage(), e);
            return Result.error("获取任务详情失败");
        }
    }
    
    /**
     * 创建检查任务
     */
    @PostMapping("/tasks")
    public Result<Void> createTask(@RequestBody ComplianceCheckTask task) {
        try {
            boolean success = complianceService.createTask(task);
            return success ? Result.success() : Result.error("创建任务失败");
        } catch (Exception e) {
            log.error("创建检查任务失败: {}", e.getMessage(), e);
            return Result.error("创建检查任务失败");
        }
    }
    
    /**
     * 执行检查任务
     */
    @PostMapping("/tasks/{id}/execute")
    public Result<Void> executeTask(@PathVariable Long id) {
        try {
            boolean success = complianceService.executeTask(id);
            return success ? Result.success() : Result.error("执行任务失败");
        } catch (Exception e) {
            log.error("执行检查任务失败: {}", e.getMessage(), e);
            return Result.error("执行检查任务失败");
        }
    }
    
    /**
     * 停止检查任务
     */
    @PostMapping("/tasks/{id}/stop")
    public Result<Void> stopTask(@PathVariable Long id) {
        try {
            boolean success = complianceService.stopTask(id);
            return success ? Result.success() : Result.error("停止任务失败");
        } catch (Exception e) {
            log.error("停止检查任务失败: {}", e.getMessage(), e);
            return Result.error("停止检查任务失败");
        }
    }
    
    /**
     * 删除检查任务
     */
    @DeleteMapping("/tasks/{id}")
    public Result<Void> deleteTask(@PathVariable Long id) {
        try {
            boolean success = complianceService.deleteTask(id);
            return success ? Result.success() : Result.error("删除任务失败");
        } catch (Exception e) {
            log.error("删除检查任务失败: {}", e.getMessage(), e);
            return Result.error("删除检查任务失败");
        }
    }
    
    // ==================== 检查结果管理 ====================
    
    /**
     * 分页查询检查结果
     */
    @GetMapping("/results")
    public Result<IPage<ComplianceCheckResult>> getResults(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) String checkStatus,
            @RequestParam(required = false) String riskLevel) {
        try {
            Page<ComplianceCheckResult> pageParam = new Page<>(page, size);
            IPage<ComplianceCheckResult> result = complianceService.getResults(pageParam, taskId, checkStatus, riskLevel);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询检查结果失败: {}", e.getMessage(), e);
            return Result.error("查询检查结果失败");
        }
    }
    
    /**
     * 获取结果详情
     */
    @GetMapping("/results/{id}")
    public Result<ComplianceCheckResult> getResultById(@PathVariable Long id) {
        try {
            ComplianceCheckResult result = complianceService.getResultById(id);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取结果详情失败: {}", e.getMessage(), e);
            return Result.error("获取结果详情失败");
        }
    }
    
    /**
     * 分配整改任务
     */
    @PostMapping("/results/{id}/assign")
    public Result<Void> assignRemediation(@PathVariable Long id, @RequestParam String assignee) {
        try {
            boolean success = complianceService.assignRemediation(id, assignee);
            return success ? Result.success() : Result.error("分配整改任务失败");
        } catch (Exception e) {
            log.error("分配整改任务失败: {}", e.getMessage(), e);
            return Result.error("分配整改任务失败");
        }
    }
    
    /**
     * 更新整改状态
     */
    @PutMapping("/results/{id}/remediation")
    public Result<Void> updateRemediationStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            boolean success = complianceService.updateRemediationStatus(id, status);
            return success ? Result.success() : Result.error("更新整改状态失败");
        } catch (Exception e) {
            log.error("更新整改状态失败: {}", e.getMessage(), e);
            return Result.error("更新整改状态失败");
        }
    }
    
    // ==================== 合规报告管理 ====================
    
    /**
     * 分页查询合规报告
     */
    @GetMapping("/reports")
    public Result<IPage<ComplianceReport>> getReports(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String standard,
            @RequestParam(required = false) String status) {
        try {
            Page<ComplianceReport> pageParam = new Page<>(page, size);
            IPage<ComplianceReport> result = complianceService.getReports(pageParam, standard, status);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询合规报告失败: {}", e.getMessage(), e);
            return Result.error("查询合规报告失败");
        }
    }
    
    /**
     * 获取报告详情
     */
    @GetMapping("/reports/{id}")
    public Result<ComplianceReport> getReportById(@PathVariable Long id) {
        try {
            ComplianceReport report = complianceService.getReportById(id);
            return Result.success(report);
        } catch (Exception e) {
            log.error("获取报告详情失败: {}", e.getMessage(), e);
            return Result.error("获取报告详情失败");
        }
    }
    
    /**
     * 生成合规报告
     */
    @PostMapping("/reports/generate")
    public Result<Void> generateReport(@RequestParam Long taskId) {
        try {
            boolean success = complianceService.generateReport(taskId);
            return success ? Result.success() : Result.error("生成报告失败");
        } catch (Exception e) {
            log.error("生成合规报告失败: {}", e.getMessage(), e);
            return Result.error("生成合规报告失败");
        }
    }
    
    /**
     * 导出合规报告
     */
    @GetMapping("/reports/{id}/export")
    public Result<String> exportReport(@PathVariable Long id) {
        try {
            String filePath = complianceService.exportReport(id);
            return Result.success(filePath);
        } catch (Exception e) {
            log.error("导出合规报告失败: {}", e.getMessage(), e);
            return Result.error("导出合规报告失败");
        }
    }
    
    // ==================== 统计分析 ====================
    
    /**
     * 获取合规统计数据
     */
    @GetMapping("/statistics")
    public Result<ComplianceStatisticsDTO> getStatistics() {
        try {
            ComplianceStatisticsDTO statistics = complianceService.getStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取合规统计数据失败: {}", e.getMessage(), e);
            return Result.error("获取合规统计数据失败");
        }
    }
}
