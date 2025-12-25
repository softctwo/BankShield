package com.bankshield.api.enums;

/**
 * 水印类型枚举
 */
public enum WatermarkType {
    TEXT("TEXT", "文本水印"),
    IMAGE("IMAGE", "图像水印"),
    DATABASE("DATABASE", "数据库水印");

    private final String code;
    private final String description;

    WatermarkType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static WatermarkType fromCode(String code) {
        for (WatermarkType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}