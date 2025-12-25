package com.bankshield.api.enums;

/**
 * 脱敏算法枚举
 */
public enum MaskingAlgorithm {
    PARTIAL_MASK("PARTIAL_MASK", "部分掩码"),
    FULL_MASK("FULL_MASK", "完整掩码"),
    HASH("HASH", "哈希算法"),
    SYMMETRIC_ENCRYPT("SYMMETRIC_ENCRYPT", "对称加密"),
    FORMAT_PRESERVING("FORMAT_PRESERVING", "格式保留加密");

    private final String code;
    private final String description;

    MaskingAlgorithm(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static MaskingAlgorithm fromCode(String code) {
        for (MaskingAlgorithm algorithm : values()) {
            if (algorithm.code.equals(code)) {
                return algorithm;
            }
        }
        return null;
    }
}