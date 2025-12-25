package com.bankshield.common.crypto;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 国密算法配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "bankshield.crypto.national")
public class NationalCryptoConfig {
    
    /**
     * 是否启用国密算法
     */
    private boolean enabled = true;
    
    /**
     * SM4默认加密模式
     */
    private String sm4DefaultMode = "CBC";
    
    /**
     * SM4默认密钥
     */
    private String sm4DefaultKey;
    
    /**
     * SM4默认IV
     */
    private String sm4DefaultIv;
    
    /**
     * SM2默认密钥对路径
     */
    private String sm2KeyPairPath;
    
    /**
     * 是否强制使用国密算法（禁用国际算法）
     */
    private boolean forceNationalCrypto = true;
    
    /**
     * 密码编码器类型（SM3或BCrypt）
     */
    private String passwordEncoderType = "SM3";
    
    /**
     * SSL协议配置
     */
    private SslConfig ssl = new SslConfig();
    
    @Data
    public static class SslConfig {
        /**
         * SSL协议版本
         */
        private String protocol = "TLCP";
        
        /**
         * 国密SSL密钥库路径
         */
        private String keyStorePath;
        
        /**
         * 国密SSL密钥库密码
         */
        private String keyStorePassword;
        
        /**
         * 国密SSL密钥库类型
         */
        private String keyStoreType = "PKCS12";
        
        /**
         * 国密SSL密钥别名
         */
        private String keyAlias;
        
        /**
         * 国密SSL加密套件
         */
        private String[] ciphers = {
            "ECC-SM2-WITH-SM4-SM3",
            "ECDHE-SM2-WITH-SM4-SM3"
        };
        
        /**
         * 是否启用国密SSL
         */
        private boolean enabled = true;
    }
}