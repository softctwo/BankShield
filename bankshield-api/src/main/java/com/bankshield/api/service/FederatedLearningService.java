package com.bankshield.api.service;

import com.bankshield.api.entity.FederatedLearningJob;
import com.bankshield.api.entity.FederatedParty;
import com.bankshield.api.entity.FederatedRound;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.Map;

/**
 * 联邦学习服务接口
 */
public interface FederatedLearningService {

    // ========== 任务管理 ==========

    /**
     * 创建联邦学习任务
     * @param job 任务信息
     * @return 创建的任务
     */
    FederatedLearningJob createJob(FederatedLearningJob job);

    /**
     * 分页查询任务列表
     */
    IPage<FederatedLearningJob> getJobs(int page, int size, String jobType, String status);

    /**
     * 获取任务详情
     */
    FederatedLearningJob getJobById(Long id);

    /**
     * 启动任务
     */
    boolean startJob(Long id);

    /**
     * 暂停任务
     */
    boolean pauseJob(Long id);

    /**
     * 恢复任务
     */
    boolean resumeJob(Long id);

    /**
     * 停止任务
     */
    boolean stopJob(Long id);

    /**
     * 删除任务
     */
    boolean deleteJob(Long id);

    // ========== 训练过程 ==========

    /**
     * 初始化全局模型
     */
    Map<String, Object> initGlobalModel(Long jobId);

    /**
     * 分发模型给参与方
     */
    boolean distributeModel(Long jobId, Integer roundNumber);

    /**
     * 接收参与方本地模型更新
     */
    boolean receiveLocalUpdate(Long jobId, String partyId, Map<String, Object> modelUpdate);

    /**
     * 执行安全聚合
     */
    Map<String, Object> aggregateModels(Long jobId, Integer roundNumber);

    /**
     * 执行一轮完整训练
     */
    FederatedRound executeRound(Long jobId, Integer roundNumber);

    /**
     * 获取训练轮次列表
     */
    List<FederatedRound> getRounds(Long jobId);

    /**
     * 获取当前全局模型
     */
    Map<String, Object> getGlobalModel(Long jobId);

    // ========== 参与方管理 ==========

    /**
     * 注册参与方
     */
    FederatedParty registerParty(FederatedParty party);

    /**
     * 获取参与方列表
     */
    List<FederatedParty> getParties(String status);

    /**
     * 获取参与方详情
     */
    FederatedParty getPartyById(Long id);

    /**
     * 更新参与方状态
     */
    boolean updatePartyStatus(Long id, String status);

    /**
     * 删除参与方
     */
    boolean deleteParty(Long id);

    /**
     * 发送心跳
     */
    boolean sendHeartbeat(String partyId);

    // ========== 安全与隐私 ==========

    /**
     * 配置差分隐私
     */
    boolean configureDifferentialPrivacy(Long jobId, Map<String, Object> privacyConfig);

    /**
     * 配置安全聚合
     */
    boolean configureSecureAggregation(Long jobId, Map<String, Object> secureConfig);

    /**
     * 验证模型完整性
     */
    Map<String, Object> verifyModelIntegrity(Long jobId);

    // ========== 统计与监控 ==========

    /**
     * 获取统计信息
     */
    Map<String, Object> getStatistics();

    /**
     * 获取任务监控数据
     */
    Map<String, Object> getJobMonitoring(Long jobId);

    /**
     * 获取训练曲线
     */
    Map<String, Object> getTrainingCurve(Long jobId);

    /**
     * 健康检查
     */
    Map<String, Object> healthCheck();
}
