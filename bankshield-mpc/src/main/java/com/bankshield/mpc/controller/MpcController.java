package com.bankshield.mpc.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.common.result.Result;
import com.bankshield.mpc.dto.*;
import com.bankshield.mpc.entity.MpcJob;
import com.bankshield.mpc.entity.MpcParty;
import com.bankshield.mpc.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 多方安全计算控制器
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/mpc")
@Api(tags = "多方安全计算模块")
public class MpcController {

    @Autowired
    private PsiService psiService;

    @Autowired
    private SecureAggregationService secureAggregationService;

    @Autowired
    private JointQueryService jointQueryService;

    @Autowired
    private MpcJobService mpcJobService;

    /**
     * 执行隐私求交
     */
    @PostMapping("/psi")
    @ApiOperation(value = "执行隐私求交", notes = "多方隐私保护求交计算")
    public Result<PsiResult> performPSI(@RequestBody PsiRequest request) {
        try {
            log.info("隐私求交请求，参与方数量: {}", request.getPartyIds().size());
            
            // 创建任务
            MpcJob job = mpcJobService.createJob("PSI", request.getPartyIds(), request.getField());
            
            // 执行求交
            PsiResult result = psiService.performPSI(request, job.getId());
            
            log.info("隐私求交完成，交集大小: {}", result.getIntersectionSize());
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("隐私求交失败", e);
            return Result.error("隐私求交失败: " + e.getMessage());
        }
    }

    /**
     * 执行安全求和
     */
    @PostMapping("/secure-sum")
    @ApiOperation(value = "执行安全求和", notes = "多方安全聚合求和计算")
    public Result<SecureSumResult> secureSum(@RequestBody SecureSumRequest request) {
        try {
            log.info("安全求和请求，参与方数量: {}", request.getPartyIds().size());
            
            // 创建任务
            MpcJob job = mpcJobService.createJob("SECURE_SUM", request.getPartyIds(), request.getField());
            
            // 执行求和
            SecureSumResult result = secureAggregationService.secureSum(request, job.getId());
            
            log.info("安全求和完成，结果: {}", result.getSum());
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("安全求和失败", e);
            return Result.error("安全求和失败: " + e.getMessage());
        }
    }

