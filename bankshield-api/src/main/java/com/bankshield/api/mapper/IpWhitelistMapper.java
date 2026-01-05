package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.IpWhitelist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * IP白名单Mapper
 */
@Mapper
public interface IpWhitelistMapper extends BaseMapper<IpWhitelist> {
    
    /**
     * 查询所有启用的白名单
     */
    @Select("SELECT * FROM ip_whitelist WHERE status = 'ENABLED'")
    List<IpWhitelist> selectEnabled();
    
    /**
     * 根据应用范围查询
     */
    @Select("SELECT * FROM ip_whitelist WHERE apply_to = #{applyTo} AND status = 'ENABLED'")
    List<IpWhitelist> selectByApplyTo(String applyTo);
    
    /**
     * 根据目标ID查询
     */
    @Select("SELECT * FROM ip_whitelist WHERE apply_to = #{applyTo} AND target_id = #{targetId} AND status = 'ENABLED'")
    List<IpWhitelist> selectByTarget(String applyTo, Long targetId);
}
