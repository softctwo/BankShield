#!/bin/bash

# BankShield AI智能安全分析模块构建脚本
# Author: BankShield
# Version: 1.0.0

set -e

echo "=========================================="
echo "BankShield AI模块构建脚本"
echo "=========================================="

# 设置变量
PROJECT_ROOT=$(cd "$(dirname "$0")/.." && pwd)
AI_MODULE_DIR="$PROJECT_ROOT/bankshield-ai"
BUILD_DIR="$AI_MODULE_DIR/target"
LOGS_DIR="$PROJECT_ROOT/logs"

# 创建日志目录
mkdir -p "$LOGS_DIR"

# 记录开始时间
START_TIME=$(date +%s)
echo "开始时间: $(date)"

# 函数：检查命令是否存在
check_command() {
    if ! command -v "$1" &> /dev/null; then
        echo "错误: $1 未安装"
        exit 1
    fi
}

# 函数：清理构建文件
clean_build() {
    echo "清理构建文件..."
    cd "$AI_MODULE_DIR"
    if [ -d "$BUILD_DIR" ]; then
        rm -rf "$BUILD_DIR"
        echo "已删除构建目录: $BUILD_DIR"
    fi
}

# 函数：运行测试
run_tests() {
    echo "运行单元测试..."
    cd "$AI_MODULE_DIR"
    
    # 运行测试并生成报告
    mvn test -q > "$LOGS_DIR/ai-test.log" 2>&1
    
    if [ $? -eq 0 ]; then
        echo "单元测试通过 ✓"
    else
        echo "单元测试失败 ✗"
        echo "查看测试日志: $LOGS_DIR/ai-test.log"
        exit 1
    fi
}

# 函数：构建项目
build_project() {
    echo "构建AI模块..."
    cd "$AI_MODULE_DIR"
    
    # 执行Maven构建
    echo "执行Maven构建..."
    mvn clean package -DskipTests -q > "$LOGS_DIR/ai-build.log" 2>&1
    
    if [ $? -eq 0 ]; then
        echo "构建成功 ✓"
        echo "构建日志: $LOGS_DIR/ai-build.log"
    else
        echo "构建失败 ✗"
        echo "查看构建日志: $LOGS_DIR/ai-build.log"
        exit 1
    fi
}

# 函数：检查构建结果
check_build_result() {
    echo "检查构建结果..."
    
    JAR_FILE="$BUILD_DIR/bankshield-ai-1.0.0-SNAPSHOT.jar"
    
    if [ -f "$JAR_FILE" ]; then
        echo "构建产物检查通过 ✓"
        echo "JAR文件: $JAR_FILE"
        
        # 显示文件大小
        FILE_SIZE=$(du -h "$JAR_FILE" | cut -f1)
        echo "文件大小: $FILE_SIZE"
        
        # 检查文件是否可执行
        if [ -r "$JAR_FILE" ]; then
            echo "JAR文件可读 ✓"
        else
            echo "JAR文件不可读 ✗"
            exit 1
        fi
    else
        echo "构建产物不存在 ✗"
        echo "预期文件: $JAR_FILE"
        exit 1
    fi
}

# 函数：生成构建报告
generate_build_report() {
    echo "生成构建报告..."
    
    END_TIME=$(date +%s)
    BUILD_DURATION=$((END_TIME - START_TIME))
    
    REPORT_FILE="$LOGS_DIR/ai-build-report.txt"
    
    cat > "$REPORT_FILE" << EOF
BankShield AI模块构建报告
=====================================
构建时间: $(date)
构建耗时: ${BUILD_DURATION}秒
项目根目录: $PROJECT_ROOT
构建目录: $BUILD_DIR

构建结果:
- JAR文件: $JAR_FILE
- 文件大小: $FILE_SIZE
- 构建状态: 成功

测试状态:
- 单元测试: 通过

构建日志:
- 详细日志: $LOGS_DIR/ai-build.log
- 测试日志: $LOGS_DIR/ai-test.log

下一步操作:
1. 可以运行启动脚本启动AI模块
2. 检查配置文件是否正确
3. 验证API接口是否正常工作
=====================================
EOF

    echo "构建报告已生成: $REPORT_FILE"
    cat "$REPORT_FILE"
}

# 函数：安装依赖
install_dependencies() {
    echo "检查并安装依赖..."
    
    # 检查Maven
    check_command mvn
    
    # 检查Java
    check_command java
    
    echo "依赖检查通过 ✓"
}

# 函数：主构建流程
main() {
    echo "开始AI模块构建流程..."
    
    # 1. 安装依赖
    install_dependencies
    
    # 2. 清理构建文件
    clean_build
    
    # 3. 运行测试
    run_tests
    
    # 4. 构建项目
    build_project
    
    # 5. 检查构建结果
    check_build_result
    
    # 6. 生成构建报告
    generate_build_report
    
    echo "=========================================="
    echo "AI模块构建完成！"
    echo "=========================================="
    echo "构建耗时: ${BUILD_DURATION}秒"
    echo "请查看构建报告了解详细信息"
    echo "下一步: 运行启动脚本启动AI模块"
}

# 处理命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        --clean)
            clean_build
            exit 0
            ;;
        --test-only)
            run_tests
            exit 0
            ;;
        --skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        --help)
            echo "用法: $0 [选项]"
            echo "选项:"
            echo "  --clean        仅清理构建文件"
            echo "  --test-only    仅运行测试"
            echo "  --skip-tests   跳过测试阶段"
            echo "  --help         显示帮助信息"
            exit 0
            ;;
        *)
            echo "未知参数: $1"
            echo "使用 --help 查看帮助信息"
            exit 1
            ;;
    esac
done

# 执行主函数
main "$@"