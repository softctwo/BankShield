package com.bankshield.api.service.lineage;

import com.bankshield.api.entity.DataFlow;
import com.bankshield.api.entity.DataLineageAutoDiscovery;
import com.bankshield.api.entity.DataSource;
import com.bankshield.api.mapper.DataFlowMapper;
import com.bankshield.api.mapper.DataLineageAutoDiscoveryMapper;
import com.bankshield.api.service.lineage.discovery.LineageDiscoveryEngine;
import com.bankshield.api.service.IDataSourceService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * 血缘发现服务
 * 协调各种血缘发现引擎，执行血缘发现任务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LineageDiscoveryService {

    private final List<LineageDiscoveryEngine> discoveryEngines;
    private final DataLineageAutoDiscoveryMapper discoveryTaskMapper;
    private final DataFlowMapper dataFlowMapper;
    private final IDataSourceService dataSourceService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 启动血缘发现任务
     */
    @Async
    public CompletableFuture<DataLineageAutoDiscovery> startDiscoveryTask(Long taskId) {
        try {
            DataLineageAutoDiscovery task = discoveryTaskMapper.selectById(taskId);
            if (task == null) {
                log.error("血缘发现任务不存在: {}", taskId);
                return CompletableFuture.completedFuture(null);
            }

            // 更新任务状态
            task.setStatus("RUNNING");
            task.setStartTime(LocalDateTime.now());
            discoveryTaskMapper.updateById(task);

            log.info("开始执行血缘发现任务: {}", task.getTaskName());

            // 执行发现
            List<DataFlow> discoveredFlows = executeDiscovery(task);

            // 保存发现结果
            saveDiscoveryResults(task, discoveredFlows);

            // 更新任务状态
            task.setStatus("SUCCESS");
            task.setEndTime(LocalDateTime.now());
            task.setDiscoveredFlowsCount(discoveredFlows.size());
            discoveryTaskMapper.updateById(task);

            log.info("血缘发现任务完成: {}, 发现 {} 条血缘关系", task.getTaskName(), discoveredFlows.size());

            return CompletableFuture.completedFuture(task);

        } catch (Exception e) {
            log.error("血缘发现任务执行失败: {}", taskId, e);
            
            // 更新任务状态为失败
            DataLineageAutoDiscovery task = discoveryTaskMapper.selectById(taskId);
            if (task != null) {
                task.setStatus("FAILED");
                task.setEndTime(LocalDateTime.now());
                task.setErrorMessage(e.getMessage());
                discoveryTaskMapper.updateById(task);
            }
            
            return CompletableFuture.completedFuture(task);
        }
    }

    /**
     * 执行血缘发现
     */
    private List<DataFlow> executeDiscovery(DataLineageAutoDiscovery task) {
        List<DataFlow> allFlows = new ArrayList<>();
        
        try {
            // 获取数据源
            DataSource dataSource = dataSourceService.getById(task.getDataSourceId());
            if (dataSource == null) {
                throw new RuntimeException("数据源不存在: " + task.getDataSourceId());
            }

            // 解析任务配置
            Map<String, Object> config = parseTaskConfig(task.getConfig());
            
            // 根据发现策略选择引擎
            String discoveryStrategy = task.getDiscoveryStrategy();
            
            if ("ALL".equalsIgnoreCase(discoveryStrategy)) {
                // 使用所有可用的引擎
                for (LineageDiscoveryEngine engine : discoveryEngines) {
                    try {
                        log.info("使用 {} 策略进行血缘发现", engine.getStrategy());
                        List<DataFlow> flows = engine.discoverLineage(dataSource, config);
                        allFlows.addAll(flows);
                        log.info("{} 策略发现 {} 条血缘关系", engine.getStrategy(), flows.size());
                    } catch (Exception e) {
                        log.error("{} 策略执行失败", engine.getStrategy(), e);
                    }
                }
            } else {
                // 使用指定的策略
                LineageDiscoveryEngine engine = discoveryEngines.stream()
                    .filter(e -> discoveryStrategy.equals(e.getStrategy()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("不支持的发现策略: " + discoveryStrategy));
                
                log.info("使用 {} 策略进行血缘发现", engine.getStrategy());
                allFlows = engine.discoverLineage(dataSource, config);
                log.info("{} 策略发现 {} 条血缘关系", engine.getStrategy(), allFlows.size());
            }
            
            // 去重和合并
            allFlows = deduplicateAndMergeFlows(allFlows);
            
            // 验证和过滤
            allFlows = validateAndFilterFlows(allFlows);
            
        } catch (Exception e) {
            log.error("血缘发现执行失败", e);
            throw new RuntimeException("血缘发现执行失败: " + e.getMessage(), e);
        }
        
        return allFlows;
    }

    /**
     * 保存发现结果
     */
    @Transactional
    private void saveDiscoveryResults(DataLineageAutoDiscovery task, List<DataFlow> discoveredFlows) {
        if (discoveredFlows.isEmpty()) {
            return;
        }

        log.info("保存血缘发现结果: {} 条关系", discoveredFlows.size());

        int batchSize = 100;
        for (int i = 0; i < discoveredFlows.size(); i += batchSize) {
            int end = Math.min(i + batchSize, discoveredFlows.size());
            List<DataFlow> batch = discoveredFlows.subList(i, end);
            
            for (DataFlow flow : batch) {
                try {
                    // 检查是否已存在相同的关系
                    if (!isFlowExists(flow)) {
                        flow.setDiscoveryTime(LocalDateTime.now());
                        flow.setLastUpdated(LocalDateTime.now());
                        dataFlowMapper.insert(flow);
                    } else {
                        // 更新现有关系的置信度和发现时间
                        updateExistingFlow(flow);
                    }
                } catch (Exception e) {
                    log.error("保存血缘关系失败: {} -> {}", flow.getSourceTable(), flow.getTargetTable(), e);
                }
            }
        }
    }

    /**
     * 去重和合并血缘关系
     */
    private List<DataFlow> deduplicateAndMergeFlows(List<DataFlow> flows) {
        Map<String, DataFlow> flowMap = new HashMap<>();
        
        for (DataFlow flow : flows) {
            String key = buildFlowKey(flow);
            
            if (flowMap.containsKey(key)) {
                // 合并相似的血缘关系
                DataFlow existingFlow = flowMap.get(key);
                mergeFlows(existingFlow, flow);
            } else {
                flowMap.put(key, flow);
            }
        }
        
        return new ArrayList<>(flowMap.values());
    }

    /**
     * 验证和过滤血缘关系
     */
    private List<DataFlow> validateAndFilterFlows(List<DataFlow> flows) {
        return flows.stream()
            .filter(this::isValidFlow)
            .filter(flow -> flow.getConfidence().doubleValue() >= 0.5) // 置信度阈值
            .collect(Collectors.toList());
    }

    /**
     * 检查血缘关系是否已存在
     */
    private boolean isFlowExists(DataFlow flow) {
        // 这里应该实现数据库查询逻辑
        // 简化处理，返回false
        return false;
    }

    /**
     * 更新现有的血缘关系
     */
    private void updateExistingFlow(DataFlow flow) {
        // 这里应该实现更新逻辑
        // 可以更新置信度、发现时间等
    }

    /**
     * 合并两个血缘关系
     */
    private void mergeFlows(DataFlow existingFlow, DataFlow newFlow) {
        // 更新置信度（取最大值）
        if (newFlow.getConfidence().compareTo(existingFlow.getConfidence()) > 0) {
            existingFlow.setConfidence(newFlow.getConfidence());
        }
        
        // 更新发现方法（如果新的方法更可靠）
        if (isMoreReliableMethod(newFlow.getDiscoveryMethod(), existingFlow.getDiscoveryMethod())) {
            existingFlow.setDiscoveryMethod(newFlow.getDiscoveryMethod());
        }
        
        // 更新转换逻辑（如果新的更详细）
        if (newFlow.getTransformation() != null && 
            newFlow.getTransformation().length() > existingFlow.getTransformation().length()) {
            existingFlow.setTransformation(newFlow.getTransformation());
        }
        
        existingFlow.setLastUpdated(LocalDateTime.now());
    }

    /**
     * 构建血缘关系键
     */
    private String buildFlowKey(DataFlow flow) {
        return String.format("%s.%s->%s.%s", 
            flow.getSourceTable(), 
            flow.getSourceColumn() != null ? flow.getSourceColumn() : "*",
            flow.getTargetTable(), 
            flow.getTargetColumn() != null ? flow.getTargetColumn() : "*");
    }

    /**
     * 验证血缘关系的有效性
     */
    private boolean isValidFlow(DataFlow flow) {
        if (flow.getSourceTable() == null || flow.getTargetTable() == null) {
            return false;
        }
        
        if (flow.getSourceTable().equals(flow.getTargetTable()) && 
            Objects.equals(flow.getSourceColumn(), flow.getTargetColumn())) {
            return false; // 自引用且同字段
        }
        
        if (flow.getFlowType() == null) {
            return false;
        }
        
        return true;
    }

    /**
     * 判断发现方法是否更可靠
     */
    private boolean isMoreReliableMethod(String newMethod, String existingMethod) {
        // 方法可靠性排序
        Map<String, Integer> reliabilityScore = Map.of(
            "SQL_PARSE", 4,
            "METADATA", 3,
            "LOG_ANALYSIS", 2,
            "ML_INFERENCE", 1
        );
        
        return reliabilityScore.getOrDefault(newMethod, 0) > 
               reliabilityScore.getOrDefault(existingMethod, 0);
    }

    /**
     * 解析任务配置
     */
    private Map<String, Object> parseTaskConfig(String configJson) {
        if (configJson == null || configJson.trim().isEmpty()) {
            log.debug("任务配置为空，使用默认配置");
            return getDefaultConfig();
        }

        try {
            // 使用Jackson解析JSON配置
            Map<String, Object> config = objectMapper.readValue(configJson, new TypeReference<Map<String, Object>>() {});
            log.debug("成功解析任务配置: {}", config);
            return config;
        } catch (Exception e) {
            log.warn("解析任务配置失败，使用默认配置，错误: {}", e.getMessage());
            return getDefaultConfig();
        }
    }

    /**
     * 获取默认配置
     */
    private Map<String, Object> getDefaultConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("scanTables", true);
        config.put("scanViews", true);
        config.put("scanProcedures", true);
        config.put("scanTriggers", false);
        config.put("confidenceThreshold", 0.5);
        config.put("maxDepth", 5);
        return config;
    }

    /**
     * 创建血缘发现任务
     */
    public DataLineageAutoDiscovery createDiscoveryTask(String taskName, Long dataSourceId, String discoveryStrategy, Map<String, Object> config) {
        DataLineageAutoDiscovery task = new DataLineageAutoDiscovery();
        task.setTaskName(taskName);
        task.setDataSourceId(dataSourceId);
        task.setDiscoveryStrategy(discoveryStrategy);
        task.setStatus("PENDING");
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        // 使用JSON序列化配置
        if (config != null && !config.isEmpty()) {
            try {
                String configJson = objectMapper.writeValueAsString(config);
                task.setConfig(configJson);
                log.debug("任务配置序列化成功: {}", configJson);
            } catch (Exception e) {
                log.error("任务配置序列化失败，使用默认配置", e);
                task.setConfig(null);
            }
        } else {
            log.debug("任务配置为空，将使用默认配置");
        }

        discoveryTaskMapper.insert(task);
        return task;
    }

    /**
     * 获取任务状态
     */
    public DataLineageAutoDiscovery getTaskStatus(Long taskId) {
        return discoveryTaskMapper.selectById(taskId);
    }

    /**
     * 取消任务
     */
    public boolean cancelTask(Long taskId) {
        DataLineageAutoDiscovery task = discoveryTaskMapper.selectById(taskId);
        if (task != null && "RUNNING".equals(task.getStatus())) {
            task.setStatus("CANCELLED");
            task.setEndTime(LocalDateTime.now());
            discoveryTaskMapper.updateById(task);
            return true;
        }
        return false;
    }

    /**
     * 获取最近的任务列表
     */
    public List<DataLineageAutoDiscovery> getRecentTasks(int limit) {
        // 这里应该实现查询逻辑
        // 简化处理，返回空列表
        return new ArrayList<>();
    }

    /**
     * 获取任务统计信息
     */
    public Map<String, Object> getTaskStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 这里应该实现统计查询逻辑
        stats.put("totalTasks", 0);
        stats.put("runningTasks", 0);
        stats.put("completedTasks", 0);
        stats.put("failedTasks", 0);
        
        return stats;
    }
}