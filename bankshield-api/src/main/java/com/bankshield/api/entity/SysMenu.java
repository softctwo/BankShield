package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 菜单实体（映射实际数据库表结构）
 */
@Data
@TableName("sys_menu")
public class SysMenu implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 菜单ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 父菜单ID
     */
    private Long parentId;
    
    /**
     * 菜单名称
     */
    private String menuName;
    
    /**
     * 菜单编码
     */
    private String menuCode;
    
    /**
     * 路由地址
     */
    private String path;
    
    /**
     * 组件路径
     */
    private String component;
    
    /**
     * 菜单图标
     */
    private String icon;
    
    /**
     * 显示顺序
     */
    private Integer sortOrder;
    
    /**
     * 菜单类型（0=目录 1=菜单 2=按钮）
     */
    private Integer menuType;
    
    /**
     * 权限标识
     */
    private String permission;
    
    /**
     * 菜单状态（0=禁用 1=启用）
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
