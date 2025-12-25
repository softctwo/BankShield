package com.bankshield.api.service;

import com.bankshield.api.entity.WatermarkTemplate;

import java.io.File;
import java.io.InputStream;

/**
 * 水印提取引擎接口
 * 
 * @author BankShield
 */
public interface WatermarkExtractEngine {

    /**
     * 从PDF文件中提取水印
     * 
     * @param inputStream 输入流
     * @return 提取的水印内容
     */
    String extractFromPdf(InputStream inputStream);

    /**
     * 从Word文档中提取水印
     * 
     * @param inputStream 输入流
     * @return 提取的水印内容
     */
    String extractFromWord(InputStream inputStream);

    /**
     * 从Excel文档中提取水印
     * 
     * @param inputStream 输入流
     * @return 提取的水印内容
     */
    String extractFromExcel(InputStream inputStream);

    /**
     * 从图片中提取水印
     * 
     * @param inputStream 输入流
     * @return 提取的水印内容
     */
    String extractFromImage(InputStream inputStream);

    /**
     * 从数据库中提取水印
     * 
     * @param dataSourceId 数据源ID
     * @param tableName 表名
     * @param columnName 列名
     * @return 提取的水印内容
     */
    String extractFromDatabase(Long dataSourceId, String tableName, String columnName);

    /**
     * 根据文件类型自动选择提取方法
     * 
     * @param inputStream 输入流
     * @param fileName 文件名
     * @return 提取的水印内容
     */
    String extractWatermarkAuto(InputStream inputStream, String fileName);

    /**
     * 验证文件是否包含水印
     * 
     * @param inputStream 输入流
     * @param fileName 文件名
     * @return 是否包含水印
     */
    boolean hasWatermark(InputStream inputStream, String fileName);

    /**
     * 检测水印的完整性
     * 
     * @param watermarkContent 水印内容
     * @return 是否完整
     */
    boolean verifyWatermarkIntegrity(String watermarkContent);

    /**
     * 解析水印内容
     * 
     * @param watermarkContent 水印内容
     * @return 解析后的信息
     */
    java.util.Map<String, Object> parseWatermarkContent(String watermarkContent);

    /**
     * 支持的文件类型
     * 
     * @return 支持的文件类型列表
     */
    java.util.List<String> getSupportedFileTypes();

    /**
     * 验证文件类型是否支持提取
     * 
     * @param fileName 文件名
     * @return 是否支持
     */
    boolean isFileTypeSupported(String fileName);
}