package com.bankshield.api.service.lineage.discovery;

import com.bankshield.api.entity.DataFlow;
import com.bankshield.api.entity.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日志分析血缘发现引擎
 * 通过分析数据库日志、ETL日志、应用日志等发现数据流转关系
 */
@Slf4j
@Component
public class LogAnalysisLineageDiscoveryEngine implements LineageDiscoveryEngine {

    // SQL语句模式
    private static final Pattern INSERT_PATTERN = Pattern.compile(
        "INSERT\\s+INTO\\s+([\\w.]+).*?VALUES.*?(\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2})", 
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    
    private static final Pattern UPDATE_PATTERN = Pattern.compile(
        "UPDATE\\s+([\\w.]+).*?SET.*?WHERE.*?(\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2})", 
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    
    private static final Pattern SELECT_INTO_PATTERN = Pattern.compile(
        "SELECT\\s+(.+?)\\s+INTO\\s+([\\w.]+).*?FROM\\s+([\\w.]+)", 
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    
    // ETL日志模式
    private static final Pattern ETL_SOURCE_PATTERN = Pattern.compile(
        "Source:\\s*([\\w.]+).*?Target:\\s*([\\w.]+)"
    );
    
    private static final Pattern ETL_MAPPING_PATTERN = Pattern.compile(
        "Mapping:\\s*([\\w.]+)\\s*->\\s*([\\w.]+)"
    );
    
    // 时间戳模式
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile(
        "(\\d{4}-\\d{2}-\\d{2}[\\sT]\\d{2}:\\d{2}:\\d{2}(?::\\d{2})?)"
    );

    @Override
    public String getStrategy() {
        return "LOG_ANALYSIS";
    }

    @Override
    public List<DataFlow> discoverLineage(DataSource dataSource, Map<String, Object> config) {
        List<DataFlow> dataFlows = new ArrayList<>();
        
        try {
            String logPath = (String) config.getOrDefault("logPath", "/var/log/database");
            String logType = (String) config.getOrDefault("logType", "DATABASE");
            String timeRange = (String) config.getOrDefault("timeRange", "24h");
            
            // 获取日志文件列表
            List<String> logFiles = getLogFiles(logPath, timeRange);
            
            for (String logFile : logFiles) {
                log.info("分析日志文件: {}", logFile);
                
                switch (logType.toUpperCase()) {
                    case "DATABASE":
                        dataFlows.addAll(analyzeDatabaseLog(logFile, dataSource.getId()));
                        break;
                    case "ETL":
                        dataFlows.addAll(analyzeEtlLog(logFile, dataSource.getId()));
                        break;
                    case "APPLICATION":
                        dataFlows.addAll(analyzeApplicationLog(logFile, dataSource.getId()));
                        break;
                    default:
                        log.warn("不支持的日志类型: {}", logType);
                }
            }
            
        } catch (Exception e) {
            log.error("日志分析血缘发现失败", e);
        }
        
        return dataFlows;
    }

    /**
     * 分析数据库日志
     */
    private List<DataFlow> analyzeDatabaseLog(String logFile, Long dataSourceId) throws IOException {
        List<DataFlow> dataFlows = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            StringBuilder currentStatement = new StringBuilder();
            
            while ((line = reader.readLine()) != null) {
                // 检查是否是SQL语句的开始
                if (isSqlStatementStart(line)) {
                    // 处理之前的语句
                    if (currentStatement.length() > 0) {
                        dataFlows.addAll(parseSqlStatement(currentStatement.toString(), dataSourceId));
                        currentStatement.setLength(0);
                    }
                }
                
                currentStatement.append(line).append("\n");
                
                // 检查语句是否完整
                if (isSqlStatementComplete(currentStatement.toString())) {
                    dataFlows.addAll(parseSqlStatement(currentStatement.toString(), dataSourceId));
                    currentStatement.setLength(0);
                }
            }
            
            // 处理最后一条语句
            if (currentStatement.length() > 0) {
                dataFlows.addAll(parseSqlStatement(currentStatement.toString(), dataSourceId));
            }
        }
        
        return dataFlows;
    }

    /**
     * 分析ETL日志
     */
    private List<DataFlow> analyzeEtlLog(String logFile, Long dataSourceId) throws IOException {
        List<DataFlow> dataFlows = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            String currentJob = null;
            String currentSource = null;
            String currentTarget = null;
            
            while ((line = reader.readLine()) != null) {
                // 提取作业名称
                if (line.contains("Job started") || line.contains("ETL Job")) {
                    Pattern jobPattern = Pattern.compile("Job:\\s*([\\w_]+)");
                    Matcher jobMatcher = jobPattern.matcher(line);
                    if (jobMatcher.find()) {
                        currentJob = jobMatcher.group(1);
                    }
                }
                
                // 提取源和目标信息
                Matcher sourceMatcher = ETL_SOURCE_PATTERN.matcher(line);
                if (sourceMatcher.find()) {
                    currentSource = sourceMatcher.group(1);
                    currentTarget = sourceMatcher.group(2);
                }
                
                // 提取字段映射关系
                Matcher mappingMatcher = ETL_MAPPING_PATTERN.matcher(line);
                if (mappingMatcher.find() && currentSource != null && currentTarget != null) {
                    String sourceField = mappingMatcher.group(1);
                    String targetField = mappingMatcher.group(2);
                    
                    DataFlow dataFlow = DataFlow.builder()
                        .sourceTable(currentSource)
                        .sourceColumn(sourceField)
                        .targetTable(currentTarget)
                        .targetColumn(targetField)
                        .flowType("TRANSFORMATION")
                        .confidence(new java.math.BigDecimal("0.90"))
                        .discoveryTime(LocalDateTime.now())
                        .lastUpdated(LocalDateTime.now())
                        .transformation("ETL作业: " + currentJob)
                        .dataSourceId(dataSourceId)
                        .discoveryMethod("LOG_ANALYSIS")
                        .build();
                    
                    dataFlows.add(dataFlow);
                }
            }
        }
        
        return dataFlows;
    }

    /**
     * 分析应用日志
     */
    private List<DataFlow> analyzeApplicationLog(String logFile, Long dataSourceId) throws IOException {
        List<DataFlow> dataFlows = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                // 查找SQL语句
                if (line.contains("INSERT") || line.contains("UPDATE") || line.contains("SELECT")) {
                    dataFlows.addAll(parseApplicationSqlLog(line, dataSourceId));
                }
                
                // 查找API调用日志
                if (line.contains("API") && line.contains("->")) {
                    dataFlows.addAll(parseApiLog(line, dataSourceId));
                }
            }
        }
        
