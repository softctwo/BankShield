package com.bankshield.api.service;

import com.bankshield.api.entity.SecurityScanResult;
import com.bankshield.api.entity.SecurityScanTask;

import java.util.List;

/**
 * 安全扫描引擎接口
 * @author BankShield
 */
public interface SecurityScanEngine {

    /**
     * 执行漏洞扫描
     */
    List<SecurityScanResult> performVulnerabilityScan(SecurityScanTask task);

    /**
     * 执行配置检查
     */
    List<SecurityScanResult> performConfigCheck(SecurityScanTask task);

    /**
     * 执行弱密码检测
     */
    List<SecurityScanResult> performWeakPasswordCheck(SecurityScanTask task);

    /**
     * 执行异常行为检测
     */
    List<SecurityScanResult> performAnomalyDetection(SecurityScanTask task);

    /**
     * 验证修复结果
     */
    boolean verifyFix(SecurityScanResult result);

    /**
     * 生成扫描报告
     */
    String generateReport(SecurityScanTask task, List<SecurityScanResult> results);

    /**
     * 获取扫描进度
     */
    int getScanProgress(SecurityScanTask task);

    /**
     * 停止扫描任务
     */
    void stopScan(SecurityScanTask task);
}