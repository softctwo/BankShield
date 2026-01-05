package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.AccessRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 访问规则Mapper
 */
@Mapper
public interface AccessRuleMapper extends BaseMapper<AccessRule> {
    
    /**
     * 根据规则编码查询
     */
    @Select("SELECT * FROM access_rule WHERE rule_code = #{ruleCode}")
    AccessRule selectByRuleCode(String ruleCode);
    
    /**
     * 根据策略ID查询规则
     */
    @Select("SELECT * FROM access_rule WHERE policy_id = #{policyId} AND status = 'ENABLED' ORDER BY priority DESC")
    List<AccessRule> selectByPolicyId(Long policyId);
    
    /**
     * 根据规则类型查询
     */
    @Select("SELECT * FROM access_rule WHERE rule_type = #{ruleType} AND status = 'ENABLED' ORDER BY priority DESC")
    List<AccessRule> selectByRuleType(String ruleType);
    
    /**
     * 查询需要MFA的规则
     */
    @Select("SELECT * FROM access_rule WHERE mfa_required = 1 AND status = 'ENABLED' ORDER BY priority DESC")
    List<AccessRule> selectMfaRequiredRules();
}
