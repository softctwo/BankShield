package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.DestructionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 销毁记录Mapper接口
 */
@Mapper
public interface DestructionRecordMapper extends BaseMapper<DestructionRecord> {
    
    /**
     * 根据资产ID查询销毁记录
     */
    @Select("SELECT * FROM destruction_record WHERE asset_id = #{assetId} ORDER BY destruction_time DESC")
    List<DestructionRecord> selectByAssetId(@Param("assetId") Long assetId);
    
    /**
     * 查询待审批的销毁记录
     */
    @Select("SELECT * FROM destruction_record WHERE approval_status = 'PENDING' ORDER BY create_time ASC")
    List<DestructionRecord> selectPendingApproval();
    
    /**
     * 统计今日销毁数
     */
    @Select("SELECT COUNT(*) FROM destruction_record WHERE DATE(destruction_time) = CURDATE()")
    Long countToday();
    
    /**
     * 统计总销毁数
     */
    @Select("SELECT COUNT(*) FROM destruction_record")
    Long countTotal();
    
    /**
     * 根据策略ID统计销毁数
     */
    @Select("SELECT COUNT(*) FROM destruction_record WHERE policy_id = #{policyId}")
    Long countByPolicy(@Param("policyId") Long policyId);
}
