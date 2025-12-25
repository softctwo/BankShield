package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.ReportTemplate;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 报表模板Mapper接口
 */
public interface ReportTemplateMapper extends BaseMapper<ReportTemplate> {
    
    /**
     * 分页查询报表模板
     */
    IPage<ReportTemplate> selectTemplatePage(Page<ReportTemplate> page, 
                                             @Param("templateName") String templateName,
                                             @Param("reportType") String reportType,
                                             @Param("enabled") Boolean enabled);
    
    /**
     * 查询已启用的指定频率的模板
     */
    List<ReportTemplate> selectEnabledByFrequency(@Param("frequency") String frequency);
}