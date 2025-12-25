package com.bankshield.ai;

import com.bankshield.ai.model.UserBehavior;
import com.bankshield.ai.service.BehaviorAnalysisService;
import com.bankshield.ai.utils.AnomalyDetectionEngine;
import com.bankshield.ai.utils.FeatureExtractor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI模块测试类
 * 
 * @author BankShield
 * @version 1.0.0
 */
@SpringBootTest
public class AnomalyDetectionTest {

    @Autowired
    private BehaviorAnalysisService behaviorAnalysisService;

    @Autowired
    private AnomalyDetectionEngine anomalyDetectionEngine;

    @Autowired
    private FeatureExtractor featureExtractor;

    @Test
    public void testFeatureExtraction() {
        // 创建测试用户行为数据
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(1L);
        behavior.setUsername("test_user");
        behavior.setBehaviorType("login");
        behavior.setOperationTime(LocalDateTime.now());
        behavior.setIpAddress("192.168.1.100");
        behavior.setLocation("北京");
        behavior.setResponseTime(100L);
        behavior.setPermissionLevel(5);

        // 提取特征
        double[] features = featureExtractor.extractFeatures(behavior);

        // 验证特征提取结果
        assertNotNull(features);
        assertTrue(features.length > 0);
        assertFalse(Arrays.stream(features).allMatch(x -> x == 0.0));

        System.out.println("特征提取测试通过，特征维度: " + features.length);
    }

    @Test
    public void testAnomalyDetection() {
        // 创建测试特征向量
        double[] normalFeatures = {0.1, 0.2, 0.3, 0.1, 0.2, 0.3, 0.1, 0.2, 0.3, 0.1, 0.2, 0.3, 0.1, 0.2, 0.3};
        double[] abnormalFeatures = {0.8, 0.9, 0.85, 0.88, 0.92, 0.87, 0.85, 0.9, 0.88, 0.85, 0.87, 0.9, 0.88, 0.85, 0.87};

        // 检测异常
        double normalScore = anomalyDetectionEngine.detectAnomaly(normalFeatures);
        double abnormalScore = anomalyDetectionEngine.detectAnomaly(abnormalFeatures);

        // 验证检测结果
        assertTrue(normalScore >= 0.0 && normalScore <= 1.0);
        assertTrue(abnormalScore >= 0.0 && abnormalScore <= 1.0);
        assertTrue(abnormalScore > normalScore); // 异常特征的分数应该更高

        System.out.println("正常特征异常分数: " + normalScore);
        System.out.println("异常特征异常分数: " + abnormalScore);
        System.out.println("异常检测测试通过");
    }

    @Test
    public void testUserBehaviorPatternLearning() {
        // 创建测试用户行为数据列表
        List<UserBehavior> behaviors = Arrays.asList(
            createNormalBehavior(1L, "user1", LocalDateTime.now().minusDays(1)),
            createNormalBehavior(1L, "user1", LocalDateTime.now().minusDays(2)),
            createNormalBehavior(1L, "user1", LocalDateTime.now().minusDays(3))
        );

        // 学习用户行为模式
        boolean success = behaviorAnalysisService.learnUserBehaviorPattern(1L, behaviors);

        // 验证学习结果
        assertTrue(success);

        System.out.println("用户行为模式学习测试通过");
    }

    @Test
    public void testAnomalyDetectionService() {
        // 创建测试用户行为数据
        UserBehavior normalBehavior = createNormalBehavior(1L, "user1", LocalDateTime.now());
        UserBehavior abnormalBehavior = createAbnormalBehavior(1L, "user1", LocalDateTime.now());

        // 检测异常
        var normalResult = behaviorAnalysisService.detectAnomaly(normalBehavior);
        var abnormalResult = behaviorAnalysisService.detectAnomaly(abnormalBehavior);

        // 验证检测结果
        assertNotNull(normalResult);
        assertNotNull(abnormalResult);
        assertNotNull(normalResult.getAnomalyScore());
        assertNotNull(abnormalResult.getAnomalyScore());

        System.out.println("正常行为异常分数: " + normalResult.getAnomalyScore());
        System.out.println("异常行为异常分数: " + abnormalResult.getAnomalyScore());
        System.out.println("异常行为检测服务测试通过");
    }

