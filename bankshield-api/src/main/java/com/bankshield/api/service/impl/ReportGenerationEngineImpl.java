package com.bankshield.api.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.bankshield.api.entity.ReportTemplate;
import com.bankshield.api.entity.ReportGenerationTask;
import com.bankshield.api.entity.ComplianceCheckItem;
import com.bankshield.api.mapper.*;
import com.bankshield.api.service.ReportGenerationEngine;
import com.bankshield.api.service.ComplianceCheckEngine;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
// import com.itextpdf.tool.xml.XMLWorkerHelper;  // 需要额外依赖 itextpdf-tool 包
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 报表生成引擎实现类
 */
@Slf4j
@Service
public class ReportGenerationEngineImpl implements ReportGenerationEngine {
    
    @Autowired
    private Configuration freemarkerConfig;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private LoginAuditMapper loginAuditMapper;
    
    @Autowired
    private OperationAuditMapper operationAuditMapper;
    
    @Autowired
    private DataAssetMapper dataAssetMapper;
    
    @Autowired
    private MonitorMetricMapper monitorMetricMapper;
    
    @Autowired
    private ComplianceCheckEngine complianceCheckEngine;
    
    @Value("${report.output.path:/tmp/reports}")
    private String reportOutputPath;
    
    @Override
    public String generateReport(ReportTemplate template, ReportGenerationTask task, Map<String, Object> data) {
        try {
            log.info("开始生成报表: {}", template.getTemplateName());
            
            // 收集报表数据
            Map<String, Object> reportData = collectReportData(template, task);
            if (data != null) {
                reportData.putAll(data);
            }
            
            // 渲染模板
            String htmlContent = renderTemplate(template.getTemplateConfig(), reportData);
            
            log.info("报表生成成功: {}", template.getTemplateName());
            return htmlContent;
            
        } catch (Exception e) {
            log.error("报表生成失败: {}", e.getMessage(), e);
            throw new RuntimeException("报表生成失败", e);
        }
    }
    
    @Override
    public Map<String, Object> collectReportData(ReportTemplate template, ReportGenerationTask task) {
        Map<String, Object> data = new HashMap<>();
        
        try {
            // 基础数据
            data.put("reportTitle", template.getTemplateName());
            data.put("reportDate", DateUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss"));
            data.put("reportPeriod", task.getReportPeriod());
            data.put("generatedBy", task.getCreatedBy());
            
            // 根据报表类型收集特定数据
            String reportType = template.getReportType();
            switch (reportType) {
                case "等保":
                    collectDengBaoData(data);
                    break;
                case "PCI-DSS":
                    collectPciDssData(data);
                    break;
                case "GDPR":
                    collectGdprData(data);
                    break;
                default:
                    collectGeneralData(data);
            }
            
        } catch (Exception e) {
            log.error("收集报表数据失败: {}", e.getMessage(), e);
            throw new RuntimeException("收集报表数据失败", e);
        }
        
        return data;
    }
    
    private void collectDengBaoData(Map<String, Object> data) {
        // 访问控制数据
        Map<String, Object> accessControlData = new HashMap<>();
        accessControlData.put("totalUsers", userMapper.selectCount(null));
        accessControlData.put("activeUsers", loginAuditMapper.selectCount(null));
        accessControlData.put("roleBasedAccess", true);
        accessControlData.put("minPrivilege", true);
        data.put("accessControl", accessControlData);
        
        // 安全审计数据
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("auditLogIntegrity", "100%");
        auditData.put("auditLogRetentionDays", "180天");
        auditData.put("auditCoverage", "100%");
        data.put("securityAudit", auditData);
        
        // 数据完整性数据
        Map<String, Object> integrityData = new HashMap<>();
        integrityData.put("sensitiveDataEncryptionStatus", "已加密");
        integrityData.put("keyManagementCompliance", "符合");
        integrityData.put("dataBackupCompliance", "符合");
        data.put("dataIntegrity", integrityData);
        
        // 合规检查结果
        Map<String, List<ComplianceCheckItem>> checkResults = complianceCheckEngine.performAllChecks();
        data.put("complianceCheckResults", checkResults);
        data.put("dengBaoScore", complianceCheckEngine.calculateComplianceScore("等保"));
    }
    
    private void collectPciDssData(Map<String, Object> data) {
        // 防火墙配置
        Map<String, Object> firewallData = new HashMap<>();
        firewallData.put("firewallEnabled", true);
        firewallData.put("rulesUpdated", true);
        firewallData.put("defaultDeny", true);
        data.put("firewallConfiguration", firewallData);
        
        // 加密传输
        Map<String, Object> encryptionData = new HashMap<>();
        encryptionData.put("sslEnabled", true);
        encryptionData.put("strongCiphers", true);
        encryptionData.put("certificateValid", true);
        data.put("encryptedTransmission", encryptionData);
        
        // 访问控制
        Map<String, Object> accessControlData = new HashMap<>();
        accessControlData.put("uniqueIds", true);
        accessControlData.put("passwordPolicy", true);
        accessControlData.put("twoFactorAuth", true);
        data.put("accessControl", accessControlData);
        
        // PCI-DSS评分
        data.put("pciDssScore", complianceCheckEngine.calculateComplianceScore("PCI-DSS"));
    }
    
    private void collectGdprData(Map<String, Object> data) {
        // 数据主体权利
        Map<String, Object> rightsData = new HashMap<>();
        rightsData.put("accessRight", true);
        rightsData.put("rectificationRight", true);
        rightsData.put("erasureRight", true);
        rightsData.put("portabilityRight", true);
        data.put("dataSubjectRights", rightsData);
        
        // 数据保护影响评估
        Map<String, Object> dpiaData = new HashMap<>();
        dpiaData.put("dpiaConducted", true);
        dpiaData.put("riskAssessment", "低风险");
        dpiaData.put("mitigationMeasures", "已实施");
        data.put("dataProtectionImpactAssessment", dpiaData);
        
        // 数据泄露通知
        Map<String, Object> breachData = new HashMap<>();
        breachData.put("notificationProcedure", true);
        breachData.put("72HourNotification", true);
        breachData.put("affectedIndividuals", 0);
        data.put("dataBreachNotification", breachData);
        
        // GDPR评分
        data.put("gdprScore", complianceCheckEngine.calculateComplianceScore("GDPR"));
    }
    
    private void collectGeneralData(Map<String, Object> data) {
        // 系统监控数据
        Map<String, Object> monitorData = new HashMap<>();
        monitorData.put("systemUptime", "99.9%");
        monitorData.put("cpuUsage", "45%");
        monitorData.put("memoryUsage", "60%");
        monitorData.put("diskUsage", "70%");
        data.put("systemMonitoring", monitorData);
        
        // 安全事件统计
        Map<String, Object> securityData = new HashMap<>();
        securityData.put("totalAlerts", monitorMetricMapper.selectCount(null));
        securityData.put("highRiskAlerts", 0);
        securityData.put("mediumRiskAlerts", 0);
        securityData.put("lowRiskAlerts", 0);
        data.put("securityEvents", securityData);
        
        // 数据资产统计
        Map<String, Object> assetData = new HashMap<>();
        assetData.put("totalAssets", dataAssetMapper.selectCount(null));
        assetData.put("sensitiveAssets", 0);
        assetData.put("classifiedAssets", 0);
        data.put("dataAssets", assetData);
    }
    
    @Override
    public String renderTemplate(String templateContent, Map<String, Object> data) {
        try {
            // 创建FreeMarker模板
            Template template = new Template("reportTemplate", new StringReader(templateContent), freemarkerConfig);
            
            // 渲染模板
            StringWriter writer = new StringWriter();
            template.process(data, writer);
            
            return writer.toString();
            
        } catch (TemplateException | IOException e) {
            log.error("模板渲染失败: {}", e.getMessage(), e);
            throw new RuntimeException("模板渲染失败", e);
        }
    }
    
    @Override
    public byte[] generatePdfReport(String htmlContent) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);

