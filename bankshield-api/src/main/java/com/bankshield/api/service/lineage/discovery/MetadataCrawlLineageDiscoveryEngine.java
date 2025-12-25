package com.bankshield.api.service.lineage.discovery;

import com.bankshield.api.entity.DataFlow;
import com.bankshield.api.entity.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 元数据爬虫血缘发现引擎
 * 通过JDBC元数据接口爬取数据库元数据，发现数据流转关系
 */
@Slf4j
@Component
public class MetadataCrawlLineageDiscoveryEngine implements LineageDiscoveryEngine {

    @Override
    public String getStrategy() {
        return "METADATA_CRAWL";
    }

    @Override
    public List<DataFlow> discoverLineage(DataSource dataSource, Map<String, Object> config) {
        List<DataFlow> dataFlows = new ArrayList<>();
        
        try {
            Connection connection = getConnection(dataSource);
            DatabaseMetaData metaData = connection.getMetaData();
            
            // 1. 分析外键关系
            dataFlows.addAll(analyzeForeignKeys(metaData, dataSource.getId()));
            
            // 2. 分析索引关系（复合索引可能暗示字段关联）
            dataFlows.addAll(analyzeIndexRelationships(metaData, dataSource.getId()));
            
            // 3. 分析表名和字段名的语义相似性
            dataFlows.addAll(analyzeSemanticSimilarity(metaData, dataSource.getId()));
            
            // 4. 分析约束关系
            dataFlows.addAll(analyzeConstraints(metaData, dataSource.getId()));
            
            // 5. 分析存储过程和函数
            dataFlows.addAll(analyzeRoutines(metaData, dataSource.getId()));
            
            connection.close();
            
        } catch (Exception e) {
            log.error("元数据爬虫血缘发现失败", e);
        }
        
        return dataFlows;
    }

    /**
     * 分析外键关系
     */
    private List<DataFlow> analyzeForeignKeys(DatabaseMetaData metaData, Long dataSourceId) throws SQLException {
        List<DataFlow> dataFlows = new ArrayList<>();
        
        // 获取所有表
        ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
        
        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            String schemaName = tables.getString("TABLE_SCHEM");
            
            // 获取外键信息
            ResultSet foreignKeys = metaData.getImportedKeys(null, schemaName, tableName);
            
            while (foreignKeys.next()) {
                String pkTableName = foreignKeys.getString("PKTABLE_NAME");
                String pkColumnName = foreignKeys.getString("PKCOLUMN_NAME");
                String fkTableName = foreignKeys.getString("FKTABLE_NAME");
                String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");
                String fkName = foreignKeys.getString("FK_NAME");
                
                DataFlow dataFlow = DataFlow.builder()
                    .sourceTable(pkTableName)
                    .sourceColumn(pkColumnName)
                    .targetTable(fkTableName)
                    .targetColumn(fkColumnName)
                    .flowType("DIRECT")
                    .confidence(new java.math.BigDecimal("1.00"))
                    .discoveryTime(LocalDateTime.now())
                    .lastUpdated(LocalDateTime.now())
                    .transformation("外键关联: " + fkName)
                    .dataSourceId(dataSourceId)
                    .discoveryMethod("METADATA")
                    .build();
                
                dataFlows.add(dataFlow);
            }
            
            foreignKeys.close();
        }
        
