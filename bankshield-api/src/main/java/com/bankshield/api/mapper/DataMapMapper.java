package com.bankshield.api.mapper;

import com.bankshield.api.entity.DataMap;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 数据地图Mapper接口
 */
@Mapper
public interface DataMapMapper extends BaseMapper<DataMap> {
    
    /**
     * 根据地图类型查询数据地图
     */
    List<DataMap> selectByMapType(@Param("mapType") String mapType);
    
    /**
     * 根据状态查询数据地图
     */
    List<DataMap> selectByStatus(@Param("status") String status);
    
    /**
     * 根据创建人查询数据地图
     */
    List<DataMap> selectByCreateBy(@Param("createBy") Long createBy);
    
    /**
     * 查询默认数据地图
     */
    DataMap selectDefaultDataMap();
    
    /**
     * 查询活跃的数据地图
     */
    List<DataMap> selectActiveDataMaps();
    
    /**
     * 统计各地图类型的地图数量
     */
    List<Map<String, Object>> countByMapType();
    
    /**
     * 统计各状态的地图数量
     */
    List<Map<String, Object>> countByStatus();
    
    /**
     * 统计创建人的地图数量
     */
    List<Map<String, Object>> countByCreateBy();
}