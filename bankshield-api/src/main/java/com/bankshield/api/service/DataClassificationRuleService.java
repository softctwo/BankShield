package com.bankshield.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bankshield.api.entity.DataClassificationRule;
import java.util.List;

/**
 * 数据分级规则服务接口
 */
public interface DataClassificationRuleService extends IService<DataClassificationRule> {
    
    /**
     * 获取所有启用的规则，按优先级排序
     */
    List<DataClassificationRule> getActiveRules();
    
    /**
     * 根据数据类型获取规则
     */
    List<DataClassificationRule> getRulesByDataType(String dataType);
    
    /**
     * 创建规则
     */
    boolean createRule(DataClassificationRule rule);
    
    /**
     * 更新规则
     */
    boolean updateRule(DataClassificationRule rule);
    
    /**
     * 启用/禁用规则
     */
    boolean toggleRuleStatus(Long ruleId, String status);
    
    /**
     * 调整规则优先级
     */
    boolean adjustPriority(Long ruleId, Integer newPriority);
}
