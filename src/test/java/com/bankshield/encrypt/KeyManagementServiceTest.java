package com.bankshield.encrypt;

import com.bankshield.common.entity.EncryptionKey;
import com.bankshield.common.enums.KeyStatus;
import com.bankshield.common.enums.KeyType;
import com.bankshield.common.exception.KeyGenerationException;
import com.bankshield.common.param.KeyGenerationParam;
import com.bankshield.common.result.Result;
import com.bankshield.encrypt.mapper.EncryptionKeyMapper;
import com.bankshield.encrypt.service.KeyManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class KeyManagementServiceTest {
    
    @Autowired
    private KeyManagementService keyService;
    
    @MockBean
    private EncryptionKeyMapper keyMapper;
    
    private KeyGenerationParam validParam;
    
    @BeforeEach
    void setUp() {
        validParam = new KeyGenerationParam();
        validParam.setKeyType("SM4");
        validParam.setKeyLength(128);
        validParam.setKeyUsage("ENCRYPT");
        validParam.setAlgorithm("SM4/ECB/PKCS5Padding");
    }
    
    @Test
    @DisplayName("生成SM4密钥成功")
    void testGenerateSM4KeySuccess() {
        // Given
        when(keyMapper.insert(any(EncryptionKey.class))).thenReturn(1);
        
        // When
        Result<Long> result = keyService.generateKey(validParam);
        
        // Then
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getMessage()).isEqualTo("密钥生成成功");
        
        // Verify database interaction
        ArgumentCaptor<EncryptionKey> keyCaptor = ArgumentCaptor.forClass(EncryptionKey.class);
        verify(keyMapper, times(1)).insert(keyCaptor.capture());
        
        EncryptionKey savedKey = keyCaptor.getValue();
        assertThat(savedKey.getKeyType()).isEqualTo(KeyType.SM4);
        assertThat(savedKey.getKeyLength()).isEqualTo(128);
        assertThat(savedKey.getKeyUsage()).isEqualTo("ENCRYPT");
        assertThat(savedKey.getKeyStatus()).isEqualTo(KeyStatus.ACTIVE);
        assertThat(savedKey.getKeyMaterial()).isNotNull();
        assertThat(savedKey.getCreateTime()).isNotNull();
    }
    
    @Test
    @DisplayName("生成SM2密钥成功")
    void testGenerateSM2KeySuccess() {
        // Given
        KeyGenerationParam param = new KeyGenerationParam();
        param.setKeyType("SM2");
        param.setKeyLength(256);
        param.setKeyUsage("SIGN");
        param.setAlgorithm("SM2");
        
        when(keyMapper.insert(any(EncryptionKey.class))).thenReturn(1);
        
        // When
        Result<Long> result = keyService.generateKey(param);
        
        // Then
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNotNull();
        
        verify(keyMapper, times(1)).insert(any(EncryptionKey.class));
    }
    
    @Test
    @DisplayName("生成密钥-密钥长度无效")
    void testGenerateKeyInvalidLength() {
        // Given
        KeyGenerationParam param = new KeyGenerationParam();
        param.setKeyType("SM4");
        param.setKeyLength(256); // SM4只支持128位
        param.setKeyUsage("ENCRYPT");
        
        // When & Then
        assertThatThrownBy(() -> keyService.generateKey(param))
            .isInstanceOf(KeyGenerationException.class)
            .hasMessageContaining("SM4密钥长度必须是128位");
        
        verify(keyMapper, never()).insert(any());
    }
    
    @Test
    @DisplayName("生成密钥-密钥类型不支持")
    void testGenerateKeyUnsupportedType() {
        // Given
        KeyGenerationParam param = new KeyGenerationParam();
        param.setKeyType("AES"); // 不支持AES
        param.setKeyLength(128);
        param.setKeyUsage("ENCRYPT");
        
        // When & Then
        assertThatThrownBy(() -> keyService.generateKey(param))
            .isInstanceOf(KeyGenerationException.class)
            .hasMessageContaining("不支持的密钥类型");
        
        verify(keyMapper, never()).insert(any());
    }
    
    @Test
    @DisplayName("生成密钥-密钥用途无效")
    void testGenerateKeyInvalidUsage() {
        // Given
        KeyGenerationParam param = new KeyGenerationParam();
        param.setKeyType("SM4");
        param.setKeyLength(128);
        param.setKeyUsage("INVALID"); // 无效用途
        
        // When & Then
        assertThatThrownBy(() -> keyService.generateKey(param))
            .isInstanceOf(KeyGenerationException.class)
            .hasMessageContaining("无效的密钥用途");
        
        verify(keyMapper, never()).insert(any());
    }
    
    @Test
    @DisplayName("轮换密钥成功")
    void testRotateKeySuccess() {
        // Given
        Long oldKeyId = 1L;
        EncryptionKey oldKey = new EncryptionKey();
        oldKey.setId(oldKeyId);
        oldKey.setKeyType(KeyType.SM4);
        oldKey.setKeyStatus(KeyStatus.ACTIVE);
        oldKey.setKeyMaterial("old-key-material");
        oldKey.setCreateTime(LocalDateTime.now().minusDays(30));
        
        when(keyMapper.selectById(oldKeyId)).thenReturn(oldKey);
        when(keyMapper.updateById(any(EncryptionKey.class))).thenReturn(1);
        when(keyMapper.insert(any(EncryptionKey.class))).thenReturn(1);
        
        // When
        Result<Long> result = keyService.rotateKey(oldKeyId);
        
        // Then
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getMessage()).isEqualTo("密钥轮换成功");
        
        // Verify old key is deactivated
        verify(keyMapper, times(1)).updateById(oldKey);
        assertThat(oldKey.getKeyStatus()).isEqualTo(KeyStatus.INACTIVE);
        
        // Verify new key is created
        ArgumentCaptor<EncryptionKey> newKeyCaptor = ArgumentCaptor.forClass(EncryptionKey.class);
        verify(keyMapper, times(1)).insert(newKeyCaptor.capture());
        
        EncryptionKey newKey = newKeyCaptor.getValue();
        assertThat(newKey.getKeyStatus()).isEqualTo(KeyStatus.ACTIVE);
        assertThat(newKey.getKeyType()).isEqualTo(KeyType.SM4);
        assertThat(newKey.getParentKeyId()).isEqualTo(oldKeyId);
    }
    
    @Test
    @DisplayName("轮换密钥-原密钥不存在")
    void testRotateKeyNotFound() {
        // Given
        Long nonExistentKeyId = 999L;
        when(keyMapper.selectById(nonExistentKeyId)).thenReturn(null);
        
        // When & Then
        assertThatThrownBy(() -> keyService.rotateKey(nonExistentKeyId))
            .isInstanceOf(KeyGenerationException.class)
            .hasMessageContaining("密钥不存在");
        
        verify(keyMapper, never()).updateById(any());
        verify(keyMapper, never()).insert(any());
    }
    
    @Test
    @DisplayName("轮换密钥-原密钥已失效")
    void testRotateKeyAlreadyInactive() {
        // Given
        Long keyId = 1L;
        EncryptionKey inactiveKey = new EncryptionKey();
        inactiveKey.setId(keyId);
        inactiveKey.setKeyStatus(KeyStatus.INACTIVE);
        
        when(keyMapper.selectById(keyId)).thenReturn(inactiveKey);
        
        // When & Then
        assertThatThrownBy(() -> keyService.rotateKey(keyId))
            .isInstanceOf(KeyGenerationException.class)
            .hasMessageContaining("密钥已失效，不能轮换");
        
        verify(keyMapper, never()).updateById(any());
        verify(keyMapper, never()).insert(any());
    }
    
    @Test
    @DisplayName("获取密钥成功")
    void testGetKeySuccess() {
        // Given
        Long keyId = 1L;
        EncryptionKey key = new EncryptionKey();
        key.setId(keyId);
        key.setKeyType(KeyType.SM4);
        key.setKeyStatus(KeyStatus.ACTIVE);
        key.setKeyMaterial("test-key-material");
        key.setKeyUsage("ENCRYPT");
        key.setCreateTime(LocalDateTime.now());
        
        when(keyMapper.selectById(keyId)).thenReturn(key);
        
        // When
        Result<EncryptionKey> result = keyService.getKey(keyId);
        
        // Then
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getId()).isEqualTo(keyId);
        assertThat(result.getData().getKeyType()).isEqualTo(KeyType.SM4);
    }
    
    @Test
    @DisplayName("获取密钥-密钥不存在")
    void testGetKeyNotFound() {
        // Given
        Long nonExistentKeyId = 999L;
        when(keyMapper.selectById(nonExistentKeyId)).thenReturn(null);
        
        // When
        Result<EncryptionKey> result = keyService.getKey(nonExistentKeyId);
        
        // Then
        assertThat(result.getCode()).isEqualTo(404);
        assertThat(result.getData()).isNull();
        assertThat(result.getMessage()).contains("密钥不存在");
    }
    
    @Test
    @DisplayName("删除密钥成功")
    void testDeleteKeySuccess() {
        // Given
        Long keyId = 1L;
        EncryptionKey key = new EncryptionKey();
        key.setId(keyId);
        key.setKeyStatus(KeyStatus.INACTIVE); // 只有失效密钥才能删除
        
        when(keyMapper.selectById(keyId)).thenReturn(key);
        when(keyMapper.deleteById(keyId)).thenReturn(1);
        
        // When
        Result<Void> result = keyService.deleteKey(keyId);
        
        // Then
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("密钥删除成功");
        
        verify(keyMapper, times(1)).deleteById(keyId);
    }
    
    @Test
    @DisplayName("删除密钥-密钥处于活跃状态")
    void testDeleteKeyActive() {
        // Given
        Long keyId = 1L;
        EncryptionKey key = new EncryptionKey();
        key.setId(keyId);
        key.setKeyStatus(KeyStatus.ACTIVE); // 活跃密钥不能删除
        
        when(keyMapper.selectById(keyId)).thenReturn(key);
        
        // When & Then
        assertThatThrownBy(() -> keyService.deleteKey(keyId))
            .isInstanceOf(KeyGenerationException.class)
            .hasMessageContaining("活跃密钥不能直接删除");
        
        verify(keyMapper, never()).deleteById(any());
    }
    
    @Test
    @DisplayName("批量生成密钥")
    void testBatchGenerateKeys() {
        // Given
        int batchSize = 5;
        when(keyMapper.insert(any(EncryptionKey.class))).thenReturn(1);
        
        // When
        Result<Void> result = keyService.batchGenerateKeys(validParam, batchSize);
        
        // Then
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).contains("批量生成成功");
        
        verify(keyMapper, times(batchSize)).insert(any(EncryptionKey.class));
    }
    
    @Test
    @DisplayName("获取密钥历史版本")
    void testGetKeyHistory() {
        // Given
        Long currentKeyId = 5L;
        EncryptionKey currentKey = new EncryptionKey();
        currentKey.setId(currentKeyId);
        currentKey.setParentKeyId(4L);
        
        EncryptionKey parentKey = new EncryptionKey();
        parentKey.setId(4L);
        parentKey.setParentKeyId(3L);
        
        EncryptionKey grandParentKey = new EncryptionKey();
        grandParentKey.setId(3L);
        grandParentKey.setParentKeyId(null);
        
        when(keyMapper.selectById(currentKeyId)).thenReturn(currentKey);
        when(keyMapper.selectById(4L)).thenReturn(parentKey);
        when(keyMapper.selectById(3L)).thenReturn(grandParentKey);
        
        // When
        Result<List<EncryptionKey>> result = keyService.getKeyHistory(currentKeyId);
        
        // Then
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).hasSize(3);
        assertThat(result.getData().get(0).getId()).isEqualTo(3L);
        assertThat(result.getData().get(1).getId()).isEqualTo(4L);
        assertThat(result.getData().get(2).getId()).isEqualTo(5L);
    }
}