package com.bankshield.encrypt.service;

import com.bankshield.common.result.PageResult;
import com.bankshield.common.result.Result;
import com.bankshield.common.test.BaseUnitTest;
import com.bankshield.encrypt.entity.EncryptionKey;
import com.bankshield.encrypt.entity.KeyRotationHistory;
import com.bankshield.encrypt.enums.KeyStatus;
import com.bankshield.encrypt.enums.KeyType;
import com.bankshield.encrypt.enums.KeyUsage;
import com.bankshield.encrypt.mapper.EncryptionKeyMapper;
import com.bankshield.encrypt.mapper.KeyRotationHistoryMapper;
import com.bankshield.encrypt.mapper.KeyUsageAuditMapper;
import com.bankshield.encrypt.service.impl.KeyManagementServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 密钥管理服务单元测试
 * 
 * @author BankShield
 */
@DisplayName("密钥管理服务单元测试")
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class KeyManagementServiceUnitTest extends BaseUnitTest {
    
    @Mock
    private EncryptionKeyMapper keyMapper;
    
    @Mock
    private KeyRotationHistoryMapper rotationHistoryMapper;
    
    @Mock
    private KeyUsageAuditMapper usageAuditMapper;
    
    @InjectMocks
    private KeyManagementServiceImpl keyService;
    
    @Test
    @Order(1)
    @DisplayName("生成SM4密钥-成功")
    void testGenerateSM4KeySuccess() {
        // Given
        String keyName = "test_sm4_key";
        KeyType keyType = KeyType.SM4;
        KeyUsage keyUsage = KeyUsage.ENCRYPT;
        Integer keyLength = 128;
        Integer expireDays = 365;
        Integer rotationCycle = 90;
        String description = "测试SM4密钥";
        String createdBy = "admin";
        
        when(keyMapper.insert(any(EncryptionKey.class))).thenAnswer(invocation -> {
            EncryptionKey key = invocation.getArgument(0);
            key.setId(1L);
            return 1;
        });
        
        // When
        Result<EncryptionKey> result = keyService.generateKey(keyName, keyType, keyUsage, 
                                                             keyLength, expireDays, rotationCycle,
                                                             description, createdBy);
        
        // Then
        assertResultSuccess(result);
        assertNotNull(result.getData());
        assertEquals(keyName, result.getData().getKeyName());
        assertEquals(keyType.name(), result.getData().getKeyType());
        assertEquals(keyUsage.name(), result.getData().getKeyUsage());
        assertEquals(keyLength, result.getData().getKeyLength());
        assertEquals(KeyStatus.ACTIVE.name(), result.getData().getKeyStatus());
        
        verify(keyMapper, times(1)).insert(any(EncryptionKey.class));
    }
    
    @Test
    @Order(2)
    @DisplayName("生成RSA密钥-成功")
    void testGenerateRSAKeySuccess() {
        // Given
        String keyName = "test_rsa_key";
        KeyType keyType = KeyType.RSA;
        KeyUsage keyUsage = KeyUsage.SIGN;
        Integer keyLength = 2048;
        Integer expireDays = 730;
        Integer rotationCycle = 180;
        String description = "测试RSA密钥";
        String createdBy = "admin";
        
        when(keyMapper.insert(any(EncryptionKey.class))).thenAnswer(invocation -> {
            EncryptionKey key = invocation.getArgument(0);
            key.setId(2L);
            return 1;
        });
        
        // When
        Result<EncryptionKey> result = keyService.generateKey(keyName, keyType, keyUsage, 
                                                             keyLength, expireDays, rotationCycle,
                                                             description, createdBy);
        
        // Then
        assertResultSuccess(result);
        assertNotNull(result.getData());
        assertEquals(keyName, result.getData().getKeyName());
        assertEquals(keyType.name(), result.getData().getKeyType());
        assertEquals(keyUsage.name(), result.getData().getKeyUsage());
        assertEquals(keyLength, result.getData().getKeyLength());
        
        verify(keyMapper, times(1)).insert(any(EncryptionKey.class));
    }
    
    @Test
    @Order(3)
    @DisplayName("分页查询密钥列表")
    void testGetKeyList() {
        // Given
        Integer pageNum = 1;
        Integer pageSize = 10;
        String keyName = "test";
        String keyType = "SM4";
        String keyStatus = "ACTIVE";
        String keyUsage = "ENCRYPT";
        
        Page<EncryptionKey> mockPage = new Page<>(pageNum, pageSize);
        List<EncryptionKey> keyList = Arrays.asList(
            createTestKey("test_key_1"),
            createTestKey("test_key_2")
        );
        mockPage.setRecords(keyList);
        mockPage.setTotal(2);
        
        when(keyMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);
        
        // When
        PageResult<EncryptionKey> result = keyService.getKeyList(pageNum, pageSize, keyName, 
                                                               keyType, keyStatus, keyUsage);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotal());
        assertEquals(2, result.getList().size());
        
        verify(keyMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }
    
    @Test
    @Order(4)
    @DisplayName("获取密钥详情-成功")
    void testGetKeyDetailSuccess() {
        // Given
        Long keyId = 1L;
        EncryptionKey mockKey = createTestKey("test_key");
        mockKey.setId(keyId);
        
        when(keyMapper.selectById(keyId)).thenReturn(mockKey);
        
        // When
        Result<EncryptionKey> result = keyService.getKeyDetail(keyId);
        
        // Then
        assertResultSuccess(result);
        assertNotNull(result.getData());
        assertEquals(keyId, result.getData().getId());
        assertEquals("test_key", result.getData().getKeyName());
        
        verify(keyMapper, times(1)).selectById(keyId);
    }
    
    @Test
    @Order(5)
    @DisplayName("获取密钥详情-密钥不存在")
    void testGetKeyDetailNotFound() {
        // Given
        Long keyId = 999L;
        when(keyMapper.selectById(keyId)).thenReturn(null);
        
        // When
        Result<EncryptionKey> result = keyService.getKeyDetail(keyId);
        
        // Then
        assertResultError(result, 500);
        assertEquals("密钥不存在", result.getMessage());
        
        verify(keyMapper, times(1)).selectById(keyId);
    }
    
    @Test
    @Order(6)
    @DisplayName("更新密钥状态-成功")
    void testUpdateKeyStatusSuccess() {
        // Given
        Long keyId = 1L;
        KeyStatus newStatus = KeyStatus.DISABLED;
        String operator = "admin";
        
        EncryptionKey existingKey = createTestKey("test_key");
        existingKey.setId(keyId);
        existingKey.setKeyStatus(KeyStatus.ACTIVE.name());
        
        when(keyMapper.selectById(keyId)).thenReturn(existingKey);
        when(keyMapper.updateById(any(EncryptionKey.class))).thenReturn(1);
        
        // When
        Result<Void> result = keyService.updateKeyStatus(keyId, newStatus, operator);
        
        // Then
        assertResultSuccess(result);
        assertEquals("密钥状态更新成功", result.getMessage());
        
        verify(keyMapper, times(1)).selectById(keyId);
        verify(keyMapper, times(1)).updateById(any(EncryptionKey.class));
    }
    
    @Test
    @Order(7)
    @DisplayName("更新密钥状态-密钥不存在")
    void testUpdateKeyStatusKeyNotFound() {
        // Given
        Long keyId = 999L;
        KeyStatus newStatus = KeyStatus.DISABLED;
        String operator = "admin";
        
        when(keyMapper.selectById(keyId)).thenReturn(null);
        
        // When
        Result<Void> result = keyService.updateKeyStatus(keyId, newStatus, operator);
        
        // Then
        assertResultError(result, 500);
        assertEquals("密钥不存在", result.getMessage());
        
        verify(keyMapper, times(1)).selectById(keyId);
        verify(keyMapper, never()).updateById(any(EncryptionKey.class));
    }
    
    @Test
    @Order(8)
    @DisplayName("密钥轮换-成功")
    void testRotateKeySuccess() {
        // Given
        Long oldKeyId = 1L;
        String rotationReason = "定期轮换";
        String operator = "admin";
        
        EncryptionKey oldKey = createTestKey("old_key");
        oldKey.setId(oldKeyId);
        oldKey.setKeyStatus(KeyStatus.ACTIVE.name());
        
        when(keyMapper.selectById(oldKeyId)).thenReturn(oldKey);
        when(keyMapper.insert(any(EncryptionKey.class))).thenAnswer(invocation -> {
            EncryptionKey newKey = invocation.getArgument(0);
            newKey.setId(2L);
            return 1;
        });
        when(keyMapper.updateById(any(EncryptionKey.class))).thenReturn(1);
        when(rotationHistoryMapper.insert(any(KeyRotationHistory.class))).thenReturn(1);
        
        // When
        Result<EncryptionKey> result = keyService.rotateKey(oldKeyId, rotationReason, operator);
        
        // Then
        assertResultSuccess(result);
        assertNotNull(result.getData());
        assertEquals(2L, result.getData().getId());
        assertEquals("old_key_rotated", result.getData().getKeyName());
        
        verify(keyMapper, times(1)).selectById(oldKeyId);
        verify(keyMapper, times(1)).insert(any(EncryptionKey.class));
        verify(keyMapper, times(1)).updateById(any(EncryptionKey.class));
        verify(rotationHistoryMapper, times(1)).insert(any(KeyRotationHistory.class));
    }
    
    @Test
    @Order(9)
    @DisplayName("密钥轮换-原密钥不存在")
    void testRotateKeyOriginalNotFound() {
        // Given
        Long oldKeyId = 999L;
        String rotationReason = "定期轮换";
        String operator = "admin";
        
        when(keyMapper.selectById(oldKeyId)).thenReturn(null);
        
        // When
        Result<EncryptionKey> result = keyService.rotateKey(oldKeyId, rotationReason, operator);
        
        // Then
        assertResultError(result, 500);
        assertEquals("原密钥不存在", result.getMessage());
        
        verify(keyMapper, times(1)).selectById(oldKeyId);
        verify(keyMapper, never()).insert(any(EncryptionKey.class));
        verify(keyMapper, never()).updateById(any(EncryptionKey.class));
        verify(rotationHistoryMapper, never()).insert(any(KeyRotationHistory.class));
    }
    
    @Test
    @Order(10)
    @DisplayName("销毁密钥-成功")
    void testDestroyKeySuccess() {
        // Given
        Long keyId = 1L;
        String operator = "admin";
        
        EncryptionKey existingKey = createTestKey("test_key");
        existingKey.setId(keyId);
        existingKey.setKeyStatus(KeyStatus.ACTIVE.name());
        
        when(keyMapper.selectById(keyId)).thenReturn(existingKey);
        when(keyMapper.updateById(any(EncryptionKey.class))).thenReturn(1);
        
        // When
        Result<Void> result = keyService.destroyKey(keyId, operator);
        
        // Then
        assertResultSuccess(result);
        assertEquals("密钥销毁成功", result.getMessage());
        
        verify(keyMapper, times(1)).selectById(keyId);
        verify(keyMapper, times(1)).updateById(any(EncryptionKey.class));
    }
    
    @Test
    @Order(11)
    @DisplayName("销毁密钥-密钥不存在")
    void testDestroyKeyNotFound() {
        // Given
        Long keyId = 999L;
        String operator = "admin";
        
        when(keyMapper.selectById(keyId)).thenReturn(null);
        
        // When
        Result<Void> result = keyService.destroyKey(keyId, operator);
        
        // Then
        assertResultError(result, 500);
        assertEquals("密钥不存在", result.getMessage());
        
        verify(keyMapper, times(1)).selectById(keyId);
        verify(keyMapper, never()).updateById(any(EncryptionKey.class));
    }
    
    @Test
    @Order(12)
    @DisplayName("获取密钥统计信息")
    void testGetKeyStatistics() {
        // Given
        Map<String, Object> mockStatistics = new HashMap<>();
        mockStatistics.put("totalCount", 100);
        mockStatistics.put("activeCount", 80);
        mockStatistics.put("expiredCount", 10);
        mockStatistics.put("disabledCount", 5);
        mockStatistics.put("rotatingCount", 5);
        
        when(keyMapper.getKeyStatistics()).thenReturn(mockStatistics);
        
        // When
        Result<Map<String, Object>> result = keyService.getKeyStatistics();
        
        // Then
        assertResultSuccess(result);
        assertNotNull(result.getData());
        assertEquals(100, result.getData().get("totalCount"));
        assertEquals(80, result.getData().get("activeCount"));
        
        verify(keyMapper, times(1)).getKeyStatistics();
    }
    
    @Test
    @Order(13)
    @DisplayName("获取支持的密钥类型")
    void testGetSupportedKeyTypes() {
        // Given
        List<Map<String, String>> mockKeyTypes = Arrays.asList(
            createKeyTypeMap("SM4", "SM4国密算法", "对称加密"),
            createKeyTypeMap("RSA", "RSA算法", "非对称加密"),
            createKeyTypeMap("ECC", "椭圆曲线算法", "非对称加密"),
            createKeyTypeMap("AES", "AES算法", "对称加密")
        );
        
        // When
        Result<List<Map<String, String>>> result = keyService.getSupportedKeyTypes();
        
        // Then
        assertResultSuccess(result);
        assertNotNull(result.getData());
        assertTrue(result.getData().size() > 0);
        
        // 验证返回的数据结构
        Map<String, String> firstType = result.getData().get(0);
        assertTrue(firstType.containsKey("value"));
        assertTrue(firstType.containsKey("label"));
        assertTrue(firstType.containsKey("category"));
    }
    
    @Test
    @Order(14)
    @DisplayName("获取密钥使用统计")
    void testGetKeyUsageStatistics() {
        // Given
        Long keyId = 1L;
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();
        
        Map<String, Object> mockUsageStats = new HashMap<>();
        mockUsageStats.put("totalUsageCount", 1000);
        mockUsageStats.put("dailyUsageData", Arrays.asList(
            createDailyUsageData(LocalDateTime.now().minusDays(6), 150),
            createDailyUsageData(LocalDateTime.now().minusDays(5), 120),
            createDailyUsageData(LocalDateTime.now().minusDays(4), 180)
        ));
        mockUsageStats.put("usageByType", Arrays.asList(
            createUsageByType("ENCRYPT", 600),
            createUsageByType("DECRYPT", 400)
        ));
        
        when(usageAuditMapper.getKeyUsageStatistics(eq(keyId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(mockUsageStats);
        
        // When
        Result<Map<String, Object>> result = keyService.getKeyUsageStatistics(keyId, startTime, endTime);
        
        // Then
        assertResultSuccess(result);
        assertNotNull(result.getData());
        assertEquals(1000, result.getData().get("totalUsageCount"));
        assertNotNull(result.getData().get("dailyUsageData"));
        assertNotNull(result.getData().get("usageByType"));
        
        verify(usageAuditMapper, times(1)).getKeyUsageStatistics(eq(keyId), any(LocalDateTime.class), any(LocalDateTime.class));
    }
    
    @Test
    @Order(15)
    @DisplayName("导出密钥信息")
    void testExportKeyInfo() {
        // Given
        List<Long> keyIds = Arrays.asList(1L, 2L, 3L);
        String mockFilePath = "/tmp/key_export_20240101.xlsx";
        
        List<EncryptionKey> mockKeys = Arrays.asList(
            createTestKey("key1"),
            createTestKey("key2"),
            createTestKey("key3")
        );
        
        when(keyMapper.selectBatchIds(keyIds)).thenReturn(mockKeys);
        // 这里应该mock Excel导出工具类，但为了简化测试，直接返回成功结果
        
        // When
        Result<String> result = keyService.exportKeyInfo(keyIds);
        
        // Then
        assertResultSuccess(result);
        assertNotNull(result.getData());
        
        verify(keyMapper, times(1)).selectBatchIds(keyIds);
    }
    
    @Test
    @Order(16)
    @DisplayName("导出密钥信息-导出所有")
    void testExportKeyInfoAll() {
        // Given
        List<EncryptionKey> mockKeys = Arrays.asList(
            createTestKey("key1"),
            createTestKey("key2")
        );
        
        when(keyMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(mockKeys);
        
        // When
        Result<String> result = keyService.exportKeyInfo(null);
        
        // Then
        assertResultSuccess(result);
        assertNotNull(result.getData());
        
        verify(keyMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }
    
    // 辅助方法
    private EncryptionKey createTestKey(String keyName) {
        EncryptionKey key = new EncryptionKey();
        key.setId(randomLong());
        key.setKeyName(keyName);
        key.setKeyType("SM4");
        key.setKeyLength(128);
        key.setKeyUsage("ENCRYPT");
        key.setKeyStatus("ACTIVE");
        key.setKeyMaterial("test_material_" + keyName);
        key.setKeyFingerprint("test_fingerprint_" + keyName);
        key.setExpireTime(LocalDateTime.now().plusYears(1));
        key.setCreateTime(LocalDateTime.now());
        key.setUpdateTime(LocalDateTime.now());
        return key;
    }
    
    private Map<String, String> createKeyTypeMap(String value, String label, String category) {
        Map<String, String> map = new HashMap<>();
        map.put("value", value);
        map.put("label", label);
        map.put("category", category);
        return map;
    }
    
    private Map<String, Object> createDailyUsageData(LocalDateTime date, int count) {
        Map<String, Object> data = new HashMap<>();
        data.put("date", date);
        data.put("count", count);
        return data;
    }
    
    private Map<String, Object> createUsageByType(String type, int count) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", type);
        data.put("count", count);
        return data;
    }
}