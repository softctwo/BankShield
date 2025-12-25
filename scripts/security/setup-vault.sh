#!/bin/bash

echo "🔐 部署HashiCorp Vault..."

# 创建Vault数据目录
mkdir -p /opt/vault/data
mkdir -p /opt/vault/config

# 创建Vault配置文件
cat > /opt/vault/config/config.hcl << 'EOF'
storage "file" {
  path = "/vault/data"
}

listener "tcp" {
  address     = "0.0.0.0:8200"
  tls_disable = "true"
}

ui = true

# 启用KV存储
# KV存储用于保存加密密钥
EOF

# 启动Vault容器
docker run -d \
  --name bankshield-vault \
  --restart unless-stopped \
  -p 8200:8200 \
  -v /opt/vault/config:/vault/config \
  -v /opt/vault/data:/vault/data \
  vault:latest

# 等待Vault启动
sleep 5

# 初始化Vault
echo "初始化Vault..."
docker exec bankshield-vault vault operator init -key-shares=5 -key-threshold=3 > /tmp/vault-init.txt

# 提取Root Token
ROOT_TOKEN=$(grep "Initial Root Token" /tmp/vault-init.txt | awk '{print $NF}')
echo "VAULT_ROOT_TOKEN=$ROOT_TOKEN" > /opt/vault/vault.env

export VAULT_ADDR=http://127.0.0.1:8200
export VAULT_TOKEN=$ROOT_TOKEN

# 启用KV存储引擎
echo "启用KV存储引擎..."
docker exec bankshield-vault vault secrets enable -path=bankshield/encrypt kv-v2

# 生成系统主密钥
echo "生成系统主密钥..."
MASTER_KEY=$(openssl rand -hex 32)

# 存储主密钥
docker exec bankshield-vault vault kv put bankshield/encrypt/master-key \
  key=$MASTER_KEY \
  algorithm="SM4" \
  length=256 \
  created_at=$(date -u +%Y-%m-%dT%H:%M:%SZ) \
  created_by="system"

# 创建只读策略
cat > /tmp/policy.hcl << EOF
path "bankshield/encrypt/*" {
  capabilities = ["read"]
}
EOF

docker exec bankshield-vault vault policy write bankshield-readonly /tmp/policy.hcl

# 创建AppRole
docker exec bankshield-vault vault auth enable approle

docker exec bankshield-vault vault write auth/approle/role/bankshield-api \
  secret_id_ttl=24h \
  token_num_uses=0 \
  token_ttl=1h \
  token_max_ttl=4h \
  secret_id_num_uses=40 \
  policies="bankshield-readonly"

# 获取RoleID
ROLE_ID=$(docker exec bankshield-vault vault read -field=role_id auth/approle/role/bankshield-api/role-id)
echo "VAULT_ROLE_ID=$ROLE_ID" >> /opt/vault/vault.env

# 生成SecretID
SECRET_ID=$(docker exec bankshield-vault vault write -f -field=secret_id auth/approle/role/bankshield-api/secret-id)
echo "VAULT_SECRET_ID=$SECRET_ID" >> /opt/vault/vault.env

echo "✅ Vault配置完成"
echo "   Root Token: $ROOT_TOKEN"
echo "   Role ID: $ROLE_ID"
echo "   Secret ID: $SECRET_ID"
echo "   Master Key已安全存储"