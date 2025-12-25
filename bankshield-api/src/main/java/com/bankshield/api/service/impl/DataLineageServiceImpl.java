package com.bankshield.api.service.impl;

import java.time.LocalDateTime;

import com.bankshield.api.entity.DataAsset;
import com.bankshield.api.entity.DataLineage;
import com.bankshield.api.mapper.DataAssetMapper;
import com.bankshield.api.mapper.DataLineageMapper;
import com.bankshield.api.service.DataLineageService;
import com.bankshield.common.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据血缘分析服务实现类
 * 
 * @author BankShield
 */
@Slf4j
@Service
public class DataLineageServiceImpl extends ServiceImpl<DataLineageMapper, DataLineage> implements DataLineageService {

    @Autowired
    private DataLineageMapper dataLineageMapper;

    @Autowired
    private DataAssetMapper dataAssetMapper;

    @Override
    public Result<String> parseLineage(String sql, String dataSource) {
        try {
            // 参数校验
            if (!org.springframework.util.StringUtils.hasText(sql)) {
                return Result.error("SQL语句不能为空");
            }
            if (!org.springframework.util.StringUtils.hasText(dataSource)) {
                return Result.error("数据源名称不能为空");
            }
            
            // TODO: 使用Apache Calcite解析SQL
            // 这里应该是实际的SQL解析逻辑
            log.info("解析SQL血缘: dataSource={}, sql={}", dataSource, sql);
            
            // 模拟SQL解析结果
            List<DataLineage> lineages = parseSqlLineage(sql, dataSource);
            
            // 批量保存血缘关系
            if (!lineages.isEmpty()) {
                dataLineageMapper.batchInsert(lineages);
            }
            
            return Result.OK("SQL血缘解析成功，发现 " + lineages.size() + " 条血缘关系");
        } catch (Exception e) {
            log.error("解析SQL血缘失败: {}", e.getMessage());
            return Result.error("解析SQL血缘失败");
        }
    }

