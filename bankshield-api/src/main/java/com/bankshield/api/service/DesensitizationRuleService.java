package com.bankshield.api.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.DesensitizationRule;

import java.util.List;

/**
 * 脱敏规则服务接口
 */
public interface DesensitizationRuleService {
    
    /**
     * 创建规则
     */
    boolean createRule(DesensitizationRule rule);
    
    /**
     * 更新规则
     */
    boolean updateRule(DesensitizationRule rule);
    
    /**
     * 删除规则
     */
    boolean deleteRule(Long id);
    
    /**
     * 根据ID获取规则
     */
    DesensitizationRule getById(Long id);
    
    /**
     * 根据规则编码获取规则
     */
    DesensitizationRule getByRuleCode(String ruleCode);
    
    /**
     * 获取所有启用的规则
     */
    List<DesensitizationRule> getEnabledRules();
    
    /**
     * 根据数据类型获取规则
     */
    List<DesensitizationRule> getRulesByDataType(String dataType);
    
    /**
     * 根据敏感级别获取规则
     */
    List<DesensitizationRule> getRulesBySensitivityLevel(String level);
    
    /**
     * 根据应用范围获取规则
     */
    List<DesensitizationRule> getRulesByApplyScope(String scope);
    
    /**
     * 启用规则
     */
    boolean enableRule(Long id);
    
    /**
     * 禁用规则
     */
    boolean disableRule(Long id);
    
    /**
     * 调整规则优先级
     */
    boolean updatePriority(Long id, Integer priority);
    
    /**
     * 分页查询规则
     */
    Page<DesensitizationRule> pageRules(int current, int size, String dataType, String status);
    
    /**
     * 测试规则
     */
    String testRule(Long id, String testValue);
}
