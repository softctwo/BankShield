# BankShield 模块集成测试指南

## 📋 文档概述

本文档提供了BankShield项目所有模块的完整集成测试指南，包括测试环境准备、测试用例、验证步骤和预期结果。

**文档版本**: v1.0.0  
**更新日期**: 2024-12-31  
**适用范围**: 所有BankShield模块

---

## 🎯 测试目标

### 核心目标
- ✅ 验证所有模块的后端API功能正常
- ✅ 验证所有模块的前端页面可访问
- ✅ 验证模块间的数据交互正确
- ✅ 验证路由配置和菜单显示正常
- ✅ 验证权限控制和安全机制有效

### 测试范围
- **区块链存证模块** (blockchain)
- **多方安全计算模块** (mpc)
- **AI智能分析模块** (ai)
- **监控管理模块** (monitor)
- **数据血缘模块** (lineage)
- **数据加密模块** (encrypt)
- **审计管理模块** (audit)
- **分类分级模块** (classification)

---

## 🔧 测试环境准备

### 1. 环境要求

#### 软件环境
```bash
# 必需软件
- Java 11+
- Node.js 16+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+
- npm 8+

# 可选软件（用于高级功能）
- Docker 20+
- Kubernetes 1.20+
- Fabric 2.2+（区块链模块）
```

#### 硬件要求
```
- CPU: 4核心以上
- 内存: 8GB以上
- 磁盘: 50GB可用空间
- 网络: 稳定的互联网连接
```

### 2. 数据库准备

```bash
# 创建数据库
mysql -u root -p3f342bb206 -e "CREATE DATABASE IF NOT EXISTS bankshield DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入基础数据
mysql -u root -p3f342bb206 bankshield < sql/init_database.sql

# 导入菜单数据
mysql -u root -p3f342bb206 bankshield < sql/menu_integration.sql

# 验证数据导入
mysql -u root -p3f342bb206 bankshield -e "SELECT COUNT(*) FROM sys_menu;"
```

### 3. 启动服务

```bash
# 启动后端服务
cd bankshield-api
mvn clean spring-boot:run -Dspring.profiles.active=dev

# 启动前端服务（新终端）
cd bankshield-ui
npm install
npm run dev

# 验证服务启动
curl http://localhost:8080/actuator/health
curl http://localhost:5173
```

---

## 🧪 模块测试用例

### 1. 区块链存证模块测试

#### 1.1 后端API测试

**测试用例1: 批量存证**
```bash
curl -X POST http://localhost:8080/blockchain/anchor/batch \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "records": [
      {
        "recordId": "audit_001",
        "recordType": "AUDIT_LOG",
        "data": "审计日志数据1"
      },
      {
        "recordId": "audit_002",
        "recordType": "AUDIT_LOG",
        "data": "审计日志数据2"
      }
    ]
  }'
```

**预期结果**:
```json
{
  "code": 200,
  "message": "批量存证成功",
  "data": {
    "successCount": 2,
    "failCount": 0,
    "records": [...]
  }
}
```

**测试用例2: 完整性验证**
```bash
curl -X GET http://localhost:8080/blockchain/verify/audit/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**预期结果**:
```json
{
  "code": 200,
  "message": "验证成功",
  "data": true
}
```

**测试用例3: 网络状态查询**
```bash
curl -X GET http://localhost:8080/blockchain/network/status \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**预期结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "networkName": "BankShield-Fabric",
    "channelCount": 1,
    "peerCount": 4,
    "blockHeight": 100,
    "status": "ACTIVE"
  }
}
```

#### 1.2 前端页面测试

**测试步骤**:
1. 登录系统: http://localhost:5173/login (admin/admin123)
2. 导航到: 区块链存证 > 存证概览
3. 验证页面元素:
   - ✅ 统计卡片显示正常（存证总数、验证成功率等）
   - ✅ 网络状态图表显示正常
   - ✅ 合规检查列表显示正常
   - ✅ 最近存证记录显示正常

4. 导航到: 区块链存证 > 存证记录
5. 测试功能:
   - ✅ 记录列表加载正常
   - ✅ 筛选功能正常（按类型、状态）
   - ✅ 查看详情弹窗正常
   - ✅ 验证完整性功能正常
   - ✅ 生成证书功能正常

---

### 2. 多方安全计算模块测试

#### 2.1 后端API测试

**测试用例1: 隐私求交（PSI）**
```bash
curl -X POST http://localhost:8080/mpc/psi \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "partyIds": [1, 2],
    "dataSet": ["user1", "user2", "user3"],
    "protocol": "ECDH_PSI"
  }'
