package com.bankshield.api.service;

import java.util.List;
import java.util.Map;

/**
 * 监控告警服务接口
 */
public interface AlertService {

    /**
     * 创建告警规则
     *
     * @param rule 告警规则
     * @return 规则ID
     */
    Long createAlertRule(Map<String, Object> rule);

    /**
     * 更新告警规则
     *
     * @param ruleId 规则ID
     * @param rule 告警规则
     * @return 是否成功
     */
    boolean updateAlertRule(Long ruleId, Map<String, Object> rule);

    /**
     * 删除告警规则
     *
     * @param ruleId 规则ID
     * @return 是否成功
     */
    boolean deleteAlertRule(Long ruleId);

    /**
     * 获取所有告警规则
     *
     * @return 告警规则列表
     */
    List<Map<String, Object>> getAllAlertRules();

    /**
     * 触发告警
     *
     * @param alertType 告警类型
     * @param level 告警级别
     * @param message 告警消息
     * @param details 告警详情
     */
    void triggerAlert(String alertType, String level, String message, Map<String, Object> details);

    /**
     * 获取告警历史
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param level 告警级别（可选）
     * @return 告警历史列表
     */
    List<Map<String, Object>> getAlertHistory(String startTime, String endTime, String level);

    /**
     * 确认告警
     *
     * @param alertId 告警ID
     * @param operator 操作人
     * @return 是否成功
     */
    boolean acknowledgeAlert(Long alertId, String operator);

    /**
     * 关闭告警
     *
     * @param alertId 告警ID
     * @param operator 操作人
     * @param resolution 解决方案
     * @return 是否成功
     */
    boolean closeAlert(Long alertId, String operator, String resolution);

    /**
     * 获取告警统计
     *
     * @param days 统计天数
     * @return 统计数据
     */
    Map<String, Object> getAlertStatistics(int days);

    /**
     * 发送告警通知
     *
     * @param alertId 告警ID
     * @param channels 通知渠道（email/sms/webhook）
     * @return 是否成功
     */
    boolean sendAlertNotification(Long alertId, List<String> channels);
}
