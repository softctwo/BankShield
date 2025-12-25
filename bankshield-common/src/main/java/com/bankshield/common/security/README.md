# BankShield 安全加固模块

## 概述

BankShield安全加固模块是一个综合性的安全框架，为银行数据安全管理系统提供多层次的安全防护。该模块包含Web安全防护、API安全强化、安全配置加固、安全审计、合规性检查和应急响应等功能。

## 功能特性

### 🔒 Web安全防护
- **WAF过滤器**: 防御SQL注入、XSS攻击、命令注入、路径遍历等常见Web攻击
- **安全响应头**: 自动添加安全HTTP头，防止点击劫持、MIME类型嗅探等攻击
- **Cookie安全**: 配置安全的Cookie属性，防止CSRF攻击
- **请求验证**: 对请求参数、请求体、请求头进行安全验证

### 🛡️ API安全强化
- **高级限流**: 基于Redis的分布式令牌桶限流算法，支持多种限流策略
- **API签名验证**: 防止API请求被篡改和重放攻击
- **访问控制**: 细粒度的API访问权限控制
- **异常处理**: 统一的异常处理和错误信息保护

### ⚙️ 安全配置强化
- **安全基线检查**: 自动检查系统安全配置，确保符合安全基线要求
- **密码策略**: 强制密码复杂度要求，支持密码过期和历史密码检查
- **会话安全**: 会话超时管理、会话固定攻击防护
- **加密配置**: 强制使用安全的加密算法和协议

### 📊 安全审计
- **安全事件日志**: 完整的安全事件记录和追踪
- **实时监控**: 实时安全事件监控和告警
- **审计报表**: 自动生成安全审计报告
- **行为分析**: 用户行为分析和异常检测

### ✅ 合规性检查
- **等保合规**: 符合等级保护2.0三级要求
- **PCI DSS**: 支持PCI DSS合规性检查
- **ISO 27001**: 符合ISO 27001信息安全管理标准
- **自动检查**: 定期自动执行合规性检查

### 🚨 应急响应
- **自动响应**: 根据安全事件严重程度自动执行响应动作
- **IP阻断**: 自动阻断恶意IP地址
- **账户管理**: 自动禁用风险账户
- **系统隔离**: 紧急情况下自动隔离系统

## 核心组件

### 1. WafFilter (Web应用防火墙过滤器)
```java
@Component
@Order(1)
public class WafFilter extends OncePerRequestFilter
```

**功能**:
- SQL注入检测和防护
- XSS攻击检测和防护
- 命令注入检测和防护
- 路径遍历检测和防护
- 异常请求头检测

**配置**:
```yaml
security:
  waf:
    enabled: true
    sql-injection-mode: strict
    xss-detection-mode: strict
    max-user-agent-length: 500
```

### 2. SecureHeadersFilter (安全响应头过滤器)
```java
@Component
@Order(2)
public class SecureHeadersFilter extends OncePerRequestFilter
```

**功能**:
- 添加X-Content-Type-Options: nosniff
- 添加X-XSS-Protection: 1; mode=block
- 添加X-Frame-Options: DENY
- 添加Strict-Transport-Security
- 配置Content-Security-Policy
- 移除敏感响应头

### 3. AdvancedRateLimiter (高级限流器)
```java
@Component
public class AdvancedRateLimiter
```

**功能**:
- 基于Redis的分布式限流
- 令牌桶算法实现
- 支持多种限流策略
- Lua脚本原子化操作
- 实时监控和统计

**配置**:
```yaml
security:
  rate-limit:
    enabled: true
    default-rules:
      - path: "/api/user/login"
        rate: 10
        capacity: 50
        period: 1s
        type: "login"
```

### 4. ApiSignatureVerifier (API签名验证器)
```java
@Component
public class ApiSignatureVerifier
```

**功能**:
- API请求签名验证
- 防止请求篡改
- 防止重放攻击
- 时间戳验证
- Nonce验证

### 5. SecurityEventLogger (安全事件日志记录器)
```java
@Component
public class SecurityEventLogger
```

**功能**:
- 安全事件异步记录
- 事件级别分类
- 实时告警机制
- 统计信息收集
- 审计日志生成

