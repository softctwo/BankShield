package com.bankshield.common.config;

import com.bankshield.common.crypto.NationalCryptoConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;

/**
 * 国密SSL配置类
 */
@Slf4j
@Configuration
@Profile("national-crypto")
public class NationalSslConfig {
    
    @Autowired
    private NationalCryptoConfig cryptoConfig;
    
    /**
     * 国密SSL服务器配置
     */
    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> nationalSslCustomizer() {
        return factory -> {
            // TODO: 实现国密SSL配置
            log.info("国密SSL配置已预留");
        };
    }
}