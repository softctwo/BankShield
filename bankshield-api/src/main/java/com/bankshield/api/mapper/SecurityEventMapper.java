package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.SecurityEvent;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 安全事件Mapper接口
 */
@Repository
public interface SecurityEventMapper extends BaseMapper<SecurityEvent> {

    /**
     * 分页查询安全事件
     */
    IPage<SecurityEvent> selectSecurityEventPage(Page<SecurityEvent> page, 
                                                 @Param("eventType") String eventType,
                                                 @Param("riskLevel") String riskLevel,
                                                 @Param("processStatus") String processStatus,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 获取最近的安全事件
     */
    @Select("SELECT * FROM security_event ORDER BY occur_time DESC LIMIT #{limit}")
    List<SecurityEvent> selectRecentEvents(@Param("limit") int limit);

    /**
     * 按风险级别统计事件数量
     */
    @Select("SELECT risk_level, COUNT(*) as count FROM security_event WHERE occur_time >= #{startTime} GROUP BY risk_level")
    List<Map<String, Object>> countByRiskLevel(@Param("startTime") LocalDateTime startTime);

    /**
     * 按事件类型统计事件数量
     */
    @Select("SELECT event_type, COUNT(*) as count FROM security_event WHERE occur_time >= #{startTime} GROUP BY event_type")
    List<Map<String, Object>> countByEventType(@Param("startTime") LocalDateTime startTime);

    /**
     * 获取未处理事件数量
     */
    @Select("SELECT COUNT(*) FROM security_event WHERE process_status = 'UNPROCESSED'")
    int countUnprocessedEvents();

    /**
     * 获取今日事件数量
     */
    @Select("SELECT COUNT(*) FROM security_event WHERE DATE(occur_time) = CURDATE()")
    int countTodayEvents();

    /**
     * 获取24小时事件趋势
     */
    @Select("SELECT DATE_FORMAT(occur_time, '%H:00') as time_label, COUNT(*) as count " +
            "FROM security_event WHERE occur_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR) " +
            "GROUP BY DATE_FORMAT(occur_time, '%H:00') ORDER BY time_label")
    List<Map<String, Object>> get24HourTrend();

    /**
     * 根据IP地址查询事件
     */
    @Select("SELECT * FROM security_event WHERE ip_address = #{ipAddress} ORDER BY occur_time DESC")
    List<SecurityEvent> selectByIpAddress(@Param("ipAddress") String ipAddress);

    /**
     * 获取地理位置分布
     */
    @Select("SELECT location, COUNT(*) as count FROM security_event WHERE location IS NOT NULL AND location != '' " +
            "AND occur_time >= #{startTime} GROUP BY location ORDER BY count DESC LIMIT 10")
    List<Map<String, Object>> getLocationDistribution(@Param("startTime") LocalDateTime startTime);
}