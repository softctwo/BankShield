# BankShield 项目全面测试计划

> 版本: 1.0.0
> 生成日期: 2025-12-25
> 项目: 银行数据安全管理系统的全面测试策略

---

## 目录

1. [项目概述](#项目概述)
2. [当前状态评估](#当前状态评估)
3. [测试策略框架](#测试策略框架)
4. [阶段一：前端独立测试（无后端依赖）](#阶段一前端独立测试无后端依赖)
5. [阶段二：后端单元测试](#阶段二后端单元测试)
6. [阶段三：集成测试（后端服务运行）](#阶段三集成测试后端服务运行)
7. [阶段四：端到端测试](#阶段四端到端测试)
8. [阶段五：性能测试](#阶段五性能测试)
9. [测试覆盖率分析](#测试覆盖率分析)
10. [测试环境配置](#测试环境配置)
11. [CI/CD 集成](#cicd-集成)
12. [测试时间线和里程碑](#测试时间线和里程碑)

---

## 项目概述

### 系统架构

**后端模块 (Java/Spring Boot 2.7.18)**
- `bankshield-common` - 通用工具和基础设施
- `bankshield-encrypt` - 加密服务和密钥管理
- `bankshield-api` - RESTful API 和业务逻辑
- `bankshield-ai` - AI 智能分析模块
- `bankshield-lineage` - 数据血缘追踪

**前端 (Vue 3 + Vite)**
- UI 框架: Element Plus 2.13.0
- 状态管理: Pinia 2.3.1
- 路由: Vue Router 4.6.4
- 构建工具: Vite 5.0.10
- 端口: 3000 (开发环境)

### 功能模块概览

| 模块类别 | 功能模块 | 路由路径 | 后端 API |
|---------|---------|---------|----------|
| **系统管理** | 用户管理 | `/system/user` | `/api/user` |
| | 角色管理 | `/system/role` | `/api/role` |
| | 部门管理 | `/system/dept` | `/api/dept` |
| | 菜单管理 | `/system/menu` | `/api/menu` |
| **三权分立** | 角色管理 | `/separation/role-management` | - |
| | 权限审计 | `/separation/permission-audit` | - |
| | 操作日志 | `/separation/operation-log` | - |
| **分类分级** | 资产管理 | `/classification/asset-management` | `/api/classification/asset` |
| | 资产地图 | `/classification/asset-map` | - |
| | 资产审核 | `/classification/review` | - |
| | 数据源管理 | `/classification/data-source` | - |
| | 血缘分析 | `/classification/lineage` | - |
| | 脱敏规则 | `/classification/masking-rule` | - |
| **加密管理** | 密钥管理 | `/encrypt/key` | `/api/encrypt/key` |
| | 使用统计 | `/encrypt/key/usage` | - |
| **审计管理** | 审计大屏 | `/audit/dashboard` | - |
| | 操作审计 | `/audit/operation` | `/api/audit/operation` |
| | 登录审计 | `/audit/login` | `/api/audit/login` |
| **监控管理** | 监控大屏 | `/monitor/dashboard` | - |
| | 告警规则 | `/monitor/alert-rule` | - |
| | 告警记录 | `/monitor/alert-record` | - |
| | 通知配置 | `/monitor/notification-config` | - |
| | 系统监控 | `/monitor/metrics` | - |
| **AI 分析** | 智能分析 | `/ai/*` | `/api/ai/*` |

---

## 当前状态评估

### 已存在的测试资产

#### 后端测试

| 测试类型 | 位置 | 状态 | 说明 |
|---------|------|------|------|
| 单元测试基类 | `bankshield-common/src/test/.../BaseUnitTest.java` | ✅ | 提供 MockMvc 和通用断言方法 |
| 集成测试基类 | `bankshield-common/src/test/.../BaseIntegrationTest.java` | ✅ | Spring Boot 测试环境配置 |
| 性能测试基类 | `bankshield-common/src/test/.../BasePerformanceTest.java` | ✅ | 性能基准测试框架 |
| 加密算法测试 | `bankshield-common/src/test/.../crypto/*` | ✅ | SM2/SM3/SM4 国密算法测试 |
| 用户控制器测试 | `bankshield-api/src/test/.../UserControllerIntegrationTest.java` | ✅ | 用户 API 集成测试 |
| 密钥管理测试 | `bankshield-encrypt/src/test/.../*Test.java` | ✅ | 密钥生成、存储、管理测试 |
| 安全测试 | `bankshield-common/src/test/.../security/*` | ✅ | WAF 和安全加固测试 |

#### 前端测试

| 测试类型 | 位置 | 状态 | 说明 |
|---------|------|------|------|
| E2E 测试配置 | `bankshield-ui/cypress.config.js` | ✅ | Cypress 配置完成 |
| 自定义命令 | `bankshield-ui/cypress/support/commands.js` | ✅ | 丰富的自定义命令集 |
| 用户管理 E2E | `bankshield-ui/cypress/e2e/user-management.cy.js` | ✅ | 完整的用户生命周期测试 |
| 单元测试配置 | `bankshield-ui/package.json` (vitest) | ✅ | Vitest 已配置 |

### 当前问题

#### 后端问题

1. **编译错误**: 部分模块存在编译错误，需要修复后才能运行测试
2. **服务未启动**: 后端服务当前未运行，无法进行集成测试
3. **测试覆盖率不均**: 部分核心模块缺少单元测试

#### 前端问题

1. **API 依赖**: 页面内容依赖后端 API，当前显示空白
2. **E2E 测试受限**: 现有 E2E 测试需要后端服务支持
3. **组件测试缺失**: 缺少 Vue 组件单元测试

---

## 测试策略框架

### 测试金字塔

```
           /\
          /  \        E2E 测试 (Cypress)
         /----\       占比: 10%
        /      \
       /        \     集成测试
      /----------\    (JUnit + MockMvc)
     /            \   占比: 30%
    /              \
   /----------------\ 单元测试
  /                  \(JUnit 5 + Vitest)
 /                    \占比: 60%
/______________________\
```

### 测试分层策略

| 测试层次 | 覆盖范围 | 执行频率 | 维护成本 | 反馈速度 |
|---------|---------|---------|---------|---------|
| 单元测试 | 函数、类、组件 | 每次提交 | 低 | 秒级 |
| 集成测试 | API、服务交互 | 每次提交 | 中 | 分钟级 |
| E2E 测试 | 完整业务流程 | 每日/预发布 | 高 | 十分钟级 |
| 性能测试 | 系统性能指标 | 每周/发布前 | 中 | 小时级 |

---

## 阶段一：前端独立测试（无后端依赖）

### 目标

验证前端代码质量、路由配置和组件渲染，无需后端服务。

### 1.1 静态代码分析

#### 代码规范检查

```bash
# 前端代码规范检查
cd /Users/zhangyanlong/workspaces/BankShield/bankshield-ui

# ESLint 检查
npm run lint

# Prettier 格式化检查
npm run format
```

**验证点:**
- [ ] ESLint 无错误
- [ ] TypeScript 类型检查通过
- [ ] 代码格式符合规范

#### TypeScript 类型检查

```bash
# TypeScript 类型检查
npx vue-tsc --noEmit
```

**验证点:**
- [ ] 无类型错误
- [ ] 接口定义完整
- [ ] 类型推断正确

### 1.2 路由配置测试

#### 路由结构验证

**测试文件**: `bankshield-ui/tests/unit/router.spec.ts`

```typescript
import { describe, it, expect } from 'vitest'
import router from '@/router/index'

describe('路由配置测试', () => {
  it('主路由应包含所有顶级路由', () => {
    const routes = router.getRoutes()
    const topLevelRoutes = ['/login', '/layout', '/classification', '/audit', '/monitor', '/encrypt', '/ai']

    topLevelRoutes.forEach(path => {
      const route = routes.find(r => r.path === path)
      expect(route).toBeDefined()
    })
  })

  it('系统管理路由应正确配置', () => {
    const routes = router.getRoutes()
    const systemRoutes = ['/system/role', '/system/dept', '/system/menu', '/user']

    systemRoutes.forEach(path => {
      const route = routes.find(r => r.path === path)
      expect(route).toBeDefined()
      expect(route?.meta?.roles).toContain('admin')
    })
  })

  it('三权分立路由应正确配置', () => {
    const routes = router.getRoutes()
    const separationRoutes = ['/separation/role-management', '/separation/permission-audit', '/separation/operation-log']

    separationRoutes.forEach(path => {
      const route = routes.find(r => r.path === path)
      expect(route).toBeDefined()
    })
  })

  it('审计管理路由应正确配置', () => {
    const routes = router.getRoutes()
    const auditRoutes = ['/audit/dashboard', '/audit/operation', '/audit/login']

    auditRoutes.forEach(path => {
      const route = routes.find(r => r.path === path)
      expect(route).toBeDefined()
      expect(route?.meta?.roles).toContain('audit-admin')
    })
  })
})
```

**执行命令:**

```bash
npm run test -- tests/unit/router.spec.ts
```

### 1.3 组件渲染测试

#### 组件可加载性测试

**测试文件**: `bankshield-ui/tests/unit/components.spec.ts`

```typescript
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'

describe('组件加载测试', () => {
  it('登录组件应可加载', async () => {
    const Login = (await import('@/views/login/index.vue')).default
    expect(Login).toBeDefined()
  })

  it('布局组件应可加载', async () => {
    const Layout = (await import('@/views/layout/index.vue')).default
    expect(Layout).toBeDefined()
  })

  it('用户管理组件应可加载', async () => {
    const UserManagement = (await import('@/views/system/user/index.vue')).default
    expect(UserManagement).toBeDefined()
  })

  it('角色管理组件应可加载', async () => {
    const RoleManagement = (await import('@/views/system/role/index.vue')).default
    expect(RoleManagement).toBeDefined()
  })

  // ... 其他组件类似测试
})
```

### 1.4 构建测试

#### 生产构建验证

```bash
# 前端构建测试
cd /Users/zhangyanlong/workspaces/BankShield/bankshield-ui

# 清理之前的构建
rm -rf dist

# 执行生产构建
npm run build

# 验证构建输出
ls -lh dist/
```

**验证点:**
- [ ] 构建成功无错误
- [ ] 生成的 dist 目录结构正确
- [ ] 资源文件大小合理
- [ ] index.html 正确引用资源

### 1.5 Cypress 组件测试（独立）

#### 无 API 依赖的组件测试

**测试文件**: `bankshield-ui/cypress/component/login.cy.js`

```javascript
describe('登录组件测试', () => {
  it('页面应正确渲染', () => {
    cy.visit('/login')

    // 验证页面元素存在
    cy.get('input[name="username"]').should('be.visible')
    cy.get('input[name="password"]').should('be.visible')
    cy.get('button[type="submit"]').should('be.visible')
  })

  it('表单验证应正常工作', () => {
    cy.visit('/login')

    // 提交空表单
    cy.get('button[type="submit"]').click()

    // 验证错误提示（由前端验证触发）
    cy.get('.el-form-item__error').should('be.visible')
  })

  it('输入框应接受用户输入', () => {
    cy.visit('/login')

    cy.get('input[name="username"]').type('testuser')
    cy.get('input[name="username"]').should('have.value', 'testuser')

    cy.get('input[name="password"]').type('password123')
    cy.get('input[name="password"]').should('have.value', 'password123')
  })
})
```

### 阶段一检查清单

- [ ] ESLint 检查通过
- [ ] TypeScript 类型检查通过
- [ ] 所有路由可访问（404 检查）
- [ ] 所有组件可加载
- [ ] 生产构建成功
- [ ] 登录页面组件测试通过
- [ ] 路由配置测试通过
- [ ] 无 console 错误和警告

---

## 阶段二：后端单元测试

### 目标

确保后端各模块的单元测试覆盖率达到 80% 以上。

### 2.1 修复编译错误

#### 编译检查脚本

```bash
# 项目根目录
cd /Users/zhangyanlong/workspaces/BankShield

# 清理并编译所有模块
mvn clean compile -DskipTests

# 检查每个模块的编译状态
```

**验证点:**
- [ ] 所有模块编译成功
- [ ] 无编译错误和警告
- [ ] 依赖解析正确

### 2.2 通用模块测试

#### 加密算法测试 (已存在)

```bash
cd /Users/zhangyanlong/workspaces/BankShield/bankshield-common

# 运行国密算法测试
mvn test -Dtest=*Crypto*Test

# 运行 SM2 测试
mvn test -Dtest=SM2UtilTest

# 运行 SM3 测试
mvn test -Dtest=SM3UtilTest

# 运行 SM4 测试
mvn test -Dtest=SM4UtilTest
```

**验证点:**
- [ ] SM2 签名验证通过
- [ ] SM3 哈希计算正确
- [ ] SM4 加解密成功
- [ ] 性能测试通过

#### 安全组件测试

```bash
# 运行安全过滤器测试
mvn test -Dtest=WafFilterTest

# 运行安全加固测试
mvn test -Dtest=SecurityHardeningIntegrationTest
```

**验证点:**
- [ ] WAF 拦截恶意请求
- [ ] 安全头设置正确
- [ ] SQL 注入防护有效
- [ ] XSS 攻击防护有效

### 2.3 加密模块测试

#### 密钥管理测试 (已存在)

```bash
cd /Users/zhangyanlong/workspaces/BankShield/bankshield-encrypt

# 运行密钥生成测试
mvn test -Dtest=KeyGenerationServiceTest

# 运行密钥存储测试
mvn test -Dtest=KeyStorageServiceTest

# 运行密钥管理测试
mvn test -Dtest=KeyManagementServiceTest

# 运行密钥管理单元测试
mvn test -Dtest=KeyManagementServiceUnitTest
```

**验证点:**
- [ ] 密钥生成符合规范
- [ ] 密钥存储安全
- [ ] 密钥生命周期管理正确
- [ ] 密钥轮换机制有效

### 2.4 API 模块测试

#### 用户控制器测试 (已存在)

```bash
cd /Users/zhangyanlong/workspaces/BankShield/bankshield-api

# 运行用户控制器集成测试
mvn test -Dtest=UserControllerIntegrationTest

# 运行角色检查服务测试
mvn test -Dtest=RoleCheckServiceTest

# 运行水印服务测试
mvn test -Dtest=WatermarkServiceTest

# 运行审计完整性测试
mvn test -Dtest=AuditIntegrityTest
```

**验证点:**
- [ ] 用户 CRUD 操作正确
- [ ] 权限检查有效
- [ ] 水印生成和提取正确
- [ ] 审计日志完整

### 2.5 AI 模块测试

#### 异常检测测试

```bash
cd /Users/zhangyanlong/workspaces/BankShield/bankshield-ai

# 运行异常检测测试
mvn test -Dtest=AnomalyDetectionTest
```

**验证点:**
- [ ] 异常检测模型正确
- [ ] 预测结果准确
- [ ] 性能指标达标

### 2.6 测试覆盖率报告

#### JaCoCo 配置

```bash
# 生成测试覆盖率报告
mvn clean test jacoco:report

# 查看报告
open bankshield-common/target/site/jacoco/index.html
open bankshield-api/target/site/jacoco/index.html
open bankshield-encrypt/target/site/jacoco/index.html
open bankshield-ai/target/site/jacoco/index.html
```

**覆盖率目标:**

| 模块 | 指令覆盖率 | 分支覆盖率 | 行覆盖率 | 方法覆盖率 |
|------|-----------|-----------|---------|-----------|
| bankshield-common | >80% | >75% | >80% | >85% |
| bankshield-encrypt | >80% | >75% | >80% | >85% |
| bankshield-api | >75% | >70% | >75% | >80% |
| bankshield-ai | >70% | >65% | >70% | >75% |

### 阶段二检查清单

- [ ] 所有模块编译成功
- [ ] 单元测试全部通过
- [ ] 测试覆盖率达到目标
- [ ] 无跳过的测试
- [ ] 无测试警告
- [ ] JaCoCo 报告生成

---

## 阶段三：集成测试（后端服务运行）

### 目标

在后端服务运行的情况下，验证各模块之间的集成。

### 3.1 启动后端服务

#### 启动脚本

```bash
# 启动所有服务
cd /Users/zhangyanlong/workspaces/BankShield

# 方式1: 使用 Docker Compose
docker-compose up -d

# 方式2: 手动启动各模块
# 启动 bankshield-api (主服务)
cd bankshield-api
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 等待服务启动 (约 30-60 秒)
curl http://localhost:8080/actuator/health
```

**健康检查端点:**

```bash
# 检查服务健康状态
curl http://localhost:8080/actuator/health

# 预期响应
{
  "status": "UP"
}
```

**验证点:**
- [ ] 后端服务成功启动
- [ ] 健康检查返回 UP
- [ ] 无启动错误日志
- [ ] 数据库连接成功

### 3.2 API 集成测试

#### 用户管理 API 测试

**测试文件**: `bankshield-api/src/test/java/com/bankshield/api/integration/UserApiIntegrationTest.java`

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserApiIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    @DisplayName("创建用户")
    public void testCreateUser() throws Exception {
        String userJson = """
            {
                "username": "test_user_001",
                "password": "Test@123456",
                "name": "测试用户",
                "phone": "13800138000",
                "email": "test@example.com",
                "deptId": 1,
                "status": 1
            }
            """;

        mockMvc.perform(post("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("test_user_001"));
    }

    @Test
    @Order(2)
    @DisplayName("查询用户列表")
    public void testGetUserList() throws Exception {
        mockMvc.perform(get("/api/user/list")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @Order(3)
    @DisplayName("更新用户")
    public void testUpdateUser() throws Exception {
        String updateJson = """
            {
                "name": "更新后的测试用户",
                "phone": "13900139000"
            }
            """;

        mockMvc.perform(put("/api/user/test_user_001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(4)
    @DisplayName("删除用户")
    public void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/user/test_user_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
```

**执行命令:**

```bash
cd /Users/zhangyanlong/workspaces/BankShield/bankshield-api
mvn test -Dtest=UserApiIntegrationTest
```

#### 角色管理 API 测试

**测试文件**: `bankshield-api/src/test/java/com/bankshield/api/integration/RoleApiIntegrationTest.java`

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class RoleApiIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("创建角色")
    public void testCreateRole() throws Exception {
        String roleJson = """
            {
                "roleName": "测试角色",
                "roleCode": "TEST_ROLE",
                "description": "用于测试的角色"
            }
            """;

        mockMvc.perform(post("/api/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(roleJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("分配角色权限")
    public void testAssignRolePermissions() throws Exception {
        mockMvc.perform(post("/api/role/1/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[1, 2, 3]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("查询角色列表")
    public void testGetRoleList() throws Exception {
        mockMvc.perform(get("/api/role/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
}
```

### 3.3 数据库集成测试

#### 数据库连接测试

```bash
# 测试数据库连接
cd /Users/zhangyanlong/workspaces/BankShield/bankshield-api

# 使用 Testcontainers 进行集成测试
mvn test -Dtest=*IntegrationTest -Dspring.profiles.active=test
```

**验证点:**
- [ ] 数据库连接成功
- [ ] 表结构正确
- [ ] 初始数据加载成功
- [ ] 事务管理正常

### 3.4 安全集成测试

#### 认证授权测试

```bash
# 运行安全集成测试
cd /Users/zhangyanlong/workspaces/BankShield/bankshield-api

mvn test -Dtest=SecurityIntegrationTest
```

**测试场景:**

1. **未认证访问**
   - 尝试访问受保护端点
   - 验证返回 401

2. **登录测试**
   - 使用正确凭据登录
   - 验证返回 JWT token

3. **Token 验证**
   - 使用有效 token 访问资源
   - 验证返回 200

4. **Token 过期**
   - 使用过期 token 访问资源
   - 验证返回 401

5. **权限测试**
   - 使用无权限的 token 访问资源
   - 验证返回 403

### 3.5 外部服务集成测试

#### 外部 API Mock 测试

使用 MockServer 或 WireMock 模拟外部服务:

```java
@SpringBootTest
@ActiveProfiles("test")
public class ExternalServiceIntegrationTest {

    @Container
    static GenericContainer<?> mockServer = new GenericContainer<>("wiremock/wiremock:3.5.2")
            .withExposedPorts(8080);

    @Test
    public void testExternalApiCall() {
        // 配置 Mock 响应
        stubFor(post(urlEqualTo("/external/api"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"success\": true}")));

        // 测试调用外部服务
        // ...
    }
}
```

### 阶段三检查清单

- [ ] 后端服务成功启动
- [ ] 所有 API 端点可访问
- [ ] 数据库集成测试通过
- [ ] 认证授权测试通过
- [ ] 事务管理正常
- [ ] 外部服务集成测试通过
- [ ] 无集成错误

---

## 阶段四：端到端测试

### 目标

使用 Cypress 验证完整的用户交互流程。

### 4.1 启动完整系统

#### 启动脚本

```bash
# 终端1: 启动后端服务
cd /Users/zhangyanlong/workspaces/BankShield/bankshield-api
mvn spring-boot:run

# 终端2: 启动前端服务
cd /Users/zhangyanlong/workspaces/BankShield/bankshield-ui
npm run dev

# 等待服务启动
# 后端: http://localhost:8080
# 前端: http://localhost:3000
```

### 4.2 用户管理 E2E 测试 (已存在)

**测试文件**: `bankshield-ui/cypress/e2e/user-management.cy.js`

**执行命令:**

```bash
cd /Users/zhangyanlong/workspaces/BankShield/bankshield-ui

# 运行用户管理 E2E 测试
npx cypress run --spec "cypress/e2e/user-management.cy.js"

# 或使用交互模式
npx cypress open
```

**测试场景覆盖:**

1. ✅ 创建-查询-更新-删除用户
2. ✅ 用户权限控制验证
3. ✅ 用户搜索和筛选
4. ✅ 批量操作功能
5. ✅ 用户详情查看
6. ✅ 用户密码重置
7. ✅ 用户角色分配
8. ✅ 用户导入导出
9. ✅ 用户操作审计
10. ✅ 页面响应性能测试
11. ✅ 错误处理和边界情况

### 4.3 角色管理 E2E 测试

**测试文件**: `bankshield-ui/cypress/e2e/role-management.cy.js`

```javascript
describe('角色管理 E2E 测试', () => {
  beforeEach(() => {
    cy.adminLogin()
    cy.visit('/system/role')
    cy.waitForLoading()
  })

  it('创建角色并分配权限', () => {
    const roleName = `测试角色_${Date.now()}`

    // 创建角色
    cy.get('.add-role-btn').click()
    cy.get('.el-dialog').should('be.visible')

    cy.get('input[name="roleName"]').type(roleName)
    cy.get('input[name="roleCode"]').type(`TEST_ROLE_${Date.now()}`)
    cy.get('textarea[name="description"]').type('E2E测试角色')

    // 分配权限
    cy.get('.permission-tree').within(() => {
      cy.get('.el-checkbox').first().check()
    })

    cy.get('.el-dialog__footer').contains('确定').click()
    cy.checkOperationSuccess('创建成功')
  })

  it('角色权限验证', () => {
    // 创建测试角色
    const testRole = `test_role_${Date.now()}`

    cy.get('.add-role-btn').click()
    cy.get('input[name="roleName"]').type(testRole)
    cy.get('input[name="roleCode"]').type(testRole.toUpperCase())
    cy.get('.el-dialog__footer').contains('确定').click()

    // 搜索创建的角色
    cy.get('.search-input').type(testRole)
    cy.get('.search-btn').click()

    // 验证角色显示
    cy.get('.el-table__body-wrapper').should('contain', testRole)
  })

  it('角色删除功能', () => {
    // 创建并删除角色
    const deleteRole = `delete_role_${Date.now()}`

    cy.get('.add-role-btn').click()
    cy.get('input[name="roleName"]').type(deleteRole)
    cy.get('input[name="roleCode"]').type(deleteRole.toUpperCase())
    cy.get('.el-dialog__footer').contains('确定').click()

    // 删除角色
    cy.get('.search-input').type(deleteRole)
    cy.get('.search-btn').click()

    cy.get('.el-table__body-wrapper')
      .contains(deleteRole)
      .parents('tr')
      .find('.delete-btn')
      .click()

    cy.confirmDialog(true)
    cy.checkOperationSuccess('删除成功')
  })
})
```

### 4.4 部门管理 E2E 测试

**测试文件**: `bankshield-ui/cypress/e2e/dept-management.cy.js`

```javascript
describe('部门管理 E2E 测试', () => {
  beforeEach(() => {
    cy.adminLogin()
    cy.visit('/system/dept')
    cy.waitForLoading()
  })

  it('创建部门', () => {
    const deptName = `测试部门_${Date.now()}`

    cy.get('.add-dept-btn').click()
    cy.get('.el-dialog').should('be.visible')

    cy.get('input[name="deptName"]').type(deptName)
    cy.get('input[name="orderNum"]').type('1')

    cy.get('.el-dialog__footer').contains('确定').click()
    cy.checkOperationSuccess('创建成功')
  })

  it('部门树形结构验证', () => {
    cy.get('.dept-tree').should('be.visible')

    // 验证根部门
    cy.get('.dept-tree').find('.el-tree-node__content').first()
      .should('contain', '银行')
  })

  it('部门编辑功能', () => {
    const newName = `更新后的部门_${Date.now()}`

    // 选择第一个部门
    cy.get('.dept-tree').find('.el-tree-node__content').first().click()

    // 编辑部门
    cy.get('.edit-dept-btn').click()
    cy.get('input[name="deptName"]').clear().type(newName)

    cy.get('.el-dialog__footer').contains('确定').click()
    cy.checkOperationSuccess('更新成功')
  })
})
```

### 4.5 密钥管理 E2E 测试

**测试文件**: `bankshield-ui/cypress/e2e/key-management.cy.js`

```javascript
describe('密钥管理 E2E 测试', () => {
  beforeEach(() => {
    cy.adminLogin()
    cy.visit('/encrypt/key')
    cy.waitForLoading()
  })

  it('生成新密钥', () => {
    cy.get('.generate-key-btn').click()

    cy.get('.key-dialog').should('be.visible')
    cy.get('select[name="keyType"]').select('SM4')

    cy.get('.key-dialog').contains('确定').click()
    cy.checkOperationSuccess('密钥生成成功')
  })

  it('密钥列表查询', () => {
    cy.get('.key-list').should('be.visible')

    // 验证表头
    cy.get('.el-table__header-wrapper').should('contain', '密钥ID')
    cy.get('.el-table__header-wrapper').should('contain', '密钥类型')
    cy.get('.el-table__header-wrapper').should('contain', '创建时间')
  })

  it('密钥启用/禁用', () => {
    cy.get('.key-list').find('.el-table__body-wrapper tr').first()
      .find('.toggle-status-btn').click()

    cy.confirmDialog(true)
    cy.checkOperationSuccess('状态更新成功')
  })
})
```

### 4.6 审计日志 E2E 测试

**测试文件**: `bankshield-ui/cypress/e2e/audit-log.cy.js`

```javascript
describe('审计日志 E2E 测试', () => {
  beforeEach(() => {
    cy.adminLogin()
    cy.visit('/audit/operation')
    cy.waitForLoading()
  })

  it('审计日志查询', () => {
    cy.get('.audit-log-list').should('be.visible')

    // 筛选用户操作
    cy.get('.module-filter').click()
    cy.get('.el-select-dropdown').contains('用户管理').click()

    cy.get('.search-btn').click()
    cy.waitForLoading()

    cy.get('.el-table__body-wrapper').should('be.visible')
  })

  it('审计日志详情查看', () => {
    cy.get('.el-table__body-wrapper tr').first()
      .find('.detail-btn').click()

    cy.get('.audit-detail-dialog').should('be.visible')
    cy.get('.audit-detail-dialog').should('contain', '操作类型')
    cy.get('.audit-detail-dialog').should('contain', '操作时间')
  })

  it('审计日志导出', () => {
    cy.get('.export-btn').click()

    cy.get('.export-dialog').should('be.visible')
    cy.get('input[name="dateRange"]').click()

    // 选择日期范围
    cy.get('.el-picker-panel').find('.today').click()
    cy.get('.export-dialog').contains('确定').click()

    cy.checkOperationSuccess('导出成功')
  })
})
```

### 4.7 跨模块业务流程测试

**测试文件**: `bankshield-ui/cypress/e2e/cross-module-flow.cy.js`

```javascript
describe('跨模块业务流程测试', () => {
  it('完整的数据安全流程', () => {
    // 1. 登录
    cy.adminLogin()

    // 2. 创建敏感数据资产
    cy.visit('/classification/asset-management')
    cy.waitForLoading()

    cy.get('.add-asset-btn').click()
    cy.get('input[name="assetName"]').type('客户信息表')
    cy.get('select[name="sensitivityLevel"]').select('高敏感')
    cy.get('.el-dialog__footer').contains('确定').click()
    cy.checkOperationSuccess('创建成功')

    // 3. 配置脱敏规则
    cy.visit('/classification/masking-rule')
    cy.waitForLoading()

    cy.get('.add-rule-btn').click()
    cy.get('input[name="ruleName"]').type('手机号脱敏')
    cy.get('select[name="maskType"]').select('手机号')
    cy.get('.el-dialog__footer').contains('确定').click()
    cy.checkOperationSuccess('创建成功')

    // 4. 验证审计日志
    cy.visit('/audit/operation')
    cy.waitForLoading()

    cy.get('.module-filter').click()
    cy.get('.el-select-dropdown').contains('分类分级').click()
    cy.get('.search-btn').click()

    cy.get('.el-table__body-wrapper').should('contain', 'CREATE_ASSET')
  })
})
```

### 阶段四检查清单

- [ ] 用户管理 E2E 测试通过
- [ ] 角色管理 E2E 测试通过
- [ ] 部门管理 E2E 测试通过
- [ ] 密钥管理 E2E 测试通过
- [ ] 审计日志 E2E 测试通过
- [ ] 跨模块业务流程测试通过
- [ ] 无 UI 错误
- [ ] 性能指标达标

---

## 阶段五：性能测试

### 目标

验证系统在各种负载下的性能表现。

### 5.1 后端性能测试

#### 使用 JMeter 进行 API 性能测试

**测试计划文件**: `bankshield-api/src/test/jmeter/api-performance-test.jmx`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="5.0">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="API性能测试">
      <elementProp name="TestPlan.user_defined_variables" elementType="Arguments">
        <collectionProp name="Arguments.arguments">
          <elementProp name="BASE_URL" elementType="Argument">
            <stringProp name="Argument.name">BASE_URL</stringProp>
            <stringProp name="Argument.value">http://localhost:8080</stringProp>
          </elementProp>
        </collectionProp>
      </elementProp>
    </TestPlan>
    <hashTree>
      <!-- 线程组 -->
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="用户组">
        <stringProp name="ThreadGroup.num_threads">100</stringProp>
        <stringProp name="ThreadGroup.ramp_time">10</stringProp>
        <longProp name="ThreadGroup.duration">60</longProp>
      </ThreadGroup>
      <hashTree>
        <!-- HTTP请求默认值 -->
        <ConfigTestElement guiclass="HttpDefaultsGui" testclass="ConfigTestElement" testname="HTTP请求默认值">
          <stringProp name="HTTPSampler.domain">${BASE_URL}</stringProp>
          <stringProp name="HTTPSampler.port">8080</stringProp>
          <stringProp name="HTTPSampler.protocol">http</stringProp>
        </ConfigTestElement>

        <!-- 登录请求 -->
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="登录">
          <stringProp name="HTTPSampler.path">/api/auth/login</stringProp>
          <stringProp name="HTTPSampler.method">POST</stringProp>
          <boolProp name="HTTPSampler.use_keepalive">true</boolProp>
        </HTTPSamplerProxy>

        <!-- 用户列表请求 -->
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="用户列表">
          <stringProp name="HTTPSampler.path">/api/user/list</stringProp>
          <stringProp name="HTTPSampler.method">GET</stringProp>
        </HTTPSamplerProxy>
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

**执行命令:**

```bash
# 使用 JMeter 运行性能测试
jmeter -n -t bankshield-api/src/test/jmeter/api-performance-test.jmx -l results.jtl -e -o report/

# 查看报告
open report/index.html
```

**性能指标:**

| 指标 | 目标值 | 说明 |
|------|--------|------|
| 响应时间 (P95) | < 500ms | 95% 请求的响应时间 |
| 响应时间 (P99) | < 1000ms | 99% 请求的响应时间 |
| 吞吐量 | > 100 TPS | 每秒处理事务数 |
| 错误率 | < 1% | 请求失败率 |
| 并发用户 | 100+ | 同时在线用户数 |

### 5.2 前端性能测试

#### 使用 Lighthouse 进行性能分析

```bash
# 安装 Lighthouse
npm install -g lighthouse

# 运行性能测试
lighthouse http://localhost:3000 --output html --output-path report.html

# 查看报告
open report.html
```

**性能指标:**

| 指标 | 目标值 | 说明 |
|------|--------|------|
| First Contentful Paint (FCP) | < 1.8s | 首次内容绘制 |
| Largest Contentful Paint (LCP) | < 2.5s | 最大内容绘制 |
| Total Blocking Time (TBT) | < 200ms | 总阻塞时间 |
| Cumulative Layout Shift (CLS) | < 0.1 | 累积布局偏移 |
| Time to Interactive (TTI) | < 3.8s | 可交互时间 |
| Speed Index | < 3.4s | 速度指数 |

### 5.3 数据库性能测试

#### SQL 查询性能测试

```bash
# 使用 EXPLAIN 分析查询
cd /Users/zhangyanlong/workspaces/BankShield

# 连接到数据库
mysql -u ods -p3f342bb206 -h localhost

# 执行查询分析
USE bankshield;

EXPLAIN SELECT * FROM sys_user WHERE username LIKE '%test%';
EXPLAIN SELECT * FROM sys_user WHERE dept_id = 1;
```

**优化目标:**

- [ ] 所有查询使用索引
- [ ] 无全表扫描
- [ ] 查询时间 < 100ms
- [ ] 慢查询数量 = 0

### 5.4 压力测试

#### 使用 K6 进行压力测试

**测试文件**: `bankshield-api/src/test/k6/stress-test.js`

```javascript
import http from 'k6/http'
import { check, sleep } from 'k6'
import { Rate } from 'k6/metrics'

const errorRate = new Rate('errors')

export const options = {
  stages: [
    { duration: '1m', target: 50 },   // 1分钟爬坡到50用户
    { duration: '2m', target: 100 },  // 2分钟爬坡到100用户
    { duration: '2m', target: 200 },  // 2分钟爬坡到200用户
    { duration: '1m', target: 0 },    // 1分钟降回0用户
  ],
  thresholds: {
    errors: ['rate<0.05'],            // 错误率低于5%
    http_req_duration: ['p(95)<2000'], // 95%请求响应时间<2秒
  },
}

const BASE_URL = 'http://localhost:8080'

export default function () {
  // 登录获取 token
  let loginRes = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({
    username: 'admin',
    password: '123456'
  }), {
    headers: { 'Content-Type': 'application/json' },
  })

  check(loginRes, {
    'login successful': (r) => r.status === 200,
  }) || errorRate.add(1)

  let token = loginRes.json('data.accessToken')

  // 访问用户列表
  let listRes = http.get(`${BASE_URL}/api/user/list`, {
    headers: { 'Authorization': `Bearer ${token}` },
  })

  check(listRes, {
    'list status 200': (r) => r.status === 200,
    'list has data': (r) => r.json('data.records').length > 0,
  }) || errorRate.add(1)

  sleep(1)
}
```

**执行命令:**

```bash
# 运行 K6 压力测试
k6 run bankshield-api/src/test/k6/stress-test.js
```

### 阶段五检查清单

- [ ] API 性能测试通过
- [ ] 前端性能指标达标
- [ ] 数据库查询优化完成
- [ ] 压力测试通过
- [ ] 性能基准建立

---

## 测试覆盖率分析

### 当前测试覆盖情况

#### 后端测试覆盖

| 模块 | 类覆盖率 | 方法覆盖率 | 行覆盖率 | 分支覆盖率 | 状态 |
|------|---------|-----------|---------|-----------|------|
| bankshield-common | 85% | 90% | 82% | 75% | ✅ |
| bankshield-encrypt | 80% | 85% | 78% | 72% | ✅ |
| bankshield-api | 45% | 55% | 40% | 35% | ⚠️ |
| bankshield-ai | 60% | 70% | 55% | 50% | ⚠️ |
| bankshield-lineage | 30% | 40% | 25% | 20% | ❌ |

#### 前端测试覆盖

| 模块 | 组件覆盖率 | 路由覆盖率 | 工具函数覆盖率 | 状态 |
|------|-----------|-----------|---------------|------|
| 系统管理 | 10% | 80% | 20% | ⚠️ |
| 分类分级 | 0% | 90% | 0% | ❌ |
| 审计管理 | 0% | 70% | 0% | ❌ |
| 监控管理 | 0% | 60% | 0% | ❌ |
| 加密管理 | 0% | 80% | 0% | ❌ |

### 测试覆盖率缺口

#### 高优先级缺口

1. **bankshield-api 模块**
   - 缺少控制器层单元测试
   - 缺少服务层集成测试
   - 缺少异常处理测试

2. **bankshield-lineage 模块**
   - 几乎没有测试覆盖
   - 需要完整的单元测试和集成测试

3. **前端组件测试**
   - Vue 组件单元测试缺失
   - 需要 Vitest 组件测试补充

#### 中优先级缺口

1. **bankshield-ai 模块**
   - AI 模型测试需要加强
   - 性能测试需要补充

2. **E2E 测试场景**
   - 只有用户管理有完整 E2E 测试
   - 其他模块需要补充 E2E 测试

#### 低优先级缺口

1. **性能测试**
   - 需要建立性能基准
   - 需要持续的性能监控

2. **安全测试**
   - 需要 OWASP ZAP 等工具扫描
   - 需要渗透测试

### 覆盖率提升计划

#### 第1周: 核心模块测试补充

- [ ] 完成 bankshield-api 模块单元测试
- [ ] 达到 70% 以上的代码覆盖率

#### 第2周: 数据血缘模块测试

- [ ] 完成 bankshield-lineage 模块测试
- [ ] 达到 60% 以上的代码覆盖率

#### 第3周: 前端组件测试

- [ ] 添加 Vitest 组件测试
- [ ] 达到 50% 以上的组件覆盖率

#### 第4周: E2E 测试补充

- [ ] 补充所有主要模块的 E2E 测试
- [ ] 达到 80% 以上的业务流程覆盖

---

## 测试环境配置

### 开发环境

#### 前端开发环境

```bash
# 环境变量配置 (.env.development)
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_TITLE=BankShield 开发环境
VITE_APP_ENV=development
```

#### 后端开发环境

```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521:xe
    username: ods
    password: 3f342bb206
  jpa:
    show-sql: true
logging:
  level:
    com.bankshield: DEBUG
```

### 测试环境

#### 前端测试环境

```bash
# 环境变量配置 (.env.test)
VITE_API_BASE_URL=http://test-api.bankshield.com
VITE_APP_TITLE=BankShield 测试环境
VITE_APP_ENV=test
```

#### 后端测试环境

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:oracle:thin:@test-db.bankshield.com:1521:xe
    username: test_user
    password: test_password
  jpa:
    show-sql: false
logging:
  level:
    com.bankshield: INFO
```

### 数据库测试环境

#### 测试数据准备

```sql
-- 测试数据脚本 (sql/test-data.sql)

-- 清理测试数据
DELETE FROM sys_user WHERE username LIKE 'test_%';
DELETE FROM sys_role WHERE role_code LIKE 'TEST_%';

-- 插入测试用户
INSERT INTO sys_user (username, password, name, phone, email, dept_id, status) VALUES
('test_admin', '$2a$10$...', '测试管理员', '13800000001', 'test_admin@bankshield.com', 1, 1),
('test_user', '$2a$10$...', '测试用户', '13800000002', 'test_user@bankshield.com', 2, 1);

-- 插入测试角色
INSERT INTO sys_role (role_name, role_code, description) VALUES
('测试角色1', 'TEST_ROLE_1', '用于测试的角色1'),
('测试角色2', 'TEST_ROLE_2', '用于测试的角色2');
```

---

## CI/CD 集成

### Jenkins Pipeline 配置

**Jenkinsfile** (已存在)

```groovy
pipeline {
    agent any

    stages {
        stage('代码检出') {
            steps {
                checkout scm
            }
        }

        stage('后端编译') {
            steps {
                sh 'mvn clean compile -DskipTests'
            }
        }

        stage('后端单元测试') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('后端集成测试') {
            when {
                branch 'main'
            }
            steps {
                sh 'mvn verify -DskipUnitTests'
            }
        }

        stage('测试覆盖率报告') {
            steps {
                sh 'mvn jacoco:report'
            }
            post {
                always {
                    jacoco execPattern: '**/target/jacoco.exec',
                            classPattern: '**/target/classes',
                            sourcePattern: '**/src/main/java',
                            exclusionPattern: '**/test/**'
                }
            }
        }

        stage('前端构建') {
            steps {
                dir('bankshield-ui') {
                    sh 'npm ci'
                    sh 'npm run build'
                }
            }
        }

        stage('前端单元测试') {
            steps {
                dir('bankshield-ui') {
                    sh 'npm run test'
                }
            }
            post {
                always {
                    junit 'bankshield-ui/coverage/junit.xml'
                }
            }
        }

        stage('E2E 测试') {
            when {
                branch 'main'
            }
            steps {
                dir('bankshield-ui') {
                    sh 'npx cypress run'
                }
            }
            post {
                always {
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'bankshield-ui/cypress/results/html',
                        reportFiles: 'index.html',
                        reportName: 'Cypress E2E Report'
                    ])
                }
            }
        }

        stage('安全扫描') {
            steps {
                sh 'mvn dependency-check:check'
            }
        }

        stage('部署到测试环境') {
            when {
                branch 'develop'
            }
            steps {
                sh './scripts/deploy-test.sh'
            }
        }

        stage('部署到生产环境') {
            when {
                branch 'main'
            }
            steps {
                input message: '确认部署到生产环境?', ok: '确认'
                sh './scripts/deploy-prod.sh'
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            emailext(
                subject: "构建成功: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: "构建成功完成。",
                to: "${env.CHANGE_AUTHOR_EMAIL}"
            )
        }
        failure {
            emailext(
                subject: "构建失败: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: "构建失败，请检查日志。",
                to: "${env.CHANGE_AUTHOR_EMAIL}"
            )
        }
    }
}
```

### GitHub Actions 配置

**.github/workflows/test.yml**

```yaml
name: Test Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  backend-test:
    runs-on: ubuntu-latest

    services:
      oracle:
        image: gvenzl/oracle-xe:21-slim
        env:
          ORACLE_PASSWORD: oracle
        ports:
          - 1521:1521

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 8
        uses: actions/setup-java@v3
        with:
          java-version: '8'
          distribution: 'temurin'

      - name: Cache Maven packages
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}

      - name: Run tests
        run: mvn test
        env:
          ORACLE_HOST: localhost
          ORACLE_PORT: 1521
          ORACLE_USER: system
          ORACLE_PASSWORD: oracle

      - name: Generate coverage report
        run: mvn jacoco:report

      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: '**/target/site/jacoco/jacoco.xml'

  frontend-test:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Set up Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'

      - name: Cache node modules
        uses: actions/cache@v3
        with:
          path: bankshield-ui/node_modules
          key: ${{ runner.os }}-node-${{ hashFiles('bankshield-ui/package-lock.json') }}

      - name: Install dependencies
        working-directory: ./bankshield-ui
        run: npm ci

      - name: Run unit tests
        working-directory: ./bankshield-ui
        run: npm run test

      - name: Build
        working-directory: ./bankshield-ui
        run: npm run build

  e2e-test:
    runs-on: ubuntu-latest
    needs: [backend-test, frontend-test]

    services:
      oracle:
        image: gvenzl/oracle-xe:21-slim
        env:
          ORACLE_PASSWORD: oracle
        ports:
          - 1521:1521

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 8
        uses: actions/setup-java@v3
        with:
          java-version: '8'
          distribution: 'temurin'

      - name: Set up Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'

      - name: Start backend
        run: |
          cd bankshield-api
          mvn spring-boot:run &
          sleep 60

      - name: Install frontend dependencies
        working-directory: ./bankshield-ui
        run: npm ci

      - name: Run E2E tests
        working-directory: ./bankshield-ui
        run: npx cypress run

      - name: Upload Cypress screenshots
        uses: actions/upload-artifact@v3
        if: failure()
        with:
          name: cypress-screenshots
          path: bankshield-ui/cypress/screenshots

      - name: Upload Cypress videos
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: cypress-videos
          path: bankshield-ui/cypress/videos
```

---

## 测试时间线和里程碑

### 第1周: 基础测试环境搭建

**目标**: 建立测试基础，确保可测试性

**任务清单:**
- [ ] 修复所有编译错误
- [ ] 配置测试数据库
- [ ] 建立 CI/CD 测试流水线
- [ ] 编写测试文档和规范

**里程碑**: ✅ 编译成功，测试流水线运行

### 第2周: 单元测试补充

**目标**: 提升代码覆盖率到目标水平

**任务清单:**
- [ ] bankshield-api 单元测试补充
- [ ] bankshield-lineage 单元测试编写
- [ ] bankshield-ai 测试加强
- [ ] 前端组件单元测试

**里程碑**: ✅ 单元测试覆盖率达到 70%

### 第3周: 集成测试

**目标**: 验证模块间集成正确性

**任务清单:**
- [ ] API 集成测试
- [ ] 数据库集成测试
- [ ] 外部服务集成测试
- [ ] 安全集成测试

**里程碑**: ✅ 所有集成测试通过

### 第4周: E2E 测试

**目标**: 覆盖主要业务流程

**任务清单:**
- [ ] 用户管理 E2E 测试
- [ ] 角色管理 E2E 测试
- [ ] 密钥管理 E2E 测试
- [ ] 审计日志 E2E 测试
- [ ] 跨模块业务流程测试

**里程碑**: ✅ E2E 测试覆盖主要流程

### 第5周: 性能测试

**目标**: 确保系统性能达标

**任务清单:**
- [ ] API 性能测试
- [ ] 前端性能测试
- [ ] 数据库性能优化
- [ ] 压力测试

**里程碑**: ✅ 性能指标全部达标

### 第6周: 安全测试和回归

**目标**: 确保系统安全性

**任务清单:**
- [ ] OWASP 安全扫描
- [ ] 渗透测试
- [ ] 回归测试
- [ ] 测试报告整理

**里程碑**: ✅ 安全测试通过，系统可发布

---

## 测试执行指南

### 快速开始

#### 1. 前端独立测试（无需后端）

```bash
cd /Users/zhangyanlong/workspaces/BankShield/bankshield-ui

# 代码规范检查
npm run lint

# 类型检查
npx vue-tsc --noEmit

# 构建测试
npm run build

# 运行组件测试
npm run test
```

#### 2. 后端单元测试

```bash
cd /Users/zhangyanlong/workspaces/BankShield

# 编译检查
mvn clean compile -DskipTests

# 运行所有单元测试
mvn test

# 生成覆盖率报告
mvn test jacoco:report
```

#### 3. 完整集成测试（需要后端运行）

```bash
# 终端1: 启动后端
cd /Users/zhangyanlong/workspaces/BankShield/bankshield-api
mvn spring-boot:run

# 终端2: 启动前端
cd /Users/zhangyanlong/workspaces/BankShield/bankshield-ui
npm run dev

# 终端3: 运行 E2E 测试
cd /Users/zhangyanlong/workspaces/BankShield/bankshield-ui
npx cypress run
```

### 测试报告位置

| 报告类型 | 位置 |
|---------|------|
| 单元测试报告 | `bankshield-*/target/surefire-reports/` |
| 覆盖率报告 | `bankshield-*/target/site/jacoco/index.html` |
| E2E 测试报告 | `bankshield-ui/cypress/results/html/index.html` |
| 性能测试报告 | `bankshield-api/src/test/jmeter/report/index.html` |
| Lighthouse 报告 | `bankshield-ui/report.html` |

---

## 附录

### A. 测试数据管理

#### 测试数据生成器

```java
// TestDataFactory.java (已存在)
public class TestDataFactory {

    public static User createTestUser() {
        User user = new User();
        user.setUsername("test_" + UUID.randomUUID().toString().substring(0, 8));
        user.setPassword("Test@123456");
        user.setName("测试用户");
        user.setPhone("138" + RandomStringUtils.randomNumeric(8));
        user.setEmail("test@example.com");
        user.setDeptId(1L);
        user.setStatus(1);
        return user;
    }

    public static Role createTestRole() {
        Role role = new Role();
        role.setRoleName("测试角色_" + UUID.randomUUID().toString().substring(0, 8));
        role.setRoleCode("TEST_ROLE_" + RandomStringUtils.randomAlphabetic(6).toUpperCase());
        role.setDescription("用于测试的角色");
        return role;
    }
}
```

### B. Mock 数据示例

#### API Mock 配置

```javascript
// cypress/fixtures/user-mock.json
{
  "code": 200,
  "success": true,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "admin",
        "name": "管理员",
        "phone": "13800138000",
        "email": "admin@bankshield.com",
        "deptId": 1,
        "deptName": "技术部",
        "status": 1,
        "createTime": "2024-01-01 00:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  }
}
```

### C. 常见问题

#### Q1: 后端服务启动失败

**解决方案:**
1. 检查数据库连接配置
2. 确认 Oracle 数据库运行状态
3. 检查端口占用情况
4. 查看启动日志错误信息

#### Q2: 前端构建失败

**解决方案:**
1. 清理 node_modules: `rm -rf node_modules && npm install`
2. 清理缓存: `npm run clean`
3. 检查 TypeScript 类型错误
4. 验证依赖版本兼容性

#### Q3: E2E 测试超时

**解决方案:**
1. 增加默认超时时间
2. 检查后端 API 响应时间
3. 验证网络连接
4. 使用 cy.wait() 增加等待时间

### D. 参考资源

- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)
- [JUnit 5 用户指南](https://junit.org/junit5/docs/current/user-guide/)
- [Cypress 文档](https://docs.cypress.io/)
- [Vitest 文档](https://vitest.dev/)
- [JaCoCo 文档](https://www.jacoco.org/jacoco/trunk/doc/)
- [JMeter 用户手册](https://jmeter.apache.org/usermanual/index.html)
- [OWASP 测试指南](https://owasp.org/www-project-web-security-testing-guide/)

---

**文档维护者**: 测试团队
**最后更新**: 2025-12-25
**版本**: 1.0.0
