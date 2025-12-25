package com.bankshield.api.mapper;

import com.bankshield.api.entity.DataLineageAutoDiscovery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 数据血缘自动发现任务Mapper接口
 */
@Mapper
public interface DataLineageAutoDiscoveryMapper extends BaseMapper<DataLineageAutoDiscovery> {
    
    /**
     * 根据数据源ID查询发现任务
     */
    List<DataLineageAutoDiscovery> selectByDataSourceId(@Param("dataSourceId") Long dataSourceId);
    
    /**
     * 根据状态查询发现任务
     */
    List<DataLineageAutoDiscovery> selectByStatus(@Param("status") String status);
    
    /**
     * 根据发现策略查询发现任务
     */
    List<DataLineageAutoDiscovery> selectByDiscoveryStrategy(@Param("discoveryStrategy") String discoveryStrategy);
    
    /**
     * 查询最近的任务
     */
    List<DataLineageAutoDiscovery> selectRecentTasks(@Param("limit") Integer limit);
    
    /**
     * 统计各状态的任务数量
     */
    List<Map<String, Object>> countByStatus();
    
    /**
     * 统计各发现策略的任务数量
     */
    List<Map<String, Object>> countByDiscoveryStrategy();
}