package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录审计实体类
 * 对应数据库表：audit_login
 * 
 * @author BankShield
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("audit_login")
public class LoginAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ExcelProperty("登录ID")
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    @ExcelProperty("用户ID")
    private Long userId;

    /**
     * 用户名
     */
    @ExcelProperty("用户名")
    private String username;

    /**
     * 登录时间
     */
    @TableField("login_time")
    @ExcelProperty("登录时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;

    /**
     * 登录IP
     */
    @TableField("login_ip")
    @ExcelProperty("登录IP")
    private String loginIp;

    /**
     * 登录地点
     */
    @TableField("login_location")
    @ExcelProperty("登录地点")
    private String loginLocation;

    /**
     * 浏览器
     */
    @ExcelProperty("浏览器")
    private String browser;

    /**
     * 操作系统
     */
    @ExcelProperty("操作系统")
    private String os;

    /**
     * 登录结果: 0-失败, 1-成功
     */
    @ExcelProperty("登录结果")
    private Integer status;

    /**
     * 失败原因
     */
    @TableField("failure_reason")
    @ExcelProperty("失败原因")
    private String failureReason;

    /**
     * 退出时间
     */
    @TableField("logout_time")
    @ExcelProperty("退出时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime logoutTime;

    /**
     * 会话时长(秒)
     */
    @TableField("session_duration")
    @ExcelProperty("会话时长(秒)")
    private Long sessionDuration;
}