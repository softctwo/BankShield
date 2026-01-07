# BankShield 概要设计说明书 (HLD)

**文档版本**: v1.0  
**项目名称**: BankShield - 银行数据安全管理系统  
**编写日期**: 2026-01-07  
**编写人**: BankShield Development Team  

---

## 1. 系统架构设计

### 1.1 总体架构

BankShield采用**微服务架构**，基于Spring Cloud生态，实现服务解耦、独立部署和弹性伸缩。

**架构层次**:
- **用户层**: Web浏览器、移动端、第三方系统
- **网关层**: Spring Cloud Gateway（路由、限流、鉴权）
- **服务层**: 业务服务 + 基础服务
- **数据层**: MySQL、Redis、Vault、区块链

### 1.2 技术选型

| 层次 | 技术栈 | 版本 | 说明 |
|------|--------|------|------|
| **前端框架** | Vue 3 | 3.5.26 | 渐进式JavaScript框架 |
| **UI组件库** | Element Plus | 2.13.0 | Vue 3组件库 |
| **状态管理** | Pinia | 2.3.1 | Vue状态管理 |
| **API网关** | Spring Cloud Gateway | 3.1.x | 统一入口 |
| **服务发现** | Netflix Eureka | 3.1.x | 服务注册 |
| **配置中心** | Spring Cloud Config | 3.1.x | 配置管理 |
| **后端框架** | Spring Boot | 2.7.18 | 微服务框架 |
| **ORM框架** | MyBatis-Plus | 3.5.3.2 | 持久层 |
| **安全框架** | Spring Security | 5.7.x | 安全认证 |
| **数据库** | MySQL | 8.0+ | 关系型数据库 |
| **缓存** | Redis | 6.0+ | 分布式缓存 |
| **消息队列** | RabbitMQ | 3.11+ | 异步消息 |
| **区块链** | Hyperledger Fabric | 2.5+ | 审计存证 |
| **密钥管理** | HashiCorp Vault | 1.15+ | 密钥存储 |
| **监控** | Prometheus + Grafana | - | 系统监控 |
| **容器化** | Docker + Kubernetes | - | 容器编排 |

---

## 2. 核心模块设计

### 2.1 认证授权模块 (bankshield-auth)

**职责**: 用户身份认证、权限管理、会话管理

**核心组件**:
- JWT Token生成器
- 权限拦截器
- RBAC权限引擎
- MFA多因素认证

**主要类**:
```java
com.bankshield.auth.service.AuthService
com.bankshield.auth.service.TokenService
com.bankshield.auth.security.JwtAuthenticationFilter
com.bankshield.auth.security.RbacPermissionEvaluator
```

**API接口**:
- `POST /auth/login` - 用户登录
- `POST /auth/logout` - 用户登出
- `POST /auth/refresh` - 刷新Token
- `GET /auth/userinfo` - 获取用户信息
- `POST /auth/mfa/enable` - 启用MFA
- `POST /auth/mfa/verify` - 验证MFA

### 2.2 核心API模块 (bankshield-api)

**职责**: 核心业务逻辑、数据管理、业务流程

**核心子模块**:
- 用户管理 (UserController)
- 角色管理 (RoleController)
- 部门管理 (DeptController)
- 菜单管理 (MenuController)
- 审计日志 (AuditController)
- 数据分类 (ClassificationController)
- 数据脱敏 (DesensitizationController)
- 合规管理 (ComplianceController)
- 安全扫描 (SecurityScanController)

**主要类**:
```java
com.bankshield.api.controller.*Controller
com.bankshield.api.service.*Service
com.bankshield.api.mapper.*Mapper
com.bankshield.api.entity.*
```

### 2.3 加密模块 (bankshield-encrypt)

**职责**: 密钥管理、数据加密解密、国密算法

**核心组件**:
- 密钥生成服务 (KeyGenerationService)
- 密钥存储服务 (KeyStorageService)
- 加密服务 (EncryptionService)
- 密钥轮换服务 (KeyRotationService)
- Vault集成 (VaultIntegration)

