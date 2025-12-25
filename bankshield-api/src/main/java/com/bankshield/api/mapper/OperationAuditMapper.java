package com.bankshield.api.mapper;

import com.bankshield.api.entity.OperationAudit;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作审计Mapper接口
 * 
 * @author BankShield
 */
@Mapper
public interface OperationAuditMapper extends BaseMapper<OperationAudit> {
    
    /**
     * 查询未上链的审计日志（按时间升序）
     */
    @Select("SELECT * FROM audit_operation WHERE block_id IS NULL ORDER BY create_time ASC LIMIT #{limit}")
    List<OperationAudit> selectPendingAudits(@Param("limit") int limit);
}