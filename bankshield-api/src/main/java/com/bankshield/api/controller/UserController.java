package com.bankshield.api.controller;

import com.bankshield.api.dto.LoginRequest;
import com.bankshield.api.entity.User;
import com.bankshield.api.service.UserService;
import com.bankshield.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 *
 * @author BankShield
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        log.info("查询用户信息，ID: {}", id);
        return userService.getUserById(id);
    }

    /**
     * 分页查询用户列表
     */
    @GetMapping("/page")
    public Result<Page<User>> getUserPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Long deptId) {
        log.info("分页查询用户列表，页码: {}, 每页大小: {}, 用户名: {}, 部门ID: {}", page, size, username, deptId);
        return userService.getUserPage(page, size, username, deptId);
    }

    /**
     * 添加用户
     */
    @PostMapping
    public Result<String> addUser(@RequestBody User user) {
        log.info("添加用户，用户名: {}", user.getUsername());
        return userService.addUser(user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping
    public Result<String> updateUser(@RequestBody User user) {
        log.info("更新用户信息，ID: {}", user.getId());
        return userService.updateUser(user);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        log.info("删除用户，ID: {}", id);
        return userService.deleteUser(id);
    }

    /**
     * 用户登录
     * 使用RequestBody传递密码，避免在URL参数中暴露敏感信息
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginRequest request) {
        log.info("用户登录，用户名: {}", request.getUsername());
        return userService.login(request.getUsername(), request.getPassword());
    }
}