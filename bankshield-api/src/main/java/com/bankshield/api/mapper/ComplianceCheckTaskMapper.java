package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.ComplianceCheckTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 合规检查任务Mapper
 */
@Mapper
public interface ComplianceCheckTaskMapper extends BaseMapper<ComplianceCheckTask> {
    
    /**
     * 获取任务状态统计
     */
    @Select("SELECT status, COUNT(*) as count FROM compliance_check_task GROUP BY status")
    List<Map<String, Object>> getTaskStatusStats();
    
    /**
     * 获取合规趋势数据
     */
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m') as month, AVG(compliance_score) as score " +
            "FROM compliance_check_task WHERE status = 'COMPLETED' " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m') ORDER BY month DESC LIMIT 12")
    List<Map<String, Object>> getComplianceTrend();
}
