package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.MonitorMetric;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 监控指标Mapper接口
 */
@Repository
public interface MonitorMetricMapper extends BaseMapper<MonitorMetric> {

    /**
     * 分页查询监控指标
     */
    IPage<MonitorMetric> selectPage(Page<MonitorMetric> page, 
                                   @Param("metricType") String metricType,
                                   @Param("status") String status,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 根据指标类型和名称查询最新指标
     */
    MonitorMetric selectLatestMetric(@Param("metricType") String metricType, 
                                    @Param("metricName") String metricName);

    /**
     * 查询指定时间范围内的指标数据
     */
    List<MonitorMetric> selectMetricsByTimeRange(@Param("metricType") String metricType,
                                                @Param("metricName") String metricName,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

    /**
     * 批量插入监控指标
     */
    int batchInsert(@Param("metrics") List<MonitorMetric> metrics);

    /**
     * 清理过期数据
     */
    int cleanExpiredData(@Param("expireTime") LocalDateTime expireTime);
}