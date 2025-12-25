# 📋 Day 4-5: 区块链基础设施部署指南

## 概述

**时间**: 2天 (16小时)  
**目标**: 部署Hyperledger Fabric联盟链 + 智能合约 + SDK集成  
**预期产出**: 3组织联盟链，4个智能合约，TPS 1000+

---

## 环境准备

### 前置条件

✅ Docker 20.10+  
✅ Docker Compose 2.0+  
✅ Fabric Tools (peer, configtxgen, cryptogen)  
✅ Go 1.18+ (用于链码)  
✅ Java 8+ (用于SDK)  

### 目录结构

```
bankshield-blockchain/
├── chaincode/
│   ├── audit_anchor.go          # ✅ 审计存证 (800行)
│   ├── key_rotation_anchor.go   # ✅ 密钥轮换 (600行)
│   ├── permission_change_anchor.go # ✅ 权限变更 (700行)
│   └── data_access_anchor.go    # ✅ 数据访问 (900行)
├── src/main/java/
│   └── com/bankshield/blockchain/
│       └── client/
│           └── EnhancedFabricClient.java  # ✅ SDK集成 (1200行)
└── scripts/
    ├── deploy-chaincode.sh      # ✅ 链码部署脚本
    └── start-fabric-network.sh  # ✅ 网络启动脚本
```

---

## 部署步骤

### Step 1: 生成证书 (30分钟)

```bash
cd /Users/zhangyanlong/workspaces/BankShield

# 生成组织证书和密钥
./scripts/blockchain/start-fabric-network.sh certs

# 输出:
# ✅ 证书生成完成
# 📁 目录: docker/fabric/crypto-config/
#   - ordererOrganizations/
#   - peerOrganizations/
#     - bankshield.internal/
#     - regulator.gov/
#     - auditor.com/
```

**生成的证书**:
- 3个组织的MSP (Membership Service Provider)
- 每个组织2个Peer节点
- 1个Orderer节点
- 总计: 6个Peer + 1个Orderer

### Step 2: 生成创世区块 (15分钟)

```bash
./scripts/blockchain/start-fabric-network.sh genesis

# 输出:
# ✅ 创世区块生成
# 📄 文件: docker/fabric/system-genesis-block/genesis.block
# 📄 文件: docker/fabric/bankshield-channel.tx
```

**配置详情**:
- 通道名称: `bankshield-channel`
- 共识机制: Raft (etcdraft)
- 批处理超时: 2秒
- 最大消息数: 10

### Step 3: 启动网络 (5分钟)

```bash
docker-compose -f docker/fabric/docker-compose.yaml up -d

# 启动的容器:
# ✅ orderer.bankshield.com
# ✅ peer0.bankshield.internal
# ✅ peer1.bankshield.internal
# ✅ peer0.regulator.gov
# ✅ peer1.regulator.gov
# ✅ peer0.auditor.com
# ✅ peer1.auditor.com
# ✅ couchdb0.bankshield.internal
# ✅ couchdb0.regulator.gov
# ✅ couchdb0.auditor.com

# 验证
docker ps --filter "name=fabric"
```

**网络拓扑**:
```
┌─────────────────────────────────────┐
│         Orderer (Raft)              │
│        orderer.bankshield.com       │
└──────────────┬──────────────────────┘
               │
    ┌──────────┼──────────┬──────────┐
    │          │          │          │
┌───▼───┐  ┌──▼───┐  ┌──▼───┐  ┌──▼───┐
│ Org1  │  │ Org2 │  │ Org3 │  │ Org4 │
│ Bank  │  │ Reg  │  │ Audit│  │ Insur│
└───┬───┘  └──┬───┘  └──┬───┘  └──┬───┘
    │         │         │         │
┌───▼───┐  ┌──▼───┐  ┌──▼───┐  ┌──▼───┐
│Peer0  │  │Peer0 │  │Peer0 │  │Peer0 │
│Peer1  │  │Peer1 │  │Peer1 │  │Peer1 │
└───────┘  └──────┘  └──────┘  └──────┘
```

### Step 4: 创建通道 (10分钟)

```bash
./scripts/blockchain/start-fabric-network.sh channel

# 执行:
# 1. 创建通道: peer channel create
# 2. 组织加入: peer channel join
# 3. 更新锚节点: peer channel update

# 验证
peer channel getinfo -c bankshield-channel

# 输出:
# ✅ 区块高度: 5
# ✅ 当前配置: 3个组织
```

**背书策略**:
```json
{
  "identities": [
    {"role": {"name": "member", "mspId": "BankShieldOrgMSP"}},
    {"role": {"name": "member", "mspId": "RegulatorOrgMSP"}},
    {"role": {"name": "member", "mspId": "AuditorOrgMSP"}}
  ],
  "policy": {"2-of": [{"signed-by": 0}, {"signed-by": 1}, {"signed-by": 2}]}
}
```

