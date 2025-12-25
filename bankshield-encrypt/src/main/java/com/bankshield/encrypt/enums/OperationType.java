package com.bankshield.encrypt.enums;

/**
 * 操作类型枚举
 */
public enum OperationType {
    
    ENCRYPT("ENCRYPT", "加密"),
    DECRYPT("DECRYPT", "解密"),
    SIGN("SIGN", "签名"),
    VERIFY("VERIFY", "验签"),
    ROTATE("ROTATE", "轮换"),
    ACTIVATE("ACTIVATE", "激活"),
    DEACTIVATE("DEACTIVATE", "禁用"),
    DESTROY("DESTROY", "销毁");
    
    private final String code;
    private final String description;
    
    OperationType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static OperationType fromCode(String code) {
        for (OperationType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid operation type code: " + code);
    }
}