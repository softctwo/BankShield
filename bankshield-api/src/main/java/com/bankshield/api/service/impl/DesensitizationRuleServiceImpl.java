package com.bankshield.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.DesensitizationRule;
import com.bankshield.api.mapper.DesensitizationRuleMapper;
import com.bankshield.api.service.DesensitizationRuleService;
import com.bankshield.api.util.DesensitizationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 脱敏规则服务实现
 */
@Slf4j
@Service
public class DesensitizationRuleServiceImpl implements DesensitizationRuleService {
    
    @Autowired
    private DesensitizationRuleMapper ruleMapper;
    
    @Override
    public boolean createRule(DesensitizationRule rule) {
        try {
            return ruleMapper.insert(rule) > 0;
        } catch (Exception e) {
            log.error("创建脱敏规则失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean updateRule(DesensitizationRule rule) {
        try {
            return ruleMapper.updateById(rule) > 0;
        } catch (Exception e) {
            log.error("更新脱敏规则失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean deleteRule(Long id) {
        try {
            return ruleMapper.deleteById(id) > 0;
        } catch (Exception e) {
            log.error("删除脱敏规则失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public DesensitizationRule getById(Long id) {
        try {
            return ruleMapper.selectById(id);
        } catch (Exception e) {
            log.error("查询脱敏规则失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public DesensitizationRule getByRuleCode(String ruleCode) {
        try {
            return ruleMapper.selectByRuleCode(ruleCode);
        } catch (Exception e) {
            log.error("根据规则编码查询失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public List<DesensitizationRule> getEnabledRules() {
        try {
            return ruleMapper.selectEnabledRules();
        } catch (Exception e) {
            log.error("查询启用规则失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public List<DesensitizationRule> getRulesByDataType(String dataType) {
        try {
            return ruleMapper.selectByDataType(dataType);
        } catch (Exception e) {
            log.error("根据数据类型查询规则失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public List<DesensitizationRule> getRulesBySensitivityLevel(String level) {
        try {
            return ruleMapper.selectBySensitivityLevel(level);
        } catch (Exception e) {
            log.error("根据敏感级别查询规则失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public List<DesensitizationRule> getRulesByApplyScope(String scope) {
        try {
            return ruleMapper.selectByApplyScope(scope);
        } catch (Exception e) {
            log.error("根据应用范围查询规则失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public boolean enableRule(Long id) {
        try {
            DesensitizationRule rule = new DesensitizationRule();
            rule.setId(id);
            rule.setStatus("ENABLED");
            return ruleMapper.updateById(rule) > 0;
        } catch (Exception e) {
            log.error("启用规则失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean disableRule(Long id) {
        try {
            DesensitizationRule rule = new DesensitizationRule();
            rule.setId(id);
            rule.setStatus("DISABLED");
            return ruleMapper.updateById(rule) > 0;
        } catch (Exception e) {
            log.error("禁用规则失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean updatePriority(Long id, Integer priority) {
        try {
            DesensitizationRule rule = new DesensitizationRule();
            rule.setId(id);
            rule.setPriority(priority);
            return ruleMapper.updateById(rule) > 0;
        } catch (Exception e) {
            log.error("更新规则优先级失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public Page<DesensitizationRule> pageRules(int current, int size, String dataType, String status) {
        try {
            Page<DesensitizationRule> page = new Page<>(current, size);
            LambdaQueryWrapper<DesensitizationRule> wrapper = new LambdaQueryWrapper<>();
            
            if (dataType != null && !dataType.isEmpty()) {
                wrapper.eq(DesensitizationRule::getDataType, dataType);
            }
            if (status != null && !status.isEmpty()) {
                wrapper.eq(DesensitizationRule::getStatus, status);
            }
            
            wrapper.orderByAsc(DesensitizationRule::getPriority);
            
            return ruleMapper.selectPage(page, wrapper);
        } catch (Exception e) {
            log.error("分页查询规则失败: {}", e.getMessage(), e);
            return new Page<>();
        }
    }
    
    @Override
    public String testRule(Long id, String testValue) {
        try {
            DesensitizationRule rule = ruleMapper.selectById(id);
            if (rule == null) {
                return "规则不存在";
            }
            
            return DesensitizationUtil.desensitize(testValue, rule.getAlgorithmType(), rule.getAlgorithmConfig());
        } catch (Exception e) {
            log.error("测试规则失败: {}", e.getMessage(), e);
            return "测试失败: " + e.getMessage();
        }
    }
    
    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<DesensitizationRule> pageRules(
            Page<DesensitizationRule> page, String ruleName, String dataType, String status) {
        try {
            LambdaQueryWrapper<DesensitizationRule> wrapper = new LambdaQueryWrapper<>();
            
            if (ruleName != null && !ruleName.isEmpty()) {
                wrapper.like(DesensitizationRule::getRuleName, ruleName);
            }
            if (dataType != null && !dataType.isEmpty()) {
                wrapper.eq(DesensitizationRule::getDataType, dataType);
            }
            if (status != null && !status.isEmpty()) {
                wrapper.eq(DesensitizationRule::getStatus, status);
            }
            
            wrapper.orderByAsc(DesensitizationRule::getPriority);
            
            return ruleMapper.selectPage(page, wrapper);
        } catch (Exception e) {
            log.error("分页查询规则失败: {}", e.getMessage(), e);
            return new Page<>();
        }
    }
    
    @Override
    public boolean updateRuleStatus(Long id, String status) {
        try {
            DesensitizationRule rule = new DesensitizationRule();
            rule.setId(id);
            rule.setStatus(status);
            return ruleMapper.updateById(rule) > 0;
        } catch (Exception e) {
            log.error("更新规则状态失败: {}", e.getMessage(), e);
            return false;
        }
    }
}
