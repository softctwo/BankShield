package com.bankshield.lineage.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.common.result.Result;
import com.bankshield.lineage.entity.DataLineageNode;
import com.bankshield.lineage.service.LineageService;
import com.bankshield.lineage.vo.ImpactAnalysis;
import com.bankshield.lineage.vo.LineageGraph;
import com.bankshield.lineage.vo.LineageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据血缘控制器
 *
 * @author BankShield
 * @since 2024-01-24
 */
@Slf4j
@RestController
@RequestMapping("/api/lineage")
@Api(tags = "数据血缘管理")
@RequiredArgsConstructor
public class DataLineageController {

    private final LineageService lineageService;

    /**
     * 构建血缘图谱
     */
    @GetMapping("/graph/{assetId}")
    @ApiOperation("构建血缘图谱")
    public Result<LineageGraph> getLineageGraph(
            @ApiParam("资产ID") @PathVariable Long assetId,
            @ApiParam("深度") @RequestParam(defaultValue = "3") int depth) {
        
        log.info("构建血缘图谱，资产ID：{}，深度：{}", assetId, depth);
        
        try {
            LineageGraph graph = lineageService.buildGraph(assetId, depth);
            return Result.success(graph);
        } catch (Exception e) {
            log.error("构建血缘图谱失败", e);
            return Result.error("构建血缘图谱失败：" + e.getMessage());
        }
    }

    /**
     * 分析影响
     */
    @GetMapping("/impact/{assetId}")
    @ApiOperation("分析影响")
    public Result<ImpactAnalysis> analyzeImpact(
            @ApiParam("资产ID") @PathVariable Long assetId) {
        
        log.info("分析影响，资产ID：{}", assetId);
        
        try {
            ImpactAnalysis impact = lineageService.analyzeImpact(assetId);
            return Result.success(impact);
        } catch (Exception e) {
            log.error("影响分析失败", e);
            return Result.error("影响分析失败：" + e.getMessage());
        }
    }

    /**
     * 分析字段影响
     */
    @GetMapping("/impact/field/{fieldId}")
    @ApiOperation("分析字段影响")
    public Result<ImpactAnalysis> analyzeFieldImpact(
            @ApiParam("字段ID") @PathVariable Long fieldId) {
        
        log.info("分析字段影响，字段ID：{}", fieldId);
        
        try {
            ImpactAnalysis impact = lineageService.analyzeFieldImpact(fieldId);
            return Result.success(impact);
        } catch (Exception e) {
            log.error("字段影响分析失败", e);
            return Result.error("字段影响分析失败：" + e.getMessage());
        }
    }

    /**
     * 分析表影响
     */
    @GetMapping("/impact/table/{tableId}")
    @ApiOperation("分析表影响")
    public Result<ImpactAnalysis> analyzeTableImpact(
            @ApiParam("表ID") @PathVariable Long tableId) {
        
        log.info("分析表影响，表ID：{}", tableId);
        
        try {
            ImpactAnalysis impact = lineageService.analyzeTableImpact(tableId);
            return Result.success(impact);
        } catch (Exception e) {
            log.error("表影响分析失败", e);
            return Result.error("表影响分析失败：" + e.getMessage());
        }
    }

    /**
     * 自动发现血缘关系
     */
    @PostMapping("/auto-discover")
    @ApiOperation("自动发现血缘关系")
    public Result<String> autoDiscover() {
        
        log.info("开始自动发现血缘关系");
        
        try {
            lineageService.discoverFromSqlLogs();
            return Result.success("血缘关系自动发现任务已启动");
        } catch (Exception e) {
            log.error("自动发现血缘关系失败", e);
            return Result.error("自动发现血缘关系失败：" + e.getMessage());
        }
    }

    /**
     * 提取SQL血缘
     */
    @PostMapping("/extract-sql")
    @ApiOperation("提取SQL血缘")
    public Result<LineageInfo> extractSqlLineage(
            @ApiParam("SQL语句") @RequestParam String sql,
            @ApiParam("数据库类型") @RequestParam(defaultValue = "mysql") String dbType) {
        
        log.info("提取SQL血缘，数据库类型：{}", dbType);
        
        try {
            LineageInfo lineageInfo = lineageService.extractSqlLineage(sql, dbType);
            return Result.success(lineageInfo);
        } catch (Exception e) {
            log.error("提取SQL血缘失败", e);
            return Result.error("提取SQL血缘失败：" + e.getMessage());
        }
    }

