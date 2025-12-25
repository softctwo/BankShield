package com.bankshield.encrypt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.encrypt.entity.KeyUsageAudit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 密钥使用审计Mapper
 */
@Mapper
public interface KeyUsageAuditMapper extends BaseMapper<KeyUsageAudit> {
    
    /**
     * 分页查询使用审计
     */
    IPage<KeyUsageAudit> selectUsageAuditPage(Page<KeyUsageAudit> page,
                                            @Param("keyId") Long keyId,
                                            @Param("operationType") String operationType,
                                            @Param("operator") String operator,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询密钥的使用统计
     */
    @Select("SELECT operation_type, COUNT(*) as count, SUM(data_size) as total_size " +
            "FROM key_usage_audit " +
            "WHERE key_id = #{keyId} AND operation_time >= #{startTime} AND operation_time <= #{endTime} " +
            "GROUP BY operation_type")
    List<Map<String, Object>> selectUsageStatistics(@Param("keyId") Long keyId,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询每日使用量
     */
    @Select("SELECT DATE(operation_time) as date, COUNT(*) as count, SUM(data_size) as total_size " +
            "FROM key_usage_audit " +
            "WHERE key_id = #{keyId} AND operation_time >= #{startTime} AND operation_time <= #{endTime} " +
            "GROUP BY DATE(operation_time) " +
            "ORDER BY date")
    List<Map<String, Object>> selectDailyUsage(@Param("keyId") Long keyId,
                                             @Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询最活跃的操作员
     */
    @Select("SELECT operator, COUNT(*) as count " +
            "FROM key_usage_audit " +
            "WHERE key_id = #{keyId} AND operation_time >= #{startTime} AND operation_time <= #{endTime} " +
            "GROUP BY operator " +
            "ORDER BY count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectTopOperators(@Param("keyId") Long keyId,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime,
                                               @Param("limit") int limit);
}