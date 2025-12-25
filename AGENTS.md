# BankShield 项目开发指南

## 项目概览

BankShield 是一个专业的银行数据安全管理平台，采用微服务架构，提供数据加密、访问控制、审计追踪、敏感数据识别与脱敏等核心功能，确保银行数据全生命周期的安全性。

### 核心功能模块
- 🔐 **数据加密管理** - 支持国密SM2/SM3/SM4算法和国际标准加密算法
- 👥 **细粒度访问控制** - 基于RBAC的权限管理体系，支持角色互斥
- 📊 **实时审计追踪** - 全链路操作日志记录与分析
- 🎯 **敏感数据识别** - 自动发现和分类敏感数据
- 🎭 **智能数据脱敏** - 动态和静态数据脱敏
- 📈 **安全态势可视化** - 实时安全监控与告警
- 📋 **合规性检查** - 符合金融行业标准规范
- 🔍 **数据血缘追踪** - 全链路数据流向追踪
- 💧 **数字水印** - 文档溯源和版权保护
- 🛡️ **安全扫描** - 自动化漏洞检测

## 技术栈

### 后端技术栈
- **框架**: Spring Boot 2.7.18 + Spring Cloud 2021.0.8
- **微服务**: Spring Cloud Alibaba 2021.0.5.0
- **安全**: Spring Security + JWT Token认证
- **数据库**: MyBatis-Plus 3.5.3.2 + MySQL 8.0
- **连接池**: Druid 1.2.20
- **缓存**: Redis + Redisson 3.17.7（分布式限流）
- **国密算法**: Bouncy Castle 1.70（SM2/SM3/SM4）
- **工具库**: Lombok 1.18.30 + Hutool 5.8.28
- **JSON处理**: FastJSON2 2.0.43
- **Excel处理**: EasyExcel 3.3.4
- **定时任务**: Quartz
- **WebSocket**: Spring WebSocket（实时告警）
- **国密SSL**: TLCP-SSL 1.0.0
- **保险库**: Spring Vault 2.3.2（密钥管理）

### 前端技术栈
- **框架**: Vue 3.5.26 + TypeScript 5.3.3
- **UI组件**: Element Plus 2.13.0
- **图标**: @element-plus/icons-vue 2.3.2
- **状态管理**: Pinia 2.3.1
- **路由**: Vue Router 4.6.4
- **HTTP客户端**: Axios 1.13.2
- **图表**: ECharts 5.6.0
- **日期处理**: Dayjs 1.11.19
- **Cookie**: JS Cookie 3.0.5
- **进度条**: NProgress 0.2.0
- **构建工具**: Vite 5.0.10
- **代码规范**: ESLint + Prettier + TypeScript严格模式

### 基础设施
- **容器化**: Docker + Docker Compose
- **编排**: Kubernetes + Helm Charts
- **服务网格**: Spring Cloud Gateway（API网关）
- **服务发现**: Netflix Eureka
- **配置中心**: Spring Cloud Config
- **负载均衡**: Spring Cloud LoadBalancer
- **熔断降级**: Sentinel
- **监控**: Prometheus + Grafana + Alertmanager
- **CI/CD**: GitHub Actions + Jenkins + ArgoCD（GitOps）
- **代码质量**: SonarQube + JaCoCo（覆盖率>80%）
- **安全扫描**: OWASP Dependency Check + Trivy（CVE评分>7失败）

## 项目结构

```
BankShield/
├── bankshield-parent/              # 父POM，统一管理依赖版本
├── bankshield-common/              # 公共模块（工具类、公共实体、异常处理）
├── bankshield-api/                 # 核心业务服务（主应用）
├── bankshield-auth/                # 认证授权服务
├── bankshield-gateway/             # API网关（路由、限流、鉴权）
├── bankshield-encrypt/             # 加密组件（国密算法封装）
├── bankshield-monitor/             # 监控服务集成
├── bankshield-ai/                  # AI智能识别模块
├── bankshield-lineage/             # 数据血缘模块
├── bankshield-blockchain/          # 区块链存证模块
├── bankshield-mpc/                 # 多方计算模块
├── bankshield-ui/                  # 前端Vue应用
├── bankshield-demo/                # 演示模块
├── sql/                            # 数据库初始化脚本
├── scripts/                        # 自动化脚本（启动、部署、健康检查）
├── docker/                         # Docker配置和Docker Compose文件
├── k8s/                            # Kubernetes部署配置（dev/preview/prod）
├── helm/                           # Helm Charts配置
├── argocd/                         # ArgoCD GitOps配置
├── monitoring/                     # 监控告警配置
├── docs/                           # 项目文档
├── tests/                          # 测试脚本（性能测试、E2E测试）
└── reports/                        # 测试报告和扫描结果
```

## 构建与测试命令

### 后端构建

```bash
# 完整构建（跳过测试）
mvn clean install -DskipTests

# 仅构建API模块
cd bankshield-api
mvn clean package -DskipTests

# 构建并执行单元测试
mvn clean test

# 构建并执行集成测试
mvn clean verify -Dspring.profiles.active=test

# 构建生产环境JAR（包含所有依赖）
mvn clean package -Pprod

# 代码覆盖率检查（要求>80%）
mvn jacoco:prepare-agent test jacoco:report

# SonarQube代码质量分析
mvn sonar:sonar \
  -Dsonar.projectKey=bankshield \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=YOUR_TOKEN

# 依赖安全漏洞扫描（CVE评分>7失败）
mvn dependency-check:check
```

