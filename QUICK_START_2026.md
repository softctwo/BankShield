# BankShield 快速开始指南 2026

**更新时间**: 2026-01-06  
**适用版本**: v1.0.0-SNAPSHOT

---

## 📋 目录

1. [环境准备](#环境准备)
2. [快速启动](#快速启动)
3. [功能验证](#功能验证)
4. [常见问题](#常见问题)
5. [下一步](#下一步)

---

## 🔧 环境准备

### 必需软件

| 软件 | 版本要求 | 下载地址 | 说明 |
|------|---------|---------|------|
| **JDK** | 17+ | https://www.oracle.com/java/technologies/downloads/ | 已从1.8升级 |
| **Node.js** | 16+ | https://nodejs.org/ | 包含npm |
| **MySQL** | 8.0+ | https://dev.mysql.com/downloads/ | 数据库 |
| **Redis** | 6.0+ | https://redis.io/download | 缓存 |
| **Maven** | 3.6+ | https://maven.apache.org/download.cgi | 构建工具 |

### 可选软件

| 软件 | 版本要求 | 用途 |
|------|---------|------|
| **Docker** | 20.10+ | 容器化部署 |
| **Git** | 2.0+ | 版本控制 |
| **IntelliJ IDEA** | 2023+ | 开发IDE |
| **VS Code** | 最新版 | 前端开发 |

### 环境验证

```bash
# 验证Java版本
java -version
# 应显示: java version "17.x.x"

# 验证Node.js版本
node -v
# 应显示: v16.x.x 或更高

# 验证Maven版本
mvn -v
# 应显示: Apache Maven 3.6.x 或更高

# 验证MySQL
mysql --version
# 应显示: mysql  Ver 8.0.x

# 验证Redis
redis-server --version
# 应显示: Redis server v=6.x.x
```

---

## 🚀 快速启动

### 方式一：自动启动脚本（推荐）⭐

```bash
# 1. 克隆项目
git clone https://github.com/softctwo/BankShield.git
cd BankShield

# 2. 启动开发环境（自动构建并启动）
./scripts/start.sh --dev

# 3. 等待启动完成（约2-3分钟）
# 前端: http://localhost:3000
# 后端: http://localhost:8081 (需要先修复编译问题)
```

**启动脚本选项**:
```bash
./scripts/start.sh --dev      # 开发环境
./scripts/start.sh --prod     # 生产环境
./scripts/start.sh --build    # 仅构建
./scripts/start.sh --stop     # 停止服务
./scripts/start.sh --help     # 查看帮助
```

### 方式二：手动启动

#### 步骤1: 数据库初始化

```bash
# 1. 启动MySQL服务
# macOS: brew services start mysql
# Linux: sudo systemctl start mysql
# Windows: 从服务管理器启动

# 2. 创建数据库
mysql -u root -p3f342bb206

# 3. 执行初始化脚本
mysql -u root -p3f342bb206 < sql/init_database.sql

# 4. 验证数据库
mysql -u root -p3f342bb206 -e "SHOW DATABASES LIKE 'bankshield%';"
```

#### 步骤2: 启动Redis

```bash
# macOS
brew services start redis

# Linux
sudo systemctl start redis

# Windows
redis-server

# 验证Redis
redis-cli ping
# 应返回: PONG
```

#### 步骤3: 编译公共模块

```bash
# 编译bankshield-common模块
cd bankshield-common
mvn clean install -DskipTests

# 验证编译结果
ls -la target/*.jar
# 应看到: bankshield-common-1.0.0-SNAPSHOT.jar
```

#### 步骤4: 启动后端（需要先修复编译问题）

```bash
cd ../bankshield-api

# 方式A: 使用Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 方式B: 使用JAR包
mvn clean package -DskipTests
java -jar target/bankshield-api-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev
```

**注意**: 目前后端有约100个编译错误，需要先修复才能启动。

#### 步骤5: 启动前端

```bash
cd ../bankshield-ui

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run dev

# 等待编译完成
# 访问: http://localhost:3000
```

### 方式三：Docker部署

```bash
# 1. 构建镜像
docker-compose -f docker/docker-compose.yml build

# 2. 启动服务
docker-compose -f docker/docker-compose.yml up -d

# 3. 查看状态
docker-compose -f docker/docker-compose.yml ps

# 4. 查看日志
docker-compose -f docker/docker-compose.yml logs -f
```

---

## ✅ 功能验证

### 1. 前端访问验证

```bash
# 打开浏览器访问
open http://localhost:3000

# 或使用curl测试
curl -I http://localhost:3000
# 应返回: HTTP/1.1 200 OK
```

**验证内容**:
- ✅ 登录页面正常显示
- ✅ 可以查看各功能模块菜单
- ✅ 页面样式正常
- ✅ 路由跳转正常

### 2. 后端API验证（待修复后）

```bash
# 健康检查
curl http://localhost:8081/actuator/health

# Swagger文档
open http://localhost:8081/swagger-ui.html

# Druid监控
open http://localhost:8081/druid
# 账号: admin / 123456
```

### 3. 数据库验证

```bash
# 连接数据库
mysql -u root -p3f342bb206

# 查看数据库
SHOW DATABASES LIKE 'bankshield%';

# 查看表
USE bankshield;
SHOW TABLES;

# 查看菜单数据
SELECT * FROM sys_menu LIMIT 5;
```

### 4. Redis验证

```bash
# 连接Redis
redis-cli

# 查看所有键
KEYS *

# 测试读写
SET test "hello"
GET test
```

---

## 🐛 常见问题

### 问题1: 端口被占用

**错误信息**: `Address already in use: bind`

**解决方案**:
```bash
# 查看端口占用
lsof -i :3000  # 前端
lsof -i :8081  # 后端

# 杀死进程
kill -9 <PID>

# 或修改配置文件中的端口
```

### 问题2: MySQL连接失败

**错误信息**: `Access denied for user 'root'@'localhost'`

**解决方案**:
```bash
# 重置MySQL密码
mysql -u root -p

# 修改配置文件
vim bankshield-api/src/main/resources/application-dev.yml
# 更新数据库密码
```

### 问题3: 后端编译失败

**错误信息**: `Compilation failure: 100 errors`

**解决方案**:
```bash
# 查看详细错误
cd bankshield-api
mvn compile 2>&1 | grep ERROR | head -20

# 主要是Service层方法缺失，需要实现
# 参考: FINAL_FIX_COMPLETION_REPORT.md
```

### 问题4: 前端依赖安装失败

**错误信息**: `npm ERR! code ECONNREFUSED`

**解决方案**:
```bash
# 清理缓存
npm cache clean --force

# 使用国内镜像
npm config set registry https://registry.npmmirror.com

# 重新安装
npm install
```

### 问题5: Java版本不匹配

**错误信息**: `Unsupported class file major version 61`

**解决方案**:
```bash
# 检查Java版本
java -version

# 安装JDK 17
# macOS: brew install openjdk@17
# Linux: sudo apt install openjdk-17-jdk

# 设置JAVA_HOME
export JAVA_HOME=/path/to/jdk-17
```

---

## 📝 默认账号

### 系统账号

| 角色 | 用户名 | 密码 | 权限 |
|------|--------|------|------|
| 超级管理员 | admin | 123456 | 全部权限 |
| 安全管理员 | security | 123456 | 安全相关 |
| 审计管理员 | audit | 123456 | 审计相关 |

### 数据库账号

| 服务 | 用户名 | 密码 |
|------|--------|------|
| MySQL | root | 3f342bb206 |
| Redis | - | 无密码 |

### 监控账号

| 服务 | 用户名 | 密码 |
|------|--------|------|
| Druid | admin | 123456 |

---

## 🎯 下一步

### 1. 修复后端编译问题（优先）

```bash
# 查看编译错误
cd bankshield-api
mvn compile 2>&1 | tee compile-errors.log

# 主要问题:
# - DesensitizationService缺失方法
# - SecurityEventDTO getter/setter缺失
# - 约100个编译错误

# 建议使用IDE自动修复
# 预计时间: 1-2小时
```

### 2. 功能测试

- [ ] 用户登录
- [ ] 数据加密
- [ ] 访问控制
- [ ] 审计日志
- [ ] 数据脱敏
- [ ] 合规检查

### 3. 性能优化

- [ ] 数据库查询优化
- [ ] 缓存策略
- [ ] 接口响应时间
- [ ] 前端资源加载

### 4. 安全加固

- [ ] SQL注入防护
- [ ] XSS攻击防护
- [ ] CSRF防护
- [ ] 敏感信息加密

### 5. 文档完善

- [ ] API接口文档
- [ ] 部署运维文档
- [ ] 用户操作手册
- [ ] 开发规范文档

---

## 📚 相关文档

- 📊 [项目状态报告](PROJECT_STATUS_2026.md)
- 🏗️ [系统架构](docs/architecture/README.md)
- 📖 [开发指南](AGENTS.md)
- 🔐 [安全规范](docs/BEST_PRACTICES.md)
- 📋 [部署指南](DEPLOYMENT_GUIDE.md)
- 💾 [数据库设计](sql/)

---

## 🆘 获取帮助

### 在线资源

- **项目地址**: https://github.com/softctwo/BankShield
- **问题反馈**: https://github.com/softctwo/BankShield/issues
- **文档中心**: [docs/](docs/)

### 技术支持

如遇到问题，请按以下顺序尝试：

1. 查看本文档的[常见问题](#常见问题)部分
2. 搜索项目的[Issues](https://github.com/softctwo/BankShield/issues)
3. 查看详细的[项目状态报告](PROJECT_STATUS_2026.md)
4. 提交新的Issue并附上详细信息

---

**最后更新**: 2026-01-06  
**文档版本**: v1.0  
**维护团队**: BankShield Development Team
