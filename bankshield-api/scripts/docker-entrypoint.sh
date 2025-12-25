#!/bin/bash

# BankShield API Docker入口脚本

set -e

echo "🔐 BankShield API 启动中..."

# 等待Vault服务可用
if [ "$VAULT_ENABLED" = "true" ] && [ -n "$VAULT_ADDR" ]; then
    echo "等待Vault服务连接: $VAULT_ADDR"
    
    timeout=60
    while [ $timeout -gt 0 ]; do
        if vault status >/dev/null 2>&1; then
            echo "✅ Vault连接成功"
            break
        fi
        echo "等待Vault启动... ($timeout 秒剩余)"
        sleep 2
        timeout=$((timeout - 2))
    done
    
    if [ $timeout -eq 0 ]; then
        echo "⚠️  警告：无法连接到Vault服务，将继续启动但Vault功能将不可用"
        export VAULT_ENABLED=false
    fi
    
    # 验证Vault凭据
    if [ -n "$VAULT_ROLE_ID" ] && [ -n "$VAULT_SECRET_ID" ]; then
        echo "正在验证Vault凭据..."
        if vault write auth/approle/login role_id=$VAULT_ROLE_ID secret_id=$VAULT_SECRET_ID >/dev/null 2>&1; then
            echo "✅ Vault凭据验证成功"
        else
            echo "⚠️  警告：Vault凭据无效，Vault功能将不可用"
            export VAULT_ENABLED=false
        fi
    else
        echo "⚠️  警告：未提供Vault凭据，Vault功能将不可用"
        export VAULT_ENABLED=false
    fi
fi

# 显示配置信息（脱敏）
echo "应用配置："
echo "  Vault集成: ${VAULT_ENABLED:-false}"
echo "  Vault地址: ${VAULT_ADDR:-未配置}"
echo "  Role ID: ${VAULT_ROLE_ID:0:8}...（如果已配置）"

# 执行原始命令
exec "$@"