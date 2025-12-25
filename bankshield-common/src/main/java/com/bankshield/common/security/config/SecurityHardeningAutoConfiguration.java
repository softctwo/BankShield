package com.bankshield.common.security.config;

import com.bankshield.common.security.audit.SecurityEventLogger;
import com.bankshield.common.security.compliance.SecurityBaselineChecker;
import com.bankshield.common.security.filter.SecureHeadersFilter;
import com.bankshield.common.security.filter.WafFilter;
import com.bankshield.common.security.incident.SecurityIncidentResponder;
import com.bankshield.common.security.ratelimit.AdvancedRateLimiter;
import com.bankshield.common.security.signature.ApiSignatureVerifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 安全加固自动配置类
 * 自动配置所有安全加固组件
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Configuration
@EnableAsync
@AutoConfigureAfter(RedisAutoConfiguration.class)
@ConditionalOnProperty(prefix = "bankshield.security", name = "enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "com.bankshield.common.security")
@PropertySource(value = "classpath:security-hardening.yml", factory = YamlPropertySourceFactory.class)
public class SecurityHardeningAutoConfiguration {

    /**
     * WAF过滤器
     */
    @Bean
    @ConditionalOnProperty(prefix = "security.waf", name = "enabled", havingValue = "true", matchIfMissing = true)
    public WafFilter wafFilter() {
        return new WafFilter();
    }

    /**
     * 安全响应头过滤器
     */
    @Bean
    @ConditionalOnProperty(prefix = "security.headers", name = "enabled", havingValue = "true", matchIfMissing = true)
    public SecureHeadersFilter secureHeadersFilter() {
        return new SecureHeadersFilter();
    }

    /**
     * 高级限流器
     */
    @Bean
    @ConditionalOnProperty(prefix = "security.rate-limit", name = "enabled", havingValue = "true", matchIfMissing = true)
    public AdvancedRateLimiter advancedRateLimiter() {
        return new AdvancedRateLimiter();
    }

    /**
     * API签名验证器
     */
    @Bean
    @ConditionalOnProperty(prefix = "security.signature", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ApiSignatureVerifier apiSignatureVerifier() {
        return new ApiSignatureVerifier();
    }

    /**
     * 安全事件日志记录器
     */
    @Bean
    @ConditionalOnProperty(prefix = "security.audit", name = "enabled", havingValue = "true", matchIfMissing = true)
    public SecurityEventLogger securityEventLogger() {
        return new SecurityEventLogger();
    }

    /**
     * 安全基线检查器
     */
    @Bean
    @ConditionalOnProperty(prefix = "security.compliance", name = "enabled", havingValue = "true", matchIfMissing = true)
    public SecurityBaselineChecker securityBaselineChecker() {
        return new SecurityBaselineChecker();
    }

    /**
     * 安全事件响应器
     */
    @Bean
    @ConditionalOnProperty(prefix = "security.incident", name = "enabled", havingValue = "true", matchIfMissing = true)
    public SecurityIncidentResponder securityIncidentResponder() {
        return new SecurityIncidentResponder();
    }

    /**
     * 安全加固配置
     */
    @Bean
    public SecurityHardeningConfig securityHardeningConfig() {
        return new SecurityHardeningConfig();
    }

    /**
     * 方法级安全配置
     */
    @Bean
    public MethodSecurityConfig methodSecurityConfig() {
        return new MethodSecurityConfig();
    }
}