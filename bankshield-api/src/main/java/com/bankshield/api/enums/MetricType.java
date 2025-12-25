package com.bankshield.api.enums;

/**
 * 监控指标类型枚举
 */
public enum MetricType {
    SYSTEM("SYSTEM", "系统指标"),
    SECURITY("SECURITY", "安全指标"),
    DATABASE("DATABASE", "数据库指标"),
    SERVICE("SERVICE", "服务指标");

    private final String code;
    private final String description;

    MetricType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}