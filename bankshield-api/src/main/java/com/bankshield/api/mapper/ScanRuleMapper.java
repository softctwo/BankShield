package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.ScanRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ScanRuleMapper extends BaseMapper<ScanRule> {

    @Select("SELECT * FROM scan_rule WHERE rule_type = #{ruleType} AND enabled = 1 ORDER BY severity DESC")
    List<ScanRule> selectEnabledByType(@Param("ruleType") String ruleType);

    @Select("SELECT * FROM scan_rule WHERE enabled = 1 ORDER BY rule_type, severity DESC")
    List<ScanRule> selectAllEnabled();

    @Select("SELECT COUNT(*) FROM scan_rule WHERE enabled = 1")
    int countEnabled();
}
