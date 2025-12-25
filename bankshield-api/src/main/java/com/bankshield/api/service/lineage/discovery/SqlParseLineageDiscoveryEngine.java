package com.bankshield.api.service.lineage.discovery;

import com.bankshield.api.entity.DataFlow;
import com.bankshield.api.entity.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL解析血缘发现引擎
 * 使用ANTLR解析SQL语句，提取数据流转关系
 */
@Slf4j
@Component
public class SqlParseLineageDiscoveryEngine implements LineageDiscoveryEngine {

    private static final Pattern CREATE_VIEW_PATTERN = Pattern.compile(
        "CREATE\\s+(?:OR\\s+REPLACE\\s+)?VIEW\\s+([\\w.]+)\\s+AS\\s+(.+)", 
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    
    private static final Pattern INSERT_SELECT_PATTERN = Pattern.compile(
        "INSERT\\s+INTO\\s+([\\w.]+).*?SELECT\\s+(.+?)(?:WHERE|$)", 
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    
    private static final Pattern SELECT_INTO_PATTERN = Pattern.compile(
        "SELECT\\s+(.+?)\\s+INTO\\s+([\\w.]+).*?FROM\\s+(.+)", 
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    @Override
    public String getStrategy() {
        return "SQL_PARSE";
    }

    @Override
    public List<DataFlow> discoverLineage(DataSource dataSource, Map<String, Object> config) {
        List<DataFlow> dataFlows = new ArrayList<>();

        try (Connection connection = getConnection(dataSource)) {
            // 1. 分析视图定义
            dataFlows.addAll(analyzeViews(connection, dataSource.getId()));

            // 2. 分析存储过程
            dataFlows.addAll(analyzeProcedures(connection, dataSource.getId()));

            // 3. 分析触发器
            dataFlows.addAll(analyzeTriggers(connection, dataSource.getId()));

            // 4. 分析SQL日志（如果可用）
            dataFlows.addAll(analyzeSqlLogs(connection, dataSource.getId()));

        } catch (Exception e) {
            log.error("SQL解析血缘发现失败", e);
        }

        return dataFlows;
    }

    /**
     * 分析视图定义中的血缘关系
     */
    private List<DataFlow> analyzeViews(Connection connection, Long dataSourceId) throws SQLException {
        List<DataFlow> dataFlows = new ArrayList<>();
        
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet views = metaData.getTables(null, null, "%", new String[]{"VIEW"});
        
        while (views.next()) {
            String viewName = views.getString("TABLE_NAME");
            String viewDefinition = getViewDefinition(connection, viewName);
            
            if (viewDefinition != null) {
                dataFlows.addAll(parseSqlLineage(viewDefinition, viewName, "VIEW", dataSourceId));
            }
        }
        
        return dataFlows;
    }

    /**
     * 分析存储过程中的血缘关系
     */
    private List<DataFlow> analyzeProcedures(Connection connection, Long dataSourceId) throws SQLException {
        List<DataFlow> dataFlows = new ArrayList<>();
        
        // 获取存储过程列表（不同数据库语法不同）
        String procedureSql = getProcedureListSql(connection.getMetaData().getDatabaseProductName());
        
        if (procedureSql != null) {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(procedureSql)) {
                
                while (rs.next()) {
                    String procedureName = rs.getString("PROCEDURE_NAME");
                    String procedureDefinition = getProcedureDefinition(connection, procedureName);
                    
                    if (procedureDefinition != null) {
                        dataFlows.addAll(parseSqlLineage(procedureDefinition, procedureName, "PROCEDURE", dataSourceId));
                    }
                }
            }
        }
        
        return dataFlows;
    }

    /**
     * 分析触发器中的血缘关系
     */
    private List<DataFlow> analyzeTriggers(Connection connection, Long dataSourceId) throws SQLException {
        List<DataFlow> dataFlows = new ArrayList<>();
        
        // 获取触发器列表
        String triggerSql = getTriggerListSql(connection.getMetaData().getDatabaseProductName());
        
        if (triggerSql != null) {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(triggerSql)) {
                
                while (rs.next()) {
                    String triggerName = rs.getString("TRIGGER_NAME");
                    String triggerDefinition = getTriggerDefinition(connection, triggerName);
                    
                    if (triggerDefinition != null) {
                        dataFlows.addAll(parseSqlLineage(triggerDefinition, triggerName, "TRIGGER", dataSourceId));
                    }
                }
            }
        }
        
        return dataFlows;
    }

    /**
     * 分析SQL日志中的血缘关系
     */
    private List<DataFlow> analyzeSqlLogs(Connection connection, Long dataSourceId) {
        List<DataFlow> dataFlows = new ArrayList<>();
        
        // 这里可以实现从数据库日志表中分析SQL语句的逻辑
        // 需要数据库开启查询日志功能
        
        return dataFlows;
    }

    /**
     * 解析SQL语句中的血缘关系
     */
    private List<DataFlow> parseSqlLineage(String sql, String targetObject, String objectType, Long dataSourceId) {
        List<DataFlow> dataFlows = new ArrayList<>();
        
        try {
            // 简化的SQL解析 - 在实际应用中应该使用完整的ANTLR语法解析器
            
            // 1. 解析CREATE VIEW语句
            Matcher viewMatcher = CREATE_VIEW_PATTERN.matcher(sql);
            if (viewMatcher.find()) {
                String viewName = viewMatcher.group(1);
                String selectStatement = viewMatcher.group(2);
                dataFlows.addAll(parseSelectStatement(selectStatement, viewName, dataSourceId));
            }
            
            // 2. 解析INSERT SELECT语句
            Matcher insertMatcher = INSERT_SELECT_PATTERN.matcher(sql);
            if (insertMatcher.find()) {
                String targetTable = insertMatcher.group(1);
                String selectStatement = insertMatcher.group(2);
                dataFlows.addAll(parseSelectStatement(selectStatement, targetTable, dataSourceId));
            }
            
            // 3. 解析SELECT INTO语句
            Matcher selectIntoMatcher = SELECT_INTO_PATTERN.matcher(sql);
            if (selectIntoMatcher.find()) {
                String targetTable = selectIntoMatcher.group(2);
                String selectStatement = selectIntoMatcher.group(1) + " FROM " + selectIntoMatcher.group(3);
                dataFlows.addAll(parseSelectStatement(selectStatement, targetTable, dataSourceId));
            }
            
        } catch (Exception e) {
            log.error("解析SQL语句失败: {}", sql, e);
        }
        
        return dataFlows;
    }

    /**
     * 解析SELECT语句中的表和字段关系
     */
    private List<DataFlow> parseSelectStatement(String selectStatement, String targetTable, Long dataSourceId) {
        List<DataFlow> dataFlows = new ArrayList<>();
        
        // 简化的SELECT解析 - 提取FROM子句中的表
        Pattern fromPattern = Pattern.compile("FROM\\s+([\\w.,\\s]+?)(?:WHERE|GROUP\\s+BY|ORDER\\s+BY|$)", Pattern.CASE_INSENSITIVE);
        Matcher fromMatcher = fromPattern.matcher(selectStatement);
        
        if (fromMatcher.find()) {
            String fromClause = fromMatcher.group(1);
            String[] sourceTables = fromClause.split(",");
            
            for (String sourceTable : sourceTables) {
                sourceTable = sourceTable.trim();
                
                // 解析字段映射关系
                Map<String, String> fieldMappings = parseFieldMappings(selectStatement);
                
                for (Map.Entry<String, String> entry : fieldMappings.entrySet()) {
                    DataFlow dataFlow = DataFlow.builder()
                        .sourceTable(sourceTable)
                        .sourceColumn(entry.getKey())
                        .targetTable(targetTable)
                        .targetColumn(entry.getValue())
                        .flowType("DIRECT")
                        .confidence(new java.math.BigDecimal("0.95"))
                        .discoveryTime(java.time.LocalDateTime.now())
                        .lastUpdated(java.time.LocalDateTime.now())
                        .transformation("SELECT语句映射")
                        .dataSourceId(dataSourceId)
                        .discoveryMethod("SQL_PARSE")
                        .build();
                    
                    dataFlows.add(dataFlow);
                }
            }
        }
        
        return dataFlows;
    }

    /**
     * 解析字段映射关系
     */
    private Map<String, String> parseFieldMappings(String selectStatement) {
        Map<String, String> fieldMappings = new HashMap<>();
        
        // 提取SELECT和FROM之间的字段列表
        Pattern selectPattern = Pattern.compile("SELECT\\s+(.+?)\\s+FROM", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher selectMatcher = selectPattern.matcher(selectStatement);
        
        if (selectMatcher.find()) {
            String fieldList = selectMatcher.group(1);
            String[] fields = fieldList.split(",");
            
            for (String field : fields) {
                field = field.trim();
                
                // 处理字段别名
                if (field.contains(" as ") || field.contains(" AS ")) {
                    String[] parts = field.split("\\s+(?:as|AS)\\s+");
                    if (parts.length == 2) {
                        fieldMappings.put(parts[0].trim(), parts[1].trim());
                    }
                } else {
                    // 没有别名，字段名就是目标字段名
                    fieldMappings.put(field, field);
                }
            }
        }
        
        return fieldMappings;
    }

    /**
     * 获取数据库连接
     * 注意：此功能需要根据实际数据源配置实现
     */
    private Connection getConnection(DataSource dataSource) throws SQLException {
        if (dataSource == null) {
            throw new SQLException("数据源不能为空");
        }

        String config = dataSource.getConnectionConfig();
        if (config == null || config.trim().isEmpty()) {
            throw new SQLException("数据源连接配置不能为空，数据源ID: " + dataSource.getId());
        }

        // TODO: 实际实现需要根据数据源类型创建连接
        // 需要解析JSON配置（包含url, username, password等）
        // 并使用DriverManager或DataSource创建连接

        throw new SQLException("SQL血缘发现引擎的数据库连接功能尚未实现，请配置数据源连接信息或使用其他发现策略。数据源ID: " + dataSource.getId());
    }

    /**
     * 获取视图定义
     */
    private String getViewDefinition(Connection connection, String viewName) {
        // 根据数据库类型获取视图定义
        try {
            String productName = connection.getMetaData().getDatabaseProductName();
            
            if ("MySQL".equalsIgnoreCase(productName)) {
                String sql = "SHOW CREATE VIEW " + viewName;
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    if (rs.next()) {
                        return rs.getString("Create View");
                    }
                }
            }
            // 其他数据库的实现...
            
        } catch (SQLException e) {
            log.error("获取视图定义失败: {}", viewName, e);
        }
        
        return null;
    }

    /**
     * 获取存储过程列表SQL
     */
    private String getProcedureListSql(String databaseProductName) {
        if ("MySQL".equalsIgnoreCase(databaseProductName)) {
            return "SELECT ROUTINE_NAME AS PROCEDURE_NAME FROM INFORMATION_SCHEMA.ROUTINES WHERE ROUTINE_TYPE = 'PROCEDURE'";
        } else if ("Oracle".equalsIgnoreCase(databaseProductName)) {
            return "SELECT OBJECT_NAME AS PROCEDURE_NAME FROM USER_OBJECTS WHERE OBJECT_TYPE = 'PROCEDURE'";
        }
        return null;
    }

    /**
     * 获取触发器列表SQL
     */
    private String getTriggerListSql(String databaseProductName) {
        if ("MySQL".equalsIgnoreCase(databaseProductName)) {
            return "SELECT TRIGGER_NAME FROM INFORMATION_SCHEMA.TRIGGERS";
        }
        return null;
    }

    /**
     * 获取存储过程定义
     */
    private String getProcedureDefinition(Connection connection, String procedureName) {
        // 根据数据库类型获取存储过程定义
        return null;
    }

    /**
     * 获取触发器定义
     */
    private String getTriggerDefinition(Connection connection, String triggerName) {
        // 根据数据库类型获取触发器定义
        return null;
    }
}