package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.LifecyclePolicy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 生命周期策略Mapper接口
 */
@Mapper
public interface LifecyclePolicyMapper extends BaseMapper<LifecyclePolicy> {
    
    /**
     * 查询所有活跃策略
     */
    @Select("SELECT * FROM lifecycle_policy WHERE policy_status = 'ACTIVE' ORDER BY priority DESC, id ASC")
    List<LifecyclePolicy> selectActivePolicies();
    
    /**
     * 根据数据类型和敏感级别查询策略
     */
    @Select("SELECT * FROM lifecycle_policy WHERE policy_status = 'ACTIVE' " +
            "AND (data_type = #{dataType} OR data_type IS NULL) " +
            "AND (sensitivity_level = #{sensitivityLevel} OR sensitivity_level IS NULL) " +
            "ORDER BY priority DESC LIMIT 1")
    LifecyclePolicy selectByDataTypeAndLevel(@Param("dataType") String dataType, 
                                              @Param("sensitivityLevel") String sensitivityLevel);
    
    /**
     * 查询需要归档的策略
     */
    @Select("SELECT * FROM lifecycle_policy WHERE policy_status = 'ACTIVE' AND archive_enabled = 1")
    List<LifecyclePolicy> selectArchivePolicies();
    
    /**
     * 查询需要销毁的策略
     */
    @Select("SELECT * FROM lifecycle_policy WHERE policy_status = 'ACTIVE' AND destroy_enabled = 1")
    List<LifecyclePolicy> selectDestroyPolicies();
}
