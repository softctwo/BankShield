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
 * 角色违规记录实体类
 * 对应数据库表：role_violation
 * 记录三权分立机制下的角色违规情况
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("role_violation")
public class RoleViolation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户名
     */
    private String username;

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
     * 违规类型: 1-手动分配, 2-系统检测
     */
    @TableField("violation_type")
    private Integer violationType;

    /**
     * 违规时间
     */
    @TableField("violation_time")
    private LocalDateTime violationTime;

    /**
     * 操作人ID
     */
    @TableField("operator_id")
    private Long operatorId;

    /**
     * 操作人姓名
     */
    @TableField("operator_name")
    private String operatorName;

    /**
     * 处理状态: 0-未处理, 1-已处理, 2-已忽略
     */
    private Integer status;

    /**
     * 处理时间
     */
    @TableField("handle_time")
    private LocalDateTime handleTime;

    /**
     * 处理备注
     */
    @TableField("handle_remark")
    private String handleRemark;

    /**
     * 是否已发送告警: 0-未发送, 1-已发送
     */
    @TableField("alert_sent")
    private Integer alertSent;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}