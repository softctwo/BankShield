#!/bin/bash

# BankShield性能优化部署脚本
# 自动化部署性能优化相关配置和组件

set -e

echo "========================================="
echo "BankShield性能优化部署脚本"
echo "========================================="

# 配置变量
PROJECT_DIR="/Users/zhangyanlong/workspaces/BankShield"
MYSQL_HOST="localhost"
MYSQL_PORT="3306"
MYSQL_USER="root"
MYSQL_PASSWORD="123456"
MYSQL_DATABASE="bankshield"
REDIS_HOST="localhost"
REDIS_PORT="6379"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查依赖
check_dependencies() {
    log_info "检查依赖环境..."
    
    # 检查MySQL
    if ! command -v mysql &> /dev/null; then
        log_error "MySQL客户端未安装"
        exit 1
    fi
    
    # 检查Redis
    if ! command -v redis-cli &> /dev/null; then
        log_error "Redis客户端未安装"
        exit 1
    fi
    
    # 检查Java
    if ! command -v java &> /dev/null; then
        log_error "Java未安装"
        exit 1
    fi
    
    log_info "依赖环境检查通过"
}

# 数据库性能优化部署
deploy_database_optimization() {
    log_info "部署数据库性能优化..."
    
    # 执行数据库优化脚本
    if [ -f "$PROJECT_DIR/sql/performance_optimization.sql" ]; then
        log_info "执行数据库索引优化脚本..."
        mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD $MYSQL_DATABASE < $PROJECT_DIR/sql/performance_optimization.sql
        log_info "数据库索引优化完成"
    else
        log_warn "数据库优化脚本不存在: $PROJECT_DIR/sql/performance_optimization.sql"
    fi
    
    # 配置MySQL性能参数
    log_info "配置MySQL性能参数..."
    mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD <<EOF
-- 开启慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;
SET GLOBAL log_queries_not_using_indexes = 'ON';

-- 优化InnoDB参数
SET GLOBAL innodb_buffer_pool_size = 8589934592; -- 8GB
SET GLOBAL innodb_log_file_size = 1073741824; -- 1GB
SET GLOBAL innodb_flush_log_at_trx_commit = 2;
SET GLOBAL innodb_flush_method = 'O_DIRECT';

-- 查询缓存优化
SET GLOBAL query_cache_type = 1;
SET GLOBAL query_cache_size = 536870912; -- 512MB

-- 连接数优化
SET GLOBAL max_connections = 500;
SET GLOBAL thread_cache_size = 50;

SHOW VARIABLES LIKE 'slow_query_log%';
SHOW VARIABLES LIKE 'innodb_buffer_pool_size';
EOF
    
    log_info "MySQL性能参数配置完成"
}

# Redis性能优化部署
deploy_redis_optimization() {
    log_info "部署Redis性能优化..."
    
    # 配置Redis性能参数
    redis-cli -h $REDIS_HOST -p $REDIS_PORT <<EOF
# 内存优化
CONFIG SET maxmemory 8gb
CONFIG SET maxmemory-policy allkeys-lru

# 持久化优化
CONFIG SET save "900 1 300 10 60 10000"
CONFIG SET appendonly yes
CONFIG SET appendfsync everysec

# 网络优化
CONFIG SET tcp-keepalive 60
CONFIG SET timeout 300

# 性能监控
CONFIG SET slowlog-log-slower-than 1000
CONFIG SET slowlog-max-len 128

# 输出配置
CONFIG GET maxmemory*
CONFIG GET tcp-keepalive
EOF
    
    log_info "Redis性能参数配置完成"
}

# JVM参数优化
deploy_jvm_optimization() {
    log_info "部署JVM性能优化..."
    
    # 创建JVM配置文件
    cat > $PROJECT_DIR/config/jvm-optimize.conf <<EOF
# JVM性能优化参数
-Xms8g
-Xmx8g
-XX:MetaspaceSize=512m
-XX:MaxMetaspaceSize=512m
-Xss512k
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+ParallelRefProcEnabled
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/var/log/bankshield/heapdump.hprof
-XX:+PrintGCDetails
-XX:+PrintGCDateStamps
-Xloggc:/var/log/bankshield/gc.log
-Dfile.encoding=UTF-8
-Djava.security.egd=file:/dev/./urandom
-XX:+UseStringDeduplication
-XX:+OptimizeStringConcat
-XX:+UseCompressedOops
-XX:+UseCompressedClassPointers
EOF
    
    log_info "JVM优化配置文件已生成: $PROJECT_DIR/config/jvm-optimize.conf"
}

