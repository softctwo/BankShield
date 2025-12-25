package com.bankshield.api.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bankshield.api.entity.ComplianceCheckItem;
import com.bankshield.api.enums.CheckStatus;
import com.bankshield.api.mapper.ComplianceCheckItemMapper;
import com.bankshield.api.service.ComplianceCheckItemService;
import com.bankshield.api.service.ComplianceCheckEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 合规检查项服务实现类
 */
@Slf4j
@Service
public class ComplianceCheckItemServiceImpl extends ServiceImpl<ComplianceCheckItemMapper, ComplianceCheckItem> implements ComplianceCheckItemService {
    
    @Autowired
    private ComplianceCheckItemMapper complianceCheckItemMapper;
    
    @Autowired
    private ComplianceCheckEngine complianceCheckEngine;
    
    @Override
    public IPage<ComplianceCheckItem> getCheckItemPage(Page<ComplianceCheckItem> page, String checkItemName, String complianceStandard, String passStatus) {
        LambdaQueryWrapper<ComplianceCheckItem> wrapper = new LambdaQueryWrapper<>();
        
        if (checkItemName != null && !checkItemName.trim().isEmpty()) {
            wrapper.like(ComplianceCheckItem::getCheckItemName, checkItemName);
        }
        
        if (complianceStandard != null && !complianceStandard.trim().isEmpty()) {
            wrapper.eq(ComplianceCheckItem::getComplianceStandard, complianceStandard);
        }
        
        if (passStatus != null && !passStatus.trim().isEmpty()) {
            wrapper.eq(ComplianceCheckItem::getPassStatus, passStatus);
        }
        
        wrapper.orderByDesc(ComplianceCheckItem::getCheckTime);
        return complianceCheckItemMapper.selectPage(page, wrapper);
    }
    
    @Override
    public ComplianceCheckItem getCheckItemById(Long id) {
        return complianceCheckItemMapper.selectById(id);
    }
    
    @Override
    @Transactional
    public ComplianceCheckItem createCheckItem(ComplianceCheckItem checkItem) {
        checkItem.setCreateTime(LocalDateTime.now());
        checkItem.setUpdateTime(LocalDateTime.now());
        
        if (checkItem.getCheckTime() == null) {
            checkItem.setCheckTime(LocalDateTime.now());
        }
        
        if (checkItem.getNextCheckTime() == null) {
            checkItem.setNextCheckTime(LocalDateTime.now().plusDays(30));
        }
        
        if (checkItem.getPassStatus() == null) {
            checkItem.setPassStatus(CheckStatus.UNKNOWN.name());
        }
        
        complianceCheckItemMapper.insert(checkItem);
        log.info("创建合规检查项成功: {}", checkItem.getCheckItemName());
        return checkItem;
    }
    
    @Override
    @Transactional
    public ComplianceCheckItem updateCheckItem(ComplianceCheckItem checkItem) {
        ComplianceCheckItem existingItem = complianceCheckItemMapper.selectById(checkItem.getId());
        if (existingItem == null) {
            throw new RuntimeException("合规检查项不存在");
        }
        
        checkItem.setUpdateTime(LocalDateTime.now());
        complianceCheckItemMapper.updateById(checkItem);
        log.info("更新合规检查项成功: {}", checkItem.getCheckItemName());
        return checkItem;
    }
    
    @Override
    @Transactional
    public boolean deleteCheckItem(Long id) {
        ComplianceCheckItem checkItem = complianceCheckItemMapper.selectById(id);
        if (checkItem == null) {
            throw new RuntimeException("合规检查项不存在");
        }
        
        int result = complianceCheckItemMapper.deleteById(id);
        if (result > 0) {
            log.info("删除合规检查项成功: {}", checkItem.getCheckItemName());
            return true;
        }
        return false;
    }
    
    @Override
    @Transactional
    public Map<String, List<ComplianceCheckItem>> performComplianceChecks() {
        log.info("开始执行合规检查...");
        
        try {
            // 执行所有合规检查
            Map<String, List<ComplianceCheckItem>> checkResults = complianceCheckEngine.performAllChecks();
            
            // 保存检查结果
            for (Map.Entry<String, List<ComplianceCheckItem>> entry : checkResults.entrySet()) {
                String complianceStandard = entry.getKey();
                List<ComplianceCheckItem> checkItems = entry.getValue();
                
                for (ComplianceCheckItem checkItem : checkItems) {
                    // 检查是否已存在相同的检查项
                    LambdaQueryWrapper<ComplianceCheckItem> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(ComplianceCheckItem::getCheckItemName, checkItem.getCheckItemName())
                           .eq(ComplianceCheckItem::getComplianceStandard, complianceStandard)
                           .orderByDesc(ComplianceCheckItem::getCheckTime)
                           .last("LIMIT 1");
                    
                    ComplianceCheckItem existingItem = complianceCheckItemMapper.selectOne(wrapper);
                    
                    if (existingItem != null) {
                        // 更新现有检查项
                        existingItem.setPassStatus(checkItem.getPassStatus());
                        existingItem.setCheckResult(checkItem.getCheckResult());
                        existingItem.setCheckTime(checkItem.getCheckTime());
                        existingItem.setNextCheckTime(checkItem.getNextCheckTime());
                        existingItem.setRemediation(checkItem.getRemediation());
                        existingItem.setUpdateTime(LocalDateTime.now());
                        
                        complianceCheckItemMapper.updateById(existingItem);
                    } else {
                        // 创建新检查项
                        complianceCheckItemMapper.insert(checkItem);
                    }
                }
                
                log.info("{}合规检查完成，检查项数量: {}", complianceStandard, checkItems.size());
            }
            
            log.info("合规检查执行完成");
            return checkResults;
            
        } catch (Exception e) {
            log.error("执行合规检查失败: {}", e.getMessage(), e);
            throw new RuntimeException("执行合规检查失败", e);
        }
    }
    
