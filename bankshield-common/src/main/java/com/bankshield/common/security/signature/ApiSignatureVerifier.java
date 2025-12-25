package com.bankshield.common.security.signature;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.bankshield.common.result.Result;
import com.bankshield.common.result.ResultCode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * API签名验证器
 * 用于验证API请求的签名，防止请求被篡改和重放攻击
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Slf4j
@Component
public class ApiSignatureVerifier {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final long SIGNATURE_EXPIRE_MS = 300000; // 5分钟
    private static final String SIGNATURE_HEADER = "X-BankShield-Signature";
    private static final String TIMESTAMP_HEADER = "X-BankShield-Timestamp";
    private static final String NONCE_HEADER = "X-BankShield-Nonce";
    private static final String APP_ID_HEADER = "X-App-Id";
    
    // 模拟应用密钥存储（实际应该存储在数据库或密钥管理系统中）
    private static final Map<String, String> APP_SECRET_STORE = new HashMap<>();
    
    static {
        APP_SECRET_STORE.put("bankshield_web", "sk_live_1234567890abcdef");
        APP_SECRET_STORE.put("bankshield_mobile", "sk_live_abcdef1234567890");
        APP_SECRET_STORE.put("bankshield_api", "sk_live_0987654321fedcba");
    }
    
    /**
     * 验证API请求签名
     * 
     * @param request HTTP请求
     * @return 验证结果
     */
    public Result<Void> verifySignature(HttpServletRequest request) {
        try {
            // 获取请求头
            String signature = request.getHeader(SIGNATURE_HEADER);
            String timestamp = request.getHeader(TIMESTAMP_HEADER);
            String nonce = request.getHeader(NONCE_HEADER);
            String appId = request.getHeader(APP_ID_HEADER);
            
            // 检查必填头
            if (StrUtil.hasBlank(signature, timestamp, nonce, appId)) {
                log.warn("API签名验证失败：缺少必填请求头");
                return Result.error(ResultCode.UNAUTHORIZED.getCode(), "缺少必填请求头");
            }
            
            // 验证时间戳
            Result<Void> timestampResult = validateTimestamp(timestamp);
            if (!timestampResult.isSuccess()) {
                return timestampResult;
            }
            
            // 验证nonce（防重放）
            Result<Void> nonceResult = validateNonce(nonce);
            if (!nonceResult.isSuccess()) {
                return nonceResult;
            }
            
            // 获取应用密钥
            String secretKey = getSecretKey(appId);
            if (secretKey == null) {
                log.warn("API签名验证失败：无效的应用ID - {}", appId);
                return Result.error(ResultCode.UNAUTHORIZED.getCode(), "无效的应用ID");
            }
            
            // 计算签名
            String calculatedSignature = calculateSignature(request, timestamp, nonce, secretKey);
            
            // 比较签名
            if (!signature.equals(calculatedSignature)) {
                log.warn("API签名验证失败：签名不匹配 - 期望: {}, 实际: {}", calculatedSignature, signature);
                return Result.error(ResultCode.UNAUTHORIZED.getCode(), "签名验证失败");
            }
            
            // 记录nonce已使用
            markNonceAsUsed(nonce);
            
            log.debug("API签名验证成功 - AppId: {}, Path: {}", appId, request.getRequestURI());
            return Result.success();
            
        } catch (Exception e) {
            log.error("API签名验证异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.ERROR.getCode(), "签名验证异常");
        }
    }
    
    /**
     * 验证时间戳
     * 
     * @param timestamp 时间戳
     * @return 验证结果
     */
    private Result<Void> validateTimestamp(String timestamp) {
        try {
            long ts = Long.parseLong(timestamp);
            long now = System.currentTimeMillis();
            long diff = Math.abs(now - ts);
            
            if (diff > SIGNATURE_EXPIRE_MS) {
                log.warn("API签名验证失败：时间戳过期 - 当前时间: {}, 请求时间: {}, 差值: {}ms",
                        now, ts, diff);
                return Result.error(ResultCode.UNAUTHORIZED.getCode(), "请求已过期");
            }
            
            return Result.success();
            
        } catch (NumberFormatException e) {
            log.warn("API签名验证失败：无效的时间戳格式 - {}", timestamp);
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "无效的时间戳格式");
        }
    }
    
    /**
     * 验证nonce
     * 
     * @param nonce 随机数
     * @return 验证结果
     */
    private Result<Void> validateNonce(String nonce) {
        if (nonce.length() < 8 || nonce.length() > 64) {
            log.warn("API签名验证失败：无效的nonce长度 - {}", nonce.length());
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "无效的nonce");
        }

