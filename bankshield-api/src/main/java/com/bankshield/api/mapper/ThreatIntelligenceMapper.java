package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.ThreatIntelligence;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 威胁情报Mapper接口
 */
@Repository
public interface ThreatIntelligenceMapper extends BaseMapper<ThreatIntelligence> {

    /**
     * 分页查询威胁情报
     */
    IPage<ThreatIntelligence> selectThreatIntelligencePage(Page<ThreatIntelligence> page,
                                                           @Param("threatType") String threatType,
                                                           @Param("threatLevel") String threatLevel,
                                                           @Param("status") String status,
                                                           @Param("startTime") LocalDateTime startTime,
                                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 获取活跃的威胁情报
     */
    @Select("SELECT * FROM threat_intelligence WHERE status = 'ACTIVE' ORDER BY discover_time DESC LIMIT #{limit}")
    List<ThreatIntelligence> selectActiveThreats(@Param("limit") int limit);

    /**
     * 按威胁类型统计
     */
    @Select("SELECT threat_type, COUNT(*) as count FROM threat_intelligence WHERE status = 'ACTIVE' GROUP BY threat_type")
    List<Map<String, Object>> countByThreatType();

    /**
     * 按威胁等级统计
     */
    @Select("SELECT threat_level, COUNT(*) as count FROM threat_intelligence WHERE status = 'ACTIVE' GROUP BY threat_level")
    List<Map<String, Object>> countByThreatLevel();

    /**
     * 获取今日新增威胁
     */
    @Select("SELECT COUNT(*) FROM threat_intelligence WHERE DATE(discover_time) = CURDATE()")
    int countTodayThreats();

    /**
     * 获取最近7天的威胁趋势
     */
    @Select("SELECT DATE(discover_time) as date, COUNT(*) as count FROM threat_intelligence " +
            "WHERE discover_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY DATE(discover_time) ORDER BY date")
    List<Map<String, Object>> get7DayTrend();

    /**
     * 根据IoC指标查询
     */
    @Select("SELECT * FROM threat_intelligence WHERE ioc_indicators LIKE CONCAT('%', #{ioc}, '%') AND status = 'ACTIVE'")
    List<ThreatIntelligence> selectByIoc(@Param("ioc") String ioc);

    /**
     * 获取地理位置分布
     */
    @Select("SELECT geo_location, COUNT(*) as count FROM threat_intelligence WHERE geo_location IS NOT NULL AND geo_location != '' " +
            "AND status = 'ACTIVE' GROUP BY geo_location ORDER BY count DESC LIMIT 10")
    List<Map<String, Object>> getGeoDistribution();

    /**
     * 获取目标行业分布
     */
    @Select("SELECT target_industry, COUNT(*) as count FROM threat_intelligence WHERE target_industry IS NOT NULL AND target_industry != '' " +
            "AND status = 'ACTIVE' GROUP BY target_industry ORDER BY count DESC")
    List<Map<String, Object>> getIndustryDistribution();

    /**
     * 获取高可信度威胁
     */
    @Select("SELECT * FROM threat_intelligence WHERE confidence_level = 'HIGH' AND status = 'ACTIVE' ORDER BY discover_time DESC LIMIT #{limit}")
    List<ThreatIntelligence> selectHighConfidenceThreats(@Param("limit") int limit);
}