package com.bankshield.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bankshield.api.entity.DataClassificationRule;
import com.bankshield.api.mapper.DataClassificationRuleMapper;
import com.bankshield.api.service.DataClassificationRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据分级规则服务实现
 */
@Slf4j
@Service
public class DataClassificationRuleServiceImpl extends ServiceImpl<DataClassificationRuleMapper, DataClassificationRule> 
        implements DataClassificationRuleService {
    
    @Override
    public List<DataClassificationRule> getActiveRules() {
        return baseMapper.selectActiveRulesOrderByPriority();
    }
    
    @Override
    public List<DataClassificationRule> getRulesByDataType(String dataType) {
        return baseMapper.selectRulesByDataType(dataType);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createRule(DataClassificationRule rule) {
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());
        if (rule.getRuleStatus() == null) {
            rule.setRuleStatus("ACTIVE");
        }
        if (rule.getPriority() == null) {
            rule.setPriority(0);
        }
        return save(rule);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRule(DataClassificationRule rule) {
        rule.setUpdateTime(LocalDateTime.now());
        return updateById(rule);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleRuleStatus(Long ruleId, String status) {
        DataClassificationRule rule = getById(ruleId);
        if (rule == null) {
            log.error("规则不存在: {}", ruleId);
            return false;
        }
        rule.setRuleStatus(status);
        rule.setUpdateTime(LocalDateTime.now());
        return updateById(rule);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adjustPriority(Long ruleId, Integer newPriority) {
        DataClassificationRule rule = getById(ruleId);
        if (rule == null) {
            log.error("规则不存在: {}", ruleId);
            return false;
        }
        rule.setPriority(newPriority);
        rule.setUpdateTime(LocalDateTime.now());
        return updateById(rule);
    }
}
