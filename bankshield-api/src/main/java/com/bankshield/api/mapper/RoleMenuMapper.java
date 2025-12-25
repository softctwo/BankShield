package com.bankshield.api.mapper;

import com.bankshield.api.entity.RoleMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色菜单关联Mapper接口
 *
 * @author BankShield
 */
@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {

    /**
     * 根据角色ID删除菜单关联
     *
     * @param roleId 角色ID
     * @return 删除的记录数
     */
    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据菜单ID删除角色关联
     *
     * @param menuId 菜单ID
     * @return 删除的记录数
     */
    @Delete("DELETE FROM sys_role_menu WHERE menu_id = #{menuId}")
    int deleteByMenuId(@Param("menuId") Long menuId);

    /**
     * 根据角色ID查询菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    @Select("SELECT menu_id FROM sys_role_menu WHERE role_id = #{roleId}")
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据菜单ID查询角色ID列表
     *
     * @param menuId 菜单ID
     * @return 角色ID列表
     */
    @Select("SELECT role_id FROM sys_role_menu WHERE menu_id = #{menuId}")
    List<Long> selectRoleIdsByMenuId(@Param("menuId") Long menuId);

    /**
     * 根据多个角色ID查询菜单ID列表
     *
     * @param roleIds 角色ID列表
     * @return 菜单ID列表
     */
    List<Long> selectMenuIdsByRoleIds(@Param("roleIds") List<Long> roleIds);
}
