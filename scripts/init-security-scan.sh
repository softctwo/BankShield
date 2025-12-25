#!/bin/bash

# BankShield 安全扫描模块初始化脚本

echo "========================================="
echo "BankShield 安全扫描模块初始化"
echo "========================================="

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误：未找到Java环境，请先安装Java 8或更高版本"
    exit 1
fi

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "错误：未找到Maven环境，请先安装Maven"
    exit 1
fi

# 检查MySQL连接
echo "正在检查数据库连接..."
mysql -u root -p123456 -e "SELECT 1" bankshield > /dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "错误：无法连接到MySQL数据库，请检查数据库配置"
    exit 1
fi

echo "数据库连接正常"

# 执行数据库初始化脚本
echo "正在初始化安全扫描模块数据库..."
mysql -u root -p123456 bankshield < sql/security_scan_module.sql
if [ $? -eq 0 ]; then
    echo "数据库初始化成功"
else
    echo "数据库初始化失败"
    exit 1
fi

# 构建后端项目
echo "正在构建后端项目..."
cd bankshield-api
mvn clean compile -DskipTests
if [ $? -eq 0 ]; then
    echo "后端项目构建成功"
else
    echo "后端项目构建失败"
    exit 1
fi

# 构建前端项目
echo "正在构建前端项目..."
cd ../bankshield-ui
npm install
if [ $? -eq 0 ]; then
    echo "前端依赖安装成功"
else
    echo "前端依赖安装失败"
    exit 1
fi

npm run build
if [ $? -eq 0 ]; then
    echo "前端项目构建成功"
else
    echo "前端项目构建失败"
    exit 1
fi

# 返回项目根目录
cd ..

# 初始化安全基线
echo "正在初始化安全基线配置..."
cat > init_baseline.sql << EOF
-- 初始化安全基线数据
INSERT INTO security_baseline (check_item_name, compliance_standard, check_type, risk_level, description, remedy_advice, builtin, created_by) VALUES
('用户密码复杂度策略', 'GB_LEVEL3', 'PASSWORD', 'HIGH', '检查密码是否符合复杂度要求（8位以上，包含大小写字母、数字、特殊字符）', '配置密码策略：minimum-length=8, require-uppercase=true, require-lowercase=true, require-digit=true, require-special=true', 1, 'system'),
('会话超时自动退出', 'GB_LEVEL3', 'SESSION', 'MEDIUM', '检查会话超时是否设置为30分钟', '设置session-timeout=30分钟，配置最大会话数限制', 1, 'system'),
('敏感数据加密传输', 'GB_LEVEL3', 'ENCRYPTION', 'HIGH', '检查是否使用HTTPS加密传输', '强制使用HTTPS，禁用HTTP访问，配置HSTS', 1, 'system'),
('数据库连接密码加密存储', 'GB_LEVEL3', 'ENCRYPTION', 'HIGH', '检查数据库密码是否加密存储', '使用Jasypt加密配置文件中的数据库密码', 1, 'system'),
('文件上传大小限制', 'GB_LEVEL3', 'ACCESS_CONTROL', 'MEDIUM', '检查文件上传是否限制大小和类型', '配置max-file-size=10MB, allowed-file-types=jpg,png,pdf,doc', 1, 'system'),
('SQL注入防护', 'GB_LEVEL3', 'INPUT_VALIDATION', 'CRITICAL', '检查是否对所有输入进行SQL注入过滤', '使用MyBatis参数化查询，禁止拼接SQL', 1, 'system'),
('XSS攻击防护', 'GB_LEVEL3', 'INPUT_VALIDATION', 'HIGH', '检查是否对所有输出进行XSS过滤', '使用HTML转义库，过滤script标签', 1, 'system'),
('CSRF令牌验证', 'GB_LEVEL3', 'AUTH', 'HIGH', '检查是否启用CSRF防护', '启用Spring Security CSRF保护，所有POST请求验证令牌', 1, 'system'),
('审计日志完整性', 'GB_LEVEL3', 'AUDIT', 'MEDIUM', '检查审计日志是否完整记录', '确保所有增删改操作记录到audit_operation表', 1, 'system'),
('密钥管理合规性', 'GB_LEVEL3', 'KEY_MANAGEMENT', 'HIGH', '检查密钥是否符合管理要求', '使用国密算法，密钥定期轮换（90天），密钥材料加密存储', 1, 'system');
EOF

mysql -u root -p123456 bankshield < init_baseline.sql
if [ $? -eq 0 ]; then
    echo "安全基线初始化成功"
    rm init_baseline.sql
else
    echo "安全基线初始化失败"
    exit 1
fi

# 创建报告目录
echo "正在创建报告目录..."
mkdir -p bankshield-api/reports/security-scans
mkdir -p logs

echo "========================================="
echo "安全扫描模块初始化完成！"
echo "========================================="
echo ""
echo "请按以下步骤启动系统："
echo "1. 启动后端服务：cd bankshield-api && mvn spring-boot:run"
echo "2. 启动前端服务：cd bankshield-ui && npm run dev"
echo "3. 访问系统：http://localhost:3000"
echo ""
echo "安全扫描模块功能："
echo "- 安全扫描任务管理"
echo "- 扫描结果管理"
echo "- 安全基线配置"
echo "- 定时自动扫描"
echo "- 扫描报告生成"
echo ""
echo "默认登录账号："
echo "- 用户名：admin"
echo "- 密码：123456"
echo ""
echo "如需帮助，请查看项目文档或联系技术支持。"