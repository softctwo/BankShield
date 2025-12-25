package com.bankshield.api.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.DataMaskingRule;
import com.bankshield.api.enums.MaskingAlgorithm;
import com.bankshield.api.enums.MaskingScenario;
import com.bankshield.api.enums.SensitiveDataType;
import com.bankshield.api.mapper.DataMaskingRuleMapper;
import com.bankshield.common.core.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 数据脱敏规则服务测试类
 */
@ExtendWith(MockitoExtension.class)
class DataMaskingRuleServiceTest {

    @Mock
    private DataMaskingRuleMapper maskingRuleMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DataMaskingRuleService maskingRuleService;

    private DataMaskingRule validRule;

    @BeforeEach
    void setUp() {
        // 初始化有效的脱敏规则
        validRule = DataMaskingRule.builder()
                .ruleName("测试规则")
                .sensitiveDataType(SensitiveDataType.PHONE.getCode())
                .maskingAlgorithm(MaskingAlgorithm.PARTIAL_MASK.getCode())
                .algorithmParams("{\"keepPrefix\": 3, \"keepSuffix\": 4, \"maskChar\": \"*\"}")
                .applicableScenarios("DISPLAY,EXPORT,QUERY")
                .description("测试描述")
                .build();
    }

    @Test
    void testCreateRule_Success() {
        // 准备数据
        String currentUser = "testUser";
        
        // 模拟Mapper行为
        when(maskingRuleMapper.countByRuleName(validRule.getRuleName(), -1L)).thenReturn(0);

        // 执行测试
        Result<String> result = maskingRuleService.createRule(validRule, currentUser);

        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("创建成功", result.getData());
        
        // 验证Mapper调用
        verify(maskingRuleMapper, times(1)).insert(validRule);
        assertEquals(currentUser, validRule.getCreatedBy());
        assertNotNull(validRule.getCreateTime());
        assertNotNull(validRule.getUpdateTime());
        assertTrue(validRule.getEnabled());
    }

    @Test
    void testCreateRule_DuplicateName() {
        // 准备数据
        String currentUser = "testUser";
        
        // 模拟Mapper行为：规则名称已存在
        when(maskingRuleMapper.countByRuleName(validRule.getRuleName(), -1L)).thenReturn(1);

        // 执行测试
        Result<String> result = maskingRuleService.createRule(validRule, currentUser);

        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("规则名称已存在", result.getMessage());
        
        // 验证Mapper未调用insert
        verify(maskingRuleMapper, never()).insert(any());
    }

