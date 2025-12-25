package com.bankshield.api.mapper;

import com.bankshield.api.entity.SensitiveDataTemplate;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 敏感数据类型模板Mapper接口
 * 
 * @author BankShield
 */
@Mapper
public interface SensitiveDataTemplateMapper extends BaseMapper<SensitiveDataTemplate> {

    /**
     * 按安全等级查询模板
     * 
     * @param securityLevel 安全等级
     * @return 模板列表
     */
    List<SensitiveDataTemplate> selectBySecurityLevel(@Param("securityLevel") Integer securityLevel);

    /**
     * 按类型编码查询模板
     * 
     * @param typeCode 类型编码
     * @return 模板信息
     */
    SensitiveDataTemplate selectByTypeCode(@Param("typeCode") String typeCode);

    /**
     * 查询所有启用的模板
     * 
     * @return 启用的模板列表
     */
    List<SensitiveDataTemplate> selectAllActive();
}