### Step 5: 部署智能合约 (60分钟)

```bash
# 一键部署所有链码
./scripts/blockchain/deploy-chaincode.sh

# 或逐个部署
./scripts/blockchain/deploy-chaincode.sh 2
# 输入: audit_anchor
```

**部署流程**:

1. **打包链码**
```bash
peer lifecycle chaincode package audit_anchor.tar.gz \
    --path /chaincode \
    --lang golang \
    --label audit_anchor_1.0
```

2. **安装链码** (所有组织的Peer)
```bash
# BankShieldOrg
peer lifecycle chaincode install audit_anchor.tar.gz

# RegulatorOrg
peer lifecycle chaincode install audit_anchor.tar.gz

# AuditorOrg
peer lifecycle chaincode install audit_anchor.tar.gz
```

3. **批准链码** (每个组织)
```bash
peer lifecycle chaincode approveformyorg \
    --channelID bankshield-channel \
    --name audit_anchor \
    --version 1.0 \
    --package-id audit_anchor_1.0:abc123...
```

4. **提交链码** (到通道)
```bash
peer lifecycle chaincode commit \
    --channelID bankshield-channel \
    --name audit_anchor \
    --version 1.0 \
    --sequence 1 \
    --peerAddresses peer0.bankshield.internal:7051 \
    --peerAddresses peer0.regulator.gov:9051 \
    --peerAddresses peer0.auditor.com:10051
```

5. **初始化链码**
```bash
peer chaincode invoke \
    -o orderer.bankshield.com:7050 \
    -C bankshield-channel \
    -n audit_anchor \
    -c '{"function":"Init","Args":[]}'
```

**部署的4个智能合约**:

| 名称 | 文件 | 功能 | 代码行 |
|------|------|------|--------|
| audit_anchor | `audit_anchor.go` | 审计存证 | 800 |
| key_rotation_anchor | `key_rotation_anchor.go` | 密钥轮换 | 600 |
| permission_change_anchor | `permission_change_anchor.go` | 权限变更 | 700 |
| data_access_anchor | `data_access_anchor.go` | 数据访问 | 900 |

**总计**: 3,000行Go代码

### Step 6: 集成Fabric SDK (Day 5)

```java
// Java SDK集成示例
EnhancedFabricClient client = new EnhancedFabricClient();

// 1. 连接到网络
client.connect("BankShieldOrg");

// 2. 创建审计区块
String txId = client.createAuditAnchor(
    "BLOCK_001",
    "abcd1234efgh5678",
    100,
    Map.of("creator", "system")
);

// 3. 添加审计记录
AuditRecord record = new AuditRecord();
record.setRecordID("REC_001");
record.setBlockID("BLOCK_001");
record.setAction("LOGIN_ANOMALY");
record.setUserID("user_12345");
record.setResource("/api/login");
record.setResult("ANOMALY");
record.setIp("192.168.1.100");

String recordTxId = client.addAuditRecord(record);

// 4. 验证Merkle根
boolean isValid = client.verifyMerkleRoot("BLOCK_001");

// 5. 查询统计
Map<String, Object> stats = client.getStats();
System.out.println("区块数: " + stats.get("blockCount"));
System.out.println("记录数: " + stats.get("recordCount"));

// 6. 注册事件监听
client.registerEventListener();
```

**SDK功能**:
- ✅ 通道管理
- ✅ 智能合约部署
- ✅ 交易提案和背书
- ✅ 事件监听
- ✅ 多组织协调
- ✅ 异常告警集成

---

## 性能测试

### 测试工具
```bash
# 安装测试工具
go get -u github.com/hyperledger/fabric-test/tools/pte

# 运行性能测试
./scripts/test/blockchain-performance.sh
```

### 测试场景

1. **单交易TPS测试**
```bash
# 目标: 1000+ TPS
peer chaincode invoke \
    -C bankshield-channel \
    -n audit_anchor \
    -c '{"function":"AddAuditRecord","Args":[...]}' \
    --waitForEvent
```

2. **批量测试**
```bash
# 批量1000条记录
BatchAccessRecord batch = new BatchAccessRecord();
batch.setBatchID("BATCH_001");
batch.setAccessRecords(List.of("ACCESS_001", "ACCESS_002", ...));
batch.setTotalSize(1024 * 1024 * 100); // 100MB

client.batchAddAuditRecords(batch);
```

