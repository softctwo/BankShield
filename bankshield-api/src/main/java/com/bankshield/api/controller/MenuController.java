package com.bankshield.api.controller;

import com.bankshield.api.entity.Menu;
import com.bankshield.api.service.MenuService;
import com.bankshield.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 * 
 * @author BankShield
 */
@Slf4j
@RestController
@RequestMapping("/api/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

    /**
     * 获取所有菜单列表
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public Result<List<Menu>> getMenuList() {
        log.info("获取所有菜单列表");
        return menuService.getMenuList();
    }

    /**
     * 获取菜单树结构
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/tree")
    public Result<List<Menu>> getMenuTree() {
        log.info("获取菜单树结构");
        return menuService.getMenuTree();
    }

    /**
     * 根据ID获取菜单信息
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public Result<Menu> getMenuById(@PathVariable Long id) {
        log.info("根据ID获取菜单信息，ID: {}", id);
        return menuService.getMenuById(id);
    }

    /**
     * 获取父级菜单列表（用于选择上级菜单）
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/parent-list")
    public Result<List<Menu>> getParentMenuList() {
        log.info("获取父级菜单列表");
        return menuService.getParentMenuList();
    }

    /**
     * 创建菜单
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Result<Long> createMenu(@RequestBody Menu menu) {
        log.info("创建菜单，菜单名称: {}", menu.getMenuName());
        return menuService.createMenu(menu);
    }

    /**
     * 更新菜单
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Result<String> updateMenu(@PathVariable Long id, @RequestBody Menu menu) {
        log.info("更新菜单，ID: {}", id);
        menu.setId(id);
        return menuService.updateMenu(menu);
    }

    /**
     * 删除菜单
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public Result<String> deleteMenu(@PathVariable Long id) {
        log.info("删除菜单，ID: {}", id);
        return menuService.deleteMenu(id);
    }
}