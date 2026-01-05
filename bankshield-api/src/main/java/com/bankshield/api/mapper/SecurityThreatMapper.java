package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.SecurityThreat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 安全威胁Mapper
 */
@Mapper
public interface SecurityThreatMapper extends BaseMapper<SecurityThreat> {
    
    /**
     * 获取威胁统计
     */
    @Select("SELECT severity, COUNT(*) as count FROM security_threat " +
            "WHERE DATE(detect_time) = CURDATE() GROUP BY severity")
    List<Map<String, Object>> getThreatStats();
    
    /**
     * 获取攻击类型分布
     */
    @Select("SELECT threat_type as name, COUNT(*) as value FROM security_threat " +
            "WHERE DATE(detect_time) = CURDATE() GROUP BY threat_type ORDER BY value DESC LIMIT 10")
    List<Map<String, Object>> getAttackTypeDistribution();
    
    /**
     * 获取24小时事件趋势
     */
    @Select("SELECT DATE_FORMAT(detect_time, '%H:00') as hour, COUNT(*) as count " +
            "FROM security_threat WHERE detect_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR) " +
            "GROUP BY DATE_FORMAT(detect_time, '%H:00') ORDER BY hour")
    List<Map<String, Object>> getEventTrend24h();
    
    /**
     * 获取地理位置分布
     */
    @Select("SELECT source_country as name, COUNT(*) as value FROM security_threat " +
            "WHERE DATE(detect_time) = CURDATE() AND source_country IS NOT NULL " +
            "GROUP BY source_country ORDER BY value DESC LIMIT 20")
    List<Map<String, Object>> getGeoDistribution();
    
    /**
     * 获取TOP10攻击源IP
     */
    @Select("SELECT source_ip as ip, source_country as country, COUNT(*) as count " +
            "FROM security_threat WHERE DATE(detect_time) = CURDATE() " +
            "GROUP BY source_ip, source_country ORDER BY count DESC LIMIT 10")
    List<Map<String, Object>> getTopAttackIPs();
    
    /**
     * 获取最近的安全事件
     */
    @Select("SELECT id, threat_type, source_ip, target_ip, status, severity, detect_time " +
            "FROM security_threat ORDER BY detect_time DESC LIMIT 20")
    List<Map<String, Object>> getRecentEvents();
}
