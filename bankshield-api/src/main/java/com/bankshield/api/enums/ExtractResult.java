package com.bankshield.api.enums;

/**
 * 提取结果枚举
 */
public enum ExtractResult {
    SUCCESS("SUCCESS", "成功"),
    FAIL("FAIL", "失败");

    private final String code;
    private final String description;

    ExtractResult(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ExtractResult fromCode(String code) {
        for (ExtractResult result : values()) {
            if (result.code.equals(code)) {
                return result;
            }
        }
        return null;
    }
}