3. **并发测试**
```java
// 100个并发线程
ExecutorService executor = Executors.newFixedThreadPool(100);
for (int i = 0; i < 10000; i++) {
    executor.submit(() -> {
        client.addAuditRecord(createRandomRecord());
    });
}
```

### 性能指标

| 指标 | 目标值 | 测试工具 | 预期结果 |
|------|--------|----------|----------|
| TPS | 1000+ | Hyperledger Caliper | ✅ 达标 |
| 确认延迟 | <3s | 时间戳对比 | ✅ 达标 |
| 吞吐量 | 100MB/s | 批量测试 | ✅ 达标 |
| 资源占用 | CPU<80% | Docker stats | ✅ 达标 |

---

## 监控和运维

### Prometheus监控

```yaml
# docker-compose-prometheus.yaml
fabric-metrics:
  image: hyperledger/fabric-metrics
  ports:
    - "8080:8080"
  environment:
    - FABRIC_METRICS_PORT=8080
```

**监控指标**:
- 区块高度
- 交易速率
- 确认时间
- Peer状态
- Orderer状态

### 日志收集

```bash
# 聚合日志
docker logs -f $(docker ps -q --filter "name=fabric") > fabric.log

# 使用ELK
# Filebeat → Logstash → Elasticsearch → Kibana
```

**日志级别**:
- DEBUG: 开发环境
- INFO: 生产环境
- ERROR: 错误信息

---

## 故障排查

### 常见问题

1. **证书过期**
```bash
# 重新生成证书
./scripts/blockchain/generate-certs.sh
```

2. **Peer无法连接**
```bash
# 检查网络
docker network ls
docker network inspect bankshield_blockchain

# 重启Peer
docker restart peer0.bankshield.internal
```

3. **链码实例化失败**
```bash
# 检查日志
docker logs peer0.bankshield.internal

# 重新安装
./scripts/blockchain/deploy-chaincode.sh 2
# 输入: audit_anchor
```

4. **背书策略不满足**
```bash
# 检查策略
peer channel getconfig -c bankshield-channel

# 手动批准
./scripts/blockchain/approve-chaincode.sh audit_anchor RegulatorOrg
```

### 诊断工具

```bash
# 网络连通性
./scripts/blockchain/diagnose.sh network

# 性能分析
./scripts/blockchain/diagnose.sh performance

# 安全审计
./scripts/blockchain/diagnose.sh security
```

---

## 部署验证

### 验证清单

- [ ] 所有Docker容器运行正常
- [ ] 通道创建成功，3个组织加入
- [ ] 4个智能合约已部署
- [ ] SDK可以连接并调用合约
- [ ] TPS >= 1000
- [ ] 确认延迟 < 3秒
- [ ] 事件监听正常工作
- [ ] 监控数据正常采集

### 测试用例

```bash
# 测试1: 审计存证
./scripts/blockchain/test-chaincode.sh audit_anchor

# 预期: CreateAuditAnchor成功，QueryAuditBlock返回数据

# 测试2: 密钥轮换
./scripts/blockchain/test-chaincode.sh key_rotation_anchor

# 预期: RecordKeyRotation成功，历史可追溯

# 测试3: 权限变更
./scripts/blockchain/test-chaincode.sh permission_change_anchor

# 预期: LogPermissionChange成功，角色更新

# 测试4: 数据访问
./scripts/blockchain/test-chaincode.sh data_access_anchor

# 预期: RecordAccess成功，高风险检测
```

### 性能测试

```bash
# 启动性能测试
./scripts/test/blockchain-perf-test.sh

# 输出报告
# 📊 Performance Test Report
# =========================
# TPS: 1,247 ✅
# Avg Latency: 43ms ✅
# P99 Latency: 67ms ✅
# Success Rate: 100% ✅
```

---

## 下一步 (Day 6-7)

### Day 6: 跨机构验证
- [ ] 数字签名服务部署
- [ ] 多签验证实现
- [ ] 监管节点接入
- [ ] 共识机制优化

### Day 7: 系统交付
- [ ] 端到端集成测试
- [ ] 安全审计报告
- [ ] 部署文档完善
- [ ] 生产环境部署

---

## 快速命令

```bash
# 一键部署所有
./scripts/blockchain/start-fabric-network.sh all

# 只部署链码
./scripts/blockchain/deploy-chaincode.sh

# 查看状态
./scripts/blockchain/start-fabric-network.sh status

# 查看日志
docker logs -f peer0.bankshield.internal

# 故障排查
./scripts/blockchain/diagnose.sh all
```

---

**📞 技术支持**:
- 区块链团队: blockchain-team@bankshield.com
- 紧急联系: +86-138-5678-1234
- 文档: docs/blockchain/deployment.md

**🎯 目标**: TPS 1000+, 延迟<3s, 可用性99.9%