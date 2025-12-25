package com.bankshield.demo.controller;

import com.bankshield.demo.crypto.SM3Util;
import com.bankshield.demo.crypto.SM4Util;
import com.bankshield.demo.crypto.SM2Util;
import com.bankshield.demo.response.ApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BankShield加密API控制器
 * 
 * 提供以下功能：
 * 1. SM3哈希算法测试
 * 2. SM4对称加密测试
 * 3. SM2非对称加密测试
 * 4. JWT Token生成测试
 * 5. 密码哈希验证测试
 */
@Api(tags = "加密服务")
@RestController
@RequestMapping("/crypto")
public class CryptoController {

    @ApiOperation("SM3哈希测试")
    @PostMapping("/sm3/hash")
    public ApiResponse<Map<String, String>> testSm3Hash(
            @ApiParam("待哈希的文本") @RequestParam String text) {
        try {
            String hash = SM3Util.hash(text);
            Map<String, String> result = new HashMap<>();
            result.put("original", text);
            result.put("hash", hash);
            result.put("algorithm", "SM3");
            result.put("status", "success");
            return ApiResponse.success(result, "SM3哈希计算成功");
        } catch (Exception e) {
            return ApiResponse.error("SM3哈希计算失败: " + e.getMessage());
        }
    }

    @ApiOperation("SM4对称加密测试")
    @PostMapping("/sm4/encrypt")
    public ApiResponse<Map<String, String>> testSm4Encrypt(
            @ApiParam("待加密的文本") @RequestParam String text) {
        try {
            String key = SM4Util.generateKey();
            String encrypted = SM4Util.encryptECB(key, text);
            String decrypted = SM4Util.decryptECB(key, encrypted);
            
            Map<String, String> result = new HashMap<>();
            result.put("original", text);
            result.put("encrypted", encrypted);
            result.put("decrypted", decrypted);
            result.put("key", key);
            result.put("algorithm", "SM4-ECB");
            result.put("success", String.valueOf(text.equals(decrypted)));
            return ApiResponse.success(result, "SM4加密解密成功");
        } catch (Exception e) {
            return ApiResponse.error("SM4加密失败: " + e.getMessage());
        }
    }

    @ApiOperation("SM2非对称加密测试")
    @PostMapping("/sm2/encrypt")
    public ApiResponse<Map<String, String>> testSm2Encrypt(
            @ApiParam("待加密的文本") @RequestParam String text) {
        try {
            java.security.KeyPair keyPair = SM2Util.generateKeyPair();
            String publicKey = SM2Util.publicKeyToString(keyPair.getPublic());
            String privateKey = SM2Util.privateKeyToString(keyPair.getPrivate());
            String encrypted = SM2Util.encrypt(publicKey, text);
            String decrypted = SM2Util.decrypt(privateKey, encrypted);
            
            Map<String, String> result = new HashMap<>();
            result.put("original", text);
            result.put("encrypted", encrypted);
            result.put("decrypted", decrypted);
            result.put("publicKey", publicKey);
            result.put("privateKey", privateKey);
            result.put("algorithm", "SM2");
            result.put("success", String.valueOf(text.equals(decrypted)));
            return ApiResponse.success(result, "SM2加密解密成功");
        } catch (Exception e) {
            return ApiResponse.error("SM2加密失败: " + e.getMessage());
        }
    }

    @ApiOperation("密码哈希测试")
    @PostMapping("/password/hash")
    public ApiResponse<Map<String, String>> testPasswordHash(
            @ApiParam("待哈希的密码") @RequestParam String password) {
        try {
            byte[] salt = SM3Util.generateSalt(16);
            String hashedPassword = SM3Util.hashWithSalt(password, salt);
            boolean verifyResult = SM3Util.verifyWithSalt(password, hashedPassword);
            boolean wrongPasswordResult = SM3Util.verifyWithSalt("wrongPassword", hashedPassword);
            
            Map<String, String> result = new HashMap<>();
            result.put("originalPassword", password);
            result.put("hashedPassword", hashedPassword);
            result.put("correctVerify", String.valueOf(verifyResult));
            result.put("wrongVerify", String.valueOf(wrongPasswordResult));
            result.put("algorithm", "SM3 Salted Hash");
            return ApiResponse.success(result, "密码哈希验证成功");
        } catch (Exception e) {
            return ApiResponse.error("密码哈希失败: " + e.getMessage());
        }
    }

    @ApiOperation("批量加密功能测试")
    @PostMapping("/batch/test")
    public ApiResponse<Map<String, Object>> batchCryptoTest() {
        try {
            Map<String, Object> results = new HashMap<>();
            
            // SM3测试
            String sm3Text = "BankShield安全测试";
            String sm3Hash = SM3Util.hash(sm3Text);
            results.put("sm3", Map.of(
                "input", sm3Text,
                "hash", sm3Hash,
                "status", "success"
            ));
            
            // SM4测试
            String sm4Text = "SM4对称加密测试";
            String sm4Key = SM4Util.generateKey();
            String sm4Encrypted = SM4Util.encryptECB(sm4Key, sm4Text);
            String sm4Decrypted = SM4Util.decryptECB(sm4Key, sm4Encrypted);
            results.put("sm4", Map.of(
                "input", sm4Text,
                "encrypted", sm4Encrypted,
                "decrypted", sm4Decrypted,
                "success", sm4Text.equals(sm4Decrypted)
            ));
            
            // SM2测试
            String sm2Text = "SM2非对称加密测试";
            java.security.KeyPair keyPair = SM2Util.generateKeyPair();
            String publicKey = SM2Util.publicKeyToString(keyPair.getPublic());
            String privateKey = SM2Util.privateKeyToString(keyPair.getPrivate());
            String sm2Encrypted = SM2Util.encrypt(publicKey, sm2Text);
            String sm2Decrypted = SM2Util.decrypt(privateKey, sm2Encrypted);
            results.put("sm2", Map.of(
                "input", sm2Text,
                "encrypted", sm2Encrypted,
                "decrypted", sm2Decrypted,
                "success", sm2Text.equals(sm2Decrypted)
            ));
            
            results.put("timestamp", System.currentTimeMillis());
            results.put("totalTests", 3);
            
            return ApiResponse.success(results, "批量加密测试完成");
        } catch (Exception e) {
            return ApiResponse.error("批量测试失败: " + e.getMessage());
        }
    }

    @ApiOperation("系统信息")
    @GetMapping("/info")
    public ApiResponse<Map<String, String>> systemInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("name", "BankShield演示系统");
        info.put("version", "1.0.0");
        info.put("description", "银行数据安全管理平台演示");
        info.put("features", "国密算法(SM3/SM2/SM4), JWT认证, 密码安全");
        info.put("algorithms", "SM3, SM2, SM4");
        info.put("status", "运行正常");
        return ApiResponse.success(info, "系统信息获取成功");
    }
}