# BankShield 依赖配置指南

## 📦 需要添加的依赖

### 1. Redis依赖（高性能缓存）

在 `bankshield-api/pom.xml` 中添加：

```xml
<!-- Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- Redisson（分布式锁和限流） -->
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.17.7</version>
</dependency>
```

### 2. WebSocket依赖（实时推送）

```xml
<!-- WebSocket -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>

<!-- STOMP协议支持 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-reactor-netty</artifactId>
</dependency>
```

### 3. PDF生成依赖（报告生成）

```xml
<!-- iText PDF（推荐） -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itextpdf</artifactId>
    <version>5.5.13.3</version>
</dependency>

<!-- iText中文字体支持 -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext-asian</artifactId>
    <version>5.2.0</version>
</dependency>
```

### 4. 测试依赖（单元测试和集成测试）

```xml
<!-- Spring Boot Test -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- JUnit 5 -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<!-- Mockito -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>
```

---

## ⚙️ 配置文件

### application.yml 配置

```yaml
spring:
  # Redis配置
  redis:
    host: localhost
    port: 6379
    password: 3f342bb206
    database: 0
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms
    timeout: 3000ms
  
  # 数据源配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/bankshield?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 3f342bb206
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
  
  # 国际化配置
  messages:
    basename: i18n/messages
    encoding: UTF-8
    cache-duration: 3600
  
  # WebSocket配置
  websocket:
    allowed-origins: "*"
    endpoint: /ws
    message-size-limit: 8192
    send-time-limit: 20000
    send-buffer-size-limit: 524288

# MyBatis Plus配置
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.bankshield.api.entity
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: true
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

# 缓存配置
cache:
  type: redis
  redis:
    time-to-live: 1800000  # 30分钟
    key-prefix: bankshield:
    use-key-prefix: true
    cache-null-values: false

# PDF报告配置
report:
  pdf:
    output-dir: ./reports/pdf
    temp-dir: ./reports/temp
    font-path: /fonts/SimSun.ttf
    max-file-size: 10485760  # 10MB

# 日志配置
logging:
  level:
    root: INFO
    com.bankshield: DEBUG
    org.springframework.web: INFO
    org.mybatis: DEBUG
  file:
    name: logs/bankshield.log
    max-size: 100MB
    max-history: 30
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{50} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{50} - %msg%n"
```

---

## 🗄️ 数据库初始化

### 1. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS bankshield 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

USE bankshield;
```

### 2. 执行初始化脚本

```bash
# 按顺序执行以下SQL脚本
mysql -u root -p3f342bb206 bankshield < sql/init_database.sql
mysql -u root -p3f342bb206 bankshield < sql/compliance_check.sql
mysql -u root -p3f342bb206 bankshield < sql/security_threat.sql
```

---

## 🚀 启动顺序

### 1. 启动基础设施

```bash
# 启动MySQL
brew services start mysql

# 启动Redis
brew services start redis

# 验证服务
mysql -u root -p3f342bb206 -e "SELECT 1"
redis-cli ping
```

### 2. 启动后端服务

```bash
cd bankshield-api
mvn clean install -DskipTests
mvn spring-boot:run
```

### 3. 启动前端应用

```bash
cd bankshield-ui
npm install
npm run dev
```

---

## 🔧 常见问题

### 问题1: Redis连接失败

**错误信息**: `Cannot get Jedis connection`

**解决方案**:
```bash
# 检查Redis是否启动
redis-cli ping

# 如果未启动，启动Redis
brew services start redis

# 检查端口是否被占用
lsof -i :6379
```

### 问题2: MySQL连接失败

**错误信息**: `Access denied for user 'root'@'localhost'`

**解决方案**:
```bash
# 重置MySQL密码
mysql -u root -p
ALTER USER 'root'@'localhost' IDENTIFIED BY '3f342bb206';
FLUSH PRIVILEGES;
```

### 问题3: 依赖下载失败

**解决方案**:
```bash
# 清理Maven缓存
mvn clean
rm -rf ~/.m2/repository

# 使用阿里云镜像
# 在 ~/.m2/settings.xml 中添加：
<mirror>
  <id>aliyun</id>
  <mirrorOf>central</mirrorOf>
  <name>Aliyun Maven</name>
  <url>https://maven.aliyun.com/repository/public</url>
</mirror>
```

---

## 📊 性能优化建议

### 1. Redis配置优化

```yaml
spring:
  redis:
    lettuce:
      pool:
        max-active: 20      # 根据并发量调整
        max-idle: 10
        min-idle: 5
    timeout: 5000ms         # 增加超时时间
```

### 2. 数据库连接池优化

```yaml
spring:
  datasource:
    druid:
      initial-size: 10      # 初始连接数
      min-idle: 10          # 最小空闲连接
      max-active: 50        # 最大活跃连接
      max-wait: 60000       # 最大等待时间
```

### 3. JVM参数优化

```bash
java -Xms2g -Xmx4g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+HeapDumpOnOutOfMemoryError \
     -jar bankshield-api.jar
```

---

## 🔒 安全配置

### 1. Redis密码配置

```yaml
spring:
  redis:
    password: ${REDIS_PASSWORD:3f342bb206}
```

### 2. 数据库密码加密

使用Jasypt加密敏感信息：

```xml
<dependency>
    <groupId>com.github.ulisesbocchio</groupId>
    <artifactId>jasypt-spring-boot-starter</artifactId>
    <version>3.0.5</version>
</dependency>
```

```yaml
spring:
  datasource:
    password: ENC(加密后的密码)

jasypt:
  encryptor:
    password: ${JASYPT_PASSWORD}
```

---

## 📝 环境变量配置

创建 `.env` 文件：

```bash
# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=bankshield
DB_USER=root
DB_PASSWORD=3f342bb206

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=3f342bb206

# 应用配置
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev

# 日志级别
LOG_LEVEL=DEBUG
```

---

**文档版本**: v1.0  
**最后更新**: 2025-01-04  
**维护者**: BankShield Team
