#!/bin/bash

# Fabric链码部署脚本
# 一键部署所有智能合约到Hyperledger Fabric网络

set -e

# 配置
CHANNEL_NAME="bankshield-channel"
CHAINCODE_DIR="/Users/zhangyanlong/workspaces/BankShield/bankshield-blockchain/chaincode"
ORG_NAME="BankShieldOrg"
PEER_ADDRESS="peer0.bankshield.internal:7051"
ORDERER_ADDRESS="orderer.bankshield.com:7050"

COLLECTIONS_CONFIG="$CHAINCODE_DIR/collections-config.json"
ENDORSEMENT_POLICY="$CHAINCODE_DIR/endorsement-policy.json"

echo "=========================================="
echo "  BankShield 区块链链码部署工具"
echo "=========================================="
echo ""

# 检查环境
check_env() {
    echo "⚙️  检查环境..."
    
    if ! command -v peer &> /dev/null; then
        echo "❌ peer命令未找到，请确保Fabric工具已安装"
        exit 1
    fi
    
    if [ ! -d "$CHAINCODE_DIR" ]; then
        echo "❌ 链码目录不存在: $CHAINCODE_DIR"
        exit 1
    fi
    
    echo "✅ 环境检查通过"
    echo ""
}

# 打包链码
package_chaincode() {
    local chaincode_name=$1
    local chaincode_version=$2
    
    echo "📦 打包链码: $chaincode_name:$chaincode_version"
    
    cd "$CHAINCODE_DIR"
    
    sed -i '' "s/package main/package main/g" "${chaincode_name}_anchor.go" 2>/dev/null || true
    
    peer lifecycle chaincode package ${chaincode_name}.tar.gz \
        --path "$CHAINCODE_DIR" \
        --lang golang \
        --label "${chaincode_name}_${chaincode_version}" \
        --connectionTimeout 300s \
        2>&1 | grep -v "ESC"
    
    if [ $? -eq 0 ]; then
        echo "✅ 链码打包成功: ${chaincode_name}.tar.gz"
    else
        echo "❌ 链码打包失败"
        exit 1
    fi
}

# 安装链码
install_chaincode() {
    local chaincode_name=$1
    local chaincode_version=$2
    
    echo "📥 安装链码: $chaincode_name"
    
    export CORE_PEER_TLS_ENABLED=true
    export CORE_PEER_LOCALMSPID="BankShieldOrgMSP"
    export CORE_PEER_TLS_ROOTCERT_FILE="${CRYPTO_CONFIG}/peerOrganizations/bankshield.internal/peers/peer0.bankshield.internal/tls/ca.crt"
    export CORE_PEER_MSPCONFIGPATH="${CRYPTO_CONFIG}/peerOrganizations/bankshield.internal/users/Admin@bankshield.internal/msp"
    export CORE_PEER_ADDRESS="$PEER_ADDRESS"
    
    peer lifecycle chaincode install ${chaincode_name}.tar.gz \
        --connTimeout 300s \
        2>&1
    
    if [ $? -eq 0 ]; then
        echo "✅ 链码安装成功: $chaincode_name"
    else
        echo "❌ 链码安装失败"
        exit 1
    fi
}

# 查询安装的链码
query_installed() {
    local chaincode_name=$1
    
    echo "🔍 查询已安装链码: $chaincode_name"
    
    peer lifecycle chaincode queryinstalled \
        --connTimeout 300s \
        --output json \
        2>&1 | jq -r '.installed_chaincodes[] | select(.label=="'"${chaincode_name}_1.0"'") | .package_id' \
        2>/dev/null
}

# 批准链码
approve_chaincode() {
    local chaincode_name=$1
    local package_id=$2
    
    echo "✅ 批准链码: $chaincode_name (Package: $package_id)"
    
    # BankShield组织批准
    peer lifecycle chaincode approveformyorg \
        --channelID "$CHANNEL_NAME" \
        --name "$chaincode_name" \
        --version "1.0" \
        --package-id "$package_id" \
        --sequence 1 \
        --tls \
        --cafile "${CRYPTO_CONFIG}/ordererOrganizations/bankshield.com/orderers/orderer.bankshield.com/msp/tlscacerts/tlsca.bankshield.com-cert.pem" \
        --connTimeout 300s \
        2>&1
    
    if [ $? -eq 0 ]; then
        echo "✅ 链码批准成功: $chaincode_name"
    else
        echo "❌ 链码批准失败"
        exit 1
    fi
}

# 提交链码
commit_chaincode() {
    local chaincode_name=$1
    
    echo "📝 提交链码: $chaincode_name"
    
    # 收集所有组织的Peer
    local peer_addresses=(
        "--peerAddresses peer0.bankshield.internal:7051"
        "--peerAddresses peer0.regulator.gov:9051"
        "--peerAddresses peer0.auditor.com:10051"
    )
    
    local tls_rootcerts=(
        "--tlsRootCertFiles ${CRYPTO_CONFIG}/peerOrganizations/bankshield.internal/peers/peer0.bankshield.internal/tls/ca.crt"
        "--tlsRootCertFiles ${CRYPTO_CONFIG}/peerOrganizations/regulator.gov/peers/peer0.regulator.gov/tls/ca.crt"
        "--tlsRootCertFiles ${CRYPTO_CONFIG}/peerOrganizations/auditor.com/peers/peer0.auditor.com/tls/ca.crt"
    )
    
    peer lifecycle chaincode commit \
        --channelID "$CHANNEL_NAME" \
        --name "$chaincode_name" \
        --version "1.0" \
        --sequence 1 \
        --tls \
        --cafile "${CRYPTO_CONFIG}/ordererOrganizations/bankshield.com/orderers/orderer.bankshield.com/msp/tlscacerts/tlsca.bankshield.com-cert.pem" \
        "${peer_addresses[@]}" \
        "${tls_rootcerts[@]}" \
        --connTimeout 300s \
        2>&1
    
    if [ $? -eq 0 ]; then
        echo "✅ 链码提交成功: $chaincode_name"
    else
        echo "❌ 链码提交失败"
        exit 1
    fi
}

