package com.bankshield.api.config;

import com.bankshield.api.service.SecureKeyManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.vault.authentication.AppRoleAuthentication;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.web.client.RestTemplate;

/**
 * Vault自动配置类
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "vault.enabled", havingValue = "true", matchIfMissing = true)
public class VaultAutoConfiguration {
    
    @Autowired
    private VaultConfig vaultConfig;
    
    @Bean
    public VaultEndpoint vaultEndpoint() {
        try {
            String host = vaultConfig.getAddress().replace("http://", "").replace("https://", "");
            String[] parts = host.split(":");
            String hostname = parts[0];
            int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 8200;
            
            VaultEndpoint endpoint = VaultEndpoint.create(hostname, port);
            log.info("Vault endpoint configured: {}:{}", hostname, port);
            return endpoint;
        } catch (Exception e) {
            log.error("Failed to configure Vault endpoint", e);
            throw new RuntimeException("Vault endpoint configuration failed", e);
        }
    }
    
    @Bean
    public AppRoleAuthentication vaultAuthentication(VaultEndpoint endpoint) {
        try {
            // 创建RestTemplate
            RestTemplate restTemplate = new RestTemplate();

            AppRoleAuthenticationOptions options = AppRoleAuthenticationOptions.builder()
                    .roleId(vaultConfig.getRoleId())
                    .secretId(vaultConfig.getSecretId())
                    .build();

            log.info("Vault AppRole authentication configured with roleId: {}",
                    vaultConfig.getRoleId() != null ? vaultConfig.getRoleId().substring(0, Math.min(8, vaultConfig.getRoleId().length())) + "..." : "null");

            // 注意：这是一个简化的实现，实际生产环境中需要更完善的配置
            return new AppRoleAuthentication(options, restTemplate);
        } catch (Exception e) {
            log.error("Failed to configure Vault authentication", e);
            throw new RuntimeException("Vault authentication configuration failed", e);
        }
    }

    @Bean
    public VaultOperations vaultOperations(VaultEndpoint endpoint) {
        try {
            // 简化配置，不使用 AppRoleAuthentication
            VaultTemplate vaultTemplate = new VaultTemplate(endpoint);
            log.info("Vault operations template configured successfully");
            return vaultTemplate;
        } catch (Exception e) {
            log.error("Failed to configure Vault operations", e);
            throw new RuntimeException("Vault operations configuration failed", e);
        }
    }
    
    @Bean
    @Primary
    public SecureKeyManagementService secureKeyManagementService() {
        return new SecureKeyManagementService();
    }
}