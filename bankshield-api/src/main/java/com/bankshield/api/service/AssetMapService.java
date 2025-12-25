package com.bankshield.api.service;

import com.bankshield.common.result.Result;

import java.util.List;
import java.util.Map;

/**
 * 资产地图服务接口
 * 
 * @author BankShield
 */
public interface AssetMapService {

    /**
     * 获取资产全景视图
     * 
     * @return 资产全景数据
     */
    Result<AssetOverview> getOverview();

    /**
     * 资产下钻查询
     * 
     * @param drillQuery 下钻查询条件
     * @return 下钻结果
     */
    Result<DrillDownResult> drillDown(DrillDownQuery drillQuery);

    /**
     * 获取风险资产清单
     * 
     * @param riskLevel 风险等级
     * @return 风险资产清单
     */
    Result<List<RiskAsset>> getRiskAssets(String riskLevel);

    /**
     * 获取业务条线分布
     * 
     * @return 业务条线分布数据
     */
    Result<List<BusinessLineDistribution>> getBusinessLineDistribution();

    /**
     * 获取存储位置分布
     * 
     * @return 存储位置分布数据
     */
    Result<List<StorageDistribution>> getStorageDistribution();

    /**
     * 导出资产清单
     * 
     * @param exportType 导出类型
     * @return 导出文件路径
     */
    Result<String> exportAssetList(String exportType);

    /**
     * 资产全景数据
     */
    class AssetOverview {
        private Long totalAssets;
        private Map<String, Long> assetsByLevel;
        private Map<String, Long> assetsByType;
        private Map<String, Long> assetsByBusinessLine;
        private Long pendingReviewCount;
        private Long riskAssetCount;

        // 构造函数、getter、setter方法
        public AssetOverview() {}

        public Long getTotalAssets() { return totalAssets; }
        public void setTotalAssets(Long totalAssets) { this.totalAssets = totalAssets; }
        
        public Map<String, Long> getAssetsByLevel() { return assetsByLevel; }
        public void setAssetsByLevel(Map<String, Long> assetsByLevel) { this.assetsByLevel = assetsByLevel; }
        
        public Map<String, Long> getAssetsByType() { return assetsByType; }
        public void setAssetsByType(Map<String, Long> assetsByType) { this.assetsByType = assetsByType; }
        
        public Map<String, Long> getAssetsByBusinessLine() { return assetsByBusinessLine; }
        public void setAssetsByBusinessLine(Map<String, Long> assetsByBusinessLine) { this.assetsByBusinessLine = assetsByBusinessLine; }
        
        public Long getPendingReviewCount() { return pendingReviewCount; }
        public void setPendingReviewCount(Long pendingReviewCount) { this.pendingReviewCount = pendingReviewCount; }
        
        public Long getRiskAssetCount() { return riskAssetCount; }
        public void setRiskAssetCount(Long riskAssetCount) { this.riskAssetCount = riskAssetCount; }
    }

    /**
     * 下钻查询条件
     */
    class DrillDownQuery {
        private String dimension; // businessLine, dataSource, securityLevel
        private String dimensionValue;
        private Integer page;
        private Integer size;
        private String keyword;

        // 构造函数、getter、setter方法
        public DrillDownQuery() {}

        public String getDimension() { return dimension; }
        public void setDimension(String dimension) { this.dimension = dimension; }
        
        public String getDimensionValue() { return dimensionValue; }
        public void setDimensionValue(String dimensionValue) { this.dimensionValue = dimensionValue; }
        
        public Integer getPage() { return page; }
        public void setPage(Integer page) { this.page = page; }
        
        public Integer getSize() { return size; }
        public void setSize(Integer size) { this.size = size; }
        
        public String getKeyword() { return keyword; }
        public void setKeyword(String keyword) { this.keyword = keyword; }
    }

    /**
     * 下钻查询结果
     */
    class DrillDownResult {
        private List<Map<String, Object>> assets;
        private Long total;
        private Integer currentPage;
        private Integer pageSize;
        private String drillPath;

        // 构造函数、getter、setter方法
        public DrillDownResult() {}

        public List<Map<String, Object>> getAssets() { return assets; }
        public void setAssets(List<Map<String, Object>> assets) { this.assets = assets; }
        
        public Long getTotal() { return total; }
        public void setTotal(Long total) { this.total = total; }
        
        public Integer getCurrentPage() { return currentPage; }
        public void setCurrentPage(Integer currentPage) { this.currentPage = currentPage; }
        
        public Integer getPageSize() { return pageSize; }
        public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
        
        public String getDrillPath() { return drillPath; }
        public void setDrillPath(String drillPath) { this.drillPath = drillPath; }
    }

    /**
     * 风险资产信息
     */
    class RiskAsset {
        private Long assetId;
        private String assetName;
        private Integer securityLevel;
        private String riskLevel;
        private String businessLine;
        private String storageLocation;
        private String riskReason;

        // 构造函数、getter、setter方法
        public RiskAsset() {}

        public Long getAssetId() { return assetId; }
        public void setAssetId(Long assetId) { this.assetId = assetId; }
        
        public String getAssetName() { return assetName; }
        public void setAssetName(String assetName) { this.assetName = assetName; }
        
        public Integer getSecurityLevel() { return securityLevel; }
        public void setSecurityLevel(Integer securityLevel) { this.securityLevel = securityLevel; }
        
        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
        
        public String getBusinessLine() { return businessLine; }
        public void setBusinessLine(String businessLine) { this.businessLine = businessLine; }
        
        public String getStorageLocation() { return storageLocation; }
        public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
        
        public String getRiskReason() { return riskReason; }
        public void setRiskReason(String riskReason) { this.riskReason = riskReason; }
    }

    /**
     * 业务条线分布
     */
    class BusinessLineDistribution {
        private String businessLine;
        private Long assetCount;
        private Map<String, Long> assetsByLevel;
        private Double percentage;

        // 构造函数、getter、setter方法
        public BusinessLineDistribution() {}

        public String getBusinessLine() { return businessLine; }
        public void setBusinessLine(String businessLine) { this.businessLine = businessLine; }
        
        public Long getAssetCount() { return assetCount; }
        public void setAssetCount(Long assetCount) { this.assetCount = assetCount; }
        
        public Map<String, Long> getAssetsByLevel() { return assetsByLevel; }
        public void setAssetsByLevel(Map<String, Long> assetsByLevel) { this.assetsByLevel = assetsByLevel; }
        
        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }
    }

    /**
     * 存储位置分布
     */
    class StorageDistribution {
        private String storageLocation;
        private Long assetCount;
        private Map<String, Long> assetsByType;
        private Double percentage;

        // 构造函数、getter、setter方法
        public StorageDistribution() {}

        public String getStorageLocation() { return storageLocation; }
        public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
        
        public Long getAssetCount() { return assetCount; }
        public void setAssetCount(Long assetCount) { this.assetCount = assetCount; }
        
        public Map<String, Long> getAssetsByType() { return assetsByType; }
        public void setAssetsByType(Map<String, Long> assetsByType) { this.assetsByType = assetsByType; }
        
        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }
    }
}