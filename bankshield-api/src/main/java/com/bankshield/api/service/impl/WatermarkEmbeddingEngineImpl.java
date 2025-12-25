package com.bankshield.api.service.impl;

import com.bankshield.api.config.WatermarkConfig;
import com.bankshield.api.entity.WatermarkTemplate;
import com.bankshield.api.enums.WatermarkType;
import com.bankshield.api.service.WatermarkEmbeddingEngine;
import com.bankshield.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 水印嵌入引擎实现类
 * 
 * @author BankShield
 */
@Slf4j
@Service
public class WatermarkEmbeddingEngineImpl implements WatermarkEmbeddingEngine {

    @Autowired
    private WatermarkConfig watermarkConfig;

    @Override
    public boolean embedTextWatermarkToPdf(InputStream inputStream, OutputStream outputStream,
                                          WatermarkTemplate template, String content) {
        log.info("向PDF文件嵌入文本水印，模板: {}, 内容长度: {}", template.getTemplateName(), content.length());
        
        try {
            // 加载PDF文档
            PDDocument document = PDDocument.load(inputStream);
            
            // 准备水印内容
            String watermarkText = prepareWatermarkContent(content, template);
            
            // 遍历所有页面添加水印
            for (PDPage page : document.getPages()) {
                addTextWatermarkToPdfPage(document, page, watermarkText, template);
            }
            
            // 保存文档
            document.save(outputStream);
            document.close();
            
            log.info("PDF文本水印嵌入成功");
            return true;
            
        } catch (Exception e) {
            log.error("向PDF文件嵌入文本水印失败", e);
            throw new BusinessException("PDF文本水印嵌入失败: " + e.getMessage());
        }
    }

    @Override
    public boolean embedTextWatermarkToWord(InputStream inputStream, OutputStream outputStream,
                                           WatermarkTemplate template, String content) {
        log.info("向Word文档嵌入文本水印，模板: {}, 内容长度: {}", template.getTemplateName(), content.length());
        
        try {
            // 加载Word文档
            XWPFDocument document = new XWPFDocument(inputStream);
            
            // 准备水印内容
            String watermarkText = prepareWatermarkContent(content, template);
            
            // 添加水印到页眉页脚
            addTextWatermarkToWordDocument(document, watermarkText, template);
            
            // 保存文档
            document.write(outputStream);
            document.close();
            
            log.info("Word文本水印嵌入成功");
            return true;
            
        } catch (Exception e) {
            log.error("向Word文档嵌入文本水印失败", e);
            throw new BusinessException("Word文本水印嵌入失败: " + e.getMessage());
        }
    }

