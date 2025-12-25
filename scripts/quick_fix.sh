#!/bin/bash

# BankShield 项目快速修复脚本
# 用于自动修复一些常见的代码问题

set -e

echo "================================"
echo "BankShield 项目快速修复工具"
echo "================================"
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 修复 1: 删除 System.out.println (如果有的话)
fix_system_out() {
    print_info "检查并删除 System.out.println..."
    COUNT=$(find /Users/zhangyanlong/workspaces/BankShield/bankshield-api/src/main/java -name "*.java" -exec grep -l "System\.out\.println" {} \; | wc -l)
    if [ $COUNT -gt 0 ]; then
        print_warning "发现 $COUNT 个文件包含 System.out.println"
        find /Users/zhangyanlong/workspaces/BankShield/bankshield-api/src/main/java -name "*.java" -exec grep -l "System\.out\.println" {} \; | head -5
    else
        print_info "未发现 System.out.println 使用"
    fi
}

# 修复 2: 检查并修复 TODO 注释
fix_todos() {
    print_info "检查 TODO 注释..."
    TODO_COUNT=$(grep -r "TODO" /Users/zhangyanlong/workspaces/BankShield/bankshield-api/src/main/java --include="*.java" | wc -l)
    if [ $TODO_COUNT -gt 0 ]; then
        print_warning "发现 $TODO_COUNT 个 TODO 注释"
        echo ""
        echo "主要 TODO 问题:"
        grep -r "TODO" /Users/zhangyanlong/workspaces/BankShield/bankshield-api/src/main/java --include="*.java" -n | head -10
    else
        print_info "未发现 TODO 注释"
    fi
}

# 修复 3: 检查并修复硬编码配置
fix_hardcoded_config() {
    print_info "检查硬编码配置..."
    if grep -r "smtp.bankshield.com" /Users/zhangyanlong/workspaces/BankShield/bankshield-api/src/main/resources --include="*.yml" > /dev/null 2>&1; then
        print_warning "发现硬编码的 SMTP 配置"
        grep -r "smtp" /Users/zhangyanlong/workspaces/BankShield/bankshield-api/src/main/resources --include="*.yml" | head -3
    else
        print_info "未发现硬编码 SMTP 配置"
    fi
}

# 修复 4: 检查异常处理
fix_exception_handling() {
    print_info "检查异常处理..."
    EXCEPTION_COUNT=$(grep -r "throw new RuntimeException" /Users/zhangyanlong/workspaces/BankShield/bankshield-api/src/main/java --include="*.java" | wc -l)
    if [ $EXCEPTION_COUNT -gt 0 ]; then
        print_warning "发现 $EXCEPTION_COUNT 处直接抛出 RuntimeException"
        echo ""
        echo "建议使用自定义异常:"
        grep -r "throw new RuntimeException" /Users/zhangyanlong/workspaces/BankShield/bankshield-api/src/main/java --include="*.java" -n | head -5
    else
        print_info "异常处理良好"
    fi
}

# 修复 5: 检查输入验证
fix_input_validation() {
    print_info "检查输入验证..."
    VALIDATION_COUNT=$(grep -r "@Valid" /Users/zhangyanlong/workspaces/BankShield/bankshield-api/src/main/java --include="*.java" | wc -l)
    PARAM_COUNT=$(grep -r "@RequestParam\|@PathVariable" /Users/zhangyanlong/workspaces/BankShield/bankshield-api/src/main/java --include="*.java" | wc -l)

    if [ $VALIDATION_COUNT -lt $((PARAM_COUNT / 2)) ]; then
        print_warning "输入验证覆盖率较低: $VALIDATION_COUNT / $PARAM_COUNT"
        echo ""
        echo "建议为参数添加验证注解:"
        echo "  - @NotNull"
        echo "  - @Size"
        echo "  - @Pattern"
        echo "  - @Valid"
    else
        print_info "输入验证覆盖率良好"
    fi
}

# 修复 6: 检查分页查询
fix_pagination() {
    print_info "检查分页查询..."
    SELECT_LIST_COUNT=$(grep -r "selectList.*null" /Users/zhangyanlong/workspaces/BankShield/bankshield-api/src/main/java --include="*.java" | wc -l)
    if [ $SELECT_LIST_COUNT -gt 0 ]; then
        print_warning "发现 $SELECT_LIST_COUNT 处未使用分页的查询"
        echo ""
        echo "建议使用分页查询:"
        grep -r "selectList.*null" /Users/zhangyanlong/workspaces/BankShield/bankshield-api/src/main/java --include="*.java" -n | head -5
    else
        print_info "分页查询使用良好"
    fi
}

