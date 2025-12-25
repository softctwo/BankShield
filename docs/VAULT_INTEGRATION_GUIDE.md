# BankShield HashiCorp Vault 集成指南

## 概述

本指南描述了如何将HashiCorp Vault集成到BankShield系统中，以解决密钥硬编码的安全问题，提供安全的密钥管理和存储方案。

## 🎯 目标

- ✅ 消除代码中的硬编码密钥
- ✅ 提供安全的密钥存储和管理
- ✅ 支持密钥的动态轮换
- ✅ 提供审计和监控功能
- ✅ 符合金融行业的安全标准

## 🏗️ 架构设计

### 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                    BankShield Application                    │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌──────────────────┐  ┌─────────────┐ │
│  │   API Layer     │  │  Service Layer   │  │   Vault     │ │
│  │                 │  │                  │  │ Integration │ │
│  └────────┬────────┘  └────────┬─────────┘  └─────────────┘ │
│           │                    │                            │
│  ┌────────▼────────┐  ┌────────▼─────────┐                 │
│  │ Vault Controller│  │SecureKeyService  │                 │
│  │                 │  │                  │                 │
│  └────────┬────────┘  └────────┬─────────┘                 │
│           │                    │                            │
│  ┌────────▼────────────────────▼─────────┐                  │
│  │         Vault Operations              │                  │
│  │  (AppRole Authentication)             │                  │
│  └────────────────┬──────────────────────┘                  │
│                   │                                           │
└───────────────────┼───────────────────────────────────────────┘
                    │
                    ▼
         ┌──────────────────┐
         │  HashiCorp Vault │
         │                  │
         │  • KV存储引擎     │
         │  • AppRole认证   │
         │  • 审计日志       │
         │  • 密钥轮换       │
         └──────────────────┘
```

## 🔧 部署步骤

### 1. 部署HashiCorp Vault

#### 选项A：使用Docker Compose（推荐开发环境）

```bash
# 启动Vault服务
cd docker/vault
docker-compose up -d

# 验证Vault状态
docker exec bankshield-vault vault status
```

#### 选项B：使用部署脚本

```bash
# 执行Vault部署脚本
./scripts/security/setup-vault.sh

