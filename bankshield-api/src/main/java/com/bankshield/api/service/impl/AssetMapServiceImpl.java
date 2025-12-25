package com.bankshield.api.service.impl;

import com.bankshield.api.entity.DataAsset;
import com.bankshield.api.mapper.DataAssetMapper;
import com.bankshield.api.service.AssetMapService;
import com.bankshield.common.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 资产地图服务实现类
 * 
 * @author BankShield
 */
@Slf4j
@Service
public class AssetMapServiceImpl extends ServiceImpl<DataAssetMapper, DataAsset> implements AssetMapService {

    @Autowired
    private DataAssetMapper dataAssetMapper;

    @Override
    public Result<AssetOverview> getOverview() {
        try {
            AssetOverview overview = new AssetOverview();
            
            // 统计总资产数量
            long totalAssets = this.count();
            overview.setTotalAssets(totalAssets);
            
            // 按安全等级统计
            Map<String, Long> assetsByLevel = new HashMap<>();
            assetsByLevel.put("C1", this.count(new LambdaQueryWrapper<DataAsset>().eq(DataAsset::getSecurityLevel, 1)));
            assetsByLevel.put("C2", this.count(new LambdaQueryWrapper<DataAsset>().eq(DataAsset::getSecurityLevel, 2)));
            assetsByLevel.put("C3", this.count(new LambdaQueryWrapper<DataAsset>().eq(DataAsset::getSecurityLevel, 3)));
            assetsByLevel.put("C4", this.count(new LambdaQueryWrapper<DataAsset>().eq(DataAsset::getSecurityLevel, 4)));
            overview.setAssetsByLevel(assetsByLevel);
            
            // 按资产类型统计
            Map<String, Long> assetsByType = new HashMap<>();
            List<Map<String, Object>> typeStats = dataAssetMapper.countByAssetType();
            for (Map<String, Object> stat : typeStats) {
                String assetType = (String) stat.get("asset_type");
                Long count = (Long) stat.get("count");
                if (assetType != null && count != null) {
                    assetsByType.put(assetType, count);
                }
            }
            overview.setAssetsByType(assetsByType);
            
            // 按业务条线统计
            Map<String, Long> assetsByBusinessLine = new HashMap<>();
            List<Map<String, Object>> businessLineStats = dataAssetMapper.countByBusinessLine();
            for (Map<String, Object> stat : businessLineStats) {
                String businessLine = (String) stat.get("business_line");
                Long count = (Long) stat.get("count");
                if (businessLine != null && count != null) {
                    assetsByBusinessLine.put(businessLine, count);
                }
            }
            overview.setAssetsByBusinessLine(assetsByBusinessLine);
            
            // 待审核资产数量
            long pendingReviewCount = this.count(new LambdaQueryWrapper<DataAsset>().eq(DataAsset::getStatus, 0));
            overview.setPendingReviewCount(pendingReviewCount);
            
            // 风险资产数量（C3、C4级）
            long riskAssetCount = this.count(new LambdaQueryWrapper<DataAsset>()
                    .in(DataAsset::getSecurityLevel, 3, 4)
                    .eq(DataAsset::getStatus, 1)); // 只统计已生效的
            overview.setRiskAssetCount(riskAssetCount);
            
            return Result.OK(overview);
        } catch (Exception e) {
            log.error("获取资产全景视图失败: {}", e.getMessage());
            return Result.error("获取资产全景视图失败");
        }
    }

