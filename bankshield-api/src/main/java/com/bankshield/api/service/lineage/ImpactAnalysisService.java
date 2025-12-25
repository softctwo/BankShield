package com.bankshield.api.service.lineage;

import com.bankshield.api.entity.DataFlow;
import com.bankshield.api.entity.DataImpactAnalysis;
import com.bankshield.api.mapper.DataFlowMapper;
import com.bankshield.api.mapper.DataImpactAnalysisMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 影响分析服务
 * 分析数据变更对下游数据的影响范围和风险等级
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImpactAnalysisService {

    private final DataFlowMapper dataFlowMapper;
    private final DataImpactAnalysisMapper impactAnalysisMapper;

    /**
     * 执行影响分析
     */
    @Async
    public CompletableFuture<DataImpactAnalysis> performImpactAnalysis(Long analysisId) {
        try {
            DataImpactAnalysis analysis = impactAnalysisMapper.selectById(analysisId);
            if (analysis == null) {
                log.error("影响分析任务不存在: {}", analysisId);
                return CompletableFuture.completedFuture(null);
            }

            // 更新分析状态
            analysis.setStatus("RUNNING");
            analysis.setStartTime(LocalDateTime.now());
            impactAnalysisMapper.updateById(analysis);

            log.info("开始执行影响分析: {}", analysis.getAnalysisName());

            // 解析分析目标
            ImpactTarget target = parseAnalysisTarget(analysis.getAnalysisTarget());
            
            // 构建影响图
            ImpactGraph impactGraph = buildImpactGraph(target);
            
            // 计算影响范围
            ImpactScope scope = calculateImpactScope(impactGraph, target);
            
            // 评估风险等级
            String riskLevel = assessRiskLevel(scope);
            
            // 保存分析结果
            saveAnalysisResults(analysis, scope, riskLevel);

            // 更新分析状态
            analysis.setStatus("COMPLETED");
            analysis.setEndTime(LocalDateTime.now());
            impactAnalysisMapper.updateById(analysis);

            log.info("影响分析完成: {}, 影响路径数量: {}, 风险等级: {}", 
                analysis.getAnalysisName(), scope.getImpactPaths().size(), riskLevel);

            return CompletableFuture.completedFuture(analysis);

        } catch (Exception e) {
            log.error("影响分析执行失败: {}", analysisId, e);
            
            // 更新分析状态为失败
            DataImpactAnalysis analysis = impactAnalysisMapper.selectById(analysisId);
            if (analysis != null) {
                analysis.setStatus("FAILED");
                analysis.setEndTime(LocalDateTime.now());
                analysis.setResult("分析失败: " + e.getMessage());
                impactAnalysisMapper.updateById(analysis);
            }
            
            return CompletableFuture.completedFuture(analysis);
        }
    }

    /**
     * 构建影响图
     */
    private ImpactGraph buildImpactGraph(ImpactTarget target) {
        ImpactGraph graph = new ImpactGraph();
        
        // 获取所有数据流转关系
        List<DataFlow> allFlows = dataFlowMapper.selectList(null);
        
        // 构建图的节点和边
        for (DataFlow flow : allFlows) {
            String sourceNode = buildGraphNode(flow.getSourceTable(), flow.getSourceColumn());
            String targetNode = buildGraphNode(flow.getTargetTable(), flow.getTargetColumn());
            
            graph.addNode(sourceNode);
            graph.addNode(targetNode);
            graph.addEdge(sourceNode, targetNode, flow);
        }
        
        return graph;
    }

    /**
     * 计算影响范围
     */
    private ImpactScope calculateImpactScope(ImpactGraph graph, ImpactTarget target) {
        ImpactScope scope = new ImpactScope();
        
        String targetNode = buildGraphNode(target.getTableName(), target.getColumnName());
        
        // 前向分析（影响下游）
        Set<String> forwardAffectedNodes = new HashSet<>();
        List<ImpactPath> forwardPaths = new ArrayList<>();
        performForwardAnalysis(graph, targetNode, new HashSet<>(), new ArrayList<>(), 
                              forwardAffectedNodes, forwardPaths);
        
        // 后向分析（追溯上游）
        Set<String> backwardAffectedNodes = new HashSet<>();
        List<ImpactPath> backwardPaths = new ArrayList<>();
        performBackwardAnalysis(graph, targetNode, new HashSet<>(), new ArrayList<>(), 
                               backwardAffectedNodes, backwardPaths);
        
        scope.setTargetNode(targetNode);
        scope.setForwardAffectedNodes(forwardAffectedNodes);
        scope.setBackwardAffectedNodes(backwardAffectedNodes);
        scope.setForwardPaths(forwardPaths);
        scope.setBackwardPaths(backwardPaths);
        scope.setTotalAffectedNodes(new HashSet<>());
        scope.getTotalAffectedNodes().addAll(forwardAffectedNodes);
        scope.getTotalAffectedNodes().addAll(backwardAffectedNodes);
        scope.getTotalAffectedNodes().add(targetNode);
        
        return scope;
    }

    /**
     * 前向分析
     */
    private void performForwardAnalysis(ImpactGraph graph, String currentNode, Set<String> visited, 
                                       List<String> currentPath, Set<String> affectedNodes, 
                                       List<ImpactPath> impactPaths) {
        if (visited.contains(currentNode)) {
            return; // 避免循环
        }
        
        visited.add(currentNode);
        currentPath.add(currentNode);
        affectedNodes.add(currentNode);
        
        // 记录影响路径
        if (currentPath.size() > 1) {
            ImpactPath impactPath = new ImpactPath();
            impactPath.setPath(new ArrayList<>(currentPath));
            impactPath.setDirection("FORWARD");
            impactPath.setLength(currentPath.size() - 1);
            impactPaths.add(impactPath);
        }
        
        // 递归分析下游节点
        List<ImpactGraph.Edge> outgoingEdges = graph.getOutgoingEdges(currentNode);
        for (ImpactGraph.Edge edge : outgoingEdges) {
            performForwardAnalysis(graph, edge.target, new HashSet<>(visited), 
                                 new ArrayList<>(currentPath), affectedNodes, impactPaths);
        }
        
        currentPath.remove(currentPath.size() - 1);
    }

    /**
     * 后向分析
     */
    private void performBackwardAnalysis(ImpactGraph graph, String currentNode, Set<String> visited, 
                                        List<String> currentPath, Set<String> affectedNodes, 
                                        List<ImpactPath> impactPaths) {
        if (visited.contains(currentNode)) {
            return; // 避免循环
        }
        
        visited.add(currentNode);
        currentPath.add(currentNode);
        affectedNodes.add(currentNode);
        
        // 记录影响路径
        if (currentPath.size() > 1) {
            ImpactPath impactPath = new ImpactPath();
            impactPath.setPath(new ArrayList<>(currentPath));
            impactPath.setDirection("BACKWARD");
            impactPath.setLength(currentPath.size() - 1);
            impactPaths.add(impactPath);
        }
        
        // 递归分析上游节点
        List<ImpactGraph.Edge> incomingEdges = graph.getIncomingEdges(currentNode);
        for (ImpactGraph.Edge edge : incomingEdges) {
            performBackwardAnalysis(graph, edge.source, new HashSet<>(visited), 
                                  new ArrayList<>(currentPath), affectedNodes, impactPaths);
        }
        
        currentPath.remove(currentPath.size() - 1);
    }

    /**
     * 评估风险等级
     */
    private String assessRiskLevel(ImpactScope scope) {
        int totalAffectedNodes = scope.getTotalAffectedNodes().size();
        int totalPaths = scope.getForwardPaths().size() + scope.getBackwardPaths().size();
        
        // 风险评分算法
        double riskScore = calculateRiskScore(totalAffectedNodes, totalPaths, scope);
        
        if (riskScore >= 0.8) {
            return "HIGH";
        } else if (riskScore >= 0.5) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * 计算风险评分
     */
    private double calculateRiskScore(int affectedNodes, int totalPaths, ImpactScope scope) {
        double nodeRisk = Math.min(affectedNodes / 20.0, 1.0); // 影响节点数评分
        double pathRisk = Math.min(totalPaths / 50.0, 1.0); // 影响路径数评分
        
        // 考虑路径长度和关键节点
        double pathLengthRisk = calculatePathLengthRisk(scope);
        double criticalNodeRisk = calculateCriticalNodeRisk(scope);
        
        return (nodeRisk * 0.4 + pathRisk * 0.3 + pathLengthRisk * 0.2 + criticalNodeRisk * 0.1);
    }

    /**
     * 计算路径长度风险
     */
    private double calculatePathLengthRisk(ImpactScope scope) {
        List<ImpactPath> allPaths = new ArrayList<>();
        allPaths.addAll(scope.getForwardPaths());
        allPaths.addAll(scope.getBackwardPaths());
        
        if (allPaths.isEmpty()) {
            return 0.0;
        }
        
        double avgLength = allPaths.stream()
            .mapToInt(ImpactPath::getLength)
            .average()
            .orElse(0.0);
        
        return Math.min(avgLength / 10.0, 1.0);
    }

    /**
     * 计算关键节点风险
     */
    private double calculateCriticalNodeRisk(ImpactScope scope) {
        // 检查是否存在关键业务表
        Set<String> criticalNodes = scope.getTotalAffectedNodes().stream()
            .filter(node -> isCriticalNode(node))
            .collect(Collectors.toSet());
        
        return criticalNodes.size() / 5.0; // 最多5个关键节点
    }

    /**
     * 判断是否为关键节点
     */
    private boolean isCriticalNode(String node) {
        String lowerNode = node.toLowerCase();
        
        // 关键业务表名称模式
        return lowerNode.contains("customer") || 
               lowerNode.contains("account") || 
               lowerNode.contains("transaction") ||
               lowerNode.contains("payment") ||
               lowerNode.contains("user") ||
               lowerNode.contains("order");
    }

    /**
     * 保存分析结果
     */
    @Transactional
    private void saveAnalysisResults(DataImpactAnalysis analysis, ImpactScope scope, String riskLevel) {
        analysis.setImpactPathCount(scope.getForwardPaths().size() + scope.getBackwardPaths().size());
        analysis.setImpactAssetCount(scope.getTotalAffectedNodes().size());
        analysis.setRiskLevel(riskLevel);
        
        // 构建影响范围JSON
        Map<String, Object> impactScopeData = new HashMap<>();
        impactScopeData.put("targetNode", scope.getTargetNode());
        impactScopeData.put("forwardAffectedNodes", scope.getForwardAffectedNodes());
        impactScopeData.put("backwardAffectedNodes", scope.getBackwardAffectedNodes());
        impactScopeData.put("forwardPaths", scope.getForwardPaths());
        impactScopeData.put("backwardPaths", scope.getBackwardPaths());
        impactScopeData.put("totalAffectedNodes", scope.getTotalAffectedNodes());
        
        analysis.setImpactScope(impactScopeData.toString()); // 这里应该使用JSON序列化
        
        // 构建分析结果JSON
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("riskLevel", riskLevel);
        resultData.put("maxPathLength", calculateMaxPathLength(scope));
        resultData.put("criticalNodes", getCriticalNodes(scope));
        resultData.put("recommendations", generateRecommendations(scope, riskLevel));
        
        analysis.setResult(resultData.toString()); // 这里应该使用JSON序列化
        
        impactAnalysisMapper.updateById(analysis);
    }

    /**
     * 计算最大路径长度
     */
    private int calculateMaxPathLength(ImpactScope scope) {
        List<ImpactPath> allPaths = new ArrayList<>();
        allPaths.addAll(scope.getForwardPaths());
        allPaths.addAll(scope.getBackwardPaths());
        
        return allPaths.stream()
            .mapToInt(ImpactPath::getLength)
            .max()
            .orElse(0);
    }

    /**
     * 获取关键节点
     */
    private List<String> getCriticalNodes(ImpactScope scope) {
        return scope.getTotalAffectedNodes().stream()
            .filter(this::isCriticalNode)
            .collect(Collectors.toList());
    }

    /**
     * 生成建议
     */
    private List<String> generateRecommendations(ImpactScope scope, String riskLevel) {
        List<String> recommendations = new ArrayList<>();
        
        if ("HIGH".equals(riskLevel)) {
            recommendations.add("建议进行详细的影响评估和回滚计划制定");
            recommendations.add("建议在低峰期执行变更操作");
            recommendations.add("建议提前通知相关团队和用户");
        } else if ("MEDIUM".equals(riskLevel)) {
            recommendations.add("建议进行影响验证测试");
            recommendations.add("建议准备回滚方案");
        } else {
            recommendations.add("风险较低，可以正常执行变更");
            recommendations.add("建议进行基本的回归测试");
        }
        
        // 通用建议
        recommendations.add("建议在变更前进行数据备份");
        recommendations.add("建议监控变更后的系统状态");
        
        return recommendations;
    }

    /**
     * 解析分析目标
     */
    private ImpactTarget parseAnalysisTarget(String analysisTargetJson) {
        // 这里应该解析JSON格式的分析目标
        // 简化处理
        ImpactTarget target = new ImpactTarget();
        target.setTableName("example_table");
        target.setColumnName("example_column");
        return target;
    }

    /**
     * 构建图节点
     */
    private String buildGraphNode(String tableName, String columnName) {
        if (columnName != null && !columnName.trim().isEmpty()) {
            return tableName + "." + columnName;
        }
        return tableName;
    }

    /**
     * 创建影响分析任务
     */
    public DataImpactAnalysis createImpactAnalysis(String analysisName, String analysisType, 
                                                   String impactObjectType, String impactObjectName,
                                                   Map<String, Object> analysisTarget, Long createBy) {
        DataImpactAnalysis analysis = new DataImpactAnalysis();
        analysis.setAnalysisName(analysisName);
        analysis.setAnalysisType(analysisType);
        analysis.setImpactObjectType(impactObjectType);
        analysis.setImpactObjectName(impactObjectName);
        analysis.setAnalysisTarget(analysisTarget.toString()); // 这里应该使用JSON序列化
        analysis.setStatus("PENDING");
        analysis.setCreateBy(createBy);
        analysis.setCreateTime(LocalDateTime.now());
        
        impactAnalysisMapper.insert(analysis);
        return analysis;
    }

    /**
     * 获取分析结果
     */
    public DataImpactAnalysis getAnalysisResult(Long analysisId) {
        return impactAnalysisMapper.selectById(analysisId);
    }

    /**
     * 获取最近的分析任务
     */
    public List<DataImpactAnalysis> getRecentAnalyses(int limit) {
        // 这里应该实现查询逻辑
        return new ArrayList<>();
    }

    /**
     * 获取分析统计信息
     */
    public Map<String, Object> getAnalysisStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 这里应该实现统计查询逻辑
        stats.put("totalAnalyses", 0);
        stats.put("runningAnalyses", 0);
        stats.put("completedAnalyses", 0);
        stats.put("highRiskAnalyses", 0);
        
        return stats;
    }

    /**
     * 影响目标类
     */
    private static class ImpactTarget {
        private String tableName;
        private String columnName;
        private String schemaName;
        
        // getters and setters
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
        public String getColumnName() { return columnName; }
        public void setColumnName(String columnName) { this.columnName = columnName; }
        public String getSchemaName() { return schemaName; }
        public void setSchemaName(String schemaName) { this.schemaName = schemaName; }
    }

    /**
     * 影响图类
     */
    private static class ImpactGraph {
        private Map<String, Set<Edge>> outgoingEdges = new HashMap<>();
        private Map<String, Set<Edge>> incomingEdges = new HashMap<>();
        private Set<String> nodes = new HashSet<>();
        
        public void addNode(String node) {
            nodes.add(node);
            outgoingEdges.putIfAbsent(node, new HashSet<>());
            incomingEdges.putIfAbsent(node, new HashSet<>());
        }
        
        public void addEdge(String source, String target, DataFlow flow) {
            Edge edge = new Edge(source, target, flow);
            outgoingEdges.get(source).add(edge);
            incomingEdges.get(target).add(edge);
        }
        
        public List<Edge> getOutgoingEdges(String node) {
            return new ArrayList<>(outgoingEdges.getOrDefault(node, new HashSet<>()));
        }
        
        public List<Edge> getIncomingEdges(String node) {
            return new ArrayList<>(incomingEdges.getOrDefault(node, new HashSet<>()));
        }
        
        public Set<String> getNodes() {
            return new HashSet<>(nodes);
        }
        
        static class Edge {
            String source;
            String target;
            DataFlow flow;
            
            Edge(String source, String target, DataFlow flow) {
                this.source = source;
                this.target = target;
                this.flow = flow;
            }
        }
    }

    /**
     * 影响范围类
     */
    private static class ImpactScope {
        private String targetNode;
        private Set<String> forwardAffectedNodes = new HashSet<>();
        private Set<String> backwardAffectedNodes = new HashSet<>();
        private Set<String> totalAffectedNodes = new HashSet<>();
        private List<ImpactPath> forwardPaths = new ArrayList<>();
        private List<ImpactPath> backwardPaths = new ArrayList<>();

        // getters and setters
        public String getTargetNode() { return targetNode; }
        public void setTargetNode(String targetNode) { this.targetNode = targetNode; }
        public Set<String> getForwardAffectedNodes() { return forwardAffectedNodes; }
        public void setForwardAffectedNodes(Set<String> forwardAffectedNodes) { this.forwardAffectedNodes = forwardAffectedNodes; }
        public Set<String> getBackwardAffectedNodes() { return backwardAffectedNodes; }
        public void setBackwardAffectedNodes(Set<String> backwardAffectedNodes) { this.backwardAffectedNodes = backwardAffectedNodes; }
        public Set<String> getTotalAffectedNodes() { return totalAffectedNodes; }
        public void setTotalAffectedNodes(Set<String> totalAffectedNodes) { this.totalAffectedNodes = totalAffectedNodes; }
        public List<ImpactPath> getForwardPaths() { return forwardPaths; }
        public void setForwardPaths(List<ImpactPath> forwardPaths) { this.forwardPaths = forwardPaths; }
        public List<ImpactPath> getBackwardPaths() { return backwardPaths; }
        public void setBackwardPaths(List<ImpactPath> backwardPaths) { this.backwardPaths = backwardPaths; }

        /**
         * 获取所有影响路径
         *
         * @return 所有影响路径列表
         */
        public List<ImpactPath> getImpactPaths() {
            List<ImpactPath> allPaths = new ArrayList<>();
            allPaths.addAll(forwardPaths);
            allPaths.addAll(backwardPaths);
            return allPaths;
        }
    }

    /**
     * 影响路径类
     */
    private static class ImpactPath {
        private List<String> path;
        private String direction;
        private int length;
        
        // getters and setters
        public List<String> getPath() { return path; }
        public void setPath(List<String> path) { this.path = path; }
        public String getDirection() { return direction; }
        public void setDirection(String direction) { this.direction = direction; }
        public int getLength() { return length; }
        public void setLength(int length) { this.length = length; }
    }
}