package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * MPC参与方实体
 */
@Data
@TableName("mpc_party")
public class MpcParty {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 参与方ID
     */
    private String partyId;

    /**
     * 参与方名称
     */
    private String partyName;

    /**
     * 参与方类型（INTERNAL/EXTERNAL）
     */
    private String partyType;

    /**
     * 连接地址
     */
    private String endpoint;

    /**
     * 公钥
     */
    private String publicKey;

    /**
     * 状态（ONLINE/OFFLINE/DISABLED）
     */
    private String status;

    /**
     * 信任级别（1-5）
     */
    private Integer trustLevel;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeat;

    /**
     * 参与任务数
     */
    private Long jobCount;

    /**
     * 描述
     */
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
