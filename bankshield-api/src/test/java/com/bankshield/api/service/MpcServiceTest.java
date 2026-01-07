package com.bankshield.api.service;

import com.bankshield.api.entity.MpcJob;
import com.bankshield.api.entity.MpcParty;
import com.bankshield.api.service.impl.MpcServiceImpl;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MPC多方安全计算服务测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MPC服务测试")
class MpcServiceTest {

    @InjectMocks
    private MpcServiceImpl mpcService;

    private MpcParty testParty;

    @BeforeEach
    void setUp() {
        testParty = new MpcParty();
        testParty.setPartyName("测试参与方A");
        testParty.setEndpoint("grpc://localhost:9000");
        testParty.setPublicKey("test-public-key");
    }

    // ========== PSI测试 ==========

    @Test
    @DisplayName("创建PSI任务")
    void testCreatePSIJob() {
        List<String> participantIds = List.of("party1", "party2");
        List<String> localDataSet = List.of("user1", "user2", "user3");

        MpcJob result = mpcService.createPSIJob("PSI测试任务", participantIds, localDataSet);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("PSI", result.getJobType());
    }

    @Test
    @DisplayName("执行PSI计算")
    void testExecutePSI() {
        List<String> participantIds = List.of("party1", "party2");
        List<String> localDataSet = List.of("user1", "user2", "user3");
        MpcJob job = mpcService.createPSIJob("PSI测试任务", participantIds, localDataSet);

        Map<String, Object> result = mpcService.executePSI(job.getId());

        assertNotNull(result);
    }

    // ========== 安全求和测试 ==========

    @Test
    @DisplayName("创建安全求和任务")
    void testCreateSecureSumJob() {
        List<String> participantIds = List.of("party1", "party2", "party3");

        MpcJob result = mpcService.createSecureSumJob("安全求和测试", participantIds, 100.0);

        assertNotNull(result);
        assertEquals("SECURE_SUM", result.getJobType());
    }

    @Test
    @DisplayName("执行安全求和")
    void testExecuteSecureSum() {
        List<String> participantIds = List.of("party1", "party2");
        MpcJob job = mpcService.createSecureSumJob("安全求和测试", participantIds, 100.0);

        Map<String, Object> result = mpcService.executeSecureSum(job.getId());

        assertNotNull(result);
    }

    // ========== 联合查询测试 ==========

    @Test
    @DisplayName("创建联合查询任务")
    void testCreateJointQueryJob() {
        List<String> participantIds = List.of("party1", "party2");
        Map<String, Object> queryParams = Map.of("table", "users", "condition", "age > 30");

        MpcJob result = mpcService.createJointQueryJob("联合查询测试", participantIds, queryParams);

        assertNotNull(result);
        assertEquals("JOINT_QUERY", result.getJobType());
    }

    @Test
    @DisplayName("执行联合查询")
    void testExecuteJointQuery() {
        List<String> participantIds = List.of("party1", "party2");
        Map<String, Object> queryParams = Map.of("table", "users");
        MpcJob job = mpcService.createJointQueryJob("联合查询测试", participantIds, queryParams);

        Map<String, Object> result = mpcService.executeJointQuery(job.getId());

        assertNotNull(result);
    }

    // ========== 安全比较测试 ==========

    @Test
    @DisplayName("创建安全比较任务")
    void testCreateSecureCompareJob() {
        MpcJob result = mpcService.createSecureCompareJob("安全比较测试", "party2", 1000.0);

        assertNotNull(result);
        assertEquals("SECURE_COMPARE", result.getJobType());
    }

    @Test
    @DisplayName("执行安全比较")
    void testExecuteSecureCompare() {
        MpcJob job = mpcService.createSecureCompareJob("安全比较测试", "party2", 1000.0);

        Map<String, Object> result = mpcService.executeSecureCompare(job.getId());

        assertNotNull(result);
    }

    // ========== 任务管理测试 ==========

    @Test
    @DisplayName("分页查询任务列表")
    void testGetJobs() {
        IPage<MpcJob> page = mpcService.getJobs(1, 10, null, null);

        assertNotNull(page);
        assertTrue(page.getTotal() >= 0);
    }

    @Test
    @DisplayName("根据ID获取任务")
    void testGetJobById() {
        List<String> participantIds = List.of("party1", "party2");
        MpcJob created = mpcService.createPSIJob("测试任务", participantIds, List.of("data1"));

        MpcJob found = mpcService.getJobById(created.getId());

        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
    }

    @Test
    @DisplayName("取消任务")
    void testCancelJob() {
        List<String> participantIds = List.of("party1", "party2");
        MpcJob created = mpcService.createPSIJob("测试任务", participantIds, List.of("data1"));

        boolean result = mpcService.cancelJob(created.getId());

        assertTrue(result);
    }

    @Test
    @DisplayName("重试任务")
    void testRetryJob() {
        List<String> participantIds = List.of("party1", "party2");
        MpcJob created = mpcService.createPSIJob("测试任务", participantIds, List.of("data1"));

        MpcJob result = mpcService.retryJob(created.getId());

        assertNotNull(result);
    }

    // ========== 参与方管理测试 ==========

    @Test
    @DisplayName("注册参与方")
    void testRegisterParty() {
        MpcParty result = mpcService.registerParty(testParty);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getPartyId());
        assertEquals("ONLINE", result.getStatus());
    }

    @Test
    @DisplayName("获取参与方列表")
    void testGetParties() {
        mpcService.registerParty(testParty);

        List<MpcParty> parties = mpcService.getParties(null);

        assertNotNull(parties);
    }

    @Test
    @DisplayName("获取在线参与方")
    void testGetOnlineParties() {
        mpcService.registerParty(testParty);

        List<MpcParty> parties = mpcService.getParties("ONLINE");

        assertNotNull(parties);
    }

    @Test
    @DisplayName("更新参与方状态")
    void testUpdatePartyStatus() {
        MpcParty party = mpcService.registerParty(testParty);

        boolean result = mpcService.updatePartyStatus(party.getId(), "OFFLINE");

        assertTrue(result);
    }

    @Test
    @DisplayName("删除参与方")
    void testDeleteParty() {
        MpcParty party = mpcService.registerParty(testParty);

        boolean result = mpcService.deleteParty(party.getId());

        assertTrue(result);
    }

    @Test
    @DisplayName("发送心跳")
    void testSendHeartbeat() {
        MpcParty party = mpcService.registerParty(testParty);

        boolean result = mpcService.sendHeartbeat(party.getPartyId());

        assertTrue(result);
    }

    // ========== 统计测试 ==========

    @Test
    @DisplayName("获取统计信息")
    void testGetStatistics() {
        Map<String, Object> stats = mpcService.getStatistics();

        assertNotNull(stats);
        assertTrue(stats.containsKey("totalJobs"));
    }

    @Test
    @DisplayName("获取协议信息")
    void testGetProtocols() {
        Map<String, Object> protocols = mpcService.getProtocols();

        assertNotNull(protocols);
    }

    @Test
    @DisplayName("健康检查")
    void testHealthCheck() {
        Map<String, Object> health = mpcService.healthCheck();

        assertNotNull(health);
        assertEquals("UP", health.get("status"));
    }
}
