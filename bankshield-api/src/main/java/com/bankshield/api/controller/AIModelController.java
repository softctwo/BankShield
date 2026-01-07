package com.bankshield.api.controller;

import com.bankshield.api.entity.AIModel;
import com.bankshield.api.entity.AITrainingJob;
import com.bankshield.api.service.AIModelService;
import com.bankshield.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI模型管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Api(tags = "AI模型管理")
public class AIModelController {

    private final AIModelService aiModelService;

    // ========== 模型管理 ==========

    @GetMapping("/models")
    @ApiOperation("分页查询模型列表")
    public Result<?> getModels(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") int size,
            @ApiParam("模型类型") @RequestParam(required = false) String modelType,
            @ApiParam("状态") @RequestParam(required = false) String status) {
        return Result.success(aiModelService.getModels(page, size, modelType, status));
    }

    @GetMapping("/model/{id}")
    @ApiOperation("获取模型详情")
    public Result<AIModel> getModelById(@PathVariable Long id) {
        return Result.success(aiModelService.getModelById(id));
    }

    @PostMapping("/model")
    @ApiOperation("创建模型")
    public Result<AIModel> createModel(@RequestBody AIModel model) {
        return Result.success(aiModelService.createModel(model));
    }

    @PutMapping("/model/{id}")
    @ApiOperation("更新模型")
    public Result<Boolean> updateModel(@PathVariable Long id, @RequestBody AIModel model) {
        model.setId(id);
        return Result.success(aiModelService.updateModel(model));
    }

    @DeleteMapping("/model/{id}")
    @ApiOperation("删除模型")
    public Result<Boolean> deleteModel(@PathVariable Long id) {
        return Result.success(aiModelService.deleteModel(id));
    }

    @PostMapping("/model/{id}/deploy")
    @ApiOperation("部署模型")
    public Result<Boolean> deployModel(@PathVariable Long id) {
        return Result.success(aiModelService.deployModel(id));
    }

    @PostMapping("/model/{id}/undeploy")
    @ApiOperation("下线模型")
    public Result<Boolean> undeployModel(@PathVariable Long id) {
        return Result.success(aiModelService.undeployModel(id));
    }

    // ========== 训练任务 ==========

    @GetMapping("/training/jobs")
    @ApiOperation("获取训练任务列表")
    public Result<?> getTrainingJobs(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") int size,
            @ApiParam("状态") @RequestParam(required = false) String status) {
        return Result.success(aiModelService.getTrainingJobs(page, size, status));
    }

    @GetMapping("/training/job/{id}")
    @ApiOperation("获取训练任务详情")
    public Result<AITrainingJob> getTrainingJobById(@PathVariable Long id) {
        return Result.success(aiModelService.getTrainingJobById(id));
    }

    @PostMapping("/training/job")
    @ApiOperation("创建训练任务")
    public Result<AITrainingJob> createTrainingJob(@RequestBody AITrainingJob job) {
        return Result.success(aiModelService.createTrainingJob(job));
    }

    @PostMapping("/training/job/{id}/cancel")
    @ApiOperation("取消训练任务")
    public Result<Boolean> cancelTrainingJob(@PathVariable Long id) {
        return Result.success(aiModelService.cancelTrainingJob(id));
    }

    // ========== 模型推理 ==========

    @PostMapping("/inference/{modelId}")
    @ApiOperation("执行模型推理")
    public Result<Map<String, Object>> inference(
            @PathVariable Long modelId,
            @RequestBody Map<String, Object> input) {
        return Result.success(aiModelService.inference(modelId, input));
    }

    @PostMapping("/inference/{modelId}/batch")
    @ApiOperation("批量推理")
    public Result<List<Map<String, Object>>> batchInference(
            @PathVariable Long modelId,
            @RequestBody List<Map<String, Object>> inputs) {
        return Result.success(aiModelService.batchInference(modelId, inputs));
    }

    // ========== 统计与监控 ==========

    @GetMapping("/statistics")
    @ApiOperation("获取模型统计信息")
    public Result<Map<String, Object>> getStatistics() {
        return Result.success(aiModelService.getModelStatistics());
    }

    @GetMapping("/model/{id}/performance")
    @ApiOperation("获取模型性能监控数据")
    public Result<Map<String, Object>> getModelPerformance(
            @PathVariable Long id,
            @ApiParam("时间范围（小时）") @RequestParam(defaultValue = "24") int hours) {
        return Result.success(aiModelService.getModelPerformance(id, hours));
    }

    // ========== 模型导入导出 ==========

    @PostMapping("/model/{id}/export/onnx")
    @ApiOperation("导出模型为ONNX格式")
    public Result<String> exportToONNX(@PathVariable Long id) {
        return Result.success(aiModelService.exportToONNX(id));
    }

    @PostMapping("/model/{id}/export/pmml")
    @ApiOperation("导出模型为PMML格式")
    public Result<String> exportToPMML(@PathVariable Long id) {
        return Result.success(aiModelService.exportToPMML(id));
    }

    @PostMapping("/model/import")
    @ApiOperation("导入模型")
    public Result<AIModel> importModel(
            @ApiParam("模型路径") @RequestParam String modelPath,
            @ApiParam("模型格式") @RequestParam String format) {
        return Result.success(aiModelService.importModel(modelPath, format));
    }
}
