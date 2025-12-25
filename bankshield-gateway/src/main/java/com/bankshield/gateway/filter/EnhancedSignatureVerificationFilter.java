package com.bankshield.gateway.filter;

import com.bankshield.common.utils.EncryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 增强版签名验证过滤器
 * 修复安全漏洞：
 * 1. 包含请求体在签名计算中
 * 2. 关键Header参与签名
 * 3. 使用HMAC-SHA256替代简单拼接+哈希
 * 4. 强化防重放攻击防护
 * 
 * @author BankShield
 */
@Slf4j
@Component
public class EnhancedSignatureVerificationFilter extends AbstractGatewayFilterFactory<EnhancedSignatureVerificationFilter.Config> implements Ordered {
    
    private static final String SIGNATURE_HEADER = "X-Signature";
    private static final String TIMESTAMP_HEADER = "X-Timestamp";
    private static final String NONCE_HEADER = "X-Nonce";
    private static final String APP_ID_HEADER = "X-App-Id";
    private static final String CONTENT_MD5_HEADER = "Content-MD5";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    
    // 参与签名的关键Header（按字母顺序排列）
    private static final List<String> SIGNED_HEADERS = Arrays.asList(
        CONTENT_MD5_HEADER,
        CONTENT_TYPE_HEADER
    );
    
    private static final long SIGNATURE_EXPIRE_MS = 5 * 60 * 1000; // 5分钟
    private static final long MAX_BODY_SIZE = 10 * 1024 * 1024; // 10MB
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    public EnhancedSignatureVerificationFilter() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                ServerHttpRequest request = exchange.getRequest();
                
                // 判断是否需要签名验证
                if (!requiresSignature(request)) {
                    return chain.filter(exchange);
                }
                
