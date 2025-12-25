#!/bin/bash
# security-scan.sh

echo "🔒 开始安全扫描..."

# OWASP Dependency Check
mvn dependency-check:check \
  -Ddependency-check.failBuildOnCVSS=7 \
  -Ddependency-check.suppressionFile=security/suppressions.xml

# 生成报告
mvn dependency-check:aggregate \
  -Ddependency-check.reportFormat=ALL \
  -Ddependency-check.reportOutputDirectory=target/security

echo "📊 扫描完成，报告：target/security/"