### 前端构建

```bash
# 安装依赖
cd bankshield-ui
npm install

# 开发服务器（带热重载）
npm run dev

# 生产构建
npm run build

# 仅TypeScript类型检查
cd bankshield-ui
npm run type-check

# 代码格式化和Lint
npm run format
npm run lint

# 单元测试
npm run test

# E2E测试
npm run test:e2e
```

### 完整项目启动

```bash
# 方式一：使用启动脚本（推荐）
./scripts/start.sh --dev      # 启动开发环境
./scripts/start.sh --prod     # 启动生产环境
./scripts/start.sh --build    # 仅构建项目
./scripts/start.sh --stop     # 停止所有服务
./scripts/start.sh --help     # 查看帮助

# 方式二：手动启动
mysql -u root -p < sql/init_database.sql    # 初始化数据库
cd bankshield-api && mvn spring-boot:run    # 启动后端
cd bankshield-ui && npm run dev             # 启动前端
```

### Docker部署

```bash
# 构建镜像
docker build -t bankshield/api:latest ./bankshield-api
docker build -t bankshield/ui:latest ./bankshield-ui

# 使用Docker Compose启动
cd docker
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f api
docker-compose logs -f ui
```

### Kubernetes部署

```bash
# 部署到开发环境
kubectl apply -f k8s/dev/

# 部署到预览环境（PR）
kubectl apply -f k8s/preview/

# 部署到生产环境（谨慎操作）
kubectl apply -f k8s/prod/

# 查看部署状态
kubectl get pods -n bankshield-dev
kubectl get pods -n bankshield-prod

# 使用Helm部署
helm install bankshield ./helm/bankshield -n bankshield-prod
```

## 代码组织规范

### 后端代码结构（bankshield-api）

```
src/main/java/com/bankshield/api/
├── annotation/           # 自定义注解（@RoleExclusive、@MaskData）
├── aspect/               # AOP切面（数据脱敏、角色检查）
├── config/               # 配置类（Redis、Vault、异步线程池）
├── controller/           # REST API控制器（按业务模块组织）
├── service/              # 服务层接口
│   └── impl/             # 服务层实现
├── service/lineage/      # 数据血缘专项服务
├── mapper/               # MyBatis Mapper接口
├── entity/               # 实体类（对应数据库表）
├── dto/                  # 数据传输对象
├── enums/                # 枚举类（状态、类型、级别）
├── job/                  # 定时任务（Quartz）
├── websocket/            # WebSocket处理器
├── filter/               # 过滤器（限流、安全）
├── interceptor/          # 拦截器（审计日志）
├── component/            # 通用组件
└── BankShieldApiApplication.java  # 启动类

src/main/resources/
├── application.yml       # 主配置文件
├── application-dev.yml   # 开发环境配置
├── application-test.yml  # 测试环境配置
├── application-prod.yml  # 生产环境配置
├── mapper/               # MyBatis XML映射文件
└── static/               # 静态资源
```

### 前端代码结构（bankshield-ui）

```
src/
├── api/                  # API接口封装（按模块组织）
├── assets/               # 静态资源（图片、图标）
├── components/           # Vue组件（通用组件）
├── router/               # 路由配置
├── store/                # Pinia状态管理（按模块）
├── types/                # TypeScript类型定义
├── utils/                # 工具函数（请求封装、加密、验证）
├── views/                # 页面组件（按功能模块）
├── hooks/                # Vue Composition API钩子
├── directives/           # Vue自定义指令
├── plugins/              # Vue插件
├── styles/               # 全局样式
└── App.vue               # 根组件
```

### 数据库命名规范

```sql
-- 表命名
sys_user                    -- 系统表加sys_前缀
monitor_metric              -- 业务表使用模块名前缀
rel_user_role               -- 关联表使用rel_前缀

-- 字段命名
id                          -- 主键统一为id
create_time                 -- 时间字段加_time后缀
alert_status                -- 状态字段加_status后缀
rule_id                     -- 外键加_id后缀

-- 索引命名
idx_username                -- 普通索引
uk_email                    -- 唯一索引
```

## 开发规范

### 1. 后端开发规范

#### 代码格式
- 遵循阿里巴巴Java开发手册
- 使用Lombok简化代码（@Data、@Slf4j）
- Service层接口定义在`service`包，实现在`service.impl`包
- Controller统一返回`Result<T>`对象
- 使用`@Slf4j`注解记录日志

#### API设计
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        try {
            User user = userService.getById(id);
            return Result.success(user);
        } catch (Exception e) {
            log.error("查询用户失败: {}", e.getMessage(), e);
            return Result.error("查询用户失败");
        }
    }
    
    @PostMapping
    public Result<Void> createUser(@RequestBody @Valid UserDTO userDTO) {
        userService.createUser(userDTO);
        return Result.success();
    }
}
```

#### 异常处理
- 自定义异常继承`RuntimeException`
- 全局异常处理器统一处理
- 敏感信息不返回给前端

#### 国密算法使用
```java
// SM2非对称加密
SM2KeyPair keyPair = Sm2Util.generateKeyPair();
String encrypted = Sm2Util.encrypt(data, keyPair.getPublicKey());
String decrypted = Sm2Util.decrypt(encrypted, keyPair.getPrivateKey());

