package com.bankshield.api.enums;

/**
 * 扫描类型枚举
 * @author BankShield
 */
public enum ScanType {
    VULNERABILITY("漏洞扫描"),
    CONFIG("配置检查"),
    WEAK_PASSWORD("弱密码检测"),
    ANOMALY("异常行为检测"),
    ALL("全面扫描");

    private final String description;

    ScanType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}