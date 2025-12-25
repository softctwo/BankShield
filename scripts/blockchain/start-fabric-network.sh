#!/bin/bash

# Hyperledger Fabric网络一键启动脚本

set -e

# 颜色定义
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${BLUE}╔════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║   BankShield Fabric 网络部署工具              ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════╝${NC}"
echo ""

# 配置
FABRIC_VERSION="2.4.7"
COMPOSE_FILE="docker/fabric/docker-compose.yaml"
CRYPTO_CONFIG_DIR="docker/fabric/crypto-config"
CHANNEL_NAME="bankshield-channel"

# 检查Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker未安装${NC}"
    exit 1
fi

# 检查Docker Compose
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}❌ Docker Compose未安装${NC}"
    exit 1
fi

# 生成证书
generate_certs() {
    echo -e "${YELLOW}🔐 生成证书和密钥...${NC}"
    
    if [ ! -f "$CRYPTO_CONFIG_DIR/crypto-config.yaml" ]; then
        echo "❌ 证书配置文件不存在"
        exit 1
    fi
    
    # 使用cryptogen生成证书
    if command -v cryptogen &> /dev/null; then
        cryptogen generate \
            --config=docker/fabric/crypto-config.yaml \
            --output=docker/fabric/crypto-config \
            2>&1 | grep -v "ESC"
        
        echo -e "${GREEN}✅ 证书生成完成${NC}"
    else
        echo -e "${YELLOW}⚠️  cryptogen未找到，使用预生成证书${NC}"
        mkdir -p $CRYPTO_CONFIG_DIR
    fi
}

# 生成创世区块
generate_genesis_block() {
    echo -e "${YELLOW}📦 生成创世区块...${NC}"
    
    # 检查configtxgen
    if ! command -v configtxgen &> /dev/null; then
        echo -e "${RED}❌ configtxgen未找到${NC}"
        exit 1
    fi
    
    # 生成创世区块
    configtxgen \
        -profile ThreeOrgsOrdererGenesis \
        -channelID system-channel \
        -outputBlock docker/fabric/system-genesis-block/genesis.block \
        -configPath docker/fabric \
        2>&1 | grep -v "ESC"
    
    # 生成通道交易
    configtxgen \
        -profile ThreeOrgsChannel \
        -outputCreateChannelTx docker/fabric/bankshield-channel.tx \
        -channelID $CHANNEL_NAME \
        -configPath docker/fabric \
        2>&1 | grep -v "ESC"
    
    echo -e "${GREEN}✅ 创世区块和通道交易生成完成${NC}"
}

# 启动网络
start_network() {
    echo -e "${YELLOW}🚀 启动Fabric网络...${NC}"
    
    # 创建必要的目录
    mkdir -p docker/fabric/{peer0.bankshield.internal,peer1.bankshield.internal,peer0.regulator.gov,peer1.regulator.gov,peer0.auditor.com,peer1.auditor.com,orderer.bankshield.com}
    mkdir -p docker/fabric/system-genesis-block
    
    # 启动Docker容器
    docker-compose -f $COMPOSE_FILE up -d \
        --timeout 300 \
        2>&1 | grep -v "ESC"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Fabric网络启动成功${NC}"
    else
        echo -e "${RED}❌ Fabric网络启动失败${NC}"
        exit 1
    fi
}

# 等待节点启动
wait_for_nodes() {
    echo -e "${YELLOW}⏳ 等待节点启动...${NC}"
    
    local max_attempts=60
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        local running_nodes=$(docker ps --filter "name=fabric" --filter "status=running" | grep -c "fabric")
        
        if [ $running_nodes -ge 7 ]; then
            echo -e "${GREEN}✅ 所有节点已启动${NC}"
            return 0
        fi
        
        echo -n "."
        sleep 5
        attempt=$((attempt + 1))
    done
    
    echo -e "${RED}❌ 等待节点启动超时${NC}"
    return 1
}

