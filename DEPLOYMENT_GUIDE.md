# BankShield 安全扫描模块部署指南

## 系统要求

### 硬件要求
- **CPU**: 4核及以上
- **内存**: 8GB及以上
- **硬盘**: 50GB可用空间
- **网络**: 千兆网络连接

### 软件要求
- **操作系统**: Linux/Windows/macOS
- **Java**: JDK 8或更高版本
- **Node.js**: 14.0或更高版本
- **MySQL**: 5.7或更高版本
- **Maven**: 3.6或更高版本
- **Redis**: 5.0或更高版本（可选，用于缓存）

## 环境准备

### 1. 安装Java环境
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-8-jdk

# CentOS/RHEL
sudo yum install java-1.8.0-openjdk-devel

# macOS (使用Homebrew)
brew install openjdk@8
```

验证安装：
```bash
java -version
javac -version
```

### 2. 安装Node.js环境
```bash
# 使用NodeSource安装最新版本
curl -fsSL https://deb.nodesource.com/setup_16.x | sudo -E bash -
sudo apt-get install -y nodejs

# 或使用nvm管理多个Node.js版本
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
nvm install 16
nvm use 16
```

验证安装：
```bash
node -v
npm -v
```

### 3. 安装MySQL数据库
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install mysql-server

# CentOS/RHEL
sudo yum install mysql-server
sudo systemctl start mysqld
sudo systemctl enable mysqld
```

配置数据库：
```sql
-- 登录MySQL
mysql -u root -p

-- 创建数据库
create database bankshield character set utf8mb4 collate utf8mb4_unicode_ci;

-- 创建用户并授权
create user 'bankshield'@'localhost' identified by 'bankshield123';
grant all privileges on bankshield.* to 'bankshield'@'localhost';
flush privileges;
```

### 4. 安装Maven
```bash
# Ubuntu/Debian
sudo apt install maven

# CentOS/RHEL
sudo yum install maven

# 或手动安装
wget https://archive.apache.org/dist/maven/maven-3/3.8.4/binaries/apache-maven-3.8.4-bin.tar.gz
tar -xzf apache-maven-3.8.4-bin.tar.gz
sudo mv apache-maven-3.8.4 /opt/maven
```

配置环境变量：
```bash
export MAVEN_HOME=/opt/maven
export PATH=$MAVEN_HOME/bin:$PATH
```

验证安装：
```bash
mvn -version
```

## 项目部署

### 1. 获取项目代码
```bash
# 克隆项目仓库
git clone https://github.com/your-org/BankShield.git
cd BankShield
```

### 2. 数据库初始化
```bash
# 使用提供的初始化脚本
./scripts/init-security-scan.sh

# 或手动执行SQL脚本
mysql -u bankshield -pbankshield123 bankshield < sql/security_scan_module.sql
```

### 3. 后端服务部署

#### 3.1 配置修改
编辑 `bankshield-api/src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    druid:
      url: jdbc:mysql://localhost:3306/bankshield?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
      username: bankshield
      password: bankshield123

# 其他配置根据实际环境调整
```

#### 3.2 构建项目
```bash
cd bankshield-api
mvn clean package -DskipTests
```

#### 3.3 启动服务
```bash
# 开发模式
mvn spring-boot:run

# 生产模式
java -jar target/bankshield-api-1.0.0.jar
```

### 4. 前端服务部署

#### 4.1 安装依赖
```bash
cd bankshield-ui
npm install
```

#### 4.2 配置修改
编辑 `bankshield-ui/.env.development`：
```
VITE_API_BASE_URL=http://localhost:8080/api
```

#### 4.3 构建项目
```bash
npm run build
```

#### 4.4 启动服务
```bash
# 开发模式
npm run dev

# 生产模式
npm run preview
```

## 系统验证

### 1. 服务状态检查
```bash
# 检查后端服务
curl http://localhost:8080/actuator/health

# 检查前端服务
curl http://localhost:3000
```

### 2. 功能验证

#### 2.1 用户登录
访问 `http://localhost:3000`
- 用户名：admin
- 密码：123456

#### 2.2 安全扫描功能
1. 创建扫描任务
2. 执行扫描任务
3. 查看扫描结果
4. 生成扫描报告

#### 2.3 基线配置功能
1. 查看安全基线
2. 启用/禁用基线检查项
3. 同步内置基线

## 生产环境部署

### 1. 反向代理配置（Nginx）
```nginx
server {
    listen 80;
    server_name bankshield.example.com;

    # 前端静态资源
    location / {
        root /var/www/bankshield-ui/dist;
        try_files $uri $uri/ /index.html;
    }

    # API代理
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # WebSocket支持（如需要）
    location /ws/ {
        proxy_pass http://localhost:8080/ws/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

### 2. SSL证书配置
```bash
# 使用Let's Encrypt获取免费证书
certbot --nginx -d bankshield.example.com
```

### 3. 系统服务配置

#### 3.1 创建系统服务（systemd）
创建 `/etc/systemd/system/bankshield-api.service`：
```ini
[Unit]
Description=BankShield API Service
After=network.target mysql.service

[Service]
Type=simple
User=bankshield
WorkingDirectory=/opt/bankshield/bankshield-api
ExecStart=/usr/bin/java -jar bankshield-api-1.0.0.jar
Restart=always
RestartSec=10
StandardOutput=syslog
StandardError=syslog
SyslogIdentifier=bankshield-api

[Install]
WantedBy=multi-user.target
```

创建 `/etc/systemd/system/bankshield-ui.service`：
```ini
[Unit]
Description=BankShield UI Service
After=network.target

[Service]
Type=simple
User=bankshield
WorkingDirectory=/opt/bankshield/bankshield-ui
ExecStart=/usr/bin/npm run preview
Restart=always
RestartSec=10
StandardOutput=syslog
StandardError=syslog
SyslogIdentifier=bankshield-ui

