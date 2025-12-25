# BankShield Gateway 安全增强模块

## 概述

BankShield Gateway 是 BankShield 系统的 API 网关模块，提供了完整的安全增强功能，包括限流、防刷、签名验证、IP黑名单、API审计等功能。

## 功能特性

### 1. 限流功能 (Rate Limiting)
- **多维度限流**: 支持IP、用户、API、全局四个维度的限流
- **灵活配置**: 支持动态配置限流规则，无需重启服务
- **Redis集群支持**: 基于Redis Lua脚本实现分布式限流
- **实时统计**: 提供限流命中统计和监控

### 2. 防刷功能 (Anti-Brush)
- **智能检测**: 基于请求频率、路径变化、错误率等多维度检测异常行为
- **自动封禁**: 自动将恶意IP加入黑名单
- **可配置阈值**: 支持自定义防刷检测参数
- **实时响应**: 毫秒级响应异常请求

### 3. 签名验证 (Signature Verification)
- **国密算法**: 支持SM3国密哈希算法进行签名验证
- **防重放攻击**: 通过timestamp和nonce防止重放攻击
- **多应用支持**: 支持多个应用的签名密钥管理
- **灵活配置**: 可根据API路径动态配置是否需要签名

### 4. IP黑名单 (IP Blacklist)
- **自动封禁**: 防刷检测自动将恶意IP加入黑名单
- **手动管理**: 支持手动添加/移除黑名单IP
- **批量操作**: 支持批量添加/移除黑名单
- **过期机制**: 支持设置封禁时长，自动解封

### 5. API审计 (API Audit)
- **完整记录**: 记录所有API访问的详细信息
- **异步处理**: 异步记录审计日志，不影响请求性能
- **慢查询检测**: 自动识别响应时间超过阈值的慢查询
- **统计分析**: 提供访问统计、错误率分析等功能

## 技术架构

### 核心组件

```
bankshield-gateway/
├── entity/                 # 实体类
│   ├── ApiAccessLog.java   # API访问日志
│   ├── ApiRouteConfig.java # API路由配置
│   ├── BlacklistIp.java    # IP黑名单
│   └── RateLimitRule.java  # 限流规则
├── filter/                 # 过滤器
│   ├── AntiBrushFilter.java         # 防刷过滤器
│   ├── ApiAuditFilter.java          # API审计过滤器
│   ├── RateLimitFilter.java         # 限流过滤器
│   └── SignatureVerificationFilter.java # 签名验证过滤器
├── repository/             # 数据访问层
│   ├── ApiAccessLogRepository.java   # API访问日志Repository
│   ├── BlacklistRepository.java      # 黑名单Repository
│   └── RateLimitRepository.java      # 限流Repository
├── service/                # 业务逻辑层
│   ├── ApiAuditService.java   # API审计服务
│   ├── BlacklistService.java  # 黑名单服务
│   └── RateLimitService.java  # 限流服务
├── controller/             # 控制层
│   ├── ApiAuditController.java     # API审计控制器
│   ├── BlacklistController.java    # 黑名单控制器
│   ├── GatewayConfigController.java # 网关配置控制器
│   └── RateLimitController.java    # 限流控制器
└── config/                 # 配置类
    ├── GatewayConfig.java         # 网关配置
    ├── RedisConfig.java           # Redis配置
    └── ScheduledConfig.java       # 定时任务配置
```

### 过滤器执行顺序

1. **RateLimitFilter** (order: -100): 限流过滤器，最先执行
2. **AntiBrushFilter** (order: -90): 防刷过滤器，在限流之后
3. **SignatureVerificationFilter** (order: -80): 签名验证过滤器
4. **ApiAuditFilter** (order: 0): API审计过滤器，最后执行

## 快速开始

### 1. 环境要求

- Java 11+
- Spring Boot 2.7+
- Redis 6.0+
- MySQL 8.0+

### 2. 数据库初始化

执行数据库初始化脚本：

```sql
mysql -u root -p bankshield < sql/gateway_security.sql
```

### 3. 配置文件

编辑 `application.yml` 文件，配置数据库和Redis连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bankshield?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8
    username: your_username
    password: your_password
  
  redis:
    host: localhost
    port: 6379
    password: your_password