# 创建通道
create_channel() {
    echo -e "${YELLOW}📡 创建通道: $CHANNEL_NAME${NC}"
    
    # 设置环境变量
    export CORE_PEER_TLS_ENABLED=true
    export CORE_PEER_LOCALMSPID="BankShieldOrgMSP"
    export CORE_PEER_TLS_ROOTCERT_FILE="${PWD}/docker/fabric/crypto-config/peerOrganizations/bankshield.internal/peers/peer0.bankshield.internal/tls/ca.crt"
    export CORE_PEER_MSPCONFIGPATH="${PWD}/docker/fabric/crypto-config/peerOrganizations/bankshield.internal/users/Admin@bankshield.internal/msp"
    export CORE_PEER_ADDRESS="peer0.bankshield.internal:7051"
    
    # 创建通道
    peer channel create \
        -o orderer.bankshield.com:7050 \
        -c $CHANNEL_NAME \
        -f "${PWD}/docker/fabric/bankshield-channel.tx" \
        --outputBlock "${PWD}/docker/fabric/${CHANNEL_NAME}.block" \
        --tls \
        --cafile "${PWD}/docker/fabric/crypto-config/ordererOrganizations/bankshield.com/orderers/orderer.bankshield.com/msp/tlscacerts/tlsca.bankshield.com-cert.pem" \
        --connTimeout 300s \
        2>&1 | grep -v "ESC"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ 通道创建成功${NC}"
    else
        echo -e "${RED}❌ 通道创建失败${NC}"
        exit 1
    fi
}

# 组织加入通道
join_channel() {
    echo -e "${YELLOW}🔗 组织加入通道...${NC}"
    
    local orgs=(
        "bankshield.internal:BankShieldOrgMSP"
        "regulator.gov:RegulatorOrgMSP"
        "auditor.com:AuditorOrgMSP"
    )
    
    for org_info in "${orgs[@]}"; do
        IFS=':' read -r domain msp_id <<< "$org_info"
        
        echo -e "${BLUE}组织 $msp_id 加入通道...${NC}"
        
        export CORE_PEER_TLS_ENABLED=true
        export CORE_PEER_LOCALMSPID="$msp_id"
        export CORE_PEER_TLS_ROOTCERT_FILE="${PWD}/docker/fabric/crypto-config/peerOrganizations/${domain}/peers/peer0.${domain}/tls/ca.crt"
        export CORE_PEER_MSPCONFIGPATH="${PWD}/docker/fabric/crypto-config/peerOrganizations/${domain}/users/Admin@${domain}/msp"
        export CORE_PEER_ADDRESS="peer0.${domain}:$(echo $domain | grep -q 'bankshield' && echo '7051' || echo $domain | grep -q 'regulator' && echo '9051' || echo '10051')"
        
        peer channel join \
            -b "${PWD}/docker/fabric/${CHANNEL_NAME}.block" \
            --tls \
            --connTimeout 300s \
            2>&1 | grep -v "ESC"
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✅ $msp_id 加入通道成功${NC}"
        else
            echo -e "${RED}❌ $msp_id 加入通道失败${NC}"
        fi
    done
}

# 更新锚节点
update_anchor_peers() {
    echo -e "${YELLOW}⚓ 更新锚节点...${NC}"
    
    # 为每个组织更新锚节点
    for org in "bankshield.internal:BankShieldOrgMSP" "regulator.gov:RegulatorOrgMSP" "auditor.com:AuditorOrgMSP"; do
        IFS=':' read -r domain msp_id <<< "$org"
        
        export CORE_PEER_LOCALMSPID="$msp_id"
        export CORE_PEER_MSPCONFIGPATH="${PWD}/docker/fabric/crypto-config/peerOrganizations/${domain}/users/Admin@${domain}/msp"
        
        peer channel update \
            -o orderer.bankshield.com:7050 \
            -c $CHANNEL_NAME \
            -f "${PWD}/docker/fabric/${domain}/anchors.tx" \
            --tls \
            --cafile "${PWD}/docker/fabric/crypto-config/ordererOrganizations/bankshield.com/orderers/orderer.bankshield.com/msp/tlscacerts/tlsca.bankshield.com-cert.pem" \
            2>&1 | grep -v "ESC" || true
    done
    
    echo -e "${GREEN}✅ 锚节点更新完成${NC}"
}

# 部署链码
deploy_chaincodes() {
    echo -e "${YELLOW}📦 部署链码...${NC}"
    
    # 等待网络稳定
    sleep 10
    
    # 调用部署脚本
    if [ -f "./scripts/blockchain/deploy-chaincode.sh" ]; then
        ./scripts/blockchain/deploy-chaincode.sh
    else
        echo -e "${RED}❌ 部署脚本未找到${NC}"
    fi
}

