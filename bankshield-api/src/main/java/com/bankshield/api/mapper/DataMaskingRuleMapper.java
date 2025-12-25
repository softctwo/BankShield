package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.DataMaskingRule;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 数据脱敏规则Mapper接口
 */
@Repository
public interface DataMaskingRuleMapper extends BaseMapper<DataMaskingRule> {

    /**
     * 根据敏感数据类型查询启用的规则
     * @param sensitiveDataType 敏感数据类型
     * @param scenario 应用场景
     * @return 脱敏规则列表
     */
    @Select("SELECT * FROM masking_rule WHERE sensitive_data_type = #{sensitiveDataType} " +
            "AND enabled = 1 AND FIND_IN_SET(#{scenario}, applicable_scenarios)")
    List<DataMaskingRule> selectEnabledRulesByType(@Param("sensitiveDataType") String sensitiveDataType, 
                                                   @Param("scenario") String scenario);

    /**
     * 分页查询脱敏规则
     * @param page 分页参数
     * @param ruleName 规则名称（模糊查询）
     * @param sensitiveDataType 敏感数据类型
     * @param scenario 适用场景
     * @param enabled 启用状态
     * @return 分页结果
     */
    IPage<DataMaskingRule> selectPageWithConditions(Page<DataMaskingRule> page,
                                                    @Param("ruleName") String ruleName,
                                                    @Param("sensitiveDataType") String sensitiveDataType,
                                                    @Param("scenario") String scenario,
                                                    @Param("enabled") Boolean enabled);

    /**
     * 检查规则名称是否已存在
     * @param ruleName 规则名称
     * @param excludeId 排除的ID（更新时使用）
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) FROM masking_rule WHERE rule_name = #{ruleName} AND #{excludeId} != id")
    int countByRuleName(@Param("ruleName") String ruleName, @Param("excludeId") Long excludeId);
}