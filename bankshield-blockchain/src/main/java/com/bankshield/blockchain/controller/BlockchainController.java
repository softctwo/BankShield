package com.bankshield.blockchain.controller;

import com.bankshield.blockchain.dto.AnchorData;
import com.bankshield.blockchain.dto.BlockchainTransaction;
import com.bankshield.blockchain.dto.ComplianceAnchorData;
import com.bankshield.blockchain.entity.AnchorRecord;
import com.bankshield.blockchain.entity.ComplianceCertificate;
import com.bankshield.blockchain.service.BlockchainAnchorService;
import com.bankshield.blockchain.service.BlockchainVerificationService;
import com.bankshield.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 区块链存证控制器
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/blockchain")
@Api(tags = "区块链存证模块")
public class BlockchainController {

    @Autowired
    private BlockchainAnchorService anchorService;

    @Autowired
    private BlockchainVerificationService verificationService;

    /**
     * 批量存证
     */
    @PostMapping("/anchor/batch")
    @ApiOperation(value = "批量存证", notes = "批量将数据存证到区块链")
    public Result<List<AnchorRecord>> batchAnchor(@RequestBody List<AnchorData> anchorDataList) {
        try {
            log.info("批量存证请求，数量: {}", anchorDataList.size());
            
            List<AnchorRecord> records = anchorService.batchAnchor(anchorDataList);
            
            log.info("批量存证完成，成功数量: {}", records.size());
            return Result.success(records);
            
        } catch (Exception e) {
            log.error("批量存证失败", e);
            return Result.error("批量存证失败: " + e.getMessage());
        }
    }

    /**
     * 异步存证
     */
    @PostMapping("/anchor/async")
    @ApiOperation(value = "异步存证", notes = "异步将数据存证到区块链")
    public Result<String> anchorAsync(@RequestBody AnchorData data) {
        try {
            log.info("异步存证请求，业务ID: {}", data.getBusinessId());
            
            String taskId = anchorService.anchorAsync(data);
            
            log.info("异步存证任务已创建，任务ID: {}", taskId);
            return Result.success(taskId);
            
        } catch (Exception e) {
            log.error("异步存证失败", e);
            return Result.error("异步存证失败: " + e.getMessage());
        }
    }

    /**
     * 存证合规检查结果
     */
    @PostMapping("/anchor/compliance")
    @ApiOperation(value = "存证合规检查结果", notes = "将合规检查结果存证到区块链")
    public Result<ComplianceCertificate> anchorComplianceCheck(@RequestBody ComplianceAnchorData complianceData) {
        try {
            log.info("存证合规检查结果请求，检查ID: {}", complianceData.getCheckId());
            
            ComplianceCertificate certificate = anchorService.anchorComplianceCheck(complianceData);
            
            log.info("合规检查结果存证完成，证书ID: {}", certificate.getId());
            return Result.success(certificate);
            
        } catch (Exception e) {
            log.error("存证合规检查结果失败", e);
            return Result.error("存证合规检查结果失败: " + e.getMessage());
        }
    }

    /**
     * 验证审计日志完整性
     */
    @GetMapping("/verify/audit/{auditId}")
    @ApiOperation(value = "验证审计日志完整性", notes = "验证指定审计日志的区块链存证完整性")
    public Result<Boolean> verifyAuditLogIntegrity(@PathVariable Long auditId) {
        try {
            log.info("验证审计日志完整性请求，审计ID: {}", auditId);
            
            boolean isValid = anchorService.verifyAuditLogIntegrity(auditId);
            
            log.info("审计日志完整性验证完成，结果: {}", isValid);
            return Result.success(isValid);
            
        } catch (Exception e) {
            log.error("验证审计日志完整性失败", e);
            return Result.error("验证审计日志完整性失败: " + e.getMessage());
        }
    }

    /**
     * 验证密钥事件完整性
     */
    @GetMapping("/verify/key/{keyId}")
    @ApiOperation(value = "验证密钥事件完整性", notes = "验证指定密钥事件的区块链存证完整性")
    public Result<Boolean> verifyKeyEventIntegrity(@PathVariable Long keyId) {
        try {
            log.info("验证密钥事件完整性请求，密钥ID: {}", keyId);
            
            boolean isValid = anchorService.verifyKeyEventIntegrity(keyId);
            
            log.info("密钥事件完整性验证完成，结果: {}", isValid);
            return Result.success(isValid);
            
        } catch (Exception e) {
            log.error("验证密钥事件完整性失败", e);
            return Result.error("验证密钥事件完整性失败: " + e.getMessage());
        }
    }

