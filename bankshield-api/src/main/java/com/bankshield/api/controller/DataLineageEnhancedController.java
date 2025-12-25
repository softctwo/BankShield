package com.bankshield.api.controller;

import com.bankshield.common.result.CommonResult;
import com.bankshield.common.result.Result;
import com.bankshield.api.entity.DataFlow;
import com.bankshield.api.entity.DataImpactAnalysis;
import com.bankshield.api.entity.DataLineageAutoDiscovery;
import com.bankshield.api.entity.DataMap;
import com.bankshield.api.service.lineage.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 数据血缘增强控制器
 * 提供数据血缘发现、影响分析、数据地图等功能的REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/lineage/enhanced")
@Api(tags = "数据血缘增强模块")
@RequiredArgsConstructor
public class DataLineageEnhancedController {

    private final LineageDiscoveryService lineageDiscoveryService;
    private final ImpactAnalysisService impactAnalysisService;
    private final DataMapService dataMapService;
    private final LineageVisualizationService visualizationService;

    // ==================== 血缘发现相关接口 ====================

    @PostMapping("/discovery/task")
    @ApiOperation("创建血缘发现任务")
    public CommonResult<DataLineageAutoDiscovery> createDiscoveryTask(
            @ApiParam("任务名称") @RequestParam String taskName,
            @ApiParam("数据源ID") @RequestParam Long dataSourceId,
            @ApiParam("发现策略") @RequestParam String discoveryStrategy,
            @ApiParam("任务配置（JSON格式）") @RequestBody(required = false) Map<String, Object> config) {
        
        try {
            DataLineageAutoDiscovery task = lineageDiscoveryService.createDiscoveryTask(
                taskName, dataSourceId, discoveryStrategy, config);
            
            // 启动异步任务
            CompletableFuture<DataLineageAutoDiscovery> future = lineageDiscoveryService.startDiscoveryTask(task.getId());
            
            return CommonResult.success(task, "血缘发现任务创建成功");
        } catch (Exception e) {
            log.error("创建血缘发现任务失败", e);
            return CommonResult.failed("创建血缘发现任务失败: " + e.getMessage());
        }
    }

    @GetMapping("/discovery/task/{taskId}")
    @ApiOperation("获取血缘发现任务状态")
    public CommonResult<DataLineageAutoDiscovery> getDiscoveryTaskStatus(
            @ApiParam("任务ID") @PathVariable Long taskId) {
        
        try {
            DataLineageAutoDiscovery task = lineageDiscoveryService.getTaskStatus(taskId);
            if (task == null) {
                return CommonResult.failed("任务不存在");
            }
            return CommonResult.success(task);
        } catch (Exception e) {
            log.error("获取任务状态失败: {}", taskId, e);
            return CommonResult.failed("获取任务状态失败: " + e.getMessage());
        }
    }

    @PostMapping("/discovery/task/{taskId}/cancel")
    @ApiOperation("取消血缘发现任务")
    public CommonResult<Boolean> cancelDiscoveryTask(
            @ApiParam("任务ID") @PathVariable Long taskId) {
        
        try {
            boolean result = lineageDiscoveryService.cancelTask(taskId);
            return CommonResult.success(result, result ? "任务取消成功" : "任务取消失败");
        } catch (Exception e) {
            log.error("取消任务失败: {}", taskId, e);
            return CommonResult.failed("取消任务失败: " + e.getMessage());
        }
    }

    @GetMapping("/discovery/tasks/recent")
    @ApiOperation("获取最近的血缘发现任务")
    public CommonResult<List<DataLineageAutoDiscovery>> getRecentDiscoveryTasks(
            @ApiParam("返回任务数量") @RequestParam(defaultValue = "10") int limit) {
        
        try {
            List<DataLineageAutoDiscovery> tasks = lineageDiscoveryService.getRecentTasks(limit);
            return CommonResult.success(tasks);
        } catch (Exception e) {
            log.error("获取最近任务失败", e);
            return CommonResult.failed("获取最近任务失败: " + e.getMessage());
        }
    }

