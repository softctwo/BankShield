package com.bankshield.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 数据脱敏工具类
 * 用于对敏感数据进行脱敏处理
 */
@Slf4j
public class DataMaskUtil {
    
    /**
     * 手机号脱敏
     * 保留前3位和后4位，中间用*代替
     * 示例: 13812345678 -> 138****5678
     */
    public static String maskPhone(String phone) {
        if (StringUtils.isBlank(phone)) {
            return phone;
        }
        if (phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
    
    /**
     * 身份证号脱敏
     * 保留前6位和后4位，中间用*代替
     * 示例: 110101199003074567 -> 110101********4567
     */
    public static String maskIdCard(String idCard) {
        if (StringUtils.isBlank(idCard)) {
            return idCard;
        }
        if (idCard.length() < 10) {
            return idCard;
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
    }
    
    /**
     * 银行卡号脱敏
     * 保留前6位和后4位，中间用*代替
     * 示例: 6222021234567890123 -> 622202*********0123
     */
    public static String maskBankCard(String bankCard) {
        if (StringUtils.isBlank(bankCard)) {
            return bankCard;
        }
        if (bankCard.length() < 10) {
            return bankCard;
        }
        return bankCard.substring(0, 6) + "*********" + bankCard.substring(bankCard.length() - 4);
    }
    
    /**
     * 姓名脱敏
     * 姓不变，名用*代替
     * 示例: 张三 -> 张*, 欧阳锋 -> 欧阳*
     */
    public static String maskName(String name) {
        if (StringUtils.isBlank(name)) {
            return name;
        }
        if (name.length() == 1) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        // 复姓处理
        if (name.length() >= 3) {
            return name.substring(0, name.length() - 1) + "*";
        }
        return name;
    }
    
    /**
     * 邮箱脱敏
     * 保留前缀第一个字符和@后面的内容
     * 示例: zhangsan@email.com -> z****@email.com
     */
    public static String maskEmail(String email) {
        if (StringUtils.isBlank(email) || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) {
            return email;
        }
        return email.charAt(0) + "****" + email.substring(atIndex);
    }
    
    /**
     * 地址脱敏
     * 保留前6位，后面用*代替
     * 示例: 北京市朝阳区建国路88号 -> 北京市朝阳区******
     */
    public static String maskAddress(String address) {
        if (StringUtils.isBlank(address)) {
            return address;
        }
        if (address.length() <= 6) {
            return address;
        }
        return address.substring(0, 6) + "******";
    }
    
    /**
     * 密码脱敏
     * 全部替换为*
     * 示例: 123456 -> ******
     */
    public static String maskPassword(String password) {
        if (StringUtils.isBlank(password)) {
            return password;
        }
        return "********";
    }
    
    /**
     * 自定义脱敏
     * @param text 原始文本
     * @param prefixLen 前缀保留长度
     * @param suffixLen 后缀保留长度
     * @param maskChar 掩码字符
     * @param maskLen 掩码长度（如果为0则自动计算）
     */
    public static String maskCustom(String text, int prefixLen, int suffixLen, char maskChar, int maskLen) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        if (text.length() <= prefixLen + suffixLen) {
            return text;
        }
        
        String prefix = text.substring(0, prefixLen);
        String suffix = text.substring(text.length() - suffixLen);
        
        int actualMaskLen = maskLen;
        if (maskLen <= 0) {
            actualMaskLen = text.length() - prefixLen - suffixLen;
        }
        
        String mask = String.valueOf(maskChar).repeat(actualMaskLen);
        return prefix + mask + suffix;
    }
    
    /**
     * 根据敏感类型自动脱敏
     */
    public static String maskByType(String text, String type) {
        if (StringUtils.isBlank(text) || StringUtils.isBlank(type)) {
            return text;
        }
        
        switch (type.toUpperCase()) {
            case "PHONE":
            case "MOBILE":
                return maskPhone(text);
            case "IDCARD":
            case "ID_CARD":
                return maskIdCard(text);
            case "BANKCARD":
            case "BANK_CARD":
                return maskBankCard(text);
            case "NAME":
                return maskName(text);
            case "EMAIL":
                return maskEmail(text);
            case "ADDRESS":
                return maskAddress(text);
            case "PASSWORD":
            case "PWD":
                return maskPassword(text);
            default:
                // 默认脱敏方式：保留前3位和后4位
                return maskCustom(text, 3, 4, '*', 4);
        }
    }
}