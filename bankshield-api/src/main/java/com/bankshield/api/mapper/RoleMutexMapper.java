package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.RoleMutex;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色互斥规则Mapper接口
 * 提供角色互斥规则的数据库操作
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Mapper
public interface RoleMutexMapper extends BaseMapper<RoleMutex> {

    /**
     * 根据角色编码查询互斥角色列表
     * 
     * @param roleCode 角色编码
     * @return 互斥角色编码列表
     */
    List<String> selectMutexRoleCodesByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 批量查询角色互斥关系
     * 
     * @param roleCodes 角色编码列表
     * @return 互斥规则列表
     */
    List<RoleMutex> selectByRoleCodes(@Param("roleCodes") List<String> roleCodes);

    /**
     * 检查两个角色是否互斥
     * 
     * @param roleCode1 角色编码1
     * @param roleCode2 角色编码2
     * @return 互斥规则数量（大于0表示互斥）
     */
    int countMutexRoles(@Param("roleCode1") String roleCode1, @Param("roleCode2") String roleCode2);

    /**
     * 检查用户角色是否存在互斥
     * 
     * @param userId 用户ID
     * @return 互斥规则列表
     */
    List<RoleMutex> selectUserRoleConflicts(@Param("userId") Long userId);
}