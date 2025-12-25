package com.bankshield.api.mapper;

import com.bankshield.api.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户Mapper接口
 * 
 * @author BankShield
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户ID查询角色编码列表
     * 
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 统计指定角色的用户数量
     * 
     * @param roleCode 角色编码
     * @return 用户数量
     */
    long countUsersByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 查询活跃用户
     *
     * @param limit 限制数量
     * @return 活跃用户列表
     */
    List<com.bankshield.api.entity.User> selectActiveUsers(@Param("limit") int limit);
}