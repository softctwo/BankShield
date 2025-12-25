package com.bankshield.api.service.lineage;

import com.bankshield.api.entity.DataFlow;
import com.bankshield.api.entity.DataMap;
import com.bankshield.api.entity.DataSource;
import com.bankshield.api.mapper.DataFlowMapper;
import com.bankshield.api.mapper.DataMapMapper;
import com.bankshield.api.mapper.DataSourceMapper;
import com.bankshield.common.exception.BusinessExceptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据地图服务
 * 生成和管理数据地图，提供数据资产的可视化展示
 */
@Service
public class DataMapService {

    private static final Logger log = LoggerFactory.getLogger(DataMapService.class);

    private final DataMapMapper dataMapMapper;
    private final DataFlowMapper dataFlowMapper;
    private final DataSourceMapper dataSourceMapper;

    public DataMapService(DataMapMapper dataMapMapper, DataFlowMapper dataFlowMapper, DataSourceMapper dataSourceMapper) {
        this.dataMapMapper = dataMapMapper;
        this.dataFlowMapper = dataFlowMapper;
        this.dataSourceMapper = dataSourceMapper;
    }

    /**
     * 生成全局数据地图
     */
    public DataMap generateGlobalDataMap() {
        try {
            log.info("开始生成全局数据地图");
            
             // 获取所有数据流关系
             List<DataFlow> allFlows = dataFlowMapper.selectList(null);
             List<DataSource> allDataSources = dataSourceMapper.selectList(null);
            
            // 构建地图数据
            MapData mapData = buildMapData(allFlows, allDataSources);
            
            // 创建数据地图
            DataMap dataMap = createDataMap("全局数据地图", "GLOBAL", mapData);
            
            log.info("全局数据地图生成完成，包含 {} 个节点，{} 条关系", 
                mapData.getNodes().size(), mapData.getEdges().size());
            
            return dataMap;
            
        } catch (Exception e) {
            log.error("生成全局数据地图失败", e);
            BusinessExceptionUtils.throwBusinessOperation("生成全局数据地图", e.getMessage());
        }
    }

    /**
     * 生成业务域数据地图
     */
    public DataMap generateBusinessDomainMap(String businessDomain) {
        try {
            log.info("开始生成业务域数据地图: {}", businessDomain);
            
            // 根据业务域过滤数据流
            List<DataFlow> domainFlows = filterFlowsByBusinessDomain(businessDomain);
            
            // 构建地图数据
            MapData mapData = buildMapData(domainFlows, null);
            
            // 创建数据地图
            DataMap dataMap = createDataMap(businessDomain + " 业务域地图", "BUSINESS_DOMAIN", mapData);
            dataMap.setScope("{\"businessDomain\": \"" + businessDomain + "\"}");
            
            log.info("业务域数据地图生成完成: {}，包含 {} 个节点，{} 条关系", 
                businessDomain, mapData.getNodes().size(), mapData.getEdges().size());
            
            return dataMap;
            
        } catch (Exception e) {
            log.error("生成业务域数据地图失败: {}", businessDomain, e);
            BusinessExceptionUtils.throwBusinessOperation("生成业务域数据地图", e.getMessage());
        }
    }

