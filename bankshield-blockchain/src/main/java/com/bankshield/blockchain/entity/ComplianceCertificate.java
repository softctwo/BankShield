package com.bankshield.blockchain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 合规检查证书实体类
 * 对应数据库表：blockchain_compliance_certificate
 * 
 * @author BankShield
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("blockchain_compliance_certificate")
public class ComplianceCertificate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 检查ID
     */
    @TableField("check_id")
    private Long checkId;

    /**
     * 证书编号
     */
    @TableField("certificate_no")
    private String certificateNo;

    /**
     * 合规标准：等保三级、PCI-DSS、ISO27001
     */
    @TableField("compliance_standard")
    private String complianceStandard;

    /**
     * 合规评分
     */
    @TableField("compliance_score")
    private Double complianceScore;

    /**
     * 通过率
     */
    @TableField("pass_rate")
    private Double passRate;

    /**
     * 检查项目总数
     */
    @TableField("total_items")
    private Integer totalItems;

    /**
     * 通过项目数
     */
    @TableField("passed_items")
    private Integer passedItems;

    /**
     * 检查时间
     */
    @TableField("check_time")
    private LocalDateTime checkTime;

    /**
     * 检查者
     */
    @TableField("checker")
    private String checker;

    /**
     * 检查者ID
     */
    @TableField("checker_id")
    private Long checkerId;

    /**
     * 关联的存证记录ID
     */
    @TableField("anchor_record_id")
    private String anchorRecordId;

    /**
     * 区块链交易哈希
     */
    @TableField("blockchain_tx_hash")
    private String blockchainTxHash;

    /**
     * 默克尔根哈希
     */
    @TableField("merkle_root")
    private String merkleRoot;

    /**
     * 证书有效期开始时间
     */
    @TableField("valid_from")
    private LocalDateTime validFrom;

    /**
     * 证书有效期结束时间
     */
    @TableField("valid_until")
    private LocalDateTime validUntil;

    /**
     * 证书状态：VALID-有效, EXPIRED-过期, REVOKED-吊销
     */
    @TableField("certificate_status")
    private String certificateStatus;

    /**
     * 证书文件路径
     */
    @TableField("certificate_file")
    private String certificateFile;

    /**
     * 证书PDF文件路径
     */
    @TableField("certificate_pdf")
    private String certificatePdf;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 备注信息
     */
    @TableField("remark")
    private String remark;
}