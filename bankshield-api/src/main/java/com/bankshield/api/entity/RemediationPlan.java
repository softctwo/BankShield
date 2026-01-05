package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("remediation_plan")
public class RemediationPlan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String planName;
    private String vulnerabilityIds;
    private String planType; // MANUAL/AUTO/SCHEDULED
    private String priority; // CRITICAL/HIGH/MEDIUM/LOW
    private String planStatus; // DRAFT/APPROVED/IN_PROGRESS/COMPLETED/CANCELLED
    private String description;
    private String remediationSteps;
    private String estimatedEffort;
    private LocalDate scheduledDate;
    private LocalDate deadline;
    private String assignedTo;
    private String approvedBy;
    private LocalDateTime approvedTime;
    private LocalDateTime completedTime;
    private String completionNotes;
    private String createdBy;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
