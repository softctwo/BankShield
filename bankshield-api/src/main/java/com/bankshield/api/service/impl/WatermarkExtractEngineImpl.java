package com.bankshield.api.service.impl;

import com.bankshield.api.config.WatermarkConfig;
import com.bankshield.api.entity.WatermarkTemplate;
import com.bankshield.api.service.WatermarkExtractEngine;
import com.bankshield.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 水印提取引擎实现类
 * 
 * @author BankShield
 */
@Slf4j
@Service
public class WatermarkExtractEngineImpl implements WatermarkExtractEngine {

    @Autowired
    private WatermarkConfig watermarkConfig;

    // 预定义的水印模式
    private static final List<Pattern> WATERMARK_PATTERNS = Arrays.asList(
        Pattern.compile("BankShield\\\\s*-\\\\s*([^-]+)\\\\s*-\\\\s*([\\\\d\\\\-\\\\s:]+)"),
        Pattern.compile("BankShield\\s*-(\\s*[^-]+\\s*)-\\s*(\\w+)\\s*([\\d\\-\\s:]+)"),
        Pattern.compile("([^@\\s]+)\\s*_\\s*([\\d\\-\\s:]+)"),
        Pattern.compile("TX_\\s*([^_]+)\\s*_\\s*([\\d\\-\\s:]+)")
    );

    @Override
    public String extractFromPdf(InputStream inputStream) {
        log.info("从PDF文件中提取水印");
        
        try {
            // 加载PDF文档
            PDDocument document = PDDocument.load(inputStream);
            
            // 提取文本内容
            PDFTextStripper textStripper = new PDFTextStripper();
            String textContent = textStripper.getText(document);
            
            document.close();
            
            // 从文本中提取水印
            String watermarkContent = extractWatermarkFromText(textContent);
            
            log.info("PDF水印提取完成，结果: {}", watermarkContent != null ? "成功" : "失败");
            return watermarkContent;
            
        } catch (Exception e) {
            log.error("从PDF文件中提取水印失败", e);
            throw new BusinessException("PDF水印提取失败: " + e.getMessage());
        }
    }

