#!/bin/bash

# BankShield Vault集成部署脚本

set -e

echo "🔐 BankShield Vault集成部署开始"

# 配置参数
ENV=${1:-dev}
VAULT_HOST=${2:-localhost}
VAULT_PORT=${3:-8200}

echo "环境: $ENV"
echo "Vault地址: $VAULT_HOST:$VAULT_PORT"

# 1. 检查依赖
check_dependencies() {
    echo "🔍 检查依赖项..."
    
    local missing_deps=()
    
    if ! command -v docker &> /dev/null; then
        missing_deps+=("docker")
    fi
    
    if ! command -v openssl &> /dev/null; then
        missing_deps+=("openssl")
    fi
    
    if ! command -v curl &> /dev/null; then
        missing_deps+=("curl")
    fi
    
    if [ ${#missing_deps[@]} -ne 0 ]; then
        echo "❌ 缺少依赖项: ${missing_deps[*]}"
        echo "请安装缺失的依赖项后重试"
        exit 1
    fi
    
    echo "✅ 所有依赖项检查通过"
}

# 2. 部署Vault
deploy_vault() {
    echo "🚀 部署HashiCorp Vault..."
    
    # 检查Vault是否已在运行
    if curl -s "http://$VAULT_HOST:$VAULT_PORT/v1/sys/health" > /dev/null 2>&1; then
        echo "⚠️  Vault似乎已在运行，跳过部署步骤"
        return 0
    fi
    
    # 使用Docker Compose部署
    if [ -f "docker/vault/docker-compose.yml" ]; then
        cd docker/vault
        docker-compose up -d
        cd ../..
        
        # 等待Vault启动
        echo "等待Vault启动..."
        local retries=30
        while [ $retries -gt 0 ]; do
            if curl -s "http://$VAULT_HOST:$VAULT_PORT/v1/sys/health" > /dev/null 2>&1; then
                echo "✅ Vault启动成功"
                break
            fi
            sleep 2
            retries=$((retries - 1))
        done
        
        if [ $retries -eq 0 ]; then
            echo "❌ Vault启动超时"
            exit 1
        fi
    else
        echo "使用部署脚本启动Vault..."
        ./scripts/security/setup-vault.sh
    fi
}

# 3. 初始化Vault
initialize_vault() {
    echo "🔑 初始化Vault..."
    
    export VAULT_ADDR="http://$VAULT_HOST:$VAULT_PORT"
    
    # 检查是否已初始化
    if vault status 2>/dev/null | grep -q "Initialized.*true"; then
        echo "⚠️  Vault已初始化，跳过初始化步骤"
        
        # 获取现有的root token
        if [ -f "/opt/vault/vault.env" ]; then
            source /opt/vault/vault.env
            export VAULT_TOKEN=$VAULT_ROOT_TOKEN
        fi
        return 0
    fi
    
    # 初始化Vault
    echo "正在初始化Vault..."
    vault operator init -key-shares=5 -key-threshold=3 > /tmp/vault-init.txt
    
    # 提取关键信息
    ROOT_TOKEN=$(grep "Initial Root Token" /tmp/vault-init.txt | awk '{print $NF}')
    UNSEAL_KEYS=$(grep "Unseal Key" /tmp/vault-init.txt | awk '{print $NF}')
    
    echo "VAULT_ROOT_TOKEN=$ROOT_TOKEN" > /opt/vault/vault.env
    echo "$UNSEAL_KEYS" > /opt/vault/unseal.keys
    
    export VAULT_TOKEN=$ROOT_TOKEN
    
    echo "✅ Vault初始化完成"
    echo "⚠️  请安全保存以下信息："
    echo "   Root Token: $ROOT_TOKEN"
    echo "   Unseal Keys: 已保存到 /opt/vault/unseal.keys"
}

# 4. 配置Vault
configure_vault() {
    echo "⚙️  配置Vault..."
    
    export VAULT_ADDR="http://$VAULT_HOST:$VAULT_PORT"
    export VAULT_TOKEN=$VAULT_ROOT_TOKEN
    
    # 解封Vault（如果需要）
    if vault status 2>/dev/null | grep -q "Sealed.*true"; then
        echo "正在解封Vault..."
        local unseal_key=$(head -n1 /opt/vault/unseal.keys)
        vault operator unseal "$unseal_key"
    fi
    
    # 启用KV存储引擎
    if ! vault secrets list | grep -q "bankshield/"; then
        echo "启用KV存储引擎..."
        vault secrets enable -path=bankshield/encrypt kv-v2
    fi
    
    # 生成系统主密钥
    echo "生成系统主密钥..."
    MASTER_KEY=$(openssl rand -hex 32)
    
    # 存储主密钥
    vault kv put bankshield/encrypt/master-key \
        key=$MASTER_KEY \
        algorithm="SM4" \
        length=256 \
        created_at=$(date -u +%Y-%m-%dT%H:%M:%SZ) \
        created_by="system"
    
    # 创建只读策略
    cat > /tmp/policy.hcl << 'EOF'
path "bankshield/encrypt/*" {
  capabilities = ["read"]
}
EOF
    vault policy write bankshield-readonly /tmp/policy.hcl
    
    # 创建AppRole
    if ! vault auth list | grep -q "approle/"; then
        vault auth enable approle
    fi
    
    vault write auth/approle/role/bankshield-api \
        secret_id_ttl=24h \
        token_num_uses=0 \
        token_ttl=1h \
        token_max_ttl=4h \
        secret_id_num_uses=40 \
        policies="bankshield-readonly"
    
    # 获取RoleID和SecretID
    ROLE_ID=$(vault read -field=role_id auth/approle/role/bankshield-api/role-id)
    SECRET_ID=$(vault write -f -field=secret_id auth/approle/role/bankshield-api/secret-id)
    
    echo "VAULT_ROLE_ID=$ROLE_ID" >> /opt/vault/vault.env
    echo "VAULT_SECRET_ID=$SECRET_ID" >> /opt/vault/vault.env
    
    echo "✅ Vault配置完成"
}

# 5. 配置应用
configure_application() {
    echo "🔧 配置BankShield应用..."
    
    # 加载Vault凭据
    source /opt/vault/vault.env
    
    # 更新应用配置
    ./scripts/security/update-app-config.sh "$ENV" "$VAULT_ROLE_ID" "$VAULT_SECRET_ID"
    
    # 设置环境变量
    cat > .env.vault << EOF
# Vault集成配置
VAULT_ADDR=http://$VAULT_HOST:$VAULT_PORT
VAULT_ROLE_ID=$VAULT_ROLE_ID
VAULT_SECRET_ID=$VAULT_SECRET_ID
VAULT_ENABLED=true
VAULT_MASTER_KEY_PATH=bankshield/encrypt/master-key
EOF
    
    echo "✅ 应用配置更新完成"
}

# 6. 验证集成
validate_integration() {
    echo "🧪 验证Vault集成..."
    
    # 等待应用启动（如果正在运行）
    sleep 10
    
    # 检查Vault状态
    echo "检查Vault状态..."
    if curl -s "http://$VAULT_HOST:$VAULT_PORT/v1/sys/health" > /dev/null 2>&1; then
        echo "✅ Vault服务正常运行"
    else
        echo "❌ Vault服务不可用"
        return 1
    fi
    
    # 验证AppRole认证
    echo "验证AppRole认证..."
    AUTH_RESPONSE=$(curl -s -X POST \
        "http://$VAULT_HOST:$VAULT_PORT/v1/auth/approle/login" \
        -d "{\"role_id\":\"$VAULT_ROLE_ID\",\"secret_id\":\"$VAULT_SECRET_ID\"}")
    
    if echo "$AUTH_RESPONSE" | grep -q "auth.*client_token"; then
        echo "✅ AppRole认证成功"
    else
        echo "❌ AppRole认证失败"
        echo "响应: $AUTH_RESPONSE"
        return 1
    fi
    
    # 验证密钥访问
    echo "验证密钥访问..."
    CLIENT_TOKEN=$(echo "$AUTH_RESPONSE" | jq -r .auth.client_token 2>/dev/null || echo "")
    
    if [ -n "$CLIENT_TOKEN" ]; then
        KEY_RESPONSE=$(curl -s -H "X-Vault-Token: $CLIENT_TOKEN" \
            "http://$VAULT_HOST:$VAULT_PORT/v1/bankshield/encrypt/data/master-key")
        
        if echo "$KEY_RESPONSE" | grep -q "data.*key"; then
            echo "✅ 主密钥访问成功"
        else
            echo "❌ 主密钥访问失败"
            echo "响应: $KEY_RESPONSE"
            return 1
        fi
    fi
    
    echo "✅ Vault集成验证通过"
}

# 7. 显示部署结果
show_deployment_summary() {
    echo ""
    echo "🎉 BankShield Vault集成部署完成！"
    echo "=================================="
    echo ""
    echo "📋 部署摘要："
    echo "  环境: $ENV"
    echo "  Vault地址: http://$VAULT_HOST:$VAULT_PORT"
    echo "  Vault UI: http://$VAULT_HOST:$VAULT_PORT/ui"
    echo ""
    echo "🔑 访问凭据："
    echo "  Role ID: ${VAULT_ROLE_ID:0:8}..."
    echo "  Secret ID: ${VAULT_SECRET_ID:0:8}..."
    echo ""
    echo "📁 配置文件："
    echo "  Vault环境: /opt/vault/vault.env"
    echo "  应用环境: .env.vault"
    echo ""
    echo "🔧 下一步操作："
    echo "  1. 启动BankShield应用"
    echo "  2. 验证Vault集成状态"
    echo "  3. 配置监控和告警"
    echo ""
    echo "⚠️  安全提醒："
    echo "  - 请妥善保管Vault的Root Token和Unseal Keys"
    echo "  - 生产环境中应启用TLS加密"
    echo "  - 定期轮换AppRole凭据"
    echo "  - 配置适当的网络访问控制"
    echo ""
}

# 主执行流程
main() {
    echo "🔐 BankShield Vault集成部署脚本"
    echo "================================="
    echo ""
    
    # 1. 检查依赖
    check_dependencies
    
    # 2. 部署Vault
    deploy_vault
    
    # 3. 初始化Vault
    initialize_vault
    
    # 4. 配置Vault
    configure_vault
    
    # 5. 配置应用
    configure_application
    
    # 6. 验证集成
    validate_integration
    
    # 7. 显示结果
    show_deployment_summary
}

# 错误处理
trap 'echo "❌ 部署过程中发生错误，请检查日志"; exit 1' ERR

# 执行主函数
main "$@"