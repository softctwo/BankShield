package com.bankshield.api.config;

import com.bankshield.api.interceptor.AuditInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 审计配置类
 * 注册审计拦截器和启用异步处理
 * 
 * @author BankShield
 */
@Configuration
@EnableAsync
public class AuditConfig implements WebMvcConfigurer {

    @Autowired
    private AuditInterceptor auditInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册审计拦截器，拦截所有/api/开头的请求
        registry.addInterceptor(auditInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/audit/**") // 排除审计相关的请求
                .excludePathPatterns("/api/auth/**")  // 排除认证相关的请求
                .excludePathPatterns("/error");
    }
}