        return dataFlows;
    }

    /**
     * 解析应用SQL日志
     */
    private List<DataFlow> parseApplicationSqlLog(String logLine, Long dataSourceId) {
        List<DataFlow> dataFlows = new ArrayList<>();
        
        // 提取SQL语句
        Pattern sqlPattern = Pattern.compile("(INSERT|UPDATE|SELECT).*?(?:;|\\n)");
        Matcher sqlMatcher = sqlPattern.matcher(logLine);
        
        while (sqlMatcher.find()) {
            String sqlStatement = sqlMatcher.group();
            dataFlows.addAll(parseSqlStatement(sqlStatement, dataSourceId));
        }
        
        return dataFlows;
    }

    /**
     * 解析API日志
     */
    private List<DataFlow> parseApiLog(String logLine, Long dataSourceId) {
        List<DataFlow> dataFlows = new ArrayList<>();
        
        // 提取API调用关系
        Pattern apiPattern = Pattern.compile("(\\w+)\\s*->\\s*(\\w+)");
        Matcher apiMatcher = apiPattern.matcher(logLine);
        
        while (apiMatcher.find()) {
            String sourceService = apiMatcher.group(1);
            String targetService = apiMatcher.group(2);
            
            DataFlow dataFlow = DataFlow.builder()
                .sourceTable(sourceService)
                .targetTable(targetService)
                .flowType("INDIRECT")
                .confidence(new java.math.BigDecimal("0.70"))
                .discoveryTime(LocalDateTime.now())
                .lastUpdated(LocalDateTime.now())
                .transformation("API调用关系")
                .dataSourceId(dataSourceId)
                .discoveryMethod("LOG_ANALYSIS")
                .build();
            
            dataFlows.add(dataFlow);
        }
        
        return dataFlows;
    }

    /**
     * 解析SQL语句
     */
    private List<DataFlow> parseSqlStatement(String sqlStatement, Long dataSourceId) {
        List<DataFlow> dataFlows = new ArrayList<>();
        
        // 解析INSERT语句
        Matcher insertMatcher = INSERT_PATTERN.matcher(sqlStatement);
        if (insertMatcher.find()) {
            String targetTable = insertMatcher.group(1);
            String timestamp = insertMatcher.group(2);
            
            // 尝试提取源表信息（如果有SELECT子句）
            Pattern selectFromPattern = Pattern.compile("FROM\\s+([\\w.]+)", Pattern.CASE_INSENSITIVE);
            Matcher selectFromMatcher = selectFromPattern.matcher(sqlStatement);
            
            if (selectFromMatcher.find()) {
                String sourceTable = selectFromMatcher.group(1);
                
                DataFlow dataFlow = DataFlow.builder()
                    .sourceTable(sourceTable)
                    .targetTable(targetTable)
                    .flowType("DIRECT")
                    .confidence(new java.math.BigDecimal("0.85"))
                    .discoveryTime(parseTimestamp(timestamp))
                    .lastUpdated(LocalDateTime.now())
                    .transformation("INSERT INTO SELECT")
                    .dataSourceId(dataSourceId)
                    .discoveryMethod("LOG_ANALYSIS")
                    .build();
                
                dataFlows.add(dataFlow);
            }
        }
        
        // 解析UPDATE语句
        Matcher updateMatcher = UPDATE_PATTERN.matcher(sqlStatement);
        if (updateMatcher.find()) {
            String tableName = updateMatcher.group(1);
            String timestamp = updateMatcher.group(2);
            
            // UPDATE语句通常表示内部数据流转，可以记录为自引用
            DataFlow dataFlow = DataFlow.builder()
                .sourceTable(tableName)
                .targetTable(tableName)
                .flowType("TRANSFORMATION")
                .confidence(new java.math.BigDecimal("0.80"))
                .discoveryTime(parseTimestamp(timestamp))
                .lastUpdated(LocalDateTime.now())
                .transformation("UPDATE操作")
                .dataSourceId(dataSourceId)
                .discoveryMethod("LOG_ANALYSIS")
                .build();
            
            dataFlows.add(dataFlow);
        }
        
        // 解析SELECT INTO语句
        Matcher selectIntoMatcher = SELECT_INTO_PATTERN.matcher(sqlStatement);
        if (selectIntoMatcher.find()) {
            String sourceTable = selectIntoMatcher.group(3);
            String targetTable = selectIntoMatcher.group(2);
            
            DataFlow dataFlow = DataFlow.builder()
                .sourceTable(sourceTable)
                .targetTable(targetTable)
                .flowType("DIRECT")
                .confidence(new java.math.BigDecimal("0.90"))
                .discoveryTime(LocalDateTime.now())
                .lastUpdated(LocalDateTime.now())
                .transformation("SELECT INTO")
                .dataSourceId(dataSourceId)
                .discoveryMethod("LOG_ANALYSIS")
                .build();
            
            dataFlows.add(dataFlow);
        }
        
        return dataFlows;
    }

    /**
     * 检查是否是SQL语句的开始
     */
    private boolean isSqlStatementStart(String line) {
        return line.trim().matches("(?i)^(INSERT|UPDATE|DELETE|SELECT|CREATE|DROP|ALTER)\\s+.*");
    }

    /**
     * 检查SQL语句是否完整
     */
    private boolean isSqlStatementComplete(String statement) {
        String trimmed = statement.trim();
        return trimmed.endsWith(";") || trimmed.endsWith("\\n") || trimmed.endsWith("\\r\\n");
    }

    /**
     * 获取日志文件列表
     */
    private List<String> getLogFiles(String logPath, String timeRange) {
        List<String> logFiles = new ArrayList<>();
        
        try {
            Path path = Paths.get(logPath);
            if (Files.isDirectory(path)) {
                // 获取目录下的所有日志文件
                Files.list(path)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".log"))
                    .filter(p -> isFileInTimeRange(p, timeRange))
                    .forEach(p -> logFiles.add(p.toString()));
            } else if (Files.isRegularFile(path)) {
                logFiles.add(path.toString());
            }
        } catch (IOException e) {
            log.error("获取日志文件列表失败", e);
        }
        
        return logFiles;
    }

    /**
     * 检查文件是否在时间范围内
     */
    private boolean isFileInTimeRange(Path file, String timeRange) {
        try {
            LocalDateTime fileTime = Files.getLastModifiedTime(file).toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
            
            LocalDateTime now = LocalDateTime.now();
            
            // 解析时间范围
            switch (timeRange.toLowerCase()) {
                case "1h":
                    return fileTime.isAfter(now.minusHours(1));
                case "24h":
                    return fileTime.isAfter(now.minusHours(24));
                case "7d":
                    return fileTime.isAfter(now.minusDays(7));
                case "30d":
                    return fileTime.isAfter(now.minusDays(30));
                default:
                    return true;
            }
        } catch (IOException e) {
            log.error("检查文件时间范围失败", e);
            return false;
        }
    }

    /**
     * 解析时间戳
     */
    private LocalDateTime parseTimestamp(String timestamp) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(timestamp, formatter);
        } catch (Exception e) {
            log.warn("解析时间戳失败: {}", timestamp, e);
            return LocalDateTime.now();
        }
    }
}