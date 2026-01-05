package com.bankshield.api.service;

import com.bankshield.api.entity.ComplianceReport;

import java.io.OutputStream;

/**
 * PDF报告生成服务接口
 */
public interface PdfReportService {

    /**
     * 生成合规性检查报告PDF
     *
     * @param reportId 报告ID
     * @return PDF文件路径
     */
    String generateComplianceReportPdf(Long reportId);

    /**
     * 生成合规性检查报告PDF并写入输出流
     *
     * @param reportId 报告ID
     * @param outputStream 输出流
     */
    void generateComplianceReportPdf(Long reportId, OutputStream outputStream);

    /**
     * 生成安全态势报告PDF
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return PDF文件路径
     */
    String generateSecurityDashboardPdf(String startTime, String endTime);

    /**
     * 生成审计日志报告PDF
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return PDF文件路径
     */
    String generateAuditLogPdf(String startTime, String endTime);

    /**
     * 生成数据血缘报告PDF
     *
     * @param tableName 表名
     * @return PDF文件路径
     */
    String generateLineageReportPdf(String tableName);
}
