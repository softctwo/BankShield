package com.bankshield.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.ai.entity.AiModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI模型Mapper接口
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Mapper
public interface AiModelMapper extends BaseMapper<AiModel> {

    /**
     * 根据模型类型查询活跃模型
     * 
     * @param modelType 模型类型
     * @return AI模型列表
     */
    List<AiModel> selectActiveModelsByType(@Param("modelType") String modelType);

    /**
     * 根据模型名称查询模型
     * 
     * @param modelName 模型名称
     * @return AI模型
     */
    AiModel selectByModelName(@Param("modelName") String modelName);

    /**
     * 查询最佳模型
     * 
     * @param modelType 模型类型
     * @param minAccuracy 最小准确率
     * @return AI模型
     */
    AiModel selectBestModel(@Param("modelType") String modelType, 
                            @Param("minAccuracy") Double minAccuracy);

    /**
     * 更新模型使用次数
     * 
     * @param modelId 模型ID
     * @param lastUsedTime 最后使用时间
     * @return 更新数量
     */
    Integer updateUsageCount(@Param("modelId") Long modelId, 
                             @Param("lastUsedTime") LocalDateTime lastUsedTime);

    /**
     * 查询需要重新训练的模型
     * 
     * @param maxTrainingTime 最大训练时间
     * @return AI模型列表
     */
    List<AiModel> selectModelsNeedRetrain(@Param("maxTrainingTime") LocalDateTime maxTrainingTime);

    /**
     * 根据状态查询模型
     * 
     * @param status 状态
     * @return AI模型列表
     */
    List<AiModel> selectByStatus(@Param("status") String status);

    /**
     * 查询模型统计信息
     * 
     * @param modelType 模型类型
     * @return 统计信息
     */
    List<ModelStatistics> selectModelStatistics(@Param("modelType") String modelType);

    /**
     * 模型统计信息
     */
    public static class ModelStatistics {
        private String modelType;
        private Long modelCount;
        private Double avgAccuracy;
        private Double maxAccuracy;
        private Double minAccuracy;
        private Long totalUsage;

        // Getters and Setters
        public String getModelType() {
            return modelType;
        }

        public void setModelType(String modelType) {
            this.modelType = modelType;
        }

        public Long getModelCount() {
            return modelCount;
        }

        public void setModelCount(Long modelCount) {
            this.modelCount = modelCount;
        }

        public Double getAvgAccuracy() {
            return avgAccuracy;
        }

        public void setAvgAccuracy(Double avgAccuracy) {
            this.avgAccuracy = avgAccuracy;
        }

        public Double getMaxAccuracy() {
            return maxAccuracy;
        }

        public void setMaxAccuracy(Double maxAccuracy) {
            this.maxAccuracy = maxAccuracy;
        }

        public Double getMinAccuracy() {
            return minAccuracy;
        }

        public void setMinAccuracy(Double minAccuracy) {
            this.minAccuracy = minAccuracy;
        }

        public Long getTotalUsage() {
            return totalUsage;
        }

        public void setTotalUsage(Long totalUsage) {
            this.totalUsage = totalUsage;
        }
    }
}