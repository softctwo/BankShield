package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.ComplianceCheckResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 合规检查结果Mapper
 */
@Mapper
public interface ComplianceCheckResultMapper extends BaseMapper<ComplianceCheckResult> {
    
    /**
     * 获取风险等级分布
     */
    @Select("SELECT risk_level, COUNT(*) as count FROM compliance_check_result " +
            "WHERE check_status = 'FAILED' GROUP BY risk_level")
    List<Map<String, Object>> getRiskLevelDistribution();
    
    /**
     * 获取整改进度统计
     */
    @Select("SELECT remediation_status, COUNT(*) as count FROM compliance_check_result " +
            "WHERE check_status = 'FAILED' GROUP BY remediation_status")
    List<Map<String, Object>> getRemediationProgress();
    
    /**
     * 获取高危风险项
     */
    @Select("SELECT r.id, r.task_id, r.rule_id, r.risk_level, r.findings, r.assignee, r.remediation_deadline, " +
            "rule.rule_name, rule.category FROM compliance_check_result r " +
            "LEFT JOIN compliance_rule rule ON r.rule_id = rule.id " +
            "WHERE r.check_status = 'FAILED' AND r.risk_level IN ('CRITICAL', 'HIGH') " +
            "AND r.remediation_status != 'COMPLETED' ORDER BY r.check_time DESC LIMIT 10")
    List<Map<String, Object>> getCriticalRisks();
}