# 应用性能优化部署
deploy_application_optimization() {
    log_info "部署应用层性能优化..."
    
    # 更新application.yml配置
    if [ -f "$PROJECT_DIR/bankshield-api/src/main/resources/application.yml" ]; then
        log_info "更新应用配置文件..."
        
        # 备份原文件
        cp $PROJECT_DIR/bankshield-api/src/main/resources/application.yml $PROJECT_DIR/bankshield-api/src/main/resources/application.yml.backup
        
        # 应用优化配置已在之前的文件更新中完成
        log_info "应用配置文件已更新"
    fi
    
    # 编译性能优化代码
    log_info "编译性能优化代码..."
    cd $PROJECT_DIR
    mvn clean compile -DskipTests
    
    log_info "应用性能优化部署完成"
}

# 前端性能优化部署
deploy_frontend_optimization() {
    log_info "部署前端性能优化..."
    
    if [ -d "$PROJECT_DIR/bankshield-ui" ]; then
        cd $PROJECT_DIR/bankshield-ui
        
        # 安装依赖
        log_info "安装前端依赖..."
        npm install
        
        # 构建优化版本
        log_info "构建前端优化版本..."
        npm run build:optimized
        
        # 生成构建报告
        npm run build:report
        
        log_info "前端性能优化部署完成"
    else
        log_warn "前端目录不存在: $PROJECT_DIR/bankshield-ui"
    fi
}

# 性能监控部署
deploy_performance_monitoring() {
    log_info "部署性能监控..."
    
    # 创建监控配置目录
    mkdir -p $PROJECT_DIR/monitoring
    
    # Prometheus配置
    cat > $PROJECT_DIR/monitoring/prometheus.yml <<EOF
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "performance_rules.yml"

scrape_configs:
  - job_name: 'bankshield-api'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s
    
  - job_name: 'bankshield-mysql'
    static_configs:
      - targets: ['localhost:9104']
    scrape_interval: 10s
    
  - job_name: 'bankshield-redis'
    static_configs:
      - targets: ['localhost:9121']
    scrape_interval: 10s
EOF
    
    # 性能告警规则
    cat > $PROJECT_DIR/monitoring/performance_rules.yml <<EOF
groups:
  - name: performance
    rules:
      - alert: HighResponseTime
        expr: http_request_duration_seconds_mean > 0.1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "API响应时间过长"
          
      - alert: LowCacheHitRate
        expr: (redis_hits / (redis_hits + redis_misses)) < 0.7
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "缓存命中率过低"
          
      - alert: HighGCOverhead
        expr: rate(jvm_gc_collection_seconds_sum[5m]) > 0.1
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "GC开销过高"
EOF
    
    # Grafana仪表板配置
    mkdir -p $PROJECT_DIR/monitoring/grafana
    
    log_info "性能监控配置已生成"
}

# 缓存预热部署
deploy_cache_warmup() {
    log_info "部署缓存预热机制..."
    
    # 创建缓存预热脚本
    cat > $PROJECT_DIR/scripts/cache-warmup.sh <<EOF
#!/bin/bash
# 缓存预热脚本

echo "开始缓存预热..."

# 预热用户缓存
curl -X POST http://localhost:8080/api/performance/cache/warmup/users

# 预热角色缓存
curl -X POST http://localhost:8080/api/performance/cache/warmup/roles

# 预热部门缓存
curl -X POST http://localhost:8080/api/performance/cache/warmup/depts

# 预热菜单缓存
curl -X POST http://localhost:8080/api/performance/cache/warmup/menus

# 预热密钥缓存
curl -X POST http://localhost:8080/api/performance/cache/warmup/keys

echo "缓存预热完成"
EOF
    
    chmod +x $PROJECT_DIR/scripts/cache-warmup.sh
    
    # 添加到定时任务（每天凌晨2点预热）
    (crontab -l 2>/dev/null; echo "0 2 * * * $PROJECT_DIR/scripts/cache-warmup.sh") | crontab -
    
    log_info "缓存预热机制部署完成"
}

