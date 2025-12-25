#!/bin/bash
# send-deploy-notifications.sh

ENV=$1
STATUS=$2
VERSION=$3

if [ "$STATUS" == "success" ]; then
    SUBJECT="✅ BankShield ${ENV}环境部署成功"
    BODY="版本 ${VERSION} 已成功部署到${ENV}环境"
else
    SUBJECT="❌ BankShield ${ENV}环境部署失败"
    BODY="版本 ${VERSION} 部署失败，请查看日志"
fi

# 发送邮件
echo "$BODY" | mail -s "$SUBJECT" -r "devops@bankshield.com" \
  "team@bankshield.com, oncall@bankshield.com"