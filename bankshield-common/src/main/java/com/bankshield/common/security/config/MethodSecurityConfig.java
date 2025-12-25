package com.bankshield.common.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

/**
 * 方法级安全配置
 * 启用Spring Security的方法级安全注解支持
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Configuration
@EnableGlobalMethodSecurity(
    prePostEnabled = true,  // 启用@PreAuthorize和@PostAuthorize注解
    securedEnabled = true,  // 启用@Secured注解
    jsr250Enabled = true    // 启用@RolesAllowed等JSR-250注解
)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
    
    /**
     * 可以在这里配置自定义的PermissionEvaluator、MethodSecurityExpressionHandler等
     * 目前使用Spring Security的默认配置即可满足需求
     */
}