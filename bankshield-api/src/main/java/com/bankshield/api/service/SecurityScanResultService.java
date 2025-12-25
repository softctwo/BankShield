package com.bankshield.api.service;

import com.bankshield.api.entity.SecurityScanResult;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 安全扫描结果服务接口
 * @author BankShield
 */
public interface SecurityScanResultService extends IService<SecurityScanResult> {

    /**
     * 获取扫描结果列表
     */
    IPage<SecurityScanResult> getScanResults(int page, int size, Long taskId, String riskLevel, String fixStatus);

    /**
     * 标记风险为已修复
     */
    boolean markAsFixed(Long resultId, String fixRemark);

    /**
     * 批量标记风险为已修复
     */
    boolean batchMarkAsFixed(List<Long> resultIds, String fixRemark);

    /**
     * 根据任务ID获取扫描结果
     */
    List<SecurityScanResult> getResultsByTaskId(Long taskId);

    /**
     * 统计风险数量
     */
    long countByTaskIdAndRiskLevel(Long taskId, String riskLevel);

    /**
     * 统计修复状态
     */
    long countByTaskIdAndFixStatus(Long taskId, String fixStatus);
}