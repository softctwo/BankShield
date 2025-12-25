package com.bankshield.api.service;

import com.bankshield.api.entity.Role;
import com.bankshield.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 角色服务接口
 * 提供角色管理功能，包含三权分立机制的角色互斥检查
 * 
 * @author BankShield
 */
public interface RoleService {

    /**
     * 根据ID获取角色信息
     * 
     * @param id 角色ID
     * @return 角色信息和结果状态
     */
    Result<Role> getRoleById(Long id);

    /**
     * 分页查询角色列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param roleName 角色名称（可选，用于模糊查询）
     * @param roleCode 角色编码（可选，用于模糊查询）
     * @return 分页角色数据和结果状态
     */
    Result<Page<Role>> getRolePage(int page, int size, String roleName, String roleCode);

    /**
     * 获取所有启用的角色列表
     * 
     * @return 角色列表和结果状态
     */
    Result<List<Role>> getAllEnabledRoles();

    /**
     * 添加角色
     * 
     * @param role 角色信息
     * @return 操作结果
     */
    Result<String> addRole(Role role);

    /**
     * 更新角色信息
     * 
     * @param role 角色信息
     * @return 操作结果
     */
    Result<String> updateRole(Role role);

    /**
     * 删除角色
     * 
     * @param id 角色ID
     * @return 操作结果
     */
    Result<String> deleteRole(Long id);

    /**
     * 给用户分配角色（带三权分立互斥检查）
     * 
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 操作结果
     */
    Result<String> assignRoleToUser(Long userId, Long roleId);

    /**
     * 批量给用户分配角色（带三权分立互斥检查）
     * 
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 操作结果
     */
    Result<String> assignRolesToUser(Long userId, List<Long> roleIds);

    /**
     * 移除用户角色
     * 
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 操作结果
     */
    Result<String> removeRoleFromUser(Long userId, Long roleId);

    /**
     * 获取用户的角色列表
     * 
     * @param userId 用户ID
     * @return 角色列表和结果状态
     */
    Result<List<Role>> getUserRoles(Long userId);

    /**
     * 检查角色分配是否违反三权分立原则
     * 
     * @param userId 用户ID
     * @param roleCode 角色编码
     * @return 检查结果
     */
    Result<Boolean> checkRoleAssignment(Long userId, String roleCode);
}