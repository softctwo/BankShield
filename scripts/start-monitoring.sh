#!/bin/bash

# BankShield 监控告警系统启动脚本
# Author: BankShield Team
# Version: 1.0.0

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 显示使用说明
show_usage() {
    cat << EOF
BankShield 监控告警系统启动脚本

使用方法: $0 [选项]

选项:
    --basic           启动基础监控组件 (Prometheus + Grafana + AlertManager)
    --full            启动完整监控栈 (包含所有组件)
    --stop            停止监控服务
    --restart         重启监控服务
    --status          查看服务状态
    --logs [service]  查看指定服务日志
    --backup          备份监控数据
    --restore         恢复监控数据
    --clean           清理监控数据
    --help            显示帮助信息

示例:
    $0 --basic        # 启动基础监控
    $0 --full         # 启动完整监控栈
    $0 --stop         # 停止所有监控服务
    $0 --logs prometheus  # 查看Prometheus日志
    $0 --backup       # 备份监控数据

EOF
}

# 检查Docker环境
check_docker() {
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装，请先安装Docker"
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    fi

    # 检查Docker服务是否运行
    if ! docker info &> /dev/null; then
        log_error "Docker服务未运行，请启动Docker服务"
        exit 1
    fi
}

# 检查端口占用
check_ports() {
    local ports=("9090" "3001" "9093" "8888" "9100" "9104" "9121" "9115")
    local occupied_ports=()
    
    for port in "${ports[@]}"; do
        if lsof -i :$port &> /dev/null; then
            occupied_ports+=($port)
        fi
    done
    
    if [ ${#occupied_ports[@]} -gt 0 ]; then
        log_warning "以下端口已被占用: ${occupied_ports[*]}"
        log_warning "请确保这些端口可用或修改配置"
        read -p "是否继续? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi
}

# 创建必要的目录
create_directories() {
    log_info "创建监控数据目录..."
    
    local dirs=(
        "docker/prometheus/alert_rules"
        "docker/grafana/provisioning/dashboards"
        "docker/grafana/provisioning/datasources"
        "docker/alertmanager/templates"
        "docker/blackbox"
        "docker/loki"
        "docker/promtail"
        "docker/fluentd"
        "docker/kibana"
        "docker/tempo"
        "logs/prometheus"
        "logs/grafana"
        "logs/alertmanager"
    )
    
    for dir in "${dirs[@]}"; do
        mkdir -p "$dir"
    done
    
    log_success "目录创建完成"
}

# 设置文件权限
set_permissions() {
    log_info "设置文件权限..."
    
    # 确保脚本有执行权限
    chmod +x scripts/*.sh
    
    # 设置日志目录权限
    chmod -R 755 logs/
    
    log_success "权限设置完成"
}

# 启动基础监控
start_basic_monitoring() {
    log_info "启动基础监控组件..."
    
    cd docker
    
    # 启动基础组件
    docker-compose up -d prometheus grafana alertmanager monitor
    
    # 等待服务启动
    log_info "等待服务启动..."
    sleep 30
    
    # 检查服务状态
    check_service_health
    
    log_success "基础监控组件启动完成"
}

# 启动完整监控栈
start_full_monitoring() {
    log_info "启动完整监控栈..."
    
    cd docker
    
    # 启动所有监控组件
    docker-compose -f docker-compose.yml -f docker-compose-monitoring.yml up -d
    
    # 等待服务启动
    log_info "等待所有服务启动..."
    sleep 60
    
    # 检查服务状态
    check_full_service_health
    
    log_success "完整监控栈启动完成"
}

# 检查基础服务健康状态
check_service_health() {
    log_info "检查服务健康状态..."
    
    local services=(
        "bankshield-prometheus:9090"
        "bankshield-grafana:3001"
        "bankshield-alertmanager:9093"
        "bankshield-monitor:8888"
    )
    
    for service in "${services[@]}"; do
        local name=$(echo $service | cut -d: -f1)
        local port=$(echo $service | cut -d: -f2)
        
        if curl -s "http://localhost:$port/-/healthy" &> /dev/null || \
           curl -s "http://localhost:$port/api/health" &> /dev/null || \
           curl -s "http://localhost:$port/actuator/health" &> /dev/null; then
            log_success "$name 服务运行正常"
        else
            log_error "$name 服务启动失败"
            return 1
        fi
    done
}

# 检查完整服务健康状态
check_full_service_health() {
    log_info "检查完整服务健康状态..."
    
    local services=(
        "bankshield-prometheus:9090"
        "bankshield-grafana:3001"
        "bankshield-alertmanager:9093"
        "bankshield-monitor:8888"
        "bankshield-node-exporter:9100"
        "bankshield-mysql-exporter:9104"
        "bankshield-redis-exporter:9121"
        "bankshield-blackbox-exporter:9115"
    )
    
    for service in "${services[@]}"; do
        local name=$(echo $service | cut -d: -f1)
        local port=$(echo $service | cut -d: -f2)
        
        if curl -s "http://localhost:$port/metrics" &> /dev/null || \
           curl -s "http://localhost:$port/-/healthy" &> /dev/null; then
            log_success "$name 服务运行正常"
        else
            log_warning "$name 服务可能未完全启动，请稍后检查"
        fi
    done
}

# 停止监控服务
stop_monitoring() {
    log_info "停止监控服务..."
    
    cd docker
    
    # 停止所有监控服务
    docker-compose -f docker-compose.yml -f docker-compose-monitoring.yml down
    
    log_success "监控服务已停止"
}

# 重启监控服务
restart_monitoring() {
    log_info "重启监控服务..."
    
    stop_monitoring
    sleep 10
    start_full_monitoring
}

# 查看服务状态
show_status() {
    log_info "监控服务状态:"
    
    cd docker
    docker-compose ps
    
    echo
    log_info "访问地址:"
    echo "  • Prometheus:       http://localhost:9090"
    echo "  • Grafana:          http://localhost:3001 (admin/BankShield@2024)"
    echo "  • AlertManager:     http://localhost:9093"
    echo "  • BankShield Monitor: http://localhost:8888"
    echo "  • Node Exporter:    http://localhost:9100/metrics"
    echo "  • MySQL Exporter:   http://localhost:9104/metrics"
    echo "  • Redis Exporter:   http://localhost:9121/metrics"
    echo "  • Kibana:           http://localhost:5601"
    echo "  • Jaeger:           http://localhost:16686"
}

# 查看日志
show_logs() {
    local service=$1
    
    if [ -z "$service" ]; then
        log_error "请指定服务名称"
        return 1
    fi
    
    cd docker
    docker-compose logs -f "$service"
}

# 备份监控数据
backup_data() {
    log_info "开始备份监控数据..."
    
    local backup_dir="backups/$(date +%Y%m%d_%H%M%S)"
    mkdir -p "$backup_dir"
    
    # 备份Prometheus数据
    log_info "备份Prometheus数据..."
    docker run --rm \
        -v bankshield_prometheus_data:/data \
        -v "$(pwd)/$backup_dir":/backup \
        alpine tar czf /backup/prometheus-backup.tar.gz /data
    
    # 备份Grafana数据
    log_info "备份Grafana数据..."
    docker run --rm \
        -v bankshield_grafana_data:/data \
        -v "$(pwd)/$backup_dir":/backup \
        alpine tar czf /backup/grafana-backup.tar.gz /data
    
    # 备份配置文件
    log_info "备份配置文件..."
    cp -r docker/prometheus "$backup_dir/"
    cp -r docker/grafana "$backup_dir/"
    cp -r docker/alertmanager "$backup_dir/"
    
    log_success "备份完成，备份目录: $backup_dir"
}

# 恢复监控数据
restore_data() {
    local backup_dir=$1
    
    if [ -z "$backup_dir" ]; then
        log_error "请指定备份目录"
        return 1
    fi
    
    if [ ! -d "$backup_dir" ]; then
        log_error "备份目录不存在: $backup_dir"
        return 1
    fi
    
    log_info "开始恢复监控数据..."
    
    # 停止服务
    stop_monitoring
    
    # 恢复Prometheus数据
    if [ -f "$backup_dir/prometheus-backup.tar.gz" ]; then
        log_info "恢复Prometheus数据..."
        docker run --rm \
            -v bankshield_prometheus_data:/data \
            -v "$(pwd)/$backup_dir":/backup \
            alpine tar xzf /backup/prometheus-backup.tar.gz -C /
    fi
    
    # 恢复Grafana数据
    if [ -f "$backup_dir/grafana-backup.tar.gz" ]; then
        log_info "恢复Grafana数据..."
        docker run --rm \
            -v bankshield_grafana_data:/data \
            -v "$(pwd)/$backup_dir":/backup \
            alpine tar xzf /backup/grafana-backup.tar.gz -C /
    fi
    
    # 恢复配置文件
    if [ -d "$backup_dir/prometheus" ]; then
        log_info "恢复Prometheus配置..."
        cp -r "$backup_dir/prometheus" docker/
    fi
    
    if [ -d "$backup_dir/grafana" ]; then
        log_info "恢复Grafana配置..."
        cp -r "$backup_dir/grafana" docker/
    fi
    
    if [ -d "$backup_dir/alertmanager" ]; then
        log_info "恢复AlertManager配置..."
        cp -r "$backup_dir/alertmanager" docker/
    fi
    
    log_success "数据恢复完成"
}

# 清理监控数据
clean_data() {
    log_warning "此操作将删除所有监控数据，是否继续?"
    read -p "输入 'yes' 确认删除: " -r confirmation
    
    if [[ "$confirmation" != "yes" ]]; then
        log_info "操作已取消"
        return 0
    fi
    
    log_info "清理监控数据..."
    
    # 停止服务
    stop_monitoring
    
    # 删除数据卷
    docker volume rm bankshield_prometheus_data bankshield_grafana_data bankshield_alertmanager_data 2>/dev/null || true
    
    # 清理日志
    rm -rf logs/*
    
    log_success "监控数据清理完成"
}

# 测试告警
test_alert() {
    log_info "测试告警系统..."
    
    # 创建测试告警
    curl -X POST http://localhost:8888/api/monitoring/alerts \
        -H "Content-Type: application/json" \
        -d '{
            "alertName": "TestAlert",
            "severity": "warning",
            "summary": "测试告警",
            "description": "这是一个测试告警，用于验证告警系统是否正常工作",
            "labels": {
                "test": "true",
                "type": "test"
            }
        }'
    
    log_success "测试告警已发送"
}

# 主函数
main() {
    # 检查参数
    if [ $# -eq 0 ]; then
        show_usage
        exit 0
    fi
    
    # 检查Docker环境
    check_docker
    
    # 解析参数
    while [[ $# -gt 0 ]]; do
        case $1 in
            --basic)
                create_directories
                set_permissions
                check_ports
                start_basic_monitoring
                show_status
                shift
                ;;
            --full)
                create_directories
                set_permissions
                check_ports
                start_full_monitoring
                show_status
                shift
                ;;
            --stop)
                stop_monitoring
                shift
                ;;
            --restart)
                restart_monitoring
                show_status
                shift
                ;;
            --status)
                show_status
                shift
                ;;
            --logs)
                show_logs "$2"
                shift 2
                ;;
            --backup)
                backup_data
                shift
                ;;
            --restore)
                restore_data "$2"
                shift 2
                ;;
            --clean)
                clean_data
                shift
                ;;
            --test)
                test_alert
                shift
                ;;
            --help)
                show_usage
                exit 0
                ;;
            *)
                log_error "未知选项: $1"
                show_usage
                exit 1
                ;;
        esac
    done
}

# 执行主函数
main "$@"