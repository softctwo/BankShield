# 🚀 BankShield AI+区块链项目 快速参考指南

> 一键启动、快速查询、常见问题解答

---

## 📋 项目概览

**项目名称：** BankShield AI智能增强 + 区块链存证系统  
**工期：** 7天（84小时）  
**当前进度：** 18.7% (18/96任务完成)  
**状态：** 🟢 进行中  

---

## 🎯 一键启动命令

### 1. 快速启动（推荐）
```bash
cd /Users/zhangyanlong/workspaces/BankShield
./quick_start_ai_blockchain.sh
```

### 2. 完整实施（交互式菜单）
```bash
./scripts/start_ai_blockchain_implementation.sh
```

### 3. 分阶段启动
```bash
# 阶段一：AI深度学习引擎（Day 1-2）
./scripts/start_ai_blockchain_implementation.sh 1

# 阶段二：AI自动化响应（Day 3）
./scripts/start_ai_blockchain_implementation.sh 2

# 阶段三：区块链基础设施（Day 4-5）
./scripts/start_ai_blockchain_implementation.sh 3

# 阶段四：跨机构验证（Day 6-7）
./scripts/start_ai_blockchain_implementation.sh 4

# 所有阶段
./scripts/start_ai_blockchain_implementation.sh all
```

### 4. 查看可视化进度
```bash
./scripts/visualize_gantt.sh
```

---

## 📊 进度查询命令

### 查看总体进度
```bash
cat AI_BLOCKCHAIN_PROGRESS.md
```

### 查看详细实施计划
```bash
open roadmaps/AI_BLOCKCHAIN_IMPLEMENTATION_PLAN.md
```

### 查看待办事项
```bash
# 使用IDE查看或命令行grep
grep -A 2 "## Active Issues" AGENTS.md
```

### 查看最新状态报告
```bash
cat IMPLEMENTATION_SUMMARY_REPORT.md
```

---

## 🔧 常用开发命令

### AI模块开发
```bash
cd bankshield-ai

# 编译mvn clean compile

# 运行测试
mvn test -Dtest=AnomalyDetectionTest

# 运行服务
java -jar target/bankshield-ai.jar --spring.profiles.active=dev

# 查看日志
tail -f logs/ai-service.log
```

### 区块链模块开发
```bash
cd bankshield-blockchain

# 启动Fabric网络（需要先配置）
cd docker/fabric
docker-compose up -d

# 检查节点状态
docker ps | grep fabric

# 查看Peer日志
docker logs -f peer0.bankshield.internal
```

### API模块开发
```bash
cd bankshield-api

# 编译
mvn clean package -DskipTests

# 启动服务
java -jar target/bankshield-api.jar

# 测试API
curl http://localhost:8080/api/health
```

---

## 🐳 Docker管理命令

### Fabric网络
```bash
# 启动Fabric网络
cd docker/fabric
docker-compose up -d

# 停止网络
docker-compose down

# 查看所有容器
docker ps -a | grep fabric

# 查看日志
docker logs -f orderer.bankshield.com
docker logs -f peer0.bankshield.internal

# 清理所有容器和镜像（谨慎使用）
docker system prune -a
```

### Vault（密钥管理）
```bash
# 启动Vault
./scripts/security/setup-vault.sh

# 检查Vault状态
docker ps | grep vault

# 访问Vault UI
open http://localhost:8200
# Token: bankshield-dev-token
```

### Redis（缓存和限流）
```bash
# 连接Redis
docker exec -it bankshield-redis redis-cli

# 查看限流Key
KEYS security:rate_limit:*

# 查看封锁IP
KEYS security:blocked_ip:*
```

### MySQL（数据库）
```bash
# 连接数据库
docker exec -it bankshield-mysql mysql -uroot -p

# 查看AI相关表
USE bankshield;
SHOW TABLES LIKE 'ai_%';
SHOW TABLES LIKE 'behavior_%';
```

---

## 📈 监控和调试

### 查看实时监控
```bash
# Prometheus指标
curl http://localhost:8080/actuator/prometheus

# 健康检查
curl http://localhost:8080/actuator/health

# 应用信息
curl http://localhost:8080/actuator/info
```

