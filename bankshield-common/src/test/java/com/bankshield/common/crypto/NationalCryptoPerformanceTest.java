package com.bankshield.common.crypto;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.KeyPair;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 国密算法性能测试
 */
@SpringBootTest
public class NationalCryptoPerformanceTest {
    
    private static final int TEST_ITERATIONS = 10000;
    private static final int LARGE_DATA_SIZE = 1024 * 1024; // 1MB
    
    @Test
    public void testSM2Performance() {
        System.out.println("=== SM2性能测试 ===");
        
        // 生成密钥对
        long keyGenStart = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            SM2Util.generateKeyPair();
        }
        long keyGenEnd = System.nanoTime();
        double keyGenTime = (keyGenEnd - keyGenStart) / 1000000.0 / 100;
        System.out.printf("SM2密钥对生成平均耗时: %.2f ms%n", keyGenTime);
        
        // 准备测试数据
        KeyPair keyPair = SM2Util.generateKeyPair();
        String publicKey = SM2Util.publicKeyToString(keyPair.getPublic());
        String privateKey = SM2Util.privateKeyToString(keyPair.getPrivate());
        String data = "Performance test data for SM2 encryption and signature";
        
        // 加密性能测试
        long encryptStart = System.nanoTime();
        String encrypted = null;
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            encrypted = SM2Util.encrypt(publicKey, data);
        }
        long encryptEnd = System.nanoTime();
        double encryptTime = (encryptEnd - encryptStart) / 1000000.0 / TEST_ITERATIONS;
        System.out.printf("SM2加密平均耗时: %.2f ms%n", encryptTime);
        
        // 解密性能测试
        long decryptStart = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            SM2Util.decrypt(privateKey, encrypted);
        }
        long decryptEnd = System.nanoTime();
        double decryptTime = (decryptEnd - decryptStart) / 1000000.0 / TEST_ITERATIONS;
        System.out.printf("SM2解密平均耗时: %.2f ms%n", decryptTime);
        
        // 签名性能测试
        long signStart = System.nanoTime();
        String signature = null;
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            signature = SM2Util.sign(privateKey, data);
        }
        long signEnd = System.nanoTime();
        double signTime = (signEnd - signStart) / 1000000.0 / TEST_ITERATIONS;
        System.out.printf("SM2签名平均耗时: %.2f ms%n", signTime);
        
        // 验签性能测试
        long verifyStart = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            SM2Util.verify(publicKey, data, signature);
        }
        long verifyEnd = System.nanoTime();
        double verifyTime = (verifyEnd - verifyStart) / 1000000.0 / TEST_ITERATIONS;
        System.out.printf("SM2验签平均耗时: %.2f ms%n", verifyTime);
        
        // 性能指标验证
        assertTrue(signTime < 1.0, "SM2签名性能应该小于1ms");
        assertTrue(verifyTime < 1.0, "SM2验签性能应该小于1ms");
    }
    
    @Test
    public void testSM3Performance() {
        System.out.println("\n=== SM3性能测试 ===");
        
        String testData = "Performance test data for SM3 hash algorithm";
        
        // 哈希性能测试
        long hashStart = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            SM3Util.hash(testData);
        }
        long hashEnd = System.nanoTime();
        double hashTime = (hashEnd - hashStart) / 1000000.0 / TEST_ITERATIONS;
        System.out.printf("SM3哈希平均耗时: %.2f ms%n", hashTime);
        
        // HMAC性能测试
        String key = "test-key";
        long hmacStart = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            SM3Util.hmac(key, testData);
        }
        long hmacEnd = System.nanoTime();
        double hmacTime = (hmacEnd - hmacStart) / 1000000.0 / TEST_ITERATIONS;
        System.out.printf("SM3-HMAC平均耗时: %.2f ms%n", hmacTime);
        
        // 密码编码性能测试
        SM3PasswordEncoder encoder = new SM3PasswordEncoder();
        long passwordStart = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            encoder.encode("TestPassword123!");
        }
        long passwordEnd = System.nanoTime();
        double passwordTime = (passwordEnd - passwordStart) / 1000000.0 / TEST_ITERATIONS;
        System.out.printf("SM3密码编码平均耗时: %.2f ms%n", passwordTime);
        
        // 性能指标验证
        assertTrue(hashTime < 0.1, "SM3哈希性能应该小于0.1ms");
        assertTrue(passwordTime < 1.0, "SM3密码编码性能应该小于1ms");
    }
    
    @Test
    public void testSM4Performance() {
        System.out.println("\n=== SM4性能测试 ===");
        
        String key = SM4Util.generateKey();
        String iv = SM4Util.generateIV();
        String testData = "Performance test data for SM4 encryption";
        
        // ECB模式性能测试
        long ecbEncryptStart = System.nanoTime();
        String ecbEncrypted = null;
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            ecbEncrypted = SM4Util.encryptECB(key, testData);
        }
        long ecbEncryptEnd = System.nanoTime();
        double ecbEncryptTime = (ecbEncryptEnd - ecbEncryptStart) / 1000000.0 / TEST_ITERATIONS;
        System.out.printf("SM4-ECB加密平均耗时: %.2f ms%n", ecbEncryptTime);
        
        long ecbDecryptStart = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            SM4Util.decryptECB(key, ecbEncrypted);
        }
        long ecbDecryptEnd = System.nanoTime();
        double ecbDecryptTime = (ecbDecryptEnd - ecbDecryptStart) / 1000000.0 / TEST_ITERATIONS;
        System.out.printf("SM4-ECB解密平均耗时: %.2f ms%n", ecbDecryptTime);
        
        // CBC模式性能测试
        long cbcEncryptStart = System.nanoTime();
        String cbcEncrypted = null;
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            cbcEncrypted = SM4Util.encryptCBC(key, iv, testData);
        }
        long cbcEncryptEnd = System.nanoTime();
        double cbcEncryptTime = (cbcEncryptEnd - cbcEncryptStart) / 1000000.0 / TEST_ITERATIONS;
        System.out.printf("SM4-CBC加密平均耗时: %.2f ms%n", cbcEncryptTime);
        
        long cbcDecryptStart = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            SM4Util.decryptCBC(key, iv, cbcEncrypted);
        }
        long cbcDecryptEnd = System.nanoTime();
        double cbcDecryptTime = (cbcDecryptEnd - cbcDecryptStart) / 1000000.0 / TEST_ITERATIONS;
        System.out.printf("SM4-CBC解密平均耗时: %.2f ms%n", cbcDecryptTime);
        
        // CTR模式性能测试
        long ctrEncryptStart = System.nanoTime();
        String ctrEncrypted = null;
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            ctrEncrypted = SM4Util.encryptCTR(key, iv, testData);
        }
        long ctrEncryptEnd = System.nanoTime();
        double ctrEncryptTime = (ctrEncryptEnd - ctrEncryptStart) / 1000000.0 / TEST_ITERATIONS;
        System.out.printf("SM4-CTR加密平均耗时: %.2f ms%n", ctrEncryptTime);
        
        long ctrDecryptStart = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            SM4Util.decryptCTR(key, iv, ctrEncrypted);
        }
        long ctrDecryptEnd = System.nanoTime();
        double ctrDecryptTime = (ctrDecryptEnd - ctrDecryptStart) / 1000000.0 / TEST_ITERATIONS;
        System.out.printf("SM4-CTR解密平均耗时: %.2f ms%n", ctrDecryptTime);
        
        // 性能指标验证
        assertTrue(ecbEncryptTime < 0.1, "SM4-ECB加密性能应该小于0.1ms");
        assertTrue(cbcEncryptTime < 0.1, "SM4-CBC加密性能应该小于0.1ms");
    }
    
    @Test
    public void testLargeDataPerformance() {
        System.out.println("\n=== 大数据量性能测试 ===");
        
        // 生成1MB测试数据
        byte[] largeData = new byte[LARGE_DATA_SIZE];
        new Random().nextBytes(largeData);
        String largeDataStr = new String(largeData);
        
        // SM3大数据哈希
        long sm3Start = System.nanoTime();
        SM3Util.hash(largeDataStr);
        long sm3End = System.nanoTime();
        double sm3Throughput = LARGE_DATA_SIZE / ((sm3End - sm3Start) / 1000000000.0) / 1024 / 1024;
        System.out.printf("SM3哈希吞吐率: %.2f MB/s%n", sm3Throughput);
        
        // SM4大数据加密
        String key = SM4Util.generateKey();
        long sm4Start = System.nanoTime();
        SM4Util.encryptECB(key, largeDataStr);
        long sm4End = System.nanoTime();
        double sm4Throughput = LARGE_DATA_SIZE / ((sm4End - sm4Start) / 1000000000.0) / 1024 / 1024;
        System.out.printf("SM4加密吞吐率: %.2f MB/s%n", sm4Throughput);
        
        // 性能指标验证
        assertTrue(sm3Throughput > 200, "SM3哈希吞吐率应该大于200MB/s");
        assertTrue(sm4Throughput > 100, "SM4加密吞吐率应该大于100MB/s");
    }
    
    @Test
    public void testConcurrentPerformance() throws InterruptedException {
        System.out.println("\n=== 并发性能测试 ===");
        
        final int threadCount = 10;
        final int operationsPerThread = 1000;
        
        Thread[] threads = new Thread[threadCount];
        final long[] results = new long[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                long start = System.nanoTime();
                for (int j = 0; j < operationsPerThread; j++) {
                    SM3Util.hash("Concurrent test data " + threadIndex + " " + j);
                }
                results[threadIndex] = System.nanoTime() - start;
            });
        }
        
        long totalStart = System.nanoTime();
        for (Thread thread : threads) {
            thread.start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        long totalEnd = System.nanoTime();
        
        double totalTime = (totalEnd - totalStart) / 1000000.0;
        double throughput = (threadCount * operationsPerThread) / (totalTime / 1000.0);
        System.out.printf("并发SM3哈希总耗时: %.2f ms%n", totalTime);
        System.out.printf("并发SM3哈希吞吐率: %.0f 次/秒%n", throughput);
        
        assertTrue(throughput > 50000, "并发SM3哈希吞吐率应该大于50000次/秒");
    }
}