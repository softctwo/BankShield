package com.bankshield.api.enums;

/**
 * 任务类型枚举
 */
public enum TaskType {
    FILE("FILE", "文件处理"),
    DATABASE("DATABASE", "数据库处理");

    private final String code;
    private final String description;

    TaskType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static TaskType fromCode(String code) {
        for (TaskType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}