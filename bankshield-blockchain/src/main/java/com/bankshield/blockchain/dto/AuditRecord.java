package com.bankshield.blockchain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 审计记录数据传输对象
 *
 * @author BankShield
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    private String recordID;

    /**
     * 区块ID
     */
    private String blockId;

    /**
     * 交易哈希
     */
    private String txHash;

    /**
     * 操作类型：CREATE-创建, UPDATE-更新, DELETE-删除, QUERY-查询
     */
    private String operationType;

    /**
     * 资源类型：TABLE-表, COLUMN-字段, VIEW-视图, PROCEDURE-存储过程
     */
    private String resourceType;

    /**
     * 资源名称
     */
    private String resourceName;

    /**
     * 资源ID
     */
    private String resourceId;

    /**
     * 操作人ID
     */
    private String operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 操作人IP
     */
    private String operatorIp;

    /**
     * 操作时间
     */
    private LocalDateTime operationTime;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 操作前的数据快照
     */
    private String beforeData;

    /**
     * 操作后的数据快照
     */
    private String afterData;

    /**
     * 数据哈希（用于完整性校验）
     */
    private String dataHash;

    /**
     * 记录状态：PENDING-待确认, CONFIRMED-已确认, REJECTED-已拒绝
     */
    private String status;

    /**
     * 记录版本号
     */
    private Long version;

    /**
     * 父记录ID（用于追踪变更历史）
     */
    private String parentRecordId;

    /**
     * 关联的业务ID
     */
    private String businessId;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 风险等级：LOW-低, MEDIUM-中, HIGH-高, CRITICAL-严重
     */
    private String riskLevel;

    /**
     * 额外参数
     */
    private Map<String, Object> extraData;

    /**
     * 签名信息
     */
    private String signature;

    /**
     * 签名算法
     */
    private String signatureAlgorithm;

    /**
     * 记录大小（字节）
     */
    private Long recordSize;

    /**
     * 是否为敏感操作
     */
    private Boolean sensitiveOperation;

    /**
     * 审批状态：PENDING-待审批, APPROVED-已审批, REJECTED-已拒绝
     */
    private String approvalStatus;

    /**
     * 审批人ID
     */
    private String approverId;

    /**
     * 审批时间
     */
    private LocalDateTime approvalTime;

    /**
     * 审批意见
     */
    private String approvalComment;
}
