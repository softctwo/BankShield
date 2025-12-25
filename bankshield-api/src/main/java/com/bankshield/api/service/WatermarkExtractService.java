package com.bankshield.api.service;

import com.bankshield.api.entity.WatermarkExtractLog;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * 水印提取服务接口
 * 
 * @author BankShield
 */
public interface WatermarkExtractService {

    /**
     * 从文件中提取水印
     * 
     * @param file 文件
     * @param operator 操作人员
     * @return 提取结果
     */
    WatermarkExtractLog extractFromFile(MultipartFile file, String operator);

    /**
     * 从图片中提取水印
     * 
     * @param imagePath 图片路径
     * @param operator 操作人员
     * @return 提取结果
     */
    WatermarkExtractLog extractFromImage(String imagePath, String operator);

    /**
     * 从数据库中提取水印
     * 
     * @param dataSourceId 数据源ID
     * @param tableName 表名
     * @param operator 操作人员
     * @return 提取结果
     */
    WatermarkExtractLog extractFromDatabase(Long dataSourceId, String tableName, String operator);

    /**
     * 分页查询提取日志
     * 
     * @param page 分页参数
     * @param watermarkContent 水印内容
     * @param extractResult 提取结果
     * @param operator 操作人员
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     */
    IPage<WatermarkExtractLog> getExtractLogPage(Page<WatermarkExtractLog> page, String watermarkContent,
                                               String extractResult, String operator, 
                                               LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 批量提取文件中的水印
     * 
     * @param files 文件列表
     * @param operator 操作人员
     * @return 提取结果列表
     */
    java.util.List<WatermarkExtractLog> batchExtractFromFiles(MultipartFile[] files, String operator);

    /**
     * 验证文件是否包含水印
     * 
     * @param file 文件
     * @return 是否包含水印
     */
    boolean hasWatermark(MultipartFile file);

    /**
     * 获取提取统计信息
     * 
     * @param days 统计天数
     * @return 统计信息
     */
    java.util.Map<String, Object> getExtractStatistics(int days);
}