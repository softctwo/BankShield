package com.bankshield.api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.dto.ComplianceCheckResult;
import com.bankshield.common.result.Result;
import com.bankshield.api.entity.*;
import com.bankshield.api.mapper.*;
import com.bankshield.api.service.ComplianceCheckEngine;
import com.bankshield.api.service.ReportGenerationEngine;
import com.bankshield.api.service.ReportGenerationTaskService;
import com.bankshield.api.service.ReportTemplateService;
import com.bankshield.common.security.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 合规报表控制器
 */
@Slf4j
@Api(tags = "合规报表管理")
@RestController
@RequestMapping("/api/report")
public class ComplianceReportController {
    
    @Autowired
    private ReportTemplateService templateService;
    
    @Autowired
    private ReportGenerationTaskService taskService;
    
    @Autowired
    private ComplianceCheckEngine checkEngine;
    
    @Autowired
    private ReportGenerationEngine generationEngine;
    
    @Autowired
    private ReportTemplateMapper templateMapper;
    
    @Autowired
    private ReportGenerationTaskMapper taskMapper;
    
    @Autowired
    private ComplianceCheckItemMapper checkItemMapper;
    
    @Autowired
    private ComplianceCheckHistoryMapper historyMapper;

    @Autowired
    private Executor reportGenerationExecutor;
    
    /**
     * 报表模板管理 - 分页查询
     */
    @ApiOperation("分页查询报表模板")
    @GetMapping("/template/page")
    @PreAuthorize("hasRole('AUDIT_ADMIN')")
    public Result<IPage<ReportTemplate>> getTemplates(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("模板名称") @RequestParam(required = false) String templateName,
            @ApiParam("报表类型") @RequestParam(required = false) String reportType,
            @ApiParam("启用状态") @RequestParam(required = false) Boolean enabled) {
        
        Page<ReportTemplate> pageParam = new Page<>(page, size);
        IPage<ReportTemplate> result = templateMapper.selectTemplatePage(pageParam, templateName, reportType, enabled);
        return Result.success(result);
    }
    
