package com.bankshield.api.service.impl;

import com.bankshield.api.entity.AlertRecord;
import com.bankshield.api.entity.AlertRule;
import com.bankshield.api.entity.MonitorMetric;
import com.bankshield.api.enums.AlertLevel;
import com.bankshield.api.enums.AlertStatus;
import com.bankshield.api.mapper.AlertRecordMapper;
import com.bankshield.api.mapper.AlertRuleMapper;
import com.bankshield.api.mapper.MonitorMetricMapper;
import com.bankshield.api.service.AlertRuleEngine;
import com.bankshield.api.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 告警规则引擎实现类
 */
@Slf4j
@Service
public class AlertRuleEngineImpl implements AlertRuleEngine {

    @Autowired
    private AlertRuleMapper alertRuleMapper;

    @Autowired
    private MonitorMetricMapper monitorMetricMapper;

    @Autowired
    private AlertRecordMapper alertRecordMapper;

    @Autowired
    private NotificationService notificationService;

    @Override
    public List<AlertRule> loadEnabledRules() {
        return alertRuleMapper.selectEnabledRules();
    }

    @Override
    public void checkMetricsAgainstRules(List<MonitorMetric> metrics, List<AlertRule> rules) {
        if (metrics == null || rules == null) {
            return;
        }

        for (MonitorMetric metric : metrics) {
            for (AlertRule rule : rules) {
                if (shouldCheckMetric(metric, rule)) {
                    boolean triggered = checkRule(metric, rule);
                    if (triggered) {
                        triggerAlert(rule, metric);
                    }
                }
            }
        }
    }

    @Override
    public boolean checkRule(MonitorMetric metric, AlertRule rule) {
        try {
            Double metricValue = metric.getMetricValue();
            Double threshold = rule.getThreshold();
            String condition = rule.getTriggerCondition();

            if (metricValue == null || threshold == null || condition == null) {
                log.warn("规则检查参数不完整: metricValue={}, threshold={}, condition={}", 
                        metricValue, threshold, condition);
                return false;
            }

            boolean triggered = false;
            switch (condition) {
                case ">":
                    triggered = metricValue > threshold;
                    break;
                case "<":
                    triggered = metricValue < threshold;
                    break;
                case "=":
                    triggered = Math.abs(metricValue - threshold) < 0.001;
                    break;
                case ">=":
                    triggered = metricValue >= threshold;
                    break;
                case "<=":
                    triggered = metricValue <= threshold;
                    break;
                case "!=":
                    triggered = Math.abs(metricValue - threshold) > 0.001;
                    break;
                default:
                    log.warn("不支持的触发条件: {}", condition);
                    return false;
            }

            log.debug("规则检查: metric={}, value={}, condition={}, threshold={}, triggered={}", 
                    metric.getMetricName(), metricValue, condition, threshold, triggered);

            return triggered;
        } catch (Exception e) {
            log.error("规则检查失败: rule={}, metric={}", rule.getRuleName(), metric.getMetricName(), e);
            return false;
        }
    }

    @Override
    public void triggerAlert(AlertRule rule, MonitorMetric metric) {
        try {
            // 检查是否已存在相同规则的未处理告警
            boolean exists = checkExistingAlert(rule.getId());
            if (exists) {
                log.info("规则 {} 已存在未处理告警，跳过创建新告警", rule.getRuleName());
                return;
            }

            // 创建告警记录
            AlertRecord alertRecord = AlertRecord.builder()
                    .ruleId(rule.getId())
                    .alertLevel(rule.getAlertLevel())
                    .alertTitle(buildAlertTitle(rule, metric))
                    .alertContent(buildAlertContent(rule, metric))
                    .alertTime(LocalDateTime.now())
                    .alertStatus(AlertStatus.UNRESOLVED.getCode())
                    .notifyStatus("UNNOTIFIED")
                    .build();

            alertRecordMapper.insert(alertRecord);
            log.info("创建告警记录: {}", alertRecord.getAlertTitle());

            // 发送通知
            notificationService.sendAlertNotification(alertRecord);

        } catch (Exception e) {
            log.error("触发告警失败: rule={}, metric={}", rule.getRuleName(), metric.getMetricName(), e);
        }
    }

