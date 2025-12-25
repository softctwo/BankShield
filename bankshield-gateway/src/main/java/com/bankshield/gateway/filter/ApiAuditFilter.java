package com.bankshield.gateway.filter;

import com.alibaba.fastjson2.JSON;
import com.bankshield.gateway.entity.ApiAccessLog;
import com.bankshield.gateway.service.ApiAuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * API审计过滤器
 * 
 * @author BankShield
 */
@Slf4j
@Component
public class ApiAuditFilter extends AbstractGatewayFilterFactory<ApiAuditFilter.Config> implements Ordered {
    
    @Autowired
    private ApiAuditService apiAuditService;
    
    public ApiAuditFilter() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                ServerHttpRequest request = exchange.getRequest();
                long startTime = System.currentTimeMillis();
                
                // 获取请求信息
                String requestPath = request.getPath().value();
                String requestMethod = request.getMethodValue();
                String requestParams = request.getURI().getQuery();
                String requestHeaders = getRequestHeaders(request, config);
                String ipAddress = getClientIp(request);
                Long userId = getUserId(request);
                String userAgent = request.getHeaders().getFirst("User-Agent");
                
                // 继续处理请求
                return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                    long endTime = System.currentTimeMillis();
                    long executeTime = endTime - startTime;
                    
                    ServerHttpResponse response = exchange.getResponse();
                    Integer responseStatus = response.getStatusCode() != null ? response.getStatusCode().value() : null;
                    
                    // 构建访问日志
                    ApiAccessLog accessLog = new ApiAccessLog();
                    accessLog.setRequestPath(requestPath);
                    accessLog.setRequestMethod(requestMethod);
                    accessLog.setRequestParams(requestParams);
                    accessLog.setRequestHeaders(requestHeaders);
                    accessLog.setResponseStatus(responseStatus);
                    accessLog.setExecuteTime(executeTime);
                    accessLog.setIpAddress(ipAddress);
                    accessLog.setUserId(userId);
                    accessLog.setAccessTime(LocalDateTime.now());
                    accessLog.setAccessResult(isSuccess(responseStatus) ? "SUCCESS" : "FAIL");
                    accessLog.setUserAgent(userAgent);
                    
                    // 异步记录审计日志
                    try {
                        apiAuditService.logApiAccess(accessLog);
                        log.debug("API审计日志已记录: {}", requestPath);
                    } catch (Exception e) {
                        log.error("记录API审计日志失败: {}", e.getMessage(), e);
                    }
                }));
            }
        };
    }
    
    /**
     * 获取请求头信息（过滤敏感信息）
     */
    private String getRequestHeaders(ServerHttpRequest request, Config config) {
        Map<String, String> headerMap = new HashMap<>();
        
        // 记录关键请求头（不包含敏感信息）
        String userAgent = request.getHeaders().getFirst("User-Agent");
        if (userAgent != null && userAgent.length() <= 500) {
            headerMap.put("User-Agent", userAgent);
        }
        
        String referer = request.getHeaders().getFirst("Referer");
        if (referer != null && referer.length() <= 500) {
            headerMap.put("Referer", referer);
        }
        
        String accept = request.getHeaders().getFirst("Accept");
        if (accept != null) {
            headerMap.put("Accept", accept);
        }
        
        String contentType = request.getHeaders().getFirst("Content-Type");
        if (contentType != null) {
            headerMap.put("Content-Type", contentType);
        }
        
        String authorization = request.getHeaders().getFirst("Authorization");
        if (authorization != null && config.isLogAuthorization()) {
            // 只记录是否存在Authorization头，不记录具体内容
            headerMap.put("Authorization", "[PRESENT]");
        }
        
        String signature = request.getHeaders().getFirst("X-Signature");
        if (signature != null) {
            headerMap.put("X-Signature", "[PRESENT]");
        }
        
        if (!headerMap.isEmpty()) {
            return JSON.toJSONString(headerMap);
        }
        return "";
    }
    
    /**
     * 获取客户端IP
     */
    private String getClientIp(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress() : "";
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        return ip;
    }
    
    /**
     * 获取用户ID
     */
    private Long getUserId(ServerHttpRequest request) {
        String userId = request.getHeaders().getFirst("X-User-Id");
        if (userId != null && !userId.isEmpty()) {
            try {
                return Long.parseLong(userId);
            } catch (NumberFormatException e) {
                log.warn("解析用户ID失败: {}", userId);
            }
        }
        return null;
    }
    
    /**
     * 判断请求是否成功
     */
    private boolean isSuccess(Integer statusCode) {
        return statusCode != null && statusCode >= 200 && statusCode < 400;
    }
    
    @Override
    public int getOrder() {
        return 0; // 最后执行，记录完整信息
    }
    
    /**
     * 配置类
     */
    public static class Config {
        private boolean enabled = true;
        private boolean logAuthorization = false; // 是否记录Authorization头
        private boolean logBody = false; // 是否记录请求体
        private long maxBodySize = 1024 * 1024; // 最大记录体大小（默认1MB）
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public boolean isLogAuthorization() {
            return logAuthorization;
        }
        
        public void setLogAuthorization(boolean logAuthorization) {
            this.logAuthorization = logAuthorization;
        }
        
        public boolean isLogBody() {
            return logBody;
        }
        
        public void setLogBody(boolean logBody) {
            this.logBody = logBody;
        }
        
        public long getMaxBodySize() {
            return maxBodySize;
        }
        
        public void setMaxBodySize(long maxBodySize) {
            this.maxBodySize = maxBodySize;
        }
    }
}