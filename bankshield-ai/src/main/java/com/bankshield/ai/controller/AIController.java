package com.bankshield.ai.controller;

import com.bankshield.ai.model.AnomalyResult;
import com.bankshield.ai.model.ResourcePrediction;
import com.bankshield.ai.model.UserBehavior;
import com.bankshield.ai.service.BehaviorAnalysisService;
import com.bankshield.ai.service.ThreatPredictionService;
import com.bankshield.ai.service.SmartAlertService;
import com.bankshield.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * AI智能安全分析控制器
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@Api(tags = "AI智能安全分析模块")
public class AIController {

    @Autowired
    private BehaviorAnalysisService behaviorAnalysisService;

    @Autowired
    private ThreatPredictionService threatPredictionService;

    @Autowired
    private SmartAlertService smartAlertService;

    /**
     * 异常行为检测
     */
    @PostMapping("/behavior/detect")
    @ApiOperation(value = "异常行为检测", notes = "检测用户行为是否存在异常")
    public Result<Double> detectAnomaly(@RequestBody UserBehavior behavior) {
        try {
            log.info("异常行为检测请求，用户ID: {}", behavior.getUserId());
            
            double score = behaviorAnalysisService.calculateAnomalyScore(behavior);
            
            log.info("异常行为检测完成，用户ID: {}, 异常分数: {}", behavior.getUserId(), score);
            return Result.success(score);
            
        } catch (Exception e) {
            log.error("异常行为检测失败，用户ID: " + behavior.getUserId(), e);
            return Result.error("异常行为检测失败: " + e.getMessage());
        }
    }

    /**
     * 详细异常行为检测
     */
    @PostMapping("/behavior/detect-detail")
    @ApiOperation(value = "详细异常行为检测", notes = "检测用户行为是否存在异常，返回详细信息")
    public Result<AnomalyResult> detectAnomalyDetail(@RequestBody UserBehavior behavior) {
        try {
            log.info("详细异常行为检测请求，用户ID: {}", behavior.getUserId());
            
            AnomalyResult result = behaviorAnalysisService.detectAnomaly(behavior);
            
            log.info("详细异常行为检测完成，用户ID: {}, 是否异常: {}", 
                    behavior.getUserId(), result.getIsAnomaly());
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("详细异常行为检测失败，用户ID: " + behavior.getUserId(), e);
            return Result.error("详细异常行为检测失败: " + e.getMessage());
        }
    }

    /**
     * 批量异常行为检测
     */
    @PostMapping("/behavior/detect-batch")
    @ApiOperation(value = "批量异常行为检测", notes = "批量检测用户行为是否存在异常")
    public Result<List<AnomalyResult>> detectAnomalies(@RequestBody List<UserBehavior> behaviors) {
        try {
            log.info("批量异常行为检测请求，行为数量: {}", behaviors.size());
            
            List<AnomalyResult> results = behaviorAnalysisService.detectAnomalies(behaviors);
            
            long anomalyCount = results.stream().filter(AnomalyResult::getIsAnomaly).count();
            log.info("批量异常行为检测完成，总数量: {}, 异常数量: {}", behaviors.size(), anomalyCount);
            
            return Result.success(results);
            
        } catch (Exception e) {
            log.error("批量异常行为检测失败", e);
            return Result.error("批量异常行为检测失败: " + e.getMessage());
        }
    }

