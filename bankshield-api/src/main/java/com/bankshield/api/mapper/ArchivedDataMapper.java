package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.ArchivedData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 归档数据Mapper接口
 */
@Mapper
public interface ArchivedDataMapper extends BaseMapper<ArchivedData> {
    
    /**
     * 根据原始表和ID查询归档数据
     */
    @Select("SELECT * FROM archived_data WHERE original_table = #{tableName} AND original_id = #{originalId}")
    ArchivedData selectByOriginal(@Param("tableName") String tableName, @Param("originalId") Long originalId);
    
    /**
     * 查询待销毁的归档数据
     */
    @Select("SELECT * FROM archived_data WHERE is_destroyed = 0 AND retention_until <= #{now}")
    List<ArchivedData> selectPendingDestruction(@Param("now") LocalDateTime now);
    
    /**
     * 统计归档数据
     */
    @Select("SELECT COUNT(*) FROM archived_data WHERE is_destroyed = 0")
    Long countActive();
    
    /**
     * 统计已销毁数据
     */
    @Select("SELECT COUNT(*) FROM archived_data WHERE is_destroyed = 1")
    Long countDestroyed();
    
    /**
     * 根据策略ID统计归档数据
     */
    @Select("SELECT COUNT(*) FROM archived_data WHERE policy_id = #{policyId} AND is_destroyed = 0")
    Long countByPolicy(@Param("policyId") Long policyId);
}
