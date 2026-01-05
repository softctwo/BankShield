package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.LifecycleExecution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 生命周期执行记录Mapper接口
 */
@Mapper
public interface LifecycleExecutionMapper extends BaseMapper<LifecycleExecution> {
    
    /**
     * 根据策略ID查询执行记录
     */
    @Select("SELECT * FROM lifecycle_execution WHERE policy_id = #{policyId} ORDER BY create_time DESC")
    List<LifecycleExecution> selectByPolicyId(@Param("policyId") Long policyId);
    
    /**
     * 根据资产ID查询执行记录
     */
    @Select("SELECT * FROM lifecycle_execution WHERE asset_id = #{assetId} ORDER BY create_time DESC")
    List<LifecycleExecution> selectByAssetId(@Param("assetId") Long assetId);
    
    /**
     * 查询最近的执行记录
     */
    @Select("SELECT * FROM lifecycle_execution ORDER BY create_time DESC LIMIT #{limit}")
    List<LifecycleExecution> selectRecent(@Param("limit") Integer limit);
    
    /**
     * 统计执行状态
     */
    @Select("SELECT execution_status, COUNT(*) as count FROM lifecycle_execution " +
            "WHERE DATE(create_time) = CURDATE() GROUP BY execution_status")
    List<java.util.Map<String, Object>> countByStatus();
}
