package com.bankshield.api.mapper;

import com.bankshield.api.entity.SecurityScanLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 安全扫描任务日志Mapper
 * @author BankShield
 */
@Mapper
public interface SecurityScanLogMapper extends BaseMapper<SecurityScanLog> {
    
    /**
     * 根据任务ID查询日志列表
     * @param taskId 任务ID
     * @return 日志列表
     */
    List<SecurityScanLog> selectByTaskId(@Param("taskId") Long taskId);
    
    /**
     * 根据任务ID删除日志
     * @param taskId 任务ID
     * @return 删除的行数
     */
    int deleteByTaskId(@Param("taskId") Long taskId);
    
    /**
     * 批量插入日志
     * @param logs 日志列表
     * @return 插入的行数
     */
    int batchInsert(@Param("logs") List<SecurityScanLog> logs);
}