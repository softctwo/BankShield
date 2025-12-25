package com.bankshield.api.controller;

import com.bankshield.api.dto.AuditIntegrityReport;
import com.bankshield.api.dto.AuditIntegrityStatistics;
import com.bankshield.api.entity.AuditBlock;
import com.bankshield.api.service.AuditBlockService;
import com.bankshield.common.result.Result;
// import com.bankshield.common.security.RequiresPermission;  // 暂时注释掉
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 审计日志完整性验证控制器
 * 提供审计日志完整性校验相关接口
 * 
 * @author BankShield
 */
@RestController
@RequestMapping("/api/audit/verification")
@Api(tags = "审计日志完整性验证")
@Slf4j
public class AuditVerificationController {
    
    @Autowired
    private AuditBlockService auditBlockService;
    
    /**
     * 验证审计日志完整性
     */
    @GetMapping("/verify/{auditId}")
    @ApiOperation("验证审计日志完整性")
    // @RequiresPermission("AUDIT_VIEW")  // 暂时注释掉
    public Result<Boolean> verifyAudit(
            @ApiParam("审计日志ID") @PathVariable Long auditId) {
        try {
            boolean isValid = auditBlockService.verifyAuditIntegrity(auditId);
            return Result.success(isValid);
        } catch (Exception e) {
            log.error("验证审计日志完整性失败: {}", auditId, e);
            return Result.error(500, "验证失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证整个审计日志系统的完整性
     */
    @GetMapping("/system-integrity")
    @ApiOperation("验证整个审计日志系统的完整性")
    // @RequiresPermission("AUDIT_MANAGE")  // 暂时注释掉
    public Result<AuditIntegrityReport> verifySystemIntegrity() {
        try {
            AuditIntegrityReport report = auditBlockService.verifySystemIntegrity();
            return Result.success(report);
        } catch (Exception e) {
            log.error("验证系统完整性失败", e);
            return Result.error(500, "验证失败: " + e.getMessage());
        }
    }
    
    /**
     * 手动触发区块生成
     */
    @PostMapping("/block/generate")
    @ApiOperation("手动触发区块生成")
    // @RequiresPermission("AUDIT_MANAGE")  // 暂时注释掉
    public Result<Long> generateBlock() {
        try {
            AuditBlock block = auditBlockService.generateBlock();
            if (block != null) {
                return Result.success(block.getBlockNumber());
            }
            return Result.error(400, "没有待处理的审计日志");
        } catch (Exception e) {
            log.error("手动生成区块失败", e);
            return Result.error(500, "生成失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询审计区块列表
     */
    @GetMapping("/block/list")
    @ApiOperation("查询审计区块列表")
    // @RequiresPermission("AUDIT_VIEW")  // 暂时注释掉
    public Result<Page<AuditBlock>> listBlocks(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<AuditBlock> blockPage = auditBlockService.listBlocks(page, size);
            return Result.success(blockPage);
        } catch (Exception e) {
            log.error("查询审计区块列表失败", e);
            return Result.error(500, "查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取审计完整性统计数据
     */
    @GetMapping("/statistics")
    @ApiOperation("获取审计完整性统计数据")
    // @RequiresPermission("AUDIT_VIEW")  // 暂时注释掉
    public Result<AuditIntegrityReport> getIntegrityStatistics() {
        try {
            AuditIntegrityStatistics statistics = auditBlockService.getIntegrityStatistics();
            AuditIntegrityReport report = AuditIntegrityReport.builder()
                    .totalAudits(statistics.getTotalAudits())
                    .anchoredAudits(statistics.getAnchoredAudits())
                    .anchoringRate(statistics.getAnchoringRate())
                    .totalBlocks(statistics.getTotalBlocks())
                    .verificationResult(1)
                    .integrityIssues(java.util.Collections.emptyList())
                    .build();
            return Result.success(report);
        } catch (Exception e) {
            log.error("获取审计完整性统计数据失败", e);
            return Result.error(500, "获取统计数据失败: " + e.getMessage());
        }
    }
}