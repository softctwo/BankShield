package com.bankshield.mpc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MPC参与方实体类
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Data
@TableName("mpc_party")
public class MpcParty {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 参与方名称
     */
    private String partyName;
    
    /**
     * 参与方类型 (BANK/ENTERPRISE/GOVERNMENT)
     */
    private String partyType;
    
    /**
     * RSA公钥
     */
    private String publicKey;
    
    /**
     * 服务地址
     */
    private String endpoint;
    
    /**
     * 状态 (ONLINE/OFFLINE)
     */
    private String status;
    
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