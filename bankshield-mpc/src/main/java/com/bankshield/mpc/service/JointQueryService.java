package com.bankshield.mpc.service;

import com.bankshield.mpc.dto.JointQueryRequest;
import com.bankshield.mpc.dto.JointQueryResult;
import com.bankshield.mpc.entity.MpcJob;
import com.bankshield.mpc.entity.MpcParty;
import com.bankshield.mpc.mapper.MpcJobMapper;
import com.bankshield.mpc.mapper.MpcPartyMapper;
import com.bankshield.mpc.protocol.SecretSharing;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 联合查询服务
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Slf4j
@Service
public class JointQueryService {
    
    @Autowired
    private MpcPartyMapper partyMapper;
    
    @Autowired
    private MpcJobMapper jobMapper;
    
    @Autowired
    private MpcClientService mpcClientService;
    
    /**
     * 执行联合查询
     * 
     * @param request 查询请求
     * @param jobId 任务ID
     * @return 查询结果
     */
    public JointQueryResult performJointQuery(JointQueryRequest request, Long jobId) {
        log.info("开始执行联合查询任务，任务ID: {}, 客户ID: {}, 查询类型: {}", 
                jobId, request.getCustomerId(), request.getQueryType());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 验证参与方
            List<MpcParty> parties = validateParties(request.getPartyIds());
            
            // 2. 将查询条件分片（Shamir秘密共享）
            SecretSharing secretSharing = new SecretSharing(request.getThreshold());
            BigInteger secret = new BigInteger(request.getCustomerId().getBytes());
            SecretSharing.SecretShare[] shares = secretSharing.share(secret, parties.size(), request.getThreshold());
            
            // 3. 各参与方本地查询
            Map<String, SecretSharing.SecretShare> localResults = new ConcurrentHashMap<>();
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            
            for (int i = 0; i < parties.size(); i++) {
                MpcParty party = parties.get(i);
                SecretSharing.SecretShare share = shares[i];
                
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        BigInteger localResult = mpcClientService.queryLocalData(
                            party.getPartyName(), 
                            request.getCustomerId(), 
                            request.getQueryType()
                        );
                        
                        // 将本地结果转换为秘密分片
                        SecretSharing.SecretShare resultShare = new SecretSharing.SecretShare(
                            share.getX(),
                            localResult
                        );
                        
                        localResults.put(party.getPartyName(), resultShare);
                        log.info("参与方 {} 本地查询完成，结果: {}", party.getPartyName(), localResult);
                        
                    } catch (Exception e) {
                        log.error("参与方 {} 本地查询失败", party.getPartyName(), e);
                        throw new RuntimeException("本地查询失败: " + party.getPartyName(), e);
                    }
                });
                
                futures.add(future);
            }
            
            // 等待所有参与方查询完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            // 4. 重构结果（拉格朗日插值）
            BigInteger reconstructed = secretSharing.reconstruct(localResults.values());
            
            // 5. 生成结果哈希
            String resultHash = generateResultHash(reconstructed);
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            JointQueryResult result = new JointQueryResult(
                request.getQueryType(),
                reconstructed,
                parties.size(),
                executionTime,
                resultHash
            );
            
            // 更新任务状态
            updateJobStatus(jobId, "SUCCESS", "查询结果: " + reconstructed);
            
            log.info("联合查询任务完成，任务ID: {}, 结果: {}, 耗时: {}ms", 
                    jobId, reconstructed, executionTime);
            
            return result;
            
        } catch (Exception e) {
            log.error("联合查询任务失败，任务ID: " + jobId, e);
            updateJobStatus(jobId, "FAILED", e.getMessage());
            throw new RuntimeException("联合查询执行失败", e);
        }
    }
    
    /**
     * 验证参与方
     */
    private List<MpcParty> validateParties(List<String> partyIds) {
        List<MpcParty> parties = partyMapper.selectBatchIds(partyIds);
        
        if (parties.size() != partyIds.size()) {
            throw new IllegalArgumentException("部分参与方不存在");
        }
        
        // 检查参与方状态
        for (MpcParty party : parties) {
            if (!"ONLINE".equals(party.getStatus())) {
                throw new IllegalArgumentException("参与方 " + party.getPartyName() + " 不在线");
            }
        }
        
        return parties;
    }
    
    /**
     * 生成结果哈希
     */
    private String generateResultHash(BigInteger result) {
        try {
            String content = result.toString();
            
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(content.getBytes("UTF-8"));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("生成结果哈希失败", e);
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
                job.setEndTime(LocalDateTime.now());
                jobMapper.updateById(job);
            }
        } catch (Exception e) {
            log.error("更新任务状态失败", e);
        }
    }
}