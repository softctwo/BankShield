package com.bankshield.api.service;

import com.bankshield.api.dto.MaskingAlgorithmParams;
import com.bankshield.api.entity.DataMaskingRule;
import com.bankshield.api.enums.MaskingAlgorithm;
import com.bankshield.common.crypto.SM3Util;
import com.bankshield.common.utils.DataMaskUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 数据脱敏执行引擎
 * 实现各种脱敏算法，提供统一的数据脱敏接口
 */
@Slf4j
//@Component
@RequiredArgsConstructor
public class DataMaskingEngine {

    private final DataMaskingRuleService maskingRuleService;
    private final ObjectMapper objectMapper;

    /**
     * 根据规则对数据进行脱敏处理
     * @param data 原始数据
     * @param rule 脱敏规则
     * @return 脱敏后的数据
     */
    public String maskData(String data, DataMaskingRule rule) {
        if (!StringUtils.hasText(data) || rule == null || !rule.getEnabled()) {
            return data;
        }

        try {
            MaskingAlgorithm algorithm = MaskingAlgorithm.fromCode(rule.getMaskingAlgorithm());
            if (algorithm == null) {
                log.warn("未知的脱敏算法：{}", rule.getMaskingAlgorithm());
                return data;
            }

            // 解析算法参数
            MaskingAlgorithmParams params = null;
            if (StringUtils.hasText(rule.getAlgorithmParams())) {
                params = objectMapper.readValue(rule.getAlgorithmParams(), MaskingAlgorithmParams.class);
            }

            return executeMasking(data, algorithm, params);
        } catch (Exception e) {
            log.error("脱敏处理失败：{}，算法：{}", e.getMessage(), rule.getMaskingAlgorithm(), e);
            return data; // 脱敏失败时返回原始数据
        }
    }

    /**
     * 根据敏感数据类型自动脱敏
     * @param data 原始数据
     * @param sensitiveDataType 敏感数据类型
     * @param scenario 应用场景
     * @return 脱敏后的数据
     */
    @Cacheable(value = "maskingRules", key = "#sensitiveDataType + ':' + #scenario")
    public String maskByType(String data, String sensitiveDataType, String scenario) {
        if (!StringUtils.hasText(data) || !StringUtils.hasText(sensitiveDataType)) {
            return data;
        }

        try {
            // 获取适用的脱敏规则
            List<DataMaskingRule> rules = maskingRuleService.getRulesBySensitiveType(sensitiveDataType, scenario);
            if (rules.isEmpty()) {
                // 没有配置规则时，使用默认脱敏
                return DataMaskUtil.maskByType(data, sensitiveDataType);
            }

            // 使用第一个启用的规则进行脱敏
            DataMaskingRule rule = rules.get(0);
            return maskData(data, rule);
        } catch (Exception e) {
            log.error("自动脱敏失败：{}，类型：{}，场景：{}", e.getMessage(), sensitiveDataType, scenario, e);
            return DataMaskUtil.maskByType(data, sensitiveDataType); // 失败时使用默认脱敏
        }
    }

