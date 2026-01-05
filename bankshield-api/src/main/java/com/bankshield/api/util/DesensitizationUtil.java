package com.bankshield.api.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据脱敏工具类
 * 支持多种脱敏算法
 */
@Slf4j
public class DesensitizationUtil {
    
    private static final Random RANDOM = new Random();
    
    /**
     * 执行脱敏
     * 
     * @param value 原始值
     * @param algorithmType 算法类型
     * @param algorithmConfig 算法配置
     * @return 脱敏后的值
     */
    public static String desensitize(String value, String algorithmType, String algorithmConfig) {
        if (StrUtil.isBlank(value)) {
            return value;
        }
        
        try {
            JSONObject config = StrUtil.isNotBlank(algorithmConfig) 
                ? JSON.parseObject(algorithmConfig) 
                : new JSONObject();
            
            switch (algorithmType) {
                case "MASK":
                    return mask(value, config);
                case "REPLACE":
                    return replace(value, config);
                case "ENCRYPT":
                    return encrypt(value, config);
                case "HASH":
                    return hash(value, config);
                case "GENERALIZE":
                    return generalize(value, config);
                case "SHUFFLE":
                    return shuffle(value, config);
                case "TRUNCATE":
                    return truncate(value, config);
                default:
                    log.warn("未知的脱敏算法类型: {}", algorithmType);
                    return value;
            }
        } catch (Exception e) {
            log.error("脱敏处理失败: {}", e.getMessage(), e);
            return value;
        }
    }
    
    /**
     * 遮盖算法
     */
    private static String mask(String value, JSONObject config) {
        String pattern = config.getString("pattern");
        String replacement = config.getString("replacement");
        
        if (StrUtil.isNotBlank(pattern) && StrUtil.isNotBlank(replacement)) {
            return value.replaceAll(pattern, replacement);
        }
        
        // 默认遮盖逻辑
        Boolean keepFirst = config.getBoolean("keepFirst");
        String maskChar = config.getString("maskChar");
        if (maskChar == null) {
            maskChar = "*";
        }
        
        if (value.length() <= 1) {
            return maskChar;
        }
        
        if (Boolean.TRUE.equals(keepFirst)) {
            // 保留第一个字符
            return value.charAt(0) + maskChar.repeat(value.length() - 1);
        } else {
            // 全部遮盖
            return maskChar.repeat(value.length());
        }
    }
    
    /**
     * 替换算法
     */
    private static String replace(String value, JSONObject config) {
        String replacement = config.getString("replacement");
        return StrUtil.isNotBlank(replacement) ? replacement : "***";
    }
    
    /**
     * 加密算法
     */
    private static String encrypt(String value, JSONObject config) {
        String algorithm = config.getString("algorithm");
        
        if ("SM4".equals(algorithm)) {
            // 使用SM4加密（简化实现，实际应使用密钥管理）
            try {
                return SM4Util.encrypt(value);
            } catch (Exception e) {
                log.error("SM4加密失败: {}", e.getMessage());
                return mask(value, new JSONObject());
            }
        } else {
            // 默认使用Base64编码（不是真正的加密，仅用于演示）
            return cn.hutool.core.codec.Base64.encode(value);
        }
    }
    
    /**
     * 哈希算法
     */
    private static String hash(String value, JSONObject config) {
        String algorithm = config.getString("algorithm");
        
        if ("SM3".equals(algorithm)) {
            return SM3Util.hash(value);
        } else {
            // 默认使用SHA-256
            return DigestUtil.sha256Hex(value);
        }
    }
    
    /**
     * 泛化算法
     */
    private static String generalize(String value, JSONObject config) {
        String level = config.getString("level");
        Integer precision = config.getInteger("precision");
        
        // 地址泛化
        if ("CITY".equals(level)) {
            // 提取城市信息（简化实现）
            if (value.contains("省")) {
                int index = value.indexOf("省");
                if (index + 1 < value.length()) {
                    String after = value.substring(index + 1);
                    if (after.contains("市")) {
                        return value.substring(0, value.indexOf("市") + 1);
                    }
                }
            }
            return value.length() > 6 ? value.substring(0, 6) + "..." : value;
        }
        
        // 数值泛化
        if (precision != null) {
            try {
                double num = Double.parseDouble(value);
                double generalized = Math.floor(num / precision) * precision;
                return String.valueOf((long) generalized);
            } catch (NumberFormatException e) {
                return value;
            }
        }
        
        return value;
    }
    
    /**
     * 扰乱算法
     */
    private static String shuffle(String value, JSONObject config) {
        Double range = config.getDouble("range");
        
        if (range != null) {
            try {
                double num = Double.parseDouble(value);
                double offset = num * range * (RANDOM.nextDouble() * 2 - 1);
                return String.valueOf(num + offset);
            } catch (NumberFormatException e) {
                return value;
            }
        }
        
        // 字符串扰乱
        char[] chars = value.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
    
    /**
     * 截断算法
     */
    private static String truncate(String value, JSONObject config) {
        Integer keepLength = config.getInteger("keepLength");
        
        if (keepLength != null && value.length() > keepLength) {
            return value.substring(0, keepLength);
        }
        
        return value;
    }
    
    /**
     * 手机号脱敏
     */
    public static String maskPhone(String phone) {
        if (StrUtil.isBlank(phone) || phone.length() != 11) {
            return phone;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }
    
    /**
     * 身份证脱敏
     */
    public static String maskIdCard(String idCard) {
        if (StrUtil.isBlank(idCard) || (idCard.length() != 15 && idCard.length() != 18)) {
            return idCard;
        }
        return idCard.replaceAll("(\\d{6})\\d+(\\d{4})", "$1********$2");
    }
    
    /**
     * 银行卡脱敏
     */
    public static String maskBankCard(String bankCard) {
        if (StrUtil.isBlank(bankCard) || bankCard.length() < 8) {
            return bankCard;
        }
        return bankCard.replaceAll("(\\d{4})\\d+(\\d{4})", "$1 **** **** $2");
    }
    
    /**
     * 邮箱脱敏
     */
    public static String maskEmail(String email) {
        if (StrUtil.isBlank(email) || !email.contains("@")) {
            return email;
        }
        return email.replaceAll("(\\w{1,3})\\w+(@\\w+\\.\\w+)", "$1***$2");
    }
    
    /**
     * 姓名脱敏
     */
    public static String maskName(String name) {
        if (StrUtil.isBlank(name) || name.length() <= 1) {
            return name;
        }
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }
    
    /**
     * 地址脱敏
     */
    public static String maskAddress(String address) {
        if (StrUtil.isBlank(address) || address.length() <= 6) {
            return address;
        }
        return address.substring(0, 6) + "****";
    }
}
