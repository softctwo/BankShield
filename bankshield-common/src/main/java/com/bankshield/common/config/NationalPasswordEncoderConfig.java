package com.bankshield.common.config;

import com.bankshield.common.crypto.NationalCryptoManager;
import com.bankshield.common.crypto.SM3PasswordEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 国密密码编码器配置
 * 当启用国密算法时，使用SM3PasswordEncoder替代BCryptPasswordEncoder
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "bankshield.crypto.national.password-encoder-type", havingValue = "SM3")
public class NationalPasswordEncoderConfig {
    
    @Autowired
    private NationalCryptoManager cryptoManager;
    
    /**
     * 配置SM3密码编码器
     */
    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        log.info("配置SM3密码编码器");
        return new SM3PasswordEncoder();
    }
}