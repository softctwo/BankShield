package com.bankshield.api.controller;

import com.bankshield.common.result.Result;
import com.bankshield.common.security.AuthoritiesConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * VaultController权限控制测试
 * 验证Vault接口的权限控制是否生效
 */
@SpringBootTest
@AutoConfigureTestMvc
public class VaultControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SecureKeyManagementService secureKeyManagementService;

    /**
     * 测试未认证用户访问Vault状态接口 - 应该被拒绝
     */
    @Test
    @WithAnonymousUser
    public void testCheckVaultStatus_Unauthenticated_ShouldDeny() throws Exception {
        mockMvc.perform(get("/api/vault/status"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * 测试普通用户访问Vault状态接口 - 应该被拒绝
     */
    @Test
    @WithMockUser(roles = "USER")
    public void testCheckVaultStatus_RegularUser_ShouldDeny() throws Exception {
        mockMvc.perform(get("/api/vault/status"))
                .andExpect(status().isForbidden());
    }

    /**
     * 测试管理员访问Vault状态接口 - 应该允许
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCheckVaultStatus_Admin_ShouldAllow() throws Exception {
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("status", "active");
        Result<Map<String, Object>> successResult = Result.success(statusData);
        
        when(secureKeyManagementService.checkVaultStatus()).thenReturn(successResult);

        mockMvc.perform(get("/api/vault/status"))
                .andExpect(status().isOk());
    }

    /**
     * 测试具有VAULT_ACCESS权限的用户访问Vault状态接口 - 应该允许
     */
    @Test
    @WithMockUser(authorities = AuthoritiesConstants.AUTHORITY_VAULT_ACCESS)
    public void testCheckVaultStatus_WithVaultAccess_ShouldAllow() throws Exception {
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("status", "active");
        Result<Map<String, Object>> successResult = Result.success(statusData);
        
        when(secureKeyManagementService.checkVaultStatus()).thenReturn(successResult);

        mockMvc.perform(get("/api/vault/status"))
                .andExpect(status().isOk());
    }

    /**
     * 测试未认证用户生成加密密钥 - 应该被拒绝
     */
    @Test
    @WithAnonymousUser
    public void testGenerateEncryptionKey_Unauthenticated_ShouldDeny() throws Exception {
        mockMvc.perform(post("/api/vault/key/generate")
                        .param("algorithm", "SM4")
                        .param("keyLength", "128"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * 测试普通用户生成加密密钥 - 应该被拒绝
     */
    @Test
    @WithMockUser(roles = "USER")
    public void testGenerateEncryptionKey_RegularUser_ShouldDeny() throws Exception {
        mockMvc.perform(post("/api/vault/key/generate")
                        .param("algorithm", "SM4")
                        .param("keyLength", "128"))
                .andExpect(status().isForbidden());
    }

    /**
     * 测试管理员生成加密密钥 - 应该允许
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGenerateEncryptionKey_Admin_ShouldAllow() throws Exception {
        Result<String> successResult = Result.success("generated-key-123");
        when(secureKeyManagementService.generateEncryptionKey(eq("SM4"), eq(128)))
                .thenReturn(successResult);

        mockMvc.perform(post("/api/vault/key/generate")
                        .param("algorithm", "SM4")
                        .param("keyLength", "128"))
                .andExpect(status().isOk());
    }

    /**
     * 测试安全管理员生成加密密钥 - 应该允许
     */
    @Test
    @WithMockUser(roles = "SECURITY_ADMIN")
    public void testGenerateEncryptionKey_SecurityAdmin_ShouldAllow() throws Exception {
        Result<String> successResult = Result.success("generated-key-123");
        when(secureKeyManagementService.generateEncryptionKey(eq("SM4"), eq(128)))
                .thenReturn(successResult);

        mockMvc.perform(post("/api/vault/key/generate")
                        .param("algorithm", "SM4")
                        .param("keyLength", "128"))
                .andExpect(status().isOk());
    }

    /**
     * 测试未认证用户解密密钥 - 应该被拒绝
     */
    @Test
    @WithAnonymousUser
    public void testDecryptKeyMaterial_Unauthenticated_ShouldDeny() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("encryptedKeyMaterial", "encrypted-data-123");

        mockMvc.perform(post("/api/vault/key/decrypt")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isUnauthorized());
    }

    /**
     * 测试普通用户解密密钥 - 应该被拒绝
     */
    @Test
    @WithMockUser(roles = "USER")
    public void testDecryptKeyMaterial_RegularUser_ShouldDeny() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("encryptedKeyMaterial", "encrypted-data-123");

        mockMvc.perform(post("/api/vault/key/decrypt")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isForbidden());
    }

    /**
     * 测试具有DECRYPT权限的用户解密密钥 - 应该允许
     */
    @Test
    @WithMockUser(authorities = AuthoritiesConstants.AUTHORITY_DECRYPT)
    public void testDecryptKeyMaterial_WithDecryptAuthority_ShouldAllow() throws Exception {
        Result<String> successResult = Result.success("decrypted-key-123");
        when(secureKeyManagementService.decryptKeyMaterial(eq("encrypted-data-123")))
                .thenReturn(successResult);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("encryptedKeyMaterial", "encrypted-data-123");

        mockMvc.perform(post("/api/vault/key/decrypt")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk());
    }

    /**
     * 测试管理员解密密钥 - 应该允许
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDecryptKeyMaterial_Admin_ShouldAllow() throws Exception {
        Result<String> successResult = Result.success("decrypted-key-123");
        when(secureKeyManagementService.decryptKeyMaterial(eq("encrypted-data-123")))
                .thenReturn(successResult);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("encryptedKeyMaterial", "encrypted-data-123");

        mockMvc.perform(post("/api/vault/key/decrypt")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk());
    }

    /**
     * 测试安全管理员解密密钥 - 应该允许
     */
    @Test
    @WithMockUser(roles = "SECURITY_ADMIN")
    public void testDecryptKeyMaterial_SecurityAdmin_ShouldAllow() throws Exception {
        Result<String> successResult = Result.success("decrypted-key-123");
        when(secureKeyManagementService.decryptKeyMaterial(eq("encrypted-data-123")))
                .thenReturn(successResult);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("encryptedKeyMaterial", "encrypted-data-123");

        mockMvc.perform(post("/api/vault/key/decrypt")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk());
    }
}