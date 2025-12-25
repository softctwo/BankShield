package com.bankshield.lineage.enums;

/**
 * 血缘关系类型枚举
 *
 * @author BankShield
 * @since 2024-01-24
 */
public enum RelationshipType {
    
    DIRECT("DIRECT", "直接关系"),
    INDIRECT("INDIRECT", "间接关系"),
    VIEW("VIEW", "视图关系"),
    ETL("ETL", "ETL转换"),
    JOIN("JOIN", "连接关系"),
    UNION("UNION", "合并关系"),
    FILTER("FILTER", "过滤关系"),
    AGGREGATE("AGGREGATE", "聚合关系"),
    CALCULATE("CALCULATE", "计算关系"),
    DERIVE("DERIVE", "派生关系");
    
    private final String code;
    private final String description;
    
    RelationshipType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static RelationshipType fromCode(String code) {
        for (RelationshipType type : RelationshipType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}