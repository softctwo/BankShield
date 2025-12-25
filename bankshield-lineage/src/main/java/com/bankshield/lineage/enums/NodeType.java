package com.bankshield.lineage.enums;

/**
 * 血缘节点类型枚举
 *
 * @author BankShield
 * @since 2024-01-24
 */
public enum NodeType {
    
    DATASOURCE("DATASOURCE", "数据源"),
    DATABASE("DATABASE", "数据库"),
    SCHEMA("SCHEMA", "模式"),
    TABLE("TABLE", "数据表"),
    COLUMN("COLUMN", "数据字段"),
    VIEW("VIEW", "视图"),
    REPORT("REPORT", "报表"),
    ETL("ETL", "ETL任务"),
    APPLICATION("APPLICATION", "应用程序"),
    API("API", "API接口");
    
    private final String code;
    private final String description;
    
    NodeType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static NodeType fromCode(String code) {
        for (NodeType type : NodeType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}