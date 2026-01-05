package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.ComplianceReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 合规报告Mapper
 */
@Mapper
public interface ComplianceReportMapper extends BaseMapper<ComplianceReport> {
}
