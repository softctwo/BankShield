package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单Mapper
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    
    /**
     * 根据用户ID查询菜单
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "LEFT JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "LEFT JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.status = 1 " +
            "ORDER BY m.parent_id, m.sort_order")
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);
    
    /**
     * 根据角色ID查询菜单
     *
     * @param roleId 角色ID
     * @return 菜单列表
     */
    @Select("SELECT m.* FROM sys_menu m " +
            "LEFT JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "WHERE rm.role_id = #{roleId} AND m.status = 1 " +
            "ORDER BY m.parent_id, m.sort_order")
    List<SysMenu> selectMenusByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据用户ID查询权限标识
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    @Select("SELECT DISTINCT m.permission FROM sys_menu m " +
            "LEFT JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "LEFT JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.status = 1 AND m.permission IS NOT NULL AND m.permission != ''")
    List<String> selectPermsByUserId(@Param("userId") Long userId);
}