        // 检查是否已使用（防重放）
        if (isNonceUsed(nonce)) {
            log.warn("API签名验证失败：nonce重放攻击 - {}", nonce);
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "请求重复");
        }
        
        return Result.success();
    }
    
    /**
     * 计算请求签名
     *
     * @param request HTTP请求
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param secretKey 密钥
     * @return 签名字符串
     */
    private String calculateSignature(HttpServletRequest request, String timestamp, String nonce, String secretKey) {
        // 构建签名字符串
        String method = request.getMethod().toUpperCase();
        String path = request.getRequestURI();
        String queryString = StrUtil.nullToEmpty(request.getQueryString());
        String body = getRequestBody(request);

        // 获取并排序请求头
        Map<String, String> headers = getSortedHeaders(request);
        String headerStr = buildHeaderString(headers);

        return calculateSignatureInternal(method, path, queryString, headerStr, body, timestamp, nonce, secretKey);
    }

    /**
     * 计算请求签名（使用参数）
     */
    private String calculateSignature(String method, String path, String queryString, String body,
                                       Map<String, String> headers, String timestamp, String nonce, String secretKey) {
        String headerStr = buildHeaderString(headers);
        return calculateSignatureInternal(method, path, queryString, headerStr, body, timestamp, nonce, secretKey);
    }

    /**
     * 内部签名计算方法
     */
    private String calculateSignatureInternal(String method, String path, String queryString, String headerStr,
                                               String body, String timestamp, String nonce, String secretKey) {
        // 构建待签名字符串
        String signStr = String.join("\n",
            method,
            path,
            queryString,
            headerStr,
            body,
            timestamp,
            nonce,
            secretKey
        );

        log.debug("待签名字符串: {}", signStr);

        // 使用SHA-256计算签名
        return DigestUtil.sha256Hex(signStr.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 获取排序后的请求头
     * 
     * @param request HTTP请求
     * @return 排序后的请求头
     */
    private Map<String, String> getSortedHeaders(HttpServletRequest request) {
        Map<String, String> headers = new TreeMap<>();
        
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            
            // 只包含参与签名的请求头
            if (isHeaderIncludedInSignature(headerName)) {
                headers.put(headerName.toLowerCase(), headerValue);
            }
        }
        
        return headers;
    }
    
    /**
     * 构建请求头字符串
     * 
     * @param headers 请求头
     * @return 请求头字符串
     */
    private String buildHeaderString(Map<String, String> headers) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
        }
        return sb.toString().trim();
    }
    
    /**
     * 判断请求头是否参与签名
     * 
     * @param headerName 请求头名称
     * @return 是否参与签名
     */
    private boolean isHeaderIncludedInSignature(String headerName) {
        String lowerName = headerName.toLowerCase();
        
        // 排除签名相关的请求头
        if (lowerName.equals(SIGNATURE_HEADER.toLowerCase()) ||
            lowerName.equals(TIMESTAMP_HEADER.toLowerCase()) ||
            lowerName.equals(NONCE_HEADER.toLowerCase()) ||
            lowerName.equals(APP_ID_HEADER.toLowerCase())) {
            return false;
        }
        
        // 包含以下请求头
        return lowerName.startsWith("x-bankshield-") ||
               lowerName.equals("content-type") ||
               lowerName.equals("content-length") ||
               lowerName.equals("accept") ||
               lowerName.equals("accept-encoding") ||
               lowerName.equals("accept-language") ||
               lowerName.equals("user-agent");
    }
    
    /**
     * 获取请求体
     * 
     * @param request HTTP请求
     * @return 请求体
     */
    private String getRequestBody(HttpServletRequest request) {
        try {
            // 从自定义的缓存中获取请求体
            Object bodyObj = request.getAttribute("cachedRequestBody");
            if (bodyObj != null) {
                return (String) bodyObj;
            }
            return "";
        } catch (Exception e) {
            log.error("获取请求体失败: {}", e.getMessage());
            return "";
        }
    }
    
    /**
     * 获取应用密钥
     * 
     * @param appId 应用ID
     * @return 应用密钥
     */
    private String getSecretKey(String appId) {
        return APP_SECRET_STORE.get(appId);
    }
    
    /**
     * 检查nonce是否已使用
     * 
     * @param nonce 随机数
     * @return 是否已使用
     */
    private boolean isNonceUsed(String nonce) {
        String key = "nonce:used:" + nonce;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    /**
     * 标记nonce为已使用
     * 
     * @param nonce 随机数
     */
    private void markNonceAsUsed(String nonce) {
        String key = "nonce:used:" + nonce;
        // 存储nonce，有效期5分钟（与签名有效期一致）
        redisTemplate.opsForValue().set(key, "1", SIGNATURE_EXPIRE_MS, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 生成签名参数
     * 
     * @param appId 应用ID
     * @param secretKey 应用密钥
     * @param method HTTP方法
     * @param path 请求路径
     * @param queryString 查询字符串
     * @param body 请求体
     * @param headers 请求头
     * @return 签名参数
     */
    public SignatureParams generateSignatureParams(String appId, String secretKey, String method, String path,
                                                   String queryString, String body, Map<String, String> headers) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = generateNonce();

        // 使用重载的签名计算方法
        String signature = calculateSignature(method, path, queryString, body, headers, timestamp, nonce, secretKey);

        SignatureParams params = new SignatureParams();
        params.setAppId(appId);
        params.setTimestamp(timestamp);
        params.setNonce(nonce);
        params.setSignature(signature);

        return params;
    }
    
    /**
     * 生成随机nonce
     * 
     * @return 随机nonce
     */
    private String generateNonce() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 签名参数
     */
    @Data
    public static class SignatureParams {
        private String appId;
        private String timestamp;
        private String nonce;
        private String signature;
        
        public Map<String, String> toHeaders() {
            Map<String, String> headers = new HashMap<>();
            headers.put(APP_ID_HEADER, appId);
            headers.put(TIMESTAMP_HEADER, timestamp);
            headers.put(NONCE_HEADER, nonce);
            headers.put(SIGNATURE_HEADER, signature);
            return headers;
        }
    }
}