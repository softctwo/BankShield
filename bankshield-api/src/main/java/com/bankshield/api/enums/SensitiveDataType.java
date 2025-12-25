package com.bankshield.api.enums;

/**
 * 敏感数据类型枚举
 */
public enum SensitiveDataType {
    PHONE("PHONE", "手机号"),
    ID_CARD("ID_CARD", "身份证号"),
    BANK_CARD("BANK_CARD", "银行卡号"),
    NAME("NAME", "姓名"),
    EMAIL("EMAIL", "邮箱"),
    ADDRESS("ADDRESS", "地址");

    private final String code;
    private final String description;

    SensitiveDataType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static SensitiveDataType fromCode(String code) {
        for (SensitiveDataType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}