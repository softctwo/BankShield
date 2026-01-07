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
 * 数据共享请求实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("data_sharing_request")
public class DataSharingRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("request_code")
    private String requestCode;

    @TableField("agreement_id")
    private Long agreementId;

    @TableField("requester_institution_id")
    private Long requesterInstitutionId;

    @TableField("requester_user_id")
    private Long requesterUserId;

    @TableField("requester_user_name")
    private String requesterUserName;

    @TableField("request_type")
    private String requestType;

    @TableField("data_category")
    private String dataCategory;

    @TableField("data_fields")
    private String dataFields;

    @TableField("filter_conditions")
    private String filterConditions;

    @TableField("request_purpose")
    private String requestPurpose;

    @TableField("request_time")
    private LocalDateTime requestTime;

    @TableField("status")
    private String status;

    @TableField("approval_status")
    private String approvalStatus;

    @TableField("approver")
    private String approver;

    @TableField("approval_time")
    private LocalDateTime approvalTime;

    @TableField("approval_comment")
    private String approvalComment;

    @TableField("process_start_time")
    private LocalDateTime processStartTime;

    @TableField("process_end_time")
    private LocalDateTime processEndTime;

    @TableField("result_file_path")
    private String resultFilePath;

    @TableField("result_record_count")
    private Integer resultRecordCount;

    @TableField("error_message")
    private String errorMessage;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("remark")
    private String remark;
}
