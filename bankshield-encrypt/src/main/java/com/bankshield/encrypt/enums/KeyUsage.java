package com.bankshield.encrypt.enums;

/**
 * 密钥用途枚举
 */
public enum KeyUsage {
    
    ENCRYPT("ENCRYPT", "加密"),
    DECRYPT("DECRYPT", "解密"),
    SIGN("SIGN", "签名"),
    VERIFY("VERIFY", "验签");
    
    private final String code;
    private final String description;
    
    KeyUsage(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static KeyUsage fromCode(String code) {
        for (KeyUsage usage : values()) {
            if (usage.code.equals(code)) {
                return usage;
            }
        }
        throw new IllegalArgumentException("Invalid key usage code: " + code);
    }
}