                // 读取并缓存请求体
                return readRequestBody(request)
                    .flatMap(bodyData -> {
                        String bodyString = bodyData.getT1();
                        DataBuffer cachedBuffer = bodyData.getT2();
                        
                        // 创建缓存请求的装饰器
                        CachedBodyHttpRequestDecorator cachedRequest = new CachedBodyHttpRequestDecorator(request, bodyString, cachedBuffer);
                        ServerWebExchange cachedExchange = exchange.mutate().request(cachedRequest).build();
                        
                        // 执行签名验证
                        return performSignatureVerification(cachedExchange, bodyString);
                    })
                    .flatMap(valid -> {
                        if (valid) {
                            return chain.filter(exchange);
                        } else {
                            return handleSignatureInvalid(exchange);
                        }
                    })
                    .onErrorResume(throwable -> {
                        log.error("签名验证过程出错", throwable);
                        return handleSignatureInvalid(exchange);
                    });
            }
        };
    }
    
    /**
     * 读取请求体内容
     */
    private Mono<Tuple2<String, DataBuffer>> readRequestBody(ServerHttpRequest request) {
        return request.getBody()
            .collectList()
            .map(dataBuffers -> {
                // 合并所有DataBuffer
                int totalSize = dataBuffers.stream().mapToInt(buffer -> buffer.readableByteCount()).sum();
                if (totalSize > MAX_BODY_SIZE) {
                    throw new RuntimeException("请求体过大，超过最大限制");
                }
                
                byte[] combinedBytes = new byte[totalSize];
                int offset = 0;
                for (DataBuffer buffer : dataBuffers) {
                    int length = buffer.readableByteCount();
                    buffer.read(combinedBytes, offset, length);
                    offset += length;
                }
                
                String bodyString = new String(combinedBytes, StandardCharsets.UTF_8);
                DataBuffer cachedBuffer = request.getResponse().bufferFactory().wrap(combinedBytes);
                
                return new Tuple2<>(bodyString, cachedBuffer);
            });
    }
    
    /**
     * 执行签名验证
     */
    private Mono<Boolean> performSignatureVerification(ServerWebExchange exchange, String requestBody) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 获取签名参数
        String signature = request.getHeaders().getFirst(SIGNATURE_HEADER);
        String timestamp = request.getHeaders().getFirst(TIMESTAMP_HEADER);
        String nonce = request.getHeaders().getFirst(NONCE_HEADER);
        String appId = request.getHeaders().getFirst(APP_ID_HEADER);
        
        // 验证必填参数
        if (!StringUtils.hasText(signature) || !StringUtils.hasText(timestamp) || 
            !StringUtils.hasText(nonce) || !StringUtils.hasText(appId)) {
            log.warn("签名验证失败：缺少必要参数");
            return Mono.just(false);
        }
        
        // 验证时间戳
        if (!verifyTimestamp(timestamp)) {
            log.warn("签名验证失败：时间戳过期");
            return Mono.just(false);
        }
        
        // 验证nonce（防止重放）
        if (!verifyNonce(nonce)) {
            log.warn("签名验证失败：nonce重复使用");
            return Mono.just(false);
        }
        
        // 获取应用的签名密钥
        String secretKey = getSecretKey(appId);
        if (!StringUtils.hasText(secretKey)) {
            log.warn("签名验证失败：找不到应用密钥");
            return Mono.just(false);
        }
        
        // 验证签名
        boolean signatureValid = verifySignature(signature, appId, timestamp, nonce, request, requestBody, secretKey);
        if (signatureValid) {
            log.debug("签名验证成功：appId={}", appId);
        } else {
            log.warn("签名验证失败：签名不匹配");
        }
        
        return Mono.just(signatureValid);
    }
    
    /**
     * 判断是否需要签名验证
     */
    private boolean requiresSignature(ServerHttpRequest request) {
        String path = request.getPath().value();
        String method = request.getMethodValue();
        
        // POST/PUT/DELETE请求需要签名验证
        return "POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method) ||
               path.startsWith("/api/sensitive");
    }
    
    /**
     * 验证签名（使用HMAC-SHA256）
     */
    private boolean verifySignature(String signature, String appId, String timestamp, String nonce, 
                                   ServerHttpRequest request, String requestBody, String secretKey) {
        try {
            // 构建签名字符串的各个部分
            String method = request.getMethodValue();
            String path = request.getPath().value();
            String queryString = request.getURI().getQuery();
            queryString = queryString == null ? "" : queryString;
            
            // 获取参与签名的Header
            Map<String, String> signedHeaders = getSignedHeaders(request.getHeaders());
            String headerString = buildHeaderString(signedHeaders);
            
            // 验证Content-MD5（如果存在）
            if (signedHeaders.containsKey(CONTENT_MD5_HEADER)) {
                String expectedMd5 = signedHeaders.get(CONTENT_MD5_HEADER);
                String calculatedMd5 = calculateMd5(requestBody);
                if (!expectedMd5.equals(calculatedMd5)) {
                    log.warn("Content-MD5不匹配：期望={}, 计算={}", expectedMd5, calculatedMd5);
                    return false;
                }
            }
            
            // 构建待签名字符串（按规范格式）
            String signStr = buildSignString(method, path, queryString, timestamp, nonce, appId, headerString, requestBody);
            
            // 使用HMAC-SHA256计算签名
            String calculatedSignature = EncryptUtil.hmacSha256(secretKey, signStr);
            
            log.debug("待签名字符串: {}", signStr);
            log.debug("期望签名: {}", signature);
            log.debug("计算签名: {}", calculatedSignature);
            
            return signature.equals(calculatedSignature);
        } catch (Exception e) {
            log.error("签名验证过程出错", e);
            return false;
        }
    }
    
    /**
     * 获取参与签名的Header
     */
    private Map<String, String> getSignedHeaders(HttpHeaders headers) {
        Map<String, String> signedHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        
        for (String headerName : SIGNED_HEADERS) {
            String headerValue = headers.getFirst(headerName);
            if (StringUtils.hasText(headerValue)) {
                signedHeaders.put(headerName, headerValue);
            }
        }
        
        return signedHeaders;
    }
    
    /**
     * 构建Header字符串
     */
    private String buildHeaderString(Map<String, String> headers) {
        if (headers.isEmpty()) {
            return "";
        }
        
        return headers.entrySet().stream()
            .map(entry -> entry.getKey().toLowerCase() + ":" + entry.getValue().trim())
            .collect(Collectors.joining("\n"));
    }
    
    /**
     * 构建待签名字符串
     */
    private String buildSignString(String method, String path, String queryString, String timestamp, 
                                 String nonce, String appId, String headerString, String body) {
        StringBuilder signStr = new StringBuilder();
        
        // HTTP方法
        signStr.append(method.toUpperCase()).append("\n");
        
        // 请求路径
        signStr.append(path).append("\n");
        
        // 查询字符串
        signStr.append(queryString).append("\n");
        
        // Header字符串
        signStr.append(headerString).append("\n");
        
        // 时间戳
        signStr.append(timestamp).append("\n");
        
        // 随机数
        signStr.append(nonce).append("\n");
        
        // 应用ID
        signStr.append(appId).append("\n");
        
        // 请求体（如果存在）
        if (StringUtils.hasText(body)) {
            signStr.append(body);
        }
        
        return signStr.toString();
    }
    
    /**
     * 计算MD5
     */
    private String calculateMd5(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        return EncryptUtil.md5Hash(content);
    }
    
    /**
     * 验证时间戳
     */
    private boolean verifyTimestamp(String timestamp) {
        try {
            long ts = Long.parseLong(timestamp);
            long now = System.currentTimeMillis();
            return Math.abs(now - ts) < SIGNATURE_EXPIRE_MS;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 验证nonce（防止重放攻击）
     */
    private boolean verifyNonce(String nonce) {
        // 验证nonce格式（应为32位十六进制字符串）
        if (nonce == null || nonce.length() != 32 || !nonce.matches("[a-fA-F0-9]{32}")) {
            log.warn("nonce格式无效: {}", nonce);
            return false;
        }
        
        // 验证nonce是否已使用
        String key = "signature:nonce:" + nonce;
        Boolean exists = redisTemplate.hasKey(key);
        if (exists != null && exists) {
            return false;
        }
        
        // 存储nonce，有效期5分钟
        redisTemplate.opsForValue().set(key, "1", SIGNATURE_EXPIRE_MS, TimeUnit.MILLISECONDS);
        return true;
    }
    
    /**
     * 获取应用的签名密钥
     */
    private String getSecretKey(String appId) {
        // 从Redis或数据库获取应用的签名密钥
        String key = "app:secret:" + appId;
        return redisTemplate.opsForValue().get(key);
    }
    
    /**
     * 处理签名无效
     */
    private Mono<Void> handleSignatureInvalid(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = "{\"code\":401,\"message\":\"签名验证失败\"}";
        
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
    
    @Override
    public int getOrder() {
        return -100; // 在高优先级执行
    }
    
    /**
     * 配置类
     */
    public static class Config {
        private boolean enabled = true;
        private long expireTime = SIGNATURE_EXPIRE_MS;
        private List<String> signedHeaders = SIGNED_HEADERS;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public long getExpireTime() {
            return expireTime;
        }
        
        public void setExpireTime(long expireTime) {
            this.expireTime = expireTime;
        }
        
        public List<String> getSignedHeaders() {
            return signedHeaders;
        }
        
        public void setSignedHeaders(List<String> signedHeaders) {
            this.signedHeaders = signedHeaders;
        }
    }
    
    /**
     * 简单的元组类，用于返回多个值
     */
    private static class Tuple2<T1, T2> {
        private final T1 t1;
        private final T2 t2;
        
        public Tuple2(T1 t1, T2 t2) {
            this.t1 = t1;
            this.t2 = t2;
        }
        
        public T1 getT1() {
            return t1;
        }
        
        public T2 getT2() {
            return t2;
        }
    }
}