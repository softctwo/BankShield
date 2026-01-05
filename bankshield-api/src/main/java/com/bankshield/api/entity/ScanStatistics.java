package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("scan_statistics")
public class ScanStatistics {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate statDate;
    private Integer scanCount;
    private Integer totalVulnerabilities;
    private Integer criticalCount;
    private Integer highCount;
    private Integer mediumCount;
    private Integer lowCount;
    private Integer resolvedCount;
    private Integer openCount;
    private Integer falsePositiveCount;
    private Integer avgResolutionTime;
    private LocalDateTime createdTime;
}
