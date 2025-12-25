package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.AlertRule;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 告警规则Mapper接口
 */
@Repository
public interface AlertRuleMapper extends BaseMapper<AlertRule> {

    /**
     * 分页查询告警规则
     */
    IPage<AlertRule> selectPage(Page<AlertRule> page,
                               @Param("ruleName") String ruleName,
                               @Param("ruleType") String ruleType,
                               @Param("alertLevel") String alertLevel,
                               @Param("enabled") Integer enabled);

    /**
     * 查询所有启用的告警规则
     */
    List<AlertRule> selectEnabledRules();

    /**
     * 根据规则名称查询
     */
    AlertRule selectByRuleName(@Param("ruleName") String ruleName);

    /**
     * 批量更新启用状态
     */
    int batchUpdateEnabled(@Param("ids") List<Long> ids, @Param("enabled") Integer enabled);
}