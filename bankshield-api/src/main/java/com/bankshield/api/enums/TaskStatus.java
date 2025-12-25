package com.bankshield.api.enums;

/**
 * 任务状态枚举
 * @author BankShield
 */
public enum TaskStatus {
    PENDING("PENDING", "待执行"),
    RUNNING("RUNNING", "执行中"),
    SUCCESS("SUCCESS", "成功"),
    FAILED("FAILED", "失败"),
    PARTIAL("PARTIAL", "部分成功");

    private final String code;
    private final String description;

    TaskStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static TaskStatus fromCode(String code) {
        for (TaskStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}