    /**
     * 验证合规证书完整性
     */
    @GetMapping("/verify/certificate/{certificateId}")
    @ApiOperation(value = "验证合规证书完整性", notes = "验证指定合规证书的区块链存证完整性")
    public Result<Boolean> verifyComplianceCertificate(@PathVariable Long certificateId) {
        try {
            log.info("验证合规证书完整性请求，证书ID: {}", certificateId);
            
            boolean isValid = anchorService.verifyComplianceCertificate(certificateId);
            
            log.info("合规证书完整性验证完成，结果: {}", isValid);
            return Result.success(isValid);
            
        } catch (Exception e) {
            log.error("验证合规证书完整性失败", e);
            return Result.error("验证合规证书完整性失败: " + e.getMessage());
        }
    }

    /**
     * 查询存证记录
     */
    @GetMapping("/record/{id}")
    @ApiOperation(value = "查询存证记录", notes = "根据ID查询存证记录详情")
    public Result<AnchorRecord> getAnchorRecord(@PathVariable Long id) {
        try {
            log.info("查询存证记录请求，记录ID: {}", id);
            
            AnchorRecord record = anchorService.getAnchorRecord(id);
            
            if (record == null) {
                return Result.error("存证记录不存在");
            }
            
            log.info("查询存证记录完成");
            return Result.success(record);
            
        } catch (Exception e) {
            log.error("查询存证记录失败", e);
            return Result.error("查询存证记录失败: " + e.getMessage());
        }
    }

    /**
     * 根据记录ID查询存证记录
     */
    @GetMapping("/record/by-record-id/{recordId}")
    @ApiOperation(value = "根据记录ID查询存证记录", notes = "根据业务记录ID查询存证记录")
    public Result<AnchorRecord> getAnchorRecordByRecordId(@PathVariable String recordId) {
        try {
            log.info("根据记录ID查询存证记录请求，记录ID: {}", recordId);
            
            AnchorRecord record = anchorService.getAnchorRecordByRecordId(recordId);
            
            if (record == null) {
                return Result.error("存证记录不存在");
            }
            
            log.info("查询存证记录完成");
            return Result.success(record);
            
        } catch (Exception e) {
            log.error("根据记录ID查询存证记录失败", e);
            return Result.error("查询存证记录失败: " + e.getMessage());
        }
    }

    /**
     * 根据交易哈希查询存证记录
     */
    @GetMapping("/record/by-tx-hash/{txHash}")
    @ApiOperation(value = "根据交易哈希查询存证记录", notes = "根据区块链交易哈希查询存证记录")
    public Result<AnchorRecord> getAnchorRecordByTxHash(@PathVariable String txHash) {
        try {
            log.info("根据交易哈希查询存证记录请求，交易哈希: {}", txHash);
            
            AnchorRecord record = anchorService.getAnchorRecordByTxHash(txHash);
            
            if (record == null) {
                return Result.error("存证记录不存在");
            }
            
            log.info("查询存证记录完成");
            return Result.success(record);
            
        } catch (Exception e) {
            log.error("根据交易哈希查询存证记录失败", e);
            return Result.error("查询存证记录失败: " + e.getMessage());
        }
    }

