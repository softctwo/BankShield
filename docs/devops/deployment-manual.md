# BankShield 部署手册

## 1. 环境准备

### 1.1 硬件要求
```bash
# 最低硬件配置
- CPU: 16核+
- RAM: 32GB+
- 磁盘: 500GB SSD (系统盘100GB + 数据盘400GB)
- 网络: 千兆内网，公网带宽100Mbps+
- 负载均衡器: 支持SSL终端，4层/7层转发
```

### 1.2 软件要求
```bash
# 操作系统
- OS: CentOS 7.9+ / Ubuntu 20.04 LTS / RHEL 8+
- 内核版本: 4.18+
- 文件系统: ext4/xfs

# 容器化平台
- Docker: 20.10+
- Docker Compose: 2.0+
- Kubernetes: 1.24+
- Helm: 3.8+

# 运行时环境
- Java: OpenJDK 8u292+
- Node.js: 16.x+
- Python: 3.8+ (监控脚本)

# 数据库
- MySQL: 8.0.25+
- Redis: 6.0.8+

# 工具
- Git: 2.30+
- curl, jq, netstat, telnet
```

### 1.3 网络配置
```bash
# 端口开放清单
- 80/443: Web服务
- 8080: API服务
- 3306: MySQL (仅内网)
- 6379: Redis (仅内网)
- 9090: Prometheus
- 3000: Grafana
- 22: SSH (堡垒机)

# 域名解析
api.bankshield.com     -> 负载均衡器IP
admin.bankshield.com   -> 管理后台
monitor.bankshield.com -> 监控系统
```

## 2. Docker部署

### 2.1 镜像构建
```dockerfile
# bankshield-api/Dockerfile
FROM openjdk:8-jre-slim

LABEL maintainer="BankShield DevOps <devops@bankshield.com>"
LABEL version="1.0.0"
LABEL description="BankShield API Service"

# 安装必要的工具
RUN apt-get update && apt-get install -y \
    curl \
    netcat \
    && rm -rf /var/lib/apt/lists/*

# 创建应用目录
WORKDIR /app

# 添加应用用户
RUN groupadd -r bankshield && useradd -r -g bankshield bankshield

# 复制应用文件
COPY target/bankshield-api-1.0.0.jar app.jar
COPY application-prod.yml ./config/
COPY docker-entrypoint.sh /usr/local/bin/

# 设置权限
RUN chmod +x /usr/local/bin/docker-entrypoint.sh \
    && chown -R bankshield:bankshield /app

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 暴露端口
EXPOSE 8080

# 切换到应用用户
USER bankshield

# 启动命令
ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["java", "-jar", "-Xmx4g", "-Xms4g", "-XX:+UseG1GC", "-XX:+PrintGCDetails", "-Xloggc:/app/logs/gc.log", "app.jar"]
```

```bash
# docker-entrypoint.sh
#!/bin/bash
set -e

# 等待数据库启动
echo "等待数据库连接..."
while ! nc -z ${DB_HOST:-mysql} ${DB_PORT:-3306}; do
    sleep 1
done
echo "数据库已连接"

# 等待Redis启动
echo "等待Redis连接..."
while ! nc -z ${REDIS_HOST:-redis} ${REDIS_PORT:-6379}; do
    sleep 1
done
echo "Redis已连接"

# 执行Flyway迁移
if [ "${FLYWAY_MIGRATE:-true}" = "true" ]; then
    echo "执行数据库迁移..."
    java -jar flyway.jar migrate
fi

# 启动应用
exec "$@"
```

