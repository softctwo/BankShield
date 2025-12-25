package com.bankshield.ai.service.impl;

import com.bankshield.ai.entity.AiFeature;
import com.bankshield.ai.entity.BehaviorPattern;
import com.bankshield.ai.mapper.AiFeatureMapper;
import com.bankshield.ai.mapper.BehaviorPatternMapper;
import com.bankshield.ai.model.AnomalyResult;
import com.bankshield.ai.model.UserBehavior;
import com.bankshield.ai.service.BehaviorAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 行为分析服务实现类
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class BehaviorAnalysisServiceImpl implements BehaviorAnalysisService {

    @Autowired
    private AiFeatureMapper aiFeatureMapper;

    @Autowired
    private BehaviorPatternMapper behaviorPatternMapper;

    @Override
    public AnomalyResult detectAnomaly(UserBehavior behavior) {
        try {
            log.info("开始检测异常行为，用户ID: {}, 行为类型: {}", behavior.getUserId(), behavior.getBehaviorType());
            
            // 计算异常分数（简化实现）
            double anomalyScore = calculateAnomalyScore(behavior);
            
            // 判断是否为异常
            boolean isAnomaly = anomalyScore > 0.7;
            AnomalyResult.AnomalyLevel anomalyLevel = AnomalyResult.AnomalyLevel.fromScore(anomalyScore);
            
            // 确定异常类型
            List<String> anomalyTypes = determineAnomalyTypes(behavior, anomalyScore);
            
            // 生成异常描述
            String anomalyDescription = generateAnomalyDescription(behavior, anomalyTypes, anomalyScore);
            
            // 生成建议措施
            List<String> recommendations = generateRecommendations(anomalyTypes, anomalyLevel);
            
            // 构建结果
            AnomalyResult result = new AnomalyResult();
            result.setUserId(behavior.getUserId());
            result.setSessionId(behavior.getSessionId());
            result.setBehaviorType(behavior.getBehaviorType());
            result.setAnomalyScore(anomalyScore);
            result.setIsAnomaly(isAnomaly);
            result.setAnomalyLevel(anomalyLevel.getDescription());
            result.setAnomalyThreshold(0.7);
            result.setDetectionTime(LocalDateTime.now());
            result.setDetectionModel("RuleBased_v1.0");
            result.setFeatureVector(Arrays.stream(new double[]{anomalyScore}).boxed().collect(Collectors.toList()));
            result.setAnomalyTypes(anomalyTypes);
            result.setAnomalyDescription(anomalyDescription);
            result.setRecommendations(recommendations);
            result.setRiskScore(calculateRiskScore(anomalyScore, behavior));
            result.setConfidence(anomalyScore);
            result.setNeedAlert(isAnomaly && anomalyScore > 0.8);
            result.setAlertMessage(isAnomaly ? "检测到异常行为，请及时处理" : "行为正常");
            result.setIpAddress(behavior.getIpAddress());
            result.setLocation(behavior.getLocation());
            
            // 保存特征数据
            saveFeatureData(behavior, new double[]{anomalyScore}, anomalyScore, isAnomaly);
            
            log.info("异常行为检测完成，用户ID: {}, 异常分数: {}, 是否异常: {}", 
                    behavior.getUserId(), anomalyScore, isAnomaly);
            
            return result;
            
        } catch (Exception e) {
            log.error("异常行为检测失败，用户ID: " + behavior.getUserId(), e);
            throw new RuntimeException("异常行为检测失败", e);
        }
    }

    @Override
    public List<AnomalyResult> detectAnomalies(List<UserBehavior> behaviors) {
        return behaviors.stream()
                .map(this::detectAnomaly)
                .collect(Collectors.toList());
    }

    @Override
    public boolean learnUserBehaviorPattern(Long userId, List<UserBehavior> behaviors) {
        try {
            log.info("开始学习用户行为模式，用户ID: {}, 行为数量: {}", userId, behaviors.size());
            
            // 简化实现：只更新行为模式记录
            updateBehaviorPattern(userId, behaviors);
            log.info("用户行为模式学习完成，用户ID: {}", userId);
            
            return true;
            
        } catch (Exception e) {
            log.error("学习用户行为模式失败，用户ID: " + userId, e);
            return false;
        }
    }

    @Override
    public BehaviorPattern getUserBehaviorPattern(Long userId) {
        List<BehaviorPattern> patterns = behaviorPatternMapper.selectByUserId(userId, null);
        return patterns.isEmpty() ? null : patterns.get(0);
    }

    @Override
    public boolean updateUserBehaviorPattern(Long userId, UserBehavior behavior) {
        try {
            // 获取现有行为模式
            BehaviorPattern pattern = getUserBehaviorPattern(userId);
            
            if (pattern == null) {
                // 创建新的行为模式
                pattern = new BehaviorPattern();
                pattern.setUserId(userId);
                pattern.setUsername(behavior.getUsername());
                pattern.setPatternType("behavior");
                pattern.setPatternData("{}");
                pattern.setConfidence(0.5);
                pattern.setStrength(0.5);
                pattern.setSampleCount(1);
                pattern.setFirstSeenTime(LocalDateTime.now());
                pattern.setLastSeenTime(LocalDateTime.now());
                pattern.setIsActive(true);
                
                behaviorPatternMapper.insert(pattern);
            } else {
                // 更新现有行为模式
                pattern.setLastSeenTime(LocalDateTime.now());
                pattern.setSampleCount(pattern.getSampleCount() + 1);
                pattern.setIsActive(true);
                
                behaviorPatternMapper.updateById(pattern);
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("更新用户行为模式失败，用户ID: " + userId, e);
            return false;
        }
    }

    @Override
    public double calculateAnomalyScore(UserBehavior behavior) {
        double score = 0.0;
        
        // 登录时间异常检测
        if (isLoginTimeAnomaly(behavior)) {
            score += 0.3;
        }
        
        // 操作频率异常检测
        if (isFrequencyAnomaly(behavior)) {
            score += 0.4;
        }
        
        // 权限使用异常检测
        if (isPermissionAnomaly(behavior)) {
            score += 0.2;
        }
        
        // 数据访问异常检测
        if (isDataAccessAnomaly(behavior)) {
            score += 0.3;
        }
        
        return Math.min(score, 1.0);
    }

    @Override
    public double[] extractFeatures(UserBehavior behavior) {
        return new double[]{calculateAnomalyScore(behavior)};
    }

    @Override
    public AnomalyStatistics getAnomalyStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        // 查询用户的特征数据
        List<AiFeature> features = aiFeatureMapper.selectByUserIdAndTimeRange(userId, startTime, endTime);
        
        AnomalyStatistics statistics = new AnomalyStatistics();
        statistics.setTotalBehaviors((long) features.size());
        
        // 统计异常行为
        long anomalyBehaviors = features.stream()
                .filter(AiFeature::getIsAnomaly)
                .count();
        statistics.setAnomalyBehaviors(anomalyBehaviors);
        
        // 计算异常率
        double anomalyRate = features.isEmpty() ? 0.0 : (double) anomalyBehaviors / features.size();
        statistics.setAnomalyRate(anomalyRate);
        
        // 按类型统计
        Map<String, Long> anomalyTypeCounts = features.stream()
                .filter(AiFeature::getIsAnomaly)
                .collect(Collectors.groupingBy(AiFeature::getBehaviorType, Collectors.counting()));
        statistics.setAnomalyTypeCounts(anomalyTypeCounts);
        
        // 计算各类型异常率
        Map<String, Double> anomalyTypeRates = new HashMap<>();
        Map<String, Long> totalTypeCounts = features.stream()
                .collect(Collectors.groupingBy(AiFeature::getBehaviorType, Collectors.counting()));
        
        for (Map.Entry<String, Long> entry : anomalyTypeCounts.entrySet()) {
            String type = entry.getKey();
            Long anomalyCount = entry.getValue();
            Long totalCount = totalTypeCounts.getOrDefault(type, 0L);
            double rate = totalCount == 0 ? 0.0 : (double) anomalyCount / totalCount;
            anomalyTypeRates.put(type, rate);
        }
        statistics.setAnomalyTypeRates(anomalyTypeRates);
        
        return statistics;
    }

    /**
     * 确定异常类型
     */
    private List<String> determineAnomalyTypes(UserBehavior behavior, double anomalyScore) {
        List<String> anomalyTypes = new ArrayList<>();
        
        // 登录时间异常检测
        if (isLoginTimeAnomaly(behavior)) {
            anomalyTypes.add(AnomalyResult.AnomalyType.LOGIN_TIME_ANOMALY.getDescription());
        }
        
        // 操作频率异常检测
        if (isFrequencyAnomaly(behavior)) {
            anomalyTypes.add(AnomalyResult.AnomalyType.FREQUENCY_ANOMALY.getDescription());
        }
        
        // 权限使用异常检测
        if (isPermissionAnomaly(behavior)) {
            anomalyTypes.add(AnomalyResult.AnomalyType.PERMISSION_ANOMALY.getDescription());
        }
        
        // 数据访问异常检测
        if (isDataAccessAnomaly(behavior)) {
            anomalyTypes.add(AnomalyResult.AnomalyType.DATA_ACCESS_ANOMALY.getDescription());
        }
        
        return anomalyTypes;
    }

    /**
     * 检测登录时间异常
     */
    private boolean isLoginTimeAnomaly(UserBehavior behavior) {
        if (behavior.getLoginTime() == null) {
            return false;
        }
        
        int hour = behavior.getLoginTime().getHour();
        // 凌晨2-5点登录视为异常
        return hour >= 2 && hour <= 5;
    }

    /**
     * 检测操作频率异常
     */
    private boolean isFrequencyAnomaly(UserBehavior behavior) {
        if (behavior.getOperationFrequency() == null) {
            return false;
        }
        
        // 操作频率超过100次/小时视为异常
        return behavior.getOperationFrequency() > 100;
    }

    /**
     * 检测权限使用异常
     */
    private boolean isPermissionAnomaly(UserBehavior behavior) {
        if (behavior.getPermissionLevel() == null || behavior.getSensitivityLevel() == null) {
            return false;
        }
        
        // 权限级别与敏感度级别不匹配视为异常
        return behavior.getPermissionLevel() < behavior.getSensitivityLevel();
    }

    /**
     * 检测数据访问异常
     */
    private boolean isDataAccessAnomaly(UserBehavior behavior) {
        if (behavior.getDataSize() == null) {
            return false;
        }
        
        // 数据量超过1GB视为异常
        return behavior.getDataSize() > 1024 * 1024 * 1024;
    }

    /**
     * 生成异常描述
     */
    private String generateAnomalyDescription(UserBehavior behavior, List<String> anomalyTypes, double anomalyScore) {
        StringBuilder description = new StringBuilder();
        description.append("检测到异常行为，异常分数: ").append(String.format("%.2f", anomalyScore)).append("。");
        
        if (!anomalyTypes.isEmpty()) {
            description.append("异常类型: ").append(String.join(", ", anomalyTypes)).append("。");
        }
        
        description.append("用户: ").append(behavior.getUsername()).append("，");
        description.append("行为: ").append(behavior.getBehaviorType()).append("，");
        description.append("时间: ").append(behavior.getOperationTime());
        
        return description.toString();
    }

    /**
     * 生成建议措施
     */
    private List<String> generateRecommendations(List<String> anomalyTypes, AnomalyResult.AnomalyLevel anomalyLevel) {
        List<String> recommendations = new ArrayList<>();
        
        if (anomalyLevel == AnomalyResult.AnomalyLevel.HIGH) {
            recommendations.add("立即锁定用户账户，进行安全审查");
            recommendations.add("通知安全团队进行紧急处理");
            recommendations.add("记录详细日志，保存证据");
        } else if (anomalyLevel == AnomalyResult.AnomalyLevel.MEDIUM) {
            recommendations.add("增加用户行为监控频率");
            recommendations.add("发送安全提醒给用户");
            recommendations.add("分析用户近期行为模式");
        } else {
            recommendations.add("继续监控用户行为");
            recommendations.add("更新用户行为模式模型");
        }
        
        // 根据具体异常类型添加建议
        if (anomalyTypes.contains(AnomalyResult.AnomalyType.LOGIN_TIME_ANOMALY.getDescription())) {
            recommendations.add("建议用户启用双因素认证");
        }
        
        if (anomalyTypes.contains(AnomalyResult.AnomalyType.FREQUENCY_ANOMALY.getDescription())) {
            recommendations.add("检查用户操作是否正常业务需求");
        }
        
        return recommendations;
    }

    /**
     * 计算风险评分
     */
    private double calculateRiskScore(double anomalyScore, UserBehavior behavior) {
        double riskScore = anomalyScore;
        
        // 根据行为类型调整风险评分
        if ("login".equals(behavior.getBehaviorType())) {
            riskScore *= 1.2;
        } else if ("data_access".equals(behavior.getBehaviorType())) {
            riskScore *= 1.1;
        }
        
        // 根据权限级别调整风险评分
        if (behavior.getPermissionLevel() != null && behavior.getPermissionLevel() > 5) {
            riskScore *= 1.3;
        }
        
        return Math.min(riskScore, 1.0);
    }

    /**
     * 保存特征数据
     */
    private void saveFeatureData(UserBehavior behavior, double[] features, double anomalyScore, boolean isAnomaly) {
        try {
            AiFeature feature = new AiFeature();
            feature.setUserId(behavior.getUserId());
            feature.setSessionId(behavior.getSessionId());
            feature.setBehaviorType(behavior.getBehaviorType());
            feature.setIpAddress(behavior.getIpAddress());
            feature.setLocation(behavior.getLocation());
            feature.setFeatureVector(Arrays.toString(features));
            feature.setAnomalyScore(anomalyScore);
            feature.setIsAnomaly(isAnomaly);
            
            aiFeatureMapper.insert(feature);
        } catch (Exception e) {
            log.error("保存特征数据失败", e);
        }
    }

    /**
     * 更新行为模式
     */
    private void updateBehaviorPattern(Long userId, List<UserBehavior> behaviors) {
        // 这里可以实现更复杂的行为模式更新逻辑
        // 简化实现：只更新最后出现时间
        BehaviorPattern pattern = getUserBehaviorPattern(userId);
        if (pattern != null) {
            pattern.setLastSeenTime(LocalDateTime.now());
            behaviorPatternMapper.updateById(pattern);
        }
    }
}