package com.bankshield.api.controller;

import com.bankshield.api.entity.Role;
import com.bankshield.api.service.RoleService;
import com.bankshield.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 * 
 * @author BankShield
 */
@Slf4j
@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 根据ID获取角色信息
     */
    @GetMapping("/{id}")
    public Result<Role> getRoleById(@PathVariable Long id) {
        log.info("查询角色信息，ID: {}", id);
        return roleService.getRoleById(id);
    }

    /**
     * 分页查询角色列表
     */
    @GetMapping("/page")
    public Result<Page<Role>> getRolePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String roleCode) {
        log.info("分页查询角色列表，页码: {}, 每页大小: {}, 角色名称: {}, 角色编码: {}", page, size, roleName, roleCode);
        return roleService.getRolePage(page, size, roleName, roleCode);
    }

    /**
     * 获取所有启用的角色列表
     */
    @GetMapping("/enabled")
    public Result<List<Role>> getAllEnabledRoles() {
        log.info("获取所有启用的角色列表");
        return roleService.getAllEnabledRoles();
    }

    /**
     * 添加角色
     */
    @PostMapping
    public Result<String> addRole(@RequestBody Role role) {
        log.info("添加角色，角色名称: {}", role.getRoleName());
        return roleService.addRole(role);
    }

    /**
     * 更新角色信息
     */
    @PutMapping
    public Result<String> updateRole(@RequestBody Role role) {
        log.info("更新角色信息，ID: {}", role.getId());
        return roleService.updateRole(role);
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteRole(@PathVariable Long id) {
        log.info("删除角色，ID: {}", id);
        return roleService.deleteRole(id);
    }
}