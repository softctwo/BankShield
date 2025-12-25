package com.bankshield.api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.dto.MaskingAlgorithmParams;
import com.bankshield.api.dto.MaskingTestRequest;
import com.bankshield.api.dto.MaskingTestResponse;
import com.bankshield.api.entity.DataMaskingRule;
import com.bankshield.api.service.DataMaskingEngine;
import com.bankshield.api.service.DataMaskingRuleService;
import com.bankshield.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 数据脱敏规则控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/masking")
@RequiredArgsConstructor
@Api(tags = "数据脱敏规则管理")
@Validated
public class DataMaskingController {

    private final DataMaskingRuleService maskingRuleService;
    private final DataMaskingEngine maskingEngine;

    /**
     * 分页查询脱敏规则
     */
    @GetMapping("/rule/page")
    @ApiOperation("分页查询脱敏规则")
    @PreAuthorize("hasAuthority('masking:rule:list')")
    public Result<IPage<DataMaskingRule>> getRulePage(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("规则名称") @RequestParam(required = false) String ruleName,
            @ApiParam("敏感数据类型") @RequestParam(required = false) String sensitiveDataType,
            @ApiParam("适用场景") @RequestParam(required = false) String scenario,
            @ApiParam("启用状态") @RequestParam(required = false) Boolean enabled) {
        
        Page<DataMaskingRule> page = new Page<>(pageNum, pageSize);
        IPage<DataMaskingRule> result = maskingRuleService.getPageList(page, ruleName, sensitiveDataType, scenario, enabled);
        return Result.success(result);
    }

    /**
     * 获取规则详情
     */
    @GetMapping("/rule/{id}")
    @ApiOperation("获取规则详情")
    @PreAuthorize("hasAuthority('masking:rule:query')")
    public Result<DataMaskingRule> getRuleDetail(
            @ApiParam("规则ID") @PathVariable @NotNull(message = "规则ID不能为空") Long id) {
        
        DataMaskingRule rule = maskingRuleService.getById(id);
        if (rule == null) {
            return Result.error("规则不存在");
        }
        return Result.success(rule);
    }

    /**
     * 创建脱敏规则
     */
    @PostMapping("/rule")
    @ApiOperation("创建脱敏规则")
    @PreAuthorize("hasAuthority('masking:rule:add')")
    public Result<String> createRule(@Valid @RequestBody DataMaskingRule rule) {
        String currentUser = getCurrentUser();
        return maskingRuleService.createRule(rule, currentUser);
    }

    /**
     * 更新脱敏规则
     */
    @PutMapping("/rule")
    @ApiOperation("更新脱敏规则")
    @PreAuthorize("hasAuthority('masking:rule:edit')")
    public Result<String> updateRule(@Valid @RequestBody DataMaskingRule rule) {
        String currentUser = getCurrentUser();
        return maskingRuleService.updateRule(rule, currentUser);
    }

    /**
     * 删除脱敏规则
     */
    @DeleteMapping("/rule/{id}")
    @ApiOperation("删除脱敏规则")
    @PreAuthorize("hasAuthority('masking:rule:delete')")
    public Result<String> deleteRule(
            @ApiParam("规则ID") @PathVariable @NotNull(message = "规则ID不能为空") Long id) {
        
        return maskingRuleService.deleteRule(id);
    }

    /**
     * 更新规则状态
     */
    @PutMapping("/rule/{id}/status")
    @ApiOperation("更新规则状态")
    @PreAuthorize("hasAuthority('masking:rule:edit')")
    public Result<String> updateRuleStatus(
            @ApiParam("规则ID") @PathVariable @NotNull(message = "规则ID不能为空") Long id,
            @ApiParam("启用状态") @RequestParam @NotNull(message = "状态不能为空") Boolean enabled) {
        
        return maskingRuleService.updateRuleStatus(id, enabled);
    }

    /**
     * 测试脱敏规则
     */
    @PostMapping("/test")
    @ApiOperation("测试脱敏规则")
    @PreAuthorize("hasAuthority('masking:rule:test')")
    public Result<MaskingTestResponse> testMaskingRule(@Valid @RequestBody MaskingTestRequest request) {
        try {
            DataMaskingRule rule = new DataMaskingRule();
            rule.setSensitiveDataType(request.getSensitiveDataType());
            rule.setMaskingAlgorithm(request.getMaskingAlgorithm());
            rule.setAlgorithmParams(request.getAlgorithmParams());
            rule.setEnabled(true);

            String maskedData = maskingEngine.maskData(request.getTestData(), rule);
            
            MaskingTestResponse response = new MaskingTestResponse();
            response.setOriginalData(request.getTestData());
            response.setMaskedData(maskedData);
            response.setSensitiveDataType(request.getSensitiveDataType());
            response.setMaskingAlgorithm(request.getMaskingAlgorithm());
            
            return Result.success(response);
        } catch (Exception e) {
            log.error("脱敏测试失败", e);
            return Result.error("脱敏测试失败：" + e.getMessage());
        }
    }

    /**
     * 获取支持的脱敏算法列表
     */
    @GetMapping("/algorithms")
    @ApiOperation("获取支持的脱敏算法列表")
    @PreAuthorize("hasAuthority('masking:rule:list')")
    public Result<List<String>> getMaskingAlgorithms() {
        List<String> algorithms = maskingRuleService.getMaskingAlgorithms();
        return Result.success(algorithms);
    }

    /**
     * 获取敏感数据类型列表
     */
    @GetMapping("/sensitive-types")
    @ApiOperation("获取敏感数据类型列表")
    @PreAuthorize("hasAuthority('masking:rule:list')")
    public Result<List<String>> getSensitiveDataTypes() {
        List<String> types = maskingRuleService.getSensitiveDataTypes();
        return Result.success(types);
    }

    /**
     * 获取适用场景列表
     */
    @GetMapping("/scenarios")
    @ApiOperation("获取适用场景列表")
    @PreAuthorize("hasAuthority('masking:rule:list')")
    public Result<List<String>> getMaskingScenarios() {
        List<String> scenarios = maskingRuleService.getMaskingScenarios();
        return Result.success(scenarios);
    }

    /**
     * 获取当前用户
     */
    private String getCurrentUser() {
        // 这里应该从Spring Security上下文中获取当前用户
        // 暂时返回默认值
        return "system";
    }
}