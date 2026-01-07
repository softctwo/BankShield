package com.bankshield.api.controller;

import com.bankshield.api.entity.FederatedLearningJob;
import com.bankshield.api.entity.FederatedParty;
import com.bankshield.api.entity.FederatedRound;
import com.bankshield.api.service.FederatedLearningService;
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
 * 联邦学习控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/federated")
@RequiredArgsConstructor
@Api(tags = "联邦学习")
public class FederatedLearningController {

    private final FederatedLearningService federatedLearningService;

    // ========== 任务管理 ==========

    @PostMapping("/job")
    @ApiOperation("创建联邦学习任务")
    public Result<FederatedLearningJob> createJob(@RequestBody FederatedLearningJob job) {
        return Result.success(federatedLearningService.createJob(job));
    }

    @GetMapping("/jobs")
    @ApiOperation("分页查询任务列表")
    public Result<?> getJobs(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") int size,
            @ApiParam("任务类型") @RequestParam(required = false) String jobType,
            @ApiParam("状态") @RequestParam(required = false) String status) {
        return Result.success(federatedLearningService.getJobs(page, size, jobType, status));
    }

    @GetMapping("/job/{id}")
    @ApiOperation("获取任务详情")
    public Result<FederatedLearningJob> getJobById(@PathVariable Long id) {
        return Result.success(federatedLearningService.getJobById(id));
    }

    @PostMapping("/job/{id}/start")
    @ApiOperation("启动任务")
    public Result<Boolean> startJob(@PathVariable Long id) {
        return Result.success(federatedLearningService.startJob(id));
    }

    @PostMapping("/job/{id}/pause")
    @ApiOperation("暂停任务")
    public Result<Boolean> pauseJob(@PathVariable Long id) {
        return Result.success(federatedLearningService.pauseJob(id));
    }

    @PostMapping("/job/{id}/resume")
    @ApiOperation("恢复任务")
    public Result<Boolean> resumeJob(@PathVariable Long id) {
        return Result.success(federatedLearningService.resumeJob(id));
    }

    @PostMapping("/job/{id}/stop")
    @ApiOperation("停止任务")
    public Result<Boolean> stopJob(@PathVariable Long id) {
        return Result.success(federatedLearningService.stopJob(id));
    }

    @DeleteMapping("/job/{id}")
    @ApiOperation("删除任务")
    public Result<Boolean> deleteJob(@PathVariable Long id) {
        return Result.success(federatedLearningService.deleteJob(id));
    }

    // ========== 训练过程 ==========

    @PostMapping("/job/{id}/init-model")
    @ApiOperation("初始化全局模型")
    public Result<Map<String, Object>> initGlobalModel(@PathVariable Long id) {
        return Result.success(federatedLearningService.initGlobalModel(id));
    }

    @PostMapping("/job/{jobId}/distribute/{roundNumber}")
    @ApiOperation("分发模型")
    public Result<Boolean> distributeModel(
            @PathVariable Long jobId,
            @PathVariable Integer roundNumber) {
        return Result.success(federatedLearningService.distributeModel(jobId, roundNumber));
    }

    @PostMapping("/job/{jobId}/local-update/{partyId}")
    @ApiOperation("接收本地更新")
    public Result<Boolean> receiveLocalUpdate(
            @PathVariable Long jobId,
            @PathVariable String partyId,
            @RequestBody Map<String, Object> modelUpdate) {
        return Result.success(federatedLearningService.receiveLocalUpdate(jobId, partyId, modelUpdate));
    }

    @PostMapping("/job/{jobId}/aggregate/{roundNumber}")
    @ApiOperation("执行安全聚合")
    public Result<Map<String, Object>> aggregateModels(
            @PathVariable Long jobId,
            @PathVariable Integer roundNumber) {
        return Result.success(federatedLearningService.aggregateModels(jobId, roundNumber));
    }

