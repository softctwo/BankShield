package com.bankshield.api.controller;

import com.bankshield.api.entity.MpcJob;
import com.bankshield.api.entity.MpcParty;
import com.bankshield.api.service.MpcService;
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
 * MPC多方安全计算控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/mpc")
@RequiredArgsConstructor
@Api(tags = "MPC多方安全计算")
public class MpcController {

    private final MpcService mpcService;

    // ========== PSI隐私求交 ==========

    @PostMapping("/psi")
    @ApiOperation("创建PSI任务")
    @SuppressWarnings("unchecked")
    public Result<MpcJob> createPSIJob(@RequestBody Map<String, Object> request) {
        String jobName = (String) request.get("jobName");
        List<String> participantIds = (List<String>) request.get("participantIds");
        List<String> localDataSet = (List<String>) request.get("localDataSet");
        return Result.success(mpcService.createPSIJob(jobName, participantIds, localDataSet));
    }

    @PostMapping("/psi/{jobId}/execute")
    @ApiOperation("执行PSI计算")
    public Result<Map<String, Object>> executePSI(@PathVariable Long jobId) {
        return Result.success(mpcService.executePSI(jobId));
    }

    // ========== 安全求和 ==========

    @PostMapping("/secure-sum")
    @ApiOperation("创建安全求和任务")
    @SuppressWarnings("unchecked")
    public Result<MpcJob> createSecureSumJob(@RequestBody Map<String, Object> request) {
        String jobName = (String) request.get("jobName");
        List<String> participantIds = (List<String>) request.get("participantIds");
        Double localValue = ((Number) request.get("localValue")).doubleValue();
        return Result.success(mpcService.createSecureSumJob(jobName, participantIds, localValue));
    }

    @PostMapping("/secure-sum/{jobId}/execute")
    @ApiOperation("执行安全求和")
    public Result<Map<String, Object>> executeSecureSum(@PathVariable Long jobId) {
        return Result.success(mpcService.executeSecureSum(jobId));
    }

    // ========== 联合查询 ==========

    @PostMapping("/joint-query")
    @ApiOperation("创建联合查询任务")
    @SuppressWarnings("unchecked")
    public Result<MpcJob> createJointQueryJob(@RequestBody Map<String, Object> request) {
        String jobName = (String) request.get("jobName");
        List<String> participantIds = (List<String>) request.get("participantIds");
        Map<String, Object> queryParams = (Map<String, Object>) request.get("queryParams");
        return Result.success(mpcService.createJointQueryJob(jobName, participantIds, queryParams));
    }

    @PostMapping("/joint-query/{jobId}/execute")
    @ApiOperation("执行联合查询")
    public Result<Map<String, Object>> executeJointQuery(@PathVariable Long jobId) {
        return Result.success(mpcService.executeJointQuery(jobId));
    }

    // ========== 安全比较 ==========

    @PostMapping("/secure-compare")
    @ApiOperation("创建安全比较任务")
    public Result<MpcJob> createSecureCompareJob(@RequestBody Map<String, Object> request) {
        String jobName = (String) request.get("jobName");
        String participantId = (String) request.get("participantId");
        Double localValue = ((Number) request.get("localValue")).doubleValue();
        return Result.success(mpcService.createSecureCompareJob(jobName, participantId, localValue));
    }

    @PostMapping("/secure-compare/{jobId}/execute")
    @ApiOperation("执行安全比较")
    public Result<Map<String, Object>> executeSecureCompare(@PathVariable Long jobId) {
        return Result.success(mpcService.executeSecureCompare(jobId));
    }

    // ========== 任务管理 ==========

    @GetMapping("/jobs")
    @ApiOperation("分页查询任务列表")
    public Result<?> getJobs(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") int size,
            @ApiParam("任务类型") @RequestParam(required = false) String jobType,
            @ApiParam("状态") @RequestParam(required = false) String status) {
        return Result.success(mpcService.getJobs(page, size, jobType, status));
    }

    @GetMapping("/job/{id}")
    @ApiOperation("获取任务详情")
    public Result<MpcJob> getJobById(@PathVariable Long id) {
        return Result.success(mpcService.getJobById(id));
    }

    @PostMapping("/job/{id}/cancel")
    @ApiOperation("取消任务")
    public Result<Boolean> cancelJob(@PathVariable Long id) {
        return Result.success(mpcService.cancelJob(id));
    }

    @PostMapping("/job/{id}/retry")
    @ApiOperation("重试任务")
    public Result<MpcJob> retryJob(@PathVariable Long id) {
        return Result.success(mpcService.retryJob(id));
    }

    // ========== 参与方管理 ==========

    @PostMapping("/party/register")
    @ApiOperation("注册参与方")
    public Result<MpcParty> registerParty(@RequestBody MpcParty party) {
        return Result.success(mpcService.registerParty(party));
    }

    @GetMapping("/parties")
    @ApiOperation("获取参与方列表")
    public Result<List<MpcParty>> getParties(
            @ApiParam("状态") @RequestParam(required = false) String status) {
        return Result.success(mpcService.getParties(status));
    }

    @GetMapping("/party/{id}")
    @ApiOperation("获取参与方详情")
    public Result<MpcParty> getPartyById(@PathVariable Long id) {
        return Result.success(mpcService.getPartyById(id));
    }

    @PutMapping("/party/{id}/status")
    @ApiOperation("更新参与方状态")
    public Result<Boolean> updatePartyStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return Result.success(mpcService.updatePartyStatus(id, status));
    }

    @DeleteMapping("/party/{id}")
    @ApiOperation("删除参与方")
    public Result<Boolean> deleteParty(@PathVariable Long id) {
        return Result.success(mpcService.deleteParty(id));
    }

    @PostMapping("/party/{partyId}/heartbeat")
    @ApiOperation("发送心跳")
    public Result<Boolean> sendHeartbeat(@PathVariable String partyId) {
        return Result.success(mpcService.sendHeartbeat(partyId));
    }

    // ========== 统计与监控 ==========

    @GetMapping("/statistics")
    @ApiOperation("获取MPC统计信息")
    public Result<Map<String, Object>> getStatistics() {
        return Result.success(mpcService.getStatistics());
    }

    @GetMapping("/protocols")
    @ApiOperation("获取协议信息")
    public Result<Map<String, Object>> getProtocols() {
        return Result.success(mpcService.getProtocols());
    }

    @GetMapping("/health")
    @ApiOperation("健康检查")
    public Result<Map<String, Object>> healthCheck() {
        return Result.success(mpcService.healthCheck());
    }
}
