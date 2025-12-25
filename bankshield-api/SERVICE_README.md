# BankShield用户管理Service层说明

## 概述
本文档描述了BankShield系统中用户管理相关的Service层接口和实现类，包括用户管理、角色管理和部门管理功能。

## 模块结构
```
bankshield-api/src/main/java/com/bankshield/api/
├── service/                    # Service接口层
│   ├── UserService.java       # 用户服务接口
│   ├── RoleService.java       # 角色服务接口
│   └── DeptService.java       # 部门服务接口
├── service/impl/              # Service实现层
│   ├── UserServiceImpl.java   # 用户服务实现
│   ├── RoleServiceImpl.java   # 角色服务实现
│   └── DeptServiceImpl.java   # 部门服务实现
├── mapper/                    # MyBatis Mapper接口
│   ├── UserMapper.java        # 用户Mapper
│   ├── RoleMapper.java        # 角色Mapper
│   └── DeptMapper.java        # 部门Mapper
└── controller/                # 控制器层
    ├── UserController.java    # 用户管理控制器
    ├── RoleController.java    # 角色管理控制器
    └── DeptController.java    # 部门管理控制器
```

## 功能特性

### 1. 用户管理 (UserService)
- **用户CRUD操作**: 创建、读取、更新、删除用户
- **分页查询**: 支持按用户名和部门ID筛选的分页查询
- **密码安全**: 使用BCrypt算法进行密码加密和验证
- **用户登录**: 提供用户登录验证功能
- **数据校验**: 完整的参数校验和业务逻辑验证

#### 主要方法
```java
Result<User> getUserById(Long id)                    // 根据ID获取用户信息
Result<Page<User>> getUserPage(int page, int size, String username, Long deptId)  // 分页查询用户
Result<String> addUser(User user)                    // 添加用户
Result<String> updateUser(User user)                 // 更新用户信息
Result<String> deleteUser(Long id)                   // 删除用户
Result<String> login(String username, String password) // 用户登录
```

### 2. 角色管理 (RoleService)
- **角色CRUD操作**: 创建、读取、更新、删除角色
- **分页查询**: 支持按角色名称和角色编码筛选的分页查询
- **启用角色列表**: 获取所有启用的角色
- **唯一性校验**: 角色编码的唯一性验证

#### 主要方法
```java
Result<Role> getRoleById(Long id)                    // 根据ID获取角色信息
Result<Page<Role>> getRolePage(int page, int size, String roleName, String roleCode)  // 分页查询角色
Result<List<Role>> getAllEnabledRoles()             // 获取所有启用的角色
Result<String> addRole(Role role)                    // 添加角色
Result<String> updateRole(Role role)                 // 更新角色信息
Result<String> deleteRole(Long id)                   // 删除角色
```

### 3. 部门管理 (DeptService)
- **部门CRUD操作**: 创建、读取、更新、删除部门
- **分页查询**: 支持按部门名称和部门编码筛选的分页查询
- **树形结构**: 支持部门树形结构查询
- **层级管理**: 支持多级部门结构
- **完整性校验**: 删除部门前的关联性检查

#### 主要方法
```java
Result<Dept> getDeptById(Long id)                    // 根据ID获取部门信息
Result<Page<Dept>> getDeptPage(int page, int size, String deptName, String deptCode)  // 分页查询部门
Result<List<Dept>> getDeptTree()                    // 获取部门树形结构
Result<String> addDept(Dept dept)                    // 添加部门
Result<String> updateDept(Dept dept)                 // 更新部门信息
Result<String> deleteDept(Long id)                   // 删除部门
```

## 技术特点

### 1. 安全性
- **密码加密**: 使用BCrypt算法进行密码加密，保证密码安全
- **参数校验**: 所有接口都有完整的参数校验
- **事务管理**: 使用Spring事务管理确保数据一致性

