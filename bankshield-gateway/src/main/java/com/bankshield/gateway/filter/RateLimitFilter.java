package com.bankshield.gateway.filter;

import com.bankshield.gateway.service.RateLimitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 限流过滤器
 * 
 * @author BankShield
 */
@Slf4j
@Component
public class RateLimitFilter extends AbstractGatewayFilterFactory<RateLimitFilter.Config> implements Ordered {
    
    @Autowired
    private RateLimitService rateLimitService;
    
    public RateLimitFilter() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                ServerHttpRequest request = exchange.getRequest();
                ServerHttpResponse response = exchange.getResponse();
                
                // 获取客户端IP
                String clientIp = getClientIp(request);
                
                // 获取用户ID（如果有认证信息）
                Long userId = getUserId(request);
                
                // 获取请求路径
                String requestPath = request.getURI().getPath();
                
                // 检查IP限流
                if (!rateLimitService.isAllowed("IP", clientIp)) {
                    log.warn("IP限流拒绝访问: {}, 路径: {}", clientIp, requestPath);
                    response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    return response.setComplete();
                }
                
                // 检查用户限流
                if (userId != null && !rateLimitService.isAllowed("USER", String.valueOf(userId))) {
                    log.warn("用户限流拒绝访问: {}, 路径: {}", userId, requestPath);
                    response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    return response.setComplete();
                }
                
                // 检查API限流
                if (!rateLimitService.isAllowed("API", requestPath)) {
                    log.warn("API限流拒绝访问: {}, IP: {}", requestPath, clientIp);
                    response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    return response.setComplete();
                }
                
                // 检查全局限流
                if (!rateLimitService.isAllowed("GLOBAL", "global")) {
                    log.warn("全局限流拒绝访问: {}, IP: {}", requestPath, clientIp);
                    response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    return response.setComplete();
                }
                
                // 自定义规则限流
                if (config.getRuleName() != null && !config.getRuleName().isEmpty()) {
                    if (!rateLimitService.isAllowed("CUSTOM", clientIp, config.getRuleName())) {
                        log.warn("自定义限流拒绝访问: {}, 规则: {}", clientIp, config.getRuleName());
                        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        return response.setComplete();
                    }
                }
                
                // 添加限流信息到请求头
                ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-RateLimit-IP-Remaining", String.valueOf(rateLimitService.getRemainingTime("IP", clientIp, "default")))
                    .header("X-RateLimit-IP-Reset", String.valueOf(rateLimitService.getCurrentCount("IP", clientIp, "default")))
                    .build();
                
                ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();
                
                return chain.filter(mutatedExchange);
            }
        };
    }
    
    /**
     * 获取客户端IP
     * 
     * @param request 请求
     * @return 客户端IP
     */
    private String getClientIp(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress() : "";
        }
        // 处理多级代理的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        return ip;
    }
    
    /**
     * 获取用户ID
     * 
     * @param request 请求
     * @return 用户ID
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
    
    @Override
    public int getOrder() {
        return -100; // 优先级较高
    }
    
    /**
     * 配置类
     */
    public static class Config {
        private String ruleName;
        private int rate;
        private int window;
        
        public String getRuleName() {
            return ruleName;
        }
        
        public void setRuleName(String ruleName) {
            this.ruleName = ruleName;
        }
        
        public int getRate() {
            return rate;
        }
        
        public void setRate(int rate) {
            this.rate = rate;
        }
        
        public int getWindow() {
            return window;
        }
        
        public void setWindow(int window) {
            this.window = window;
        }
    }
}