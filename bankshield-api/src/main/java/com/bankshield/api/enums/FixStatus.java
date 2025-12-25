package com.bankshield.api.enums;

/**
 * 修复状态枚举
 * @author BankShield
 */
public enum FixStatus {
    UNFIXED("未修复"),
    RESOLVED("已修复"),
    WONT_FIX("不修复");

    private final String description;

    FixStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}