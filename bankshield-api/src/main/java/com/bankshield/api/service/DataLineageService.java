package com.bankshield.api.service;

import com.bankshield.api.entity.DataLineage;
import com.bankshield.common.result.Result;

import java.util.List;

/**
 * 数据血缘分析服务接口
 * 
 * @author BankShield
 */
public interface DataLineageService {

    /**
     * 解析SQL，构建数据血缘
     * 
     * @param sql SQL语句
     * @param dataSource 数据源名称
     * @return 解析结果
     */
    Result<String> parseLineage(String sql, String dataSource);

    /**
     * 查询字段血缘链路
     * 
     * @param tableName 表名
     * @param fieldName 字段名
     * @return 血缘链路
     */
    Result<List<LineagePath>> getFieldLineage(String tableName, String fieldName);

    /**
     * 查询表的上游血缘
     * 
     * @param tableName 表名
     * @return 上游血缘关系
     */
    Result<List<DataLineage>> getUpstreamLineage(String tableName);

    /**
     * 查询表的下游血缘
     * 
     * @param tableName 表名
     * @return 下游血缘关系
     */
    Result<List<DataLineage>> getDownstreamLineage(String tableName);

    /**
     * 构建完整的血缘图谱
     * 
     * @param assetId 资产ID
     * @return 血缘图谱
     */
    Result<LineageGraph> buildLineageGraph(Long assetId);

    /**
     * 识别跨境、跨安全域传输
     * 
     * @param assetId 资产ID
     * @return 传输风险分析结果
     */
    Result<TransferRisk> analyzeTransferRisk(Long assetId);

    /**
     * 血缘路径信息
     */
    class LineagePath {
        private List<DataLineage> path;
        private Integer pathLength;
        private String sourceTable;
        private String targetTable;
        private List<String> intermediateTables;

        // 构造函数、getter、setter方法
        public LineagePath() {}

        public List<DataLineage> getPath() { return path; }
        public void setPath(List<DataLineage> path) { this.path = path; }
        
        public Integer getPathLength() { return pathLength; }
        public void setPathLength(Integer pathLength) { this.pathLength = pathLength; }
        
        public String getSourceTable() { return sourceTable; }
        public void setSourceTable(String sourceTable) { this.sourceTable = sourceTable; }
        
        public String getTargetTable() { return targetTable; }
        public void setTargetTable(String targetTable) { this.targetTable = targetTable; }
        
        public List<String> getIntermediateTables() { return intermediateTables; }
        public void setIntermediateTables(List<String> intermediateTables) { this.intermediateTables = intermediateTables; }
    }

    /**
     * 血缘图谱信息
     */
    class LineageGraph {
        private Long centerAssetId;
        private List<DataLineage> upstreamRelations;
        private List<DataLineage> downstreamRelations;
        private Integer totalNodes;
        private Integer totalEdges;

        // 构造函数、getter、setter方法
        public LineageGraph() {}

        public Long getCenterAssetId() { return centerAssetId; }
        public void setCenterAssetId(Long centerAssetId) { this.centerAssetId = centerAssetId; }
        
        public List<DataLineage> getUpstreamRelations() { return upstreamRelations; }
        public void setUpstreamRelations(List<DataLineage> upstreamRelations) { this.upstreamRelations = upstreamRelations; }
        
        public List<DataLineage> getDownstreamRelations() { return downstreamRelations; }
        public void setDownstreamRelations(List<DataLineage> downstreamRelations) { this.downstreamRelations = downstreamRelations; }
        
        public Integer getTotalNodes() { return totalNodes; }
        public void setTotalNodes(Integer totalNodes) { this.totalNodes = totalNodes; }
        
        public Integer getTotalEdges() { return totalEdges; }
        public void setTotalEdges(Integer totalEdges) { this.totalEdges = totalEdges; }
    }

    /**
     * 传输风险分析结果
     */
    class TransferRisk {
        private Boolean hasCrossBorderTransfer;
        private Boolean hasCrossDomainTransfer;
        private List<String> riskPaths;
        private String riskLevel;
        private String recommendation;

        // 构造函数、getter、setter方法
        public TransferRisk() {}

        public Boolean getHasCrossBorderTransfer() { return hasCrossBorderTransfer; }
        public void setHasCrossBorderTransfer(Boolean hasCrossBorderTransfer) { this.hasCrossBorderTransfer = hasCrossBorderTransfer; }
        
        public Boolean getHasCrossDomainTransfer() { return hasCrossDomainTransfer; }
        public void setHasCrossDomainTransfer(Boolean hasCrossDomainTransfer) { this.hasCrossDomainTransfer = hasCrossDomainTransfer; }
        
        public List<String> getRiskPaths() { return riskPaths; }
        public void setRiskPaths(List<String> riskPaths) { this.riskPaths = riskPaths; }
        
        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
        
        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    }
}