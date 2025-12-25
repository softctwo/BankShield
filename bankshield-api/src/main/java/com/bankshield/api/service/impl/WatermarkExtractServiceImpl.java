package com.bankshield.api.service.impl;

import com.bankshield.api.config.WatermarkConfig;
import com.bankshield.api.entity.WatermarkExtractLog;
import com.bankshield.api.mapper.WatermarkExtractLogMapper;
import com.bankshield.api.service.WatermarkExtractEngine;
import com.bankshield.api.service.WatermarkExtractService;
import com.bankshield.common.result.Result;
import com.bankshield.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 水印提取服务实现类
 * 
 * @author BankShield
 */
@Slf4j
@Service
public class WatermarkExtractServiceImpl extends ServiceImpl<WatermarkExtractLogMapper, WatermarkExtractLog> 
        implements WatermarkExtractService {

    @Autowired
    private WatermarkExtractLogMapper extractLogMapper;

    @Autowired
    private WatermarkExtractEngine extractEngine;

    @Autowired
    private WatermarkConfig watermarkConfig;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WatermarkExtractLog extractFromFile(MultipartFile file, String operator) {
        log.info("从文件中提取水印，文件名: {}, 操作人: {}", file.getOriginalFilename(), operator);
        
        long startTime = System.currentTimeMillis();
        WatermarkExtractLog extractLog = new WatermarkExtractLog();
        
        try {
            // 参数验证
            if (file == null || file.isEmpty()) {
                throw new BusinessException("文件不能为空");
            }
            if (operator == null || operator.trim().isEmpty()) {
                throw new BusinessException("操作人员不能为空");
            }
            
            // 验证文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !extractEngine.isFileTypeSupported(originalFilename)) {
                throw new BusinessException("不支持的文件类型");
            }
            
            // 设置提取日志基本信息
            extractLog.setExtractSource(file.getOriginalFilename());
            extractLog.setOperator(operator);
            extractLog.setFileName(file.getOriginalFilename());
            extractLog.setFileType(getFileType(originalFilename));
            extractLog.setFileSize(file.getSize());
            extractLog.setExtractTime(LocalDateTime.now());
            
            // 执行水印提取
            String watermarkContent;
            try (InputStream inputStream = file.getInputStream()) {
                watermarkContent = extractEngine.extractWatermarkAuto(inputStream, originalFilename);
            }
            
            // 设置提取结果
            extractLog.setWatermarkContent(watermarkContent != null ? watermarkContent : "");
            extractLog.setExtractResult(watermarkContent != null && !watermarkContent.trim().isEmpty() ? "SUCCESS" : "FAIL");
            
            // 验证水印完整性
            if ("SUCCESS".equals(extractLog.getExtractResult())) {
                boolean isValid = extractEngine.verifyWatermarkIntegrity(watermarkContent);
                if (!isValid) {
                    extractLog.setExtractResult("FAIL");
                    extractLog.setErrorMessage("水印完整性验证失败");
                }
            }
            
            // 计算提取耗时
            long endTime = System.currentTimeMillis();
            extractLog.setExtractDuration(endTime - startTime);
            
            // 保存提取日志
            extractLogMapper.insert(extractLog);
            
            log.info("水印提取完成，文件: {}, 结果: {}, 耗时: {}ms", 
                    originalFilename, extractLog.getExtractResult(), extractLog.getExtractDuration());
            
            return extractLog;
            
        } catch (BusinessException e) {
            // 保存失败日志
            extractLog.setExtractResult("FAIL");
            extractLog.setErrorMessage(e.getMessage());
            extractLog.setExtractDuration(System.currentTimeMillis() - startTime);
            extractLogMapper.insert(extractLog);
            throw e;
        } catch (Exception e) {
            log.error("提取水印失败，文件名: {}", file.getOriginalFilename(), e);
            
            // 保存失败日志
            extractLog.setExtractResult("FAIL");
            extractLog.setErrorMessage(e.getMessage());
            extractLog.setExtractDuration(System.currentTimeMillis() - startTime);
            extractLogMapper.insert(extractLog);
            
            throw new BusinessException("提取水印失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WatermarkExtractLog extractFromImage(String imagePath, String operator) {
        log.info("从图片中提取水印，图片路径: {}, 操作人: {}", imagePath, operator);
        
        long startTime = System.currentTimeMillis();
        WatermarkExtractLog extractLog = new WatermarkExtractLog();
        
        try {
            // 参数验证
            if (imagePath == null || imagePath.trim().isEmpty()) {
                throw new BusinessException("图片路径不能为空");
            }
            if (operator == null || operator.trim().isEmpty()) {
                throw new BusinessException("操作人员不能为空");
            }
            
            // 验证文件是否存在
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                throw new BusinessException("图片文件不存在");
            }
            
            // 验证文件类型
            if (!extractEngine.isFileTypeSupported(imagePath)) {
                throw new BusinessException("不支持的图片类型");
            }
            
            // 设置提取日志基本信息
            extractLog.setExtractSource(imagePath);
            extractLog.setOperator(operator);
            extractLog.setFileName(imageFile.getName());
            extractLog.setFileType(getFileType(imagePath));
            extractLog.setFileSize(imageFile.length());
            extractLog.setExtractTime(LocalDateTime.now());
            
            // 执行水印提取
            String watermarkContent;
            try (InputStream inputStream = new FileInputStream(imageFile)) {
                watermarkContent = extractEngine.extractFromImage(inputStream);
            }
            
            // 设置提取结果
            extractLog.setWatermarkContent(watermarkContent != null ? watermarkContent : "");
            extractLog.setExtractResult(watermarkContent != null && !watermarkContent.trim().isEmpty() ? "SUCCESS" : "FAIL");
            
            // 验证水印完整性
            if ("SUCCESS".equals(extractLog.getExtractResult())) {
                boolean isValid = extractEngine.verifyWatermarkIntegrity(watermarkContent);
                if (!isValid) {
                    extractLog.setExtractResult("FAIL");
                    extractLog.setErrorMessage("水印完整性验证失败");
                }
            }
            
            // 计算提取耗时
            long endTime = System.currentTimeMillis();
            extractLog.setExtractDuration(endTime - startTime);
            
            // 保存提取日志
            extractLogMapper.insert(extractLog);
            
            log.info("图片水印提取完成，文件: {}, 结果: {}, 耗时: {}ms", 
                    imagePath, extractLog.getExtractResult(), extractLog.getExtractDuration());
            
            return extractLog;
            
        } catch (BusinessException e) {
            // 保存失败日志
            extractLog.setExtractResult("FAIL");
            extractLog.setErrorMessage(e.getMessage());
            extractLog.setExtractDuration(System.currentTimeMillis() - startTime);
            extractLogMapper.insert(extractLog);
            throw e;
        } catch (Exception e) {
            log.error("提取图片水印失败，图片路径: {}", imagePath, e);
            
            // 保存失败日志
            extractLog.setExtractResult("FAIL");
            extractLog.setErrorMessage(e.getMessage());
            extractLog.setExtractDuration(System.currentTimeMillis() - startTime);
            extractLogMapper.insert(extractLog);
            
            throw new BusinessException("提取图片水印失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WatermarkExtractLog extractFromDatabase(Long dataSourceId, String tableName, String operator) {
        log.info("从数据库中提取水印，数据源ID: {}, 表名: {}, 操作人: {}", dataSourceId, tableName, operator);
        
        long startTime = System.currentTimeMillis();
        WatermarkExtractLog extractLog = new WatermarkExtractLog();
        
        try {
            // 参数验证
            if (dataSourceId == null) {
                throw new BusinessException("数据源ID不能为空");
            }
            if (tableName == null || tableName.trim().isEmpty()) {
                throw new BusinessException("表名不能为空");
            }
            if (operator == null || operator.trim().isEmpty()) {
                throw new BusinessException("操作人员不能为空");
            }
            
            // 设置提取日志基本信息
            extractLog.setExtractSource("数据库: " + dataSourceId + ", 表: " + tableName);
            extractLog.setOperator(operator);
            extractLog.setFileType("DATABASE");
            extractLog.setExtractTime(LocalDateTime.now());
            
            // 执行数据库水印提取
            String watermarkContent = extractEngine.extractFromDatabase(dataSourceId, tableName, null);
            
            // 设置提取结果
            extractLog.setWatermarkContent(watermarkContent != null ? watermarkContent : "");
            extractLog.setExtractResult(watermarkContent != null && !watermarkContent.trim().isEmpty() ? "SUCCESS" : "FAIL");
            
            // 验证水印完整性
            if ("SUCCESS".equals(extractLog.getExtractResult())) {
                boolean isValid = extractEngine.verifyWatermarkIntegrity(watermarkContent);
                if (!isValid) {
                    extractLog.setExtractResult("FAIL");
                    extractLog.setErrorMessage("水印完整性验证失败");
                }
            }
            
            // 计算提取耗时
            long endTime = System.currentTimeMillis();
            extractLog.setExtractDuration(endTime - startTime);
            
            // 保存提取日志
            extractLogMapper.insert(extractLog);
            
            log.info("数据库水印提取完成，数据源: {}, 表: {}, 结果: {}, 耗时: {}ms", 
                    dataSourceId, tableName, extractLog.getExtractResult(), extractLog.getExtractDuration());
            
            return extractLog;
            
        } catch (BusinessException e) {
            // 保存失败日志
            extractLog.setExtractResult("FAIL");
            extractLog.setErrorMessage(e.getMessage());
            extractLog.setExtractDuration(System.currentTimeMillis() - startTime);
            extractLogMapper.insert(extractLog);
            throw e;
        } catch (Exception e) {
            log.error("提取数据库水印失败，数据源ID: {}, 表名: {}", dataSourceId, tableName, e);
            
            // 保存失败日志
            extractLog.setExtractResult("FAIL");
            extractLog.setErrorMessage(e.getMessage());
            extractLog.setExtractDuration(System.currentTimeMillis() - startTime);
            extractLogMapper.insert(extractLog);
            
            throw new BusinessException("提取数据库水印失败: " + e.getMessage());
        }
    }

    @Override
    public IPage<WatermarkExtractLog> getExtractLogPage(Page<WatermarkExtractLog> page, String watermarkContent,
                                                       String extractResult, String operator, 
                                                       LocalDateTime startTime, LocalDateTime endTime) {
        log.info("分页查询水印提取日志，页码: {}, 每页大小: {}, 水印内容: {}, 提取结果: {}, 操作人员: {}", 
                page.getCurrent(), page.getSize(), watermarkContent, extractResult, operator);
        
        try {
            return extractLogMapper.selectExtractLogPage(page, watermarkContent, extractResult, 
                    operator, startTime, endTime);
        } catch (Exception e) {
            log.error("分页查询提取日志失败", e);
            throw new BusinessException("查询提取日志失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<WatermarkExtractLog> batchExtractFromFiles(MultipartFile[] files, String operator) {
        log.info("批量提取文件中的水印，文件数量: {}, 操作人: {}", files.length, operator);
        
        if (files == null || files.length == 0) {
            throw new BusinessException("文件列表不能为空");
        }
        if (operator == null || operator.trim().isEmpty()) {
            throw new BusinessException("操作人员不能为空");
        }
        
        List<WatermarkExtractLog> results = new ArrayList<>();
        
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            log.info("批量提取进度: {}/{}", i + 1, files.length);
            
            try {
                WatermarkExtractLog result = extractFromFile(file, operator);
                results.add(result);
            } catch (Exception e) {
                log.error("批量提取中单个文件处理失败，文件名: {}", file.getOriginalFilename(), e);
                
                // 创建失败记录
                WatermarkExtractLog failedLog = new WatermarkExtractLog();
                failedLog.setWatermarkContent("");
                failedLog.setExtractSource(file.getOriginalFilename());
                failedLog.setExtractResult("FAIL");
                failedLog.setExtractTime(LocalDateTime.now());
                failedLog.setOperator(operator);
                failedLog.setFileName(file.getOriginalFilename());
                failedLog.setFileType(getFileType(file.getOriginalFilename()));
                failedLog.setFileSize(file.getSize());
                failedLog.setErrorMessage(e.getMessage());
                failedLog.setExtractDuration(0L);
                
                extractLogMapper.insert(failedLog);
                results.add(failedLog);
            }
        }
        
        log.info("批量提取水印完成，成功: {}, 失败: {}", 
                results.stream().filter(r -> "SUCCESS".equals(r.getExtractResult())).count(),
                results.stream().filter(r -> "FAIL".equals(r.getExtractResult())).count());
        
        return results;
    }

    @Override
    public boolean hasWatermark(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !extractEngine.isFileTypeSupported(originalFilename)) {
                return false;
            }
            
            try (InputStream inputStream = file.getInputStream()) {
                return extractEngine.hasWatermark(inputStream, originalFilename);
            }
        } catch (IOException e) {
            log.error("验证文件是否包含水印失败，文件名: {}", file.getOriginalFilename(), e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getExtractStatistics(int days) {
        if (days <= 0) {
            days = 7; // 默认7天
        }
        
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);
        
        try {
            // 获取总的提取统计
            long totalExtracts = extractLogMapper.countExtractLogs(startTime, endTime, null);
            long successCount = extractLogMapper.countExtractLogs(startTime, endTime, "SUCCESS");
            long failCount = extractLogMapper.countExtractLogs(startTime, endTime, "FAIL");
            
            double successRate = totalExtracts > 0 ? (double) successCount / totalExtracts * 100 : 0;
            
            // 获取每日统计（这里简化处理，实际需要按天分组查询）
            List<Map<String, Object>> dailyStats = new ArrayList<>();
            for (int i = 0; i < days; i++) {
                LocalDateTime dayStart = startTime.plusDays(i);
                LocalDateTime dayEnd = dayStart.plusDays(1);
                
                // 这里应该查询数据库获取每日数据，这里简化处理
                Map<String, Object> dayStat = new HashMap<>();
                dayStat.put("date", dayStart.toLocalDate().toString());
                dayStat.put("total", totalExtracts / days); // 平均分配，实际需要精确查询
                dayStat.put("success", successCount / days);
                dayStat.put("fail", failCount / days);
                dailyStats.add(dayStat);
            }
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalExtracts", totalExtracts);
            statistics.put("successCount", successCount);
            statistics.put("failCount", failCount);
            statistics.put("successRate", String.format("%.2f", successRate));
            statistics.put("dailyStats", dailyStats);
            
            return statistics;
            
        } catch (Exception e) {
            log.error("获取提取统计信息失败", e);
            throw new BusinessException("获取统计信息失败");
        }
    }

    /**
     * 获取文件类型
     */
    private String getFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "UNKNOWN";
        }
        
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase();
        switch (extension) {
            case "PDF":
                return "PDF";
            case "DOC":
            case "DOCX":
                return "WORD";
            case "XLS":
            case "XLSX":
                return "EXCEL";
            case "JPG":
            case "JPEG":
                return "JPEG";
            case "PNG":
                return "PNG";
            case "GIF":
                return "GIF";
            case "BMP":
                return "BMP";
            default:
                return extension;
        }
    }
}