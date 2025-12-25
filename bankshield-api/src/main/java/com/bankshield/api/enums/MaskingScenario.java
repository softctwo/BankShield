package com.bankshield.api.enums;

/**
 * 脱敏场景枚举
 */
public enum MaskingScenario {
    DISPLAY("DISPLAY", "页面展示"),
    EXPORT("EXPORT", "数据导出"),
    QUERY("QUERY", "查询结果"),
    TRANSFER("TRANSFER", "数据传输");

    private final String code;
    private final String description;

    MaskingScenario(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static MaskingScenario fromCode(String code) {
        for (MaskingScenario scenario : values()) {
            if (scenario.code.equals(code)) {
                return scenario;
            }
        }
        return null;
    }
}