// SM3杂凑算法
String hash = Sm3Util.hash(data);
boolean isValid = Sm3Util.verify(data, hash);

// SM4对称加密
String key = Sm4Util.generateKey();
String encrypted = Sm4Util.encrypt(data, key);
String decrypted = Sm4Util.decrypt(encrypted, key);
```

### 2. 前端开发规范

#### TypeScript严格模式
- 所有变量必须声明类型
- 接口和类型定义放在`@/types`目录
- 使用`interface`定义对象结构
- 使用`enum`定义枚举值

#### Vue组件规范
```vue
<script setup lang="ts">
import { ref, computed } from 'vue'
import type { User } from '@/types/user'

// 定义Props
interface Props {
  userId: string
}

const props = defineProps<Props>()

// 定义Emits
const emit = defineEmits<{
  (e: 'update', user: User): void
}>()

// 响应式数据
const loading = ref(false)
const user = ref<User | null>(null)

// 计算属性
const fullName = computed(() => {
  return user.value ? `${user.value.firstName} ${user.value.lastName}` : ''
})
</script>

<template>
  <div class="user-detail">
    <el-card v-loading="loading">
      <h2>{{ fullName }}</h2>
    </el-card>
  </div>
</template>

<style scoped lang="less">
.user-detail {
  padding: 20px;
}
</style>
```

#### API接口规范
```typescript
// api/user.ts
import request from '@/utils/request'
import type { User, UserForm } from '@/types/user'

export const getUser = (id: string): Promise<User> => {
  return request.get(`/users/${id}`)
}

export const createUser = (data: UserForm): Promise<void> => {
  return request.post('/users', data)
}

export const updateUser = (id: string, data: UserForm): Promise<void> => {
  return request.put(`/users/${id}`, data)
}

export const deleteUser = (id: string): Promise<void> => {
  return request.delete(`/users/${id}`)
}
```

#### 状态管理（Pinia）
```typescript
// store/user.ts
import { defineStore } from 'pinia'
import type { User } from '@/types/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    users: [] as User[],
    loading: false
  }),
  
  getters: {
    activeUsers: (state) => state.users.filter(u => u.status === 'active')
  },
  
  actions: {
    async fetchUsers() {
      this.loading = true
      try {
        this.users = await api.getUsers()
      } finally {
        this.loading = false
      }
    }
  }
})
```

### 3. 数据库设计规范

#### MyBatis-Plus使用
```java
// Mapper接口
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT * FROM sys_user WHERE dept_id = #{deptId}")
    List<User> selectByDeptId(@Param("deptId") Long deptId);
}

// Service实现
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    public List<User> getUsersByDept(Long deptId) {
        return baseMapper.selectByDeptId(deptId);
    }
}
```

#### 索引优化原则
- 查询条件字段必须建立索引
- 外键字段必须建立索引
- 排序字段建议建立索引
- 组合索引遵循最左前缀原则
- 定期分析和优化索引

## 测试策略

### 1. 单元测试

#### 测试框架配置
```java
// JUnit 5 + Mockito
@SpringBootTest
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserMapper userMapper;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    @Test
    void testGetUserById() {
        // Arrange
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("test");
        when(userMapper.selectById(1L)).thenReturn(mockUser);
        
        // Act
        User result = userService.getById(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals("test", result.getUsername());
        verify(userMapper, times(1)).selectById(1L);
    }
}
```

#### 覆盖率要求
- 核心业务逻辑覆盖率必须达到100%
- Service层覆盖率不低于90%
- Controller层覆盖率不低于80%
- 整体覆盖率不低于80%（JaCoCo强制执行）

### 2. 集成测试

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class UserControllerIntegrationTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testCreateUser() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("integration");
        userDTO.setName("集成测试");
        
        ResponseEntity<Result> response = restTemplate.postForEntity(
            "/api/users", userDTO, Result.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getCode());
    }
}
```

### 3. 端到端测试（E2E）

#### Cypress测试
```javascript
// cypress/integration/login.spec.js
describe('登录功能测试', () => {
  it('成功登录', () => {
    cy.visit('/login')
    cy.get('[data-test="username"]').type('admin')
    cy.get('[data-test="password"]').type('123456')
    cy.get('[data-test="login-button"]').click()
    cy.url().should('include', '/dashboard')
    cy.get('[data-test="user-menu"]').should('contain', 'admin')
  })
  
  it('登录失败', () => {
    cy.visit('/login')
    cy.get('[data-test="username"]').type('admin')
    cy.get('[data-test="password"]').type('wrong')
    cy.get('[data-test="login-button"]').click()
    cy.get('[data-test="error-message"]').should('be.visible')
  })
})
```

### 4. 性能测试

#### k6测试脚本
```javascript
// tests/k6/bankshield-load-test.js
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '2m', target: 100 },
    { duration: '5m', target: 100 },
    { duration: '2m', target: 200 },
    { duration: '5m', target: 200 },
    { duration: '2m', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<200'],
    http_req_failed: ['rate<0.01'],
  },
};

const BASE_URL = 'http://localhost:8080/api';
const USERNAME = 'admin';
const PASSWORD = '123456';

export default function () {
  const loginRes = http.post(`${BASE_URL}/auth/login`, {
    username: USERNAME,
    password: PASSWORD,
  });
  
  check(loginRes, { '登录成功': (r) => r.status === 200 });
  
  const authHeaders = {
    headers: {
      Authorization: `Bearer ${loginRes.json('token')}`,
    },
  };
  
  const myObjects = http.get(`${BASE_URL}/users`, authHeaders);
  check(myObjects, { '获取用户列表': (r) => r.status === 200 });
  
  sleep(1);
}
```

