package com.bankshield.mpc.service;

import com.bankshield.mpc.dto.SecureSumRequest;
import com.bankshield.mpc.dto.SecureSumResult;
import com.bankshield.mpc.entity.MpcJob;
import com.bankshield.mpc.entity.MpcParty;
import com.bankshield.mpc.mapper.MpcJobMapper;
import com.bankshield.mpc.mapper.MpcPartyMapper;
import com.bankshield.mpc.protocol.PaillierHomomorphicEncryption;
import com.bankshield.mpc.protocol.PaillierHomomorphicEncryption.PaillierKeyPair;
import com.bankshield.mpc.protocol.PaillierHomomorphicEncryption.PaillierPublicKey;
import com.bankshield.mpc.protocol.PaillierHomomorphicEncryption.PaillierPrivateKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 安全聚合服务
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Slf4j
@Service
public class SecureAggregationService {
    
    @Autowired
    private MpcPartyMapper partyMapper;
    
    @Autowired
    private MpcJobMapper jobMapper;
    
    @Autowired
    private MpcClientService mpcClientService;
    
    /**
     * 执行安全求和
     * 
     * @param request 求和请求
     * @param jobId 任务ID
     * @return 求和结果
     */
    public SecureSumResult secureSum(SecureSumRequest request, Long jobId) {
        log.info("开始执行安全求和任务，任务ID: {}, 参与方: {}", jobId, request.getPartyIds());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 验证参与方
            List<MpcParty> parties = validateParties(request.getPartyIds());
            
            // 2. 生成或获取Paillier密钥对
            PaillierKeyPair keyPair = generateKeyPair(request);
            PaillierPublicKey publicKey = keyPair.getPublicKey();
            PaillierPrivateKey privateKey = keyPair.getPrivateKey();
            
            // 3. 各参与方加密本地数据
            List<BigInteger> encryptedValues = new ArrayList<>();
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            
            for (MpcParty party : parties) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        // 获取本地数据
                        BigInteger localValue = mpcClientService.getLocalValue(
                            party.getPartyName(), 
                            request.getField()
                        );
                        
                        // 加密本地数据
                        BigInteger encryptedValue = PaillierHomomorphicEncryption.encrypt(publicKey, localValue);
                        encryptedValues.add(encryptedValue);
                        
                        log.info("参与方 {} 数据加密完成，原始值: {}, 加密值: {}", 
                                party.getPartyName(), localValue, encryptedValue);
                        
                    } catch (Exception e) {
                        log.error("参与方 {} 数据加密失败", party.getPartyName(), e);
                        throw new RuntimeException("数据加密失败: " + party.getPartyName(), e);
                    }
                });
                
                futures.add(future);
            }
            
            // 等待所有参与方加密完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            // 4. 密文聚合（同态加法）
            BigInteger aggregatedCipher = encryptedValues.stream()
                    .reduce(BigInteger.ONE, (a, b) -> PaillierHomomorphicEncryption.add(publicKey, a, b));
            
            log.info("密文聚合完成，聚合密文: {}", aggregatedCipher);
            
            // 5. 解密结果
            BigInteger decryptedSum = PaillierHomomorphicEncryption.decrypt(privateKey, aggregatedCipher);
            
            // 6. 生成结果哈希
            String resultHash = generateResultHash(decryptedSum);
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            SecureSumResult result = new SecureSumResult(
                decryptedSum,
                parties.size(),
                executionTime,
                resultHash
            );
            
            // 更新任务状态
            updateJobStatus(jobId, "SUCCESS", "求和结果: " + decryptedSum);
            
            log.info("安全求和任务完成，任务ID: {}, 求和结果: {}, 耗时: {}ms", 
                    jobId, decryptedSum, executionTime);
            
            return result;
            
        } catch (Exception e) {
            log.error("安全求和任务失败，任务ID: " + jobId, e);
            updateJobStatus(jobId, "FAILED", e.getMessage());
            throw new RuntimeException("安全求和执行失败", e);
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
     * 生成或获取Paillier密钥对
     */
    private PaillierKeyPair generateKeyPair(SecureSumRequest request) {
        if (request.getPublicKey() != null && request.getPrivateKey() != null) {
            // 使用提供的密钥
            try {
                BigInteger n = new BigInteger(request.getPublicKey());
                BigInteger lambda = new BigInteger(request.getPrivateKey());
                
                PaillierPublicKey publicKey = new PaillierPublicKey(n, n.add(BigInteger.ONE));
                PaillierPrivateKey privateKey = new PaillierPrivateKey(lambda, n);
                
                return new PaillierKeyPair(publicKey, privateKey);
            } catch (Exception e) {
                log.warn("提供的密钥格式无效，将生成新密钥", e);
            }
        }
        
        // 生成新的密钥对
        return PaillierHomomorphicEncryption.generateKeyPair();
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