package com.bankshield.api.service;

import com.bankshield.api.entity.SecurityScanResult;
import com.bankshield.api.entity.SecurityScanTask;
import com.bankshield.api.enums.RiskLevel;
import com.bankshield.api.service.impl.SecurityScanEngineImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 安全扫描引擎确定性测试
 * 验证修复后的引擎不再使用Math.random()，相同输入产生相同输出
 */
@SpringBootTest
public class SecurityScanEngineDeterministicTest {

    @Autowired
    private SecurityScanEngine securityScanEngine;

    private SecurityScanTask testTask;

    @BeforeEach
    void setUp() {
        testTask = new SecurityScanTask();
        testTask.setId(1L);
        testTask.setTaskName("测试扫描任务");
        testTask.setScanTarget("http://test.example.com");
        testTask.setScanType("VULNERABILITY");
        testTask.setTaskStatus("RUNNING");
        testTask.setCreateTime(LocalDateTime.now());
        testTask.setUpdateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("验证漏洞扫描结果的可重复性 - 多次运行结果相同")
    void testVulnerabilityScanDeterministic() {
        // 第一次扫描
        List<SecurityScanResult> results1 = securityScanEngine.performVulnerabilityScan(testTask);
        
        // 第二次扫描
        List<SecurityScanResult> results2 = securityScanEngine.performVulnerabilityScan(testTask);
        
        // 验证结果数量相同
        assertEquals(results1.size(), results2.size(), 
            "两次扫描结果数量应该相同，证明没有使用随机数");
        
        // 验证具体结果内容相同
        for (int i = 0; i < results1.size(); i++) {
            SecurityScanResult result1 = results1.get(i);
            SecurityScanResult result2 = results2.get(i);
            
            assertEquals(result1.getRiskType(), result2.getRiskType(), 
                "风险类型应该相同");
            assertEquals(result1.getRiskLevel(), result2.getRiskLevel(), 
                "风险等级应该相同");
            assertEquals(result1.getRiskDescription(), result2.getRiskDescription(), 
                "风险描述应该相同");
        }
    }

    @Test
    @DisplayName("验证弱密码检查结果的可重复性")
    void testWeakPasswordCheckDeterministic() {
        testTask.setScanTarget("192.168.1.1,192.168.1.2,192.168.1.3");
        
        // 第一次检查
        List<SecurityScanResult> results1 = securityScanEngine.performWeakPasswordCheck(testTask);
        
        // 第二次检查
        List<SecurityScanResult> results2 = securityScanEngine.performWeakPasswordCheck(testTask);
        
        // 验证结果相同
        assertEquals(results1.size(), results2.size(), 
            "两次弱密码检查结果数量应该相同");
        
        // 验证具体结果
        for (int i = 0; i < results1.size(); i++) {
            SecurityScanResult result1 = results1.get(i);
            SecurityScanResult result2 = results2.get(i);
            
            assertEquals(result1.getRiskType(), result2.getRiskType());
            assertEquals(result1.getImpactScope(), result2.getImpactScope());
        }
    }

    @Test
    @DisplayName("验证异常行为检测结果的可重复性")
    void testAnomalyDetectionDeterministic() {
        // 第一次检测
        List<SecurityScanResult> results1 = securityScanEngine.performAnomalyDetection(testTask);
        
        // 第二次检测
        List<SecurityScanResult> results2 = securityScanEngine.performAnomalyDetection(testTask);
        
        // 验证结果相同
        assertEquals(results1.size(), results2.size(), 
            "两次异常行为检测结果数量应该相同");
        
        // 验证具体结果
        for (int i = 0; i < results1.size(); i++) {
            SecurityScanResult result1 = results1.get(i);
            SecurityScanResult result2 = results2.get(i);
            
            assertEquals(result1.getRiskType(), result2.getRiskType());
            assertEquals(result1.getRiskLevel(), result2.getRiskLevel());
        }
    }

    @RepeatedTest(5)
    @DisplayName("重复测试验证确定性 - 5次运行结果完全相同")
    void testRepeatedScansAreIdentical() {
        List<SecurityScanResult> firstResults = null;
        
        for (int i = 0; i < 5; i++) {
            List<SecurityScanResult> currentResults = securityScanEngine.performVulnerabilityScan(testTask);
            
            if (firstResults == null) {
                firstResults = currentResults;
            } else {
                // 验证每次结果都相同
                assertEquals(firstResults.size(), currentResults.size(), 
                    "第" + (i + 1) + "次扫描结果数量与第一次不同");
                
                for (int j = 0; j < firstResults.size(); j++) {
                    SecurityScanResult first = firstResults.get(j);
                    SecurityScanResult current = currentResults.get(j);
                    
                    assertEquals(first.getRiskType(), current.getRiskType(), 
                        "第" + (i + 1) + "次扫描风险类型与第一次不同");
                    assertEquals(first.getRiskLevel(), current.getRiskLevel(), 
                        "第" + (i + 1) + "次扫描风险等级与第一次不同");
                }
            }
        }
    }

    @Test
    @DisplayName("验证修复验证方法的确定性")
    void testVerificationDeterministic() {
        SecurityScanResult testResult = new SecurityScanResult();
        testResult.setId(1L);
        testResult.setTaskId(testTask.getId());
        testResult.setRiskType("SQL_INJECTION");
        testResult.setRiskLevel(RiskLevel.HIGH.name());
        testResult.setRiskDescription("SQL注入漏洞");
        testResult.setImpactScope("http://test.example.com");
        
        // 多次验证修复结果
        boolean verify1 = securityScanEngine.verifyFix(testResult);
        boolean verify2 = securityScanEngine.verifyFix(testResult);
        boolean verify3 = securityScanEngine.verifyFix(testResult);
        
        // 验证结果应该一致
        assertEquals(verify1, verify2, "修复验证结果应该一致");
        assertEquals(verify2, verify3, "修复验证结果应该一致");
    }

    @Test
    @DisplayName("验证不同输入产生不同但确定的结果")
    void testDifferentInputsProduceDifferentResults() {
        SecurityScanTask task1 = new SecurityScanTask();
        task1.setId(1L);
        task1.setTaskName("任务1");
        task1.setScanTarget("http://site1.example.com");
        task1.setScanType("VULNERABILITY");
        task1.setTaskStatus("RUNNING");
        task1.setCreateTime(LocalDateTime.now());
        task1.setUpdateTime(LocalDateTime.now());
        
        SecurityScanTask task2 = new SecurityScanTask();
        task2.setId(2L);
        task2.setTaskName("任务2");
        task2.setScanTarget("http://site2.example.com");
        task2.setScanType("VULNERABILITY");
        task2.setTaskStatus("RUNNING");
        task2.setCreateTime(LocalDateTime.now());
        task2.setUpdateTime(LocalDateTime.now());
        
        List<SecurityScanResult> results1 = securityScanEngine.performVulnerabilityScan(task1);
        List<SecurityScanResult> results2 = securityScanEngine.performVulnerabilityScan(task2);
        
        // 不同输入应该产生不同结果（但各自都是确定的）
        assertNotEquals(results1.size(), results2.size(), 
            "不同目标应该产生不同数量的漏洞检测结果");
    }

    @Test
    @DisplayName("验证不再使用Math.random() - 通过结果模式分析")
    void testNoRandomUsage() {
        List<SecurityScanResult> results = securityScanEngine.performVulnerabilityScan(testTask);
        
        // 验证结果不是随机分布的
        // 如果使用了Math.random()，结果数量应该呈现随机分布
        // 但我们期望的是确定性结果
        
        int totalResults = results.size();
        
        // 多次运行应该得到完全相同的结果数量
        for (int i = 0; i < 10; i++) {
            List<SecurityScanResult> newResults = securityScanEngine.performVulnerabilityScan(testTask);
            assertEquals(totalResults, newResults.size(), 
                "第" + (i + 1) + "次运行结果数量与第一次不同，可能使用了随机数");
        }
        
        // 验证高风险漏洞的检测是确定的
        long highRiskCount = results.stream()
            .filter(r -> RiskLevel.HIGH.name().equals(r.getRiskLevel()))
            .count();
        
        for (int i = 0; i < 5; i++) {
            List<SecurityScanResult> newResults = securityScanEngine.performVulnerabilityScan(testTask);
            long newHighRiskCount = newResults.stream()
                .filter(r -> RiskLevel.HIGH.name().equals(r.getRiskLevel()))
                .count();
            assertEquals(highRiskCount, newHighRiskCount, 
                "高风险漏洞数量应该保持一致");
        }
    }

    @Test
    @DisplayName("验证基于哈希的确定性算法工作正常")
    void testDeterministicHashAlgorithm() {
        // 测试相同输入产生相同哈希
        String input1 = "http://test.example.com|admin|password123";
        String input2 = "http://test.example.com|admin|password123";
        
        // 通过弱密码检查验证确定性
        testTask.setScanTarget("http://test.example.com");
        
        List<SecurityScanResult> results1 = securityScanEngine.performWeakPasswordCheck(testTask);
        List<SecurityScanResult> results2 = securityScanEngine.performWeakPasswordCheck(testTask);
        
        // 相同输入应该产生完全相同的结果
        assertEquals(results1.size(), results2.size(), 
            "相同输入应该产生相同数量的弱密码检测结果");
    }
}