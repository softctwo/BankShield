package com.bankshield.lineage.enums;

/**
 * 数据质量规则类型枚举
 *
 * @author BankShield
 * @since 2024-01-24
 */
public enum RuleType {
    
    COMPLETENESS("COMPLETENESS", "完整性"),
    ACCURACY("ACCURACY", "准确性"),
    CONSISTENCY("CONSISTENCY", "一致性"),
    TIMELINESS("TIMELINESS", "及时性"),
    UNIQUENESS("UNIQUENESS", "唯一性"),
    VALIDITY("VALIDITY", "有效性"),
    INTEGRITY("INTEGRITY", "完整性约束"),
    CONFORMITY("CONFORMITY", "规范性");
    
    private final String code;
    private final String description;
    
    RuleType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static RuleType fromCode(String code) {
        for (RuleType type : RuleType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}