### 2.2 Docker Compose配置
```yaml
# docker-compose.yml
version: '3.8'

services:
  mysql:
    image: mysql:8.0.32
    container_name: bankshield-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASSWORD}
      MYSQL_DATABASE: bankshield
      MYSQL_USER: ${DB_USER}
      MYSQL_PASSWORD: ${DB_PASSWORD}
      MYSQL_INITDB_SKIP_TZINFO: 1
    volumes:
      - mysql-data:/var/lib/mysql
      - ./sql/init.sql:/docker-entrypoint-initdb.d/01-init.sql:ro
      - ./sql/procedures.sql:/docker-entrypoint-initdb.d/02-procedures.sql:ro
    ports:
      - "3306:3306"
    networks:
      - bankshield-network
    command: >
      --default-authentication-plugin=mysql_native_password
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_unicode_ci
      --innodb_buffer_pool_size=2G
      --max_connections=500
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${DB_ROOT_PASSWORD}"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  redis:
    image: redis:6.2-alpine
    container_name: bankshield-redis
    restart: unless-stopped
    command: >
      redis-server
      --requirepass ${REDIS_PASSWORD}
      --maxmemory 2gb
      --maxmemory-policy allkeys-lru
      --save 900 1
      --save 300 10
      --save 60 10000
    volumes:
      - redis-data:/data
      - ./redis/redis.conf:/usr/local/etc/redis/redis.conf:ro
    ports:
      - "6379:6379"
    networks:
      - bankshield-network
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 30s
      timeout: 10s
      retries: 3

  api:
    build:
      context: ./bankshield-api
      dockerfile: Dockerfile
    container_name: bankshield-api
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: prod,docker
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: bankshield
      DB_USER: ${DB_USER}
      DB_PASSWORD: ${DB_PASSWORD}
      REDIS_HOST: redis
      REDIS_PORT: 6379
      REDIS_PASSWORD: ${REDIS_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      MASTER_KEY: ${MASTER_KEY}
      LOG_LEVEL: INFO
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
    ports:
      - "8080:8080"
    volumes:
      - ./logs:/app/logs
      - ./config:/app/config
    networks:
      - bankshield-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  nginx:
    image: nginx:alpine
    container_name: bankshield-nginx
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/ssl:/etc/nginx/ssl:ro
      - ./nginx/logs:/var/log/nginx
    depends_on:
      - api
    networks:
      - bankshield-network

volumes:
  mysql-data:
    driver: local
  redis-data:
    driver: local

networks:
  bankshield-network:
    driver: bridge
    ipam:
      config:
        - subnet: 172.20.0.0/16
```

### 2.3 环境变量配置
```bash
# .env.production
# 数据库配置
DB_ROOT_PASSWORD=BankShield@2024#Root
DB_USER=bankshield
DB_PASSWORD=BankShield@2024#User
DB_HOST=mysql
DB_PORT=3306
DB_NAME=bankshield

# Redis配置
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=BankShield@2024#Redis

# JWT配置
JWT_SECRET=BankShield_JWT_Secret_Key_2024_Random_String_Must_Be_Long_Enough

# 加密配置
MASTER_KEY=BankShield_Master_Encryption_Key_32_Bytes_Length

# 日志配置
LOG_LEVEL=INFO
LOG_PATH=/app/logs

# 监控配置
PROMETHEUS_ENDPOINT=http://prometheus:9090
GRAFANA_ENDPOINT=http://grafana:3000

# 时区
TZ=Asia/Shanghai
```

### 2.4 部署脚本
```bash
#!/bin/bash
# deploy.sh

set -e

echo "开始部署BankShield..."

# 检查环境文件
if [ ! -f ".env.production" ]; then
    echo "错误: .env.production文件不存在"
    exit 1
fi

# 加载环境变量
source .env.production

# 创建必要的目录
mkdir -p logs mysql-data redis-data nginx/logs nginx/ssl

# 设置目录权限
chmod 755 logs mysql-data redis-data
chmod 700 nginx/ssl

# 检查端口是否被占用
check_port() {
    if netstat -tlnp | grep -q ":$1 "; then
        echo "错误: 端口 $1 已被占用"
        exit 1
    fi
}

check_port 80
check_port 443
check_port 3306
check_port 6379
check_port 8080

# 拉取最新镜像
echo "拉取最新镜像..."
docker-compose pull

# 构建应用镜像
echo "构建应用镜像..."
docker-compose build api

# 启动服务
echo "启动服务..."
docker-compose up -d

# 等待服务启动
echo "等待服务启动..."
sleep 30

# 健康检查
echo "执行健康检查..."
if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "API服务启动成功"
else
    echo "错误: API服务启动失败"
    docker-compose logs api
    exit 1
fi

# 显示状态
echo "服务状态:"
docker-compose ps

echo "部署完成!"
echo "API地址: http://localhost:8080"
echo "管理后台: https://localhost/admin"
echo "监控面板: http://localhost:3000"
```

