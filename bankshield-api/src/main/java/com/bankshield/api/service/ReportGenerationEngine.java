package com.bankshield.api.service;

import com.bankshield.api.entity.ReportTemplate;
import com.bankshield.api.entity.ReportGenerationTask;

import java.util.Map;

/**
 * 报表生成引擎接口
 */
public interface ReportGenerationEngine {
    
    /**
     * 生成报表
     */
    String generateReport(ReportTemplate template, ReportGenerationTask task, Map<String, Object> data);
    
    /**
     * 收集报表数据
     */
    Map<String, Object> collectReportData(ReportTemplate template, ReportGenerationTask task);
    
    /**
     * 渲染报表模板
     */
    String renderTemplate(String templateContent, Map<String, Object> data);
    
    /**
     * 生成PDF报表
     */
    byte[] generatePdfReport(String htmlContent);
    
    /**
     * 生成Excel报表
     */
    byte[] generateExcelReport(Map<String, Object> data);
    
    /**
     * 保存报表文件
     */
    String saveReportFile(String content, String fileName, String fileType);
}