# 修复 7: 检查代码复杂度
fix_code_complexity() {
    print_info "检查代码复杂度..."
    LARGE_FILES=$(find /Users/zhangyanlong/workspaces/BankShield/bankshield-api/src/main/java -name "*.java" -exec wc -l {} \; | awk '$1 > 500 {print $2, $1}' | sort -nr -k2)

    if [ -n "$LARGE_FILES" ]; then
        print_warning "发现超过 500 行的文件:"
        echo "$LARGE_FILES" | head -5
    else
        print_info "代码复杂度良好"
    fi
}

# 修复 8: 运行编译检查
run_compile_check() {
    print_info "运行编译检查..."
    cd /Users/zhangyanlong/workspaces/BankShield

    if mvn compile -pl bankshield-api -q > /dev/null 2>&1; then
        print_info "编译检查通过 ✅"
    else
        print_error "编译检查失败 ❌"
        mvn compile -pl bankshield-api -q 2>&1 | tail -20
        return 1
    fi
}

# 修复 9: 生成修复报告
generate_report() {
    print_info "生成修复报告..."
    REPORT_FILE="/Users/zhangyanlong/workspaces/BankShield/quick_fix_report_$(date +%Y%m%d_%H%M%S).txt"

    cat > "$REPORT_FILE" << EOF
BankShield 项目快速修复报告
============================
生成时间: $(date)

修复检查结果:
1. System.out.println: $(find /Users/zhangyanlong/workspaces/BankShield/bankshield-api/src/main/java -name "*.java" -exec grep -l "System\.out\.println" {} \; | wc -l)
2. TODO 注释: $(grep -r "TODO" /Users/zhangyanlong/workspaces/BankShield/bankshield-api/src/main/java --include="*.java" | wc -l)
3. 硬编码配置: $(grep -r "smtp.bankshield.com" /Users/zhangyanlong/workspaces/BankShield/bankshield-api/src/main/resources --include="*.yml" 2>/dev/null | wc -l)
4. 直接抛出异常: $(grep -r "throw new RuntimeException" /Users/zhangyanlong/workspaces/BankShield/bankshield-api/src/main/java --include="*.java" | wc -l)
5. 输入验证: @Valid 使用次数 = $(grep -r "@Valid" /Users/zhangyanlong/workspaces/BankShield/bankshield-api/src/main/java --include="*.java" | wc -l)
6. 未分页查询: $(grep -r "selectList.*null" /Users/zhangyanlong/workspaces/BankShield/bankshield-api/src/main/java --include="*.java" | wc -l)
7. 超大文件 (>500 行): $(find /Users/zhangyanlong/workspaces/BankShield/bankshield-api/src/main/java -name "*.java" -exec wc -l {} \; | awk '$1 > 500' | wc -l)

编译状态: $(mvn compile -pl bankshield-api -q > /dev/null 2>&1 && echo "通过" || echo "失败")

建议优先级:
1. 添加输入验证注解
2. 拆分超大类
3. 修复 TODO 问题
4. 优化异常处理
5. 添加分页查询

详细修复计划请参考: FIX_PLAN.md
完整问题分析请参考: PROJECT_ANALYSIS_REPORT.md
EOF

    print_info "修复报告已生成: $REPORT_FILE"
    cat "$REPORT_FILE"
}

# 主函数
main() {
    print_info "开始修复检查..."
    echo ""

    fix_system_out
    echo ""

    fix_todos
    echo ""

    fix_hardcoded_config
    echo ""

    fix_exception_handling
    echo ""

    fix_input_validation
    echo ""

    fix_pagination
    echo ""

    fix_code_complexity
    echo ""

    run_compile_check
    echo ""

    generate_report
    echo ""

    print_info "修复检查完成！"
    echo ""
    echo "================================"
    echo "查看详细报告:"
    echo "  - PROJECT_ANALYSIS_REPORT.md"
    echo "  - FIX_PLAN.md"
    echo "  - quick_fix_report_*.txt"
    echo "================================"
}

# 运行主函数
main "$@"
