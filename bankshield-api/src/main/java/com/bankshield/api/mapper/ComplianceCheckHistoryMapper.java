package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.ComplianceCheckHistory;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 合规检查历史Mapper接口
 */
public interface ComplianceCheckHistoryMapper extends BaseMapper<ComplianceCheckHistory> {
    
    /**
     * 分页查询合规检查历史
     */
    IPage<ComplianceCheckHistory> selectHistoryPage(Page<ComplianceCheckHistory> page,
                                                     @Param("complianceStandard") String complianceStandard,
                                                     @Param("checker") String checker,
                                                     @Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询最近7次检查评分趋势
     */
    List<Map<String, Object>> selectRecent7Scores(@Param("complianceStandard") String complianceStandard);
    
    /**
     * 统计合规评分分布
     */
    List<Map<String, Object>> selectScoreDistribution(@Param("complianceStandard") String complianceStandard);
    
    /**
     * 查询指定标准的最新检查历史
     */
    ComplianceCheckHistory selectLatestByStandard(@Param("complianceStandard") String complianceStandard);
    
    /**
     * 统计指定时间范围内的检查次数
     */
    int countByTimeRange(@Param("startTime") LocalDateTime startTime,
                         @Param("endTime") LocalDateTime endTime,
                         @Param("complianceStandard") String complianceStandard);
}