```

**预期结果**:
```json
{
  "code": 200,
  "message": "PSI计算成功",
  "data": {
    "jobId": 1,
    "intersectionSize": 2,
    "intersection": ["user1", "user2"]
  }
}
```

**测试用例2: 安全求和**
```bash
curl -X POST http://localhost:8080/mpc/secure-sum \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "partyIds": [1, 2, 3],
    "localValue": 100
  }'
```

**预期结果**:
```json
{
  "code": 200,
  "message": "安全求和成功",
  "data": {
    "jobId": 2,
    "sum": 300,
    "partyCount": 3
  }
}
```

**测试用例3: 任务查询**
```bash
curl -X GET http://localhost:8080/mpc/jobs?type=PSI&status=COMPLETED \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**预期结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 10,
    "list": [...]
  }
}
```

#### 2.2 前端页面测试

**测试步骤**:
1. 导航到: 多方安全计算 > MPC概览
2. 验证页面元素:
   - ✅ 任务统计卡片显示正常
   - ✅ 任务类型分布图表显示正常
   - ✅ 参与方状态显示正常
   - ✅ 协议信息展示正常
   - ✅ 快速操作按钮正常

3. 导航到: 多方安全计算 > 任务列表
4. 测试功能:
   - ✅ 任务列表加载正常
   - ✅ 筛选功能正常（按类型、状态）
   - ✅ 查看详情弹窗正常
   - ✅ 取消任务功能正常

---

### 3. AI智能分析模块测试

#### 3.1 后端API测试

**测试用例1: 异常检测**
```bash
curl -X POST http://localhost:8080/ai/behavior/detect \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "userId": "user001",
    "loginTime": "2024-12-31T10:00:00",
    "loginIp": "192.168.1.100",
    "operationType": "DATA_EXPORT",
    "dataVolume": 10000
  }'
```

**预期结果**:
```json
{
  "code": 200,
  "message": "检测成功",
  "data": 0.85
}
```

**测试用例2: 威胁预测**
```bash
curl -X POST http://localhost:8080/ai/threat/predict \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "timeRange": 7,
    "threatType": "DATA_BREACH"
  }'
```

**预期结果**:
```json
{
  "code": 200,
  "message": "预测成功",
  "data": {
    "probability": 0.15,
    "riskLevel": "MEDIUM",
    "factors": [...]
  }
}
```

#### 3.2 前端页面测试

**测试步骤**:
1. 导航到: AI智能分析 > AI分析大屏
2. 验证页面元素:
   - ✅ 异常行为统计显示正常
   - ✅ 威胁趋势图表显示正常
   - ✅ 模型性能指标显示正常
   - ✅ 实时告警列表显示正常

3. 导航到: AI智能分析 > 异常行为管理
4. 测试功能:
   - ✅ 异常列表加载正常
   - ✅ 筛选和搜索功能正常
   - ✅ 查看详情功能正常
   - ✅ 处理异常功能正常

---

### 4. 监控管理模块测试

#### 4.1 后端API测试

**测试用例1: 系统健康检查**
```bash
curl -X GET http://localhost:8080/monitor/health \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**预期结果**:
```json
{
  "code": 200,
  "message": "系统健康",
  "data": {
    "status": "UP",
    "components": {
      "database": "UP",
      "redis": "UP",
      "diskSpace": "UP"
    }
  }
}
```

**测试用例2: 告警查询**
```bash
curl -X GET http://localhost:8080/monitor/alerts/recent?limit=10 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**预期结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "level": "WARNING",
      "message": "CPU使用率超过80%",
      "timestamp": "2024-12-31T10:00:00"
    }
  ]
}
```

