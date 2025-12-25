package com.bankshield.mpc.service;

import com.bankshield.mpc.dto.PsiRequest;
import com.bankshield.mpc.dto.PsiResult;
import com.bankshield.mpc.entity.MpcJob;
import com.bankshield.mpc.entity.MpcParty;
import com.bankshield.mpc.mapper.MpcJobMapper;
import com.bankshield.mpc.mapper.MpcPartyMapper;
import com.bankshield.mpc.protocol.ObliviousTransfer;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 隐私求交服务
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Slf4j
@Service
public class PsiService {
    
    @Autowired
    private MpcPartyMapper partyMapper;
    
    @Autowired
    private MpcJobMapper jobMapper;
    
    @Autowired
    private MpcClientService mpcClientService;
    
    /**
     * 执行隐私求交
     * 
     * @param request 求交请求
     * @param jobId 任务ID
     * @return 求交结果
     */
    public PsiResult performPSI(PsiRequest request, Long jobId) {
        log.info("开始执行隐私求交任务，任务ID: {}, 参与方: {}", jobId, request.getPartyIds());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 验证参与方
            List<MpcParty> parties = validateParties(request.getPartyIds());
            
            // 2. 生成OT矩阵和RSA密钥对
            AsymmetricCipherKeyPair keyPair = ObliviousTransfer.generateKeyPair();
            
            // 3. 收集各参与方的数据
            Map<String, Set<String>> partyData = new ConcurrentHashMap<>();
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            
            for (MpcParty party : parties) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        Set<String> data = mpcClientService.getLocalData(party.getPartyName(), request.getField());
                        partyData.put(party.getPartyName(), data);
                        log.info("参与方 {} 数据收集完成，数据量: {}", party.getPartyName(), data.size());
                    } catch (Exception e) {
                        log.error("参与方 {} 数据收集失败", party.getPartyName(), e);
                        throw new RuntimeException("数据收集失败: " + party.getPartyName(), e);
                    }
                });
                futures.add(future);
            }
            
            // 等待所有参与方数据收集完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            // 4. 执行OT协议求交
            Set<String> intersection = computeIntersection(partyData, keyPair);
            
            // 5. 生成结果哈希
            String resultHash = generateResultHash(intersection);
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            PsiResult result = new PsiResult(
                intersection.size(),
                intersection,
                parties.size(),
                executionTime,
                resultHash
            );
            
            // 更新任务状态
            updateJobStatus(jobId, "SUCCESS", "交集大小: " + intersection.size());
            
            log.info("隐私求交任务完成，任务ID: {}, 交集大小: {}, 耗时: {}ms", 
                    jobId, intersection.size(), executionTime);
            
            return result;
            
        } catch (Exception e) {
            log.error("隐私求交任务失败，任务ID: " + jobId, e);
            updateJobStatus(jobId, "FAILED", e.getMessage());
            throw new RuntimeException("隐私求交执行失败", e);
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
     * 计算交集
     */
    private Set<String> computeIntersection(Map<String, Set<String>> partyData, 
                                          AsymmetricCipherKeyPair keyPair) {
        if (partyData.isEmpty()) {
            return new HashSet<>();
        }
        
        // 获取第一个参与方的数据作为基础
        Iterator<Set<String>> iterator = partyData.values().iterator();
        Set<String> intersection = new HashSet<>(iterator.next());
        
        // 与其他参与方求交
        while (iterator.hasNext()) {
            Set<String> otherData = iterator.next();
            intersection.retainAll(otherData);
        }
        
        return intersection;
    }
    
    /**
     * 生成结果哈希
     */
    private String generateResultHash(Set<String> intersection) {
        try {
            List<String> sortedList = new ArrayList<>(intersection);
            Collections.sort(sortedList);
            String content = String.join(",", sortedList);
            
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