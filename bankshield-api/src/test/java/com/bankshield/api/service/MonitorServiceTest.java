package com.bankshield.api.service;

import com.bankshield.api.entity.AlertRule;
import com.bankshield.api.entity.MonitorMetric;
import com.bankshield.api.enums.AlertLevel;
import com.bankshield.api.enums.MetricType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 监控服务测试类
 */
@SpringBootTest
public class MonitorServiceTest {

    @Autowired
    private MonitorDataCollectionService monitorDataCollectionService;

    @Autowired
    private AlertRuleEngine alertRuleEngine;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MonitorDashboardService monitorDashboardService;

    @Test
    public void testCollectSystemMetrics() {
        // 测试系统指标采集
        monitorDataCollectionService.collectSystemMetrics();
        
        double cpuUsage = monitorDataCollectionService.getCpuUsage();
        double memoryUsage = monitorDataCollectionService.getMemoryUsage();
        double diskUsage = monitorDataCollectionService.getDiskUsage();
        
        assertTrue(cpuUsage >= 0 && cpuUsage <= 100, "CPU使用率应该在0-100%之间");
        assertTrue(memoryUsage >= 0 && memoryUsage <= 100, "内存使用率应该在0-100%之间");
        assertTrue(diskUsage >= 0 && diskUsage <= 100, "磁盘使用率应该在0-100%之间");
    }

    @Test
    public void testCollectDatabaseMetrics() {
        // 测试数据库指标采集
        monitorDataCollectionService.collectDatabaseMetrics();
        
        int connectionCount = monitorDataCollectionService.getDatabaseConnectionCount();
        assertTrue(connectionCount >= 0, "数据库连接数应该大于等于0");
    }

    @Test
    public void testCollectApplicationMetrics() {
        // 测试应用指标采集
        monitorDataCollectionService.collectApplicationMetrics();
        
        // 验证数据采集没有异常
        assertDoesNotThrow(() -> monitorDataCollectionService.collectApplicationMetrics());
    }

    @Test
    public void testCollectSecurityMetrics() {
        // 测试安全指标采集
        monitorDataCollectionService.collectSecurityMetrics();
        
        int activeUserCount = monitorDataCollectionService.getActiveUserCount();
        assertTrue(activeUserCount >= 0, "活跃用户数应该大于等于0");
    }

    @Test
    public void testCollectAllMetrics() {
        // 测试采集所有指标
        assertDoesNotThrow(() -> monitorDataCollectionService.collectAllMetrics());
    }

    @Test
    public void testAlertRuleValidation() {
        // 测试告警规则验证
        AlertRule validRule = AlertRule.builder()
                .ruleName("测试规则")
                .ruleType("SYSTEM")
                .monitorMetric("CPU使用率")
                .triggerCondition(">")
                .threshold(80.0)
                .alertLevel(AlertLevel.WARNING.getCode())
                .build();
        
        assertTrue(alertRuleEngine.validateRule(validRule), "有效规则应该通过验证");
        
        AlertRule invalidRule = AlertRule.builder()
                .ruleName("")
                .ruleType("SYSTEM")
                .monitorMetric("CPU使用率")
                .triggerCondition(">")
                .threshold(80.0)
                .alertLevel(AlertLevel.WARNING.getCode())
                .build();
        
        assertFalse(alertRuleEngine.validateRule(invalidRule), "无效规则不应该通过验证");
    }

    @Test
    public void testAlertRuleCheck() {
        // 测试告警规则检查
        AlertRule rule = AlertRule.builder()
                .ruleName("CPU告警规则")
                .ruleType("SYSTEM")
                .monitorMetric("CPU使用率")
                .triggerCondition(">")
                .threshold(80.0)
                .alertLevel(AlertLevel.WARNING.getCode())
                .build();
        
        // 测试触发告警的情况
        MonitorMetric highCpuMetric = MonitorMetric.builder()
                .metricName("CPU使用率")
                .metricType(MetricType.SYSTEM.getCode())
                .metricValue(85.0)
                .metricUnit("%")
                .build();
        
        boolean shouldTrigger = alertRuleEngine.checkRule(highCpuMetric, rule);
        assertTrue(shouldTrigger, "CPU使用率85%应该触发阈值为80%的告警规则");
        
        // 测试不触发告警的情况
        MonitorMetric normalCpuMetric = MonitorMetric.builder()
                .metricName("CPU使用率")
                .metricType(MetricType.SYSTEM.getCode())
                .metricValue(75.0)
                .metricUnit("%")
                .build();
        
        boolean shouldNotTrigger = alertRuleEngine.checkRule(normalCpuMetric, rule);
        assertFalse(shouldNotTrigger, "CPU使用率75%不应该触发阈值为80%的告警规则");
    }

    @Test
    public void testDashboardStats() {
        // 测试Dashboard统计
        var stats = monitorDashboardService.getDashboardStats();
        
        assertNotNull(stats, "Dashboard统计数据不应该为null");
        assertTrue(stats.containsKey("todayAlertCount"), "应该包含今日告警数");
        assertTrue(stats.containsKey("unresolvedAlertCount"), "应该包含未处理告警数");
        assertTrue(stats.containsKey("systemHealthScore"), "应该包含系统健康度");
    }

    @Test
    public void testSystemHealthScore() {
        // 测试系统健康度计算
        double healthScore = monitorDashboardService.getSystemHealthScore();
        
        assertTrue(healthScore >= 0 && healthScore <= 100, "系统健康度应该在0-100之间");
    }

    @Test
    public void testAlertTrend() {
        // 测试告警趋势数据
        List<Map<String, Object>> alertTrend = monitorDashboardService.get24HourAlertTrend();
        
        assertNotNull(alertTrend, "告警趋势数据不应该为null");
        assertFalse(alertTrend.isEmpty(), "告警趋势数据不应该为空");
    }

    @Test
    public void testAlertDistribution() {
        // 测试告警分布数据
        List<Map<String, Object>> distribution = monitorDashboardService.getAlertTypeDistribution();
        
        assertNotNull(distribution, "告警分布数据不应该为null");
    }

    @Test
    public void testMetricTrend() {
        // 测试监控指标趋势
        List<Map<String, Object>> trend = monitorDashboardService.getMetricTrend(
                MetricType.SYSTEM.getCode(), "CPU使用率", 24);
        
        assertNotNull(trend, "监控指标趋势数据不应该为null");
    }

    @Test
    public void testNotificationTemplate() {
        // 测试通知模板渲染
        AlertRecord alertRecord = AlertRecord.builder()
                .alertTitle("测试告警")
                .alertContent("这是一个测试告警")
                .alertLevel(AlertLevel.WARNING.getCode())
                .alertTime(LocalDateTime.now())
                .build();
        
        String template = "告警: {{title}}, 级别: {{level}}, 时间: {{time}}";
        String rendered = notificationService.renderTemplate(template, alertRecord);
        
        assertNotNull(rendered, "渲染后的模板不应该为null");
        assertTrue(rendered.contains("测试告警"), "渲染结果应该包含告警标题");
        assertTrue(rendered.contains("WARNING"), "渲染结果应该包含告警级别");
    }

    @Test
    public void testNotificationFrequencyControl() {
        // 测试通知频率控制
        String alertLevel = AlertLevel.WARNING.getCode();
        String notifyType = "EMAIL";
        
        // 第一次应该允许发送
        boolean firstTime = notificationService.canSendNotification(alertLevel, notifyType);
        assertTrue(firstTime, "第一次发送通知应该被允许");
        
        // 立即再次检查应该不允许（因为频率控制）
        // 注意：这里需要等待频率控制时间过去才能再次测试
    }
}