package com.bankshield.api.service.impl;

import com.bankshield.api.entity.WatermarkTemplate;
import com.bankshield.api.enums.WatermarkPosition;
import com.bankshield.api.enums.WatermarkType;
import com.bankshield.api.mapper.WatermarkTemplateMapper;
import com.bankshield.api.service.WatermarkTemplateService;
import com.bankshield.common.result.Result;
import com.bankshield.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 水印模板服务实现类
 * 
 * @author BankShield
 */
@Slf4j
@Service
public class WatermarkTemplateServiceImpl extends ServiceImpl<WatermarkTemplateMapper, WatermarkTemplate> 
        implements WatermarkTemplateService {

    @Autowired
    private WatermarkTemplateMapper templateMapper;

    @Override
    public IPage<WatermarkTemplate> getTemplatePage(Page<WatermarkTemplate> page, String templateName, 
                                                   String watermarkType, Integer enabled) {
        log.info("分页查询水印模板，页码: {}, 每页大小: {}, 模板名称: {}, 水印类型: {}, 是否启用: {}", 
                page.getCurrent(), page.getSize(), templateName, watermarkType, enabled);
        
        try {
            return templateMapper.selectTemplatePage(page, templateName, watermarkType, enabled);
        } catch (Exception e) {
            log.error("分页查询水印模板失败", e);
            throw new BusinessException("查询水印模板失败");
        }
    }

    @Override
    public List<WatermarkTemplate> getEnabledTemplates(String watermarkType) {
        log.info("查询启用的水印模板列表，水印类型: {}", watermarkType);
        
        try {
            return templateMapper.selectEnabledTemplates(watermarkType);
        } catch (Exception e) {
            log.error("查询启用的水印模板列表失败", e);
            throw new BusinessException("查询启用的模板列表失败");
        }
    }

    @Override
    public WatermarkTemplate getTemplateById(Long id) {
        log.info("根据ID获取水印模板，ID: {}", id);
        
        try {
            WatermarkTemplate template = templateMapper.selectById(id);
            if (template == null) {
                throw new BusinessException("水印模板不存在");
            }
            return template;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取水印模板失败，ID: {}", id, e);
            throw new BusinessException("获取水印模板失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WatermarkTemplate createTemplate(WatermarkTemplate template) {
        log.info("创建水印模板，模板名称: {}", template.getTemplateName());
        
        try {
            // 参数验证
            if (!validateTemplate(template)) {
                throw new BusinessException("模板参数验证失败");
            }
            
            // 检查模板名称是否已存在
            if (templateMapper.existsByTemplateName(template.getTemplateName(), null)) {
                throw new BusinessException("模板名称已存在");
            }
            
            // 设置默认值
            if (template.getEnabled() == null) {
                template.setEnabled(1);
            }
            if (template.getTransparency() == null) {
                template.setTransparency(30);
            }
            template.setCreateTime(LocalDateTime.now());
            template.setUpdateTime(LocalDateTime.now());
            
            // 根据水印类型设置默认值
            if (WatermarkType.TEXT.getCode().equals(template.getWatermarkType())) {
                if (template.getFontSize() == null) {
                    template.setFontSize(12);
                }
                if (template.getFontColor() == null) {
                    template.setFontColor("#CCCCCC");
                }
                if (template.getFontFamily() == null) {
                    template.setFontFamily("Arial");
                }
                if (template.getWatermarkPosition() == null) {
                    template.setWatermarkPosition(WatermarkPosition.BOTTOM_RIGHT.getCode());
                }
            } else if (WatermarkType.IMAGE.getCode().equals(template.getWatermarkType())) {
                if (template.getWatermarkPosition() == null) {
                    template.setWatermarkPosition(WatermarkPosition.TOP_LEFT.getCode());
                }
            }
            
            templateMapper.insert(template);
            log.info("创建水印模板成功，ID: {}", template.getId());
            return template;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建水印模板失败", e);
            throw new BusinessException("创建水印模板失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WatermarkTemplate updateTemplate(WatermarkTemplate template) {
        log.info("更新水印模板，ID: {}", template.getId());
        
        try {
            if (template.getId() == null) {
                throw new BusinessException("模板ID不能为空");
            }
            
            // 检查模板是否存在
            WatermarkTemplate existingTemplate = templateMapper.selectById(template.getId());
            if (existingTemplate == null) {
                throw new BusinessException("水印模板不存在");
            }
            
            // 参数验证
            if (!validateTemplate(template)) {
                throw new BusinessException("模板参数验证失败");
            }
            
            // 检查模板名称是否已存在（排除自身）
            if (template.getTemplateName() != null && 
                templateMapper.existsByTemplateName(template.getTemplateName(), template.getId())) {
                throw new BusinessException("模板名称已存在");
            }
            
            // 设置更新时间
            template.setUpdateTime(LocalDateTime.now());
            
            templateMapper.updateById(template);
            log.info("更新水印模板成功，ID: {}", template.getId());
            return template;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新水印模板失败，ID: {}", template.getId(), e);
            throw new BusinessException("更新水印模板失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTemplate(Long id) {
        log.info("删除水印模板，ID: {}", id);
        
        try {
            // 检查模板是否存在
            WatermarkTemplate template = templateMapper.selectById(id);
            if (template == null) {
                throw new BusinessException("水印模板不存在");
            }
            
            // 检查是否有正在使用的任务（这里需要根据实际需求实现）
            // 可以添加逻辑检查是否有未完成的任务在使用此模板
            
            int result = templateMapper.deleteById(id);
            boolean success = result > 0;
            
            if (success) {
                log.info("删除水印模板成功，ID: {}", id);
            } else {
                log.warn("删除水印模板失败，ID: {}", id);
            }
            
            return success;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除水印模板失败，ID: {}", id, e);
            throw new BusinessException("删除水印模板失败");
        }
    }

    @Override
    public boolean validateTemplate(WatermarkTemplate template) {
        if (template == null) {
            return false;
        }
        
        // 验证必填字段
        if (template.getTemplateName() == null || template.getTemplateName().trim().isEmpty()) {
            log.warn("模板名称为空");
            return false;
        }
        
        if (template.getWatermarkType() == null || template.getWatermarkType().trim().isEmpty()) {
            log.warn("水印类型为空");
            return false;
        }
        
        if (template.getWatermarkContent() == null || template.getWatermarkContent().trim().isEmpty()) {
            log.warn("水印内容为空");
            return false;
        }
        
        // 验证水印类型
        WatermarkType watermarkType = WatermarkType.fromCode(template.getWatermarkType());
        if (watermarkType == null) {
            log.warn("无效的水印类型: {}", template.getWatermarkType());
            return false;
        }
        
        // 根据水印类型验证特定字段
        switch (watermarkType) {
            case TEXT:
                if (template.getFontSize() != null && template.getFontSize() <= 0) {
                    log.warn("字体大小必须大于0");
                    return false;
                }
                if (template.getTransparency() != null && 
                    (template.getTransparency() < 0 || template.getTransparency() > 100)) {
                    log.warn("透明度必须在0-100之间");
                    return false;
                }
                if (template.getWatermarkPosition() != null) {
                    WatermarkPosition position = WatermarkPosition.fromCode(template.getWatermarkPosition());
                    if (position == null) {
                        log.warn("无效的水印位置: {}", template.getWatermarkPosition());
                        return false;
                    }
                }
                break;
                
            case IMAGE:
                if (template.getWatermarkPosition() != null) {
                    WatermarkPosition position = WatermarkPosition.fromCode(template.getWatermarkPosition());
                    if (position == null) {
                        log.warn("无效的水印位置: {}", template.getWatermarkPosition());
                        return false;
                    }
                }
                if (template.getTransparency() != null && 
                    (template.getTransparency() < 0 || template.getTransparency() > 100)) {
                    log.warn("透明度必须在0-100之间");
                    return false;
                }
                break;
                
            case DATABASE:
                // 数据库水印不需要位置和透明度设置
                break;
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleTemplateStatus(Long id, Integer enabled) {
        log.info("切换水印模板状态，ID: {}, 启用状态: {}", id, enabled);
        
        try {
            if (id == null || enabled == null) {
                throw new BusinessException("参数不能为空");
            }
            
            // 检查模板是否存在
            WatermarkTemplate template = templateMapper.selectById(id);
            if (template == null) {
                throw new BusinessException("水印模板不存在");
            }
            
            template.setEnabled(enabled);
            template.setUpdateTime(LocalDateTime.now());
            
            int result = templateMapper.updateById(template);
            boolean success = result > 0;
            
            if (success) {
                log.info("切换水印模板状态成功，ID: {}, 启用状态: {}", id, enabled);
            } else {
                log.warn("切换水印模板状态失败，ID: {}, 启用状态: {}", id, enabled);
            }
            
            return success;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("切换水印模板状态失败，ID: {}", id, e);
            throw new BusinessException("切换模板状态失败");
        }
    }
}