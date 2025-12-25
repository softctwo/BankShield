package com.bankshield.encrypt.enums;

/**
 * 密钥状态枚举
 */
public enum KeyStatus {
    
    ACTIVE("ACTIVE", "活跃"),
    INACTIVE("INACTIVE", "禁用"),
    EXPIRED("EXPIRED", "已过期"),
    REVOKED("REVOKED", "已撤销"),
    DESTROYED("DESTROYED", "已销毁");
    
    private final String code;
    private final String description;
    
    KeyStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static KeyStatus fromCode(String code) {
        for (KeyStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid key status code: " + code);
    }
}