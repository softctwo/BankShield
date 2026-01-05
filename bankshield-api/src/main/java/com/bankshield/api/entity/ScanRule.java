package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("scan_rule")
public class ScanRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleCode;
    private String ruleName;
    private String ruleType;
    private String severity;
    private String description;
    private String detectionPattern;
    private String ruleConfig; // JSON
    private Boolean enabled;
    private BigDecimal falsePositiveRate;
    private String referenceLinks;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
