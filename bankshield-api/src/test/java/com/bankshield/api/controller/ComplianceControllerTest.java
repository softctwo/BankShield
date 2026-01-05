package com.bankshield.api.controller;

import com.bankshield.api.dto.ComplianceStatisticsDTO;
import com.bankshield.api.entity.ComplianceCheckTask;
import com.bankshield.api.entity.ComplianceRule;
import com.bankshield.api.service.ComplianceService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 合规性检查控制器集成测试
 */
@WebMvcTest(ComplianceController.class)
class ComplianceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ComplianceService complianceService;

    private ComplianceRule testRule;
    private ComplianceCheckTask testTask;
    private ComplianceStatisticsDTO testStatistics;

    @BeforeEach
    void setUp() {
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

        testStatistics = new ComplianceStatisticsDTO();
        testStatistics.setTotalRules(100);
        testStatistics.setComplianceScore(85);
        testStatistics.setPassedChecks(80);
        testStatistics.setCriticalRiskCount(5);
    }

    @Test
    void testGetRulesByPage() throws Exception {
        // Given
        Page<ComplianceRule> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testRule));
        page.setTotal(1);

        when(complianceService.getRulesByPage(anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/compliance/rules")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void testCreateRule() throws Exception {
        // Given
        when(complianceService.createRule(any(ComplianceRule.class)))
                .thenReturn(testRule);

        // When & Then
        mockMvc.perform(post("/api/compliance/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRule)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.ruleCode").value("GDPR_001"));
    }

    @Test
    void testGetRuleById() throws Exception {
        // Given
        when(complianceService.getRuleById(1L)).thenReturn(testRule);

        // When & Then
        mockMvc.perform(get("/api/compliance/rules/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.ruleCode").value("GDPR_001"));
    }

    @Test
    void testUpdateRule() throws Exception {
        // Given
        when(complianceService.updateRule(any(ComplianceRule.class)))
                .thenReturn(true);

        // When & Then
        mockMvc.perform(put("/api/compliance/rules/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRule)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testDeleteRule() throws Exception {
        // Given
        when(complianceService.deleteRule(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/compliance/rules/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testCreateTask() throws Exception {
        // Given
        when(complianceService.createTask(any(ComplianceCheckTask.class)))
                .thenReturn(testTask);

        // When & Then
        mockMvc.perform(post("/api/compliance/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTask)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.taskName").value("全量合规检查"));
    }

    @Test
    void testExecuteTask() throws Exception {
        // Given
        when(complianceService.executeCheckTask(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/compliance/tasks/1/execute")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testGetStatistics() throws Exception {
        // Given
        when(complianceService.getStatistics()).thenReturn(testStatistics);

        // When & Then
        mockMvc.perform(get("/api/compliance/statistics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalRules").value(100))
                .andExpect(jsonPath("$.data.complianceScore").value(85));
    }
}
