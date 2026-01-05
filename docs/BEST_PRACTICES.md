# BankShield 最佳实践指南

## 📋 目录

1. [代码规范](#代码规范)
2. [安全最佳实践](#安全最佳实践)
3. [性能优化](#性能优化)
4. [数据库设计](#数据库设计)
5. [API设计](#api设计)
6. [测试策略](#测试策略)
7. [部署实践](#部署实践)
8. [监控运维](#监控运维)

---

## 代码规范

### 1. Java代码规范

**命名规范**:
```java
// 类名：大驼峰命名
public class ComplianceService {}

// 方法名：小驼峰命名
public void checkCompliance() {}

// 常量：全大写，下划线分隔
public static final String DEFAULT_ALGORITHM = "SM4";

// 变量：小驼峰命名，见名知意
private String userName;
private int maxRetryCount;
```

**注释规范**:
```java
/**
 * 合规性检查服务
 * 
 * @author BankShield Team
 * @since 1.0.0
 */
public class ComplianceService {
    
    /**
     * 执行合规检查
     *
     * @param ruleId 规则ID
     * @param targetId 目标ID
     * @return 检查结果
     * @throws ComplianceException 检查异常
     */
    public ComplianceResult check(Long ruleId, Long targetId) {
        // 实现代码
    }
}
```

**异常处理**:
```java
// ✅ 推荐
try {
    // 业务逻辑
} catch (SpecificException e) {
    log.error("具体错误描述: {}", e.getMessage(), e);
    throw new BusinessException("业务友好的错误信息");
}

// ❌ 不推荐
try {
    // 业务逻辑
} catch (Exception e) {
    e.printStackTrace(); // 不要使用printStackTrace
}
```

### 2. 前端代码规范

**Vue组件规范**:
```vue
<script setup lang="ts">
// 1. 导入
import { ref, computed, onMounted } from 'vue'
import type { User } from '@/types/user'

// 2. Props定义
interface Props {
  userId: string
  readonly?: boolean
}
const props = withDefaults(defineProps<Props>(), {
  readonly: false
})

// 3. Emits定义
const emit = defineEmits<{
  (e: 'update', user: User): void
  (e: 'delete', id: string): void
}>()

// 4. 响应式数据
const loading = ref(false)
const user = ref<User | null>(null)

// 5. 计算属性
const fullName = computed(() => {
  return user.value ? `${user.value.firstName} ${user.value.lastName}` : ''
})

// 6. 方法
const loadUser = async () => {
  loading.value = true
  try {
    // 加载用户数据
  } finally {
    loading.value = false
  }
}

// 7. 生命周期
onMounted(() => {
  loadUser()
})
</script>

<template>
  <div class="user-detail">
    <!-- 模板内容 -->
  </div>
</template>

<style scoped lang="less">
.user-detail {
  // 样式
}
</style>
```

---

## 安全最佳实践

### 1. 数据加密

**敏感数据加密**:
```java
// 使用国密SM4加密敏感数据
public String encryptSensitiveData(String data) {
    String key = keyManagementService.getActiveKey();
    return Sm4Util.encrypt(data, key);
}

// 密钥定期轮换
@Scheduled(cron = "0 0 0 1 * ?") // 每月1号执行
public void rotateKeys() {
    keyManagementService.rotateKey();
}
```

**密码存储**:
```java
// ✅ 使用BCrypt存储密码
String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));

// ❌ 不要使用MD5或SHA1
String md5Password = DigestUtils.md5Hex(plainPassword); // 不安全
```

### 2. SQL注入防护

```java
// ✅ 使用参数化查询
@Select("SELECT * FROM users WHERE username = #{username}")
User findByUsername(@Param("username") String username);

// ❌ 不要拼接SQL
String sql = "SELECT * FROM users WHERE username = '" + username + "'"; // 危险
```

### 3. XSS防护

```java
// 输出时进行HTML转义
public String escapeHtml(String input) {
    return StringEscapeUtils.escapeHtml4(input);
}
```

```vue
<!-- Vue中使用v-text而不是v-html -->
<div v-text="userInput"></div>

<!-- 如果必须使用v-html，先进行清理 -->
<div v-html="sanitizeHtml(userInput)"></div>
```

### 4. CSRF防护

```java
// Spring Security CSRF配置
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf()
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
    }
}
```

### 5. 访问控制

```java
// 使用注解进行权限控制
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(Long userId) {
    // 只有管理员可以执行
}

// 数据级权限控制
@PreAuthorize("@dataPermissionService.canAccess(#userId)")
public User getUserData(Long userId) {
    // 检查用户是否有权限访问该数据
}
```

---

## 性能优化

### 1. 数据库查询优化

**使用索引**:
```sql
-- 为常用查询字段添加索引
CREATE INDEX idx_user_email ON sys_user(email);
CREATE INDEX idx_audit_time ON audit_log(operation_time);

-- 复合索引
CREATE INDEX idx_user_status_time ON sys_user(status, create_time);
```

**避免N+1查询**:
```java
// ❌ N+1查询
List<User> users = userMapper.selectAll();
for (User user : users) {
    List<Role> roles = roleMapper.selectByUserId(user.getId()); // N次查询
}

// ✅ 使用JOIN或批量查询
@Select("SELECT u.*, r.* FROM sys_user u LEFT JOIN sys_role r ON u.id = r.user_id")
List<UserWithRoles> selectUsersWithRoles();
```

**分页查询**:
```java
// 使用MyBatis-Plus分页
Page<User> page = new Page<>(pageNum, pageSize);
IPage<User> result = userMapper.selectPage(page, queryWrapper);
```

### 2. 缓存策略

**多级缓存**:
```java
// L1: 本地缓存（Caffeine）
@Cacheable(value = "users", key = "#id")
public User getUserById(Long id) {
    return userMapper.selectById(id);
}

// L2: Redis缓存
@Cacheable(value = "compliance:statistics", key = "#root.methodName")
public ComplianceStatistics getStatistics() {
    return calculateStatistics();
}
```

**缓存更新策略**:
```java
// 更新时清除缓存
@CacheEvict(value = "users", key = "#user.id")
public void updateUser(User user) {
    userMapper.updateById(user);
}

// 更新时刷新缓存
@CachePut(value = "users", key = "#result.id")
public User createUser(User user) {
    userMapper.insert(user);
    return user;
}
```

### 3. 异步处理

```java
// 异步执行耗时操作
@Async
public CompletableFuture<ComplianceReport> generateReport(Long taskId) {
    ComplianceReport report = doGenerateReport(taskId);
    return CompletableFuture.completedFuture(report);
}

// 批量异步处理
public void batchProcess(List<Long> ids) {
    List<CompletableFuture<Void>> futures = ids.stream()
        .map(id -> CompletableFuture.runAsync(() -> process(id)))
        .collect(Collectors.toList());
    
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
}
```

### 4. 连接池优化

```yaml
spring:
  datasource:
    druid:
      initial-size: 10
      min-idle: 10
      max-active: 100
      max-wait: 60000
      # 连接有效性检查
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      validation-query: SELECT 1
      # 连接泄漏检测
      remove-abandoned: true
      remove-abandoned-timeout: 180
```

---

## 数据库设计

### 1. 表设计规范

**基础字段**:
```sql
CREATE TABLE base_table (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(64) COMMENT '创建人',
    update_by VARCHAR(64) COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0:未删除,1:已删除)',
    version INT NOT NULL DEFAULT 0 COMMENT '版本号(乐观锁)',
    remark VARCHAR(500) COMMENT '备注'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基础表';
```

**索引设计**:
```sql
-- 单列索引
CREATE INDEX idx_column_name ON table_name(column_name);

-- 复合索引（注意顺序）
CREATE INDEX idx_status_time ON table_name(status, create_time);

-- 唯一索引
CREATE UNIQUE INDEX uk_email ON sys_user(email);

-- 全文索引
CREATE FULLTEXT INDEX ft_content ON article(title, content);
```

### 2. 分表分库策略

**水平分表**:
```java
// 按用户ID分表
public String getTableName(Long userId) {
    int tableIndex = (int) (userId % 10);
    return "user_data_" + tableIndex;
}

// 按时间分表
public String getTableName(LocalDate date) {
    return "audit_log_" + date.format(DateTimeFormatter.ofPattern("yyyyMM"));
}
```

### 3. 数据归档

```sql
-- 创建归档表
CREATE TABLE audit_log_archive LIKE audit_log;

-- 归档历史数据
INSERT INTO audit_log_archive 
SELECT * FROM audit_log 
WHERE create_time < DATE_SUB(NOW(), INTERVAL 1 YEAR);

-- 删除已归档数据
DELETE FROM audit_log 
WHERE create_time < DATE_SUB(NOW(), INTERVAL 1 YEAR);
```

---

## API设计

### 1. RESTful API规范

**URL设计**:
```
GET    /api/users          # 获取用户列表
GET    /api/users/{id}     # 获取单个用户
POST   /api/users          # 创建用户
PUT    /api/users/{id}     # 更新用户
DELETE /api/users/{id}     # 删除用户
```

**统一响应格式**:
```java
public class Result<T> {
    private Integer code;      // 状态码
    private String message;    // 消息
    private T data;            // 数据
    private Long timestamp;    // 时间戳
    
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }
    
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }
}
```

### 2. API版本控制

```java
// URL版本控制
@RequestMapping("/api/v1/users")
public class UserControllerV1 {}

@RequestMapping("/api/v2/users")
public class UserControllerV2 {}

// 请求头版本控制
@RequestMapping(value = "/api/users", headers = "API-Version=1")
public class UserControllerV1 {}
```

### 3. API限流

```java
@RateLimit(permitsPerSecond = 100)
@GetMapping("/api/users")
public Result<List<User>> getUsers() {
    // 每秒最多100个请求
}
```

### 4. API文档

```java
@Api(tags = "用户管理")
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @ApiOperation("获取用户列表")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "page", value = "页码", required = true),
        @ApiImplicitParam(name = "size", value = "每页大小", required = true)
    })
    @GetMapping
    public Result<Page<User>> getUsers(
        @RequestParam int page,
        @RequestParam int size
    ) {
        // 实现
    }
}
```

---

## 测试策略

### 1. 单元测试

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserMapper userMapper;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    @Test
    void testGetUserById() {
        // Given
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("test");
        when(userMapper.selectById(1L)).thenReturn(mockUser);
        
        // When
        User result = userService.getUserById(1L);
        
        // Then
        assertNotNull(result);
        assertEquals("test", result.getUsername());
        verify(userMapper, times(1)).selectById(1L);
    }
}
```

### 2. 集成测试

```java
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testGetUser() throws Exception {
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.username").value("test"));
    }
}
```

### 3. 性能测试

```java
@Test
void performanceTest() {
    long startTime = System.currentTimeMillis();
    
    for (int i = 0; i < 10000; i++) {
        userService.getUserById(1L);
    }
    
    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;
    
    assertTrue(duration < 1000, "10000次查询应在1秒内完成");
}
```

---

## 部署实践

### 1. 容器化部署

**Dockerfile**:
```dockerfile
FROM openjdk:11-jre-slim

WORKDIR /app

COPY target/bankshield-api.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Xms2g", "-Xmx4g", "-jar", "app.jar"]
```

**Docker Compose**:
```yaml
version: '3.8'

services:
  app:
    image: bankshield/api:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - MYSQL_HOST=mysql
      - REDIS_HOST=redis
    depends_on:
      - mysql
      - redis
  
  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=password
      - MYSQL_DATABASE=bankshield
    volumes:
      - mysql_data:/var/lib/mysql
  
  redis:
    image: redis:6.0
    volumes:
      - redis_data:/data

volumes:
  mysql_data:
  redis_data:
```

### 2. Kubernetes部署

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: bankshield-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: bankshield-api
  template:
    metadata:
      labels:
        app: bankshield-api
    spec:
      containers:
      - name: api
        image: bankshield/api:latest
        ports:
        - containerPort: 8080
        resources:
          requests:
            memory: "2Gi"
            cpu: "1000m"
          limits:
            memory: "4Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /api/health/live
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /api/health/ready
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
```

### 3. 蓝绿部署

```bash
#!/bin/bash
# 蓝绿部署脚本

# 部署新版本到绿色环境
kubectl apply -f deployment-green.yaml

# 等待绿色环境就绪
kubectl wait --for=condition=available deployment/bankshield-api-green

# 切换流量到绿色环境
kubectl patch service bankshield-api -p '{"spec":{"selector":{"version":"green"}}}'

# 验证绿色环境
curl http://bankshield-api/api/health

# 如果验证通过，删除蓝色环境
kubectl delete deployment bankshield-api-blue
```

---

## 监控运维

### 1. 日志规范

```java
// 使用SLF4J + Logback
@Slf4j
public class UserService {
    
    public User getUserById(Long id) {
        log.info("查询用户: id={}", id);
        
        try {
            User user = userMapper.selectById(id);
            log.debug("查询结果: {}", user);
            return user;
        } catch (Exception e) {
            log.error("查询用户失败: id={}", id, e);
            throw new BusinessException("查询用户失败");
        }
    }
}
```

### 2. 指标监控

```java
// 使用Micrometer记录指标
@Service
public class UserService {
    
    private final Counter userQueryCounter;
    private final Timer userQueryTimer;
    
    public UserService(MeterRegistry registry) {
        this.userQueryCounter = Counter.builder("user.query.count")
            .description("用户查询次数")
            .register(registry);
        
        this.userQueryTimer = Timer.builder("user.query.time")
            .description("用户查询耗时")
            .register(registry);
    }
    
    public User getUserById(Long id) {
        userQueryCounter.increment();
        return userQueryTimer.record(() -> {
            return userMapper.selectById(id);
        });
    }
}
```

### 3. 告警配置

```yaml
# Prometheus告警规则
groups:
  - name: bankshield
    rules:
      - alert: HighErrorRate
        expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.05
        for: 5m
        annotations:
          summary: "错误率过高"
          description: "错误率超过5%"
      
      - alert: HighResponseTime
        expr: http_request_duration_seconds{quantile="0.95"} > 1
        for: 5m
        annotations:
          summary: "响应时间过长"
          description: "P95响应时间超过1秒"
```

---

## 附录

### A. 常用工具

- **代码质量**: SonarQube, Checkstyle, PMD
- **性能分析**: JProfiler, VisualVM, Arthas
- **压力测试**: JMeter, Gatling, Locust
- **监控**: Prometheus, Grafana, ELK Stack
- **APM**: Skywalking, Pinpoint, Zipkin

### B. 参考资料

- [阿里巴巴Java开发手册](https://github.com/alibaba/p3c)
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Spring Boot Best Practices](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Vue.js Style Guide](https://vuejs.org/style-guide/)

---

**文档版本**: v1.0  
**最后更新**: 2025-01-04  
**维护者**: BankShield开发团队
