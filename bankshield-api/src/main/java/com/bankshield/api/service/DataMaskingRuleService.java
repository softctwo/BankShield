package com.bankshield.api.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.dto.MaskingAlgorithmParams;
import com.bankshield.api.entity.DataMaskingRule;
import com.bankshield.api.enums.MaskingAlgorithm;
import com.bankshield.api.enums.MaskingScenario;
import com.bankshield.api.enums.SensitiveDataType;
import com.bankshield.api.mapper.DataMaskingRuleMapper;
import com.bankshield.common.result.Result;
import com.bankshield.common.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据脱敏规则服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataMaskingRuleService {

    private final DataMaskingRuleMapper maskingRuleMapper;
    private final ObjectMapper objectMapper;

    /**
     * 分页查询脱敏规则
     */
    public IPage<DataMaskingRule> getPageList(Page<DataMaskingRule> page, String ruleName, 
                                             String sensitiveDataType, String scenario, Boolean enabled) {
        return maskingRuleMapper.selectPageWithConditions(page, ruleName, sensitiveDataType, scenario, enabled);
    }

    /**
     * 获取规则详情
     */
    public DataMaskingRule getById(Long id) {
        return maskingRuleMapper.selectById(id);
    }

    /**
     * 创建脱敏规则
     */
    @Transactional
    public Result<String> createRule(DataMaskingRule rule, String currentUser) {
        // 参数验证
        validateRule(rule);
        
        // 检查规则名称唯一性
        if (maskingRuleMapper.countByRuleName(rule.getRuleName(), -1L) > 0) {
            return Result.error("规则名称已存在");
        }

        // 设置默认值
        rule.setEnabled(true);
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());
        rule.setCreatedBy(currentUser);

        maskingRuleMapper.insert(rule);
        log.info("创建脱敏规则成功：{} - {}", rule.getRuleName(), rule.getSensitiveDataType());
        
        return Result.success("创建成功");
    }

    /**
     * 更新脱敏规则
     */
    @Transactional
    public Result<String> updateRule(DataMaskingRule rule, String currentUser) {
        if (rule.getId() == null) {
            return Result.error("规则ID不能为空");
        }

        // 参数验证
        validateRule(rule);
        
        // 检查规则名称唯一性
        if (maskingRuleMapper.countByRuleName(rule.getRuleName(), rule.getId()) > 0) {
            return Result.error("规则名称已存在");
        }

        // 检查规则是否存在
        DataMaskingRule existingRule = maskingRuleMapper.selectById(rule.getId());
        if (existingRule == null) {
            return Result.error("规则不存在");
        }

        // 更新字段
        existingRule.setRuleName(rule.getRuleName());
        existingRule.setSensitiveDataType(rule.getSensitiveDataType());
        existingRule.setMaskingAlgorithm(rule.getMaskingAlgorithm());
        existingRule.setAlgorithmParams(rule.getAlgorithmParams());
        existingRule.setApplicableScenarios(rule.getApplicableScenarios());
        existingRule.setDescription(rule.getDescription());
        existingRule.setUpdateTime(LocalDateTime.now());

        maskingRuleMapper.updateById(existingRule);
        log.info("更新脱敏规则成功：{} - {}", rule.getRuleName(), rule.getSensitiveDataType());
        
        return Result.success("更新成功");
    }

    /**
     * 删除脱敏规则
     */
    @Transactional
    public Result<String> deleteRule(Long id) {
        DataMaskingRule rule = maskingRuleMapper.selectById(id);
        if (rule == null) {
            return Result.error("规则不存在");
        }

        maskingRuleMapper.deleteById(id);
        log.info("删除脱敏规则成功：{} - {}", rule.getRuleName(), rule.getSensitiveDataType());
        
        return Result.success("删除成功");
    }

    /**
     * 启用/禁用脱敏规则
     */
    @Transactional
    public Result<String> updateRuleStatus(Long id, Boolean enabled) {
        DataMaskingRule rule = maskingRuleMapper.selectById(id);
        if (rule == null) {
            return Result.error("规则不存在");
        }

        rule.setEnabled(enabled);
        rule.setUpdateTime(LocalDateTime.now());
        maskingRuleMapper.updateById(rule);
        
        log.info("{}脱敏规则成功：{} - {}", enabled ? "启用" : "禁用", rule.getRuleName(), rule.getSensitiveDataType());
        return Result.success(enabled ? "启用成功" : "禁用成功");
    }

    /**
     * 根据敏感数据类型获取适用的脱敏规则
     */
    public List<DataMaskingRule> getRulesBySensitiveType(String sensitiveDataType, String scenario) {
        return maskingRuleMapper.selectEnabledRulesByType(sensitiveDataType, scenario);
    }

    /**
     * 获取支持的敏感数据类型列表
     */
    public List<String> getSensitiveDataTypes() {
        return Arrays.stream(SensitiveDataType.values())
                .map(SensitiveDataType::getCode)
                .collect(Collectors.toList());
    }

    /**
     * 获取支持的脱敏算法列表
     */
    public List<String> getMaskingAlgorithms() {
        return Arrays.stream(MaskingAlgorithm.values())
                .map(MaskingAlgorithm::getCode)
                .collect(Collectors.toList());
    }

    /**
     * 获取支持的脱敏场景列表
     */
    public List<String> getMaskingScenarios() {
        return Arrays.stream(MaskingScenario.values())
                .map(MaskingScenario::getCode)
                .collect(Collectors.toList());
    }

    /**
     * 参数验证
     */
    private void validateRule(DataMaskingRule rule) {
        if (!StringUtils.hasText(rule.getRuleName())) {
            throw new BusinessException("规则名称不能为空");
        }
        if (!StringUtils.hasText(rule.getSensitiveDataType())) {
            throw new BusinessException("敏感数据类型不能为空");
        }
        if (!StringUtils.hasText(rule.getMaskingAlgorithm())) {
            throw new BusinessException("脱敏算法不能为空");
        }

        // 验证枚举值
        if (SensitiveDataType.fromCode(rule.getSensitiveDataType()) == null) {
            throw new BusinessException("无效的敏感数据类型");
        }
        if (MaskingAlgorithm.fromCode(rule.getMaskingAlgorithm()) == null) {
            throw new BusinessException("无效的脱敏算法");
        }

        // 验证算法参数
        if (StringUtils.hasText(rule.getAlgorithmParams())) {
            try {
                objectMapper.readValue(rule.getAlgorithmParams(), MaskingAlgorithmParams.class);
            } catch (JsonProcessingException e) {
                throw new BusinessException("算法参数格式错误");
            }
        }

        // 验证适用场景
        if (StringUtils.hasText(rule.getApplicableScenarios())) {
            String[] scenarios = rule.getApplicableScenarios().split(",");
            for (String scenario : scenarios) {
                if (MaskingScenario.fromCode(scenario.trim()) == null) {
                    throw new BusinessException("无效的适用场景：" + scenario);
                }
            }
        }
    }
}