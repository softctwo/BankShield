package com.bankshield.encrypt.service;

import com.bankshield.common.BaseUnitTest;
import com.bankshield.common.result.Result;
import com.bankshield.encrypt.dto.GenerateKeyRequest;
import com.bankshield.encrypt.dto.RotateKeyRequest;
import com.bankshield.encrypt.entity.EncryptionKey;
import com.bankshield.encrypt.entity.KeyRotationHistory;
import com.bankshield.encrypt.enums.KeyStatus;
import com.bankshield.encrypt.enums.KeyType;
import com.bankshield.encrypt.enums.KeyUsage;
import com.bankshield.encrypt.mapper.EncryptionKeyMapper;
import com.bankshield.encrypt.mapper.KeyRotationHistoryMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 密钥管理服务单元测试
 */
@DisplayName("密钥管理服务单元测试")
@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class KeyManagementServiceTest extends BaseUnitTest {
    
    @Mock
    private EncryptionKeyMapper encryptionKeyMapper;
    
    @Mock
    private KeyRotationHistoryMapper keyRotationHistoryMapper;
    
    @Mock
    private KeyGenerationService keyGenerationService;
    
    @Mock
    private KeyStorageService keyStorageService;
    
    @InjectMocks
    private KeyManagementServiceImpl keyManagementService;
    
    private EncryptionKey testKey;
    
    @BeforeEach
    void setUp() {
        // 准备测试密钥
        testKey = new EncryptionKey();
        testKey.setId(1L);
        testKey.setKeyName("test-key");
        testKey.setKeyType(KeyType.SM4);
        testKey.setKeyUsage(KeyUsage.ENCRYPT);
        testKey.setKeyStatus(KeyStatus.ACTIVE);
        testKey.setKeyMaterial("test-key-material");
        testKey.setKeyLength(128);
        testKey.setExpireTime(LocalDateTime.now().plusDays(30));
        testKey.setCreateTime(LocalDateTime.now());
        testKey.setUpdateTime(LocalDateTime.now());
    }
    
    @Test
    @Order(1)
    @DisplayName("生成SM4密钥成功")
    void testGenerateSM4KeySuccess() {
        // Given
        GenerateKeyRequest request = new GenerateKeyRequest();
        request.setKeyType(KeyType.SM4);
        request.setKeyName("test-sm4-key");
        request.setKeyUsage(KeyUsage.ENCRYPT);
        request.setKeyLength(128);
        request.setExpireDays(365);
        
        EncryptionKey generatedKey = new EncryptionKey();
        generatedKey.setId(1L);
        generatedKey.setKeyName(request.getKeyName());
        generatedKey.setKeyType(request.getKeyType());
        generatedKey.setKeyUsage(request.getKeyUsage());
        generatedKey.setKeyStatus(KeyStatus.ACTIVE);
        generatedKey.setKeyMaterial("generated-sm4-key-material");
        generatedKey.setKeyLength(request.getKeyLength());
        generatedKey.setExpireTime(LocalDateTime.now().plusDays(request.getExpireDays()));
        
        when(keyGenerationService.generateKey(any(KeyType.class), any(KeyUsage.class), any(Integer.class))).thenReturn("generated-sm4-key-material");
        when(encryptionKeyMapper.insert(any(EncryptionKey.class))).thenReturn(1);
        
        // When
        Result<EncryptionKey> result = keyManagementService.generateKey(
            request.getKeyName(),
            request.getKeyType(),
            request.getKeyUsage(),
            request.getKeyLength(),
            request.getExpireDays(),
            request.getRotationCycle(),
            request.getDescription(),
            request.getCreatedBy()
        );
        
        // Then
        assertResultSuccess(result);
        assertNotNull(result.getData());
        assertEquals(1L, result.getData().getId());
        
        verify(keyGenerationService, times(1)).generateKey(any(KeyType.class), any(KeyUsage.class), any(Integer.class));
        verify(encryptionKeyMapper, times(1)).insert(any(EncryptionKey.class));
    }
    
    @Test
    @Order(2)
    @DisplayName("生成SM2密钥成功")
    void testGenerateSM2KeySuccess() {
        // Given
        GenerateKeyRequest request = new GenerateKeyRequest();
        request.setKeyType(KeyType.SM2);
        request.setKeyName("test-sm2-key");
        request.setKeyUsage(KeyUsage.SIGN);
        request.setExpireDays(365);
        
        EncryptionKey generatedKey = new EncryptionKey();
        generatedKey.setId(2L);
        generatedKey.setKeyName(request.getKeyName());
        generatedKey.setKeyType(request.getKeyType());
        generatedKey.setKeyUsage(request.getKeyUsage());
        generatedKey.setKeyStatus(KeyStatus.ACTIVE);
        generatedKey.setPublicKeyMaterial("public-key-material");
        generatedKey.setPrivateKeyMaterial("private-key-material");
        generatedKey.setExpireTime(LocalDateTime.now().plusDays(request.getExpireDays()));
        
        when(keyGenerationService.generateKey(any(GenerateKeyRequest.class))).thenReturn(generatedKey);
        when(encryptionKeyMapper.insert(any(EncryptionKey.class))).thenReturn(1);
        
        // When
        Result<Long> result = keyManagementService.generateKey(request);
        
        // Then
        assertResultSuccess(result);
        assertNotNull(result.getData());
        assertEquals(2L, result.getData());
        
        verify(keyGenerationService, times(1)).generateKey(any(GenerateKeyRequest.class));
        verify(encryptionKeyMapper, times(1)).insert(any(EncryptionKey.class));
    }
    
    @Test
    @Order(3)
    @DisplayName("密钥轮换成功")
    void testRotateKeySuccess() {
        // Given
        Long oldKeyId = 1L;
        RotateKeyRequest request = new RotateKeyRequest();
        request.setKeyId(oldKeyId);
        request.setNewKeyName("rotated-key");
        request.setExpireDays(365);
        
        EncryptionKey oldKey = new EncryptionKey();
        oldKey.setId(oldKeyId);
        oldKey.setKeyName("old-key");
        oldKey.setKeyType(KeyType.SM4);
        oldKey.setKeyStatus(KeyStatus.ACTIVE);
        oldKey.setKeyMaterial("old-key-material");
        oldKey.setExpireTime(LocalDateTime.now().plusDays(30));
        
        EncryptionKey newKey = new EncryptionKey();
        newKey.setId(3L);
        newKey.setKeyName(request.getNewKeyName());
        newKey.setKeyType(oldKey.getKeyType());
        newKey.setKeyUsage(oldKey.getKeyUsage());
        newKey.setKeyStatus(KeyStatus.ACTIVE);
        newKey.setKeyMaterial("new-key-material");
        newKey.setExpireTime(LocalDateTime.now().plusDays(request.getExpireDays()));
        
        when(encryptionKeyMapper.selectById(oldKeyId)).thenReturn(oldKey);
        when(keyGenerationService.generateKey(any(GenerateKeyRequest.class))).thenReturn(newKey);
        when(encryptionKeyMapper.updateById(any(EncryptionKey.class))).thenReturn(1);
        when(encryptionKeyMapper.insert(any(EncryptionKey.class))).thenReturn(1);
        when(keyRotationHistoryMapper.insert(any(KeyRotationHistory.class))).thenReturn(1);
        
        // When
        Result<Long> result = keyManagementService.rotateKey(request);
        
        // Then
        assertResultSuccess(result);
        assertNotNull(result.getData());
        assertEquals(3L, result.getData());
        
        // 验证旧密钥状态更新为ROTATING
        verify(encryptionKeyMapper, times(1)).updateById(argThat(key -> 
            key.getId().equals(oldKeyId) && key.getKeyStatus() == KeyStatus.ROTATING
        ));
        
        // 验证新密钥插入
        verify(encryptionKeyMapper, times(1)).insert(any(EncryptionKey.class));
        
        // 验证轮换历史记录
        verify(keyRotationHistoryMapper, times(1)).insert(any(KeyRotationHistory.class));
    }
    
    @Test
    @Order(4)
    @DisplayName("密钥销毁成功")
    void testDestroyKeySuccess() {
        // Given
        Long keyId = 1L;
        
        EncryptionKey key = new EncryptionKey();
        key.setId(keyId);
        key.setKeyName("test-key");
        key.setKeyType(KeyType.SM4);
        key.setKeyStatus(KeyStatus.INACTIVE);
        key.setKeyMaterial("test-key-material");
        key.setExpireTime(LocalDateTime.now().minusDays(1)); // 已过期
        
        when(encryptionKeyMapper.selectById(keyId)).thenReturn(key);
        when(encryptionKeyMapper.updateById(any(EncryptionKey.class))).thenReturn(1);
        
        // When
        Result<String> result = keyManagementService.destroyKey(keyId);
        
        // Then
        assertResultSuccess(result);
        assertEquals("密钥销毁成功", result.getData());
        
        // 验证密钥状态更新为DESTROYED且密钥材料被清除
        verify(encryptionKeyMapper, times(1)).updateById(argThat(k ->
            k.getId().equals(keyId) && 
            k.getKeyStatus() == KeyStatus.DESTROYED &&
            k.getKeyMaterial() == null
        ));
    }
    
    @Test
    @Order(5)
    @DisplayName("销毁活动密钥失败")
    void testDestroyActiveKeyFail() {
        // Given
        Long keyId = 1L;
        
        EncryptionKey key = new EncryptionKey();
        key.setId(keyId);
        key.setKeyName("test-key");
        key.setKeyType(KeyType.SM4);
        key.setKeyStatus(KeyStatus.ACTIVE); // 活动状态
        key.setKeyMaterial("test-key-material");
        key.setExpireTime(LocalDateTime.now().plusDays(30));
        
        when(encryptionKeyMapper.selectById(keyId)).thenReturn(key);
        
        // When
        Result<String> result = keyManagementService.destroyKey(keyId);
        
        // Then
        assertResultError(result, 400);
        assertEquals("只能销毁非活动状态的密钥", result.getMsg());
        
        // 验证没有更新操作
        verify(encryptionKeyMapper, never()).updateById(any(EncryptionKey.class));
    }
    
    @Test
    @Order(6)
    @DisplayName("批量生成密钥")
    void testBatchGenerateKeys() {
        // Given
        List<GenerateKeyRequest> requests = Arrays.asList(
            createKeyRequest("batch-key-1", KeyType.SM4, KeyUsage.ENCRYPT),
            createKeyRequest("batch-key-2", KeyType.SM2, KeyUsage.SIGN),
            createKeyRequest("batch-key-3", KeyType.SM4, KeyUsage.ENCRYPT)
        );
        
        when(keyGenerationService.generateKey(any(GenerateKeyRequest.class)))
            .thenAnswer(invocation -> {
                GenerateKeyRequest request = invocation.getArgument(0);
                EncryptionKey key = new EncryptionKey();
                key.setId(generateTestId());
                key.setKeyName(request.getKeyName());
                key.setKeyType(request.getKeyType());
                key.setKeyUsage(request.getKeyUsage());
                key.setKeyStatus(KeyStatus.ACTIVE);
                key.setKeyMaterial("generated-key-material");
                return key;
            });
        
        when(encryptionKeyMapper.insert(any(EncryptionKey.class))).thenReturn(1);
        
        // When
        Result<List<Long>> result = keyManagementService.batchGenerateKeys(requests);
        
        // Then
        assertResultSuccess(result);
        assertNotNull(result.getData());
        assertEquals(3, result.getData().size());
        
        // 验证批量插入
        verify(encryptionKeyMapper, times(3)).insert(any(EncryptionKey.class));
    }
    
    @Test
    @Order(7)
    @DisplayName("查询密钥详情")
    void testGetKeyDetail() {
        // Given
        Long keyId = 1L;
        
        when(encryptionKeyMapper.selectById(keyId)).thenReturn(testKey);
        
        // When
        Result<EncryptionKey> result = keyManagementService.getKeyDetail(keyId);
        
        // Then
        assertResultSuccess(result);
        assertNotNull(result.getData());
        assertEquals(testKey.getId(), result.getData().getId());
        assertEquals(testKey.getKeyName(), result.getData().getKeyName());
        
        verify(encryptionKeyMapper, times(1)).selectById(keyId);
    }
    
    @Test
    @Order(8)
    @DisplayName("查询不存在的密钥")
    void testGetKeyDetailNotFound() {
        // Given
        Long keyId = 999L;
        
        when(encryptionKeyMapper.selectById(keyId)).thenReturn(null);
        
        // When
        Result<EncryptionKey> result = keyManagementService.getKeyDetail(keyId);
        
        // Then
        assertResultError(result, 404);
        assertEquals("密钥不存在", result.getMsg());
        
        verify(encryptionKeyMapper, times(1)).selectById(keyId);
    }
    
    @Test
    @Order(9)
    @DisplayName("更新密钥状态")
    void testUpdateKeyStatus() {
        // Given
        Long keyId = 1L;
        KeyStatus newStatus = KeyStatus.INACTIVE;
        
        when(encryptionKeyMapper.selectById(keyId)).thenReturn(testKey);
        when(encryptionKeyMapper.updateById(any(EncryptionKey.class))).thenReturn(1);
        
        // When
        Result<String> result = keyManagementService.updateKeyStatus(keyId, newStatus);
        
        // Then
        assertResultSuccess(result);
        assertEquals("密钥状态更新成功", result.getData());
        
        verify(encryptionKeyMapper, times(1)).updateById(argThat(key ->
            key.getId().equals(keyId) && key.getKeyStatus() == newStatus
        ));
    }
    
    /**
     * 创建密钥请求辅助方法
     */
    private GenerateKeyRequest createKeyRequest(String keyName, KeyType keyType, KeyUsage keyUsage) {
        GenerateKeyRequest request = new GenerateKeyRequest();
        request.setKeyName(keyName);
        request.setKeyType(keyType);
        request.setKeyUsage(keyUsage);
        request.setKeyLength(128);
        request.setExpireDays(365);
        return request;
    }
}