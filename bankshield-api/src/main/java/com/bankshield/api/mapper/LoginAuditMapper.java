package com.bankshield.api.mapper;

import com.bankshield.api.entity.LoginAudit;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录审计Mapper接口
 * 
 * @author BankShield
 */
@Mapper
public interface LoginAuditMapper extends BaseMapper<LoginAudit> {
}