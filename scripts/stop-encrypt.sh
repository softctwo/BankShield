#!/bin/bash

# BankShield 密钥管理模块停止脚本

APP_NAME="bankshield-encrypt"
APP_DIR=$(cd "$(dirname "$0")/.." && pwd)
LOG_DIR="$APP_DIR/logs"
PID_FILE="$LOG_DIR/$APP_NAME.pid"

# 检查PID文件是否存在
if [ ! -f "$PID_FILE" ]; then
    echo "$APP_NAME 没有在运行"
    exit 0
fi

# 读取PID
PID=$(cat "$PID_FILE")

# 检查进程是否存在
if ps -p "$PID" > /dev/null 2>&1; then
    echo "正在停止 $APP_NAME (PID: $PID)..."
    
    # 优雅关闭
    kill -TERM "$PID"
    
    # 等待进程结束
    for i in {1..30}; do
        if ! ps -p "$PID" > /dev/null 2>&1; then
            echo "$APP_NAME 已停止"
            rm -f "$PID_FILE"
            exit 0
        fi
        sleep 1
    done
    
    # 如果优雅关闭失败，强制关闭
    echo "优雅关闭失败，正在强制关闭..."
    kill -KILL "$PID"
    sleep 2
    
    if ! ps -p "$PID" > /dev/null 2>&1; then
        echo "$APP_NAME 已强制停止"
        rm -f "$PID_FILE"
    else
        echo "停止 $APP_NAME 失败"
        exit 1
    fi
else
    echo "$APP_NAME 没有在运行 (PID: $PID 不存在)"
    rm -f "$PID_FILE"
fi