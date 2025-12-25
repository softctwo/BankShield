#!/bin/bash

# 更新应用的Vault配置
ENV=$1
ROLE_ID=$2
SECRET_ID=$3

if [ -z "$ENV" ] || [ -z "$ROLE_ID" ] || [ -z "$SECRET_ID" ]; then
    echo "Usage: $0 <env> <role_id> <secret_id>"
    echo "Example: $0 prod your-role-id your-secret-id"
    exit 1
fi

echo "🔐 更新BankShield应用的Vault配置..."
echo "环境: $ENV"
echo "Role ID: ${ROLE_ID:0:8}..."
echo "Secret ID: ${SECRET_ID:0:8}..."

# 创建Kubernetes Secret（如果使用K8s）
if command -v kubectl &> /dev/null; then
    echo "创建Kubernetes Secret..."
    kubectl create secret generic bankshield-vault-credentials \
      --from-literal=role-id=$ROLE_ID \
      --from-literal=secret-id=$SECRET_ID \
      -n bankshield-$ENV \
      --dry-run=client -o yaml | kubectl apply -f -
    
    echo "✅ Kubernetes Secret已更新"
fi

# 更新Docker环境变量文件
cat > .env.vault << EOF
# Vault配置 - 由update-app-config.sh自动生成
VAULT_ADDR=http://vault:8200
VAULT_ROLE_ID=$ROLE_ID
VAULT_SECRET_ID=$SECRET_ID
VAULT_ENABLED=true
EOF

echo "✅ Docker环境变量文件已更新"

# 更新系统环境变量（可选）
if [ "$ENV" = "prod" ]; then
    echo "⚠️  生产环境警告："
    echo "   请确保这些凭据已安全地存储在："
    echo "   1. 密钥管理系统（如AWS Secrets Manager、Azure Key Vault）"
    echo "   2. 容器编排平台的Secret管理"
    echo "   3. 硬件安全模块（HSM）"
fi

echo "✅ Vault凭据更新完成"
echo "   请重启相关服务以应用新的配置"