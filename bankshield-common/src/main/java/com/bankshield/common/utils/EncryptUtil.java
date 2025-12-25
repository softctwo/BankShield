package com.bankshield.common.utils;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.symmetric.SM4;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

/**
 * 加解密工具类
 * 支持国密算法（SM2、SM3、SM4）和国际标准算法（AES、RSA）
 */
@Slf4j
public class EncryptUtil {

    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_TAG_LENGTH = 16;
    private static final int GCM_IV_LENGTH = 12;

    private static final String RSA_ALGORITHM = "RSA/ECB/PKCS1Padding";
    private static final int RSA_KEY_SIZE = 2048;

    // BCryptPasswordEncoder实例，单例使用避免重复创建开销
    private static final BCryptPasswordEncoder BCRYPT_ENCODER = new BCryptPasswordEncoder();

    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * SM2公钥加密
     */
    public static String sm2Encrypt(byte[] publicKey, String plainText) {
        try {
            SM2 sm2 = SmUtil.sm2(null, publicKey);
            return sm2.encryptBase64(plainText, KeyType.PublicKey);
        } catch (Exception e) {
            log.error("SM2加密失败: {}", e.getMessage());
            throw new RuntimeException("SM2加密失败", e);
        }
    }

    /**
     * SM2私钥解密
     */
    public static String sm2Decrypt(byte[] privateKey, String cipherText) {
        try {
            SM2 sm2 = SmUtil.sm2(privateKey, null);
            return sm2.decryptStr(cipherText, KeyType.PrivateKey);
        } catch (Exception e) {
            log.error("SM2解密失败: {}", e.getMessage());
            throw new RuntimeException("SM2解密失败", e);
        }
    }

    /**
     * SM3哈希加密
     */
    public static String sm3Hash(String plainText) {
        try {
            return SmUtil.sm3(plainText);
        } catch (Exception e) {
            log.error("SM3哈希失败: {}", e.getMessage());
            throw new RuntimeException("SM3哈希失败", e);
        }
    }

    /**
     * SM4加密
     */
    public static String sm4Encrypt(String key, String plainText) {
        try {
            SM4 sm4 = SmUtil.sm4(key.getBytes());
            return sm4.encryptBase64(plainText);
        } catch (Exception e) {
            log.error("SM4加密失败: {}", e.getMessage());
            throw new RuntimeException("SM4加密失败", e);
        }
    }

    /**
     * SM4解密
     */
    public static String sm4Decrypt(String key, String cipherText) {
        try {
            SM4 sm4 = SmUtil.sm4(key.getBytes());
            return sm4.decryptStr(cipherText);
        } catch (Exception e) {
            log.error("SM4解密失败: {}", e.getMessage());
            throw new RuntimeException("SM4解密失败", e);
        }
    }

    /**
     * 生成AES密钥（默认256位）
     */
    public static String generateAesKey() {
        return generateAesKey(AES_KEY_SIZE);
    }

    /**
     * 生成AES密钥
     *
     * @param keySize 密钥长度（位），必须是128、192或256
     */
    public static String generateAesKey(int keySize) {
        try {
            if (keySize != 128 && keySize != 192 && keySize != 256) {
                throw new IllegalArgumentException("AES密钥长度必须是128、192或256位");
            }
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(keySize);
            SecretKey secretKey = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            log.error("生成AES密钥失败，密钥长度: {}", keySize, e);
            throw new RuntimeException("生成AES密钥失败", e);
        }
    }

    /**
     * 生成RSA密钥对（默认2048位）
     */
    public static KeyPair generateRsaKeyPair() {
        return generateRsaKeyPair(RSA_KEY_SIZE);
    }

    /**
     * 生成RSA密钥对
     *
     * @param keySize 密钥长度（位），必须至少为1024
     */
    public static KeyPair generateRsaKeyPair(int keySize) {
        try {
            if (keySize < 1024) {
                throw new IllegalArgumentException("RSA密钥长度必须至少为1024位");
            }
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(keySize);
            return keyGen.generateKeyPair();
        } catch (Exception e) {
            log.error("生成RSA密钥对失败，密钥长度: {}", keySize, e);
            throw new RuntimeException("生成RSA密钥对失败", e);
        }
    }

    /**
     * MD5哈希
     *
     * @deprecated MD5算法已被证明存在碰撞攻击漏洞，不应用于安全相关的场景。
     *             如需哈希，请使用SM3（国密）或SHA-256（国际标准）。
     *             此方法仅保留用于非安全目的的数据完整性校验。
     */
    @Deprecated
    public static String md5Hash(String plainText) {
        try {
            return SecureUtil.md5(plainText);
        } catch (Exception e) {
            log.error("MD5哈希失败: {}", e.getMessage());
            throw new RuntimeException("MD5哈希失败", e);
        }
    }

    /**
     * BCrypt加密
     *
     * @param plainText 明文
     * @return 加密后的哈希值
     */
    public static String bcryptEncrypt(String plainText) {
        try {
            return BCRYPT_ENCODER.encode(plainText);
        } catch (Exception e) {
            log.error("BCrypt加密失败: {}", e.getMessage());
            throw new RuntimeException("BCrypt加密失败", e);
        }
    }

    /**
     * BCrypt密码校验
     *
     * @param plainText 明文
     * @param hashedText 哈希值
     * @return 是否匹配
     */
    public static boolean bcryptCheck(String plainText, String hashedText) {
        try {
            return BCRYPT_ENCODER.matches(plainText, hashedText);
        } catch (Exception e) {
            log.error("BCrypt校验失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * HMAC-SHA256签名
     *
     * @param secretKey 密钥
     * @param data 待签名数据
     * @return Base64编码的签名结果
     */
    public static String hmacSha256(String secretKey, String data) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            log.error("HMAC-SHA256签名失败: {}", e.getMessage());
            throw new RuntimeException("HMAC-SHA256签名失败", e);
        }
    }

    /**
     * HMAC-SHA256签名验证
     *
     * @param secretKey 密钥
     * @param data 待签名数据
     * @param signature 待验证签名
     * @return 签名是否匹配
     */
    public static boolean verifyHmacSha256(String secretKey, String data, String signature) {
        try {
            String calculatedSignature = hmacSha256(secretKey, data);
            return calculatedSignature.equals(signature);
        } catch (Exception e) {
            log.error("HMAC-SHA256签名验证失败: {}", e.getMessage());
            return false;
        }
    }
}