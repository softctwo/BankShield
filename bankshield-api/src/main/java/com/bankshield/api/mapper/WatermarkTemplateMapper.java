package com.bankshield.api.mapper;

import com.bankshield.api.entity.WatermarkTemplate;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 水印模板Mapper接口
 * 
 * @author BankShield
 */
@Mapper
public interface WatermarkTemplateMapper extends BaseMapper<WatermarkTemplate> {

    /**
     * 分页查询水印模板
     * 
     * @param page 分页参数
     * @param templateName 模板名称（模糊查询）
     * @param watermarkType 水印类型
     * @param enabled 是否启用
     * @return 分页结果
     */
    IPage<WatermarkTemplate> selectTemplatePage(Page<WatermarkTemplate> page,
                                               @Param("templateName") String templateName,
                                               @Param("watermarkType") String watermarkType,
                                               @Param("enabled") Integer enabled);

    /**
     * 查询启用的模板列表
     * 
     * @param watermarkType 水印类型
     * @return 模板列表
     */
    List<WatermarkTemplate> selectEnabledTemplates(@Param("watermarkType") String watermarkType);

    /**
     * 检查模板名称是否已存在
     * 
     * @param templateName 模板名称
     * @param excludeId 排除的ID（更新时排除自身）
     * @return 是否存在
     */
    boolean existsByTemplateName(@Param("templateName") String templateName,
                                @Param("excludeId") Long excludeId);
}