# 显示网络状态
show_network_status() {
    echo ""
    echo -e "${BLUE}╔══════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║      Fabric 网络状态                             ║${NC}"
    echo -e "${BLUE}╚══════════════════════════════════════════════════╝${NC}"
    echo ""
    
    # 显示Docker容器
    echo "📦 Docker容器状态："
    docker ps --filter "name=fabric" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    
    echo ""
    echo "🔗 通道信息："
    peer channel getinfo -c $CHANNEL_NAME --connTimeout 300s 2>/dev/null || echo "  通道信息不可用"
    
    echo ""
    echo "📊 链码信息："
    peer lifecycle chaincode querycommitted --channelID $CHANNEL_NAME --connTimeout 300s --output json 2>/dev/null | jq '.' || echo "  链码信息不可用"
    
    echo ""
}

# 生成锚节点交易
generate_anchor_tx() {
    echo -e "${YELLOW}📄 生成锚节点交易...${NC}"
    
    for org in "bankshield.internal:BankShieldOrgMSP" "regulator.gov:RegulatorOrgMSP" "auditor.com:AuditorOrgMSP"; do
        IFS=':' read -r domain msp_id <<< "$org"
        
        configtxgen \
            -profile ${msp_id}Anchor \
            -outputAnchorPeersUpdate "${PWD}/docker/fabric/${domain}/anchors.tx" \
            -channelID $CHANNEL_NAME \
            -asOrg $msp_id \
            2>&1 | grep -v "ESC" || true
    done
    
    echo -e "${GREEN}✅ 锚节点交易生成完成${NC}"
}

# 主部署流程
main() {
    local step=1
    local total_steps=7
    
    echo -e "${BLUE}开始部署BankShield Fabric网络...${NC}"
    echo ""
    
    # 步骤1: 生成证书
    echo -e "${BLUE}[步骤 $step/$total_steps]${NC} 生成证书"
    generate_certs
    ((step++))
    
    # 步骤2: 生成创世区块
    echo -e "${BLUE}[步骤 $step/$total_steps]${NC} 生成创世区块"
    generate_genesis_block
    ((step++))
    
    # 步骤3: 生成锚节点交易
    echo -e "${BLUE}[步骤 $step/$total_steps]${NC} 生成锚节点交易"
    generate_anchor_tx
    ((step++))
    
    # 步骤4: 启动网络
    echo -e "${BLUE}[步骤 $step/$total_steps]${NC} 启动网络"
    start_network
    ((step++))
    
    # 步骤5: 等待节点启动
    echo -e "${BLUE}[步骤 $step/$total_steps]${NC} 等待节点启动"
    wait_for_nodes
    ((step++))
    
    # 步骤6: 创建和加入通道
    echo -e "${BLUE}[步骤 $step/$total_steps]${NC} 创建和加入通道"
    create_channel
    join_channel
    update_anchor_peers
    ((step++))
    
    # 步骤7: 部署链码
    echo -e "${BLUE}[步骤 $step/$total_steps]${NC} 部署链码"
    deploy_chaincodes
    
    # 显示最终状态
    show_network_status
    
    echo ""
    echo -e "${GREEN}╔════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║        ✅ Fabric网络部署成功！                  ║${NC}"
    echo -e "${GREEN}╚════════════════════════════════════════════════╝${NC}"
    echo ""
    echo "📌 后续操作："
    echo "   1. 查看监控: ./scripts/blockchain/monitor.sh"
    echo "   2. 测试链码: ./scripts/blockchain/test-chaincode.sh"
    echo "   3. 查看日志: docker logs -f peer0.bankshield.internal"
    echo ""
}

# 处理命令行参数
case "${1:-all}" in
    all)
        main
        ;;
    certs)
        generate_certs
        ;;
    genesis)
        generate_genesis_block
        ;;
    start)
        start_network
        ;;
    channel)
        create_channel
        join_channel
        ;;
    chaincode)
        deploy_chaincodes
        ;;
    status)
        show_network_status
        ;;
    *)
        echo "用法: $0 {all|certs|genesis|start|channel|chaincode|status}"
        exit 1
        ;;
esac