    @Override
    public Map<String, Object> getComplianceStatistics(String complianceStandard) {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            // 获取合规检查项统计
            LambdaQueryWrapper<ComplianceCheckItem> wrapper = new LambdaQueryWrapper<>();
            if (complianceStandard != null && !complianceStandard.trim().isEmpty()) {
                wrapper.eq(ComplianceCheckItem::getComplianceStandard, complianceStandard);
            }
            
            List<ComplianceCheckItem> checkItems = complianceCheckItemMapper.selectList(wrapper);
            
            // 统计各种状态的数量
            long totalCount = checkItems.size();
            long passCount = checkItems.stream()
                .filter(item -> CheckStatus.PASS.name().equals(item.getPassStatus()))
                .count();
            long failCount = checkItems.stream()
                .filter(item -> CheckStatus.FAIL.name().equals(item.getPassStatus()))
                .count();
            long unknownCount = checkItems.stream()
                .filter(item -> CheckStatus.UNKNOWN.name().equals(item.getPassStatus()))
                .count();
            
            // 计算合规评分
            double complianceScore = totalCount > 0 ? (double) passCount / totalCount * 100 : 0.0;
            
            statistics.put("totalCount", totalCount);
            statistics.put("passCount", passCount);
            statistics.put("failCount", failCount);
            statistics.put("unknownCount", unknownCount);
            statistics.put("complianceScore", complianceScore);
            statistics.put("complianceRate", String.format("%.1f%%", complianceScore));
            
            // 获取最近的不合规项
            List<ComplianceCheckItem> recentFailedItems = getRecentFailedItems(10);
            statistics.put("recentFailedItems", recentFailedItems);
            
            // 获取合规趋势（最近30天）
            List<Map<String, Object>> trendData = getComplianceTrend(complianceStandard, 30);
            statistics.put("trendData", trendData);
            
        } catch (Exception e) {
            log.error("获取合规统计信息失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取合规统计信息失败", e);
        }
        
        return statistics;
    }
    
    @Override
    public List<ComplianceCheckItem> getRecentFailedItems(int limit) {
        LambdaQueryWrapper<ComplianceCheckItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ComplianceCheckItem::getPassStatus, CheckStatus.FAIL.name())
               .orderByDesc(ComplianceCheckItem::getCheckTime)
               .last("LIMIT " + limit);
        
        return complianceCheckItemMapper.selectList(wrapper);
    }
    
    @Override
    public double calculateComplianceScore(String complianceStandard) {
        return complianceCheckEngine.calculateComplianceScore(complianceStandard);
    }
    
    @Override
    public List<ComplianceCheckItem> getItemsNeedCheck(LocalDateTime currentTime) {
        return complianceCheckItemMapper.selectItemsNeedCheck(currentTime);
    }
    
    // 辅助方法：获取合规趋势数据
    private List<Map<String, Object>> getComplianceTrend(String complianceStandard, int days) {
        List<Map<String, Object>> trendData = new ArrayList<>();
        
        try {
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(days);
            
            // 按天统计合规评分
            for (int i = 0; i < days; i++) {
                LocalDateTime date = startDate.plusDays(i);
                
                LambdaQueryWrapper<ComplianceCheckItem> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(ComplianceCheckItem::getComplianceStandard, complianceStandard)
                       .ge(ComplianceCheckItem::getCheckTime, date)
                       .lt(ComplianceCheckItem::getCheckTime, date.plusDays(1));
                
                List<ComplianceCheckItem> dailyItems = complianceCheckItemMapper.selectList(wrapper);
                
                if (!dailyItems.isEmpty()) {
                    long passCount = dailyItems.stream()
                        .filter(item -> CheckStatus.PASS.name().equals(item.getPassStatus()))
                        .count();
                    
                    double dailyScore = (double) passCount / dailyItems.size() * 100;
                    
                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("date", DateUtil.format(date, "yyyy-MM-dd"));
                    dayData.put("score", dailyScore);
                    dayData.put("passCount", passCount);
                    dayData.put("totalCount", dailyItems.size());
                    
                    trendData.add(dayData);
                }
            }
            
        } catch (Exception e) {
            log.error("获取合规趋势数据失败: {}", e.getMessage(), e);
        }
        
        return trendData;
    }
}