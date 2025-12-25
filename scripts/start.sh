#!/bin/bash

# BankShield 项目启动脚本
# 支持开发模式和生产模式启动

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志输出函数
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

# 显示帮助信息
show_help() {
    cat << EOF
BankShield 项目启动脚本

使用方法: $0 [选项]

选项:
    -h, --help          显示帮助信息
    -d, --dev           启动开发环境
    -p, --prod          启动生产环境
    -b, --build         构建项目
    -s, --stop          停止服务
    --skip-db           跳过数据库初始化
    --skip-frontend     跳过前端构建
    --skip-backend      跳过后端构建

示例:
    $0 --dev           # 启动开发环境
    $0 --prod          # 启动生产环境
    $0 --build         # 构建项目
    $0 --stop          # 停止服务
EOF
}

# 检查依赖
check_dependencies() {
    log_info "检查依赖..."
    
    # 检查Java
    if ! command -v java &> /dev/null; then
        log_error "Java未安装，请先安装JDK 1.8+"
        exit 1
    fi
    
    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        log_error "Maven未安装，请先安装Maven 3.6+"
        exit 1
    fi
    
    # 检查Node.js
    if ! command -v node &> /dev/null; then
        log_error "Node.js未安装，请先安装Node.js 16+"
        exit 1
    fi
    
    # 检查npm
    if ! command -v npm &> /dev/null; then
        log_error "npm未安装，请先安装npm"
        exit 1
    fi
    
    # 检查MySQL
    if ! command -v mysql &> /dev/null; then
        log_warning "MySQL客户端未安装，请确保MySQL服务可用"
    fi
    
    log_success "依赖检查完成"
}

# 初始化数据库
init_database() {
    log_info "开始初始化数据库..."
    
    if [ -f "./sql/init_database.sql" ]; then
        log_info "数据库脚本存在，准备执行..."
        log_warning "请确保MySQL服务已启动，并配置好数据库连接信息"
        log_warning "如果需要自动执行，请修改脚本中的数据库配置"
    else
        log_error "数据库初始化脚本不存在: sql/init_database.sql"
        exit 1
    fi
    
    log_success "数据库初始化准备完成"
}

# 构建后端
build_backend() {
    log_info "开始构建后端服务..."
    
    cd bankshield-api
    mvn clean package -DskipTests
    
    if [ $? -eq 0 ]; then
        log_success "后端服务构建成功"
    else
        log_error "后端服务构建失败"
        exit 1
    fi
    
    cd ..
}

# 构建前端
build_frontend() {
    log_info "开始构建前端项目..."
    
    cd bankshield-ui
    npm install
    
    if [ $? -ne 0 ]; then
        log_error "前端依赖安装失败"
        exit 1
    fi
    
    npm run build
    
    if [ $? -eq 0 ]; then
        log_success "前端项目构建成功"
    else
        log_error "前端项目构建失败"
        exit 1
    fi
    
    cd ..
}

# 启动后端服务
start_backend() {
    log_info "启动后端服务..."
    
    cd bankshield-api
    
    nohup java -jar -Xms512m -Xmx1024m -server target/bankshield-api-1.0.0-SNAPSHOT.jar \
        --spring.profiles.active=prod \
        > ../logs/api.log 2>&1 &
    
    API_PID=$!
    echo $API_PID > ../pids/api.pid
    
    cd ..
    log_success "后端服务启动成功，PID: $API_PID"
}

# 启动前端服务
start_frontend() {
    log_info "启动前端服务..."
    
    cd bankshield-ui
    
    nohup npm run dev > ../logs/ui.log 2>&1 &
    
    FRONTEND_PID=$!
    echo $FRONTEND_PID > ../pids/frontend.pid
    
    cd ..
    log_success "前端服务启动成功，PID: $FRONTEND_PID"
    log_info "前端访问地址: http://localhost:3000"
}

