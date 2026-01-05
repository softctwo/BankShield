package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.RemediationPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RemediationPlanMapper extends BaseMapper<RemediationPlan> {

    @Select("SELECT * FROM remediation_plan WHERE plan_status = #{status} ORDER BY priority DESC, scheduled_date ASC")
    List<RemediationPlan> selectByStatus(@Param("status") String status);

    @Select("SELECT * FROM remediation_plan WHERE assigned_to = #{assignedTo} AND plan_status IN ('APPROVED', 'IN_PROGRESS') ORDER BY deadline ASC")
    List<RemediationPlan> selectByAssignee(@Param("assignedTo") String assignedTo);

    @Select("SELECT COUNT(*) FROM remediation_plan WHERE plan_status = 'IN_PROGRESS'")
    int countInProgress();
}
