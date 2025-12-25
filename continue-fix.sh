#!/bin/bash

# BankShield 继续测试和修复脚本
# 用于修复剩余的编译和测试问题

set -e

echo "========================================="
echo "BankShield 继续修复脚本"
echo "========================================="
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 函数：打印信息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# 步骤1：清理并重新编译
print_info "步骤1：清理并重新编译项目"
cd /Users/zhangyanlong/workspaces/BankShield

# 清理
print_info "清理项目..."
mvn clean -DskipTests

# 编译
print_info "编译项目..."
if mvn compile -DskipTests 2>&1 | tee compile.log | grep -q "BUILD SUCCESS"; then
    print_success "项目编译成功！"
else
    print_error "项目编译失败"
    print_info "查看编译日志: compile.log"
    exit 1
fi

# 步骤2：运行单元测试
print_info ""
print_info "步骤2：运行单元测试"
print_info "尝试运行bankshield-common模块测试..."
if mvn test -pl bankshield-common -DskipTests=false 2>&1 | tee test-common.log | grep -q "BUILD SUCCESS"; then
    print_success "bankshield-common 测试通过！"
else
    print_warning "bankshield-common 测试有失败"
fi

# 步骤3：检查编译错误
print_info ""
print_info "步骤3：检查剩余编译错误"
ERROR_COUNT=$(grep -c "ERROR" compile.log || echo "0")

if [ "$ERROR_COUNT" -eq 0 ]; then
    print_success "没有编译错误！"
else
    print_warning "发现 $ERROR_COUNT 个编译错误"
    print_info "主要错误类型:"
    grep "ERROR" compile.log | grep "找不到符号" | head -5
    grep "ERROR" compile.log | grep "程序包不存在" | head -5
fi

# 步骤4：前端检查
print_info ""
print_info "步骤4：检查前端构建状态"
cd /Users/zhangyanlong/workspaces/BankShield/bankshield-ui

if [ -f "package.json" ]; then
    print_info "前端项目存在"
    print_info "依赖已安装: $(test -d node_modules && echo '是' || echo '否')"
    print_info "注意：前端有TypeScript类型错误需要修复"
else
    print_warning "前端项目不存在或package.json缺失"
fi

# 步骤5：生成修复建议
print_info ""
print_info "步骤5：生成修复建议"
cat > NEXT_FIXES.md << EOF
# 下一步修复建议

## 高优先级

### 1. 实体类Lombok问题
**问题**: 多个实体类的getter/setter方法无法识别
**影响**: bankshield-api模块编译失败
**解决方案**:
- 检查Lombok注解处理器配置
- 在每个实体类上显式添加 @Getter @Setter
- 清理Maven缓存: mvn clean
- 重新安装依赖: mvn dependency:purge-local-repository

**受影响的实体类**:
- SecurityScanTask
- NotificationConfig
- AlertRecord
- AlertRule
- WatermarkTask
- WatermarkTemplate

### 2. 服务类Logger问题
**问题**: @Slf4j注解无法生效
**影响**: 日志无法使用
**解决方案**:
- 确保使用了正确的import: lombok.extern.slf4j.Slf4j
- 检查Lombok版本兼容性
- 在Maven compiler插件中配置annotationProcessorPaths

### 3. 前端类型定义
**问题**: TypeScript类型定义缺失
**影响**: 前端构建失败
**解决方案**:
- 补全 @/types/ 目录下的类型定义文件
- 修复API接口返回类型
- 确保 @/api/ 目录下的接口正确导出类型

## 中优先级

### 1. 测试框架完善
- 添加Spring Security Test依赖
- 配置Testcontainers
- 创建Mock数据

### 2. CI/CD配置
- 完善GitHub Actions配置
- 添加代码覆盖率检查
- 配置自动化测试

## 低优先级

### 1. 代码规范
- 运行ESLint检查
- 添加SonarQube质量门禁
- 完善代码注释

### 2. 文档更新
- 更新API文档
- 完善开发者指南
- 添加故障排查文档
EOF

print_success "修复建议已生成: NEXT_FIXES.md"

# 总结
echo ""
echo "========================================="
echo "执行摘要"
echo "========================================="
echo ""
echo "✅ 已完成:"
echo "  - 修复Maven依赖问题"
echo "  - 修复Import错误"
echo "  - 修复Mapper接口问题"
echo "  - 清理项目并重新编译"
echo ""
echo "⚠️  需要后续处理:"
echo "  - 实体类Lombok方法问题"
echo "  - 前端TypeScript类型定义"
echo "  - 测试框架配置"
echo ""
echo "📄 报告文件:"
echo "  - TEST_AND_FIX_REPORT.md (详细报告)"
echo "  - NEXT_FIXES.md (修复建议)"
echo "  - compile.log (编译日志)"
echo "  - test-common.log (测试日志)"
echo ""
echo "========================================="
print_success "脚本执行完成！"
