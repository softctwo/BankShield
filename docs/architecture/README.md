# BankShield 架构设计文档

本目录包含了BankShield银行数据安全管理系统的完整架构设计文档，涵盖了系统架构、数据架构、安全架构以及详细的架构决策记录。

## 📋 文档索引

### 🏗️ 系统架构设计
- **[系统架构设计文档（SAD）](./sad.md)** - 整体技术架构、部署拓扑、组件交互等
- **[数据架构文档](./data-architecture.md)** - 数据库选型、ER设计、存储策略、性能优化
- **[安全架构设计](./security-architecture.md)** - 威胁模型、权限矩阵、加密方案、攻击面分析

### 📝 架构决策记录（ADR）
- **[ADR-001: Spring Cloud Gateway](./adr/ADR-001-spring-cloud-gateway.md)** - API网关技术选型
- **[ADR-002: JWT认证](./adr/ADR-002-jwt-authentication.md)** - 无状态认证机制
- **[ADR-003: 国密算法](./adr/ADR-003-national-crypto-algorithms.md)** - 国密SM2/SM3/SM4算法选择
- **[ADR-004: MyBatis-Plus](./adr/ADR-004-mybatis-plus.md)** - 持久层框架选型
- **[ADR-005: Vue 3 + TypeScript](./adr/ADR-005-vue3-typescript.md)** - 前端技术栈选择

### 🔧 详细设计文档
- **[密钥管理服务设计](./detailed-design/key-management-service.md)** - KMS详细架构和实现
- **[审计日志服务设计](./detailed-design/audit-log-service.md)** - ALS详细架构和实现

## 🎯 设计原则

### 安全优先
- 零信任架构
- 纵深防御
- 最小权限原则
- 默认拒绝
- 安全左移

### 高可用性
- 99.9%系统可用性
- 自动故障恢复
- 数据多副本存储
- 异地容灾备份

### 高性能
- API响应时间 < 100ms
- 支持1000+并发连接
- 吞吐量 > 5000 TPS
- 国密算法性能优化

### 合规性
- 符合《密码法》要求
- 满足等保2.0标准
- 通过商用密码应用安全性评估
- 遵循金融行业安全规范

## 📊 技术栈概览

### 后端技术栈
```
Java 8 + Spring Boot 2.7.18（主框架）
Spring Cloud 2021.0.8（微服务）
Spring Cloud Gateway 3.1.4（API网关）
MyBatis-Plus 3.5.2（持久层）
MySQL 8.0（关系型数据库）
Redis 6.0（缓存+限流+Session）
Elasticsearch 7.17（日志检索）
Quartz 2.3（定时任务）
BouncyCastle 1.77（国密算法）
JWT 0.11.5（认证）
Maven 3.8（构建）
```

### 前端技术栈
```
Vue 3.4.5（渐进式框架）
TypeScript 5.0.2（类型安全）
Element Plus 2.4.4（UI组件库）
Pinia 2.1.7（状态管理）
Vue Router 4.2.5（路由）
Axios 1.6.2（HTTP客户端）
ECharts 5.4.3（可视化图表）
Vite 5.0.12（构建工具）
Less 4.2.0（CSS预处理）
```

### DevOps工具链
```
Docker 24.0（容器化）
Docker Compose 2.20（本地编排）
Kubernetes 1.28（生产编排）
Jenkins 2.414（CI/CD）
GitLab CI（持续集成）
SonarQube 9.9（代码质量）
Nexus 3.58（私服仓库）
```

## 🚀 快速开始

### 阅读顺序建议

1. **新手入门**: [系统架构设计文档（SAD）](./sad.md) → [ADR索引](./adr/)
2. **架构师**: [SAD](./sad.md) → [数据架构](./data-architecture.md) → [安全架构](./security-architecture.md) → [ADR](./adr/)
3. **开发人员**: [ADR](./adr/) → [详细设计](./detailed-design/)
4. **安全专家**: [安全架构](./security-architecture.md) → [详细设计](./detailed-design/)

### 关键设计文档

| 文档 | 重要性 | 阅读时间 | 目标读者 |
|------|--------|----------|----------|
| SAD | ⭐⭐⭐⭐⭐ | 60分钟 | 所有人 |
| 数据架构 | ⭐⭐⭐⭐⭐ | 45分钟 | 架构师、DBA |
| 安全架构 | ⭐⭐⭐⭐⭐ | 90分钟 | 安全专家、架构师 |
| 密钥管理设计 | ⭐⭐⭐⭐⭐ | 60分钟 | 安全专家、开发人员 |
| 审计日志设计 | ⭐⭐⭐⭐ | 45分钟 | 安全专家、运维人员 |

## 📈 架构演进

### 当前状态 (v1.0.0)
- ✅ 基础微服务架构
- ✅ 国密算法支持
- ✅ RBAC权限管理
- ✅ 基础审计功能

### 近期规划 (v1.1)
- 🔄 支持更多国密算法
- 🔄 数据水印功能
- 🔄 数据血缘分析
- 🔄 外部身份认证集成

### 中期规划 (v1.2)
- 📋 多方安全计算
- 📋 同态加密支持
- 📋 零知识证明
- 📋 区块链存证

### 长期规划 (v2.0)
- 📋 完整数据中台能力
- 📋 AI驱动的安全威胁检测
- 📋 自动化合规检查
- 📋 多云部署支持

## 🔗 相关链接

- [项目概述](../PROJECT_OVERVIEW.md)
- [开发指南](../DEVELOPMENT_SETUP.md)
- [产品需求文档](../prd.md)
- [业务需求文档](../brd.md)

## 📞 联系方式

**技术支持**: support@bankshield.com  
**架构咨询**: architecture@bankshield.com  
**安全咨询**: security@bankshield.com  

---

**最后更新**: 2025-12-24  
**维护团队**: BankShield架构团队