#### 4.2 前端页面测试

**测试步骤**:
1. 导航到: 监控管理 > 监控大屏
2. 验证页面元素:
   - ✅ 系统指标卡片显示正常
   - ✅ 资源使用率图表显示正常
   - ✅ 告警趋势图表显示正常
   - ✅ 实时日志流显示正常

---

### 5. 数据血缘模块测试

#### 5.1 后端API测试

**测试用例1: 血缘图查询**
```bash
curl -X GET http://localhost:8080/lineage/graph/1?depth=3 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**预期结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "nodes": [...],
    "edges": [...],
    "depth": 3
  }
}
```

**测试用例2: 影响分析**
```bash
curl -X POST http://localhost:8080/lineage/impact/analyze \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "assetId": 1,
    "changeType": "SCHEMA_CHANGE"
  }'
```

**预期结果**:
```json
{
  "code": 200,
  "message": "分析成功",
  "data": {
    "impactedAssets": 5,
    "impactLevel": "HIGH",
    "details": [...]
  }
}
```

#### 5.2 前端页面测试

**测试步骤**:
1. 导航到: 数据血缘 > 血缘关系图
2. 验证页面元素:
   - ✅ 搜索表单显示正常
   - ✅ 血缘图可视化显示正常
   - ✅ 统计信息显示正常
   - ✅ 导出功能正常

3. 测试交互功能:
   - ✅ 输入资产ID并查询
   - ✅ 调整血缘深度
   - ✅ 切换血缘方向
   - ✅ 缩放和拖拽图表
   - ✅ 导出图表为PNG

---

## 🔐 权限和安全测试

### 1. 登录认证测试

**测试用例1: 正常登录**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**预期结果**: 返回JWT Token

**测试用例2: 错误密码**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "wrong_password"
  }'
```

**预期结果**: 返回401错误

### 2. 权限控制测试

**测试用例: 未授权访问**
```bash
curl -X GET http://localhost:8080/blockchain/network/status
```

**预期结果**: 返回401或403错误

**测试用例: 角色权限**
- 使用普通用户登录
- 尝试访问管理员功能
- 验证是否被正确拒绝

---

## 📊 性能测试

### 1. 并发测试

使用Apache JMeter或k6进行并发测试：

```bash
# 使用k6进行性能测试
cd tests/k6
k6 run performance-test.js
```

**测试指标**:
- 并发用户数: 100
- 测试时长: 5分钟
- 预期响应时间: < 500ms (P95)
- 预期错误率: < 1%

### 2. 压力测试

```bash
# API压力测试
ab -n 10000 -c 100 http://localhost:8080/blockchain/statistics
```

**预期结果**:
- 吞吐量: > 1000 req/s
- 平均响应时间: < 200ms
- 成功率: > 99%

---

## 🐛 常见问题排查

### 1. 前端无法访问

**问题**: 访问 http://localhost:5173 显示无法连接

**排查步骤**:
```bash
# 检查前端进程
ps aux | grep vite

# 检查端口占用
lsof -i :5173

# 重启前端服务
cd bankshield-ui
npm run dev
```

### 2. 后端API报错

**问题**: API调用返回500错误

**排查步骤**:
```bash
# 查看后端日志
tail -f bankshield-api/logs/bankshield.log

# 检查数据库连接
mysql -u root -p3f342bb206 -e "SELECT 1"

# 检查Redis连接
redis-cli ping
```

### 3. 菜单不显示

**问题**: 登录后看不到新模块菜单

**排查步骤**:
```bash
# 检查菜单数据
mysql -u root -p3f342bb206 bankshield -e "SELECT * FROM sys_menu WHERE menu_name LIKE '%区块链%' OR menu_name LIKE '%MPC%';"

# 重新执行菜单脚本
mysql -u root -p3f342bb206 bankshield < sql/menu_integration.sql

