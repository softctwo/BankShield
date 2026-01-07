package com.bankshield.api.controller;

import com.bankshield.api.entity.FederatedLearningJob;
import com.bankshield.api.entity.FederatedParty;
import com.bankshield.api.service.FederatedLearningService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 联邦学习控制器测试
 */
@WebMvcTest(FederatedLearningController.class)
@DisplayName("联邦学习Controller测试")
class FederatedLearningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FederatedLearningService federatedLearningService;

    private FederatedLearningJob testJob;
    private FederatedParty testParty;

    @BeforeEach
    void setUp() {
        testJob = new FederatedLearningJob();
        testJob.setId(1L);
        testJob.setJobName("测试联邦学习任务");
        testJob.setJobType("HORIZONTAL_FL");
        testJob.setModelType("LR");
        testJob.setStatus("PENDING");
        testJob.setTotalRounds(50);

        testParty = new FederatedParty();
        testParty.setId(1L);
        testParty.setPartyId("FL_PARTY_001");
        testParty.setPartyName("测试参与方");
        testParty.setStatus("ONLINE");
    }

    @Test
    @DisplayName("创建联邦学习任务")
    void testCreateJob() throws Exception {
        when(federatedLearningService.createJob(any())).thenReturn(testJob);

        mockMvc.perform(post("/api/federated/job")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testJob)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取任务列表")
    void testGetJobs() throws Exception {
        mockMvc.perform(get("/api/federated/jobs")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("获取任务详情")
    void testGetJobById() throws Exception {
        when(federatedLearningService.getJobById(1L)).thenReturn(testJob);

        mockMvc.perform(get("/api/federated/job/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("启动任务")
    void testStartJob() throws Exception {
        when(federatedLearningService.startJob(1L)).thenReturn(true);

        mockMvc.perform(post("/api/federated/job/1/start"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("暂停任务")
    void testPauseJob() throws Exception {
        when(federatedLearningService.pauseJob(1L)).thenReturn(true);

        mockMvc.perform(post("/api/federated/job/1/pause"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("恢复任务")
    void testResumeJob() throws Exception {
        when(federatedLearningService.resumeJob(1L)).thenReturn(true);

        mockMvc.perform(post("/api/federated/job/1/resume"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("停止任务")
    void testStopJob() throws Exception {
        when(federatedLearningService.stopJob(1L)).thenReturn(true);

        mockMvc.perform(post("/api/federated/job/1/stop"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("删除任务")
    void testDeleteJob() throws Exception {
        when(federatedLearningService.deleteJob(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/federated/job/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("初始化全局模型")
    void testInitGlobalModel() throws Exception {
        when(federatedLearningService.initGlobalModel(1L))
                .thenReturn(Map.of("modelVersion", "v0", "weights", List.of(0.1, 0.2)));

        mockMvc.perform(post("/api/federated/job/1/init-model"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("获取训练轮次")
    void testGetRounds() throws Exception {
        when(federatedLearningService.getRounds(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/federated/job/1/rounds"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("获取全局模型")
    void testGetGlobalModel() throws Exception {
        when(federatedLearningService.getGlobalModel(1L))
                .thenReturn(Map.of("modelVersion", "v1", "accuracy", 0.95));

        mockMvc.perform(get("/api/federated/job/1/global-model"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("注册参与方")
    void testRegisterParty() throws Exception {
        when(federatedLearningService.registerParty(any())).thenReturn(testParty);

        mockMvc.perform(post("/api/federated/party/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testParty)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("获取参与方列表")
    void testGetParties() throws Exception {
        when(federatedLearningService.getParties(null)).thenReturn(List.of(testParty));

        mockMvc.perform(get("/api/federated/parties"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("获取参与方详情")
    void testGetPartyById() throws Exception {
        when(federatedLearningService.getPartyById(1L)).thenReturn(testParty);

        mockMvc.perform(get("/api/federated/party/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("更新参与方状态")
    void testUpdatePartyStatus() throws Exception {
        when(federatedLearningService.updatePartyStatus(1L, "OFFLINE")).thenReturn(true);

        mockMvc.perform(put("/api/federated/party/1/status")
                .param("status", "OFFLINE"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("删除参与方")
    void testDeleteParty() throws Exception {
        when(federatedLearningService.deleteParty(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/federated/party/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("发送心跳")
    void testSendHeartbeat() throws Exception {
        when(federatedLearningService.sendHeartbeat("FL_PARTY_001")).thenReturn(true);

        mockMvc.perform(post("/api/federated/party/FL_PARTY_001/heartbeat"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("配置差分隐私")
    void testConfigureDifferentialPrivacy() throws Exception {
        when(federatedLearningService.configureDifferentialPrivacy(eq(1L), any())).thenReturn(true);

        mockMvc.perform(post("/api/federated/job/1/privacy-config")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"epsilon\": 1.0, \"delta\": 1e-5}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("验证模型完整性")
    void testVerifyModelIntegrity() throws Exception {
        when(federatedLearningService.verifyModelIntegrity(1L))
                .thenReturn(Map.of("integrityValid", true, "checksum", "abc123"));

        mockMvc.perform(get("/api/federated/job/1/verify-integrity"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("获取统计信息")
    void testGetStatistics() throws Exception {
        when(federatedLearningService.getStatistics())
                .thenReturn(Map.of("totalJobs", 10, "completedJobs", 8));

        mockMvc.perform(get("/api/federated/statistics"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("获取任务监控数据")
    void testGetJobMonitoring() throws Exception {
        when(federatedLearningService.getJobMonitoring(1L))
                .thenReturn(Map.of("status", "TRAINING", "currentRound", 25));

        mockMvc.perform(get("/api/federated/job/1/monitoring"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("获取训练曲线")
    void testGetTrainingCurve() throws Exception {
        when(federatedLearningService.getTrainingCurve(1L))
                .thenReturn(Map.of("dataPoints", List.of()));

        mockMvc.perform(get("/api/federated/job/1/training-curve"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("健康检查")
    void testHealthCheck() throws Exception {
        when(federatedLearningService.healthCheck())
                .thenReturn(Map.of("status", "UP"));

        mockMvc.perform(get("/api/federated/health"))
                .andExpect(status().isOk());
    }
}