    /**
     * 执行联合查询
     */
    @PostMapping("/joint-query")
    @ApiOperation(value = "执行联合查询", notes = "多方联合数据查询")
    public Result<JointQueryResult> performJointQuery(@RequestBody JointQueryRequest request) {
        try {
            log.info("联合查询请求，查询类型: {}, 客户ID: {}", 
                    request.getQueryType(), request.getCustomerId());
            
            // 创建任务
            MpcJob job = mpcJobService.createJob("JOINT_QUERY", request.getPartyIds(), 
                    request.getQueryType() + ":" + request.getCustomerId());
            
            // 执行查询
            JointQueryResult result = jointQueryService.performJointQuery(request, job.getId());
            
            log.info("联合查询完成");
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("联合查询失败", e);
            return Result.error("联合查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询任务详情
     */
    @GetMapping("/job/{jobId}")
    @ApiOperation(value = "查询任务详情", notes = "根据任务ID查询MPC任务详情")
    public Result<MpcJob> getJob(@PathVariable Long jobId) {
        try {
            log.info("查询任务详情请求，任务ID: {}", jobId);
            
            MpcJob job = mpcJobService.getJobById(jobId);
            
            if (job == null) {
                return Result.error("任务不存在");
            }
            
            log.info("查询任务详情完成");
            return Result.success(job);
            
        } catch (Exception e) {
            log.error("查询任务详情失败", e);
            return Result.error("查询任务详情失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询任务列表
     */
    @GetMapping("/jobs")
    @ApiOperation(value = "分页查询任务列表", notes = "分页查询MPC任务列表")
    public Result<IPage<MpcJob>> getJobs(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") int size,
            @ApiParam("任务类型") @RequestParam(required = false) String jobType,
            @ApiParam("任务状态") @RequestParam(required = false) String jobStatus) {
        try {
            log.info("分页查询任务列表请求，页码: {}, 每页条数: {}", page, size);
            
            Page<MpcJob> pageParam = new Page<>(page, size);
            IPage<MpcJob> result = mpcJobService.getJobs(pageParam, jobType, jobStatus);
            
            log.info("分页查询任务列表完成，总数: {}", result.getTotal());
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("分页查询任务列表失败", e);
            return Result.error("分页查询任务列表失败: " + e.getMessage());
        }
    }

    /**
     * 取消任务
     */
    @PostMapping("/job/{jobId}/cancel")
    @ApiOperation(value = "取消任务", notes = "取消正在执行的MPC任务")
    public Result<String> cancelJob(@PathVariable Long jobId) {
        try {
            log.info("取消任务请求，任务ID: {}", jobId);
            
            boolean success = mpcJobService.cancelJob(jobId);
            
            if (success) {
                log.info("任务取消成功");
                return Result.success("任务已取消");
            } else {
                return Result.error("任务取消失败");
            }
            
        } catch (Exception e) {
            log.error("取消任务失败", e);
            return Result.error("取消任务失败: " + e.getMessage());
        }
    }

    /**
     * 注册参与方
     */
    @PostMapping("/party/register")
    @ApiOperation(value = "注册参与方", notes = "注册新的MPC参与方")
    public Result<MpcParty> registerParty(@RequestBody MpcParty party) {
        try {
            log.info("注册参与方请求，参与方名称: {}", party.getPartyName());
            
            MpcParty registered = mpcJobService.registerParty(party);
            
            log.info("参与方注册成功，ID: {}", registered.getId());
            return Result.success(registered);
            
        } catch (Exception e) {
            log.error("注册参与方失败", e);
            return Result.error("注册参与方失败: " + e.getMessage());
        }
    }

    /**
     * 查询参与方列表
     */
    @GetMapping("/parties")
    @ApiOperation(value = "查询参与方列表", notes = "查询所有MPC参与方")
    public Result<List<MpcParty>> getParties(
            @ApiParam("参与方状态") @RequestParam(required = false) String status) {
        try {
            log.info("查询参与方列表请求");
            
            List<MpcParty> parties = mpcJobService.getParties(status);
            
            log.info("查询参与方列表完成，数量: {}", parties.size());
            return Result.success(parties);
            
        } catch (Exception e) {
            log.error("查询参与方列表失败", e);
            return Result.error("查询参与方列表失败: " + e.getMessage());
        }
    }

    /**
     * 更新参与方状态
     */
    @PutMapping("/party/{partyId}/status")
    @ApiOperation(value = "更新参与方状态", notes = "更新MPC参与方的在线状态")
    public Result<String> updatePartyStatus(
            @PathVariable Long partyId,
            @ApiParam("状态 (ONLINE/OFFLINE)") @RequestParam String status) {
        try {
            log.info("更新参与方状态请求，参与方ID: {}, 状态: {}", partyId, status);
            
            boolean success = mpcJobService.updatePartyStatus(partyId, status);
            
            if (success) {
                log.info("参与方状态更新成功");
                return Result.success("状态已更新");
            } else {
                return Result.error("状态更新失败");
            }
            
        } catch (Exception e) {
            log.error("更新参与方状态失败", e);
            return Result.error("更新参与方状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    @ApiOperation(value = "获取统计信息", notes = "获取MPC任务的统计信息")
    public Result<Map<String, Object>> getStatistics() {
        try {
            log.info("获取统计信息请求");
            
            Map<String, Object> statistics = new HashMap<>();
            
            // 查询所有任务
            Page<MpcJob> allJobs = new Page<>(1, Integer.MAX_VALUE);
            IPage<MpcJob> jobsPage = mpcJobService.getJobs(allJobs, null, null);
            List<MpcJob> jobs = jobsPage.getRecords();
            
            // 统计任务数量
            statistics.put("totalJobs", jobs.size());
            
            // 按状态统计
            long runningJobs = jobs.stream()
                    .filter(j -> "RUNNING".equals(j.getJobStatus()))
                    .count();
            long successJobs = jobs.stream()
                    .filter(j -> "SUCCESS".equals(j.getJobStatus()))
                    .count();
            long failedJobs = jobs.stream()
                    .filter(j -> "FAILED".equals(j.getJobStatus()))
                    .count();
            
            statistics.put("runningJobs", runningJobs);
            statistics.put("successJobs", successJobs);
            statistics.put("failedJobs", failedJobs);
            
            // 按类型统计
            long psiJobs = jobs.stream()
                    .filter(j -> "PSI".equals(j.getJobType()))
                    .count();
            long sumJobs = jobs.stream()
                    .filter(j -> "SECURE_SUM".equals(j.getJobType()))
                    .count();
            long queryJobs = jobs.stream()
                    .filter(j -> "JOINT_QUERY".equals(j.getJobType()))
                    .count();
            
            statistics.put("psiJobs", psiJobs);
            statistics.put("sumJobs", sumJobs);
            statistics.put("queryJobs", queryJobs);
            
            // 参与方统计
            List<MpcParty> parties = mpcJobService.getParties(null);
            statistics.put("totalParties", parties.size());
            
            long onlineParties = parties.stream()
                    .filter(p -> "ONLINE".equals(p.getStatus()))
                    .count();
            statistics.put("onlineParties", onlineParties);
            
            log.info("获取统计信息完成");
            return Result.success(statistics);
            
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return Result.error("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @ApiOperation(value = "健康检查", notes = "检查MPC模块的健康状态")
    public Result<Map<String, String>> healthCheck() {
        try {
            Map<String, String> health = new HashMap<>();
            health.put("status", "healthy");
            health.put("timestamp", String.valueOf(System.currentTimeMillis()));
            health.put("service", "多方安全计算模块");
            
            return Result.success(health);
            
        } catch (Exception e) {
            log.error("健康检查失败", e);
            return Result.error("健康检查失败: " + e.getMessage());
        }
    }

    /**
     * 获取协议信息
     */
    @GetMapping("/protocols")
    @ApiOperation(value = "获取协议信息", notes = "获取支持的MPC协议信息")
    public Result<Map<String, Object>> getProtocols() {
        try {
            log.info("获取协议信息请求");
            
            Map<String, Object> protocols = new HashMap<>();
            
            // PSI协议
            Map<String, Object> psi = new HashMap<>();
            psi.put("name", "隐私求交 (PSI)");
            psi.put("description", "基于不经意传输的隐私保护求交协议");
            psi.put("algorithm", "Oblivious Transfer");
            psi.put("security", "半诚实模型");
            protocols.put("psi", psi);
            
            // 安全聚合协议
            Map<String, Object> secureSum = new HashMap<>();
            secureSum.put("name", "安全求和");
            secureSum.put("description", "基于Paillier同态加密的安全聚合协议");
            secureSum.put("algorithm", "Paillier Homomorphic Encryption");
            secureSum.put("security", "半诚实模型");
            protocols.put("secureSum", secureSum);
            
            // 联合查询协议
            Map<String, Object> jointQuery = new HashMap<>();
            jointQuery.put("name", "联合查询");
            jointQuery.put("description", "基于Shamir秘密共享的联合查询协议");
            jointQuery.put("algorithm", "Shamir Secret Sharing");
            jointQuery.put("security", "半诚实模型");
            protocols.put("jointQuery", jointQuery);
            
            log.info("获取协议信息完成");
            return Result.success(protocols);
            
        } catch (Exception e) {
            log.error("获取协议信息失败", e);
            return Result.error("获取协议信息失败: " + e.getMessage());
        }
    }
}
