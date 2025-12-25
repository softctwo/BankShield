# BankShield 密钥管理模块

## 项目简介

BankShield密钥管理模块是银行数据安全加密系统的核心组件，提供完善的密钥全生命周期管理功能，支持国密算法（SM2/SM3/SM4）和国际算法（AES/RSA）的密钥生成、存储、使用、轮换、销毁等操作。

## 功能特性

### 核心功能
- **密钥生成**：支持多种加密算法的密钥生成
- **密钥存储**：安全的密钥材料加密存储
- **密钥轮换**：自动和手动密钥轮换机制
- **密钥销毁**：安全的密钥销毁流程
- **使用审计**：完整的密钥使用记录和审计
- **统计报表**：密钥使用统计和分析

### 支持的算法
- **国密算法**：SM2（非对称）、SM3（哈希）、SM4（对称）
- **国际算法**：AES（对称）、RSA（非对称）

### 安全特性
- **密钥加密存储**：使用主密钥加密所有密钥材料
- **访问控制**：基于角色的权限管理
- **操作审计**：所有密钥操作都有完整记录
- **定时轮换**：支持自动密钥轮换策略
- **过期提醒**：密钥过期前自动提醒

## 技术架构

### 后端技术栈
- **框架**：Spring Boot 2.7.18
- **数据库**：MySQL 8.0
- **持久层**：MyBatis Plus
- **定时任务**：Quartz
- **国密算法**：Bouncy Castle + Hutool
- **文档生成**：Swagger/OpenAPI

### 前端技术栈
- **框架**：Vue 3 + TypeScript
- **UI组件**：Element Plus
- **图表**：ECharts
- **HTTP客户端**：Axios

## 快速开始

### 环境要求
- Java 8+
- MySQL 8.0+
- Maven 3.6+
- Node.js 14+

### 数据库初始化
```sql
-- 执行SQL脚本创建表结构
mysql -u root -p bankshield < sql/encrypt_module.sql
```

### 后端启动
```bash
# 编译项目
mvn clean package

# 启动服务（开发环境）
./scripts/start-encrypt.sh

# 或者使用Spring Boot直接启动
java -jar bankshield-encrypt/target/bankshield-encrypt-1.0.0.jar
```

### 前端启动
```bash
# 进入前端目录
cd bankshield-ui

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

### 访问系统
- 后端API：http://localhost:8083/encrypt
- 前端界面：http://localhost:3000

## 配置说明

### 应用配置
主要配置项在`application.yml`中：

```yaml
bankshield:
  encrypt:
    # 主密钥配置（用于加密其他密钥）
    master-key: "your-secure-master-key"
    # 存储类型
    storage-type: "SM4"
    # 轮换配置
    rotation:
      default-cycle: 90
      check-ahead-days: 30
      cron: "0 0 2 * * ?"
```

### 环境变量
- `ENCRYPT_MASTER_KEY`：加密主密钥
- `ENCRYPT_STORAGE_TYPE`：存储类型（默认SM4）
- `APP_PORT`：应用端口（默认8083）
- `APP_PROFILE`：运行环境（默认dev）

## API接口

### 密钥管理接口
- `GET /api/key/page` - 分页查询密钥
- `GET /api/key/{id}` - 获取密钥详情
- `POST /api/key/generate` - 生成新密钥
- `POST /api/key/rotate/{id}` - 轮换密钥
- `PUT /api/key/status/{id}` - 更新密钥状态
- `DELETE /api/key/{id}` - 销毁密钥
- `GET /api/key/statistics` - 获取统计信息
- `GET /api/key/usage/{id}` - 获取使用统计

### 使用示例
```bash
# 生成新密钥
curl -X POST http://localhost:8083/encrypt/api/key/generate \
  -H "Content-Type: application/json" \
  -d '{
    "keyName": "test-sm4-key",
    "keyType": "SM4",
    "keyUsage": "ENCRYPT",
    "expireDays": 365,
    "rotationCycle": 90,
    "description": "测试SM4密钥",
    "createdBy": "admin"
  }'

# 查询密钥列表
curl "http://localhost:8083/encrypt/api/key/page?pageNum=1&pageSize=10"
```

## 数据库设计

### 核心表结构
- **encrypt_key**：密钥主表
- **key_rotation_history**：密钥轮换历史
- **key_usage_audit**：密钥使用审计

### 索引设计
- 按密钥类型、状态、过期时间建立复合索引
- 按操作时间和密钥ID建立审计表索引

## 安全建议

### 生产环境配置
1. **主密钥管理**：使用专用的密钥管理系统（KMS）
2. **访问控制**：严格限制SECURITY_ADMIN角色权限
3. **网络隔离**：部署在独立的网络区域
4. **日志监控**：启用详细的操作日志和监控告警
5. **定期审计**：定期检查和审计密钥使用情况

### 密钥管理最佳实践
1. **定期轮换**：建议90天轮换一次密钥
2. **密钥强度**：使用足够长的密钥长度
3. **分离职责**：密钥管理和使用权限分离
4. **备份策略**：制定密钥备份和恢复计划
5. **销毁确认**：密钥销毁后确认无法恢复

## 监控和告警

### 监控指标
- 密钥生成/轮换/销毁操作次数
- 密钥过期提醒
- 异常操作检测
- 系统性能指标

### 告警规则
- 密钥即将过期（30天内）
- 密钥轮换失败
- 异常频繁的操作
- 系统错误率过高

## 故障排查

### 常见问题
1. **密钥生成失败**：检查算法库依赖和配置
2. **存储加密失败**：验证主密钥配置
3. **定时任务不执行**：检查Quartz配置和权限
4. **API访问失败**：检查角色权限配置

### 日志位置
- 应用日志：`logs/bankshield-encrypt.log`
- 错误日志：`logs/error.log`
- 审计日志：`logs/audit.log`

## 开发规范

### 代码规范
- 遵循Spring Boot开发规范
- 使用统一的异常处理机制
- 完善的接口文档和注释
- 单元测试覆盖率>80%

### 安全规范
- 所有敏感数据必须加密存储
- 接口权限严格控制
- 输入参数严格验证
- 防止SQL注入和XSS攻击

## 更新日志

### v1.0.0 (2024-01-01)
- 初始版本发布
- 支持SM2/SM3/SM4国密算法
- 支持AES/RSA国际算法
- 实现密钥全生命周期管理
- 提供完整的前后端功能

## 联系方式

如有问题或建议，请联系开发团队：
- 邮箱：dev@bankshield.com
- 技术支持：support@bankshield.com