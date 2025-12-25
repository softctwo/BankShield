package com.bankshield.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.ai.entity.AiPrediction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI预测结果Mapper接口
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Mapper
public interface AiPredictionMapper extends BaseMapper<AiPrediction> {

    /**
     * 根据模型ID查询预测结果
     * 
     * @param modelId 模型ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 预测结果列表
     */
    List<AiPrediction> selectByModelId(@Param("modelId") Long modelId, 
                                       @Param("startTime") LocalDateTime startTime, 
                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 根据用户ID查询预测结果
     * 
     * @param userId 用户ID
     * @param predictionType 预测类型
     * @return 预测结果列表
     */
    List<AiPrediction> selectByUserId(@Param("userId") Long userId, 
                                      @Param("predictionType") String predictionType);

    /**
     * 查询准确的预测结果
     * 
     * @param modelId 模型ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 准确的预测结果列表
     */
    List<AiPrediction> selectAccuratePredictions(@Param("modelId") Long modelId, 
                                                 @Param("startTime") LocalDateTime startTime, 
                                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 查询需要验证的预测结果
     * 
     * @param validationTime 验证时间
     * @return 需要验证的预测结果列表
     */
    List<AiPrediction> selectPredictionsNeedValidation(@Param("validationTime") LocalDateTime validationTime);

    /**
     * 更新预测结果准确性
     * 
     * @param predictionId 预测结果ID
     * @param isAccurate 是否准确
     * @param validationTime 验证时间
     * @return 更新数量
     */
    Integer updatePredictionAccuracy(@Param("predictionId") Long predictionId, 
                                     @Param("isAccurate") Boolean isAccurate, 
                                     @Param("validationTime") LocalDateTime validationTime);

    /**
     * 查询预测统计信息
     * 
     * @param modelId 模型ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 预测统计信息
     */
    PredictionStatistics selectPredictionStatistics(@Param("modelId") Long modelId, 
                                                    @Param("startTime") LocalDateTime startTime, 
                                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 查询告警预测结果
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param minConfidence 最小置信度
     * @return 告警预测结果列表
     */
    List<AiPrediction> selectAlertPredictions(@Param("startTime") LocalDateTime startTime, 
                                              @Param("endTime") LocalDateTime endTime, 
                                              @Param("minConfidence") Double minConfidence);

    /**
     * 批量插入预测结果
     * 
     * @param predictions 预测结果列表
     * @return 插入数量
     */
    Integer batchInsert(@Param("predictions") List<AiPrediction> predictions);

    /**
     * 删除过期预测结果
     * 
     * @param expireTime 过期时间
     * @return 删除数量
     */
    Integer deleteExpiredPredictions(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 预测统计信息
     */
    public static class PredictionStatistics {
        private Long totalPredictions;
        private Long accuratePredictions;
        private Long inaccuratePredictions;
        private Double accuracyRate;
        private Double avgConfidence;
        private Double maxConfidence;
        private Double minConfidence;

        // Getters and Setters
        public Long getTotalPredictions() {
            return totalPredictions;
        }

        public void setTotalPredictions(Long totalPredictions) {
            this.totalPredictions = totalPredictions;
        }

        public Long getAccuratePredictions() {
            return accuratePredictions;
        }

        public void setAccuratePredictions(Long accuratePredictions) {
            this.accuratePredictions = accuratePredictions;
        }

        public Long getInaccuratePredictions() {
            return inaccuratePredictions;
        }

        public void setInaccuratePredictions(Long inaccuratePredictions) {
            this.inaccuratePredictions = inaccuratePredictions;
        }

        public Double getAccuracyRate() {
            return accuracyRate;
        }

        public void setAccuracyRate(Double accuracyRate) {
            this.accuracyRate = accuracyRate;
        }

        public Double getAvgConfidence() {
            return avgConfidence;
        }

        public void setAvgConfidence(Double avgConfidence) {
            this.avgConfidence = avgConfidence;
        }

        public Double getMaxConfidence() {
            return maxConfidence;
        }

        public void setMaxConfidence(Double maxConfidence) {
            this.maxConfidence = maxConfidence;
        }

        public Double getMinConfidence() {
            return minConfidence;
        }

        public void setMinConfidence(Double minConfidence) {
            this.minConfidence = minConfidence;
        }
    }
}