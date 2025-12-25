package com.bankshield.blockchain.service;

import com.bankshield.api.entity.OperationAudit;
import com.bankshield.blockchain.dto.AnchorData;
import com.bankshield.blockchain.dto.BlockchainTransaction;
import com.bankshield.blockchain.dto.ComplianceAnchorData;
import com.bankshield.blockchain.entity.AnchorRecord;
import com.bankshield.blockchain.entity.ComplianceCertificate;
import com.bankshield.blockchain.entity.KeyChainEvent;

import java.util.List;

/**
 * 区块链存证服务接口
 * 
 * @author BankShield
 */
public interface BlockchainAnchorService {

    /**
     * 存证审计日志
     * 
     * @param audit 审计日志
     * @return 存证记录
     */
    AnchorRecord anchorAuditLog(OperationAudit audit);

    /**
     * 存证密钥事件
     * 
     * @param keyEvent 密钥事件
     * @return 存证记录
     */
    AnchorRecord anchorKeyEvent(KeyChainEvent keyEvent);

    /**
     * 存证合规检查结果
     * 
     * @param complianceData 合规检查数据
     * @return 合规证书
     */
    ComplianceCertificate anchorComplianceCheck(ComplianceAnchorData complianceData);

    /**
     * 验证审计日志完整性
     * 
     * @param auditId 审计日志ID
     * @return 验证结果
     */
    boolean verifyAuditLogIntegrity(Long auditId);

    /**
     * 验证密钥事件完整性
     * 
     * @param keyId 密钥ID
     * @return 验证结果
     */
    boolean verifyKeyEventIntegrity(Long keyId);

    /**
     * 验证合规证书完整性
     * 
     * @param certificateId 证书ID
     * @return 验证结果
     */
    boolean verifyComplianceCertificate(Long certificateId);

    /**
     * 根据ID查询存证记录
     * 
     * @param id 记录ID
     * @return 存证记录
     */
    AnchorRecord getAnchorRecord(Long id);

    /**
     * 根据记录ID查询存证记录
     * 
     * @param recordId 记录ID
     * @return 存证记录
     */
    AnchorRecord getAnchorRecordByRecordId(String recordId);

    /**
     * 根据交易哈希查询存证记录
     * 
     * @param txHash 交易哈希
     * @return 存证记录
     */
    AnchorRecord getAnchorRecordByTxHash(String txHash);

    /**
     * 查询存证记录列表
     * 
     * @param anchorType 存证类型
     * @param businessId 业务ID
     * @return 存证记录列表
     */
    List<AnchorRecord> getAnchorRecords(String anchorType, String businessId);

    /**
     * 获取区块链网络状态
     * 
     * @return 网络状态
     */
    com.bankshield.blockchain.client.BlockchainNetworkStatus getNetworkStatus();

    /**
     * 获取交易详情
     * 
     * @param txHash 交易哈希
     * @return 交易详情
     */
    BlockchainTransaction getTransaction(String txHash);

    /**
     * 批量存证
     * 
     * @param anchorDataList 存证数据列表
     * @return 批量存证结果
     */
    List<AnchorRecord> batchAnchor(List<AnchorData> anchorDataList);

    /**
     * 异步存证
     * 
     * @param data 存证数据
     * @return 异步存证任务ID
     */
    String anchorAsync(AnchorData data);

    /**
     * 生成存证证书
     * 
     * @param recordId 记录ID
     * @return 证书文件路径
     */
    String generateCertificate(String recordId);
}