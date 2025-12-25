package com.bankshield.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Vault配置属性类
 */
@Configuration
@ConfigurationProperties(prefix = "vault")
@Getter
@Setter
public class VaultConfig {
    
    private String address;
    private String roleId;
    private String secretId;
    private String masterKeyPath;
    private boolean enabled = true;
    private int timeout = 30;
    private int retryAttempts = 3;
    private long retryInterval = 1000;
}