    @GetMapping("/job/{id}/rounds")
    @ApiOperation("获取训练轮次列表")
    public Result<List<FederatedRound>> getRounds(@PathVariable Long id) {
        return Result.success(federatedLearningService.getRounds(id));
    }

    @GetMapping("/job/{id}/global-model")
    @ApiOperation("获取当前全局模型")
    public Result<Map<String, Object>> getGlobalModel(@PathVariable Long id) {
        return Result.success(federatedLearningService.getGlobalModel(id));
    }

    // ========== 参与方管理 ==========

    @PostMapping("/party/register")
    @ApiOperation("注册参与方")
    public Result<FederatedParty> registerParty(@RequestBody FederatedParty party) {
        return Result.success(federatedLearningService.registerParty(party));
    }

    @GetMapping("/parties")
    @ApiOperation("获取参与方列表")
    public Result<List<FederatedParty>> getParties(
            @ApiParam("状态") @RequestParam(required = false) String status) {
        return Result.success(federatedLearningService.getParties(status));
    }

    @GetMapping("/party/{id}")
    @ApiOperation("获取参与方详情")
    public Result<FederatedParty> getPartyById(@PathVariable Long id) {
        return Result.success(federatedLearningService.getPartyById(id));
    }

    @PutMapping("/party/{id}/status")
    @ApiOperation("更新参与方状态")
    public Result<Boolean> updatePartyStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return Result.success(federatedLearningService.updatePartyStatus(id, status));
    }

    @DeleteMapping("/party/{id}")
    @ApiOperation("删除参与方")
    public Result<Boolean> deleteParty(@PathVariable Long id) {
        return Result.success(federatedLearningService.deleteParty(id));
    }

    @PostMapping("/party/{partyId}/heartbeat")
    @ApiOperation("发送心跳")
    public Result<Boolean> sendHeartbeat(@PathVariable String partyId) {
        return Result.success(federatedLearningService.sendHeartbeat(partyId));
    }

    // ========== 安全与隐私 ==========

    @PostMapping("/job/{id}/privacy-config")
    @ApiOperation("配置差分隐私")
    public Result<Boolean> configureDifferentialPrivacy(
            @PathVariable Long id,
            @RequestBody Map<String, Object> privacyConfig) {
        return Result.success(federatedLearningService.configureDifferentialPrivacy(id, privacyConfig));
    }

    @PostMapping("/job/{id}/secure-aggregation")
    @ApiOperation("配置安全聚合")
    public Result<Boolean> configureSecureAggregation(
            @PathVariable Long id,
            @RequestBody Map<String, Object> secureConfig) {
        return Result.success(federatedLearningService.configureSecureAggregation(id, secureConfig));
    }

    @GetMapping("/job/{id}/verify-integrity")
    @ApiOperation("验证模型完整性")
    public Result<Map<String, Object>> verifyModelIntegrity(@PathVariable Long id) {
        return Result.success(federatedLearningService.verifyModelIntegrity(id));
    }

    // ========== 统计与监控 ==========

    @GetMapping("/statistics")
    @ApiOperation("获取统计信息")
    public Result<Map<String, Object>> getStatistics() {
        return Result.success(federatedLearningService.getStatistics());
    }

    @GetMapping("/job/{id}/monitoring")
    @ApiOperation("获取任务监控数据")
    public Result<Map<String, Object>> getJobMonitoring(@PathVariable Long id) {
        return Result.success(federatedLearningService.getJobMonitoring(id));
    }

    @GetMapping("/job/{id}/training-curve")
    @ApiOperation("获取训练曲线")
    public Result<Map<String, Object>> getTrainingCurve(@PathVariable Long id) {
        return Result.success(federatedLearningService.getTrainingCurve(id));
    }

    @GetMapping("/health")
    @ApiOperation("健康检查")
    public Result<Map<String, Object>> healthCheck() {
        return Result.success(federatedLearningService.healthCheck());
    }
}
