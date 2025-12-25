package com.bankshield.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security配置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable() // 禁用CSRF保护
            .authorizeRequests()
                .antMatchers("/api/**").permitAll() // 允许所有/api路径访问
                .anyRequest().authenticated()
            .and()
            .httpBasic().disable() // 禁用HTTP Basic认证
            .formLogin().disable(); // 禁用表单登录

        return http.build();
    }
}