## 3. Kubernetes部署

### 3.1 命名空间配置
```yaml
# namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: bankshield-prod
  labels:
    name: bankshield-prod
    environment: production
    app: bankshield
```

### 3.2 配置管理
```yaml
# configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: bankshield-config
  namespace: bankshield-prod
data:
  application.yml: |
    spring:
      datasource:
        url: jdbc:mysql://mysql-service:3306/bankshield?useSSL=true&characterEncoding=utf8
        username: bankshield
        hikari:
          maximum-pool-size: 100
          minimum-idle: 20
          connection-timeout: 30000
          idle-timeout: 600000
          max-lifetime: 1800000
      
      redis:
        host: redis-service
        port: 6379
        timeout: 2000ms
        lettuce:
          pool:
            max-active: 20
            max-idle: 10
            min-idle: 5
      
      jpa:
        hibernate:
          ddl-auto: validate
        show-sql: false
        properties:
          hibernate:
            dialect: org.hibernate.dialect.MySQL8Dialect
            format_sql: true
      
      security:
        jwt:
          access-token-expiration: 1800
          refresh-token-expiration: 604800
      
      logging:
        level:
          com.bankshield: INFO
          org.springframework.web: WARN
        pattern:
          console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
          file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    
    management:
      endpoints:
        web:
          exposure:
            include: health,info,metrics,prometheus
      endpoint:
        health:
          show-details: when-authorized
      metrics:
        export:
          prometheus:
            enabled: true
    
    encrypt:
      key-rotation-days: 90
      algorithm: AES/GCM/NoPadding
```

### 3.3 密钥管理
```yaml
# secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: bankshield-secrets
  namespace: bankshield-prod
type: Opaque
data:
  # base64编码的值
  db-password: QmFua1NoaWVsZEAyMDI0I1VzZXI=  # BankShield@2024#User
  redis-password: QmFua1NoaWVsZEAyMDI0I1JlZGlz  # BankShield@2024#Redis
  jwt-secret: QmFua1NoaWVsZF9KV1RfU2VjcmV0X0tleV8yMDI0X1JhbmRvbV9TdHJpbmdfTXVzdF9CZV9Mb25nX0Vub3VnaA==
  master-key: QmFua1NoaWVsZF9NYXN0ZXJfRW5jcnlwdGlvbl9LZXlfMzJfQnl0ZXNfTGVuZ3Ro
```

### 3.4 数据库 StatefulSet
```yaml
# mysql-statefulset.yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: mysql
  namespace: bankshield-prod
spec:
  serviceName: mysql
  replicas: 1
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
      - name: mysql
        image: mysql:8.0.32
        env:
        - name: MYSQL_ROOT_PASSWORD
          valueFrom:
            secretKeyRef:
              name: bankshield-secrets
              key: db-password
        - name: MYSQL_DATABASE
          value: bankshield
        - name: MYSQL_USER
          value: bankshield
        - name: MYSQL_PASSWORD
          valueFrom:
            secretKeyRef:
              name: bankshield-secrets
              key: db-password
        ports:
        - containerPort: 3306
          name: mysql
        volumeMounts:
        - name: mysql-storage
          mountPath: /var/lib/mysql
        - name: mysql-config
          mountPath: /etc/mysql/conf.d
        resources:
          requests:
            cpu: 500m
            memory: 1Gi
          limits:
            cpu: 2000m
            memory: 4Gi
        livenessProbe:
          exec:
            command:
            - mysqladmin
            - ping
            - -h
            - localhost
            - -u
            - root
            - -p$(MYSQL_ROOT_PASSWORD)
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          exec:
            command:
            - mysqladmin
            - ping
            - -h
            - localhost
            - -u
            - root
            - -p$(MYSQL_ROOT_PASSWORD)
          initialDelaySeconds: 30
          periodSeconds: 10
      volumes:
      - name: mysql-config
        configMap:
          name: mysql-config
  volumeClaimTemplates:
  - metadata:
      name: mysql-storage
    spec:
      accessModes: ["ReadWriteOnce"]
      storageClassName: fast-ssd
      resources:
        requests:
          storage: 500Gi
---
apiVersion: v1
kind: Service
metadata:
  name: mysql-service
  namespace: bankshield-prod
spec:
  ports:
  - port: 3306
    name: mysql
  clusterIP: None
  selector:
    app: mysql
```

