#!/bin/bash

# BankShield 密钥管理模块启动脚本

# 设置Java环境变量
export JAVA_HOME=${JAVA_HOME:-/usr/lib/jvm/java-8-openjdk-amd64}
export PATH=$JAVA_HOME/bin:$PATH

# 设置应用参数
APP_NAME="bankshield-encrypt"
APP_PORT=${APP_PORT:-8083}
APP_PROFILE=${APP_PROFILE:-dev}

# 设置JVM参数
JVM_OPTS="-Xms512m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m"
JVM_OPTS="$JVM_OPTS -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
JVM_OPTS="$JVM_OPTS -Dfile.encoding=UTF-8 -Djava.awt.headless=true"

# 设置加密主密钥（生产环境请使用安全的密钥管理方案）
if [ -z "$ENCRYPT_MASTER_KEY" ] && [ "$VAULT_ENABLED" != "true" ]; then
    echo "警告：未设置ENCRYPT_MASTER_KEY环境变量且Vault未启用，将使用默认主密钥（仅用于开发环境）"
    export ENCRYPT_MASTER_KEY="default_master_key_for_development_only"
fi

# 加载Vault配置（如果存在）
if [ -f ".env.vault" ]; then
    echo "加载Vault配置..."
    source .env.vault
fi

# 检查Vault集成状态
if [ "$VAULT_ENABLED" = "true" ]; then
    echo "Vault集成已启用"
    echo "Vault地址: ${VAULT_ADDR:-未配置}"
    echo "Role ID: ${VAULT_ROLE_ID:0:8}...（如果已配置）"
else
    echo "Vault集成未启用，使用传统密钥管理方式"
fi

# 设置应用路径
APP_DIR=$(cd "$(dirname "$0")/.." && pwd)
JAR_FILE="$APP_DIR/bankshield-encrypt/target/bankshield-encrypt-1.0.0.jar"

# 检查JAR文件是否存在
if [ ! -f "$JAR_FILE" ]; then
    echo "错误：找不到JAR文件 $JAR_FILE"
    echo "请先执行：mvn clean package"
    exit 1
fi

# 创建日志目录
LOG_DIR="$APP_DIR/logs"
mkdir -p "$LOG_DIR"

# 设置日志文件
LOG_FILE="$LOG_DIR/$APP_NAME-$APP_PROFILE.log"
PID_FILE="$LOG_DIR/$APP_NAME.pid"

# 检查端口是否被占用
if lsof -Pi :$APP_PORT -sTCP:LISTEN -t >/dev/null ; then
    echo "错误：端口 $APP_PORT 已被占用"
    exit 1
fi

# 检查是否已经在运行
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p "$PID" > /dev/null 2>&1; then
        echo "错误：$APP_NAME 已经在运行 (PID: $PID)"
        exit 1
    fi
fi

# 启动应用
echo "正在启动 $APP_NAME ..."
echo "端口: $APP_PORT"
echo "环境: $APP_PROFILE"
echo "日志: $LOG_FILE"

nohup java $JVM_OPTS \
    -Dspring.profiles.active=$APP_PROFILE \
    -Dserver.port=$APP_PORT \
    -jar "$JAR_FILE" \
    > "$LOG_FILE" 2>&1 &

# 保存PID
echo $! > "$PID_FILE"

# 等待应用启动
echo "等待应用启动..."
sleep 10

# 检查启动状态
if ps -p $! > /dev/null 2>&1; then
    echo "$APP_NAME 启动成功 (PID: $!)"
    echo "日志文件: $LOG_FILE"
    echo "访问地址: http://localhost:$APP_PORT"
else
    echo "$APP_NAME 启动失败"
    echo "请查看日志文件: $LOG_FILE"
    rm -f "$PID_FILE"
    exit 1
fi