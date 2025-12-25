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
 * 操作审计实体类
 * 对应数据库表：audit_operation
 * 
 * @author BankShield
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("audit_operation")
public class OperationAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ExcelProperty("操作ID")
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
     * 操作类型
     */
    @TableField("operation_type")
    @ExcelProperty("操作类型")
    private String operationType;

    /**
     * 操作模块
     */
    @TableField("operation_module")
    @ExcelProperty("操作模块")
    private String operationModule;

    /**
     * 操作内容
     */
    @TableField("operation_content")
    @ExcelProperty("操作内容")
    private String operationContent;

    /**
     * 请求URL
     */
    @TableField("request_url")
    @ExcelProperty("请求URL")
    private String requestUrl;

    /**
     * 请求方法
     */
    @TableField("request_method")
    @ExcelProperty("请求方法")
    private String requestMethod;

    /**
     * 请求参数
     */
    @TableField("request_params")
    private String requestParams;

    /**
     * 响应结果
     */
    @TableField("response_result")
    private String responseResult;

    /**
     * IP地址
     */
    @TableField("ip_address")
    @ExcelProperty("IP地址")
    private String ipAddress;

    /**
     * 操作地点
     */
    @ExcelProperty("操作地点")
    private String location;

    /**
     * 操作结果: 0-失败, 1-成功
     */
    @ExcelProperty("操作结果")
    private Integer status;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 操作时间
     */
    @TableField("create_time")
    @ExcelProperty("操作时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 关联的区块ID（用于完整性校验）
     */
    @TableField("block_id")
    private Long blockId;
}