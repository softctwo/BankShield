package com.bankshield.ai.service;

import com.bankshield.ai.model.AnomalyResult;
import com.bankshield.ai.model.UserBehavior;

/**
 * 行为分析服务接口
 * 
 * @author BankShield
 * @version 1.0.0
 */
public interface BehaviorAnalysisService {

    /**
     * 检测异常行为
     * 
     * @param behavior 用户行为数据
     * @return 异常检测结果
     */
    AnomalyResult detectAnomaly(UserBehavior behavior);

    /**
     * 批量检测异常行为
     * 
     * @param behaviors 用户行为数据列表
     * @return 异常检测结果列表
     */
    java.util.List<AnomalyResult> detectAnomalies(java.util.List<UserBehavior> behaviors);

    /**
     * 学习用户行为模式
     * 
     * @param userId 用户ID
     * @param behaviors 用户行为数据列表
     * @return 学习结果
     */
    boolean learnUserBehaviorPattern(Long userId, java.util.List<UserBehavior> behaviors);

    /**
     * 获取用户行为模式
     * 
     * @param userId 用户ID
     * @return 用户行为模式
     */
    com.bankshield.ai.entity.BehaviorPattern getUserBehaviorPattern(Long userId);

    /**
     * 更新用户行为模式
     * 
     * @param userId 用户ID
     * @param behavior 最新行为数据
     * @return 更新结果
     */
    boolean updateUserBehaviorPattern(Long userId, UserBehavior behavior);

    /**
     * 计算行为异常分数
     * 
     * @param behavior 用户行为数据
     * @return 异常分数
     */
    double calculateAnomalyScore(UserBehavior behavior);

    /**
     * 提取行为特征
     * 
     * @param behavior 用户行为数据
     * @return 特征向量
     */
    double[] extractFeatures(UserBehavior behavior);

    /**
     * 获取异常行为统计
     * 
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 异常行为统计
     */
    AnomalyStatistics getAnomalyStatistics(Long userId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);

    /**
     * 异常行为统计类
     */
    class AnomalyStatistics {
        private Long totalBehaviors;
        private Long anomalyBehaviors;
        private Double anomalyRate;
        private java.util.Map<String, Long> anomalyTypeCounts;
        private java.util.Map<String, Double> anomalyTypeRates;

        // Getters and Setters
        public Long getTotalBehaviors() {
            return totalBehaviors;
        }

        public void setTotalBehaviors(Long totalBehaviors) {
            this.totalBehaviors = totalBehaviors;
        }

        public Long getAnomalyBehaviors() {
            return anomalyBehaviors;
        }

        public void setAnomalyBehaviors(Long anomalyBehaviors) {
            this.anomalyBehaviors = anomalyBehaviors;
        }

        public Double getAnomalyRate() {
            return anomalyRate;
        }

        public void setAnomalyRate(Double anomalyRate) {
            this.anomalyRate = anomalyRate;
        }

        public java.util.Map<String, Long> getAnomalyTypeCounts() {
            return anomalyTypeCounts;
        }

        public void setAnomalyTypeCounts(java.util.Map<String, Long> anomalyTypeCounts) {
            this.anomalyTypeCounts = anomalyTypeCounts;
        }

        public java.util.Map<String, Double> getAnomalyTypeRates() {
            return anomalyTypeRates;
        }

        public void setAnomalyTypeRates(java.util.Map<String, Double> anomalyTypeRates) {
            this.anomalyTypeRates = anomalyTypeRates;
        }
    }
}