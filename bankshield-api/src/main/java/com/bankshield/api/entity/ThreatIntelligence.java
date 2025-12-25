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
 * 威胁情报实体类
 * 对应数据库表：threat_intelligence
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("threat_intelligence")
public class ThreatIntelligence implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 情报ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 威胁类型：MALWARE/PHISHING/DDOS/INTRUSION/DATA_LEAK
     */
    @TableField("threat_type")
    private String threatType;

    /**
     * 威胁描述
     */
    @TableField("threat_description")
    private String threatDescription;

    /**
     * 威胁等级：CRITICAL/HIGH/MEDIUM/LOW
     */
    @TableField("threat_level")
    private String threatLevel;

    /**
     * 影响范围
     */
    @TableField("impact_scope")
    private String impactScope;

    /**
     * 发现时间
     */
    @TableField("discover_time")
    private LocalDateTime discoverTime;

    /**
     * 威胁来源
     */
    @TableField("threat_source")
    private String threatSource;

    /**
     * IoC指标（Indicators of Compromise）
     */
    @TableField("ioc_indicators")
    private String iocIndicators;

    /**
     * 相关CVE编号
     */
    @TableField("cve_codes")
    private String cveCodes;

    /**
     * 参考链接
     */
    @TableField("reference_urls")
    private String referenceUrls;

    /**
     * 处理建议
     */
    @TableField("recommendations")
    private String recommendations;

    /**
     * 可信度：HIGH/MEDIUM/LOW
     */
    @TableField("confidence_level")
    private String confidenceLevel;

    /**
     * 状态：ACTIVE/RESOLVED/EXPIRED
     */
    @TableField("status")
    private String status;

    /**
     * 标签
     */
    @TableField("tags")
    private String tags;

    /**
     * 地理位置
     */
    @TableField("geo_location")
    private String geoLocation;

    /**
     * 目标行业
     */
    @TableField("target_industry")
    private String targetIndustry;

    /**
     * 攻击手法
     */
    @TableField("attack_techniques")
    private String attackTechniques;
}