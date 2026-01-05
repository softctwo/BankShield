package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.IpBlacklist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * IP黑名单Mapper
 */
@Mapper
public interface IpBlacklistMapper extends BaseMapper<IpBlacklist> {
    
    /**
     * 查询所有活跃的黑名单
     */
    @Select("SELECT * FROM ip_blacklist WHERE status = 'ACTIVE' " +
            "AND (expire_time IS NULL OR expire_time > NOW())")
    List<IpBlacklist> selectActive();
    
    /**
     * 根据IP地址查询
     */
    @Select("SELECT * FROM ip_blacklist WHERE ip_address = #{ipAddress} AND status = 'ACTIVE' " +
            "AND (expire_time IS NULL OR expire_time > NOW())")
    IpBlacklist selectByIpAddress(String ipAddress);
    
    /**
     * 查询已过期但未更新状态的记录
     */
    @Select("SELECT * FROM ip_blacklist WHERE status = 'ACTIVE' " +
            "AND expire_time IS NOT NULL AND expire_time < NOW()")
    List<IpBlacklist> selectExpired();
}
