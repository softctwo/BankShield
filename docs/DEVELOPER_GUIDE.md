# BankShield 开发者手册

**版本**: v1.0.0
**更新日期**: 2025-12-25
**目标读者**: 项目开发者

---

## 📋 目录

- [快速开始](#快速开始)
- [开发环境搭建](#开发环境搭建)
- [代码规范](#代码规范)
- [开发指南](#开发指南)
- [测试指南](#测试指南)
- [调试技巧](#调试技巧)
- [常见问题](#常见问题)
- [最佳实践](#最佳实践)

---

## 🚀 快速开始

### 前置要求

- **JDK**: 1.8+ (OpenJDK 1.8.0_362+)
- **Maven**: 3.6+
- **Node.js**: 16.x (推荐 16.20.0+)
- **IDE**: IntelliJ IDEA 2023+ 或 VS Code
- **Git**: 2.30+

### 克隆项目

```bash
# 1. 克隆代码仓库
git clone https://github.com/bankshield/bankshield.git
cd BankShield

# 2. 切换到开发分支
git checkout develop

# 3. 创建功能分支
git checkout -b feature/your-feature-name
```

### 初始化数据库

```bash
# 导入初始化脚本
mysql -u root -p < sql/init_database.sql

# 导入模块脚本（按需）
mysql -u root -p bankshield < sql/encrypt_module.sql
mysql -u root -p bankshield < sql/audit_tables.sql
```

### 启动项目

```bash
# 方式一：使用启动脚本
./scripts/start.sh --dev

# 方式二：分别启动
# 后端
cd bankshield-api && mvn spring-boot:run

# 前端
cd bankshield-ui && npm run dev
```

---

## 💻 开发环境搭建

### IDE配置

#### IntelliJ IDEA配置

1. **安装插件**
   - Lombok Plugin
   - MyBatisX Plugin
   - Rainbow Brackets
   - Alibaba Java Coding Guidelines

2. **配置Maven**
   ```
   File → Settings → Build, Execution, Deployment → Build Tools → Maven
   - Maven home: /usr/local/maven
   - User settings file: ~/.m2/settings.xml
   ```

3. **配置代码格式化**
   ```
   File → Settings → Editor → Code Style → Java
   Scheme: Project
   Import from: GoogleStyleGuide.xml
   ```

4. **配置自动保存**
   ```
   File → Settings → Appearance & Behavior → System Settings
   ✓ Save file on frame deactivation
   ✓ Save file automatically if application is idle for X sec
   ```

#### VS Code配置

1. **安装扩展**
   ```
   - Java Extension Pack (Microsoft)
   - Spring Boot Extension Pack (Pivotal)
   - Vue - Official (Vue)
   - ESLint
   - Prettier
   - GitLens
   ```

2. **配置settings.json**
   ```json
   {
     "java.configuration.runtimes": [
       {
         "name": "JavaSE-1.8",
         "path": "/usr/lib/jvm/java-1.8.0-openjdk"
       }
     ],
     "editor.formatOnSave": true,
     "editor.codeActionsOnSave": {
       "source.fixAll.eslint": true
     },
     "vetur.format.defaultFormatter": "prettier"
   }
   ```

### 本地配置

#### 后端配置

```yaml
# bankshield-api/src/main/resources/application-local.yml
server:
  port: 8080

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/bankshield?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password

  redis:
    host: localhost
    port: 6379
    database: 0

logging:
  level:
    com.bankshield: DEBUG
    org.springframework: INFO
```

#### 前端配置

```bash
# bankshield-ui/.env.local
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_ENV=development
```

---

## 📝 代码规范

### Java代码规范

#### 命名规范

```java
// 类名：大驼峰
public class UserService {}

// 方法名：小驼峰
public void getUserById() {}

// 常量：全大写，下划线分隔
private static final int MAX_RETRY = 3;

// 变量：小驼峰
private String userName;

// 包名：全小写
package com.bankshield.api.service;
```

#### 注释规范

```java
/**
 * 用户服务
 *
 * <p>提供用户增删改查等核心功能</p>
 *
 * @author BankShield
 * @since 1.0.0
 */
@Service
public class UserService {

    /**
     * 根据ID获取用户信息
     *
     * @param id 用户ID，不能为null
     * @return 用户信息，如果不存在返回null
     * @throws IllegalArgumentException 如果ID为null
     */
    public User getUserById(Long id) {
        // TODO: 实现查询逻辑
        return null;
    }
}
```

#### 异常处理规范

```java
// 1. 自定义异常
public class BusinessException extends RuntimeException {
    private final String code;
    private final String message;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}

// 2. 统一返回
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.error(e.getMessage());
    }
}

// 3. 使用异常
public User getUserById(Long id) {
    User user = userMapper.selectById(id);
    if (user == null) {
        throw new BusinessException("USER_NOT_FOUND", "用户不存在");
    }
    return user;
}
```

#### 日志规范

```java
@Slf4j
@Service
public class UserService {

    public User getUserById(Long id) {
        log.debug("查询用户，ID: {}", id);

        User user = userMapper.selectById(id);

        if (user == null) {
            log.warn("用户不存在，ID: {}", id);
            throw new BusinessException("USER_NOT_FOUND", "用户不存在");
        }

        log.info("查询用户成功，ID: {}, 用户名: {}", id, user.getUsername());
        return user;
    }
}
```

### Vue代码规范

#### 组件命名

```vue
<!-- 文件名：大驼峰 -->
<!-- UserProfile.vue -->

<script setup lang="ts">
// 组件名：大驼峰
defineOptions({
  name: 'UserProfile'
})
</script>
```

#### TypeScript类型定义

```typescript
// 定义接口
interface User {
  id: number
  username: string
  email?: string  // 可选属性
}

// 定义类型
type UserRole = 'ADMIN' | 'USER' | 'GUEST'

// 定义枚举
enum UserStatus {
  ACTIVE = 1,
  DISABLED = 0
}
```

#### 组件写法

```vue
<script setup lang="ts">
import { ref, computed } from 'vue'
import type { User } from '@/types/user'

// 定义Props
interface Props {
  userId: number
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
const displayName = computed(() => {
  return user.value ? user.value.username : '未知用户'
})

// 方法
const fetchUser = async () => {
  loading.value = true
  try {
    user.value = await api.getUser(props.userId)
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(() => {
  fetchUser()
})
</script>

<template>
  <div class="user-profile">
    <el-card v-loading="loading">
      <h2>{{ displayName }}</h2>
    </el-card>
  </div>
</template>

<style scoped lang="less">
.user-profile {
  padding: 20px;
}
</style>
```

---

## 🔧 开发指南

### 后端开发

#### 添加新的Controller

```java
@Slf4j
@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/{id}")
    public Result<Product> getProduct(@PathVariable Long id) {
        log.info("查询产品，ID: {}", id);
        return productService.getById(id);
    }

    @PostMapping
    public Result<String> addProduct(@RequestBody @Valid ProductDTO dto) {
        log.info("添加产品: {}", dto.getName());
        return productService.add(dto);
    }
}
```

#### 添加新的Service

```java
// 1. 定义接口
public interface ProductService {
    Result<Product> getById(Long id);
    Result<String> add(ProductDTO dto);
}

// 2. 实现接口
@Service
@Slf4j
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product>
        implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public Result<Product> getById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return Result.error("产品不存在");
        }
        return Result.success(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> add(ProductDTO dto) {
        // 业务逻辑
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        product.setCreateTime(LocalDateTime.now());

        boolean success = productMapper.insert(product);
        if (!success) {
            throw new BusinessException("添加产品失败");
        }

        return Result.success(product.getId().toString());
    }
}
```

#### 添加新的Mapper

```java
// 1. 定义接口
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @Select("SELECT * FROM product WHERE status = 1 ORDER BY create_time DESC LIMIT #{limit}")
    List<Product> selectLatestProducts(@Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM product WHERE category_id = #{categoryId}")
    int countByCategory(@Param("categoryId") Long categoryId);
}

// 2. 可选：定义XML映射
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.bankshield.api.mapper.ProductMapper">
    <select id="selectByCategory" resultType="com.bankshield.api.entity.Product">
        SELECT * FROM product
        WHERE category_id = #{categoryId}
        ORDER BY create_time DESC
    </select>
</mapper>
```

### 前端开发

#### 添加新的页面

```vue
<template>
  <div class="product-list">
    <el-card>
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="产品名称">
          <el-input v-model="queryForm.name" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="id" label="ID" />
        <el-table-column prop="name" label="产品名称" />
        <el-table-column prop="price" label="价格" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        @current-change="handlePageChange"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as api from '@/api/product'
import type { Product, QueryForm } from '@/types/product'

const loading = ref(false)
const tableData = ref<Product[]>([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const queryForm = ref<QueryForm>({
  name: '',
  status: null
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await api.getProductList({
      page: page.value,
      size: size.value,
      ...queryForm.value
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  page.value = 1
  fetchData()
}

const handleReset = () => {
  queryForm.value = {
    name: '',
    status: null
  }
  handleQuery()
}

const handleEdit = (row: Product) => {
  // 打开编辑对话框
  console.log('编辑', row)
}

const handleDelete = async (row: Product) => {
  await ElMessageBox.confirm('确认删除该产品吗？', '提示', {
    type: 'warning'
  })
  await api.deleteProduct(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

const handlePageChange = () => {
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="less">
.product-list {
  padding: 20px;
}
</style>
```

#### 添加新的API

```typescript
// bankshield-ui/src/api/product.ts
import request from '@/utils/request'
import type { Product, ProductDTO } from '@/types/product'

export const getProductList = (params: any) => {
  return request.get('/product/page', { params })
}

export const getProductById = (id: number) => {
  return request.get<Product>(`/product/${id}`)
}

export const addProduct = (data: ProductDTO) => {
  return request.post('/product', data)
}

export const updateProduct = (data: ProductDTO) => {
  return request.put('/product', data)
}

export const deleteProduct = (id: number) => {
  return request.delete(`/product/${id}`)
}
```

---

## 🧪 测试指南

### 单元测试

#### 后端单元测试

```java
@SpringBootTest
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testGetUserById() {
        // Arrange (准备)
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername("test");
        when(userMapper.selectById(userId)).thenReturn(mockUser);

        // Act (执行)
        Result<User> result = userService.getById(userId);

        // Assert (断言)
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("test", result.getData().getUsername());

        // 验证Mock调用
        verify(userMapper, times(1)).selectById(userId);
    }

    @Test
    void testGetUserById_NotFound() {
        // Arrange
        Long userId = 999L;
        when(userMapper.selectById(userId)).thenReturn(null);

        // Act
        Result<User> result = userService.getById(userId);

        // Assert
        assertEquals(404, result.getCode());
        verify(userMapper, times(1)).selectById(userId);
    }
}
```

#### 前端单元测试

```typescript
// UserList.test.ts
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import UserList from '@/views/user/UserList.vue'
import * as api from '@/api/user'

vi.mock('@/api/user')

describe('UserList', () => {
  it('should render user list', async () => {
    // Mock API
    vi.spyOn(api, 'getUserList').mockResolvedValue({
      data: {
        records: [{ id: 1, username: 'test' }],
        total: 1
      }
    })

    const wrapper = mount(UserList)

    // 等待异步加载
    await wrapper.vm.$nextTick()

    // 断言
    expect(wrapper.text()).toContain('test')
  })

  it('should handle delete', async () => {
    const wrapper = mount(UserList)
    const deleteSpy = vi.spyOn(wrapper.vm, 'handleDelete')

    // 触发删除
    await wrapper.find('.delete-btn').trigger('click')

    expect(deleteSpy).toHaveBeenCalled()
  })
})
```

### 集成测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockDatabase
class UserControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testCreateUser() {
        // Arrange
        UserDTO dto = new UserDTO();
        dto.setUsername("testuser");
        dto.setPassword("123456");

        // Act
        ResponseEntity<Result> response = restTemplate.postForEntity(
            "/api/user", dto, Result.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getCode());
    }
}
```

### E2E测试

```javascript
// cypress/integration/user.spec.js
describe('用户管理', () => {
  beforeEach(() => {
    cy.visit('/login')
    cy.get('[data-test="username"]').type('admin')
    cy.get('[data-test="password"]').type('123456')
    cy.get('[data-test="login-button"]').click()
    cy.url().should('include', '/dashboard')
  })

  it('应该成功添加用户', () => {
    cy.visit('/user/list')
    cy.get('[data-test="add-button"]').click()
    cy.get('[data-test="username-input"]').type('newuser')
    cy.get('[data-test="password-input"]').type('123456')
    cy.get('[data-test="submit-button"]').click()
    cy.get('[data-test="success-message"]').should('be.visible')
  })

  it('应该成功删除用户', () => {
    cy.visit('/user/list')
    cy.get('[data-test="user-row-1"]').find('.delete-button').click()
    cy.get('[data-test="confirm-button"]').click()
    cy.get('[data-test="success-message"]').should('be.visible')
  })
})
```

---

## 🔍 调试技巧

### 后端调试

#### IDEA断点调试

```bash
# 1. 设置断点
# 在代码行号左侧点击，出现红点

# 2. 启动调试模式
# 点击IDEA右上角的Debug按钮

# 3. 访问接口
# curl http://localhost:8080/api/user/1

# 4. 断点触发，开始调试
# - Step Over (F8): 单步执行
# - Step Into (F7): 进入方法
# - Step Out (Shift+F8): 跳出方法
# - Resume (F9): 继续执行
```

#### 远程调试

```bash
# 1. 启动应用时添加调试参数
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 \
  -jar bankshield-api.jar

# 2. 配置IDEA远程调试
# Run → Edit Configurations → + → Remote
# Host: localhost
# Port: 5005

# 3. 启动远程调试
# 点击Debug按钮开始调试
```

### 前端调试

#### Chrome DevTools

```javascript
// 1. 打开Chrome DevTools
// F12 或 Ctrl+Shift+I

// 2. 使用Console
console.log('调试信息', data)

// 3. 使用断点
// 在Source中点击行号设置断点

// 4. 使用Vue DevTools
// 安装Vue.js devtools扩展
// 在Vue标签页查看组件状态
```

#### Network调试

```javascript
// 查看网络请求
// Network标签页

// 过滤请求
// 输入关键词过滤

// 查看请求详情
// 点击请求查看Headers、Payload、Response

// 重放请求
// 右键 → Replay XHR
```

---

## ❓ 常见问题

### 后端问题

**Q: Maven依赖下载慢？**

```bash
# 使用阿里云镜像
# ~/.m2/settings.xml
<mirror>
  <id>aliyun</id>
  <mirrorOf>central</mirrorOf>
  <url>https://maven.aliyun.com/repository/public</url>
</mirror>
```

**Q: 端口被占用？**

```bash
# 查找占用进程
lsof -i :8080

# 杀死进程
kill -9 <PID>

# 或修改端口
# application.yml
server:
  port: 8081
```

**Q: MyBatis XML找不到？**

```bash
# 检查pom.xml
<build>
  <resources>
    <resource>
      <directory>src/main/resources</directory>
      <includes>
        <include>**/*.xml</include>
      </includes>
    </resource>
  </resources>
</build>

# 重新编译
mvn clean compile
```

### 前端问题

**Q: npm install失败？**

```bash
# 清除缓存
rm -rf node_modules package-lock.json
npm cache clean --force

# 使用淘宝镜像
npm config set registry https://registry.npmmirror.com

# 重新安装
npm install
```

**Q: TypeScript类型错误？**

```bash
# 检查tsconfig.json
{
  "compilerOptions": {
    "strict": false,  // 临时关闭严格模式
    "skipLibCheck": true  // 跳过库检查
  }
}
```

**Q: 样式不生效？**

```vue
<!-- 确保使用scoped -->
<style scoped lang="less">
.user-profile {
  /* 样式代码 */
}
</style>

<!-- 或使用CSS Modules -->
<style module>
.userProfile {
  /* 样式代码 */
}
</style>
```

---

## ✅ 最佳实践

### 代码组织

```
# 后端结构
com.bankshield.api/
├── controller/          # 控制器层
│   └── UserController.java
├── service/            # 服务层
│   ├── UserService.java           # 接口
│   └── impl/
│       └── UserServiceImpl.java   # 实现
├── mapper/             # 数据访问层
│   └── UserMapper.java
├── entity/             # 实体类
│   └── User.java
└── dto/                # 数据传输对象
    └── UserDTO.java
```

```bash
# 前端结构
src/
├── api/               # API接口
│   └── user.ts
├── components/         # 公共组件
│   └── UserProfile.vue
├── views/             # 页面组件
│   └── user/
│       └── UserList.vue
├── store/             # 状态管理
│   └── user.ts
├── router/            # 路由配置
│   └── index.ts
├── utils/             # 工具函数
│   └── request.ts
└── types/             # 类型定义
    └── user.ts
```

### 性能优化

```java
// 1. 使用缓存
@Cacheable(value = "user", key = "#id")
public User getUserById(Long id) {
    return userMapper.selectById(id);
}

// 2. 批量查询
// 不推荐
for (Long id : ids) {
    User user = userMapper.selectById(id);
}

// 推荐
List<User> users = userMapper.selectBatchIds(ids);

// 3. 使用异步
@Async
public CompletableFuture<String> asyncMethod() {
    // 异步处理
}
```

```typescript
// 1. 使用computed缓存
const filteredData = computed(() => {
  return tableData.value.filter(item => item.status === 1)
})

// 2. 使用防抖
import { debounce } from 'lodash-es'

const handleSearch = debounce((keyword: string) => {
  fetchData(keyword)
}, 300)

// 3. 使用虚拟滚动
<el-table-v2 :data="largeData" />
```

---

## 📞 技术支持

- **技术支持**: tech-support@bankshield.com
- **代码审查**: code-review@bankshield.com
- **培训文档**: training@bankshield.com

---

**文档版本**: v1.0.0
**最后更新**: 2025-12-25
