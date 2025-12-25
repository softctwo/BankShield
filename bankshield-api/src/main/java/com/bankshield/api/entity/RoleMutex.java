package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色互斥规则实体类
 * 对应数据库表：role_mutex
 * 用于实现三权分立机制，确保等保三级合规要求
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("role_mutex")
public class RoleMutex implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色编码
     */
    @TableField("role_code")
    private String roleCode;

    /**
     * 互斥角色编码
     */
    @TableField("mutex_role_code")
    private String mutexRoleCode;

    /**
     * 互斥规则描述
     */
    private String description;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    @TableField("create_by")
    private String createBy;
}