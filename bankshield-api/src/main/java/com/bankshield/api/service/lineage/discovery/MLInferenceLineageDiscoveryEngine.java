package com.bankshield.api.service.lineage.discovery;

import com.bankshield.api.entity.DataFlow;
import com.bankshield.api.entity.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 机器学习关系推断引擎
 * 使用机器学习算法推断数据字段之间的潜在关系
 */
@Slf4j
@Component
public class MLInferenceLineageDiscoveryEngine implements LineageDiscoveryEngine {

    // 样本数据采样大小
    private static final int SAMPLE_SIZE = 1000;
    
    // 相似度阈值
    private static final double SIMILARITY_THRESHOLD = 0.7;
    
    // 相关性阈值
    private static final double CORRELATION_THRESHOLD = 0.6;

    @Override
    public String getStrategy() {
        return "ML_INFERENCE";
    }

    @Override
    public List<DataFlow> discoverLineage(DataSource dataSource, Map<String, Object> config) {
        List<DataFlow> dataFlows = new ArrayList<>();
        
        try {
            Connection connection = getConnection(dataSource);
            
            // 1. 数据采样和特征提取
            Map<String, TableDataSample> dataSamples = collectDataSamples(connection, config);
            
            // 2. 数值型字段相关性分析
            dataFlows.addAll(analyzeNumericCorrelations(dataSamples, dataSource.getId()));
            
            // 3. 字符串相似性分析
            dataFlows.addAll(analyzeStringSimilarity(dataSamples, dataSource.getId()));
            
            // 4. 时间序列相关性分析
            dataFlows.addAll(analyzeTemporalCorrelations(dataSamples, dataSource.getId()));
            
            // 5. 聚类分析发现潜在关系
            dataFlows.addAll(analyzeClusteringRelationships(dataSamples, dataSource.getId()));
            
            // 6. 主键-外键关系推断
            dataFlows.addAll(inferPrimaryKeyRelationships(dataSamples, dataSource.getId()));
            
            connection.close();
            
        } catch (Exception e) {
            log.error("机器学习关系推断失败", e);
        }
        
        return dataFlows;
    }

    /**
     * 收集数据样本
     */
    private Map<String, TableDataSample> collectDataSamples(Connection connection, Map<String, Object> config) throws SQLException {
        Map<String, TableDataSample> samples = new HashMap<>();
        
        DatabaseMetaData metaData = connection.getMetaData();
        
        // 获取所有表
        ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
        
        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            String schemaName = tables.getString("TABLE_SCHEM");
            
            // 采样限制
            if (samples.size() >= 50) { // 限制处理的表数量
                break;
            }
            
            TableDataSample sample = new TableDataSample();
            sample.tableName = tableName;
            sample.schemaName = schemaName;
            
            // 获取列信息
            ResultSet columns = metaData.getColumns(null, schemaName, tableName, "%");
            while (columns.next()) {
                ColumnSample columnSample = new ColumnSample();
                columnSample.columnName = columns.getString("COLUMN_NAME");
                columnSample.dataType = columns.getString("TYPE_NAME");
                columnSample.isNullable = columns.getString("IS_NULLABLE").equals("YES");
                sample.columns.add(columnSample);
            }
            columns.close();
            
            // 采样数据
            sampleDataFromTable(connection, sample);
            
            samples.put(tableName, sample);
        }
        
