package com.bankshield.blockchain.client;

import com.bankshield.blockchain.dto.AnchorData;
import com.bankshield.blockchain.dto.BlockchainTransaction;
import com.bankshield.blockchain.entity.AnchorRecord;
import com.bankshield.blockchain.utils.EncryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.gateway.*;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Hyperledger Fabric区块链客户端实现
 * 
 * @author BankShield
 */
@Slf4j
@Component
public class FabricBlockchainClient implements BlockchainClient {

    @Value("${blockchain.fabric.wallet-path:wallet}")
    private String walletPath;

    @Value("${blockchain.fabric.connection-profile:connection.json}")
    private String connectionProfilePath;

    @Value("${blockchain.fabric.channel:mychannel}")
    private String channelName;

    @Value("${blockchain.fabric.contract:BankShieldContract}")
    private String contractName;

    @Value("${blockchain.fabric.identity:admin}")
    private String identityName;

    private Gateway gateway;
    private Network network;
    private Contract contract;

    @PostConstruct
    public void init() throws Exception {
        try {
            log.info("初始化Hyperledger Fabric区块链客户端...");
            
            // 加载钱包
            Wallet wallet = Wallets.newFileSystemWallet(Paths.get(walletPath));
            
            // 检查身份是否存在
            if (!wallet.get(identityName).isPresent()) {
                throw new RuntimeException("钱包中找不到身份: " + identityName);
            }

            // 创建网关连接
            Gateway.Builder builder = Gateway.createBuilder();
            builder.identity(wallet, identityName)
                   .networkConfig(Paths.get(connectionProfilePath))
                   .discovery(true)
                   .commitTimeout(30, TimeUnit.SECONDS)
                   .commitWaitTime(60, TimeUnit.SECONDS);

            this.gateway = builder.connect();
            this.network = gateway.getNetwork(channelName);
            this.contract = network.getContract(contractName);
            
            log.info("Hyperledger Fabric区块链客户端初始化成功");
            
        } catch (Exception e) {
            log.error("Hyperledger Fabric区块链客户端初始化失败", e);
            throw new RuntimeException("区块链客户端初始化失败", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (gateway != null) {
            gateway.close();
            log.info("Hyperledger Fabric区块链客户端已关闭");
        }
    }

    @Override
    public String sendTransaction(AnchorData data) {
        try {
            log.info("发送Fabric存证交易，记录ID: {}", data.getRecordId());
            
            // 创建存证记录
            byte[] result = contract.createTransaction("createAnchor")
                .submit(
                    data.getRecordId(),
                    data.getAnchorType(),
                    data.getBusinessId(),
                    data.getContentHash(),
                    data.getOriginalData(),
                    data.getMetadata(),
                    data.getTimestamp().toString(),
                    data.getCreator()
                );
            
            String txHash = new String(result);
            log.info("Fabric交易发送成功，交易哈希: {}", txHash);
            
            return txHash;
            
        } catch (Exception e) {
            log.error("Fabric交易发送失败", e);
            throw new RuntimeException("Fabric交易发送失败", e);
        }
    }

    @Override
    public AnchorRecord queryTransaction(String recordId) {
        try {
            log.info("查询Fabric存证记录，记录ID: {}", recordId);
            
            byte[] result = contract.evaluateTransaction("queryAnchor", recordId);
            String json = new String(result);
            
            // 解析JSON数据并转换为AnchorRecord
            AnchorRecord record = parseAnchorRecord(json);
            record.setRecordId(recordId);
            
            log.info("Fabric存证记录查询成功");
            return record;
            
        } catch (Exception e) {
            log.error("Fabric存证记录查询失败", e);
            throw new RuntimeException("Fabric存证记录查询失败", e);
        }
    }

    @Override
    public boolean verifyDataIntegrity(String recordId, String expectedHash) {
        try {
            log.info("验证Fabric数据完整性，记录ID: {}", recordId);
            
            byte[] result = contract.evaluateTransaction("verifyIntegrity", recordId, expectedHash);
            boolean verified = Boolean.parseBoolean(new String(result));
            
            log.info("Fabric数据完整性验证结果: {}", verified);
            return verified;
            
        } catch (Exception e) {
            log.error("Fabric数据完整性验证失败", e);
            return false;
        }
    }

    @Override
    public List<AnchorRecord> queryTransactions(String anchorType, String businessId) {
        try {
            log.info("批量查询Fabric交易，类型: {}, 业务ID: {}", anchorType, businessId);
            
            // 构造查询范围
            String startKey = anchorType + ":" + businessId + ":0";
            String endKey = anchorType + ":" + businessId + ":9999999999999";
            
            byte[] result = contract.evaluateTransaction("queryByRange", startKey, endKey);
            String json = new String(result);
            
            // 解析JSON数组并转换为AnchorRecord列表
            List<AnchorRecord> records = parseAnchorRecordList(json);
            
            log.info("Fabric批量查询成功，返回{}条记录", records.size());
            return records;
            
        } catch (Exception e) {
            log.error("Fabric批量查询失败", e);
            throw new RuntimeException("Fabric批量查询失败", e);
        }
    }

    @Override
    public BlockchainNetworkStatus getNetworkStatus() {
        try {
            Channel channel = network.getChannel();
            
            // 查询最新区块
            BlockEvent blockEvent = channel.queryBlockByNumber(channel.queryBlockchainInfo().getHeight() - 1);
            
            BlockchainNetworkStatus status = BlockchainNetworkStatus.builder()
                .networkType("Hyperledger Fabric")
                .connected(true)
                .blockHeight(channel.queryBlockchainInfo().getHeight())
                .latestBlockHash(blockEvent.getBlockHash() != null ? blockEvent.getBlockHash().toString() : "")
                .latestBlockTime(LocalDateTime.now())
                .peerCount(channel.getPeers().size())
                .pendingTransactions(0) // Fabric没有待确认交易概念
                .networkDelay(0)
                .checkTime(LocalDateTime.now())
                .build();
            
            return status;
            
        } catch (Exception e) {
            log.error("获取Fabric网络状态失败", e);
            return BlockchainNetworkStatus.builder()
                .networkType("Hyperledger Fabric")
                .connected(false)
                .errorMessage(e.getMessage())
                .checkTime(LocalDateTime.now())
                .build();
        }
    }

    @Override
    public BlockchainTransaction getTransaction(String txHash) {
        try {
            log.info("查询Fabric交易详情，交易哈希: {}", txHash);
            
            Channel channel = network.getChannel();
            
            // 查询交易详情
            BlockEvent.TransactionEvent txEvent = channel.queryTransactionByID(txHash);
            
            BlockchainTransaction transaction = BlockchainTransaction.builder()
                .txHash(txHash)
                .blockHash(txEvent.getBlockEvent().getBlockHash().toString())
                .blockNumber(txEvent.getBlockEvent().getBlockNumber())
                .status("CONFIRMED")
                .timestamp(LocalDateTime.now())
                .confirmations(1)
                .build();
            
            return transaction;
            
        } catch (Exception e) {
            log.error("查询Fabric交易详情失败", e);
            throw new RuntimeException("查询Fabric交易详情失败", e);
        }
    }

    @Override
    public boolean waitForConfirmation(String txHash, int confirmations) {
        try {
            log.info("等待Fabric交易确认，交易哈希: {}", txHash);
            
            // Fabric交易一旦提交就被确认，不需要等待
            return true;
            
        } catch (Exception e) {
            log.error("等待Fabric交易确认失败", e);
            return false;
        }
    }

    @Override
    public long getCurrentBlockHeight() {
        try {
            Channel channel = network.getChannel();
            return channel.queryBlockchainInfo().getHeight();
        } catch (Exception e) {
            log.error("获取Fabric区块高度失败", e);
            return 0;
        }
    }

    @Override
    public String estimateTransactionFee(AnchorData data) {
        // Fabric不需要交易费用
        return "0";
    }

    /**
     * 解析JSON数据为AnchorRecord
     */
    private AnchorRecord parseAnchorRecord(String json) {
        // 这里需要根据实际的JSON结构进行解析
        // 简化实现，实际需要根据Fabric链码返回的格式进行解析
        AnchorRecord record = new AnchorRecord();
        record.setBlockchainTxHash("fabric-tx-hash");
        record.setBlockNumber(12345L);
        record.setBlockHash("fabric-block-hash");
        record.setAnchorTime(LocalDateTime.now());
        record.setAnchorStatus("CONFIRMED");
        record.setBlockchainType("FABRIC");
        
        return record;
    }

    /**
     * 解析JSON数组为AnchorRecord列表
     */
    private List<AnchorRecord> parseAnchorRecordList(String json) {
        List<AnchorRecord> records = new ArrayList<>();
        // 简化实现，实际需要根据JSON结构解析
        return records;
    }
}