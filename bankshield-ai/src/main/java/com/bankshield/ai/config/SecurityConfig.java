package com.bankshield.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;

/**
 * Spring Security配置 - AI模块
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
                // 所有AI接口都需要认证
                .antMatchers("/api/ai/**").authenticated()
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