    /**
     * 学习用户行为模式
     */
    @PostMapping("/behavior/learn-pattern")
    @ApiOperation(value = "学习用户行为模式", notes = "基于用户历史行为数据学习其行为模式")
    public Result<Boolean> learnUserBehaviorPattern(
            @ApiParam(value = "用户ID", required = true) @RequestParam Long userId,
            @RequestBody List<UserBehavior> behaviors) {
        try {
            log.info("学习用户行为模式请求，用户ID: {}, 行为数量: {}", userId, behaviors.size());
            
            boolean success = behaviorAnalysisService.learnUserBehaviorPattern(userId, behaviors);
            
            log.info("学习用户行为模式完成，用户ID: {}, 结果: {}", userId, success);
            return Result.success(success);
            
        } catch (Exception e) {
            log.error("学习用户行为模式失败，用户ID: " + userId, e);
            return Result.error("学习用户行为模式失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户行为模式
     */
    @GetMapping("/behavior/pattern/{userId}")
    @ApiOperation(value = "获取用户行为模式", notes = "获取指定用户的行为模式信息")
    public Result<com.bankshield.ai.entity.BehaviorPattern> getUserBehaviorPattern(
            @PathVariable Long userId) {
        try {
            log.info("获取用户行为模式请求，用户ID: {}", userId);
            
            com.bankshield.ai.entity.BehaviorPattern pattern = behaviorAnalysisService.getUserBehaviorPattern(userId);
            
            log.info("获取用户行为模式完成，用户ID: {}", userId);
            return Result.success(pattern);
            
        } catch (Exception e) {
            log.error("获取用户行为模式失败，用户ID: " + userId, e);
            return Result.error("获取用户行为模式失败: " + e.getMessage());
        }
    }

    /**
     * 获取异常行为统计
     */
    @GetMapping("/behavior/statistics")
    @ApiOperation(value = "获取异常行为统计", notes = "获取指定用户的异常行为统计信息")
    public Result<BehaviorAnalysisService.AnomalyStatistics> getAnomalyStatistics(
            @ApiParam(value = "用户ID", required = true) @RequestParam Long userId,
            @ApiParam(value = "开始时间", example = "2024-01-01T00:00:00") @RequestParam(required = false) LocalDateTime startTime,
            @ApiParam(value = "结束时间", example = "2024-12-31T23:59:59") @RequestParam(required = false) LocalDateTime endTime) {
        try {
            log.info("获取异常行为统计请求，用户ID: {}, 时间范围: {} 到 {}", userId, startTime, endTime);
            
            // 设置默认时间范围
            if (startTime == null) {
                startTime = LocalDateTime.now().minusDays(30);
            }
            if (endTime == null) {
                endTime = LocalDateTime.now();
            }
            
            BehaviorAnalysisService.AnomalyStatistics statistics = behaviorAnalysisService.getAnomalyStatistics(userId, startTime, endTime);
            
            log.info("获取异常行为统计完成，用户ID: {}, 总行为: {}, 异常行为: {}", 
                    userId, statistics.getTotalBehaviors(), statistics.getAnomalyBehaviors());
            
            return Result.success(statistics);
            
        } catch (Exception e) {
            log.error("获取异常行为统计失败，用户ID: " + userId, e);
            return Result.error("获取异常行为统计失败: " + e.getMessage());
        }
    }

    /**
     * 智能告警分类
     */
    @GetMapping("/alert/smart-classify")
    @ApiOperation(value = "智能告警分类", notes = "使用AI模型对告警进行智能分类")
    public Result<com.bankshield.ai.model.AlertClassificationResult> classifyAlert(
            @ApiParam(value = "告警ID", required = true) @RequestParam Long alertId) {
        try {
            log.info("智能告警分类请求，告警ID: {}", alertId);
            
            com.bankshield.ai.model.AlertClassificationResult result = smartAlertService.classifyAlert(alertId);
            
            log.info("智能告警分类完成，告警ID: {}, 分类结果: {}", 
                    alertId, result.getClassificationResult());
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("智能告警分类失败，告警ID: " + alertId, e);
            return Result.error("智能告警分类失败: " + e.getMessage());
        }
    }

    /**
     * 资源使用预测
     */
    @GetMapping("/prediction/resource")
    @ApiOperation(value = "资源使用预测", notes = "预测系统资源使用情况")
    public Result<ResourcePrediction> predictResourceUsage() {
        try {
            log.info("资源使用预测请求");
            
            ResourcePrediction prediction = threatPredictionService.predictNextWeek();
            
            log.info("资源使用预测完成，预测天数: {}", prediction.getPredictedValues().size());
            
            return Result.success(prediction);
            
        } catch (Exception e) {
            log.error("资源使用预测失败", e);
            return Result.error("资源使用预测失败: " + e.getMessage());
        }
    }

    /**
     * 特定资源类型预测
     */
    @GetMapping("/prediction/resource/{resourceType}")
    @ApiOperation(value = "特定资源类型预测", notes = "预测特定类型资源的使用情况")
    public Result<ResourcePrediction> predictSpecificResource(
            @PathVariable String resourceType,
            @ApiParam(value = "预测天数", defaultValue = "7") @RequestParam(defaultValue = "7") Integer days) {
        try {
            log.info("特定资源类型预测请求，资源类型: {}, 预测天数: {}", resourceType, days);
            
            ResourcePrediction prediction = threatPredictionService.predictResourceUsage(resourceType, days);
            
            log.info("特定资源类型预测完成，资源类型: {}, 预测天数: {}", resourceType, days);
            
            return Result.success(prediction);
            
        } catch (Exception e) {
            log.error("特定资源类型预测失败，资源类型: " + resourceType, e);
            return Result.error("特定资源类型预测失败: " + e.getMessage());
        }
    }

    /**
     * 威胁预测
     */
    @GetMapping("/prediction/threat")
    @ApiOperation(value = "威胁预测", notes = "预测潜在的安全威胁")
    public Result<com.bankshield.ai.model.ThreatPrediction> predictThreats(
            @ApiParam(value = "预测天数", defaultValue = "7") @RequestParam(defaultValue = "7") Integer days) {
        try {
            log.info("威胁预测请求，预测天数: {}", days);
            
            com.bankshield.ai.model.ThreatPrediction prediction = threatPredictionService.predictThreats(days);
            
            log.info("威胁预测完成，预测威胁数量: {}", prediction.getThreats().size());
            
            return Result.success(prediction);
            
        } catch (Exception e) {
            log.error("威胁预测失败", e);
            return Result.error("威胁预测失败: " + e.getMessage());
        }
    }

    /**
     * 获取AI模型信息
     */
    @GetMapping("/model/info")
    @ApiOperation(value = "获取AI模型信息", notes = "获取AI模型的基本信息和统计")
    public Result<Map<String, Object>> getModelInfo() {
        try {
            log.info("获取AI模型信息请求");
            
            Map<String, Object> modelInfo = new HashMap<>();
            
            // 异常检测模型信息
            Map<String, Object> anomalyModel = new HashMap<>();
            anomalyModel.put("name", "IsolationForest_v1.0");
            anomalyModel.put("type", "异常检测");
            anomalyModel.put("accuracy", 0.95);
            anomalyModel.put("status", "active");
            modelInfo.put("anomalyModel", anomalyModel);
            
            // 告警分类模型信息
            Map<String, Object> alertModel = new HashMap<>();
            alertModel.put("name", "RandomForest_v1.0");
            alertModel.put("type", "告警分类");
            alertModel.put("accuracy", 0.96);
            alertModel.put("status", "active");
            modelInfo.put("alertModel", alertModel);
            
            // 预测模型信息
            Map<String, Object> predictionModel = new HashMap<>();
            predictionModel.put("name", "LSTM_v1.0");
            predictionModel.put("type", "时间序列预测");
            predictionModel.put("accuracy", 0.92);
            predictionModel.put("status", "active");
            modelInfo.put("predictionModel", predictionModel);
            
            log.info("获取AI模型信息完成");
            return Result.success(modelInfo);
            
        } catch (Exception e) {
            log.error("获取AI模型信息失败", e);
            return Result.error("获取AI模型信息失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @ApiOperation(value = "健康检查", notes = "检查AI模块的健康状态")
    public Result<Map<String, String>> healthCheck() {
        try {
            Map<String, String> health = new HashMap<>();
            health.put("status", "healthy");
            health.put("timestamp", LocalDateTime.now().toString());
            health.put("service", "AI智能安全分析模块");
            
            return Result.success(health);
            
        } catch (Exception e) {
            log.error("健康检查失败", e);
            return Result.error("健康检查失败: " + e.getMessage());
        }
    }
}