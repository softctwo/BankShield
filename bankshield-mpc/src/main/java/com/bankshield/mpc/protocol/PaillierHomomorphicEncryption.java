package com.bankshield.mpc.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Paillier同态加密实现
 * 
 * @author BankShield
 * @version 1.0.0
 */
public class PaillierHomomorphicEncryption {
    
    private static final int KEY_SIZE = 2048;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    /**
     * Paillier公钥
     */
    @Data
    @AllArgsConstructor
    public static class PaillierPublicKey {
        private BigInteger n;    // n = p * q
        private BigInteger nSquare;  // n^2
        private BigInteger g;    // g = n + 1
        
        public PaillierPublicKey(BigInteger n, BigInteger g) {
            this.n = n;
            this.nSquare = n.multiply(n);
            this.g = g;
        }
    }
    
    /**
     * Paillier私钥
     */
    @Data
    @AllArgsConstructor
    public static class PaillierPrivateKey {
        private BigInteger lambda;  // lambda = lcm(p-1, q-1)
        private BigInteger mu;      // mu = lambda^(-1) mod n
        private BigInteger n;       // n = p * q
        private BigInteger nSquare; // n^2
        
        public PaillierPrivateKey(BigInteger lambda, BigInteger n) {
            this.lambda = lambda;
            this.n = n;
            this.nSquare = n.multiply(n);
            this.mu = lambda.modInverse(n);
        }
    }
    
    /**
     * Paillier密钥对
     */
    @Data
    @AllArgsConstructor
    public static class PaillierKeyPair {
        private PaillierPublicKey publicKey;
        private PaillierPrivateKey privateKey;
    }
    
    /**
     * 生成Paillier密钥对
     * 
     * @return Paillier密钥对
     */
    public static PaillierKeyPair generateKeyPair() {
        // 生成两个大素数p和q
        BigInteger p = generateLargePrime(KEY_SIZE / 2);
        BigInteger q = generateLargePrime(KEY_SIZE / 2);
        
        // 计算n = p * q
        BigInteger n = p.multiply(q);
        BigInteger nSquare = n.multiply(n);
        
        // 计算lambda = lcm(p-1, q-1)
        BigInteger lambda = lcm(p.subtract(BigInteger.ONE), q.subtract(BigInteger.ONE));
        
        // 生成g = n + 1
        BigInteger g = n.add(BigInteger.ONE);
        
        // 创建密钥
        PaillierPublicKey publicKey = new PaillierPublicKey(n, g);
        PaillierPrivateKey privateKey = new PaillierPrivateKey(lambda, n);
        
        return new PaillierKeyPair(publicKey, privateKey);
    }
    
    /**
     * 加密
     * 
     * @param publicKey 公钥
     * @param plaintext 明文
     * @return 密文
     */
    public static BigInteger encrypt(PaillierPublicKey publicKey, BigInteger plaintext) {
        BigInteger n = publicKey.getN();
        BigInteger nSquare = publicKey.getNSquare();
        BigInteger g = publicKey.getG();
        
        // 确保明文小于n
        if (plaintext.compareTo(n) >= 0) {
            throw new IllegalArgumentException("明文必须小于n");
        }
        
        // 生成随机数r，满足gcd(r, n) = 1
        BigInteger r = generateRandomCoprime(n);
        
        // c = g^m * r^n mod n^2
        BigInteger gm = g.modPow(plaintext, nSquare);
        BigInteger rn = r.modPow(n, nSquare);
        return gm.multiply(rn).mod(nSquare);
    }
    
    /**
     * 解密
     * 
     * @param privateKey 私钥
     * @param ciphertext 密文
     * @return 明文
     */
    public static BigInteger decrypt(PaillierPrivateKey privateKey, BigInteger ciphertext) {
        BigInteger lambda = privateKey.getLambda();
        BigInteger mu = privateKey.getMu();
        BigInteger n = privateKey.getN();
        BigInteger nSquare = privateKey.getNSquare();
        
        // m = L(c^lambda mod n^2) * mu mod n
        // 其中L(x) = (x - 1) / n
        BigInteger cLambda = ciphertext.modPow(lambda, nSquare);
        BigInteger l = cLambda.subtract(BigInteger.ONE).divide(n);
        return l.multiply(mu).mod(n);
    }
    
    /**
     * 同态加法：E(m1) * E(m2) = E(m1 + m2)
     * 
     * @param publicKey 公钥
     * @param cipher1 密文1
     * @param cipher2 密文2
     * @return 加法结果密文
     */
    public static BigInteger add(PaillierPublicKey publicKey, BigInteger cipher1, BigInteger cipher2) {
        BigInteger nSquare = publicKey.getNSquare();
        return cipher1.multiply(cipher2).mod(nSquare);
    }
    
    /**
     * 同态标量乘法：E(m)^k = E(k * m)
     * 
     * @param publicKey 公钥
     * @param cipher 密文
     * @param scalar 标量
     * @return 标量乘法结果密文
     */
    public static BigInteger multiply(PaillierPublicKey publicKey, BigInteger cipher, BigInteger scalar) {
        BigInteger nSquare = publicKey.getNSquare();
        return cipher.modPow(scalar, nSquare);
    }
    
    /**
     * 生成大素数
     */
    private static BigInteger generateLargePrime(int bitLength) {
        return BigInteger.probablePrime(bitLength, SECURE_RANDOM);
    }
    
    /**
     * 计算最小公倍数
     */
    private static BigInteger lcm(BigInteger a, BigInteger b) {
        return a.multiply(b).divide(a.gcd(b));
    }
    
    /**
     * 生成与n互质的随机数
     */
    private static BigInteger generateRandomCoprime(BigInteger n) {
        BigInteger r;
        do {
            r = new BigInteger(n.bitLength(), SECURE_RANDOM);
        } while (r.compareTo(n) >= 0 || r.compareTo(BigInteger.ZERO) <= 0 || !r.gcd(n).equals(BigInteger.ONE));
        
        return r;
    }
}