package com.bankshield.api.mapper;

import com.bankshield.api.entity.DataImpactAnalysis;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 影响分析结果Mapper接口
 */
@Mapper
public interface DataImpactAnalysisMapper extends BaseMapper<DataImpactAnalysis> {
    
    /**
     * 根据分析类型查询影响分析
     */
    List<DataImpactAnalysis> selectByAnalysisType(@Param("analysisType") String analysisType);
    
    /**
     * 根据影响对象类型查询影响分析
     */
    List<DataImpactAnalysis> selectByImpactObjectType(@Param("impactObjectType") String impactObjectType);
    
    /**
     * 根据风险等级查询影响分析
     */
    List<DataImpactAnalysis> selectByRiskLevel(@Param("riskLevel") String riskLevel);
    
    /**
     * 根据状态查询影响分析
     */
    List<DataImpactAnalysis> selectByStatus(@Param("status") String status);
    
    /**
     * 根据创建人查询影响分析
     */
    List<DataImpactAnalysis> selectByCreateBy(@Param("createBy") Long createBy);
    
    /**
     * 查询最近的分析任务
     */
    List<DataImpactAnalysis> selectRecentAnalyses(@Param("limit") Integer limit);
    
    /**
     * 统计各风险等级的分析数量
     */
    List<Map<String, Object>> countByRiskLevel();
    
    /**
     * 统计各状态的分析数量
     */
    List<Map<String, Object>> countByStatus();
    
    /**
     * 统计各分析类型的分析数量
     */
    List<Map<String, Object>> countByAnalysisType();
}