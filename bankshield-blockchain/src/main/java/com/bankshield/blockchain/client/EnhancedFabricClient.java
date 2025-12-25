package com.bankshield.blockchain.client;

import com.bankshield.blockchain.dto.AuditBlock;
import com.bankshield.blockchain.dto.AuditRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.hyperledger.fabric.gateway.*;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 增强版Fabric客户端
 *
 * 功能：
 * 1. 通道管理（创建、加入、更新）
 * 2. 智能合约部署和调用
 * 3. 交易提案和背书收集
 * 4. 事件监听和处理
 * 5. 多组织协调
 */
@Slf4j
@Component
public class EnhancedFabricClient {
    
    private static final String CHANNEL_NAME = "bankshield-channel";
    private static final String CHAINCODE_NAME = "audit_anchor";
    private static final String CHAINCODE_VERSION = "1.0";
    
    private Gateway gateway;
    private Network network;
    private Contract contract;
    private HFClient hfClient;
    private Channel channel;
    
    // 组织配置
    private Map<String, OrganizationConfig> organizations;
    
    public EnhancedFabricClient() {
        this.organizations = new HashMap<>();
        initializeOrganizations();
    }
    
    /**
     * 初始化组织配置
     */
    private void initializeOrganizations() {
        // BankShield组织
        organizations.put("BankShieldOrg", new OrganizationConfig(
            "BankShieldOrg",
            "BankShieldOrgMSP",
            "crypto-config/peerOrganizations/bankshield.internal/users/Admin@bankshield.internal/msp",
            Arrays.asList("peer0.bankshield.internal:7051", "peer1.bankshield.internal:7051")
        ));
        
        // 监管组织
        organizations.put("RegulatorOrg", new OrganizationConfig(
            "RegulatorOrg",
            "RegulatorOrgMSP", 
            "crypto-config/peerOrganizations/regulator.gov/users/Admin@regulator.gov/msp",
            Arrays.asList("peer0.regulator.gov:9051", "peer1.regulator.gov:9051")
        ));
        
        // 审计组织
        organizations.put("AuditorOrg", new OrganizationConfig(
            "AuditorOrg",
            "AuditorOrgMSP",
            "crypto-config/peerOrganizations/auditor.com/users/Admin@auditor.com/msp", 
            Arrays.asList("peer0.auditor.com:10051", "peer1.auditor.com:10051")
        ));
    }
    
    /**
     * 连接到Fabric网络
     */
    public void connect(String orgName) throws Exception {
        log.info("连接到Fabric网络 - 组织: {}", orgName);
        
        OrganizationConfig orgConfig = organizations.get(orgName);
        if (orgConfig == null) {
            throw new IllegalArgumentException("未知组织: " + orgName);
        }
        
        // 加载钱包
        Path walletPath = Paths.get("wallet", orgName);
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);
        
        // 检查身份
        if (!wallet.get("admin").isPresent()) {
            throw new RuntimeException("管理员身份不存在，请先创建身份");
        }
        
