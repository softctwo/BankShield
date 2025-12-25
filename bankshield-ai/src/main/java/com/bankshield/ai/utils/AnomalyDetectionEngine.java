package com.bankshield.ai.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 异常检测引擎
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Slf4j
@Component
public class AnomalyDetectionEngine {

    // 用户行为模式缓存
    private final Map<Long, UserBehaviorPattern> userPatterns = new HashMap<>();
    
    // 全局异常检测模型参数
    private static final double GLOBAL_ANOMALY_THRESHOLD = 0.7;
    private static final double ISOLATION_THRESHOLD = 0.6;
    private static final int MIN_SAMPLES_FOR_PATTERN = 10;

    /**
     * 检测异常
     * 
     * @param features 特征向量
     * @return 异常分数 (0-1)
     */
    public double detectAnomaly(double[] features) {
        try {
            // 使用隔离森林算法进行异常检测
            double isolationScore = calculateIsolationScore(features);
            
            // 使用局部异常因子算法
            double lofScore = calculateLOFScore(features);
            
            // 综合评分
            double combinedScore = (isolationScore + lofScore) / 2.0;
            
            log.debug("异常检测完成，隔离分数: {}, LOF分数: {}, 综合分数: {}", 
                    isolationScore, lofScore, combinedScore);
            
            return combinedScore;
            
        } catch (Exception e) {
            log.error("异常检测失败", e);
            return 0.0;
        }
    }

    /**
     * 检测用户特定异常
     * 
     * @param userId 用户ID
     * @param features 特征向量
     * @return 异常分数
     */
    public double detectUserAnomaly(Long userId, double[] features) {
        try {
            UserBehaviorPattern pattern = userPatterns.get(userId);
            if (pattern == null || pattern.getSampleCount() < MIN_SAMPLES_FOR_PATTERN) {
                // 用户模式不足，使用全局检测
                return detectAnomaly(features);
            }
            
            // 计算与用户模式的偏离程度
            double deviationScore = calculateDeviationScore(features, pattern);
            
            // 结合全局异常检测
            double globalScore = detectAnomaly(features);
            
            // 加权综合评分
            double combinedScore = 0.7 * deviationScore + 0.3 * globalScore;
            
            log.debug("用户特定异常检测完成，用户ID: {}, 偏离分数: {}, 全局分数: {}, 综合分数: {}", 
                    userId, deviationScore, globalScore, combinedScore);
            
            return combinedScore;
            
        } catch (Exception e) {
            log.error("用户特定异常检测失败，用户ID: " + userId, e);
            return detectAnomaly(features);
        }
    }

    /**
     * 学习用户行为模式
     * 
     * @param userId 用户ID
     * @param featureVectors 特征向量列表
     * @return 是否学习成功
     */
    public boolean learnUserPattern(Long userId, List<double[]> featureVectors) {
        try {
            if (featureVectors.isEmpty()) {
                return false;
            }
            
            // 计算用户行为模式的统计特征
            UserBehaviorPattern pattern = new UserBehaviorPattern();
            pattern.setUserId(userId);
            pattern.setSampleCount(featureVectors.size());
            
            // 计算特征均值
            double[] mean = calculateMean(featureVectors);
            pattern.setMean(mean);
            
            // 计算特征标准差
            double[] stdDev = calculateStdDev(featureVectors, mean);
            pattern.setStdDev(stdDev);
            
            // 计算特征协方差矩阵
            double[][] covariance = calculateCovariance(featureVectors, mean);
            pattern.setCovariance(covariance);
            
            // 缓存用户模式
            userPatterns.put(userId, pattern);
            
            log.info("用户行为模式学习完成，用户ID: {}, 样本数量: {}", userId, featureVectors.size());
            
            return true;
            
        } catch (Exception e) {
            log.error("学习用户行为模式失败，用户ID: " + userId, e);
            return false;
        }
    }

    /**
     * 计算隔离分数（Isolation Forest简化版）
     */
    private double calculateIsolationScore(double[] features) {
        // 简化实现：基于特征值的随机分割
        int featureCount = features.length;
        double score = 0.0;
        
        // 模拟随机分割过程
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            int splitFeature = random.nextInt(featureCount);
            double splitValue = features[splitFeature] * (0.5 + random.nextDouble() * 0.5);
            
            // 计算分割深度（简化版）
            if (features[splitFeature] > splitValue) {
                score += 1.0;
            } else {
                score += 0.5;
            }
        }
        
