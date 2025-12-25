package com.bankshield.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 签名工具类
 * 用于客户端生成符合安全规范的签名
 * 
 * @author BankShield
 */
@Slf4j
public class SignatureUtil {
    
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    private static final String SIGNATURE_HEADER = "X-Signature";
    private static final String TIMESTAMP_HEADER = "X-Timestamp";
    private static final String NONCE_HEADER = "X-Nonce";
    private static final String APP_ID_HEADER = "X-App-Id";
    private static final String CONTENT_MD5_HEADER = "Content-MD5";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    
    // 参与签名的关键Header
    private static final List<String> SIGNED_HEADERS = Arrays.asList(
        CONTENT_MD5_HEADER,
        CONTENT_TYPE_HEADER
    );
    
    /**
     * 生成签名字符串
     * 
     * @param method HTTP方法
     * @param path 请求路径
     * @param queryString 查询字符串
     * @param headers HTTP头
     * @param body 请求体
     * @param appId 应用ID
     * @param secretKey 密钥
     * @return 签名结果
     */
    public static String generateSignature(String method, String path, String queryString,
                                         Map<String, String> headers, String body, String appId, String secretKey) {
        try {
            // 生成时间戳
            String timestamp = String.valueOf(System.currentTimeMillis());
            
            // 生成随机数
            String nonce = generateNonce();
            
            // 计算Content-MD5（如果存在请求体）
            if (body != null && !body.isEmpty()) {
                String contentMd5 = calculateMd5(body);
                headers.put(CONTENT_MD5_HEADER, contentMd5);
            }
            
            // 获取参与签名的Header
            Map<String, String> signedHeaders = getSignedHeaders(headers);
            String headerString = buildHeaderString(signedHeaders);
            
            // 构建待签名字符串
            String signStr = buildSignString(method, path, queryString, timestamp, nonce, appId, headerString, body);
            
            // 使用HMAC-SHA256计算签名
            String signature = hmacSha256(secretKey, signStr);
            
            // 添加签名相关的Header
            headers.put(TIMESTAMP_HEADER, timestamp);
            headers.put(NONCE_HEADER, nonce);
            headers.put(APP_ID_HEADER, appId);
            headers.put(SIGNATURE_HEADER, signature);
            
            return signature;
        } catch (Exception e) {
            log.error("生成签名失败", e);
            throw new RuntimeException("生成签名失败", e);
        }
    }
    
    /**
     * 验证签名
     * 
     * @param signature 待验证签名
     * @param method HTTP方法
     * @param path 请求路径
     * @param queryString 查询字符串
     * @param headers HTTP头
     * @param body 请求体
     * @param appId 应用ID
     * @param secretKey 密钥
     * @return 签名是否有效
     */
    public static boolean verifySignature(String signature, String method, String path, String queryString,
                                        Map<String, String> headers, String body, String appId, String secretKey) {
        try {
            String timestamp = headers.get(TIMESTAMP_HEADER);
            String nonce = headers.get(NONCE_HEADER);
            
            // 获取参与签名的Header
            Map<String, String> signedHeaders = getSignedHeaders(headers);
            String headerString = buildHeaderString(signedHeaders);
            
            // 构建待签名字符串
            String signStr = buildSignString(method, path, queryString, timestamp, nonce, appId, headerString, body);
            
            // 计算签名
            String calculatedSignature = hmacSha256(secretKey, signStr);
            
            return signature.equals(calculatedSignature);
        } catch (Exception e) {
            log.error("验证签名失败", e);
            return false;
        }
    }
    
    /**
     * 生成随机数
     */
    private static String generateNonce() {
        // 生成16字节的随机数，转换为32位十六进制字符串
        byte[] nonceBytes = new byte[16];
        new Random().nextBytes(nonceBytes);
        StringBuilder nonce = new StringBuilder();
        for (byte b : nonceBytes) {
            nonce.append(String.format("%02x", b));
        }
        return nonce.toString();
    }
    
    /**
     * 计算MD5
     */
    private static String calculateMd5(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] md5Bytes = md.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder md5 = new StringBuilder();
            for (byte b : md5Bytes) {
                md5.append(String.format("%02x", b));
            }
            return md5.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5计算失败", e);
        }
    }
    
    /**
     * 获取参与签名的Header
     */
    private static Map<String, String> getSignedHeaders(Map<String, String> headers) {
        Map<String, String> signedHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        
        for (String headerName : SIGNED_HEADERS) {
            String headerValue = headers.get(headerName);
            if (headerValue != null && !headerValue.trim().isEmpty()) {
                signedHeaders.put(headerName, headerValue.trim());
            }
        }
        
        return signedHeaders;
    }
    
    /**
     * 构建Header字符串
     */
    private static String buildHeaderString(Map<String, String> headers) {
        if (headers.isEmpty()) {
            return "";
        }
        
        return headers.entrySet().stream()
            .map(entry -> entry.getKey().toLowerCase() + ":" + entry.getValue())
            .collect(Collectors.joining("\n"));
    }
    
    /**
     * 构建待签名字符串
     */
    private static String buildSignString(String method, String path, String queryString, String timestamp, 
                                        String nonce, String appId, String headerString, String body) {
        StringBuilder signStr = new StringBuilder();
        
        // HTTP方法
        signStr.append(method.toUpperCase()).append("\n");
        
        // 请求路径
        signStr.append(path).append("\n");
        
        // 查询字符串
        signStr.append(queryString != null ? queryString : "").append("\n");
        
        // Header字符串
        signStr.append(headerString).append("\n");
        
        // 时间戳
        signStr.append(timestamp).append("\n");
        
        // 随机数
        signStr.append(nonce).append("\n");
        
        // 应用ID
        signStr.append(appId).append("\n");
        
        // 请求体（如果存在）
        if (body != null && !body.isEmpty()) {
            signStr.append(body);
        }
        
        return signStr.toString();
    }
    
    /**
     * HMAC-SHA256签名
     */
    private static String hmacSha256(String secretKey, String data) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256签名失败", e);
        }
    }
    
    /**
     * 生成完整的签名Headers
     */
    public static Map<String, String> generateSignatureHeaders(String method, String path, String queryString,
                                                             String body, String appId, String secretKey) {
        Map<String, String> headers = new HashMap<>();
        generateSignature(method, path, queryString, headers, body, appId, secretKey);
        return headers;
    }
}