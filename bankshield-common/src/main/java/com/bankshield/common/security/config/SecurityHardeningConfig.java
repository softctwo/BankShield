package com.bankshield.common.security.config;

import com.bankshield.common.security.filter.SecureHeadersFilter;
import com.bankshield.common.security.filter.WafFilter;
import com.bankshield.common.security.ratelimit.AdvancedRateLimiter;
import com.bankshield.common.security.signature.ApiSignatureVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 安全加固配置
 * 配置各种安全过滤器、认证机制和安全策略
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
public class SecurityHardeningConfig extends WebSecurityConfigurerAdapter {
    
    @Autowired
    private WafFilter wafFilter;
    
    @Autowired
    private SecureHeadersFilter secureHeadersFilter;
    
    @Autowired
    private AdvancedRateLimiter advancedRateLimiter;
    
    @Autowired
    private ApiSignatureVerifier apiSignatureVerifier;
    
    /**
     * 配置HTTP安全策略
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（API服务通常使用Token认证）
            .csrf().disable()
            
            // 配置请求授权
            .authorizeRequests()
                // 静态资源允许匿名访问
                .antMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                // 健康检查接口允许匿名访问
                .antMatchers("/actuator/health", "/health", "/ping").permitAll()
                // 登录相关接口允许匿名访问
                .antMatchers("/api/auth/login", "/api/auth/logout").permitAll()
                // 文档接口允许匿名访问（生产环境应该限制）
                .antMatchers("/swagger-ui/**", "/v2/api-docs", "/swagger-resources/**").permitAll()
                // 其他接口需要认证
                .anyRequest().authenticated()
                .and()
            
            // 配置会话管理
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 无状态会话
                .and()
            
            // 配置异常处理
            .exceptionHandling()
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"未认证或认证已过期\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":\"ACCESS_DENIED\",\"message\":\"权限不足\"}");
                })
                .and()
            
            // 配置安全响应头
            .headers()
                .contentTypeOptions() // X-Content-Type-Options: nosniff
                .and()
                .xssProtection() // X-XSS-Protection: 1; mode=block
                .and()
                .frameOptions() // X-Frame-Options: DENY
                .and()
                .httpStrictTransportSecurity() // Strict-Transport-Security
                .and()
                .contentSecurityPolicy("default-src 'self'") // Content-Security-Policy
                .and()
                .and()

            // 添加自定义过滤器
            .addFilterBefore(wafFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(secureHeadersFilter, WafFilter.class);
    }
    
    /**
     * WAF过滤器配置
     */
    @Bean
    @Order(1)
    public FilterRegistrationBean<WafFilter> wafFilterRegistration() {
        FilterRegistrationBean<WafFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(wafFilter);
        registration.addUrlPatterns("/*");
        registration.setName("wafFilter");
        registration.setOrder(1);
        return registration;
    }
    
    /**
     * 安全响应头过滤器配置
     */
    @Bean
    @Order(2)
    public FilterRegistrationBean<SecureHeadersFilter> secureHeadersFilterRegistration() {
        FilterRegistrationBean<SecureHeadersFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(secureHeadersFilter);
        registration.addUrlPatterns("/*");
        registration.setName("secureHeadersFilter");
        registration.setOrder(2);
        return registration;
    }
    
    /**
     * API限流过滤器配置
     */
    @Bean
    @Order(3)
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilterRegistration() {
        FilterRegistrationBean<RateLimitingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(rateLimitingFilter());
        registration.addUrlPatterns("/api/*");
        registration.setName("rateLimitingFilter");
        registration.setOrder(3);
        return registration;
    }
    
    /**
     * API签名验证过滤器配置
     */
    @Bean
    @Order(4)
    public FilterRegistrationBean<SignatureVerificationFilter> signatureVerificationFilterRegistration() {
        FilterRegistrationBean<SignatureVerificationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(signatureVerificationFilter());
        registration.addUrlPatterns("/api/*");
        registration.setName("signatureVerificationFilter");
        registration.setOrder(4);
        return registration;
    }
    
    /**
     * 创建限流过滤器
     */
    @Bean
    public RateLimitingFilter rateLimitingFilter() {
        return new RateLimitingFilter(advancedRateLimiter);
    }
    
    /**
     * 创建签名验证过滤器
     */
    @Bean
    public SignatureVerificationFilter signatureVerificationFilter() {
        return new SignatureVerificationFilter(apiSignatureVerifier);
    }
    
    /**
     * 限流过滤器
     */
    public static class RateLimitingFilter implements Filter {
        private final AdvancedRateLimiter rateLimiter;
        
        public RateLimitingFilter(AdvancedRateLimiter rateLimiter) {
            this.rateLimiter = rateLimiter;
        }
        
        @Override
        public void doFilter(javax.servlet.ServletRequest request, 
                           javax.servlet.ServletResponse response, 
                           javax.servlet.FilterChain chain) 
                throws java.io.IOException, javax.servlet.ServletException {
            
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            
            String path = httpRequest.getRequestURI();
            String userId = getUserId(httpRequest);
            
            // 检查限流
            if (!rateLimiter.tryAcquire(path, userId)) {
                httpResponse.setStatus(429); // Too Many Requests
                httpResponse.setContentType("application/json;charset=UTF-8");
                httpResponse.getWriter().write("{\"code\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"请求过于频繁，请稍后再试\"}");
                return;
            }
            
            chain.doFilter(request, response);
        }
        
        private String getUserId(HttpServletRequest request) {
            // 从请求中获取用户ID
            String userId = request.getHeader("X-User-Id");
            if (userId == null) {
                userId = request.getParameter("userId");
            }
            if (userId == null) {
                userId = "anonymous";
            }
            return userId;
        }
    }
    
    /**
     * 签名验证过滤器
     */
    public static class SignatureVerificationFilter implements Filter {
        private final ApiSignatureVerifier signatureVerifier;
        
        public SignatureVerificationFilter(ApiSignatureVerifier signatureVerifier) {
            this.signatureVerifier = signatureVerifier;
        }
        
        @Override
        public void doFilter(javax.servlet.ServletRequest request, 
                           javax.servlet.ServletResponse response, 
                           javax.servlet.FilterChain chain) 
                throws java.io.IOException, javax.servlet.ServletException {
            
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            
            // 只对特定路径进行签名验证
            String path = httpRequest.getRequestURI();
            if (shouldVerifySignature(path)) {
                com.bankshield.common.result.Result<Void> result = signatureVerifier.verifySignature(httpRequest);
                if (!result.isSuccess()) {
                    httpResponse.setStatus(401);
                    httpResponse.setContentType("application/json;charset=UTF-8");
                    httpResponse.getWriter().write(String.format(
                        "{\"code\":\"SIGNATURE_VERIFICATION_FAILED\",\"message\":\"%s\"}",
                        result.getMessage()
                    ));
                    return;
                }
            }
            
            chain.doFilter(request, response);
        }
        
        private boolean shouldVerifySignature(String path) {
            // 只对敏感接口进行签名验证
            return path.contains("/api/key/") ||
                   path.contains("/api/encrypt/") ||
                   path.contains("/api/decrypt/") ||
                   path.contains("/api/audit/export") ||
                   path.contains("/api/report/");
        }
    }
}