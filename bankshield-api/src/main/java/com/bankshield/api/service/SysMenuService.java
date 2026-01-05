package com.bankshield.api.service;

import com.bankshield.api.entity.SysMenu;

import java.util.List;
import java.util.Map;

/**
 * 菜单服务接口
 */
public interface SysMenuService {
    
    /**
     * 根据用户ID查询菜单树
     *
     * @param userId 用户ID
     * @return 菜单树
     */
    List<Map<String, Object>> getMenuTreeByUserId(Long userId);
    
    /**
     * 根据用户ID查询权限标识
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    List<String> getPermsByUserId(Long userId);
    
    /**
     * 查询所有菜单
     *
     * @return 菜单列表
     */
    List<SysMenu> getAllMenus();
    
    /**
     * 根据ID查询菜单
     *
     * @param menuId 菜单ID
     * @return 菜单
     */
    SysMenu getMenuById(Long menuId);
    
    /**
     * 新增菜单
     *
     * @param menu 菜单
     * @return 是否成功
     */
    boolean createMenu(SysMenu menu);
    
    /**
     * 修改菜单
     *
     * @param menu 菜单
     * @return 是否成功
     */
    boolean updateMenu(SysMenu menu);
    
    /**
     * 删除菜单
     *
     * @param menuId 菜单ID
     * @return 是否成功
     */
    boolean deleteMenu(Long menuId);
    
    /**
     * 构建菜单树
     *
     * @param menus 菜单列表
     * @return 菜单树
     */
    List<Map<String, Object>> buildMenuTree(List<SysMenu> menus);
}
