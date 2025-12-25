package com.bankshield.ai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 *
 * @author BankShield Team
 * @version 1.0.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置跨域访问
     * 注意：出于安全考虑，生产环境中应只允许特定的信任域名
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/ai/**")
                // 生产环境应替换为具体的信任域名，如：https://bankshield.internal, https://admin.bankshield.com
                .allowedOriginPatterns("https://*.bankshield.com", "https://*.bankshield.internal", "http://localhost:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "X-Requested-With")
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
