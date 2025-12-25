package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 安全扫描任务日志实体
 * @author BankShield
 */
@Data
@TableName("security_scan_log")
public class SecurityScanLog {
    
    /**
     * 日志ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 任务ID
     */
    @TableField("task_id")
    private Long taskId;
    
    /**
     * 日志级别
     */
    @TableField("log_level")
    private String logLevel;
    
    /**
     * 日志内容
     */
    @TableField("message")
    private String message;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 日志级别枚举
     */
    public enum LogLevel {
        INFO("INFO"),
        WARN("WARN"),
        ERROR("ERROR"),
        DEBUG("DEBUG");
        
        private final String value;
        
        LogLevel(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
}