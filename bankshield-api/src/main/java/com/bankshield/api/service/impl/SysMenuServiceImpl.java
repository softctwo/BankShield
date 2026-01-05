package com.bankshield.api.service.impl;

import com.bankshield.api.entity.SysMenu;
import com.bankshield.api.mapper.SysMenuMapper;
import com.bankshield.api.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl implements SysMenuService {
    
    private final SysMenuMapper menuMapper;
    
    @Override
    public List<Map<String, Object>> getMenuTreeByUserId(Long userId) {
        log.info("查询用户菜单树: userId={}", userId);
        
        // 查询用户菜单
        List<SysMenu> menus = menuMapper.selectMenusByUserId(userId);
        
        // 构建菜单树
        return buildMenuTree(menus);
    }
    
    @Override
    public List<String> getPermsByUserId(Long userId) {
        log.info("查询用户权限: userId={}", userId);
        return menuMapper.selectPermsByUserId(userId);
    }
    
    @Override
    public List<SysMenu> getAllMenus() {
        return menuMapper.selectList(null);
    }
    
    @Override
    public SysMenu getMenuById(Long menuId) {
        return menuMapper.selectById(menuId);
    }
    
    @Override
    public boolean createMenu(SysMenu menu) {
        return menuMapper.insert(menu) > 0;
    }
    
    @Override
    public boolean updateMenu(SysMenu menu) {
        return menuMapper.updateById(menu) > 0;
    }
    
    @Override
    public boolean deleteMenu(Long menuId) {
        return menuMapper.deleteById(menuId) > 0;
    }
    
    @Override
    public List<Map<String, Object>> buildMenuTree(List<SysMenu> menus) {
        // 获取所有父菜单（一级菜单）
        List<SysMenu> rootMenus = menus.stream()
                .filter(menu -> menu.getParentId() == 0)
                .sorted(Comparator.comparing(SysMenu::getSortOrder))
                .collect(Collectors.toList());
        
        // 构建菜单树
        List<Map<String, Object>> menuTree = new ArrayList<>();
        for (SysMenu rootMenu : rootMenus) {
            Map<String, Object> menuNode = convertToMenuNode(rootMenu);
            List<Map<String, Object>> children = buildChildren(rootMenu.getId(), menus);
            if (!children.isEmpty()) {
                menuNode.put("children", children);
            }
            menuTree.add(menuNode);
        }
        
        return menuTree;
    }
    
    /**
     * 递归构建子菜单
     */
    private List<Map<String, Object>> buildChildren(Long parentId, List<SysMenu> allMenus) {
        List<SysMenu> children = allMenus.stream()
                .filter(menu -> parentId.equals(menu.getParentId()))
                .sorted(Comparator.comparing(SysMenu::getSortOrder))
                .collect(Collectors.toList());
        
        List<Map<String, Object>> childrenNodes = new ArrayList<>();
        for (SysMenu child : children) {
            Map<String, Object> childNode = convertToMenuNode(child);
            List<Map<String, Object>> grandChildren = buildChildren(child.getId(), allMenus);
            if (!grandChildren.isEmpty()) {
                childNode.put("children", grandChildren);
            }
            childrenNodes.add(childNode);
        }
        
        return childrenNodes;
    }
    
    /**
     * 转换为菜单节点
     */
    private Map<String, Object> convertToMenuNode(SysMenu menu) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", menu.getId());
        node.put("name", menu.getMenuName());
        node.put("path", menu.getPath());
        node.put("component", menu.getComponent());
        node.put("icon", menu.getIcon());
        node.put("sortOrder", menu.getSortOrder());
        node.put("menuType", menu.getMenuType());
        node.put("permission", menu.getPermission());
        
        // 路由元信息
        Map<String, Object> meta = new HashMap<>();
        meta.put("title", menu.getMenuName());
        meta.put("icon", menu.getIcon());
        meta.put("noCache", false);
        node.put("meta", meta);
        
        return node;
    }
}
