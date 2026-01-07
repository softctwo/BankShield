package com.bankshield.api.dto;

import lombok.Data;

/**
 * 安全事件DTO
 */
@Data
public class SecurityEventDTO {
    
    private Long id;
    
    private String time;
    
    private String type;
    
    private String source;
    
    private String target;
    
    private String status;
    
    private String level;
    
    /**
     * 事件类型（别名）
     */
    private String eventType;
    
    /**
     * 事件级别（别名）
     */
    private String eventLevel;
    
    /**
     * 事件来源（别名）
     */
    private String eventSource;
    
    /**
     * 事件消息
     */
    private String eventMessage;
    
    /**
     * 事件时间
     */
    private String eventTime;
}
