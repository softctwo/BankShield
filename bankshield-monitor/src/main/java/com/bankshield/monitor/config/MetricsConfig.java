package com.bankshield.monitor.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自定义监控指标配置
 * 
 * @author BankShield Team
 * @version 1.0.0
 */
@Configuration
public class MetricsConfig {

    /**
     * 安全告警计数器
     */
    @Bean
    public Counter securityAlertCounter(MeterRegistry registry) {
        return Counter.builder("bankshield_security_alerts_total")
                .description("Total number of security alerts")
                .tag("type", "security")
                .register(registry);
    }

    /**
     * 数据处理量计数器
     */
    @Bean
    public Counter dataProcessingCounter(MeterRegistry registry) {
        return Counter.builder("bankshield_data_processing_total")
                .description("Total number of data processing operations")
                .tag("type", "business")
                .register(registry);
    }

    /**
     * 加密操作计数器
     */
    @Bean
    public Counter encryptionCounter(MeterRegistry registry) {
        return Counter.builder("bankshield_encryption_operations_total")
                .description("Total number of encryption operations")
                .tag("type", "encryption")
                .register(registry);
    }

    /**
     * API响应时间计时器
     */
    @Bean
    public Timer apiResponseTimer(MeterRegistry registry) {
        return Timer.builder("bankshield_api_response_time")
                .description("API response time")
                .tag("type", "performance")
                .register(registry);
    }

    /**
     * 审计日志计数器
     */
    @Bean
    public Counter auditLogCounter(MeterRegistry registry) {
        return Counter.builder("bankshield_audit_logs_total")
                .description("Total number of audit logs")
                .tag("type", "audit")
                .register(registry);
    }

    /**
     * 系统健康度指标
     */
    @Bean
    public Gauge systemHealthGauge(MeterRegistry registry) {
        return Gauge.builder("bankshield_system_health_score")
                .description("System health score (0-100)")
                .tag("type", "health")
                .register(registry, this, MetricsConfig::getHealthScore);
    }

    /**
     * 在线用户数指标
     */
    @Bean
    public Gauge onlineUsersGauge(MeterRegistry registry) {
        return Gauge.builder("bankshield_online_users")
                .description("Number of online users")
                .tag("type", "users")
                .register(registry, this, MetricsConfig::getOnlineUsers);
    }

    /**
     * 获取系统健康分数（模拟实现）
     */
    private double getHealthScore() {
        // 这里可以集成实际的健康检查逻辑
        // 目前返回模拟数据
        return 95.0;
    }

    /**
     * 获取在线用户数（模拟实现）
     */
    private double getOnlineUsers() {
        // 这里可以集成实际的用户统计逻辑
        // 目前返回模拟数据
        return 128.0;
    }
}