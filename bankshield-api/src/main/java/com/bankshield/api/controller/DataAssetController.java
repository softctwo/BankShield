package com.bankshield.api.controller;

import com.bankshield.api.entity.DataAsset;
import com.bankshield.api.entity.ClassificationHistory;
import com.bankshield.api.service.ClassificationService;
import com.bankshield.api.service.AutoClassificationService;
import com.bankshield.api.service.AssetMapService;
import com.bankshield.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据资产管理控制器
 * 
 * @author BankShield
 */
@Slf4j
@RestController
@RequestMapping("/api/asset")
public class DataAssetController {

    @Autowired
    private ClassificationService classificationService;

    @Autowired
    private AutoClassificationService autoClassificationService;

    @Autowired
    private AssetMapService assetMapService;

    /**
     * 启动资产发现任务
     */
    @PostMapping("/discover")
    public Result<Long> discoverAssets(@RequestParam Long dataSourceId) {
        log.info("启动资产发现任务，数据源ID: {}", dataSourceId);
        return autoClassificationService.scanDataSource(dataSourceId);
    }

    /**
     * 查询资产详情
     */
    @GetMapping("/{id}")
    public Result<ClassificationHistory> getAssetById(@PathVariable Long id) {
        log.info("查询资产详情，ID: {}", id);
        return classificationService.getClassificationHistory(id);
    }

    /**
     * 人工标注分级
     */
    @PutMapping("/{id}/classify")
    public Result<String> classifyAsset(@PathVariable Long id, 
                                       @RequestParam Integer manualLevel,
                                       @RequestParam Long operatorId) {
        log.info("人工标注资产分级，资产ID: {}, 等级: {}, 操作人: {}", id, manualLevel, operatorId);
        return classificationService.manualClassify(id, manualLevel, operatorId);
    }

    /**
     * 提交审核
     */
    @PostMapping("/{id}/review")
    public Result<String> submitReview(@PathVariable Long id,
                                      @RequestParam Integer finalLevel,
                                      @RequestParam(required = false) String reason) {
        log.info("提交资产审核，资产ID: {}, 最终等级: {}", id, finalLevel);
        return classificationService.submitReview(id, finalLevel, reason);
    }

    /**
     * 审核通过/拒绝
     */
    @PutMapping("/{id}/approve")
    public Result<String> approveAsset(@PathVariable Long id,
                                      @RequestParam Boolean approved,
                                      @RequestParam(required = false) String comment,
                                      @RequestParam Long reviewerId) {
        log.info("审核资产，资产ID: {}, 是否通过: {}, 审核人: {}", id, approved, reviewerId);
        return classificationService.reviewClassification(id, approved, comment, reviewerId);
    }