    @GetMapping("/discovery/statistics")
    @ApiOperation("获取血缘发现统计信息")
    public CommonResult<Map<String, Object>> getDiscoveryStatistics() {
        
        try {
            Map<String, Object> statistics = lineageDiscoveryService.getTaskStatistics();
            return CommonResult.success(statistics);
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return CommonResult.failed("获取统计信息失败: " + e.getMessage());
        }
    }

    // ==================== 影响分析相关接口 ====================

    @PostMapping("/impact-analysis")
    @ApiOperation("创建影响分析任务")
    public CommonResult<DataImpactAnalysis> createImpactAnalysis(
            @ApiParam("分析名称") @RequestParam String analysisName,
            @ApiParam("分析类型") @RequestParam String analysisType,
            @ApiParam("影响对象类型") @RequestParam String impactObjectType,
            @ApiParam("影响对象名称") @RequestParam String impactObjectName,
            @ApiParam("分析目标（JSON格式）") @RequestBody Map<String, Object> analysisTarget,
            @ApiParam("创建人ID") @RequestParam Long createBy) {
        
        try {
            DataImpactAnalysis analysis = impactAnalysisService.createImpactAnalysis(
                analysisName, analysisType, impactObjectType, impactObjectName, analysisTarget, createBy);
            
            // 启动异步分析
            CompletableFuture<DataImpactAnalysis> future = impactAnalysisService.performImpactAnalysis(analysis.getId());
            
            return CommonResult.success(analysis, "影响分析任务创建成功");
        } catch (Exception e) {
            log.error("创建影响分析任务失败", e);
            return CommonResult.failed("创建影响分析任务失败: " + e.getMessage());
        }
    }

    @GetMapping("/impact-analysis/{analysisId}")
    @ApiOperation("获取影响分析结果")
    public CommonResult<DataImpactAnalysis> getImpactAnalysisResult(
            @ApiParam("分析ID") @PathVariable Long analysisId) {
        
        try {
            DataImpactAnalysis result = impactAnalysisService.getAnalysisResult(analysisId);
            if (result == null) {
                return CommonResult.failed("分析结果不存在");
            }
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("获取影响分析结果失败: {}", analysisId, e);
            return CommonResult.failed("获取影响分析结果失败: " + e.getMessage());
        }
    }

    @GetMapping("/impact-analysis/recent")
    @ApiOperation("获取最近的影响分析任务")
    public CommonResult<List<DataImpactAnalysis>> getRecentImpactAnalyses(
            @ApiParam("返回任务数量") @RequestParam(defaultValue = "10") int limit) {
        
        try {
            List<DataImpactAnalysis> analyses = impactAnalysisService.getRecentAnalyses(limit);
            return CommonResult.success(analyses);
        } catch (Exception e) {
            log.error("获取最近分析失败", e);
            return CommonResult.failed("获取最近分析失败: " + e.getMessage());
        }
    }

    @GetMapping("/impact-analysis/statistics")
    @ApiOperation("获取影响分析统计信息")
    public CommonResult<Map<String, Object>> getImpactAnalysisStatistics() {
        
        try {
            Map<String, Object> statistics = impactAnalysisService.getAnalysisStatistics();
            return CommonResult.success(statistics);
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return CommonResult.failed("获取统计信息失败: " + e.getMessage());
        }
    }

    // ==================== 数据地图相关接口 ====================

    @PostMapping("/data-map/global")
    @ApiOperation("生成全局数据地图")
    public CommonResult<DataMap> generateGlobalDataMap() {
        
        try {
            DataMap dataMap = dataMapService.generateGlobalDataMap();
            return CommonResult.success(dataMap, "全局数据地图生成成功");
        } catch (Exception e) {
            log.error("生成全局数据地图失败", e);
            return CommonResult.failed("生成全局数据地图失败: " + e.getMessage());
        }
    }