        tables.close();
        return dataFlows;
    }

    /**
     * 分析索引关系
     */
    private List<DataFlow> analyzeIndexRelationships(DatabaseMetaData metaData, Long dataSourceId) throws SQLException {
        List<DataFlow> dataFlows = new ArrayList<>();
        
        // 获取所有表
        ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
        
        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            String schemaName = tables.getString("TABLE_SCHEM");
            
            // 获取索引信息
            ResultSet indexes = metaData.getIndexInfo(null, schemaName, tableName, false, false);
            
            Map<String, List<String>> indexColumns = new HashMap<>();
            
            while (indexes.next()) {
                String indexName = indexes.getString("INDEX_NAME");
                String columnName = indexes.getString("COLUMN_NAME");
                
                if (indexName != null && columnName != null) {
                    indexColumns.computeIfAbsent(indexName, k -> new ArrayList<>()).add(columnName);
                }
            }
            
            // 分析复合索引中的字段关系
            for (List<String> columns : indexColumns.values()) {
                if (columns.size() > 1) {
                    // 复合索引中的字段可能存在业务关联
                    for (int i = 0; i < columns.size() - 1; i++) {
                        String sourceColumn = columns.get(i);
                        String targetColumn = columns.get(i + 1);
                        
                        DataFlow dataFlow = DataFlow.builder()
                            .sourceTable(tableName)
                            .sourceColumn(sourceColumn)
                            .targetTable(tableName)
                            .targetColumn(targetColumn)
                            .flowType("INDIRECT")
                            .confidence(new java.math.BigDecimal("0.60"))
                            .discoveryTime(LocalDateTime.now())
                            .lastUpdated(LocalDateTime.now())
                            .transformation("复合索引暗示关联")
                            .dataSourceId(dataSourceId)
                            .discoveryMethod("METADATA")
                            .build();
                        
                        dataFlows.add(dataFlow);
                    }
                }
            }
            
            indexes.close();
        }
        
        tables.close();
        return dataFlows;
    }

    /**
     * 分析语义相似性
     */
    private List<DataFlow> analyzeSemanticSimilarity(DatabaseMetaData metaData, Long dataSourceId) throws SQLException {
        List<DataFlow> dataFlows = new ArrayList<>();
        
        // 获取所有表的列信息
        Map<String, List<ColumnInfo>> tableColumns = new HashMap<>();
        
        ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            String schemaName = tables.getString("TABLE_SCHEM");
            
            List<ColumnInfo> columns = new ArrayList<>();
            ResultSet columnsRs = metaData.getColumns(null, schemaName, tableName, "%");
            
            while (columnsRs.next()) {
                ColumnInfo columnInfo = new ColumnInfo();
                columnInfo.tableName = tableName;
                columnInfo.columnName = columnsRs.getString("COLUMN_NAME");
                columnInfo.dataType = columnsRs.getString("TYPE_NAME");
                columnInfo.isPrimaryKey = false; // 将在后面检查
                columns.add(columnInfo);
            }
            
            columnsRs.close();
            tableColumns.put(tableName, columns);
        }
        
        tables.close();
        
        // 分析列名相似性
        for (Map.Entry<String, List<ColumnInfo>> entry1 : tableColumns.entrySet()) {
            String table1 = entry1.getKey();
            List<ColumnInfo> columns1 = entry1.getValue();
            
            for (Map.Entry<String, List<ColumnInfo>> entry2 : tableColumns.entrySet()) {
                String table2 = entry2.getKey();
                List<ColumnInfo> columns2 = entry2.getValue();
                
                if (table1.equals(table2)) {
                    continue; // 跳过同一张表
                }
                
                // 查找相似的列名
                for (ColumnInfo col1 : columns1) {
                    for (ColumnInfo col2 : columns2) {
                        if (isColumnNamesSimilar(col1.columnName, col2.columnName)) {
                            double similarity = calculateColumnSimilarity(col1, col2);
                            
                            if (similarity > 0.7) {
                                DataFlow dataFlow = DataFlow.builder()
                                    .sourceTable(table1)
                                    .sourceColumn(col1.columnName)
                                    .targetTable(table2)
                                    .targetColumn(col2.columnName)
                                    .flowType("INDIRECT")
                                    .confidence(new java.math.BigDecimal(similarity))
                                    .discoveryTime(LocalDateTime.now())
                                    .lastUpdated(LocalDateTime.now())
                                    .transformation("语义相似性分析")
                                    .dataSourceId(dataSourceId)
                                    .discoveryMethod("METADATA")
                                    .build();
                                
                                dataFlows.add(dataFlow);
                            }
                        }
                    }
                }
            }
        }
        
        return dataFlows;
    }

    /**
     * 分析约束关系
     */
    private List<DataFlow> analyzeConstraints(DatabaseMetaData metaData, Long dataSourceId) throws SQLException {
        List<DataFlow> dataFlows = new ArrayList<>();
        
        // 获取所有表
        ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
        
        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            String schemaName = tables.getString("TABLE_SCHEM");
            
            // 获取主键信息
            ResultSet primaryKeys = metaData.getPrimaryKeys(null, schemaName, tableName);
            Set<String> pkColumns = new HashSet<>();
            
            while (primaryKeys.next()) {
                pkColumns.add(primaryKeys.getString("COLUMN_NAME"));
            }
            primaryKeys.close();
            
            // 获取唯一约束
            ResultSet indexInfo = metaData.getIndexInfo(null, schemaName, tableName, true, false);
            while (indexInfo.next()) {
                boolean nonUnique = indexInfo.getBoolean("NON_UNIQUE");
                if (!nonUnique) { // 唯一索引
                    String columnName = indexInfo.getString("COLUMN_NAME");
                    String indexName = indexInfo.getString("INDEX_NAME");
                    
                    if (columnName != null && !pkColumns.contains(columnName)) {
                        DataFlow dataFlow = DataFlow.builder()
                            .sourceTable(tableName)
                            .sourceColumn(columnName)
                            .targetTable(tableName)
                            .targetColumn(columnName)
                            .flowType("INDIRECT")
                            .confidence(new java.math.BigDecimal("0.75"))
                            .discoveryTime(LocalDateTime.now())
                            .lastUpdated(LocalDateTime.now())
                            .transformation("唯一约束: " + indexName)
                            .dataSourceId(dataSourceId)
                            .discoveryMethod("METADATA")
                            .build();
                        
                        dataFlows.add(dataFlow);
                    }
                }
            }
            indexInfo.close();
        }
        
        tables.close();
        return dataFlows;
    }

    /**
     * 分析存储过程和函数
     */
    private List<DataFlow> analyzeRoutines(DatabaseMetaData metaData, Long dataSourceId) throws SQLException {
        List<DataFlow> dataFlows = new ArrayList<>();
        
        // 获取存储过程
        ResultSet procedures = metaData.getProcedures(null, null, "%");
        
        while (procedures.next()) {
            String procedureName = procedures.getString("PROCEDURE_NAME");
            String procedureSchema = procedures.getString("PROCEDURE_SCHEM");
            
            // 这里可以获取存储过程的源代码并分析
            // 由于不同数据库的元数据接口不同，这里简化处理
            
            // 如果存储过程名称暗示了数据流转，可以创建相应的关系
            if (procedureName.toLowerCase().contains("sync") || 
                procedureName.toLowerCase().contains("copy") ||
                procedureName.toLowerCase().contains("transfer")) {
                
                // 创建通用的存储过程血缘关系
                DataFlow dataFlow = DataFlow.builder()
                    .sourceTable("UNKNOWN_SOURCE")
                    .targetTable("UNKNOWN_TARGET")
                    .flowType("TRANSFORMATION")
                    .confidence(new java.math.BigDecimal("0.50"))
                    .discoveryTime(LocalDateTime.now())
                    .lastUpdated(LocalDateTime.now())
                    .transformation("存储过程: " + procedureName)
                    .dataSourceId(dataSourceId)
                    .discoveryMethod("METADATA")
                    .build();
                
                dataFlows.add(dataFlow);
            }
        }
        
        procedures.close();
        return dataFlows;
    }

    /**
     * 检查列名是否相似
     */
    private boolean isColumnNamesSimilar(String columnName1, String columnName2) {
        // 简单的相似性检查
        String normalized1 = columnName1.toLowerCase().replace("_", "");
        String normalized2 = columnName2.toLowerCase().replace("_", "");
        
        // 完全匹配
        if (normalized1.equals(normalized2)) {
            return true;
        }
        
        // 包含关系
        if (normalized1.contains(normalized2) || normalized2.contains(normalized1)) {
            return true;
        }
        
        // 编辑距离小于2
        return calculateEditDistance(normalized1, normalized2) <= 2;
    }

    /**
     * 计算列相似度
     */
    private double calculateColumnSimilarity(ColumnInfo col1, ColumnInfo col2) {
        double similarity = 0.0;
        
        // 列名相似性
        double nameSimilarity = calculateNameSimilarity(col1.columnName, col2.columnName);
        similarity += nameSimilarity * 0.6;
        
        // 数据类型相似性
        double typeSimilarity = col1.dataType.equalsIgnoreCase(col2.dataType) ? 1.0 : 0.3;
        similarity += typeSimilarity * 0.3;
        
        // 表名相似性（如果表名相似，列名相似的可能性更大）
        double tableNameSimilarity = calculateNameSimilarity(col1.tableName, col2.tableName);
        similarity += tableNameSimilarity * 0.1;
        
        return Math.min(similarity, 1.0);
    }

    /**
     * 计算名称相似度
     */
    private double calculateNameSimilarity(String name1, String name2) {
        String normalized1 = name1.toLowerCase().replace("_", "");
        String normalized2 = name2.toLowerCase().replace("_", "");
        
        if (normalized1.equals(normalized2)) {
            return 1.0;
        }
        
        // 计算最长公共子序列
        int lcsLength = longestCommonSubsequence(normalized1, normalized2);
        double similarity = (double) lcsLength / Math.max(normalized1.length(), normalized2.length());
        
        return similarity;
    }

    /**
     * 计算编辑距离
     */
    private int calculateEditDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1])) + 1;
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }

    /**
     * 计算最长公共子序列
     */
    private int longestCommonSubsequence(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }

    /**
     * 获取数据库连接
     */
    private Connection getConnection(DataSource dataSource) throws SQLException {
        // 解析连接配置并创建连接
        // 这里应该根据数据源类型和配置创建相应的连接
        return null;
    }

    /**
     * 列信息内部类
     */
    private static class ColumnInfo {
        String tableName;
        String columnName;
        String dataType;
        boolean isPrimaryKey;
    }
}