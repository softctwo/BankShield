package com.bankshield.api.enums;

/**
 * 告警状态枚举
 */
public enum AlertStatus {
    UNRESOLVED("UNRESOLVED", "未处理"),
    RESOLVED("RESOLVED", "已处理"),
    IGNORED("IGNORED", "已忽略");

    private final String code;
    private final String description;

    AlertStatus(String code, String description) {
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