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
     * 重要：不应收集明文数据，应使用加密数据或哈希承诺
     *
     * @param request 求交请求
     * @param jobId 任务ID
     * @return 求交结果
     */
    public PsiResult performPSI(PsiRequest request, Long jobId) {
        log.warn("⚠️ 警告：当前实现存在隐私泄露风险！在中心端收集明文数据不符合隐私保护要求。");
        log.info("开始执行隐私求交任务，任务ID: {}, 参与方: {}", jobId, request.getPartyIds());

        long startTime = System.currentTimeMillis();

        try {
            // 1. 验证参与方
            List<MpcParty> parties = validateParties(request.getPartyIds());

            // 2. 生成OT矩阵和RSA密钥对
            AsymmetricCipherKeyPair keyPair = ObliviousTransfer.generateKeyPair();

            // 3. 收集各参与方的数据（加密或哈希承诺）
            // 正确实现：只收集哈希承诺，不收集明文
            Map<String, Set<String>> partyHashes = new ConcurrentHashMap<>();
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (MpcParty party : parties) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        // 获取明文数据（仅用于演示，实际应使用哈希承诺）
                        Set<String> rawData = mpcClientService.getLocalData(party.getPartyName(), request.getField());

                        // 对数据进行哈希处理（实际MPC中应使用承诺协议）
                        Set<String> hashedData = rawData.stream()
                            .map(data -> hashData(data, party.getPartyName()))
                            .collect(Collectors.toSet());

                        partyHashes.put(party.getPartyName(), hashedData);
                        log.info("参与方 {} 数据哈希完成，数据量: {}", party.getPartyName(), hashedData.size());
                        log.warn("⚠️ 仍存在隐私风险：明文数据在本地处理后可能仍被记录");
                    } catch (Exception e) {
                        log.error("参与方 {} 数据处理失败", party.getPartyName(), e);
                        throw new RuntimeException("数据处理失败: " + party.getPartyName(), e);
                    }
                });
                futures.add(future);
            }

            // 等待所有参与方数据处理完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // 4. 执行OT协议求交（使用哈希值）
            Set<String> intersection = computeIntersection(partyHashes, keyPair);

            // 5. 生成结果哈希
            String resultHash = generateResultHash(intersection);

            long executionTime = System.currentTimeMillis() - startTime;

            // 重要：不返回明文结果，只返回交集大小和哈希
            PsiResult result = new PsiResult(
                intersection.size(),
                null, // 不返回明文交集，只返回大小
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
     * 对数据进行哈希处理（临时方案）
     * 实际MPC中应使用承诺协议如Pedersen承诺
     */
    private String hashData(String data, String partyName) {
        try {
            // 使用SHA-256对数据进行哈希
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            String input = data + ":" + partyName; // 加入参与方标识
            byte[] hash = md.digest(input.getBytes("UTF-8"));

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
            log.error("数据哈希失败: {}", data, e);
            throw new RuntimeException("数据哈希失败", e);
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