### 3.5 API Deployment
```yaml
# bankshield-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: bankshield-api
  namespace: bankshield-prod
  labels:
    app: bankshield-api
    version: v1.0.0
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: bankshield-api
  template:
    metadata:
      labels:
        app: bankshield-api
        version: v1.0.0
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8080"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
          - weight: 100
            podAffinityTerm:
              labelSelector:
                matchExpressions:
                - key: app
                  operator: In
                  values:
                  - bankshield-api
              topologyKey: kubernetes.io/hostname
      containers:
      - name: api
        image: bankshield/api:1.0.0
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 8080
          name: http
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod,k8s"
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: bankshield-secrets
              key: db-password
        - name: REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: bankshield-secrets
              key: redis-password
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: bankshield-secrets
              key: jwt-secret
        - name: MASTER_KEY
          valueFrom:
            secretKeyRef:
              name: bankshield-secrets
              key: master-key
        volumeMounts:
        - name: config-volume
          mountPath: /app/config
        - name: logs-volume
          mountPath: /app/logs
        resources:
          requests:
            memory: "2Gi"
            cpu: "1000m"
          limits:
            memory: "4Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 3
        startupProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 30
      volumes:
      - name: config-volume
        configMap:
          name: bankshield-config
      - name: logs-volume
        emptyDir: {}
      restartPolicy: Always
---
apiVersion: v1
kind: Service
metadata:
  name: bankshield-api-service
  namespace: bankshield-prod
  labels:
    app: bankshield-api
spec:
  selector:
    app: bankshield-api
  ports:
  - name: http
    port: 80
    targetPort: 8080
    protocol: TCP
  type: ClusterIP
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: bankshield-api-ingress
  namespace: bankshield-prod
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/backend-protocol: "HTTP"
    nginx.ingress.kubernetes.io/proxy-connect-timeout: "30"
    nginx.ingress.kubernetes.io/proxy-send-timeout: "60"
    nginx.ingress.kubernetes.io/proxy-read-timeout: "60"
    nginx.ingress.kubernetes.io/client-max-body-size: "10m"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  ingressClassName: nginx
  tls:
  - hosts:
    - api.bankshield.com
    secretName: bankshield-api-tls
  rules:
  - host: api.bankshield.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: bankshield-api-service
            port:
              number: 80
```

## 4. 数据库迁移

