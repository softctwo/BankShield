package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.DataClassificationRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 数据分级规则Mapper
 */
@Mapper
public interface DataClassificationRuleMapper extends BaseMapper<DataClassificationRule> {
    
    /**
     * 查询启用的规则，按优先级排序
     */
    @Select("SELECT * FROM data_classification_rule WHERE rule_status = 'ACTIVE' ORDER BY priority DESC")
    List<DataClassificationRule> selectActiveRulesOrderByPriority();
    
    /**
     * 根据数据类型查询规则
     */
    @Select("SELECT * FROM data_classification_rule WHERE rule_status = 'ACTIVE' AND (data_type = #{dataType} OR data_type IS NULL) ORDER BY priority DESC")
    List<DataClassificationRule> selectRulesByDataType(String dataType);
}