### 6. SecurityBaselineChecker (安全基线检查器)
```java
@Component
public class SecurityBaselineChecker
```

**功能**:
- 自动安全基线检查
- 多维度安全检查
- 合规性评估
- 修复建议生成
- 检查报告生成

### 7. SecurityIncidentResponder (安全事件响应器)
```java
@Component
public class SecurityIncidentResponder
```

**功能**:
- 自动事件响应
- 分级响应策略
- IP自动阻断
- 账户自动管理
- 系统隔离机制

## 配置说明

### 基本配置
```yaml
# application.yml
security:
  # 启用安全加固模块
  enabled: true
  
  # WAF配置
  waf:
    enabled: true
    sql-injection-mode: strict
    xss-detection-mode: strict
    
  # 限流配置
  rate-limit:
    enabled: true
    redis:
      key-prefix: "bankshield:rate_limit:"
      
  # 签名验证配置
  signature:
    enabled: true
    expire-time: 300000  # 5分钟
    
  # 事件响应配置
  incident:
    enabled: true
    auto-response: true
    auto-block: true
```

### 高级配置
```yaml
# security-hardening.yml
bankshield:
  security:
    # 密码策略
    password-policy:
      min-length: 8
      require-uppercase: true
      require-lowercase: true
      require-digit: true
      require-special: true
      
    # 会话配置
    session:
      timeout: 30m
      max-concurrent-sessions: 1
      session-fixation-protection: true
      
    # 数据保护
    data-protection:
      encryption:
        enabled: true
        algorithm: "SM4"
        key-rotation-days: 90
        
    # 审计配置
    audit:
      enabled: true
      retention-days: 365
      storage: "database"
      
    # 合规性检查
    compliance:
      enabled: true
      level-2.0-grade-3: true
      pci-dss: true
      iso-27001: true
```

## API接口

### 安全基线检查
```http
GET /api/security/baseline/check
```

### 获取安全统计
```http
GET /api/security/stats?date=20231224
```

### 限流状态查询
```http
GET /api/security/rate-limit/status?userId=123&path=/api/user/login
```

### 重置限流
```http
POST /api/security/rate-limit/reset?userId=123&path=/api/user/login
```

### 被阻断IP列表
```http
GET /api/security/blocked-ips?date=20231224
```

### 解除IP阻断
```http
POST /api/security/unblock-ip?ip=192.168.1.100
```

### 创建安全事件
```http
POST /api/security/incidents
Content-Type: application/json

{
  "title": "暴力破解攻击",
  "description": "检测到多次登录失败尝试",
  "type": "BRUTE_FORCE",
  "severity": "HIGH",
  "userId": "admin",
  "sourceIp": "192.168.1.100"
}
```

### 获取安全事件列表
```http
GET /api/security/incidents?severity=HIGH&status=RESPONDING&date=20231224
```

### 更新事件状态
```http
PUT /api/security/incidents/{id}/status?status=RESOLVED
```

## 使用示例

### 1. 启用安全加固
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Autowired
    private SecurityHardeningConfig securityHardeningConfig;
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.apply(securityHardeningConfig);
    }
}
```

### 2. 自定义限流规则
```java
@Component
public class CustomRateLimitConfig {
    
    @Autowired
    private AdvancedRateLimiter rateLimiter;
    
    @PostConstruct
    public void init() {
        // 添加自定义限流规则
        rateLimiter.addRule("/api/sensitive/*", 5, 25, Duration.ofSeconds(1), "sensitive");
    }
}
```

### 3. 记录安全事件
```java
@Service
public class UserService {
    
    @Autowired
    private SecurityEventLogger eventLogger;
    
    public void login(String username, String password, String ip) {
        boolean success = authenticate(username, password);
        
        eventLogger.logLoginEvent(username, success, ip, 
            success ? null : "Invalid credentials");
        
        if (!success) {
            // 检查是否需要触发安全事件
            checkBruteForceAttack(username, ip);
        }
    }
}
```

### 4. 处理安全事件
```java
@Component
public class SecurityEventHandler {
    