```

### 4. 启动服务

```bash
mvn spring-boot:run
```

## API接口

### 限流规则管理

- `GET /gateway/rate-limit/rules` - 获取限流规则列表
- `POST /gateway/rate-limit/rule` - 创建限流规则
- `PUT /gateway/rate-limit/rule/{id}` - 更新限流规则
- `DELETE /gateway/rate-limit/rule/{id}` - 删除限流规则

### IP黑名单管理

- `GET /gateway/blacklist/ips` - 获取IP黑名单列表
- `POST /gateway/blacklist/ip` - 添加IP到黑名单
- `PUT /gateway/blacklist/ip/{id}/unblock` - 解封IP
- `GET /gateway/blacklist/ip/check` - 检查IP是否在黑名单中

### API审计日志

- `GET /gateway/api/access/logs` - 获取API访问日志
- `GET /gateway/api/slow-queries` - 获取慢查询日志
- `GET /gateway/api/statistics` - 获取访问统计信息
- `GET /gateway/api/top-paths` - 获取TOP访问路径

## 使用示例

### 1. 创建限流规则

```json
POST /gateway/rate-limit/rule
{
  "ruleName": "登录接口限流",
  "limitDimension": "IP",
  "limitThreshold": 10,
  "limitWindow": 60,
  "enabled": true,
  "description": "每个IP每分钟最多10次登录请求"
}
```

### 2. 添加IP到黑名单

```json
POST /gateway/blacklist/ip
{
  "ipAddress": "192.168.1.100",
  "blockReason": "异常访问行为",
  "blockDuration": 3600,
  "operator": "admin"
}
```

### 3. 签名验证请求

客户端请求需要添加以下请求头：

```
X-App-Id: test-app
X-Timestamp: 1640995200000
X-Nonce: random-string
X-Signature: sm3-hash-signature
```

签名算法：
```
signStr = method + "\n" + path + "\n" + query + "\n" + timestamp + "\n" + nonce + "\n" + appId
signature = SM3(signStr + secretKey)
```

## 监控与统计

### 限流统计

- 总规则数、启用规则数、禁用规则数
- 按限流维度统计分布
- 限流命中次数统计

### 黑名单统计

- 总黑名单记录数
- 活跃封禁数、已解封数、已过期数
- 按状态分布统计

### API审计统计

- 总访问量、错误率、平均响应时间
- TOP访问路径排行
- 慢查询统计分析

## 性能优化

### 1. Redis优化

- 使用Redis Pipeline批量操作
- Lua脚本保证原子性操作
- 合理设置Key的过期时间

### 2. 异步处理

- API审计日志异步记录
- 批量操作优化

### 3. 缓存策略

- 限流规则缓存
- 黑名单缓存
- 应用密钥缓存

## 安全配置

### 1. 白名单配置

```yaml
bankshield:
  gateway:
    security:
      whitelist-ips:
        - 127.0.0.1
        - 192.168.1.0/24
```

### 2. 签名验证配置

```yaml
bankshield:
  gateway:
    filters:
      signature:
        enabled: true
        expire-time: 300000  # 5分钟
```

## 故障排查

### 1. 限流不生效

- 检查限流规则是否启用
- 检查Redis连接是否正常
- 检查过滤器是否启用

### 2. 签名验证失败

- 检查时间戳是否过期
- 检查签名算法是否正确
- 检查应用密钥是否有效

### 3. 黑名单不生效

- 检查IP是否在白名单中
- 检查Redis缓存是否更新
- 检查防刷检测参数

## 扩展开发

### 1. 自定义限流算法

实现 `RateLimitAlgorithm` 接口：

```java
@Component
public class CustomRateLimitAlgorithm implements RateLimitAlgorithm {
    @Override
    public boolean isAllowed(String key, int limit, int window) {
        // 自定义限流逻辑
        return true;
    }
}
```

### 2. 自定义签名算法

实现 `SignatureAlgorithm` 接口：

```java
@Component
public class CustomSignatureAlgorithm implements SignatureAlgorithm {
    @Override
    public String sign(String data, String secretKey) {
        // 自定义签名算法
        return "signature";
    }
}
```

## 测试

运行测试用例：

```bash
mvn test
```

查看测试报告：

```bash
mvn surefire-report:report
```

## 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

- 项目维护者：BankShield Team
- 邮箱：support@bankshield.com
- 技术支持：https://github.com/bankshield/gateway/issues

## 更新日志

### v1.0.0 (2024-01-01)
- 初始版本发布
- 实现限流、防刷、签名验证、IP黑名单、API审计功能
- 支持Redis集群部署
- 提供完整的前端管理界面
- 支持国密SM3签名算法