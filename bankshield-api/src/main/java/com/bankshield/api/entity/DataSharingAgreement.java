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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 数据共享协议实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("data_sharing_agreement")
public class DataSharingAgreement implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("agreement_code")
    private String agreementCode;

    @TableField("agreement_name")
    private String agreementName;

    @TableField("provider_institution_id")
    private Long providerInstitutionId;

    @TableField("consumer_institution_id")
    private Long consumerInstitutionId;

    @TableField("agreement_type")
    private String agreementType;

    @TableField("data_scope")
    private String dataScope;

    @TableField("sharing_purpose")
    private String sharingPurpose;

    @TableField("validity_start_date")
    private LocalDate validityStartDate;

    @TableField("validity_end_date")
    private LocalDate validityEndDate;

    @TableField("status")
    private String status;

    @TableField("approval_status")
    private String approvalStatus;

    @TableField("approver")
    private String approver;

    @TableField("approval_time")
    private LocalDateTime approvalTime;

    @TableField("approval_comment")
    private String approvalComment;

    @TableField("terms_and_conditions")
    private String termsAndConditions;

    @TableField("data_security_level")
    private String dataSecurityLevel;

    @TableField("encryption_required")
    private Boolean encryptionRequired;

    @TableField("audit_required")
    private Boolean auditRequired;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("create_by")
    private String createBy;

    @TableField("update_by")
    private String updateBy;

    @TableField("remark")
    private String remark;
}
