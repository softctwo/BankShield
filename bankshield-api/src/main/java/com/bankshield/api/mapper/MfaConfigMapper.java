package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.MfaConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * MFA配置Mapper
 */
@Mapper
public interface MfaConfigMapper extends BaseMapper<MfaConfig> {
    
    /**
     * 根据用户ID和MFA类型查询
     */
    @Select("SELECT * FROM mfa_config WHERE user_id = #{userId} AND mfa_type = #{mfaType}")
    MfaConfig selectByUserIdAndType(Long userId, String mfaType);
    
    /**
     * 根据用户ID查询所有MFA配置
     */
    @Select("SELECT * FROM mfa_config WHERE user_id = #{userId}")
    List<MfaConfig> selectByUserId(Long userId);
    
    /**
     * 查询用户启用的MFA配置
     */
    @Select("SELECT * FROM mfa_config WHERE user_id = #{userId} AND mfa_enabled = 1")
    List<MfaConfig> selectEnabledByUserId(Long userId);
    
    /**
     * 检查用户是否启用了MFA
     */
    @Select("SELECT COUNT(*) FROM mfa_config WHERE user_id = #{userId} AND mfa_enabled = 1")
    int countEnabledMfa(Long userId);
}
