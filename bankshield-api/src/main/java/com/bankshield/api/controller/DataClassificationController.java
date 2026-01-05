package com.bankshield.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.DataClassificationHistory;
import com.bankshield.api.entity.DataClassificationRule;
import com.bankshield.api.mapper.DataClassificationHistoryMapper;
import com.bankshield.api.service.DataClassificationEngineService;
import com.bankshield.api.service.DataClassificationRuleService;
import com.bankshield.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据分类分级控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/classification")
@Api(tags = "数据分类分级管理")
public class DataClassificationController {
    
    @Autowired
    private DataClassificationRuleService ruleService;
    
    @Autowired
    private DataClassificationEngineService engineService;
    
    @Autowired
    private DataClassificationHistoryMapper historyMapper;
    
    /**
     * 获取分级规则列表（分页）
     */
    @GetMapping("/rules")
    @ApiOperation("获取分级规则列表")
    public Result<Page<DataClassificationRule>> getRules(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String ruleStatus) {
        try {
            Page<DataClassificationRule> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<DataClassificationRule> wrapper = new LambdaQueryWrapper<>();
            
            if (ruleStatus != null && !ruleStatus.isEmpty()) {
                wrapper.eq(DataClassificationRule::getRuleStatus, ruleStatus);
            }
            
            wrapper.orderByDesc(DataClassificationRule::getPriority);
            Page<DataClassificationRule> result = ruleService.page(page, wrapper);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取分级规则列表失败", e);
            return Result.error("获取分级规则列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有启用的规则
     */
    @GetMapping("/rules/active")
    @ApiOperation("获取所有启用的规则")
    public Result<List<DataClassificationRule>> getActiveRules() {
        try {
            List<DataClassificationRule> rules = ruleService.getActiveRules();
            return Result.success(rules);
        } catch (Exception e) {
            log.error("获取启用规则失败", e);
            return Result.error("获取启用规则失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建分级规则
     */
    @PostMapping("/rules")
    @ApiOperation("创建分级规则")
    public Result<String> createRule(@RequestBody DataClassificationRule rule) {
        try {
            boolean success = ruleService.createRule(rule);
            if (success) {
                return Result.success("创建规则成功");
            } else {
                return Result.error("创建规则失败");
            }
        } catch (Exception e) {
            log.error("创建规则失败", e);
            return Result.error("创建规则失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新分级规则
     */
    @PutMapping("/rules/{id}")
    @ApiOperation("更新分级规则")
    public Result<String> updateRule(@PathVariable Long id, @RequestBody DataClassificationRule rule) {
        try {
            rule.setId(id);
            boolean success = ruleService.updateRule(rule);
            if (success) {
                return Result.success("更新规则成功");
            } else {
                return Result.error("更新规则失败");
            }
        } catch (Exception e) {
            log.error("更新规则失败", e);
            return Result.error("更新规则失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除分级规则
     */
    @DeleteMapping("/rules/{id}")
    @ApiOperation("删除分级规则")
    public Result<String> deleteRule(@PathVariable Long id) {
        try {
            boolean success = ruleService.removeById(id);
            if (success) {
                return Result.success("删除规则成功");
            } else {
                return Result.error("删除规则失败");
            }
        } catch (Exception e) {
            log.error("删除规则失败", e);
            return Result.error("删除规则失败: " + e.getMessage());
        }
    }
    
    /**
     * 启用/禁用规则
     */
    @PutMapping("/rules/{id}/status")
    @ApiOperation("启用/禁用规则")
    public Result<String> toggleRuleStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            boolean success = ruleService.toggleRuleStatus(id, status);
            if (success) {
                return Result.success("更新规则状态成功");
            } else {
                return Result.error("更新规则状态失败");
            }
        } catch (Exception e) {
            log.error("更新规则状态失败", e);
            return Result.error("更新规则状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 调整规则优先级
     */
    @PutMapping("/rules/{id}/priority")
    @ApiOperation("调整规则优先级")
    public Result<String> adjustPriority(@PathVariable Long id, @RequestParam Integer priority) {
        try {
            boolean success = ruleService.adjustPriority(id, priority);
            if (success) {
                return Result.success("调整优先级成功");
            } else {
                return Result.error("调整优先级失败");
            }
        } catch (Exception e) {
            log.error("调整优先级失败", e);
            return Result.error("调整优先级失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行自动分级
     */
    @PostMapping("/auto-classify")
    @ApiOperation("执行自动分级")
    public Result<Map<String, Object>> autoClassify() {
        try {
            int count = engineService.classifyAllUnclassified();
            Map<String, Object> result = new HashMap<>();
            result.put("classifiedCount", count);
            result.put("message", "自动分级完成，成功分级 " + count + " 个资产");
            return Result.success(result);
        } catch (Exception e) {
            log.error("自动分级失败", e);
            return Result.error("自动分级失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试规则匹配
     */
    @PostMapping("/test-match")
    @ApiOperation("测试规则匹配")
    public Result<Map<String, Object>> testMatch(
            @RequestParam String fieldName,
            @RequestParam(required = false) String content) {
        try {
            String level = engineService.matchRules(fieldName, content);
            Map<String, Object> result = new HashMap<>();
            result.put("fieldName", fieldName);
            result.put("content", content);
            result.put("matchedLevel", level);
            result.put("matched", level != null);
            return Result.success(result);
        } catch (Exception e) {
            log.error("测试规则匹配失败", e);
            return Result.error("测试规则匹配失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取分级历史
     */
    @GetMapping("/history")
    @ApiOperation("获取分级历史")
    public Result<Page<DataClassificationHistory>> getHistory(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long assetId) {
        try {
            Page<DataClassificationHistory> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<DataClassificationHistory> wrapper = new LambdaQueryWrapper<>();
            
            if (assetId != null) {
                wrapper.eq(DataClassificationHistory::getAssetId, assetId);
            }
            
            wrapper.orderByDesc(DataClassificationHistory::getClassifyTime);
            Page<DataClassificationHistory> result = historyMapper.selectPage(page, wrapper);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取分级历史失败", e);
            return Result.error("获取分级历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取分级统计
     */
    @GetMapping("/statistics")
    @ApiOperation("获取分级统计")
    public Result<Map<String, Object>> getStatistics() {
        try {
            // 这里可以调用统计服务获取详细统计数据
            // 目前返回模拟数据
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalAssets", 1000);
            stats.put("classifiedAssets", 850);
            stats.put("unclassifiedAssets", 150);
            
            Map<String, Integer> levelDistribution = new HashMap<>();
            levelDistribution.put("C1", 100);
            levelDistribution.put("C2", 300);
            levelDistribution.put("C3", 250);
            levelDistribution.put("C4", 150);
            levelDistribution.put("C5", 50);
            stats.put("levelDistribution", levelDistribution);
            
            Map<String, Integer> methodDistribution = new HashMap<>();
            methodDistribution.put("AUTO", 600);
            methodDistribution.put("MANUAL", 250);
            stats.put("methodDistribution", methodDistribution);
            
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取分级统计失败", e);
            return Result.error("获取分级统计失败: " + e.getMessage());
        }
    }
}
