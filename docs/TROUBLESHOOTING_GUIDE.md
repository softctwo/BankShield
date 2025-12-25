# BankShield 故障排查指南

**版本**: v1.0.0
**更新日期**: 2025-12-25
**适用场景**: 生产环境故障处理

---

## 📋 目录

- [故障处理流程](#故障处理流程)
- [常见故障及解决方案](#常见故障及解决方案)
- [性能问题排查](#性能问题排查)
- [安全问题排查](#安全问题排查)
- [数据问题排查](#数据问题排查)
- [日志分析](#日志分析)
- [应急处理](#应急处理)

---

## 🔄 故障处理流程

### 标准故障处理流程

```
故障发现 → 问题定级 → 应急处理 → 根因分析 → 解决方案 → 复盘总结
```

### 故障分级

| 等级 | 影响 | 响应时间 | 恢复时间 | 示例 |
|------|------|----------|----------|------|
| P0 | 核心业务不可用 | 5分钟 | 30分钟 | 系统完全宕机 |
| P1 | 重要功能不可用 | 15分钟 | 2小时 | 数据库连接失败 |
| P2 | 部分功能异常 | 1小时 | 4小时 | 某接口报错 |
| P3 | 性能下降 | 2小时 | 8小时 | 响应时间变慢 |
| P4 | 轻微影响 | 4小时 | 24小时 | 日志堆积 |

### 应急联系人

| 角色 | 姓名 | 电话 | 邮箱 | 负责范围 |
|------|------|------|------|----------|
| 技术负责人 | 张三 | 138****0001 | zhangsan@bankshield.com | 技术决策 |
| 运维负责人 | 李四 | 138****0002 | lisi@bankshield.com | 基础设施 |
| 安全负责人 | 王五 | 138****0003 | wangwu@bankshield.com | 安全事件 |
| 业务负责人 | 赵六 | 138****0004 | zhaoliu@bankshield.com | 业务影响 |

---

## 🔧 常见故障及解决方案

### 1. 应用启动失败

#### 故障现象

```bash
# 应用启动报错
Error: Failed to start ApplicationContext
```

#### 排查步骤

```bash
# 1. 检查JDK版本
java -version
# 要求: JDK 1.8+

# 2. 检查端口占用
netstat -tulnp | grep 8080
# 或
lsof -i :8080

# 3. 检查数据库连接
mysql -h localhost -P 3306 -u root -p -e "SELECT 1"

# 4. 检查Redis连接
redis-cli ping

# 5. 查看详细错误日志
tail -n 100 logs/api.log | grep -A 20 "Error"
```

#### 解决方案

**问题1：端口被占用**
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

**问题2：JDK版本不匹配**
```bash
# 安装正确的JDK版本
sudo yum install -y java-1.8.0-openjdk

# 设置环境变量
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk
export PATH=$JAVA_HOME/bin:$PATH

# 验证版本
java -version
```

**问题3：依赖缺失**
```bash
# 重新构建项目
mvn clean install -DskipTests

# 或下载依赖
mvn dependency:go-offline
```

---

### 2. 数据库连接失败

#### 故障现象

```bash
# 应用日志显示
com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure
```

#### 排查步骤

```bash
# 1. 检查MySQL服务状态
systemctl status mysql
# 或Docker环境
docker ps | grep mysql

# 2. 检查MySQL进程
ps aux | grep mysql

# 3. 测试网络连接
telnet localhost 3306
# 或
nc -zv localhost 3306

# 4. 检查配置文件
cat bankshield-api/src/main/resources/application-dev.yml | grep -A 5 datasource

# 5. 查看MySQL错误日志
tail -f /var/log/mysql/error.log
```

#### 解决方案

**问题1：MySQL服务未启动**
```bash
# 启动MySQL
sudo systemctl start mysql

# 或Docker环境
docker start bankshield-mysql
```

**问题2：连接数达到上限**
```bash
# 查看当前连接数
mysql -u root -p -e "SHOW STATUS LIKE 'Threads_connected';"

# 查看最大连接数
mysql -u root -p -e "SHOW VARIABLES LIKE 'max_connections';"

# 增加连接数
mysql -u root -p -e "SET GLOBAL max_connections = 500;"

# 或修改配置文件
# /etc/mysql/mysql.conf.d/mysqld.cnf
[mysqld]
max_connections = 500
```

**问题3：连接池配置不当**
```yaml
# 调整连接池配置
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

---

### 3. Redis连接超时

#### 故障现象

```bash
# 应用日志显示
io.lettuce.core.RedisConnectionTimeoutException: Command timed out
```

#### 排查步骤

```bash
# 1. 检查Redis服务
redis-cli ping

# 2. 检查Redis连接数
redis-cli info clients

# 3. 检查内存使用
redis-cli info memory

# 4. 检查慢查询
redis-cli slowlog get 10

# 5. 检查网络延迟
ping redis-host
```

#### 解决方案

**问题1：Redis内存不足**
```bash
# 查看内存使用
redis-cli INFO memory | grep used_memory_human

# 设置最大内存
redis-cli CONFIG SET maxmemory 4gb
redis-cli CONFIG SET maxmemory-policy allkeys-lru

# 清理无用数据
redis-cli FLUSHDB  # 谨慎操作！
```

**问题2：连接数过多**
```bash
# 查看连接数
redis-cli info clients

# 查看最大连接数
redis-cli config get maxclients

# 增加最大连接数
redis-cli config set maxclients 10000
```

**问题3：Lettuce连接池配置**
```yaml
# 调整连接池配置
spring:
  redis:
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms
    timeout: 3000
```

---

### 4. 接口响应慢

#### 故障现象

- 页面加载超过5秒
- API接口响应时间 > 2秒
- 用户体验明显下降

#### 排查步骤

```bash
# 1. 查看应用性能指标
curl http://localhost:8080/api/actuator/metrics/http.server.requests

# 2. 查看JVM状态
jstat -gcutil <PID> 1000 10

# 3. 查看线程状态
jstack <PID> | grep -A 10 "java.lang.Thread.State: RUNNABLE"

# 4. 查看数据库慢查询
# Druid监控: http://localhost:8080/api/druid/sql.html

# 5. 分析慢查询日志
mysqldumpslow -s t -t 10 /var/log/mysql/mysql-slow.log
```

#### 解决方案

**问题1：数据库慢查询**
```sql
-- 分析慢查询
SELECT * FROM sys_user WHERE SUBSTRING(username, 1, 3) = 'adm';

-- 优化：添加索引
CREATE INDEX idx_username ON sys_user(username);

-- 优化：避免函数
SELECT * FROM sys_user WHERE username LIKE 'adm%';
```

**问题2：N+1查询问题**
```java
// 问题代码
List<User> users = userMapper.selectList(null);
for (User user : users) {
    List<Role> roles = roleMapper.selectByUserId(user.getId()); // N+1问题
}

// 优化方案：使用JOIN
@Select("SELECT u.*, r.* FROM sys_user u " +
        "LEFT JOIN sys_user_role ur ON u.id = ur.user_id " +
        "LEFT JOIN sys_role r ON ur.role_id = r.id " +
        "WHERE u.id = #{userId}")
List<UserWithRoles> selectUserWithRoles(@Param("userId") Long userId);
```

**问题3：JVM内存不足**
```bash
# 调整JVM参数
java -jar \
  -Xms2g -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=100 \
  -XX:+UseStringDeduplication \
  -Xloggc:/var/log/bankshield/gc.log \
  bankshield-api.jar
```

---

### 5. 内存溢出（OOM）

#### 故障现象

```bash
# 应用崩溃
java.lang.OutOfMemoryError: Java heap space
```

#### 排查步骤

```bash
# 1. 查看JVM堆内存
jmap -heap <PID>

# 2. 导出堆转储
jmap -dump:format=b,file=heap.hprof <PID>

# 3. 分析堆转储
jhat heap.hprof
# 访问: http://localhost:7000

# 4. 查看GC日志
tail -f /var/log/bankshield/gc.log
```

#### 解决方案

**问题1：堆内存不足**
```bash
# 增加堆内存
java -jar -Xms4g -Xmx4g bankshield-api.jar

# 或在应用配置中
JAVA_OPTS="-Xms4g -Xmx4g"
```

**问题2：内存泄漏**
```java
// 问题代码：缓存未清理
public class CacheService {
    private static final Map<String, Object> CACHE = new HashMap<>(); // 静态Map永不清理

    public void put(String key, Object value) {
        CACHE.put(key, value);
    }
}

// 优化方案：使用LRU缓存
public class CacheService {
    private final Cache<String, Object> CACHE = Caffeine.newBuilder()
        .maximumSize(10000)
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build();

    public void put(String key, Object value) {
        CACHE.put(key, value);
    }
}
```

**问题3：大对象未释放**
```java
// 问题代码
public List<Data> loadAllData() {
    List<Data> result = new ArrayList<>();
    // 加载百万条数据到内存
    result.addAll(dataMapper.selectAll());
    return result;
}

// 优化方案：分页查询
public Page<Data> loadData(int page, int size) {
    Page<Data> pageParam = new Page<>(page, size);
    return dataMapper.selectPage(pageParam, null);
}
```

---

### 6. CPU使用率过高

#### 故障现象

```bash
# CPU使用率持续>80%
top -p <PID>
# PID %CPU
# 1234  95.2
```

#### 排查步骤

```bash
# 1. 查看CPU使用率
top -p <PID>

# 2. 查看线程CPU使用
top -H -p <PID>

# 3. 查看线程堆栈
jstack <PID> | grep -A 20 "tid=0x<thread_id>"

# 4. 查看应用监控
curl http://localhost:8080/api/actuator/metrics/process.cpu.usage
```

#### 解决方案

**问题1：死循环**
```java
// 问题代码
while (true) {
    // 无限循环
}

// 优化方案：添加退出条件
int count = 0;
while (count < 10000) {
    // 处理逻辑
    count++;
}
```

**问题2：过度GC**
```bash
# 调整GC参数
java -jar \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=100 \
  -XX:InitiatingHeapOccupancyPercent=45 \
  bankshield-api.jar
```

**问题3：频繁创建对象**
```java
// 问题代码：循环中创建对象
for (int i = 0; i < 100000; i++) {
    Date date = new Date(); // 每次循环创建对象
}

// 优化方案：复用对象
Date date = new Date();
for (int i = 0; i < 100000; i++) {
    date.setTime(System.currentTimeMillis());
}
```

---

### 7. 磁盘空间不足

#### 故障现象

```bash
# 磁盘使用率>90%
df -h
# Filesystem      Size  Used Avail Use% Mounted on
# /dev/sda1      100G  95G   5G  95% /
```

#### 排查步骤

```bash
# 1. 查看磁盘使用
df -h

# 2. 查看大文件
find / -type f -size +1G 2>/dev/null | head -10

# 3. 查看日志大小
du -sh /var/log/bankshield/

# 4. 查看数据库文件大小
du -sh /var/lib/mysql/bankshield/
```

#### 解决方案

**问题1：日志文件过大**
```bash
# 清理旧日志
find /var/log/bankshield/ -name "*.log.*" -mtime +7 -delete

# 配置日志滚动
# logback.xml
<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>/var/log/bankshield/api.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>/var/log/bankshield/api.%d{yyyy-MM-dd}.log</fileNamePattern>
        <maxHistory>7</maxHistory>
    </rollingPolicy>
</appender>
```

**问题2：数据库binlog过多**
```bash
# 清理binlog
mysql -u root -p -e "PURGE BINARY LOGS BEFORE DATE(NOW() - INTERVAL 7 DAY);"

# 或修改配置
# /etc/mysql/mysql.conf.d/mysqld.cnf
[mysqld]
expire_logs_days = 7
max_binlog_size = 100M
```

**问题3：Docker镜像占用空间**
```bash
# 清理未使用的镜像
docker image prune -a

# 清理未使用的容器
docker container prune

# 清理未使用的卷
docker volume prune
```

---

## 📊 性能问题排查

### 性能监控指标

| 指标 | 正常值 | 告警阈值 | 严重阈值 |
|------|--------|----------|----------|
| 响应时间(P95) | <200ms | >500ms | >1s |
| 吞吐量(QPS) | >1000 | <500 | <100 |
| 错误率 | <0.1% | >1% | >5% |
| CPU使用率 | <50% | >70% | >90% |
| 内存使用率 | <60% | >80% | >95% |
| 数据库连接数 | <10 | >15 | >20 |

### 性能分析工具

```bash
# 1. JVM监控
jconsole <PID>

# 2. 性能分析
jvisualvm

# 3. 线程分析
jstack <PID> > thread_dump.txt

# 4. 堆分析
jmap -dump:live,format=b,file=heap.hprof <PID>
```

### 性能优化检查清单

```markdown
- [ ] 数据库索引是否合理
- [ ] 查询是否使用索引
- [ ] 是否有N+1查询
- [ ] 连接池配置是否合理
- [ ] 缓存是否有效利用
- [ ] 日志级别是否适当
- [ ] GC参数是否优化
- [ ] 是否有内存泄漏
- [ ] 是否有死循环
- [ ] 是否有锁竞争
```

---

## 🔒 安全问题排查

### 1. 登录失败过多

#### 故障现象

```bash
# 大量登录失败
tail -f logs/api.log | grep "登录失败"
# 2025-12-25 10:00:00 登录失败: 用户admin, IP: 192.168.1.100
# 2025-12-25 10:00:01 登录失败: 用户admin, IP: 192.168.1.100
```

#### 排查步骤

```bash
# 1. 查看登录失败统计
grep "登录失败" logs/api.log | wc -l

# 2. 查看异常IP
grep "登录失败" logs/api.log | awk '{print $NF}' | sort | uniq -c | sort -rn

# 3. 检查是否是暴力破解
# 同一IP连续失败>5次
```

#### 解决方案

```bash
# 1. 封禁异常IP
sudo iptables -A INPUT -s 192.168.1.100 -j DROP

# 2. 启用验证码
# application.yml
security:
  captcha:
    enabled: true

# 3. 增加限流
# application.yml
ratelimit:
  login:
    maxAttempts: 5
    windowMinutes: 15
```

### 2. SQL注入攻击

#### 故障现象

```bash
# 异常请求
grep "union select" logs/access.log
# 192.168.1.100 - - [25/Dec/2025:10:00:00] "GET /api/user?id=1' OR '1'='1" 500
```

#### 排查步骤

```bash
# 1. 查看可疑SQL日志
grep -E "(union|select|drop|delete|update)" logs/api.log

# 2. 查看异常请求参数
grep "union select" logs/access.log -A 5

# 3. 检查数据库是否有异常操作
mysql -u root -p -e "SELECT * FROM information_schema.PROCESSLIST WHERE INFO LIKE '%union%';"
```

#### 解决方案

```java
// 使用参数化查询
// 问题代码
String sql = "SELECT * FROM user WHERE id = " + userId;

// 优化方案
@Select("SELECT * FROM user WHERE id = #{userId}")
User selectById(@Param("userId") Long userId);

// 添加输入验证
@Pattern(regexp = "^[0-9]+$", message = "ID只能为数字")
private Long userId;
```

---

## 🗄️ 日志分析

### 日志级别说明

| 级别 | 说明 | 使用场景 |
|------|------|----------|
| TRACE | 最详细 | 调试入口参数 |
| DEBUG | 调试信息 | 开发环境 |
| INFO | 一般信息 | 关键业务流程 |
| WARN | 警告信息 | 可忽略的异常 |
| ERROR | 错误信息 | 需要处理的错误 |
| FATAL | 严重错误 | 系统崩溃 |

### 日志查询技巧

```bash
# 1. 按时间范围查询
grep "2025-12-25 10:" logs/api.log

# 2. 按级别查询
grep -i error logs/api.log
grep -i warn logs/api.log

# 3. 按用户查询
grep "userId=1" logs/api.log

# 4. 按接口查询
grep "/api/user" logs/access.log

# 5. 查看上下文
grep "错误ID: 12345" logs/api.log -B 5 -A 5

# 6. 统计错误数量
grep -i error logs/api.log | wc -l

# 7. 查找最慢的请求
awk '{print $NF, $7}' logs/access.log | sort -rn | head -10
```

### ELK日志分析

```bash
# Kibana查询
# 查询ERROR级别
@timestamp:>=now-1h AND level:ERROR

# 查询慢请求
@timestamp:>=now-1h AND responseTime:>1000

# 查询特定用户
@timestamp:>=now-24h AND userId:1
```

---

## 🚨 应急处理

### 紧急重启服务

```bash
# 1. 确认服务状态
systemctl status bankshield-api

# 2. 优雅停机
systemctl stop bankshield-api
# 或Docker环境
docker stop bankshield-api

# 3. 等待进程结束
ps aux | grep bankshield-api

# 4. 启动服务
systemctl start bankshield-api
# 或Docker环境
docker start bankshield-api

# 5. 验证服务健康
curl http://localhost:8080/api/actuator/health
```

### 回滚到上一版本

```bash
# 1. 查看部署历史
helm history bankshield-prod -n bankshield-prod

# 2. 回滚到上一版本
helm rollback bankshield-prod -n bankshield-prod

# 3. 回滚到指定版本
helm rollback bankshield-prod 2 -n bankshield-prod

# 4. 验证回滚
kubectl rollout status deployment/bankshield-api -n bankshield-prod
```

### 数据库紧急恢复

```bash
# 1. 停止应用
systemctl stop bankshield-api

# 2. 停止MySQL
systemctl stop mysql

# 3. 恢复备份
gunzip < /backup/bankshield_20251225.sql.gz | mysql -u root -p bankshield

# 4. 启动MySQL
systemctl start mysql

# 5. 启动应用
systemctl start bankshield-api

# 6. 验证数据
mysql -u root -p bankshield -e "SELECT COUNT(*) FROM sys_user;"
```

---

## 📞 技术支持

- **技术支持**: tech-support@bankshield.com
- **安全团队**: security@bankshield.com
- **运维团队**: ops@bankshield.com
- **值班电话**: +86-400-123-4567
- **紧急联系**: +86-138-****-9999 (24小时)

---

**文档版本**: v1.0.0
**最后更新**: 2025-12-25
