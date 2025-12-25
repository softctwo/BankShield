#!/bin/bash

# BankShield 国密证书生成脚本
# 用于生成SM2密钥对和国密SSL证书

set -e

echo "======================================"
echo "BankShield 国密证书生成工具"
echo "======================================"

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误: Java环境未安装"
    exit 1
fi

# 检查Keytool
if ! command -v keytool &> /dev/null; then
    echo "错误: keytool工具未找到"
    exit 1
fi

# 创建证书目录
CERT_DIR="certs"
mkdir -p $CERT_DIR

echo "生成国密证书到目录: $CERT_DIR"

# 生成SM2密钥对和证书
echo ""
echo "1. 生成SM2服务器证书..."
keytool -genkeypair \
    -alias sm2-server \
    -keyalg SM2 \
    -sigalg SM3withSM2 \
    -keysize 256 \
    -validity 3650 \
    -keypass changeit \
    -storepass changeit \
    -keystore $CERT_DIR/sm2-server.p12 \
    -storetype PKCS12 \
    -dname "CN=bankshield-server, OU=BankShield, O=BankShield Inc, L=Beijing, ST=Beijing, C=CN"

echo "✓ SM2服务器证书生成完成"

# 导出服务器证书
echo ""
echo "2. 导出SM2服务器证书..."
keytool -export \
    -alias sm2-server \
    -keystore $CERT_DIR/sm2-server.p12 \
    -storepass changeit \
    -file $CERT_DIR/sm2-server.cer

echo "✓ SM2服务器证书导出完成"

# 生成SM2客户端证书（可选）
echo ""
echo "3. 生成SM2客户端证书..."
keytool -genkeypair \
    -alias sm2-client \
    -keyalg SM2 \
    -sigalg SM3withSM2 \
    -keysize 256 \
    -validity 3650 \
    -keypass changeit \
    -storepass changeit \
    -keystore $CERT_DIR/sm2-client.p12 \
    -storetype PKCS12 \
    -dname "CN=bankshield-client, OU=BankShield, O=BankShield Inc, L=Beijing, ST=Beijing, C=CN"

echo "✓ SM2客户端证书生成完成"

# 导出客户端证书
echo ""
echo "4. 导出SM2客户端证书..."
keytool -export \
    -alias sm2-client \
    -keystore $CERT_DIR/sm2-client.p12 \
    -storepass changeit \
    -file $CERT_DIR/sm2-client.cer

echo "✓ SM2客户端证书导出完成"

# 生成CA证书（可选）
echo ""
echo "5. 生成SM2 CA证书..."
keytool -genkeypair \
    -alias sm2-ca \
    -keyalg SM2 \
    -sigalg SM3withSM2 \
    -keysize 256 \
    -validity 3650 \
    -keypass changeit \
    -storepass changeit \
    -keystore $CERT_DIR/sm2-ca.p12 \
    -storetype PKCS12 \
    -dname "CN=bankshield-ca, OU=BankShield CA, O=BankShield Inc, L=Beijing, ST=Beijing, C=CN"

echo "✓ SM2 CA证书生成完成"

# 导出CA证书
echo ""
echo "6. 导出SM2 CA证书..."
keytool -export \
    -alias sm2-ca \
    -keystore $CERT_DIR/sm2-ca.p12 \
    -storepass changeit \
    -file $CERT_DIR/sm2-ca.cer

echo "✓ SM2 CA证书导出完成"

# 查看证书信息
echo ""
echo "7. 查看证书信息..."
echo "服务器证书信息:"
keytool -list -v \
    -alias sm2-server \
    -keystore $CERT_DIR/sm2-server.p12 \
    -storepass changeit

echo ""
echo "客户端证书信息:"
keytool -list -v \
    -alias sm2-client \
    -keystore $CERT_DIR/sm2-client.p12 \
    -storepass changeit

echo ""
echo "CA证书信息:"
keytool -list -v \
    -alias sm2-ca \
    -keystore $CERT_DIR/sm2-ca.p12 \
    -storepass changeit

# 生成证书链（可选）
echo ""
echo "8. 生成证书链..."
# 将CA证书导入服务器密钥库
keytool -import \
    -alias sm2-ca \
    -file $CERT_DIR/sm2-ca.cer \
    -keystore $CERT_DIR/sm2-server.p12 \
    -storepass changeit \
    -noprompt

echo "✓ 证书链生成完成"

# 列出所有证书
echo ""
echo "9. 证书清单:"
ls -la $CERT_DIR/

echo ""
echo "======================================"
echo "国密证书生成完成！"
echo "======================================"
echo ""
echo "生成的文件:"
echo "- $CERT_DIR/sm2-server.p12 (服务器密钥库)"
echo "- $CERT_DIR/sm2-server.cer (服务器证书)"
echo "- $CERT_DIR/sm2-client.p12 (客户端密钥库)"
echo "- $CERT_DIR/sm2-client.cer (客户端证书)"
echo "- $CERT_DIR/sm2-ca.p12 (CA密钥库)"
echo "- $CERT_DIR/sm2-ca.cer (CA证书)"
echo ""
echo "使用说明:"
echo "1. 将服务器证书配置到application.yml中"
echo "2. 设置正确的密钥库密码"
echo "3. 配置国密SSL协议为TLCP"
echo "4. 启用国密加密套件"
echo ""
echo "注意事项:"
echo "- 生产环境请使用强密码"
echo "- 妥善保管密钥库文件"
echo "- 定期更新证书"
echo "- 遵循密钥管理最佳实践"