### 5. API测试（RestAssured）

```java
class AlertRuleApiTest {
    
    @Test
    void testCreateAlertRule() {
        String jsonBody = "{"
            + "\"ruleName\":\"CPU告警\","
            + "\"metricType\":\"CPU_USAGE\","
            + "\"condition\":\">\","
            + "\"threshold\":80,"
            + "\"alertLevel\":\"WARNING\","
            + "\"enabled\":true"
            + "}";
        
        given()
            .auth().oauth2(token)
            .contentType(ContentType.JSON)
            .body(jsonBody)
        .when()
            .post("/api/alert-rules")
        .then()
            .statusCode(200)
            .body("code", equalTo(200))
            .body("data.ruleName", equalTo("CPU告警"));
    }
}
```

### 6. 测试执行命令汇总

```bash
# 单元测试
mvn test -Dtest=**/*UnitTest.java

# 集成测试
mvn verify -Dspring.profiles.active=test

# 所有测试
mvn clean verify

# 前端单元测试
cd bankshield-ui && npm run test:unit

# E2E测试
cd bankshield-ui && npm run test:e2e

# 性能测试
cd tests/k6 && k6 run bankshield-load-test.js

# 生成测试报告
mvn allure:serve    # 查看Allure测试报告
```

## 安全规范

### 1. 认证授权

#### JWT认证流程
```java
// JWT过滤器
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain chain) 
            throws ServletException, IOException {
        
        String token = getTokenFromRequest(request);
        
        if (StringUtils.hasText(token) && jwtTokenUtil.validateToken(token)) {
            String username = jwtTokenUtil.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        chain.doFilter(request, response);
    }
}
```

#### 角色互斥注解
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RoleExclusive {
    String value();  // 互斥角色组标识
}

// 使用示例
@RoleExclusive("risk-management")
@DeleteMapping("/high-risk-data")
public Result<Void> deleteHighRiskData(@PathVariable Long id) {
    // 该方法不允许风险管理组的其他角色同时访问
}
```

### 2. 数据加密

#### 国密算法使用规范
```java
// SM2公钥加密（用于密钥交换）
@Service
public class SecureKeyManagementService {
    
    public String encryptWithSm2(String data, String publicKey) {
        return Sm2Util.encrypt(data, publicKey);
    }
    
    public String decryptWithSm2(String encryptedData, String privateKey) {
        return Sm2Util.decrypt(encryptedData, privateKey);
    }
}

// SM4对称加密（用于数据加密）
@Service
public class DataEncryptionService {
    
    public String encryptWithSm4(String data, String key) {
        return Sm4Util.encrypt(data, key);
    }
    
    public String decryptWithSm4(String encryptedData, String key) {
        return Sm4Util.decrypt(encryptedData, key);
    }
}

// SM3杂凑算法（用于完整性校验）
@Service
public class IntegrityVerificationService {
    
    public String hashWithSm3(String data) {
        return Sm3Util.hash(data);
    }
    
    public boolean verifyIntegrity(String data, String hash) {
        return Sm3Util.verify(data, hash);
    }
}
```

#### 字段级加密
```java
@Entity
@Table(name = "sys_user")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username")
    private String username;
    
    @Column(name = "phone")
    @SensitiveField(type = SensitiveType.PHONE)  // 敏感字段注解
    private String phone;  // 自动加密存储
    
    @Column(name = "id_card")
    @SensitiveField(type = SensitiveType.ID_CARD)
    private String idCard;  // 自动加密存储
}
```

### 3. 数据脱敏

#### 注解式脱敏
```java
@Data
public class UserDTO {
    
    private Long id;
    
    private String username;
    
    @MaskData(type = MaskType.PHONE)  // 手机号脱敏 138****8888
    private String phone;
    
    @MaskData(type = MaskType.ID_CARD)  // 身份证脱敏 1101**********1234
    private String idCard;
    
    @MaskData(type = MaskType.BANK_CARD)  // 银行卡脱敏 6222**********5678
    private String bankCard;
}
```

#### 动态脱敏
```java
@Service
public class DataMaskingEngine {
    
    public <T> T maskData(T data, UserRole viewerRole) {
        // 根据查看者角色决定脱敏级别
        MaskingLevel level = determineMaskingLevel(viewerRole);
        return applyMasking(data, level);
    }
}
```

### 4. 输入验证

#### 全局验证配置
```java
@Configuration
public class ValidationConfig {
    
    @Bean
    public Validator validator() {
        ValidatorFactory factory = Validation.byProvider(HibernateValidator.class)
            .configure()
            .failFast(true)  // 快速失败模式
            .buildValidatorFactory();
        return factory.getValidator();
    }
}
```

#### 请求参数验证
```java
@Data
public class UserCreateRequest {
    
    @NotBlank(message = "用户名不能为空")
    @Length(min = 3, max = 50, message = "用户名长度必须在3-50之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Length(min = 8, message = "密码长度至少8位")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$", 
             message = "密码必须包含数字、大小写字母")
    private String password;
    
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @Email(message = "邮箱格式不正确")
    private String email;
}
```

### 5. API安全

#### 速率限制
```java
@Component
public class RateLimiterFilter implements GlobalFilter {
    
