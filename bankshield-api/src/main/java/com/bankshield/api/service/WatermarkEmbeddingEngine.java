package com.bankshield.api.service;

import com.bankshield.api.entity.WatermarkTemplate;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 水印嵌入引擎接口
 * 
 * @author BankShield
 */
public interface WatermarkEmbeddingEngine {

    /**
     * 嵌入文本水印到PDF文件
     * 
     * @param inputStream 输入流
     * @param outputStream 输出流
     * @param template 水印模板
     * @param content 水印内容
     * @return 是否成功
     */
    boolean embedTextWatermarkToPdf(InputStream inputStream, OutputStream outputStream,
                                   WatermarkTemplate template, String content);

    /**
     * 嵌入文本水印到Word文档
     * 
     * @param inputStream 输入流
     * @param outputStream 输出流
     * @param template 水印模板
     * @param content 水印内容
     * @return 是否成功
     */
    boolean embedTextWatermarkToWord(InputStream inputStream, OutputStream outputStream,
                                    WatermarkTemplate template, String content);

    /**
     * 嵌入文本水印到Excel文档
     * 
     * @param inputStream 输入流
     * @param outputStream 输出流
     * @param template 水印模板
     * @param content 水印内容
     * @return 是否成功
     */
    boolean embedTextWatermarkToExcel(InputStream inputStream, OutputStream outputStream,
                                     WatermarkTemplate template, String content);

    /**
     * 嵌入图像水印到图片
     * 
     * @param inputStream 输入流
     * @param outputStream 输出流
     * @param template 水印模板
     * @param watermarkImagePath 水印图片路径
     * @return 是否成功
     */
    boolean embedImageWatermarkToImage(InputStream inputStream, OutputStream outputStream,
                                      WatermarkTemplate template, String watermarkImagePath);

    /**
     * 嵌入数据库水印
     * 
     * @param dataSourceId 数据源ID
     * @param tableName 表名
     * @param columnName 列名
     * @param template 水印模板
     * @param content 水印内容
     * @return 处理记录数
     */
    long embedDatabaseWatermark(Long dataSourceId, String tableName, String columnName,
                               WatermarkTemplate template, String content);

    /**
     * 根据文件类型自动选择嵌入方法
     * 
     * @param inputStream 输入流
     * @param outputStream 输出流
     * @param fileName 文件名
     * @param template 水印模板
     * @param content 水印内容
     * @return 是否成功
     */
    boolean embedWatermarkAuto(InputStream inputStream, OutputStream outputStream,
                              String fileName, WatermarkTemplate template, String content);

    /**
     * 支持的文件类型
     * 
     * @return 支持的文件类型列表
     */
    java.util.List<String> getSupportedFileTypes();

    /**
     * 验证文件类型是否支持
     * 
     * @param fileName 文件名
     * @return 是否支持
     */
    boolean isFileTypeSupported(String fileName);
}