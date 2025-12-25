package com.bankshield.lineage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.lineage.entity.DataQualityRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据质量规则 Mapper 接口
 *
 * @author BankShield
 * @since 2024-01-24
 */
@Mapper
public interface DataQualityRuleMapper extends BaseMapper<DataQualityRule> {
}