    @Test
    void testCreateRule_InvalidData() {
        // 准备数据：空规则名称
        DataMaskingRule invalidRule = DataMaskingRule.builder()
                .sensitiveDataType(SensitiveDataType.PHONE.getCode())
                .maskingAlgorithm(MaskingAlgorithm.PARTIAL_MASK.getCode())
                .build();

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            maskingRuleService.createRule(invalidRule, "testUser");
        });
    }

    @Test
    void testUpdateRule_Success() {
        // 准备数据
        Long ruleId = 1L;
        String currentUser = "testUser";
        validRule.setId(ruleId);
        
        // 模拟Mapper行为
        when(maskingRuleMapper.countByRuleName(validRule.getRuleName(), ruleId)).thenReturn(0);
        DataMaskingRule existingRule = DataMaskingRule.builder()
                .id(ruleId)
                .ruleName("旧规则名称")
                .build();
        when(maskingRuleMapper.selectById(ruleId)).thenReturn(existingRule);

        // 执行测试
        Result<String> result = maskingRuleService.updateRule(validRule, currentUser);

        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("更新成功", result.getData());
        
        // 验证字段更新
        assertEquals(validRule.getRuleName(), existingRule.getRuleName());
        assertNotNull(existingRule.getUpdateTime());
    }

    @Test
    void testUpdateRule_NotFound() {
        // 准备数据
        Long ruleId = 1L;
        validRule.setId(ruleId);
        
        // 模拟Mapper行为：规则不存在
        when(maskingRuleMapper.countByRuleName(validRule.getRuleName(), ruleId)).thenReturn(0);
        when(maskingRuleMapper.selectById(ruleId)).thenReturn(null);

        // 执行测试
        Result<String> result = maskingRuleService.updateRule(validRule, "testUser");

        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("规则不存在", result.getMessage());
    }

    @Test
    void testDeleteRule_Success() {
        // 准备数据
        Long ruleId = 1L;
        DataMaskingRule existingRule = DataMaskingRule.builder()
                .id(ruleId)
                .ruleName("测试规则")
                .build();
        
        // 模拟Mapper行为
        when(maskingRuleMapper.selectById(ruleId)).thenReturn(existingRule);

        // 执行测试
        Result<String> result = maskingRuleService.deleteRule(ruleId);

        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("删除成功", result.getData());
        
        // 验证Mapper调用
        verify(maskingRuleMapper, times(1)).deleteById(ruleId);
    }

    @Test
    void testDeleteRule_NotFound() {
        // 准备数据
        Long ruleId = 1L;
        
        // 模拟Mapper行为：规则不存在
        when(maskingRuleMapper.selectById(ruleId)).thenReturn(null);

        // 执行测试
        Result<String> result = maskingRuleService.deleteRule(ruleId);

        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("规则不存在", result.getMessage());
        
        // 验证Mapper未调用delete
        verify(maskingRuleMapper, never()).deleteById(any());
    }

    @Test
    void testUpdateRuleStatus_Success() {
        // 准备数据
        Long ruleId = 1L;
        Boolean newStatus = false;
        DataMaskingRule existingRule = DataMaskingRule.builder()
                .id(ruleId)
                .ruleName("测试规则")
                .enabled(true)
                .build();
        
        // 模拟Mapper行为
        when(maskingRuleMapper.selectById(ruleId)).thenReturn(existingRule);

        // 执行测试
        Result<String> result = maskingRuleService.updateRuleStatus(ruleId, newStatus);

        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("禁用成功", result.getData());
        
        // 验证状态更新
        assertEquals(newStatus, existingRule.getEnabled());
        assertNotNull(existingRule.getUpdateTime());
    }

    @Test
    void testGetRulesBySensitiveType() {
        // 准备数据
        String sensitiveType = SensitiveDataType.PHONE.getCode();
        String scenario = MaskingScenario.DISPLAY.getCode();
        List<DataMaskingRule> expectedRules = Arrays.asList(validRule);
        
        // 模拟Mapper行为
        when(maskingRuleMapper.selectEnabledRulesByType(sensitiveType, scenario))
                .thenReturn(expectedRules);

        // 执行测试
        List<DataMaskingRule> result = maskingRuleService.getRulesBySensitiveType(sensitiveType, scenario);

        // 验证结果
        assertEquals(expectedRules, result);
    }

    @Test
    void testGetSensitiveDataTypes() {
        // 执行测试
        List<String> result = maskingRuleService.getSensitiveDataTypes();

        // 验证结果
        assertNotNull(result);
        assertTrue(result.size() > 0);
        assertTrue(result.contains(SensitiveDataType.PHONE.getCode()));
        assertTrue(result.contains(SensitiveDataType.ID_CARD.getCode()));
    }

    @Test
    void testGetMaskingAlgorithms() {
        // 执行测试
        List<String> result = maskingRuleService.getMaskingAlgorithms();

        // 验证结果
        assertNotNull(result);
        assertTrue(result.size() > 0);
        assertTrue(result.contains(MaskingAlgorithm.PARTIAL_MASK.getCode()));
        assertTrue(result.contains(MaskingAlgorithm.HASH.getCode()));
    }

    @Test
    void testGetMaskingScenarios() {
        // 执行测试
        List<String> result = maskingRuleService.getMaskingScenarios();

        // 验证结果
        assertNotNull(result);
        assertTrue(result.size() > 0);
        assertTrue(result.contains(MaskingScenario.DISPLAY.getCode()));
        assertTrue(result.contains(MaskingScenario.EXPORT.getCode()));
    }
}