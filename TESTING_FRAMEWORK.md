# BankShield 自动化测试框架

## 概述

BankShield自动化测试框架是一个完整的测试解决方案，涵盖单元测试、集成测试、端到端(E2E)测试和性能测试。该框架基于现代测试最佳实践，确保系统的高质量和可靠性。

## 测试架构

```
测试金字塔结构：
┌─────────────────────────────────────┐
│        E2E测试 (10%)                │  Cypress/Selenium
├─────────────────────────────────────┤
│      集成测试 (20%)                 │  RestAssured/TestContainers
├─────────────────────────────────────┤
│      单元测试 (70%)                 │  JUnit5/Mockito
└─────────────────────────────────────┘
```

## 测试类型

### 1. 单元测试 (Unit Tests)

**目标**：验证单个类或方法的功能正确性

**技术栈**：
- JUnit 5 - 测试框架
- Mockito - Mock框架
- AssertJ - 断言库
- JaCoCo - 代码覆盖率

**执行命令**：
```bash
mvn test -Dtest=**/*UnitTest.java
```

**覆盖率要求**：
- 整体覆盖率 > 80%
- 核心模块覆盖率 > 90%
- 关键业务逻辑覆盖率 = 100%

### 2. 集成测试 (Integration Tests)

**目标**：验证多个组件之间的交互

**技术栈**：
- TestContainers - 容器化测试环境
- RestAssured - API测试
- Spring Boot Test - 集成测试框架

**执行命令**：
```bash
mvn test -Dtest=**/*IntegrationTest.java
```

**测试范围**：
- API接口测试
- 数据库集成测试
- 缓存集成测试
- 消息队列集成测试

### 3. 端到端测试 (E2E Tests)

**目标**：验证完整的用户业务流程

**技术栈**：
- Cypress - E2E测试框架
- Selenium - 浏览器自动化
- Cucumber - BDD测试

**执行命令**：
```bash
cd bankshield-ui && npm run cypress:run
```

**测试场景**：
- 用户注册登录流程
- 数据加密解密流程
- 密钥管理流程
- 审计日志查看流程

### 4. 性能测试 (Performance Tests)

**目标**：验证系统在高负载下的表现

**技术栈**：
- k6 - 现代性能测试工具
- JMeter - 传统性能测试工具
- Gatling - 高性能测试工具

**执行命令**：
```bash
k6 run tests/performance/bankshield-k6-test.js
```

**性能指标**：
- 响应时间 < 200ms (95%请求)
- 并发用户数支持 1000+
- 错误率 < 1%
- 吞吐量 > 1000 TPS

## 测试环境

### 测试数据

测试数据使用以下策略生成：

1. **工厂模式**：`TestDataFactory`类提供标准化的测试数据
2. **随机生成**：使用Faker库生成真实的测试数据
3. **数据隔离**：每个测试使用独立的数据集
4. **数据清理**：测试完成后自动清理测试数据

### 测试数据库

- **MySQL**：使用TestContainers创建隔离的MySQL实例
- **Redis**：使用TestContainers创建隔离的Redis实例
- **初始化脚本**：`test-data.sql`提供基础测试数据

### 测试配置

测试配置文件位于：
- `application-test.yml` - 测试环境配置
- `application-integration.yml` - 集成测试配置
- `application-performance.yml` - 性能测试配置

## 测试执行

### 快速开始

运行所有测试：
```bash
./tests/run-all-tests.sh
```

运行特定类型测试：
```bash
./tests/run-all-tests.sh --unit        # 仅单元测试
./tests/run-all-tests.sh --integration # 仅集成测试
./tests/run-all-tests.sh --e2e         # 仅E2E测试
./tests/run-all-tests.sh --performance # 仅性能测试
```

### CI/CD集成

测试框架已集成到GitHub Actions中：

1. **代码质量检查**：SonarQube、OWASP依赖检查
2. **单元测试**：自动运行，生成覆盖率报告
3. **集成测试**：使用TestContainers环境
4. **E2E测试**：Cypress自动化测试
5. **性能测试**：k6和JMeter性能测试
6. **安全扫描**：Trivy、Snyk安全扫描

## 测试报告

### 报告类型

1. **Surefire报告**：Maven测试执行报告
2. **JaCoCo报告**：代码覆盖率报告
3. **Allure报告**：美观的测试报告
4. **Cypress报告**：E2E测试报告
5. **性能报告**：k6和JMeter性能分析报告

### 报告查看

测试完成后，可以在以下位置查看报告：