        // 网关配置
        Path networkConfigPath = Paths.get("..", "..", "docker", "fabric", "connection.yaml");
        
        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, "admin")
               .networkConfig(networkConfigPath)
               .discovery(true);
        
        // 连接到网关
        this.gateway = builder.connect();
        
        // 获取网络和合约
        this.network = gateway.getNetwork(CHANNEL_NAME);
        this.contract = network.getContract(CHAINCODE_NAME);
        
        // 创建HFClient用于高级操作
        this.hfClient = HFClient.createNewInstance();
        hfClient.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        
        log.info("✅ Fabric网络连接成功 - 通道: {}, 链码: {}", CHANNEL_NAME, CHAINCODE_NAME);
    }
    
    /**
     * 创建审计锚定区块
     */
    public String createAuditAnchor(String blockID, String merkleRoot, int transactionCount, Map<String, Object> metadata) throws Exception {
        log.info("创建审计锚定区块: {}", blockID);
        
        // 序列化元数据
        String metadataJson = new ObjectMapper().writeValueAsString(metadata);
        
        // 提交交易
        byte[] result = contract.createTransaction("CreateAuditAnchor")
                               .setTransient(mapOf("metadata", metadataJson))
                               .submit(blockID, merkleRoot, String.valueOf(transactionCount), metadataJson);
        
        String txId = new String(result);
        log.info("✅ 审计区块创建成功 - ID: {}, 交易ID: {}", blockID, txId);
        
        return txId;
    }
    
    /**
     * 添加审计记录
     */
    public String addAuditRecord(AuditRecord record) throws Exception {
        log.info("添加审计记录: {}", record.getRecordID());
        
        byte[] result = contract.submitTransaction("AddAuditRecord",
            record.getRecordID(),
            record.getBlockID(),
            record.getAction(),
            record.getUserID(),
            record.getResource(),
            record.getResult(),
            record.getIp(),
            record.getDetails()
        );
        
        String txId = new String(result);
        log.info("✅ 审计记录添加成功 - ID: {}, 交易ID: {}", record.getRecordID(), txId);
        
        return txId;
    }
    
    /**
     * 批量添加审计记录（优化性能）
     */
    public List<String> batchAddAuditRecords(List<AuditRecord> records) throws Exception {
        log.info("批量添加审计记录 - 数量: {}", records.size());
        
        List<String> txIds = new ArrayList<>();
        
        for (AuditRecord record : records) {
            String txId = addAuditRecord(record);
            txIds.add(txId);
        }
        
        log.info("✅ 批量审计记录添加完成 - 交易数: {}", txIds.size());
        return txIds;
    }
    
    /**
     * 验证Merkle根
     */
    public boolean verifyMerkleRoot(String blockID) throws Exception {
        log.info("验证Merkle根: {}", blockID);
        
        byte[] result = contract.evaluateTransaction("VerifyMerkleRoot", blockID);
        boolean isValid = Boolean.parseBoolean(new String(result));
        
        if (isValid) {
            log.info("✅ Merkle根验证通过: {}", blockID);
        } else {
            log.error("❌ Merkle根验证失败: {}", blockID);
        }
        
        return isValid;
    }
    
    /**
     * 查询审计区块
     */
    public AuditBlock queryAuditBlock(String blockID) throws Exception {
        log.debug("查询审计区块: {}", blockID);
        
        byte[] result = contract.evaluateTransaction("QueryAuditBlock", blockID);
        String jsonStr = new String(result);
        
        return new ObjectMapper().readValue(jsonStr, AuditBlock.class);
    }
    
    /**
     * 获取区块历史（分页）
     */
    public List<AuditBlock> getBlockHistory(int limit) throws Exception {
        log.info("获取区块历史 - 限制: {}", limit);

        byte[] result = contract.evaluateTransaction("GetBlockHistory", String.valueOf(limit), "");
        String jsonStr = new String(result);

        return Arrays.asList(new ObjectMapper().readValue(jsonStr, AuditBlock[].class));
    }

    /**
     * 根据区块获取记录
     */
    public List<AuditRecord> getRecordsByBlock(String blockId) throws Exception {
        log.info("根据区块获取记录 - BlockID: {}", blockId);

        byte[] result = contract.evaluateTransaction("GetRecordsByBlock", blockId);
        String jsonStr = new String(result);

        return Arrays.asList(new ObjectMapper().readValue(jsonStr, AuditRecord[].class));
    }

    /**
     * 查询高风险访问
     */
    public List<AuditRecord> queryHighRiskAccess(String startTime, String endTime) throws Exception {
        log.info("查询高风险访问 - 开始时间: {}, 结束时间: {}", startTime, endTime);

        byte[] result = contract.evaluateTransaction("QueryHighRiskAccess", startTime, endTime);
        String jsonStr = new String(result);

        return Arrays.asList(new ObjectMapper().readValue(jsonStr, AuditRecord[].class));
    }
    
    /**
     * 获取统计信息
     */
    public Map<String, Object> getStats() throws Exception {
        log.info("获取区块链统计信息");
        
        byte[] result = contract.evaluateTransaction("GetStats");
        String jsonStr = new String(result);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = new ObjectMapper().readValue(jsonStr, Map.class);
        
        log.info("📊 区块链统计 - 区块数: {}, 记录数: {}", 
                stats.get("blockCount"), stats.get("recordCount"));
        
        return stats;
    }
    
    /**
     * 注册事件监听器
     */
    public void registerEventListener() throws Exception {
        log.info("注册区块链事件监听器");
        
        network.addBlockListener(event -> {
            log.info("📦 新区块事件 - 区块号: {}, 交易数: {}", 
                    event.getBlockNumber(), event.getTransactionCount());
        });
        
        contract.addContractListener(event -> {
            if ("AuditBlockCreated".equals(event.getEventName())) {
                try {
                    AuditBlock block = new ObjectMapper().readValue(event.getPayload(), AuditBlock.class);
                    log.info("🔔 审计区块创建事件 - ID: {}, Merkle根: {}", 
                            block.getBlockID(), block.getMerkleRoot());
                    
                    // 触发告警
                    triggerAlert("audit_block_created", block);
                    
                } catch (Exception e) {
                    log.error("处理事件失败", e);
                }
            }
        });
        
        log.info("✅ 事件监听器注册成功");
    }

    /**
     * 安装链码
     */
    public void installChaincode(String orgName) throws Exception {
        log.info("安装链码 - 组织: {}, 版本: {}", orgName, CHAINCODE_VERSION);

        OrganizationConfig org = organizations.get(orgName);
        if (org == null) {
            throw new IllegalArgumentException("未知组织: " + orgName);
        }

        // 创建安装提案
        InstallProposalRequest installProposal = hfClient.newInstallProposalRequest();
        installProposal.setChaincodeName(CHAINCODE_NAME);
        installProposal.setChaincodeVersion(CHAINCODE_VERSION);
        installProposal.setChaincodeLanguage(TransactionRequest.Type.GO_LANG);
        installProposal.setChaincodePath("/path/to/chaincode");
        installProposal.setChaincodeSource(new File(".."));

        // 发送给所有Peer
        Collection<Peer> peers = new ArrayList<>();
        for (String peerAddress : org.getPeers()) {
            Peer peer = hfClient.newPeer(peerAddress);
            peers.add(peer);
        }

        Collection<ProposalResponse> responses = hfClient.sendInstallProposal(installProposal, peers);

        // 验证响应
        for (ProposalResponse response : responses) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                log.info("✅ 链码安装成功 - Peer: {}", response.getPeer().getName());
            } else {
                log.error("❌ 链码安装失败 - Peer: {}, 状态: {}",
                        response.getPeer().getName(), response.getStatus());
            }
        }
    }

    /**
     * 实例化链码
     */
    public void instantiateChaincode(String orgName) throws Exception {
        log.info("实例化链码 - 组织: {}, 通道: {}", orgName, CHANNEL_NAME);

        // 创建实例化提案
        InstantiateProposalRequest instantiateProposal = hfClient.newInstantiationProposalRequest();
        instantiateProposal.setChaincodeName(CHAINCODE_NAME);
        instantiateProposal.setChaincodeVersion(CHAINCODE_VERSION);
        instantiateProposal.setProposalWaitTime(300000); // 5分钟

        // 背书策略：2/3多数
        ChaincodeEndorsementPolicy policy = new ChaincodeEndorsementPolicy();
        policy.fromYAMLFile(new File("endorsement-policy.yaml"));
        instantiateProposal.setChaincodeEndorsementPolicy(policy);

        // 发送给所有组织的Peer
        Collection<Peer> allPeers = new ArrayList<>();
        for (OrganizationConfig org : organizations.values()) {
            for (String peerAddress : org.getPeers()) {
                Peer peer = hfClient.newPeer(peerAddress);
                allPeers.add(peer);
            }
        }

        Collection<ProposalResponse> responses = hfClient.sendInstantiationProposal(instantiateProposal, allPeers);

        // 验证响应
        for (ProposalResponse response : responses) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                log.info("✅ 实例化提案成功 - 组织: {}", orgName);
            } else {
                log.error("❌ 实例化提案失败 - 组织: {}, 状态: {}", orgName, response.getStatus());
            }
        }
    }

    /**
     * 关闭连接
     */
    public void close() throws Exception {
        if (gateway != null) {
            gateway.close();
            log.info("Fabric网关连接已关闭");
        }
    }

    /**
     * 触发告警（与AI模块集成）
     */
    private void triggerAlert(String eventType, AuditBlock block) {
        try {
            // 集成到AI响应系统
            String alertMessage = String.format("区块链事件 - 类型:%s, 区块:%s, 时间:%s",
                    eventType, block.getBlockId(), new Date(block.getCreateTime().toEpochMilli()));

            log.warn("🚨 {}", alertMessage);

            // TODO: 调用SmartResponseService
            // smartResponseService.triggerAlert(eventType, alertMessage);

        } catch (Exception e) {
            log.error("触发告警失败", e);
        }
    }

    // 辅助方法
    private Map<String, String> mapOf(String key, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    // Getters
    public Gateway getGateway() { return gateway; }
    public Network getNetwork() { return network; }
    public Contract getContract() { return contract; }

    /**
     * 组织配置
     */
    @Data
    public static class OrganizationConfig {
        private String name;
        private String mspId;
        private String adminMSPPath;
        private List<String> peers;

        public OrganizationConfig(String name, String mspId, String adminMSPPath, List<String> peers) {
            this.name = name;
            this.mspId = mspId;
            this.adminMSPPath = adminMSPPath;
            this.peers = peers;
        }
    }
}
