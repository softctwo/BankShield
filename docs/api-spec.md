# API需求规格书

**文档版本**: v1.0.0  
**作者**: BankShield技术团队  
**创建时间**: 2025-12-24  
**最后修订**: 2025-12-24  

## 修订历史

| 版本 | 修订日期 | 修订人 | 修订内容 |
|------|----------|--------|----------|
| v1.0.0 | 2025-12-24 | 技术团队 | 初始版本 |

## 目录

1. [认证机制](#认证机制)
2. [通用规范](#通用规范)
3. [接口清单](#接口清单)
4. [重要接口定义](#重要接口定义)
5. [速率限制](#速率限制)
6. [错误处理](#错误处理)
7. [版本管理](#版本管理)
8. [安全要求](#安全要求)
9. [性能要求](#性能要求)
10. [术语表](#术语表)

## 认证机制

### 1.1 JWT Token认证

#### 1.1.1 Token结构
BankShield采用JWT（JSON Web Token）双Token机制，包含访问令牌（accessToken）和刷新令牌（refreshToken）。

**Token类型说明:**
| Token类型 | 有效期 | 用途 | 存储位置 |
|-----------|--------|------|----------|
| accessToken | 30分钟 | API访问认证 | 内存/localStorage |
| refreshToken | 7天 | 刷新accessToken | HttpOnly Cookie |

#### 1.1.2 Token生成流程
```mermaid
sequenceDiagram
    participant Client
    participant AuthService
    participant TokenService
    participant Redis
    
    Client->>AuthService: POST /auth/login
    AuthService->>AuthService: 验证用户名密码
    AuthService->>TokenService: 生成accessToken
    TokenService->>TokenService: 创建JWT声明
    TokenService->>TokenService: 使用SM2签名
    TokenService-->>AuthService: accessToken
    
    AuthService->>TokenService: 生成refreshToken
    TokenService->>TokenService: 创建JWT声明
    TokenService->>TokenService: 使用SM2签名
    TokenService-->>AuthService: refreshToken
    
    AuthService->>Redis: 存储refreshToken
    AuthService-->>Client: 返回双Token
```

#### 1.1.3 JWT声明内容
```json
{
    "header": {
        "alg": "SM2",
        "typ": "JWT"
    },
    "payload": {
        "sub": "user123",
        "username": "admin",
        "roles": ["ROLE_ADMIN", "ROLE_USER"],
        "permissions": ["user:create", "user:update"],
        "iat": 1640323200,
        "exp": 1640325000,
        "jti": "550e8400-e29b-41d4-a716-446655440000",
        "iss": "BankShield",
        "aud": "BankShield-API"
    }
}
```

#### 1.1.4 Token刷新机制
**刷新请求:**
```http
POST /auth/refresh
Content-Type: application/json
Cookie: refreshToken=eyJhbGciOiJIUzUxMiJ9...

{
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

**刷新响应:**
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
        "tokenType": "Bearer",
        "expiresIn": 1800
    }
}
```

### 1.2 签名验证机制

#### 1.2.1 请求签名算法
所有敏感API请求都需要进行签名验证，确保请求完整性和防重放攻击。

**签名算法:**
```
Signature = SM3-SM2(
    secret_key,
    timestamp + nonce + http_method + request_path + request_body
)
```

#### 1.2.2 签名请求头
```http
POST /api/secure/data
Content-Type: application/json
X-Timestamp: 1640323200
X-Nonce: random_string_32_chars
X-Signature: MEUCIQDxGjZb1dZGqHvJPv0p2mEY...
X-Client-Id: client_123
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...

{
    "data": "sensitive information"
}
```

#### 1.2.3 签名验证流程
```mermaid
flowchart TD
    Client[客户端] --> Gateway[API网关]
    Gateway --> Extract[提取签名参数]
    Extract --> Validate[验证时间戳]
    Validate --> CheckNonce[检查Nonce唯一性]
    CheckNonce --> VerifySig[验证签名]
    VerifySig --> CheckReplay[防重放检查]
    CheckReplay --> Route[路由到后端服务]
    
    Validate -- 时间戳无效 --> RejectTime[拒绝请求]
    CheckNonce -- Nonce重复 --> RejectNonce[拒绝请求]
    VerifySig -- 签名无效 --> RejectSig[拒绝请求]
    CheckReplay -- 重放攻击 --> RejectReplay[拒绝请求]
```

## 通用规范

### 2.1 统一响应格式

#### 2.1.1 成功响应
```json
{
    "code": 200,
    "message": "success",
    "data": {
        // 业务数据
    },
    "timestamp": "2025-12-24T10:00:00Z",
    "requestId": "550e8400-e29b-41d4-a716-446655440000",
    "path": "/api/users",
    "method": "POST"
}
```

#### 2.1.2 错误响应
```json
{
    "code": 400,
    "message": "参数验证失败",
    "errors": [
        {
            "field": "username",
            "message": "用户名长度必须在3-20位之间",
            "code": "VALIDATION_ERROR"
        }
    ],
    "timestamp": "2025-12-24T10:00:00Z",
    "requestId": "660e8400-e29b-41d4-a716-446655440000",
    "path": "/api/users",
    "method": "POST"
}
```

#### 2.1.3 分页响应
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "list": [
            // 数据列表
        ],
        "pagination": {
            "page": 1,
            "size": 10,
            "total": 100,
            "pages": 10,
            "hasNext": true,
            "hasPrevious": false
        }
    },
    "timestamp": "2025-12-24T10:00:00Z",
    "requestId": "770e8400-e29b-41d4-a716-446655440000"
}
```

### 2.2 请求头规范

#### 2.2.1 标准请求头
| 头部名称 | 类型 | 必需 | 描述 |
|----------|------|------|------|
| Content-Type | string | 是 | application/json |
| Accept | string | 是 | application/json |
| Authorization | string | 是 | Bearer {accessToken} |
| X-Request-Id | string | 否 | 请求唯一标识 |
| X-Timestamp | long | 签名时必需 | Unix时间戳 |
| X-Nonce | string | 签名时必需 | 随机字符串 |
| X-Signature | string | 签名时必需 | 请求签名 |
| X-Client-Id | string | 签名时必需 | 客户端ID |

#### 2.2.2 自定义请求头
| 头部名称 | 类型 | 描述 |
|----------|------|------|
| X-User-Id | long | 用户ID（内部使用）|
| X-User-Role | string | 用户角色 |
| X-Department-Id | long | 部门ID |
| X-Trace-Id | string | 链路追踪ID |

### 2.3 响应头规范

#### 2.3.1 标准响应头
| 头部名称 | 类型 | 描述 |
|----------|------|------|
| Content-Type | string | application/json; charset=utf-8 |
| X-Request-Id | string | 请求唯一标识 |
| X-Response-Time | long | 响应时间（毫秒）|
| X-RateLimit-Limit | int | 速率限制上限 |
| X-RateLimit-Remaining | int | 剩余请求次数 |
| X-RateLimit-Reset | long | 重置时间戳 |

#### 2.3.2 安全响应头
```http
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000; includeSubDomains
Content-Security-Policy: default-src 'self'
```

### 2.4 通用参数规范

#### 2.4.1 分页参数
| 参数名 | 类型 | 必需 | 默认值 | 描述 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码，从1开始 |
| size | int | 否 | 10 | 每页大小，最大100 |
| sort | string | 否 | - | 排序字段，如：createdAt,desc |

#### 2.4.2 时间参数
所有时间参数统一使用ISO 8601格式：
```
2025-12-24T10:00:00Z     # UTC时间
2025-12-24T18:00:00+08:00 # 带时区时间
```

#### 2.4.3 状态码定义

**业务状态码:**
| 状态码 | 描述 | 说明 |
|--------|------|------|
| 200 | 成功 | 操作成功完成 |
| 201 | 创建成功 | 资源创建成功 |
| 400 | 参数错误 | 请求参数不合法 |
| 401 | 未认证 | 需要登录认证 |
| 403 | 无权限 | 没有操作权限 |
| 404 | 资源不存在 | 请求的资源不存在 |
| 409 | 冲突 | 资源冲突，如用户名已存在 |
| 422 | 业务校验失败 | 业务规则校验失败 |
| 429 | 请求过频 | 触发速率限制 |
| 500 | 系统错误 | 内部服务器错误 |
| 503 | 服务不可用 | 服务暂时不可用 |

## 接口清单

### 3.1 认证授权接口

#### 3.1.1 认证接口
| 接口名称 | 方法 | 路径 | 描述 |
|----------|------|------|------|
| 用户登录 | POST | /auth/login | 用户身份认证 |
| Token刷新 | POST | /auth/refresh | 刷新访问令牌 |
| 用户登出 | POST | /auth/logout | 用户退出登录 |
| 获取用户信息 | GET | /auth/userinfo | 获取当前用户信息 |
| 修改密码 | PUT | /auth/password | 修改用户密码 |
| 重置密码 | POST | /auth/reset-password | 重置用户密码 |
| 发送验证码 | POST | /auth/captcha | 发送手机验证码 |
| 验证验证码 | POST | /auth/verify-captcha | 验证手机验证码 |

#### 3.1.2 权限接口
| 接口名称 | 方法 | 路径 | 描述 |
|----------|------|------|------|
| 获取用户权限 | GET | /auth/permissions | 获取用户权限列表 |
| 获取用户菜单 | GET | /auth/menus | 获取用户可访问菜单 |
| 验证权限 | POST | /auth/check-permission | 验证用户权限 |

### 3.2 用户管理接口

#### 3.2.1 用户CRUD接口
| 接口名称 | 方法 | 路径 | 描述 |
|----------|------|------|------|
| 创建用户 | POST | /api/users | 创建新用户 |
| 获取用户列表 | GET | /api/users | 分页查询用户 |
| 获取用户详情 | GET | /api/users/{id} | 获取用户信息 |
| 更新用户 | PUT | /api/users/{id} | 更新用户信息 |
| 删除用户 | DELETE | /api/users/{id} | 删除用户 |
| 批量删除用户 | DELETE | /api/users/batch | 批量删除用户 |
| 导入用户 | POST | /api/users/import | 批量导入用户 |
| 导出用户 | GET | /api/users/export | 导出用户数据 |

#### 3.2.2 用户状态接口
| 接口名称 | 方法 | 路径 | 描述 |
|----------|------|------|------|
| 启用用户 | PUT | /api/users/{id}/enable | 启用用户 |
| 禁用用户 | PUT | /api/users/{id}/disable | 禁用用户 |
| 解锁用户 | PUT | /api/users/{id}/unlock | 解锁用户 |
| 重置用户密码 | PUT | /api/users/{id}/reset-password | 重置密码 |

### 3.3 角色权限接口

#### 3.3.1 角色管理接口
| 接口名称 | 方法 | 路径 | 描述 |
|----------|------|------|------|
| 创建角色 | POST | /api/roles | 创建新角色 |
| 获取角色列表 | GET | /api/roles | 分页查询角色 |
| 获取角色详情 | GET | /api/roles/{id} | 获取角色信息 |
| 更新角色 | PUT | /api/roles/{id} | 更新角色信息 |
| 删除角色 | DELETE | /api/roles/{id} | 删除角色 |

#### 3.3.2 权限管理接口
| 接口名称 | 方法 | 路径 | 描述 |
|----------|------|------|------|
| 获取权限列表 | GET | /api/permissions | 获取所有权限 |
| 获取菜单权限 | GET | /api/permissions/menus | 获取菜单权限 |
| 获取按钮权限 | GET | /api/permissions/buttons | 获取按钮权限 |
| 分配角色权限 | PUT | /api/roles/{id}/permissions | 分配权限给角色 |
| 获取角色权限 | GET | /api/roles/{id}/permissions | 获取角色权限 |

### 3.4 密钥管理接口

#### 3.4.1 密钥生命周期接口
| 接口名称 | 方法 | 路径 | 描述 |
|----------|------|------|------|
| 生成密钥 | POST | /api/keys | 生成新密钥 |
| 获取密钥列表 | GET | /api/keys | 分页查询密钥 |
| 获取密钥详情 | GET | /api/keys/{id} | 获取密钥信息 |
| 更新密钥 | PUT | /api/keys/{id} | 更新密钥信息 |
| 删除密钥 | DELETE | /api/keys/{id} | 删除密钥 |
| 激活密钥 | PUT | /api/keys/{id}/activate | 激活密钥 |
| 禁用密钥 | PUT | /api/keys/{id}/disable | 禁用密钥 |
| 销毁密钥 | PUT | /api/keys/{id}/destroy | 销毁密钥 |

#### 3.4.2 密钥轮换接口
| 接口名称 | 方法 | 路径 | 描述 |
|----------|------|------|------|
| 密钥轮换 | POST | /api/keys/{id}/rotate | 轮换密钥 |
| 获取轮换历史 | GET | /api/keys/{id}/rotation-history | 获取轮换历史 |
| 计划轮换 | POST | /api/keys/schedule-rotation | 计划密钥轮换 |

### 3.5 数据脱敏接口

#### 3.5.1 脱敏规则接口
| 接口名称 | 方法 | 路径 | 描述 |
|----------|------|------|------|
| 创建脱敏规则 | POST | /api/desens/rules | 创建脱敏规则 |
| 获取脱敏规则列表 | GET | /api/desens/rules | 分页查询脱敏规则 |
| 获取脱敏规则详情 | GET | /api/desens/rules/{id} | 获取脱敏规则 |
| 更新脱敏规则 | PUT | /api/desens/rules/{id} | 更新脱敏规则 |
| 删除脱敏规则 | DELETE | /api/desens/rules/{id} | 删除脱敏规则 |
| 测试脱敏效果 | POST | /api/desens/test | 测试脱敏效果 |
| 应用脱敏规则 | POST | /api/desens/apply | 应用脱敏规则 |

#### 3.5.2 敏感数据识别接口
| 接口名称 | 方法 | 路径 | 描述 |
|----------|------|------|------|
| 扫描数据源 | POST | /api/desens/scan | 扫描敏感数据 |
| 获取扫描结果 | GET | /api/desens/scan-results | 获取扫描结果 |
| 标记敏感字段 | PUT | /api/desens/fields/{id}/mark | 标记敏感字段 |
| 审核敏感字段 | PUT | /api/desens/fields/{id}/review | 审核敏感字段 |

### 3.6 审计日志接口

#### 3.6.1 审计查询接口
| 接口名称 | 方法 | 路径 | 描述 |
|----------|------|------|------|
| 查询操作审计 | GET | /api/audit/operations | 查询操作审计日志 |
| 查询登录审计 | GET | /api/audit/logins | 查询登录审计日志 |
| 查询密钥审计 | GET | /api/audit/keys | 查询密钥审计日志 |
| 获取审计详情 | GET | /api/audit/logs/{id} | 获取审计日志详情 |

#### 3.6.2 审计报表接口
| 接口名称 | 方法 | 路径 | 描述 |
|----------|------|------|------|
| 生成合规报表 | POST | /api/audit/reports/compliance | 生成合规报表 |
| 生成安全报表 | POST | /api/audit/reports/security | 生成安全报表 |
| 获取报表列表 | GET | /api/audit/reports | 获取报表列表 |
| 下载报表 | GET | /api/audit/reports/{id}/download | 下载报表文件 |

### 3.7 监控告警接口

#### 3.7.1 监控指标接口
| 接口名称 | 方法 | 路径 | 描述 |
|----------|------|------|------|
| 获取系统指标 | GET | /api/monitor/metrics | 获取系统监控指标 |
| 获取服务健康 | GET | /api/monitor/health | 获取服务健康状态 |
| 获取性能数据 | GET | /api/monitor/performance | 获取性能监控数据 |

#### 3.7.2 告警管理接口
| 接口名称 | 方法 | 路径 | 描述 |
|----------|------|------|------|
| 获取告警列表 | GET | /api/alerts | 获取告警列表 |
| 获取告警详情 | GET | /api/alerts/{id} | 获取告警详情 |
| 处理告警 | PUT | /api/alerts/{id}/handle | 处理告警 |
| 关闭告警 | PUT | /api/alerts/{id}/close | 关闭告警 |
| 获取告警统计 | GET | /api/alerts/statistics | 获取告警统计 |

## 重要接口定义

### 4.1 用户登录接口

#### 4.1.1 接口详情
- **接口地址:** POST /auth/login
- **接口描述:** 用户身份认证，获取访问令牌
- **权限要求:** 无
- **速率限制:** 10次/分钟/IP

#### 4.1.2 请求参数
```json
{
    "username": "admin",
    "password": "password123",
    "captcha": "8a9b2c",
    "rememberMe": true,
    "clientId": "web_client",
    "clientVersion": "1.0.0"
}
```

**参数说明:**
| 参数名 | 类型 | 必需 | 描述 |
|--------|------|------|------|
| username | string | 是 | 用户名，3-20位字母数字 |
| password | string | 是 | 密码，6-32位字符 |
| captcha | string | 条件必需 | 验证码，登录失败3次后必需 |
| rememberMe | boolean | 否 | 记住我，默认false |
| clientId | string | 否 | 客户端ID |
| clientVersion | string | 否 | 客户端版本 |

#### 4.1.3 响应数据
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyMTIzIiwidXNlcm5hbWUiOiJhZG1pbiIsInJvbGVzIjpbIlJPTEVfQURNSU4iXSwicGVybWlzc2lvbnMiOlsidXNlcjpjcmVhdGUiLCJ1c2VyOnVwZGF0ZSJdLCJpYXQiOjE2NDAzMjMyMDAsImV4cCI6MTY0MDMyNTAwMCwianRpIjoiNTUwZTg0MDAtZTI5Yi00MWQ0LWE3MTYtNDQ2NjU1NDQwMDAwIiwiaXNzIjoiQmFua1NoaWVsZCIsImF1ZCI6IkJhbmtTaGllbGQtQVBJIn0.signature",
        "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyMTIzIiwidXNlcm5hbWUiOiJhZG1pbiIsImlhdCI6MTY0MDMyMzIwMCwiZXhwIjoxNjQwOTI4MDAwLCJqdGkiOiI2NjBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAifQ.signature",
        "tokenType": "Bearer",
        "expiresIn": 1800,
        "user": {
            "id": 1,
            "username": "admin",
            "realName": "系统管理员",
            "email": "admin@bankshield.com",
            "phone": "13800138000",
            "avatar": "https://bankshield.com/avatar/admin.png",
            "department": {
                "id": 1,
                "name": "信息技术部",
                "code": "IT"
            },
            "roles": [
                {
                    "id": 1,
                    "name": "系统管理员",
                    "code": "ROLE_ADMIN"
                }
            ],
            "permissions": [
                "user:create",
                "user:update",
                "user:delete",
                "role:create",
                "role:update",
                "role:delete"
            ],
            "lastLoginTime": "2025-12-24T09:30:00Z",
            "lastLoginIp": "192.168.1.100"
        }
    },
    "timestamp": "2025-12-24T10:00:00Z",
    "requestId": "880e8400-e29b-41d4-a716-446655440000",
    "path": "/auth/login",
    "method": "POST"
}
```

#### 4.1.4 错误码定义
| 错误码 | 描述 | 说明 |
|--------|------|------|
| 400001 | 参数验证失败 | 请求参数格式错误 |
| 400002 | 验证码错误 | 图形验证码不正确 |
| 401001 | 用户名或密码错误 | 认证失败 |
| 401002 | 用户已锁定 | 账户被锁定 |
| 401003 | 用户已禁用 | 账户被禁用 |
| 429001 | 登录频率过高 | 触发限流保护 |

### 4.2 用户管理接口

#### 4.2.1 创建用户接口
- **接口地址:** POST /api/users
- **接口描述:** 创建新用户
- **权限要求:** user:create

**请求参数:**
```json
{
    "username": "newuser",
    "password": "password123",
    "realName": "新用户",
    "email": "newuser@bankshield.com",
    "phone": "13800138000",
    "status": 1,
    "roleIds": [2, 3],
    "departmentId": 5,
    "jobTitle": "开发工程师",
    "workNumber": "EMP001",
    "remark": "这是一个测试用户"
}
```

**响应数据:**
```json
{
    "code": 201,
    "message": "用户创建成功",
    "data": {
        "id": 123,
        "username": "newuser",
        "realName": "新用户",
        "email": "newuser@bankshield.com",
        "phone": "13800138000",
        "status": 1,
        "department": {
            "id": 5,
            "name": "研发部"
        },
        "roles": [
            {
                "id": 2,
                "name": "普通用户"
            },
            {
                "id": 3,
                "name": "开发人员"
            }
        ],
        "createdAt": "2025-12-24T10:00:00Z",
        "createdBy": "admin"
    }
}
```

#### 4.2.2 分页查询用户接口
- **接口地址:** GET /api/users
- **接口描述:** 分页查询用户列表
- **权限要求:** user:query

**请求参数:**
```
GET /api/users?page=1&size=10&sort=createdAt,desc&username=new&status=1&departmentId=5&roleId=2
```

**参数说明:**
| 参数名 | 类型 | 必需 | 描述 |
|--------|------|------|------|
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页大小，默认10，最大100 |
| sort | string | 否 | 排序字段，如：createdAt,desc |
| username | string | 否 | 用户名模糊查询 |
| realName | string | 否 | 真实姓名模糊查询 |
| email | string | 否 | 邮箱模糊查询 |
| phone | string | 否 | 手机号模糊查询 |
| status | int | 否 | 状态筛选 |
| departmentId | long | 否 | 部门ID筛选 |
| roleId | long | 否 | 角色ID筛选 |
| createdAtStart | string | 否 | 创建开始时间 |
| createdAtEnd | string | 否 | 创建结束时间 |

### 4.3 密钥管理接口

#### 4.3.1 生成密钥接口
- **接口地址:** POST /api/keys
- **接口描述:** 生成新的加密密钥
- **权限要求:** key:create

**请求参数:**
```json
{
    "keyType": "SM4",
    "keySize": 128,
    "algorithm": "SM4/CBC/PKCS7Padding",
    "purpose": "ENCRYPT",
    "validityDays": 365,
    "name": "客户数据加密密钥",
    "description": "用于加密客户敏感数据的对称密钥",
    "rotationPolicy": {
        "enabled": true,
        "rotationCycle": 90,
        "advanceNotice": 30
    }
}
```

**响应数据:**
```json
{
    "code": 201,
    "message": "密钥生成成功",
    "data": {
        "id": 1001,
        "name": "客户数据加密密钥",
        "keyType": "SM4",
        "keySize": 128,
        "algorithm": "SM4/CBC/PKCS7Padding",
        "purpose": "ENCRYPT",
        "status": "ACTIVE",
        "validityStart": "2025-12-24T10:00:00Z",
        "validityEnd": "2026-12-24T10:00:00Z",
        "keyFingerprint": "a1b2c3d4e5f6...",
        "createdAt": "2025-12-24T10:00:00Z",
        "createdBy": "admin",
        "rotationPolicy": {
            "enabled": true,
            "rotationCycle": 90,
            "advanceNotice": 30,
            "nextRotationTime": "2026-03-24T10:00:00Z"
        }
    }
}
```

#### 4.3.2 密钥轮换接口
- **接口地址:** POST /api/keys/{id}/rotate
- **接口描述:** 轮换指定密钥
- **权限要求:** key:rotate

**请求参数:**
```json
{
    "reason": "定期轮换",
    "immediate": false,
    "notification": true,
    "approvalRequired": false
}
```

**响应数据:**
```json
{
    "code": 200,
    "message": "密钥轮换成功",
    "data": {
        "oldKeyId": 1001,
        "newKeyId": 1002,
        "rotationTime": "2025-12-24T10:00:00Z",
        "status": "ROTATING",
        "nextRotationCheck": "2025-12-31T10:00:00Z"
    }
}
```

### 4.4 数据脱敏接口

#### 4.4.1 创建脱敏规则接口
- **接口地址:** POST /api/desens/rules
- **接口描述:** 创建数据脱敏规则
- **权限要求:** desens:create

**请求参数:**
```json
{
    "name": "客户信息脱敏规则",
    "description": "适用于客户敏感信息的脱敏处理",
    "priority": 1,
    "conditions": {
        "database": "customer_db",
        "table": "customers",
        "columns": ["phone", "id_card", "email"],
        "userRoles": ["SALES", "CUSTOMER_SERVICE"]
    },
    "actions": {
        "phone": {
            "algorithm": "PARTIAL_MASK",
            "parameters": {
                "prefixLength": 3,
                "suffixLength": 4,
                "maskChar": "*"
            }
        },
        "id_card": {
            "algorithm": "PARTIAL_MASK",
            "parameters": {
                "prefixLength": 4,
                "suffixLength": 4,
                "maskChar": "*"
            }
        },
        "email": {
            "algorithm": "PARTIAL_MASK",
            "parameters": {
                "prefixLength": 2,
                "suffixLength": 2,
                "maskChar": "*"
            }
        }
    },
    "exceptions": {
        "adminRoles": ["ADMIN", "SECURITY_ADMIN"],
        "specificUsers": ["manager1", "director1"]
    },
    "enabled": true
}
```

#### 4.4.2 测试脱敏效果接口
- **接口地址:** POST /api/desens/test
- **接口描述:** 测试脱敏算法效果
- **权限要求:** desens:query

**请求参数:**
```json
{
    "algorithm": "PARTIAL_MASK",
    "data": "13800138000",
    "parameters": {
        "prefixLength": 3,
        "suffixLength": 4,
        "maskChar": "*"
    }
}
```

**响应数据:**
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "original": "13800138000",
        "desensitized": "138****8000",
        "algorithm": "PARTIAL_MASK",
        "parameters": {
            "prefixLength": 3,
            "suffixLength": 4,
            "maskChar": "*"
        }
    }
}
```

### 4.5 审计日志接口

#### 4.5.1 查询操作审计接口
- **接口地址:** GET /api/audit/operations
- **接口描述:** 查询操作审计日志
- **权限要求:** audit:query

**请求参数:**
```
GET /api/audit/operations?page=1&size=20&sort=operationTime,desc&userId=123&operationType=UPDATE&resourceType=USER&startTime=2025-12-01&endTime=2025-12-24
```

**响应数据:**
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "list": [
            {
                "id": 10001,
                "userId": 123,
                "username": "admin",
                "realName": "系统管理员",
                "operation": "UPDATE",
                "resourceType": "USER",
                "resourceId": 456,
                "resourceName": "testuser",
                "description": "更新用户信息",
                "beforeData": "{\"status\": 1, \"email\": \"old@email.com\"}",
                "afterData": "{\"status\": 0, \"email\": \"new@email.com\"}",
                "operationTime": "2025-12-24T10:00:00Z",
                "ipAddress": "192.168.1.100",
                "userAgent": "Mozilla/5.0...",
                "result": "SUCCESS",
                "duration": 150,
                "requestId": "990e8400-e29b-41d4-a716-446655440000"
            }
        ],
        "pagination": {
            "page": 1,
            "size": 20,
            "total": 156,
            "pages": 8,
            "hasNext": true,
            "hasPrevious": false
        }
    }
}
```

#### 4.5.2 生成合规报表接口
- **接口地址:** POST /api/audit/reports/compliance
- **接口描述:** 生成合规性审计报表
- **权限要求:** audit:report

**请求参数:**
```json
{
    "reportType": "GRADE_PROTECTION_LEVEL3",
    "startTime": "2025-12-01T00:00:00Z",
    "endTime": "2025-12-24T23:59:59Z",
    "format": "PDF",
    "template": "default",
    "includeDetails": true,
    "language": "zh-CN"
}
```

**响应数据:**
```json
{
    "code": 201,
    "message": "报表生成任务已创建",
    "data": {
        "reportId": "RPT2025122410001",
        "status": "GENERATING",
        "reportType": "GRADE_PROTECTION_LEVEL3",
        "startTime": "2025-12-01T00:00:00Z",
        "endTime": "2025-12-24T23:59:59Z",
        "format": "PDF",
        "estimatedCompletionTime": "2025-12-24T10:05:00Z",
        "createdAt": "2025-12-24T10:00:00Z",
        "createdBy": "admin"
    }
}
```

### 4.6 监控告警接口

#### 4.6.1 获取系统指标接口
- **接口地址:** GET /api/monitor/metrics
- **接口描述:** 获取系统监控指标
- **权限要求:** monitor:query

**请求参数:**
```
GET /api/monitor/metrics?metricType=system&timeRange=1h&aggregation=avg
```

**响应数据:**
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "timestamp": "2025-12-24T10:00:00Z",
        "metrics": {
            "cpu": {
                "usage": 45.2,
                "cores": 16,
                "loadAverage": 2.1
            },
            "memory": {
                "total": 32,
                "used": 18.5,
                "free": 13.5,
                "usagePercent": 57.8
            },
            "disk": {
                "total": 1000,
                "used": 450,
                "free": 550,
                "usagePercent": 45.0
            },
            "network": {
                "bytesIn": 1250000,
                "bytesOut": 980000,
                "packetsIn": 15000,
                "packetsOut": 12000
            }
        },
        "trends": {
            "cpuTrend": [40.1, 42.3, 44.8, 45.2],
            "memoryTrend": [55.2, 56.1, 57.0, 57.8]
        }
    }
}
```

## 速率限制

### 5.1 限制策略

#### 5.1.1 基于IP的限制
| 接口类型 | 限制次数 | 时间窗口 | 限制范围 |
|----------|----------|----------|----------|
| 认证接口 | 10次 | 1分钟 | 每IP |
| 普通接口 | 100次 | 1分钟 | 每IP |
| 文件上传 | 5次 | 1分钟 | 每IP |
| 导出接口 | 20次 | 1分钟 | 每IP |

#### 5.1.2 基于用户的限制
| 接口类型 | 限制次数 | 时间窗口 | 限制范围 |
|----------|----------|----------|----------|
| 所有接口 | 200次 | 1分钟 | 每用户 |
| 批量操作 | 50次 | 1分钟 | 每用户 |
| 报表生成 | 10次 | 1小时 | 每用户 |
| 数据导出 | 100次 | 1小时 | 每用户 |

#### 5.1.3 基于接口的限制
| 接口路径 | 限制次数 | 时间窗口 | 备注 |
|----------|----------|----------|------|
| /auth/login | 5次 | 1分钟 | 防暴力破解 |
| /auth/refresh | 50次 | 1分钟 | Token刷新 |
| /api/users/import | 2次 | 1分钟 | 批量导入 |
| /api/reports/generate | 5次 | 10分钟 | 报表生成 |

### 5.2 限制响应

#### 5.2.1 触发限流响应
```http
HTTP/1.1 429 Too Many Requests
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1640323260
Retry-After: 60
Content-Type: application/json

{
    "code": 429,
    "message": "请求过于频繁，请稍后再试",
    "data": {
        "limit": 100,
        "remaining": 0,
        "resetTime": "2025-12-24T10:01:00Z",
        "retryAfter": 60
    }
}
```

#### 5.2.2 响应头说明
| 响应头 | 描述 |
|--------|------|
| X-RateLimit-Limit | 时间窗口内的请求上限 |
| X-RateLimit-Remaining | 剩余可用请求次数 |
| X-RateLimit-Reset | 限流重置时间戳 |
| Retry-After | 建议等待时间（秒） |

## 错误处理

### 6.1 错误响应格式

#### 6.1.1 标准错误响应
```json
{
    "code": 400,
    "message": "请求参数错误",
    "errors": [
        {
            "field": "username",
            "message": "用户名不能为空",
            "code": "NOT_NULL",
            "rejectedValue": null
        },
        {
            "field": "password",
            "message": "密码长度必须在6-32位之间",
            "code": "LENGTH",
            "rejectedValue": "123"
        }
    ],
    "timestamp": "2025-12-24T10:00:00Z",
    "requestId": "110e8400-e29b-41d4-a716-446655440000",
    "path": "/api/users",
    "method": "POST"
}
```

### 6.2 错误码分类

#### 6.2.1 客户端错误 (4xx)
| 错误码 | HTTP状态 | 描述 | 解决建议 |
|--------|----------|------|----------|
| 400001 | 400 | 参数验证失败 | 检查请求参数 |
| 400002 | 400 | JSON格式错误 | 检查JSON格式 |
| 401001 | 401 | 认证失败 | 重新登录 |
| 401002 | 401 | Token过期 | 刷新Token |
| 401003 | 401 | 签名验证失败 | 检查签名算法 |
| 403001 | 403 | 权限不足 | 申请相应权限 |
| 404001 | 404 | 资源不存在 | 检查资源ID |
| 409001 | 409 | 资源冲突 | 检查唯一约束 |
| 422001 | 422 | 业务校验失败 | 检查业务规则 |
| 429001 | 429 | 请求过频 | 降低请求频率 |

#### 6.2.2 服务器错误 (5xx)
| 错误码 | HTTP状态 | 描述 | 解决建议 |
|--------|----------|------|----------|
| 500001 | 500 | 数据库错误 | 稍后重试 |
| 500002 | 500 | 缓存服务错误 | 联系技术支持 |
| 500003 | 500 | 消息队列错误 | 联系技术支持 |
| 500004 | 500 | 外部服务错误 | 联系技术支持 |
| 503001 | 503 | 服务维护中 | 等待维护完成 |

### 6.3 异常处理流程

#### 6.3.1 全局异常处理
```mermaid
flowchart TD
    Request[API请求] --> Validation[参数验证]
    Validation -- 验证失败 --> ValidationError[参数异常]
    Validation -- 验证成功 --> BusinessLogic[业务处理]
    
    BusinessLogic -- 业务异常 --> BusinessError[业务异常]
    BusinessLogic -- 系统异常 --> SystemError[系统异常]
    BusinessLogic -- 成功 --> Success[成功响应]
    
    ValidationError --> ErrorResponse[错误响应]
    BusinessError --> ErrorResponse
    SystemError --> ErrorResponse
    
    ErrorResponse --> LogError[记录错误日志]
    SystemError --> SendAlert[发送告警]
    
    ErrorResponse --> Client[返回客户端]
```

## 版本管理

### 7.1 API版本策略

#### 7.1.1 版本标识方式
BankShield采用URL路径版本标识方式：
```
https://api.bankshield.com/v1/users
https://api.bankshield.com/v2/users
```

#### 7.1.2 版本兼容性规则
| 变更类型 | 兼容性 | 版本策略 |
|----------|--------|----------|
| 新增接口 | 向后兼容 | 小版本升级 |
| 新增字段 | 向后兼容 | 小版本升级 |
| 字段废弃 | 向后兼容 | 小版本升级 |
| 接口删除 | 不兼容 | 大版本升级 |
| 字段删除 | 不兼容 | 大版本升级 |
| 参数变更 | 不兼容 | 大版本升级 |

### 7.2 版本生命周期

#### 7.2.1 版本支持策略
```yaml
api_version_lifecycle:
  current_version: "v1.1"
  supported_versions:
    - version: "v1.1"
      status: "current"
      support_end: "2026-12-31"
      
    - version: "v1.0"
      status: "deprecated"
      deprecation_date: "2025-06-30"
      support_end: "2025-12-31"
      
    - version: "v2.0"
      status: "planned"
      release_date: "2026-01-01"
```

#### 7.2.2 版本迁移指南
- 提前6个月发布版本弃用通知
- 提供详细的迁移文档和工具
- 新旧版本并行运行3个月
- 提供技术支持协助迁移

## 安全要求

### 8.1 传输安全

#### 8.1.1 TLS配置要求
- 强制使用TLS 1.3
- 禁用TLS 1.0和1.1
- 支持前向保密
- 证书有效期不超过2年

#### 8.1.2 加密套件
```
TLS_AES_256_GCM_SHA384
TLS_CHACHA20_POLY1305_SHA256
TLS_AES_128_GCM_SHA256
```

### 8.2 认证安全

#### 8.2.1 Token安全
- 使用强随机数生成Token
- Token签名使用国密SM2算法
- 支持Token黑名单机制
- 定期轮换签名密钥

#### 8.2.2 密码安全
- 密码使用BCrypt加密存储
- 支持密码复杂度策略
- 定期密码过期提醒
- 登录失败锁定机制

### 8.3 数据安全

#### 8.3.1 敏感数据加密
| 数据类型 | 加密算法 | 密钥管理 | 存储位置 |
|----------|----------|----------|----------|
| 密码 | BCrypt | 固定盐值 | 数据库 |
| 手机号 | SM4 | 定期轮换 | 数据库 |
| 身份证 | SM4 | 定期轮换 | 数据库 |
| 银行卡号 | SM4 | 定期轮换 | 数据库 |
| Token签名 | SM2 | 定期轮换 | 配置文件 |

#### 8.3.2 数据脱敏
- 日志中敏感数据自动脱敏
- 接口返回数据按需脱敏
- 支持基于角色的脱敏策略
- 脱敏算法支持多种模式

### 8.4 审计安全

#### 8.4.1 审计日志内容
```json
{
    "timestamp": "2025-12-24T10:00:00Z",
    "userId": 123,
    "username": "admin",
    "operation": "CREATE",
    "resourceType": "USER",
    "resourceId": 456,
    "description": "创建新用户",
    "ipAddress": "192.168.1.100",
    "userAgent": "Mozilla/5.0...",
    "result": "SUCCESS",
    "requestId": "120e8400-e29b-41d4-a716-446655440000",
    "duration": 200
}
```

#### 8.4.2 审计覆盖范围
- 所有用户操作（CRUD）
- 认证和授权事件
- 系统配置变更
- 异常访问行为
- 安全事件记录

## 性能要求

### 9.1 响应时间要求

#### 9.1.1 API响应时间
| 接口类型 | 目标响应时间 | 测量方法 | 达标标准 |
|----------|--------------|----------|----------|
| 认证接口 | <100ms | 95th percentile | 必须达标 |
| 查询接口 | <200ms | 95th percentile | 必须达标 |
| 创建接口 | <300ms | 95th percentile | 必须达标 |
| 复杂查询 | <500ms | 95th percentile | 推荐达标 |
| 报表生成 | <5s | 平均响应时间 | 推荐达标 |

#### 9.1.2 并发处理能力
| 指标类型 | 目标值 | 测试场景 | 持续时间 |
|----------|--------|----------|----------|
| 并发用户数 | 2000 | 混合业务场景 | 30分钟 |
| 每秒事务数 | 5000 | 读写混合 | 30分钟 |
| 峰值处理能力 | 8000 TPS | 压力测试 | 5分钟 |

### 9.2 资源使用要求

#### 9.2.1 服务器资源
| 资源类型 | 目标使用率 | 告警阈值 | 峰值容忍 |
|----------|------------|----------|----------|
| CPU | <70% | 80% | 90% |
| 内存 | <80% | 90% | 95% |
| 磁盘I/O | <70% | 80% | 85% |
| 网络带宽 | <70% | 85% | 90% |

#### 9.2.2 数据库性能
| 指标类型 | 目标值 | 测量方法 | 优化策略 |
|----------|--------|----------|----------|
| 查询响应时间 | <50ms | 慢查询日志 | 索引优化 |
| 连接池利用率 | <80% | 连接池监控 | 连接调优 |
| 事务响应时间 | <100ms | APM监控 | SQL优化 |
| 并发连接数 | <最大连接数80% | 数据库监控 | 连接池调优 |

### 9.3 性能优化策略

#### 9.3.1 缓存策略
```yaml
cache_optimization:
  redis_cache:
    user_info: "10分钟"
    user_permissions: "1小时"
    system_config: "30分钟"
    
  local_cache:
    dictionary_data: "1小时"
    static_config: "24小时"
    
  cache_patterns:
    - "cache-aside": "读多写少"
    - "write-through": "读写均衡"
    - "write-behind": "写多读少"
```

#### 9.3.2 数据库优化
- 热点数据添加索引
- 复杂查询使用读写分离
- 大批量操作使用批量处理
- 定期分析和优化慢查询

## 术语表

| 术语 | 英文全称 | 中文解释 |
|------|----------|----------|
| API | Application Programming Interface | 应用程序接口 |
| REST | Representational State Transfer | 表述性状态转移 |
| JWT | JSON Web Token | JSON网络令牌 |
| HTTP | HyperText Transfer Protocol | 超文本传输协议 |
| TLS | Transport Layer Security | 传输层安全协议 |
| JSON | JavaScript Object Notation | JavaScript对象表示法 |
| CRUD | Create, Read, Update, Delete | 增删改查操作 |
| RBAC | Role-Based Access Control | 基于角色的访问控制 |
| SM2/3/4 | ShangMi Algorithm | 国密算法 |
| BCrypt | Blowfish Crypt | 密码哈希算法 |
| QoS | Quality of Service | 服务质量 |
| SLA | Service Level Agreement | 服务等级协议 |
| RPS | Requests Per Second | 每秒请求数 |
| TPS | Transactions Per Second | 每秒事务数 |
| HMAC | Hash-based Message Authentication Code | 基于哈希的消息认证码 |
| CSP | Content Security Policy | 内容安全策略 |
| XSS | Cross-Site Scripting | 跨站脚本攻击 |
| CSRF | Cross-Site Request Forgery | 跨站请求伪造 |
| OWASP | Open Web Application Security Project | 开放式Web应用程序安全项目 |
| APM | Application Performance Monitoring | 应用性能监控 |