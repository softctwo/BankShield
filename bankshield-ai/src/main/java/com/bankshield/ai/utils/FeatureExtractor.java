package com.bankshield.ai.utils;

import com.bankshield.ai.model.UserBehavior;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 特征提取器
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Slf4j
@Component
public class FeatureExtractor {

    // 特征维度
    private static final int FEATURE_DIMENSION = 15;
    
    // 时间特征分段
    private static final int[] TIME_SEGMENTS = {0, 6, 12, 18, 24};
    
    // 地理位置特征映射
    private static final String[] LOCATION_REGIONS = {
        "北京", "上海", "广州", "深圳", "杭州", "南京", "武汉", "成都", "西安", "其他"
    };

    /**
     * 提取用户行为特征
     * 
     * @param behavior 用户行为数据
     * @return 特征向量
     */
    public double[] extractFeatures(UserBehavior behavior) {
        try {
            double[] features = new double[FEATURE_DIMENSION];
            int index = 0;
            
            // 1. 时间特征 (索引 0-4)
            index = extractTimeFeatures(behavior, features, index);
            
            // 2. 地理位置特征 (索引 5-7)
            index = extractLocationFeatures(behavior, features, index);
            
            // 3. 行为类型特征 (索引 8-10)
            index = extractBehaviorTypeFeatures(behavior, features, index);
            
            // 4. 操作特征 (索引 11-14)
            index = extractOperationFeatures(behavior, features, index);
            
            log.debug("特征提取完成，用户ID: {}, 特征维度: {}", behavior.getUserId(), index);
            
            return features;
            
        } catch (Exception e) {
            log.error("特征提取失败，用户ID: " + behavior.getUserId(), e);
            return new double[FEATURE_DIMENSION];
        }
    }

    /**
     * 提取时间特征
     */
    private int extractTimeFeatures(UserBehavior behavior, double[] features, int startIndex) {
        LocalDateTime operationTime = behavior.getOperationTime();
        if (operationTime == null) {
            operationTime = LocalDateTime.now();
        }
        
        int hour = operationTime.getHour();
        int dayOfWeek = operationTime.getDayOfWeek().getValue();
        
        // 1. 小时特征 (one-hot编码)
        for (int i = 0; i < TIME_SEGMENTS.length - 1; i++) {
            if (hour >= TIME_SEGMENTS[i] && hour < TIME_SEGMENTS[i + 1]) {
                features[startIndex + i] = 1.0;
            } else {
                features[startIndex + i] = 0.0;
            }
        }
        
        // 2. 星期特征 (归一化)
        features[startIndex + 4] = (double) dayOfWeek / 7.0;
        
        return startIndex + 5;
    }

    /**
     * 提取地理位置特征
     */
    private int extractLocationFeatures(UserBehavior behavior, double[] features, int startIndex) {
        String location = behavior.getLocation();
        String ipAddress = behavior.getIpAddress();
        
        // 1. 地理位置编码
        if (location != null) {
            boolean locationFound = false;
            for (int i = 0; i < LOCATION_REGIONS.length; i++) {
                if (location.contains(LOCATION_REGIONS[i])) {
                    features[startIndex] = (double) i / (LOCATION_REGIONS.length - 1);
                    locationFound = true;
                    break;
                }
            }
            if (!locationFound) {
                features[startIndex] = 1.0; // 其他
            }
        } else {
            features[startIndex] = 0.5; // 未知
        }
        
        // 2. IP地址特征（简化版）
        if (ipAddress != null && !ipAddress.isEmpty()) {
            // 使用IP地址的哈希值作为特征
            features[startIndex + 1] = (double) (ipAddress.hashCode() & 0xFF) / 255.0;
        } else {
            features[startIndex + 1] = 0.0;
        }
        
        // 3. 地理位置变化特征
        features[startIndex + 2] = 0.0; // 简化实现，需要历史数据比较
        
        return startIndex + 3;
    }

    /**
     * 提取行为类型特征
     */
    private int extractBehaviorTypeFeatures(UserBehavior behavior, double[] features, int startIndex) {
        String behaviorType = behavior.getBehaviorType();
        String operationType = behavior.getOperationType();
        String resourceType = behavior.getResourceType();
        
        // 1. 行为类型编码
        features[startIndex] = encodeBehaviorType(behaviorType);
        
        // 2. 操作类型编码
        features[startIndex + 1] = encodeOperationType(operationType);
        
        // 3. 资源类型编码
        features[startIndex + 2] = encodeResourceType(resourceType);
        
        return startIndex + 3;
    }

