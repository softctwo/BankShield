package com.bankshield.api.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.ReportTemplate;

/**
 * 报表模板服务接口
 */
public interface ReportTemplateService {
    
    /**
     * 分页查询报表模板
     */
    IPage<ReportTemplate> getTemplatePage(Page<ReportTemplate> page, String templateName, String reportType, Boolean enabled);
    
    /**
     * 根据ID查询报表模板
     */
    ReportTemplate getTemplateById(Long id);
    
    /**
     * 创建报表模板
     */
    ReportTemplate createTemplate(ReportTemplate template);
    
    /**
     * 更新报表模板
     */
    ReportTemplate updateTemplate(ReportTemplate template);
    
    /**
     * 删除报表模板
     */
    boolean deleteTemplate(Long id);
    
    /**
     * 启用/禁用模板
     */
    boolean toggleTemplateStatus(Long id, Boolean enabled);
    
    /**
     * 根据报表类型查询模板
     */
    ReportTemplate getTemplateByType(String reportType);
}