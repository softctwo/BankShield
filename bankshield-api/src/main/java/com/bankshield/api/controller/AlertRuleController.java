package com.bankshield.api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.common.result.Result;
import com.bankshield.api.entity.AlertRule;
import com.bankshield.api.mapper.AlertRuleMapper;
import com.bankshield.api.service.AlertRuleEngine;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 告警规则管理接口控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/alert/rule")
@Api(tags = "告警规则管理")
public class AlertRuleController {

    @Autowired
    private AlertRuleMapper alertRuleMapper;

    @Autowired
    private AlertRuleEngine alertRuleEngine;

    @GetMapping("/page")
    @ApiOperation("分页查询告警规则")
    public Result<IPage<AlertRule>> getAlertRulePage(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") int size,
            @ApiParam("规则名称") @RequestParam(required = false) String ruleName,
            @ApiParam("规则类型") @RequestParam(required = false) String ruleType,
            @ApiParam("告警级别") @RequestParam(required = false) String alertLevel,
            @ApiParam("启用状态") @RequestParam(required = false) Integer enabled) {
        
        try {
            Page<AlertRule> pageParam = new Page<>(page, size);
            IPage<AlertRule> result = alertRuleMapper.selectPage(pageParam, ruleName, ruleType, alertLevel, enabled);
            return Result.success(result);
        } catch (Exception e) {
            log.error("分页查询告警规则失败", e);
            return Result.error("分页查询告警规则失败");
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("获取告警规则详情")
    public Result<AlertRule> getAlertRule(@PathVariable Long id) {
        try {
            AlertRule rule = alertRuleMapper.selectById(id);
            if (rule == null) {
                return Result.error("告警规则不存在");
            }
            return Result.success(rule);
        } catch (Exception e) {
            log.error("获取告警规则详情失败", e);
            return Result.error("获取告警规则详情失败");
        }
    }

    @PostMapping
    @ApiOperation("创建告警规则")
    public Result<String> createAlertRule(@RequestBody AlertRule alertRule) {
        try {
            // 验证规则
            if (!alertRuleEngine.validateRule(alertRule)) {
                return Result.error("告警规则配置无效");
            }

            // 检查规则名称是否已存在
            AlertRule existingRule = alertRuleMapper.selectByRuleName(alertRule.getRuleName());
            if (existingRule != null) {
                return Result.error("规则名称已存在");
            }

            // 设置默认值
            if (alertRule.getEnabled() == null) {
                alertRule.setEnabled(1);
            }
            alertRule.setCreateTime(LocalDateTime.now());
            alertRule.setUpdateTime(LocalDateTime.now());

            int result = alertRuleMapper.insert(alertRule);
            if (result > 0) {
                log.info("创建告警规则成功: {}", alertRule.getRuleName());
                return Result.success("创建告警规则成功");
            } else {
                return Result.error("创建告警规则失败");
            }
        } catch (Exception e) {
            log.error("创建告警规则失败", e);
            return Result.error("创建告警规则失败");
        }
    }

    @PutMapping("/{id}")
    @ApiOperation("更新告警规则")
    public Result<String> updateAlertRule(@PathVariable Long id, @RequestBody AlertRule alertRule) {
        try {
            // 检查规则是否存在
            AlertRule existingRule = alertRuleMapper.selectById(id);
            if (existingRule == null) {
                return Result.error("告警规则不存在");
            }

            // 验证规则
            alertRule.setId(id);
            if (!alertRuleEngine.validateRule(alertRule)) {
                return Result.error("告警规则配置无效");
            }

            // 检查规则名称是否已存在（排除自己）
            AlertRule ruleWithSameName = alertRuleMapper.selectByRuleName(alertRule.getRuleName());
            if (ruleWithSameName != null && !ruleWithSameName.getId().equals(id)) {
                return Result.error("规则名称已存在");
            }

            alertRule.setUpdateTime(LocalDateTime.now());

            int result = alertRuleMapper.updateById(alertRule);
            if (result > 0) {
                log.info("更新告警规则成功: {}", alertRule.getRuleName());
                return Result.success("更新告警规则成功");
            } else {
                return Result.error("更新告警规则失败");
            }
        } catch (Exception e) {
            log.error("更新告警规则失败", e);
            return Result.error("更新告警规则失败");
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除告警规则")
    public Result<String> deleteAlertRule(@PathVariable Long id) {
        try {
            AlertRule existingRule = alertRuleMapper.selectById(id);
            if (existingRule == null) {
                return Result.error("告警规则不存在");
            }

            int result = alertRuleMapper.deleteById(id);
            if (result > 0) {
                log.info("删除告警规则成功: {}", existingRule.getRuleName());
                return Result.success("删除告警规则成功");
            } else {
                return Result.error("删除告警规则失败");
            }
        } catch (Exception e) {
            log.error("删除告警规则失败", e);
            return Result.error("删除告警规则失败");
        }
    }

    @PutMapping("/{id}/enable")
    @ApiOperation("启用/禁用告警规则")
    public Result<String> toggleAlertRule(@PathVariable Long id, @RequestParam Integer enabled) {
        try {
            AlertRule existingRule = alertRuleMapper.selectById(id);
            if (existingRule == null) {
                return Result.error("告警规则不存在");
            }

            AlertRule updateRule = new AlertRule();
            updateRule.setId(id);
            updateRule.setEnabled(enabled);
            updateRule.setUpdateTime(LocalDateTime.now());

            int result = alertRuleMapper.updateById(updateRule);
            if (result > 0) {
                String status = enabled == 1 ? "启用" : "禁用";
                log.info("{}告警规则成功: {}", status, existingRule.getRuleName());
                return Result.success(status + "告警规则成功");
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            log.error("启用/禁用告警规则失败", e);
            return Result.error("操作失败");
        }
    }

    @PostMapping("/batch/enable")
    @ApiOperation("批量启用/禁用告警规则")
    public Result<String> batchToggleAlertRules(@RequestParam List<Long> ids, @RequestParam Integer enabled) {
        try {
            int result = alertRuleMapper.batchUpdateEnabled(ids, enabled);
            if (result > 0) {
                String status = enabled == 1 ? "启用" : "禁用";
                log.info("批量{}告警规则成功: {}条", status, result);
                return Result.success("批量" + status + "告警规则成功");
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            log.error("批量启用/禁用告警规则失败", e);
            return Result.error("操作失败");
        }
    }

    @PostMapping("/{id}/test")
    @ApiOperation("测试告警规则")
    public Result<String> testAlertRule(@PathVariable Long id) {
        try {
            AlertRule rule = alertRuleMapper.selectById(id);
            if (rule == null) {
                return Result.error("告警规则不存在");
            }

            // 创建一个模拟的监控指标来测试规则
            com.bankshield.api.entity.MonitorMetric testMetric = com.bankshield.api.entity.MonitorMetric.builder()
                    .metricName(rule.getMonitorMetric())
                    .metricType(rule.getRuleType())
                    .metricValue(rule.getThreshold() + 10) // 确保触发告警
                    .metricUnit("test")
                    .collectTime(LocalDateTime.now())
                    .build();

            boolean triggered = alertRuleEngine.checkRule(testMetric, rule);
            if (triggered) {
                // 触发测试告警
                alertRuleEngine.triggerAlert(rule, testMetric);
                return Result.success("告警规则测试成功，已触发测试告警");
            } else {
                return Result.error("告警规则测试未触发告警");
            }
        } catch (Exception e) {
            log.error("测试告警规则失败", e);
            return Result.error("测试告警规则失败");
        }
    }

    @GetMapping("/enabled")
    @ApiOperation("获取所有启用的告警规则")
    public Result<List<AlertRule>> getEnabledAlertRules() {
        try {
            List<AlertRule> enabledRules = alertRuleEngine.loadEnabledRules();
            return Result.success(enabledRules);
        } catch (Exception e) {
            log.error("获取启用的告警规则失败", e);
            return Result.error("获取启用的告警规则失败");
        }
    }
}