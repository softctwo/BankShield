package com.bankshield.api.controller;

import com.bankshield.api.entity.AlertRule;
import com.bankshield.api.mapper.AlertRuleMapper;
import com.bankshield.api.service.AlertRuleEngine;
import com.bankshield.common.result.Result;
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

@ExtendWith(MockitoExtension.class)
public class AlertRuleControllerTest {

    @Mock
    private AlertRuleMapper alertRuleMapper;

    @Mock
    private AlertRuleEngine alertRuleEngine;

    @InjectMocks
    private AlertRuleController alertRuleController;

    private AlertRule testRule;

    @BeforeEach
    public void setUp() {
        testRule = new AlertRule();
        testRule.setId(1L);
        testRule.setRuleName("CPU使用率告警");
        testRule.setRuleType("THRESHOLD");
        testRule.setAlertLevel("HIGH");
        testRule.setMonitorMetric("cpu_usage");
        testRule.setThreshold(80.0);
        testRule.setEnabled(1);
        testRule.setCreateTime(LocalDateTime.now());
        testRule.setUpdateTime(LocalDateTime.now());
    }

    @Test
    public void testGetAlertRule_Success() {
        when(alertRuleMapper.selectById(1L)).thenReturn(testRule);

        Result<AlertRule> result = alertRuleController.getAlertRule(1L);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("CPU使用率告警", result.getData().getRuleName());
        verify(alertRuleMapper, times(1)).selectById(1L);
    }

    @Test
    public void testGetAlertRule_NotFound() {
        when(alertRuleMapper.selectById(999L)).thenReturn(null);

        Result<AlertRule> result = alertRuleController.getAlertRule(999L);

        assertFalse(result.isSuccess());
        assertEquals("告警规则不存在", result.getMessage());
    }

    @Test
    public void testCreateAlertRule_Success() {
        when(alertRuleEngine.validateRule(any(AlertRule.class))).thenReturn(true);
        when(alertRuleMapper.selectByRuleName(anyString())).thenReturn(null);
        when(alertRuleMapper.insert(any(AlertRule.class))).thenReturn(1);

        Result<String> result = alertRuleController.createAlertRule(testRule);

        assertTrue(result.isSuccess());
        assertEquals("创建告警规则成功", result.getMessage());
        verify(alertRuleMapper, times(1)).insert(any(AlertRule.class));
    }

    @Test
    public void testCreateAlertRule_InvalidRule() {
        when(alertRuleEngine.validateRule(any(AlertRule.class))).thenReturn(false);

        Result<String> result = alertRuleController.createAlertRule(testRule);

        assertFalse(result.isSuccess());
        assertEquals("告警规则配置无效", result.getMessage());
        verify(alertRuleMapper, never()).insert(any(AlertRule.class));
    }

    @Test
    public void testCreateAlertRule_DuplicateName() {
        when(alertRuleEngine.validateRule(any(AlertRule.class))).thenReturn(true);
        when(alertRuleMapper.selectByRuleName(anyString())).thenReturn(testRule);

        Result<String> result = alertRuleController.createAlertRule(testRule);

        assertFalse(result.isSuccess());
        assertEquals("规则名称已存在", result.getMessage());
        verify(alertRuleMapper, never()).insert(any(AlertRule.class));
    }

    @Test
    public void testUpdateAlertRule_Success() {
        when(alertRuleMapper.selectById(1L)).thenReturn(testRule);
        when(alertRuleEngine.validateRule(any(AlertRule.class))).thenReturn(true);
        when(alertRuleMapper.selectByRuleName(anyString())).thenReturn(null);
        when(alertRuleMapper.updateById(any(AlertRule.class))).thenReturn(1);

        Result<String> result = alertRuleController.updateAlertRule(1L, testRule);

        assertTrue(result.isSuccess());
        assertEquals("更新告警规则成功", result.getMessage());
        verify(alertRuleMapper, times(1)).updateById(any(AlertRule.class));
    }

    @Test
    public void testUpdateAlertRule_NotFound() {
        when(alertRuleMapper.selectById(999L)).thenReturn(null);

        Result<String> result = alertRuleController.updateAlertRule(999L, testRule);

        assertFalse(result.isSuccess());
        assertEquals("告警规则不存在", result.getMessage());
        verify(alertRuleMapper, never()).updateById(any(AlertRule.class));
    }

    @Test
    public void testDeleteAlertRule_Success() {
        when(alertRuleMapper.selectById(1L)).thenReturn(testRule);
        when(alertRuleMapper.deleteById(1L)).thenReturn(1);

        Result<String> result = alertRuleController.deleteAlertRule(1L);

        assertTrue(result.isSuccess());
        assertEquals("删除告警规则成功", result.getMessage());
        verify(alertRuleMapper, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteAlertRule_NotFound() {
        when(alertRuleMapper.selectById(999L)).thenReturn(null);

        Result<String> result = alertRuleController.deleteAlertRule(999L);

        assertFalse(result.isSuccess());
        assertEquals("告警规则不存在", result.getMessage());
        verify(alertRuleMapper, never()).deleteById(anyLong());
    }

    @Test
    public void testToggleAlertRule_Enable() {
        when(alertRuleMapper.selectById(1L)).thenReturn(testRule);
        when(alertRuleMapper.updateById(any(AlertRule.class))).thenReturn(1);

        Result<String> result = alertRuleController.toggleAlertRule(1L, 1);

        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("启用"));
        verify(alertRuleMapper, times(1)).updateById(any(AlertRule.class));
    }

    @Test
    public void testToggleAlertRule_Disable() {
        when(alertRuleMapper.selectById(1L)).thenReturn(testRule);
        when(alertRuleMapper.updateById(any(AlertRule.class))).thenReturn(1);

        Result<String> result = alertRuleController.toggleAlertRule(1L, 0);

        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("禁用"));
    }

    @Test
    public void testBatchToggleAlertRules_Success() {
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        when(alertRuleMapper.batchUpdateEnabled(anyList(), anyInt())).thenReturn(3);

        Result<String> result = alertRuleController.batchToggleAlertRules(ids, 1);

        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("批量"));
        verify(alertRuleMapper, times(1)).batchUpdateEnabled(ids, 1);
    }

    @Test
    public void testGetEnabledAlertRules_Success() {
        List<AlertRule> enabledRules = Arrays.asList(testRule);
        when(alertRuleEngine.loadEnabledRules()).thenReturn(enabledRules);

        Result<List<AlertRule>> result = alertRuleController.getEnabledAlertRules();

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        verify(alertRuleEngine, times(1)).loadEnabledRules();
    }
}