**支持算法**:
- 对称加密: SM4、AES-256
- 非对称加密: SM2、RSA-2048
- 哈希算法: SM3、SHA-256

**API接口**:
- `POST /encrypt/key/generate` - 生成密钥
- `POST /encrypt/data` - 加密数据
- `POST /encrypt/decrypt` - 解密数据
- `POST /encrypt/key/rotate` - 密钥轮换
- `GET /encrypt/key/list` - 密钥列表

### 2.4 数据血缘模块 (bankshield-lineage)

**职责**: 数据血缘追踪、影响分析、可视化

**核心组件**:
- 血缘图构建器 (LineageGraphBuilder)
- SQL解析器 (SqlParser)
- 影响分析引擎 (ImpactAnalyzer)
- 可视化服务 (VisualizationService)

**API接口**:
- `POST /lineage/build` - 构建血缘图
- `GET /lineage/graph` - 获取血缘图
- `POST /lineage/impact` - 影响分析
- `GET /lineage/path` - 路径查询

### 2.5 区块链模块 (bankshield-blockchain)

**职责**: 审计日志上链、存证验证

**核心组件**:
- Fabric客户端 (FabricClient)
- 智能合约 (Chaincode)
- 存证服务 (AnchorService)
- 验证服务 (VerificationService)

**Chaincode**:
- audit_anchor.go - 审计日志存证
- data_access_anchor.go - 数据访问存证
- key_rotation_anchor.go - 密钥轮换存证

### 2.6 多方计算模块 (bankshield-mpc)

**职责**: 隐私保护下的数据协作

**核心协议**:
- PSI (私有集合交集)
- 安全聚合
- 联合查询

**API接口**:
- `POST /mpc/psi/create` - 创建PSI任务
- `POST /mpc/aggregate/create` - 创建聚合任务
- `POST /mpc/query/create` - 创建联合查询

### 2.7 AI分析模块 (bankshield-ai)

**职责**: 异常检测、行为分析、威胁预测

**核心功能**:
- 异常检测 (AnomalyDetector)
- 用户行为分析 (BehaviorAnalyzer)
- 威胁预测 (ThreatPredictor)
- 资源预测 (ResourcePredictor)

**API接口**:
- `POST /ai/anomaly/detect` - 异常检测
- `POST /ai/behavior/analyze` - 行为分析
- `GET /ai/threat/predict` - 威胁预测

---

## 3. 数据库设计

### 3.1 数据库架构

**主数据库**: MySQL 8.0
- 字符集: utf8mb4
- 排序规则: utf8mb4_unicode_ci
- 存储引擎: InnoDB

**数据库列表**:
- `bankshield` - 主业务数据库
- `bankshield_audit` - 审计日志数据库（可选分离）

### 3.2 核心表设计

#### 系统管理表

**用户表 (sys_user)**
```sql
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    dept_id BIGINT COMMENT '部门ID',
    status TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '更新人',
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

**角色表 (sys_role)**
```sql
CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) UNIQUE NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) UNIQUE NOT NULL COMMENT '角色编码',
    description VARCHAR(200) COMMENT '描述',
    data_scope TINYINT DEFAULT 1 COMMENT '数据范围:1-全部,2-部门,3-本人',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';
