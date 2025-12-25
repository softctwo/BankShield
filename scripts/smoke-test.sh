#!/bin/bash
# smoke-test.sh

ENV=$1
API_URL="https://api-${ENV}.bankshield.com"
UI_URL="https://app-${ENV}.bankshield.com"

echo "🧪 运行 ${ENV} 环境的冒烟测试..."

# 1. API健康检查
echo "测试 API 健康状态..."
curl -f "${API_URL}/api/health" || {
    echo "❌ API 健康检查失败"
    exit 1
}

# 2. UI访问测试
echo "测试 UI 访问..."
curl -f "${UI_URL}" || {
    echo "❌ UI 访问测试失败"
    exit 1
}

# 3. API基本功能测试
echo "测试 API 基本功能..."
# 测试认证端点
curl -f "${API_URL}/api/auth/login" \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}' \
  || echo "⚠️  认证端点测试失败（可能是预期行为）"

# 4. 数据库连接测试
echo "测试数据库连接..."
curl -f "${API_URL}/api/system/db-status" || {
    echo "❌ 数据库连接测试失败"
    exit 1
}

echo "✅ 冒烟测试通过！"