# 停止服务
stop_services() {
    log_info "停止服务..."
    
    if [ -d "pids" ]; then
        for pid_file in pids/*.pid; do
            if [ -f "$pid_file" ]; then
                PID=$(cat $pid_file)
                if ps -p $PID > /dev/null; then
                    kill -9 $PID
                    log_success "已停止进程 $PID"
                    rm $pid_file
                fi
            fi
        done
    fi
    
    # 检查Java进程
    JAVA_PIDS=$(pgrep -f "bankshield-api")
    if [ ! -z "$JAVA_PIDS" ]; then
        echo "$JAVA_PIDS" | xargs kill -9
        log_success "已停止所有Java进程"
    fi
    
    # 检查Node进程
    NODE_PIDS=$(pgrep -f "bankshield-ui")
    if [ ! -z "$NODE_PIDS" ]; then
        echo "$NODE_PIDS" | xargs kill -9
        log_success "已停止所有Node进程"
    fi
}

# 查看服务状态
status() {
    log_info "服务状态检查..."
    
    # 检查API端口
    if nc -z localhost 8080 2>/dev/null; then
        log_success "后端API服务运行正常 (端口: 8080)"
    else
        log_error "后端API服务未运行 (端口: 8080)"
    fi
    
    # 检查前端端口
    if nc -z localhost 3000 2>/dev/null; then
        log_success "前端服务运行正常 (端口: 3000)"
    else
        log_error "前端服务未运行 (端口: 3000)"
    fi
}

# 创建日志和PID目录
create_dirs() {
    mkdir -p logs
    mkdir -p pids
}

# 开发环境启动
deploy_dev() {
    log_info "启动开发环境..."
    
    check_dependencies
    create_dirs
    
    if [ "$SKIP_DB" != "true" ]; then
        init_database
    fi
    
    if [ "$SKIP_BACKEND" != "true" ]; then
        log_warning "开发环境将使用Spring Boot DevTools，无需手动构建"
        cd bankshield-api
        nohup mvn spring-boot:run > ../logs/api.log 2>&1 &
        API_PID=$!
        echo $API_PID > ../pids/api.pid
        cd ..
        log_success "后端服务启动成功 (开发模式)"
    fi
    
    if [ "$SKIP_FRONTEND" != "true" ]; then
        cd bankshield-ui
        npm install
        nohup npm run dev > ../logs/ui.log 2>&1 &
        FRONTEND_PID=$!
        echo $FRONTEND_PID > ../pids/frontend.pid
        cd ..
        log_success "前端服务启动成功 (开发模式)"
    fi
    
    log_success "开发环境启动完成！"
    log_info "前端地址: http://localhost:3000"
    log_info "后端API: http://localhost:8080/api"
    log_info "Druid监控: http://localhost:8080/api/druid/login.html (admin/123456)"
    status
}

# 生产环境启动
deploy_prod() {
    log_info "启动生产环境..."
    
    check_dependencies
    create_dirs
    
    if [ "$SKIP_DB" != "true" ]; then
        init_database
    fi
    
    if [ "$SKIP_BACKEND" != "true" ]; then
        build_backend
        start_backend
    fi
    
    if [ "$SKIP_FRONTEND" != "true" ]; then
        build_frontend
        # 生产环境前端使用nginx部署，这里仅作演示
        log_warning "生产环境前端请使用Nginx部署，静态文件在: bankshield-ui/dist"
    fi
    
    log_success "生产环境部署完成！"
    status
}

# 构建项目
build_all() {
    log_info "开始构建项目..."
    
    check_dependencies
    
    if [ "$SKIP_DB" != "true" ]; then
        init_database
    fi
    
    if [ "$SKIP_BACKEND" != "true" ]; then
        build_backend
    fi
    
    if [ "$SKIP_FRONTEND" != "true" ]; then
        build_frontend
    fi
    
    log_success "项目构建完成！"
}

# 解析参数
SKIP_DB=false
SKIP_FRONTEND=false
SKIP_BACKEND=false
COMMAND="help"

while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            COMMAND="help"
            shift
            ;;
        -d|--dev)
            COMMAND="dev"
            shift
            ;;
        -p|--prod)
            COMMAND="prod"
            shift
            ;;
        -b|--build)
            COMMAND="build"
            shift
            ;;
        -s|--stop)
            COMMAND="stop"
            shift
            ;;
        --skip-db)
            SKIP_DB=true
            shift
            ;;
        --skip-frontend)
            SKIP_FRONTEND=true
            shift
            ;;
        --skip-backend)
            SKIP_BACKEND=true
            shift
            ;;
        *)
            log_error "未知参数: $1"
            show_help
            exit 1
            ;;
    esac
done

# 执行命令
case $COMMAND in
    help)
        show_help
        ;;
    dev)
        deploy_dev
        ;;
    prod)
        deploy_prod
        ;;
    build)
        build_all
        ;;
    stop)
        stop_services
        ;;
    *)
        show_help
        ;;
esac