# 性能测试
deploy_performance_testing() {
    log_info "部署性能测试工具..."
    
    # 创建性能测试脚本
    cat > $PROJECT_DIR/scripts/performance-test.sh <<EOF
#!/bin/bash
# 性能测试脚本

# 安装k6（如果未安装）
if ! command -v k6 &> /dev/null; then
    echo "安装k6性能测试工具..."
    sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
    echo "deb https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
    sudo apt-get update
    sudo apt-get install k6
fi

# 运行性能测试
echo "开始性能测试..."
k6 run $PROJECT_DIR/tests/performance/load-test.js

echo "性能测试完成"
EOF
    
    chmod +x $PROJECT_DIR/scripts/performance-test.sh
    
    # 创建性能测试用例
    mkdir -p $PROJECT_DIR/tests/performance
    
    cat > $PROJECT_DIR/tests/performance/load-test.js <<EOF
import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  stages: [
    { duration: '2m', target: 100 }, //  Ramp-up to 100 users over 2 minutes
    { duration: '5m', target: 100 }, // Stay at 100 users for 5 minutes
    { duration: '2m', target: 200 }, // Ramp-up to 200 users over 2 minutes
    { duration: '5m', target: 200 }, // Stay at 200 users for 5 minutes
    { duration: '2m', target: 0 },   // Ramp-down to 0 users over 2 minutes
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests must complete below 500ms
    http_req_failed: ['rate<0.1'],   // Error rate must be below 10%
  },
};

const BASE_URL = 'http://localhost:8080';

export default function () {
  // Test user login
  let loginRes = http.post(\`\${BASE_URL}/api/auth/login\`, {
    username: 'admin',
    password: 'admin123'
  });
  
  check(loginRes, {
    'login successful': (r) => r.status === 200,
    'login response time OK': (r) => r.timings.duration < 1000,
  });
  
  if (loginRes.status === 200) {
    const token = JSON.parse(loginRes.body).data.token;
    
    // Test user list query
    let userListRes = http.get(\`\${BASE_URL}/api/user/list?page=1&size=20\`, {
      headers: {
        'Authorization': \`Bearer \${token}\`
      }
    });
    
    check(userListRes, {
      'user list query successful': (r) => r.status === 200,
      'user list response time OK': (r) => r.timings.duration < 500,
    });
    
    // Test dashboard metrics
    let dashboardRes = http.get(\`\${BASE_URL}/api/dashboard/metrics\`, {
      headers: {
        'Authorization': \`Bearer \${token}\`
      }
    });
    
    check(dashboardRes, {
      'dashboard metrics successful': (r) => r.status === 200,
      'dashboard response time OK': (r) => r.timings.duration < 300,
    });
  }
  
  sleep(1);
}
EOF
    
    log_info "性能测试工具部署完成"
}

# 清理和回滚
cleanup_optimization() {
    log_info "清理性能优化配置..."
    
    # 恢复数据库配置
    mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD <<EOF
-- 恢复默认配置
SET GLOBAL slow_query_log = 'OFF';
SET GLOBAL long_query_time = 10;
SET GLOBAL query_cache_type = 0;
SET GLOBAL max_connections = 151;
EOF
    
    # 恢复Redis配置
    redis-cli -h $REDIS_HOST -p $REDIS_PORT <<EOF
CONFIG RESETSTAT
CONFIG REWRITE
EOF
    
    # 恢复应用配置
    if [ -f "$PROJECT_DIR/bankshield-api/src/main/resources/application.yml.backup" ]; then
        cp $PROJECT_DIR/bankshield-api/src/main/resources/application.yml.backup $PROJECT_DIR/bankshield-api/src/main/resources/application.yml
    fi
    
    # 清理定时任务
    crontab -l | grep -v "cache-warmup.sh" | crontab -
    
    log_info "性能优化配置已清理"
}

# 主函数
main() {
    case "$1" in
        deploy)
            log_info "开始部署性能优化..."
            check_dependencies
            deploy_database_optimization
            deploy_redis_optimization
            deploy_jvm_optimization
            deploy_application_optimization
            deploy_frontend_optimization
            deploy_performance_monitoring
            deploy_cache_warmup
            deploy_performance_testing
            log_info "性能优化部署完成！"
            ;;
        test)
            log_info "运行性能测试..."
            $PROJECT_DIR/scripts/performance-test.sh
            ;;
        cleanup)
            log_info "清理性能优化配置..."
            cleanup_optimization
            ;;
        *)
            echo "用法: $0 {deploy|test|cleanup}"
            echo "  deploy  - 部署性能优化"
            echo "  test    - 运行性能测试"
            echo "  cleanup - 清理优化配置"
            exit 1
            ;;
    esac
}

# 运行主函数
main "$@"