# 查看部署结果
cat /opt/vault/vault.env
```

### 2. 配置应用集成

#### 更新应用配置

在 `application-vault.yml` 中配置Vault连接：

```yaml
vault:
  enabled: true
  address: ${VAULT_ADDR:http://localhost:8200}
  role-id: ${VAULT_ROLE_ID}
  secret-id: ${VAULT_SECRET_ID}
  master-key-path: bankshield/encrypt/master-key
```

#### 设置环境变量

```bash
# 加载Vault凭据
source scripts/env/vault-credentials.env

# 或者手动设置
export VAULT_ADDR=http://localhost:8200
export VAULT_ROLE_ID="your-role-id"
export VAULT_SECRET_ID="your-secret-id"
```

### 3. 验证集成

#### 检查Vault状态

```bash
# 调用状态检查API
curl http://localhost:8080/api/vault/status

# 预期响应：
{
  "success": true,
  "data": {
    "vaultAddress": "localhost:8200",
    "status": "CONNECTED",
    "masterKeyAvailable": true,
    "timestamp": "2024-01-01T12:00:00"
  }
}
```

#### 测试密钥生成

```bash
# 生成新的加密密钥
curl -X POST "http://localhost:8080/api/vault/key/generate?algorithm=SM4&keyLength=128"

# 预期响应：加密的密钥材料
{
  "success": true,
  "data": "ENCRYPTED_..."
}
```

## 🔐 安全配置

### 1. Vault安全配置

#### 启用TLS（生产环境）

```hcl
# config.hcl
listener "tcp" {
  address       = "0.0.0.0:8200"
  tls_cert_file = "/path/to/server.crt"
  tls_key_file  = "/path/to/server.key"
}
```

#### 配置访问策略

```hcl
# bankshield-policy.hcl
path "bankshield/encrypt/*" {
  capabilities = ["read", "list"]
}

path "bankshield/encrypt/master-key" {
  capabilities = ["read"]
}
```

### 2. AppRole认证

#### 创建受限角色

```bash
# 创建具有严格限制的角色
vault write auth/approle/role/bankshield-api \
  secret_id_ttl=24h \
  token_num_uses=0 \
  token_ttl=1h \
  token_max_ttl=4h \
  secret_id_num_uses=40 \
  policies="bankshield-readonly"
```

#### 定期轮换凭据

```bash
# 生成新的SecretID
vault write -f auth/approle/role/bankshield-api/secret-id

# 更新应用配置
./scripts/security/update-app-config.sh prod <new-role-id> <new-secret-id>
```

## 📊 监控和审计

### 1. 启用Vault审计

```bash
# 启用文件审计后端
vault audit enable file file_path=/var/log/vault-audit.log

# 启用Syslog审计后端
vault audit enable syslog tag="vault"
```

### 2. 监控指标

#### Vault指标

- `vault_core_unsealed` - Vault是否已解封
- `vault_runtime_alloc_bytes` - 内存使用情况
- `vault_audit_log_request_failure` - 审计日志失败率

#### 应用指标

- `bankshield_vault_requests_total` - Vault请求总数
- `bankshield_vault_request_duration_seconds` - Vault请求延迟
- `bankshield_key_generation_total` - 密钥生成总数

### 3. 告警规则

```yaml
# Prometheus告警规则
- alert: VaultDown
  expr: up{job="vault"} == 0
  for: 5m
  labels:
    severity: critical
  annotations:
    summary: "Vault服务不可用"

- alert: VaultSealed
  expr: vault_core_unsealed == 0
  for: 1m
  labels:
    severity: critical
  annotations:
    summary: "Vault已密封"

- alert: HighVaultLatency
  expr: histogram_quantile(0.95, bankshield_vault_request_duration_seconds) > 1
  for: 5m
  labels:
    severity: warning
  annotations:
    summary: "Vault请求延迟过高"
```

## 🔄 密钥轮换

### 1. 自动轮换策略

#### 配置轮换策略

```bash
# 为密钥设置轮换策略
vault kv put bankshield/encrypt/master-key \
  key="${NEW_MASTER_KEY}" \
  algorithm="SM4" \
  length=256 \
  rotation_schedule="30d" \
  rotation_enabled=true
```

#### 轮换流程

```bash
#!/bin/bash
# 密钥轮换脚本

# 1. 生成新密钥
NEW_KEY=$(openssl rand -hex 32)

# 2. 重新加密现有密钥
# 3. 更新Vault中的主密钥
vault kv put bankshield/encrypt/master-key \
  key="$NEW_KEY" \
  algorithm="SM4" \
  length=256 \
  rotated_at=$(date -u +%Y-%m-%dT%H:%M:%SZ) \
  rotated_by="system"

# 4. 重启相关服务
kubectl rollout restart deployment/bankshield-api -n bankshield
```

## 🚨 故障排除

### 常见问题

#### 1. Vault连接失败

**症状**：应用无法连接到Vault

**解决方案**：
```bash
# 检查Vault状态
vault status

# 检查网络连接
telnet vault-host 8200

# 检查凭据
echo $VAULT_ROLE_ID
echo $VAULT_SECRET_ID
```

#### 2. 权限被拒绝

**症状**：403 Forbidden错误

**解决方案**：
```bash
# 检查AppRole权限
vault read auth/approle/role/bankshield-api

# 检查策略权限
vault policy read bankshield-readonly

# 检查令牌权限
vault token lookup
```

#### 3. 密钥解密失败

**症状**：无法解密密钥材料

**解决方案**：
```bash
# 检查主密钥完整性
vault kv get bankshield/encrypt/master-key

# 检查加密算法兼容性
# 验证密钥格式
```

## 🔒 安全最佳实践

### 1. 网络安全

- 使用TLS加密所有Vault通信
- 限制Vault管理接口的访问
- 使用网络分段隔离Vault服务

### 2. 访问控制

- 实施最小权限原则
- 定期轮换AppRole凭据
- 启用多因素认证（MFA）

### 3. 密钥管理

- 使用强随机密钥
- 实施密钥轮换策略
- 定期备份Vault数据

### 4. 监控和审计

- 启用全面的审计日志
- 设置实时告警
- 定期安全评估

## 📋 合规性

### 符合标准

- **PCI DSS**: 密钥安全管理
- **GDPR**: 数据保护和隐私
- **SOX**: 财务数据安全
- **ISO 27001**: 信息安全管理

### 审计要求

- 所有密钥操作都有审计日志
- 定期合规性检查
- 安全事件响应计划

## 🚀 性能优化

### 1. 缓存策略

```yaml
# 配置密钥缓存
bankshield:
  encrypt:
    key-cache:
      enabled: true
      max-size: 100
      ttl: 3600  # 1小时
```

### 2. 连接池优化

```yaml
# Vault连接配置
spring:
  vault:
    connection-timeout: 5000
    read-timeout: 15000
    session:
      lifecycle:
        enabled: true
        expiry-threshold: 30s
```

### 3. 批量操作

```java
// 批量密钥生成
public Result<List<String>> batchGenerateKeys(List<KeyGenerationRequest> requests) {
    // 实现批量处理逻辑
}
```

## 📚 相关文档

- [HashiCorp Vault官方文档](https://www.vaultproject.io/docs)
- [Spring Vault参考指南](https://docs.spring.io/spring-vault/docs/current/reference/html/)
- [BankShield安全架构](architecture/security-architecture.md)
- [密钥管理最佳实践](best-practices/key-management.md)

## 🔧 技术支持

### 获取帮助

1. **查看日志**：检查应用日志和Vault审计日志
2. **监控指标**：检查Prometheus指标
3. **社区支持**：HashiCorp社区论坛
4. **专业支持**：联系BankShield技术支持团队

### 升级指南

1. **备份数据**：升级前备份Vault数据
2. **测试环境**：先在测试环境验证
3. **滚动升级**：使用蓝绿部署策略
4. **回滚计划**：准备回滚方案

---

**安全提醒**：本指南中的配置和代码示例可能需要根据实际环境进行调整。在生产环境中部署前，请务必进行充分的安全评估和测试。