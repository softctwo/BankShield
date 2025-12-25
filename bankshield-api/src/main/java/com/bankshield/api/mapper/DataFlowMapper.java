package com.bankshield.api.mapper;

import com.bankshield.api.entity.DataFlow;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 数据流关系Mapper接口
 */
@Mapper
public interface DataFlowMapper extends BaseMapper<DataFlow> {
    
    /**
     * 根据源表名查询数据流
     */
    List<DataFlow> selectBySourceTable(@Param("sourceTable") String sourceTable);
    
    /**
     * 根据目标表名查询数据流
     */
    List<DataFlow> selectByTargetTable(@Param("targetTable") String targetTable);
    
    /**
     * 根据数据源ID查询数据流
     */
    List<DataFlow> selectByDataSourceId(@Param("dataSourceId") Long dataSourceId);
    
    /**
     * 根据流转类型查询数据流
     */
    List<DataFlow> selectByFlowType(@Param("flowType") String flowType);
    
    /**
     * 根据发现方法查询数据流
     */
    List<DataFlow> selectByDiscoveryMethod(@Param("discoveryMethod") String discoveryMethod);
    
    /**
     * 查询置信度大于指定值的数据流
     */
    List<DataFlow> selectByMinConfidence(@Param("minConfidence") Double minConfidence);
    
    /**
     * 统计各流转类型的数据流数量
     */
    List<Map<String, Object>> countByFlowType();
    
    /**
     * 统计各发现方法的数据流数量
     */
    List<Map<String, Object>> countByDiscoveryMethod();
    
    /**
     * 查询热门数据表（按血缘关系数量排序）
     */
    List<Map<String, Object>> getHotDataTables(@Param("limit") Integer limit);
}