package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.AccessPolicy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 访问策略Mapper
 */
@Mapper
public interface AccessPolicyMapper extends BaseMapper<AccessPolicy> {
    
    /**
     * 根据策略编码查询
     */
    @Select("SELECT * FROM access_policy WHERE policy_code = #{policyCode}")
    AccessPolicy selectByPolicyCode(String policyCode);
    
    /**
     * 查询所有启用的策略
     */
    @Select("SELECT * FROM access_policy WHERE status = 'ENABLED' ORDER BY priority DESC")
    List<AccessPolicy> selectEnabledPolicies();
    
    /**
     * 根据策略类型查询
     */
    @Select("SELECT * FROM access_policy WHERE policy_type = #{policyType} AND status = 'ENABLED' ORDER BY priority DESC")
    List<AccessPolicy> selectByPolicyType(String policyType);
}
