package com.bankshield.api.enums;

/**
 * 报表类型枚举
 */
public enum ReportType {
    
    /**
     * 等保合规报表
     */
    DENG_BAO("等保"),
    
    /**
     * PCI-DSS合规报表
     */
    PCI_DSS("PCI-DSS"),
    
    /**
     * GDPR合规报表
     */
    GDPR("GDPR"),
    
    /**
     * 自定义报表
     */
    CUSTOM("自定义");
    
    private final String description;
    
    ReportType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}