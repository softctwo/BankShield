package com.bankshield.mpc.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.common.result.Result;
import com.bankshield.mpc.dto.*;
import com.bankshield.mpc.entity.MpcJob;
import com.bankshield.mpc.mapper.MpcJobMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MPC任务服务
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Slf4j
@Service
public class MpcJobService {
    
    @Autowired
    private MpcJobMapper jobMapper;
    
    @Autowired
    private PsiService psiService;
    
    @Autowired
    private JointQueryService jointQueryService;
    
    @Autowired
    private SecureAggregationService secureAggregationService;
    
    /**
     * 创建隐私求交任务
     * 
     * @param request 求交请求
     * @return 任务ID
     */
    public Long createPsiJob(PsiRequest request) {
        log.info("创建隐私求交任务，参与方: {}", request.getPartyIds());
        
        // 创建任务
        MpcJob job = new MpcJob();
        job.setJobType("PSI");
        job.setJobStatus("PENDING");
        job.setPartyIds(String.join(",", request.getPartyIds()));
        job.setParameters(com.alibaba.fastjson2.JSON.toJSONString(request));
        job.setStartTime(LocalDateTime.now());
        job.setCreateTime(LocalDateTime.now());
        job.setUpdateTime(LocalDateTime.now());
        
        jobMapper.insert(job);
        
        // 异步执行任务
        executePsiJob(job.getId(), request);
        
        log.info("隐私求交任务创建成功，任务ID: {}", job.getId());
        return job.getId();
    }
    
    /**
     * 创建联合查询任务
     * 
     * @param request 查询请求
     * @return 任务ID
     */
    public Long createJointQueryJob(JointQueryRequest request) {
        log.info("创建联合查询任务，客户ID: {}, 查询类型: {}", request.getCustomerId(), request.getQueryType());
        
        // 创建任务
        MpcJob job = new MpcJob();
        job.setJobType("JOINT_QUERY");
        job.setJobStatus("PENDING");
        job.setPartyIds(String.join(",", request.getPartyIds()));
        job.setParameters(com.alibaba.fastjson2.JSON.toJSONString(request));
        job.setStartTime(LocalDateTime.now());
        job.setCreateTime(LocalDateTime.now());
        job.setUpdateTime(LocalDateTime.now());
        
        jobMapper.insert(job);
        
        // 异步执行任务
        executeJointQueryJob(job.getId(), request);
        
        log.info("联合查询任务创建成功，任务ID: {}", job.getId());
        return job.getId();
    }
    
    /**
     * 创建安全求和任务
     * 
     * @param request 求和请求
     * @return 任务ID
     */
    public Long createSecureSumJob(SecureSumRequest request) {
        log.info("创建安全求和任务，参与方: {}", request.getPartyIds());
        
        // 创建任务
        MpcJob job = new MpcJob();
        job.setJobType("SECURE_SUM");
        job.setJobStatus("PENDING");
        job.setPartyIds(String.join(",", request.getPartyIds()));
        job.setParameters(com.alibaba.fastjson2.JSON.toJSONString(request));
        job.setStartTime(LocalDateTime.now());
        job.setCreateTime(LocalDateTime.now());
        job.setUpdateTime(LocalDateTime.now());
        
        jobMapper.insert(job);
        
        // 异步执行任务
        executeSecureSumJob(job.getId(), request);
        
        log.info("安全求和任务创建成功，任务ID: {}", job.getId());
        return job.getId();
    }
    
    /**
     * 获取任务状态
     * 
     * @param jobId 任务ID
     * @return 任务信息
     */
    public MpcJob getJob(Long jobId) {
        MpcJob job = jobMapper.selectById(jobId);
        if (job == null) {
            throw new IllegalArgumentException("任务不存在: " + jobId);
        }
        return job;
    }
    
    /**
     * 获取任务列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @return 任务列表
     */
    public Page<MpcJob> getJobList(int page, int size) {
        QueryWrapper<MpcJob> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        
        return jobMapper.selectPage(new Page<>(page, size), queryWrapper);
    }
    
    /**
     * 异步执行隐私求交任务
     * 
     * @param jobId 任务ID
     * @param request 请求参数
     */
    @Async("mpcExecutor")
    public void executePsiJob(Long jobId, PsiRequest request) {
        log.info("异步执行隐私求交任务，任务ID: {}", jobId);
        
        try {
            // 更新任务状态为运行中
            updateJobStatus(jobId, "RUNNING", null);
            
            // 执行PSI
            PsiResult result = psiService.performPSI(request, jobId);
            
            // 保存结果
            saveJobResult(jobId, result);
            
        } catch (Exception e) {
            log.error("隐私求交任务执行失败，任务ID: " + jobId, e);
            updateJobStatus(jobId, "FAILED", e.getMessage());
        }
    }
    
    /**
     * 异步执行联合查询任务
     * 
     * @param jobId 任务ID
     * @param request 请求参数
     */
    @Async("mpcExecutor")
    public void executeJointQueryJob(Long jobId, JointQueryRequest request) {
        log.info("异步执行联合查询任务，任务ID: {}", jobId);
        
        try {
            // 更新任务状态为运行中
            updateJobStatus(jobId, "RUNNING", null);
            
            // 执行联合查询
            JointQueryResult result = jointQueryService.performJointQuery(request, jobId);
            
            // 保存结果
            saveJobResult(jobId, result);
            
        } catch (Exception e) {
            log.error("联合查询任务执行失败，任务ID: " + jobId, e);
            updateJobStatus(jobId, "FAILED", e.getMessage());
        }
    }
    
    /**
     * 异步执行安全求和任务
     * 
     * @param jobId 任务ID
     * @param request 请求参数
     */
    @Async("mpcExecutor")
    public void executeSecureSumJob(Long jobId, SecureSumRequest request) {
        log.info("异步执行安全求和任务，任务ID: {}", jobId);
        
        try {
            // 更新任务状态为运行中
            updateJobStatus(jobId, "RUNNING", null);
            
            // 执行安全求和
            SecureSumResult result = secureAggregationService.secureSum(request, jobId);
            
            // 保存结果
            saveJobResult(jobId, result);
            
        } catch (Exception e) {
            log.error("安全求和任务执行失败，任务ID: " + jobId, e);
            updateJobStatus(jobId, "FAILED", e.getMessage());
        }
    }
    
    /**
     * 更新任务状态
     */
    private void updateJobStatus(Long jobId, String status, String result) {
        try {
            MpcJob job = jobMapper.selectById(jobId);
            if (job != null) {
                job.setJobStatus(status);
                job.setResult(result);
                job.setUpdateTime(LocalDateTime.now());
                
                if ("RUNNING".equals(status)) {
                    job.setStartTime(LocalDateTime.now());
                } else if ("SUCCESS".equals(status) || "FAILED".equals(status)) {
                    job.setEndTime(LocalDateTime.now());
                }
                
                jobMapper.updateById(job);
            }
        } catch (Exception e) {
            log.error("更新任务状态失败", e);
        }
    }
    
    /**
     * 保存任务结果
     */
    private void saveJobResult(Long jobId, Object result) {
        try {
            MpcJob job = jobMapper.selectById(jobId);
            if (job != null) {
                job.setResult(com.alibaba.fastjson2.JSON.toJSONString(result));
                job.setEndTime(LocalDateTime.now());
                jobMapper.updateById(job);
            }
        } catch (Exception e) {
            log.error("保存任务结果失败", e);
        }
    }
}