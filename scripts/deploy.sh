#!/bin/bash
# deploy.sh

set -e

ENV=${1:-dev}
NAMESPACE="bankshield-${ENV}"
REGISTRY="harbor.bankshield.com"
IMAGE_TAG=${2:-latest}

echo "🚀 开始部署BankShield到 ${ENV} 环境..."

# 1. 检查环境变量
if [ -z "$KUBECONFIG" ]; then
    echo "❌ 未设置 KUBECONFIG 环境变量"
    exit 1
fi

# 2. 创建命名空间（如果不存在）
kubectl create namespace ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -

# 3. 部署数据库
helm upgrade --install mysql ./helm/mysql \
  --namespace ${NAMESPACE} \
  --values values/mysql-${ENV}.yaml

# 4. 部署Redis
helm upgrade --install redis ./helm/redis \
  --namespace ${NAMESPACE} \
  --values values/redis-${ENV}.yaml

# 5. 部署应用
helm upgrade --install bankshield ./helm/bankshield \
  --namespace ${NAMESPACE} \
  --set image.tag=${IMAGE_TAG} \
  --set image.registry=${REGISTRY} \
  --values values/bankshield-${ENV}.yaml \
  --wait \
  --timeout 600s

# 6. 健康检查
echo "⏳ 等待服务就绪..."
sleep 30

kubectl wait deployment/bankshield-api \
  --namespace ${NAMESPACE} \
  --for condition=available \
  --timeout=300s

kubectl wait deployment/bankshield-ui \
  --namespace ${NAMESPACE} \
  --for condition=available \
  --timeout=300s

# 7. 运行冒烟测试
echo "🧪 运行冒烟测试..."
./scripts/smoke-test.sh ${ENV}

echo "✅ 部署完成！"
echo "📊 访问地址:"
echo "   API: https://api-${ENV}.bankshield.com"
echo "   UI: https://app-${ENV}.bankshield.com"
echo "   Grafana: https://grafana-${ENV}.bankshield.com"