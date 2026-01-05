package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.DesensitizationRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 脱敏规则Mapper接口
 */
@Mapper
public interface DesensitizationRuleMapper extends BaseMapper<DesensitizationRule> {
    
    /**
     * 查询启用的规则
     */
    @Select("SELECT * FROM desensitization_rule WHERE status = 'ENABLED' ORDER BY priority ASC")
    List<DesensitizationRule> selectEnabledRules();
    
    /**
     * 根据数据类型查询规则
     */
    @Select("SELECT * FROM desensitization_rule WHERE data_type = #{dataType} AND status = 'ENABLED' ORDER BY priority ASC")
    List<DesensitizationRule> selectByDataType(@Param("dataType") String dataType);
    
    /**
     * 根据规则编码查询
     */
    @Select("SELECT * FROM desensitization_rule WHERE rule_code = #{ruleCode}")
    DesensitizationRule selectByRuleCode(@Param("ruleCode") String ruleCode);
    
    /**
     * 根据敏感级别查询规则
     */
    @Select("SELECT * FROM desensitization_rule WHERE FIND_IN_SET(#{level}, sensitivity_level) > 0 AND status = 'ENABLED' ORDER BY priority ASC")
    List<DesensitizationRule> selectBySensitivityLevel(@Param("level") String level);
    
    /**
     * 根据应用范围查询规则
     */
    @Select("SELECT * FROM desensitization_rule WHERE (apply_scope = #{scope} OR apply_scope = 'ALL') AND status = 'ENABLED' ORDER BY priority ASC")
    List<DesensitizationRule> selectByApplyScope(@Param("scope") String scope);
}
