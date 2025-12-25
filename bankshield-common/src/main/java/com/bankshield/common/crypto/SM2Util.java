package com.bankshield.common.crypto;

import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Properties;

/**
 * SM2工具类 - 国密非对称加密算法
 * 用于数字签名和密钥交换
 */
@Slf4j
public class SM2Util {

    private static KeyPair keyPair;
    private static Properties properties = new Properties();
    private static final String KEYSTORE_FILE = "sm2_keystore.properties";
    private static final String PUBLIC_KEY_PROP = "sm2.public.key";
    private static final String PRIVATE_KEY_PROP = "sm2.private.key";

    private static Environment environment;

    public static void setEnvironment(Environment env) {
        environment = env;
    }

    /**
     * 初始化密钥对（从文件或配置加载，如果不存在则生成新密钥对）
     */
    @PostConstruct
    public static void initKeyPair() {
        try {
            String publicKey = environment.getProperty(PUBLIC_KEY_PROP);
            String privateKey = environment.getProperty(PRIVATE_KEY_PROP);

            // 如果配置中存在密钥，则从配置加载
            if (publicKey != null && privateKey != null && !publicKey.trim().isEmpty() && !privateKey.trim().isEmpty()) {
                log.info("从环境配置加载SM2密钥对");
                PublicKey pubKey = stringToPublicKey(publicKey);
                PrivateKey privKey = stringToPrivateKey(privateKey);
                keyPair = new KeyPair(pubKey, privKey);
                return;
            }

            // 尝试从文件加载
            File keystoreFile = new File(KEYSTORE_FILE);
            if (keystoreFile.exists()) {
                try (InputStream input = new FileInputStream(keystoreFile)) {
                    properties.load(input);
                    String filePublicKey = properties.getProperty(PUBLIC_KEY_PROP);
                    String filePrivateKey = properties.getProperty(PRIVATE_KEY_PROP);

                    if (filePublicKey != null && filePrivateKey != null) {
                        log.info("从文件加载SM2密钥对: {}", KEYSTORE_FILE);
                        PublicKey pubKey = stringToPublicKey(filePublicKey);
                        PrivateKey privKey = stringToPrivateKey(filePrivateKey);
                        keyPair = new KeyPair(pubKey, privKey);
                        return;
                    }
                }
            }

            // 如果文件不存在或没有密钥，则生成新的密钥对
            log.info("生成新的SM2密钥对");
            keyPair = generateKeyPair();
            saveKeyPair();
            log.info("SM2密钥对已保存到文件: {}", KEYSTORE_FILE);

        } catch (Exception e) {
            log.error("初始化SM2密钥对失败: {}", e.getMessage(), e);
            throw new RuntimeException("初始化SM2密钥对失败", e);
        }
    }

    /**
     * 保存密钥对到文件
     */
    private static void saveKeyPair() {
        try (OutputStream output = new FileOutputStream(KEYSTORE_FILE)) {
            properties.setProperty(PUBLIC_KEY_PROP, publicKeyToString(keyPair.getPublic()));
            properties.setProperty(PRIVATE_KEY_PROP, privateKeyToString(keyPair.getPrivate()));
            properties.store(output, "SM2 Key Pair - Generated on " + new java.util.Date());
        } catch (Exception e) {
            log.error("保存SM2密钥对失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 程序关闭前清理资源
     */
    @PreDestroy
    public static void cleanup() {
        log.info("清理SM2密钥资源");
        keyPair = null;
    }

    /**
     * 生成SM2密钥对
     */
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
            keyPairGenerator.initialize(256);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            log.error("生成SM2密钥对失败: {}", e.getMessage());
            throw new RuntimeException("生成SM2密钥对失败", e);
        }
    }

    /**
     * 获取公钥字符串
     */
    public static String getPublicKeyString() {
        if (keyPair == null) {
            throw new RuntimeException("SM2密钥对未初始化");
        }
        return publicKeyToString(keyPair.getPublic());
    }

    /**
     * 获取私钥字符串
     */
    public static String getPrivateKeyString() {
        if (keyPair == null) {
            throw new RuntimeException("SM2密钥对未初始化");
        }
        return privateKeyToString(keyPair.getPrivate());
    }

    /**
     * SM2公钥加密
     */
    public static String encrypt(String publicKey, String plainText) {
        try {
            SM2 sm2 = SmUtil.sm2(null, Base64.getDecoder().decode(publicKey));
            return sm2.encryptBase64(plainText, KeyType.PublicKey);
        } catch (Exception e) {
            log.error("SM2公钥加密失败: {}", e.getMessage());
            throw new RuntimeException("SM2公钥加密失败", e);
        }
    }

    /**
     * SM2私钥解密
     */
    public static String decrypt(String privateKey, String cipherText) {
        try {
            SM2 sm2 = SmUtil.sm2(Base64.getDecoder().decode(privateKey), null);
            return sm2.decryptStr(cipherText, KeyType.PrivateKey);
        } catch (Exception e) {
            log.error("SM2私钥解密失败: {}", e.getMessage());
            throw new RuntimeException("SM2私钥解密失败", e);
        }
    }

    /**
     * SM2私钥签名
     */
    public static String sign(String privateKey, String data) {
        try {
            SM2 sm2 = SmUtil.sm2(Base64.getDecoder().decode(privateKey), null);
            return Base64.getEncoder().encodeToString(sm2.sign(data.getBytes()));
        } catch (Exception e) {
            log.error("SM2私钥签名失败: {}", e.getMessage());
            throw new RuntimeException("SM2私钥签名失败", e);
        }
    }

    /**
     * SM2公钥验签
     */
    public static boolean verify(String publicKey, String data, String signature) {
        try {
            SM2 sm2 = SmUtil.sm2(null, Base64.getDecoder().decode(publicKey));
            return sm2.verify(data.getBytes(), Base64.getDecoder().decode(signature));
        } catch (Exception e) {
            log.error("SM2公钥验签失败: {}", e.getMessage());
            throw new RuntimeException("SM2公钥验签失败", e);
        }
    }

    /**
     * 将公钥转换为字符串
     */
    public static String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * 将私钥转换为字符串
     */
    public static String privateKeyToString(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    /**
     * 从字符串解析公钥
     */
    public static PublicKey stringToPublicKey(String publicKeyStr) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            log.error("解析公钥失败: {}", e.getMessage());
            throw new RuntimeException("解析公钥失败", e);
        }
    }

    /**
     * 从字符串解析私钥
     */
    public static PrivateKey stringToPrivateKey(String privateKeyStr) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            log.error("解析私钥失败: {}", e.getMessage());
            throw new RuntimeException("解析私钥失败", e);
        }
    }
}