    private final RedissonClient redissonClient;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientId = getClientId(exchange);
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(clientId);
        
        // 每分钟最多100次请求
        rateLimiter.trySetRate(RateType.OVERALL, 100, 1, RateIntervalUnit.MINUTES);
        
        if (rateLimiter.tryAcquire()) {
            return chain.filter(exchange);
        } else {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }
    }
}
```

#### 安全响应头
```java
@Configuration
public class SecurityHeaderConfig implements WebMvcConfigurer {
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public void postHandle(HttpServletRequest request, HttpServletResponse response, 
                                 Object handler, ModelAndView modelAndView) {
                response.addHeader("X-Content-Type-Options", "nosniff");
                response.addHeader("X-XSS-Protection", "1; mode=block");
                response.addHeader("X-Frame-Options", "DENY");
                response.addHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
                response.addHeader("Content-Security-Policy", "default-src 'self'");
            }
        });
    }
}
```

### 6. 密钥管理（Vault集成）

#### Vault配置
```yaml
# application.yml
spring:
  cloud:
    vault:
      enabled: true
      host: localhost
      port: 8200
      scheme: http
      authentication: TOKEN
      token: YOUR_VAULT_TOKEN
  datasource:
    password: ${vault.secret.database.password}
  redis:
    password: ${vault.secret.redis.password}
```

#### 密钥轮换
```java
@Service
public class KeyRotationService {
    
    @Scheduled(cron = "0 0 1 * * ?")  // 每天凌晨1点执行
    public void rotateEncryptionKeys() {
        List<SecurityKey> keys = securityKeyMapper.selectNeedRotationKeys();
        
        for (SecurityKey key : keys) {
            try {
                String newKey = generateNewKey(key.getAlgorithm());
                key.setKeyValue(newKey);
                key.setLastRotationTime(LocalDateTime.now());
                securityKeyMapper.updateById(key);
                
                log.info("密钥轮换成功: keyId={}", key.getId());
            } catch (Exception e) {
                log.error("密钥轮换失败: keyId={}", key.getId(), e);
            }
        }
    }
}
```

## CI/CD流程

### 1. GitHub Actions工作流

#### 主要Pipeline
```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  code-quality:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 8
        uses: actions/setup-java@v3
        with:
          java-version: '8'
          distribution: 'temurin'
      - name: Code Quality Check
        run: |
          mvn clean compile
          mvn sonar:sonar \
            -Dsonar.projectKey=bankshield \
            -Dsonar.host.url=${{ secrets.SONAR_URL }} \
            -Dsonar.login=${{ secrets.SONAR_TOKEN }}
  
  test:
    runs-on: ubuntu-latest
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: ${{ secrets.DB_PASSWORD }}
        ports:
          - 3306:3306
      redis:
        image: redis:6.0
        ports:
          - 6379:6379
    steps:
      - uses: actions/checkout@v3
      - name: Run Tests
        run: mvn clean verify
      - name: Upload Coverage
        uses: codecov/codecov-action@v3
        with:
          files: ./target/site/jacoco/jacoco.xml
  
  build-and-push:
    needs: [code-quality, test]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Build Docker Images
        run: |
          docker build -t bankshield/api:${{ github.sha }} ./bankshield-api
          docker build -t bankshield/ui:${{ github.sha }} ./bankshield-ui
      - name: Security Scan
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: 'bankshield/api:${{ github.sha }}'
          format: 'sarif'
      - name: Push to Registry
        run: |
          echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
          docker push bankshield/api:${{ github.sha }}
          docker push bankshield/ui:${{ github.sha }}
