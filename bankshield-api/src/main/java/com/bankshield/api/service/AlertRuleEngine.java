package com.bankshield.api.service;

import com.bankshield.api.entity.AlertRule;
import com.bankshield.api.entity.MonitorMetric;

import java.util.List;

/**
 * 告警规则引擎接口
 */
public interface AlertRuleEngine {

    /**
     * 加载所有启用的告警规则
     */
    List<AlertRule> loadEnabledRules();

    /**
     * 检查监控指标是否触发告警
     */
    void checkMetricsAgainstRules(List<MonitorMetric> metrics, List<AlertRule> rules);

    /**
     * 检查单个规则
     */
    boolean checkRule(MonitorMetric metric, AlertRule rule);

    /**
     * 触发告警
     */
    void triggerAlert(AlertRule rule, MonitorMetric metric);

    /**
     * 执行告警检查任务
     */
    void executeAlertCheck();

    /**
     * 验证规则配置
     */
    boolean validateRule(AlertRule rule);
}