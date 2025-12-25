package com.bankshield.api.mapper;

import com.bankshield.api.entity.Menu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 菜单数据访问层
 *
 * @author BankShield
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    /**
     * 查询所有菜单
     *
     * @return 所有菜单列表
     */
    List<Menu> selectAllMenus();
}