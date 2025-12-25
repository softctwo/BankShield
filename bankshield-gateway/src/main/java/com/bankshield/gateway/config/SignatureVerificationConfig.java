package com.bankshield.gateway.config;

import com.bankshield.gateway.filter.EnhancedSignatureVerificationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 签名验证过滤器配置
 * 
 * @author BankShield
 */
@Configuration
public class SignatureVerificationConfig {
    
    @Autowired
    private EnhancedSignatureVerificationFilter signatureVerificationFilter;
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("secure_route", r -> r
                .path("/api/**")
                .filters(f -> f
                    .filter(signatureVerificationFilter.apply(new EnhancedSignatureVerificationFilter.Config()))
                )
                .uri("lb://bankshield-api"))
            .build();
    }
}