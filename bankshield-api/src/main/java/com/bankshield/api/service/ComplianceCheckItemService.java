package com.bankshield.api.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.ComplianceCheckItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 合规检查项服务接口
 */
public interface ComplianceCheckItemService {
    
    /**
     * 分页查询合规检查项
     */
    IPage<ComplianceCheckItem> getCheckItemPage(Page<ComplianceCheckItem> page, String checkItemName, String complianceStandard, String passStatus);
    
    /**
     * 根据ID查询检查项
     */
    ComplianceCheckItem getCheckItemById(Long id);
    
    /**
     * 创建检查项
     */
    ComplianceCheckItem createCheckItem(ComplianceCheckItem checkItem);
    
    /**
     * 更新检查项
     */
    ComplianceCheckItem updateCheckItem(ComplianceCheckItem checkItem);
    
    /**
     * 删除检查项
     */
    boolean deleteCheckItem(Long id);
    
    /**
     * 执行合规检查
     */
    Map<String, List<ComplianceCheckItem>> performComplianceChecks();
    
    /**
     * 获取合规统计信息
     */
    Map<String, Object> getComplianceStatistics(String complianceStandard);
    
    /**
     * 获取最近的不合规项
     */
    List<ComplianceCheckItem> getRecentFailedItems(int limit);
    
    /**
     * 计算合规评分
     */
    double calculateComplianceScore(String complianceStandard);
    
    /**
     * 查询需要检查的项目
     */
    List<ComplianceCheckItem> getItemsNeedCheck(LocalDateTime currentTime);
}