```

### 2. Jenkins Pipeline

#### Jenkinsfile示例
```groovy
pipeline {
    agent any
    
    tools {
        maven 'Maven-3.8'
        jdk 'JDK-1.8'
        nodejs 'Node-16'
    }
    
    environment {
        REGISTRY = 'harbor.bankshield.com'
        DOCKER_CREDENTIALS = 'docker-hub-credentials'
        KUBE_CONFIG = 'kube-config-prod'
    }
    
    stages {
        stage('检出') {
            steps {
                checkout scm
            }
        }
        
        stage('代码质量检查') {
            parallel {
                stage('SonarQube分析') {
                    steps {
                        withSonarQubeEnv('SonarQube') {
                            sh 'mvn clean verify sonar:sonar'
                        }
                    }
                }
                stage('依赖检查') {
                    steps {
                        sh 'mvn dependency-check:check'
                    }
                }
            }
        }
        
        stage('测试') {
            parallel {
                stage('单元测试') {
                    steps {
                        sh 'mvn clean test'
                    }
                    post {
                        always {
                            junit 'target/surefire-reports/*.xml'
                            publishHTML([
                                allowMissing: false,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'target/site/jacoco',
                                reportFiles: 'index.html',
                                reportName: 'Coverage Report'
                            ])
                        }
                    }
                }
                stage('E2E测试') {
                    steps {
                        dir('bankshield-ui') {
                            sh 'npm install'
                            sh 'npm run test:e2e'
                        }
                    }
                }
            }
        }
        
        stage('构建镜像') {
            steps {
                script {
                    def imageTag = "${REGISTRY}/bankshield/api:${BUILD_NUMBER}"
                    
                    docker.withRegistry("https://${REGISTRY}", DOCKER_CREDENTIALS) {
                        def apiImage = docker.build("${imageTag}", "./bankshield-api")
                        apiImage.push()
                    }
                }
            }
        }
        
        stage('部署到生产') {
            when {
                branch 'main'
            }
            steps {
                script {
                    withKubeConfig([credentialsId: KUBE_CONFIG]) {
                        sh """
                            kubectl set image deployment/bankshield-api \
                              bankshield-api=${REGISTRY}/bankshield/api:${BUILD_NUMBER} \
                              -n bankshield-prod
                            kubectl rollout status deployment/bankshield-api -n bankshield-prod
                        """
                    }
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            slackSend(
                channel: '#deployments',
                color: 'good',
                message: "✅ BankShield部署成功！版本: ${BUILD_NUMBER}"
            )
        }
        failure {
            slackSend(
                channel: '#deployments',
                color: 'danger',
                message: "❌ BankShield部署失败！版本: ${BUILD_NUMBER}"
            )
        }
    }
}
```

### 3. ArgoCD GitOps部署

#### Application定义
```yaml
# argocd/bankshield-prod.yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: bankshield-prod
  namespace: argocd
spec:
  project: default
  source:
    repoURL: https://github.com/bankshield/bankshield.git
    targetRevision: main
    path: k8s/prod
  destination:
    server: https://kubernetes.default.svc
    namespace: bankshield-prod
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
    syncOptions:
      - CreateNamespace=true
  revisionHistoryLimit: 10
```

## 部署流程

### 1. 环境配置

#### 开发环境（Dev）
```bash
# 后端配置
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/bankshield?useSSL=false
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=dev_password
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379

# 前端配置
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_ENV=development
```

#### 测试环境（Test）
```bash
# 后端配置
SERVER_PORT=8081
SPRING_PROFILES_ACTIVE=test
SPRING_DATASOURCE_URL=jdbc:mysql://test-db:3306/bankshield
SPRING_DATASOURCE_USERNAME=test_user
SPRING_DATASOURCE_PASSWORD=test_password_encrypted

# 前端配置
VITE_API_BASE_URL=https://test.bankshield.com/api
VITE_APP_ENV=test
```

#### 生产环境（Prod）
```bash
# 后端配置
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:mysql://prod-db:3306/bankshield?useSSL=true&verifyServerCertificate=true
SPRING_DATASOURCE_USERNAME=prod_user
SPRING_DATASOURCE_PASSWORD=${VAULT_SECRET}
SPRING_REDIS_PASSWORD=${VAULT_REDIS_SECRET}

# 安全加固
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=https://auth.bankshield.com
SPRING_VAULT_URI=https://vault.bankshield.com
SPRING_VAULT_TOKEN=${VAULT_TOKEN}

# 前端配置
VITE_API_BASE_URL=https://api.bankshield.com
VITE_APP_ENV=production
```

### 2. 部署脚本

#### 一键部署脚本
```bash
#!/bin/bash
# scripts/deploy.sh

set -e

ENVIRONMENT=$1
VERSION=$2

if [ -z "$ENVIRONMENT" ] || [ -z "$VERSION" ]; then
    echo "Usage: ./deploy.sh <environment> <version>"
    echo "Example: ./deploy.sh prod v1.0.0"
    exit 1
fi

# 设置Kubernetes上下文
kubectl config use-context bankshield-${ENVIRONMENT}

# 更新镜像版本
helm upgrade bankshield ./helm/bankshield \
  --namespace bankshield-${ENVIRONMENT} \
  --set api.image.tag=${VERSION} \
  --set ui.image.tag=${VERSION} \
  --wait \
  --timeout 10m

# 健康检查
./scripts/health-check.sh https://api.bankshield-${ENVIRONMENT}.com

# 发送通知
echo "✅ BankShield ${VERSION} 部署到 ${ENVIRONMENT} 环境成功！"
```

#### 健康检查脚本
```bash
#!/bin/bash
# scripts/health-check.sh

API_URL=$1
MAX_RETRIES=30
RETRY_INTERVAL=10

echo "开始健康检查: ${API_URL}"

for i in $(seq 1 $MAX_RETRIES); do
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" ${API_URL}/actuator/health)
    
    if [ "$STATUS" == "200" ]; then
        echo "✅ 服务健康检查通过！"
        exit 0
    fi
    
    echo "⏳ 第 $i/$MAX_RETRIES 次检查失败，状态码: $STATUS"
    sleep $RETRY_INTERVAL
done

echo "❌ 健康检查失败，部署回滚..."
exit 1
```

### 3. 数据库迁移

#### Flyway迁移脚本
```sql
-- sql/V1__init_database.sql
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username(username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- sql/V2__encrypt_config.sql
CREATE TABLE encrypt_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    table_name VARCHAR(50) NOT NULL,
    column_name VARCHAR(50) NOT NULL,
    algorithm VARCHAR(20) NOT NULL COMMENT 'SM4/AES',
    key_id BIGINT NOT NULL,
    status TINYINT DEFAULT 1,
    UNIQUE KEY uk_table_column(table_name, column_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加密配置表';
```

### 4. 监控告警

#### Prometheus指标
```yaml
# monitoring/prometheus/config.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "alert_rules.yml"

scrape_configs:
  - job_name: 'bankshield-api'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['bankshield-api:8080']
    scrape_interval: 10s

  - job_name: 'bankshield-ui'
    static_configs:
      - targets: ['bankshield-ui:80']
    scrape_interval: 30s

  - job_name: 'mysql'
    static_configs:
      - targets: ['mysql-exporter:9104']
    scrape_interval: 30s

  - job_name: 'redis'
    static_configs:
      - targets: ['redis-exporter:9121']
    scrape_interval: 30s
```

#### 告警规则
```yaml
# monitoring/prometheus/alert_rules.yml
groups:
  - name: bankshield-alerts
    rules:
      - alert: HighCPUUsage
        expr: avg(rate(process_cpu_seconds_total{job="bankshield-api"}[5m])) * 100 > 80
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "API服务CPU使用率高"
          description: "CPU使用率: {{ $value }}%"
      
      - alert: HighMemoryUsage
        expr: (process_resident_memory_bytes{job="bankshield-api"} / 1024 / 1024) > 1024
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "API服务内存使用高"
          description: "内存使用: {{ $value }}MB"
      
      - alert: TooManyFailedLogins
        expr: rate(bankshield_failed_login_total[5m]) > 10
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "登录失败次数过多"
          description: "可能正在遭受暴力破解攻击"
      
      - alert: VaultSeal
        expr: vault_seal_status == 1
        for: 0m
        labels:
          severity: critical
        annotations:
          summary: "Vault已被密封"
          description: "密钥管理服务不可用"
```

## 故障排查

### 1. 常见问题

#### 数据库连接失败
```bash
# 检查MySQL服务状态
systemctl status mysql

# 检查连接配置
cat bankshield-api/src/main/resources/application-dev.yml

# 测试连接
mysql -h localhost -P 3306 -u root -p

# 查看JDBC连接池状态
# 访问: http://localhost:8080/api/druid/login.html
# 账号: admin
# 密码: 123456
```

#### Redis连接失败
```bash
# 检查Redis服务
redis-cli ping  # 应该返回 PONG

# 检查配置
redis-cli config get requirepass

# 清空缓存（谨慎操作）
redis-cli FLUSHALL
```

#### 前端构建失败
```bash
# 清除npm缓存
cd bankshield-ui
rm -rf node_modules package-lock.json
npm cache clean --force

# 重新安装依赖
npm install

# 检查TypeScript编译错误
npm run type-check
```

#### 服务启动后访问404
```bash
# 检查后端是否正确启动
curl http://localhost:8080/api/actuator/health

# 检查前端代理配置
cat bankshield-ui/vite.config.ts

# 检查Nginx配置（生产环境）
cat bankshield-ui/nginx.conf
```

### 2. 日志分析

#### 后端日志
```bash
# 实时查看日志
tail -f logs/api.log

# 按级别过滤
grep "ERROR" logs/api.log

# 查看特定时间段的日志
sed -n '/2024-01-01 10:00:00/,/2024-01-01 11:00:00/p' logs/api.log

# 查看慢SQL日志
# Druid监控: http://localhost:8080/api/druid/sql.html
```

#### 前端日志
```bash
# 浏览器开发者工具控制台
# 查看网络请求: Network tab
# 查看控制台日志: Console tab
# 查看应用状态: Vue DevTools

# 生产环境前端日志
docker logs bankshield-ui-container
```

#### Docker容器日志
```bash
# 查看所有容器
docker ps

# 查看容器日志
docker logs -f bankshield-api
docker logs -f bankshield-ui

# 查看容器资源使用
docker stats

# 进入容器调试
docker exec -it bankshield-api /bin/bash
```

#### Kubernetes日志
```bash
# 查看Pod列表
kubectl get pods -n bankshield-prod

# 查看Pod日志
kubectl logs -f deployment/bankshield-api -n bankshield-prod --tail=100

# 查看具体容器日志
kubectl logs bankshield-api-xxxx -c bankshield-api -n bankshield-prod

# 查看Service状态
kubectl get svc -n bankshield-prod

# 查看Ingress状态
kubectl get ingress -n bankshield-prod
```

### 3. 性能调优

#### JVM调优
```bash
# 启动参数示例
java -jar \
  -Xms2g -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=100 \
  -XX:+UseStringDeduplication \
  -Dspring.profiles.active=prod \
  bankshield-api.jar
```

#### 数据库调优
```sql
-- 查看慢查询
SHOW VARIABLES LIKE 'slow_query_log';
SHOW VARIABLES LIKE 'long_query_time';

-- 查看连接数
SHOW STATUS LIKE 'Threads_connected';
SHOW STATUS LIKE 'Max_used_connections';

-- 查看InnoDB状态
SHOW ENGINE INNODB STATUS;

-- 优化表
OPTIMIZE TABLE sys_user;
ANALYZE TABLE sys_user;
```

#### Redis调优
```bash
# 查看内存使用
redis-cli INFO memory

# 查看连接数
redis-cli INFO clients

# 查看慢查询
redis-cli SLOWLOG GET 10

# 配置最大内存
redis-cli CONFIG SET maxmemory 4gb
redis-cli CONFIG SET maxmemory-policy allkeys-lru
```

### 4. 监控告警

#### 服务健康检查
```bash
# Actuator端点
curl http://localhost:8080/api/actuator/health
curl http://localhost:8080/api/actuator/info
curl http://localhost:8080/api/actuator/metrics

# 自定义健康检查
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public Health health() {
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(1)) {
                return Health.up().withDetail("database", "UP").build();
            }
        } catch (Exception e) {
            return Health.down(e).withDetail("database", "DOWN").build();
        }
        return Health.down().withDetail("database", "DOWN").build();
    }
}
```

#### Prometheus查询示例
```promql
# CPU使用率
100 - (avg by (instance) (rate(node_cpu_seconds_total{mode="idle", job="bankshield-api"}[5m])) * 100)

# 内存使用率
(node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / node_memory_MemTotal_bytes * 100

# HTTP请求速率
sum(rate(http_requests_total{job="bankshield-api"}[5m])) by (uri)

# 错误率
sum(rate(http_requests_total{job="bankshield-api", status=~"5.."}[5m])) / 
sum(rate(http_requests_total{job="bankshield-api"}[5m])) * 100

# JVM堆内存使用量
jvm_memory_used_bytes{area="heap", job="bankshield-api"}
```

### 5. 备份恢复

#### 数据库备份
```bash
# MySQL全量备份
mysqldump -u root -p bankshield > bankshield_$(date +%Y%m%d_%H%M%S).sql

# MySQL增量备份（基于binlog）
mysqlbinlog --database=bankshield /var/log/mysql/mysql-bin.000001 > binlog_001.sql

# 压缩备份
mysqldump -u root -p bankshield | gzip > bankshield_$(date +%Y%m%d).sql.gz

# 自动备份脚本
# scripts/backup-database.sh
#!/bin/bash
BACKUP_DIR="/backup/bankshield"
DATE=$(date +%Y%m%d_%H%M%S)
mysqldump -u root -p'password' bankshield > ${BACKUP_DIR}/bankshield_${DATE}.sql
gzip ${BACKUP_DIR}/bankshield_${DATE}.sql
find ${BACKUP_DIR} -name "bankshield_*.sql.gz" -mtime +7 -delete
```

#### 密钥备份
```bash
# Vault密钥备份
vault operator unseal
vault read -field=keys secret/bankshield/encryption-keys > keys-backup.json

# 加密密钥备份
openssl enc -aes-256-cbc -salt -in keys-backup.json -out keys-backup.enc -k "BACKUP_PASSWORD"
```

## 环境配置快速参考

### 开发环境（Development）
```bash
# 启动所有服务（推荐）
./scripts/start.sh --dev

# 或者分步启动：
mysql -u root -p dev_password < sql/init_database.sql
cd bankshield-api && mvn spring-boot:run
cd bankshield-ui && npm run dev

# 访问地址
前端: http://localhost:3000
后端: http://localhost:8080/api
Druid监控: http://localhost:8080/api/druid/login.html（admin/123456）

# 默认账号
admin/123456    # 超级管理员
security/123456 # 安全管理员
audit/123456    # 审计管理员
user/123456     # 普通用户
```

### 测试环境（Testing）
```bash
# 部署到测试环境
kubectl apply -f k8s/test/

# 或使用Helm
helm install bankshield-test ./helm/bankshield \
  --namespace bankshield-test \
  --set api.image.tag=latest \
  --set ui.image.tag=latest

# 访问地址
https://test.bankshield.com

# 运行测试
mvn clean verify -Dspring.profiles.active=test
cd bankshield-ui && npm run test:e2e
```

### 生产环境（Production）
```bash
# 部署到生产环境（需要审批）
./scripts/deploy.sh prod v1.0.0

# 访问地址
https://bankshield.com

# 监控告警
Grafana: https://grafana.bankshield.com
Prometheus: https://prometheus.bankshield.com
Alertmanager: https://alertmanager.bankshield.com
Vault: https://vault.bankshield.com

# 日志查看
kubectl logs -f deployment/bankshield-api -n bankshield-prod --tail=100
```

## 技术支持

### 文档索引
- [项目架构设计](docs/architecture.md)
- [API文档](docs/api-spec.md)
- [数据库设计](docs/fsd.md)
- [安全规范](docs/security-hardening.md)
- [监控告警配置](monitoring/README.md)
- [数据血缘](DATA_LINEAGE_MODULE_SUMMARY.md)
- [合规报告](COMPLIANCE_REPORT_MODULE_SUMMARY.md)

### 联系方式
- **技术支持**: tech-support@bankshield.com
- **安全团队**: security@bankshield.com
- **运维团队**: ops@bankshield.com
- **值班电话**: +86-400-123-4567

### 问题反馈
```bash
# GitHub Issues
git@github.com:bankshield/bankshield/issues

# Slack Channel
#bankshield-tech-support

# 钉钉群
BankShield技术交流群（二维码见README.md）
```

## 更新日志

### 2024-01-24
- ✅ 添加数据血缘模块开发规范
- ✅ 完善国密算法使用指南
- ✅ 补充常见问题解答
- ✅ 集成Vault密钥管理
- ✅ 添加性能测试k6脚本示例

### 2024-01-20
- ✅ 创建初始开发指南
- ✅ 定义代码规范
- ✅ 说明技术栈选择
- ✅ 添加CI/CD流程
- ✅ 配置多环境部署

---

**注意**: 本文档会根据项目发展持续更新，请定期查看最新版本。如有问题，请参考项目文档或联系技术支持。
