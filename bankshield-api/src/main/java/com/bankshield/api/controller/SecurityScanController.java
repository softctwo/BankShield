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

    /**
     * 创建扫描任务
     */
    @PostMapping("/task")
    @ApiOperation("创建扫描任务")
    @PreAuthorize("hasRole('SECURITY_ADMIN')")
    public Result<String> createScanTask(@RequestBody SecurityScanTask task) {
        try {
            SecurityScanTask createdTask = scanTaskService.createScanTask(task);
            return Result.success("扫描任务创建成功", String.valueOf(createdTask.getId()));
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
            return Result.success("扫描报告生成成功", reportPath);
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
    public Result<String> updateBaseline(
            @ApiParam("基线ID") @PathVariable Long id,
            @RequestBody SecurityBaseline baseline) {
        try {
            baseline.setId(id);
            boolean success = baselineService.updateById(baseline);
            if (success) {
                return Result.success("安全基线更新成功");
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
}