### 查看系统资源
```bash
# CPU和内存
top -p $(pgrep -f bankshield)

# 磁盘空间
df -h

# 网络连接
netstat -anp | grep 808
```

### 日志查看
```bash
# 实时查看所有日志
tail -f logs/*.log

# 查看错误日志
grep ERROR logs/*.log

# 查看AI模块日志
tail -f bankshield-ai/logs/ai-service.log

# 查看区块链日志
tail -f bankshield-blockchain/logs/blockchain-service.log
```

---

## 🧪 测试命令

### 单元测试
```bash
# 测试AI模块
cd bankshield-ai
mvn test

# 测试指定类
mvn test -Dtest=DQNAgentTest

# 测试覆盖率
mvn test jacoco:report
open target/site/jacoco/index.html
```

### 集成测试
```bash
# 启动测试环境
./scripts/test/start-test-env.sh

# 执行集成测试
./scripts/test/run-integration-tests.sh

# 查看测试报告
open tests/target/surefire-reports/index.html
```

### 性能测试
```bash
# 压力测试
cd tests/performance
./stress-test.sh

# 区块链性能测试
cd bankshield-blockchain
./gradlew PerformanceTest
```

---

## 🔐 安全检查清单

### 部署前检查

- [ ] Vault服务正常运行
- [ ] Redis集群已启动
- [ ] MySQL主从复制正常
- [ ] Fabric网络所有节点健康
- [ ] 证书未过期
- [ ] 密钥已配置到Vault
- [ ] 防火墙策略已设置
- [ ] 审计日志已配置

### 运行时检查（每日）

```bash
# 1. 检查服务状态
docker ps | grep -E "(vault|redis|mysql|fabric|bankshield)"

# 2. 检查磁盘空间
df -h / | tail -1

# 3. 检查内存使用
free -h

# 4. 检查错误日志
grep -i error logs/*.log | tail -20

# 5. 检查系统负载
uptime

# 6. 检查网络连接
netstat -an | grep ESTABLISHED | wc -l
```

---

## 🚀 性能优化命令

### AI模型优化
```bash
# GPU加速检查（如果有GPU）
nvidia-smi

# 模型训练性能
export ND4J_CUDA_VERBOSE=true

# JVM调优
java -Xmx8g -Xms4g -jar bankshield-ai.jar
```

### 数据库优化
```bash
# MySQL慢查询
docker exec bankshield-mysql mysql -uroot -p -e "SHOW PROCESSLIST;"

# Redis性能
redis-cli --latency

# 清理过期Key
redis-cli EVAL "local keys = redis.call('KEYS', ARGV[1]) return #keys" 0 "security:*"
```

### 区块链优化
```bash
# 查看区块高度
docker exec -e "CORE_PEER_LOCALMSPID=BankShieldOrgMSP" peer0.bankshield.internal peer channel getinfo -c bankshield-channel

# 查看交易吞吐量
watch -n 1 'docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}" $(docker ps --filter name=fabric -q)'
```

---

## 🆘 常见问题与解决

### ❌ 问题1：Docker容器无法启动
```bash
# 检查Docker服务
systemctl status docker

# 重启Docker
sudo systemctl restart docker

# 清理Docker缓存
docker system prune -f

# 检查端口占用
netstat -anp | grep 808
lsof -i :8080
```

### ❌ 问题2：Vault连接失败
```bash
# 检查Vault容器
docker ps | grep vault

# 查看Vault日志
docker logs vault

# 检查Vault地址
echo $VAULT_ADDR

# 重置Vault（开发环境）
./scripts/security/setup-vault.sh
```

### ❌ 问题3：Maven依赖下载慢
```bash
# 使用阿里云Maven镜像
cat > ~/.m2/settings.xml << 'EOF'
<settings>
    <mirrors>
        <mirror>
            <id>aliyun</id>
            <mirrorOf>*</mirrorOf>
            <url>https://maven.aliyun.com/repository/public</url>
        </mirror>
    </mirrors>
</settings>
EOF
```

