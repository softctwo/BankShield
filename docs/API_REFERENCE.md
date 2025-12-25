# BankShield API接口文档

**版本**: v1.0.0
**更新日期**: 2025-12-25
**基础URL**: `http://localhost:8080/api`
**认证方式**: JWT Bearer Token

---

## 📋 目录

- [认证说明](#认证说明)
- [通用响应格式](#通用响应格式)
- [用户管理](#用户管理)
- [角色管理](#角色管理)
- [数据资产管理](#数据资产管理)
- [审计管理](#审计管理)
- [监控告警](#监控告警)
- [合规报告](#合规报告)
- [数据血缘](#数据血缘)
- [加密管理](#加密管理)
- [数据脱敏](#数据脱敏)
- [AI智能](#ai智能)

---

## 🔐 认证说明

所有API请求（除登录接口外）都需要在请求头中携带JWT Token：

```http
Authorization: Bearer <token>
```

**获取Token**: 通过登录接口获取

---

## 📦 通用响应格式

### 成功响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": "2025-12-25T10:00:00"
}
```

### 失败响应

```json
{
  "code": 400,
  "message": "参数错误",
  "data": null,
  "timestamp": "2025-12-25T10:00:00"
}
```

### 状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权或Token过期 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 👤 用户管理

### 获取用户信息

**接口**: `GET /user/{id}`

**请求参数**:
- `id` (path): 用户ID

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "name": "管理员",
    "phone": "138****8888",
    "email": "admin@bankshield.com",
    "deptId": 1,
    "deptName": "信息技术部",
    "status": 1,
    "createTime": "2025-01-01T00:00:00"
  }
}
```

### 分页查询用户

**接口**: `GET /user/page`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页大小，默认10 |
| username | string | 否 | 用户名模糊查询 |
| name | string | 否 | 姓名模糊查询 |
| deptId | long | 否 | 部门ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

### 创建用户

**接口**: `POST /user`

**请求体**:
```json
{
  "username": "testuser",
  "password": "123456",
  "name": "测试用户",
  "phone": "13800138000",
  "email": "test@example.com",
  "deptId": 1
}
```

### 更新用户

**接口**: `PUT /user`

**请求体**:
```json
{
  "id": 1,
  "name": "新姓名",
  "phone": "13900139000",
  "email": "new@example.com",
  "deptId": 2
}
```

### 删除用户

**接口**: `DELETE /user/{id}`

**请求参数**:
- `id` (path): 用户ID

---

## 🎭 角色管理

### 根据ID获取角色

**接口**: `GET /role/{id}`

**请求参数**:
- `id` (path): 角色ID

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "roleName": "超级管理员",
    "roleCode": "SUPER_ADMIN",
    "description": "系统最高权限",
    "status": 1,
    "createTime": "2025-01-01T00:00:00"
  }
}
```

### 分页查询角色

**接口**: `GET /role/page`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页大小，默认10 |
| roleName | string | 否 | 角色名称模糊查询 |
| roleCode | string | 否 | 角色编码模糊查询 |

### 获取所有启用的角色

**接口**: `GET /role/enabled`

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "roleName": "超级管理员",
      "roleCode": "SUPER_ADMIN"
    },
    {
      "id": 2,
      "roleName": "安全管理员",
      "roleCode": "SECURITY_ADMIN"
    }
  ]
}
```

### 添加角色

**接口**: `POST /role`

**请求体**:
```json
{
  "roleName": "新角色",
  "roleCode": "NEW_ROLE",
  "description": "角色描述",
  "status": 1
}
```

### 更新角色

**接口**: `PUT /role`

**请求体**:
```json
{
  "id": 1,
  "roleName": "更新后的角色名",
  "description": "更新后的描述",
  "status": 1
}
```

### 删除角色

**接口**: `DELETE /role/{id}`

**请求参数**:
- `id` (path): 角色ID

---

## 📊 数据资产管理

### 启动资产发现任务

**接口**: `POST /asset/discover`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dataSourceId | long | 是 | 数据源ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "资产发现任务已启动",
  "data": {
    "taskId": 12345,
    "status": "RUNNING"
  }
}
```

### 查询资产详情

**接口**: `GET /asset/{id}`

**请求参数**:
- `id` (path): 资产ID

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "assetName": "客户信息表",
    "assetType": "TABLE",
    "dataSourceName": "生产数据库",
    "securityLevel": 3,
    "status": "APPROVED",
    "createTime": "2025-01-01T00:00:00"
  }
}
```

### 人工标注分级

**接口**: `PUT /asset/{id}/classify`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id (path) | long | 是 | 资产ID |
| manualLevel | int | 是 | 安全等级(1-4) |
| operatorId | long | 是 | 操作人ID |

### 提交审核

**接口**: `POST /asset/{id}/review`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id (path) | long | 是 | 资产ID |
| finalLevel | int | 是 | 最终安全等级 |
| reason | string | 否 | 修改原因 |

### 审核通过/拒绝

**接口**: `PUT /asset/{id}/approve`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id (path) | long | 是 | 资产ID |
| approved | boolean | 是 | 是否通过 |
| comment | string | 否 | 审核意见 |
| reviewerId | long | 是 | 审核人ID |

### 分页查询资产列表

**接口**: `GET /asset/list`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页大小，默认10 |
| assetName | string | 否 | 资产名称模糊查询 |
| assetType | string | 否 | 资产类型 |
| securityLevel | int | 否 | 安全等级 |
| businessLine | string | 否 | 业务条线 |
| status | int | 否 | 状态 |

### 资产地图概览

**接口**: `GET /asset/map/overview`

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalAssets": 1000,
    "classifiedAssets": 850,
    "pendingReviewAssets": 50,
    "approvedAssets": 800,
    "businessLineDistribution": [
      {
        "businessLine": "零售银行",
        "assetCount": 300,
        "percentage": 30
      }
    ],
    "storageDistribution": [
      {
        "storageLocation": "MySQL主库",
        "assetCount": 500,
        "percentage": 50
      }
    ]
  }
}
```

### 资产下钻查询

**接口**: `GET /asset/map/drill-down`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dimension | string | 是 | 维度（businessLine/storageLocation） |
| dimensionValue | string | 否 | 维度值 |
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页大小，默认10 |
| keyword | string | 否 | 关键词搜索 |

### 获取扫描进度

**接口**: `GET /asset/scan-progress/{taskId}`

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "taskId": 12345,
    "status": "RUNNING",
    "totalTables": 100,
    "scannedTables": 50,
    "progress": 50,
    "estimatedTimeRemaining": 300
  }
}
```

### 停止扫描任务

**接口**: `POST /asset/scan-stop/{taskId}`

**请求参数**:
- `taskId` (path): 任务ID

### 批量审核通过

**接口**: `POST /asset/batch-approve`

**请求体**:
```json
{
  "assetIds": [1, 2, 3],
  "reviewerId": 1
}
```

### 批量审核拒绝

**接口**: `POST /asset/batch-reject`

**请求体**:
```json
{
  "assetIds": [1, 2, 3],
  "comment": "不符合规范",
  "reviewerId": 1
}
```

### 获取待审核资产列表

**接口**: `GET /asset/pending-review`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页大小，默认10 |

### 获取风险资产清单

**接口**: `GET /asset/risk-assets`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| riskLevel | string | 否 | 风险等级（HIGH/MEDIUM/LOW） |

### 导出资产清单

**接口**: `GET /asset/export`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| exportType | string | 否 | 导出类型（EXCEL/PDF），默认EXCEL |

---

## 📝 审计管理

### 查询审计日志

**接口**: `GET /audit/list`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页大小，默认10 |
| startTime | string | 否 | 开始时间 |
| endTime | string | 否 | 结束时间 |
| userId | long | 否 | 用户ID |
| operation | string | 否 | 操作类型 |
| module | string | 否 | 模块名称 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 1,
        "username": "admin",
        "operation": "CREATE",
        "module": "用户管理",
        "resource": "用户[testuser]",
        "ipAddress": "192.168.1.100",
        "operationTime": "2025-01-01T10:00:00",
        "status": "SUCCESS"
      }
    ],
    "total": 100
  }
}
```

### 审计日志验证

**接口**: `POST /audit/verify`

**请求体**:
```json
{
  "auditLogId": 1,
  "blockchainHash": "0xabc123..."
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "验证通过",
  "data": {
    "valid": true,
    "blockchainTxId": "0x...",
    "verifyTime": "2025-01-01T10:00:00"
  }
}
```

---

## 📈 监控告警

### 查询告警规则

**接口**: `GET /alert-rules`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页大小，默认10 |
| ruleName | string | 否 | 规则名称 |
| alertLevel | string | 否 | 告警级别 |
| enabled | boolean | 否 | 是否启用 |

### 创建告警规则

**接口**: `POST /alert-rules`

**请求体**:
```json
{
  "ruleName": "CPU使用率告警",
  "metricType": "CPU_USAGE",
  "condition": ">",
  "threshold": 80,
  "duration": 5,
  "alertLevel": "WARNING",
  "enabled": true
}
```

### 更新告警规则

**接口**: `PUT /alert-rules`

**请求体**:
```json
{
  "id": 1,
  "ruleName": "CPU使用率告警",
  "threshold": 85,
  "enabled": false
}
```

### 删除告警规则

**接口**: `DELETE /alert-rules/{id}`

### 查询告警记录

**接口**: `GET /alert-records`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页大小，默认10 |
| ruleId | long | 否 | 规则ID |
| alertLevel | string | 否 | 告警级别 |
| status | string | 否 | 状态 |
| startTime | string | 否 | 开始时间 |
| endTime | string | 否 | 结束时间 |

### 告警确认

**接口**: `PUT /alert-records/{id}/acknowledge`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id (path) | long | 是 | 告警记录ID |
| comment | string | 否 | 确认意见 |

---

## 📋 合规报告

### 生成合规报告

**接口**: `POST /compliance-report/generate`

**请求体**:
```json
{
  "reportType": "WEEKLY",
  "startTime": "2025-01-01T00:00:00",
  "endTime": "2025-01-07T23:59:59",
  "businessLines": ["零售银行", "对公银行"],
  "securityLevels": [1, 2, 3, 4]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "报告生成中",
  "data": {
    "reportId": 1,
    "status": "GENERATING"
  }
}
```

### 查询报告列表

**接口**: `GET /compliance-report/list`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页大小，默认10 |
| reportType | string | 否 | 报告类型 |
| status | string | 否 | 状态 |

### 下载报告

**接口**: `GET /compliance-report/{id}/download`

**请求参数**:
- `id` (path): 报告ID

### 获取报告详情

**接口**: `GET /compliance-report/{id}`

---

## 🔍 数据血缘

### 查询数据血缘链路

**接口**: `GET /lineage/{assetId}`

**请求参数**:
- `assetId` (path): 资产ID

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "nodes": [
      {
        "id": 1,
        "name": "原始数据表",
        "type": "SOURCE"
      },
      {
        "id": 2,
        "name": "中间表",
        "type": "TRANSFORM"
      },
      {
        "id": 3,
        "name": "目标表",
        "type": "TARGET"
      }
    ],
    "edges": [
      {
        "source": 1,
        "target": 2,
        "type": "DATA_FLOW"
      },
      {
        "source": 2,
        "target": 3,
        "type": "DATA_FLOW"
      }
    ]
  }
}
```

### 影响分析

**接口**: `POST /lineage/impact-analysis`

**请求体**:
```json
{
  "assetId": 1,
  "analysisType": "DOWNSTREAM"
}
```

### 数据变更追踪

**接口**: `GET /lineage/changes`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页大小，默认10 |
| assetId | long | 否 | 资产ID |
| startTime | string | 否 | 开始时间 |
| endTime | string | 否 | 结束时间 |

---

## 🔐 加密管理

### 配置加密规则

**接口**: `POST /encrypt/config`

**请求体**:
```json
{
  "tableName": "sys_user",
  "columnName": "phone",
  "algorithm": "SM4",
  "keyId": 1,
  "enabled": true
}
```

### 查询加密配置

**接口**: `GET /encrypt/config`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| tableName | string | 否 | 表名 |
| columnName | string | 否 | 列名 |

### 更新加密密钥

**接口**: `PUT /encrypt/rotate-key`

**请求体**:
```json
{
  "keyId": 1,
  "rotationReason": "定期轮换"
}
```

---

## 🎭 数据脱敏

### 配置脱敏规则

**接口**: `POST /masking/rule`

**请求体**:
```json
{
  "ruleName": "手机号脱敏",
  "fieldType": "PHONE",
  "maskPattern": "138****8888",
  "enabled": true
}
```

### 查询脱敏规则

**接口**: `GET /masking/rules`

### 应用脱敏

**接口**: `POST /masking/apply`

**请求体**:
```json
{
  "dataSourceId": 1,
  "ruleId": 1
}
```

---

## 🤖 AI智能

### AI威胁检测

**接口**: `POST /ai/threat-detect`

**请求体**:
```json
{
  "data": "检测数据",
  "detectionType": "ANOMALY_DETECTION"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "检测完成",
  "data": {
    "threatLevel": "HIGH",
    "confidence": 0.95,
    "threatType": "SQL注入",
    "recommendation": "立即阻断该请求"
  }
}
```

### AI智能推荐

**接口**: `GET /ai/recommendations`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | string | 否 | 推荐类型 |

---

## 📊 监控指标

### 获取系统健康状态

**接口**: `GET /monitor/health`

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "status": "UP",
    "components": {
      "database": {
        "status": "UP",
        "details": {
          "database": "MySQL",
          "validationQuery": "isValid()"
        }
      },
      "redis": {
        "status": "UP",
        "details": {
          "version": "6.0.0"
        }
      }
    }
  }
}
```

### 获取系统指标

**接口**: `GET /monitor/metrics`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| metric | string | 否 | 指标名称 |
| tags | string | 否 | 标签 |

---

## 🔄 数据源管理

### 查询数据源列表

**接口**: `GET /datasource`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页大小，默认10 |

### 添加数据源

**接口**: `POST /datasource`

**请求体**:
```json
{
  "sourceName": "生产数据库",
  "sourceType": "MYSQL",
  "host": "localhost",
  "port": 3306,
  "database": "bankshield",
  "username": "root",
  "password": "password",
  "enabled": true
}
```

---

## ⚠️ 错误码说明

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 1001 | Token无效 | 重新登录获取Token |
| 1002 | Token过期 | 刷新Token或重新登录 |
| 1003 | 权限不足 | 联系管理员分配权限 |
| 2001 | 用户不存在 | 检查用户ID |
| 2002 | 角色不存在 | 检查角色ID |
| 3001 | 数据库连接失败 | 检查数据库配置 |
| 3002 | Redis连接失败 | 检查Redis配置 |

---

## 📞 技术支持

- **技术支持**: tech-support@bankshield.com
- **安全团队**: security@bankshield.com
- **值班电话**: +86-400-123-4567

---

**文档版本**: v1.0.0
**最后更新**: 2025-12-25
