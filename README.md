# BankShield - 银行数据安全管理系统

## 项目简介
BankShield 是一个专业的银行数据安全管理平台，提供数据加密、访问控制、审计追踪、敏感数据识别与脱敏等核心功能，确保银行数据全生命周期的安全性。

## 核心功能
- 🔐 **数据加密管理** - 支持国密算法和国际标准加密算法
- 👥 **细粒度访问控制** - 基于RBAC的权限管理体系
- 📊 **实时审计追踪** - 全链路操作日志记录与分析
- 🎯 **敏感数据识别** - 自动发现和分类敏感数据
- 🎭 **智能数据脱敏** - 动态和静态数据脱敏
- 📈 **安全态势可视化** - 实时安全监控与告警
- 📋 **合规性检查** - 符合金融行业标准规范

## 技术架构
- **前端**: Vue 3 + TypeScript + Element Plus
- **后端**: Spring Boot 2.7 + Spring Security
- **数据库**: MySQL 8.0 + Redis
- **安全**: 国密SM2/SM3/SM4、AES、RSA加密算法
- **中间件**: Apache Shiro、MyBatis-Plus
- **构建工具**: Maven + npm

## 项目结构
```
BankShield/
├── bankshield-ui/              # 前端工程
├── bankshield-api/             # 后端API服务
├── bankshield-auth/            # 认证授权服务
├── bankshield-discovery/       # 服务注册中心
├── bankshield-gateway/         # API网关
├── bankshield-monitor/         # 监控服务
├── bankshield-common/          # 公共模块
├── bankshield-encrypt/         # 加密组件
├── docs/                       # 项目文档
├── scripts/                    # 脚本文件
├── sql/                        # 数据库脚本
└── docker/                     # Docker配置
```

## 快速开始

### 环境要求
- JDK 1.8+
- Node.js 16+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

### 方式一：使用启动脚本（推荐）

```bash
# 进入项目目录
cd BankShield

# 启动开发环境（自动构建并启动前后端）
./scripts/start.sh --dev

# 启动生产环境（完整构建）
./scripts/start.sh --prod

# 仅构建项目
./scripts/start.sh --build

# 停止所有服务
./scripts/start.sh --stop

# 查看帮助
./scripts/start.sh --help
```

### 方式二：手动启动

```bash
# 1. 数据库初始化
mysql -u root -p < sql/init_database.sql

# 2. 启动后端服务
cd bankshield-api
mvn clean install
mvn spring-boot:run

# 3. 启动前端
cd bankshield-ui
npm install
npm run dev
```

### 访问系统
- 前端地址：http://localhost:3000
- 后端API：http://localhost:8080/api
- Druid监控：http://localhost:8080/api/druid/login.html（admin/123456）

**默认账号**：
- 超级管理员：admin / 123456
- 安全管理员：security / 123456
- 审计管理员：audit / 123456
- 普通用户：user / 123456

### Docker部署（即将推出）

```bash
# 使用Docker Compose一键部署
cd docker
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f [service-name]
```

## 文档
- [系统架构设计](docs/architecture.md)
- [API文档](docs/api.md)
- [部署指南](docs/deployment.md)
- [安全规范](docs/security.md)
- [数据库设计](docs/database.md)

## 许可证
Apache License 2.0

## 联系方式
如有问题或建议，请联系：support@bankshield.com