### ❌ 问题4：AI模型训练OOM
```bash
# 减小批量大小
export TRAIN_BATCH_SIZE=16

# 减少经验回放内存
export REPLAY_MEMORY_SIZE=1000

# 使用CPU训练
export ND4J_BACKEND=cpu
```

### ❌ 问题5：Fabric节点无法通信
```bash
# 检查网络
docker network ls
docker network inspect bankshield_blockchain

# 重启网络
docker-compose -f docker/fabric/docker-compose.yaml restart

# 重新生成证书
./scripts/blockchain/generate-certs.sh
```

---

## 📞 紧急联系

### 技术支持
- **项目负责人**：AI & Blockchain Team
- **架构师**：技术总监
- **安全专家**：安全负责人

### 查看日志
```bash
# 聚合所有日志
tail -f logs/*.log > all_logs.log

# 搜索错误
grep -r "ERROR\|FATAL\|EXCEPTION" logs/ > errors_summary.log

# 生成诊断报告
./scripts/diagnose.sh > diagnose_report.txt
```

---

## 🎯 成功检查清单

### 启动前检查
- [x] 所有脚本已添加执行权限
- [x] Docker服务正常运行
- [x] Java 1.8+ 和 Maven 3.6+ 已安装
- [x] 网络连接正常
- [x] 磁盘空间充足（至少10GB）

### 启动后检查
- [ ] AI服务启动成功
- [ ] 区块链网络所有节点正常
- [ ] Vault服务可访问
- [ ] Redis和MySQL运行正常
- [ ] 所有API可以访问
- [ ] 日志无严重错误

### 功能验证
- [ ] AI异常检测准确率达到95%+
- [ ] 自动化响应时间<100ms
- [ ] 区块链存证功能正常
- [ ] 跨机构验证通过
- [ ] 监管查询接口可用

---

## 📚 参考资料

### 核心文档
1. **详细实施计划**：`roadmaps/AI_BLOCKCHAIN_IMPLEMENTATION_PLAN.md`
2. **进度仪表板**：`AI_BLOCKCHAIN_PROGRESS.md`
3. **总结报告**：`IMPLEMENTATION_SUMMARY_REPORT.md`
4. **POM配置**：`pom_ai_blockchain.xml`

### 技术文档
1. DL4J文档：https://deeplearning4j.konduit.ai/
2. Fabric文档：https://hyperledger-fabric.readthedocs.io/
3. XGBoost文档：https://xgboost.readthedocs.io/
4. Prophet文档：https://facebook.github.io/prophet/

---

## 🚀 快速启动流程

```bash
# 第1步：进入项目目录
cd /Users/zhangyanlong/workspaces/BankShield

# 第2步：检查环境
./scripts/start_ai_blockchain_implementation.sh check

# 第3步：启动Vault（密钥管理）
./scripts/security/setup-vault.sh

# 第4步：完整实施
./quick_start_ai_blockchain.sh

# 第5步：查看进度
./scripts/visualize_gantt.sh

# 第6步：验证功能
./scripts/test/end-to-end-test.sh
```

---

## 💡 提示和技巧

### 开发效率
- 使用IDEA的Live Templates快速生成代码
- 配置快捷键快速运行测试
- 使用Postman测试API接口
- 使用Docker Desktop管理容器

### 调试技巧
- 使用Spring Boot DevTools热部署
- 配置远程Debug：`-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005`
- 使用Arthas排查线上问题
- 使用Wireshark分析网络包

### 备份建议
- 每天备份代码到Git
- 每小时备份数据库
- 关键配置异地备份
- 使用Git Tag标记里程碑

---

**🎯 如果只能记住一条命令：**

```bash
./quick_start_ai_blockchain.sh
```

**📊 如果需要查看状态：**

```bash
cat AI_BLOCKCHAIN_PROGRESS.md
```

**🆘 如果遇到问题：**

```bash
./scripts/start_ai_blockchain_implementation.sh help
cat QUICK_REFERENCE.md
```

---

**最后更新：2025-12-24 15:10:00**
