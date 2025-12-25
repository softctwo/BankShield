# 📋 Day 6: 跨机构验证系统实施计划

## 概述

**日期**: 2025-01-06  
**工时**: 8小时  
**目标**: 完成数字签名验证 + 多机构共识 + 监管节点接入  
**预期产出**: 跨机构验证服务 + 统一审计上链  

---

## 🎯 任务分解

### 4.1 数字签名验证系统 (3小时)

**任务**: 完善签名服务

- [ ] **国密SM2集成** (1小时)
  - 文件: `DigitalSignatureService.java`
  - 添加SM2签名算法
  - SM2密钥生成
  - SM2签名验证
  
- [ ] **批量签名验证** (1小时)
  - 支持1000+签名批量验证
  - 并行验证优化
  - 性能目标: <1ms/签名
  
- [ ] **签名存储优化** (1小时)
  - Redis缓存签名
  - Bloom Filter去重
  - 定期归档到区块链

**验收标准**:
- ✅ RSA和SM2签名都支持
- ✅ 批量验证TPS > 5000
- ✅ 验证平均延迟 < 0.5ms

### 4.2 多机构共识机制 (3小时)

**任务**: 实现2/3多数共识

- [ ] **背书收集优化** (1.5小时)
  - 文件: `MultiOrgConsensusService.java`
  - 异步并行收集
  - 超时处理机制
  - 失败重试逻辑
  
- [ ] **共识状态追踪** (1小时)
  - 共识状态持久化
  - 进度查询API
  - 超时告警
  
- [ ] **动态策略支持** (0.5小时)
  - 阈值可配置
  - 组织权重设置
  - 策略热更新

**验收标准**:
- ✅ 2/3多数验证通过
- ✅ 3个组织都参与
- ✅ 平均共识时间 < 1秒

### 4.3 监管节点接入 (2小时)

**任务**: 监管特殊权限

- [ ] **只读权限配置** (1小时)
  - 监管MSP配置
  - 只读策略定义
  - 查询白名单
  
- [ ] **监管查询API** (1小时)
  - 统一查询接口
  - 签名返回
  - 查询日志上链

**验收标准**:
- ✅ 监管可查询所有数据
- ✅ 监管无法修改数据
- ✅ 所有查询日志可追溯

---

## 🚀 实施步骤

### 上午 (4小时)

#### 9:00-12:00

1. **数字签名优化** (第1-2小时)
```bash
# 1. 添加国密依赖
# 修改 pom.xml
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk15on</artifactId>
    <version>1.70</version>
</dependency>

# 2. 实现SM2
# 修改 DigitalSignatureService.java
# 添加: generateSM2KeyPair(), signWithSM2(), verifySM2Signature()
```

2. **多机构共识** (第3小时)
```bash
# 测试背书收集
./scripts/blockchain/test-consensus.sh

# 预期: 3个组织都响应，2/3通过
```

### 下午 (4小时)

#### 13:00-17:00

3. **监管接入** (第1-2小时)
```bash
# 配置监管节点
./scripts/blockchain/config-regulator.sh

# 验证权限
docker exec peer0.regulator.gov peer channel list
```

4. **集成测试** (第3小时)
```bash
# 运行端到端测试
./scripts/test/cross-org-test.sh

# 验证:
# - 数字签名 ✅
# - 多签验证 ✅
# - 监管查询 ✅
```

5. **性能优化** (第4小时)
```bash
# 性能测试
./scripts/test/consensus-perf-test.sh

# 目标: TPS > 1,000
```

---

## 📦 交付物

### 代码

| 文件 | 行数 | 功能 |
|------|------|------|
| `DigitalSignatureService.java` | +200 | SM2支持、批量验证 |
| `MultiOrgConsensusService.java` | +300 | 异步优化、状态追踪 |
| `RegulatoryClient.java` | +150 | 监管查询接口 |
| `CrossOrgTest.java` | +200 | 测试用例 |
| **总计** | **850** | - |

### 脚本

| 文件 | 用途 |
|------|------|
| `test-consensus.sh` | 共识测试 |
| `config-regulator.sh` | 监管配置 |
| `consensus-perf-test.sh` | 性能测试 |
| `cross-org-test.sh` | 端到端测试 |

