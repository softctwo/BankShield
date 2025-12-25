package com.bankshield.api.enums;

/**
 * 告警级别枚举
 */
public enum AlertLevel {
    INFO("INFO", "信息", "#909399"),
    WARNING("WARNING", "警告", "#E6A23C"),
    CRITICAL("CRITICAL", "严重", "#F56C6C"),
    EMERGENCY("EMERGENCY", "紧急", "#FF0000");

    private final String code;
    private final String description;
    private final String color;

    AlertLevel(String code, String description, String color) {
        this.code = code;
        this.description = description;
        this.color = color;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getColor() {
        return color;
    }
}