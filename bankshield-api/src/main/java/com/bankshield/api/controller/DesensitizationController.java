package com.bankshield.api.controller;

import com.bankshield.api.entity.DesensitizationLog;
import com.bankshield.api.entity.DesensitizationRule;
import com.bankshield.api.entity.DesensitizationTemplate;
import com.bankshield.api.service.DesensitizationRuleService;
import com.bankshield.api.service.DesensitizationTemplateService;
import com.bankshield.api.service.DesensitizationService;
import com.bankshield.common.result.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 数据脱敏控制器
 * 
 * @author BankShield
 */
@Slf4j
@RestController
@RequestMapping("/api/desensitization")
@RequiredArgsConstructor
@Api(tags = "数据脱敏管理")
public class DesensitizationController {

    private final DesensitizationRuleService ruleService;
    private final DesensitizationTemplateService templateService;
    private final DesensitizationService desensitizationService;

    // ==================== 脱敏规则管理 ====================

    @GetMapping("/rules")
    @ApiOperation("分页查询脱敏规则")
    @PreAuthorize("hasAuthority('desensitization:rule:query')")
    public Result<IPage<DesensitizationRule>> getRules(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String ruleName,
            @RequestParam(required = false) String dataType,
            @RequestParam(required = false) String status) {
        try {
            Page<DesensitizationRule> page = new Page<>(current, size);
            IPage<DesensitizationRule> result = ruleService.pageRules(page, ruleName, dataType, status);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询脱敏规则失败", e);
            return Result.error("查询脱敏规则失败: " + e.getMessage());
        }
    }

    @GetMapping("/rules/{id}")
    @ApiOperation("根据ID查询脱敏规则")
    @PreAuthorize("hasAuthority('desensitization:rule:query')")
    public Result<DesensitizationRule> getRuleById(@PathVariable Long id) {
        try {
            DesensitizationRule rule = ruleService.getById(id);
            return Result.success(rule);
        } catch (Exception e) {
            log.error("查询脱敏规则失败", e);
            return Result.error("查询脱敏规则失败: " + e.getMessage());
        }
    }

    @PostMapping("/rules")
    @ApiOperation("新增脱敏规则")
    @PreAuthorize("hasAuthority('desensitization:rule:add')")
    public Result<Void> createRule(@Valid @RequestBody DesensitizationRule rule) {
        try {
            ruleService.createRule(rule);
            return Result.success();
        } catch (Exception e) {
            log.error("新增脱敏规则失败", e);
            return Result.error("新增脱敏规则失败: " + e.getMessage());
        }
    }

