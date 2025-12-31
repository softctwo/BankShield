#!/bin/bash

# BankShield 全系统测试脚本
# 运行所有测试套件并生成报告

echo "=========================================="
echo "    BankShield 全系统测试"
echo "=========================================="
echo ""

# 编译所有测试
echo "正在编译测试文件..."
javac -encoding UTF-8 IndependentSecurityTest.java
javac -encoding UTF-8 IntegrationTestSuite.java
javac -encoding UTF-8 EndToEndTestSuite.java
javac -encoding UTF-8 PerformanceTestSuite.java
javac -encoding UTF-8 SecurityScanTestSuite.java

if [ $? -eq 0 ]; then
    echo "✅ 测试文件编译成功"
else
    echo "❌ 测试文件编译失败"
    exit 1
fi

echo ""
echo "开始运行测试套件..."
echo ""

# 运行独立安全验证测试
echo "1. 运行独立安全验证测试..."
java IndependentSecurityTest > test_results/security_test.log 2>&1
echo ""

# 运行集成测试
echo "2. 运行集成测试..."
java IntegrationTestSuite > test_results/integration_test.log 2>&1
echo ""

# 运行端到端测试
echo "3. 运行端到端测试..."
java EndToEndTestSuite > test_results/e2e_test.log 2>&1
echo ""

# 运行性能测试
echo "4. 运行性能测试..."
java PerformanceTestSuite > test_results/performance_test.log 2>&1
echo ""

# 运行安全扫描测试
echo "5. 运行安全扫描测试..."
java SecurityScanTestSuite > test_results/security_scan_test.log 2>&1
echo ""

echo "=========================================="
echo "    所有测试完成！"
echo "=========================================="
echo ""
echo "测试结果已保存到 test_results/ 目录"
echo ""
echo "查看详细报告:"
echo "  cat test_results/security_test.log"
echo "  cat test_results/integration_test.log"
echo "  cat test_results/e2e_test.log"
echo "  cat test_results/performance_test.log"
echo "  cat test_results/security_scan_test.log"
echo ""
echo "综合报告: COMPREHENSIVE_TEST_REPORT.md"
echo ""