    @Override
    public String extractFromWord(InputStream inputStream) {
        log.info("从Word文档中提取水印");
        
        try {
            // 加载Word文档
            XWPFDocument document = new XWPFDocument(inputStream);
            
            // 提取所有段落的文本
            StringBuilder textContent = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                textContent.append(paragraph.getText()).append("\n");
            }
            
            document.close();
            
            // 从文本中提取水印
            String watermarkContent = extractWatermarkFromText(textContent.toString());
            
            log.info("Word水印提取完成，结果: {}", watermarkContent != null ? "成功" : "失败");
            return watermarkContent;
            
        } catch (Exception e) {
            log.error("从Word文档中提取水印失败", e);
            throw new BusinessException("Word水印提取失败: " + e.getMessage());
        }
    }

    @Override
    public String extractFromExcel(InputStream inputStream) {
        log.info("从Excel文档中提取水印");
        
        try {
            // 加载Excel文档
            Workbook workbook = new XSSFWorkbook(inputStream);
            
            // 提取所有工作表的内容
            StringBuilder textContent = new StringBuilder();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                textContent.append("Sheet: ").append(sheet.getSheetName()).append("\n");
                
                // 提取单元格内容
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        textContent.append(getCellContent(cell)).append("\t");
                    }
                    textContent.append("\n");
                }
                textContent.append("\n");
            }
            
            workbook.close();
            
            // 从文本中提取水印
            String watermarkContent = extractWatermarkFromText(textContent.toString());
            
            log.info("Excel水印提取完成，结果: {}", watermarkContent != null ? "成功" : "失败");
            return watermarkContent;
            
        } catch (Exception e) {
            log.error("从Excel文档中提取水印失败", e);
            throw new BusinessException("Excel水印提取失败: " + e.getMessage());
        }
    }

    @Override
    public String extractFromImage(InputStream inputStream) {
        log.info("从图片中提取水印");
        
        try {
            // 加载图片
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new BusinessException("无法读取图片文件");
            }
            
            // 从图片中提取水印
            String watermarkContent = extractWatermarkFromImage(image);
            
            log.info("图片水印提取完成，结果: {}", watermarkContent != null ? "成功" : "失败");
            return watermarkContent;
            
        } catch (Exception e) {
            log.error("从图片中提取水印失败", e);
            throw new BusinessException("图片水印提取失败: " + e.getMessage());
        }
    }

    @Override
    public String extractFromDatabase(Long dataSourceId, String tableName, String columnName) {
        log.info("从数据库中提取水印，数据源ID: {}, 表名: {}, 列名: {}", dataSourceId, tableName, columnName);
        
        try {
            // 这里需要实现具体的数据库水印提取逻辑
            // 1. 连接数据库
            // 2. 查询伪列或水印列
            // 3. 提取水印内容
            
            // 模拟数据库水印提取
            String watermarkContent = simulateDatabaseWatermarkExtraction(dataSourceId, tableName, columnName);
            
            log.info("数据库水印提取完成，结果: {}", watermarkContent != null ? "成功" : "失败");
            return watermarkContent;
            
        } catch (Exception e) {
            log.error("从数据库中提取水印失败", e);
            throw new BusinessException("数据库水印提取失败: " + e.getMessage());
        }
    }

    @Override
    public String extractWatermarkAuto(InputStream inputStream, String fileName) {
        log.info("自动选择提取方法，文件名: {}", fileName);
        
        try {
            if (fileName == null || fileName.trim().isEmpty()) {
                throw new BusinessException("文件名不能为空");
            }
            
            String fileExtension = getFileExtension(fileName).toLowerCase();
            
            switch (fileExtension) {
                case "pdf":
                    return extractFromPdf(inputStream);
                    
                case "doc":
                case "docx":
                    return extractFromWord(inputStream);
                    
                case "xls":
                case "xlsx":
                    return extractFromExcel(inputStream);
                    
                case "jpg":
                case "jpeg":
                case "png":
                case "gif":
                case "bmp":
                    return extractFromImage(inputStream);
                    
                default:
                    throw new BusinessException("不支持的文件类型: " + fileExtension);
            }
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("自动提取水印失败，文件名: {}", fileName, e);
            throw new BusinessException("自动提取水印失败: " + e.getMessage());
        }
    }

    @Override
    public boolean hasWatermark(InputStream inputStream, String fileName) {
        log.info("验证文件是否包含水印，文件名: {}", fileName);
        
        try {
            String watermarkContent = extractWatermarkAuto(inputStream, fileName);
            boolean hasWatermark = watermarkContent != null && !watermarkContent.trim().isEmpty();
            
            log.info("文件水印验证完成，结果: {}", hasWatermark ? "包含水印" : "不包含水印");
            return hasWatermark;
            
        } catch (Exception e) {
            log.error("验证文件是否包含水印失败，文件名: {}", fileName, e);
            return false;
        }
    }

    @Override
    public boolean verifyWatermarkIntegrity(String watermarkContent) {
        if (watermarkContent == null || watermarkContent.trim().isEmpty()) {
            return false;
        }
        
        try {
            // 验证水印内容的完整性
            // 检查是否包含预期的格式和关键信息
            
            // 检查是否包含BankShield标识
            if (!watermarkContent.contains("BankShield")) {
                return false;
            }
            
            // 检查内容长度和格式
            if (watermarkContent.length() < 10) {
                return false;
            }
            
            // 检查是否符合预期的格式模式
            for (Pattern pattern : WATERMARK_PATTERNS) {
                Matcher matcher = pattern.matcher(watermarkContent);
                if (matcher.find()) {
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("验证水印完整性失败", e);
            return false;
        }
    }

    @Override
    public Map<String, Object> parseWatermarkContent(String watermarkContent) {
        Map<String, Object> parsedContent = new HashMap<>();
        
        if (watermarkContent == null || watermarkContent.trim().isEmpty()) {
            return parsedContent;
        }
        
        try {
            // 解析水印内容
            parsedContent.put("rawContent", watermarkContent);
            parsedContent.put("hasBankShield", watermarkContent.contains("BankShield"));
            
            // 尝试匹配不同的模式
            for (Pattern pattern : WATERMARK_PATTERNS) {
                Matcher matcher = pattern.matcher(watermarkContent);
                if (matcher.find()) {
                    parsedContent.put("pattern", pattern.pattern());
                    parsedContent.put("matches", matcher.groupCount());
                    
                    for (int i = 1; i <= matcher.groupCount(); i++) {
                        parsedContent.put("group" + i, matcher.group(i));
                    }
                    
                    break;
                }
            }
            
            // 提取时间戳
            String timestamp = extractTimestamp(watermarkContent);
            if (timestamp != null) {
                parsedContent.put("timestamp", timestamp);
            }
            
            // 提取用户信息
            String userInfo = extractUserInfo(watermarkContent);
            if (userInfo != null) {
                parsedContent.put("user", userInfo);
            }
            
            return parsedContent;
            
        } catch (Exception e) {
            log.error("解析水印内容失败", e);
            return parsedContent;
        }
    }

    @Override
    public List<String> getSupportedFileTypes() {
        return Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "jpg", "jpeg", "png", "gif", "bmp");
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
     * 从文本中提取水印
     */
    private String extractWatermarkFromText(String textContent) {
        if (textContent == null || textContent.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 使用预定义的模式匹配水印
            for (Pattern pattern : WATERMARK_PATTERNS) {
                Matcher matcher = pattern.matcher(textContent);
                if (matcher.find()) {
                    return matcher.group(0); // 返回完整匹配的水印内容
                }
            }
            
            // 如果没有匹配到预定义模式，尝试查找包含BankShield的内容
            if (textContent.contains("BankShield")) {
                // 提取包含BankShield的整行或段落
                String[] lines = textContent.split("\n");
                for (String line : lines) {
                    if (line.contains("BankShield")) {
                        return line.trim();
                    }
                }
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("从文本中提取水印失败", e);
            return null;
        }
    }

    /**
     * 从图片中提取水印
     */
    private String extractWatermarkFromImage(BufferedImage image) {
        if (image == null) {
            return null;
        }
        
        try {
            // 这里需要实现图片水印提取算法
            // 1. 分析图片的LSB（最低有效位）
            // 2. 检测透明度变化
            // 3. 提取隐藏的水印信息
            
            // 简化处理：检查图片元数据或特定区域
            String extractedText = extractTextFromImagePixels(image);
            
            if (extractedText != null && !extractedText.trim().isEmpty()) {
                // 验证提取的内容是否符合水印格式
                if (verifyWatermarkIntegrity(extractedText)) {
                    return extractedText;
                }
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("从图片中提取水印失败", e);
            return null;
        }
    }

    /**
     * 从图片像素中提取文本
     */
    private String extractTextFromImagePixels(BufferedImage image) {
        // 这里需要实现具体的图片隐写术提取算法
        // 简化处理：检查特定区域的像素值
        
        try {
            int width = image.getWidth();
            int height = image.getHeight();
            
            // 检查图片的元数据或特定区域
            // 这里只是一个简化的示例
            
            // 检查角落区域（常见的水印位置）
            StringBuilder extractedText = new StringBuilder();
            
            // 检查右下角区域
            int cornerSize = Math.min(width, height) / 10;
            for (int y = height - cornerSize; y < height; y++) {
                for (int x = width - cornerSize; x < width; x++) {
                    int pixel = image.getRGB(x, y);
                    // 简单的像素值到字符的转换（实际需要更复杂的算法）
                    char character = (char) (pixel & 0xFF);
                    if (character >= 32 && character <= 126) { // 可打印字符
                        extractedText.append(character);
                    }
                }
            }
            
            String result = extractedText.toString().trim();
            return result.isEmpty() ? null : result;
            
        } catch (Exception e) {
            log.error("从图片像素中提取文本失败", e);
            return null;
        }
    }

    /**
     * 模拟数据库水印提取
     */
    private String simulateDatabaseWatermarkExtraction(Long dataSourceId, String tableName, String columnName) {
        // 这里应该实现真实的数据库水印提取逻辑
        // 返回模拟的水印内容
        return "BankShield - Database - " + tableName + " - " + System.currentTimeMillis();
    }

    /**
     * 获取单元格内容
     */
    private String getCellContent(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * 提取时间戳
     */
    private String extractTimestamp(String content) {
        if (content == null) {
            return null;
        }
        
        // 匹配日期时间格式
        Pattern timestampPattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}");
        Matcher matcher = timestampPattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(0);
        }
        
        return null;
    }

    /**
     * 提取用户信息
     */
    private String extractUserInfo(String content) {
        if (content == null) {
            return null;
        }
        
        // 匹配用户名模式（简化处理）
        Pattern userPattern = Pattern.compile("user:(\\w+)");
        Matcher matcher = userPattern.matcher(content.toLowerCase());
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
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
}