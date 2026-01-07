package com.bankshield.api.service;

import com.bankshield.api.entity.AIModel;
import com.bankshield.api.entity.AITrainingJob;
import com.bankshield.api.service.impl.AIModelServiceImpl;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI模型服务测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AI模型服务测试")
class AIModelServiceTest {

    @InjectMocks
    private AIModelServiceImpl aiModelService;

    private AIModel testModel;
    private AITrainingJob testJob;

    @BeforeEach
    void setUp() {
        testModel = new AIModel();
        testModel.setModelName("测试异常检测模型");
        testModel.setModelType("ANOMALY_DETECTION");
        testModel.setAlgorithm("ISOLATION_FOREST");
        testModel.setDescription("测试用模型");

        testJob = new AITrainingJob();
        testJob.setJobName("测试训练任务");
        testJob.setModelId(1L);
    }

    @Test
    @DisplayName("创建AI模型")
    void testCreateModel() {
        AIModel result = aiModelService.createModel(testModel);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("测试异常检测模型", result.getModelName());
        assertEquals("ANOMALY_DETECTION", result.getModelType());
        assertEquals("CREATED", result.getStatus());
    }

    @Test
    @DisplayName("分页查询模型列表")
    void testGetModels() {
        // 先创建一个模型
        aiModelService.createModel(testModel);

        IPage<AIModel> page = aiModelService.getModels(1, 10, null, null);

        assertNotNull(page);
        assertTrue(page.getTotal() > 0);
        assertNotNull(page.getRecords());
    }

    @Test
    @DisplayName("根据类型查询模型")
    void testGetModelsByType() {
        testModel.setModelType("THREAT_PREDICTION");
        aiModelService.createModel(testModel);

        IPage<AIModel> page = aiModelService.getModels(1, 10, "THREAT_PREDICTION", null);

        assertNotNull(page);
    }

    @Test
    @DisplayName("根据ID获取模型")
    void testGetModelById() {
        AIModel created = aiModelService.createModel(testModel);
        AIModel found = aiModelService.getModelById(created.getId());

        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals(created.getModelName(), found.getModelName());
    }

    @Test
    @DisplayName("更新模型")
    void testUpdateModel() {
        AIModel created = aiModelService.createModel(testModel);
        created.setModelName("更新后的模型名称");
        created.setDescription("更新后的描述");

        boolean result = aiModelService.updateModel(created);

        assertTrue(result);
        AIModel updated = aiModelService.getModelById(created.getId());
        assertEquals("更新后的模型名称", updated.getModelName());
    }

    @Test
    @DisplayName("删除模型")
    void testDeleteModel() {
        AIModel created = aiModelService.createModel(testModel);
        boolean result = aiModelService.deleteModel(created.getId());

        assertTrue(result);
        assertNull(aiModelService.getModelById(created.getId()));
    }

    @Test
    @DisplayName("创建训练任务")
    void testCreateTrainingJob() {
        AIModel model = aiModelService.createModel(testModel);
        testJob.setModelId(model.getId());

        AITrainingJob result = aiModelService.createTrainingJob(testJob);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    @DisplayName("获取训练任务列表")
    void testGetTrainingJobs() {
        AIModel model = aiModelService.createModel(testModel);
        testJob.setModelId(model.getId());
        aiModelService.createTrainingJob(testJob);

        IPage<AITrainingJob> jobs = aiModelService.getTrainingJobs(1, 10, null);

        assertNotNull(jobs);
    }

    @Test
    @DisplayName("取消训练任务")
    void testCancelTrainingJob() {
        AIModel model = aiModelService.createModel(testModel);
        testJob.setModelId(model.getId());
        AITrainingJob job = aiModelService.createTrainingJob(testJob);

        boolean result = aiModelService.cancelTrainingJob(job.getId());

        assertTrue(result);
    }

    @Test
    @DisplayName("部署模型")
    void testDeployModel() {
        AIModel model = aiModelService.createModel(testModel);
        model.setStatus("TRAINED");

        boolean result = aiModelService.deployModel(model.getId());

        assertTrue(result);
    }

    @Test
    @DisplayName("下线模型")
    void testUndeployModel() {
        AIModel model = aiModelService.createModel(testModel);

        boolean result = aiModelService.undeployModel(model.getId());

        assertTrue(result);
    }

    @Test
    @DisplayName("模型推理")
    void testInference() {
        AIModel model = aiModelService.createModel(testModel);
        Map<String, Object> inputData = Map.of(
            "feature1", 0.5,
            "feature2", 0.8,
            "feature3", 0.3
        );

        Map<String, Object> result = aiModelService.inference(model.getId(), inputData);

        assertNotNull(result);
        assertTrue(result.containsKey("prediction"));
        assertTrue(result.containsKey("confidence"));
    }

    @Test
    @DisplayName("批量推理")
    void testBatchInference() {
        AIModel model = aiModelService.createModel(testModel);
        List<Map<String, Object>> inputDataList = List.of(
            Map.of("feature1", 0.5, "feature2", 0.8),
            Map.of("feature1", 0.3, "feature2", 0.6)
        );

        List<Map<String, Object>> results = aiModelService.batchInference(model.getId(), inputDataList);

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("获取模型统计信息")
    void testGetModelStatistics() {
        Map<String, Object> stats = aiModelService.getModelStatistics();

        assertNotNull(stats);
        assertTrue(stats.containsKey("totalModels"));
        assertTrue(stats.containsKey("deployedModels"));
    }

    @Test
    @DisplayName("导出ONNX格式")
    void testExportToONNX() {
        AIModel model = aiModelService.createModel(testModel);

        String result = aiModelService.exportToONNX(model.getId());

        assertNotNull(result);
    }

    @Test
    @DisplayName("导出PMML格式")
    void testExportToPMML() {
        AIModel model = aiModelService.createModel(testModel);

        String result = aiModelService.exportToPMML(model.getId());

        assertNotNull(result);
    }

    @Test
    @DisplayName("获取模型性能监控数据")
    void testGetModelPerformance() {
        AIModel model = aiModelService.createModel(testModel);

        Map<String, Object> performance = aiModelService.getModelPerformance(model.getId(), 24);

        assertNotNull(performance);
    }
}
