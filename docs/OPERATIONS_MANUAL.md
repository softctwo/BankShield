# BankShield 运维手册

## 📋 目录

1. [系统架构](#系统架构)
2. [部署指南](#部署指南)
3. [监控告警](#监控告警)
4. [故障排查](#故障排查)
5. [性能优化](#性能优化)
6. [备份恢复](#备份恢复)
7. [安全加固](#安全加固)
8. [日常维护](#日常维护)

---

## 系统架构

### 组件清单

| 组件 | 版本 | 端口 | 用途 |
|------|------|------|------|
| MySQL | 8.0 | 3306 | 数据存储 |
| Redis | 6.0+ | 6379 | 缓存和会话 |
| Spring Boot | 2.7.18 | 8080 | 后端服务 |
| Vue 3 | 3.5.26 | 5173 | 前端应用 |
| Nginx | 1.24+ | 80/443 | 反向代理 |

### 服务依赖关系

```
前端(Vue) → Nginx → 后端(Spring Boot) → MySQL
                                      ↓
                                    Redis
```

---

## 部署指南

### 1. 环境准备

**系统要求**:
- 操作系统: CentOS 7+/Ubuntu 20.04+
- CPU: 4核心+
- 内存: 8GB+
- 磁盘: 100GB+

**软件要求**:
```bash
# 安装Java
sudo yum install java-11-openjdk java-11-openjdk-devel

# 安装MySQL
sudo yum install mysql-server
sudo systemctl start mysqld
sudo systemctl enable mysqld

# 安装Redis
sudo yum install redis
sudo systemctl start redis
sudo systemctl enable redis

# 安装Nginx
sudo yum install nginx
sudo systemctl start nginx
sudo systemctl enable nginx
```

### 2. 数据库初始化

```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE bankshield DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入SQL脚本
mysql -u root -p bankshield < sql/init_database.sql
mysql -u root -p bankshield < sql/compliance_check.sql
mysql -u root -p bankshield < sql/security_threat.sql
```

### 3. 后端部署

```bash
# 构建项目
cd bankshield-api
mvn clean package -DskipTests

# 创建部署目录
sudo mkdir -p /opt/bankshield
sudo cp target/bankshield-api-1.0.0-SNAPSHOT.jar /opt/bankshield/

# 创建启动脚本
sudo cat > /opt/bankshield/start.sh << 'EOF'
#!/bin/bash
java -Xms2g -Xmx4g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -Dspring.profiles.active=prod \
     -jar /opt/bankshield/bankshield-api-1.0.0-SNAPSHOT.jar \
     > /opt/bankshield/logs/app.log 2>&1 &
EOF

sudo chmod +x /opt/bankshield/start.sh

# 创建systemd服务
sudo cat > /etc/systemd/system/bankshield.service << 'EOF'
[Unit]
Description=BankShield Application
After=network.target mysql.service redis.service

[Service]
Type=forking
User=bankshield
ExecStart=/opt/bankshield/start.sh
ExecStop=/bin/kill -15 $MAINPID
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# 启动服务
sudo systemctl daemon-reload
sudo systemctl start bankshield
sudo systemctl enable bankshield
```

### 4. 前端部署

```bash
# 构建前端
cd bankshield-ui
npm install
npm run build

# 部署到Nginx
sudo cp -r dist/* /usr/share/nginx/html/

# 配置Nginx
sudo cat > /etc/nginx/conf.d/bankshield.conf << 'EOF'
server {
    listen 80;
    server_name your-domain.com;
    
    root /usr/share/nginx/html;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
    
    location /ws/ {
        proxy_pass http://localhost:8080/ws/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
EOF

# 重启Nginx
sudo nginx -t
sudo systemctl reload nginx
```

---

## 监控告警

### 1. 系统监控

**CPU监控**:
```bash
# 查看CPU使用率
top -bn1 | grep "Cpu(s)"

# 查看进程CPU使用
ps aux --sort=-%cpu | head -10
```

**内存监控**:
```bash
# 查看内存使用
free -h

# 查看进程内存使用
ps aux --sort=-%mem | head -10
```

**磁盘监控**:
```bash
# 查看磁盘使用
df -h

# 查看磁盘IO
iostat -x 1
```

### 2. 应用监控

**健康检查**:
```bash
# 基础健康检查
curl http://localhost:8080/api/health

# 详细健康检查
curl http://localhost:8080/api/health/detailed

# 就绪检查
curl http://localhost:8080/api/health/ready

# 存活检查
curl http://localhost:8080/api/health/live
```

**日志监控**:
```bash
# 查看应用日志
tail -f /opt/bankshield/logs/app.log

# 查看错误日志
grep ERROR /opt/bankshield/logs/app.log | tail -100

# 统计错误数量
grep ERROR /opt/bankshield/logs/app.log | wc -l
```

### 3. 数据库监控

```sql
-- 查看连接数
SHOW STATUS LIKE 'Threads_connected';

-- 查看慢查询
SHOW VARIABLES LIKE 'slow_query_log';
SELECT * FROM mysql.slow_log ORDER BY start_time DESC LIMIT 10;

-- 查看锁等待
SHOW ENGINE INNODB STATUS;

-- 查看表大小
SELECT 
    table_name,
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS size_mb
FROM information_schema.TABLES
WHERE table_schema = 'bankshield'
ORDER BY size_mb DESC;
```

### 4. Redis监控

```bash
# 连接Redis
redis-cli

# 查看信息
INFO

# 查看内存使用
INFO memory

# 查看连接数
INFO clients

# 查看命中率
INFO stats
```

---

## 故障排查

### 1. 服务无法启动

**检查步骤**:
```bash
# 1. 查看服务状态
sudo systemctl status bankshield

# 2. 查看日志
sudo journalctl -u bankshield -n 100

# 3. 检查端口占用
sudo netstat -tulpn | grep 8080

# 4. 检查配置文件
cat /opt/bankshield/application.yml

# 5. 检查依赖服务
sudo systemctl status mysql
sudo systemctl status redis
```

**常见问题**:

**问题1**: 端口被占用
```bash
# 查找占用进程
sudo lsof -i :8080

# 杀死进程
sudo kill -9 <PID>
```

**问题2**: 数据库连接失败
```bash
# 检查MySQL状态
sudo systemctl status mysql

# 测试连接
mysql -h localhost -u root -p -e "SELECT 1"

# 检查防火墙
sudo firewall-cmd --list-all
```

**问题3**: 内存不足
```bash
# 查看内存使用
free -h

# 清理缓存
sudo sync && sudo echo 3 > /proc/sys/vm/drop_caches

# 调整JVM参数
# 编辑 /opt/bankshield/start.sh
# 减小 -Xmx 参数
```

### 2. 性能问题

**慢查询排查**:
```bash
# 开启慢查询日志
mysql -u root -p
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;

# 分析慢查询
mysqldumpslow -s t -t 10 /var/lib/mysql/slow.log
```

**高CPU排查**:
```bash
# 查看线程堆栈
jstack <PID> > thread_dump.txt

# 分析线程状态
grep -A 10 "BLOCKED" thread_dump.txt
```

**内存泄漏排查**:
```bash
# 生成堆转储
jmap -dump:format=b,file=heap_dump.hprof <PID>

# 使用MAT工具分析
# 下载Eclipse Memory Analyzer
```

### 3. 数据问题

**数据不一致**:
```sql
-- 检查数据完整性
SELECT COUNT(*) FROM table_name;

-- 检查重复数据
SELECT column_name, COUNT(*) 
FROM table_name 
GROUP BY column_name 
HAVING COUNT(*) > 1;

-- 修复数据
-- 根据具体情况编写修复SQL
```

**数据丢失**:
```bash
# 检查备份
ls -lh /backup/mysql/

# 恢复数据
mysql -u root -p bankshield < /backup/mysql/backup_20250104.sql
```

---

## 性能优化

### 1. 数据库优化

**索引优化**:
```sql
-- 查看索引使用情况
SHOW INDEX FROM table_name;

-- 添加索引
CREATE INDEX idx_column_name ON table_name(column_name);

-- 删除无用索引
DROP INDEX idx_unused ON table_name;
```

**查询优化**:
```sql
-- 使用EXPLAIN分析查询
EXPLAIN SELECT * FROM table_name WHERE condition;

-- 优化JOIN查询
-- 使用小表驱动大表
-- 确保JOIN字段有索引
```

**配置优化**:
```ini
# /etc/my.cnf
[mysqld]
innodb_buffer_pool_size = 4G
innodb_log_file_size = 512M
max_connections = 500
query_cache_size = 128M
```

### 2. Redis优化

**内存优化**:
```bash
# 设置最大内存
CONFIG SET maxmemory 2gb
CONFIG SET maxmemory-policy allkeys-lru

# 开启持久化
CONFIG SET save "900 1 300 10 60 10000"
```

**性能优化**:
```bash
# 禁用危险命令
rename-command FLUSHDB ""
rename-command FLUSHALL ""

# 开启慢日志
CONFIG SET slowlog-log-slower-than 10000
CONFIG SET slowlog-max-len 128
```

### 3. 应用优化

**JVM优化**:
```bash
# 优化GC参数
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1HeapRegionSize=16m
-XX:InitiatingHeapOccupancyPercent=45

# 优化堆大小
-Xms4g -Xmx4g

# 开启GC日志
-Xloggc:/opt/bankshield/logs/gc.log
-XX:+PrintGCDetails
-XX:+PrintGCDateStamps
```

**连接池优化**:
```yaml
spring:
  datasource:
    druid:
      initial-size: 10
      min-idle: 10
      max-active: 100
      max-wait: 60000
      validation-query: SELECT 1
```

---

## 备份恢复

### 1. 数据库备份

**全量备份**:
```bash
#!/bin/bash
# /opt/scripts/mysql_backup.sh

BACKUP_DIR="/backup/mysql"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/bankshield_$DATE.sql"

# 创建备份目录
mkdir -p $BACKUP_DIR

# 执行备份
mysqldump -u root -p3f342bb206 \
  --single-transaction \
  --routines \
  --triggers \
  --events \
  bankshield > $BACKUP_FILE

# 压缩备份
gzip $BACKUP_FILE

# 删除7天前的备份
find $BACKUP_DIR -name "*.sql.gz" -mtime +7 -delete

echo "备份完成: $BACKUP_FILE.gz"
```

**增量备份**:
```bash
# 开启binlog
# /etc/my.cnf
[mysqld]
log-bin=mysql-bin
binlog_format=ROW
expire_logs_days=7

# 备份binlog
mysqlbinlog --start-datetime="2025-01-04 00:00:00" \
            --stop-datetime="2025-01-04 23:59:59" \
            mysql-bin.000001 > incremental_backup.sql
```

### 2. 数据恢复

**全量恢复**:
```bash
# 解压备份
gunzip bankshield_20250104.sql.gz

# 恢复数据
mysql -u root -p bankshield < bankshield_20250104.sql
```

**增量恢复**:
```bash
# 恢复binlog
mysql -u root -p bankshield < incremental_backup.sql
```

### 3. Redis备份

**RDB备份**:
```bash
# 手动备份
redis-cli BGSAVE

# 自动备份
# redis.conf
save 900 1
save 300 10
save 60 10000

# 复制RDB文件
cp /var/lib/redis/dump.rdb /backup/redis/dump_$(date +%Y%m%d).rdb
```

**AOF备份**:
```bash
# 开启AOF
# redis.conf
appendonly yes
appendfilename "appendonly.aof"

# 备份AOF
cp /var/lib/redis/appendonly.aof /backup/redis/
```

---

## 安全加固

### 1. 系统安全

**防火墙配置**:
```bash
# 开启防火墙
sudo systemctl start firewalld
sudo systemctl enable firewalld

# 允许必要端口
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --permanent --add-port=443/tcp
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload
```

**SSH加固**:
```bash
# 编辑SSH配置
sudo vi /etc/ssh/sshd_config

# 禁用root登录
PermitRootLogin no

# 禁用密码登录
PasswordAuthentication no

# 修改默认端口
Port 2222

# 重启SSH
sudo systemctl restart sshd
```

### 2. 数据库安全

```sql
-- 删除匿名用户
DELETE FROM mysql.user WHERE User='';

-- 删除test数据库
DROP DATABASE IF EXISTS test;

-- 限制root远程登录
DELETE FROM mysql.user WHERE User='root' AND Host NOT IN ('localhost', '127.0.0.1');

-- 创建应用专用用户
CREATE USER 'bankshield'@'localhost' IDENTIFIED BY 'strong_password';
GRANT SELECT, INSERT, UPDATE, DELETE ON bankshield.* TO 'bankshield'@'localhost';

-- 刷新权限
FLUSH PRIVILEGES;
```

### 3. Redis安全

```bash
# redis.conf

# 设置密码
requirepass your_strong_password

# 绑定IP
bind 127.0.0.1

# 禁用危险命令
rename-command FLUSHDB ""
rename-command FLUSHALL ""
rename-command CONFIG ""
```

---

## 日常维护

### 1. 日志管理

**日志轮转**:
```bash
# /etc/logrotate.d/bankshield
/opt/bankshield/logs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 0644 bankshield bankshield
    postrotate
        /bin/kill -USR1 $(cat /opt/bankshield/app.pid 2>/dev/null) 2>/dev/null || true
    endscript
}
```

### 2. 定期任务

**Crontab配置**:
```bash
# 编辑crontab
crontab -e

# 每天凌晨2点备份数据库
0 2 * * * /opt/scripts/mysql_backup.sh

# 每小时检查服务状态
0 * * * * /opt/scripts/health_check.sh

# 每天清理临时文件
0 3 * * * find /tmp -name "bankshield_*" -mtime +7 -delete
```

### 3. 性能监控

**监控脚本**:
```bash
#!/bin/bash
# /opt/scripts/monitor.sh

# 检查CPU
CPU_USAGE=$(top -bn1 | grep "Cpu(s)" | awk '{print $2}' | cut -d'%' -f1)
if (( $(echo "$CPU_USAGE > 80" | bc -l) )); then
    echo "WARNING: High CPU usage: $CPU_USAGE%"
fi

# 检查内存
MEM_USAGE=$(free | grep Mem | awk '{print ($3/$2) * 100.0}')
if (( $(echo "$MEM_USAGE > 80" | bc -l) )); then
    echo "WARNING: High memory usage: $MEM_USAGE%"
fi

# 检查磁盘
DISK_USAGE=$(df -h / | tail -1 | awk '{print $5}' | cut -d'%' -f1)
if [ $DISK_USAGE -gt 80 ]; then
    echo "WARNING: High disk usage: $DISK_USAGE%"
fi
```

---

## 附录

### A. 常用命令

```bash
# 启动服务
sudo systemctl start bankshield

# 停止服务
sudo systemctl stop bankshield

# 重启服务
sudo systemctl restart bankshield

# 查看状态
sudo systemctl status bankshield

# 查看日志
sudo journalctl -u bankshield -f

# 重新加载配置
sudo systemctl reload bankshield
```

### B. 联系方式

- **技术支持**: support@bankshield.com
- **紧急热线**: 400-xxx-xxxx
- **文档地址**: https://docs.bankshield.com

---

**文档版本**: v1.0  
**最后更新**: 2025-01-04  
**维护者**: BankShield运维团队