    /**
     * 报表模板管理 - 创建模板
     */
    @ApiOperation("创建报表模板")
    @PostMapping("/template")
    @PreAuthorize("hasRole('AUDIT_ADMIN')")
    public Result<String> createTemplate(@RequestBody ReportTemplate template) {
        try {
            templateService.createTemplate(template);
            return Result.success("模板创建成功");
        } catch (Exception e) {
            log.error("创建模板失败", e);
            return Result.error("模板创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 报表模板管理 - 更新模板
     */
    @ApiOperation("更新报表模板")
    @PutMapping("/template/{id}")
    @PreAuthorize("hasRole('AUDIT_ADMIN')")
    public Result<String> updateTemplate(@PathVariable Long id, @RequestBody ReportTemplate template) {
        try {
            template.setId(id);
            templateService.updateTemplate(template);
            return Result.success("模板更新成功");
        } catch (Exception e) {
            log.error("更新模板失败", e);
            return Result.error("模板更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 报表模板管理 - 删除模板
     */
    @ApiOperation("删除报表模板")
    @DeleteMapping("/template/{id}")
    @PreAuthorize("hasRole('AUDIT_ADMIN')")
    public Result<String> deleteTemplate(@PathVariable Long id) {
        try {
            templateMapper.deleteById(id);
            return Result.success("模板删除成功");
        } catch (Exception e) {
            log.error("删除模板失败", e);
            return Result.error("模板删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 报表模板管理 - 获取模板详情
     */
    @ApiOperation("获取报表模板详情")
    @GetMapping("/template/{id}")
    @PreAuthorize("hasRole('AUDIT_ADMIN')")
    public Result<ReportTemplate> getTemplateDetail(@PathVariable Long id) {
        ReportTemplate template = templateMapper.selectById(id);
        if (template == null) {
            return Result.error("模板不存在");
        }
        return Result.success(template);
    }
    
    /**
     * 报表生成任务 - 创建任务
     */
    @ApiOperation("创建报表生成任务")
    @PostMapping("/task")
    @PreAuthorize("hasRole('AUDIT_ADMIN')")
    public Result<String> createTask(@RequestBody Map<String, Object> params) {
        try {
            Long templateId = Long.valueOf(params.get("templateId").toString());
            String reportPeriod = params.get("reportPeriod").toString();
            
            // 创建生成任务
            ReportGenerationTask task = new ReportGenerationTask();
            task.setTemplateId(templateId);
            task.setStatus("PENDING");
            task.setCreatedBy(SecurityUtils.getCurrentUsername());
            task.setReportPeriod(reportPeriod);
            task.setCreateTime(LocalDateTime.now());
            taskMapper.insert(task);
            
            // 异步执行生成（使用专用线程池）
            CompletableFuture.runAsync(() -> {
                try {
                    taskService.generateReport(task.getId());
                } catch (Exception e) {
                    log.error("异步生成报表失败", e);
                }
            }, reportGenerationExecutor);
            
            return Result.success("生成任务已创建，任务ID: " + task.getId());
        } catch (Exception e) {
            log.error("创建任务失败", e);
            return Result.error("创建任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 报表生成任务 - 获取任务状态
     */
    @ApiOperation("获取报表生成任务状态")
    @GetMapping("/task/{id}")
    @PreAuthorize("hasRole('AUDIT_ADMIN')")
    public Result<ReportGenerationTask> getTaskStatus(@PathVariable Long id) {
        ReportGenerationTask task = taskMapper.selectById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        return Result.success(task);
    }
    
    /**
     * 报表生成任务 - 分页查询任务
     */
    @ApiOperation("分页查询报表生成任务")
    @GetMapping("/task/page")
    @PreAuthorize("hasRole('AUDIT_ADMIN')")
    public Result<IPage<ReportGenerationTask>> getTasks(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("模板ID") @RequestParam(required = false) Long templateId,
            @ApiParam("任务状态") @RequestParam(required = false) String status,
            @ApiParam("创建人") @RequestParam(required = false) String createdBy) {
        
        Page<ReportGenerationTask> pageParam = new Page<>(page, size);
        IPage<ReportGenerationTask> result = taskMapper.selectTaskPage(pageParam, templateId, status, createdBy);
        return Result.success(result);
    }
    
    /**
     * 报表下载
     */
    @ApiOperation("下载报表文件")
    @GetMapping("/download/{taskId}")
    @PreAuthorize("hasRole('AUDIT_ADMIN')")
    public ResponseEntity<Resource> downloadReport(@PathVariable Long taskId) {
        try {
            ReportGenerationTask task = taskMapper.selectById(taskId);
            if (task == null || !"SUCCESS".equals(task.getStatus())) {
                return ResponseEntity.notFound().build();
            }
            
            String reportFilePath = task.getReportFilePath();
            if (reportFilePath == null || reportFilePath.trim().isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            File file = new File(reportFilePath);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(file);
            String fileName = "合规报告_" + task.getReportPeriod() + ".pdf";
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("下载报表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 合规检查 - 执行检查
     */
    @ApiOperation("执行合规检查")
    @PostMapping("/check")
    @PreAuthorize("hasRole('AUDIT_ADMIN')")
    public Result<ComplianceCheckResult> performCheck(@RequestBody Map<String, String> params) {
        try {
            String standard = params.get("standard");
            if (standard == null || standard.trim().isEmpty()) {
                return Result.error("合规标准不能为空");
            }

            // 调用对应的检查方法
            ComplianceCheckResult result = new ComplianceCheckResult();
            result.setStandard(standard);
            result.setCheckTime(LocalDateTime.now());

            List<ComplianceCheckItem> items = null;
            switch (standard) {
                case "等保":
                case "等保二级":
                case "等保三级":
                    items = checkEngine.performDengBaoCheck();
                    break;
                case "PCI-DSS":
                    items = checkEngine.performPciDssCheck();
                    break;
                case "GDPR":
                    items = checkEngine.performGdprCheck();
                    break;
                default:
                    throw new IllegalArgumentException("不支持的合规标准: " + standard);
            }
            result.setAllItems(items);
            result.setTotalCount(items.size());
            result.calculateScore();

            return Result.success(result);
        } catch (Exception e) {
            log.error("执行合规检查失败", e);
            return Result.error("执行合规检查失败: " + e.getMessage());
        }
    }
    
    /**
     * 合规检查 - 获取检查项列表
     */
    @ApiOperation("获取合规检查项列表")
    @GetMapping("/check/item/page")
    @PreAuthorize("hasRole('AUDIT_ADMIN')")
    public Result<IPage<ComplianceCheckItem>> getCheckItems(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("检查项名称") @RequestParam(required = false) String checkItemName,
            @ApiParam("合规标准") @RequestParam(required = false) String complianceStandard,
            @ApiParam("通过状态") @RequestParam(required = false) String passStatus) {
        
        Page<ComplianceCheckItem> pageParam = new Page<>(page, size);
        IPage<ComplianceCheckItem> result = checkItemMapper.selectCheckItemPage(pageParam, checkItemName, complianceStandard, passStatus);
        return Result.success(result);
    }
    
    /**
     * 合规检查 - 获取合规评分
     */
    @ApiOperation("获取合规评分")
    @GetMapping("/check/score")
    @PreAuthorize("hasRole('AUDIT_ADMIN')")
    public Result<Map<String, Object>> getComplianceScore(@ApiParam("合规标准") @RequestParam String standard) {
        try {
            double score = checkEngine.calculateComplianceScore(standard);
            List<ComplianceCheckItem> nonCompliances = checkEngine.getNonCompliantItems(standard);
            
            Map<String, Object> result = new HashMap<>();
            result.put("standard", standard);
            result.put("score", score);
            result.put("nonComplianceCount", nonCompliances.size());
            result.put("nonCompliances", nonCompliances);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取合规评分失败", e);
            return Result.error("获取合规评分失败: " + e.getMessage());
        }
    }
    
    /**
     * 合规检查 - 获取检查历史
     */
    @ApiOperation("获取合规检查历史")
    @GetMapping("/check/history/page")
    @PreAuthorize("hasRole('AUDIT_ADMIN')")
    public Result<IPage<ComplianceCheckHistory>> getCheckHistory(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("合规标准") @RequestParam(required = false) String complianceStandard,
            @ApiParam("检查人") @RequestParam(required = false) String checker,
            @ApiParam("开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @ApiParam("结束时间") @RequestParam(required = false) LocalDateTime endTime) {
        
        Page<ComplianceCheckHistory> pageParam = new Page<>(page, size);
        IPage<ComplianceCheckHistory> result = historyMapper.selectHistoryPage(pageParam, complianceStandard, checker, startTime, endTime);
        return Result.success(result);
    }
    
    /**
     * 统计信息
     */
    @ApiOperation("获取报表统计信息")
    @GetMapping("/stats")
    @PreAuthorize("hasRole('AUDIT_ADMIN')")
    public Result<Map<String, Object>> getReportStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 报表数量统计
            stats.put("totalTemplates", templateMapper.selectCount(null));
            stats.put("enabledTemplates", templateMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ReportTemplate>().eq("enabled", true)));
            
            // 任务统计
            stats.put("totalTasks", taskMapper.selectCount(null));
            stats.put("pendingTasks", taskMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ReportGenerationTask>().eq("status", "PENDING")));
            stats.put("runningTasks", taskMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ReportGenerationTask>().eq("status", "RUNNING")));
            stats.put("successTasks", taskMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ReportGenerationTask>().eq("status", "SUCCESS")));
            stats.put("failedTasks", taskMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ReportGenerationTask>().eq("status", "FAILED")));
            
            // 合规评分趋势（最近7次检查）
            stats.put("dengBaoTrend", historyMapper.selectRecent7Scores("等保"));
            stats.put("pciDssTrend", historyMapper.selectRecent7Scores("PCI-DSS"));
            stats.put("gdprTrend", historyMapper.selectRecent7Scores("GDPR"));
            
            // 最近不合规项
            stats.put("recentNonCompliances", checkItemMapper.selectRecentFailedItems(5));
            
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取报表统计信息失败", e);
            return Result.error("获取报表统计信息失败: " + e.getMessage());
        }
    }
}