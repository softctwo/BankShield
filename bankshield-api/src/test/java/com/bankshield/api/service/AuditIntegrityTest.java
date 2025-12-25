package com.bankshield.api.service;

import com.bankshield.api.dto.AuditIntegrityReport;
import com.bankshield.api.entity.AuditBlock;
import com.bankshield.api.entity.OperationAudit;
import com.bankshield.api.mapper.OperationAuditMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 审计日志完整性测试
 * 
 * @author BankShield
 */
@SpringBootTest
@Transactional
public class AuditIntegrityTest {
    
    @Autowired
    private AuditBlockService auditBlockService;
    
    @Autowired
    private OperationAuditMapper auditMapper;
    
    @Test
    @DisplayName("测试审计日志完整性验证")
    void testAuditIntegrityVerification() {
        // 1. 生成测试审计日志
        OperationAudit audit = new OperationAudit();
        audit.setUserId(1L);
        audit.setUsername("admin");
        audit.setOperationType("TEST");
        audit.setOperationModule("INTEGRITY");
        audit.setRequestUrl("/api/test");
        audit.setRequestMethod("GET");
        audit.setStatus(1);
        auditMapper.insert(audit);
        
        // 2. 生成区块
        AuditBlock block = auditBlockService.generateBlock();
        assertNotNull(block, "应该成功生成区块");
        assertTrue(block.getBlockNumber() > 0, "区块号应该大于0");
        assertTrue(block.getAuditCount() > 0, "区块应该包含审计日志");
        
        // 3. 验证完整性
        boolean isValid = auditBlockService.verifyAuditIntegrity(audit.getId());
        assertTrue(isValid, "审计日志完整性验证应该通过");
        
        // 4. 模拟篡改（修改审计日志后应该验证失败）
        audit.setOperationType("TAMPERED");
        auditMapper.updateById(audit);
        
        boolean isValidAfterTamper = auditBlockService.verifyAuditIntegrity(audit.getId());
        assertFalse(isValidAfterTamper, "篡改后的审计日志验证应该失败");
    }
    
    @Test
    @DisplayName("测试系统完整性验证")
    void testSystemIntegrityVerification() {
        // 1. 生成测试审计日志
        OperationAudit audit1 = createTestAudit("TEST1", "INTEGRITY_TEST");
        OperationAudit audit2 = createTestAudit("TEST2", "INTEGRITY_TEST");
        
        auditMapper.insert(audit1);
        auditMapper.insert(audit2);
        
        // 2. 生成区块
        AuditBlock block = auditBlockService.generateBlock();
        assertNotNull(block);
        
        // 3. 验证系统完整性
        AuditIntegrityReport report = auditBlockService.verifySystemIntegrity();
        assertNotNull(report);
        assertTrue(report.getTotalAudits() >= 2, "总审计日志数量应该至少为2");
        assertTrue(report.getAnchoredAudits() >= 2, "已上链审计日志数量应该至少为2");
        assertEquals(Integer.valueOf(1), report.getVerificationResult(), "系统完整性验证应该通过");
        assertTrue(report.getIntegrityIssues().isEmpty(), "应该没有完整性问题");
    }
    
    @Test
    @DisplayName("测试区块生成逻辑")
    void testBlockGeneration() {
        // 1. 清空现有数据
        auditMapper.delete(null);
        
        // 2. 生成测试审计日志
        for (int i = 0; i < 10; i++) {
            OperationAudit audit = createTestAudit("TEST_" + i, "BLOCK_GENERATION");
            auditMapper.insert(audit);
        }
        
        // 3. 生成区块
        AuditBlock block = auditBlockService.generateBlock();
        assertNotNull(block);
        assertEquals(10L, block.getAuditCount().longValue(), "区块应该包含10条审计日志");
        assertNotNull(block.getCurrentHash(), "区块哈希不应该为空");
        assertNotNull(block.getMerkleRoot(), "Merkle根不应该为空");
        assertNotNull(block.getPreviousHash(), "前一个区块哈希不应该为空");
    }
    
    @Test
    @DisplayName("测试空审计日志情况")
    void testEmptyAuditLogs() {
        // 清空现有数据
        auditMapper.delete(null);
        
        // 生成区块（应该返回null）
        AuditBlock block = auditBlockService.generateBlock();
        assertNull(block, "没有审计日志时应该返回null");
    }
    
    @Test
    @DisplayName("测试未上链审计日志验证")
    void testUnanchoredAuditVerification() {
        // 生成未上链的审计日志
        OperationAudit audit = new OperationAudit();
        audit.setUserId(1L);
        audit.setUsername("admin");
        audit.setOperationType("UNANCHORED");
        audit.setOperationModule("INTEGRITY");
        audit.setRequestUrl("/api/test");
        audit.setRequestMethod("GET");
        audit.setStatus(1);
        audit.setBlockId(null); // 未上链
        auditMapper.insert(audit);
        
        // 验证应该失败
        boolean isValid = auditBlockService.verifyAuditIntegrity(audit.getId());
        assertFalse(isValid, "未上链的审计日志验证应该失败");
    }
    
    /**
     * 创建测试审计日志
     */
    private OperationAudit createTestAudit(String operationType, String module) {
        OperationAudit audit = new OperationAudit();
        audit.setUserId(1L);
        audit.setUsername("test_user");
        audit.setOperationType(operationType);
        audit.setOperationModule(module);
        audit.setRequestUrl("/api/test/" + operationType.toLowerCase());
        audit.setRequestMethod("POST");
        audit.setStatus(1);
        return audit;
    }
}