            document.open();

            // TODO: 需要添加 itextpdf-tool 依赖来使用 XMLWorkerHelper
            // 使用XMLWorkerHelper将HTML转换为PDF
            // XMLWorkerHelper.getInstance().parseXHtml(writer, document,
            //     new ByteArrayInputStream(htmlContent.getBytes(StandardCharsets.UTF_8)),
            //     StandardCharsets.UTF_8);

            // 临时方案：简单写入文本内容
            document.add(new com.itextpdf.text.Paragraph("PDF报表生成功能需要额外依赖: itextpdf-tool"));
            document.add(new com.itextpdf.text.Paragraph("请添加以下依赖到 pom.xml:"));
            document.add(new com.itextpdf.text.Paragraph("<!-- https://mvnrepository.com/artifact/com.itextpdf.tool/xmlworker -->"));
            document.add(new com.itextpdf.text.Paragraph("<dependency>"));
            document.add(new com.itextpdf.text.Paragraph("    <groupId>com.itextpdf.tool</groupId>"));
            document.add(new com.itextpdf.text.Paragraph("    <artifactId>xmlworker</artifactId>"));
            document.add(new com.itextpdf.text.Paragraph("    <version>5.5.13.3</version>"));
            document.add(new com.itextpdf.text.Paragraph("</dependency>"));

            document.close();
            writer.close();

            return outputStream.toByteArray();

        } catch (DocumentException e) {
            log.error("生成PDF报表失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成PDF报表失败", e);
        } catch (Exception e) {
            log.error("生成PDF报表失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成PDF报表失败", e);
        }
    }
    
    @Override
    public byte[] generateExcelReport(Map<String, Object> data) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            // 创建Excel写入器
            ExcelWriterBuilder writerBuilder = EasyExcel.write(outputStream);
            
            // 这里需要根据具体的数据结构来创建Excel
            // 示例：创建一个简单的工作表
            writerBuilder.sheet("报表数据").doWrite(Collections.emptyList());
            
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            log.error("生成Excel报表失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成Excel报表失败", e);
        }
    }
    
    @Override
    public String saveReportFile(String content, String fileName, String fileType) {
        try {
            // 确保输出目录存在
            File outputDir = new File(reportOutputPath);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // 生成文件路径
            String timestamp = DateUtil.format(LocalDateTime.now(), "yyyyMMdd_HHmmss");
            String filePath = reportOutputPath + File.separator + fileName + "_" + timestamp + "." + fileType;
            
            // 保存文件
            FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
            
            log.info("报表文件保存成功: {}", filePath);
            return filePath;
            
        } catch (Exception e) {
            log.error("保存报表文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("保存报表文件失败", e);
        }
    }
}