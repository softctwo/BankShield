package com.bankshield.api.interceptor;

import com.bankshield.api.config.RateLimitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * API限流拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitConfig rateLimitConfig;
    private final Map<String, Integer> defaultRateLimits;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestPath = request.getRequestURI();
        String clientId = getClientId(request);
        
        // 获取限流配置
        int permitsPerSecond = getPermitsForPath(requestPath);
        
        // 获取限流器
        RateLimitConfig.RateLimiter rateLimiter = rateLimitConfig.getRateLimiter(requestPath, permitsPerSecond);
        
        // 尝试获取令牌
        if (!rateLimiter.tryAcquire(clientId)) {
            log.warn("请求被限流: path={}, client={}", requestPath, clientId);
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}");
            return false;
        }
        
        // 设置限流响应头
        int remaining = rateLimiter.getAvailablePermits(clientId);
        response.setHeader("X-RateLimit-Limit", String.valueOf(permitsPerSecond));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 1000));
        
        return true;
    }

    /**
     * 获取客户端标识
     */
    private String getClientId(HttpServletRequest request) {
        // 优先使用用户ID
        String userId = request.getHeader("X-User-Id");
        if (userId != null && !userId.isEmpty()) {
            return "user:" + userId;
        }
        
        // 使用IP地址
        String ip = getClientIp(request);
        return "ip:" + ip;
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 获取路径的限流配置
     */
    private int getPermitsForPath(String path) {
        for (Map.Entry<String, Integer> entry : defaultRateLimits.entrySet()) {
            String pattern = entry.getKey();
            if (pattern.endsWith("/**")) {
                String prefix = pattern.substring(0, pattern.length() - 3);
                if (path.startsWith(prefix)) {
                    return entry.getValue();
                }
            } else if (pattern.equals(path)) {
                return entry.getValue();
            }
        }
        return defaultRateLimits.getOrDefault("default", 100);
    }
}
