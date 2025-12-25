package com.bankshield.api.service;

import com.bankshield.api.entity.Menu;
import com.bankshield.api.mapper.MenuMapper;
import com.bankshield.common.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单管理服务类
 * 
 * @author BankShield
 */
@Slf4j
@Service
public class MenuService {

    @Autowired
    private MenuMapper menuMapper;

    /**
     * 获取所有菜单列表
     */
    public Result<List<Menu>> getMenuList() {
        try {
            LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByAsc(Menu::getSortOrder).orderByAsc(Menu::getId);
            List<Menu> menuList = menuMapper.selectList(wrapper);
            return Result.success(menuList);
        } catch (Exception e) {
            log.error("获取菜单列表失败", e);
            return Result.error("获取菜单列表失败");
        }
    }

    /**
     * 获取菜单树结构
     */
    public Result<List<Menu>> getMenuTree() {
        try {
            LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByAsc(Menu::getSortOrder).orderByAsc(Menu::getId);
            List<Menu> menuList = menuMapper.selectList(wrapper);
            
            // 构建树结构
            List<Menu> tree = buildMenuTree(menuList, null);
            return Result.success(tree);
        } catch (Exception e) {
            log.error("获取菜单树结构失败", e);
            return Result.error("获取菜单树结构失败");
        }
    }

    /**
     * 根据ID获取菜单信息
     */
    public Result<Menu> getMenuById(Long id) {
        try {
            Menu menu = menuMapper.selectById(id);
            if (menu == null) {
                return Result.error("菜单不存在");
            }
            return Result.success(menu);
        } catch (Exception e) {
            log.error("获取菜单信息失败，ID: " + id, e);
            return Result.error("获取菜单信息失败");
        }
    }

    /**
     * 获取父级菜单列表（用于选择上级菜单）
     */
    public Result<List<Menu>> getParentMenuList() {
        try {
            LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
            // 只查询目录和菜单类型的数据，排除按钮类型
            wrapper.in(Menu::getMenuType, 0, 1);
            wrapper.orderByAsc(Menu::getSortOrder).orderByAsc(Menu::getId);
            List<Menu> menuList = menuMapper.selectList(wrapper);
            
            // 构建树结构
            List<Menu> tree = buildMenuTree(menuList, null);
            return Result.success(tree);
        } catch (Exception e) {
            log.error("获取父级菜单列表失败", e);
            return Result.error("获取父级菜单列表失败");
        }
    }

    /**
     * 创建菜单
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> createMenu(Menu menu) {
        try {
            // 验证菜单编码是否已存在
            if (checkMenuCodeExists(menu.getMenuCode(), null)) {
                return Result.error("菜单编码已存在");
            }
            
            // 设置创建时间
            menu.setCreateTime(LocalDateTime.now());
            menu.setUpdateTime(LocalDateTime.now());
            
            // 设置默认排序值
            if (menu.getSortOrder() == null) {
                menu.setSortOrder(0);
            }
            
            int result = menuMapper.insert(menu);
            if (result > 0) {
                log.info("创建菜单成功，菜单ID: {}", menu.getId());
                return Result.success(menu.getId());
            } else {
                return Result.error("创建菜单失败");
            }
        } catch (Exception e) {
            log.error("创建菜单失败", e);
            return Result.error("创建菜单失败");
        }
    }

    /**
     * 更新菜单
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateMenu(Menu menu) {
        try {
            // 验证菜单是否存在
            Menu existingMenu = menuMapper.selectById(menu.getId());
            if (existingMenu == null) {
                return Result.error("菜单不存在");
            }
            
            // 验证菜单编码是否已存在（排除当前菜单）
            if (checkMenuCodeExists(menu.getMenuCode(), menu.getId())) {
                return Result.error("菜单编码已存在");
            }
            
            // 设置更新时间
            menu.setUpdateTime(LocalDateTime.now());
            
            int result = menuMapper.updateById(menu);
            if (result > 0) {
                log.info("更新菜单成功，菜单ID: {}", menu.getId());
                return Result.success("更新成功");
            } else {
                return Result.error("更新菜单失败");
            }
        } catch (Exception e) {
            log.error("更新菜单失败，菜单ID: " + menu.getId(), e);
            return Result.error("更新菜单失败");
        }
    }

    /**
     * 删除菜单
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteMenu(Long id) {
        try {
            // 验证菜单是否存在
            Menu menu = menuMapper.selectById(id);
            if (menu == null) {
                return Result.error("菜单不存在");
            }
            
            // 检查是否有子菜单
            LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Menu::getParentId, id);
            Long childCount = menuMapper.selectCount(wrapper);
            if (childCount > 0) {
                return Result.error("该菜单存在子菜单，不能删除");
            }
            
            int result = menuMapper.deleteById(id);
            if (result > 0) {
                log.info("删除菜单成功，菜单ID: {}", id);
                return Result.success("删除成功");
            } else {
                return Result.error("删除菜单失败");
            }
        } catch (Exception e) {
            log.error("删除菜单失败，菜单ID: " + id, e);
            return Result.error("删除菜单失败");
        }
    }

    /**
     * 检查菜单编码是否已存在
     */
    private boolean checkMenuCodeExists(String menuCode, Long excludeId) {
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getMenuCode, menuCode);
        if (excludeId != null) {
            wrapper.ne(Menu::getId, excludeId);
        }
        return menuMapper.selectCount(wrapper) > 0;
    }

    /**
     * 构建菜单树结构
     */
    private List<Menu> buildMenuTree(List<Menu> menuList, Long parentId) {
        List<Menu> tree = new ArrayList<>();
        
        for (Menu menu : menuList) {
            if ((parentId == null && menu.getParentId() == null) || 
                (parentId != null && parentId.equals(menu.getParentId()))) {
                
                // 递归构建子树
                List<Menu> children = buildMenuTree(menuList, menu.getId());
                if (!children.isEmpty()) {
                    menu.setChildren(children);
                }
                tree.add(menu);
            }
        }
        
        return tree;
    }
}