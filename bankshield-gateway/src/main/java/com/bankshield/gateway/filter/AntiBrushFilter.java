package com.bankshield.gateway.filter;

import com.bankshield.gateway.service.BlacklistService;
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 防刷过滤器
 * 
 * @author BankShield
 */
@Slf4j
@Component
public class AntiBrushFilter extends AbstractGatewayFilterFactory<AntiBrushFilter.Config> implements Ordered {
    
    @Autowired
    private BlacklistService blacklistService;
    
    // 请求计数器（用于检测异常请求模式）
    private final Map<String, RequestCounter> requestCounters = new ConcurrentHashMap<>();
    
    // 异常IP检测器
    private final Map<String, SuspiciousDetector> suspiciousDetectors = new ConcurrentHashMap<>();
    
    public AntiBrushFilter() {
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
                
                // 检查IP是否在黑名单中
                if (blacklistService.isBlacklisted(clientIp)) {
                    log.warn("防刷过滤器检测到黑名单IP: {}", clientIp);
                    response.setStatusCode(HttpStatus.FORBIDDEN);
                    return response.setComplete();
                }
                
                // 获取请求路径
                String requestPath = request.getURI().getPath();
                
                // 检测异常请求模式
                if (isSuspiciousRequest(clientIp, requestPath, config)) {
                    log.warn("检测到异常请求模式，IP: {}, 路径: {}", clientIp, requestPath);
                    
                    // 自动加入黑名单
                    blacklistService.addToBlacklist(clientIp, "异常请求模式检测", config.getBlockDuration(), "SYSTEM");
                    
                    response.setStatusCode(HttpStatus.FORBIDDEN);
                    return response.setComplete();
                }
                
                // 记录请求信息
                recordRequest(clientIp, requestPath);
                
                return chain.filter(exchange);
            }
        };
    }
    
    /**
     * 检测是否为可疑请求
     * 
     * @param clientIp 客户端IP
     * @param requestPath 请求路径
     * @param config 配置
     * @return true: 可疑请求, false: 正常请求
     */
    private boolean isSuspiciousRequest(String clientIp, String requestPath, Config config) {
        // 获取可疑IP检测器
        SuspiciousDetector detector = suspiciousDetectors.computeIfAbsent(clientIp, k -> new SuspiciousDetector());
        
        // 检查请求频率
        if (detector.isHighFrequency(config.getMaxRequestsPerSecond())) {
            log.warn("检测到高频请求，IP: {}, 频率: {}/秒", clientIp, detector.getRequestsPerSecond());
            return true;
        }
        
        // 检查请求路径变化频率
        if (detector.isPathChangingRapidly(requestPath, config.getMaxPathChangesPerSecond())) {
            log.warn("检测到路径快速变化，IP: {}, 路径变化频率: {}/秒", clientIp, detector.getPathChangesPerSecond());
            return true;
        }
        
        // 检查错误率
        if (detector.isHighErrorRate(config.getMaxErrorRate())) {
            log.warn("检测到高错误率，IP: {}, 错误率: {}%", clientIp, detector.getErrorRate() * 100);
            return true;
        }
        
        // 检查连续错误
        if (detector.hasConsecutiveErrors(config.getMaxConsecutiveErrors())) {
            log.warn("检测到连续错误，IP: {}, 连续错误数: {}", clientIp, detector.getConsecutiveErrors());
            return true;
        }
        
        return false;
    }
    
    /**
     * 记录请求信息
     * 
     * @param clientIp 客户端IP
     * @param requestPath 请求路径
     */
    private void recordRequest(String clientIp, String requestPath) {
        RequestCounter counter = requestCounters.computeIfAbsent(clientIp, k -> new RequestCounter());
        counter.recordRequest(requestPath);
        
        // 清理过期的计数器
        cleanupExpiredCounters();
    }
    
    /**
     * 清理过期的计数器
     */
    private void cleanupExpiredCounters() {
        long currentTime = System.currentTimeMillis();
        requestCounters.entrySet().removeIf(entry -> {
            RequestCounter counter = entry.getValue();
            return currentTime - counter.getLastRequestTime() > 300000; // 5分钟无请求则清理
        });
        
        suspiciousDetectors.entrySet().removeIf(entry -> {
            SuspiciousDetector detector = entry.getValue();
            return currentTime - detector.getLastRequestTime() > 300000; // 5分钟无请求则清理
        });
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
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        return ip;
    }
    
    @Override
    public int getOrder() {
        return -90; // 优先级较高，在限流过滤器之后
    }
    
    /**
     * 请求计数器
     */
    private static class RequestCounter {
        private final AtomicInteger requestCount = new AtomicInteger(0);
        private volatile long lastRequestTime = System.currentTimeMillis();
        private volatile long windowStartTime = System.currentTimeMillis();
        
        public void recordRequest(String path) {
            requestCount.incrementAndGet();
            lastRequestTime = System.currentTimeMillis();
        }
        
        public int getRequestCount() {
            return requestCount.get();
        }
        
        public long getLastRequestTime() {
            return lastRequestTime;
        }
        
        public double getRequestsPerSecond() {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - windowStartTime;
            if (elapsedTime > 0) {
                return (double) requestCount.get() / (elapsedTime / 1000.0);
            }
            return 0;
        }
        
        public void reset() {
            requestCount.set(0);
            windowStartTime = System.currentTimeMillis();
        }
    }
    
    /**
     * 可疑IP检测器
     */
    private static class SuspiciousDetector {
        private final AtomicInteger requestCount = new AtomicInteger(0);
        private final AtomicInteger errorCount = new AtomicInteger(0);
        private final AtomicInteger consecutiveErrors = new AtomicInteger(0);
        private volatile String lastPath = "";
        private volatile int pathChangeCount = 0;
        private volatile long lastRequestTime = System.currentTimeMillis();
        private volatile long windowStartTime = System.currentTimeMillis();
        private volatile long pathWindowStartTime = System.currentTimeMillis();
        
        public void recordRequest(String path, boolean isError) {
            requestCount.incrementAndGet();
            lastRequestTime = System.currentTimeMillis();
            
            // 记录路径变化
            if (!path.equals(lastPath)) {
                pathChangeCount++;
                lastPath = path;
                
                // 重置路径变化时间窗口
                long currentTime = System.currentTimeMillis();
                if (currentTime - pathWindowStartTime > 1000) {
                    pathChangeCount = 1;
                    pathWindowStartTime = currentTime;
                }
            }
            
            // 记录错误
            if (isError) {
                errorCount.incrementAndGet();
                consecutiveErrors.incrementAndGet();
            } else {
                consecutiveErrors.set(0);
            }
        }
        
        public boolean isHighFrequency(int maxRequestsPerSecond) {
            double requestsPerSecond = getRequestsPerSecond();
            return requestsPerSecond > maxRequestsPerSecond;
        }
        
        public boolean isPathChangingRapidly(int maxPathChangesPerSecond) {
            double pathChangesPerSecond = getPathChangesPerSecond();
            return pathChangesPerSecond > maxPathChangesPerSecond;
        }
        
        public boolean isHighErrorRate(double maxErrorRate) {
            double errorRate = getErrorRate();
            return errorRate > maxErrorRate;
        }
        
        public boolean hasConsecutiveErrors(int maxConsecutiveErrors) {
            return consecutiveErrors.get() >= maxConsecutiveErrors;
        }
        
        public double getRequestsPerSecond() {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - windowStartTime;
            if (elapsedTime > 0) {
                return (double) requestCount.get() / (elapsedTime / 1000.0);
            }
            return 0;
        }
        
        public double getPathChangesPerSecond() {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - pathWindowStartTime;
            if (elapsedTime > 0) {
                return (double) pathChangeCount / (elapsedTime / 1000.0);
            }
            return 0;
        }
        
        public double getErrorRate() {
            int totalRequests = requestCount.get();
            if (totalRequests > 0) {
                return (double) errorCount.get() / totalRequests;
            }
            return 0;
        }
        
        public int getConsecutiveErrors() {
            return consecutiveErrors.get();
        }
        
        public long getLastRequestTime() {
            return lastRequestTime;
        }
    }
    
    /**
     * 配置类
     */
    public static class Config {
        private int maxRequestsPerSecond = 50; // 每秒最大请求数
        private int maxPathChangesPerSecond = 10; // 每秒最大路径变化数
        private double maxErrorRate = 0.5; // 最大错误率（50%）
        private int maxConsecutiveErrors = 10; // 最大连续错误数
        private long blockDuration = 3600; // 封禁时长（秒，默认1小时）
        
        // Getters and Setters
        public int getMaxRequestsPerSecond() {
            return maxRequestsPerSecond;
        }
        
        public void setMaxRequestsPerSecond(int maxRequestsPerSecond) {
            this.maxRequestsPerSecond = maxRequestsPerSecond;
        }
        
        public int getMaxPathChangesPerSecond() {
            return maxPathChangesPerSecond;
        }
        
        public void setMaxPathChangesPerSecond(int maxPathChangesPerSecond) {
            this.maxPathChangesPerSecond = maxPathChangesPerSecond;
        }
        
        public double getMaxErrorRate() {
            return maxErrorRate;
        }
        
        public void setMaxErrorRate(double maxErrorRate) {
            this.maxErrorRate = maxErrorRate;
        }
        
        public int getMaxConsecutiveErrors() {
            return maxConsecutiveErrors;
        }
        
        public void setMaxConsecutiveErrors(int maxConsecutiveErrors) {
            this.maxConsecutiveErrors = maxConsecutiveErrors;
        }
        
        public long getBlockDuration() {
            return blockDuration;
        }
        
        public void setBlockDuration(long blockDuration) {
            this.blockDuration = blockDuration;
        }
    }
}