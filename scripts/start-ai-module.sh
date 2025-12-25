#!/bin/bash

# BankShield AI智能安全分析模块启动脚本
# Author: BankShield
# Version: 1.0.0

set -e

echo "=========================================="
echo "BankShield AI模块启动脚本"
echo "=========================================="

# 设置变量
PROJECT_ROOT=$(cd "$(dirname "$0")/.." && pwd)
AI_MODULE_DIR="$PROJECT_ROOT/bankshield-ai"
JAR_FILE="$AI_MODULE_DIR/target/bankshield-ai-1.0.0-SNAPSHOT.jar"
LOGS_DIR="$PROJECT_ROOT/logs"
CONFIG_DIR="$AI_MODULE_DIR/src/main/resources"
PID_FILE="$LOGS_DIR/ai-module.pid"

# 创建日志目录
mkdir -p "$LOGS_DIR"

# 默认配置
JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps"
SPRING_PROFILES="dev"
SERVER_PORT="8085"

# 函数：检查程序是否正在运行
is_running() {
    if [ -f "$PID_FILE" ]; then
        local pid=$(cat "$PID_FILE")
        if ps -p "$pid" > /dev/null 2>&1; then
            return 0
        else
            rm -f "$PID_FILE"
            return 1
        fi
    fi
    return 1
}

# 函数：获取进程ID
get_pid() {
    if [ -f "$PID_FILE" ]; then
        cat "$PID_FILE"
    fi
}

# 函数：启动服务
start_service() {
    echo "检查服务状态..."
    if is_running; then
        echo "AI模块已经在运行中 (PID: $(get_pid))"
        exit 1
    fi

    echo "检查JAR文件..."
    if [ ! -f "$JAR_FILE" ]; then
        echo "错误: JAR文件不存在: $JAR_FILE"
        echo "请先运行构建脚本: ./scripts/build-ai-module.sh"
        exit 1
    fi

    echo "启动AI模块..."
    echo "JAR文件: $JAR_FILE"
    echo "配置文件: $CONFIG_DIR/application.yml"
    echo "日志目录: $LOGS_DIR"
    echo "环境配置: $SPRING_PROFILES"
    echo "服务端口: $SERVER_PORT"

    # 启动Java进程
    nohup java $JAVA_OPTS \
        -Dspring.profiles.active="$SPRING_PROFILES" \
        -Dserver.port="$SERVER_PORT" \
        -Dlogging.file.path="$LOGS_DIR" \
        -jar "$JAR_FILE" \
        > "$LOGS_DIR/ai-module.out" 2>&1 &

    local pid=$!
    echo $pid > "$PID_FILE"

    echo "AI模块启动中 (PID: $pid)..."

    # 等待服务启动
    sleep 10

    # 检查服务是否成功启动
    if is_running; then
        echo "AI模块启动成功 ✓"
        echo "进程ID: $pid"
        echo "服务端口: $SERVER_PORT"
        echo "日志文件: $LOGS_DIR/ai-module.out"
        
        # 显示启动日志
        echo "启动日志预览:"
        tail -n 20 "$LOGS_DIR/ai-module.out"
        
        # 显示健康检查URL
        echo "健康检查URL: http://localhost:$SERVER_PORT/api/ai/health"
        echo "API文档: http://localhost:$SERVER_PORT/swagger-ui.html"
    else
        echo "AI模块启动失败 ✗"
        echo "查看详细日志: $LOGS_DIR/ai-module.out"
        rm -f "$PID_FILE"
        exit 1
    fi
}

# 函数：停止服务
stop_service() {
    echo "停止AI模块..."
    
    if is_running; then
        local pid=$(get_pid)
        echo "正在停止进程 (PID: $pid)..."
        
        # 尝试优雅停止
        kill -TERM "$pid"
        
        # 等待进程停止
        local count=0
        while is_running && [ $count -lt 30 ]; do
            echo "等待进程停止... ($count/30)"
            sleep 1
            count=$((count + 1))
        done
        
        # 如果进程还在运行，强制停止
        if is_running; then
            echo "强制停止进程..."
            kill -KILL "$pid"
            sleep 2
        fi
        
        # 删除PID文件
        rm -f "$PID_FILE"
        
        if is_running; then
            echo "停止失败 ✗"
            exit 1
        else
            echo "AI模块已停止 ✓"
        fi
    else
        echo "AI模块未运行"
    fi
}