### 2. 性能优化
- **分页查询**: 所有列表查询都支持分页
- **索引优化**: 数据库查询使用合适的索引字段
- **缓存友好**: 设计支持后续Redis缓存扩展

### 3. 代码规范
- **接口隔离**: 每个Service都有清晰的职责划分
- **异常处理**: 统一的异常处理和错误返回
- **日志记录**: 完整的操作日志记录
- **代码复用**: 使用MyBatis-Plus的ServiceImpl减少重复代码

## 使用示例

### 1. 添加用户
```java
User user = User.builder()
    .username("testuser")
    .password("123456")
    .name("测试用户")
    .phone("13800138000")
    .email("test@bankshield.com")
    .deptId(1L)
    .status(1)
    .build();

Result<String> result = userService.addUser(user);
if (result.isSuccess()) {
    System.out.println("添加用户成功");
}
```

### 2. 用户登录
```java
Result<String> result = userService.login("testuser", "123456");
if (result.isSuccess()) {
    System.out.println("登录成功: " + result.getMessage());
} else {
    System.out.println("登录失败: " + result.getMessage());
}
```

### 3. 分页查询用户
```java
Result<Page<User>> result = userService.getUserPage(1, 10, "test", 1L);
if (result.isSuccess()) {
    Page<User> userPage = result.getResult();
    System.out.println("总记录数: " + userPage.getTotal());
    System.out.println("当前页数据: " + userPage.getRecords());
}
```

### 4. 获取部门树
```java
Result<List<Dept>> result = deptService.getDeptTree();
if (result.isSuccess()) {
    List<Dept> deptTree = result.getResult();
    System.out.println("部门树: " + deptTree);
}
```

## API接口

### 用户管理接口
- `GET /api/user/{id}` - 根据ID获取用户信息
- `GET /api/user/page` - 分页查询用户列表
- `POST /api/user` - 添加用户
- `PUT /api/user` - 更新用户信息
- `DELETE /api/user/{id}` - 删除用户
- `POST /api/user/login` - 用户登录

### 角色管理接口
- `GET /api/role/{id}` - 根据ID获取角色信息
- `GET /api/role/page` - 分页查询角色列表
- `GET /api/role/enabled` - 获取所有启用的角色
- `POST /api/role` - 添加角色
- `PUT /api/role` - 更新角色信息
- `DELETE /api/role/{id}` - 删除角色

### 部门管理接口
- `GET /api/dept/{id}` - 根据ID获取部门信息
- `GET /api/dept/page` - 分页查询部门列表
- `GET /api/dept/tree` - 获取部门树形结构
- `POST /api/dept` - 添加部门
- `PUT /api/dept` - 更新部门信息
- `DELETE /api/dept/{id}` - 删除部门

## 注意事项

1. **密码安全**: 所有密码都使用BCrypt加密存储，不可逆
2. **事务管理**: 所有写操作都使用事务管理
3. **软删除**: 目前实现的是物理删除，后续可考虑软删除
4. **关联查询**: 用户-角色、角色-菜单等关联关系需要后续完善
5. **权限控制**: 需要结合Spring Security进行权限控制
6. **审计日志**: 重要操作需要记录审计日志

## 后续优化

1. **缓存支持**: 添加Redis缓存提高查询性能
2. **关联查询**: 完善用户-角色-部门的多表关联查询
3. **权限集成**: 集成Spring Security进行权限控制
4. **审计功能**: 添加操作审计和数据变更记录
5. **导入导出**: 支持用户数据的批量导入导出
6. **高级搜索**: 支持更多条件的组合搜索

## 测试数据

项目提供了测试数据初始化脚本：`/sql/test-data.sql`，包含：
- 4个测试部门（总行、技术部、财务部、风控部）
- 4个测试角色（超级管理员、系统管理员、业务操作员、审计员）
- 4个测试用户（admin、system、operator、auditor）

所有测试用户的密码都是：`123456`