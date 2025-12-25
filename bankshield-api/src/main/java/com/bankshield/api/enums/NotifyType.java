package com.bankshield.api.enums;

/**
 * 通知类型枚举
 */
public enum NotifyType {
    EMAIL("EMAIL", "邮件"),
    SMS("SMS", "短信"),
    WEBHOOK("WEBHOOK", "Webhook"),
    DINGTALK("DINGTALK", "钉钉"),
    WECHAT("WECHAT", "企业微信");

    private final String code;
    private final String description;

    NotifyType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}