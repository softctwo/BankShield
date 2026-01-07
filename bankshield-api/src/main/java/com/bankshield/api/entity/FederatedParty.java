package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 联邦学习参与方实体
 */
@Data
@TableName("federated_party")
public class FederatedParty {

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
     * 参与方角色（COORDINATOR/PARTICIPANT）
     */
    private String role;

    /**
     * 连接地址
     */
    private String endpoint;

    /**
     * 公钥（用于安全聚合）
     */
    private String publicKey;

    /**
     * 状态（ONLINE/OFFLINE/BUSY/DISABLED）
     */
    private String status;

    /**
     * 数据规模（样本数）
     */
    private Long dataSize;

    /**
     * 计算能力评分（1-10）
     */
    private Integer computeCapacity;

    /**
     * 网络带宽（Mbps）
     */
    private Integer bandwidth;

    /**
     * 参与任务数
     */
    private Long jobCount;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeat;

    /**
     * 描述
     */
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
