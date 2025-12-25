package com.bankshield.mpc.protocol;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

/**
 * 不经意传输（OT）协议实现
 * 
 * @author BankShield
 * @version 1.0.0
 */
public class ObliviousTransfer {
    
    private static final int RSA_KEY_SIZE = 2048;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    /**
     * 1-out-of-N OT协议
     * 
     * @param messages 发送方的消息数组
     * @param choice 接收方的选择（0到n-1）
     * @param keyPair RSA密钥对
     * @return 接收方获得的消息
     */
    public static byte[] performOT(byte[][] messages, int choice, AsymmetricCipherKeyPair keyPair) {
        if (messages == null || messages.length == 0) {
            throw new IllegalArgumentException("消息数组不能为空");
        }
        
        if (choice < 0 || choice >= messages.length) {
            throw new IllegalArgumentException("选择索引超出范围");
        }
        
        // 1. 接收方生成盲化值
        BigInteger[] blindedValues = new BigInteger[messages.length];
        BigInteger blindingFactor = null;
        
        RSAKeyParameters publicKey = (RSAKeyParameters) keyPair.getPublic();
        BigInteger n = publicKey.getModulus();
        BigInteger e = publicKey.getExponent();
        
        for (int i = 0; i < messages.length; i++) {
            if (i == choice) {
                // 为选择的消息生成有效的盲化值
                blindingFactor = generateRandomInteger(n);
                BigInteger messageHash = hashToBigInteger("message_" + i);
                blindedValues[i] = messageHash.multiply(blindingFactor.modPow(e, n)).mod(n);
            } else {
                // 为其他消息生成随机的盲化值
                blindedValues[i] = generateRandomInteger(n);
            }
        }
        
        // 2. 发送方使用RSA盲签名
        RSAPrivateCrtKeyParameters privateKey = (RSAPrivateCrtKeyParameters) keyPair.getPrivate();
        BigInteger d = privateKey.getExponent();
        BigInteger[] signatures = new BigInteger[messages.length];
        
        for (int i = 0; i < messages.length; i++) {
            signatures[i] = blindedValues[i].modPow(d, n);
        }
        
        // 3. 接收方去盲化第choice个签名
        BigInteger unblindedSignature = signatures[choice].multiply(blindingFactor.modInverse(n)).mod(n);
        
        // 4. 验证并解密第choice个消息
        return decryptMessage(messages[choice], unblindedSignature);
    }
    
    /**
     * 生成RSA密钥对
     */
    public static AsymmetricCipherKeyPair generateKeyPair() {
        RSAKeyPairGenerator generator = new RSAKeyPairGenerator();
        generator.init(new RSAKeyGenerationParameters(
            BigInteger.valueOf(65537),  // 公钥指数
            SECURE_RANDOM,
            RSA_KEY_SIZE,
            80  // 素数测试的确定性
        ));
        
        return generator.generateKeyPair();
    }
    
    /**
     * 生成随机大整数
     */
    private static BigInteger generateRandomInteger(BigInteger max) {
        BigInteger random;
        do {
            random = new BigInteger(max.bitLength(), SECURE_RANDOM);
        } while (random.compareTo(max) >= 0 || random.compareTo(BigInteger.ZERO) <= 0);
        
        return random;
    }
    
    /**
     * 将字符串哈希为大整数
     */
    private static BigInteger hashToBigInteger(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes("UTF-8"));
            return new BigInteger(1, hash);
        } catch (Exception e) {
            throw new RuntimeException("哈希计算失败", e);
        }
    }
    
    /**
     * 解密消息（简单的异或加密）
     */
    private static byte[] decryptMessage(byte[] encryptedMessage, BigInteger key) {
        byte[] keyBytes = key.toByteArray();
        byte[] result = new byte[encryptedMessage.length];
        
        for (int i = 0; i < encryptedMessage.length; i++) {
            result[i] = (byte) (encryptedMessage[i] ^ keyBytes[i % keyBytes.length]);
        }
        
        return result;
    }
}