package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.ComplianceRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 合规规则Mapper
 */
@Mapper
public interface ComplianceRuleMapper extends BaseMapper<ComplianceRule> {
    
    /**
     * 获取规则分类统计
     */
    @Select("SELECT category, COUNT(*) as count FROM compliance_rule WHERE status = 'ACTIVE' GROUP BY category")
    List<Map<String, Object>> getRuleCategoryStats();
    
    /**
     * 获取规则标准统计
     */
    @Select("SELECT standard, COUNT(*) as count FROM compliance_rule WHERE status = 'ACTIVE' GROUP BY standard")
    List<Map<String, Object>> getRuleStandardStats();
}
