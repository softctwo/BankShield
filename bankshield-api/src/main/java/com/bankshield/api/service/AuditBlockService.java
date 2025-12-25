package com.bankshield.api.service;

import com.bankshield.api.dto.AuditIntegrityReport;
import com.bankshield.api.dto.AuditIntegrityStatistics;
import com.bankshield.api.entity.AuditBlock;
import com.bankshield.api.entity.AuditOperationBlock;
import com.bankshield.api.entity.OperationAudit;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 审计区块服务接口
 * 提供审计日志完整性校验功能
 * 
 * @author BankShield
 */
public interface AuditBlockService {
    
    /**
     * 生成新的审计区块
     * 
     * @return 生成的区块，如果没有待处理的审计日志则返回null
     */
    AuditBlock generateBlock();
    
    /**
     * 验证审计日志完整性
     * 
     * @param auditId 审计日志ID
     * @return 验证结果
     */
    boolean verifyAuditIntegrity(Long auditId);
    
    /**
     * 验证整个审计日志系统的完整性
     * 
     * @return 完整性报告
     */
    AuditIntegrityReport verifySystemIntegrity();
    
    /**
     * 分页查询审计区块
     * 
     * @param page 页码
     * @param size 每页大小
     * @return 区块分页结果
     */
    Page<AuditBlock> listBlocks(int page, int size);
    
    /**
     * 获取审计完整性统计数据
     * 
     * @return 统计数据
     */
    AuditIntegrityStatistics getIntegrityStatistics();
}