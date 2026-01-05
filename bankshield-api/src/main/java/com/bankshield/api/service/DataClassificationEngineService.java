package com.bankshield.api.service;

import com.bankshield.api.entity.DataAsset;
import java.util.List;
import java.util.Map;

/**
 * 数据分级引擎服务接口
 * 基于规则的自动分级引擎
 */
public interface DataClassificationEngineService {
    
    /**
     * 对单个数据资产进行自动分级
     * @param asset 数据资产
     * @return 分级结果（敏感级别）
     */
    String classifyAsset(DataAsset asset);
    
    /**
     * 批量自动分级
     * @param assets 数据资产列表
     * @return 分级结果映射 (assetId -> sensitivityLevel)
     */
    Map<Long, String> batchClassify(List<DataAsset> assets);
    
    /**
     * 对所有未分级的资产进行自动分级
     * @return 分级数量
     */
    int classifyAllUnclassified();
    
    /**
     * 根据字段名和内容匹配规则
     * @param fieldName 字段名
     * @param content 字段内容
     * @return 匹配的敏感级别，如果没有匹配返回null
     */
    String matchRules(String fieldName, String content);
    
    /**
     * 验证分级结果
     * @param assetId 资产ID
     * @param suggestedLevel 建议级别
     * @return 是否需要人工审核
     */
    boolean needManualReview(Long assetId, String suggestedLevel);
}
