package com.bankshield.api.controller;

import com.bankshield.api.entity.SysMenu;
import com.bankshield.api.service.SysMenuService;
import com.bankshield.common.core.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/system/menu")
@RequiredArgsConstructor
public class SysMenuController {
    
    private final SysMenuService menuService;
    
    /**
     * 获取用户菜单树
     */
    @GetMapping("/user/{userId}")
    public Result<List<Map<String, Object>>> getUserMenuTree(@PathVariable Long userId) {
        List<Map<String, Object>> menuTree = menuService.getMenuTreeByUserId(userId);
        return Result.success(menuTree);
    }
    
    /**
     * 获取用户权限
     */
    @GetMapping("/perms/{userId}")
    public Result<List<String>> getUserPerms(@PathVariable Long userId) {
        List<String> perms = menuService.getPermsByUserId(userId);
        return Result.success(perms);
    }
    
    /**
     * 获取路由信息
     */
    @GetMapping("/getRouters")
    public Result<Map<String, Object>> getRouters() {
        // 从token或session中获取用户ID，这里暂时使用固定值
        Long userId = 1L;
        
        List<Map<String, Object>> menuTree = menuService.getMenuTreeByUserId(userId);
        List<String> perms = menuService.getPermsByUserId(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("menus", menuTree);
        result.put("permissions", perms);
        
        return Result.success(result);
    }
    
    /**
     * 查询所有菜单
     */
    @GetMapping("/list")
    public Result<List<SysMenu>> list() {
        List<SysMenu> menus = menuService.getAllMenus();
        return Result.success(menus);
    }
    
    /**
     * 根据ID查询菜单
     */
    @GetMapping("/{menuId}")
    public Result<SysMenu> getInfo(@PathVariable Long menuId) {
        SysMenu menu = menuService.getMenuById(menuId);
        return Result.success(menu);
    }
    
    /**
     * 新增菜单
     */
    @PostMapping
    public Result<Void> add(@RequestBody SysMenu menu) {
        boolean success = menuService.createMenu(menu);
        return success ? Result.success() : Result.error("新增菜单失败");
    }
    
    /**
     * 修改菜单
     */
    @PutMapping
    public Result<Void> edit(@RequestBody SysMenu menu) {
        boolean success = menuService.updateMenu(menu);
        return success ? Result.success() : Result.error("修改菜单失败");
    }
    
    /**
     * 删除菜单
     */
    @DeleteMapping("/{menuId}")
    public Result<Void> remove(@PathVariable Long menuId) {
        boolean success = menuService.deleteMenu(menuId);
        return success ? Result.success() : Result.error("删除菜单失败");
    }
    
    /**
     * 获取菜单树（用于菜单管理页面）
     */
    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> getMenuTree() {
        List<SysMenu> menus = menuService.getAllMenus();
        List<Map<String, Object>> menuTree = menuService.buildMenuTree(menus);
        return Result.success(menuTree);
    }
}
