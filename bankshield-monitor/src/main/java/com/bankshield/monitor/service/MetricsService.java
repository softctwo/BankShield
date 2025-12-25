package com.bankshield.monitor.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 监控指标服务
 * 
 * @author BankShield Team
 * @version 1.0.0
 */
@Service
public class MetricsService {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private Counter securityAlertCounter;

    @Autowired
    private Counter dataProcessingCounter;

    @Autowired
    private Counter encryptionCounter;

    @Autowired
    private Counter auditLogCounter;

    @Autowired
    private Timer apiResponseTimer;

    /**
     * 记录安全告警
     */
    public void recordSecurityAlert(String alertType, String severity) {
        meterRegistry.counter("bankshield_security_alerts_total", 
            "type", "security", 
            "alert_type", alertType, 
            "severity", severity)
            .increment();
    }

    /**
     * 记录数据处理操作
     */
    public void recordDataProcessing(String operationType, String status) {
        meterRegistry.counter("bankshield_data_processing_total", 
            "type", "business", 
            "operation", operationType, 
            "status", status)
            .increment();
    }

    /**
     * 记录加密操作
     */
    public void recordEncryptionOperation(String algorithm, String operationType) {
        meterRegistry.counter("bankshield_encryption_operations_total", 
            "type", "encryption", 
            "algorithm", algorithm, 
            "operation", operationType)
            .increment();
    }

    /**
     * 记录审计日志
     */
    public void recordAuditLog(String auditType, String result) {
        meterRegistry.counter("bankshield_audit_logs_total", 
            "type", "audit", 
            "audit_type", auditType, 
            "result", result)
            .increment();
    }

    /**
     * 记录API响应时间
     */
    public Timer.Sample startApiTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * 记录API响应时间
     */
    public void recordApiResponseTime(Timer.Sample sample, String method, String endpoint, String status) {
        sample.stop(Timer.builder("bankshield_api_response_time")
                .tag("type", "performance")
                .tag("method", method)
                .tag("endpoint", endpoint)
                .tag("status", status)
                .register(meterRegistry));
    }

    /**
     * 记录数据库操作
     */
    public void recordDatabaseOperation(String operation, String table, String status, long duration) {
        meterRegistry.timer("bankshield_database_operation_duration", 
            "type", "database", 
            "operation", operation, 
            "table", table, 
            "status", status)
            .record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * 记录缓存操作
     */
    public void recordCacheOperation(String operation, String cacheName, String status) {
        meterRegistry.counter("bankshield_cache_operations_total", 
            "type", "cache", 
            "operation", operation, 
            "cache_name", cacheName, 
            "status", status)
            .increment();
    }

    /**
     * 记录用户登录
     */
    public void recordUserLogin(String username, boolean success) {
        meterRegistry.counter("bankshield_user_login_total", 
            "type", "user", 
            "username", username, 
            "status", success ? "success" : "failure")
            .increment();
    }

    /**
     * 记录权限检查
     */
    public void recordPermissionCheck(String resource, String action, boolean granted) {
        meterRegistry.counter("bankshield_permission_checks_total", 
            "type", "security", 
            "resource", resource, 
            "action", action, 
            "result", granted ? "granted" : "denied")
            .increment();
    }

    /**
     * 记录系统错误
     */
    public void recordSystemError(String errorType, String component) {
        meterRegistry.counter("bankshield_system_errors_total", 
            "type", "system", 
            "error_type", errorType, 
            "component", component)
            .increment();
    }

    /**
     * 记录业务指标
     */
    public void recordBusinessMetric(String metricName, double value, String... tags) {
        meterRegistry.gauge("bankshield_business_" + metricName, 
            java.util.Arrays.asList(tags), value);
    }
}