# 清除浏览器缓存并重新登录
```

### 4. TypeScript类型错误

**问题**: 前端编译时出现类型错误

**解决方案**:
```bash
# 重新安装依赖
cd bankshield-ui
rm -rf node_modules package-lock.json
npm install

# 清除TypeScript缓存
rm -rf .tsbuildinfo
```

---

## ✅ 测试检查清单

### 基础功能测试
- [ ] 用户登录/登出功能正常
- [ ] 所有模块菜单显示正常
- [ ] 所有页面可正常访问
- [ ] 页面布局和样式正常

### 区块链存证模块
- [ ] 存证概览页面数据正常
- [ ] 存证记录列表加载正常
- [ ] 批量存证功能正常
- [ ] 完整性验证功能正常
- [ ] 证书生成功能正常
- [ ] 网络状态监控正常

### 多方安全计算模块
- [ ] MPC概览页面数据正常
- [ ] 任务列表加载正常
- [ ] PSI计算功能正常
- [ ] 安全求和功能正常
- [ ] 联合查询功能正常
- [ ] 任务管理功能正常

### AI智能分析模块
- [ ] AI分析大屏数据正常
- [ ] 异常检测功能正常
- [ ] 威胁预测功能正常
- [ ] 模型管理功能正常
- [ ] 告警分类功能正常

### 监控管理模块
- [ ] 监控大屏数据正常
- [ ] 系统指标显示正常
- [ ] 告警管理功能正常
- [ ] 日志查询功能正常

### 数据血缘模块
- [ ] 血缘关系图显示正常
- [ ] 血缘查询功能正常
- [ ] 影响分析功能正常
- [ ] 图表交互功能正常
- [ ] 导出功能正常

### 性能测试
- [ ] 并发测试通过
- [ ] 压力测试通过
- [ ] 响应时间符合要求
- [ ] 资源使用率正常

### 安全测试
- [ ] 登录认证正常
- [ ] 权限控制有效
- [ ] 未授权访问被拒绝
- [ ] 敏感数据加密正常

---

## 📝 测试报告模板

### 测试执行记录

| 测试项 | 测试时间 | 测试人员 | 测试结果 | 备注 |
|--------|----------|----------|----------|------|
| 区块链存证模块 | 2024-12-31 | 张三 | ✅ 通过 | - |
| MPC模块 | 2024-12-31 | 李四 | ✅ 通过 | - |
| AI模块 | 2024-12-31 | 王五 | ⚠️ 部分通过 | 模型训练功能待优化 |
| 监控模块 | 2024-12-31 | 赵六 | ✅ 通过 | - |
| 血缘模块 | 2024-12-31 | 孙七 | ✅ 通过 | - |

### 问题汇总

| 问题ID | 模块 | 问题描述 | 严重程度 | 状态 | 负责人 |
|--------|------|----------|----------|------|--------|
| BUG-001 | AI模块 | 模型训练速度较慢 | 中 | 待修复 | 李四 |
| BUG-002 | 前端 | TypeScript类型警告 | 低 | 已知问题 | 张三 |

---

## 🎓 最佳实践

### 1. 测试前准备
- 确保测试环境独立，不影响生产环境
- 备份数据库，以便测试后恢复
- 准备充足的测试数据
- 记录测试环境配置

### 2. 测试执行
- 按照测试用例顺序执行
- 记录每个测试的结果和截图
- 发现问题及时记录和反馈
- 重复测试以确保稳定性

### 3. 测试后处理
- 整理测试报告
- 分析测试结果
- 提交问题单
- 跟踪问题修复

---

## 📞 技术支持

### 联系方式
- **开发团队**: dev@bankshield.com
- **技术支持**: support@bankshield.com
- **问题反馈**: https://github.com/bankshield/issues

### 相关文档
- 项目开发指南: `AGENTS.md`
- 快速启动指南: `NEW_MODULES_QUICK_START.md`
- 部署检查清单: `DEPLOYMENT_CHECKLIST.md`
- API参考文档: http://localhost:8080/swagger-ui.html

---

**文档维护**: BankShield开发团队  
**最后更新**: 2024-12-31  
**版本**: v1.0.0
