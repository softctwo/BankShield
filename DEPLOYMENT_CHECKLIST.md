# BankShield 新模块部署检查清单

## 📋 部署前检查

### 环境检查
- [ ] JDK 11+ 已安装
- [ ] Maven 3.6+ 已安装
- [ ] Node.js 16+ 已安装
- [ ] MySQL 8.0+ 已安装并运行
- [ ] Redis 6.0+ 已安装并运行
- [ ] 所需端口未被占用（8080, 8081, 8082, 5173, 3306, 6379）

### 数据库准备
- [ ] 创建 bankshield 数据库
- [ ] 执行基础表结构初始化脚本
- [ ] 执行菜单集成脚本 `sql/menu_integration.sql`
- [ ] 验证菜单数据已正确插入
- [ ] 配置数据库连接信息（用户名、密码）

### 代码准备
- [ ] 从Git拉取最新代码
- [ ] 后端代码编译成功 `mvn clean install -DskipTests`
- [ ] 前端依赖安装成功 `npm install`
- [ ] 前端代码构建成功 `npm run build`

## 🚀 部署步骤

### 第一步：数据库初始化
```bash
# 1. 登录MySQL
mysql -u root -p3f342bb206

# 2. 创建数据库
CREATE DATABASE IF NOT EXISTS bankshield DEFAULT CHARACTER SET utf8mb4;

# 3. 执行初始化脚本
USE bankshield;
source /path/to/BankShield/sql/init_database.sql;
source /path/to/BankShield/sql/menu_integration.sql;

# 4. 验证
SELECT COUNT(*) FROM sys_menu WHERE menu_name IN ('区块链存证', '多方安全计算');
```
- [ ] 数据库创建成功
- [ ] 初始化脚本执行成功
- [ ] 菜单数据验证通过

### 第二步：后端部署

#### 方式A：开发环境（Maven）
```bash
# 启动主应用
cd bankshield-api
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 启动区块链模块（如需独立部署）
cd bankshield-blockchain
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 启动MPC模块（如需独立部署）
cd bankshield-mpc
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
- [ ] 主应用启动成功
- [ ] 区块链模块启动成功（如独立部署）
- [ ] MPC模块启动成功（如独立部署）

#### 方式B：生产环境（JAR包）
```bash
# 构建JAR包
mvn clean package -Pprod -DskipTests

# 启动服务
nohup java -jar bankshield-api/target/bankshield-api.jar \
  --spring.profiles.active=prod \
  > logs/api.log 2>&1 &

nohup java -jar bankshield-blockchain/target/bankshield-blockchain.jar \
  --spring.profiles.active=prod \
  > logs/blockchain.log 2>&1 &

nohup java -jar bankshield-mpc/target/bankshield-mpc.jar \
  --spring.profiles.active=prod \
  > logs/mpc.log 2>&1 &
```
- [ ] JAR包构建成功
- [ ] 服务启动成功
- [ ] 日志输出正常

### 第三步：验证后端服务

访问健康检查端点：
```bash
# 主应用
curl http://localhost:8080/actuator/health

# 区块链模块
curl http://localhost:8081/blockchain/health

# MPC模块
curl http://localhost:8082/mpc/health
```
- [ ] 主应用健康检查通过
- [ ] 区块链模块健康检查通过
- [ ] MPC模块健康检查通过
- [ ] Swagger文档可访问 http://localhost:8080/swagger-ui.html

### 第四步：前端部署

#### 开发环境
```bash
cd bankshield-ui
npm run dev
```
- [ ] 开发服务器启动成功
- [ ] 可访问 http://localhost:5173

#### 生产环境
```bash
cd bankshield-ui
npm run build