    /**
     * 提取操作特征
     */
    private int extractOperationFeatures(UserBehavior behavior, double[] features, int startIndex) {
        Long responseTime = behavior.getResponseTime();
        Long dataSize = behavior.getDataSize();
        Double operationFrequency = behavior.getOperationFrequency();
        Integer permissionLevel = behavior.getPermissionLevel();
        
        // 1. 响应时间特征（对数归一化）
        if (responseTime != null && responseTime > 0) {
            features[startIndex] = Math.log10(responseTime + 1) / 6.0; // 假设最大响应时间为1秒
        } else {
            features[startIndex] = 0.0;
        }
        
        // 2. 数据量特征（对数归一化）
        if (dataSize != null && dataSize > 0) {
            features[startIndex + 1] = Math.log10(dataSize + 1) / 12.0; // 假设最大数据量为1TB
        } else {
            features[startIndex + 1] = 0.0;
        }
        
        // 3. 操作频率特征
        if (operationFrequency != null && operationFrequency > 0) {
            features[startIndex + 2] = Math.min(operationFrequency / 1000.0, 1.0); // 假设最大频率为1000次/小时
        } else {
            features[startIndex + 2] = 0.0;
        }
        
        // 4. 权限级别特征（归一化）
        if (permissionLevel != null && permissionLevel > 0) {
            features[startIndex + 3] = (double) permissionLevel / 10.0; // 假设最大权限级别为10
        } else {
            features[startIndex + 3] = 0.0;
        }
        
        return startIndex + 4;
    }

    /**
     * 编码行为类型
     */
    private double encodeBehaviorType(String behaviorType) {
        if (behaviorType == null) {
            return 0.0;
        }
        
        switch (behaviorType.toLowerCase()) {
            case "login":
                return 0.1;
            case "logout":
                return 0.2;
            case "access":
                return 0.3;
            case "operation":
                return 0.4;
            case "download":
                return 0.5;
            case "upload":
                return 0.6;
            case "query":
                return 0.7;
            case "modify":
                return 0.8;
            case "delete":
                return 0.9;
            default:
                return 0.0;
        }
    }

    /**
     * 编码操作类型
     */
    private double encodeOperationType(String operationType) {
        if (operationType == null) {
            return 0.0;
        }
        
        // 根据操作类型的敏感程度进行编码
        String lowerType = operationType.toLowerCase();
        if (lowerType.contains("create") || lowerType.contains("add")) {
            return 0.2;
        } else if (lowerType.contains("read") || lowerType.contains("view")) {
            return 0.1;
        } else if (lowerType.contains("update") || lowerType.contains("modify")) {
            return 0.6;
        } else if (lowerType.contains("delete") || lowerType.contains("remove")) {
            return 0.9;
        } else if (lowerType.contains("export") || lowerType.contains("download")) {
            return 0.7;
        } else if (lowerType.contains("import") || lowerType.contains("upload")) {
            return 0.5;
        } else {
            return 0.3;
        }
    }

    /**
     * 编码资源类型
     */
    private double encodeResourceType(String resourceType) {
        if (resourceType == null) {
            return 0.0;
        }
        
        // 根据资源类型的敏感程度进行编码
        String lowerType = resourceType.toLowerCase();
        if (lowerType.contains("user") || lowerType.contains("account")) {
            return 0.9;
        } else if (lowerType.contains("role") || lowerType.contains("permission")) {
            return 0.8;
        } else if (lowerType.contains("config") || lowerType.contains("setting")) {
            return 0.7;
        } else if (lowerType.contains("data") || lowerType.contains("database")) {
            return 0.6;
        } else if (lowerType.contains("file") || lowerType.contains("document")) {
            return 0.5;
        } else if (lowerType.contains("log") || lowerType.contains("audit")) {
            return 0.4;
        } else if (lowerType.contains("report") || lowerType.contains("statistics")) {
            return 0.3;
        } else {
            return 0.1;
        }
    }

    /**
     * 提取时间差特征
     */
    public double extractTimeDifference(LocalDateTime time1, LocalDateTime time2) {
        if (time1 == null || time2 == null) {
            return 0.0;
        }
        
        long minutes = ChronoUnit.MINUTES.between(time1, time2);
        return Math.abs(minutes) / (24.0 * 60.0); // 归一化到0-1范围
    }

    /**
     * 提取地理位置相似度特征
     */
    public double extractLocationSimilarity(String location1, String location2) {
        if (location1 == null || location2 == null) {
            return 0.0;
        }
        
        // 简单的字符串相似度计算
        int maxLength = Math.max(location1.length(), location2.length());
        if (maxLength == 0) {
            return 0.0;
        }
        
        int distance = calculateLevenshteinDistance(location1, location2);
        return 1.0 - (double) distance / maxLength;
    }

    /**
     * 计算Levenshtein距离（编辑距离）
     */
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], 
                                   Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
}