```

#### 审计日志表

**审计日志表 (audit_log)**
```sql
CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    operation VARCHAR(50) COMMENT '操作类型',
    module VARCHAR(50) COMMENT '模块名称',
    method VARCHAR(10) COMMENT '请求方法',
    request_url VARCHAR(500) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    location VARCHAR(100) COMMENT '操作地点',
    result TINYINT COMMENT '操作结果:0-失败,1-成功',
    error_msg TEXT COMMENT '错误信息',
    execution_time INT COMMENT '执行时间(ms)',
    blockchain_hash VARCHAR(128) COMMENT '区块链哈希',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_operation (operation),
    INDEX idx_create_time (create_time),
    INDEX idx_blockchain_hash (blockchain_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';
```

#### 加密管理表

**加密密钥表 (encrypt_key)**
```sql
CREATE TABLE encrypt_key (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '密钥ID',
    key_name VARCHAR(100) UNIQUE NOT NULL COMMENT '密钥名称',
    key_type VARCHAR(20) NOT NULL COMMENT '密钥类型:SM2,SM4,AES,RSA',
    key_usage VARCHAR(50) COMMENT '密钥用途',
    key_status TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-启用,2-已轮换',
    vault_path VARCHAR(200) COMMENT 'Vault存储路径',
    rotation_period INT COMMENT '轮换周期(天)',
    last_rotation_time DATETIME COMMENT '上次轮换时间',
    next_rotation_time DATETIME COMMENT '下次轮换时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    expire_time DATETIME COMMENT '过期时间',
    INDEX idx_key_name (key_name),
    INDEX idx_key_type (key_type),
    INDEX idx_next_rotation (next_rotation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加密密钥表';
```

#### 数据脱敏表

**脱敏规则表 (desensitization_rule)**
```sql
CREATE TABLE desensitization_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '规则ID',
    rule_name VARCHAR(100) UNIQUE NOT NULL COMMENT '规则名称',
    rule_type VARCHAR(20) NOT NULL COMMENT '规则类型:MASK,REPLACE,ENCRYPT,HASH',
    data_type VARCHAR(50) COMMENT '数据类型',
    pattern VARCHAR(200) COMMENT '匹配模式',
    mask_char VARCHAR(10) DEFAULT '*' COMMENT '脱敏字符',
    keep_prefix INT DEFAULT 0 COMMENT '保留前缀长度',
    keep_suffix INT DEFAULT 0 COMMENT '保留后缀长度',
    status TINYINT DEFAULT 1 COMMENT '状态',
    priority INT DEFAULT 0 COMMENT '优先级',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_rule_name (rule_name),
    INDEX idx_data_type (data_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='脱敏规则表';
```

### 3.3 索引设计原则

- 主键使用自增BIGINT
- 唯一字段创建唯一索引
- 查询频繁字段创建普通索引
- 联合查询创建组合索引
- 时间字段创建索引便于范围查询

---

## 4. 接口设计

### 4.1 RESTful API规范

**URL设计规范**:
- 使用名词复数: `/api/v1/users`
- 使用小写字母和连字符: `/api/v1/audit-logs`
- 版本号放在URL中: `/api/v1/`

**HTTP方法**:
- `GET` - 查询资源
- `POST` - 创建资源
- `PUT` - 更新资源(全量)
- `PATCH` - 更新资源(部分)
- `DELETE` - 删除资源

**统一响应格式**:
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1704556800000
}
```

**错误响应格式**:
```json
{
  "code": 400,
  "message": "参数错误",
  "data": null,
  "timestamp": 1704556800000
}
```

### 4.2 核心API列表

#### 用户管理API
- `GET /api/v1/users` - 用户列表
- `GET /api/v1/users/{id}` - 用户详情
- `POST /api/v1/users` - 创建用户
- `PUT /api/v1/users/{id}` - 更新用户
- `DELETE /api/v1/users/{id}` - 删除用户
- `PATCH /api/v1/users/{id}/status` - 修改状态

#### 加密管理API
- `GET /api/v1/encrypt/keys` - 密钥列表
- `POST /api/v1/encrypt/keys` - 生成密钥
- `POST /api/v1/encrypt/data` - 加密数据
- `POST /api/v1/encrypt/decrypt` - 解密数据
- `POST /api/v1/encrypt/keys/{id}/rotate` - 密钥轮换

#### 审计日志API
- `GET /api/v1/audit/logs` - 日志查询
- `GET /api/v1/audit/logs/{id}` - 日志详情
- `POST /api/v1/audit/blockchain` - 上链存证
- `GET /api/v1/audit/blockchain/verify` - 验证存证
- `GET /api/v1/audit/statistics` - 统计分析

---

## 5. 部署架构

### 5.1 开发环境

```
开发机 (本地)
├── MySQL 8.0 (localhost:3306)
├── Redis 6.0 (localhost:6379)
├── 后端服务 (localhost:8081)
└── 前端服务 (localhost:3000)
```

### 5.2 生产环境

```
负载均衡层 (Nginx/ALB)
    │
Kubernetes集群
├── Namespace: bankshield-prod
│   ├── Gateway Pod (x3)
│   ├── Auth Service Pod (x2)
│   ├── API Service Pod (x3)
│   ├── Encrypt Service Pod (x2)
│   └── 其他服务 Pod
│
数据层
├── MySQL集群 (主从复制)
├── Redis集群 (哨兵模式)
├── Vault HA集群
└── Fabric区块链网络
```

### 5.3 高可用设计

**应用层**:
- 多实例部署 (至少2个实例)
- 自动伸缩 (HPA)
- 健康检查和自动重启

**数据库层**:
- 主从复制
- 读写分离
- 自动故障转移

**缓存层**:
- Redis Sentinel哨兵模式
- 主从复制
- 自动故障转移

**网关层**:
- 多实例负载均衡
- 限流熔断
- 健康检查

---

## 6. 安全设计

### 6.1 安全架构

**安全防护层**:
- WAF Web应用防火墙
- DDoS防护
- IP黑白名单

**应用安全层**:
- JWT Token认证
- RBAC权限控制
- SQL注入防护
- XSS攻击防护
- CSRF防护

**数据安全层**:
- 传输加密 (TLS 1.2+)
- 存储加密 (AES-256/SM4)
- 密钥管理 (Vault)
- 数据脱敏

### 6.2 安全措施

**认证授权**:
- JWT Token (有效期2小时)
- Refresh Token (有效期7天)
- MFA多因素认证
- 登录失败锁定 (5次/30分钟)

**数据保护**:
- 敏感数据加密存储
- 密码BCrypt加密
- 国密算法支持
- 数据脱敏展示

**审计追踪**:
- 全链路操作日志
- 区块链存证
- 日志不可篡改
- 实时告警

---

## 7. 性能设计

### 7.1 性能指标

| 指标 | 目标值 | 说明 |
|------|--------|------|
| API响应时间 | < 500ms | 90%请求 |
| 并发用户数 | 500+ | 同时在线 |
| 数据库QPS | 5000+ | 查询性能 |
| 缓存命中率 | > 80% | Redis缓存 |
| 系统可用性 | 99.9% | 年停机<8.76h |

### 7.2 性能优化策略

**缓存策略**:
- Redis多级缓存
- 热点数据缓存
- 缓存预热
- 缓存穿透防护

**数据库优化**:
- 索引优化
- 读写分离
- 分库分表 (未来)
- 慢查询优化

**异步处理**:
- 消息队列解耦
- 异步任务处理
- 定时任务调度

**前端优化**:
- CDN加速
- 资源压缩
- 懒加载
- 代码分割

---

## 8. 监控告警

### 8.1 监控指标

**系统指标**:
- CPU使用率
- 内存使用率
- 磁盘使用率
- 网络流量

**应用指标**:
- API响应时间
- 请求成功率
- 错误率
- QPS/TPS

**业务指标**:
- 在线用户数
- 登录成功率
- 加密操作次数
- 审计日志数量

### 8.2 告警规则

| 指标 | 阈值 | 级别 | 处理 |
|------|------|------|------|
| CPU使用率 | > 80% | 警告 | 通知运维 |
| 内存使用率 | > 85% | 警告 | 通知运维 |
| API错误率 | > 5% | 严重 | 立即处理 |
| 响应时间 | > 2s | 警告 | 性能优化 |
| 系统宕机 | - | 紧急 | 立即恢复 |

---

**文档结束**

**批准签字**:

系统架构师: ________________  日期: ________

技术负责人: ______________  日期: ________
