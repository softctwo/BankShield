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
 * 操作审计实体类（别名）
 * 为了兼容性而保留，实际上与 OperationAudit 相同
 *
 * @author BankShield
 * @version 1.0.0
 * @deprecated 请使用 {@link OperationAudit} 代替
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("audit_operation")
@Deprecated
public class AuditOperation implements Serializable {

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
     * 操作类型
     */
    @TableField("operation_type")
    private String operationType;

    /**
     * 操作模块
     */
    @TableField("operation_module")
    private String operationModule;

    /**
     * 操作内容
     */
    @TableField("operation_content")
    private String operationContent;

    /**
     * 请求URL
     */
    @TableField("request_url")
    private String requestUrl;

    /**
     * 请求方法
     */
    @TableField("request_method")
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
     * 操作结果: 0-失败, 1-成功
     */
    private Integer status;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * IP地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 操作地点
     */
    private String location;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 执行时间（毫秒）
     */
    @TableField("execution_time")
    private Long executionTime;

    /**
     * 关联的区块ID（用于完整性校验）
     */
    @TableField("block_id")
    private Long blockId;

    /**
     * 从 OperationAudit 转换
     */
    public static AuditOperation from(OperationAudit operationAudit) {
        return AuditOperation.builder()
                .id(operationAudit.getId())
                .userId(operationAudit.getUserId())
                .username(operationAudit.getUsername())
                .operationType(operationAudit.getOperationType())
                .operationModule(operationAudit.getOperationModule())
                .operationContent(operationAudit.getOperationContent())
                .requestUrl(operationAudit.getRequestUrl())
                .requestMethod(operationAudit.getRequestMethod())
                .requestParams(operationAudit.getRequestParams())
                .responseResult(operationAudit.getResponseResult())
                .status(operationAudit.getStatus())
                .errorMessage(operationAudit.getErrorMessage())
                .ipAddress(operationAudit.getIpAddress())
                .location(operationAudit.getLocation())
                .createTime(operationAudit.getCreateTime())
                .blockId(operationAudit.getBlockId())
                .build();
    }

    /**
     * 转换为 OperationAudit
     */
    public OperationAudit toOperationAudit() {
        return OperationAudit.builder()
                .id(this.id)
                .userId(this.userId)
                .username(this.username)
                .operationType(this.operationType)
                .operationModule(this.operationModule)
                .operationContent(this.operationContent)
                .requestUrl(this.requestUrl)
                .requestMethod(this.requestMethod)
                .requestParams(this.requestParams)
                .responseResult(this.responseResult)
                .status(this.status)
                .errorMessage(this.errorMessage)
                .ipAddress(this.ipAddress)
                .location(this.location)
                .createTime(this.createTime)
                .blockId(this.blockId)
                .build();
    }
}
