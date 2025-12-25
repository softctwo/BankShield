package com.bankshield.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bankshield.api.entity.ReportTemplate;
import com.bankshield.api.mapper.ReportTemplateMapper;
import com.bankshield.api.service.ReportTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 报表模板服务实现类
 */
@Slf4j
@Service
public class ReportTemplateServiceImpl extends ServiceImpl<ReportTemplateMapper, ReportTemplate> implements ReportTemplateService {
    
    @Autowired
    private ReportTemplateMapper reportTemplateMapper;
    
    @Override
    public IPage<ReportTemplate> getTemplatePage(Page<ReportTemplate> page, String templateName, String reportType, Boolean enabled) {
        LambdaQueryWrapper<ReportTemplate> wrapper = new LambdaQueryWrapper<>();
        
        if (templateName != null && !templateName.trim().isEmpty()) {
            wrapper.like(ReportTemplate::getTemplateName, templateName);
        }
        
        if (reportType != null && !reportType.trim().isEmpty()) {
            wrapper.eq(ReportTemplate::getReportType, reportType);
        }
        
        if (enabled != null) {
            wrapper.eq(ReportTemplate::getEnabled, enabled);
        }
        
        wrapper.orderByDesc(ReportTemplate::getCreateTime);
        return reportTemplateMapper.selectPage(page, wrapper);
    }
    
    @Override
    public ReportTemplate getTemplateById(Long id) {
        return reportTemplateMapper.selectById(id);
    }
    
    @Override
    @Transactional
    public ReportTemplate createTemplate(ReportTemplate template) {
        // 检查模板名称是否已存在
        LambdaQueryWrapper<ReportTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReportTemplate::getTemplateName, template.getTemplateName());
        
        if (reportTemplateMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("模板名称已存在");
        }
        
        reportTemplateMapper.insert(template);
        log.info("创建报表模板成功: {}", template.getTemplateName());
        return template;
    }
    
    @Override
    @Transactional
    public ReportTemplate updateTemplate(ReportTemplate template) {
        ReportTemplate existingTemplate = reportTemplateMapper.selectById(template.getId());
        if (existingTemplate == null) {
            throw new RuntimeException("报表模板不存在");
        }
        
        // 检查模板名称是否重复
        if (!existingTemplate.getTemplateName().equals(template.getTemplateName())) {
            LambdaQueryWrapper<ReportTemplate> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ReportTemplate::getTemplateName, template.getTemplateName())
                   .ne(ReportTemplate::getId, template.getId());
            
            if (reportTemplateMapper.selectCount(wrapper) > 0) {
                throw new RuntimeException("模板名称已存在");
            }
        }
        
        reportTemplateMapper.updateById(template);
        log.info("更新报表模板成功: {}", template.getTemplateName());
        return template;
    }
    
    @Override
    @Transactional
    public boolean deleteTemplate(Long id) {
        ReportTemplate template = reportTemplateMapper.selectById(id);
        if (template == null) {
            throw new RuntimeException("报表模板不存在");
        }
        
        int result = reportTemplateMapper.deleteById(id);
        if (result > 0) {
            log.info("删除报表模板成功: {}", template.getTemplateName());
            return true;
        }
        return false;
    }
    
    @Override
    @Transactional
    public boolean toggleTemplateStatus(Long id, Boolean enabled) {
        ReportTemplate template = reportTemplateMapper.selectById(id);
        if (template == null) {
            throw new RuntimeException("报表模板不存在");
        }
        
        template.setEnabled(enabled);
        int result = reportTemplateMapper.updateById(template);
        if (result > 0) {
            log.info("更新报表模板状态成功: {} -> {}", template.getTemplateName(), enabled ? "启用" : "禁用");
            return true;
        }
        return false;
    }
    
    @Override
    public ReportTemplate getTemplateByType(String reportType) {
        LambdaQueryWrapper<ReportTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReportTemplate::getReportType, reportType)
               .eq(ReportTemplate::getEnabled, true)
               .orderByDesc(ReportTemplate::getCreateTime)
               .last("LIMIT 1");
        
        return reportTemplateMapper.selectOne(wrapper);
    }
}