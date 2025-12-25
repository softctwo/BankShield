package com.bankshield.api.mapper;

import com.bankshield.api.entity.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 角色Mapper接口
 *
 * @author BankShield
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 查询所有角色
     *
     * @return 所有角色列表
     */
    List<Role> selectAllRoles();
}