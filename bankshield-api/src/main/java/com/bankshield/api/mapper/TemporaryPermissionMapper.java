package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.TemporaryPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 临时权限Mapper
 */
@Mapper
public interface TemporaryPermissionMapper extends BaseMapper<TemporaryPermission> {
    
    /**
     * 查询用户的有效临时权限
     */
    @Select("SELECT * FROM temporary_permission WHERE user_id = #{userId} AND status = 'ACTIVE' " +
            "AND valid_from <= NOW() AND valid_to >= NOW()")
    List<TemporaryPermission> selectActiveByUserId(Long userId);
    
    /**
     * 查询即将过期的临时权限
     */
    @Select("SELECT * FROM temporary_permission WHERE status = 'ACTIVE' " +
            "AND valid_to BETWEEN NOW() AND #{expiryTime}")
    List<TemporaryPermission> selectExpiringPermissions(LocalDateTime expiryTime);
    
    /**
     * 查询已过期但未更新状态的权限
     */
    @Select("SELECT * FROM temporary_permission WHERE status = 'ACTIVE' AND valid_to < NOW()")
    List<TemporaryPermission> selectExpiredPermissions();
}
