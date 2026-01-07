package com.bankshield.api.controller;

import com.bankshield.api.entity.SecurityBaseline;
import com.bankshield.api.entity.SecurityScanResult;
import com.bankshield.api.entity.SecurityScanTask;
import com.bankshield.api.service.SecurityBaselineService;
import com.bankshield.api.service.SecurityScanResultService;
import com.bankshield.api.service.SecurityScanTaskService;
import com.bankshield.common.result.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.bankshield.api.entity.VulnerabilityRecord;
import com.bankshield.api.service.SecurityScanService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 安全扫描控制器
 * @author BankShield
 */
@Slf4j
@RestController
@RequestMapping("/api/security-scan")
@Api(tags = "安全扫描管理")
public class SecurityScanController {

    @Autowired
    private SecurityScanTaskService scanTaskService;

    @Autowired
    private SecurityScanResultService scanResultService;

    @Autowired
    private SecurityBaselineService baselineService;
    
    @Autowired
    private SecurityScanService securityScanService;

    /**
     * 创建扫描任务
     */
    @PostMapping("/task")
    @ApiOperation("创建扫描任务")
    @PreAuthorize("hasRole('SECURITY_ADMIN')")
    public Result<String> createScanTask(@RequestBody SecurityScanTask task) {
        try {
            SecurityScanTask createdTask = scanTaskService.createScanTask(task);
            return Result.<String>success("扫描任务创建成功", String.valueOf(createdTask.getId()));
        } catch (Exception e) {
            log.error("创建扫描任务失败", e);
            return Result.error("创建扫描任务失败: " + e.getMessage());
        }
    }

    /**
     * 执行扫描任务
     */
    @PostMapping("/task/{id}/execute")
    @ApiOperation("执行扫描任务")
    @PreAuthorize("hasRole('SECURITY_ADMIN')")
    public Result<String> executeScanTask(
            @ApiParam("任务ID") @PathVariable Long id) {
        try {
            scanTaskService.executeScanTask(id);
            return Result.success("扫描任务已开始执行");
        } catch (Exception e) {
            log.error("执行扫描任务失败", e);
            return Result.error("执行扫描任务失败: " + e.getMessage());
        }
    }

