package com.bankshield.api.service.lineage;

import com.bankshield.api.entity.DataFlow;
import com.bankshield.api.entity.DataMap;
import com.bankshield.api.mapper.DataFlowMapper;
import com.bankshield.api.mapper.DataMapMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 血缘可视化服务
 * 提供数据血缘关系的可视化展示功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LineageVisualizationService {

    private final DataFlowMapper dataFlowMapper;
    private final DataMapMapper dataMapMapper;
    private final DataMapService dataMapService;

    /**
     * 生成血缘关系图（ECharts格式）
     */
    public Map<String, Object> generateLineageGraph(String tableName, String columnName, int depth) {
        try {
            log.info("生成血缘关系图: {}.{}, 深度: {}", tableName, columnName, depth);
            
            // 获取相关的数据流
            List<DataFlow> relatedFlows = getRelatedDataFlows(tableName, columnName, depth);
            
            // 构建ECharts图数据
            Map<String, Object> graphData = buildEChartsGraphData(relatedFlows, tableName, columnName);
            
            log.info("血缘关系图生成完成，包含 {} 个节点，{} 条边", 
                ((List<?>) graphData.get("nodes")).size(), 
                ((List<?>) graphData.get("links")).size());
            
            return graphData;
            
        } catch (Exception e) {
            log.error("生成血缘关系图失败: {}.{}", tableName, columnName, e);
            throw new RuntimeException("生成血缘关系图失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成影响分析图
     */
    public Map<String, Object> generateImpactAnalysisGraph(String tableName, String columnName) {
        try {
            log.info("生成影响分析图: {}.{}", tableName, columnName);
            
            // 获取前向和后向影响
            List<DataFlow> forwardFlows = getForwardImpactFlows(tableName, columnName);
            List<DataFlow> backwardFlows = getBackwardImpactFlows(tableName, columnName);
            
            // 构建影响分析图数据
            Map<String, Object> graphData = buildImpactAnalysisGraphData(forwardFlows, backwardFlows, tableName, columnName);
            
            log.info("影响分析图生成完成，前向影响 {} 条，后向影响 {} 条", 
                forwardFlows.size(), backwardFlows.size());
            
            return graphData;
            
        } catch (Exception e) {
            log.error("生成影响分析图失败: {}.{}", tableName, columnName, e);
            throw new RuntimeException("生成影响分析图失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成数据地图（3D可视化）
     */
    public Map<String, Object> generate3DDataMap(Long mapId) {
        try {
            log.info("生成3D数据地图: {}", mapId);
            
            DataMap dataMap = dataMapMapper.selectById(mapId);
            if (dataMap == null) {
                throw new RuntimeException("数据地图不存在: " + mapId);
            }
            
            // 构建3D图数据
            Map<String, Object> graphData = build3DGraphData(dataMap);
            
            log.info("3D数据地图生成完成: {}", mapId);
            
            return graphData;
            
        } catch (Exception e) {
            log.error("生成3D数据地图失败: {}", mapId, e);
            throw new RuntimeException("生成3D数据地图失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成溯源路径图
     */
    public Map<String, Object> generateTraceabilityGraph(String sourceTable, String sourceColumn, 
                                                        String targetTable, String targetColumn) {
        try {
            log.info("生成溯源路径图: {}.{} -> {}.{}", sourceTable, sourceColumn, targetTable, targetColumn);
            
            // 查找所有可能的路径
            List<List<DataFlow>> allPaths = findAllPaths(sourceTable, sourceColumn, targetTable, targetColumn);
            
            // 构建溯源路径图数据
            Map<String, Object> graphData = buildTraceabilityGraphData(allPaths, sourceTable, targetTable);
            
            log.info("溯源路径图生成完成，找到 {} 条路径", allPaths.size());
            
            return graphData;
            
        } catch (Exception e) {
            log.error("生成溯源路径图失败: {}.{} -> {}.{}", sourceTable, sourceColumn, targetTable, targetColumn, e);
            throw new RuntimeException("生成溯源路径图失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成数据流向图
     */
    public Map<String, Object> generateDataFlowGraph(String tableName, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            log.info("生成数据流向图: {}, 时间范围: {} - {}", tableName, startTime, endTime);
            
            // 获取时间范围内的数据流
            List<DataFlow> timeRangeFlows = getDataFlowsInTimeRange(tableName, startTime, endTime);
            
            // 构建流向图数据
            Map<String, Object> graphData = buildDataFlowGraphData(timeRangeFlows, tableName, startTime, endTime);
            
            log.info("数据流向图生成完成，时间范围内 {} 条数据流", timeRangeFlows.size());
            
            return graphData;
            
        } catch (Exception e) {
            log.error("生成数据流向图失败: {}", tableName, e);
            throw new RuntimeException("生成数据流向图失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导出可视化图表为HTML
     */
    public String exportVisualizationToHtml(String chartType, Map<String, Object> chartData, String title) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "lineage_" + chartType + "_" + timestamp + ".html";
            String filePath = "/tmp/" + fileName;
            
            String htmlContent = generateHtmlContent(chartType, chartData, title);
            
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(htmlContent);
            }
            
            log.info("可视化图表导出完成: {}", filePath);
            return filePath;
            
        } catch (IOException e) {
            log.error("导出可视化图表失败", e);
            throw new RuntimeException("导出可视化图表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取相关的数据流
     */
    private List<DataFlow> getRelatedDataFlows(String tableName, String columnName, int depth) {
        List<DataFlow> allFlows = dataFlowMapper.selectList(null);
        Set<String> relatedTables = new HashSet<>();
        relatedTables.add(tableName);
        
        // 迭代查找相关表
        for (int i = 0; i < depth; i++) {
            Set<String> newTables = new HashSet<>();
            
            for (DataFlow flow : allFlows) {
                if (relatedTables.contains(flow.getSourceTable()) || relatedTables.contains(flow.getTargetTable())) {
                    newTables.add(flow.getSourceTable());
                    newTables.add(flow.getTargetTable());
                }
            }
            
            relatedTables.addAll(newTables);
        }
        
        // 过滤相关数据流
        return allFlows.stream()
            .filter(flow -> relatedTables.contains(flow.getSourceTable()) || relatedTables.contains(flow.getTargetTable()))
            .collect(Collectors.toList());
    }

    /**
     * 获取前向影响流
     */
    private List<DataFlow> getForwardImpactFlows(String tableName, String columnName) {
        return dataFlowMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataFlow>()
                .eq("source_table", tableName)
        );
    }

    /**
     * 获取后向影响流
     */
    private List<DataFlow> getBackwardImpactFlows(String tableName, String columnName) {
        return dataFlowMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataFlow>()
                .eq("target_table", tableName)
        );
    }

    /**
     * 获取时间范围内的数据流
     */
    private List<DataFlow> getDataFlowsInTimeRange(String tableName, LocalDateTime startTime, LocalDateTime endTime) {
        return dataFlowMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataFlow>()
                .and(wrapper -> wrapper
                    .eq("source_table", tableName)
                    .or()
                    .eq("target_table", tableName)
                )
                .ge("discovery_time", startTime)
                .le("discovery_time", endTime)
        );
    }

    /**
     * 查找所有路径
     */
    private List<List<DataFlow>> findAllPaths(String sourceTable, String sourceColumn, String targetTable, String targetColumn) {
        List<List<DataFlow>> allPaths = new ArrayList<>();
        List<DataFlow> allFlows = dataFlowMapper.selectList(null);
        
        // 构建图
        Map<String, List<DataFlow>> graph = new HashMap<>();
        for (DataFlow flow : allFlows) {
            graph.computeIfAbsent(flow.getSourceTable(), k -> new ArrayList<>()).add(flow);
        }
        
        // 深度优先搜索查找路径
        List<DataFlow> currentPath = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        
        dfsFindPaths(sourceTable, targetTable, graph, currentPath, visited, allPaths, 0, 10); // 限制路径长度
        
        return allPaths;
    }

    /**
     * 深度优先搜索查找路径
     */
    private void dfsFindPaths(String current, String target, Map<String, List<DataFlow>> graph,
                             List<DataFlow> currentPath, Set<String> visited, List<List<DataFlow>> allPaths,
                             int depth, int maxDepth) {
        if (depth > maxDepth) return;
        if (visited.contains(current)) return;
        if (current.equals(target)) {
            allPaths.add(new ArrayList<>(currentPath));
            return;
        }
        
        visited.add(current);
        
        List<DataFlow> neighbors = graph.getOrDefault(current, new ArrayList<>());
        for (DataFlow flow : neighbors) {
            currentPath.add(flow);
            dfsFindPaths(flow.getTargetTable(), target, graph, currentPath, visited, allPaths, depth + 1, maxDepth);
            currentPath.remove(currentPath.size() - 1);
        }
        
        visited.remove(current);
    }

    /**
     * 构建ECharts图数据
     */
    private Map<String, Object> buildEChartsGraphData(List<DataFlow> flows, String centerTable, String centerColumn) {
        Map<String, Object> graphData = new HashMap<>();
        
        // 节点数据
        List<Map<String, Object>> nodes = new ArrayList<>();
        Set<String> processedNodes = new HashSet<>();
        
        for (DataFlow flow : flows) {
            // 源节点
            if (!processedNodes.contains(flow.getSourceTable())) {
                Map<String, Object> sourceNode = new HashMap<>();
                sourceNode.put("id", flow.getSourceTable());
                sourceNode.put("name", flow.getSourceTable());
                sourceNode.put("category", determineNodeCategory(flow.getSourceTable()))
                ;
                sourceNode.put("symbolSize", calculateNodeSize(flows, flow.getSourceTable()));
                sourceNode.put("itemStyle", createNodeStyle(flow.getSourceTable().equals(centerTable)));
                nodes.add(sourceNode);
                processedNodes.add(flow.getSourceTable());
            }
            
            // 目标节点
            if (!processedNodes.contains(flow.getTargetTable())) {
                Map<String, Object> targetNode = new HashMap<>();
                targetNode.put("id", flow.getTargetTable());
                targetNode.put("name", flow.getTargetTable());
                targetNode.put("category", determineNodeCategory(flow.getTargetTable()));
                targetNode.put("symbolSize", calculateNodeSize(flows, flow.getTargetTable()));
                targetNode.put("itemStyle", createNodeStyle(flow.getTargetTable().equals(centerTable)));
                nodes.add(targetNode);
                processedNodes.add(flow.getTargetTable());
            }
        }
        
        // 边数据
        List<Map<String, Object>> links = new ArrayList<>();
        
        for (DataFlow flow : flows) {
            Map<String, Object> link = new HashMap<>();
            link.put("source", flow.getSourceTable());
            link.put("target", flow.getTargetTable());
            link.put("value", flow.getConfidence().doubleValue());
            link.put("lineStyle", createEdgeStyle(flow));
            links.add(link);
        }
        
        // 类别数据
        List<Map<String, Object>> categories = createCategories();
        
        graphData.put("nodes", nodes);
        graphData.put("links", links);
        graphData.put("categories", categories);
        graphData.put("title", "数据血缘关系图");
        
        return graphData;
    }

    /**
     * 构建影响分析图数据
     */
    private Map<String, Object> buildImpactAnalysisGraphData(List<DataFlow> forwardFlows, List<DataFlow> backwardFlows,
                                                            String centerTable, String centerColumn) {
        Map<String, Object> graphData = new HashMap<>();
        
        // 合并数据流
        List<DataFlow> allFlows = new ArrayList<>();
        allFlows.addAll(forwardFlows);
        allFlows.addAll(backwardFlows);
        
        // 节点数据
        List<Map<String, Object>> nodes = new ArrayList<>();
        Set<String> processedNodes = new HashSet<>();
        
        // 中心节点
        Map<String, Object> centerNode = new HashMap<>();
        centerNode.put("id", centerTable);
        centerNode.put("name", centerTable);
        centerNode.put("category", "center");
        centerNode.put("symbolSize", 60);
        centerNode.put("itemStyle", createCenterNodeStyle());
        nodes.add(centerNode);
        processedNodes.add(centerTable);
        
        // 前向影响节点
        for (DataFlow flow : forwardFlows) {
            if (!processedNodes.contains(flow.getTargetTable())) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", flow.getTargetTable());
                node.put("name", flow.getTargetTable());
                node.put("category", "forward");
                node.put("symbolSize", calculateNodeSize(forwardFlows, flow.getTargetTable()));
                node.put("itemStyle", createForwardNodeStyle());
                nodes.add(node);
                processedNodes.add(flow.getTargetTable());
            }
        }
        
        // 后向影响节点
        for (DataFlow flow : backwardFlows) {
            if (!processedNodes.contains(flow.getSourceTable())) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", flow.getSourceTable());
                node.put("name", flow.getSourceTable());
                node.put("category", "backward");
                node.put("symbolSize", calculateNodeSize(backwardFlows, flow.getSourceTable()));
                node.put("itemStyle", createBackwardNodeStyle());
                nodes.add(node);
                processedNodes.add(flow.getSourceTable());
            }
        }
        
        // 边数据
        List<Map<String, Object>> links = new ArrayList<>();
        
        // 前向影响边
        for (DataFlow flow : forwardFlows) {
            Map<String, Object> link = new HashMap<>();
            link.put("source", flow.getSourceTable());
            link.put("target", flow.getTargetTable());
            link.put("value", flow.getConfidence().doubleValue());
            link.put("lineStyle", createForwardEdgeStyle());
            links.add(link);
        }
        
        // 后向影响边
        for (DataFlow flow : backwardFlows) {
            Map<String, Object> link = new HashMap<>();
            link.put("source", flow.getSourceTable());
            link.put("target", flow.getTargetTable());
            link.put("value", flow.getConfidence().doubleValue());
            link.put("lineStyle", createBackwardEdgeStyle());
            links.add(link);
        }
        
        // 类别数据
        List<Map<String, Object>> categories = Arrays.asList(
            createCategory("center", "中心节点", "#FF6B6B"),
            createCategory("forward", "前向影响", "#4ECDC4"),
            createCategory("backward", "后向影响", "#45B7D1")
        );
        
        graphData.put("nodes", nodes);
        graphData.put("links", links);
        graphData.put("categories", categories);
        graphData.put("title", "数据影响分析图");
        
        return graphData;
    }

    /**
     * 构建3D图数据
     */
    private Map<String, Object> build3DGraphData(DataMap dataMap) {
        Map<String, Object> graphData = new HashMap<>();
        
        // 这里应该从dataMap的mapData字段解析出3D数据
        // 简化处理，返回示例数据
        
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> links = new ArrayList<>();
        
        // 生成示例3D节点
        for (int i = 0; i < 50; i++) {
            Map<String, Object> node = new HashMap<>();
            node.put("name", "node_" + i);
            node.put("value", Arrays.asList(
                Math.random() * 100,
                Math.random() * 100,
                Math.random() * 100
            ));
            node.put("category", i % 5);
            nodes.add(node);
        }
        
        // 生成示例3D边
        for (int i = 0; i < 100; i++) {
            Map<String, Object> link = new HashMap<>();
            link.put("source", "node_" + (i % 50));
            link.put("target", "node_" + ((i + 1) % 50));
            links.add(link);
        }
        
        graphData.put("nodes", nodes);
        graphData.put("links", links);
        graphData.put("title", "3D数据地图");
        
        return graphData;
    }

    /**
     * 构建溯源路径图数据
     */
    private Map<String, Object> buildTraceabilityGraphData(List<List<DataFlow>> allPaths, String sourceTable, String targetTable) {
        Map<String, Object> graphData = new HashMap<>();
        
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> links = new ArrayList<>();
        Set<String> processedNodes = new HashSet<>();
        
        // 添加路径节点和边
        for (int pathIndex = 0; pathIndex < allPaths.size(); pathIndex++) {
            List<DataFlow> path = allPaths.get(pathIndex);
            String pathColor = getPathColor(pathIndex);
            
            for (int i = 0; i < path.size(); i++) {
                DataFlow flow = path.get(i);
                
                // 源节点
                if (!processedNodes.contains(flow.getSourceTable())) {
                    Map<String, Object> sourceNode = new HashMap<>();
                    sourceNode.put("id", flow.getSourceTable());
                    sourceNode.put("name", flow.getSourceTable());
                    sourceNode.put("category", flow.getSourceTable().equals(sourceTable) ? "source" : "intermediate");
                    sourceNode.put("symbolSize", calculateNodeSizeByPathIndex(pathIndex, allPaths.size()));
                    sourceNode.put("itemStyle", createPathNodeStyle(pathColor, i == 0));
                    nodes.add(sourceNode);
                    processedNodes.add(flow.getSourceTable());
                }
                
                // 目标节点
                if (!processedNodes.contains(flow.getTargetTable())) {
                    Map<String, Object> targetNode = new HashMap<>();
                    targetNode.put("id", flow.getTargetTable());
                    targetNode.put("name", flow.getTargetTable());
                    targetNode.put("category", flow.getTargetTable().equals(targetTable) ? "target" : "intermediate");
                    targetNode.put("symbolSize", calculateNodeSizeByPathIndex(pathIndex, allPaths.size()));
                    targetNode.put("itemStyle", createPathNodeStyle(pathColor, i == path.size() - 1));
                    nodes.add(targetNode);
                    processedNodes.add(flow.getTargetTable());
                }
                
                // 边
                Map<String, Object> link = new HashMap<>();
                link.put("source", flow.getSourceTable());
                link.put("target", flow.getTargetTable());
                link.put("value", flow.getConfidence().doubleValue());
                link.put("lineStyle", createPathEdgeStyle(pathColor));
                links.add(link);
            }
        }
        
        // 类别数据
        List<Map<String, Object>> categories = Arrays.asList(
            createCategory("source", "源节点", "#FF6B6B"),
            createCategory("target", "目标节点", "#4ECDC4"),
            createCategory("intermediate", "中间节点", "#45B7D1")
        );
        
        graphData.put("nodes", nodes);
        graphData.put("links", links);
        graphData.put("categories", categories);
        graphData.put("title", "数据溯源路径图");
        graphData.put("pathCount", allPaths.size());
        
        return graphData;
    }

    /**
     * 构建数据流向图数据
     */
    private Map<String, Object> buildDataFlowGraphData(List<DataFlow> flows, String tableName, 
                                                      LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> graphData = new HashMap<>();
        
        // 按时间分组
        Map<String, List<DataFlow>> timeGroups = flows.stream()
            .collect(Collectors.groupingBy(flow -> 
                flow.getDiscoveryTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH"))
            ));
        
        List<Map<String, Object>> series = new ArrayList<>();
        
        for (Map.Entry<String, List<DataFlow>> entry : timeGroups.entrySet()) {
            String timeGroup = entry.getKey();
            List<DataFlow> groupFlows = entry.getValue();
            
            Map<String, Object> seriesData = new HashMap<>();
            seriesData.put("name", timeGroup);
            seriesData.put("type", "graph");
            seriesData.put("data", buildTimeSliceGraphData(groupFlows));
            series.add(seriesData);
        }
        
        graphData.put("series", series);
        graphData.put("title", "数据流向时序图 - " + tableName);
        graphData.put("timeRange", Arrays.asList(startTime.toString(), endTime.toString()));
        
        return graphData;
    }

    /**
     * 构建时间片图数据
     */
    private Map<String, Object> buildTimeSliceGraphData(List<DataFlow> flows) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> links = new ArrayList<>();
        Set<String> processedNodes = new HashSet<>();
        
        for (DataFlow flow : flows) {
            // 节点
            if (!processedNodes.contains(flow.getSourceTable())) {
                Map<String, Object> sourceNode = new HashMap<>();
                sourceNode.put("id", flow.getSourceTable());
                sourceNode.put("name", flow.getSourceTable());
                sourceNode.put("category", determineNodeCategory(flow.getSourceTable()));
                nodes.add(sourceNode);
                processedNodes.add(flow.getSourceTable());
            }
            
            if (!processedNodes.contains(flow.getTargetTable())) {
                Map<String, Object> targetNode = new HashMap<>();
                targetNode.put("id", flow.getTargetTable());
                targetNode.put("name", flow.getTargetTable());
                targetNode.put("category", determineNodeCategory(flow.getTargetTable()));
                nodes.add(targetNode);
                processedNodes.add(flow.getTargetTable());
            }
            
            // 边
            Map<String, Object> link = new HashMap<>();
            link.put("source", flow.getSourceTable());
            link.put("target", flow.getTargetTable());
            link.put("value", flow.getConfidence().doubleValue());
            links.add(link);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("nodes", nodes);
        result.put("links", links);
        
        return result;
    }

    /**
     * 计算节点大小
     */
    private int calculateNodeSize(List<DataFlow> flows, String tableName) {
        long degree = flows.stream()
            .filter(flow -> flow.getSourceTable().equals(tableName) || flow.getTargetTable().equals(tableName))
            .count();
        
        return Math.max(20, Math.min(60, (int) (degree * 5 + 20)));
    }

    /**
     * 根据路径索引计算节点大小
     */
    private int calculateNodeSizeByPathIndex(int pathIndex, int totalPaths) {
        return 30 + (pathIndex * 10) % 30;
    }

    /**
     * 确定节点类别
     */
    private int determineNodeCategory(String tableName) {
        String lowerName = tableName.toLowerCase();
        
        if (lowerName.contains("customer") || lowerName.contains("user")) {
            return 0; // 客户相关
        } else if (lowerName.contains("order") || lowerName.contains("transaction")) {
            return 1; // 交易相关
        } else if (lowerName.contains("product")) {
            return 2; // 产品相关
        } else if (lowerName.contains("account")) {
            return 3; // 账户相关
        } else {
            return 4; // 其他
        }
    }

    /**
     * 创建节点样式
     */
    private Map<String, Object> createNodeStyle(boolean isCenter) {
        Map<String, Object> style = new HashMap<>();
        style.put("color", isCenter ? "#FF6B6B" : "#4ECDC4");
        style.put("borderColor", isCenter ? "#FF4757" : "#26D0CE");
        style.put("borderWidth", isCenter ? 3 : 1);
        return style;
    }

    /**
     * 创建中心节点样式
     */
    private Map<String, Object> createCenterNodeStyle() {
        Map<String, Object> style = new HashMap<>();
        style.put("color", "#FF6B6B");
        style.put("borderColor", "#FF4757");
        style.put("borderWidth", 3);
        return style;
    }

    /**
     * 创建前向节点样式
     */
    private Map<String, Object> createForwardNodeStyle() {
        Map<String, Object> style = new HashMap<>();
        style.put("color", "#4ECDC4");
        style.put("borderColor", "#26D0CE");
        style.put("borderWidth", 1);
        return style;
    }

    /**
     * 创建后向节点样式
     */
    private Map<String, Object> createBackwardNodeStyle() {
        Map<String, Object> style = new HashMap<>();
        style.put("color", "#45B7D1");
        style.put("borderColor", "#3742FA");
        style.put("borderWidth", 1);
        return style;
    }

    /**
     * 创建路径节点样式
     */
    private Map<String, Object> createPathNodeStyle(String color, boolean isEndpoint) {
        Map<String, Object> style = new HashMap<>();
        style.put("color", color);
        style.put("borderColor", color);
        style.put("borderWidth", isEndpoint ? 3 : 1);
        return style;
    }

    /**
     * 创建边样式
     */
    private Map<String, Object> createEdgeStyle(DataFlow flow) {
        Map<String, Object> style = new HashMap<>();
        style.put("color", "#999");
        style.put("width", Math.max(1, flow.getConfidence().doubleValue() * 3));
        style.put("curveness", 0.1);
        return style;
    }

    /**
     * 创建前向边样式
     */
    private Map<String, Object> createForwardEdgeStyle() {
        Map<String, Object> style = new HashMap<>();
        style.put("color", "#4ECDC4");
        style.put("width", 2);
        style.put("curveness", 0.1);
        return style;
    }

    /**
     * 创建后向边样式
     */
    private Map<String, Object> createBackwardEdgeStyle() {
        Map<String, Object> style = new HashMap<>();
        style.put("color", "#45B7D1");
        style.put("width", 2);
        style.put("curveness", 0.1);
        return style;
    }

    /**
     * 创建路径边样式
     */
    private Map<String, Object> createPathEdgeStyle(String color) {
        Map<String, Object> style = new HashMap<>();
        style.put("color", color);
        style.put("width", 2);
        style.put("curveness", 0.2);
        return style;
    }

    /**
     * 创建类别
     */
    private Map<String, Object> createCategory(String name, String label, String color) {
        Map<String, Object> category = new HashMap<>();
        category.put("name", name);
        category.put("label", label);
        category.put("color", color);
        return category;
    }

    /**
     * 创建类别列表
     */
    private List<Map<String, Object>> createCategories() {
        return Arrays.asList(
            createCategory("customer", "客户相关", "#FF6B6B"),
            createCategory("transaction", "交易相关", "#4ECDC4"),
            createCategory("product", "产品相关", "#45B7D1"),
            createCategory("account", "账户相关", "#F7B731"),
            createCategory("other", "其他", "#A4B0BE")
        );
    }

    /**
     * 获取路径颜色
     */
    private String getPathColor(int pathIndex) {
        String[] colors = {
            "#FF6B6B", "#4ECDC4", "#45B7D1", "#F7B731", "#A4B0BE",
            "#FF9FF3", "#54A0FF", "#5F27CD", "#00D2D3", "#FF9F43"
        };
        return colors[pathIndex % colors.length];
    }

    /**
     * 生成HTML内容
     */
    private String generateHtmlContent(String chartType, Map<String, Object> chartData, String title) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("<meta charset=\"utf-8\">\n");
        html.append("<title>").append(title).append("</title>\n");
        html.append("<script src=\"https://cdn.jsdelivr.net/npm/echarts@5.4.0/dist/echarts.min.js\"></script>\n");
        html.append("<style>\n");
        html.append("body { margin: 0; padding: 20px; font-family: Arial, sans-serif; }\n");
        html.append("#chart { width: 100%; height: 600px; border: 1px solid #ccc; }\n");
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("<h2>").append(title).append("</h2>\n");
        html.append("<div id=\"chart\"></div>\n");
        html.append("<script>\n");
        html.append("var chart = echarts.init(document.getElementById('chart'));\n");
        html.append("var option = ").append(convertToJson(chartData)).append(";\n");
        html.append("chart.setOption(option);\n");
        html.append("window.addEventListener('resize', function() { chart.resize(); });\n");
        html.append("</script>\n");
        html.append("</body>\n");
        html.append("</html>\n");
        
        return html.toString();
    }

    /**
     * 转换为JSON字符串
     */
    private String convertToJson(Map<String, Object> data) {
        // 这里应该使用JSON序列化库
        // 简化处理，返回基本格式
        return data.toString().replace("=", ":");
    }
}