```
test-results/
├── 20240101_120000/                    # 测试时间戳
│   ├── unit-test-reports/              # 单元测试报告
│   ├── unit-test-jacoco/               # 覆盖率报告
│   ├── integration-test-reports/       # 集成测试报告
│   ├── e2e-results/                    # E2E测试报告
│   ├── k6-results.json                 # k6性能报告
│   ├── jmeter-report/                  # JMeter性能报告
│   └── test-summary.html               # 综合测试报告
```

## 测试最佳实践

### 1. 单元测试最佳实践

```java
@DisplayName("用户服务单元测试")
@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest extends BaseUnitTest {
    
    @Mock
    private UserMapper userMapper;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    @Test
    @Order(1)
    @DisplayName("创建用户成功")
    void testCreateUserSuccess() {
        // Given
        User user = TestDataFactory.createTestUser("testuser");
        when(userMapper.insert(any(User.class))).thenReturn(1);
        
        // When
        Result<String> result = userService.addUser(user);
        
        // Then
        assertResultSuccess(result);
        verify(userMapper, times(1)).insert(any(User.class));
    }
}
```

### 2. 集成测试最佳实践

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public class UserControllerIntegrationTest extends BaseIntegrationTest {
    
    @Test
    @Order(1)
    @DisplayName("创建用户API测试")
    void testCreateUserApi() {
        // Given
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("username", "api_test_user");
        createRequest.put("password", "Test@123456");
        createRequest.put("name", "API测试用户");
        
        // When
        Response response = givenAuthenticated()
            .body(toJson(createRequest))
            .when()
            .post("/api/user");
        
        // Then
        assertSuccessResponse(response);
        response.then()
            .body("message", equalTo("添加用户成功"));
    }
}
```

### 3. E2E测试最佳实践

```javascript
describe('用户管理完整生命周期测试', () => {
  beforeEach(() => {
    cy.adminLogin()
    cy.visit('/system/user')
    cy.waitForLoading()
  })

  it('创建-查询-更新-删除用户', () => {
    const username = `e2e_user_${Date.now()}`
    
    // 创建用户
    cy.get('.add-user-btn').click()
    cy.get('input[name="username"]').type(username)
    cy.get('input[name="password"]').type('Test@123456')
    cy.get('input[name="name"]').type('E2E测试用户')
    cy.get('.el-dialog__footer').contains('确定').click()
    
    // 验证创建成功
    cy.checkOperationSuccess('创建成功')
    
    // 查询用户
    cy.get('.search-input').type(username)
    cy.get('.search-btn').click()
    cy.get('.el-table__body-wrapper').should('contain', username)
    
    // 更新用户
    cy.get('.el-table__body-wrapper')
      .contains(username)
      .parents('tr')
      .find('.edit-btn')
      .click()
    
    cy.get('input[name="name"]').clear().type('更新后的用户')
    cy.get('.el-dialog__footer').contains('确定').click()
    
    cy.checkOperationSuccess('更新成功')
    
    // 删除用户
    cy.get('.el-table__body-wrapper')
      .contains(username)
      .parents('tr')
      .find('.delete-btn')
      .click()
    
    cy.confirmDialog(true)
    cy.checkOperationSuccess('删除成功')
  })
})
```

### 4. 性能测试最佳实践

```javascript
import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate } from 'k6/metrics';

const errorRate = new Rate('errors');

export const options = {
  stages: [
    { duration: '2m', target: 100 },   // 预热阶段
    { duration: '5m', target: 500 },   // 加压阶段
    { duration: '5m', target: 1000 },  // 峰值阶段
    { duration: '2m', target: 0 },     // 恢复阶段
  ],
  thresholds: {
    'http_req_duration': ['p(95)<200'],     // 95%请求响应时间小于200ms
    'http_req_failed': ['rate<0.01'],       // 错误率小于1%
    'errors': ['rate<0.01'],
  },
};

export default function () {
  group('用户管理', function () {
    const loginResponse = http.post('http://localhost:8080/api/auth/login', JSON.stringify({
      username: 'admin',
      password: '123456'
    }), {
      headers: { 'Content-Type': 'application/json' }
    });
    
    check(loginResponse, {
      '登录成功': (r) => r.status === 200,
      '获取到token': (r) => r.json().data.accessToken !== undefined
    });
    
    errorRate.add(loginResponse.status !== 200);
    sleep(1);
  });
}
```

## 测试数据管理

### 1. 测试数据工厂

```java
public class TestDataFactory {
    
