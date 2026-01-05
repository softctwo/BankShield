package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.AccessLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 访问日志Mapper
 */
@Mapper
public interface AccessLogMapper extends BaseMapper<AccessLog> {
    
    /**
     * 根据用户ID查询访问日志
     */
    @Select("SELECT * FROM access_log WHERE user_id = #{userId} ORDER BY access_time DESC LIMIT #{limit}")
    List<AccessLog> selectByUserId(Long userId, Integer limit);
    
    /**
     * 根据IP地址查询访问日志
     */
    @Select("SELECT * FROM access_log WHERE ip_address = #{ipAddress} ORDER BY access_time DESC LIMIT #{limit}")
    List<AccessLog> selectByIpAddress(String ipAddress, Integer limit);
    
    /**
     * 查询拒绝访问的日志
     */
    @Select("SELECT * FROM access_log WHERE access_result = 'DENY' AND access_time >= #{startTime} ORDER BY access_time DESC")
    List<AccessLog> selectDeniedLogs(LocalDateTime startTime);
    
    /**
     * 统计访问次数
     */
    @Select("SELECT COUNT(*) as count, access_result FROM access_log WHERE access_time >= #{startTime} GROUP BY access_result")
    List<Map<String, Object>> countAccessByResult(LocalDateTime startTime);
    
    /**
     * 统计用户访问次数
     */
    @Select("SELECT username, COUNT(*) as count FROM access_log WHERE access_time >= #{startTime} GROUP BY username ORDER BY count DESC LIMIT #{limit}")
    List<Map<String, Object>> countAccessByUser(LocalDateTime startTime, Integer limit);
}
