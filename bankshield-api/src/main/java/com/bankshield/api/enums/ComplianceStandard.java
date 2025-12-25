package com.bankshield.api.enums;

/**
 * 合规标准枚举
 * @author BankShield
 */
public enum ComplianceStandard {
    GB_LEVEL3("等保三级", "GB/T 22239-2019"),
    PCI_DSS("PCI DSS", "Payment Card Industry Data Security Standard"),
    OWASP_TOP10("OWASP TOP10", "OWASP Top 10 Security Risks"),
    ISO27001("ISO 27001", "Information Security Management"),
    CUSTOM("自定义", "Custom Security Baseline");

    private final String name;
    private final String description;

    ComplianceStandard(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}