    @Override
    public Result<DrillDownResult> drillDown(DrillDownQuery drillQuery) {
        try {
            if (drillQuery == null || !org.springframework.util.StringUtils.hasText(drillQuery.getDimension())) {
                return Result.error("查询维度不能为空");
            }
            
            DrillDownResult result = new DrillDownResult();
            
            // 设置分页信息
            result.setCurrentPage(drillQuery.getPage() != null ? drillQuery.getPage() : 1);
            result.setPageSize(drillQuery.getSize() != null ? drillQuery.getSize() : 10);
            
            // 构建查询条件
            LambdaQueryWrapper<DataAsset> queryWrapper = new LambdaQueryWrapper<>();
            
            // 根据维度设置查询条件
            switch (drillQuery.getDimension()) {
                case "businessLine":
                    if (org.springframework.util.StringUtils.hasText(drillQuery.getDimensionValue())) {
                        queryWrapper.eq(DataAsset::getBusinessLine, drillQuery.getDimensionValue());
                    }
                    break;
                case "dataSource":
                    if (drillQuery.getDimensionValue() != null) {
                        try {
                            Long dataSourceId = Long.valueOf(drillQuery.getDimensionValue());
                            queryWrapper.eq(DataAsset::getDataSourceId, dataSourceId);
                        } catch (NumberFormatException e) {
                            return Result.error("数据源ID格式错误");
                        }
                    }
                    break;
                case "securityLevel":
                    if (drillQuery.getDimensionValue() != null) {
                        try {
                            Integer securityLevel = Integer.valueOf(drillQuery.getDimensionValue());
                            queryWrapper.eq(DataAsset::getSecurityLevel, securityLevel);
                        } catch (NumberFormatException e) {
                            return Result.error("安全等级格式错误");
                        }
                    }
                    break;
                default:
                    return Result.error("不支持的查询维度");
            }
            
            // 添加关键词搜索
            if (org.springframework.util.StringUtils.hasText(drillQuery.getKeyword())) {
                queryWrapper.and(wrapper -> wrapper
                        .like(DataAsset::getAssetName, drillQuery.getKeyword())
                        .or()
                        .like(DataAsset::getStorageLocation, drillQuery.getKeyword())
                        .or()
                        .like(DataAsset::getBusinessOwner, drillQuery.getKeyword()));
            }
            
            // 只查询已生效的资产
            queryWrapper.eq(DataAsset::getStatus, 1);
            
            // 执行查询
            List<DataAsset> assets = this.list(queryWrapper);
            
            // 转换为Map列表（用于前端展示）
            List<Map<String, Object>> assetMaps = assets.stream()
                    .map(this::convertAssetToMap)
                    .collect(Collectors.toList());
            
            result.setAssets(assetMaps);
            result.setTotal((long) assetMaps.size());
            
            // 设置钻取路径
            String drillPath = String.format("%s: %s", 
                    getDimensionName(drillQuery.getDimension()), 
                    drillQuery.getDimensionValue() != null ? drillQuery.getDimensionValue() : "全部");
            result.setDrillPath(drillPath);
            
            return Result.OK(result);
        } catch (Exception e) {
            log.error("资产下钻查询失败: {}", e.getMessage());
            return Result.error("资产下钻查询失败");
        }
    }

    @Override
    public Result<List<RiskAsset>> getRiskAssets(String riskLevel) {
        try {
            LambdaQueryWrapper<DataAsset> queryWrapper = new LambdaQueryWrapper<>();
            
            // 根据风险等级设置查询条件
            if (org.springframework.util.StringUtils.hasText(riskLevel)) {
                switch (riskLevel.toUpperCase()) {
                    case "HIGH":
                        queryWrapper.in(DataAsset::getSecurityLevel, 3, 4);
                        break;
                    case "MEDIUM":
                        queryWrapper.eq(DataAsset::getSecurityLevel, 2);
                        break;
                    case "LOW":
                        queryWrapper.eq(DataAsset::getSecurityLevel, 1);
                        break;
                    default:
                        return Result.error("不支持的风险等级");
                }
            } else {
                // 默认查询高风险资产
                queryWrapper.in(DataAsset::getSecurityLevel, 3, 4);
            }
            
            // 只查询已生效的资产
            queryWrapper.eq(DataAsset::getStatus, 1);
            queryWrapper.orderByDesc(DataAsset::getSecurityLevel);
            queryWrapper.orderByDesc(DataAsset::getUpdateTime);
            
            List<DataAsset> assets = this.list(queryWrapper);
            
            // 转换为风险资产对象
            List<RiskAsset> riskAssets = assets.stream()
                    .map(asset -> {
                        RiskAsset riskAsset = new RiskAsset();
                        riskAsset.setAssetId(asset.getId());
                        riskAsset.setAssetName(asset.getAssetName());
                        riskAsset.setSecurityLevel(asset.getSecurityLevel());
                        riskAsset.setRiskLevel(getRiskLevelName(asset.getSecurityLevel()));
                        riskAsset.setBusinessLine(asset.getBusinessLine());
                        riskAsset.setStorageLocation(asset.getStorageLocation());
                        riskAsset.setRiskReason(generateRiskReason(asset));
                        return riskAsset;
                    })
                    .collect(Collectors.toList());
            
            return Result.OK(riskAssets);
        } catch (Exception e) {
            log.error("获取风险资产清单失败: {}", e.getMessage());
            return Result.error("获取风险资产清单失败");
        }
    }

