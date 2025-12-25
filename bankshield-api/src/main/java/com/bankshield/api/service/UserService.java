package com.bankshield.api.service;

import com.bankshield.api.entity.User;
import com.bankshield.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Map;

/**
 * 用户服务接口
 * 
 * @author BankShield
 */
public interface UserService {

    /**
     * 根据ID获取用户信息
     * 
     * @param id 用户ID
     * @return 用户信息和结果状态
     */
    Result<User> getUserById(Long id);

    /**
     * 分页查询用户列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param username 用户名（可选，用于模糊查询）
     * @param deptId 部门ID（可选）
     * @return 分页用户数据和结果状态
     */
    Result<Page<User>> getUserPage(int page, int size, String username, Long deptId);

    /**
     * 添加用户
     * 
     * @param user 用户信息
     * @return 操作结果
     */
    Result<String> addUser(User user);

    /**
     * 更新用户信息
     * 
     * @param user 用户信息
     * @return 操作结果
     */
    Result<String> updateUser(User user);

    /**
     * 删除用户
     * 
     * @param id 用户ID
     * @return 操作结果
     */
    Result<String> deleteUser(Long id);

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果（包含JWT token和用户信息）
     */
    Result<Map<String, Object>> login(String username, String password);
}