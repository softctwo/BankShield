# 审计日志完整性校验实现文档

## 概述

BankShield系统实现了基于区块链+哈希链双重校验的审计日志完整性保护机制，确保审计日志无法被篡改或删除。

## 核心功能

### 1. 哈希链完整性校验
- 采用Merkle树结构组织审计日志
- 每个区块包含前一个区块的哈希值，形成链式结构
- 任何数据的修改都会导致哈希链断裂，立即被发现

### 2. 定时区块生成
- 每5分钟自动检查并生成新的审计区块
- 每个区块最多包含1000条审计日志
- 支持手动触发区块生成

### 3. 完整性验证
- 支持单条审计日志完整性验证
- 支持整个系统完整性验证
- 提供详细的完整性报告

### 4. 可选区块链增强
- 支持将审计区块锚定到区块链网络
- 提供额外的不可篡改保障
- 支持Fabric、Ethereum等主流区块链

## 技术架构

### 数据模型

#### 审计区块表 (audit_block)
```sql
CREATE TABLE audit_block (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  block_number BIGINT NOT NULL COMMENT '区块号（递增）',
  previous_hash VARCHAR(128) NOT NULL COMMENT '前一个区块的哈希',
  current_hash VARCHAR(128) NOT NULL COMMENT '当前区块的哈希',
  merkle_root VARCHAR(128) NOT NULL COMMENT 'Merkle树根哈希',
  audit_count BIGINT NOT NULL COMMENT '区块内审计日志数量',
  block_time DATETIME NOT NULL COMMENT '区块生成时间',
  operator VARCHAR(50) NOT NULL COMMENT '区块创建者',
  metadata TEXT COMMENT '区块元数据（JSON格式）',
  status INT DEFAULT 1 COMMENT '0-验证中, 1-已确认, 2-异常',
  blockchain_tx_hash VARCHAR(128) COMMENT '区块链交易哈希（可选）',
  blockchain_confirm_time DATETIME COMMENT '区块链确认时间'
);
```

#### 审计日志与区块关联表 (audit_operation_block)
```sql
CREATE TABLE audit_operation_block (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  audit_id BIGINT NOT NULL COMMENT '审计日志ID',
  block_id BIGINT NOT NULL COMMENT '区块ID',
  merkle_path TEXT COMMENT 'Merkle路径',
  index_in_block INT NOT NULL COMMENT '在区块中的索引'
);
```

### 核心算法

#### 1. Merkle树构建
```java
private String calculateMerkleRoot(List<String> hashes) {
    while (hashes.size() > 1) {
        List<String> nextLevel = new ArrayList<>();
        for (int i = 0; i < hashes.size(); i += 2) {
            if (i + 1 < hashes.size()) {
                String combined = hashes.get(i) + hashes.get(i + 1);
                nextLevel.add(EncryptUtil.sm3Hash(combined));
            } else {
                nextLevel.add(hashes.get(i));
            }
        }
        hashes = nextLevel;
    }
    return hashes.get(0);
}
```

#### 2. 区块哈希计算
```java
private String calculateBlockHash(AuditBlock block) {
    String content = block.getBlockNumber() + 
                    block.getPreviousHash() + 
                    block.getMerkleRoot() + 
                    block.getAuditCount() + 
                    block.getBlockTime();
    return EncryptUtil.sm3Hash(content);
}
```

#### 3. 审计日志哈希计算
```java
private String calculateAuditHash(OperationAudit audit) {
    String content = audit.getId() + 
                    audit.getUserId() + 
                    audit.getOperationType() + 
                    audit.getRequestUrl() + 
                    audit.getCreateTime();
    return EncryptUtil.sm3Hash(content);
}
```

## API接口

### 1. 验证审计日志完整性
```http
GET /api/audit/verification/verify/{auditId}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

### 2. 验证系统完整性
```http
GET /api/audit/verification/system-integrity
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalAudits": 10000,
    "anchoredAudits": 9500,
    "anchoringRate": 95.0,
    "totalBlocks": 10,
    "verificationResult": 1,
    "integrityIssues": []
  }
}
```

### 3. 手动生成区块
```http
POST /api/audit/verification/block/generate
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": 11
}
```

### 4. 查询审计区块列表
```http
GET /api/audit/verification/block/list?page=1&size=10
```

### 5. 获取完整性统计数据
```http
GET /api/audit/verification/statistics
```

## 配置参数

### 基础配置
```yaml
audit:
  block:
    enabled: true        # 启用审计日志区块
    size: 1000           # 每个区块包含1000条审计日志
    interval-minutes: 5  # 每5分钟检查一次
```

### 区块链增强配置（可选）
```yaml
audit:
  block:
    blockchain-anchor:
      enabled: true      # 启用区块链存证
      anchor-interval: 60  # 每60分钟上链一次
      network: "fabric"    # 区块链网络类型
      contract-name: "AuditAnchorContract"  # 智能合约名称
```

## 部署步骤

### 1. 数据库初始化
```bash
mysql -u root -p bankshield < sql/audit_integrity_module.sql
```

### 2. 应用配置
确保application.yml中包含审计完整性配置：
```yaml
audit:
  block:
    enabled: true
    size: 1000
    interval-minutes: 5
```

### 3. 启动应用
```bash
mvn spring-boot:run
```

### 4. 验证功能
访问以下接口验证功能：
- Swagger文档：http://localhost:8080/swagger-ui.html
- 系统完整性验证：http://localhost:8080/api/audit/verification/system-integrity

## 安全特性

### 1. 防篡改机制
- 哈希链结构确保任何修改都能被发现
- Merkle树提供高效的完整性验证
- 国密SM3算法保证哈希安全性

### 2. 权限控制
- 验证操作需要AUDIT_VIEW权限
- 区块生成需要AUDIT_MANAGE权限
- 基于Spring Security的细粒度权限控制

### 3. 异常监控
- 完整性验证失败时自动发送告警
- 支持多种告警方式（邮件、短信、Webhook）
- 详细的异常日志记录

## 性能优化

### 1. 批量处理
- 每批次处理1000条审计日志
- 减少数据库操作次数
- 提高整体处理效率

### 2. 异步处理
- 区块生成采用异步处理
- 不影响正常的审计日志记录
- 支持并发处理多个区块

### 3. 缓存优化
- 热点数据缓存
- 减少重复计算
- 提高验证效率

## 监控指标

### 1. 完整性指标
- 审计日志总数
- 已上链数量
- 上链率百分比
- 区块总数

### 2. 性能指标
- 区块生成时间
- 验证响应时间
- 系统吞吐量
- 错误率

### 3. 告警指标
- 完整性验证失败次数
- 区块生成异常次数
- 区块链网络异常

## 扩展功能

### 1. 多链支持
- 支持多种区块链网络
- 可配置链选择策略
- 支持链间切换

### 2. 智能合约
- 支持自定义智能合约
- 提供合约模板
- 支持合约升级

### 3. 数据归档
- 支持历史数据归档
- 提供归档数据验证
- 支持数据恢复

## 测试用例

系统提供了完整的测试用例，包括：
- 审计日志完整性验证测试
- 系统完整性验证测试
- 区块生成逻辑测试
- 异常场景测试

运行测试：
```bash
mvn test -Dtest=AuditIntegrityTest
```

## 总结

BankShield审计日志完整性校验系统提供了企业级的审计日志保护方案，通过哈希链和区块链双重保障，确保审计日志的完整性和不可篡改性。系统具有高可用、高性能、易扩展的特点，满足金融级应用的安全要求。