    /**
     * 分页查询资产列表
     */
    @GetMapping("/list")
    public Result<Page<DataAsset>> getAssetList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String assetName,
            @RequestParam(required = false) String assetType,
            @RequestParam(required = false) Integer securityLevel,
            @RequestParam(required = false) String businessLine,
            @RequestParam(required = false) Integer status) {
        log.info("分页查询资产列表，页码: {}, 每页大小: {}", page, size);
        // TODO: 实现带条件的分页查询
        return Result.success(new Page<>());
    }

    /**
     * 资产地图概览
     */
    @GetMapping("/map/overview")
    public Result<AssetMapService.AssetOverview> getAssetOverview() {
        log.info("获取资产地图概览");
        return assetMapService.getOverview();
    }

    /**
     * 资产下钻查询
     */
    @GetMapping("/map/drill-down")
    public Result<AssetMapService.DrillDownResult> drillDown(
            @RequestParam String dimension,
            @RequestParam(required = false) String dimensionValue,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        log.info("资产下钻查询，维度: {}, 值: {}", dimension, dimensionValue);
        
        AssetMapService.DrillDownQuery query = new AssetMapService.DrillDownQuery();
        query.setDimension(dimension);
        query.setDimensionValue(dimensionValue);
        query.setPage(page);
        query.setSize(size);
        query.setKeyword(keyword);
        
        return assetMapService.drillDown(query);
    }

    /**
     * 查询血缘链路
     */
    @GetMapping("/lineage/{id}")
    public Result<List<DataAsset>> getAssetLineage(@PathVariable Long id) {
        log.info("查询资产血缘链路，资产ID: {}", id);
        // TODO: 实现血缘链路查询
        return Result.success(null);
    }

    /**
     * 获取扫描进度
     */
    @GetMapping("/scan-progress/{taskId}")
    public Result<AutoClassificationService.ScanProgress> getScanProgress(@PathVariable Long taskId) {
        log.info("获取扫描进度，任务ID: {}", taskId);
        return autoClassificationService.getScanProgress(taskId);
    }

    /**
     * 停止扫描任务
     */
    @PostMapping("/scan-stop/{taskId}")
    public Result<String> stopScanTask(@PathVariable Long taskId) {
        log.info("停止扫描任务，任务ID: {}", taskId);
        return autoClassificationService.stopScanTask(taskId);
    }

    /**
     * 批量审核通过
     */
    @PostMapping("/batch-approve")
    public Result<String> batchApproveAssets(@RequestBody Map<String, Object> params) {
        Long[] assetIds = ((List<Integer>) params.get("assetIds")).stream()
                .map(Integer::longValue)
                .toArray(Long[]::new);
        Long reviewerId = ((Integer) params.get("reviewerId")).longValue();
        
        log.info("批量审核通过资产，数量: {}, 审核人: {}", assetIds.length, reviewerId);
        return classificationService.batchApproveAssets(assetIds, reviewerId);
    }

    /**
     * 批量审核拒绝
     */
    @PostMapping("/batch-reject")
    public Result<String> batchRejectAssets(@RequestBody Map<String, Object> params) {
        Long[] assetIds = ((List<Integer>) params.get("assetIds")).stream()
                .map(Integer::longValue)
                .toArray(Long[]::new);
        String comment = (String) params.get("comment");
        Long reviewerId = ((Integer) params.get("reviewerId")).longValue();
        
        log.info("批量审核拒绝资产，数量: {}, 审核人: {}", assetIds.length, reviewerId);
        return classificationService.batchRejectAssets(assetIds, comment, reviewerId);
    }

    /**
     * 获取待审核资产列表
     */
    @GetMapping("/pending-review")
    public Result<Page<DataAsset>> getPendingReviewAssets(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("获取待审核资产列表，页码: {}, 每页大小: {}", page, size);
        return classificationService.getPendingReviewAssets(page, size);
    }

    /**
     * 获取风险资产清单
     */
    @GetMapping("/risk-assets")
    public Result<List<AssetMapService.RiskAsset>> getRiskAssets(@RequestParam(required = false) String riskLevel) {
        log.info("获取风险资产清单，风险等级: {}", riskLevel);
        return assetMapService.getRiskAssets(riskLevel);
    }

    /**
     * 获取业务条线分布
     */
    @GetMapping("/business-line-distribution")
    public Result<List<AssetMapService.BusinessLineDistribution>> getBusinessLineDistribution() {
        log.info("获取业务条线分布");
        return assetMapService.getBusinessLineDistribution();
    }

    /**
     * 获取存储位置分布
     */
    @GetMapping("/storage-distribution")
    public Result<List<AssetMapService.StorageDistribution>> getStorageDistribution() {
        log.info("获取存储位置分布");
        return assetMapService.getStorageDistribution();
    }

    /**
     * 导出资产清单
     */
    @GetMapping("/export")
    public Result<String> exportAssetList(@RequestParam(required = false, defaultValue = "EXCEL") String exportType) {
        log.info("导出资产清单，导出类型: {}", exportType);
        return assetMapService.exportAssetList(exportType);
    }
}