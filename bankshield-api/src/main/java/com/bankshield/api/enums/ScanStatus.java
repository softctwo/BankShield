package com.bankshield.api.enums;

/**
 * 扫描状态枚举
 * @author BankShield
 */
public enum ScanStatus {
    /**
     * 待执行
     */
    PENDING("待执行"),
    
    /**
     * 执行中
     */
    RUNNING("执行中"),
    
    /**
     * 执行成功
     */
    SUCCESS("执行成功"),
    
    /**
     * 执行失败
     */
    FAILED("执行失败"),
    
    /**
     * 部分成功
     */
    PARTIAL("部分成功");

    private final String description;

    ScanStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}