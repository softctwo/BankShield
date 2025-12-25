package com.bankshield.api.mapper;

import com.bankshield.api.entity.DataAsset;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 数据资产Mapper接口
 * 
 * @author BankShield
 */
@Mapper
public interface DataAssetMapper extends BaseMapper<DataAsset> {

    /**
     * 按安全等级统计资产数量
     * 
     * @return 安全等级统计结果
     */
    List<Map<String, Object>> countBySecurityLevel();

    /**
     * 按业务条线统计资产数量
     * 
     * @return 业务条线统计结果
     */
    List<Map<String, Object>> countByBusinessLine();

    /**
     * 按资产类型统计资产数量
     * 
     * @return 资产类型统计结果
     */
    List<Map<String, Object>> countByAssetType();

    /**
     * 查询待审核的资产
     *
     * @return 待审核资产列表
     */
    List<DataAsset> selectPendingReviewAssets();

    /**
     * 查询常见资产
     *
     * @param limit 限制数量
     * @return 常见资产列表
     */
    List<DataAsset> selectCommonAssets(@Param("limit") int limit);

    /**
     * 查询指定数据源下的资产
     * 
     * @param dataSourceId 数据源ID
     * @return 资产列表
     */
    List<DataAsset> selectByDataSourceId(@Param("dataSourceId") Long dataSourceId);

    /**
     * 统计风险资产数量（C3、C4级）
     * 
     * @return 风险资产统计
     */
    Map<String, Object> countRiskAssets();

    /**
     * 更新资产状态
     * 
     * @param assetId 资产ID
     * @param status 状态
     * @param reviewerId 审核人ID
     * @param reviewComment 审核意见
     * @return 更新结果
     */
    int updateAssetStatus(@Param("assetId") Long assetId, 
                         @Param("status") Integer status,
                         @Param("reviewerId") Long reviewerId,
                         @Param("reviewComment") String reviewComment);
}