package com.bankshield.encrypt.entity;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 密钥轮换监控实体
 * 用于展示密钥轮换的实时监控数据
 */
@Data
public class KeyRotationMonitor {
    
    /**
     * 处于预热期的密钥数量
     */
    private Long keysInWarmUp;
    
    /**
     * 处于双密钥活跃期的密钥数量
     */
    private Long keysInDualActive;
    
    /**
     * 处于仅解密期的密钥数量
     */
    private Long keysInDecryptOnly;
    
    /**
     * 即将过期的密钥数量（30天内）
     */
    private Long keysExpiringSoon;
    
    /**
     * 总的活跃密钥数量
     */
    private Long totalActiveKeys;
    
    /**
     * 轮换健康分数（0-100）
     */
    private Double rotationHealthScore;
    
    /**
     * 最近的轮换历史记录
     */
    private List<Map<String, Object>> recentRotations;
    
    /**
     * 监控时间戳
     */
    private java.time.LocalDateTime monitorTime;
    
    /**
     * 构造函数
     */
    public KeyRotationMonitor() {
        this.monitorTime = java.time.LocalDateTime.now();
    }
    
    /**
     * 获取健康状态描述
     */
    public String getHealthStatusDescription() {
        if (rotationHealthScore == null) {
            return "未知";
        }
        
        if (rotationHealthScore >= 90) {
            return "优秀";
        } else if (rotationHealthScore >= 80) {
            return "良好";
        } else if (rotationHealthScore >= 70) {
            return "一般";
        } else if (rotationHealthScore >= 60) {
            return "警告";
        } else {
            return "严重";
        }
    }
    
    /**
     * 获取健康状态颜色
     */
    public String getHealthStatusColor() {
        if (rotationHealthScore == null) {
            return "gray";
        }
        
        if (rotationHealthScore >= 90) {
            return "green";
        } else if (rotationHealthScore >= 80) {
            return "blue";
        } else if (rotationHealthScore >= 70) {
            return "yellow";
        } else if (rotationHealthScore >= 60) {
            return "orange";
        } else {
            return "red";
        }
    }
    
    /**
     * 是否需要关注
     */
    public boolean needsAttention() {
        return (keysExpiringSoon != null && keysExpiringSoon > 0) ||
               (rotationHealthScore != null && rotationHealthScore < 70);
    }
    
    /**
     * 获取关注级别
     */
    public String getAttentionLevel() {
        if (!needsAttention()) {
            return "正常";
        }
        
        if (keysExpiringSoon != null && keysExpiringSoon > 5) {
            return "高";
        } else if (rotationHealthScore != null && rotationHealthScore < 60) {
            return "高";
        } else if (keysExpiringSoon != null && keysExpiringSoon > 0) {
            return "中";
        } else {
            return "低";
        }
    }
}