package com.bankshield.common.crypto;

import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import lombok.extern.slf4j.Slf4j;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * SM2工具类 - 国密非对称加密算法
 * 用于数字签名和密钥交换
 */
@Slf4j
public class SM2Util {
    
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