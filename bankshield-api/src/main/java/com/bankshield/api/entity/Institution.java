package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 机构信息实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("institution")
public class Institution implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("institution_code")
    private String institutionCode;

    @TableField("institution_name")
    private String institutionName;

    @TableField("institution_type")
    private String institutionType;

    @TableField("contact_person")
    private String contactPerson;

    @TableField("contact_phone")
    private String contactPhone;

    @TableField("contact_email")
    private String contactEmail;

    @TableField("address")
    private String address;

    @TableField("status")
    private String status;

    @TableField("trust_level")
    private Integer trustLevel;

    @TableField("public_key")
    private String publicKey;

    @TableField("certificate")
    private String certificate;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("create_by")
    private String createBy;

    @TableField("update_by")
    private String updateBy;

    @TableField("remark")
    private String remark;
}
