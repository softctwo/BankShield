package com.bankshield.api.service;

import com.bankshield.api.dto.ComplianceStatisticsDTO;
import com.bankshield.api.entity.ComplianceCheckTask;
import com.bankshield.api.entity.ComplianceReport;
import com.bankshield.api.entity.ComplianceRule;
import com.bankshield.api.mapper.ComplianceCheckResultMapper;
import com.bankshield.api.mapper.ComplianceCheckTaskMapper;
import com.bankshield.api.mapper.ComplianceReportMapper;
import com.bankshield.api.mapper.ComplianceRuleMapper;
import com.bankshield.api.service.impl.ComplianceServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 合规性检查服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class ComplianceServiceTest {

    @Mock
    private ComplianceRuleMapper ruleMapper;

    @Mock
    private ComplianceCheckTaskMapper taskMapper;

    @Mock
    private ComplianceCheckResultMapper resultMapper;

    @Mock
    private ComplianceReportMapper reportMapper;

    @InjectMocks
    private ComplianceServiceImpl complianceService;

    private ComplianceRule testRule;
    private ComplianceCheckTask testTask;
    private ComplianceReport testReport;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testRule = new ComplianceRule();
        testRule.setId(1L);
        testRule.setRuleCode("GDPR_001");
        testRule.setRuleName("数据处理合法性基础");
        testRule.setRuleCategory("数据处理");
        testRule.setComplianceStandard("GDPR");
        testRule.setSeverity("HIGH");
        testRule.setStatus("ACTIVE");

        testTask = new ComplianceCheckTask();
        testTask.setId(1L);
        testTask.setTaskName("全量合规检查");
        testTask.setTaskType("FULL");
        testTask.setComplianceStandard("GDPR");
        testTask.setTaskStatus("PENDING");

        testReport = new ComplianceReport();
        testReport.setId(1L);
        testReport.setTaskId(1L);
        testReport.setReportName("合规检查报告");
        testReport.setReportType("SUMMARY");
        testReport.setComplianceScore(85);
    }

    @Test
    void testCreateRule() {
        // Given
        when(ruleMapper.insert(any(ComplianceRule.class))).thenReturn(1);

        // When
        ComplianceRule result = complianceService.createRule(testRule);

        // Then
        assertNotNull(result);
        verify(ruleMapper, times(1)).insert(any(ComplianceRule.class));
    }

    @Test
    void testGetRuleById() {
        // Given
        when(ruleMapper.selectById(1L)).thenReturn(testRule);

        // When
        ComplianceRule result = complianceService.getRuleById(1L);

        // Then
        assertNotNull(result);
        assertEquals("GDPR_001", result.getRuleCode());
        assertEquals("数据处理合法性基础", result.getRuleName());
        verify(ruleMapper, times(1)).selectById(1L);
    }

    @Test
    void testGetRulesByPage() {
        // Given
        Page<ComplianceRule> page = new Page<>(1, 10);
        List<ComplianceRule> rules = Arrays.asList(testRule);
        page.setRecords(rules);
        page.setTotal(1);

        when(ruleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        // When
        Page<ComplianceRule> result = complianceService.getRulesByPage(1, 10, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        verify(ruleMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void testUpdateRule() {
        // Given
        when(ruleMapper.updateById(any(ComplianceRule.class))).thenReturn(1);

        // When
        boolean result = complianceService.updateRule(testRule);

        // Then
        assertTrue(result);
        verify(ruleMapper, times(1)).updateById(any(ComplianceRule.class));
    }

    @Test
    void testDeleteRule() {
        // Given
        when(ruleMapper.deleteById(1L)).thenReturn(1);

        // When
        boolean result = complianceService.deleteRule(1L);

        // Then
        assertTrue(result);
        verify(ruleMapper, times(1)).deleteById(1L);
    }

    @Test
    void testCreateTask() {
        // Given
        when(taskMapper.insert(any(ComplianceCheckTask.class))).thenReturn(1);

        // When
        ComplianceCheckTask result = complianceService.createTask(testTask);

        // Then
        assertNotNull(result);
        verify(taskMapper, times(1)).insert(any(ComplianceCheckTask.class));
    }

    @Test
    void testGetTaskById() {
        // Given
        when(taskMapper.selectById(1L)).thenReturn(testTask);

        // When
        ComplianceCheckTask result = complianceService.getTaskById(1L);

        // Then
        assertNotNull(result);
        assertEquals("全量合规检查", result.getTaskName());
        verify(taskMapper, times(1)).selectById(1L);
    }

    @Test
    void testGenerateReport() {
        // Given
        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(reportMapper.insert(any(ComplianceReport.class))).thenReturn(1);

        // When
        ComplianceReport result = complianceService.generateReport(1L, "SUMMARY");

        // Then
        assertNotNull(result);
        verify(taskMapper, times(1)).selectById(1L);
        verify(reportMapper, times(1)).insert(any(ComplianceReport.class));
    }

    @Test
    void testGetStatistics() {
        // Given
        when(ruleMapper.selectCount(any())).thenReturn(100L);
        when(taskMapper.selectCount(any())).thenReturn(50L);

        // When
        ComplianceStatisticsDTO result = complianceService.getStatistics();

        // Then
        assertNotNull(result);
        assertTrue(result.getTotalRules() >= 0);
        assertTrue(result.getComplianceScore() >= 0 && result.getComplianceScore() <= 100);
    }

    @Test
    void testExecuteCheckTask_Success() {
        // Given
        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(taskMapper.updateById(any(ComplianceCheckTask.class))).thenReturn(1);

        // When
        boolean result = complianceService.executeCheckTask(1L);

        // Then
        assertTrue(result);
        verify(taskMapper, times(1)).selectById(1L);
        verify(taskMapper, atLeastOnce()).updateById(any(ComplianceCheckTask.class));
    }

    @Test
    void testExecuteCheckTask_TaskNotFound() {
        // Given
        when(taskMapper.selectById(1L)).thenReturn(null);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            complianceService.executeCheckTask(1L);
        });
        verify(taskMapper, times(1)).selectById(1L);
    }

    @Test
    void testStopCheckTask() {
        // Given
        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(taskMapper.updateById(any(ComplianceCheckTask.class))).thenReturn(1);

        // When
        boolean result = complianceService.stopCheckTask(1L);

        // Then
        assertTrue(result);
        verify(taskMapper, times(1)).selectById(1L);
        verify(taskMapper, times(1)).updateById(any(ComplianceCheckTask.class));
    }
}