# 函数：重启服务
restart_service() {
    echo "重启AI模块..."
    stop_service
    sleep 3
    start_service
}

# 函数：查看状态
show_status() {
    if is_running; then
        local pid=$(get_pid)
        echo "AI模块运行中 ✓"
        echo "进程ID: $pid"
        
        # 显示进程信息
        if ps -p "$pid" -o pid,ppid,cmd,%mem,%cpu > /dev/null 2>&1; then
            echo "进程信息:"
            ps -p "$pid" -o pid,ppid,cmd,%mem,%cpu
        fi
        
        # 检查端口监听
        if command -v netstat > /dev/null 2>&1; then
            echo "端口监听:"
            netstat -tlnp 2>/dev/null | grep ":$SERVER_PORT" || echo "端口 $SERVER_PORT 未监听"
        elif command -v ss > /dev/null 2>&1; then
            echo "端口监听:"
            ss -tlnp 2>/dev/null | grep ":$SERVER_PORT" || echo "端口 $SERVER_PORT 未监听"
        fi
        
        echo "日志文件: $LOGS_DIR/ai-module.out"
    else
        echo "AI模块未运行"
        if [ -f "$PID_FILE" ]; then
            echo "PID文件存在但进程未运行，可能是异常退出"
            echo "PID文件: $PID_FILE"
        fi
    fi
}

# 函数：查看日志
show_logs() {
    local log_file="$LOGS_DIR/ai-module.out"
    if [ -f "$log_file" ]; then
        echo "查看日志文件: $log_file"
        tail -f "$log_file"
    else
        echo "日志文件不存在: $log_file"
    fi
}

# 函数：健康检查
health_check() {
    if is_running; then
        local health_url="http://localhost:$SERVER_PORT/api/ai/health"
        echo "执行健康检查: $health_url"
        
        if command -v curl > /dev/null 2>&1; then
            local response=$(curl -s -w "%{http_code}" "$health_url" 2>/dev/null)
            local http_code="${response: -3}"
            local body="${response%???}"
            
            if [ "$http_code" = "200" ]; then
                echo "健康检查通过 ✓"
                echo "响应: $body"
            else
                echo "健康检查失败 ✗"
                echo "HTTP状态码: $http_code"
                echo "响应: $body"
            fi
        else
            echo "curl命令未安装，无法执行健康检查"
        fi
    else
        echo "服务未运行，无法执行健康检查"
    fi
}

# 函数：显示帮助信息
show_help() {
    echo "用法: $0 {start|stop|restart|status|logs|health|help}"
    echo ""
    echo "命令:"
    echo "  start    启动AI模块"
    echo "  stop     停止AI模块"
    echo "  restart  重启AI模块"
    echo "  status   查看服务状态"
    echo "  logs     查看运行日志"
    echo "  health   执行健康检查"
    echo "  help     显示帮助信息"
    echo ""
    echo "环境变量:"
    echo "  JAVA_OPTS          Java虚拟机参数"
    echo "  SPRING_PROFILES    Spring环境配置 (dev/prod/test)"
    echo "  SERVER_PORT        服务端口 (默认: 8085)"
}

# 主函数
main() {
    case "$1" in
        start)
            start_service
            ;;
        stop)
            stop_service
            ;;
        restart)
            restart_service
            ;;
        status)
            show_status
            ;;
        logs)
            show_logs
            ;;
        health)
            health_check
            ;;
        help)
            show_help
            ;;
        *)
            echo "未知命令: $1"
            show_help
            exit 1
            ;;
    esac
}

# 检查参数
if [ $# -eq 0 ]; then
    echo "错误: 未指定命令"
    show_help
    exit 1
fi

# 执行主函数
main "$@"