### 4.1 Flyway配置
```sql
-- V1.0__init_database.sql
CREATE DATABASE IF NOT EXISTS bankshield CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE bankshield;

-- 用户表
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    status TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 加密密钥表
CREATE TABLE encryption_keys (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    key_name VARCHAR(100) NOT NULL,
    key_value TEXT NOT NULL,
    key_version INT DEFAULT 1,
    algorithm VARCHAR(50) DEFAULT 'AES',
    key_size INT DEFAULT 256,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    status TINYINT DEFAULT 1,
    INDEX idx_key_name (key_name),
    INDEX idx_status (status),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 审计日志表
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50),
    resource_id VARCHAR(100),
    details JSON,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_created_at (created_at),
    INDEX idx_resource (resource_type, resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

```sql
-- V1.1__add_encrypt_tables.sql
-- 敏感数据表
CREATE TABLE sensitive_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    data_type VARCHAR(50) NOT NULL,
    encrypted_data TEXT NOT NULL,
    encryption_key_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_data_type (data_type),
    INDEX idx_key_id (encryption_key_id),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (encryption_key_id) REFERENCES encryption_keys(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 数据分类表
CREATE TABLE data_classification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(100) NOT NULL,
    column_name VARCHAR(100) NOT NULL,
    classification_level VARCHAR(20) NOT NULL,
    encryption_required BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_table_column (table_name, column_name),
    INDEX idx_classification (classification_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 4.2 迁移脚本
```bash
#!/bin/bash
# migrate.sh

set -e

DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-3306}
DB_NAME=${DB_NAME:-bankshield}
DB_USER=${DB_USER:-root}
DB_PASSWORD=${DB_PASSWORD}

if [ -z "$DB_PASSWORD" ]; then
    echo "错误: DB_PASSWORD环境变量未设置"
    exit 1
fi

echo "开始数据库迁移..."

# Flyway命令
flyway \
    -url="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=true&characterEncoding=utf8" \
    -user="${DB_USER}" \
    -password="${DB_PASSWORD}" \
    -locations="filesystem:./sql/migration" \
    -baselineOnMigrate=true \
    -validateOnMigrate=true \
    -outOfOrder=false \
    migrate

echo "数据库迁移完成"

# 验证迁移结果
echo "验证数据库结构..."
mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} -p${DB_PASSWORD} ${DB_NAME} -e "
SELECT 
    table_name,
    table_rows,
    table_comment
FROM information_schema.tables 
WHERE table_schema = '${DB_NAME}' 
ORDER BY table_name;
"

echo "验证通过"
```

## 5. 配置参数

### 5.1 生产环境配置
```yaml
# application-prod.yml
spring:
  application:
    name: bankshield-api
  
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:bankshield}?useSSL=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: ${DB_USER:root}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 100
      minimum-idle: 20
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
      validation-timeout: 5000
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
  
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD}
    timeout: 2000ms
    database: 0
    ssl: true
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
        max-wait: 1000ms
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
        use_sql_comments: true
        jdbc:
          batch_size: 25
          time_zone: Asia/Shanghai
    open-in-view: false
  
  security:
    jwt:
      secret: ${JWT_SECRET}
      access-token-expiration: 1800  # 30分钟
      refresh-token-expiration: 604800  # 7天
      issuer: bankshield.com
      audience: bankshield-api
  
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1小时
      cache-null-values: false
  
  logging:
    level:
      root: WARN
      com.bankshield: INFO
      org.springframework.web: WARN
      org.hibernate.SQL: WARN
      org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    pattern:
      console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
      file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
    file:
      name: /app/logs/bankshield-api.log
      max-size: 100MB
      max-history: 30
      total-size-cap: 3GB

# 加密配置
encrypt:
  master-key: ${MASTER_KEY}  # 从Vault获取
  key-rotation-days: 90
  algorithm: AES/GCM/NoPadding
  key-size: 256
  salt-length: 16
  iv-length: 12
  tag-length: 128

# 监控配置
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
      show-components: always
    metrics:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
      percentiles:
        http.server.requests: 0.5,0.95,0.99
      slo:
        http.server.requests: 50ms,100ms,200ms,500ms
  
  tracing:
    sampling:
      probability: 0.1  # 10%采样率

# 自定义配置
bankshield:
  security:
    rate-limit:
      enabled: true
      requests-per-minute: 100
      burst-capacity: 20
    cors:
      allowed-origins: https://bankshield.com,https://admin.bankshield.com
      allowed-methods: GET,POST,PUT,DELETE,OPTIONS
      allowed-headers: "*"
      allow-credentials: true
  
  audit:
    enabled: true
    log-sensitive-data: false
    retention-days: 2555  # 7年
  
  data-classification:
    enabled: true
    auto-scan: true
    scan-interval: 24h
  
  notification:
    enabled: true
    channels:
      - type: email
        recipients: security@bankshield.com,devops@bankshield.com
      - type: webhook
        url: https://hooks.slack.com/services/xxx

# 服务器配置
server:
  port: 8080
  servlet:
    context-path: /
  compression:
    enabled: true
    mime-types: text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json
    min-response-size: 1024
  tomcat:
    max-connections: 8192
    accept-count: 100
    max-threads: 200
    min-spare-threads: 10
    connection-timeout: 20000ms
```

### 5.2 环境特定配置
```yaml
# application-k8s.yml (Kubernetes环境)
spring:
  datasource:
    url: jdbc:mysql://mysql-service:3306/bankshield?useSSL=true&characterEncoding=utf8
    username: bankshield
  redis:
    host: redis-service
  
  cloud:
    kubernetes:
      discovery:
        enabled: true
      config:
        enabled: true
        sources:
          - name: bankshield-config
            namespace: bankshield-prod

# Kubernetes特定配置
kubernetes:
  namespace: bankshield-prod
  service-account: bankshield-api
  config-map: bankshield-config
  secrets: bankshield-secrets

# 服务发现
eureka:
  client:
    enabled: false
```

## 6. 部署验证

### 6.1 健康检查
```bash
#!/bin/bash
# health-check.sh

API_URL="http://localhost:8080"
MAX_RETRIES=30
RETRY_INTERVAL=10

echo "开始健康检查..."

# 检查应用健康状态
check_health() {
    local endpoint=$1
    local expected_status=$2
    local retry_count=0
    
    while [ $retry_count -lt $MAX_RETRIES ]; do
        response=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL$endpoint")
        
        if [ "$response" = "$expected_status" ]; then
            echo "✓ $endpoint 健康检查通过 (HTTP $response)"
            return 0
        fi
        
        echo "  等待 $endpoint 就绪... (HTTP $response)"
        sleep $RETRY_INTERVAL
        retry_count=$((retry_count + 1))
    done
    
    echo "✗ $endpoint 健康检查失败"
    return 1
}

# 执行各项检查
check_health "/actuator/health" "200" || exit 1
check_health "/actuator/info" "200" || exit 1
check_health "/actuator/metrics" "200" || exit 1

# 检查数据库连接
db_status=$(curl -s "$API_URL/actuator/health" | jq -r '.components.db.status')
if [ "$db_status" = "UP" ]; then
    echo "✓ 数据库连接正常"
else
    echo "✗ 数据库连接异常: $db_status"
    exit 1
fi

# 检查Redis连接
redis_status=$(curl -s "$API_URL/actuator/health" | jq -r '.components.redis.status')
if [ "$redis_status" = "UP" ]; then
    echo "✓ Redis连接正常"
else
    echo "✗ Redis连接异常: $redis_status"
    exit 1
fi

echo "所有健康检查通过!"
```

### 6.2 性能测试
```bash
#!/bin/bash
# performance-test.sh

API_URL="http://localhost:8080"
CONCURRENT_USERS=100
RAMP_UP_TIME=30
DURATION=300

# 使用Apache Bench进行简单压测
echo "开始性能测试..."
echo "并发用户数: $CONCURRENT_USERS"
echo "测试持续时间: ${DURATION}秒"

# 测试API响应时间
echo "测试API响应时间..."
ab -n 1000 -c $CONCURRENT_USERS -t $DURATION \
   -H "Content-Type: application/json" \
   "$API_URL/actuator/health"

# 测试数据库查询性能
echo "测试数据库查询性能..."
ab -n 500 -c 50 -t 120 \
   -H "Authorization: Bearer $TEST_TOKEN" \
   "$API_URL/api/v1/users?page=0&size=20"

echo "性能测试完成"
```

### 6.3 安全检查
```bash
#!/bin/bash
# security-check.sh

API_URL="http://localhost:8080"

echo "开始安全检查..."

# 检查SSL配置
echo "检查SSL配置..."
if curl -s -I "$API_URL" | grep -q "Strict-Transport-Security"; then
    echo "✓ HSTS头已配置"
else
    echo "✗ HSTS头缺失"
fi

# 检查CORS配置
echo "检查CORS配置..."
cors_headers=$(curl -s -I -X OPTIONS "$API_URL/api/v1/health" | grep -i "access-control")
if [ -n "$cors_headers" ]; then
    echo "✓ CORS头已配置"
else
    echo "✗ CORS头缺失"
fi

# 检查安全头
echo "检查安全头..."
security_headers=$(curl -s -I "$API_URL" | grep -E "(X-Content-Type-Options|X-Frame-Options|X-XSS-Protection)")
echo "$security_headers"

# 检查SQL注入防护
echo "测试SQL注入防护..."
sql_test=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL/api/v1/users?name=test' OR '1'='1")
if [ "$sql_test" = "400" ] || [ "$sql_test" = "403" ]; then
    echo "✓ SQL注入防护正常"
else
    echo "✗ SQL注入防护可能存在漏洞"
fi

echo "安全检查完成"
```

## 7. 故障排查

### 7.1 常见问题
```markdown
### 应用启动失败
**症状**: 容器不断重启，健康检查失败
**排查步骤**:
1. 检查日志: `kubectl logs -f deployment/bankshield-api`
2. 检查事件: `kubectl describe pod <pod-name>`
3. 检查资源限制: `kubectl top pods`
4. 检查依赖服务: 数据库、Redis连接

**常见原因**:
- 数据库连接失败
- 内存不足导致OOM
- 配置错误
- 镜像拉取失败

### 数据库连接超时
**症状**: 应用响应慢，数据库连接池耗尽
**解决方案**:
1. 增加连接池大小
2. 优化慢查询
3. 增加数据库资源
4. 启用连接池监控

### 内存泄漏
**症状**: 内存使用持续增长，频繁GC
**排查工具**:
- jstat -gc <pid>
- jmap -dump:format=b,file=heap.hprof <pid>
- MAT分析工具
```

### 7.2 日志分析
```bash
#!/bin/bash
# log-analysis.sh

# 查看错误日志
echo "查看错误日志..."
grep -i "error\|exception\|fail" /app/logs/bankshield-api.log | tail -20

# 查看慢查询
echo "查看慢查询..."
grep -i "slow\|timeout" /app/logs/bankshield-api.log | tail -10

# 查看GC日志
echo "查看GC情况..."
grep -i "gc\|full gc" /app/logs/gc.log | tail -10

# 统计HTTP状态码
echo "HTTP状态码统计..."
grep "POST\|GET" /app/logs/bankshield-api.log | \
    awk '{print $9}' | sort | uniq -c | sort -nr

# 查看数据库连接
echo "数据库连接状态..."
mysql -u${DB_USER} -p${DB_PASSWORD} -e "
SHOW PROCESSLIST;
SHOW VARIABLES LIKE 'max_connections';
SHOW STATUS LIKE 'Threads_connected';
"
```

### 7.3 监控告警
```yaml
# alert-rules.yaml
groups:
- name: bankshield-alerts
  rules:
  - alert: BankShieldAPIDown
    expr: up{job="bankshield-api"} == 0
    for: 1m
    labels:
      severity: critical
    annotations:
      summary: "BankShield API服务不可用"
      description: "{{ $labels.instance }} 的API服务已停止响应超过1分钟"
  
  - alert: BankShieldHighErrorRate
    expr: rate(http_requests_total{job="bankshield-api",status=~"5.."}[5m]) > 0.1
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "BankShield API错误率过高"
      description: "{{ $labels.instance }} 的错误率超过10%"
  
  - alert: BankShieldHighLatency
    expr: histogram_quantile(0.95, rate(http_request_duration_seconds_bucket{job="bankshield-api"}[5m])) > 0.5
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "BankShield API响应延迟过高"
      description: "{{ $labels.instance }} 的95%分位延迟超过500ms"
  
  - alert: BankShieldDatabaseDown
    expr: mysql_up{job="mysql"} == 0
    for: 1m
    labels:
      severity: critical
    annotations:
      summary: "BankShield数据库不可用"
      description: "MySQL数据库已停止响应"
  
  - alert: BankShieldRedisDown
    expr: redis_up{job="redis"} == 0
    for: 1m
    labels:
      severity: critical
    annotations:
      summary: "BankShield Redis不可用"
      description: "Redis服务已停止响应"
  
  - alert: BankShieldHighMemoryUsage
    expr: (jvm_memory_used_bytes{job="bankshield-api",area="heap"} / jvm_memory_max_bytes{job="bankshield-api",area="heap"}) > 0.9
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "BankShield内存使用率过高"
      description: "{{ $labels.instance }} 的堆内存使用率超过90%"
```

### 7.4 应急处理
```bash
#!/bin/bash
# emergency-response.sh

# 紧急重启服务
emergency_restart() {
    echo "执行紧急重启..."
    kubectl delete pods -l app=bankshield-api -n bankshield-prod
    sleep 30
    kubectl rollout status deployment/bankshield-api -n bankshield-prod
}

# 回滚到上一个版本
rollback() {
    echo "执行版本回滚..."
    kubectl rollout undo deployment/bankshield-api -n bankshield-prod
    kubectl rollout status deployment/bankshield-api -n bankshield-prod
}

# 扩容应对高负载
scale_up() {
    echo "执行紧急扩容..."
    kubectl scale deployment/bankshield-api --replicas=10 -n bankshield-prod
}

# 缩容节省资源
scale_down() {
    echo "执行缩容..."
    kubectl scale deployment/bankshield-api --replicas=3 -n bankshield-prod
}

# 清理日志
cleanup_logs() {
    echo "清理日志文件..."
    find /app/logs -name "*.log" -mtime +7 -delete
    find /app/logs -name "*.gz" -mtime +30 -delete
}

# 主菜单
case "$1" in
    restart)
        emergency_restart
        ;;
    rollback)
        rollback
        ;;
    scale-up)
        scale_up
        ;;
    scale-down)
        scale_down
        ;;
    cleanup)
        cleanup_logs
        ;;
    *)
        echo "用法: $0 {restart|rollback|scale-up|scale-down|cleanup}"
        exit 1
        ;;
esac
```

## 8. 维护操作

### 8.1 定期维护任务
```bash
#!/bin/bash
# maintenance.sh

# 每日维护任务
daily_maintenance() {
    echo "执行每日维护任务..."
    
    # 备份数据库
    backup_database
    
    # 清理过期日志
    cleanup_expired_logs
    
    # 检查磁盘空间
    check_disk_space
    
    # 检查证书有效期
    check_certificate_expiry
}

# 每周维护任务
weekly_maintenance() {
    echo "执行每周维护任务..."
    
    # 全量备份
    full_backup
    
    # 性能报告
    generate_performance_report
    
    # 安全扫描
    security_scan
}

# 每月维护任务
monthly_maintenance() {
    echo "执行每月维护任务..."
    
    # 密钥轮换
    rotate_encryption_keys
    
    # 清理过期数据
    cleanup_expired_data
    
    # 容量规划报告
    capacity_planning_report
}

# 数据库备份
backup_database() {
    echo "备份数据库..."
    BACKUP_DIR="/backup/mysql/$(date +%Y%m%d)"
    mkdir -p $BACKUP_DIR
    
    mysqldump -u${DB_USER} -p${DB_PASSWORD} --single-transaction --routines --triggers bankshield > \
        $BACKUP_DIR/bankshield_$(date +%Y%m%d_%H%M%S).sql
    
    # 压缩备份文件
    gzip $BACKUP_DIR/*.sql
    
    # 上传到远程存储
    aws s3 sync $BACKUP_DIR s3://bankshield-backup/mysql/$(date +%Y%m%d)/
}

# 主执行逻辑
case "$1" in
    daily)
        daily_maintenance
        ;;
    weekly)
        weekly_maintenance
        ;;
    monthly)
        monthly_maintenance
        ;;
    *)
        echo "用法: $0 {daily|weekly|monthly}"
        exit 1
        ;;
esac
```

## 9. 文档版本

| 版本 | 日期 | 作者 | 变更描述 |
|------|------|------|----------|
| v1.0.0 | 2024-01-01 | DevOps团队 | 初始版本 |
| v1.0.1 | 2024-01-15 | DevOps团队 | 添加K8s部署配置 |
| v1.0.2 | 2024-02-01 | DevOps团队 | 更新监控配置 |
| v1.1.0 | 2024-03-01 | DevOps团队 | 增加安全扫描和故障排查 |

---

**维护团队**: BankShield DevOps <devops@bankshield.com>
**更新频率**: 每季度更新
**审核周期**: 每月审核