    @Autowired
    private SecurityIncidentResponder incidentResponder;
    
    public void handleBruteForceAttack(String username, String ip) {
        SecurityIncident incident = new SecurityIncident();
        incident.setTitle("暴力破解攻击");
        incident.setDescription("检测到多次登录失败尝试");
        incident.setType("BRUTE_FORCE");
        incident.setSeverity(Severity.HIGH);
        incident.setUserId(username);
        incident.setSourceIp(ip);
        
        incidentResponder.handleIncident(incident);
    }
}
```

## 安全最佳实践

### 1. 配置建议
- 在生产环境中启用所有安全功能
- 定期更新安全规则和策略
- 配置合适的限流阈值
- 启用详细的审计日志
- 定期执行安全基线检查

### 2. 监控建议
- 监控安全事件趋势
- 设置合理的告警阈值
- 定期分析安全日志
- 关注异常访问模式
- 及时响应安全告警

### 3. 运维建议
- 定期备份安全配置
- 测试应急响应流程
- 更新安全补丁
- 培训安全意识
- 制定安全策略

## 合规性支持

### 等级保护2.0（三级）
- ✅ 访问控制
- ✅ 安全审计
- ✅ 通信完整性
- ✅ 通信保密性
- ✅ 数据完整性
- ✅ 数据保密性
- ✅ 备份恢复
- ✅ 剩余信息保护
- ✅ 个人信息保护

### PCI DSS
- ✅ 安全网络和系统
- ✅ 持卡人数据保护
- ✅ 漏洞管理
- ✅ 访问控制
- ✅ 网络监控
- ✅ 信息安全政策

### ISO 27001
- ✅ 信息安全政策
- ✅ 组织信息安全
- ✅ 人力资源安全
- ✅ 资产管理
- ✅ 访问控制
- ✅ 密码学
- ✅ 物理和环境安全
- ✅ 操作安全
- ✅ 通信安全
- ✅ 系统获取、开发和维护
- ✅ 供应商关系
- ✅ 信息安全事件管理
- ✅ 业务连续性管理
- ✅ 合规性

## 性能优化

### 1. WAF性能优化
- 使用高效的正则表达式
- 启用请求缓存机制
- 配置合理的检测超时
- 使用异步处理模式

### 2. 限流性能优化
- 使用Redis集群
- 优化Lua脚本
- 配置合理的TTL
- 使用批量操作

### 3. 审计性能优化
- 使用异步日志记录
- 配置日志级别
- 定期清理过期日志
- 使用批量写入

## 故障排除

### 常见问题

1. **WAF误报率高**
   - 调整检测模式为normal或loose
   - 配置白名单规则
   - 更新检测规则

2. **限流影响正常用户**
   - 调整限流阈值
   - 配置用户白名单
   - 使用更精细的限流策略

3. **签名验证失败**
   - 检查时钟同步
   - 验证密钥配置
   - 检查签名算法

4. **性能问题**
   - 启用缓存机制
   - 优化数据库查询
   - 使用异步处理

## 更新日志

### v1.0.0 (2024-12-24)
- ✨ 初始版本发布
- ✨ 实现WAF过滤器
- ✨ 实现安全响应头过滤器
- ✨ 实现高级限流器
- ✨ 实现API签名验证器
- ✨ 实现安全事件日志记录器
- ✨ 实现安全基线检查器
- ✨ 实现安全事件响应器
- ✨ 添加完整的配置支持
- ✨ 添加RESTful API接口
- ✨ 添加详细的测试用例
- ✨ 添加完整的文档

## 支持与维护

### 技术支持
- 文档地址: https://docs.bankshield.com
- 问题反馈: https://github.com/your-org/BankShield/issues
- 技术支持: support@bankshield.com

### 更新维护
- 定期更新安全规则
- 持续优化性能
- 及时修复安全漏洞
- 不断完善功能

## 许可证

Apache License 2.0

## 贡献指南

欢迎提交Issue和Pull Request来完善项目！请参考项目根目录的CONTRIBUTING.md文件。

---

**注意**: 本安全加固模块是BankShield数据安全管理系统的核心组件，请确保正确配置和使用，以保障系统安全。