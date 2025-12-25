package com.bankshield.api.controller;

import com.bankshield.api.service.SecureKeyManagementService;
import com.bankshield.common.result.Result;
// import com.bankshield.common.security.AuthoritiesConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Vault管理控制器
 * 提供Vault状态检查和密钥管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/vault")
@Api(tags = "Vault管理", description = "HashiCorp Vault集成管理接口")
public class VaultController {
    
    @Autowired
    private SecureKeyManagementService secureKeyManagementService;
    
    /**
     * 检查Vault连接状态
     * 需要管理员权限才能访问Vault状态信息
     */
    @GetMapping("/status")
    @ApiOperation(value = "检查Vault状态", notes = "检查与HashiCorp Vault的连接状态 - 需要管理员权限")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('VAULT_ACCESS')")
    public Result<Map<String, Object>> checkVaultStatus() {
        try {
            log.info("Checking Vault connection status");
            return secureKeyManagementService.checkVaultStatus();
        } catch (Exception e) {
            log.error("Failed to check Vault status", e);
            return Result.error("检查Vault状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成新的加密密钥
     * 只有管理员角色才能生成新的加密密钥，遵循最小权限原则
     */
    @PostMapping("/key/generate")
    @ApiOperation(value = "生成加密密钥", notes = "使用Vault管理的主密钥生成新的加密密钥 - 需要管理员权限")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECURITY_ADMIN')")
    public Result<String> generateEncryptionKey(
            @ApiParam(value = "加密算法", required = true, example = "SM4")
            @RequestParam String algorithm,
            @ApiParam(value = "密钥长度", required = true, example = "128")
            @RequestParam int keyLength) {
        try {
            log.info("Generating encryption key: algorithm={}, keyLength={}", algorithm, keyLength);
            return secureKeyManagementService.generateEncryptionKey(algorithm, keyLength);
        } catch (Exception e) {
            log.error("Failed to generate encryption key", e);
            return Result.error("生成加密密钥失败: " + e.getMessage());
        }
    }
    
    /**
     * 解密密钥材料
     * 需要特定的解密权限才能执行此敏感操作
     */
    @PostMapping("/key/decrypt")
    @ApiOperation(value = "解密密钥", notes = "使用Vault管理的主密钥解密密钥材料 - 需要解密权限")
    @PreAuthorize("hasAuthority('DECRYPT') or hasRole('ADMIN') or hasRole('SECURITY_ADMIN')")
    public Result<String> decryptKeyMaterial(
            @ApiParam(value = "加密的密钥材料", required = true)
            @RequestBody Map<String, String> request) {
        try {
            String encryptedKeyMaterial = request.get("encryptedKeyMaterial");
            if (encryptedKeyMaterial == null || encryptedKeyMaterial.trim().isEmpty()) {
                return Result.error("加密的密钥材料不能为空");
            }
            
            log.info("Decrypting key material");
            return secureKeyManagementService.decryptKeyMaterial(encryptedKeyMaterial);
        } catch (Exception e) {
            log.error("Failed to decrypt key material", e);
            return Result.error("解密密钥材料失败: " + e.getMessage());
        }
    }
}