    @Test
    public void testBatchAnomalyDetection() {
        // 创建批量测试数据
        List<UserBehavior> behaviors = Arrays.asList(
            createNormalBehavior(1L, "user1", LocalDateTime.now().minusHours(1)),
            createNormalBehavior(2L, "user2", LocalDateTime.now().minusHours(2)),
            createAbnormalBehavior(3L, "user3", LocalDateTime.now().minusHours(3)),
            createNormalBehavior(4L, "user4", LocalDateTime.now().minusHours(4))
        );

        // 批量检测
        var results = behaviorAnalysisService.detectAnomalies(behaviors);

        // 验证检测结果
        assertNotNull(results);
        assertEquals(4, results.size());

        // 验证异常检测结果
        long anomalyCount = results.stream()
                .filter(result -> result.getIsAnomaly() != null && result.getIsAnomaly())
                .count();

        System.out.println("批量检测总数: " + results.size());
        System.out.println("异常数量: " + anomalyCount);
        System.out.println("批量异常检测测试通过");
    }

    @Test
    public void testTimeFeatures() {
        // 测试凌晨登录（应该被视为异常）
        UserBehavior lateNightBehavior = new UserBehavior();
        lateNightBehavior.setUserId(1L);
        lateNightBehavior.setBehaviorType("login");
        lateNightBehavior.setLoginTime(LocalDateTime.now().withHour(3).withMinute(0));
        lateNightBehavior.setOperationTime(LocalDateTime.now().withHour(3).withMinute(0));

        var result = behaviorAnalysisService.detectAnomaly(lateNightBehavior);
        System.out.println("凌晨登录异常分数: " + result.getAnomalyScore());
        System.out.println("凌晨登录异常类型: " + result.getAnomalyTypes());

        // 测试正常工作时间登录
        UserBehavior normalBehavior = new UserBehavior();
        normalBehavior.setUserId(1L);
        normalBehavior.setBehaviorType("login");
        normalBehavior.setLoginTime(LocalDateTime.now().withHour(10).withMinute(0));
        normalBehavior.setOperationTime(LocalDateTime.now().withHour(10).withMinute(0));

        var normalResult = behaviorAnalysisService.detectAnomaly(normalBehavior);
        System.out.println("正常时间登录异常分数: " + normalResult.getAnomalyScore());

        assertTrue(result.getAnomalyScore() > normalResult.getAnomalyScore());
        System.out.println("时间特征检测测试通过");
    }

    @Test
    public void testFrequencyFeatures() {
        // 测试高频率操作（应该被视为异常）
        UserBehavior highFreqBehavior = new UserBehavior();
        highFreqBehavior.setUserId(1L);
        highFreqBehavior.setBehaviorType("operation");
        highFreqBehavior.setOperationFrequency(150.0); // 150次/小时

        var result = behaviorAnalysisService.detectAnomaly(highFreqBehavior);
        System.out.println("高频率操作异常分数: " + result.getAnomalyScore());
        System.out.println("高频率操作异常类型: " + result.getAnomalyTypes());

        assertTrue(result.getAnomalyScore() > 0.3);
        System.out.println("频率特征检测测试通过");
    }

    @Test
    public void testDataAccessFeatures() {
        // 测试大数据量访问（应该被视为异常）
        UserBehavior largeDataBehavior = new UserBehavior();
        largeDataBehavior.setUserId(1L);
        largeDataBehavior.setBehaviorType("download");
        largeDataBehavior.setDataSize(2L * 1024 * 1024 * 1024); // 2GB

        var result = behaviorAnalysisService.detectAnomaly(largeDataBehavior);
        System.out.println("大数据量访问异常分数: " + result.getAnomalyScore());
        System.out.println("大数据量访问异常类型: " + result.getAnomalyTypes());

        assertTrue(result.getAnomalyScore() > 0.2);
        System.out.println("数据访问特征检测测试通过");
    }

    // 辅助方法：创建正常行为数据
    private UserBehavior createNormalBehavior(Long userId, String username, LocalDateTime time) {
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setUsername(username);
        behavior.setBehaviorType("access");
        behavior.setOperationType("read");
        behavior.setOperationTime(time);
        behavior.setLoginTime(time.withHour(10).withMinute(0));
        behavior.setIpAddress("192.168.1.100");
        behavior.setLocation("北京");
        behavior.setResponseTime(100L);
        behavior.setDataSize(1024L);
        behavior.setOperationFrequency(10.0);
        behavior.setPermissionLevel(3);
        behavior.setOperationResult("success");
        return behavior;
    }

    // 辅助方法：创建异常行为数据
    private UserBehavior createAbnormalBehavior(Long userId, String username, LocalDateTime time) {
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setUsername(username);
        behavior.setBehaviorType("login");
        behavior.setOperationTime(time);
        behavior.setLoginTime(time.withHour(3).withMinute(0)); // 凌晨3点登录
        behavior.setIpAddress("10.0.0.1"); // 异常IP
        behavior.setLocation("未知地区");
        behavior.setResponseTime(5000L); // 响应时间异常
        behavior.setOperationResult("failure");
        behavior.setErrorMessage("异常错误信息");
        return behavior;
    }
}