    /**
     * 批量脱敏处理
     * @param dataList 数据列表
     * @param rule 脱敏规则
     * @return 脱敏后的数据列表
     */
    public List<String> maskDataList(List<String> dataList, DataMaskingRule rule) {
        if (dataList == null || dataList.isEmpty()) {
            return dataList;
        }

        return dataList.stream()
                .map(data -> maskData(data, rule))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * 对Map中的指定字段进行脱敏
     * @param dataMap 数据Map
     * @param fieldName 字段名
     * @param sensitiveDataType 敏感数据类型
     * @param scenario 应用场景
     * @return 脱敏后的数据Map
     */
    public Map<String, Object> maskMapField(Map<String, Object> dataMap, String fieldName, 
                                          String sensitiveDataType, String scenario) {
        if (dataMap == null || !dataMap.containsKey(fieldName) || !StringUtils.hasText(sensitiveDataType)) {
            return dataMap;
        }

        Object value = dataMap.get(fieldName);
        if (value instanceof String) {
            String maskedValue = maskByType((String) value, sensitiveDataType, scenario);
            dataMap.put(fieldName, maskedValue);
        }
        
        return dataMap;
    }

    /**
     * 执行具体的脱敏算法
     */
    private String executeMasking(String data, MaskingAlgorithm algorithm, MaskingAlgorithmParams params) 
            throws JsonProcessingException {
        
        switch (algorithm) {
            case PARTIAL_MASK:
                return executePartialMask(data, params);
            case FULL_MASK:
                return executeFullMask(data, params);
            case HASH:
                return executeHash(data, params);
            case SYMMETRIC_ENCRYPT:
                return executeSymmetricEncrypt(data, params);
            case FORMAT_PRESERVING:
                return executeFormatPreserving(data, params);
            default:
                return data;
        }
    }

    /**
     * 部分掩码算法
     */
    private String executePartialMask(String data, MaskingAlgorithmParams params) {
        if (params == null) {
            // 默认参数：保留前3位和后4位
            return DataMaskUtil.maskCustom(data, 3, 4, '*', 0);
        }

        int keepPrefix = params.getKeepPrefix() != null ? params.getKeepPrefix() : 0;
        int keepSuffix = params.getKeepSuffix() != null ? params.getKeepSuffix() : 0;
        char maskChar = params.getMaskChar() != null && !params.getMaskChar().isEmpty() 
                ? params.getMaskChar().charAt(0) : '*';
        int maskLength = params.getMaskLength() != null ? params.getMaskLength() : 0;

        return DataMaskUtil.maskCustom(data, keepPrefix, keepSuffix, maskChar, maskLength);
    }

    /**
     * 完整掩码算法
     */
    private String executeFullMask(String data, MaskingAlgorithmParams params) {
        char maskChar = params != null && params.getMaskChar() != null && !params.getMaskChar().isEmpty() 
                ? params.getMaskChar().charAt(0) : '*';
        return String.valueOf(maskChar).repeat(data.length());
    }

    /**
     * 哈希算法
     */
    private String executeHash(String data, MaskingAlgorithmParams params) {
        String hashAlgorithm = params != null && params.getHashAlgorithm() != null
                ? params.getHashAlgorithm() : "SM3";

        try {
            switch (hashAlgorithm.toUpperCase()) {
                case "SM3":
                    return SM3Util.hash(data);
                case "SHA256":
                    return bytesToHex(java.security.MessageDigest.getInstance("SHA-256")
                            .digest(data.getBytes(StandardCharsets.UTF_8)));
                default:
                    return SM3Util.hash(data);
            }
        } catch (Exception e) {
            log.error("哈希计算失败：{}", e.getMessage(), e);
            throw new RuntimeException("哈希计算失败", e);
        }
    }

    /**
     * 对称加密算法
     */
    private String executeSymmetricEncrypt(String data, MaskingAlgorithmParams params) {
        // 这里可以集成SM4或其他对称加密算法
        // 暂时使用部分掩码作为示例
        log.warn("对称加密算法暂未实现，使用部分掩码代替");
        return executePartialMask(data, params);
    }

    /**
     * 格式保留加密
     */
    private String executeFormatPreserving(String data, MaskingAlgorithmParams params) {
        int preserveLength = params != null && params.getFormatPreserveLength() != null 
                ? params.getFormatPreserveLength() : 4;
        
        if (data.length() <= preserveLength) {
            return data;
        }

        // 保留前后各一部分，中间加密
        int prefixLen = preserveLength / 2;
        int suffixLen = preserveLength - prefixLen;
        
        String prefix = data.substring(0, prefixLen);
        String suffix = data.substring(data.length() - suffixLen);
        String middle = data.substring(prefixLen, data.length() - suffixLen);
        
        // 中间部分用*代替
        String maskedMiddle = "*".repeat(middle.length());
        
        return prefix + maskedMiddle + suffix;
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}