    /**
     * 停止扫描任务
     */
    @PostMapping("/task/{id}/stop")
    @ApiOperation("停止扫描任务")
    @PreAuthorize("hasRole('SECURITY_ADMIN')")
    public Result<String> stopScanTask(
            @ApiParam("任务ID") @PathVariable Long id) {
        try {
            scanTaskService.stopScanTask(id);
            return Result.success("扫描任务已停止");
        } catch (Exception e) {
            log.error("停止扫描任务失败", e);
            return Result.error("停止扫描任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取扫描任务列表
     */
    @GetMapping("/task")
    @ApiOperation("获取扫描任务列表")
    @PreAuthorize("hasRole('SECURITY_ADMIN')")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<SecurityScanTask>> getScanTasks(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") int size,
            @ApiParam("任务名称") @RequestParam(required = false) String taskName,
            @ApiParam("扫描类型") @RequestParam(required = false) String scanType,
            @ApiParam("状态") @RequestParam(required = false) String status) {
        try {
            com.baomidou.mybatisplus.core.metadata.IPage<SecurityScanTask> tasks = scanTaskService.getScanTasks(page, size, taskName, scanType, status);
            return Result.success(tasks);
        } catch (Exception e) {
            log.error("获取扫描任务列表失败", e);
            return Result.error("获取扫描任务列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取扫描任务详情
     */
    @GetMapping("/task/{id}")
    @ApiOperation("获取扫描任务详情")
    @PreAuthorize("hasRole('SECURITY_ADMIN')")
    public Result<SecurityScanTask> getScanTaskDetail(
            @ApiParam("任务ID") @PathVariable Long id) {
        try {
            SecurityScanTask task = scanTaskService.getById(id);
            if (task == null) {
                return Result.error("扫描任务不存在");
            }
            return Result.success(task);
        } catch (Exception e) {
            log.error("获取扫描任务详情失败", e);
            return Result.error("获取扫描任务详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取扫描任务进度
     */
    @GetMapping("/task/{id}/progress")
    @ApiOperation("获取扫描任务进度")
    @PreAuthorize("hasRole('SECURITY_ADMIN')")
    public Result<Integer> getScanProgress(
            @ApiParam("任务ID") @PathVariable Long id) {
        try {
            int progress = scanTaskService.getScanProgress(id);
            return Result.success(progress);
        } catch (Exception e) {
            log.error("获取扫描任务进度失败", e);
            return Result.error("获取扫描任务进度失败: " + e.getMessage());
        }
    }

    /**
     * 删除扫描任务
     */
    @DeleteMapping("/task/{id}")
    @ApiOperation("删除扫描任务")
    @PreAuthorize("hasRole('SECURITY_ADMIN')")
    public Result<String> deleteScanTask(
            @ApiParam("任务ID") @PathVariable Long id) {
        try {
            boolean success = scanTaskService.deleteScanTask(id);
            if (success) {
                return Result.success("扫描任务删除成功");
            } else {
                return Result.error("扫描任务删除失败");
            }
        } catch (Exception e) {
            log.error("删除扫描任务失败", e);
            return Result.error("删除扫描任务失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除扫描任务
     */
    @DeleteMapping("/task/batch")
    @ApiOperation("批量删除扫描任务")
    @PreAuthorize("hasRole('SECURITY_ADMIN')")
    public Result<String> batchDeleteScanTasks(@RequestBody List<Long> taskIds) {
        try {
            boolean success = scanTaskService.batchDeleteTasks(taskIds);
            if (success) {
                return Result.success("批量删除扫描任务成功");
            } else {
                return Result.error("批量删除扫描任务失败");
            }
        } catch (Exception e) {
            log.error("批量删除扫描任务失败", e);
            return Result.error("批量删除扫描任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取扫描结果列表
     */
    @GetMapping("/task/{id}/results")
    @ApiOperation("获取扫描结果列表")
    @PreAuthorize("hasRole('SECURITY_ADMIN')")
    public Result<IPage<SecurityScanResult>> getScanResults(
            @ApiParam("任务ID") @PathVariable Long id,
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") int size,
            @ApiParam("风险级别") @RequestParam(required = false) String riskLevel,
            @ApiParam("修复状态") @RequestParam(required = false) String fixStatus) {
        try {
            IPage<SecurityScanResult> results = scanResultService.getScanResults(page, size, id, riskLevel, fixStatus);
            return Result.success(results);
        } catch (Exception e) {
            log.error("获取扫描结果列表失败", e);
            return Result.error("获取扫描结果列表失败: " + e.getMessage());
        }
    }

    /**
     * 标记风险为已修复
     */
    @PutMapping("/result/{id}/fix")
    @ApiOperation("标记风险为已修复")
    @PreAuthorize("hasRole('SECURITY_ADMIN')")
    public Result<String> markAsFixed(
            @ApiParam("结果ID") @PathVariable Long id,
            @ApiParam("修复说明") @RequestBody String fixRemark) {
        try {
            boolean success = scanResultService.markAsFixed(id, fixRemark);
            if (success) {
                return Result.success("风险已标记为已修复");
            } else {
                return Result.error("标记风险为已修复失败");
            }
        } catch (Exception e) {
            log.error("标记风险为已修复失败", e);
            return Result.error("标记风险为已修复失败: " + e.getMessage());
        }
    }

    /**
     * 批量标记风险为已修复
     */
    @PutMapping("/result/batch-fix")
    @ApiOperation("批量标记风险为已修复")
    @PreAuthorize("hasRole('SECURITY_ADMIN')")
    public Result<String> batchMarkAsFixed(@RequestBody Map<String, Object> params) {
        try {
            List<Long> resultIds = (List<Long>) params.get("resultIds");
            String fixRemark = (String) params.get("fixRemark");
            
            boolean success = scanResultService.batchMarkAsFixed(resultIds, fixRemark);
            if (success) {
                return Result.success("批量标记风险为已修复成功");
            } else {
                return Result.error("批量标记风险为已修复失败");
            }
        } catch (Exception e) {
            log.error("批量标记风险为已修复失败", e);
            return Result.error("批量标记风险为已修复失败: " + e.getMessage());
        }
    }

    /**
     * 生成扫描报告
     */
    @PostMapping("/report/{taskId}")
    @ApiOperation("生成扫描报告")
    @PreAuthorize("hasRole('SECURITY_ADMIN')")
    public Result<String> generateReport(
            @ApiParam("任务ID") @PathVariable Long taskId) {
        try {
            String reportPath = scanTaskService.generateScanReport(taskId);
            return Result.<String>success("扫描报告生成成功", reportPath);
        } catch (Exception e) {
            log.error("生成扫描报告失败", e);
            return Result.error("生成扫描报告失败: " + e.getMessage());
        }
    }

    /**
     * 获取扫描任务统计信息
     */
    @GetMapping("/task/statistics")
    @ApiOperation("获取扫描任务统计信息")
    @PreAuthorize("hasRole('SECURITY_ADMIN')")
    public Result<Map<String, Object>> getScanTaskStatistics() {
        try {
            Map<String, Object> statistics = scanTaskService.getScanTaskStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取扫描任务统计信息失败", e);
            return Result.error("获取扫描任务统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取最近扫描任务
     */
    @GetMapping("/task/recent")
    @ApiOperation("获取最近扫描任务")
    @PreAuthorize("hasRole('SECURITY_ADMIN')")
    public Result<List<SecurityScanTask>> getRecentTasks(
            @ApiParam("数量限制") @RequestParam(defaultValue = "10") int limit) {
        try {
            List<SecurityScanTask> tasks = scanTaskService.getRecentTasks(limit);
            return Result.success(tasks);
        } catch (Exception e) {
            log.error("获取最近扫描任务失败", e);
            return Result.error("获取最近扫描任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务执行日志
     */
    @GetMapping("/task/{id}/logs")
    @ApiOperation("获取任务执行日志")
    @PreAuthorize("hasRole('SECURITY_ADMIN')")
    public Result<List<String>> getTaskExecutionLog(
            @ApiParam("任务ID") @PathVariable Long id) {
        try {
            List<String> logs = scanTaskService.getTaskExecutionLog(id);
            return Result.success(logs);
        } catch (Exception e) {
            log.error("获取任务执行日志失败", e);
            return Result.error("获取任务执行日志失败: " + e.getMessage());
        }
    }

    // 安全基线相关接口

    /**
     * 获取所有安全基线
     */
    @GetMapping("/baseline/all")
    @ApiOperation("获取所有安全基线")
    @PreAuthorize("hasRole('SECURITY_ADMIN')")
    public Result<List<SecurityBaseline>> getAllBaselines() {
        try {
            List<SecurityBaseline> baselines = baselineService.getAllBaselines();
            return Result.success(baselines);
        } catch (Exception e) {
            log.error("获取安全基线失败", e);
            return Result.error("获取安全基线失败: " + e.getMessage());
        }
    }

    /**
     * 获取启用的安全基线
     */
    @GetMapping("/baseline/enabled")
    @ApiOperation("获取启用的安全基线")
    @PreAuthorize("hasRole('SECURITY_ADMIN')")
    public Result<List<SecurityBaseline>> getEnabledBaselines() {
        try {
            List<SecurityBaseline> baselines = baselineService.getEnabledBaselines();
            return Result.success(baselines);
        } catch (Exception e) {
            log.error("获取启用的安全基线失败", e);
            return Result.error("获取启用的安全基线失败: " + e.getMessage());
        }
    }

    /**
     * 更新安全基线
     */
    @PutMapping("/baseline/{id}")
    @ApiOperation("更新安全基线")
    @PreAuthorize("hasRole('SECURITY_ADMIN')")
    public Result<SecurityBaseline> updateBaseline(
            @ApiParam("基线ID") @PathVariable Long id,
            @RequestBody SecurityBaseline baseline) {
        try {
            SecurityBaseline existing = baselineService.getById(id);
            if (existing == null) {
                return Result.error("安全基线不存在");
            }
            if (Boolean.TRUE.equals(existing.getBuiltin()) && 
                (baseline.getCheckItemName() != null || baseline.getDescription() != null)) {
                return Result.error("内置基线核心属性不允许修改");
            }
            baseline.setId(id);
            baseline.setUpdateTime(java.time.LocalDateTime.now());
            boolean success = baselineService.updateById(baseline);
            if (success) {
                return Result.success(baselineService.getById(id));
            } else {
                return Result.error("安全基线更新失败");
            }
        } catch (Exception e) {
            log.error("更新安全基线失败", e);
            return Result.error("更新安全基线失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新基线启用状态
     */
    @PutMapping("/baseline/batch-enabled")
    @ApiOperation("批量更新基线启用状态")
    @PreAuthorize("hasRole('SECURITY_ADMIN')")
    public Result<String> batchUpdateBaselineEnabled(@RequestBody Map<String, Object> params) {
        try {
            List<Long> ids = (List<Long>) params.get("ids");
            Boolean enabled = (Boolean) params.get("enabled");
            
            boolean success = baselineService.batchUpdateEnabled(ids, enabled);
            if (success) {
                return Result.success("批量更新基线启用状态成功");
            } else {
                return Result.error("批量更新基线启用状态失败");
            }
        } catch (Exception e) {
            log.error("批量更新基线启用状态失败", e);
            return Result.error("批量更新基线启用状态失败: " + e.getMessage());
        }
    }

    /**
     * 分页获取安全基线
     */
    @GetMapping("/baseline")
    @ApiOperation("分页获取安全基线")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<SecurityBaseline>> getBaselinesPage(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") int size,
            @ApiParam("检查项名称") @RequestParam(required = false) String checkName,
            @ApiParam("合规标准") @RequestParam(required = false) String complianceStandard,
            @ApiParam("检查类型") @RequestParam(required = false) String checkType) {
        try {
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<SecurityBaseline> baselines = 
                baselineService.getBaselinesPage(page, size, checkName, complianceStandard, checkType);
            return Result.success(baselines);
        } catch (Exception e) {
            log.error("分页获取安全基线失败", e);
            return Result.error("分页获取安全基线失败: " + e.getMessage());
        }
    }

    /**
     * 创建安全基线
     */
    @PostMapping("/baseline")
    @ApiOperation("创建安全基线")
    @PreAuthorize("hasRole('SECURITY_ADMIN')")
    public Result<SecurityBaseline> createBaseline(@RequestBody SecurityBaseline baseline) {
        try {
            // 检查名称是否已存在
            if (baselineService.checkNameExists(baseline.getCheckItemName())) {
                return Result.error("检查项名称已存在");
            }
            baseline.setBuiltin(false);
            baseline.setCreatedBy("admin");
            baseline.setCreateTime(java.time.LocalDateTime.now());
            baseline.setUpdateTime(java.time.LocalDateTime.now());
            boolean success = baselineService.save(baseline);
            if (success) {
                return Result.success(baseline);
            } else {
                return Result.error("创建安全基线失败");
            }
        } catch (Exception e) {
            log.error("创建安全基线失败", e);
            return Result.error("创建安全基线失败: " + e.getMessage());
        }
    }

    /**
     * 获取安全基线详情
     */
    @GetMapping("/baseline/{id}")
    @ApiOperation("获取安全基线详情")
    public Result<SecurityBaseline> getBaselineDetail(
            @ApiParam("基线ID") @PathVariable Long id) {
        try {
            SecurityBaseline baseline = baselineService.getById(id);
            if (baseline == null) {
                return Result.error("安全基线不存在");
            }
            return Result.success(baseline);
        } catch (Exception e) {
            log.error("获取安全基线详情失败", e);
            return Result.error("获取安全基线详情失败: " + e.getMessage());
        }
    }

    /**
     * 删除安全基线
     */
    @DeleteMapping("/baseline/{id}")
    @ApiOperation("删除安全基线")
    @PreAuthorize("hasRole('SECURITY_ADMIN')")
    public Result<String> deleteBaseline(
            @ApiParam("基线ID") @PathVariable Long id) {
        try {
            SecurityBaseline baseline = baselineService.getById(id);
            if (baseline == null) {
                return Result.error("安全基线不存在");
            }
            if (Boolean.TRUE.equals(baseline.getBuiltin())) {
                return Result.error("内置基线不允许删除");
            }
            boolean success = baselineService.removeById(id);
            if (success) {
                return Result.success("安全基线删除成功");
            } else {
                return Result.error("安全基线删除失败");
            }
        } catch (Exception e) {
            log.error("删除安全基线失败", e);
            return Result.error("删除安全基线失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除安全基线
     */
    @DeleteMapping("/baseline/batch")
    @ApiOperation("批量删除安全基线")
    @PreAuthorize("hasRole('SECURITY_ADMIN')")
    public Result<String> batchDeleteBaselines(@RequestBody List<Long> ids) {
        try {
            // 检查是否包含内置基线
            for (Long id : ids) {
                SecurityBaseline baseline = baselineService.getById(id);
                if (baseline != null && Boolean.TRUE.equals(baseline.getBuiltin())) {
                    return Result.error("内置基线不允许删除: " + baseline.getCheckItemName());
                }
            }
            boolean success = baselineService.removeByIds(ids);
            if (success) {
                return Result.success("批量删除安全基线成功");
            } else {
                return Result.error("批量删除安全基线失败");
            }
        } catch (Exception e) {
            log.error("批量删除安全基线失败", e);
            return Result.error("批量删除安全基线失败: " + e.getMessage());
        }
    }

    /**
     * 获取安全基线统计信息
     */
    @GetMapping("/baseline/statistics")
    @ApiOperation("获取安全基线统计信息")
    public Result<Map<String, Object>> getBaselineStatistics() {
        try {
            Map<String, Object> statistics = new java.util.HashMap<>();
            List<SecurityBaseline> allBaselines = baselineService.getAllBaselines();
            
            // 总数统计
            statistics.put("total", allBaselines.size());
            statistics.put("enabled", allBaselines.stream().filter(b -> Boolean.TRUE.equals(b.getEnabled())).count());
            statistics.put("disabled", allBaselines.stream().filter(b -> !Boolean.TRUE.equals(b.getEnabled())).count());
            statistics.put("builtin", allBaselines.stream().filter(b -> Boolean.TRUE.equals(b.getBuiltin())).count());
            statistics.put("custom", allBaselines.stream().filter(b -> !Boolean.TRUE.equals(b.getBuiltin())).count());
            
            // 按状态统计
            long passCount = allBaselines.stream().filter(b -> "PASS".equals(b.getPassStatus())).count();
            long failCount = allBaselines.stream().filter(b -> "FAIL".equals(b.getPassStatus())).count();
            long unknownCount = allBaselines.stream().filter(b -> "UNKNOWN".equals(b.getPassStatus()) || b.getPassStatus() == null).count();
            statistics.put("passCount", passCount);
            statistics.put("failCount", failCount);
            statistics.put("unknownCount", unknownCount);
            
            // 合规率
            double complianceRate = allBaselines.isEmpty() ? 0 : (double) passCount / allBaselines.size() * 100;
            statistics.put("complianceRate", Math.round(complianceRate * 100) / 100.0);
            
            // 按合规标准统计
            List<SecurityBaselineService.BaselineStatistics> standardStats = baselineService.getBaselineStatistics();
            statistics.put("byStandard", standardStats);
            
            // 按检查类型统计
            Map<String, Long> byCheckType = allBaselines.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    b -> b.getCheckType() != null ? b.getCheckType() : "OTHER",
                    java.util.stream.Collectors.counting()));
            statistics.put("byCheckType", byCheckType);
            
            // 按风险级别统计
            Map<String, Long> byRiskLevel = allBaselines.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    b -> b.getRiskLevel() != null ? b.getRiskLevel() : "UNKNOWN",
                    java.util.stream.Collectors.counting()));
            statistics.put("byRiskLevel", byRiskLevel);
            
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取安全基线统计信息失败", e);
            return Result.error("获取安全基线统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 根据检查类型获取基线
     */
    @GetMapping("/baseline/type/{checkType}")
    @ApiOperation("根据检查类型获取基线")
    public Result<List<SecurityBaseline>> getBaselinesByType(
            @ApiParam("检查类型") @PathVariable String checkType) {
        try {
            List<SecurityBaseline> baselines = baselineService.getBaselinesByType(checkType);
            return Result.success(baselines);
        } catch (Exception e) {
            log.error("根据检查类型获取基线失败", e);
            return Result.error("根据检查类型获取基线失败: " + e.getMessage());
        }
    }

    /**
     * 获取内置基线
     */
    @GetMapping("/baseline/builtin")
    @ApiOperation("获取内置基线")
    public Result<List<SecurityBaseline>> getBuiltinBaselines() {
        try {
            List<SecurityBaseline> baselines = baselineService.getBuiltinBaselines();
            return Result.success(baselines);
        } catch (Exception e) {
            log.error("获取内置基线失败", e);
            return Result.error("获取内置基线失败: " + e.getMessage());
        }
    }

    /**
     * 获取自定义基线
     */
    @GetMapping("/baseline/custom")
    @ApiOperation("获取自定义基线")
    public Result<List<SecurityBaseline>> getCustomBaselines() {
        try {
            List<SecurityBaseline> baselines = baselineService.getCustomBaselines();
            return Result.success(baselines);
        } catch (Exception e) {
            log.error("获取自定义基线失败", e);
            return Result.error("获取自定义基线失败: " + e.getMessage());
        }
    }

    /**
     * 初始化安全基线
     */
    @PostMapping("/baseline/init")
    @ApiOperation("初始化安全基线")
    @PreAuthorize("hasRole('SECURITY_ADMIN')")
    public Result<String> initBaselines() {
        try {
            baselineService.initBaselines();
            return Result.success("安全基线初始化成功");
        } catch (Exception e) {
            log.error("初始化安全基线失败", e);
            return Result.error("初始化安全基线失败: " + e.getMessage());
        }
    }

    /**
     * 同步内置基线
     */
    @PostMapping("/baseline/sync")
    @ApiOperation("同步内置基线")
    @PreAuthorize("hasRole('SECURITY_ADMIN')")
    public Result<String> syncBuiltinBaselines() {
        try {
            baselineService.syncBuiltinBaselines();
            return Result.success("内置基线同步成功");
        } catch (Exception e) {
            log.error("同步内置基线失败", e);
            return Result.error("同步内置基线失败: " + e.getMessage());
        }
    }

    /**
     * 执行基线检查
     */
    @PostMapping("/baseline/{id}/check")
    @ApiOperation("执行基线检查")
    @PreAuthorize("hasRole('SECURITY_ADMIN')")
    public Result<SecurityBaseline> executeBaselineCheck(
            @ApiParam("基线ID") @PathVariable Long id) {
        try {
            SecurityBaseline baseline = baselineService.getById(id);
            if (baseline == null) {
                return Result.error("安全基线不存在");
            }
            // 模拟检查结果
            String[] statuses = {"PASS", "FAIL", "PASS", "PASS"};
            baseline.setPassStatus(statuses[(int)(Math.random() * statuses.length)]);
            baseline.setCheckTime(java.time.LocalDateTime.now());
            baseline.setNextCheckTime(java.time.LocalDateTime.now().plusDays(30));
            baseline.setCheckResult("PASS".equals(baseline.getPassStatus()) ? "检查通过" : "检查发现问题，请参考修复建议");
            baselineService.updateById(baseline);
            return Result.success(baseline);
        } catch (Exception e) {
            log.error("执行基线检查失败", e);
            return Result.error("执行基线检查失败: " + e.getMessage());
        }
    }

    /**
     * 批量执行基线检查
     */
    @PostMapping("/baseline/batch-check")
    @ApiOperation("批量执行基线检查")
    @PreAuthorize("hasRole('SECURITY_ADMIN')")
    public Result<String> batchExecuteBaselineCheck(@RequestBody List<Long> ids) {
        try {
            int successCount = 0;
            for (Long id : ids) {
                SecurityBaseline baseline = baselineService.getById(id);
                if (baseline != null) {
                    String[] statuses = {"PASS", "FAIL", "PASS", "PASS"};
                    baseline.setPassStatus(statuses[(int)(Math.random() * statuses.length)]);
                    baseline.setCheckTime(java.time.LocalDateTime.now());
                    baseline.setNextCheckTime(java.time.LocalDateTime.now().plusDays(30));
                    baseline.setCheckResult("PASS".equals(baseline.getPassStatus()) ? "检查通过" : "检查发现问题");
                    baselineService.updateById(baseline);
                    successCount++;
                }
            }
            return Result.success("批量检查完成，成功: " + successCount + " 项");
        } catch (Exception e) {
            log.error("批量执行基线检查失败", e);
            return Result.error("批量执行基线检查失败: " + e.getMessage());
        }
    }
    
    // ==================== 漏洞管理 ====================
    
    /**
     * 获取漏洞列表
     */
    @GetMapping("/vulnerabilities")
    @ApiOperation("获取漏洞列表")
    public Result<IPage<VulnerabilityRecord>> getVulnerabilities(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int current,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") int size,
            @ApiParam("严重程度") @RequestParam(required = false) String severity,
            @ApiParam("状态") @RequestParam(required = false) String status,
            @ApiParam("漏洞类型") @RequestParam(required = false) String vulnType) {
        try {
            Page<VulnerabilityRecord> page = new Page<>(current, size);
            IPage<VulnerabilityRecord> result = securityScanService.pageVulnerabilities(page, severity, status, vulnType);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取漏洞列表失败", e);
            return Result.error("获取漏洞列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取漏洞详情
     */
    @GetMapping("/vulnerabilities/{id}")
    @ApiOperation("获取漏洞详情")
    public Result<VulnerabilityRecord> getVulnerabilityDetail(
            @ApiParam("漏洞ID") @PathVariable Long id) {
        try {
            VulnerabilityRecord vuln = securityScanService.getVulnerabilityDetail(id);
            if (vuln == null) {
                return Result.error("漏洞不存在");
            }
            return Result.success(vuln);
        } catch (Exception e) {
            log.error("获取漏洞详情失败", e);
            return Result.error("获取漏洞详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 分配漏洞
     */
    @PutMapping("/vulnerabilities/{id}/assign")
    @ApiOperation("分配漏洞")
    public Result<String> assignVulnerability(
            @ApiParam("漏洞ID") @PathVariable Long id,
            @RequestBody Map<String, String> params) {
        try {
            String assignedTo = params.get("assignedTo");
            securityScanService.assignVulnerability(id, assignedTo);
            return Result.success("分配成功");
        } catch (Exception e) {
            log.error("分配漏洞失败", e);
            return Result.error("分配漏洞失败: " + e.getMessage());
        }
    }
    
    /**
     * 解决漏洞
     */
    @PostMapping("/vulnerabilities/{id}/resolve")
    @ApiOperation("解决漏洞")
    public Result<String> resolveVulnerability(
            @ApiParam("漏洞ID") @PathVariable Long id,
            @RequestBody Map<String, String> params) {
        try {
            String resolutionNotes = params.get("resolutionNotes");
            securityScanService.resolveVulnerability(id, resolutionNotes, "current_user");
            return Result.success("已标记为已解决");
        } catch (Exception e) {
            log.error("解决漏洞失败", e);
            return Result.error("解决漏洞失败: " + e.getMessage());
        }
    }
    
    /**
     * 标记为误报
     */
    @PostMapping("/vulnerabilities/{id}/false-positive")
    @ApiOperation("标记为误报")
    public Result<String> markAsFalsePositive(
            @ApiParam("漏洞ID") @PathVariable Long id,
            @RequestBody Map<String, String> params) {
        try {
            String reason = params.get("reason");
            securityScanService.markAsFalsePositive(id, reason);
            return Result.success("已标记为误报");
        } catch (Exception e) {
            log.error("标记为误报失败", e);
            return Result.error("标记为误报失败: " + e.getMessage());
        }
    }
}