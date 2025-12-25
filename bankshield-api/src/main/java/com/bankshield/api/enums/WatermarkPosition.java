package com.bankshield.api.enums;

/**
 * 水印位置枚举
 */
public enum WatermarkPosition {
    TOP_LEFT("TOP_LEFT", "左上角"),
    TOP_RIGHT("TOP_RIGHT", "右上角"),
    BOTTOM_LEFT("BOTTOM_LEFT", "左下角"),
    BOTTOM_RIGHT("BOTTOM_RIGHT", "右下角"),
    CENTER("CENTER", "中心"),
    FULLSCREEN("FULLSCREEN", "全屏");

    private final String code;
    private final String description;

    WatermarkPosition(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static WatermarkPosition fromCode(String code) {
        for (WatermarkPosition position : values()) {
            if (position.code.equals(code)) {
                return position;
            }
        }
        return null;
    }
}