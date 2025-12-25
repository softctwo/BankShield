package com.bankshield.encrypt.enums;

/**
 * 密钥类型枚举
 */
public enum KeyType {
    
    SM2("SM2", "国密SM2非对称加密算法"),
    SM3("SM3", "国密SM3哈希算法"),
    SM4("SM4", "国密SM4对称加密算法"),
    AES("AES", "AES对称加密算法"),
    RSA("RSA", "RSA非对称加密算法");
    
    private final String code;
    private final String description;
    
    KeyType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static KeyType fromCode(String code) {
        for (KeyType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid key type code: " + code);
    }
}