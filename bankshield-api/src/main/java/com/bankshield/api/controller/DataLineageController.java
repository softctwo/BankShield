package com.bankshield.api.controller;

import com.bankshield.api.entity.DataLineage;
import com.bankshield.api.service.DataLineageService;
import com.bankshield.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据血缘分析控制器
 * 
 * @author BankShield
 */
@Slf4j
@RestController
@RequestMapping("/api/lineage")
public class DataLineageController {

    @Autowired
    private DataLineageService dataLineageService;

    /**
     * 解析SQL，构建数据血缘
     */
    @PostMapping("/parse")
    public Result<String> parseLineage(@RequestBody Map<String, String> params) {
        String sql = params.get("sql");
        String dataSource = params.get("dataSource");
        
        log.info("解析SQL血缘，数据源: {}", dataSource);
        return dataLineageService.parseLineage(sql, dataSource);
    }

    /**
     * 查询字段血缘链路
     */
    @GetMapping("/field")
    public Result<List<DataLineageService.LineagePath>> getFieldLineage(
            @RequestParam String tableName,
            @RequestParam String fieldName) {
        log.info("查询字段血缘链路，表名: {}, 字段名: {}", tableName, fieldName);
        return dataLineageService.getFieldLineage(tableName, fieldName);
    }

    /**
     * 查询表的上游血缘
     */
    @GetMapping("/upstream")
    public Result<List<DataLineage>> getUpstreamLineage(@RequestParam String tableName) {
        log.info("查询表的上游血缘，表名: {}", tableName);
        return dataLineageService.getUpstreamLineage(tableName);
    }

    /**
     * 查询表的下游血缘
     */
    @GetMapping("/downstream")
    public Result<List<DataLineage>> getDownstreamLineage(@RequestParam String tableName) {
        log.info("查询表的下游血缘，表名: {}", tableName);
        return dataLineageService.getDownstreamLineage(tableName);
    }

    /**
     * 构建完整的血缘图谱
     */
    @GetMapping("/graph/{assetId}")
    public Result<DataLineageService.LineageGraph> buildLineageGraph(@PathVariable Long assetId) {
        log.info("构建血缘图谱，资产ID: {}", assetId);
        return dataLineageService.buildLineageGraph(assetId);
    }

    /**
     * 识别跨境、跨安全域传输
     */
    @GetMapping("/transfer-risk/{assetId}")
    public Result<DataLineageService.TransferRisk> analyzeTransferRisk(@PathVariable Long assetId) {
        log.info("分析传输风险，资产ID: {}", assetId);
        return dataLineageService.analyzeTransferRisk(assetId);
    }

    /**
     * 批量解析SQL血缘
     */
    @PostMapping("/batch-parse")
    public Result<String> batchParseLineage(@RequestBody List<Map<String, String>> sqlList) {
        log.info("批量解析SQL血缘，数量: {}", sqlList.size());
        
        int successCount = 0;
        for (Map<String, String> sqlInfo : sqlList) {
            String sql = sqlInfo.get("sql");
            String dataSource = sqlInfo.get("dataSource");
            
            Result<String> result = dataLineageService.parseLineage(sql, dataSource);
            if (result.isSuccess()) {
                successCount++;
            }
        }
        
        return Result.OK(String.format("批量解析完成，成功 %d/%d 条SQL", successCount, sqlList.size()));
    }

    /**
     * 获取血缘关系详情
     */
    @GetMapping("/{id}")
    public Result<DataLineage> getLineageById(@PathVariable Long id) {
        log.info("查询血缘关系详情，ID: {}", id);
        // TODO: 实现血缘关系详情查询
        return Result.OK(null);
    }

    /**
     * 删除血缘关系
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteLineage(@PathVariable Long id) {
        log.info("删除血缘关系，ID: {}", id);
        // TODO: 实现血缘关系删除
        return Result.OK("删除成功");
    }

    /**
     * 批量删除血缘关系
     */
    @PostMapping("/batch-delete")
    public Result<String> batchDeleteLineage(@RequestBody List<Long> ids) {
        log.info("批量删除血缘关系，数量: {}", ids.size());
        // TODO: 实现批量删除血缘关系
        return Result.OK("批量删除成功");
    }

    /**
     * 查询资产的所有血缘关系
     */
    @GetMapping("/by-asset/{assetId}")
    public Result<List<DataLineage>> getLineageByAssetId(@PathVariable Long assetId) {
        log.info("查询资产的所有血缘关系，资产ID: {}", assetId);
        // TODO: 实现按资产ID查询血缘关系
        return Result.OK(null);
    }

    /**
     * 查询数据源的所有血缘关系
     */
    @GetMapping("/by-data-source/{dataSourceId}")
    public Result<List<DataLineage>> getLineageByDataSourceId(@PathVariable Long dataSourceId) {
        log.info("查询数据源的所有血缘关系，数据源ID: {}", dataSourceId);
        // TODO: 实现按数据源ID查询血缘关系
        return Result.OK(null);
    }

    /**
     * 血缘关系统计
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getLineageStatistics() {
        log.info("获取血缘关系统计信息");
        // TODO: 实现血缘关系统计
        Map<String, Object> statistics = new java.util.HashMap<>();
        statistics.put("totalRelations", 0);
        statistics.put("activeRelations", 0);
        statistics.put("riskRelations", 0);
        return Result.OK(statistics);
    }
}