[Install]
WantedBy=multi-user.target
```

#### 3.2 启动服务
```bash
sudo systemctl daemon-reload
sudo systemctl enable bankshield-api
sudo systemctl enable bankshield-ui
sudo systemctl start bankshield-api
sudo systemctl start bankshield-ui
```

### 4. 数据库备份策略
```bash
# 创建备份脚本 /opt/bankshield/backup.sh
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/opt/bankshield/backups"
DB_NAME="bankshield"
DB_USER="bankshield"
DB_PASS="bankshield123"

mkdir -p $BACKUP_DIR
mysqldump -u$DB_USER -p$DB_PASS $DB_NAME > $BACKUP_DIR/bankshield_$DATE.sql

# 删除7天前的备份
find $BACKUP_DIR -name "*.sql" -mtime +7 -delete
```

添加定时任务：
```bash
# 每天凌晨2点执行备份
0 2 * * * /opt/bankshield/backup.sh
```

## 安全配置

### 1. 防火墙配置
```bash
# Ubuntu/Debian (使用ufw)
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable

# CentOS/RHEL (使用firewalld)
sudo firewall-cmd --permanent --add-service=ssh
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --reload
```

### 2. 数据库安全
```sql
-- 限制数据库访问
REVOKE ALL PRIVILEGES ON *.* FROM 'bankshield'@'%';
GRANT ALL PRIVILEGES ON bankshield.* TO 'bankshield'@'localhost';
FLUSH PRIVILEGES;

-- 启用SSL连接
SET GLOBAL require_secure_transport = ON;
```

### 3. 应用安全配置
编辑 `application.yml`：
```yaml
# 安全相关配置
spring:
  security:
    headers:
      frame-options: DENY
      content-security-policy: "default-src 'self'"
      strict-transport-security: max-age=31536000; includeSubDomains
    
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
    key-alias: bankshield
```

## 性能调优

### 1. JVM参数优化
```bash
java -Xms2g -Xmx4g -XX:+UseG1GC -XX:+UseStringDeduplication \
     -XX:+PrintGCDetails -XX:+PrintGCTimeStamps \
     -jar bankshield-api-1.0.0.jar
```

### 2. 数据库连接池优化
```yaml
spring:
  datasource:
    druid:
      initial-size: 20
      max-active: 100
      min-idle: 20
      max-wait: 60000
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
```

### 3. 前端性能优化
```bash
# 启用gzip压缩
npm install compression-webpack-plugin --save-dev

# 配置生产环境构建
# 在vue.config.js中添加：
module.exports = {
  configureWebpack: config => {
    if (process.env.NODE_ENV === 'production') {
      config.plugins.push(
        new CompressionPlugin({
          algorithm: 'gzip',
          test: /\.(js|css|html|svg)$/,
          threshold: 8192,
          minRatio: 0.8
        })
      )
    }
  }
}
```

## 监控和日志

### 1. 应用监控
```yaml
# 启用Spring Boot Actuator
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
```

### 2. 日志配置
```yaml
logging:
  level:
    com.bankshield.security: DEBUG
    org.springframework.security: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: /var/log/bankshield/bankshield-api.log
    max-size: 100MB
    max-history: 30
```

### 3. 系统监控
安装和配置Prometheus + Grafana：
```bash
# 安装Prometheus
wget https://github.com/prometheus/prometheus/releases/download/v2.37.0/prometheus-2.37.0.linux-amd64.tar.gz
tar xvf prometheus-2.37.0.linux-amd64.tar.gz

# 配置Prometheus
# 编辑 prometheus.yml，添加BankShield应用监控
```

## 故障排查

### 1. 常见问题

#### 1.1 数据库连接失败
```bash
# 检查MySQL服务状态
sudo systemctl status mysql

# 检查数据库配置
mysql -u bankshield -pbankshield123 -e "SELECT 1" bankshield

# 检查网络连接
telnet localhost 3306
```

#### 1.2 端口冲突
```bash
# 检查端口占用
sudo netstat -tlnp | grep :8080
sudo netstat -tlnp | grep :3000

# 终止占用进程
sudo kill -9 <PID>
```

#### 1.3 内存不足
```bash
# 检查内存使用
free -h
top

# 调整JVM堆大小
java -Xms1g -Xmx2g -jar bankshield-api-1.0.0.jar
```

### 2. 日志分析
```bash
# 查看应用日志
tail -f /var/log/bankshield/bankshield-api.log

# 查看系统日志
journalctl -u bankshield-api -f

# 查看错误日志
grep ERROR /var/log/bankshield/bankshield-api.log
```

## 升级和维护

### 1. 版本升级
```bash
# 备份当前版本
cp -r /opt/bankshield /opt/bankshield.backup.$(date +%Y%m%d)

# 停止服务
sudo systemctl stop bankshield-api
sudo systemctl stop bankshield-ui

# 部署新版本
# 按照部署步骤重新部署

# 数据库升级（如有需要）
mysql -u bankshield -pbankshield123 bankshield < upgrade.sql

# 启动服务
sudo systemctl start bankshield-api
sudo systemctl start bankshield-ui
```

### 2. 定期维护
- 每周检查系统日志
- 每月更新安全基线
- 每季度进行安全评估
- 每年进行渗透测试

## 技术支持

如遇到问题，请通过以下方式获取支持：
- 查看项目文档：`docs/`目录
- 检查日志文件：`/var/log/bankshield/`
- 提交Issue到项目仓库
- 联系技术支持团队

## 安全声明

本系统仅供学习和研究使用，部署在生产环境前请进行充分的安全评估和测试。对于因使用本系统而产生的任何安全问题，开发团队不承担责任。