        // 归一化分数
        return Math.min(score / 100.0, 1.0);
    }

    /**
     * 计算LOF分数（Local Outlier Factor简化版）
     */
    private double calculateLOFScore(double[] features) {
        // 简化实现：基于特征距离的局部异常因子
        double distance = calculateEuclideanDistance(features, new double[features.length]);
        
        // 距离越远，异常分数越高
        double normalizedDistance = Math.min(distance / 10.0, 1.0);
        
        return normalizedDistance;
    }

    /**
     * 计算偏离分数
     */
    private double calculateDeviationScore(double[] features, UserBehaviorPattern pattern) {
        double[] mean = pattern.getMean();
        double[] stdDev = pattern.getStdDev();
        
        double deviation = 0.0;
        for (int i = 0; i < features.length; i++) {
            if (stdDev[i] != 0) {
                double zScore = Math.abs(features[i] - mean[i]) / stdDev[i];
                deviation += zScore;
            }
        }
        
        // 归一化偏离分数
        double avgDeviation = deviation / features.length;
        return Math.min(avgDeviation / 3.0, 1.0);
    }

    /**
     * 计算均值
     */
    private double[] calculateMean(List<double[]> vectors) {
        if (vectors.isEmpty()) {
            return new double[0];
        }
        
        int featureCount = vectors.get(0).length;
        double[] mean = new double[featureCount];
        
        for (double[] vector : vectors) {
            for (int i = 0; i < featureCount; i++) {
                mean[i] += vector[i];
            }
        }
        
        for (int i = 0; i < featureCount; i++) {
            mean[i] /= vectors.size();
        }
        
        return mean;
    }

    /**
     * 计算标准差
     */
    private double[] calculateStdDev(List<double[]> vectors, double[] mean) {
        if (vectors.isEmpty()) {
            return new double[mean.length];
        }
        
        int featureCount = mean.length;
        double[] stdDev = new double[featureCount];
        
        for (double[] vector : vectors) {
            for (int i = 0; i < featureCount; i++) {
                double diff = vector[i] - mean[i];
                stdDev[i] += diff * diff;
            }
        }
        
        for (int i = 0; i < featureCount; i++) {
            stdDev[i] = Math.sqrt(stdDev[i] / vectors.size());
        }
        
        return stdDev;
    }

    /**
     * 计算协方差矩阵
     */
    private double[][] calculateCovariance(List<double[]> vectors, double[] mean) {
        if (vectors.isEmpty()) {
            return new double[mean.length][mean.length];
        }
        
        int featureCount = mean.length;
        double[][] covariance = new double[featureCount][featureCount];
        
        for (double[] vector : vectors) {
            for (int i = 0; i < featureCount; i++) {
                for (int j = 0; j < featureCount; j++) {
                    covariance[i][j] += (vector[i] - mean[i]) * (vector[j] - mean[j]);
                }
            }
        }
        
        for (int i = 0; i < featureCount; i++) {
            for (int j = 0; j < featureCount; j++) {
                covariance[i][j] /= vectors.size();
            }
        }
        
        return covariance;
    }

    /**
     * 计算欧几里得距离
     */
    private double calculateEuclideanDistance(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    /**
     * 用户行为模式类
     */
    private static class UserBehaviorPattern {
        private Long userId;
        private int sampleCount;
        private double[] mean;
        private double[] stdDev;
        private double[][] covariance;

        // Getters and Setters
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public int getSampleCount() {
            return sampleCount;
        }

        public void setSampleCount(int sampleCount) {
            this.sampleCount = sampleCount;
        }

        public double[] getMean() {
            return mean;
        }

        public void setMean(double[] mean) {
            this.mean = mean;
        }

        public double[] getStdDev() {
            return stdDev;
        }

        public void setStdDev(double[] stdDev) {
            this.stdDev = stdDev;
        }

        public double[][] getCovariance() {
            return covariance;
        }

        public void setCovariance(double[][] covariance) {
            this.covariance = covariance;
        }
    }
}