    /**
     * 查询存证记录列表
     */
    @GetMapping("/records")
    @ApiOperation(value = "查询存证记录列表", notes = "根据条件查询存证记录列表")
    public Result<List<AnchorRecord>> getAnchorRecords(
            @ApiParam("存证类型") @RequestParam(required = false) String anchorType,
            @ApiParam("业务ID") @RequestParam(required = false) String businessId) {
        try {
            log.info("查询存证记录列表请求，类型: {}, 业务ID: {}", anchorType, businessId);
            
            List<AnchorRecord> records = anchorService.getAnchorRecords(anchorType, businessId);
            
            log.info("查询存证记录列表完成，数量: {}", records.size());
            return Result.success(records);
            
        } catch (Exception e) {
            log.error("查询存证记录列表失败", e);
            return Result.error("查询存证记录列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取区块链网络状态
     */
    @GetMapping("/network/status")
    @ApiOperation(value = "获取区块链网络状态", notes = "获取区块链网络的当前状态信息")
    public Result<Map<String, Object>> getNetworkStatus() {
        try {
            log.info("获取区块链网络状态请求");
            
            com.bankshield.blockchain.client.BlockchainNetworkStatus status = anchorService.getNetworkStatus();
            
            Map<String, Object> result = new HashMap<>();
            result.put("connected", status.isConnected());
            result.put("blockHeight", status.getBlockHeight());
            result.put("peerCount", status.getPeerCount());
            result.put("channelName", status.getChannelName());
            result.put("lastBlockTime", status.getLastBlockTime());
            
            log.info("获取区块链网络状态完成");
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("获取区块链网络状态失败", e);
            return Result.error("获取区块链网络状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取交易详情
     */
    @GetMapping("/transaction/{txHash}")
    @ApiOperation(value = "获取交易详情", notes = "根据交易哈希获取区块链交易详情")
    public Result<BlockchainTransaction> getTransaction(@PathVariable String txHash) {
        try {
            log.info("获取交易详情请求，交易哈希: {}", txHash);
            
            BlockchainTransaction transaction = anchorService.getTransaction(txHash);
            
            if (transaction == null) {
                return Result.error("交易不存在");
            }
            
            log.info("获取交易详情完成");
            return Result.success(transaction);
            
        } catch (Exception e) {
            log.error("获取交易详情失败", e);
            return Result.error("获取交易详情失败: " + e.getMessage());
        }
    }

    /**
     * 生成存证证书
     */
    @GetMapping("/certificate/generate/{recordId}")
    @ApiOperation(value = "生成存证证书", notes = "为指定存证记录生成证书文件")
    public Result<String> generateCertificate(@PathVariable String recordId) {
        try {
            log.info("生成存证证书请求，记录ID: {}", recordId);
            
            String certificatePath = anchorService.generateCertificate(recordId);
            
            log.info("存证证书生成完成，路径: {}", certificatePath);
            return Result.success(certificatePath);
            
        } catch (Exception e) {
            log.error("生成存证证书失败", e);
            return Result.error("生成存证证书失败: " + e.getMessage());
        }
    }

    /**
     * 验证审计区块完整性
     */
    @GetMapping("/verify/block/{blockId}")
    @ApiOperation(value = "验证审计区块完整性", notes = "验证指定区块的完整性")
    public Result<BlockchainVerificationService.VerificationResult> verifyAuditBlock(@PathVariable String blockId) {
        try {
            log.info("验证审计区块完整性请求，区块ID: {}", blockId);
            
            BlockchainVerificationService.VerificationResult result = verificationService.verifyAuditBlock(blockId);
            
            log.info("审计区块完整性验证完成，结果: {}", result.isSuccess());
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("验证审计区块完整性失败", e);
            return Result.error("验证审计区块完整性失败: " + e.getMessage());
        }
    }

    /**
     * 批量验证多个区块
     */
    @PostMapping("/verify/blocks")
    @ApiOperation(value = "批量验证多个区块", notes = "批量验证多个区块的完整性")
    public Result<BlockchainVerificationService.BatchVerificationResult> verifyMultipleBlocks(
            @RequestBody List<String> blockIds) {
        try {
            log.info("批量验证区块请求，数量: {}", blockIds.size());
            
            BlockchainVerificationService.BatchVerificationResult result = 
                    verificationService.verifyMultipleBlocks(blockIds);
            
            log.info("批量验证完成，通过: {}, 失败: {}", result.getPassed(), result.getFailed());
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("批量验证区块失败", e);
            return Result.error("批量验证区块失败: " + e.getMessage());
        }
    }

    /**
     * 查询审计追踪历史
     */
    @GetMapping("/audit-trail/{userId}")
    @ApiOperation(value = "查询审计追踪历史", notes = "查询指定用户的审计追踪历史")
    public Result<BlockchainVerificationService.AuditTrailResult> queryAuditTrail(
            @PathVariable String userId,
            @ApiParam("开始时间戳") @RequestParam long startTime,
            @ApiParam("结束时间戳") @RequestParam long endTime) {
        try {
            log.info("查询审计追踪历史请求，用户ID: {}", userId);
            
            BlockchainVerificationService.AuditTrailResult result = 
                    verificationService.queryAuditTrail(userId, startTime, endTime);
            
            log.info("审计追踪历史查询完成，记录数: {}", result.getRecords().size());
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("查询审计追踪历史失败", e);
            return Result.error("查询审计追踪历史失败: " + e.getMessage());
        }
    }

    /**
     * 生成监管报告
     */
    @GetMapping("/regulatory-report")
    @ApiOperation(value = "生成监管报告", notes = "生成指定时间范围的监管报告")
    public Result<BlockchainVerificationService.RegulatoryReport> generateRegulatoryReport(
            @ApiParam("监管机构") @RequestParam String regulator,
            @ApiParam("开始时间戳") @RequestParam long startTime,
            @ApiParam("结束时间戳") @RequestParam long endTime) {
        try {
            log.info("生成监管报告请求，监管机构: {}", regulator);
            
            BlockchainVerificationService.RegulatoryReport report = 
                    verificationService.generateRegulatoryReport(regulator, startTime, endTime);
            
            log.info("监管报告生成完成");
            return Result.success(report);
            
        } catch (Exception e) {
            log.error("生成监管报告失败", e);
            return Result.error("生成监管报告失败: " + e.getMessage());
        }
    }

    /**
     * 合规性检查
     */
    @GetMapping("/compliance/check")
    @ApiOperation(value = "合规性检查", notes = "执行指定标准的合规性检查")
    public Result<BlockchainVerificationService.ComplianceCheckResult> checkCompliance(
            @ApiParam("合规标准 (GDPR/PCI_DSS/SOX)") @RequestParam String standard) {
        try {
            log.info("合规性检查请求，标准: {}", standard);
            
            BlockchainVerificationService.ComplianceCheckResult result = 
                    verificationService.checkCompliance(standard);
            
            log.info("合规性检查完成，结果: {}", result.isCompliant());
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("合规性检查失败", e);
            return Result.error("合规性检查失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @ApiOperation(value = "健康检查", notes = "检查区块链模块的健康状态")
    public Result<Map<String, String>> healthCheck() {
        try {
            Map<String, String> health = new HashMap<>();
            health.put("status", "healthy");
            health.put("timestamp", String.valueOf(System.currentTimeMillis()));
            health.put("service", "区块链存证模块");
            
            return Result.success(health);
            
        } catch (Exception e) {
            log.error("健康检查失败", e);
            return Result.error("健康检查失败: " + e.getMessage());
        }
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    @ApiOperation(value = "获取统计信息", notes = "获取区块链存证的统计信息")
    public Result<Map<String, Object>> getStatistics() {
        try {
            log.info("获取统计信息请求");
            
            Map<String, Object> statistics = new HashMap<>();
            
            // 获取网络状态
            com.bankshield.blockchain.client.BlockchainNetworkStatus status = anchorService.getNetworkStatus();
            statistics.put("blockHeight", status.getBlockHeight());
            statistics.put("peerCount", status.getPeerCount());
            
            // 获取存证记录统计
            List<AnchorRecord> allRecords = anchorService.getAnchorRecords(null, null);
            statistics.put("totalRecords", allRecords.size());
            
            // 按类型统计
            long auditRecords = allRecords.stream()
                    .filter(r -> "AUDIT".equals(r.getAnchorType()))
                    .count();
            long keyRecords = allRecords.stream()
                    .filter(r -> "KEY".equals(r.getAnchorType()))
                    .count();
            long complianceRecords = allRecords.stream()
                    .filter(r -> "COMPLIANCE".equals(r.getAnchorType()))
                    .count();
            
            statistics.put("auditRecords", auditRecords);
            statistics.put("keyRecords", keyRecords);
            statistics.put("complianceRecords", complianceRecords);
            
            log.info("获取统计信息完成");
            return Result.success(statistics);
            
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return Result.error("获取统计信息失败: " + e.getMessage());
        }
    }
}
