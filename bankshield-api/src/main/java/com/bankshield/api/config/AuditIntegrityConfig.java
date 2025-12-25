package com.bankshield.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 审计日志完整性配置
 * 
 * @author BankShield
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "audit.block")
public class AuditIntegrityConfig {
    
    /**
     * 是否启用审计日志区块功能
     */
    private boolean enabled = true;
    
    /**
     * 每个区块包含的审计日志数量
     */
    private int size = 1000;
    
    /**
     * 区块生成检查间隔（分钟）
     */
    private int intervalMinutes = 5;
    
    /**
     * 区块链存证配置
     */
    private BlockchainAnchor blockchainAnchor = new BlockchainAnchor();
    
    @Data
    public static class BlockchainAnchor {
        /**
         * 是否启用区块链存证
         */
        private boolean enabled = false;
        
        /**
         * 区块链上链间隔（分钟）
         */
        private int anchorInterval = 60;
        
        /**
         * 区块链网络配置
         */
        private String network = "fabric";
        
        /**
         * 智能合约名称
         */
        private String contractName = "AuditAnchorContract";
    }
}