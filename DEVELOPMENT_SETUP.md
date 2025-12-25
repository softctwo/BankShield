# 本机开发环境配置完成

## ✅ 配置状态

### 数据库配置
- **数据库地址**: localhost:3306
- **数据库名称**: bankshield
- **用户名**: root
- **密码**: 3f342bb206
- **初始化状态**: ✅ 已完成
  - 13张数据表已创建
  - 4个初始用户已导入
  - 5种角色权限已配置
  - 6种敏感数据类型已定义

### 后端配置
- **服务端口**: 8080
- **Context Path**: /api
- **配置文件**: `bankshield-api/src/main/resources/application.yml`
- **数据库连接**: 已更新为本机配置
- **Redis连接**: 已配置（localhost:6379）

### 前端配置
- **框架**: Vue 3 + TypeScript + Vite
- **开发端口**: 3000
- **环境文件**: `.env.development` 已创建
- **API地址**: 已配置为 http://localhost:8080/api

### 初始用户
系统已预置以下用户，密码均为: **123456**

| 用户名   | 角色         | 说明               |
|---------|-------------|-------------------|
| admin   | 超级管理员    | 系统最高权限账号     |
| security| 安全管理员    | 负责系统安全管理     |
| audit   | 审计管理员    | 负责系统审计         |
| user    | 普通用户      | 日常测试使用         |

### 访问地址
- **前端页面**: http://localhost:3000
- **后端API**: http://localhost:8080/api
- **Druid监控**: http://localhost:8080/api/druid/login.html
  - 账号: admin
  - 密码: 123456

## 🚀 快速启动

### 方式一：使用启动脚本（推荐）

```bash
# 进入项目目录
cd /Users/zhangyanlong/workspaces/BankShield

# 启动开发环境（会自动启动后端和前端）
./scripts/start.sh --dev

# 单独停止服务
./scripts/start.sh --stop
```

### 方式二：手动启动

#### 启动后端
```bash
cd bankshield-api
mvn spring-boot:run
```

#### 启动前端（新开终端）
```bash
cd bankshield-ui
npm install  # 首次运行需要安装依赖
npm run dev
```

### 方式三：使用VS Code / IDEA

1. **导入项目**: 将整个 BankShield 目录导入IDE
2. **启动后端**: 在 bankshield-api 模块中找到主类，点击运行
3. **启动前端**: 在 bankshield-ui 目录下打开终端，执行 `npm run dev`

## 🔧 开发工具

### 数据库访问
```bash
# 使用命令行访问
mysql -u root -p3f342bb206 bankshield

# 或使用IDEA/DBeaver等工具连接
# Host: localhost
# Port: 3306
# User: root
# Password: 3f342bb206
# Database: bankshield
```

### Redis访问
```bash
# Redis已在运行，可通过以下命令测试
redis-cli ping  # 应返回 PONG

# 查看所有key
redis-cli keys *
```

## 📋 下一步开发建议

### 1. 登录系统测试
- 访问 http://localhost:3000
- 使用 admin/123456 登录
- 测试各功能模块

### 2. 核心功能模块
- **用户管理**: 查看/编辑/删除用户
- **角色权限**: 管理角色和菜单权限
- **敏感数据识别**: 配置敏感数据规则
- **数据加密**: 配置表字段加密
- **审计日志**: 查看操作审计日志

### 3. 数据库表结构
主要数据表：
- `sys_user`: 用户表
- `sys_role`: 角色表
- `sys_menu`: 菜单表
- `sys_dept`: 部门表
- `sensitive_data_type`: 敏感数据类型
- `encrypt_config`: 加密配置
- `audit_log`: 审计日志
- `data_access_log`: 数据访问日志

### 4. 开发文档
- [项目文档](docs/)
- [API文档](docs/api.md)
- [数据库设计](docs/database.md)
- [安全规范](docs/security.md)

## 🛡️ 安全提醒

⚠️ **重要**: 本机开发环境使用简单密码，仅供开发测试使用。生产环境务必：
- 使用强密码
- 限制数据库访问IP
- 启用SSL连接
- 定期备份数据
- 配置Redis密码

---

**环境配置完成！可以开始开发了 🎉**
