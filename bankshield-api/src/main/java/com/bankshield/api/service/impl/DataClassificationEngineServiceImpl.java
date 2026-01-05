package com.bankshield.api.service.impl;

import com.bankshield.api.entity.DataAsset;
import com.bankshield.api.entity.DataClassificationHistory;
import com.bankshield.api.entity.DataClassificationRule;
import com.bankshield.api.mapper.DataAssetMapper;
import com.bankshield.api.mapper.DataClassificationHistoryMapper;
import com.bankshield.api.service.DataClassificationEngineService;
import com.bankshield.api.service.DataClassificationRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 数据分级引擎服务实现
 * 核心自动分级算法
 */
@Slf4j
@Service
public class DataClassificationEngineServiceImpl implements DataClassificationEngineService {
    
    @Autowired
    private DataClassificationRuleService ruleService;
    
    @Autowired
    private DataAssetMapper assetMapper;
    
    @Autowired
    private DataClassificationHistoryMapper historyMapper;
    
    @Override
    public String classifyAsset(DataAsset asset) {
        if (asset == null) {
            return null;
        }
        
        // 获取所有启用的规则，按优先级排序
        List<DataClassificationRule> rules = ruleService.getActiveRules();
        
        // 遍历规则进行匹配
        for (DataClassificationRule rule : rules) {
            if (matchRule(asset, rule)) {
                log.info("资产 {} 匹配规则 {}, 分级为 {}", 
                    asset.getAssetName(), rule.getRuleName(), rule.getSensitivityLevel());
                return rule.getSensitivityLevel();
            }
        }
        
        // 如果没有匹配的规则，返回默认级别C2（内部数据）
        log.warn("资产 {} 未匹配任何规则，使用默认级别C2", asset.getAssetName());
        return "C2";
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<Long, String> batchClassify(List<DataAsset> assets) {
        Map<Long, String> results = new HashMap<>();
        
        for (DataAsset asset : assets) {
            String level = classifyAsset(asset);
            results.put(asset.getId(), level);
            
            // 更新资产分级信息
            updateAssetClassification(asset, level, "AUTO", "自动分级引擎");
        }
        
        return results;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int classifyAllUnclassified() {
        // 查询所有未分级的资产
        List<DataAsset> unclassifiedAssets = assetMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DataAsset>()
                .isNull(DataAsset::getSensitivityLevel)
                .or()
                .eq(DataAsset::getSensitivityLevel, "")
        );
        
        log.info("开始自动分级，共 {} 个未分级资产", unclassifiedAssets.size());
        
        int classifiedCount = 0;
        for (DataAsset asset : unclassifiedAssets) {
            String level = classifyAsset(asset);
            if (level != null) {
                updateAssetClassification(asset, level, "AUTO", "自动分级引擎");
                classifiedCount++;
            }
        }
        
        log.info("自动分级完成，成功分级 {} 个资产", classifiedCount);
        return classifiedCount;
    }
    
    @Override
    public String matchRules(String fieldName, String content) {
        if (fieldName == null && content == null) {
            return null;
        }
        
        List<DataClassificationRule> rules = ruleService.getActiveRules();
        
        for (DataClassificationRule rule : rules) {
            // 字段名匹配
            if (fieldName != null && rule.getFieldPattern() != null) {
                try {
                    Pattern fieldPattern = Pattern.compile(rule.getFieldPattern(), Pattern.CASE_INSENSITIVE);
                    if (fieldPattern.matcher(fieldName).find()) {
                        // 如果有内容匹配规则，继续验证内容
                        if (rule.getContentPattern() != null && content != null) {
                            Pattern contentPattern = Pattern.compile(rule.getContentPattern());
                            if (contentPattern.matcher(content).find()) {
                                return rule.getSensitivityLevel();
                            }
                        } else {
                            // 没有内容匹配规则，仅字段名匹配即可
                            return rule.getSensitivityLevel();
                        }
                    }
                } catch (Exception e) {
                    log.error("规则匹配失败: {}, 错误: {}", rule.getRuleName(), e.getMessage());
                }
            }
        }
        
        return null;
    }
    
    @Override
    public boolean needManualReview(Long assetId, String suggestedLevel) {
        // 如果是C4或C5级别，建议人工审核
        if ("C4".equals(suggestedLevel) || "C5".equals(suggestedLevel)) {
            return true;
        }
        
        // 可以添加更多审核规则
        return false;
    }
    
    /**
     * 匹配单个规则
     */
    private boolean matchRule(DataAsset asset, DataClassificationRule rule) {
        try {
            // 1. 数据类型匹配
            if (rule.getDataType() != null && !rule.getDataType().isEmpty()) {
                if (!rule.getDataType().equals(asset.getAssetType())) {
                    return false;
                }
            }
            
            // 2. 字段名匹配
            if (rule.getFieldPattern() != null && !rule.getFieldPattern().isEmpty()) {
                Pattern fieldPattern = Pattern.compile(rule.getFieldPattern(), Pattern.CASE_INSENSITIVE);
                String fieldName = asset.getAssetName() != null ? asset.getAssetName() : "";
                if (!fieldPattern.matcher(fieldName).find()) {
                    return false;
                }
            }
            
            // 3. 内容匹配（如果有样本数据）
            if (rule.getContentPattern() != null && !rule.getContentPattern().isEmpty()) {
                // 这里可以扩展，从asset中获取样本数据进行匹配
                // 目前简化处理，如果有内容匹配规则但没有样本数据，则不匹配
                String description = asset.getClassificationBasis() != null ? asset.getClassificationBasis() : "";
                Pattern contentPattern = Pattern.compile(rule.getContentPattern());
                if (!contentPattern.matcher(description).find()) {
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            log.error("规则匹配异常: {}, 错误: {}", rule.getRuleName(), e.getMessage());
            return false;
        }
    }
    
    /**
     * 更新资产分级信息
     */
    private void updateAssetClassification(DataAsset asset, String newLevel, String method, String reason) {
        String oldLevel = asset.getSensitivityLevel();
        
        // 更新资产表
        asset.setSensitivityLevel(newLevel);
        asset.setClassificationMethod(method);
        asset.setClassificationTime(LocalDateTime.now());
        asset.setClassificationReason(reason);
        assetMapper.updateById(asset);
        
        // 记录分级历史
        DataClassificationHistory history = new DataClassificationHistory();
        history.setAssetId(asset.getId());
        history.setAssetName(asset.getAssetName());
        history.setOldLevel(oldLevel);
        history.setNewLevel(newLevel);
        history.setClassificationMethod(method);
        history.setReason(reason);
        history.setOperator("system");
        history.setClassifyTime(LocalDateTime.now());
        historyMapper.insert(history);
    }
}
