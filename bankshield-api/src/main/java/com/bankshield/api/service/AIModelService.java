package com.bankshield.api.service;

import com.bankshield.api.entity.AIModel;
import com.bankshield.api.entity.AITrainingJob;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.Map;

/**
 * AI模型管理服务接口
 */
public interface AIModelService {

    /**
     * 分页查询模型列表
     */
    IPage<AIModel> getModels(int page, int size, String modelType, String status);

    /**
     * 获取模型详情
     */
    AIModel getModelById(Long id);

    /**
     * 创建模型
     */
    AIModel createModel(AIModel model);

    /**
     * 更新模型
     */
    boolean updateModel(AIModel model);

    /**
     * 删除模型
     */
    boolean deleteModel(Long id);

    /**
     * 部署模型
     */
    boolean deployModel(Long id);

    /**
     * 下线模型
     */
    boolean undeployModel(Long id);

    /**
     * 创建训练任务
     */
    AITrainingJob createTrainingJob(AITrainingJob job);

    /**
     * 获取训练任务列表
     */
    IPage<AITrainingJob> getTrainingJobs(int page, int size, String status);

    /**
     * 获取训练任务详情
     */
    AITrainingJob getTrainingJobById(Long id);

    /**
     * 取消训练任务
     */
    boolean cancelTrainingJob(Long id);

    /**
     * 执行模型推理
     */
    Map<String, Object> inference(Long modelId, Map<String, Object> input);

    /**
     * 批量推理
     */
    List<Map<String, Object>> batchInference(Long modelId, List<Map<String, Object>> inputs);

    /**
     * 获取模型统计信息
     */
    Map<String, Object> getModelStatistics();

    /**
     * 获取模型性能监控数据
     */
    Map<String, Object> getModelPerformance(Long modelId, int hours);

    /**
     * 导出模型为ONNX格式
     */
    String exportToONNX(Long modelId);

    /**
     * 导出模型为PMML格式
     */
    String exportToPMML(Long modelId);

    /**
     * 导入模型
     */
    AIModel importModel(String modelPath, String format);
}
