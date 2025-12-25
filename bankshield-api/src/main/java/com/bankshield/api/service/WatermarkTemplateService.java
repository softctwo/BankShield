package com.bankshield.api.service;

import com.bankshield.api.entity.WatermarkTemplate;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 水印模板服务接口
 * 
 * @author BankShield
 */
public interface WatermarkTemplateService {

    /**
     * 分页查询水印模板
     * 
     * @param page 分页参数
     * @param templateName 模板名称
     * @param watermarkType 水印类型
     * @param enabled 是否启用
     * @return 分页结果
     */
    IPage<WatermarkTemplate> getTemplatePage(Page<WatermarkTemplate> page, String templateName, 
                                           String watermarkType, Integer enabled);

    /**
     * 获取启用的模板列表
     * 
     * @param watermarkType 水印类型
     * @return 模板列表
     */
    List<WatermarkTemplate> getEnabledTemplates(String watermarkType);

    /**
     * 根据ID获取模板
     * 
     * @param id 模板ID
     * @return 模板信息
     */
    WatermarkTemplate getTemplateById(Long id);

    /**
     * 创建水印模板
     * 
     * @param template 模板信息
     * @return 创建的模板
     */
    WatermarkTemplate createTemplate(WatermarkTemplate template);

    /**
     * 更新水印模板
     * 
     * @param template 模板信息
     * @return 更新后的模板
     */
    WatermarkTemplate updateTemplate(WatermarkTemplate template);

    /**
     * 删除水印模板
     * 
     * @param id 模板ID
     * @return 是否删除成功
     */
    boolean deleteTemplate(Long id);

    /**
     * 验证模板参数
     * 
     * @param template 模板信息
     * @return 验证结果
     */
    boolean validateTemplate(WatermarkTemplate template);

    /**
     * 启用/禁用模板
     * 
     * @param id 模板ID
     * @param enabled 是否启用
     * @return 是否成功
     */
    boolean toggleTemplateStatus(Long id, Integer enabled);
}