    @PostMapping("/data-map/business-domain")
    @ApiOperation("生成业务域数据地图")
    public CommonResult<DataMap> generateBusinessDomainMap(
            @ApiParam("业务域名称") @RequestParam String businessDomain) {
        
        try {
            DataMap dataMap = dataMapService.generateBusinessDomainMap(businessDomain);
            return CommonResult.success(dataMap, "业务域数据地图生成成功");
        } catch (Exception e) {
            log.error("生成业务域数据地图失败: {}", businessDomain, e);
            return CommonResult.failed("生成业务域数据地图失败: " + e.getMessage());
        }
    }

    @PostMapping("/data-map/data-source/{dataSourceId}")
    @ApiOperation("生成数据源数据地图")
    public CommonResult<DataMap> generateDataSourceMap(
            @ApiParam("数据源ID") @PathVariable Long dataSourceId) {
        
        try {
            DataMap dataMap = dataMapService.generateDataSourceMap(dataSourceId);
            return CommonResult.success(dataMap, "数据源数据地图生成成功");
        } catch (Exception e) {
            log.error("生成数据源数据地图失败: {}", dataSourceId, e);
            return CommonResult.failed("生成数据源数据地图失败: " + e.getMessage());
        }
    }

    @PostMapping("/data-map/custom")
    @ApiOperation("生成自定义数据地图")
    public CommonResult<DataMap> generateCustomDataMap(
            @ApiParam("地图名称") @RequestParam String mapName,
            @ApiParam("包含的表名列表") @RequestParam(required = false) List<String> includedTables,
            @ApiParam("包含的数据源列表") @RequestParam(required = false) List<String> includedDataSources,
            @ApiParam("布局配置（JSON格式）") @RequestBody(required = false) Map<String, Object> layoutConfig) {
        
        try {
            DataMap dataMap = dataMapService.generateCustomDataMap(mapName, includedTables, includedDataSources, layoutConfig);
            return CommonResult.success(dataMap, "自定义数据地图生成成功");
        } catch (Exception e) {
            log.error("生成自定义数据地图失败: {}", mapName, e);
            return CommonResult.failed("生成自定义数据地图失败: " + e.getMessage());
        }
    }

    @GetMapping("/data-map/{mapId}")
    @ApiOperation("获取数据地图")
    public CommonResult<DataMap> getDataMap(
            @ApiParam("地图ID") @PathVariable Long mapId) {
        
        try {
            DataMap dataMap = dataMapService.getDataMap(mapId);
            if (dataMap == null) {
                return CommonResult.failed("数据地图不存在");
            }
            return CommonResult.success(dataMap);
        } catch (Exception e) {
            log.error("获取数据地图失败: {}", mapId, e);
            return CommonResult.failed("获取数据地图失败: " + e.getMessage());
        }
    }

    @GetMapping("/data-map/active")
    @ApiOperation("获取所有活跃的数据地图")
    public CommonResult<List<DataMap>> getActiveDataMaps() {
        
        try {
            List<DataMap> dataMaps = dataMapService.getActiveDataMaps();
            return CommonResult.success(dataMaps);
        } catch (Exception e) {
            log.error("获取活跃数据地图失败", e);
            return CommonResult.failed("获取活跃数据地图失败: " + e.getMessage());
        }
    }

    @GetMapping("/data-map/default")
    @ApiOperation("获取默认数据地图")
    public CommonResult<DataMap> getDefaultDataMap() {
        
        try {
            DataMap dataMap = dataMapService.getDefaultDataMap();
            if (dataMap == null) {
                return CommonResult.failed("默认数据地图不存在");
            }
            return CommonResult.success(dataMap);
        } catch (Exception e) {
            log.error("获取默认数据地图失败", e);
            return CommonResult.failed("获取默认数据地图失败: " + e.getMessage());
        }
    }

    @PutMapping("/data-map/{mapId}/default")
    @ApiOperation("设置默认数据地图")
    public CommonResult<Boolean> setDefaultDataMap(
            @ApiParam("地图ID") @PathVariable Long mapId) {
        
        try {
            boolean result = dataMapService.setDefaultDataMap(mapId);
            return CommonResult.success(result, result ? "设置默认数据地图成功" : "设置默认数据地图失败");
        } catch (Exception e) {
            log.error("设置默认数据地图失败: {}", mapId, e);
            return CommonResult.failed("设置默认数据地图失败: " + e.getMessage());
        }
    }

