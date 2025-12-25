package com.bankshield.api.mapper;

import com.bankshield.api.entity.AuditOperationBlock;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

/**
 * 审计日志与区块关联Mapper接口
 * 
 * @author BankShield
 */
@Mapper
public interface AuditOperationBlockMapper extends BaseMapper<AuditOperationBlock> {
    
    /**
     * 查询审计日志的区块关联
     */
    @Select("SELECT * FROM audit_operation_block WHERE audit_id = #{auditId}")
    AuditOperationBlock selectByAuditId(@Param("auditId") Long auditId);
}