    @Override
    public Result<List<BusinessLineDistribution>> getBusinessLineDistribution() {
        try {
            // 查询所有业务条线
            LambdaQueryWrapper<DataAsset> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(DataAsset::getBusinessLine);
            queryWrapper.eq(DataAsset::getStatus, 1); // 只统计已生效的
            queryWrapper.groupBy(DataAsset::getBusinessLine);
            
            List<Object> businessLines = this.listObjs(queryWrapper);
            
            List<BusinessLineDistribution> distributions = new ArrayList<>();
            
            for (Object businessLineObj : businessLines) {
                String businessLine = (String) businessLineObj;
                if (org.springframework.util.StringUtils.hasText(businessLine)) {
                    BusinessLineDistribution distribution = new BusinessLineDistribution();
                    distribution.setBusinessLine(businessLine);
                    
                    // 统计该业务条线的资产数量
                    long assetCount = this.count(new LambdaQueryWrapper<DataAsset>()
                            .eq(DataAsset::getBusinessLine, businessLine)
                            .eq(DataAsset::getStatus, 1));
                    distribution.setAssetCount(assetCount);
                    
                    // 按安全等级统计
                    Map<String, Long> assetsByLevel = new HashMap<>();
                    for (int level = 1; level <= 4; level++) {
                        long levelCount = this.count(new LambdaQueryWrapper<DataAsset>()
                                .eq(DataAsset::getBusinessLine, businessLine)
                                .eq(DataAsset::getSecurityLevel, level)
                                .eq(DataAsset::getStatus, 1));
                        assetsByLevel.put("C" + level, levelCount);
                    }
                    distribution.setAssetsByLevel(assetsByLevel);
                    
                    // 计算百分比
                    if (assetCount > 0) {
                        long totalAssets = this.count(new LambdaQueryWrapper<DataAsset>().eq(DataAsset::getStatus, 1));
                        distribution.setPercentage((double) assetCount / totalAssets * 100);
                    } else {
                        distribution.setPercentage(0.0);
                    }
                    
                    distributions.add(distribution);
                }
            }
            
            // 按资产数量排序
            distributions.sort((a, b) -> Long.compare(b.getAssetCount(), a.getAssetCount()));
            
            return Result.OK(distributions);
        } catch (Exception e) {
            log.error("获取业务条线分布失败: {}", e.getMessage());
            return Result.error("获取业务条线分布失败");
        }
    }

    @Override
    public Result<List<StorageDistribution>> getStorageDistribution() {
        try {
            // 查询所有存储位置
            LambdaQueryWrapper<DataAsset> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(DataAsset::getStorageLocation);
            queryWrapper.eq(DataAsset::getStatus, 1); // 只统计已生效的
            queryWrapper.groupBy(DataAsset::getStorageLocation);
            
            List<Object> storageLocations = this.listObjs(queryWrapper);
            
            List<StorageDistribution> distributions = new ArrayList<>();
            
            for (Object storageLocationObj : storageLocations) {
                String storageLocation = (String) storageLocationObj;
                if (org.springframework.util.StringUtils.hasText(storageLocation)) {
                    StorageDistribution distribution = new StorageDistribution();
                    distribution.setStorageLocation(storageLocation);
                    
                    // 统计该存储位置的资产数量
                    long assetCount = this.count(new LambdaQueryWrapper<DataAsset>()
                            .eq(DataAsset::getStorageLocation, storageLocation)
                            .eq(DataAsset::getStatus, 1));
                    distribution.setAssetCount(assetCount);
                    
                    // 按资产类型统计
                    Map<String, Long> assetsByType = new HashMap<>();
                    List<Map<String, Object>> typeStats = dataAssetMapper.countByAssetType();
                    for (Map<String, Object> stat : typeStats) {
                        String assetType = (String) stat.get("asset_type");
                        Long count = (Long) stat.get("count");
                        if (assetType != null && count != null) {
                            assetsByType.put(assetType, count);
                        }
                    }
                    distribution.setAssetsByType(assetsByType);
                    
                    // 计算百分比
                    if (assetCount > 0) {
                        long totalAssets = this.count(new LambdaQueryWrapper<DataAsset>().eq(DataAsset::getStatus, 1));
                        distribution.setPercentage((double) assetCount / totalAssets * 100);
                    } else {
                        distribution.setPercentage(0.0);
                    }
                    
                    distributions.add(distribution);
                }
            }
            
            // 按资产数量排序
            distributions.sort((a, b) -> Long.compare(b.getAssetCount(), a.getAssetCount()));
            
            return Result.OK(distributions);
        } catch (Exception e) {
            log.error("获取存储位置分布失败: {}", e.getMessage());
            return Result.error("获取存储位置分布失败");
        }
    }

