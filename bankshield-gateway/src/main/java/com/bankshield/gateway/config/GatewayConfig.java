package com.bankshield.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关配置类
 * 
 * @author BankShield
 */
@Data
@Component
@ConfigurationProperties(prefix = "bankshield.gateway")
public class GatewayConfig {
    
    /**
     * 过滤器配置
     */
    private FilterConfig filters = new FilterConfig();
    
    /**
     * 安全配置
     */
    private SecurityConfig security = new SecurityConfig();
    
    /**
     * 缓存配置
     */
    private CacheConfig cache = new CacheConfig();
    
    /**
     * 过滤器配置类
     */
    @Data
    public static class FilterConfig {
        private RateLimitConfig rateLimit = new RateLimitConfig();
        private AntiBrushConfig antiBrush = new AntiBrushConfig();
        private SignatureConfig signature = new SignatureConfig();
        private ApiAuditConfig apiAudit = new ApiAuditConfig();
    }
    
    /**
     * 限流过滤器配置
     */
    @Data
    public static class RateLimitConfig {
        private int order = -100;
        private boolean enabled = true;
        private int defaultRate = 100;
    }
    
    /**
     * 防刷过滤器配置
     */
    @Data
    public static class AntiBrushConfig {
        private int order = -90;
        private boolean enabled = true;
        private int maxRequestsPerSecond = 50;
        private int maxPathChangesPerSecond = 10;
        private double maxErrorRate = 0.5;
        private int maxConsecutiveErrors = 10;
        private long defaultBlockDuration = 3600;
    }
    
    /**
     * 签名验证过滤器配置
     */
    @Data
    public static class SignatureConfig {
        private int order = -80;
        private boolean enabled = true;
        private long expireTime = 300000; // 5分钟
        private List<String> algorithms = new ArrayList<>();
        
        public SignatureConfig() {
            algorithms.add("SM3");
        }
    }
    
    /**
     * API审计过滤器配置
     */
    @Data
    public static class ApiAuditConfig {
        private int order = 0;
        private boolean enabled = true;
        private boolean logAuthorization = false;
        private boolean logBody = false;
        private long maxBodySize = 10485760; // 10MB
        private long slowQueryThreshold = 1000; // 1秒
    }
    
    /**
     * 安全配置类
     */
    @Data
    public static class SecurityConfig {
        private List<String> whitelistIps = new ArrayList<>();
        private List<String> blacklistIps = new ArrayList<>();
        private List<String> authRequiredPaths = new ArrayList<>();
        private List<String> authExcludedPaths = new ArrayList<>();
        
        public SecurityConfig() {
            whitelistIps.add("127.0.0.1");
            whitelistIps.add("0:0:0:0:0:0:0:1"); // IPv6 localhost
            authRequiredPaths.add("/api/admin");
            authRequiredPaths.add("/api/user");
            authRequiredPaths.add("/api/security");
            authExcludedPaths.add("/api/auth/login");
            authExcludedPaths.add("/api/auth/register");
            authExcludedPaths.add("/api/public");
        }
    }
    
    /**
     * 缓存配置类
     */
    @Data
    public static class CacheConfig {
        private ExpireTimes expireTimes = new ExpireTimes();
        
        @Data
        public static class ExpireTimes {
            private long routeConfig = 3600; // 1小时
            private long rateLimitRules = 300; // 5分钟
            private long blacklist = 60; // 1分钟
            private long appSecrets = 86400; // 24小时
        }
    }
}