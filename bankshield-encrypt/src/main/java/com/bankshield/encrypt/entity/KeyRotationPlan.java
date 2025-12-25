package com.bankshield.encrypt.entity;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 密钥轮换计划实体
 * 记录密钥轮换的完整时间线和状态
 */
@Data
public class KeyRotationPlan {
    
    /**
     * 旧密钥ID
     */
    private Long oldKeyId;
    
    /**
     * 新密钥ID
     */
    private Long newKeyId;
    
    /**
     * 轮换计划状态
     */
    private String status; // IN_PROGRESS, SUCCESS, FAILED, CANCELLED
    
    /**
     * 轮换原因
     */
    private String rotationReason;
    
    /**
     * 预热期开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime warmUpStartTime;
    
    /**
     * 预热期结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime warmUpEndTime;
    
    /**
     * 预热期持续天数
     */
    private int warmUpDurationDays;
    
    /**
     * 双密钥活跃期开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dualActiveStartTime;
    
    /**
     * 双密钥活跃期结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dualActiveEndTime;
    
    /**
     * 双密钥活跃期持续天数
     */
    private int dualActiveDurationDays;
    
    /**
     * 仅解密期开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime decryptOnlyStartTime;
    
    /**
     * 仅解密期结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime decryptOnlyEndTime;
    
    /**
     * 仅解密期持续天数
     */
    private int decryptOnlyDurationDays;
    
    /**
     * 轮换完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completeTime;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    /**
     * 进度百分比
     */
    private int progressPercentage;
    
    /**
     * 当前阶段
     */
    private String currentPhase;
    
    /**
     * 预计剩余时间（小时）
     */
    private int estimatedRemainingHours;
    
    /**
     * 计算当前进度百分比
     */
    public int calculateProgressPercentage() {
        LocalDateTime now = LocalDateTime.now();
        
        if (status.equals("SUCCESS") || status.equals("FAILED")) {
            return status.equals("SUCCESS") ? 100 : 0;
        }
        
        if (now.isBefore(warmUpEndTime)) {
            // 预热期：0-25%
            long totalMinutes = java.time.Duration.between(warmUpStartTime, warmUpEndTime).toMinutes();
            long elapsedMinutes = java.time.Duration.between(warmUpStartTime, now).toMinutes();
            return (int) ((elapsedMinutes * 25) / totalMinutes);
        } else if (now.isBefore(dualActiveEndTime)) {
            // 双密钥期：25-75%
            long totalMinutes = java.time.Duration.between(dualActiveStartTime, dualActiveEndTime).toMinutes();
            long elapsedMinutes = java.time.Duration.between(dualActiveStartTime, now).toMinutes();
            return 25 + (int) ((elapsedMinutes * 50) / totalMinutes);
        } else if (now.isBefore(decryptOnlyEndTime)) {
            // 仅解密期：75-95%
            long totalMinutes = java.time.Duration.between(decryptOnlyStartTime, decryptOnlyEndTime).toMinutes();
            long elapsedMinutes = java.time.Duration.between(decryptOnlyStartTime, now).toMinutes();
            return 75 + (int) ((elapsedMinutes * 20) / totalMinutes);
        } else {
            // 完成期：95-100%
            return 95;
        }
    }
    
    /**
     * 获取当前阶段描述
     */
    public String getCurrentPhaseDescription() {
        LocalDateTime now = LocalDateTime.now();
        
        if (status.equals("SUCCESS")) {
            return "轮换完成";
        } else if (status.equals("FAILED")) {
            return "轮换失败";
        } else if (now.isBefore(warmUpEndTime)) {
            return "预热期 - 新密钥准备中";
        } else if (now.isBefore(dualActiveEndTime)) {
            return "双密钥期 - 新旧密钥并行运行";
        } else if (now.isBefore(decryptOnlyEndTime)) {
            return "仅解密期 - 旧密钥仅用于解密历史数据";
        } else {
            return "完成期 - 即将结束轮换";
        }
    }
}