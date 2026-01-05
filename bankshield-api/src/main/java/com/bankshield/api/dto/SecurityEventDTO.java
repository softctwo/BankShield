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
}
