package com.bankshield.mpc.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

/**
 * 秘密共享（Shamir方案）实现
 * 
 * @author BankShield
 * @version 1.0.0
 */
public class SecretSharing {
    
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final BigInteger PRIME = new BigInteger("115792089237316195423570985008687907853269984665640564039457584007913129639747");
    
    private final int threshold;
    
    public SecretSharing(int threshold) {
        if (threshold < 2) {
            throw new IllegalArgumentException("阈值必须至少为2");
        }
        this.threshold = threshold;
    }
    
    /**
     * 将秘密分片
     * 
     * @param secret 要分片的秘密
     * @param n 分片数量
     * @param t 重构阈值
     * @return 秘密分片数组
     */
    public SecretShare[] share(BigInteger secret, int n, int t) {
        if (t > n) {
            throw new IllegalArgumentException("阈值不能大于分片数量");
        }
        
        // 构造t-1次多项式：f(x) = secret + a1*x + a2*x^2 + ... + a(t-1)*x^(t-1)
        BigInteger[] coefficients = new BigInteger[t];
        coefficients[0] = secret;
        
        // 随机生成其他系数
        for (int i = 1; i < t; i++) {
            coefficients[i] = generateRandomBigInteger();
        }
        
        // 生成n个分片
        SecretShare[] shares = new SecretShare[n];
        for (int i = 1; i <= n; i++) {
            BigInteger x = BigInteger.valueOf(i);
            BigInteger y = evaluatePolynomial(coefficients, x);
            shares[i - 1] = new SecretShare(x, y);
        }
        
        return shares;
    }
    
    /**
     * 重构秘密（拉格朗日插值）
     * 
     * @param shares 秘密分片集合
     * @return 重构的秘密
     */
    public BigInteger reconstruct(Collection<SecretShare> shares) {
        if (shares.size() < threshold) {
            throw new IllegalArgumentException("分片数量不足，需要至少" + threshold + "个分片");
        }
        
        return lagrangeInterpolation(shares);
    }
    
    /**
     * 评估多项式值
     */
    private BigInteger evaluatePolynomial(BigInteger[] coefficients, BigInteger x) {
        BigInteger result = BigInteger.ZERO;
        BigInteger power = BigInteger.ONE;
        
        for (BigInteger coefficient : coefficients) {
            result = result.add(coefficient.multiply(power)).mod(PRIME);
            power = power.multiply(x).mod(PRIME);
        }
        
        return result;
    }
    
    /**
     * 拉格朗日插值
     */
    private BigInteger lagrangeInterpolation(Collection<SecretShare> shares) {
        BigInteger secret = BigInteger.ZERO;
        
        for (SecretShare share : shares) {
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;
            
            for (SecretShare other : shares) {
                if (!share.equals(other)) {
                    // numerator *= other.x
                    numerator = numerator.multiply(other.getX()).mod(PRIME);
                    // denominator *= (other.x - share.x)
                    denominator = denominator.multiply(
                        other.getX().subtract(share.getX()).mod(PRIME)
                    ).mod(PRIME);
                }
            }
            
            // 计算拉格朗日基多项式值
            BigInteger lagrange = numerator.multiply(
                denominator.modInverse(PRIME)
            ).mod(PRIME).multiply(share.getY()).mod(PRIME);
            
            secret = secret.add(lagrange).mod(PRIME);
        }
        
        return secret;
    }
    
    /**
     * 生成随机大整数
     */
    private BigInteger generateRandomBigInteger() {
        return new BigInteger(PRIME.bitLength() - 1, SECURE_RANDOM);
    }
    
    /**
     * 秘密分片类
     */
    @Data
    @AllArgsConstructor
    public static class SecretShare {
        private BigInteger x;  // x坐标
        private BigInteger y;  // y坐标
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SecretShare that = (SecretShare) o;
            return Objects.equals(x, that.x) && Objects.equals(y, that.y);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
}