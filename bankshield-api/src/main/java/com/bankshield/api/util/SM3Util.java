package com.bankshield.api.util;

import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;

/**
 * SM3哈希算法工具类
 * 基于BouncyCastle实现国密SM3算法
 */
public class SM3Util {
    
    /**
     * 计算字符串的SM3哈希值
     * @param data 原始数据
     * @return 十六进制哈希字符串
     */
    public static String hash(String data) {
        if (data == null) {
            return null;
        }
        return hash(data.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 计算字节数组的SM3哈希值
     * @param data 原始数据
     * @return 十六进制哈希字符串
     */
    public static String hash(byte[] data) {
        if (data == null) {
            return null;
        }
        
        SM3Digest digest = new SM3Digest();
        digest.update(data, 0, data.length);
        
        byte[] result = new byte[digest.getDigestSize()];
        digest.doFinal(result, 0);
        
        return Hex.toHexString(result);
    }
    
    /**
     * 验证数据的SM3哈希值
     * @param data 原始数据
     * @param expectedHash 期望的哈希值
     * @return 是否匹配
     */
    public static boolean verify(String data, String expectedHash) {
        if (data == null || expectedHash == null) {
            return false;
        }
        String actualHash = hash(data);
        return expectedHash.equalsIgnoreCase(actualHash);
    }
    
    /**
     * 计算多个字符串拼接后的SM3哈希值
     * @param parts 字符串数组
     * @return 十六进制哈希字符串
     */
    public static String hashMultiple(String... parts) {
        if (parts == null || parts.length == 0) {
            return null;
        }
        
        StringBuilder combined = new StringBuilder();
        for (String part : parts) {
            if (part != null) {
                combined.append(part);
            }
        }
        
        return hash(combined.toString());
    }
    
    /**
     * 计算Merkle树根哈希
     * @param hashes 叶子节点哈希数组
     * @return Merkle根哈希
     */
    public static String calculateMerkleRoot(String[] hashes) {
        if (hashes == null || hashes.length == 0) {
            return hash("");
        }
        
        if (hashes.length == 1) {
            return hashes[0];
        }
        
        // 递归计算Merkle树
        int newLength = (hashes.length + 1) / 2;
        String[] newHashes = new String[newLength];
        
        for (int i = 0; i < hashes.length; i += 2) {
            if (i + 1 < hashes.length) {
                // 两个节点合并
                newHashes[i / 2] = hash(hashes[i] + hashes[i + 1]);
            } else {
                // 奇数个节点，最后一个节点自己合并
                newHashes[i / 2] = hash(hashes[i] + hashes[i]);
            }
        }
        
        return calculateMerkleRoot(newHashes);
    }
}
