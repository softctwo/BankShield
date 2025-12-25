# BankShield 三权分立机制实现文档

## 概述

三权分立机制是《信息安全等级保护》三级标准的核心要求之一。BankShield系统实现了完整的三权分立机制，确保系统管理员、安全策略员、合规审计员三个角色相互独立、相互制约，禁止同一人同时拥有多个互斥角色。

## 功能特点

### 🔒 角色定义
- **SYSTEM_ADMIN（系统管理员）**：用户/角色管理、系统监控、备份恢复
- **SECURITY_ADMIN（安全策略员）**：策略配置、密钥管理、风险规则
- **AUDIT_ADMIN（合规审计员）**：日志查询（只读）、报表生成、策略监控

### ⚖️ 角色互斥矩阵
- 禁止SYSTEM_ADMIN + SECURITY_ADMIN
- 禁止SYSTEM_ADMIN + AUDIT_ADMIN  
- 禁止SECURITY_ADMIN + AUDIT_ADMIN

### 🛡️ 核心功能
1. **实时互斥检查**：角色分配时实时检查是否违反三权分立原则
2. **定时违规扫描**：每天凌晨2点自动扫描所有用户角色，发现违规情况
3. **违规记录管理**：完整记录所有违规操作，支持处理和跟踪
4. **多渠道告警**：支持邮件、短信、企业微信等多种告警方式
5. **可视化监控**：提供三权分立状态监控和违规统计

## 技术架构

### 后端架构
```
bankshield-api/
├── entity/                 # 实体类
│   ├── RoleMutex.java     # 角色互斥规则实体
│   └── RoleViolation.java # 角色违规记录实体
├── mapper/                # 数据访问层
│   ├── RoleMutexMapper.java
│   └── RoleViolationMapper.java
├── service/               # 业务逻辑层
│   ├── RoleCheckService.java      # 角色检查服务接口
│   └── impl/RoleCheckServiceImpl.java # 角色检查服务实现
├── aspect/                # AOP切面
│   └── RoleCheckAspect.java       # 角色检查切面
├── job/                   # 定时任务
│   └── RoleCheckJob.java          # 角色检查定时任务
├── controller/            # 控制层
│   └── RoleCheckController.java   # 角色检查控制器
└── annotation/            # 自定义注解
    └── RoleExclusive.java         # 角色互斥注解
```

### 数据库设计
```sql
-- 角色互斥规则表
CREATE TABLE role_mutex (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
  mutex_role_code VARCHAR(50) NOT NULL COMMENT '互斥角色编码',
  description VARCHAR(255) COMMENT '互斥规则描述',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by VARCHAR(50) COMMENT '创建人',
  UNIQUE KEY uk_role_mutex (role_code, mutex_role_code)
);

-- 角色违规记录表
CREATE TABLE role_violation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  username VARCHAR(50) NOT NULL COMMENT '用户名',
  role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
  mutex_role_code VARCHAR(50) NOT NULL COMMENT '互斥角色编码',
  violation_type TINYINT NOT NULL COMMENT '违规类型',
  violation_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '违规时间',
  operator_id BIGINT COMMENT '操作人ID',
  operator_name VARCHAR(50) COMMENT '操作人姓名',
  status TINYINT DEFAULT 0 COMMENT '处理状态',
  handle_time DATETIME COMMENT '处理时间',
  handle_remark VARCHAR(500) COMMENT '处理备注',
  alert_sent TINYINT DEFAULT 0 COMMENT '是否已发送告警',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);
```

### 前端架构
```
bankshield-ui/
├── src/
│   ├── views/
│   │   └── system/
│   │       └── role-mutex/
│   │           └── index.vue    # 三权分立管理页面
│   ├── api/
│   │   └── role-mutex.js        # 角色互斥API
│   ├── router/
│   │   └── modules/
│   │       └── role-mutex.js    # 路由配置
│   └── utils/
│       └── request.js           # HTTP请求工具
```

## API接口

### 角色分配接口
```http
POST /api/role/assign
Content-Type: application/x-www-form-urlencoded

userId=123&roleCode=SYSTEM_ADMIN
```

### 合规性检查接口
```http
GET /api/role/check/{userId}
```

### 违规记录查询接口
```http
GET /api/role/violations?pageNum=1&pageSize=10&status=0
```

