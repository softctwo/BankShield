package com.bankshield.gateway.filter;

import com.bankshield.common.utils.EncryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 签名验证过滤器
 * 
 * @author BankShield
 */
@Slf4j
@Component
public class SignatureVerificationFilter extends AbstractGatewayFilterFactory<SignatureVerificationFilter.Config> implements Ordered {
    
    private static final String SIGNATURE_HEADER = "X-Signature";
    private static final String TIMESTAMP_HEADER = "X-Timestamp";
    private static final String NONCE_HEADER = "X-Nonce";
    private static final String APP_ID_HEADER = "X-App-Id";
    private static final long SIGNATURE_EXPIRE_MS = 5 * 60 * 1000; // 5分钟
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    public SignatureVerificationFilter() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                ServerHttpRequest request = exchange.getRequest();
                ServerHttpResponse response = exchange.getResponse();
                
                // 判断是否需要签名验证
                if (requiresSignature(request)) {
                    String signature = request.getHeaders().getFirst(SIGNATURE_HEADER);
                    String timestamp = request.getHeaders().getFirst(TIMESTAMP_HEADER);
                    String nonce = request.getHeaders().getFirst(NONCE_HEADER);
                    String appId = request.getHeaders().getFirst(APP_ID_HEADER);
                    
                    // 验证必填参数
                    if (!StringUtils.hasText(signature) || !StringUtils.hasText(timestamp) || 
                        !StringUtils.hasText(nonce) || !StringUtils.hasText(appId)) {
                        log.warn("签名验证失败：缺少必要参数");
                        return handleSignatureInvalid(exchange);
                    }
                    
                    // 验证时间戳
                    if (!verifyTimestamp(timestamp)) {
                        log.warn("签名验证失败：时间戳过期");
                        return handleSignatureExpired(exchange);
                    }
                    
                    // 验证nonce（防止重放）
                    if (!verifyNonce(nonce)) {
                        log.warn("签名验证失败：nonce重复使用");
                        return handleNonceDuplicate(exchange);
                    }
                    
                    // 获取应用的签名密钥
                    String secretKey = getSecretKey(appId);
                    if (!StringUtils.hasText(secretKey)) {
                        log.warn("签名验证失败：找不到应用密钥");
                        return handleSignatureInvalid(exchange);
                    }
                    
                    // 验证签名
                    if (!verifySignature(signature, appId, timestamp, nonce, request, secretKey)) {
                        log.warn("签名验证失败：签名不匹配");
                        return handleSignatureInvalid(exchange);
                    }
                    
                    log.debug("签名验证成功：appId={}", appId);
                }
                
                return chain.filter(exchange);
            }
        };
    }
    
    /**
     * 判断是否需要签名验证
     */
    private boolean requiresSignature(ServerHttpRequest request) {
        // 可以根据请求路径、方法或其他条件判断是否需要签名验证
        String path = request.getPath().value();
        String method = request.getMethodValue();
        
        // 示例：POST/PUT/DELETE请求需要签名验证
        return "POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method) ||
               path.startsWith("/api/sensitive"); // 敏感接口需要签名验证
    }
    
    /**
     * 验证签名
     */
    private boolean verifySignature(String signature, String appId, String timestamp, String nonce, 
                                   ServerHttpRequest request, String secretKey) {
        try {
            // 构建签名字符串
            String method = request.getMethodValue();
            String path = request.getPath().value();
            String queryString = request.getURI().getQuery();
            queryString = queryString == null ? "" : queryString;
            
            // 构建待签名字符串：method + path + query + timestamp + nonce + appId
            String signStr = method + "\n" + path + "\n" + queryString + "\n" + timestamp + "\n" + nonce + "\n" + appId;
            
            // 使用SM3国密哈希算法计算签名
            String calculatedSignature = EncryptUtil.sm3Hash(signStr + secretKey);
            
            return signature.equals(calculatedSignature);
        } catch (Exception e) {
            log.error("签名验证过程出错", e);
            return false;
        }
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
        org.springframework.core.io.buffer.DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
    
    /**
     * 处理签名过期
     */
    private Mono<Void> handleSignatureExpired(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = "{\"code\":401,\"message\":\"签名已过期\"}";
        
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        org.springframework.core.io.buffer.DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
    
    /**
     * 处理nonce重复
     */
    private Mono<Void> handleNonceDuplicate(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = "{\"code\":401,\"message\":\"请求重复提交\"}";
        
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        org.springframework.core.io.buffer.DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
    
    @Override
    public int getOrder() {
        return -99; // 在限流过滤器之后执行
    }
    
    /**
     * 配置类
     */
    public static class Config {
        private boolean enabled = true;
        private long expireTime = SIGNATURE_EXPIRE_MS;
        
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
    }
}