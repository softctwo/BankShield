package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.DataClassificationHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 数据分级历史Mapper
 */
@Mapper
public interface DataClassificationHistoryMapper extends BaseMapper<DataClassificationHistory> {
    
    /**
     * 根据资产ID查询分级历史
     */
    @Select("SELECT * FROM data_classification_history WHERE asset_id = #{assetId} ORDER BY classify_time DESC")
    List<DataClassificationHistory> selectByAssetId(Long assetId);
    
    /**
     * 查询最近的分级历史
     */
    @Select("SELECT * FROM data_classification_history ORDER BY classify_time DESC LIMIT #{limit}")
    List<DataClassificationHistory> selectRecentHistory(int limit);
}
