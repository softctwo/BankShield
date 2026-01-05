package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.DesensitizationTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 脱敏模板Mapper接口
 */
@Mapper
public interface DesensitizationTemplateMapper extends BaseMapper<DesensitizationTemplate> {
    
    /**
     * 查询启用的模板
     */
    @Select("SELECT * FROM desensitization_template WHERE status = 'ENABLED'")
    List<DesensitizationTemplate> selectEnabledTemplates();
    
    /**
     * 根据模板编码查询
     */
    @Select("SELECT * FROM desensitization_template WHERE template_code = #{templateCode}")
    DesensitizationTemplate selectByTemplateCode(@Param("templateCode") String templateCode);
    
    /**
     * 根据模板类型查询
     */
    @Select("SELECT * FROM desensitization_template WHERE template_type = #{templateType} AND status = 'ENABLED'")
    List<DesensitizationTemplate> selectByTemplateType(@Param("templateType") String templateType);
    
    /**
     * 根据目标表查询
     */
    @Select("SELECT * FROM desensitization_template WHERE target_table = #{targetTable} AND status = 'ENABLED'")
    List<DesensitizationTemplate> selectByTargetTable(@Param("targetTable") String targetTable);
}
