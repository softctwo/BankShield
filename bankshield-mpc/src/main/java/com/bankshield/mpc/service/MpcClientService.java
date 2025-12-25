package com.bankshield.mpc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * MPC客户端服务（模拟实现）
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Slf4j
@Service
public class MpcClientService {

    private static final Random RANDOM = new Random();

    /**
     * 对字符串进行哈希处理
     */
    private String hashString(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes("UTF-8"));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.substring(0, 16) + "..."; // 只返回前16位，避免泄露完整哈希
        } catch (Exception e) {
            return "HASH_ERROR";
        }
    }
    
    /**
     * 获取参与方的本地数据（模拟实现）
     * 
     * @param partyName 参与方名称
     * @param field 字段名
     * @return 本地数据集合
     */
    public Set<String> getLocalData(String partyName, String field) {
        log.info("获取参与方 {} 的本地数据，字段: {}", partyName, field);

        // 模拟不同参与方的数据
        Set<String> data = new HashSet<>();
        
        switch (partyName) {
            case "BANK_A":
                // 银行A的客户数据
                data.add("CUST_001");
                data.add("CUST_002");
                data.add("CUST_003");
                data.add("CUST_004");
                data.add("CUST_005");
                break;
                
            case "BANK_B":
                // 银行B的客户数据
                data.add("CUST_002");
                data.add("CUST_003");
                data.add("CUST_006");
                data.add("CUST_007");
                data.add("CUST_008");
                break;
                
            case "BANK_C":
                // 银行C的客户数据
                data.add("CUST_001");
                data.add("CUST_003");
                data.add("CUST_009");
                data.add("CUST_010");
                break;
                
            default:
                // 生成随机数据
                int dataSize = RANDOM.nextInt(100) + 50;
                for (int i = 0; i < dataSize; i++) {
                    data.add("CUST_" + String.format("%06d", RANDOM.nextInt(100000)));
                }
        }

        log.info("参与方 {} 返回 {} 条数据", partyName, data.size());
        return data;
    }
    
    /**
     * 本地查询（模拟实现）
     * 
     * @param partyName 参与方名称
     * @param customerId 客户ID
     * @param queryType 查询类型
     * @return 查询结果
     */
    public BigInteger queryLocalData(String partyName, String customerId, String queryType) {
        log.info("参与方 {} 执行本地查询，客户ID哈希: {}, 查询类型: {}",
                partyName, hashString(customerId), queryType);

        // 模拟不同查询类型的结果
        BigInteger result;
        
        switch (queryType) {
            case "TOTAL_DEBT":
                // 总负债查询
                result = BigInteger.valueOf(RANDOM.nextInt(1000000) + 10000);
                break;
                
            case "TOTAL_ASSET":
                // 总资产查询
                result = BigInteger.valueOf(RANDOM.nextInt(5000000) + 50000);
                break;
                
            case "CREDIT_SCORE":
                // 信用评分查询
                result = BigInteger.valueOf(RANDOM.nextInt(500) + 300);
                break;
                
            default:
                // 默认随机结果
                result = BigInteger.valueOf(RANDOM.nextInt(1000000));
        }

        log.info("参与方 {} 本地查询完成", partyName);
        return result;
    }
    
    /**
     * 获取本地数值（模拟实现）
     * 
     * @param partyName 参与方名称
     * @param field 字段名
     * @return 本地数值
     */
    public BigInteger getLocalValue(String partyName, String field) {
        log.info("获取参与方 {} 的本地数值，字段: {}", partyName, field);

        // 模拟不同参与方和字段的数值
        BigInteger value;
        
        if ("deposit".equalsIgnoreCase(field)) {
            // 存款金额
            switch (partyName) {
                case "BANK_A":
                    value = new BigInteger("50000000"); // 5000万
                    break;
                case "BANK_B":
                    value = new BigInteger("80000000"); // 8000万
                    break;
                case "BANK_C":
                    value = new BigInteger("120000000"); // 1.2亿
                    break;
                default:
                    value = BigInteger.valueOf(RANDOM.nextInt(100000000) + 10000000);
            }
        } else if ("loan".equalsIgnoreCase(field)) {
            // 贷款金额
            switch (partyName) {
                case "BANK_A":
                    value = new BigInteger("30000000"); // 3000万
                    break;
                case "BANK_B":
                    value = new BigInteger("60000000"); // 6000万
                    break;
                case "BANK_C":
                    value = new BigInteger("90000000"); // 9000万
                    break;
                default:
                    value = BigInteger.valueOf(RANDOM.nextInt(100000000) + 5000000);
            }
        } else {
            // 默认随机数值
            value = BigInteger.valueOf(RANDOM.nextInt(100000000));
        }

        log.info("参与方 {} 本地数值获取完成", partyName);
        return value;
    }
}