# 使用Nginx部署
sudo cp -r dist/* /var/www/html/bankshield/
sudo systemctl restart nginx
```
- [ ] 构建成功
- [ ] 静态文件部署成功
- [ ] Nginx配置正确
- [ ] 可访问生产地址

### 第五步：功能验证

#### 登录验证
- [ ] 可以正常登录（admin/admin123）
- [ ] 登录后跳转到Dashboard
- [ ] 左侧菜单正常显示

#### 菜单验证
- [ ] "区块链存证"菜单显示
- [ ] "多方安全计算"菜单显示
- [ ] 所有子菜单可点击

#### 区块链存证功能
- [ ] 存证概览页面加载成功
- [ ] 统计数据显示正常
- [ ] 网络状态查询成功
- [ ] 存证记录页面加载成功
- [ ] 记录查询功能正常
- [ ] 详情查看功能正常
- [ ] 验证功能正常
- [ ] 证书生成功能正常

#### MPC功能
- [ ] MPC概览页面加载成功
- [ ] 任务统计显示正常
- [ ] 协议信息显示正常
- [ ] 任务列表页面加载成功
- [ ] 任务查询功能正常
- [ ] 任务详情查看正常
- [ ] 任务取消功能正常（如有运行中任务）

## 🔍 测试验证

### API测试
```bash
# 获取Token（根据实际登录接口调整）
TOKEN=$(curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  | jq -r '.data.token')

# 测试区块链API
curl -X GET http://localhost:8080/blockchain/statistics \
  -H "Authorization: Bearer $TOKEN"

# 测试MPC API
curl -X GET http://localhost:8082/mpc/statistics \
  -H "Authorization: Bearer $TOKEN"
```
- [ ] 登录接口正常
- [ ] Token获取成功
- [ ] 区块链API调用成功
- [ ] MPC API调用成功

### 前端测试
- [ ] 页面加载速度正常（< 3秒）
- [ ] 无JavaScript错误
- [ ] 无网络请求失败
- [ ] 数据展示正确
- [ ] 交互功能正常

### 性能测试
```bash
# 使用Apache Bench测试
ab -n 1000 -c 10 http://localhost:8080/blockchain/statistics

# 使用JMeter测试
jmeter -n -t tests/performance/bankshield-performance-test.jmx
```
- [ ] 响应时间 < 500ms
- [ ] 并发处理正常
- [ ] 无内存泄漏
- [ ] CPU使用率正常

## 🔐 安全检查

### 配置安全
- [ ] 修改默认管理员密码
- [ ] 数据库密码已加密存储
- [ ] Redis密码已配置
- [ ] JWT密钥已更换
- [ ] 敏感配置已加密

### 访问控制
- [ ] 角色权限配置正确
- [ ] 菜单权限验证正常
- [ ] API接口鉴权正常
- [ ] 跨域配置正确

### 数据安全
- [ ] SQL注入防护已启用
- [ ] XSS防护已启用
- [ ] CSRF防护已启用
- [ ] 敏感数据已脱敏

## 📊 监控配置

### 日志监控
- [ ] 应用日志正常输出
- [ ] 错误日志正常记录
- [ ] 审计日志正常记录
- [ ] 日志轮转配置正确

### 性能监控
- [ ] Prometheus监控已配置
- [ ] Grafana Dashboard已配置
- [ ] 告警规则已配置
- [ ] 监控指标正常采集

### 健康检查
- [ ] Actuator端点已启用
- [ ] 健康检查接口正常
- [ ] 数据库连接监控正常
- [ ] Redis连接监控正常

## 🔄 回滚准备

### 备份
- [ ] 数据库已备份
- [ ] 配置文件已备份
- [ ] 旧版本代码已标记
- [ ] 回滚脚本已准备

### 回滚步骤
```bash
# 1. 停止新服务
./scripts/stop.sh

# 2. 回滚数据库
mysql -u root -p bankshield < sql/menu_rollback.sql

# 3. 恢复旧版本
git checkout v1.0.0
mvn clean install -DskipTests

# 4. 启动旧服务
./scripts/start.sh
```
- [ ] 回滚步骤已测试
- [ ] 回滚脚本可执行

## 📝 文档检查

### 技术文档
- [ ] API文档已更新
- [ ] 架构文档已更新
- [ ] 数据库设计文档已更新
- [ ] 部署文档已更新

### 用户文档
- [ ] 用户手册已更新
- [ ] 快速开始指南已提供
- [ ] 常见问题已整理
- [ ] 视频教程已录制（可选）

## ✅ 最终确认

### 部署确认
- [ ] 所有服务正常运行
- [ ] 所有功能测试通过
- [ ] 性能指标达标
- [ ] 安全检查通过
- [ ] 监控告警正常
- [ ] 文档齐全

### 上线准备
- [ ] 项目负责人已审批
- [ ] 运维团队已通知
- [ ] 用户已通知
- [ ] 上线时间已确定
- [ ] 应急预案已准备

## 📞 联系方式

### 技术支持
- **开发团队**: dev@bankshield.com
- **运维团队**: ops@bankshield.com
- **紧急联系**: +86-xxx-xxxx-xxxx

### 问题反馈
- **Issue跟踪**: https://github.com/bankshield/issues
- **技术论坛**: https://forum.bankshield.com
- **在线文档**: https://docs.bankshield.com

---

## 📋 部署签字确认

| 角色 | 姓名 | 签字 | 日期 |
|------|------|------|------|
| 开发负责人 | | | |
| 测试负责人 | | | |
| 运维负责人 | | | |
| 项目经理 | | | |

---

**部署完成时间**: _______________

**部署人员**: _______________

**备注**: _______________
