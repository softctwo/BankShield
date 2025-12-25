package com.bankshield.api.mapper;

import com.bankshield.api.entity.DataLineage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据血缘关系Mapper接口
 * 
 * @author BankShield
 */
@Mapper
public interface DataLineageMapper extends BaseMapper<DataLineage> {

    /**
     * 查询指定资产的上游血缘
     * 
     * @param targetAssetId 目标资产ID
     * @return 上游血缘关系列表
     */
    List<DataLineage> selectUpstreamLineage(@Param("targetAssetId") Long targetAssetId);

    /**
     * 查询指定资产的下游血缘
     * 
     * @param sourceAssetId 源资产ID
     * @return 下游血缘关系列表
     */
    List<DataLineage> selectDownstreamLineage(@Param("sourceAssetId") Long sourceAssetId);

    /**
     * 查询指定字段的血缘链路
     * 
     * @param assetId 资产ID
     * @param fieldName 字段名称
     * @return 血缘链路列表
     */
    List<DataLineage> selectFieldLineage(@Param("assetId") Long assetId, @Param("fieldName") String fieldName);

    /**
     * 批量插入血缘关系
     * 
     * @param lineages 血缘关系列表
     * @return 插入结果
     */
    int batchInsert(@Param("lineages") List<DataLineage> lineages);
}