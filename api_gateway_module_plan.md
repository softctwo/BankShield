# API安全网关增强模块开发计划

## 功能概述
在bankshield-gateway模块中实现限流、防刷、签名验证、API审计等安全控制。

## 后端实现（bankshield-gateway）

### 核心过滤器
1. **RateLimitFilter**：基于Redis的分布式限流
   - 支持IP、用户、API、全局多维度限流
   - 返回429状态码

2. **AntiBrushFilter**：防刷检测
   - 识别CC攻击、暴力破解模式
   - 自动加入黑名单

3. **SignatureVerificationFilter**：国密SM2/SM3签名验证
   - 防止请求篡改和重放攻击
   - 支持时间戳验证（5分钟有效期）

4. **ApiAuditFilter**：API访问日志记录
   - 记录所有API调用到数据库
   - 包含请求参数、响应结果、执行时间

5. **IpBlacklistFilter**：IP黑名单检查
   - 自动阻止恶意IP访问

### 实体类
- ApiRouteConfig - API路由配置
- ApiAccessLog - API访问日志
- RateLimitRule - 限流规则
- BlacklistIp - 黑名单IP

### API接口
- GET /gateway/api/routes - 查询API路由配置
- POST /gateway/rate-limit/rule - 创建限流规则
- GET /gateway/blacklist/ips - 查询IP黑名单
- GET /gateway/api/access/logs - 查询API访问日志

## 前端页面
1. API路由配置（限流阈值、签名验证配置）
2. 限流规则管理（查看统计、动态调整）
3. IP黑名单管理（封禁/解封、封禁时长）
4. API审计日志（慢查询分析、调用统计）

## 配置示例
```yaml
rate-limit:
  enabled: true
  default-rate: 100  # 默认每秒100个请求

blacklist:
  enabled: true
  auto-block-threshold: 100  # 1分钟内100次请求自动封禁
```

## 技术要点
- Redis Lua脚本实现分布式限流
- 国密SM2/SM3签名算法
- 异步记录API日志
- 慢查询识别（响应时间>1秒）
