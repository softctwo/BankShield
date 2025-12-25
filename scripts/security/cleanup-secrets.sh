#!/bin/bash

# BankShield安全清理脚本
# 用于清理开发环境中的敏感信息

set -e

echo "🔒 BankShield安全清理脚本"
echo "=========================="
echo ""

# 清理函数
cleanup_files() {
    echo "🗑️  清理敏感文件..."
    
    # 清理临时文件
    rm -f /tmp/vault-init.txt
    rm -f /tmp/policy.hcl
    
    # 清理测试密钥文件
    find . -name "*.key" -type f -delete 2>/dev/null || true
    find . -name "*.pem" -type f -delete 2>/dev/null || true
    find . -name "*.p12" -type f -delete 2>/dev/null || true
    
    # 清理日志文件中的敏感信息
    find logs/ -name "*.log" -type f -exec sed -i 's/[a-f0-9]\{32,\}/[REDACTED]/g' {} \; 2>/dev/null || true
    
    echo "✅ 临时文件清理完成"
}

cleanup_env_vars() {
    echo "🧹 清理环境变量..."
    
    # 清理当前会话中的敏感环境变量
    unset VAULT_ROOT_TOKEN
    unset VAULT_ROLE_ID
    unset VAULT_SECRET_ID
    unset ENCRYPT_MASTER_KEY
    unset JWT_SECRET
    
    # 清理.env文件
    rm -f .env.vault
    rm -f .env.local
    rm -f .env.secrets
    
    echo "✅ 环境变量清理完成"
}

cleanup_docker() {
    echo "🐳 清理Docker容器和镜像..."
    
    # 停止并删除Vault容器
    if docker ps -a | grep -q "bankshield-vault"; then
        echo "停止Vault容器..."
        docker stop bankshield-vault 2>/dev/null || true
        docker rm bankshield-vault 2>/dev/null || true
    fi
    
    # 清理未使用的镜像
    docker image prune -f > /dev/null 2>&1 || true
    
    echo "✅ Docker清理完成"
}

cleanup_database() {
    echo "🗄️  清理数据库中的测试数据..."
    
    # 检查MySQL连接
    if command -v mysql &> /dev/null; then
        read -p "是否清理数据库中的测试密钥？(y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            # 这里可以添加数据库清理SQL脚本
            echo "⚠️  数据库清理需要手动执行，请运行相应的SQL脚本"
        fi
    fi
    
    echo "✅ 数据库清理检查完成"
}

secure_wipe() {
    echo "🛡️  安全擦除敏感文件..."
    
    # 查找包含敏感信息的文件
    local sensitive_files=(
        "vault.env"
        "*.key"
        "*.pem"
        "*.p12"
        "*secret*"
        "*credential*"
    )
    
    for pattern in "${sensitive_files[@]}"; do
        find . -name "$pattern" -type f -print0 2>/dev/null | while IFS= read -r -d '' file; do
            echo "安全擦除: $file"
            
            # 使用随机数据覆盖文件内容
            if command -v shred &> /dev/null; then
                shred -vfz -n 3 "$file" 2>/dev/null || rm -f "$file"
            else
                # 如果没有shred命令，使用dd命令
                dd if=/dev/urandom of="$file" bs=1M count=1 conv=notrunc 2>/dev/null || true
                rm -f "$file"
            fi
        done
    done
    
    echo "✅ 安全擦除完成"
}

generate_security_report() {
    echo "📋 生成安全报告..."
    
    local report_file="security-cleanup-report-$(date +%Y%m%d-%H%M%S).txt"
    
    cat > "$report_file" << EOF
BankShield安全清理报告
========================

清理时间: $(date)
执行用户: $(whoami)
工作目录: $(pwd)

清理操作:
✅ 临时文件清理
✅ 环境变量清理
✅ Docker容器清理
✅ 敏感文件安全擦除

剩余安全检查:
- 检查Git历史是否包含敏感信息
- 检查日志文件是否包含密钥
- 检查配置文件安全性
- 验证Vault配置

建议:
1. 运行Git泄露检查: git log --all --full-history -- .env*
2. 检查配置文件: grep -r "password\|secret\|key" --exclude-dir=.git .
3. 验证Vault密封状态: vault status
4. 检查文件权限: find . -type f -perm -002

EOF
    
    echo "✅ 安全报告已生成: $report_file"
}

check_git_history() {
    echo "🔍 检查Git历史中的敏感信息..."
    
    # 检查常见的敏感文件模式
    local sensitive_patterns=(
        "password"
        "secret"
        "key.*="
        "token"
        "credential"
        "vault.*token"
        "private.*key"
    )
    
    echo "搜索Git历史中的潜在敏感信息..."
    for pattern in "${sensitive_patterns[@]}"; do
        echo "搜索模式: $pattern"
        git log --all --full-history -S "$pattern" --oneline 2>/dev/null | head -10
    done
    
    echo "⚠️  如果发现敏感信息，请考虑使用git-filter-branch或BFG工具清理历史"
}

# 主执行流程
main() {
    echo "🔒 BankShield安全清理脚本"
    echo "=========================="
    echo ""
    
    read -p "此操作将清理敏感信息，是否继续？(y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "操作已取消"
        exit 0
    fi
    
    echo "开始安全清理..."
    echo ""
    
    # 1. 清理文件
    cleanup_files
    echo ""
    
    # 2. 清理环境变量
    cleanup_env_vars
    echo ""
    
    # 3. 清理Docker
    cleanup_docker
    echo ""
    
    # 4. 清理数据库（可选）
    cleanup_database
    echo ""
    
    # 5. 安全擦除
    secure_wipe
    echo ""
    
    # 6. 生成报告
    generate_security_report
    echo ""
    
    # 7. Git历史检查
    check_git_history
    echo ""
    
    echo "🎉 安全清理完成！"
    echo ""
    echo "🔍 建议后续操作："
    echo "  1. 检查生成的安全报告"
    echo "  2. 验证应用功能是否正常"
    echo "  3. 重新配置必要的安全凭据"
    echo "  4. 运行安全扫描工具"
    echo ""
    echo "⚠️  重要提醒："
    echo "  - 清理的凭据需要重新配置"
    echo "  - 检查应用配置文件"
    echo "  - 验证数据库连接"
    echo "  - 测试所有功能模块"
}

# 错误处理
trap 'echo "❌ 清理过程中发生错误"; exit 1' ERR

# 执行主函数
main "$@"