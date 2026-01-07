package com.bankshield.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.DesensitizationTemplate;
import com.bankshield.api.mapper.DesensitizationTemplateMapper;
import com.bankshield.api.service.DesensitizationTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 脱敏模板服务实现
 */
@Slf4j
@Service
public class DesensitizationTemplateServiceImpl implements DesensitizationTemplateService {
    
    @Autowired
    private DesensitizationTemplateMapper templateMapper;
    
    @Override
    public boolean createTemplate(DesensitizationTemplate template) {
        try {
            return templateMapper.insert(template) > 0;
        } catch (Exception e) {
            log.error("创建脱敏模板失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean updateTemplate(DesensitizationTemplate template) {
        try {
            return templateMapper.updateById(template) > 0;
        } catch (Exception e) {
            log.error("更新脱敏模板失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean deleteTemplate(Long id) {
        try {
            return templateMapper.deleteById(id) > 0;
        } catch (Exception e) {
            log.error("删除脱敏模板失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public DesensitizationTemplate getById(Long id) {
        try {
            return templateMapper.selectById(id);
        } catch (Exception e) {
            log.error("查询脱敏模板失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public DesensitizationTemplate getByTemplateCode(String templateCode) {
        try {
            return templateMapper.selectByTemplateCode(templateCode);
        } catch (Exception e) {
            log.error("根据模板编码查询失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public List<DesensitizationTemplate> getEnabledTemplates() {
        try {
            return templateMapper.selectEnabledTemplates();
        } catch (Exception e) {
            log.error("查询启用模板失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public List<DesensitizationTemplate> getTemplatesByType(String templateType) {
        try {
            return templateMapper.selectByTemplateType(templateType);
        } catch (Exception e) {
            log.error("根据模板类型查询失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public List<DesensitizationTemplate> getTemplatesByTargetTable(String targetTable) {
        try {
            return templateMapper.selectByTargetTable(targetTable);
        } catch (Exception e) {
            log.error("根据目标表查询模板失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public boolean enableTemplate(Long id) {
        try {
            DesensitizationTemplate template = new DesensitizationTemplate();
            template.setId(id);
            template.setStatus("ENABLED");
            return templateMapper.updateById(template) > 0;
        } catch (Exception e) {
            log.error("启用模板失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean disableTemplate(Long id) {
        try {
            DesensitizationTemplate template = new DesensitizationTemplate();
            template.setId(id);
            template.setStatus("DISABLED");
            return templateMapper.updateById(template) > 0;
        } catch (Exception e) {
            log.error("禁用模板失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public Page<DesensitizationTemplate> pageTemplates(int current, int size, String templateType, String status) {
        try {
            Page<DesensitizationTemplate> page = new Page<>(current, size);
            LambdaQueryWrapper<DesensitizationTemplate> wrapper = new LambdaQueryWrapper<>();
            
            if (templateType != null && !templateType.isEmpty()) {
                wrapper.eq(DesensitizationTemplate::getTemplateType, templateType);
            }
            if (status != null && !status.isEmpty()) {
                wrapper.eq(DesensitizationTemplate::getStatus, status);
            }
            
            wrapper.orderByDesc(DesensitizationTemplate::getCreateTime);
            
            return templateMapper.selectPage(page, wrapper);
        } catch (Exception e) {
            log.error("分页查询模板失败: {}", e.getMessage(), e);
            return new Page<>();
        }
    }
    
    @Override
    public boolean applyTemplate(String templateCode, String userId, String userName) {
        try {
            DesensitizationTemplate template = templateMapper.selectByTemplateCode(templateCode);
            if (template == null) {
                log.error("模板不存在: {}", templateCode);
                return false;
            }
            
            log.info("应用脱敏模板: {}, 用户: {}", templateCode, userName);
            return true;
        } catch (Exception e) {
            log.error("应用模板失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<DesensitizationTemplate> pageTemplates(
            Page<DesensitizationTemplate> page, String templateName, String templateType, String status) {
        try {
            LambdaQueryWrapper<DesensitizationTemplate> wrapper = new LambdaQueryWrapper<>();
            
            if (templateName != null && !templateName.isEmpty()) {
                wrapper.like(DesensitizationTemplate::getTemplateName, templateName);
            }
            if (templateType != null && !templateType.isEmpty()) {
                wrapper.eq(DesensitizationTemplate::getTemplateType, templateType);
            }
            if (status != null && !status.isEmpty()) {
                wrapper.eq(DesensitizationTemplate::getStatus, status);
            }
            
            wrapper.orderByDesc(DesensitizationTemplate::getCreateTime);
            
            return templateMapper.selectPage(page, wrapper);
        } catch (Exception e) {
            log.error("分页查询模板失败: {}", e.getMessage(), e);
            return new Page<>();
        }
    }
    
    @Override
    public boolean applyTemplate(Long id, String scheduleTime) {
        try {
            DesensitizationTemplate template = templateMapper.selectById(id);
            if (template == null) {
                log.error("模板不存在: {}", id);
                return false;
            }
            
            log.info("应用脱敏模板: {}, 计划时间: {}", id, scheduleTime);
            return true;
        } catch (Exception e) {
            log.error("应用模板失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean updateTemplateStatus(Long id, String status) {
        try {
            DesensitizationTemplate template = new DesensitizationTemplate();
            template.setId(id);
            template.setStatus(status);
            return templateMapper.updateById(template) > 0;
        } catch (Exception e) {
            log.error("更新模板状态失败: {}", e.getMessage(), e);
            return false;
        }
    }
}
