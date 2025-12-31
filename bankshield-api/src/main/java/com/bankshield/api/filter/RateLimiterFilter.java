package com.bankshield.api.filter;

import com.google.common.util.concurrent.RateLimiter;
import com.bankshield.common.result.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 请求限流过滤器
 * 对不同接口实施不同的限流策略
 * 
 * @author BankShield
 */
@Slf4j
@Component
@Order(1)
public class RateLimiterFilter extends OncePerRequestFilter {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 不同接口的限流策略
    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();
    
    public RateLimiterFilter() {
        initializeLimiters();
    }
    
    /**
     * 初始化限流器
     */
    private void initializeLimiters() {
        // 用户管理接口：100次/秒 (修正路径匹配实际控制器)
        limiters.put("/api/user/page", RateLimiter.create(100.0));
        limiters.put("/api/dept/tree", RateLimiter.create(100.0));
        limiters.put("/api/dept/page", RateLimiter.create(100.0));
        limiters.put("/api/role/page", RateLimiter.create(100.0));
        limiters.put("/api/role/enabled", RateLimiter.create(100.0));
        limiters.put("/api/menu/list", RateLimiter.create(100.0));
        limiters.put("/api/menu/tree", RateLimiter.create(100.0));

        // 数据资产接口：50次/秒 (修正路径匹配实际控制器)
        limiters.put("/api/asset/list", RateLimiter.create(50.0));
        limiters.put("/api/asset/map/overview", RateLimiter.create(50.0));
        limiters.put("/api/data-source/page", RateLimiter.create(50.0));
        limiters.put("/api/data-source/all", RateLimiter.create(50.0));

        // 敏感操作接口：10次/秒
        limiters.put("/api/user", RateLimiter.create(10.0)); // POST/DELETE
        limiters.put("/api/role", RateLimiter.create(10.0));
        limiters.put("/api/dept", RateLimiter.create(10.0));
        limiters.put("/api/menu", RateLimiter.create(10.0));
        limiters.put("/api/data-source", RateLimiter.create(10.0));

        // 导出接口：5次/秒 (修正路径匹配实际控制器)
        limiters.put("/api/audit/export/operation", RateLimiter.create(5.0));
        limiters.put("/api/audit/export/login", RateLimiter.create(5.0));
        limiters.put("/api/asset/export", RateLimiter.create(5.0));

        // 审计日志查询：20次/秒 (修正路径匹配实际控制器)
        limiters.put("/api/audit/operation/page", RateLimiter.create(20.0));
        limiters.put("/api/audit/login/page", RateLimiter.create(20.0));

        // 监控与告警接口：30次/秒 (修正路径匹配实际控制器)
        limiters.put("/api/monitor/metrics/current", RateLimiter.create(30.0));
        limiters.put("/api/monitor/dashboard/stats", RateLimiter.create(30.0));
        limiters.put("/api/alert/record/page", RateLimiter.create(30.0));
        limiters.put("/api/alert/rule/page", RateLimiter.create(30.0));

        // 安全与血缘接口：20次/秒
        limiters.put("/api/security/stats", RateLimiter.create(20.0));
        limiters.put("/api/security/baseline/check", RateLimiter.create(10.0));
        limiters.put("/api/lineage/enhanced/discovery/task", RateLimiter.create(10.0));

        // 数据源扫描操作：5次/秒 (敏感操作)
        limiters.put("/api/data-source/scan", RateLimiter.create(5.0));
        limiters.put("/api/data-source/rescan", RateLimiter.create(5.0));
        limiters.put("/api/data-source/test-connection", RateLimiter.create(5.0));

        // 登录接口：严格限流防止暴力破解
        limiters.put("/api/user/login", RateLimiter.create(5.0));
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // 构建接口标识
        String apiPath = path;
        if ("POST".equals(method) || "PUT".equals(method)) {
            // 对于POST/PUT请求，可以基于路径参数或请求体内容进一步细化限流
            apiPath = path;
        }
        
        RateLimiter limiter = limiters.get(apiPath);
        
        if (limiter != null) {
            if (!limiter.tryAcquire()) {
                // 触发限流
                log.warn("请求限流触发: {} {}", method, path);
                handleRateLimitExceeded(response);
                return;
            }
        }
        
        // 记录请求处理时间
        long startTime = System.currentTimeMillis();
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            // 记录请求处理耗时
            long duration = System.currentTimeMillis() - startTime;
            if (duration > 1000) {
                log.warn("慢请求: {} {} 耗时: {}ms", method, path, duration);
            }
        }
    }
    
    /**
     * 处理限流响应
     */
    private void handleRateLimitExceeded(HttpServletResponse response) throws IOException {
        response.setStatus(429); // HTTP 429 Too Many Requests
        response.setContentType("application/json;charset=UTF-8");
        
        Result<Object> result = Result.error(429, "请求过于频繁，请稍后重试");
        String json = objectMapper.writeValueAsString(result);
        
        try (PrintWriter writer = response.getWriter()) {
            writer.write(json);
            writer.flush();
        }
    }
    
    /**
     * 动态更新限流配置
     */
    public void updateRateLimit(String path, double permitsPerSecond) {
        limiters.put(path, RateLimiter.create(permitsPerSecond));
        log.info("更新限流配置: {} -> {} permits/second", path, permitsPerSecond);
    }
    
    /**
     * 获取当前限流配置
     */
    public Map<String, Double> getRateLimitConfig() {
        Map<String, Double> config = new ConcurrentHashMap<>();
        limiters.forEach((path, limiter) -> {
            config.put(path, limiter.getRate());
        });
        return config;
    }
}