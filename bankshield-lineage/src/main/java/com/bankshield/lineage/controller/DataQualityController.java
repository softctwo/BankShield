package com.bankshield.lineage.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.common.result.Result;
import com.bankshield.lineage.dto.QualityRuleTemplate;
import com.bankshield.lineage.dto.QualityStatistics;
import com.bankshield.lineage.dto.QualityTestResult;
import com.bankshield.lineage.entity.DataQualityRule;
import com.bankshield.lineage.entity.DataQualityResult;
import com.bankshield.lineage.service.DataQualityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据质量控制器
 *
 * @author BankShield
 * @since 2024-01-24
 */
@Slf4j
@RestController
@RequestMapping("/api/quality")
@Api(tags = "数据质量管理")
@RequiredArgsConstructor
public class DataQualityController {

    private final DataQualityService dataQualityService;

    /**
     * 执行质量检查
     */
    @PostMapping("/execute")
    @ApiOperation("执行质量检查")
    public Result<String> executeQualityChecks() {
        log.info("执行质量检查");
        
        try {
            dataQualityService.executeQualityChecks();
            return Result.success("质量检查任务已启动");
        } catch (Exception e) {
            log.error("执行质量检查失败", e);
            return Result.error("执行质量检查失败：" + e.getMessage());
        }
    }

