package com.bankshield.api.service;

import com.bankshield.api.entity.MpcJob;
import com.bankshield.api.entity.MpcParty;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.Map;

/**
 * MPC多方安全计算服务接口
 */
public interface MpcService {

    // ========== PSI隐私求交 ==========

    /**
     * 创建PSI任务
     * @param jobName 任务名称
     * @param participantIds 参与方ID列表
     * @param localDataSet 本地数据集
     * @return 任务信息
     */
    MpcJob createPSIJob(String jobName, List<String> participantIds, List<String> localDataSet);

    /**
     * 执行PSI计算
     * @param jobId 任务ID
     * @return 交集结果
     */
    Map<String, Object> executePSI(Long jobId);

    // ========== 安全求和 ==========

    /**
     * 创建安全求和任务
     * @param jobName 任务名称
     * @param participantIds 参与方ID列表
     * @param localValue 本地数值
     * @return 任务信息
     */
    MpcJob createSecureSumJob(String jobName, List<String> participantIds, Double localValue);

    /**
     * 执行安全求和
     * @param jobId 任务ID
     * @return 求和结果
     */
    Map<String, Object> executeSecureSum(Long jobId);

    // ========== 联合查询 ==========

    /**
     * 创建联合查询任务
     * @param jobName 任务名称
     * @param participantIds 参与方ID列表
     * @param queryParams 查询参数
     * @return 任务信息
     */
    MpcJob createJointQueryJob(String jobName, List<String> participantIds, Map<String, Object> queryParams);

    /**
     * 执行联合查询
     * @param jobId 任务ID
     * @return 查询结果
     */
    Map<String, Object> executeJointQuery(Long jobId);

    // ========== 安全比较 ==========

    /**
     * 创建安全比较任务
     * @param jobName 任务名称
     * @param participantId 对比方ID
     * @param localValue 本地数值
     * @return 任务信息
     */
    MpcJob createSecureCompareJob(String jobName, String participantId, Double localValue);

    /**
     * 执行安全比较
     * @param jobId 任务ID
     * @return 比较结果（大于/等于/小于）
     */
    Map<String, Object> executeSecureCompare(Long jobId);

    // ========== 任务管理 ==========

    /**
     * 分页查询任务列表
     */
    IPage<MpcJob> getJobs(int page, int size, String jobType, String status);

    /**
     * 获取任务详情
     */
    MpcJob getJobById(Long id);

    /**
     * 取消任务
     */
    boolean cancelJob(Long id);

    /**
     * 重试任务
     */
    MpcJob retryJob(Long id);

    // ========== 参与方管理 ==========

    /**
     * 注册参与方
     */
    MpcParty registerParty(MpcParty party);

    /**
     * 获取参与方列表
     */
    List<MpcParty> getParties(String status);

    /**
     * 获取参与方详情
     */
    MpcParty getPartyById(Long id);

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

    // ========== 统计与监控 ==========

    /**
     * 获取MPC统计信息
     */
    Map<String, Object> getStatistics();

    /**
     * 获取协议信息
     */
    Map<String, Object> getProtocols();

    /**
     * 健康检查
     */
    Map<String, Object> healthCheck();
}
