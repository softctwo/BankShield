package com.bankshield.api.mapper;

import com.bankshield.api.entity.ClassificationHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分类分级历史Mapper接口
 * 
 * @author BankShield
 */
@Mapper
public interface ClassificationHistoryMapper extends BaseMapper<ClassificationHistory> {

    /**
     * 查询指定资产的分级变更历史
     * 
     * @param assetId 资产ID
     * @return 分级变更历史列表
     */
    List<ClassificationHistory> selectByAssetId(@Param("assetId") Long assetId);

    /**
     * 查询最近的分级变更记录
     * 
     * @param limit 限制数量
     * @return 最近的分级变更记录
     */
    List<ClassificationHistory> selectRecentHistory(@Param("limit") Integer limit);
}