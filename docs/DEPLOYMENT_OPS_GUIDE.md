# BankShield 部署运维指南

**版本**: v1.0.0
**更新日期**: 2025-12-25
**适用环境**: 开发环境、测试环境、生产环境

---

## 📋 目录

- [环境要求](#环境要求)
- [快速部署](#快速部署)
- [Docker部署](#docker部署)
- [Kubernetes部署](#kubernetes部署)
- [配置管理](#配置管理)
- [监控告警](#监控告警)
- [故障排查](#故障排查)
- [运维手册](#运维手册)
- [备份恢复](#备份恢复)
- [安全加固](#安全加固)

---

## 🔧 环境要求

### 硬件要求

| 环境 | CPU | 内存 | 磁盘 | 网络 |
|------|-----|------|------|------|
| 开发环境 | 4核 | 8GB | 50GB | 100Mbps |
| 测试环境 | 8核 | 16GB | 100GB | 1Gbps |
| 生产环境 | 16核+ | 32GB+ | 500GB+ | 10Gbps |

### 软件要求

#### 后端环境
- **JDK**: 1.8+ (推荐 OpenJDK 1.8.0_362+)
- **Maven**: 3.6+
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **Nginx**: 1.18+ (生产环境)

#### 前端环境
- **Node.js**: 16.x (推荐 16.20.0+)
- **npm**: 8.x+

#### 容器环境（可选）
- **Docker**: 20.10+
- **Docker Compose**: 2.0+
- **Kubernetes**: 1.24+ (生产环境)
- **Helm**: 3.0+

---

## 🚀 快速部署

### 方式一：使用启动脚本（推荐）

```bash
# 1. 克隆代码
git clone https://github.com/bankshield/bankshield.git
cd BankShield

# 2. 初始化数据库
mysql -u root -p < sql/init_database.sql

# 3. 启动开发环境
./scripts/start.sh --dev

# 4. 访问系统
# 前端: http://localhost:3000
# 后端: http://localhost:8080/api
# Druid监控: http://localhost:8080/api/druid/login.html (admin/123456)
```

### 方式二：手动部署

#### 后端部署

```bash
# 1. 进入API模块
cd bankshield-api

# 2. 构建项目
mvn clean package -DskipTests

# 3. 配置数据库
# 编辑 src/main/resources/application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bankshield?useSSL=false
    username: root
    password: your_password

# 4. 启动服务
java -jar target/bankshield-api-1.0.0-SNAPSHOT.jar

# 或使用Maven启动
mvn spring-boot:run
```

#### 前端部署

```bash
# 1. 进入UI模块
cd bankshield-ui

# 2. 安装依赖
npm install

# 3. 配置API地址
# 编辑 .env.development
VITE_API_BASE_URL=http://localhost:8080/api

# 4. 启动开发服务器
npm run dev
```

---

## 🐳 Docker部署

### 准备工作

```bash
# 1. 构建镜像
docker build -t bankshield/api:latest ./bankshield-api
docker build -t bankshield/ui:latest ./bankshield-ui

# 2. 查看镜像
docker images
```

### 使用Docker Compose

```bash
# 1. 进入docker目录
cd docker

# 2. 启动所有服务
docker-compose up -d

# 3. 查看服务状态
docker-compose ps

# 4. 查看日志
docker-compose logs -f api
docker-compose logs -f ui
```

### Docker Compose配置

```yaml
version: '3.8'

services:
  # MySQL数据库
  mysql:
    image: mysql:8.0
    container_name: bankshield-mysql
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: bankshield
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - bankshield-network
    restart: always

  # Redis缓存
  redis:
    image: redis:6.0
    container_name: bankshield-redis
    command: redis-server --requirepass ${REDIS_PASSWORD}
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - bankshield-network
    restart: always

  # API服务
  api:
    image: bankshield/api:latest
    container_name: bankshield-api
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/bankshield
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PASSWORD: ${REDIS_PASSWORD}
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
    networks:
      - bankshield-network
    restart: always

  # UI服务
  ui:
    image: bankshield/ui:latest
    container_name: bankshield-ui
    ports:
      - "80:80"
    depends_on:
      - api
    networks:
      - bankshield-network
    restart: always

volumes:
  mysql-data:
  redis-data:

networks:
  bankshield-network:
    driver: bridge
```

---

## ☸️ Kubernetes部署

### 前置要求

```bash
# 1. 确认Kubernetes连接
kubectl cluster-info

# 2. 确认Helm安装
helm version
```

### 使用Helm部署

```bash
# 1. 添加Helm仓库
helm repo add bankshield https://charts.bankshield.com

# 2. 更新仓库
helm repo update

# 3. 安装到开发环境
helm install bankshield-dev ./helm/bankshield \
  --namespace bankshield-dev \
  --create-namespace \
  --set api.image.tag=latest \
  --set ui.image.tag=latest \
  --values helm/bankshield/values-dev.yaml

# 4. 查看部署状态
helm status bankshield-dev -n bankshield-dev

# 5. 查看Pod状态
kubectl get pods -n bankshield-dev
```

### 自定义配置

```bash
# 创建自定义配置文件
cat > my-values.yaml << EOF
api:
  replicaCount: 3
  resources:
    requests:
      memory: "2Gi"
      cpu: "1000m"
    limits:
      memory: "4Gi"
      cpu: "2000m"

ui:
  replicaCount: 2
  resources:
    requests:
      memory: "512Mi"
      cpu: "500m"
    limits:
      memory: "1Gi"
      cpu: "1000m"

mysql:
  persistence:
    enabled: true
    size: 100Gi
EOF

# 使用自定义配置部署
helm upgrade bankshield-prod ./helm/bankshield \
  --namespace bankshield-prod \
  -f my-values.yaml
```

### 部署到生产环境

```bash
# 1. 创建生产命名空间
kubectl create namespace bankshield-prod

# 2. 配置密钥
kubectl create secret generic bankshield-secrets \
  --from-literal=mysql-password=your-mysql-password \
  --from-literal=redis-password=your-redis-password \
  -n bankshield-prod

# 3. 部署
helm install bankshield-prod ./helm/bankshield \
  --namespace bankshield-prod \
  -f helm/bankshield/values-prod.yaml

# 4. 等待部署完成
kubectl rollout status deployment/bankshield-api -n bankshield-prod
kubectl rollout status deployment/bankshield-ui -n bankshield-prod
```

---

## ⚙️ 配置管理

### 环境变量

| 变量名 | 说明 | 默认值 | 环境类型 |
|--------|------|--------|----------|
| `SPRING_PROFILES_ACTIVE` | 环境配置 | dev | dev/test/prod |
| `SERVER_PORT` | 服务端口 | 8080 | - |
| `SPRING_DATASOURCE_URL` | 数据库URL | - | - |
| `SPRING_DATASOURCE_USERNAME` | 数据库用户名 | - | - |
| `SPRING_DATASOURCE_PASSWORD` | 数据库密码 | - | - |
| `SPRING_REDIS_HOST` | Redis主机 | localhost | - |
| `SPRING_REDIS_PORT` | Redis端口 | 6379 | - |
| `SPRING_REDIS_PASSWORD` | Redis密码 | - | prod |
| `JWT_SECRET` | JWT密钥 | - | prod |

### 配置文件结构

```
bankshield-api/src/main/resources/
├── application.yml              # 主配置文件
├── application-dev.yml          # 开发环境配置
├── application-test.yml         # 测试环境配置
└── application-prod.yml        # 生产环境配置
```

### 开发环境配置

```yaml
# application-dev.yml
server:
  port: 8080

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/bankshield?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: dev_password

  redis:
    host: localhost
    port: 6379
    database: 0

  jpa:
    show-sql: true

logging:
  level:
    com.bankshield: DEBUG
    org.springframework: INFO
```

### 生产环境配置

```yaml
# application-prod.yml
server:
  port: 8080
  compression:
    enabled: true

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://prod-db:3306/bankshield?useSSL=true&verifyServerCertificate=true
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5

  redis:
    host: prod-redis
    port: 6379
    password: ${REDIS_PASSWORD}
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0

logging:
  level:
    com.bankshield: INFO
    org.springframework: WARN
  file:
    name: /var/log/bankshield/api.log
```

---

## 📊 监控告警

### Prometheus监控

#### 配置Prometheus

```yaml
# prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'bankshield-api'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['bankshield-api:8080']
    scrape_interval: 10s

  - job_name: 'bankshield-ui'
    static_configs:
      - targets: ['bankshield-ui:80']

  - job_name: 'mysql'
    static_configs:
      - targets: ['mysql-exporter:9104']

  - job_name: 'redis'
    static_configs:
      - targets: ['redis-exporter:9121']
```

#### 关键指标

| 指标名称 | 说明 | 告警阈值 |
|-----------|------|----------|
| `jvm_memory_used_bytes` | JVM内存使用 | > 4GB |
| `process_cpu_usage` | CPU使用率 | > 80% |
| `http_server_requests_seconds` | HTTP请求耗时 | P95 > 1s |
| `tomcat_threads_busy` | Tomcat活跃线程数 | > 200 |
| `hikaricp_connections_active` | 数据库活跃连接数 | > 15 |

### Grafana仪表盘

#### 导入仪表盘

```bash
# 导入ID为1860的Spring Boot仪表盘
curl -X POST http://grafana:3000/api/dashboards/db \
  -H "Content-Type: application/json" \
  -d '{
    "dashboard": {...},
    "overwrite": true
  }'
```

#### 推荐仪表盘

| 仪表盘 | ID | 说明 |
|--------|-----|------|
| Spring Boot 2.1 | 6756 | Spring Boot应用监控 |
| JVM (Micrometer) | 4701 | JVM性能监控 |
| MySQL | 7362 | MySQL数据库监控 |
| Redis | 11835 | Redis缓存监控 |

### 告警规则

```yaml
# alert_rules.yml
groups:
  - name: bankshield-alerts
    rules:
      # CPU使用率告警
      - alert: HighCPUUsage
        expr: rate(process_cpu_seconds_total{job="bankshield-api"}[5m]) * 100 > 80
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "API服务CPU使用率高"
          description: "CPU使用率: {{ $value }}%"

      # 内存使用告警
      - alert: HighMemoryUsage
        expr: jvm_memory_used_bytes{area="heap", job="bankshield-api"} / jvm_memory_max_bytes{area="heap", job="bankshield-api"} * 100 > 85
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "API服务内存使用高"
          description: "堆内存使用率: {{ $value }}%"

      # HTTP错误率告警
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{job="bankshield-api",status=~"5.."}[5m]) / rate(http_server_requests_seconds_count{job="bankshield-api"}[5m]) * 100 > 1
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "HTTP错误率过高"
          description: "错误率: {{ $value }}%"

      # 数据库连接池告警
      - alert: DatabaseConnectionPoolExhausted
        expr: hikaricp_connections_active{job="bankshield-api"} / hikaricp_connections_max{job="bankshield-api"} * 100 > 90
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "数据库连接池耗尽"
          description: "连接池使用率: {{ $value }}%"
```

---

## 🔍 故障排查

### 常见问题

#### 1. 数据库连接失败

**症状**: 应用启动时提示数据库连接失败

**排查步骤**:

```bash
# 1. 检查MySQL服务状态
systemctl status mysql
# 或
docker ps | grep mysql

# 2. 测试连接
mysql -h localhost -P 3306 -u root -p

# 3. 检查连接配置
cat bankshield-api/src/main/resources/application-dev.yml | grep datasource

# 4. 查看错误日志
tail -f logs/api.log | grep -i error

# 5. 检查防火墙
sudo firewall-cmd --list-ports
sudo firewall-cmd --add-port=3306/tcp --permanent
sudo firewall-cmd --reload
```

**解决方案**:

```yaml
# 检查application.yml配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bankshield?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: ${DB_PASSWORD}  # 确保密码正确
    hikari:
      maximum-pool-size: 20
      connection-timeout: 30000
```

#### 2. Redis连接失败

**症状**: 应用提示Redis连接超时

**排查步骤**:

```bash
# 1. 检查Redis服务
redis-cli ping  # 应返回PONG

# 2. 检查Redis配置
redis-cli config get requirepass

# 3. 测试连接
redis-cli -h localhost -p 6379 -a your_password ping

# 4. 查看连接数
redis-cli info clients
```

**解决方案**:

```yaml
# 检查application.yml配置
spring:
  redis:
    host: localhost
    port: 6379
    password: ${REDIS_PASSWORD}  # 检查密码是否正确
    timeout: 3000
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms
```

#### 3. 服务启动后404错误

**症状**: 服务正常启动，但访问API返回404

**排查步骤**:

```bash
# 1. 检查服务健康状态
curl http://localhost:8080/api/actuator/health

# 2. 检查Controller是否存在
find . -name "*Controller.java" -type f

# 3. 查看日志确认路由注册
tail -f logs/api.log | grep -i "RequestMappingHandlerMapping"

# 4. 检查Nginx配置（生产环境）
cat /etc/nginx/conf.d/bankshield.conf
```

**解决方案**:

```nginx
# Nginx配置示例
upstream bankshield-api {
    server 127.0.0.1:8080;
}

server {
    listen 80;
    server_name api.bankshield.com;

    location /api {
        proxy_pass http://bankshield-api;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

#### 4. 前端构建失败

**症状**: npm run build 报错

**排查步骤**:

```bash
# 1. 清除缓存
rm -rf node_modules package-lock.json
npm cache clean --force

# 2. 重新安装
npm install

# 3. 检查Node版本
node --version  # 应该是16.x

# 4. 检查TypeScript错误
npm run type-check
```

**解决方案**:

```bash
# 如果Node版本不匹配，使用nvm安装正确版本
nvm install 16
nvm use 16
npm install
npm run build
```

---

## 📖 运维手册

### 日常巡检

#### 每日巡检

```bash
#!/bin/bash
# scripts/daily-check.sh

echo "=== BankShield 每日巡检 ==="
echo ""

# 1. 检查服务状态
echo "1. 服务状态检查"
docker ps --filter "name=bankshield" --format "table {{.Names}}\t{{.Status}}"

# 2. 检查健康状态
echo ""
echo "2. 健康状态检查"
curl -s http://localhost:8080/api/actuator/health | jq '.'

# 3. 检查磁盘使用
echo ""
echo "3. 磁盘使用检查"
df -h

# 4. 检查内存使用
echo ""
echo "4. 内存使用检查"
free -h

# 5. 检查最近的错误日志
echo ""
echo "5. 最近错误日志"
tail -n 20 logs/api.log | grep -i error || echo "无错误日志"

echo ""
echo "=== 巡检完成 ==="
```

#### 每周巡检

```bash
#!/bin/bash
# scripts/weekly-check.sh

echo "=== BankShield 每周巡检 ==="
echo ""

# 1. 数据库备份检查
echo "1. 检查备份文件"
ls -lht /backup/mysql/ | head -5

# 2. 性能指标统计
echo ""
echo "2. 性能指标统计"
curl -s http://localhost:8080/api/actuator/prometheus \
  | grep 'http_server_requests_seconds_sum' | tail -5

# 3. 慢查询分析
echo ""
echo "3. 慢查询分析"
mysql -u root -p${MYSQL_PASSWORD} -e "SHOW VARIABLES LIKE 'slow_query_log';"

# 4. 安全事件统计
echo ""
echo "4. 本周安全事件"
curl -s "http://localhost:8080/api/audit/list?startTime=$(date -d '7 days ago' '+%Y-%m-%d')"

echo ""
echo "=== 巡检完成 ==="
```

### 日志管理

#### 日志收集

```bash
# 1. 查看实时日志
docker logs -f bankshield-api

# 2. 查看最近100行日志
docker logs --tail 100 bankshield-api

# 3. 按时间筛选
docker logs bankshield-api | grep "2025-12-25 10:"

# 4. 按级别筛选
docker logs bankshield-api | grep -i error
docker logs bankshield-api | grep -i warn
```

#### 日志分析

```bash
# 统计错误数量
grep -i error logs/api.log | wc -l

# 查看最慢的接口
grep "http_server_requests_seconds" logs/api.log \
  | sort -k5 -nr | head -10

# 统计请求量
grep "GET" logs/access.log | awk '{print $7}' | sort | uniq -c | sort -rn | head -10
```

### 性能优化

#### JVM调优

```bash
# 生产环境推荐参数
java -jar \
  -Xms2g -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=100 \
  -XX:+UseStringDeduplication \
  -XX:InitiatingHeapOccupancyPercent=45 \
  -XX:+PrintGCDetails \
  -XX:+PrintGCDateStamps \
  -Xloggc:/var/log/bankshield/gc.log \
  -Dspring.profiles.active=prod \
  bankshield-api.jar
```

#### 数据库优化

```sql
-- 查看慢查询
SHOW VARIABLES LIKE 'slow_query_log';
SHOW VARIABLES LIKE 'long_query_time';

-- 启用慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;

-- 查看连接数
SHOW STATUS LIKE 'Threads_connected';
SHOW STATUS LIKE 'Max_used_connections';

-- 优化表
OPTIMIZE TABLE sys_user;
ANALYZE TABLE sys_user;
```

---

## 💾 备份恢复

### 数据库备份

#### 自动备份脚本

```bash
#!/bin/bash
# scripts/backup-database.sh

BACKUP_DIR="/backup/bankshield/mysql"
DATE=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS=7

# 创建备份目录
mkdir -p ${BACKUP_DIR}

# 执行备份
mysqldump -u root -p"${MYSQL_PASSWORD}" \
  --single-transaction \
  --routines \
  --triggers \
  --events \
  bankshield | gzip > ${BACKUP_DIR}/bankshield_${DATE}.sql.gz

# 记录日志
echo "[$(date '+%Y-%m-%d %H:%M:%S')] 备份成功: bankshield_${DATE}.sql.gz" >> ${BACKUP_DIR}/backup.log

# 清理旧备份
find ${BACKUP_DIR} -name "bankshield_*.sql.gz" -mtime +${RETENTION_DAYS} -delete

echo "备份完成，保留最近 ${RETENTION_DAYS} 天"
```

#### 手动备份

```bash
# 全量备份
mysqldump -u root -p bankshield > bankshield_$(date +%Y%m%d).sql

# 压缩备份
mysqldump -u root -p bankshield | gzip > bankshield_$(date +%Y%m%d).sql.gz

# 备份特定表
mysqldump -u root -p bankshield sys_user > sys_user_backup.sql
```

### 数据恢复

```bash
# 解压并恢复
gunzip < bankshield_20251225.sql.gz | mysql -u root -p bankshield

# 直接恢复
mysql -u root -p bankshield < bankshield_20251225.sql

# 恢复特定表
mysql -u root -p bankshield < sys_user_backup.sql
```

### 定时备份

```bash
# 编辑crontab
crontab -e

# 每天凌晨2点备份
0 2 * * * /path/to/scripts/backup-database.sh

# 每周日凌晨3点全量备份
0 3 * * 0 /path/to/scripts/backup-database.sh --full
```

---

## 🔒 安全加固

### 系统安全

#### 防火墙配置

```bash
# 仅开放必要端口
sudo firewall-cmd --permanent --add-port=80/tcp    # HTTP
sudo firewall-cmd --permanent --add-port=443/tcp   # HTTPS
sudo firewall-cmd --permanent --add-port=22/tcp    # SSH
sudo firewall-cmd --reload

# 查看规则
sudo firewall-cmd --list-all
```

#### SSH加固

```bash
# 编辑SSH配置
sudo vi /etc/ssh/sshd_config

# 修改以下配置
Port 2222                          # 修改默认端口
PermitRootLogin no                 # 禁止root登录
PasswordAuthentication no           # 禁用密码登录
PubkeyAuthentication yes           # 启用密钥认证

# 重启SSH服务
sudo systemctl restart sshd
```

### 应用安全

#### 密钥管理

```bash
# 1. 安装Vault
wget https://releases.hashicorp.com/vault/1.14.0/vault_1.14.0_linux_amd64.zip
unzip vault_1.14.0_linux_amd64.zip
sudo mv vault /usr/local/bin/

# 2. 启动Vault
vault server -dev

# 3. 存储密钥
vault kv put secret/bankshield/database password="your-db-password"
vault kv put secret/bankshield/redis password="your-redis-password"

# 4. 读取密钥
vault kv get -field=password secret/bankshield/database
```

#### HTTPS配置

```nginx
# Nginx SSL配置
server {
    listen 443 ssl http2;
    server_name api.bankshield.com;

    ssl_certificate /etc/ssl/certs/bankshield.crt;
    ssl_certificate_key /etc/ssl/private/bankshield.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    location /api {
        proxy_pass http://bankshield-api;
        # ... 其他配置
    }
}
```

---

## 📞 技术支持

- **技术支持**: tech-support@bankshield.com
- **安全团队**: security@bankshield.com
- **运维团队**: ops@bankshield.com
- **值班电话**: +86-400-123-4567

---

**文档版本**: v1.0.0
**最后更新**: 2025-12-25
