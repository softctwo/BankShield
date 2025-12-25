package com.bankshield.mpc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MPC结果实体类
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Data
@TableName("mpc_result")
public class MpcResult {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 任务ID
     */
    private Long jobId;
    
    /**
     * 结果数据 (JSON格式)
     */
    private String resultData;
    
    /**
     * 结果哈希（防篡改）
     */
    private String resultHash;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}