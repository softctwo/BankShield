package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.AlertRecord;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 告警记录Mapper接口
 */
@Repository
public interface AlertRecordMapper extends BaseMapper<AlertRecord> {

    /**
     * 分页查询告警记录
     */
    IPage<AlertRecord> selectPage(Page<AlertRecord> page,
                                 @Param("alertLevel") String alertLevel,
                                 @Param("alertStatus") String alertStatus,
                                 @Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime,
                                 @Param("keyword") String keyword);

    /**
     * 查询未处理告警
     */
    List<AlertRecord> selectUnresolvedAlerts(@Param("limit") Integer limit);

    /**
     * 统计告警数量
     */
    Map<String, Object> countAlerts(@Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 统计各类告警数量
     */
    List<Map<String, Object>> countAlertsByLevel(@Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

    /**
     * 统计告警趋势
     */
    List<Map<String, Object>> getAlertTrend(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime,
                                           @Param("interval") String interval);

    /**
     * 批量更新告警状态
     */
    int batchUpdateStatus(@Param("ids") List<Long> ids, 
                         @Param("status") String status,
                         @Param("handler") String handler,
                         @Param("handleRemark") String handleRemark);

    /**
     * 清理过期数据
     */
    int cleanExpiredData(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 查询最近的告警记录
     *
     * @param limit 限制数量
     * @return 最近告警记录列表
     */
    List<AlertRecord> getRecentAlerts(@Param("limit") int limit);

    /**
     * 获取未处理告警数量
     *
     * @return 未处理告警数量
     */
    @org.apache.ibatis.annotations.Select("SELECT COUNT(*) FROM alert_record WHERE alert_status = 'UNPROCESSED'")
    int countUnprocessedAlerts();

    /**
     * 按告警级别统计
     *
     * @param startTime 开始时间
     * @return 告警级别统计列表
     */
    @org.apache.ibatis.annotations.Select("SELECT alert_level, COUNT(*) as count FROM alert_record WHERE create_time >= #{startTime} GROUP BY alert_level")
    List<Map<String, Object>> countByAlertLevel(@Param("startTime") LocalDateTime startTime);

    /**
     * 获取小时级告警趋势
     *
     * @param hours 小时数
     * @return 告警趋势列表
     */
    @org.apache.ibatis.annotations.Select("SELECT DATE_FORMAT(create_time, '%H:00') as time_label, COUNT(*) as count " +
            "FROM alert_record WHERE create_time >= DATE_SUB(NOW(), INTERVAL #{hours} HOUR) " +
            "GROUP BY DATE_FORMAT(create_time, '%H:00') ORDER BY time_label")
    List<Map<String, Object>> getHourlyTrend(@Param("hours") int hours);
}