    @PutMapping("/rules/{id}")
    @ApiOperation("更新脱敏规则")
    @PreAuthorize("hasAuthority('desensitization:rule:edit')")
    public Result<Void> updateRule(@PathVariable Long id, @Valid @RequestBody DesensitizationRule rule) {
        try {
            rule.setId(id);
            ruleService.updateRule(rule);
            return Result.success();
        } catch (Exception e) {
            log.error("更新脱敏规则失败", e);
            return Result.error("更新脱敏规则失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/rules/{id}")
    @ApiOperation("删除脱敏规则")
    @PreAuthorize("hasAuthority('desensitization:rule:delete')")
    public Result<Void> deleteRule(@PathVariable Long id) {
        try {
            ruleService.deleteRule(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除脱敏规则失败", e);
            return Result.error("删除脱敏规则失败: " + e.getMessage());
        }
    }

    @PutMapping("/rules/{id}/status")
    @ApiOperation("更新脱敏规则状态")
    @PreAuthorize("hasAuthority('desensitization:rule:edit')")
    public Result<Void> updateRuleStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            ruleService.updateRuleStatus(id, status);
            return Result.success();
        } catch (Exception e) {
            log.error("更新脱敏规则状态失败", e);
            return Result.error("更新脱敏规则状态失败: " + e.getMessage());
        }
    }

    @PostMapping("/rules/{id}/test")
    @ApiOperation("测试脱敏规则")
    @PreAuthorize("hasAuthority('desensitization:rule:test')")
    public Result<String> testRule(@PathVariable Long id, @RequestParam String testData) {
        try {
            String result = ruleService.testRule(id, testData);
            return Result.success(result);
        } catch (Exception e) {
            log.error("测试脱敏规则失败", e);
            return Result.error("测试脱敏规则失败: " + e.getMessage());
        }
    }

    @GetMapping("/rules/enabled")
    @ApiOperation("查询所有启用的脱敏规则")
    @PreAuthorize("hasAuthority('desensitization:rule:query')")
    public Result<List<DesensitizationRule>> getEnabledRules() {
        try {
            List<DesensitizationRule> rules = ruleService.getEnabledRules();
            return Result.success(rules);
        } catch (Exception e) {
            log.error("查询启用的脱敏规则失败", e);
            return Result.error("查询启用的脱敏规则失败: " + e.getMessage());
        }
    }

    // ==================== 脱敏模板管理 ====================

    @GetMapping("/templates")
    @ApiOperation("分页查询脱敏模板")
    @PreAuthorize("hasAuthority('desensitization:template:query')")
    public Result<IPage<DesensitizationTemplate>> getTemplates(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String templateName,
            @RequestParam(required = false) String templateType,
            @RequestParam(required = false) String status) {
        try {
            Page<DesensitizationTemplate> page = new Page<>(current, size);
            IPage<DesensitizationTemplate> result = templateService.pageTemplates(page, templateName, templateType, status);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询脱敏模板失败", e);
            return Result.error("查询脱敏模板失败: " + e.getMessage());
        }
    }

    @GetMapping("/templates/{id}")
    @ApiOperation("根据ID查询脱敏模板")
    @PreAuthorize("hasAuthority('desensitization:template:query')")
    public Result<DesensitizationTemplate> getTemplateById(@PathVariable Long id) {
        try {
            DesensitizationTemplate template = templateService.getById(id);
            return Result.success(template);
        } catch (Exception e) {
            log.error("查询脱敏模板失败", e);
            return Result.error("查询脱敏模板失败: " + e.getMessage());
        }
    }

    @PostMapping("/templates")
    @ApiOperation("新增脱敏模板")
    @PreAuthorize("hasAuthority('desensitization:template:add')")
    public Result<Void> createTemplate(@Valid @RequestBody DesensitizationTemplate template) {
        try {
            templateService.createTemplate(template);
            return Result.success();
        } catch (Exception e) {
            log.error("新增脱敏模板失败", e);
            return Result.error("新增脱敏模板失败: " + e.getMessage());
        }
    }

    @PutMapping("/templates/{id}")
    @ApiOperation("更新脱敏模板")
    @PreAuthorize("hasAuthority('desensitization:template:edit')")
    public Result<Void> updateTemplate(@PathVariable Long id, @Valid @RequestBody DesensitizationTemplate template) {
        try {
            template.setId(id);
            templateService.updateTemplate(template);
            return Result.success();
        } catch (Exception e) {
            log.error("更新脱敏模板失败", e);
            return Result.error("更新脱敏模板失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/templates/{id}")
    @ApiOperation("删除脱敏模板")
    @PreAuthorize("hasAuthority('desensitization:template:delete')")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        try {
            templateService.deleteTemplate(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除脱敏模板失败", e);
            return Result.error("删除脱敏模板失败: " + e.getMessage());
        }
    }

    @PutMapping("/templates/{id}/status")
    @ApiOperation("更新脱敏模板状态")
    @PreAuthorize("hasAuthority('desensitization:template:edit')")
    public Result<Void> updateTemplateStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            templateService.updateTemplateStatus(id, status);
            return Result.success();
        } catch (Exception e) {
            log.error("更新脱敏模板状态失败", e);
            return Result.error("更新脱敏模板状态失败: " + e.getMessage());
        }
    }

    @PostMapping("/templates/{id}/apply")
    @ApiOperation("应用脱敏模板")
    @PreAuthorize("hasAuthority('desensitization:template:apply')")
    public Result<Void> applyTemplate(
            @PathVariable Long id,
            @RequestParam(required = false) String scheduleTime) {
        try {
            templateService.applyTemplate(id, scheduleTime);
            return Result.success();
        } catch (Exception e) {
            log.error("应用脱敏模板失败", e);
            return Result.error("应用脱敏模板失败: " + e.getMessage());
        }
    }

    // ==================== 脱敏日志查询 ====================

    @GetMapping("/logs")
    @ApiOperation("分页查询脱敏日志")
    @PreAuthorize("hasAuthority('desensitization:log:query')")
    public Result<IPage<DesensitizationLog>> getLogs(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String targetTable,
            @RequestParam(required = false) String logType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            Page<DesensitizationLog> page = new Page<>(current, size);
            IPage<DesensitizationLog> result = desensitizationService.pageLogs(
                    page, userName, targetTable, logType, status, startTime, endTime);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询脱敏日志失败", e);
            return Result.error("查询脱敏日志失败: " + e.getMessage());
        }
    }

