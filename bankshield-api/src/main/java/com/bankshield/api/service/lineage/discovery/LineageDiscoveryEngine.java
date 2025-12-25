package com.bankshield.api.service.lineage.discovery;

import com.bankshield.api.entity.DataFlow;
import com.bankshield.api.entity.DataSource;

import java.util.List;
import java.util.Map;

/**
 * 血缘发现引擎接口
 * 定义各种血缘发现策略的统一接口
 */
public interface LineageDiscoveryEngine {
    
    /**
     * 获取发现策略名称
     */
    String getStrategy();
    
    /**
     * 发现数据血缘关系
     * 
     * @param dataSource 数据源
     * @param config 发现配置
     * @return 发现的数据流转关系列表
     */
    List<DataFlow> discoverLineage(DataSource dataSource, Map<String, Object> config);
}