    /**
     * 生成数据源数据地图
     */
    public DataMap generateDataSourceMap(Long dataSourceId) {
        try {
            log.info("开始生成数据源数据地图: {}", dataSourceId);
            
            // 根据数据源过滤数据流
            List<DataFlow> sourceFlows = dataFlowMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataFlow>()
                    .eq("data_source_id", dataSourceId)
            );
            
            DataSource dataSource = dataSourceMapper.selectById(dataSourceId);
            
            // 构建地图数据
            MapData mapData = buildMapData(sourceFlows, Arrays.asList(dataSource));
            
            // 创建数据地图
            DataMap dataMap = createDataMap(dataSource.getSourceName() + " 数据源地图", "DATA_SOURCE", mapData);
            dataMap.setScope("{\"dataSourceId\": " + dataSourceId + "}");
            
            log.info("数据源数据地图生成完成: {}，包含 {} 个节点，{} 条关系", 
                dataSource.getSourceName(), mapData.getNodes().size(), mapData.getEdges().size());
            
            return dataMap;
            
        } catch (Exception e) {
            log.error("生成数据源数据地图失败: {}", dataSourceId, e);
            BusinessExceptionUtils.throwBusinessOperation("生成数据源数据地图", e.getMessage());
        }
    }

    /**
     * 生成自定义数据地图
     */
    public DataMap generateCustomDataMap(String mapName, List<String> includedTables, 
                                        List<String> includedDataSources, Map<String, Object> layoutConfig) {
        try {
            log.info("开始生成自定义数据地图: {}", mapName);
            
            // 根据条件过滤数据流
            List<DataFlow> filteredFlows = filterFlowsByCustomCriteria(includedTables, includedDataSources);
            
            // 构建地图数据
            MapData mapData = buildMapData(filteredFlows, null);
            
            // 应用布局配置
            applyLayoutConfig(mapData, layoutConfig);
            
            // 创建数据地图
            DataMap dataMap = createDataMap(mapName, "CUSTOM", mapData);
            dataMap.setConfig(layoutConfig.toString()); // 这里应该使用JSON序列化
            
            log.info("自定义数据地图生成完成: {}，包含 {} 个节点，{} 条关系", 
                mapName, mapData.getNodes().size(), mapData.getEdges().size());
            
            return dataMap;
            
        } catch (Exception e) {
            log.error("生成自定义数据地图失败: {}", mapName, e);
            BusinessExceptionUtils.throwBusinessOperation("生成自定义数据地图", e.getMessage());
        }
    }

    /**
     * 构建地图数据
     */
    private MapData buildMapData(List<DataFlow> flows, List<DataSource> dataSources) {
        MapData mapData = new MapData();
        
        // 构建节点
        Set<String> processedNodes = new HashSet<>();
        
        for (DataFlow flow : flows) {
            // 源节点
            String sourceNodeId = flow.getSourceTable();
            if (!processedNodes.contains(sourceNodeId)) {
                Node sourceNode = createNode(sourceNodeId, flow.getSourceTable(), "table", determineNodeCategory(flow.getSourceTable()));
                mapData.addNode(sourceNode);
                processedNodes.add(sourceNodeId);
            }
            
            // 目标节点
            String targetNodeId = flow.getTargetTable();
            if (!processedNodes.contains(targetNodeId)) {
                Node targetNode = createNode(targetNodeId, flow.getTargetTable(), "table", determineNodeCategory(flow.getTargetTable()));
                mapData.addNode(targetNode);
                processedNodes.add(targetNodeId);
            }
            
            // 边
            Edge edge = createEdge(flow);
            mapData.addEdge(edge);
        }
        
        // 如果有数据源信息，添加数据源节点
        if (dataSources != null) {
            for (DataSource dataSource : dataSources) {
                Node dataSourceNode = createNode("ds_" + dataSource.getId(), dataSource.getSourceName(), "datasource", "datasource");
                mapData.addNode(dataSourceNode);
            }
        }
        
        // 计算节点统计信息
        calculateNodeStatistics(mapData, flows);
        
        return mapData;
    }

    /**
     * 创建节点
     */
    private Node createNode(String id, String name, String type, String category) {
        Node node = new Node();
        node.setId(id);
        node.setName(name);
        node.setType(type);
        node.setCategory(category);
        node.setProperties(new HashMap<>());
        return node;
    }

    /**
     * 创建边
     */
    private Edge createEdge(DataFlow flow) {
        Edge edge = new Edge();
        edge.setId("edge_" + flow.getId());
        edge.setSource(flow.getSourceTable());
        edge.setTarget(flow.getTargetTable());
        edge.setLabel(buildEdgeLabel(flow));
        edge.setType(flow.getFlowType());
        edge.setWeight(flow.getConfidence().doubleValue());
        edge.setProperties(new HashMap<>());
        
        // 添加额外属性
        edge.getProperties().put("sourceColumn", flow.getSourceColumn());
        edge.getProperties().put("targetColumn", flow.getTargetColumn());
        edge.getProperties().put("transformation", flow.getTransformation());
        edge.getProperties().put("discoveryMethod", flow.getDiscoveryMethod());
        edge.getProperties().put("confidence", flow.getConfidence());
        
        return edge;
    }

    /**
     * 构建边标签
     */
    private String buildEdgeLabel(DataFlow flow) {
        StringBuilder label = new StringBuilder();
        
        if (flow.getSourceColumn() != null && flow.getTargetColumn() != null) {
            label.append(flow.getSourceColumn()).append(" → ").append(flow.getTargetColumn());
        } else {
            label.append("数据流转");
        }
        
        if (flow.getTransformation() != null && !flow.getTransformation().isEmpty()) {
            label.append("\\n").append(flow.getTransformation());
        }
        
        return label.toString();
    }

    /**
     * 确定节点类别
     */
    private String determineNodeCategory(String tableName) {
        String lowerName = tableName.toLowerCase();
        
        if (lowerName.contains("customer") || lowerName.contains("user") || lowerName.contains("client")) {
            return "customer";
        } else if (lowerName.contains("order") || lowerName.contains("transaction") || lowerName.contains("payment")) {
            return "transaction";
        } else if (lowerName.contains("product") || lowerName.contains("item")) {
            return "product";
        } else if (lowerName.contains("account") || lowerName.contains("balance")) {
            return "account";
        } else if (lowerName.contains("log") || lowerName.contains("audit") || lowerName.contains("history")) {
            return "log";
        } else if (lowerName.contains("config") || lowerName.contains("setting")) {
            return "config";
        } else {
            return "other";
        }
    }

    /**
     * 计算节点统计信息
     */
    private void calculateNodeStatistics(MapData mapData, List<DataFlow> flows) {
        for (Node node : mapData.getNodes()) {
            if ("table".equals(node.getType())) {
                // 计算入度和出度
                long inDegree = flows.stream()
                    .filter(flow -> flow.getTargetTable().equals(node.getName()))
                    .count();
                
                long outDegree = flows.stream()
                    .filter(flow -> flow.getSourceTable().equals(node.getName()))
                    .count();
                
                node.getProperties().put("inDegree", inDegree);
                node.getProperties().put("outDegree", outDegree);
                node.getProperties().put("degree", inDegree + outDegree);
                
                // 计算数据质量指标
                double avgConfidence = flows.stream()
                    .filter(flow -> flow.getSourceTable().equals(node.getName()) || 
                                   flow.getTargetTable().equals(node.getName()))
                    .mapToDouble(flow -> flow.getConfidence().doubleValue())
                    .average()
                    .orElse(0.0);
                
                node.getProperties().put("avgConfidence", avgConfidence);
            }
        }
    }

    /**
     * 应用布局配置
     */
    private void applyLayoutConfig(MapData mapData, Map<String, Object> layoutConfig) {
        String layoutType = (String) layoutConfig.getOrDefault("type", "force");
        
        switch (layoutType.toLowerCase()) {
            case "force":
                applyForceLayout(mapData);
                break;
            case "hierarchical":
                applyHierarchicalLayout(mapData);
                break;
            case "circular":
                applyCircularLayout(mapData);
                break;
            case "grid":
                applyGridLayout(mapData);
                break;
            default:
                applyForceLayout(mapData);
        }
    }

    /**
     * 应用力导向布局
     */
    private void applyForceLayout(MapData mapData) {
        // 简化的力导向布局算法
        Random random = new Random();
        
        for (Node node : mapData.getNodes()) {
            // 随机初始位置
            node.setX(random.nextDouble() * 1000);
            node.setY(random.nextDouble() * 600);
            
            // 根据节点度数调整大小
            Long degree = (Long) node.getProperties().getOrDefault("degree", 1L);
            node.setSize(Math.max(20, Math.min(60, degree * 5 + 20)));
        }
        
        // 简单的力导向迭代
        for (int iteration = 0; iteration < 50; iteration++) {
            // 斥力
            for (Node node1 : mapData.getNodes()) {
                for (Node node2 : mapData.getNodes()) {
                    if (node1 != node2) {
                        applyRepulsionForce(node1, node2);
                    }
                }
            }
            
            // 引力
            for (Edge edge : mapData.getEdges()) {
                Node sourceNode = findNodeById(mapData, edge.getSource());
                Node targetNode = findNodeById(mapData, edge.getTarget());
                if (sourceNode != null && targetNode != null) {
                    applyAttractionForce(sourceNode, targetNode);
                }
            }
        }
    }

    /**
     * 应用层次布局
     */
    private void applyHierarchicalLayout(MapData mapData) {
        // 简单的层次布局
        Map<String, Integer> nodeLevels = calculateNodeLevels(mapData);
        
        Map<Integer, List<Node>> levelNodes = new HashMap<>();
        for (Node node : mapData.getNodes()) {
            int level = nodeLevels.getOrDefault(node.getId(), 0);
            levelNodes.computeIfAbsent(level, k -> new ArrayList<>()).add(node);
        }
        
        int maxLevel = levelNodes.keySet().stream().max(Integer::compareTo).orElse(0);
        
        for (Map.Entry<Integer, List<Node>> entry : levelNodes.entrySet()) {
            int level = entry.getKey();
            List<Node> nodes = entry.getValue();
            
            double y = (double) level / maxLevel * 600;
            double spacing = nodes.size() > 1 ? 1000.0 / (nodes.size() - 1) : 500;
            
            for (int i = 0; i < nodes.size(); i++) {
                Node node = nodes.get(i);
                node.setX(i * spacing);
                node.setY(y);
                node.setSize(30);
            }
        }
    }

    /**
     * 应用圆形布局
     */
    private void applyCircularLayout(MapData mapData) {
        List<Node> nodes = new ArrayList<>(mapData.getNodes());
        int nodeCount = nodes.size();
        
        if (nodeCount == 0) return;
        
        double centerX = 500;
        double centerY = 300;
        double radius = Math.min(centerX, centerY) - 50;
        
        for (int i = 0; i < nodeCount; i++) {
            Node node = nodes.get(i);
            double angle = 2 * Math.PI * i / nodeCount;
            
            node.setX(centerX + radius * Math.cos(angle));
            node.setY(centerY + radius * Math.sin(angle));
            node.setSize(25);
        }
    }

    /**
     * 应用网格布局
     */
    private void applyGridLayout(MapData mapData) {
        List<Node> nodes = new ArrayList<>(mapData.getNodes());
        int nodeCount = nodes.size();
        
        if (nodeCount == 0) return;
        
        int cols = (int) Math.ceil(Math.sqrt(nodeCount));
        int rows = (int) Math.ceil((double) nodeCount / cols);
        
        double cellWidth = 1000.0 / cols;
        double cellHeight = 600.0 / rows;
        
        for (int i = 0; i < nodeCount; i++) {
            Node node = nodes.get(i);
            int row = i / cols;
            int col = i % cols;
            
            node.setX(col * cellWidth + cellWidth / 2);
            node.setY(row * cellHeight + cellHeight / 2);
            node.setSize(20);
        }
    }

    /**
     * 应用斥力
     */
    private void applyRepulsionForce(Node node1, Node node2) {
        double dx = node2.getX() - node1.getX();
        double dy = node2.getY() - node1.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance < 1) distance = 1;
        
        double force = 1000 / (distance * distance);
        double fx = force * dx / distance;
        double fy = force * dy / distance;
        
        node1.setX(node1.getX() - fx * 0.01);
        node1.setY(node1.getY() - fy * 0.01);
    }

    /**
     * 应用引力
     */
    private void applyAttractionForce(Node source, Node target) {
        double dx = target.getX() - source.getX();
        double dy = target.getY() - source.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance < 1) distance = 1;
        
        double force = distance * 0.001;
        double fx = force * dx / distance;
        double fy = force * dy / distance;
        
        source.setX(source.getX() + fx);
        source.setY(source.getY() + fy);
        target.setX(target.getX() - fx);
        target.setY(target.getY() - fy);
    }

    /**
     * 计算节点层次
     */
    private Map<String, Integer> calculateNodeLevels(MapData mapData) {
        Map<String, Integer> levels = new HashMap<>();
        Map<String, Set<String>> dependencies = new HashMap<>();
        
        // 构建依赖关系
        for (Edge edge : mapData.getEdges()) {
            dependencies.computeIfAbsent(edge.getTarget(), k -> new HashSet<>()).add(edge.getSource());
        }
        
        // 计算层次
        for (Node node : mapData.getNodes()) {
            int level = calculateNodeLevel(node.getId(), dependencies, new HashMap<>(), new HashSet<>());
            levels.put(node.getId(), level);
        }
        
        return levels;
    }

    /**
     * 计算单个节点的层次
     */
    private int calculateNodeLevel(String nodeId, Map<String, Set<String>> dependencies, 
                                  Map<String, Integer> cachedLevels, Set<String> visited) {
        if (cachedLevels.containsKey(nodeId)) {
            return cachedLevels.get(nodeId);
        }
        
        if (visited.contains(nodeId)) {
            return 0; // 避免循环依赖
        }
        
        visited.add(nodeId);
        
        Set<String> deps = dependencies.getOrDefault(nodeId, new HashSet<>());
        if (deps.isEmpty()) {
            cachedLevels.put(nodeId, 0);
            return 0;
        }
        
        int maxDepLevel = 0;
        for (String dep : deps) {
            int depLevel = calculateNodeLevel(dep, dependencies, cachedLevels, new HashSet<>(visited));
            maxDepLevel = Math.max(maxDepLevel, depLevel);
        }
        
        int level = maxDepLevel + 1;
        cachedLevels.put(nodeId, level);
        return level;
    }

    /**
     * 查找节点
     */
    private Node findNodeById(MapData mapData, String nodeId) {
        return mapData.getNodes().stream()
            .filter(node -> node.getId().equals(nodeId))
            .findFirst()
            .orElse(null);
    }

    /**
     * 创建数据地图
     */
    private DataMap createDataMap(String mapName, String mapType, MapData mapData) {
        DataMap dataMap = new DataMap();
        dataMap.setMapName(mapName);
        dataMap.setMapType(mapType);
        dataMap.setNodeCount(mapData.getNodes().size());
        dataMap.setRelationshipCount(mapData.getEdges().size());
        dataMap.setMapData(mapData.toJson()); // 这里应该使用JSON序列化
        dataMap.setVersion(1);
        dataMap.setIsDefault(false);
        dataMap.setStatus("ACTIVE");
        dataMap.setCreateBy(1L); // 默认创建人
        dataMap.setCreateTime(LocalDateTime.now());
        dataMap.setUpdateTime(LocalDateTime.now());
        
        dataMapMapper.insert(dataMap);
        return dataMap;
    }

    /**
     * 按业务域过滤数据流
     */
    private List<DataFlow> filterFlowsByBusinessDomain(String businessDomain) {
        // 这里应该实现根据业务域过滤的逻辑
        // 简化处理，返回所有数据流
        return dataFlowMapper.selectList(null);
    }

    /**
     * 按自定义条件过滤数据流
     */
    private List<DataFlow> filterFlowsByCustomCriteria(List<String> includedTables, List<String> includedDataSources) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataFlow> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        
        if (includedTables != null && !includedTables.isEmpty()) {
            wrapper.in("source_table", includedTables)
                   .or()
                   .in("target_table", includedTables);
        }
        
        if (includedDataSources != null && !includedDataSources.isEmpty()) {
            // 这里需要根据数据源名称查找对应的ID
            // 简化处理
        }
        
        return dataFlowMapper.selectList(wrapper);
    }

    /**
     * 获取数据地图
     */
    public DataMap getDataMap(Long mapId) {
        return dataMapMapper.selectById(mapId);
    }

    /**
     * 获取所有活跃的数据地图
     */
    public List<DataMap> getActiveDataMaps() {
        return dataMapMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataMap>()
                .eq("status", "ACTIVE")
        );
    }

    /**
     * 获取默认数据地图
     */
    public DataMap getDefaultDataMap() {
        return dataMapMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataMap>()
                .eq("is_default", true)
                .eq("status", "ACTIVE")
        );
    }

    /**
     * 设置默认数据地图
     */
    public boolean setDefaultDataMap(Long mapId) {
        // 取消现有的默认地图
        List<DataMap> existingDefaults = dataMapMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataMap>()
                .eq("is_default", true)
        );
        
        for (DataMap map : existingDefaults) {
            map.setIsDefault(false);
            dataMapMapper.updateById(map);
        }
        
        // 设置新的默认地图
        DataMap newDefault = dataMapMapper.selectById(mapId);
        if (newDefault != null) {
            newDefault.setIsDefault(true);
            dataMapMapper.updateById(newDefault);
            return true;
        }
        
        return false;
    }

    /**
     * 更新数据地图
     */
    public boolean updateDataMap(Long mapId, String mapName, Map<String, Object> config) {
        DataMap dataMap = dataMapMapper.selectById(mapId);
        if (dataMap != null) {
            dataMap.setMapName(mapName);
            dataMap.setConfig(config.toString()); // 这里应该使用JSON序列化
            dataMap.setUpdateTime(LocalDateTime.now());
            dataMapMapper.updateById(dataMap);
            return true;
        }
        return false;
    }

    /**
     * 删除数据地图
     */
    public boolean deleteDataMap(Long mapId) {
        DataMap dataMap = dataMapMapper.selectById(mapId);
        if (dataMap != null && !dataMap.getIsDefault()) {
            dataMapMapper.deleteById(mapId);
            return true;
        }
        return false;
    }

    /**
     * 获取数据地图统计信息
     */
    public Map<String, Object> getDataMapStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 基础统计
        stats.put("totalMaps", dataMapMapper.selectCount(null));
        stats.put("activeMaps", dataMapMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataMap>()
                .eq("status", "ACTIVE")
        ));
        stats.put("defaultMaps", dataMapMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataMap>()
                .eq("is_default", true)
        ));
        
        // 类型分布
        Map<String, Integer> typeDistribution = new HashMap<>();
        List<DataMap> allMaps = dataMapMapper.selectList(null);
        for (DataMap map : allMaps) {
            typeDistribution.merge(map.getMapType(), 1, Integer::sum);
        }
        stats.put("typeDistribution", typeDistribution);
        
        return stats;
    }

    /**
     * 地图数据类
     */
    private static class MapData {
        private List<Node> nodes = new ArrayList<>();
        private List<Edge> edges = new ArrayList<>();
        
        public void addNode(Node node) {
            nodes.add(node);
        }
        
        public void addEdge(Edge edge) {
            edges.add(edge);
        }
        
        public List<Node> getNodes() {
            return nodes;
        }
        
        public List<Edge> getEdges() {
            return edges;
        }
        
        public String toJson() {
            // 这里应该实现JSON序列化
            return "{\"nodes\": " + nodes.size() + ", \"edges\": " + edges.size() + "}";
        }
    }

    /**
     * 节点类
     */
    private static class Node {
        private String id;
        private String name;
        private String type;
        private String category;
        private double x;
        private double y;
        private double size;
        private Map<String, Object> properties;
        
        // getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public double getX() { return x; }
        public void setX(double x) { this.x = x; }
        public double getY() { return y; }
        public void setY(double y) { this.y = y; }
        public double getSize() { return size; }
        public void setSize(double size) { this.size = size; }
        public Map<String, Object> getProperties() { return properties; }
        public void setProperties(Map<String, Object> properties) { this.properties = properties; }
    }

    /**
     * 边类
     */
    private static class Edge {
        private String id;
        private String source;
        private String target;
        private String label;
        private String type;
        private double weight;
        private Map<String, Object> properties;
        
        // getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public double getWeight() { return weight; }
        public void setWeight(double weight) { this.weight = weight; }
        public Map<String, Object> getProperties() { return properties; }
        public void setProperties(Map<String, Object> properties) { this.properties = properties; }
    }
}