    @Override
    public void executeAlertCheck() {
        log.info("开始执行告警检查任务");
        try {
            // 加载所有启用的告警规则
            List<AlertRule> enabledRules = loadEnabledRules();
            log.info("加载到 {} 条启用的告警规则", enabledRules.size());

            if (enabledRules.isEmpty()) {
                log.info("没有启用的告警规则，跳过检查");
                return;
            }

            // 获取最新的监控指标
            List<MonitorMetric> latestMetrics = getLatestMetrics();
            log.info("获取到 {} 个最新监控指标", latestMetrics.size());

            if (latestMetrics.isEmpty()) {
                log.info("没有监控指标数据，跳过检查");
                return;
            }

            // 检查指标是否触发告警规则
            checkMetricsAgainstRules(latestMetrics, enabledRules);

            log.info("告警检查任务执行完成");
        } catch (Exception e) {
            log.error("执行告警检查任务失败", e);
        }
    }

    @Override
    public boolean validateRule(AlertRule rule) {
        if (rule == null) {
            return false;
        }

        // 检查必填字段
        if (rule.getRuleName() == null || rule.getRuleName().trim().isEmpty()) {
            log.warn("规则名称不能为空");
            return false;
        }

        if (rule.getMonitorMetric() == null || rule.getMonitorMetric().trim().isEmpty()) {
            log.warn("监控指标不能为空");
            return false;
        }

        if (rule.getTriggerCondition() == null || rule.getTriggerCondition().trim().isEmpty()) {
            log.warn("触发条件不能为空");
            return false;
        }

        if (rule.getThreshold() == null) {
            log.warn("阈值不能为空");
            return false;
        }

        if (rule.getAlertLevel() == null || rule.getAlertLevel().trim().isEmpty()) {
            log.warn("告警级别不能为空");
            return false;
        }

        // 检查触发条件是否有效
        String condition = rule.getTriggerCondition();
        if (!condition.matches("^(>|<|=|>=|<=|!=)$")) {
            log.warn("无效的触发条件: {}", condition);
            return false;
        }

        // 检查告警级别是否有效
        String alertLevel = rule.getAlertLevel();
        try {
            AlertLevel.valueOf(alertLevel);
        } catch (IllegalArgumentException e) {
            log.warn("无效的告警级别: {}", alertLevel);
            return false;
        }

        return true;
    }

    /**
     * 判断是否应该检查该指标
     */
    private boolean shouldCheckMetric(MonitorMetric metric, AlertRule rule) {
        if (metric == null || rule == null) {
            return false;
        }

        // 检查指标名称是否匹配
        String metricName = metric.getMetricName();
        String ruleMetric = rule.getMonitorMetric();
        
        if (metricName == null || ruleMetric == null) {
            return false;
        }

        // 完全匹配或包含匹配
        return metricName.equals(ruleMetric) || metricName.contains(ruleMetric) || ruleMetric.contains(metricName);
    }

    /**
     * 检查是否已存在相同规则的未处理告警
     */
    private boolean checkExistingAlert(Long ruleId) {
        // 这里简化处理，实际应该查询数据库
        // 检查最近1小时内是否已存在相同规则的未处理告警
        return false;
    }

    /**
     * 获取最新的监控指标
     */
    private List<MonitorMetric> getLatestMetrics() {
        // 获取最近5分钟内的指标
        return monitorMetricMapper.selectMetricsByTimeRange(null, null, 
                LocalDateTime.now().minusMinutes(5), LocalDateTime.now());
    }

    /**
     * 构建告警标题
     */
    private String buildAlertTitle(AlertRule rule, MonitorMetric metric) {
        return String.format("[%s] %s - %s", 
                rule.getAlertLevel(), rule.getRuleName(), metric.getMetricName());
    }

    /**
     * 构建告警内容
     */
    private String buildAlertContent(AlertRule rule, MonitorMetric metric) {
        return String.format("监控指标 [%s] 触发告警规则 [%s]。当前值: %.2f %s，阈值: %.2f %s，触发条件: %s，告警级别: %s",
                metric.getMetricName(),
                rule.getRuleName(),
                metric.getMetricValue(),
                metric.getMetricUnit() != null ? metric.getMetricUnit() : "",
                rule.getThreshold(),
                metric.getMetricUnit() != null ? metric.getMetricUnit() : "",
                rule.getTriggerCondition(),
                rule.getAlertLevel());
    }
}