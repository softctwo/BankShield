package com.bankshield.api.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.DesensitizationTemplate;

import java.util.List;

/**
 * 脱敏模板服务接口
 */
public interface DesensitizationTemplateService {
    
    /**
     * 创建模板
     */
    boolean createTemplate(DesensitizationTemplate template);
    
    /**
     * 更新模板
     */
    boolean updateTemplate(DesensitizationTemplate template);
    
    /**
     * 删除模板
     */
    boolean deleteTemplate(Long id);
    
    /**
     * 根据ID获取模板
     */
    DesensitizationTemplate getById(Long id);
    
    /**
     * 根据模板编码获取模板
     */
    DesensitizationTemplate getByTemplateCode(String templateCode);
    
    /**
     * 获取所有启用的模板
     */
    List<DesensitizationTemplate> getEnabledTemplates();
    
    /**
     * 根据模板类型获取模板
     */
    List<DesensitizationTemplate> getTemplatesByType(String templateType);
    
    /**
     * 根据目标表获取模板
     */
    List<DesensitizationTemplate> getTemplatesByTargetTable(String targetTable);
    
    /**
     * 启用模板
     */
    boolean enableTemplate(Long id);
    
    /**
     * 禁用模板
     */
    boolean disableTemplate(Long id);
    
    /**
     * 分页查询模板
     */
    Page<DesensitizationTemplate> pageTemplates(int current, int size, String templateType, String status);
    
    /**
     * 分页查询模板（带Page对象）
     */
    com.baomidou.mybatisplus.core.metadata.IPage<DesensitizationTemplate> pageTemplates(
            Page<DesensitizationTemplate> page, String templateName, String templateType, String status);
    
    /**
     * 应用模板进行脱敏
     */
    boolean applyTemplate(String templateCode, String userId, String userName);
    
    /**
     * 应用模板进行脱敏（指定ID）
     */
    boolean applyTemplate(Long id, String scheduleTime);
    
    /**
     * 更新模板状态
     */
    boolean updateTemplateStatus(Long id, String status);
}