    /**
     * 查询节点详情
     */
    @GetMapping("/node/{nodeId}")
    @ApiOperation("查询节点详情")
    public Result<DataLineageNode> getNode(
            @ApiParam("节点ID") @PathVariable Long nodeId) {
        
        log.info("查询节点详情，节点ID：{}", nodeId);
        
        try {
            DataLineageNode node = lineageService.getNodeById(nodeId);
            if (node == null) {
                return Result.error("节点不存在");
            }
            return Result.success(node);
        } catch (Exception e) {
            log.error("查询节点详情失败", e);
            return Result.error("查询节点详情失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询节点
     */
    @GetMapping("/nodes")
    @ApiOperation("分页查询节点")
    public Result<IPage<DataLineageNode>> getNodes(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") int size,
            @ApiParam("节点类型") @RequestParam(required = false) String nodeType,
            @ApiParam("节点名称") @RequestParam(required = false) String nodeName,
            @ApiParam("数据源ID") @RequestParam(required = false) Long dataSourceId) {
        
        log.info("分页查询节点，页码：{}，每页条数：{}", page, size);
        
        try {
            Page<DataLineageNode> pageParam = new Page<>(page, size);
            IPage<DataLineageNode> result = lineageService.getNodes(pageParam, nodeType, nodeName, dataSourceId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("分页查询节点失败", e);
            return Result.error("分页查询节点失败：" + e.getMessage());
        }
    }

    /**
     * 查询上游节点
     */
    @GetMapping("/upstream/{nodeId}")
    @ApiOperation("查询上游节点")
    public Result<List<DataLineageNode>> getUpstreamNodes(
            @ApiParam("节点ID") @PathVariable Long nodeId) {
        
        log.info("查询上游节点，节点ID：{}", nodeId);
        
        try {
            List<DataLineageNode> upstreamNodes = lineageService.getUpstreamNodes(nodeId);
            return Result.success(upstreamNodes);
        } catch (Exception e) {
            log.error("查询上游节点失败", e);
            return Result.error("查询上游节点失败：" + e.getMessage());
        }
    }

    /**
     * 查询下游节点
     */
    @GetMapping("/downstream/{nodeId}")
    @ApiOperation("查询下游节点")
    public Result<List<DataLineageNode>> getDownstreamNodes(
            @ApiParam("节点ID") @PathVariable Long nodeId) {
        
        log.info("查询下游节点，节点ID：{}", nodeId);
        
        try {
            List<DataLineageNode> downstreamNodes = lineageService.getDownstreamNodes(nodeId);
            return Result.success(downstreamNodes);
        } catch (Exception e) {
            log.error("查询下游节点失败", e);
            return Result.error("查询下游节点失败：" + e.getMessage());
        }
    }

    /**
     * 查询血缘路径
     */
    @GetMapping("/path")
    @ApiOperation("查询血缘路径")
    public Result<List<LineageGraph.LineageEdge>> getLineagePath(
            @ApiParam("源节点ID") @RequestParam Long sourceNodeId,
            @ApiParam("目标节点ID") @RequestParam Long targetNodeId,
            @ApiParam("最大深度") @RequestParam(defaultValue = "10") Integer maxDepth) {
        
        log.info("查询血缘路径，源节点ID：{}，目标节点ID：{}", sourceNodeId, targetNodeId);
        
        try {
            List<LineageGraph.LineageEdge> path = lineageService.getLineagePath(sourceNodeId, targetNodeId, maxDepth);
            return Result.success(path);
        } catch (Exception e) {
            log.error("查询血缘路径失败", e);
            return Result.error("查询血缘路径失败：" + e.getMessage());
        }
    }

    /**
     * 计算复杂性
     */
    @GetMapping("/complexity/{nodeId}")
    @ApiOperation("计算复杂性")
    public Result<Double> calculateComplexity(
            @ApiParam("节点ID") @PathVariable Long nodeId) {
        
        log.info("计算复杂性，节点ID：{}", nodeId);
        
        try {
            Double complexity = lineageService.calculateComplexity(nodeId);
            return Result.success(complexity);
        } catch (Exception e) {
            log.error("计算复杂性失败", e);
            return Result.error("计算复杂性失败：" + e.getMessage());
        }
    }

    /**
     * 验证血缘关系
     */
    @GetMapping("/validate")
    @ApiOperation("验证血缘关系")
    public Result<Boolean> validateLineage(
            @ApiParam("源节点ID") @RequestParam Long sourceNodeId,
            @ApiParam("目标节点ID") @RequestParam Long targetNodeId) {
        
        log.info("验证血缘关系，源节点ID：{}，目标节点ID：{}", sourceNodeId, targetNodeId);
        
        try {
            boolean isValid = lineageService.validateLineage(sourceNodeId, targetNodeId);
            return Result.success(isValid);
        } catch (Exception e) {
            log.error("验证血缘关系失败", e);
            return Result.error("验证血缘关系失败：" + e.getMessage());
        }
    }

    /**
     * 导入血缘数据
     */
    @PostMapping("/import")
    @ApiOperation("导入血缘数据")
    public Result<String> importLineageData(
            @ApiParam("血缘数据") @RequestBody String lineageData) {
        
        log.info("导入血缘数据");
        
        try {
            boolean success = lineageService.importLineageData(lineageData);
            if (success) {
                return Result.success("血缘数据导入成功");
            } else {
                return Result.error("血缘数据导入失败");
            }
        } catch (Exception e) {
            log.error("导入血缘数据失败", e);
            return Result.error("导入血缘数据失败：" + e.getMessage());
        }
    }

    /**
     * 导出血缘数据
     */
    @GetMapping("/export/{nodeId}")
    @ApiOperation("导出血缘数据")
    public Result<String> exportLineageData(
            @ApiParam("节点ID") @PathVariable Long nodeId,
            @ApiParam("深度") @RequestParam(defaultValue = "3") Integer depth) {
        
        log.info("导出血缘数据，节点ID：{}，深度：{}", nodeId, depth);
        
        try {
            String exportedData = lineageService.exportLineageData(nodeId, depth);
            if (exportedData != null) {
                return Result.success(exportedData);
            } else {
                return Result.error("导出血缘数据失败");
            }
        } catch (Exception e) {
            log.error("导出血缘数据失败", e);
            return Result.error("导出血缘数据失败：" + e.getMessage());
        }
    }
}