### 违规记录处理接口
```http
PUT /api/role/violation/handle/{id}
Content-Type: application/x-www-form-urlencoded

status=1&handleRemark=已处理&handlerName=管理员
```

### 三权分立状态接口
```http
GET /api/role/separation-status
```

## 配置说明

### 应用配置
```yaml
# 三权分立配置
role:
  mutex:
    enabled: true  # 是否启用角色互斥检查
  violation:
    alert:
      enabled: true  # 是否启用违规告警
  check:
    job:
      cron: "0 0 2 * * ?"  # 角色检查任务执行时间（每天凌晨2点）
```

### 数据库配置
```sql
-- 系统配置项
INSERT INTO sys_config (config_key, config_value, config_name, description) VALUES
('role.mutex.enabled', '1', '角色互斥检查开关', '是否启用三权分立角色互斥检查'),
('role.violation.alert.enabled', '1', '违规告警开关', '是否启用角色违规告警'),
('role.check.job.cron', '0 0 2 * * ?', '角色检查任务定时', '角色互斥检查任务执行时间表达式'),
('role.violation.alert.channels', 'email,sms,wechat', '违规告警渠道', '角色违规告警通知渠道');
```

## 使用指南

### 1. 启用三权分立机制
确保在配置文件中启用三权分立功能：
```yaml
role:
  mutex:
    enabled: true
```

### 2. 角色分配
通过API接口或前端页面进行角色分配时，系统会自动进行三权分立检查：
```java
// 后端服务调用
boolean canAssign = roleCheckService.checkRoleAssignment(userId, roleCode);
```

### 3. 违规监控
系统会定时扫描违规情况，管理员可以通过前端页面查看和处理违规记录。

### 4. 告警通知
当发现角色违规时，系统会通过配置的渠道发送告警通知。

## 测试验证

### 单元测试
运行角色互斥检查服务的单元测试：
```bash
cd bankshield-api
mvn test -Dtest=RoleCheckServiceTest
```

### 集成测试
1. 创建测试用户
2. 分配SYSTEM_ADMIN角色
3. 尝试分配SECURITY_ADMIN角色（应该失败）
4. 验证违规记录生成
5. 处理违规记录

## 安全考虑

### 1. 强制检查
角色分配时的三权分立检查是强制性的，无法绕过。

### 2. 审计日志
所有角色分配和违规处理操作都会被记录到审计日志中。

### 3. 权限控制
只有具有相应权限的管理员才能查看和处理违规记录。

### 4. 数据完整性
通过数据库约束和事务保证数据的一致性和完整性。

## 性能优化

### 1. 缓存机制
互斥规则可以缓存到Redis中，减少数据库查询。

### 2. 异步处理
违规告警采用异步方式发送，不影响主业务流程。

### 3. 批量检查
定时任务采用批量处理方式，提高检查效率。

## 扩展性

### 1. 动态规则
支持动态配置角色互斥规则，无需修改代码。

### 2. 多维度检查
可以扩展支持基于部门、业务线等维度的角色互斥检查。

### 3. 自定义告警
支持自定义告警规则和通知方式。

## 合规性

本实现完全符合《信息安全等级保护》三级标准的以下要求：

1. **权限分离**：实现了系统管理、安全管理、审计管理的三权分立
2. **最小权限**：每个角色只拥有必要的权限
3. **职责分离**：不同职责由不同人员承担
4. **审计追踪**：完整记录所有操作和违规情况
5. **强制访问控制**：通过技术手段强制执行访问控制策略

## 维护指南

### 日常维护
1. 定期检查违规记录处理情况
2. 监控定时任务执行状态
3. 查看系统告警通知

### 故障排查
1. 查看应用日志了解详细错误信息
2. 检查数据库连接和配置
3. 验证角色互斥规则配置

### 性能监控
1. 监控角色检查任务执行时间
2. 关注数据库查询性能
3. 检查告警发送成功率

## 更新记录

| 版本 | 日期 | 更新内容 | 作者 |
|------|------|----------|------|
| 1.0.0 | 2024-01-01 | 初始版本，实现基本三权分立功能 | BankShield |

## 联系方式

如有问题或建议，请联系：
- 技术支持：support@bankshield.com
- 项目地址：https://github.com/your-org/BankShield