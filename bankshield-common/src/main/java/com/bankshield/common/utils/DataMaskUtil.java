package com.bankshield.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据脱敏工具类
 */
public class DataMaskUtil {
    
    /**
     * 手机号脱敏：保留前3位和后4位
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
    
    /**
     * 身份证号脱敏：保留前6位和后4位
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 10) {
            return idCard;
        }
        int length = idCard.length();
        return idCard.substring(0, 6) + "********" + idCard.substring(length - 4);
    }
    
    /**
     * 姓名脱敏：保留姓氏
     */
    public static String maskName(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        if (name.length() == 1) {
            return name;
        }
        StringBuilder masked = new StringBuilder();
        masked.append(name.charAt(0));
        for (int i = 1; i < name.length(); i++) {
            masked.append("*");
        }
        return masked.toString();
    }
    
    /**
     * 邮箱脱敏：保留前缀第一个字符和@后面的域名
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        if (parts[0].length() <= 1) {
            return email;
        }
        return parts[0].charAt(0) + "***@" + parts[1];
    }
    
    /**
     * 银行卡号脱敏：保留前4位和后4位
     */
    public static String maskBankCard(String cardNo) {
        if (cardNo == null || cardNo.length() < 8) {
            return cardNo;
        }
        int length = cardNo.length();
        StringBuilder masked = new StringBuilder();
        masked.append(cardNo.substring(0, 4));
        for (int i = 4; i < length - 4; i++) {
            masked.append("*");
        }
        masked.append(cardNo.substring(length - 4));
        return masked.toString();
    }
    
    /**
     * 地址脱敏：保留省市，详细地址脱敏
     */
    public static String maskAddress(String address) {
        if (address == null || address.length() <= 6) {
            return address;
        }
        return address.substring(0, 6) + "****";
    }
    
    /**
     * 自定义脱敏：保留前n位和后m位
     */
    public static String mask(String data, int prefixLen, int suffixLen) {
        if (data == null || data.length() <= prefixLen + suffixLen) {
            return data;
        }
        int length = data.length();
        StringBuilder masked = new StringBuilder();
        masked.append(data.substring(0, prefixLen));
        for (int i = prefixLen; i < length - suffixLen; i++) {
            masked.append("*");
        }
        masked.append(data.substring(length - suffixLen));
        return masked.toString();
    }
    
    /**
     * 完全脱敏：全部替换为*
     */
    public static String maskAll(String data) {
        if (data == null) {
            return null;
        }
        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {
            masked.append("*");
        }
        return masked.toString();
    }
    
    /**
     * 检测是否包含敏感信息（手机号）
     */
    public static boolean containsPhone(String text) {
        if (text == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("1[3-9]\\d{9}");
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }
    
    /**
     * 检测是否包含敏感信息（身份证号）
     */
    public static boolean containsIdCard(String text) {
        if (text == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("\\d{17}[\\dXx]");
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }
    
    /**
     * 自动脱敏文本中的敏感信息
     */
    public static String autoMask(String text) {
        if (text == null) {
            return null;
        }
        String result = text;
        
        // 脱敏手机号
        Pattern phonePattern = Pattern.compile("1[3-9]\\d{9}");
        Matcher phoneMatcher = phonePattern.matcher(result);
        while (phoneMatcher.find()) {
            String phone = phoneMatcher.group();
            result = result.replace(phone, maskPhone(phone));
        }
        
        // 脱敏身份证号
        Pattern idCardPattern = Pattern.compile("\\d{17}[\\dXx]");
        Matcher idCardMatcher = idCardPattern.matcher(result);
        while (idCardMatcher.find()) {
            String idCard = idCardMatcher.group();
            result = result.replace(idCard, maskIdCard(idCard));
        }
        
        return result;
    }
}
