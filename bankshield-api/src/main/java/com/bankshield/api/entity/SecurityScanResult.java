package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 安全扫描结果实体类
 * @author BankShield
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("security_scan_result")
public class SecurityScanResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 结果ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 风险级别：CRITICAL/HIGH/MEDIUM/LOW/INFO
     */
    private String riskLevel;

    /**
     * 风险类型：SQL_INJECTION/XSS/WEAK_PASSWORD/CONFIG_ERROR/ANOMALY_BEHAVIOR等
     */
    private String riskType;

    /**
     * 风险描述
     */
    private String riskDescription;

    /**
     * 影响范围
     */
    private String impactScope;

    /**
     * 修复建议
     */
    private String remediationAdvice;

    /**
     * 发现时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime discoveredTime;

    /**
     * 修复状态：UNFIXED/RESOLVED/WONT_FIX
     */
    private String fixStatus;

    /**
     * 修复时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fixTime;

    /**
     * 修复人
     */
    private String fixBy;

    /**
     * 验证结果
     */
    private String verifyResult;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 风险详情（JSON格式存储详细信息）
     */
    private String riskDetails;

    /**
     * CVE编号（如果有）
     */
    private String cveId;

    /**
     * CVSS评分
     */
    private Double cvssScore;

    /**
     * 资产信息（IP、端口、服务等）
     */
    private String assetInfo;
}