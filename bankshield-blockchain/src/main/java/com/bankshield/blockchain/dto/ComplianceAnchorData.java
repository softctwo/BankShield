package com.bankshield.blockchain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 合规检查存证数据传输对象
 * 
 * @author BankShield
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceAnchorData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 检查ID
     */
    private Long checkId;

    /**
     * 合规标准：等保三级、PCI-DSS、ISO27001
     */
    private String standard;

    /**
     * 合规评分
     */
    private Double score;

    /**
     * 通过率
     */
    private Double passRate;

    /**
     * 检查项目总数
     */
    private Integer totalCount;

    /**
     * 通过项目数
     */
    private Integer passCount;

    /**
     * 检查时间
     */
    private LocalDateTime checkTime;

    /**
     * 检查者
     */
    private String checker;

    /**
     * 默克尔根哈希（用于高效验证）
     */
    private String merkleRoot;

    /**
     * 检查项目哈希列表
     */
    private List<String> checkItemHashes;

    /**
     * 存证基础数据
     */
    private AnchorData anchorData;
}