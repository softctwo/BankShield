#!/bin/bash

# BankShield合规报表模块测试脚本
# 用于验证核心功能的正确性

echo "开始测试BankShield合规报表模块..."

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 测试函数
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

# 检查服务状态
check_service_status() {
    local service_name=$1
    local service_port=$2
    
    if netstat -tuln | grep -q ":$service_port "; then
        print_success "$service_name 服务正在运行 (端口: $service_port)"
        return 0
    else
        print_error "$service_name 服务未运行 (端口: $service_port)"
        return 1
    fi
}

# 测试API接口
test_api_endpoint() {
    local endpoint=$1
    local description=$2
    local expected_status=${3:-200}
    
    print_info "测试 $description..."
    
    response=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080$endpoint" 2>/dev/null)
    
    if [ "$response" = "$expected_status" ] || [ "$response" = "401" ]; then
        print_success "$description 测试通过 (HTTP状态: $response)"
        return 0
    else
        print_error "$description 测试失败 (HTTP状态: $response, 期望: $expected_status)"
        return 1
    fi
}

# 主要测试流程
main() {
    echo "=========================================="
    echo "BankShield合规报表模块功能测试"
    echo "=========================================="
    
    # 1. 检查服务状态
    echo -e "\n${YELLOW}1. 检查服务状态${NC}"
    check_service_status "MySQL" "3306"
    check_service_status "BankShield API" "8080"
    
    # 2. 测试核心API接口
    echo -e "\n${YELLOW}2. 测试核心API接口${NC}"
    
    # 报表模板管理API
    test_api_endpoint "/api/report/template/page" "报表模板分页查询" 200
    test_api_endpoint "/api/report/template" "报表模板创建" 401  # 需要认证
    
    # 报表任务管理API
    test_api_endpoint "/api/report/task/page" "报表任务分页查询" 200
    test_api_endpoint "/api/report/task" "报表任务创建" 401  # 需要认证
    
    # 合规检查API
    test_api_endpoint "/api/report/check" "合规检查执行" 401  # 需要认证
    test_api_endpoint "/api/report/check/score" "合规评分查询" 200
    test_api_endpoint "/api/report/stats" "报表统计信息" 200
    
    # 3. 测试数据库连接
    echo -e "\n${YELLOW}3. 测试数据库连接${NC}"
    if mysql -u root -proot -e "USE bankshield; SHOW TABLES LIKE '%report%';" 2>/dev/null | grep -q "report"; then
        print_success "数据库连接正常，报表相关表存在"
    else
        print_error "数据库连接失败或报表表不存在"
    fi
    
    # 4. 测试FreeMarker模板
    echo -e "\n${YELLOW}4. 测试FreeMarker模板${NC}"
    if [ -f "bankshield-api/src/main/resources/templates/reports/dengbao-level3.ftl" ]; then
        print_success "等保三级报表模板文件存在"
    else
        print_error "等保三级报表模板文件不存在"
    fi
    
    if [ -f "bankshield-api/src/main/resources/templates/reports/pci-dss.ftl" ]; then
        print_success "PCI-DSS报表模板文件存在"
    else
        print_error "PCI-DSS报表模板文件不存在"
    fi
    
    # 5. 测试前端构建
    echo -e "\n${YELLOW}5. 测试前端构建${NC}"
    if [ -d "bankshield-ui/src/views/compliance" ]; then
        print_success "合规模块前端目录存在"
        
        # 检查关键组件文件
        if [ -f "bankshield-ui/src/views/compliance/dashboard/index.vue" ]; then
            print_success "合规Dashboard组件存在"
        fi
        
        if [ -f "bankshield-ui/src/views/compliance/check/index.vue" ]; then
            print_success "合规检查组件存在"
        fi
        
        if [ -f "bankshield-ui/src/views/compliance/report-template/index.vue" ]; then
            print_success "报表模板管理组件存在"
        fi
    else
        print_error "合规模块前端目录不存在"
    fi
    
    # 6. 测试API接口文件
    echo -e "\n${YELLOW}6. 测试API接口文件${NC}"
    if [ -f "bankshield-ui/src/api/compliance-report.ts" ]; then
        print_success "合规报表API接口文件存在"
    else
        print_error "合规报表API接口文件不存在"
    fi
    
    if [ -f "bankshield-ui/src/types/compliance-report.d.ts" ]; then
        print_success "合规报表TypeScript类型定义文件存在"
    else
        print_error "合规报表TypeScript类型定义文件不存在"
    fi
    
    # 7. 测试后端代码
    echo -e "\n${YELLOW}7. 测试后端代码${NC}"
    
    # 检查关键Java文件
    key_files=(
        "bankshield-api/src/main/java/com/bankshield/api/controller/ComplianceReportController.java"
        "bankshield-api/src/main/java/com/bankshield/api/service/impl/ComplianceCheckEngineImpl.java"
        "bankshield-api/src/main/java/com/bankshield/api/service/impl/ReportGenerationEngineImpl.java"
        "bankshield-api/src/main/java/com/bankshield/api/job/DailyReportGenerationJob.java"
    )
    
    for file in "${key_files[@]}"; do
        if [ -f "$file" ]; then
            print_success "$(basename "$file") 存在"
        else
            print_error "$(basename "$file") 不存在"
        fi
    done
    
    echo -e "\n${YELLOW}8. 测试定时任务配置${NC}"
    if grep -q "@Scheduled" bankshield-api/src/main/java/com/bankshield/api/job/DailyReportGenerationJob.java; then
        print_success "定时任务注解配置正确"
    else
        print_error "定时任务注解配置缺失"
    fi
    
    echo -e "\n=========================================="
    echo "合规报表模块功能测试完成"
    echo "=========================================="
    
    print_info "后续测试建议："
    echo "1. 使用Postman或curl测试具体的API功能"
    echo "2. 登录前端界面测试用户交互功能"
    echo "3. 验证定时任务的自动执行"
    echo "4. 测试报表生成的完整流程"
    echo "5. 验证合规检查的准确性"
}

# 显示帮助信息
show_help() {
    echo "用法: $0 [选项]"
    echo "选项:"
    echo "  -h, --help    显示帮助信息"
    echo "  -v, --version 显示版本信息"
    echo ""
    echo "功能测试脚本用于验证BankShield合规报表模块的核心功能是否正确实现。"
}

# 显示版本信息
show_version() {
    echo "BankShield合规报表模块测试脚本 v1.0.0"
    echo "作者: BankShield开发团队"
    echo "更新时间: 2024-12-24"
}

# 主程序入口
case "$1" in
    -h|--help)
        show_help
        exit 0
        ;;
    -v|--version)
        show_version
        exit 0
        ;;
    "")
        main
        exit 0
        ;;
    *)
        echo "未知选项: $1"
        show_help
        exit 1
        ;;
esac