    @GetMapping("/logs/{id}")
    @ApiOperation("根据ID查询脱敏日志详情")
    @PreAuthorize("hasAuthority('desensitization:log:query')")
    public Result<DesensitizationLog> getLogById(@PathVariable Long id) {
        try {
            DesensitizationLog log = desensitizationService.getLogById(id);
            return Result.success(log);
        } catch (Exception e) {
            log.error("查询脱敏日志详情失败", e);
            return Result.error("查询脱敏日志详情失败: " + e.getMessage());
        }
    }

    @GetMapping("/logs/statistics")
    @ApiOperation("查询脱敏日志统计")
    @PreAuthorize("hasAuthority('desensitization:log:statistics')")
    public Result<Map<String, Object>> getLogStatistics(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            Map<String, Object> statistics = desensitizationService.getLogStatistics(startTime, endTime);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("查询脱敏日志统计失败", e);
            return Result.error("查询脱敏日志统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/logs/export")
    @ApiOperation("导出脱敏日志")
    @PreAuthorize("hasAuthority('desensitization:log:export')")
    public Result<String> exportLogs(
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String targetTable,
            @RequestParam(required = false) String logType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            String filePath = desensitizationService.exportLogs(
                    userName, targetTable, logType, status, startTime, endTime);
            return Result.success(filePath);
        } catch (Exception e) {
            log.error("导出脱敏日志失败", e);
            return Result.error("导出脱敏日志失败: " + e.getMessage());
        }
    }

    // ==================== 脱敏测试 ====================

    @PostMapping("/test/single")
    @ApiOperation("单条数据脱敏测试")
    @PreAuthorize("hasAuthority('desensitization:test:single')")
    public Result<String> testSingle(
            @ApiParam("规则编码") @RequestParam String ruleCode,
            @ApiParam("测试数据") @RequestParam String testData) {
        try {
            String result = desensitizationService.desensitizeSingle(ruleCode, testData);
            return Result.success(result);
        } catch (Exception e) {
            log.error("单条脱敏测试失败", e);
            return Result.error("单条脱敏测试失败: " + e.getMessage());
        }
    }

    @PostMapping("/test/batch")
    @ApiOperation("批量数据脱敏测试")
    @PreAuthorize("hasAuthority('desensitization:test:batch')")
    public Result<List<Map<String, Object>>> testBatch(
            @ApiParam("规则编码") @RequestParam String ruleCode,
            @ApiParam("测试数据列表") @RequestBody List<String> testDataList) {
        try {
            List<Map<String, Object>> results = desensitizationService.desensitizeBatch(ruleCode, testDataList);
            return Result.success(results);
        } catch (Exception e) {
            log.error("批量脱敏测试失败", e);
            return Result.error("批量脱敏测试失败: " + e.getMessage());
        }
    }

    @PostMapping("/quick-test")
    @ApiOperation("快捷脱敏测试")
    @PreAuthorize("hasAuthority('desensitization:test')")
    public Result<String> quickTest(
            @ApiParam("数据类型") @RequestParam String dataType,
            @ApiParam("测试数据") @RequestParam String testData) {
        try {
            String result = desensitizationService.quickDesensitize(dataType, testData);
            return Result.success(result);
        } catch (Exception e) {
            log.error("快捷脱敏测试失败", e);
            return Result.error("快捷脱敏测试失败: " + e.getMessage());
        }
    }
}