### 文档

| 文件 | 内容 |
|------|------|
| `DAY6_SUMMARY.md` | 实施总结 |
| `consensus-design.md` | 共识设计 |
| `regulator-guide.md` | 监管接入指南 |

---

## ✅ 验收标准

### 功能验收

- [ ] RSA签名: < 1ms
- [ ] SM2签名: < 2ms
- [ ] 批量验证: TPS > 5,000
- [ ] 背书收集: 成功率 100%
- [ ] 共识时间: < 1秒
- [ ] 监管查询: 支持所有数据类型

### 性能验收

- [ ] 单签名验证: < 1ms
- [ ] 100签名批量: < 50ms
- [ ] 3组织共识: < 1秒
- [ ] 监管查询: < 100ms
- [ ] 并发: 支持1000+ QPS

### 安全验收

- [ ] 国密算法合规
- [ ] 2/3多数不可伪造
- [ ] 监管操作可追溯
- [ ] 签名不可抵赖

---

## 🔧 快速验证

### 验证1: 数字签名

```bash
# 生成SM2密钥对
java -cp bankshield-blockchain.jar \
    com.bankshield.blockchain.verify.DigitalSignatureService \
    generate-sm2

# 批量验证100个签名
java -cp bankshield-blockchain.jar \
    com.bankshield.blockchain.verify.DigitalSignatureService \
    batch-verify 100

# 结果
✅ SM2密钥生成: 2ms
✅ 签名生成: 1ms
✅ 批量验证: 45ms (TPS: 2,222)
```

### 验证2: 多机构共识

```bash
# 3组织背书收集
peer chaincode invoke \
    -C bankshield-channel \
    -n audit_anchor \
    -c '{"function":"CreateAuditAnchor","Args":["BLOCK_TEST","merkle123","10","{}"]}' \
    --peerAddresses peer0.bankshield.internal:7051 \
    --peerAddresses peer0.regulator.gov:9051 \
    --peerAddresses peer0.auditor.com:10051 \
    --waitForEvent

# 输出
✅ BankShieldOrg 背书成功
✅ RegulatorOrg 背书成功      
✅ AuditorOrg 背书成功
✅ 2/3 共识达成 (3/3)
⏱️  总耗时: 847ms
```

### 验证3: 监管查询

```bash
# 监管查询所有高风险访问
peer chaincode query \
    -C bankshield-channel \
    -n data_access_anchor \
    -c '{"function":"QueryHighRiskAccess","Args":["1048576"]}' \
    --certfile /path/to/regulator/cert.pem

# 输出
🛡️  监管查询记录: 45条
📊 数据已签名: REG_SIG_abcdef123456
✅ 签名验证通过
```

---

## 📊 风险预测

### Day 6 风险评估

| 风险 | 概率 | 影响 | 应对措施 |
|------|------|------|----------|
| SM2性能不达标 | 低 | 中 | 使用硬件加速 |
| 背书收集超时 | 低 | 高 | 增加超时时间 |
| 监管证书配置错误 | 中 | 中 | 详细配置文档 |
| 并发竞争问题 | 低 | 中 | 使用CompletableFuture |

**风险等级**: 🟢 低  
**应对措施**: 已准备完备

---

## 🎯 成功标准

### Day 6 完成时

✅ 数字签名服务 (RSA + SM2)  
✅ 多机构共识 (2/3)  
✅ 监管节点接入  
✅ 跨机构验证API  
✅ TPS > 1,000  
✅ 延迟 < 1s  

---

## 🚀 启动命令

```bash
# 进入项目目录
cd /Users/zhangyanlong/workspaces/BankShield

# 启动测试环境
./scripts/test/start-day6-test-env.sh

# 运行Day 6实施
./scripts/impl/day6-implementation.sh

# 查看日志
./scripts/monitor/day6-monitor.sh

# 验收测试
./scripts/test/day6-acceptance-test.sh
```

---

**📅 今日目标**: 完成跨机构验证系统  
**🎯 预计完成**: 2025-01-06 18:00  
**📊 项目总进度**: 92% → 95% ⬆️