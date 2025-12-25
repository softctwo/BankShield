package com.bankshield.monitor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;

/**
 * Spring Security配置 - 监控模块
 *
 * @author BankShield Team
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().disable()
            .authorizeRequests()
                // 健康检查接口允许公开访问（用于监控系统检测）
                .antMatchers("/api/monitoring/health", "/api/monitoring/metrics").permitAll()
                // 告警管理接口需要认证
                .antMatchers("/api/monitoring/alerts/**").authenticated()
                // 其他所有请求需要认证
                .anyRequest().authenticated()
            .and()
            .httpBasic().disable()
            .formLogin().disable()
            // 添加JWT认证过滤器
            .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            // 认证失败处理
            .exceptionHandling()
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"请提供有效的认证信息\"}");
                })
            .and()
            // 禁用缓存
            .headers().cacheControl();

        return http.build();
    }
}
