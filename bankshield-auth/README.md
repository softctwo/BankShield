# BankShield Authentication Module

银行数据安全管理系统的认证授权模块，基于Spring Boot + Spring Security + JWT实现。

## 功能特性

- **用户认证**: 支持用户注册和登录
- **JWT令牌**: 基于JWT的无状态认证机制
- **Spring Security**: 强大的安全框架集成
- **密码加密**: 使用BCrypt进行密码加密
- **用户管理**: 用户信息的增删改查
- **角色权限**: 基于角色的访问控制

## 技术栈

- Spring Boot 2.7.18
- Spring Security
- JWT (JSON Web Token)
- Spring Data JPA
- MySQL 8.0
- Maven 3.x

## 快速开始

### 1. 数据库配置

在MySQL中创建数据库：
```sql
CREATE DATABASE bankshield_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 配置文件

修改 `src/main/resources/application.yml` 中的数据库连接配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bankshield_auth?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: your_username
    password: your_password
```

### 3. 启动应用

```bash
mvn spring-boot:run
```

应用将在 http://localhost:8081/auth 启动

## API接口

### 用户注册
```http
POST /api/auth/register
Content-Type: application/json

{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "fullName": "Test User"
}
```

### 用户登录
```http
POST /api/auth/login
Content-Type: application/json

{
    "username": "testuser",
    "password": "password123"
}
```

### 验证令牌
```http
GET /api/auth/verify
Authorization: Bearer <your_jwt_token>
```

### 用户登出
```http
POST /api/auth/logout
Authorization: Bearer <your_jwt_token>
```

## 响应格式

### 成功响应
```json
{
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "message": "Login successful",
    "success": true
}
```

### 错误响应
```json
{
    "token": null,
    "message": "Invalid credentials",
    "success": false
}
```

## 安全配置

- JWT密钥和过期时间在 `application.yml` 中配置
- 密码使用BCrypt加密
- 支持CORS跨域请求
- 基于角色的访问控制

## 模块结构

```
bankshield-auth/
├── src/main/java/com/bankshield/auth/
│   ├── config/          # 安全配置
│   ├── controller/      # REST控制器
│   ├── dto/            # 数据传输对象
│   ├── model/          # 实体类
│   ├── repository/     # 数据访问层
│   ├── service/        # 业务逻辑层
│   └── util/           # 工具类
├── src/main/resources/
│   └── application.yml # 配置文件
└── pom.xml             # Maven配置
```

## 开发说明

### 添加新的API端点

1. 在 `controller` 包中创建新的控制器
2. 在 `service` 包中实现业务逻辑
3. 在 `config/SecurityConfig.java` 中配置访问权限

### 自定义JWT配置

在 `application.yml` 中修改以下配置：

```yaml
app:
  jwt:
    secret: your-secret-key-here
    expiration: 86400000  # 24小时，单位毫秒
```

## 测试

运行单元测试：
```bash
mvn test
```

运行集成测试：
```bash
mvn verify
```

## 注意事项

1. 生产环境中请使用强密钥
2. 定期更新JWT密钥
3. 启用HTTPS以确保安全传输
4. 配置适当的数据库连接池
5. 启用监控和日志记录

## 故障排除

### 数据库连接失败
- 检查MySQL服务是否运行
- 验证数据库连接配置
- 确保数据库存在

### JWT验证失败
- 检查JWT密钥配置
- 验证令牌是否过期
- 检查令牌格式是否正确

### 编译错误
- 确保使用Java 8或更高版本
- 检查Maven依赖是否正确下载
- 清理并重新编译项目