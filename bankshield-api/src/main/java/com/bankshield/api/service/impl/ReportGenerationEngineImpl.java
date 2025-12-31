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
import freemarker.template.*;
import freemarker.core.*;
import freemarker.core.Configurable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

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
            // 安全检查：验证模板内容
            validateTemplateContent(templateContent);

            // 安全数据处理：对数据进行转义和净化
            Map<String, Object> safeData = sanitizeTemplateData(data);

            // 创建安全的FreeMarker配置
            Configuration safeConfig = createSecureFreemarkerConfig();

            // 创建FreeMarker模板
            Template template = new Template("reportTemplate", new StringReader(templateContent), safeConfig);

            // 渲染模板
            StringWriter writer = new StringWriter();
            template.process(safeData, writer);

            return writer.toString();

        } catch (TemplateException | IOException e) {
            log.error("模板渲染失败: {}", e.getMessage(), e);
            throw new RuntimeException("模板渲染失败", e);
        }
    }

    /**
     * 验证模板内容，防止SSTI攻击
     */
    private void validateTemplateContent(String templateContent) {
        if (templateContent == null || templateContent.trim().isEmpty()) {
            throw new IllegalArgumentException("模板内容不能为空");
        }

        // 检查危险模式
        String[] dangerousPatterns = {
            "${",                    // 表达式替换
            "#{",                    // 数字替换
            "<#",                    // FreeMarker标签
            "<%",                    // JSP标签
            "${__",                  // Python-style expressions
            "${self",                // Self-reference
            "${main",                // Main reference
            "${root",                // Root reference
            "${globals",             // Globals reference
            "${Session",             // Session access
            "${Application}",        // Application access
            "${Request}",            // Request access
            "${Response}",           // Response access
            "${config}",             // Config access
            "freemarker.template",   // Java class access
            "getClass()",            // Class loading
            "getClassLoader()",      // ClassLoader access
            "Runtime.exec",          // Command execution
            "ProcessBuilder",        // Process building
            "java.lang.Class",       // Class manipulation
            "java.lang.Runtime",     // Runtime access
            "java.lang.Process",     // Process access
            "org.apache",            // Apache classes
            "java.io.File",          // File access
            "java.net.URL",          // URL access
            "java.net.Socket",       // Socket access
            "javax.script",          // Script engines
            "jruby",                 // JRuby
            "groovy.lang",           // Groovy
            "python",                // Python
            "javascript:",           // JavaScript
            "eval(",                 // Eval function
            "executeQuery(",         // SQL query
            "Statement",             // SQL statement
            "PreparedStatement",     // Prepared statement
            "Connection"             // DB connection
        };

        String lowerContent = templateContent.toLowerCase();
        for (String pattern : dangerousPatterns) {
            if (lowerContent.contains(pattern.toLowerCase())) {
                log.error("检测到危险的模板模式: {}", pattern);
                throw new SecurityException("模板包含危险内容: " + pattern);
            }
        }

        // 检查模板长度（防止过大的模板）
        if (templateContent.length() > 100000) {
            throw new IllegalArgumentException("模板内容过长，最大允许100KB");
        }
    }

    /**
     * 净化模板数据，防止数据注入
     */
    private Map<String, Object> sanitizeTemplateData(Map<String, Object> data) {
        if (data == null) {
            return new HashMap<>();
        }

        Map<String, Object> safeData = new HashMap<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            safeData.put(entry.getKey(), sanitizeValue(entry.getValue()));
        }

        return safeData;
    }

    /**
     * 净化单个值
     */
    private Object sanitizeValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return sanitizeString((String) value);
        } else if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> mapValue = (Map<String, Object>) value;
            return sanitizeTemplateData(mapValue);
        } else if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> listValue = (List<Object>) value;
            List<Object> safeList = new ArrayList<>();
            for (Object item : listValue) {
                safeList.add(sanitizeValue(item));
            }
            return safeList;
        } else if (value instanceof Number || value instanceof Boolean) {
            // 数字和布尔值是安全的
            return value;
        } else {
            // 其他类型转换为字符串并转义
            return sanitizeString(value.toString());
        }
    }

    /**
     * 转义字符串中的危险字符
     */
    private String sanitizeString(String str) {
        if (str == null) {
            return null;
        }

        // 转义HTML特殊字符
        str = str.replace("&", "&amp;")
                 .replace("<", "&lt;")
                 .replace(">", "&gt;")
                 .replace("\"", "&quot;")
                 .replace("'", "&#x27;");

        // 转义FreeMarker特殊字符
        str = str.replace("${", "&#36;{")
                 .replace("#{", "&#35;{");

        // 移除控制字符
        str = str.replaceAll("[\\x00-\\x1F\\x7F]", "");

        return str;
    }

    /**
     * 创建安全的FreeMarker配置
     */
    private Configuration createSecureFreemarkerConfig() {
        Configuration config = new Configuration(Configuration.VERSION_2_3_31);

        // 设置模板加载器为空（仅从字符串创建）
        config.setTemplateLoader(new StringTemplateLoader());

        // 设置默认编码
        config.setDefaultEncoding("UTF-8");

        // 禁用对某些内置函数的支持
        try {
            // 获取当前配置
            Configurable cfg = config;

            // 设置模板异常处理
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

            // 禁用显示友好错误信息
            cfg.setLogTemplateExceptions(false);

            // 限制API可用性
            config.setAPIBuiltinEnabled(false);

        } catch (Exception e) {
            log.warn("无法完全配置FreeMarker安全设置: {}", e.getMessage());
        }

        return config;
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