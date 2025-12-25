#!/bin/bash
# health-check.sh

URL=$1
TIMEOUT=${2:-60}
RETRY_COUNT=${3:-5}
RETRY_DELAY=${4:-10}

echo "🔍 健康检查: ${URL}"

for i in $(seq 1 ${RETRY_COUNT}); do
    echo "尝试 ${i}/${RETRY_COUNT}..."
    
    HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" --max-time ${TIMEOUT} ${URL})
    
    if [ "${HTTP_STATUS}" -eq 200 ]; then
        echo "✅ 服务正常运行 (HTTP ${HTTP_STATUS})"
        exit 0
    else
        echo "❌ 服务异常 (HTTP ${HTTP_STATUS})"
        if [ ${i} -lt ${RETRY_COUNT} ]; then
            echo "等待 ${RETRY_DELAY} 秒后重试..."
            sleep ${RETRY_DELAY}
        fi
    fi
done

echo "❌ 健康检查失败，服务未能正常启动"
exit 1