package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.DesensitizationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 脱敏日志Mapper接口
 */
@Mapper
public interface DesensitizationLogMapper extends BaseMapper<DesensitizationLog> {
    
    /**
     * 根据用户ID查询日志
     */
    @Select("SELECT * FROM desensitization_log WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<DesensitizationLog> selectByUserId(@Param("userId") String userId, @Param("limit") int limit);
    
    /**
     * 根据目标表查询日志
     */
    @Select("SELECT * FROM desensitization_log WHERE target_table = #{targetTable} ORDER BY create_time DESC LIMIT #{limit}")
    List<DesensitizationLog> selectByTargetTable(@Param("targetTable") String targetTable, @Param("limit") int limit);
    
    /**
     * 统计用户操作次数
     */
    @Select("SELECT user_id, user_name, COUNT(*) as count FROM desensitization_log WHERE create_time >= #{startTime} GROUP BY user_id, user_name ORDER BY count DESC")
    List<Map<String, Object>> countByUser(@Param("startTime") LocalDateTime startTime);
    
    /**
     * 统计按日期分组
     */
    @Select("SELECT DATE(create_time) as date, COUNT(*) as count, SUM(record_count) as total_records FROM desensitization_log WHERE create_time >= #{startTime} GROUP BY DATE(create_time) ORDER BY date DESC")
    List<Map<String, Object>> countByDate(@Param("startTime") LocalDateTime startTime);
    
    /**
     * 统计成功和失败次数
     */
    @Select("SELECT status, COUNT(*) as count FROM desensitization_log WHERE create_time >= #{startTime} GROUP BY status")
    List<Map<String, Object>> countByStatus(@Param("startTime") LocalDateTime startTime);
}