        tables.close();
        return samples;
    }

    /**
     * 从表中采样数据
     */
    private void sampleDataFromTable(Connection connection, TableDataSample sample) throws SQLException {
        // 构建采样SQL
        String sql = buildSampleSql(sample.tableName, sample.columns);
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Map<String, Object> rowData = new HashMap<>();
                
                for (ColumnSample column : sample.columns) {
                    Object value = rs.getObject(column.columnName);
                    rowData.put(column.columnName, value);
                    column.values.add(value);
                }
                
                sample.sampleData.add(rowData);
            }
        }
    }

    /**
     * 构建采样SQL
     */
    private String buildSampleSql(String tableName, List<ColumnSample> columns) {
        StringBuilder sql = new StringBuilder("SELECT ");
        
        // 选择所有列
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append(columns.get(i).columnName);
        }
        
        sql.append(" FROM ").append(tableName);
        sql.append(" ORDER BY RANDOM() LIMIT ").append(SAMPLE_SIZE);
        
        return sql.toString();
    }

    /**
     * 分析数值型字段相关性
     */
    private List<DataFlow> analyzeNumericCorrelations(Map<String, TableDataSample> dataSamples, Long dataSourceId) {
        List<DataFlow> dataFlows = new ArrayList<>();
        
        List<NumericColumn> numericColumns = extractNumericColumns(dataSamples);
        
        for (int i = 0; i < numericColumns.size(); i++) {
            for (int j = i + 1; j < numericColumns.size(); j++) {
                NumericColumn col1 = numericColumns.get(i);
                NumericColumn col2 = numericColumns.get(j);
                
                double correlation = calculatePearsonCorrelation(col1.values, col2.values);
                
                if (Math.abs(correlation) > CORRELATION_THRESHOLD) {
                    DataFlow dataFlow = DataFlow.builder()
                        .sourceTable(col1.tableName)
                        .sourceColumn(col1.columnName)
                        .targetTable(col2.tableName)
                        .targetColumn(col2.columnName)
                        .flowType(correlation > 0 ? "POSITIVE_CORRELATION" : "NEGATIVE_CORRELATION")
                        .confidence(new BigDecimal(Math.abs(correlation)))
                        .discoveryTime(LocalDateTime.now())
                        .lastUpdated(LocalDateTime.now())
                        .transformation("数值相关性分析: r=" + String.format("%.3f", correlation))
                        .dataSourceId(dataSourceId)
                        .discoveryMethod("ML_INFERENCE")
                        .build();
                    
                    dataFlows.add(dataFlow);
                }
            }
        }
        
        return dataFlows;
    }

    /**
     * 分析字符串相似性
     */
    private List<DataFlow> analyzeStringSimilarity(Map<String, TableDataSample> dataSamples, Long dataSourceId) {
        List<DataFlow> dataFlows = new ArrayList<>();
        
        List<StringColumn> stringColumns = extractStringColumns(dataSamples);
        
        for (int i = 0; i < stringColumns.size(); i++) {
            for (int j = i + 1; j < stringColumns.size(); j++) {
                StringColumn col1 = stringColumns.get(i);
                StringColumn col2 = stringColumns.get(j);
                
                double similarity = calculateStringSetSimilarity(col1.values, col2.values);
                
                if (similarity > SIMILARITY_THRESHOLD) {
                    DataFlow dataFlow = DataFlow.builder()
                        .sourceTable(col1.tableName)
                        .sourceColumn(col1.columnName)
                        .targetTable(col2.tableName)
                        .targetColumn(col2.columnName)
                        .flowType("STRING_SIMILARITY")
                        .confidence(new BigDecimal(similarity))
                        .discoveryTime(LocalDateTime.now())
                        .lastUpdated(LocalDateTime.now())
                        .transformation("字符串相似性分析: " + String.format("%.3f", similarity))
                        .dataSourceId(dataSourceId)
                        .discoveryMethod("ML_INFERENCE")
                        .build();
                    
                    dataFlows.add(dataFlow);
                }
            }
        }
        
        return dataFlows;
    }

    /**
     * 分析时间序列相关性
     */
    private List<DataFlow> analyzeTemporalCorrelations(Map<String, TableDataSample> dataSamples, Long dataSourceId) {
        List<DataFlow> dataFlows = new ArrayList<>();
        
        List<TemporalColumn> temporalColumns = extractTemporalColumns(dataSamples);
        
        for (int i = 0; i < temporalColumns.size(); i++) {
            for (int j = i + 1; j < temporalColumns.size(); j++) {
                TemporalColumn col1 = temporalColumns.get(i);
                TemporalColumn col2 = temporalColumns.get(j);
                
                double temporalCorrelation = calculateTemporalCorrelation(col1.timestamps, col2.timestamps);
                
                if (temporalCorrelation > CORRELATION_THRESHOLD) {
                    DataFlow dataFlow = DataFlow.builder()
                        .sourceTable(col1.tableName)
                        .sourceColumn(col1.columnName)
                        .targetTable(col2.tableName)
                        .targetColumn(col2.columnName)
                        .flowType("TEMPORAL_CORRELATION")
                        .confidence(new BigDecimal(temporalCorrelation))
                        .discoveryTime(LocalDateTime.now())
                        .lastUpdated(LocalDateTime.now())
                        .transformation("时间序列相关性分析")
                        .dataSourceId(dataSourceId)
                        .discoveryMethod("ML_INFERENCE")
                        .build();
                    
                    dataFlows.add(dataFlow);
                }
            }
        }
        
        return dataFlows;
    }

    /**
     * 分析聚类关系
     */
    private List<DataFlow> analyzeClusteringRelationships(Map<String, TableDataSample> dataSamples, Long dataSourceId) {
        List<DataFlow> dataFlows = new ArrayList<>();
        
        // 简化的聚类分析 - 基于数值特征的K-means
        for (TableDataSample sample : dataSamples.values()) {
            List<NumericColumn> numericColumns = extractNumericColumnsFromTable(sample);
            
            if (numericColumns.size() >= 2) {
                // 执行简单的聚类分析
                Map<String, Integer> clusters = performSimpleClustering(numericColumns);
                
                // 同一聚类中的字段可能存在关联
                Map<Integer, List<String>> clusterMembers = new HashMap<>();
                for (Map.Entry<String, Integer> entry : clusters.entrySet()) {
                    clusterMembers.computeIfAbsent(entry.getValue(), k -> new ArrayList<>())
                        .add(entry.getKey());
                }
                
                for (List<String> members : clusterMembers.values()) {
                    if (members.size() >= 2) {
                        for (int i = 0; i < members.size() - 1; i++) {
                            for (int j = i + 1; j < members.size(); j++) {
                                String col1 = members.get(i);
                                String col2 = members.get(j);
                                
                                DataFlow dataFlow = DataFlow.builder()
                                    .sourceTable(sample.tableName)
                                    .sourceColumn(col1)
                                    .targetTable(sample.tableName)
                                    .targetColumn(col2)
                                    .flowType("CLUSTER_RELATIONSHIP")
                                    .confidence(new BigDecimal("0.65"))
                                    .discoveryTime(LocalDateTime.now())
                                    .lastUpdated(LocalDateTime.now())
                                    .transformation("聚类分析发现的关系")
                                    .dataSourceId(dataSourceId)
                                    .discoveryMethod("ML_INFERENCE")
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
     * 推断主键-外键关系
     */
    private List<DataFlow> inferPrimaryKeyRelationships(Map<String, TableDataSample> dataSamples, Long dataSourceId) {
        List<DataFlow> dataFlows = new ArrayList<>();
        
        // 识别潜在的主键列
        List<PotentialKeyColumn> potentialKeys = identifyPotentialPrimaryKeys(dataSamples);
        
        // 识别潜在的外键列
        List<PotentialKeyColumn> potentialForeignKeys = identifyPotentialForeignKeys(dataSamples, potentialKeys);
        
        // 建立主键-外键关系
        for (PotentialKeyColumn fk : potentialForeignKeys) {
            for (PotentialKeyColumn pk : potentialKeys) {
                if (couldBeForeignKey(fk, pk)) {
                    DataFlow dataFlow = DataFlow.builder()
                        .sourceTable(pk.tableName)
                        .sourceColumn(pk.columnName)
                        .targetTable(fk.tableName)
                        .targetColumn(fk.columnName)
                        .flowType("INFERRED_FK")
                        .confidence(new BigDecimal(fk.uniquenessScore * 0.8))
                        .discoveryTime(LocalDateTime.now())
                        .lastUpdated(LocalDateTime.now())
                        .transformation("机器学习推断的外键关系")
                        .dataSourceId(dataSourceId)
                        .discoveryMethod("ML_INFERENCE")
                        .build();
                    
                    dataFlows.add(dataFlow);
                }
            }
        }
        
        return dataFlows;
    }

    /**
     * 提取数值型列
     */
    private List<NumericColumn> extractNumericColumns(Map<String, TableDataSample> dataSamples) {
        List<NumericColumn> numericColumns = new ArrayList<>();
        
        for (TableDataSample sample : dataSamples.values()) {
            for (ColumnSample column : sample.columns) {
                if (isNumericType(column.dataType) && column.values.size() > 10) {
                    NumericColumn numericColumn = new NumericColumn();
                    numericColumn.tableName = sample.tableName;
                    numericColumn.columnName = column.columnName;
                    
                    for (Object value : column.values) {
                        if (value != null) {
                            try {
                                numericColumn.values.add(Double.parseDouble(value.toString()));
                            } catch (NumberFormatException e) {
                                // 跳过非数值数据
                            }
                        }
                    }
                    
                    if (numericColumn.values.size() > 5) {
                        numericColumns.add(numericColumn);
                    }
                }
            }
        }
        
        return numericColumns;
    }

    /**
     * 提取字符串列
     */
    private List<StringColumn> extractStringColumns(Map<String, TableDataSample> dataSamples) {
        List<StringColumn> stringColumns = new ArrayList<>();
        
        for (TableDataSample sample : dataSamples.values()) {
            for (ColumnSample column : sample.columns) {
                if (isStringType(column.dataType)) {
                    StringColumn stringColumn = new StringColumn();
                    stringColumn.tableName = sample.tableName;
                    stringColumn.columnName = column.columnName;
                    
                    for (Object value : column.values) {
                        if (value != null) {
                            stringColumn.values.add(value.toString());
                        }
                    }
                    
                    if (stringColumn.values.size() > 5) {
                        stringColumns.add(stringColumn);
                    }
                }
            }
        }
        
        return stringColumns;
    }

    /**
     * 提取时间列
     */
    private List<TemporalColumn> extractTemporalColumns(Map<String, TableDataSample> dataSamples) {
        List<TemporalColumn> temporalColumns = new ArrayList<>();
        
        for (TableDataSample sample : dataSamples.values()) {
            for (ColumnSample column : sample.columns) {
                if (isTemporalType(column.dataType)) {
                    TemporalColumn temporalColumn = new TemporalColumn();
                    temporalColumn.tableName = sample.tableName;
                    temporalColumn.columnName = column.columnName;
                    
                    for (Object value : column.values) {
                        if (value != null) {
                            try {
                                if (value instanceof Timestamp) {
                                    temporalColumn.timestamps.add(((Timestamp) value).getTime());
                                } else if (value instanceof java.util.Date) {
                                    temporalColumn.timestamps.add(((java.util.Date) value).getTime());
                                } else {
                                    // 尝试解析字符串时间
                                    temporalColumn.timestamps.add(parseDateTime(value.toString()));
                                }
                            } catch (Exception e) {
                                log.warn("解析时间数据失败: {}", value, e);
                            }
                        }
                    }
                    
                    if (temporalColumn.timestamps.size() > 5) {
                        temporalColumns.add(temporalColumn);
                    }
                }
            }
        }
        
        return temporalColumns;
    }

    /**
     * 计算皮尔逊相关系数
     */
    private double calculatePearsonCorrelation(List<Double> x, List<Double> y) {
        if (x.size() != y.size() || x.size() < 2) {
            return 0.0;
        }
        
        double sumX = 0.0, sumY = 0.0, sumXY = 0.0;
        double sumX2 = 0.0, sumY2 = 0.0;
        int n = x.size();
        
        for (int i = 0; i < n; i++) {
            double xi = x.get(i);
            double yi = y.get(i);
            
            sumX += xi;
            sumY += yi;
            sumXY += xi * yi;
            sumX2 += xi * xi;
            sumY2 += yi * yi;
        }
        
        double numerator = n * sumXY - sumX * sumY;
        double denominator = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));
        
        return denominator == 0 ? 0.0 : numerator / denominator;
    }

    /**
     * 计算字符串集合相似性
     */
    private double calculateStringSetSimilarity(List<String> set1, List<String> set2) {
        Set<String> unique1 = new HashSet<>(set1);
        Set<String> unique2 = new HashSet<>(set2);
        
        Set<String> intersection = new HashSet<>(unique1);
        intersection.retainAll(unique2);
        
        Set<String> union = new HashSet<>(unique1);
        union.addAll(unique2);
        
        if (union.isEmpty()) {
            return 0.0;
        }
        
        return (double) intersection.size() / union.size();
    }

    /**
     * 计算时间相关性
     */
    private double calculateTemporalCorrelation(List<Long> timestamps1, List<Long> timestamps2) {
        if (timestamps1.size() != timestamps2.size() || timestamps1.size() < 2) {
            return 0.0;
        }
        
        // 简单的时间模式相似性：检查时间间隔的相似性
        List<Double> intervals1 = calculateTimeIntervals(timestamps1);
        List<Double> intervals2 = calculateTimeIntervals(timestamps2);
        
        if (intervals1.size() != intervals2.size()) {
            return 0.0;
        }
        
        return calculatePearsonCorrelation(intervals1, intervals2);
    }

    /**
     * 计算时间间隔
     */
    private List<Double> calculateTimeIntervals(List<Long> timestamps) {
        List<Double> intervals = new ArrayList<>();
        
        Collections.sort(timestamps);
        
        for (int i = 1; i < timestamps.size(); i++) {
            double interval = (timestamps.get(i) - timestamps.get(i - 1)) / 1000.0; // 转换为秒
            intervals.add(interval);
        }
        
        return intervals;
    }

    /**
     * 执行简单聚类
     */
    private Map<String, Integer> performSimpleClustering(List<NumericColumn> columns) {
        Map<String, Integer> clusters = new HashMap<>();
        
        // 简化的K-means聚类算法
        int k = Math.min(3, columns.size() / 2); // 最多3个聚类
        
        if (k < 2) {
            // 如果聚类数太少，直接返回
            for (NumericColumn column : columns) {
                clusters.put(column.columnName, 0);
            }
            return clusters;
        }
        
        // 提取特征：均值、标准差、最小值、最大值
        double[][] features = new double[columns.size()][4];
        for (int i = 0; i < columns.size(); i++) {
            NumericColumn col = columns.get(i);
            features[i][0] = calculateMean(col.values);
            features[i][1] = calculateStdDev(col.values);
            features[i][2] = Collections.min(col.values);
            features[i][3] = Collections.max(col.values);
        }
        
        // 执行K-means聚类
        int[] assignments = kMeansClustering(features, k);
        
        for (int i = 0; i < columns.size(); i++) {
            clusters.put(columns.get(i).columnName, assignments[i]);
        }
        
        return clusters;
    }

    /**
     * K-means聚类算法
     */
    private int[] kMeansClustering(double[][] data, int k) {
        int n = data.length;
        int[] assignments = new int[n];
        
        // 随机初始化聚类中心
        Random random = new Random();
        double[][] centroids = new double[k][data[0].length];
        for (int i = 0; i < k; i++) {
            int randomIndex = random.nextInt(n);
            System.arraycopy(data[randomIndex], 0, centroids[i], 0, data[0].length);
        }
        
        // 迭代优化
        for (int iter = 0; iter < 100; iter++) {
            // 分配数据点到最近的聚类中心
            for (int i = 0; i < n; i++) {
                int bestCluster = 0;
                double minDistance = Double.MAX_VALUE;
                
                for (int j = 0; j < k; j++) {
                    double distance = euclideanDistance(data[i], centroids[j]);
                    if (distance < minDistance) {
                        minDistance = distance;
                        bestCluster = j;
                    }
                }
                
                assignments[i] = bestCluster;
            }
            
            // 更新聚类中心
            double[][] newCentroids = new double[k][data[0].length];
            int[] counts = new int[k];
            
            for (int i = 0; i < n; i++) {
                int cluster = assignments[i];
                for (int j = 0; j < data[i].length; j++) {
                    newCentroids[cluster][j] += data[i][j];
                }
                counts[cluster]++;
            }
            
            for (int i = 0; i < k; i++) {
                if (counts[i] > 0) {
                    for (int j = 0; j < newCentroids[i].length; j++) {
                        newCentroids[i][j] /= counts[i];
                    }
                }
            }
            
            centroids = newCentroids;
        }
        
        return assignments;
    }

    /**
     * 计算欧几里得距离
     */
    private double euclideanDistance(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    /**
     * 计算均值
     */
    private double calculateMean(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    /**
     * 计算标准差
     */
    private double calculateStdDev(List<Double> values) {
        if (values.size() < 2) return 0.0;
        
        double mean = calculateMean(values);
        double variance = values.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0.0);
        
        return Math.sqrt(variance);
    }

    /**
     * 识别潜在主键
     */
    private List<PotentialKeyColumn> identifyPotentialPrimaryKeys(Map<String, TableDataSample> dataSamples) {
        List<PotentialKeyColumn> potentialKeys = new ArrayList<>();
        
        for (TableDataSample sample : dataSamples.values()) {
            for (ColumnSample column : sample.columns) {
                if (column.values.size() > 0) {
                    double uniqueness = calculateUniqueness(column.values);
                    
                    if (uniqueness > 0.9) { // 高唯一性
                        PotentialKeyColumn pk = new PotentialKeyColumn();
                        pk.tableName = sample.tableName;
                        pk.columnName = column.columnName;
                        pk.uniquenessScore = uniqueness;
                        pk.dataType = column.dataType;
                        potentialKeys.add(pk);
                    }
                }
            }
        }
        
        return potentialKeys;
    }

    /**
     * 识别潜在外键
     */
    private List<PotentialKeyColumn> identifyPotentialForeignKeys(Map<String, TableDataSample> dataSamples, 
                                                                   List<PotentialKeyColumn> potentialKeys) {
        List<PotentialKeyColumn> potentialForeignKeys = new ArrayList<>();
        
        for (TableDataSample sample : dataSamples.values()) {
            for (ColumnSample column : sample.columns) {
                if (column.values.size() > 0) {
                    double uniqueness = calculateUniqueness(column.values);
                    
                    if (uniqueness < 0.8 && uniqueness > 0.1) { // 中等唯一性，可能是外键
                        PotentialKeyColumn fk = new PotentialKeyColumn();
                        fk.tableName = sample.tableName;
                        fk.columnName = column.columnName;
                        fk.uniquenessScore = uniqueness;
                        fk.dataType = column.dataType;
                        potentialForeignKeys.add(fk);
                    }
                }
            }
        }
        
        return potentialForeignKeys;
    }

    /**
     * 计算唯一性
     */
    private double calculateUniqueness(List<Object> values) {
        if (values.isEmpty()) return 0.0;
        
        Set<Object> uniqueValues = new HashSet<>(values);
        return (double) uniqueValues.size() / values.size();
    }

    /**
     * 判断是否为外键关系
     */
    private boolean couldBeForeignKey(PotentialKeyColumn fk, PotentialKeyColumn pk) {
        // 数据类型应该兼容
        if (!areDataTypesCompatible(fk.dataType, pk.dataType)) {
            return false;
        }
        
        // 列名应该有相似性
        double nameSimilarity = calculateNameSimilarity(fk.columnName, pk.columnName);
        if (nameSimilarity < 0.3) {
            return false;
        }
        
        return true;
    }

    /**
     * 检查数据类型是否兼容
     */
    private boolean areDataTypesCompatible(String type1, String type2) {
        // 简化的类型兼容性检查
        Set<String> numericTypes = Set.of("INT", "BIGINT", "DECIMAL", "DOUBLE", "FLOAT", "NUMBER");
        Set<String> stringTypes = Set.of("VARCHAR", "CHAR", "TEXT", "STRING");
        Set<String> temporalTypes = Set.of("DATE", "TIMESTAMP", "DATETIME");
        
        if (numericTypes.contains(type1.toUpperCase()) && numericTypes.contains(type2.toUpperCase())) {
            return true;
        }
        
        if (stringTypes.contains(type1.toUpperCase()) && stringTypes.contains(type2.toUpperCase())) {
            return true;
        }
        
        if (temporalTypes.contains(type1.toUpperCase()) && temporalTypes.contains(type2.toUpperCase())) {
            return true;
        }
        
        return type1.equalsIgnoreCase(type2);
    }

    /**
     * 解析日期时间字符串
     */
    private long parseDateTime(String dateTimeStr) {
        try {
            // 尝试多种格式
            String[] patterns = {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd",
                "MM/dd/yyyy HH:mm:ss",
                "MM/dd/yyyy"
            };

            for (String pattern : patterns) {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(pattern);
                    java.util.Date parsedDate = sdf.parse(dateTimeStr);
                    return parsedDate.getTime();
                } catch (java.text.ParseException e) {
                    // 尝试下一个格式
                }
            }
        } catch (Exception e) {
            log.warn("无法解析日期时间: {}", dateTimeStr, e);
        }

        return System.currentTimeMillis();
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
     * 检查是否为数值类型
     */
    private boolean isNumericType(String dataType) {
        String upperType = dataType.toUpperCase();
        return upperType.contains("INT") || upperType.contains("DECIMAL") || 
               upperType.contains("DOUBLE") || upperType.contains("FLOAT") ||
               upperType.contains("NUMBER") || upperType.contains("REAL");
    }

    /**
     * 检查是否为字符串类型
     */
    private boolean isStringType(String dataType) {
        String upperType = dataType.toUpperCase();
        return upperType.contains("VARCHAR") || upperType.contains("CHAR") ||
               upperType.contains("TEXT") || upperType.contains("STRING");
    }

    /**
     * 检查是否为时间类型
     */
    private boolean isTemporalType(String dataType) {
        String upperType = dataType.toUpperCase();
        return upperType.contains("DATE") || upperType.contains("TIME") ||
               upperType.contains("TIMESTAMP");
    }

    /**
     * 数据样本类
     */
    private static class TableDataSample {
        String tableName;
        String schemaName;
        List<ColumnSample> columns = new ArrayList<>();
        List<Map<String, Object>> sampleData = new ArrayList<>();
    }

    /**
     * 列样本类
     */
    private static class ColumnSample {
        String columnName;
        String dataType;
        boolean isNullable;
        List<Object> values = new ArrayList<>();
    }

    /**
     * 数值列类
     */
    private static class NumericColumn {
        String tableName;
        String columnName;
        List<Double> values = new ArrayList<>();
    }

    /**
     * 字符串列类
     */
    private static class StringColumn {
        String tableName;
        String columnName;
        List<String> values = new ArrayList<>();
    }

    /**
     * 时间列类
     */
    private static class TemporalColumn {
        String tableName;
        String columnName;
        List<Long> timestamps = new ArrayList<>();
    }

    /**
     * 从表数据样本中提取数值列
     */
    private List<NumericColumn> extractNumericColumnsFromTable(TableDataSample sample) {
        List<NumericColumn> numericColumns = new ArrayList<>();
        for (ColumnSample column : sample.columns) {
            if (isNumericType(column.dataType)) {
                NumericColumn numericColumn = new NumericColumn();
                numericColumn.tableName = sample.tableName;
                numericColumn.columnName = column.columnName;
                for (Object value : column.values) {
                    if (value instanceof Number) {
                        numericColumn.values.add(((Number) value).doubleValue());
                    }
                }
                if (!numericColumn.values.isEmpty()) {
                    numericColumns.add(numericColumn);
                }
            }
        }
        return numericColumns;
    }

    /**
     * 计算两个列名之间的相似度
     */
    private double calculateNameSimilarity(String name1, String name2) {
        if (name1 == null || name2 == null) {
            return 0.0;
        }
        String lower1 = name1.toLowerCase();
        String lower2 = name2.toLowerCase();

        // 完全相同
        if (lower1.equals(lower2)) {
            return 1.0;
        }

        // 检查是否一个包含另一个
        if (lower1.contains(lower2) || lower2.contains(lower1)) {
            return 0.8;
        }

        // 检查常见的外键命名模式 (id, _id, fk_等)
        if (lower1.replace("_id", "").replace("id", "").equals(lower2.replace("_id", "").replace("id", ""))) {
            return 0.7;
        }

        // 简单的编辑距离相似度
        int maxLen = Math.max(lower1.length(), lower2.length());
        if (maxLen == 0) {
            return 1.0;
        }
        int distance = levenshteinDistance(lower1, lower2);
        return 1.0 - (double) distance / maxLen;
    }

    /**
     * 计算两个字符串之间的编辑距离
     */
    private int levenshteinDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]) + 1;
                }
            }
        }
        return dp[len1][len2];
    }

    /**
     * 潜在键列类
     */
    private static class PotentialKeyColumn {
        String tableName;
        String columnName;
        String dataType;
        double uniquenessScore;
    }
}