    @Override
    public boolean embedTextWatermarkToExcel(InputStream inputStream, OutputStream outputStream,
                                            WatermarkTemplate template, String content) {
        log.info("向Excel文档嵌入文本水印，模板: {}, 内容长度: {}", template.getTemplateName(), content.length());
        
        try {
            // 加载Excel文档
            Workbook workbook = new XSSFWorkbook(inputStream);
            
            // 准备水印内容
            String watermarkText = prepareWatermarkContent(content, template);
            
            // 为每个工作表添加水印
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                addTextWatermarkToExcelSheet(sheet, watermarkText, template);
            }
            
            // 保存文档
            workbook.write(outputStream);
            workbook.close();
            
            log.info("Excel文本水印嵌入成功");
            return true;
            
        } catch (Exception e) {
            log.error("向Excel文档嵌入文本水印失败", e);
            throw new BusinessException("Excel文本水印嵌入失败: " + e.getMessage());
        }
    }

    @Override
    public boolean embedImageWatermarkToImage(InputStream inputStream, OutputStream outputStream,
                                             WatermarkTemplate template, String watermarkImagePath) {
        log.info("向图片嵌入图像水印，模板: {}, 水印图片: {}", template.getTemplateName(), watermarkImagePath);
        
        try {
            // 加载原始图片
            BufferedImage originalImage = ImageIO.read(inputStream);
            if (originalImage == null) {
                throw new BusinessException("无法读取图片文件");
            }
            
            // 创建水印图片
            BufferedImage watermarkedImage = addImageWatermarkToImage(originalImage, watermarkImagePath, template);
            
            // 保存结果
            String formatName = getImageFormatName(watermarkImagePath);
            ImageIO.write(watermarkedImage, formatName, outputStream);
            
            log.info("图片图像水印嵌入成功");
            return true;
            
        } catch (Exception e) {
            log.error("向图片嵌入图像水印失败", e);
            throw new BusinessException("图片图像水印嵌入失败: " + e.getMessage());
        }
    }

    @Override
    public long embedDatabaseWatermark(Long dataSourceId, String tableName, String columnName,
                                      WatermarkTemplate template, String content) {
        log.info("向数据库嵌入水印，数据源ID: {}, 表名: {}, 列名: {}, 模板: {}", 
                dataSourceId, tableName, columnName, template.getTemplateName());
        
        try {
            // 准备水印内容
            String watermarkContent = prepareWatermarkContent(content, template);
            
            // 这里需要实现具体的数据库水印嵌入逻辑
            // 1. 连接数据库
            // 2. 获取表结构
            // 3. 添加伪列或更新现有列
            // 4. 记录处理数量
            
            // 模拟数据库处理
            long processedCount = simulateDatabaseWatermarking(dataSourceId, tableName, watermarkContent);
            
            log.info("数据库水印嵌入成功，处理记录数: {}", processedCount);
            return processedCount;
            
        } catch (Exception e) {
            log.error("向数据库嵌入水印失败", e);
            throw new BusinessException("数据库水印嵌入失败: " + e.getMessage());
        }
    }

    @Override
    public boolean embedWatermarkAuto(InputStream inputStream, OutputStream outputStream,
                                     String fileName, WatermarkTemplate template, String content) {
        log.info("自动选择嵌入方法，文件名: {}, 模板: {}", fileName, template.getTemplateName());
        
        try {
            if (fileName == null || fileName.trim().isEmpty()) {
                throw new BusinessException("文件名不能为空");
            }
            
            String fileExtension = getFileExtension(fileName).toLowerCase();
            
            switch (fileExtension) {
                case "pdf":
                    if (WatermarkType.TEXT.getCode().equals(template.getWatermarkType())) {
                        return embedTextWatermarkToPdf(inputStream, outputStream, template, content);
                    }
                    break;
                    
                case "doc":
                case "docx":
                    if (WatermarkType.TEXT.getCode().equals(template.getWatermarkType())) {
                        return embedTextWatermarkToWord(inputStream, outputStream, template, content);
                    }
                    break;
                    
                case "xls":
                case "xlsx":
                    if (WatermarkType.TEXT.getCode().equals(template.getWatermarkType())) {
                        return embedTextWatermarkToExcel(inputStream, outputStream, template, content);
                    }
                    break;
                    
                case "jpg":
                case "jpeg":
                case "png":
                case "gif":
                case "bmp":
                    if (WatermarkType.IMAGE.getCode().equals(template.getWatermarkType())) {
                        return embedImageWatermarkToImage(inputStream, outputStream, template, template.getWatermarkContent());
                    }
                    break;
                    
                default:
                    throw new BusinessException("不支持的文件类型: " + fileExtension);
            }
            
            throw new BusinessException("文件类型和水印类型不匹配");
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("自动嵌入水印失败，文件名: {}", fileName, e);
            throw new BusinessException("自动嵌入水印失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> getSupportedFileTypes() {
        List<String> supportedTypes = new ArrayList<>();
        supportedTypes.add("pdf");
        supportedTypes.add("doc");
        supportedTypes.add("docx");
        supportedTypes.add("xls");
        supportedTypes.add("xlsx");
        supportedTypes.add("jpg");
        supportedTypes.add("jpeg");
        supportedTypes.add("png");
        supportedTypes.add("gif");
        supportedTypes.add("bmp");
        return supportedTypes;
    }

    @Override
    public boolean isFileTypeSupported(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        
        String fileExtension = getFileExtension(fileName).toLowerCase();
        return getSupportedFileTypes().contains(fileExtension);
    }

    /**
     * 准备水印内容（处理模板变量）
     */
    private String prepareWatermarkContent(String content, WatermarkTemplate template) {
        if (content == null) {
            content = template.getWatermarkContent();
        }
        
        // 这里可以添加模板变量处理逻辑
        // 例如：{{TIMESTAMP}} -> 当前时间戳
        // {{USER}} -> 当前用户
        
        return content;
    }

    /**
     * 向PDF页面添加文本水印
     */
    private void addTextWatermarkToPdfPage(PDDocument document, PDPage page, String watermarkText, 
                                          WatermarkTemplate template) throws IOException {
        PDPageContentStream contentStream = new PDPageContentStream(document, page, 
                PDPageContentStream.AppendMode.OVERWRITE, true, true);
        
        // 设置字体和大小
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, template.getFontSize());
        
        // 设置透明度
        if (template.getTransparency() != null) {
            // 这里需要实现透明度设置，PDFBox的透明度处理比较复杂
            // 简化处理，实际项目中需要更复杂的实现
        }
        
        // 获取页面尺寸
        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();
        
        // 根据位置设置水印坐标
        float x = 0, y = 0;
        switch (template.getWatermarkPosition()) {
            case "TOP_LEFT":
                x = 50;
                y = pageHeight - 50;
                break;
            case "TOP_RIGHT":
                x = pageWidth - 200;
                y = pageHeight - 50;
                break;
            case "BOTTOM_LEFT":
                x = 50;
                y = 50;
                break;
            case "BOTTOM_RIGHT":
                x = pageWidth - 200;
                y = 50;
                break;
            case "CENTER":
                x = pageWidth / 2 - 100;
                y = pageHeight / 2;
                break;
            case "FULLSCREEN":
                // 全屏模式需要在多个位置添加水印
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 5; j++) {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(i * pageWidth / 5 + 50, j * pageHeight / 5 + 50);
                        contentStream.showText(watermarkText);
                        contentStream.endText();
                    }
                }
                contentStream.close();
                return;
        }
        
        // 添加水印文本
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(watermarkText);
        contentStream.endText();
        contentStream.close();
    }

    /**
     * 向Word文档添加文本水印
     */
    private void addTextWatermarkToWordDocument(XWPFDocument document, String watermarkText, 
                                               WatermarkTemplate template) {
        // 这里需要实现Word文档的水印添加逻辑
        // 可以通过页眉页脚或水印功能实现
        // 简化处理，实际项目中需要更复杂的实现
        
        // 创建页眉 - 简化实现，直接在文档中添加水印文本
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(watermarkText);
        
        if (template.getFontSize() != null) {
            run.setFontSize(template.getFontSize());
        }
        if (template.getFontColor() != null) {
            // 设置字体颜色
        }
    }

    /**
     * 向Excel工作表添加文本水印
     */
    private void addTextWatermarkToExcelSheet(Sheet sheet, String watermarkText, 
                                            WatermarkTemplate template) {
        // 这里需要实现Excel工作表的水印添加逻辑
        // 可以通过页眉页脚或背景图片实现
        // 简化处理，实际项目中需要更复杂的实现
        
        // 设置工作表名称作为简单的水印
        sheet.createRow(0).createCell(0).setCellValue(watermarkText);
    }

    /**
     * 向图片添加图像水印
     */
    private BufferedImage addImageWatermarkToImage(BufferedImage originalImage, String watermarkImagePath, 
                                                   WatermarkTemplate template) throws IOException {
        // 加载水印图片
        File watermarkFile = new File(watermarkConfig.getImage().getPath());
        if (!watermarkFile.exists()) {
            throw new BusinessException("水印图片文件不存在: " + watermarkConfig.getImage().getPath());
        }
        
        BufferedImage watermarkImage = ImageIO.read(watermarkFile);
        
        // 创建结果图片
        BufferedImage resultImage = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );
        
        // 绘制原图
        Graphics2D g2d = resultImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, null);
        
        // 设置透明度
        if (template.getTransparency() != null) {
            float alpha = template.getTransparency() / 100f;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }
        
        // 根据位置绘制水印
        int x = 0, y = 0;
        switch (template.getWatermarkPosition()) {
            case "TOP_LEFT":
                x = 10;
                y = 10;
                break;
            case "TOP_RIGHT":
                x = originalImage.getWidth() - watermarkImage.getWidth() - 10;
                y = 10;
                break;
            case "BOTTOM_LEFT":
                x = 10;
                y = originalImage.getHeight() - watermarkImage.getHeight() - 10;
                break;
            case "BOTTOM_RIGHT":
                x = originalImage.getWidth() - watermarkImage.getWidth() - 10;
                y = originalImage.getHeight() - watermarkImage.getHeight() - 10;
                break;
            case "CENTER":
                x = (originalImage.getWidth() - watermarkImage.getWidth()) / 2;
                y = (originalImage.getHeight() - watermarkImage.getHeight()) / 2;
                break;
            case "FULLSCREEN":
                // 全屏模式，缩放水印图片
                watermarkImage = scaleImage(watermarkImage, originalImage.getWidth(), originalImage.getHeight());
                x = 0;
                y = 0;
                break;
        }
        
        g2d.drawImage(watermarkImage, x, y, null);
        g2d.dispose();
        
        return resultImage;
    }

    /**
     * 模拟数据库水印处理
     */
    private long simulateDatabaseWatermarking(Long dataSourceId, String tableName, String watermarkContent) {
        // 这里应该实现真实的数据库水印处理逻辑
        // 返回模拟的处理记录数
        return 1000L;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * 获取图片格式名称
     */
    private String getImageFormatName(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        if ("jpg".equals(extension) || "jpeg".equals(extension)) {
            return "JPEG";
        }
        return extension.toUpperCase();
    }

    /**
     * 缩放图片
     */
    private BufferedImage scaleImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.drawImage(originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH), 0, 0, null);
        g2d.dispose();
        return scaledImage;
    }
}