package com.bankshield.api.mapper;

import com.bankshield.api.entity.AuditBlock;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计区块Mapper接口
 * 
 * @author BankShield
 */
@Mapper
public interface AuditBlockMapper extends BaseMapper<AuditBlock> {
    
    /**
     * 查询最新的区块
     */
    @Select("SELECT * FROM audit_block ORDER BY block_number DESC LIMIT 1")
    AuditBlock selectLastBlock();
    
    /**
     * 查询指定时间段内的区块
     */
    @Select("SELECT * FROM audit_block WHERE block_time BETWEEN #{startTime} AND #{endTime} ORDER BY block_number")
    List<AuditBlock> selectBlocksByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                            @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计已上链的审计日志数量
     */
    @Select("SELECT COUNT(*) FROM audit_operation WHERE block_id IS NOT NULL")
    Long countAnchoredAudits();
    
    /**
     * 统计总审计日志数量
     */
    @Select("SELECT COUNT(*) FROM audit_operation")
    Long countTotalAudits();
}