    /**
     * 执行单个规则检查
     */
    @PostMapping("/execute/{ruleId}")
    @ApiOperation("执行单个规则检查")
    public Result<DataQualityResult> executeQualityCheck(
            @ApiParam("规则ID") @PathVariable Long ruleId) {
        
        log.info("执行单个规则检查，规则ID：{}", ruleId);
        
        try {
            DataQualityResult result = dataQualityService.executeQualityCheck(ruleId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("执行规则检查失败", e);
            return Result.error("执行规则检查失败：" + e.getMessage());
        }
    }

    /**
     * 计算表质量评分
     */
    @GetMapping("/score/table/{tableId}")
    @ApiOperation("计算表质量评分")
    public Result<Double> calculateTableQuality(
            @ApiParam("表ID") @PathVariable Long tableId) {
        
        log.info("计算表质量评分，表ID：{}", tableId);
        
        try {
            Double score = dataQualityService.calculateTableQuality(tableId);
            return Result.success(score);
        } catch (Exception e) {
            log.error("计算表质量评分失败", e);
            return Result.error("计算表质量评分失败：" + e.getMessage());
        }
    }

    /**
     * 计算字段质量评分
     */
    @GetMapping("/score/column/{columnId}")
    @ApiOperation("计算字段质量评分")
    public Result<Double> calculateColumnQuality(
            @ApiParam("字段ID") @PathVariable Long columnId) {
        
        log.info("计算字段质量评分，字段ID：{}", columnId);
        
        try {
            Double score = dataQualityService.calculateColumnQuality(columnId);
            return Result.success(score);
        } catch (Exception e) {
            log.error("计算字段质量评分失败", e);
            return Result.error("计算字段质量评分失败：" + e.getMessage());
        }
    }

    /**
     * 查询质量规则
     */
    @GetMapping("/rules")
    @ApiOperation("查询质量规则")
    public Result<IPage<DataQualityRule>> getQualityRules(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") int size,
            @ApiParam("规则类型") @RequestParam(required = false) String ruleType,
            @ApiParam("规则名称") @RequestParam(required = false) String ruleName,
            @ApiParam("是否启用") @RequestParam(required = false) Boolean enabled) {
        
        log.info("查询质量规则，页码：{}，每页条数：{}", page, size);
        
        try {
            Page<DataQualityRule> pageParam = new Page<>(page, size);
            IPage<DataQualityRule> result = dataQualityService.getQualityRules(pageParam, ruleType, ruleName, enabled);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询质量规则失败", e);
            return Result.error("查询质量规则失败：" + e.getMessage());
        }
    }

    /**
     * 创建质量规则
     */
    @PostMapping("/rules")
    @ApiOperation("创建质量规则")
    public Result<Long> createQualityRule(
            @ApiParam("规则信息") @RequestBody DataQualityRule rule) {
        
        log.info("创建质量规则：{}", rule.getRuleName());
        
        try {
            Long ruleId = dataQualityService.createQualityRule(rule);
            return Result.success(ruleId);
        } catch (Exception e) {
            log.error("创建质量规则失败", e);
            return Result.error("创建质量规则失败：" + e.getMessage());
        }
    }

    /**
     * 更新质量规则
     */
    @PutMapping("/rules/{ruleId}")
    @ApiOperation("更新质量规则")
    public Result<String> updateQualityRule(
            @ApiParam("规则ID") @PathVariable Long ruleId,
            @ApiParam("规则信息") @RequestBody DataQualityRule rule) {
        
        log.info("更新质量规则，规则ID：{}", ruleId);
        
        try {
            rule.setId(ruleId);
            boolean success = dataQualityService.updateQualityRule(rule);
            if (success) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新质量规则失败", e);
            return Result.error("更新质量规则失败：" + e.getMessage());
        }
    }

    /**
     * 删除质量规则
     */
    @DeleteMapping("/rules/{ruleId}")
    @ApiOperation("删除质量规则")
    public Result<String> deleteQualityRule(
            @ApiParam("规则ID") @PathVariable Long ruleId) {
        
        log.info("删除质量规则，规则ID：{}", ruleId);
        
        try {
            boolean success = dataQualityService.deleteQualityRule(ruleId);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除质量规则失败", e);
            return Result.error("删除质量规则失败：" + e.getMessage());
        }
    }

    /**
     * 查询质量检查结果
     */
    @GetMapping("/results")
    @ApiOperation("查询质量检查结果")
    public Result<IPage<DataQualityResult>> getQualityResults(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") int size,
            @ApiParam("规则ID") @RequestParam(required = false) Long ruleId,
            @ApiParam("表ID") @RequestParam(required = false) Long tableId,
            @ApiParam("开始时间") @RequestParam(required = false) String startTime,
            @ApiParam("结束时间") @RequestParam(required = false) String endTime) {
        
        log.info("查询质量检查结果，页码：{}，每页条数：{}", page, size);
        
        try {
            Page<DataQualityResult> pageParam = new Page<>(page, size);
            IPage<DataQualityResult> result = dataQualityService.getQualityResults(pageParam, ruleId, tableId, startTime, endTime);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询质量检查结果失败", e);
            return Result.error("查询质量检查结果失败：" + e.getMessage());
        }
    }

    /**
     * 获取质量统计信息
     */
    @GetMapping("/statistics/{tableId}")
    @ApiOperation("获取质量统计信息")
    public Result<QualityStatistics> getQualityStatistics(
            @ApiParam("表ID") @PathVariable Long tableId) {
        
        log.info("获取质量统计信息，表ID：{}", tableId);
        
        try {
            QualityStatistics statistics = dataQualityService.getQualityStatistics(tableId);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取质量统计信息失败", e);
            return Result.error("获取质量统计信息失败：" + e.getMessage());
        }
    }

    /**
     * 测试质量规则
     */
    @PostMapping("/test/{ruleId}")
    @ApiOperation("测试质量规则")
    public Result<QualityTestResult> testQualityRule(
            @ApiParam("规则ID") @PathVariable Long ruleId) {
        
        log.info("测试质量规则，规则ID：{}", ruleId);
        
        try {
            QualityTestResult result = dataQualityService.testQualityRule(ruleId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("测试质量规则失败", e);
            return Result.error("测试质量规则失败：" + e.getMessage());
        }
    }

    /**
     * 批量创建质量规则
     */
    @PostMapping("/rules/batch")
    @ApiOperation("批量创建质量规则")
    public Result<String> batchCreateQualityRules(
            @ApiParam("规则列表") @RequestBody List<DataQualityRule> rules) {
        
        log.info("批量创建质量规则，数量：{}", rules.size());
        
        try {
            boolean success = dataQualityService.batchCreateQualityRules(rules);
            if (success) {
                return Result.success("批量创建成功");
            } else {
                return Result.error("批量创建失败");
            }
        } catch (Exception e) {
            log.error("批量创建质量规则失败", e);
            return Result.error("批量创建质量规则失败：" + e.getMessage());
        }
    }

    /**
     * 获取规则模板
     */
    @GetMapping("/templates/{ruleType}")
    @ApiOperation("获取规则模板")
    public Result<List<QualityRuleTemplate>> getRuleTemplates(
            @ApiParam("规则类型") @PathVariable String ruleType) {
        
        log.info("获取规则模板，规则类型：{}", ruleType);
        
        try {
            List<QualityRuleTemplate> templates = dataQualityService.getRuleTemplates(ruleType);
            return Result.success(templates);
        } catch (Exception e) {
            log.error("获取规则模板失败", e);
            return Result.error("获取规则模板失败：" + e.getMessage());
        }
    }
}