    public static User createTestUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9KFLHcpN7W/YKaO");
        user.setName("测试用户" + username);
        user.setPhone("138" + String.format("%08d", (int)(Math.random() * 100000000)));
        user.setEmail(username + "@test.com");
        user.setDeptId(1L);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        return user;
    }
    
    public static EncryptionKey createTestKey(String keyName) {
        EncryptionKey key = new EncryptionKey();
        key.setKeyName(keyName);
        key.setKeyType("SM4");
        key.setKeyLength(128);
        key.setKeyUsage("ENCRYPT");
        key.setKeyStatus("ACTIVE");
        key.setKeyMaterial("test_key_material_" + keyName);
        key.setKeyFingerprint("test_fingerprint_" + keyName);
        key.setExpireTime(LocalDateTime.now().plusYears(1));
        key.setCreateTime(LocalDateTime.now());
        key.setUpdateTime(LocalDateTime.now());
        return key;
    }
}
```

### 2. Mock配置

```java
@Configuration
public class MockConfig {
    
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
        ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.set(anyString(), any())).thenReturn(true);
        when(valueOperations.get(anyString())).thenReturn(null);
        
        return redisTemplate;
    }
}
```

## 持续集成

### GitHub Actions工作流

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop, feature/* ]
  pull_request:
    branches: [ main, develop ]
  schedule:
    - cron: '0 2 * * *'  # 每天凌晨2点运行

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 8
        uses: actions/setup-java@v3
        with:
          java-version: '8'
          distribution: 'temurin'
      - name: Run unit tests
        run: mvn clean test -Dspring.profiles.active=test
      - name: Generate coverage report
        run: mvn jacoco:report
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
```

## 测试质量指标

### 1. 代码覆盖率
- **行覆盖率**：> 80%
- **分支覆盖率**：> 75%
- **方法覆盖率**：> 85%
- **类覆盖率**：> 90%

### 2. 性能指标
- **响应时间**：95%请求 < 200ms
- **并发能力**：支持 1000+ 并发用户
- **错误率**：< 1%
- **吞吐量**：> 1000 TPS

### 3. 质量门槛
- **单元测试通过率**：100%
- **集成测试通过率**：100%
- **E2E测试通过率**：100%
- **性能测试通过率**：100%
- **安全扫描**：0 Critical, 0 High

## 故障排查

### 常见问题

1. **测试数据库连接失败**
   ```bash
   # 检查MySQL服务状态
   sudo systemctl status mysql
   
   # 检查端口是否被占用
   netstat -tlnp | grep 3306
   ```

2. **Redis连接失败**
   ```bash
   # 检查Redis服务状态
   sudo systemctl status redis
   
   # 测试Redis连接
   redis-cli ping
   ```

3. **Cypress测试失败**
   ```bash
   # 检查前端服务是否启动
   curl http://localhost:3000
   
   # 查看Cypress错误日志
   cat cypress/results/mochawesome.json
   ```

4. **性能测试工具未安装**
   ```bash
   # 安装k6
   sudo apt-get install k6
   
   # 安装JMeter
   wget https://downloads.apache.org//jmeter/binaries/apache-jmeter-5.5.zip
   ```

## 扩展和定制

### 1. 添加新的测试类型

在 `tests/` 目录下创建新的测试模块：

```bash
mkdir tests/new-test-type
cd tests/new-test-type
# 添加测试脚本和配置文件
```

### 2. 自定义测试数据

扩展 `TestDataFactory` 类：

```java
public static NewEntity createTestNewEntity(String name) {
    NewEntity entity = new NewEntity();
    entity.setName(name);
    // ... 设置其他属性
    return entity;
}
```

### 3. 配置新的性能测试场景

在 `tests/performance/` 目录下添加新的k6脚本：

```javascript
// new-performance-test.js
import http from 'k6/http';
import { check } from 'k6';

export const options = {
  stages: [
    { duration: '1m', target: 100 },
    { duration: '3m', target: 500 },
    { duration: '1m', target: 0 },
  ],
};

export default function () {
  // 测试逻辑
}
```

## 最佳实践总结

1. **测试先行**：在开发功能前先写测试
2. **保持独立**：每个测试应该独立运行，不依赖其他测试
3. **数据隔离**：使用独立的测试数据，避免数据污染
4. **快速失败**：测试应该快速执行，快速发现问题
5. **持续集成**：将测试集成到CI/CD流程中
6. **监控指标**：持续监控测试质量和覆盖率
7. **文档完整**：为复杂的测试场景编写详细文档
8. **定期维护**：定期更新测试用例，保持测试的有效性

## 联系方式

如有问题或建议，请联系：
- 邮箱：support@bankshield.com
- 文档：https://docs.bankshield.com
- 问题反馈：https://github.com/bankshield/bankshield/issues

---

*最后更新：2024年1月*