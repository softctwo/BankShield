package com.bankshield.lineage.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 血缘信息视图对象
 *
 * @author BankShield
 * @since 2024-01-24
 */
@Data
public class LineageInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 源表列表
     */
    private List<TableInfo> sourceTables;

    /**
     * 目标表列表
     */
    private List<TableInfo> targetTables;

    /**
     * 目标列列表
     */
    private List<ColumnInfo> targetColumns;

    /**
     * 语句类型
     */
    private String statementType;

    /**
     * 关系类型
     */
    private String relationshipType;

    /**
     * 转换逻辑
     */
    private String transformation;

    /**
     * 影响权重
     */
    private Integer impactWeight;

    /**
     * 获取源表名称（用于日志记录）
     */
    public String getSourceTable() {
        if (sourceTables != null && !sourceTables.isEmpty()) {
            return sourceTables.get(0).getTableName();
        }
        return null;
    }

    @Data
    public static class TableInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 表名称
         */
        private String tableName;

        /**
         * 数据库名称
         */
        private String databaseName;

        /**
         * Schema名称
         */
        private String schemaName;

        /**
         * 节点类型
         */
        private String nodeType;

        /**
         * 别名
         */
        private String alias;

        /**
         * 描述信息
         */
        private String description;
    }

    @Data
    public static class ColumnInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 列名称
         */
        private String columnName;

        /**
         * 表名称
         */
        private String tableName;

        /**
         * 数据库名称
         */
        private String databaseName;

        /**
         * 节点类型
         */
        private String nodeType;

        /**
         * 数据类型
         */
        private String dataType;

        /**
         * 表达式
         */
        private String expression;

        /**
         * 描述信息
         */
        private String description;
    }
}