# 初始化链码
init_chaincode() {
    local chaincode_name=$1
    
    echo "🔧 初始化链码: $chaincode_name"
    
    peer chaincode invoke \
        -o "$ORDERER_ADDRESS" \
        -C "$CHANNEL_NAME" \
        -n "$chaincode_name" \
        --tls \
        --cafile "${CRYPTO_CONFIG}/ordererOrganizations/bankshield.com/orderers/orderer.bankshield.com/msp/tlscacerts/tlsca.bankshield.com-cert.pem" \
        -c '{"function":"Init","Args":[]}' \
        --connTimeout 300s \
        2>&1
    
    if [ $? -eq 0 ]; then
        echo "✅ 链码初始化成功: $chaincode_name"
    else
        echo "❌ 链码初始化失败"
    fi
}

# 部署单个链码
deploy_single_chaincode() {
    local chaincode_name=$1
    local chaincode_version=$2
    
    echo "=========================================="
    echo "部署链码: $chaincode_name"
    echo "=========================================="
    
    package_chaincode "$chaincode_name" "$chaincode_version"
    install_chaincode "$chaincode_name" "$chaincode_version"
    
    PACKAGE_ID=$(query_installed "$chaincode_name")
    if [ -z "$PACKAGE_ID" ]; then
        echo "❌ 无法获取Package ID"
        exit 1
    fi
    
    approve_chaincode "$chaincode_name" "$PACKAGE_ID"
    commit_chaincode "$chaincode_name"
    init_chaincode "$chaincode_name"
    
    echo ""
}

# 部署所有链码
deploy_all_chaincodes() {
    echo "🚀 开始部署所有链码..."
    echo ""
    
    # 定义要部署的链码
    local chaincodes=(
        "audit_anchor"
        "key_rotation_anchor"
        "permission_change_anchor"
        "data_access_anchor"
    )
    
    for cc in "${chaincodes[@]}"; do
        deploy_single_chaincode "$cc" "1.0"
    done
    
    echo "=========================================="
    echo "✅ 所有链码部署完成！"
    echo "=========================================="
}

# 查询已部署链码
query_deployed_chaincodes() {
    echo "🔍 查询已部署链码..."
    
    peer lifecycle chaincode querycommitted \
        --channelID "$CHANNEL_NAME" \
        --connTimeout 300s \
        --output json \
        2>&1 | jq '.'
}

# 测试链码
test_chaincode() {
    local chaincode_name=$1
    
    echo "🧪 测试链码: $chaincode_name"
    
    local test_data='{"blockId":"TEST_BLOCK_001","merkleRoot":"abcd1234efgh5678","transactionCount":100}'
    
    peer chaincode invoke \
        -o "$ORDERER_ADDRESS" \
        -C "$CHANNEL_NAME" \
        -n "$chaincode_name" \
        --tls \
        --cafile "${CRYPTO_CONFIG}/ordererOrganizations/bankshield.com/orderers/orderer.bankshield.com/msp/tlscacerts/tlsca.bankshield.com-cert.pem" \
        -c "{\"function\":\"CreateAuditAnchor\",\"Args\":[\"TEST_BLOCK_001\",\"abcd1234efgh5678\",\"100\",\"{}\"]}" \
        --connTimeout 300s \
        2>&1
    
    if [ $? -eq 0 ]; then
        echo "✅ 链码测试成功: $chaincode_name"
        
        # 查询测试
        peer chaincode query \
            -C "$CHANNEL_NAME" \
            -n "$chaincode_name" \
            -c '{"function":"QueryAuditBlock","Args":["TEST_BLOCK_001"]}' \
            --connTimeout 300s \
            2>&1 | jq '.'
    else
        echo "❌ 链码测试失败"
    fi
}

# 主菜单
show_menu() {
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "  BankShield 区块链链码部署工具"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "1) 部署所有链码"
    echo "2) 部署单个链码"
    echo "3) 查询已部署链码"
    echo "4) 测试链码"
    echo "5) 显示帮助"
    echo "6) 退出"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
}

# 主函数
main() {
    # 加载环境变量
    if [ -f ".env" ]; then
        source .env
    fi
    
    # 检查必需变量
    if [ -z "$CRYPTO_CONFIG" ]; then
        export CRYPTO_CONFIG="${PWD}/docker/fabric/crypto-config"
    fi
    
    check_env
    
    while true; do
        show_menu
        read -p "请选择操作 [1-6]: " choice
        
        case $choice in
            1)
                deploy_all_chaincodes
                ;;
            2)
                read -p "请输入链码名称: " cc_name
                deploy_single_chaincode "$cc_name" "1.0"
                ;;
            3)
                query_deployed_chaincodes
                ;;
            4)
                read -p "请输入链码名称: " cc_name
                test_chaincode "$cc_name"
                ;;
            5)
                echo ""
                echo "📖 帮助信息："
                echo "1. 确保Fabric网络已启动"
                echo "2. 确保peer命令可用"
                echo "3. 确保有足够的权限"
                echo "4. 查看日志排查问题"
                echo ""
                ;;
            6)
                echo "👋 再见！"
                exit 0
                ;;
            *)
                echo "❌ 无效选择"
                ;;
        esac
    done
}

# 执行主函数
main "$@"
