package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.AuditLogBlock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 审计日志区块Mapper
 */
@Mapper
public interface AuditLogBlockMapper extends BaseMapper<AuditLogBlock> {
    
    /**
     * 获取最新区块
     */
    @Select("SELECT * FROM audit_log_block ORDER BY block_index DESC LIMIT 1")
    AuditLogBlock selectLatestBlock();
    
    /**
     * 根据区块索引获取区块
     */
    @Select("SELECT * FROM audit_log_block WHERE block_index = #{blockIndex}")
    AuditLogBlock selectByBlockIndex(Long blockIndex);
    
    /**
     * 获取区块链高度
     */
    @Select("SELECT MAX(block_index) FROM audit_log_block")
    Long selectChainHeight();
    
    /**
     * 统计有效区块数
     */
    @Select("SELECT COUNT(*) FROM audit_log_block WHERE is_valid = 1")
    Long countValidBlocks();
    
    /**
     * 统计无效区块数
     */
    @Select("SELECT COUNT(*) FROM audit_log_block WHERE is_valid = 0")
    Long countInvalidBlocks();
}
