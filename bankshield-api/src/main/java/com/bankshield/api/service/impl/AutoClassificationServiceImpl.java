package com.bankshield.api.service.impl;

import com.bankshield.api.entity.DataAsset;
import com.bankshield.api.entity.DataSource;
import com.bankshield.api.entity.ScanTask;
import com.bankshield.api.entity.SensitiveDataTemplate;
import com.bankshield.api.mapper.DataAssetMapper;
import com.bankshield.api.mapper.DataSourceMapper;
import com.bankshield.api.mapper.ScanTaskMapper;
import com.bankshield.api.mapper.SensitiveDataTemplateMapper;
import com.bankshield.api.service.AutoClassificationService;
import com.bankshield.common.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 自动分类分级服务实现类
 * 
 * @author BankShield
 */
@Slf4j
@Service
public class AutoClassificationServiceImpl extends ServiceImpl<DataAssetMapper, DataAsset> implements AutoClassificationService {

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Autowired
    private ScanTaskMapper scanTaskMapper;

    @Autowired
    private SensitiveDataTemplateMapper sensitiveDataTemplateMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> scanDataSource(Long dataSourceId) {
        try {
            // 参数校验
            if (dataSourceId == null) {
                return Result.error("数据源ID不能为空");
            }
            
            // 检查数据源是否存在
            DataSource dataSource = dataSourceMapper.selectById(dataSourceId);
            if (dataSource == null) {
                return Result.error("数据源不存在");
            }
            
            // 检查是否已有正在执行的任务
            LambdaQueryWrapper<ScanTask> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ScanTask::getDataSourceId, dataSourceId);
            queryWrapper.eq(ScanTask::getTaskStatus, 1); // 执行中
            
            List<ScanTask> runningTasks = scanTaskMapper.selectList(queryWrapper);
            if (!runningTasks.isEmpty()) {
                return Result.error("该数据源已有正在执行的扫描任务");
            }
            
            // 创建新的扫描任务
            ScanTask scanTask = ScanTask.builder()
                    .taskName("扫描任务-" + dataSource.getSourceName() + "-" + System.currentTimeMillis())
                    .dataSourceId(dataSourceId)
                    .scanType("FULL")
                    .taskStatus(0) // 待执行
                    .progressPercent(0)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            
            scanTaskMapper.insert(scanTask);
            
            // TODO: 启动异步扫描任务
            // 这里应该启动一个异步任务来执行实际的扫描工作
            startScanTask(scanTask.getId());
            
            return Result.OK(scanTask.getId());
        } catch (Exception e) {
            log.error("创建扫描任务失败: {}", e.getMessage());
            return Result.error("创建扫描任务失败");
        }
    }

    @Override
    public Result<DataAsset> recognizeField(String fieldName, String sampleData) {
        try {
            // 参数校验
            if (!org.springframework.util.StringUtils.hasText(fieldName)) {
                return Result.error("字段名称不能为空");
            }
            
            // 获取所有敏感数据模板
            List<SensitiveDataTemplate> templates = sensitiveDataTemplateMapper.selectAllActive();
            
            // 尝试正则匹配
            for (SensitiveDataTemplate template : templates) {
                if (org.springframework.util.StringUtils.hasText(template.getPattern())) {
                    try {
                        Pattern pattern = Pattern.compile(template.getPattern());
                        if (sampleData != null && pattern.matcher(sampleData).matches()) {
                            return createRecognitionResult(template, "REGEX", new BigDecimal("0.95"));
                        }
                        
                        // 检查字段名是否匹配模式
                        if (pattern.matcher(fieldName).matches()) {
                            return createRecognitionResult(template, "REGEX", new BigDecimal("0.90"));
                        }
                    } catch (Exception e) {
                        log.warn("正则表达式匹配失败: {}", template.getPattern(), e);
                    }
                }
            }
            
            // 尝试关键词匹配
            for (SensitiveDataTemplate template : templates) {
                if (org.springframework.util.StringUtils.hasText(template.getKeywords())) {
                    String keywords = template.getKeywords().toLowerCase();
                    String fieldNameLower = fieldName.toLowerCase();
                    
                    // 简单的关键词匹配（实际应该解析JSON数组）
                    if (keywords.contains("姓名") && fieldNameLower.contains("name")) {
                        return createRecognitionResult(template, "KEYWORD", new BigDecimal("0.80"));
                    }
                    if (keywords.contains("手机") && fieldNameLower.contains("phone")) {
                        return createRecognitionResult(template, "KEYWORD", new BigDecimal("0.80"));
                    }
                    if (keywords.contains("身份证") && fieldNameLower.contains("id")) {
                        return createRecognitionResult(template, "KEYWORD", new BigDecimal("0.80"));
                    }
                    if (keywords.contains("银行卡") && fieldNameLower.contains("card")) {
                        return createRecognitionResult(template, "KEYWORD", new BigDecimal("0.80"));
                    }
                }
            }
            
            // 默认C1级
            DataAsset defaultAsset = DataAsset.builder()
                    .assetName(fieldName)
                    .securityLevel(1)
                    .automaticLevel(1)
                    .finalLevel(1)
                    .recognizeMethod("DEFAULT")
                    .recognizeConfidence(BigDecimal.ZERO)
                    .classificationBasis("未识别到敏感特征，默认为C1级")
                    .build();
            
            return Result.OK(defaultAsset);
        } catch (Exception e) {
            log.error("字段识别失败: {}", e.getMessage());
            return Result.error("字段识别失败");
        }
    }

    @Override
    public Result<ScanProgress> getScanProgress(Long scanTaskId) {
        try {
            if (scanTaskId == null) {
                return Result.error("扫描任务ID不能为空");
            }
            
            ScanTask scanTask = scanTaskMapper.selectById(scanTaskId);
            if (scanTask == null) {
                return Result.error("扫描任务不存在");
            }
            
            ScanProgress progress = new ScanProgress();
            progress.setTaskId(scanTaskId);
            progress.setProgress(scanTask.getProgressPercent());
            progress.setStatus(getTaskStatusName(scanTask.getTaskStatus()));
            
            // 这里应该查询实际的扫描统计信息
            progress.setTotalAssets(100); // 示例数据
            progress.setScannedAssets(scanTask.getProgressPercent());
            progress.setErrorMessage(scanTask.getErrorMessage());
            
            return Result.OK(progress);
        } catch (Exception e) {
            log.error("获取扫描进度失败: {}", e.getMessage());
            return Result.error("获取扫描进度失败");
        }
    }

    @Override
    public Result<String> stopScanTask(Long scanTaskId) {
        try {
            if (scanTaskId == null) {
                return Result.error("扫描任务ID不能为空");
            }
            
            ScanTask scanTask = scanTaskMapper.selectById(scanTaskId);
            if (scanTask == null) {
                return Result.error("扫描任务不存在");
            }
            
            // 只有执行中的任务才能停止
            if (scanTask.getTaskStatus() != 1) {
                return Result.error("任务不处于执行中状态");
            }
            
            scanTask.setTaskStatus(3); // 失败状态
            scanTask.setErrorMessage("用户手动停止任务");
            scanTask.setEndTime(LocalDateTime.now());
            scanTask.setUpdateTime(LocalDateTime.now());
            
            scanTaskMapper.updateById(scanTask);
            
            return Result.OK("停止任务成功");
        } catch (Exception e) {
            log.error("停止扫描任务失败: {}", e.getMessage());
            return Result.error("停止扫描任务失败");
        }
    }

    @Override
    public Result<Long> rescanDataSource(Long dataSourceId) {
        try {
            if (dataSourceId == null) {
                return Result.error("数据源ID不能为空");
            }
            
            // 检查数据源是否存在
            DataSource dataSource = dataSourceMapper.selectById(dataSourceId);
            if (dataSource == null) {
                return Result.error("数据源不存在");
            }
            
            // 创建新的重新扫描任务
            return scanDataSource(dataSourceId);
        } catch (Exception e) {
            log.error("重新扫描数据源失败: {}", e.getMessage());
            return Result.error("重新扫描数据源失败");
        }
    }

    @Override
    public Result<String> testConnection(DataSource dataSource) {
        try {
            if (dataSource == null || dataSource.getId() == null) {
                return Result.error("数据源信息不能为空");
            }
            
            // 从数据库获取完整的数据源信息
            DataSource completeDataSource = dataSourceMapper.selectById(dataSource.getId());
            if (completeDataSource == null) {
                return Result.error("数据源不存在");
            }
            
            // TODO: 实际的数据库连接测试
            // 这里应该根据数据源类型和连接配置进行实际的连接测试
            String sourceType = completeDataSource.getSourceType();
            String connectionConfig = completeDataSource.getConnectionConfig();
            
            // 模拟连接测试
            log.info("测试数据源连接: type={}, config={}", sourceType, connectionConfig);
            
            // 模拟测试结果
            boolean connectionSuccess = true; // 这里应该是实际的连接测试结果
            
            if (connectionSuccess) {
                return Result.OK("连接测试成功");
            } else {
                return Result.error("连接测试失败");
            }
        } catch (Exception e) {
            log.error("连接测试失败: {}", e.getMessage());
            return Result.error("连接测试失败: " + e.getMessage());
        }
    }

    /**
     * 启动扫描任务（模拟实现）
     */
    private void startScanTask(Long scanTaskId) {
        // TODO: 这里应该启动异步任务
        // 模拟任务启动
        log.info("启动扫描任务: {}", scanTaskId);
    }

    /**
     * 创建识别结果
     */
    private Result<DataAsset> createRecognitionResult(SensitiveDataTemplate template, String method, BigDecimal confidence) {
        DataAsset asset = DataAsset.builder()
                .assetName(template.getTypeName())
                .securityLevel(template.getSecurityLevel())
                .automaticLevel(template.getSecurityLevel())
                .finalLevel(template.getSecurityLevel())
                .recognizeMethod(method)
                .recognizeConfidence(confidence)
                .patternMatched(template.getPattern())
                .classificationBasis(String.format("匹配到模板: %s (%s)", template.getTypeName(), template.getTypeCode()))
                .build();
        
        return Result.OK(asset);
    }

    /**
     * 获取任务状态名称
     */
    private String getTaskStatusName(Integer status) {
        switch (status) {
            case 0: return "待执行";
            case 1: return "执行中";
            case 2: return "已完成";
            case 3: return "失败";
            default: return "未知";
        }
    }
}