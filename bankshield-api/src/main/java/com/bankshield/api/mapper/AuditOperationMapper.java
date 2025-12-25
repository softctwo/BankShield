package com.bankshield.api.mapper;

import com.bankshield.api.entity.AuditOperation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 操作审计Mapper接口（别名）
 * 为了兼容性而保留，实际上与 OperationAuditMapper 相同
 *
 * @author BankShield
 * @version 1.0.0
 * @deprecated 请使用 {@link OperationAuditMapper} 代替
 */
@Mapper
@Deprecated
public interface AuditOperationMapper extends BaseMapper<AuditOperation> {

    /**
     * 查询未上链的审计日志（按时间升序）
     */
    @Select("SELECT * FROM audit_operation WHERE block_id IS NULL ORDER BY create_time ASC LIMIT #{limit}")
    List<AuditOperation> selectPendingAudits(@Param("limit") int limit);

    /**
     * 根据用户ID查询审计日志
     *
     * @param userId 用户ID
     * @return 审计日志列表
     */
    @Select("SELECT * FROM audit_operation WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<AuditOperation> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据操作类型查询审计日志
     *
     * @param operationType 操作类型
     * @return 审计日志列表
     */
    @Select("SELECT * FROM audit_operation WHERE operation_type = #{operationType} ORDER BY create_time DESC")
    List<AuditOperation> selectByOperationType(@Param("operationType") String operationType);

    /**
     * 根据时间范围查询审计日志
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 审计日志列表
     */
    List<AuditOperation> selectByTimeRange(@Param("startTime") String startTime, @Param("endTime") String endTime);
}