    @Override
    public Result<List<LineagePath>> getFieldLineage(String tableName, String fieldName) {
        try {
            // 参数校验
            if (!org.springframework.util.StringUtils.hasText(tableName)) {
                return Result.error("表名不能为空");
            }
            if (!org.springframework.util.StringUtils.hasText(fieldName)) {
                return Result.error("字段名不能为空");
            }
            
            // 查找资产ID
            LambdaQueryWrapper<DataAsset> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DataAsset::getAssetName, tableName);
            DataAsset asset = dataAssetMapper.selectOne(queryWrapper);
            
            if (asset == null) {
                return Result.error("未找到对应的资产");
            }
            
            // 查询字段血缘
            List<DataLineage> lineageList = dataLineageMapper.selectFieldLineage(asset.getId(), fieldName);
            
            // 构建血缘路径
            List<LineagePath> paths = buildLineagePaths(lineageList);
            
            return Result.OK(paths);
        } catch (Exception e) {
            log.error("查询字段血缘失败: {}", e.getMessage());
            return Result.error("查询字段血缘失败");
        }
    }

    @Override
    public Result<List<DataLineage>> getUpstreamLineage(String tableName) {
        try {
            // 参数校验
            if (!org.springframework.util.StringUtils.hasText(tableName)) {
                return Result.error("表名不能为空");
            }
            
            // 查找资产ID
            LambdaQueryWrapper<DataAsset> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DataAsset::getAssetName, tableName);
            DataAsset asset = dataAssetMapper.selectOne(queryWrapper);
            
            if (asset == null) {
                return Result.error("未找到对应的资产");
            }
            
            // 查询上游血缘
            List<DataLineage> upstreamLineage = dataLineageMapper.selectUpstreamLineage(asset.getId());
            
            return Result.OK(upstreamLineage);
        } catch (Exception e) {
            log.error("查询上游血缘失败: {}", e.getMessage());
            return Result.error("查询上游血缘失败");
        }
    }

    @Override
    public Result<List<DataLineage>> getDownstreamLineage(String tableName) {
        try {
            // 参数校验
            if (!org.springframework.util.StringUtils.hasText(tableName)) {
                return Result.error("表名不能为空");
            }
            
            // 查找资产ID
            LambdaQueryWrapper<DataAsset> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DataAsset::getAssetName, tableName);
            DataAsset asset = dataAssetMapper.selectOne(queryWrapper);
            
            if (asset == null) {
                return Result.error("未找到对应的资产");
            }
            
            // 查询下游血缘
            List<DataLineage> downstreamLineage = dataLineageMapper.selectDownstreamLineage(asset.getId());
            
            return Result.OK(downstreamLineage);
        } catch (Exception e) {
            log.error("查询下游血缘失败: {}", e.getMessage());
            return Result.error("查询下游血缘失败");
        }
    }

    @Override
    public Result<LineageGraph> buildLineageGraph(Long assetId) {
        try {
            // 参数校验
            if (assetId == null) {
                return Result.error("资产ID不能为空");
            }
            
            // 检查资产是否存在
            DataAsset asset = dataAssetMapper.selectById(assetId);
            if (asset == null) {
                return Result.error("资产不存在");
            }
            
            // 构建血缘图谱
            LineageGraph graph = new LineageGraph();
            graph.setCenterAssetId(assetId);
            
            // 查询上游关系
            List<DataLineage> upstreamRelations = dataLineageMapper.selectUpstreamLineage(assetId);
            graph.setUpstreamRelations(upstreamRelations);
            
            // 查询下游关系
            List<DataLineage> downstreamRelations = dataLineageMapper.selectDownstreamLineage(assetId);
            graph.setDownstreamRelations(downstreamRelations);
            
            // 计算节点和边的数量
            graph.setTotalNodes(calculateTotalNodes(upstreamRelations, downstreamRelations));
            graph.setTotalEdges(upstreamRelations.size() + downstreamRelations.size());
            
            return Result.OK(graph);
        } catch (Exception e) {
            log.error("构建血缘图谱失败: {}", e.getMessage());
            return Result.error("构建血缘图谱失败");
        }
    }

    @Override
    public Result<TransferRisk> analyzeTransferRisk(Long assetId) {
        try {
            // 参数校验
            if (assetId == null) {
                return Result.error("资产ID不能为空");
            }
            
            // 检查资产是否存在
            DataAsset asset = dataAssetMapper.selectById(assetId);
            if (asset == null) {
                return Result.error("资产不存在");
            }
            
            // 分析传输风险
            TransferRisk risk = new TransferRisk();
            
            // 查询完整的血缘链路
            List<DataLineage> upstreamLineage = dataLineageMapper.selectUpstreamLineage(assetId);
            List<DataLineage> downstreamLineage = dataLineageMapper.selectDownstreamLineage(assetId);
            
            // 分析是否存在跨境传输
            boolean hasCrossBorderTransfer = analyzeCrossBorderTransfer(upstreamLineage, downstreamLineage);
            risk.setHasCrossBorderTransfer(hasCrossBorderTransfer);
            
            // 分析是否存在跨安全域传输
            boolean hasCrossDomainTransfer = analyzeCrossDomainTransfer(upstreamLineage, downstreamLineage);
            risk.setHasCrossDomainTransfer(hasCrossDomainTransfer);
            
            // 识别风险路径
            List<String> riskPaths = identifyRiskPaths(upstreamLineage, downstreamLineage);
            risk.setRiskPaths(riskPaths);
            
            // 评估风险等级
            String riskLevel = evaluateRiskLevel(hasCrossBorderTransfer, hasCrossDomainTransfer, asset.getSecurityLevel());
            risk.setRiskLevel(riskLevel);
            
            // 生成建议
            String recommendation = generateRecommendation(riskLevel, riskPaths);
            risk.setRecommendation(recommendation);
            
            return Result.OK(risk);
        } catch (Exception e) {
            log.error("分析传输风险失败: {}", e.getMessage());
            return Result.error("分析传输风险失败");
        }
    }

    /**
     * 模拟SQL解析
     */
    private List<DataLineage> parseSqlLineage(String sql, String dataSource) {
        List<DataLineage> lineages = new ArrayList<>();
        
        // TODO: 这里应该是实际的SQL解析逻辑
        // 模拟解析结果
        if (sql.toLowerCase().contains("select") && sql.toLowerCase().contains("from")) {
            // 假设解析出以下血缘关系
            DataLineage lineage = new DataLineage();
            lineage.setSourceAssetId(1L); // 源表ID
            lineage.setTargetAssetId(2L); // 目标表ID
            lineage.setSourceField("customer_id");
            lineage.setTargetField("user_id");
            lineage.setTransformation("字段重命名");
            lineage.setLineageType("DIRECT");
            lineage.setCreateTime(LocalDateTime.now());
            
            lineages.add(lineage);
        }
        
        return lineages;
    }

    /**
     * 构建血缘路径
     */
    private List<LineagePath> buildLineagePaths(List<DataLineage> lineageList) {
        List<LineagePath> paths = new ArrayList<>();
        
        // 按路径分组
        Map<String, List<DataLineage>> pathGroups = new HashMap<>();
        for (DataLineage lineage : lineageList) {
            String pathKey = lineage.getSourceAssetId() + "->" + lineage.getTargetAssetId();
            pathGroups.computeIfAbsent(pathKey, k -> new ArrayList<>()).add(lineage);
        }
        
        // 构建路径对象
        for (Map.Entry<String, List<DataLineage>> entry : pathGroups.entrySet()) {
            LineagePath path = new LineagePath();
            path.setPath(entry.getValue());
            path.setPathLength(entry.getValue().size());
            
            // 设置源表和目标表（这里简化处理）
            if (!entry.getValue().isEmpty()) {
                DataLineage firstLineage = entry.getValue().get(0);
                path.setSourceTable("表" + firstLineage.getSourceAssetId());
                path.setTargetTable("表" + firstLineage.getTargetAssetId());
            }
            
            paths.add(path);
        }
        
        return paths;
    }

    /**
     * 计算总节点数
     */
    private Integer calculateTotalNodes(List<DataLineage> upstreamRelations, List<DataLineage> downstreamRelations) {
        // 简化的节点计数逻辑
        return upstreamRelations.size() + downstreamRelations.size() + 1; // +1 是中心节点
    }

    /**
     * 分析跨境传输
     */
    private boolean analyzeCrossBorderTransfer(List<DataLineage> upstreamLineage, List<DataLineage> downstreamLineage) {
        // TODO: 实际的跨境传输分析逻辑
        // 这里应该根据数据源的地理位置信息来判断
        return false; // 模拟结果：无跨境传输
    }

    /**
     * 分析跨安全域传输
     */
    private boolean analyzeCrossDomainTransfer(List<DataLineage> upstreamLineage, List<DataLineage> downstreamLineage) {
        // TODO: 实际的跨安全域传输分析逻辑
        // 这里应该根据数据源的安全域信息来判断
        
        // 模拟分析：检查上下游资产的安全等级差异
        for (DataLineage lineage : upstreamLineage) {
            // 查询上下游资产的安全等级
            DataAsset sourceAsset = dataAssetMapper.selectById(lineage.getSourceAssetId());
            DataAsset targetAsset = dataAssetMapper.selectById(lineage.getTargetAssetId());
            
            if (sourceAsset != null && targetAsset != null) {
                // 如果安全等级差异较大，认为存在跨域传输风险
                int levelDiff = Math.abs(sourceAsset.getSecurityLevel() - targetAsset.getSecurityLevel());
                if (levelDiff >= 2) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * 识别风险路径
     */
    private List<String> identifyRiskPaths(List<DataLineage> upstreamLineage, List<DataLineage> downstreamLineage) {
        List<String> riskPaths = new ArrayList<>();
        
        // 分析上游风险路径
        for (DataLineage lineage : upstreamLineage) {
            DataAsset sourceAsset = dataAssetMapper.selectById(lineage.getSourceAssetId());
            if (sourceAsset != null && sourceAsset.getSecurityLevel() >= 3) {
                riskPaths.add(String.format("%s -> %s (高敏感数据源)", 
                    sourceAsset.getAssetName(), lineage.getTargetAssetId()));
            }
        }
        
        // 分析下游风险路径
        for (DataLineage lineage : downstreamLineage) {
            DataAsset targetAsset = dataAssetMapper.selectById(lineage.getTargetAssetId());
            if (targetAsset != null && targetAsset.getSecurityLevel() >= 3) {
                riskPaths.add(String.format("%s -> %s (传输到高敏感目标)", 
                    lineage.getSourceAssetId(), targetAsset.getAssetName()));
            }
        }
        
        return riskPaths;
    }

    /**
     * 评估风险等级
     */
    private String evaluateRiskLevel(boolean hasCrossBorderTransfer, boolean hasCrossDomainTransfer, Integer securityLevel) {
        if (hasCrossBorderTransfer) {
            return "极高风险";
        }
        if (hasCrossDomainTransfer) {
            return "高风险";
        }
        if (securityLevel != null && securityLevel >= 3) {
            return "中等风险";
        }
        return "低风险";
    }

    /**
     * 生成建议
     */
    private String generateRecommendation(String riskLevel, List<String> riskPaths) {
        StringBuilder recommendation = new StringBuilder();
        
        switch (riskLevel) {
            case "极高风险":
                recommendation.append("建议立即停止数据传输，进行安全评估");
                break;
            case "高风险":
                recommendation.append("建议加强数据传输监控，实施额外的安全措施");
                break;
            case "中等风险":
                recommendation.append("建议定期审查数据传输情况，确保合规性");
                break;
            default:
                recommendation.append("风险较低，保持正常监控即可");
        }
        
        if (!riskPaths.isEmpty()) {
            recommendation.append(" 重点关注以下风险路径: ");
            recommendation.append(String.join(", ", riskPaths));
        }
        
        return recommendation.toString();
    }
}