    @PutMapping("/data-map/{mapId}")
    @ApiOperation("更新数据地图")
    public CommonResult<Boolean> updateDataMap(
            @ApiParam("地图ID") @PathVariable Long mapId,
            @ApiParam("地图名称") @RequestParam String mapName,
            @ApiParam("布局配置（JSON格式）") @RequestBody(required = false) Map<String, Object> config) {
        
        try {
            boolean result = dataMapService.updateDataMap(mapId, mapName, config);
            return CommonResult.success(result, result ? "更新数据地图成功" : "更新数据地图失败");
        } catch (Exception e) {
            log.error("更新数据地图失败: {}", mapId, e);
            return CommonResult.failed("更新数据地图失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/data-map/{mapId}")
    @ApiOperation("删除数据地图")
    public CommonResult<Boolean> deleteDataMap(
            @ApiParam("地图ID") @PathVariable Long mapId) {
        
        try {
            boolean result = dataMapService.deleteDataMap(mapId);
            return CommonResult.success(result, result ? "删除数据地图成功" : "删除数据地图失败");
        } catch (Exception e) {
            log.error("删除数据地图失败: {}", mapId, e);
            return CommonResult.failed("删除数据地图失败: " + e.getMessage());
        }
    }

    @GetMapping("/data-map/statistics")
    @ApiOperation("获取数据地图统计信息")
    public CommonResult<Map<String, Object>> getDataMapStatistics() {
        
        try {
            Map<String, Object> statistics = dataMapService.getDataMapStatistics();
            return CommonResult.success(statistics);
        } catch (Exception e) {
            log.error("获取数据地图统计信息失败", e);
            return CommonResult.failed("获取数据地图统计信息失败: " + e.getMessage());
        }
    }

    // ==================== 可视化相关接口 ====================

    @GetMapping("/visualization/lineage-graph")
    @ApiOperation("生成血缘关系图")
    public CommonResult<Map<String, Object>> generateLineageGraph(
            @ApiParam("表名") @RequestParam String tableName,
            @ApiParam("字段名") @RequestParam(required = false) String columnName,
            @ApiParam("深度") @RequestParam(defaultValue = "2") int depth) {
        
        try {
            Map<String, Object> graphData = visualizationService.generateLineageGraph(tableName, columnName, depth);
            return CommonResult.success(graphData, "血缘关系图生成成功");
        } catch (Exception e) {
            log.error("生成血缘关系图失败: {}.{}", tableName, columnName, e);
            return CommonResult.failed("生成血缘关系图失败: " + e.getMessage());
        }
    }

    @GetMapping("/visualization/impact-analysis")
    @ApiOperation("生成影响分析图")
    public CommonResult<Map<String, Object>> generateImpactAnalysisGraph(
            @ApiParam("表名") @RequestParam String tableName,
            @ApiParam("字段名") @RequestParam(required = false) String columnName) {
        
        try {
            Map<String, Object> graphData = visualizationService.generateImpactAnalysisGraph(tableName, columnName);
            return CommonResult.success(graphData, "影响分析图生成成功");
        } catch (Exception e) {
            log.error("生成影响分析图失败: {}.{}", tableName, columnName, e);
            return CommonResult.failed("生成影响分析图失败: " + e.getMessage());
        }
    }

    @GetMapping("/visualization/3d-data-map/{mapId}")
    @ApiOperation("生成3D数据地图")
    public CommonResult<Map<String, Object>> generate3DDataMap(
            @ApiParam("地图ID") @PathVariable Long mapId) {
        
        try {
            Map<String, Object> graphData = visualizationService.generate3DDataMap(mapId);
            return CommonResult.success(graphData, "3D数据地图生成成功");
        } catch (Exception e) {
            log.error("生成3D数据地图失败: {}", mapId, e);
            return CommonResult.failed("生成3D数据地图失败: " + e.getMessage());
        }
    }

    @GetMapping("/visualization/traceability")
    @ApiOperation("生成溯源路径图")
    public CommonResult<Map<String, Object>> generateTraceabilityGraph(
            @ApiParam("源表名") @RequestParam String sourceTable,
            @ApiParam("源字段名") @RequestParam(required = false) String sourceColumn,
            @ApiParam("目标表名") @RequestParam String targetTable,
            @ApiParam("目标字段名") @RequestParam(required = false) String targetColumn) {
        
        try {
            Map<String, Object> graphData = visualizationService.generateTraceabilityGraph(
                sourceTable, sourceColumn, targetTable, targetColumn);
            return CommonResult.success(graphData, "溯源路径图生成成功");
        } catch (Exception e) {
            log.error("生成溯源路径图失败: {}.{} -> {}.{}", sourceTable, sourceColumn, targetTable, targetColumn, e);
            return CommonResult.failed("生成溯源路径图失败: " + e.getMessage());
        }
    }

    @GetMapping("/visualization/data-flow")
    @ApiOperation("生成数据流向图")
    public CommonResult<Map<String, Object>> generateDataFlowGraph(
            @ApiParam("表名") @RequestParam String tableName,
            @ApiParam("开始时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @ApiParam("结束时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        try {
            Map<String, Object> graphData = visualizationService.generateDataFlowGraph(tableName, startTime, endTime);
            return CommonResult.success(graphData, "数据流向图生成成功");
        } catch (Exception e) {
            log.error("生成数据流向图失败: {}", tableName, e);
            return CommonResult.failed("生成数据流向图失败: " + e.getMessage());
        }
    }

    @PostMapping("/visualization/export-html")
    @ApiOperation("导出可视化图表为HTML")
    public CommonResult<String> exportVisualizationToHtml(
            @ApiParam("图表类型") @RequestParam String chartType,
            @ApiParam("图表数据") @RequestBody Map<String, Object> chartData,
            @ApiParam("图表标题") @RequestParam String title) {
        
        try {
            String filePath = visualizationService.exportVisualizationToHtml(chartType, chartData, title);
            return CommonResult.success(filePath, "可视化图表导出成功");
        } catch (Exception e) {
            log.error("导出可视化图表失败", e);
            return CommonResult.failed("导出可视化图表失败: " + e.getMessage());
        }
    }

    // ==================== 数据查询相关接口 ====================

    @GetMapping("/flows")
    @ApiOperation("查询数据流关系")
    public CommonResult<List<DataFlow>> getDataFlows(
            @ApiParam("源表名") @RequestParam(required = false) String sourceTable,
            @ApiParam("目标表名") @RequestParam(required = false) String targetTable,
            @ApiParam("流转类型") @RequestParam(required = false) String flowType,
            @ApiParam("发现方法") @RequestParam(required = false) String discoveryMethod,
            @ApiParam("最小置信度") @RequestParam(required = false) Double minConfidence) {
        
        try {
            // 这里应该实现查询逻辑
            // 简化处理，返回空列表
            return CommonResult.success(java.util.Collections.emptyList());
        } catch (Exception e) {
            log.error("查询数据流关系失败", e);
            return CommonResult.failed("查询数据流关系失败: " + e.getMessage());
        }
    }

    @GetMapping("/flows/statistics")
    @ApiOperation("获取数据流统计信息")
    public CommonResult<Map<String, Object>> getDataFlowStatistics() {
        
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 这里应该实现统计查询逻辑
            statistics.put("totalFlows", 0);
            statistics.put("directFlows", 0);
            statistics.put("indirectFlows", 0);
            statistics.put("transformationFlows", 0);
            statistics.put("avgConfidence", 0.0);
            
            return CommonResult.success(statistics);
        } catch (Exception e) {
            log.error("获取数据流统计信息失败", e);
            return CommonResult.failed("获取数据流统计信息失败: " + e.getMessage());
        }
    }
}