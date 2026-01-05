# BankShield 新模块快速启动指南

## 📋 目录

1. [环境准备](#环境准备)
2. [数据库配置](#数据库配置)
3. [后端启动](#后端启动)
4. [前端启动](#前端启动)
5. [菜单配置](#菜单配置)
6. [功能使用](#功能使用)
7. [常见问题](#常见问题)

## 🔧 环境准备

### 必需软件
- **JDK**: 11 或以上
- **Maven**: 3.6+
- **Node.js**: 16+
- **MySQL**: 8.0+
- **Redis**: 6.0+

### 检查环境
```bash
java -version
mvn -version
node -version
mysql --version
redis-cli --version
```

## 💾 数据库配置

### 1. 创建数据库
```bash
mysql -u root -p3f342bb206
```

```sql
-- 创建主数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS bankshield DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE bankshield;
```

### 2. 执行菜单集成脚本
```bash
# 进入项目目录
cd /Users/zhangyanlong/workspaces/BankShield

# 执行菜单集成脚本
mysql -u root -p3f342bb206 bankshield < sql/menu_integration.sql
```

**验证菜单是否创建成功**:
```sql
USE bankshield;

-- 查看新增菜单
SELECT menu_name, path, component, perms 
FROM sys_menu 
WHERE menu_name IN ('区块链存证', '多方安全计算')
   OR parent_id IN (
       SELECT id FROM sys_menu 
       WHERE menu_name IN ('区块链存证', '多方安全计算')
   )
ORDER BY parent_id, order_num;
```

### 3. 初始化区块链和MPC模块表（如果需要）
```bash
# 如果有专门的初始化脚本
mysql -u root -p3f342bb206 bankshield < sql/blockchain_init.sql
mysql -u root -p3f342bb206 bankshield < sql/mpc_init.sql
```

## 🚀 后端启动

### 方式一：使用 Maven 启动（推荐开发环境）

#### 1. 启动主应用
```bash
cd bankshield-api
mvn clean spring-boot:run -Dspring-boot.run.profiles=dev
```

#### 2. 启动区块链模块（如果独立部署）
```bash
cd bankshield-blockchain
mvn clean spring-boot:run -Dspring-boot.run.profiles=dev
```

#### 3. 启动MPC模块（如果独立部署）
```bash
cd bankshield-mpc
mvn clean spring-boot:run -Dspring-boot.run.profiles=dev
```

### 方式二：使用启动脚本
```bash
cd /Users/zhangyanlong/workspaces/BankShield
./scripts/start.sh --dev
```

### 验证后端启动
访问以下地址检查服务状态：

- **主应用**: http://localhost:8080/actuator/health
- **Swagger文档**: http://localhost:8080/swagger-ui.html
- **区块链模块**: http://localhost:8081/blockchain/health
- **MPC模块**: http://localhost:8082/mpc/health

## 🎨 前端启动

### 1. 安装依赖
```bash
cd bankshield-ui
npm install
```

### 2. 启动开发服务器
```bash
npm run dev
```

### 3. 访问应用
打开浏览器访问: http://localhost:5173

### 默认登录信息
- **用户名**: admin
- **密码**: admin123

## 📱 菜单配置

登录后，新模块菜单应该自动显示在左侧导航栏：

```
📊 数据大屏
👥 系统管理
🔐 数据加密
📋 审计管理
📊 监控管理
🔗 区块链存证          ← 新增
   ├─ 📊 存证概览
   └─ 📄 存证记录
🔐 多方安全计算        ← 新增
   ├─ 📊 MPC概览
   ├─ 📋 任务列表
   ├─ 🔗 隐私求交
   ├─ ➕ 安全求和
   ├─ 🔍 联合查询
   └─ 👥 参与方管理
```

### 如果菜单未显示

#### 方法1：清除缓存并重新登录
```bash
# 浏览器控制台执行
localStorage.clear()
sessionStorage.clear()
# 然后刷新页面重新登录
```

#### 方法2：检查角色权限
```sql
-- 查看当前用户的角色
SELECT u.user_name, r.role_name 
FROM sys_user u
JOIN sys_user_role ur ON u.user_id = ur.user_id
JOIN sys_role r ON ur.role_id = r.role_id
WHERE u.user_name = 'admin';

-- 查看角色的菜单权限
SELECT m.menu_name, m.path, m.perms
FROM sys_menu m
JOIN sys_role_menu rm ON m.menu_id = rm.menu_id
WHERE rm.role_id = 1  -- 假设admin的角色ID是1
AND m.menu_name IN ('区块链存证', 'MPC概览', '任务列表');
```

## 🎯 功能使用

### 区块链存证模块

#### 1. 查看存证概览
1. 点击左侧菜单 **区块链存证 > 存证概览**
2. 查看统计卡片：总记录数、审计记录、密钥记录、合规记录
3. 查看区块链网络状态
4. 执行合规性检查（GDPR、PCI_DSS、SOX）

#### 2. 管理存证记录
1. 点击左侧菜单 **区块链存证 > 存证记录**
2. 使用筛选条件查询记录
3. 点击 **详情** 查看完整信息
4. 点击 **验证** 检查数据完整性
5. 点击 **证书** 生成存证证书

#### 3. API 调用示例
```typescript
// 批量存证
import * as blockchainApi from '@/api/blockchain'

const records = [
  { recordId: 1, dataHash: 'hash1', metadata: {} },
  { recordId: 2, dataHash: 'hash2', metadata: {} }
]

const result = await blockchainApi.batchAnchor(records)

// 验证完整性
const isValid = await blockchainApi.verifyAuditLogIntegrity(auditId)
```

### 多方安全计算模块

#### 1. 查看MPC概览
1. 点击左侧菜单 **多方安全计算 > MPC概览**
2. 查看任务统计（总数、成功、运行中、失败）
3. 查看任务类型分布
4. 查看参与方状态
5. 查看支持的MPC协议

#### 2. 执行隐私求交（PSI）
1. 点击左侧菜单 **多方安全计算 > 隐私求交**
2. 选择参与方
3. 输入数据集
4. 点击 **执行** 开始计算
5. 查看交集结果

#### 3. 执行安全求和
1. 点击左侧菜单 **多方安全计算 > 安全求和**
2. 选择参与方
3. 输入各方数值
4. 点击 **执行** 开始计算
5. 查看求和结果

#### 4. 管理任务
1. 点击左侧菜单 **多方安全计算 > 任务列表**
2. 查看所有MPC任务
3. 筛选任务（按类型、状态）
4. 点击 **详情** 查看任务信息
5. 点击 **取消** 终止运行中的任务

#### 5. 管理参与方
1. 点击左侧菜单 **多方安全计算 > 参与方管理**
2. 查看所有参与方
3. 点击 **注册** 添加新参与方
4. 更新参与方状态
5. 删除无效参与方

## 🔍 常见问题

### Q1: 菜单不显示怎么办？
**A**: 
1. 确认已执行 `menu_integration.sql` 脚本
2. 清除浏览器缓存并重新登录
3. 检查角色权限配置
4. 查看浏览器控制台是否有错误

### Q2: 后端API调用失败
**A**:
1. 检查后端服务是否启动成功
2. 查看后端日志：`tail -f logs/bankshield.log`
3. 确认数据库连接正常
4. 检查Redis服务是否运行

### Q3: 前端页面报错
**A**:
1. 检查控制台错误信息
2. 确认API接口地址配置正确
3. 清除npm缓存：`npm cache clean --force`
4. 重新安装依赖：`rm -rf node_modules && npm install`

### Q4: TypeScript类型错误
**A**:
这些是编译时警告，不影响运行。如需修复：
```typescript
// 在API调用时添加类型断言
const res = await blockchainApi.getStatistics() as any
```

### Q5: 区块链网络连接失败
**A**:
1. 检查Fabric网络是否启动
2. 确认网络配置正确
3. 查看区块链模块日志

### Q6: MPC计算超时
**A**:
1. 检查参与方是否在线
2. 增加超时时间配置
3. 减少数据集大小
4. 查看MPC模块日志

## 📊 性能优化建议

### 后端优化
```yaml
# application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
  redis:
    lettuce:
      pool:
        max-active: 20
```

### 前端优化
```typescript
// 路由懒加载已配置
// 组件按需加载
import { ElButton } from 'element-plus'
```

## 🔐 安全建议

1. **修改默认密码**: 首次登录后立即修改admin密码
2. **配置HTTPS**: 生产环境使用HTTPS协议
3. **启用访问控制**: 配置细粒度权限
4. **定期备份**: 定期备份数据库和区块链数据
5. **监控日志**: 定期查看审计日志

## 📞 技术支持

如遇到问题，请查看：
1. **项目文档**: `/docs` 目录
2. **API文档**: http://localhost:8080/swagger-ui.html
3. **开发指南**: `AGENTS.md`
4. **完成报告**: `MODULE_COMPLETION_REPORT.md`

## 🎉 快速测试

### 测试区块链存证
```bash
# 使用curl测试API
curl -X POST http://localhost:8080/blockchain/anchor/batch \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '[{"recordId":1,"dataHash":"test","metadata":{}}]'
```

### 测试MPC计算
```bash
# 测试隐私求交
curl -X POST http://localhost:8082/mpc/psi \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"partyIds":[1,2],"dataset":["a","b","c"]}'
```

---

**祝您使用愉快！** 🚀

如有问题，请参考 `MODULE_COMPLETION_REPORT.md` 获取更多详细信息。
