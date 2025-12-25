package com.bankshield.api.service;

import com.bankshield.api.entity.DataAsset;
import com.bankshield.api.entity.DataSource;
import com.bankshield.common.result.Result;

/**
 * 自动分类分级服务接口
 * 
 * @author BankShield
 */
public interface AutoClassificationService {

    /**
     * 扫描数据源，自动识别敏感数据
     * 
     * @param dataSourceId 数据源ID
     * @return 扫描任务ID
     */
    Result<Long> scanDataSource(Long dataSourceId);

    /**
     * 自动识别单个字段
     * 
     * @param fieldName 字段名称
     * @param sampleData 样本数据
     * @return 识别结果
     */
    Result<DataAsset> recognizeField(String fieldName, String sampleData);

    /**
     * 获取扫描进度
     * 
     * @param scanTaskId 扫描任务ID
     * @return 扫描进度信息
     */
    Result<ScanProgress> getScanProgress(Long scanTaskId);

    /**
     * 停止扫描任务
     * 
     * @param scanTaskId 扫描任务ID
     * @return 操作结果
     */
    Result<String> stopScanTask(Long scanTaskId);

    /**
     * 重新扫描数据源
     * 
     * @param dataSourceId 数据源ID
     * @return 新的扫描任务ID
     */
    Result<Long> rescanDataSource(Long dataSourceId);

    /**
     * 测试数据源连接
     * 
     * @param dataSource 数据源配置
     * @return 连接测试结果
     */
    Result<String> testConnection(DataSource dataSource);

    /**
     * 扫描进度信息
     */
    class ScanProgress {
        private Long taskId;
        private Integer progress;
        private String status;
        private Integer totalAssets;
        private Integer scannedAssets;
        private String errorMessage;

        // 构造函数、getter、setter方法
        public ScanProgress() {}

        public ScanProgress(Long taskId, Integer progress, String status, 
                          Integer totalAssets, Integer scannedAssets, String errorMessage) {
            this.taskId = taskId;
            this.progress = progress;
            this.status = status;
            this.totalAssets = totalAssets;
            this.scannedAssets = scannedAssets;
            this.errorMessage = errorMessage;
        }

        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }
        
        public Integer getProgress() { return progress; }
        public void setProgress(Integer progress) { this.progress = progress; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public Integer getTotalAssets() { return totalAssets; }
        public void setTotalAssets(Integer totalAssets) { this.totalAssets = totalAssets; }
        
        public Integer getScannedAssets() { return scannedAssets; }
        public void setScannedAssets(Integer scannedAssets) { this.scannedAssets = scannedAssets; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}