    @Override
    public Result<String> exportAssetList(String exportType) {
        try {
            // 参数校验
            if (!org.springframework.util.StringUtils.hasText(exportType)) {
                exportType = "EXCEL"; // 默认导出Excel
            }
            
            // 查询所有已生效的资产
            LambdaQueryWrapper<DataAsset> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DataAsset::getStatus, 1);
            queryWrapper.orderByDesc(DataAsset::getSecurityLevel);
            queryWrapper.orderByDesc(DataAsset::getUpdateTime);
            
            List<DataAsset> assets = this.list(queryWrapper);
            
            // 生成文件名
            String fileName = String.format("数据资产清单_%s.%s", 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")),
                    exportType.toLowerCase());
            
            // TODO: 实际导出逻辑
            // 这里应该调用实际的文件导出服务
            log.info("导出资产清单: type={}, fileName={}, assetCount={}", exportType, fileName, assets.size());
            
            // 模拟导出成功，返回文件路径
            String filePath = "/exports/" + fileName;
            
            return Result.OK(filePath);
        } catch (Exception e) {
            log.error("导出资产清单失败: {}", e.getMessage());
            return Result.error("导出资产清单失败");
        }
    }

    /**
     * 将资产对象转换为Map
     */
    private Map<String, Object> convertAssetToMap(DataAsset asset) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", asset.getId());
        map.put("assetName", asset.getAssetName());
        map.put("assetType", asset.getAssetType());
        map.put("securityLevel", asset.getSecurityLevel());
        map.put("securityLevelName", getSecurityLevelName(asset.getSecurityLevel()));
        map.put("businessLine", asset.getBusinessLine());
        map.put("storageLocation", asset.getStorageLocation());
        map.put("businessOwner", asset.getBusinessOwner());
        map.put("technicalOwner", asset.getTechnicalOwner());
        map.put("recognizeMethod", asset.getRecognizeMethod());
        map.put("recognizeConfidence", asset.getRecognizeConfidence());
        map.put("status", asset.getStatus());
        map.put("statusName", getStatusName(asset.getStatus()));
        map.put("createTime", asset.getCreateTime());
        map.put("updateTime", asset.getUpdateTime());
        return map;
    }

    /**
     * 获取维度名称
     */
    private String getDimensionName(String dimension) {
        switch (dimension) {
            case "businessLine": return "业务条线";
            case "dataSource": return "数据源";
            case "securityLevel": return "安全等级";
            default: return dimension;
        }
    }

    /**
     * 获取安全等级名称
     */
    private String getSecurityLevelName(Integer securityLevel) {
        if (securityLevel == null) return "未知";
        switch (securityLevel) {
            case 1: return "C1-内部信息";
            case 2: return "C2-一般个人信息";
            case 3: return "C3-敏感个人信息";
            case 4: return "C4-极敏感信息";
            default: return "未知";
        }
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待审核";
            case 1: return "已生效";
            case 2: return "已下线";
            default: return "未知";
        }
    }

    /**
     * 获取风险等级名称
     */
    private String getRiskLevelName(Integer securityLevel) {
        if (securityLevel == null) return "未知";
        switch (securityLevel) {
            case 4: return "极高风险";
            case 3: return "高风险";
            case 2: return "中等风险";
            case 1: return "低风险";
            default: return "未知";
        }
    }

    /**
     * 生成风险原因
     */
    private String generateRiskReason(DataAsset asset) {
        StringBuilder reason = new StringBuilder();
        
        if (asset.getSecurityLevel() != null) {
            switch (asset.getSecurityLevel()) {
                case 4:
                    reason.append("包含极敏感信息（生物特征、密码等）");
                    break;
                case 3:
                    reason.append("包含敏感个人信息（身份证号、银行卡号等）");
                    break;
                default:
                    reason.append("安全等级为C").append(asset.getSecurityLevel());
            }
        }
        
        if (org.springframework.util.StringUtils.hasText(asset.getRecognizeMethod())) {
            reason.append("，识别方法：").append(asset.getRecognizeMethod());
        }
        
        if (asset.getRecognizeConfidence() != null) {
            reason.append("，置信度：").append(String.format("%.2f", asset.getRecognizeConfidence()));
        }
        
        return reason.toString();
    }
}