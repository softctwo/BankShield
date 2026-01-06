# BankShield - 银行数据安全管理系统

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.5.26-green.svg)](https://vuejs.org/)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/softctwo/BankShield)

## 📖 项目简介

BankShield 是一个专业的**银行数据安全管理平台**，采用微服务架构，提供数据加密、访问控制、审计追踪、敏感数据识别与脱敏等核心功能，确保银行数据全生命周期的安全性。

**项目状态**: 🟡 开发中（88%完成） | **最新版本**: v1.0.0-SNAPSHOT | **更新时间**: 2026-01-06

## ✨ 核心功能

- 🔐 **数据加密管理** - 支持国密SM2/SM3/SM4算法和国际标准加密算法
- 👥 **细粒度访问控制** - 基于RBAC的权限管理体系，支持角色互斥
- 📊 **实时审计追踪** - 全链路操作日志记录与区块链存证
- 🎯 **敏感数据识别** - AI驱动的自动发现和分类
- 🎭 **智能数据脱敏** - 动态脱敏、静态脱敏、格式保留脱敏
- 📈 **安全态势可视化** - 实时安全监控与智能告警
- 📋 **合规性检查** - 符合金融行业标准规范
- 🔍 **数据血缘追踪** - 完整的数据流向分析
- 💧 **数字水印** - 文档溯源和版权保护
- 🛡️ **安全扫描** - 自动化漏洞检测与修复建议
- ⛓️ **区块链存证** - 审计日志不可篡改存储
- 🤝 **多方安全计算** - 隐私保护下的数据协作

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
- **JDK**: 17+（已从1.8升级）
- **Node.js**: 16+
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **Maven**: 3.6+
- **Docker**: 20.10+（可选）

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

| 服务 | 地址 | 状态 |
|------|------|------|
| **前端** | http://localhost:3000 | ✅ 运行中 |
| **后端API** | http://localhost:8081 | ⚠️ 编译待修复 |
| **Swagger** | http://localhost:8081/swagger-ui.html | ⚠️ 待启动 |
| **Druid监控** | http://localhost:8081/druid | ⚠️ 待启动 |

**默认账号**：
- 超级管理员：admin / 123456
- 安全管理员：security / 123456
- 审计管理员：audit / 123456

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

## 📚 文档

- 📊 [项目状态报告 2026](PROJECT_STATUS_2026.md) - 最新的项目进度和状态
- 🏗️ [系统架构设计](docs/architecture/README.md)
- 📖 [开发指南](AGENTS.md)
- 🚀 [快速开始](QUICK_REFERENCE.md)
- 📋 [部署指南](DEPLOYMENT_GUIDE.md)
- 🔐 [安全规范](docs/BEST_PRACTICES.md)
- 💾 [数据库设计](sql/)
- 📝 [更多文档](docs/)

## 🎯 项目进度

**整体完成度**: 88%

- ✅ 基础设施（100%）
- ✅ 公共模块（100%）
- ✅ 前端系统（95%）
- ⚠️ 后端API（75%）- 约100个编译错误待修复
- ✅ 数据库（100%）
- ✅ 加密模块（90%）
- ✅ 区块链模块（85%）
- ✅ 多方计算（85%）
- ✅ AI模块（80%）
- ✅ 监控告警（90%）

详细进度请查看 [PROJECT_STATUS_2026.md](PROJECT_STATUS_2026.md)

## 🤝 贡献

欢迎贡献代码、报告问题或提出建议！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

## 📞 联系方式

- **项目地址**: https://github.com/softctwo/BankShield
- **问题反馈**: https://github.com/softctwo/BankShield/issues
- **文档地址**: [docs/](docs/)

---

**最后更新**: 2026-01-06 | **维护团队**: BankShield Development Team