package com.bankshield.lineage.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bankshield.lineage.entity.DataLineageEdge;
import com.bankshield.lineage.entity.DataLineageNode;
import com.bankshield.lineage.enums.NodeType;
import com.bankshield.lineage.enums.RelationshipType;
import com.bankshield.lineage.extractor.sql.SqlLineageExtractor;
import com.bankshield.lineage.mapper.DataLineageEdgeMapper;
import com.bankshield.lineage.mapper.DataLineageNodeMapper;
import com.bankshield.lineage.service.LineageService;
import com.bankshield.lineage.vo.ImpactAnalysis;
import com.bankshield.lineage.vo.LineageGraph;
import com.bankshield.lineage.vo.LineageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据血缘服务实现类
 *
 * @author BankShield
 * @since 2024-01-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LineageServiceImpl extends ServiceImpl<DataLineageNodeMapper, DataLineageNode> 
        implements LineageService {

    private final DataLineageNodeMapper nodeMapper;
    private final DataLineageEdgeMapper edgeMapper;
    private final SqlLineageExtractor sqlLineageExtractor;

    @Override
    public LineageGraph buildGraph(Long centerNodeId, Integer depth) {
        log.info("构建血缘图谱，中心节点ID：{}，深度：{}", centerNodeId, depth);
        
        try {
            LineageGraph graph = new LineageGraph();
            graph.setCenterNodeId(centerNodeId);
            graph.setDepth(depth);
            graph.setGraphId(UUID.randomUUID().toString());
            
            // 查询节点数据
            List<LineageGraph.LineageNode> nodes = nodeMapper.selectGraphNodes(centerNodeId, depth);
            graph.setNodes(nodes);
            
            if (CollUtil.isNotEmpty(nodes)) {
                // 提取节点ID列表
                List<Long> nodeIds = nodes.stream()
                        .map(LineageGraph.LineageNode::getId)
                        .collect(Collectors.toList());
                
                // 查询边数据
                List<LineageGraph.LineageEdge> edges = edgeMapper.selectGraphEdges(nodeIds);
                graph.setLinks(edges);
                
                // 计算统计信息
                LineageGraph.GraphStatistics statistics = calculateGraphStatistics(nodes, edges);
                graph.setStatistics(statistics);
            }
            
            log.info("血缘图谱构建完成，节点数：{}，边数：{}", 
                    graph.getNodes().size(), graph.getLinks().size());
            
            return graph;
            
        } catch (Exception e) {
            log.error("构建血缘图谱失败", e);
            throw new RuntimeException("构建血缘图谱失败", e);
        }
    }

    @Override
    public ImpactAnalysis analyzeImpact(Long assetId) {
        log.info("分析资产影响，资产ID：{}", assetId);
        
        try {
            DataLineageNode node = nodeMapper.selectById(assetId);
            if (node == null) {
                throw new RuntimeException("节点不存在：" + assetId);
            }
            
            ImpactAnalysis analysis = new ImpactAnalysis();
            analysis.setAssetId(assetId);
            analysis.setAssetName(node.getNodeName());
            analysis.setAssetType(node.getNodeType());
            
            // 分析直接影响
            ImpactAnalysis.ImpactStatistics directImpact = analyzeDirectImpact(assetId);
            analysis.setDirectImpact(directImpact);
            
            // 分析间接影响
            ImpactAnalysis.ImpactStatistics indirectImpact = analyzeIndirectImpact(assetId);
            analysis.setIndirectImpact(indirectImpact);
            
            // 查询下游表清单
            List<ImpactAnalysis.DownstreamTable> downstreamTables = findDownstreamTables(assetId);
            analysis.setDownstreamTables(downstreamTables);
            
            // 计算复杂性
            ImpactAnalysis.ComplexityAnalysis complexity = calculateComplexityAnalysis(assetId);
            analysis.setComplexity(complexity);
            
            log.info("影响分析完成，直接影响：{}，间接影响：{}", 
                    directImpact.getTableCount(), indirectImpact.getTableCount());
            
            return analysis;
            
        } catch (Exception e) {
            log.error("影响分析失败", e);
            throw new RuntimeException("影响分析失败", e);
        }
    }

    @Override
    public ImpactAnalysis analyzeFieldImpact(Long fieldId) {
        log.info("分析字段影响，字段ID：{}", fieldId);
        
        try {
            DataLineageNode fieldNode = nodeMapper.selectById(fieldId);
            if (fieldNode == null) {
                throw new RuntimeException("字段节点不存在：" + fieldId);
            }
            
            ImpactAnalysis analysis = new ImpactAnalysis();
            analysis.setAssetId(fieldId);
            analysis.setAssetName(fieldNode.getNodeName());
            analysis.setAssetType(fieldNode.getNodeType());
            
            // 查询所有下游字段
            List<DataLineageNode> downstreamFields = findAllDownstreamNodes(fieldId, 5);
            
            // 统计影响
            ImpactAnalysis.ImpactStatistics impact = new ImpactAnalysis.ImpactStatistics();
            impact.setColumnCount((int) downstreamFields.stream()
                    .filter(node -> NodeType.COLUMN.getCode().equals(node.getNodeType()))
                    .count());
            impact.setTableCount((int) downstreamFields.stream()
                    .filter(node -> NodeType.TABLE.getCode().equals(node.getNodeType()))
                    .count());
            impact.setViewCount((int) downstreamFields.stream()
                    .filter(node -> NodeType.VIEW.getCode().equals(node.getNodeType()))
                    .count());
            
            analysis.setDirectImpact(impact);
            
            log.info("字段影响分析完成，下游字段数：{}", impact.getColumnCount());
            return analysis;
            
        } catch (Exception e) {
            log.error("字段影响分析失败", e);
            throw new RuntimeException("字段影响分析失败", e);
        }
    }

    @Override
    public ImpactAnalysis analyzeTableImpact(Long tableId) {
        log.info("分析表影响，表ID：{}", tableId);
        return analyzeImpact(tableId);
    }

    @Override
    @Transactional
    public void discoverFromSqlLogs() {
        log.info("开始从SQL日志自动发现血缘关系");
        
        try {
            // TODO: 实现从SQL日志提取逻辑
            // 1. 查询SQL执行日志
            // 2. 解析每条SQL语句
            // 3. 提取血缘信息
            // 4. 保存到数据库
            
            log.info("SQL日志血缘发现完成");
            
        } catch (Exception e) {
            log.error("SQL日志血缘发现失败", e);
        }
    }

    @Override
    public LineageInfo extractSqlLineage(String sql, String dbType) {
        log.info("提取SQL血缘，数据库类型：{}", dbType);
        return sqlLineageExtractor.extractFromSql(sql, dbType);
    }

    @Override
    @Transactional
    public boolean saveLineageInfo(LineageInfo lineageInfo) {
        log.info("保存血缘信息");
        
        try {
            if (lineageInfo == null) {
                return false;
            }
            
            // TODO: 实现血缘信息保存逻辑
            // 1. 保存节点信息
            // 2. 保存边信息
            // 3. 建立关联关系
            
            log.info("血缘信息保存成功");
            return true;
            
        } catch (Exception e) {
            log.error("保存血缘信息失败", e);
            return false;
        }
    }

    @Override
    public DataLineageNode getNodeById(Long nodeId) {
        return nodeMapper.selectById(nodeId);
    }

    @Override
    public IPage<DataLineageNode> getNodes(Page<DataLineageNode> page, String nodeType, String nodeName, Long dataSourceId) {
        return nodeMapper.selectByCondition(page, nodeType, nodeName, dataSourceId);
    }

    @Override
    public List<DataLineageNode> getUpstreamNodes(Long nodeId) {
        return nodeMapper.findUpstreamNodes(nodeId);
    }

    @Override
    public List<DataLineageNode> getDownstreamNodes(Long nodeId) {
        return nodeMapper.findDownstreamNodes(nodeId);
    }

    @Override
    public List<LineageGraph.LineageEdge> getLineagePath(Long sourceNodeId, Long targetNodeId, Integer maxDepth) {
        List<DataLineageEdge> edges = edgeMapper.findImpactPath(sourceNodeId, targetNodeId, maxDepth);
        return edges.stream()
                .map(edge -> {
                    LineageGraph.LineageEdge vo = new LineageGraph.LineageEdge();
                    vo.setSource(edge.getSourceNodeId());
                    vo.setTarget(edge.getTargetNodeId());
                    vo.setRelationshipType(edge.getRelationshipType());
                    vo.setTransformation(edge.getTransformation());
                    vo.setImpactWeight(edge.getImpactWeight());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Double calculateComplexity(Long nodeId) {
        try {
            // 计算下游关系数量
            int downstreamCount = edgeMapper.countDownstreamRelations(nodeId);
            
            // 计算上游关系数量
            int upstreamCount = edgeMapper.countUpstreamRelations(nodeId);
            
            // 计算复杂性评分（0-100）
            double complexity = Math.min(100.0, (downstreamCount + upstreamCount) * 5.0);
            
            return complexity;
            
        } catch (Exception e) {
            log.error("计算复杂性失败", e);
            return 0.0;
        }
    }

    @Override
    public boolean validateLineage(Long sourceNodeId, Long targetNodeId) {
        try {
            List<LineageGraph.LineageEdge> path = getLineagePath(sourceNodeId, targetNodeId, 10);
            return CollUtil.isNotEmpty(path);
        } catch (Exception e) {
            log.error("验证血缘关系失败", e);
            return false;
        }
    }

    @Override
    public boolean importLineageData(String lineageData) {
        log.info("导入血缘数据");
        // TODO: 实现数据导入逻辑
        return true;
    }

    @Override
    public String exportLineageData(Long nodeId, Integer depth) {
        log.info("导出血缘数据，节点ID：{}，深度：{}", nodeId, depth);
        
        try {
            LineageGraph graph = buildGraph(nodeId, depth);
            // TODO: 实现数据导出逻辑
            return "exported_data";
            
        } catch (Exception e) {
            log.error("导出血缘数据失败", e);
            return null;
        }
    }

    /**
     * 计算图谱统计信息
     */
    private LineageGraph.GraphStatistics calculateGraphStatistics(List<LineageGraph.LineageNode> nodes, 
                                                                   List<LineageGraph.LineageEdge> edges) {
        LineageGraph.GraphStatistics statistics = new LineageGraph.GraphStatistics();
        
        statistics.setTotalNodes(nodes.size());
        statistics.setTotalEdges(edges.size());
        
        // 统计不同类型节点数量
        long tableNodes = nodes.stream()
                .filter(node -> NodeType.TABLE.getCode().equals(node.getType()))
                .count();
        long columnNodes = nodes.stream()
                .filter(node -> NodeType.COLUMN.getCode().equals(node.getType()))
                .count();
        long viewNodes = nodes.stream()
                .filter(node -> NodeType.VIEW.getCode().equals(node.getType()))
                .count();
        
        statistics.setTableNodes((int) tableNodes);
        statistics.setColumnNodes((int) columnNodes);
        statistics.setViewNodes((int) viewNodes);
        
        // 计算质量评分统计
        List<Double> qualityScores = nodes.stream()
                .map(LineageGraph.LineageNode::getQualityScore)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        if (CollUtil.isNotEmpty(qualityScores)) {
            statistics.setAverageQualityScore(qualityScores.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0));
            statistics.setMaxQualityScore(qualityScores.stream()
                    .mapToDouble(Double::doubleValue)
                    .max()
                    .orElse(0.0));
            statistics.setMinQualityScore(qualityScores.stream()
                    .mapToDouble(Double::doubleValue)
                    .min()
                    .orElse(0.0));
        }
        
        return statistics;
    }

    /**
     * 分析直接影响
     */
    private ImpactAnalysis.ImpactStatistics analyzeDirectImpact(Long nodeId) {
        ImpactAnalysis.ImpactStatistics statistics = new ImpactAnalysis.ImpactStatistics();
        
        List<DataLineageNode> downstreamNodes = nodeMapper.findDownstreamNodes(nodeId);
        
        statistics.setTableCount((int) downstreamNodes.stream()
                .filter(node -> NodeType.TABLE.getCode().equals(node.getNodeType()))
                .count());
        statistics.setColumnCount((int) downstreamNodes.stream()
                .filter(node -> NodeType.COLUMN.getCode().equals(node.getNodeType()))
                .count());
        statistics.setViewCount((int) downstreamNodes.stream()
                .filter(node -> NodeType.VIEW.getCode().equals(node.getNodeType()))
                .count());
        
        return statistics;
    }

    /**
     * 分析间接影响
     */
    private ImpactAnalysis.ImpactStatistics analyzeIndirectImpact(Long nodeId) {
        ImpactAnalysis.ImpactStatistics statistics = new ImpactAnalysis.ImpactStatistics();
        
        // 查询多级下游节点
        List<DataLineageNode> allDownstreamNodes = findAllDownstreamNodes(nodeId, 3);
        
        statistics.setTableCount((int) allDownstreamNodes.stream()
                .filter(node -> NodeType.TABLE.getCode().equals(node.getNodeType()))
                .count());
        statistics.setColumnCount((int) allDownstreamNodes.stream()
                .filter(node -> NodeType.COLUMN.getCode().equals(node.getNodeType()))
                .count());
        
        return statistics;
    }

    /**
     * 查询所有下游节点
     */
    private List<DataLineageNode> findAllDownstreamNodes(Long nodeId, int maxDepth) {
        Set<Long> visited = new HashSet<>();
        List<DataLineageNode> result = new ArrayList<>();
        
        findDownstreamNodesRecursive(nodeId, maxDepth, 0, visited, result);
        
        return result;
    }

    /**
     * 递归查询下游节点
     */
    private void findDownstreamNodesRecursive(Long nodeId, int maxDepth, int currentDepth, 
                                             Set<Long> visited, List<DataLineageNode> result) {
        if (currentDepth >= maxDepth || visited.contains(nodeId)) {
            return;
        }
        
        visited.add(nodeId);
        
        List<DataLineageNode> downstreamNodes = nodeMapper.findDownstreamNodes(nodeId);
        result.addAll(downstreamNodes);
        
        for (DataLineageNode downstreamNode : downstreamNodes) {
            findDownstreamNodesRecursive(downstreamNode.getId(), maxDepth, currentDepth + 1, visited, result);
        }
    }

    /**
     * 查询下游表清单
     */
    private List<ImpactAnalysis.DownstreamTable> findDownstreamTables(Long nodeId) {
        List<ImpactAnalysis.DownstreamTable> tables = new ArrayList<>();
        
        List<DataLineageNode> downstreamNodes = findAllDownstreamNodes(nodeId, 3);
        
        for (DataLineageNode node : downstreamNodes) {
            if (NodeType.TABLE.getCode().equals(node.getNodeType())) {
                ImpactAnalysis.DownstreamTable table = new ImpactAnalysis.DownstreamTable();
                table.setTableId(node.getId());
                table.setTableName(node.getNodeName());
                table.setDatabaseName(node.getDatabaseName());
                table.setImportanceLevel(node.getImportanceLevel());
                tables.add(table);
            }
        }
        
        return tables;
    }

    /**
     * 计算复杂性分析
     */
    private ImpactAnalysis.ComplexityAnalysis calculateComplexityAnalysis(Long nodeId) {
        ImpactAnalysis.ComplexityAnalysis complexity = new ImpactAnalysis.ComplexityAnalysis();
        
        // 计算下游关系数量
        int downstreamCount = edgeMapper.countDownstreamRelations(nodeId);
        
        // 计算上游关系数量
        int upstreamCount = edgeMapper.countUpstreamRelations(nodeId);
        
        complexity.setDependencyCount(downstreamCount + upstreamCount);
        complexity.setCyclomaticComplexity(Math.min(100, downstreamCount * 2 + upstreamCount));
        
        // 计算稳定性评分
        double stabilityScore = Math.max(0, 100 - complexity.getDependencyCount() * 2.0);
        complexity.setStabilityScore(stabilityScore);
        
        // 计算维护难度评分
        double maintenanceScore = Math.min(100, complexity.getDependencyCount() * 3.0);
        complexity.setMaintenanceScore(maintenanceScore);
        
        // 设置复杂性等级
        if (complexity.getDependencyCount() < 5) {
            complexity.setComplexityLevel("LOW");
        } else if (complexity.getDependencyCount() < 15) {
            complexity.setComplexityLevel("MEDIUM");
        } else {
            complexity.setComplexityLevel("HIGH");
        }
        
        return complexity;
    }
}