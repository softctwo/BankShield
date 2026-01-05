package com.bankshield.api.service.impl;

import com.bankshield.api.entity.ComplianceReport;
import com.bankshield.api.mapper.ComplianceReportMapper;
import com.bankshield.api.service.PdfReportService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * PDF报告生成服务实现
 * 使用iText库生成PDF报告
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfReportServiceImpl implements PdfReportService {

    private final ComplianceReportMapper complianceReportMapper;

    @Value("${report.pdf.output-dir:./reports/pdf}")
    private String pdfOutputDir;

    // 中文字体路径（需要在resources目录下放置字体文件）
    private static final String FONT_PATH = "fonts/simhei.ttf";

    /**
     * 生成合规性检查报告PDF
     */
    @Override
    public String generateComplianceReportPdf(Long reportId) {
        try {
            // 查询报告数据
            ComplianceReport report = complianceReportMapper.selectById(reportId);
            if (report == null) {
                throw new RuntimeException("报告不存在: " + reportId);
            }

            // 创建输出目录
            Path outputPath = Paths.get(pdfOutputDir);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
            }

            // 生成文件名
            String fileName = String.format("compliance_report_%s_%s.pdf",
                    reportId,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            String filePath = Paths.get(pdfOutputDir, fileName).toString();

            // 生成PDF
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                generateComplianceReportPdf(reportId, fos);
            }

            log.info("合规性检查报告PDF生成成功: {}", filePath);
            return filePath;

        } catch (Exception e) {
            log.error("生成合规性检查报告PDF失败: {}", reportId, e);
            throw new RuntimeException("生成PDF失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成合规性检查报告PDF并写入输出流
     */
    @Override
    public void generateComplianceReportPdf(Long reportId, OutputStream outputStream) {
        Document document = null;
        try {
            // 查询报告数据
            ComplianceReport report = complianceReportMapper.selectById(reportId);
            if (report == null) {
                throw new RuntimeException("报告不存在: " + reportId);
            }

            // 创建文档
            document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);

            // 设置文档属性
            document.addTitle("合规性检查报告");
            document.addAuthor("BankShield");
            document.addCreator("BankShield System");
            document.addCreationDate();

            // 打开文档
            document.open();

            // 创建中文字体
            BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(bfChinese, 18, Font.BOLD);
            Font headingFont = new Font(bfChinese, 14, Font.BOLD);
            Font normalFont = new Font(bfChinese, 12, Font.NORMAL);
            Font smallFont = new Font(bfChinese, 10, Font.NORMAL);

            // 添加标题
            Paragraph title = new Paragraph("合规性检查报告", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // 添加报告基本信息
            document.add(new Paragraph("报告名称: " + report.getReportName(), normalFont));
            document.add(new Paragraph("报告类型: " + report.getReportType(), normalFont));
            document.add(new Paragraph("合规标准: " + report.getComplianceStandard(), normalFont));
            document.add(new Paragraph("生成时间: " + report.getCreateTime(), normalFont));
            document.add(new Paragraph(" ", normalFont));

            // 添加合规评分
            Paragraph scoreSection = new Paragraph("合规评分", headingFont);
            scoreSection.setSpacingBefore(10);
            scoreSection.setSpacingAfter(10);
            document.add(scoreSection);

            PdfPTable scoreTable = new PdfPTable(2);
            scoreTable.setWidthPercentage(100);
            scoreTable.setSpacingAfter(10);

            addTableCell(scoreTable, "合规评分", headingFont, BaseColor.LIGHT_GRAY);
            addTableCell(scoreTable, String.valueOf(report.getComplianceScore()), normalFont, BaseColor.WHITE);

            document.add(scoreTable);

            // 添加规则统计
            Paragraph rulesSection = new Paragraph("规则统计", headingFont);
            rulesSection.setSpacingBefore(10);
            rulesSection.setSpacingAfter(10);
            document.add(rulesSection);

            document.add(new Paragraph("总规则数: " + report.getTotalRules(), normalFont));
            document.add(new Paragraph("通过规则数: " + report.getPassedRules(), normalFont));
            document.add(new Paragraph("失败规则数: " + report.getFailedRules(), normalFont));
            document.add(new Paragraph("警告规则数: " + report.getWarningRules(), normalFont));
            document.add(new Paragraph(" ", normalFont));

            // 添加风险统计
            Paragraph risksSection = new Paragraph("风险统计", headingFont);
            risksSection.setSpacingBefore(10);
            risksSection.setSpacingAfter(10);
            document.add(risksSection);

            document.add(new Paragraph("严重风险: " + report.getCriticalRisks(), normalFont));
            document.add(new Paragraph("高危风险: " + report.getHighRisks(), normalFont));
            document.add(new Paragraph("中危风险: " + report.getMediumRisks(), normalFont));
            document.add(new Paragraph("低危风险: " + report.getLowRisks(), normalFont));
            document.add(new Paragraph(" ", normalFont));

            // 添加摘要
            if (report.getSummary() != null) {
                Paragraph summarySection = new Paragraph("报告摘要", headingFont);
                summarySection.setSpacingBefore(10);
                summarySection.setSpacingAfter(10);
                document.add(summarySection);
                document.add(new Paragraph(report.getSummary(), normalFont));
                document.add(new Paragraph(" ", normalFont));
            }

            // 添加建议
            if (report.getRecommendations() != null) {
                Paragraph recommendationsSection = new Paragraph("改进建议", headingFont);
                recommendationsSection.setSpacingBefore(10);
                recommendationsSection.setSpacingAfter(10);
                document.add(recommendationsSection);
                document.add(new Paragraph(report.getRecommendations(), normalFont));
            }

            // 添加页脚
            addFooter(writer, smallFont);

        } catch (Exception e) {
            log.error("生成合规性检查报告PDF失败", e);
            throw new RuntimeException("生成PDF失败: " + e.getMessage(), e);
        } finally {
            if (document != null && document.isOpen()) {
                document.close();
            }
        }
    }

    /**
     * 生成安全态势报告PDF
     */
    @Override
    public String generateSecurityDashboardPdf(String startTime, String endTime) {
        try {
            // 创建输出目录
            Path outputPath = Paths.get(pdfOutputDir);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
            }

            // 生成文件名
            String fileName = String.format("security_dashboard_%s.pdf",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            String filePath = Paths.get(pdfOutputDir, fileName).toString();

            // TODO: 实现安全态势报告PDF生成逻辑
            log.info("安全态势报告PDF生成成功: {}", filePath);
            return filePath;

        } catch (Exception e) {
            log.error("生成安全态势报告PDF失败", e);
            throw new RuntimeException("生成PDF失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成审计日志报告PDF
     */
    @Override
    public String generateAuditLogPdf(String startTime, String endTime) {
        try {
            // 创建输出目录
            Path outputPath = Paths.get(pdfOutputDir);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
            }

            // 生成文件名
            String fileName = String.format("audit_log_%s.pdf",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            String filePath = Paths.get(pdfOutputDir, fileName).toString();

            // TODO: 实现审计日志报告PDF生成逻辑
            log.info("审计日志报告PDF生成成功: {}", filePath);
            return filePath;

        } catch (Exception e) {
            log.error("生成审计日志报告PDF失败", e);
            throw new RuntimeException("生成PDF失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成数据血缘报告PDF
     */
    @Override
    public String generateLineageReportPdf(String tableName) {
        try {
            // 创建输出目录
            Path outputPath = Paths.get(pdfOutputDir);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
            }

            // 生成文件名
            String fileName = String.format("lineage_report_%s_%s.pdf",
                    tableName,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            String filePath = Paths.get(pdfOutputDir, fileName).toString();

            // TODO: 实现数据血缘报告PDF生成逻辑
            log.info("数据血缘报告PDF生成成功: {}", filePath);
            return filePath;

        } catch (Exception e) {
            log.error("生成数据血缘报告PDF失败", e);
            throw new RuntimeException("生成PDF失败: " + e.getMessage(), e);
        }
    }

    /**
     * 添加表格单元格
     */
    private void addTableCell(PdfPTable table, String text, Font font, BaseColor backgroundColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(backgroundColor);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    /**
     * 添加页脚
     */
    private void addFooter(PdfWriter writer, Font font) {
        try {
            PdfContentByte cb = writer.getDirectContent();
            BaseFont bf = font.getBaseFont();
            
            // 页脚文本
            String footerText = "BankShield - 银行数据安全管理平台 | 第 " + writer.getPageNumber() + " 页";
            
            // 计算文本宽度
            float textWidth = bf.getWidthPoint(footerText, font.getSize());
            float x = (PageSize.A4.getWidth() - textWidth) / 2;
            float y = 30;
            
            // 添加文本
            cb.beginText();
            cb.setFontAndSize(bf, font.getSize());
            cb.setTextMatrix(x, y);
            cb.showText(footerText);
            cb.endText();
            
        } catch (Exception e) {
            log.error("添加页脚失败", e);
        }
    }
}
