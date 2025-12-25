package com.bankshield.mpc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MPC任务实体类
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Data
@TableName("mpc_job")
public class MpcJob {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 任务类型 (PSI/JOINT_QUERY/SECURE_SUM)
     */
    private String jobType;
    
    /**
     * 任务状态 (PENDING/RUNNING/SUCCESS/FAILED)
     */
    private String jobStatus;
    
    /**
     * 参与方ID列表 (JSON格式)
     */
    private String partyIds;
    
    /**
     * 任务参数 (JSON格式)
     */
    private String parameters;
    
    /**
     * 结果摘要
     */
    private String result;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}