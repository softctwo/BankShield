package com.bankshield.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI模块配置类
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AIConfig {

    /**
     * 异常检测模型配置
     */
    private AnomalyDetectionConfig anomalyDetection;

    /**
     * 告警分类模型配置
     */
    private AlertClassificationConfig alertClassification;

    /**
     * 预测模型配置
     */
    private PredictionConfig prediction;

    /**
     * 特征工程配置
     */
    private FeatureConfig feature;

    /**
     * 外部AI服务配置
     */
    private ExternalServiceConfig external;

    /**
     * 异常检测模型配置
     */
    @Data
    public static class AnomalyDetectionConfig {
        private boolean enabled = true;
        private String modelPath = "classpath:models/anomaly-detection";
        private double threshold = 0.7;
        private String retrainInterval = "24h";
        private int minSamples = 100;
        private double contamination = 0.1;
    }

    /**
     * 告警分类模型配置
     */
    @Data
    public static class AlertClassificationConfig {
        private boolean enabled = true;
        private String modelPath = "classpath:models/alert-classification";
        private double accuracyThreshold = 0.95;
        private int maxDepth = 10;
        private int minSamplesLeaf = 5;
        private int nEstimators = 100;
    }

    /**
     * 预测模型配置
     */
    @Data
    public static class PredictionConfig {
        private boolean enabled = true;
        private String modelPath = "classpath:models/prediction";
        private int predictionDays = 7;
        private int lstmUnits = 50;
        private double dropout = 0.2;
        private int batchSize = 32;
    }

    /**
     * 特征工程配置
     */
    @Data
    public static class FeatureConfig {
        private BehaviorConfig behavior;
        private TrainingConfig training;

        /**
         * 行为特征配置
         */
        @Data
        public static class BehaviorConfig {
            private String timeWindow = "24h";
            private int minSamples = 100;
            private int featureDimensions = 15;
            private double normalizationFactor = 1.0;
        }

        /**
         * 训练数据配置
         */
        @Data
        public static class TrainingConfig {
            private int batchSize = 1000;
            private int epochs = 100;
            private double learningRate = 0.001;
            private double validationSplit = 0.2;
            private double testSplit = 0.1;
            private boolean earlyStopping = true;
            private int patience = 10;
        }
    }

    /**
     * 外部AI服务配置
     */
    @Data
    public static class ExternalServiceConfig {
        private PythonServiceConfig pythonService;

        /**
         * Python服务配置
         */
        @Data
        public static class PythonServiceConfig {
            private boolean enabled = true;
            private String url = "http://localhost:5000";
            private String timeout = "30s";
            private int retryTimes = 3;
            private long retryDelay = 1000;
            private String apiKey = "";
        }
    }
}