# BankShield 数据血缘增强模块

## 模块介绍

数据血缘增强模块是BankShield银行数据安全管理平台的核心组件，提供自动血缘发现、可视化分析、影响分析和数据质量监控等功能，帮助银行实现数据资产的全面治理和风险控制。

## 核心功能

### 🔍 自动血缘发现
- **SQL解析血缘**：支持MySQL、Oracle、PostgreSQL等主流数据库
- **ETL任务血缘**：集成Kettle、DataX、Sqoop等ETL工具
- **智能识别**：自动识别表、字段、视图间的血缘关系

### 📊 可视化分析
- **交互式图谱**：基于ECharts的动态血缘图谱
- **多维度展示**：支持力导向、环形、层次等多种布局
- **实时交互**：节点点击查看详情，拖拽调整布局

### ⚠️ 影响分析
- **字段级影响**：分析字段变更的下游影响范围
- **表级影响**：评估表结构变更的整体影响
- **复杂性评估**：计算依赖复杂度和维护难度

### ✅ 质量监控
- **规则引擎**：支持完整性、准确性、一致性等多维度质量检查
- **实时监控**：定时执行质量检查，实时告警
- **评分体系**：综合质量评分和趋势分析

## 技术架构

### 后端技术栈
- **框架**：Spring Boot 2.7 + MyBatis-Plus
- **数据库**：MySQL 8.0 + Redis
- **SQL解析**：Apache Calcite + Druid
- **调度**：Quartz
- **图数据库**：Neo4j（可选）

### 前端技术栈
- **框架**：Vue 3.4 + TypeScript 5.0
- **图表**：ECharts 5.0
- **UI组件**：Element Plus
- **状态管理**：Pinia

## 快速开始

### 环境要求
- Java 8+
- MySQL 8.0+
- Redis 6.0+
- Node.js 16+

### 后端部署

1. **创建数据库**
```sql
CREATE DATABASE bankshield_lineage DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. **执行数据库脚本**
```bash
mysql -u root -p bankshield_lineage < src/main/resources/sql/lineage_schema.sql
```

3. **配置文件**
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bankshield_lineage
    username: root
    password: your_password
  
  redis:
    host: localhost
    port: 6379
    database: 5

server:
  port: 8085
```

4. **启动服务**
```bash
mvn clean package
java -jar target/bankshield-lineage.jar
```

### 前端集成

1. **安装依赖**
```bash
cd bankshield-ui
npm install
```

2. **配置API**
```javascript
// src/api/lineage.ts
const API_BASE_URL = '/lineage/api'
```

3. **启动开发服务器**
```bash
npm run dev
```

## API文档

### 血缘图谱API

#### 构建血缘图谱
```http
GET /api/lineage/graph/{assetId}?depth=3
```

#### 分析影响
```http
GET /api/lineage/impact/{assetId}
```

#### 提取SQL血缘
```http
POST /api/lineage/extract-sql?sql=SELECT * FROM table1&dbType=mysql
```

### 质量监控API

#### 执行质量检查
```http
POST /api/quality/execute
```

#### 计算质量评分
```http
GET /api/quality/score/table/{tableId}
```

#### 创建质量规则
```http
POST /api/quality/rules
```

## 配置说明

### 血缘发现配置
```yaml
lineage:
  sql-parser:
    db-types: mysql,oracle,postgresql
    timeout: 30
  auto-discover:
    enabled: true
    interval: 60
    sql-log-tables: sys_sql_log,etl_execution_log
```

### 质量检查配置
```yaml
lineage:
  quality:
    enabled: true
    check-interval: 6
    default-threshold: 95.0
```

## 使用示例

### 1. 查看血缘图谱

```javascript
// 前端代码示例
import { getLineageGraph } from '@/api/lineage'

const loadGraph = async () => {
  const response = await getLineageGraph(123, 3)
  const graphData = response.data
  // 使用ECharts渲染图谱
  renderGraph(graphData)
}
```

### 2. 分析数据影响

```java
// 后端代码示例
ImpactAnalysis analysis = lineageService.analyzeImpact(tableId);
System.out.println("直接影响表数量: " + analysis.getDirectImpact().getTableCount());
System.out.println("间接影响表数量: " + analysis.getIndirectImpact().getTableCount());
```

### 3. 配置质量规则

```sql
-- 创建质量规则
INSERT INTO data_quality_rule (rule_name, rule_type, check_sql, threshold, severity)
VALUES ('客户信息完整性检查', 'COMPLETENESS', 
        'SELECT COUNT(*) FROM customer_info WHERE customer_name IS NULL', 
        95.0, 'HIGH');
```

## 性能优化

### 数据库优化
- 为高频查询字段建立索引
- 使用分页查询处理大数据量
- 定期清理历史数据

### 缓存策略
- Redis缓存热点数据
- 图谱结构缓存1小时
- 质量评分缓存30分钟

### 查询优化
- 使用连接查询替代子查询
- 合理使用数据库视图
- 避免N+1查询问题

## 监控告警

### 系统监控
- **响应时间**：API响应时间监控
- **错误率**：系统错误率统计
- **资源使用**：CPU、内存、磁盘使用率

### 业务监控
- **血缘发现成功率**：SQL解析成功率
- **质量检查通过率**：数据质量达标率
- **影响分析准确性**：影响范围识别准确率

## 安全特性

### 数据安全
- 敏感数据加密存储
- 数据传输HTTPS加密
- 数据库连接SSL加密

### 访问控制
- 基于角色的权限管理
- API接口权限验证
- 操作审计日志记录

## 扩展开发

### 添加新的SQL解析器

1. 创建解析器类
```java
@Component
public class OracleLineageExtractor implements SqlLineageExtractor {
    @Override
    public LineageInfo extractLineage(String sql) {
        // 实现Oracle特有的解析逻辑
    }
}
```

2. 注册解析器
```java
@Configuration
public class LineageConfig {
    @Bean
    public Map<String, SqlLineageExtractor> lineageExtractors() {
        Map<String, SqlLineageExtractor> extractors = new HashMap<>();
        extractors.put("oracle", new OracleLineageExtractor());
        return extractors;
    }
}
```

### 自定义质量规则

1. 创建规则模板
```java
@Component
public class CustomRuleTemplate implements QualityRuleTemplate {
    @Override
    public String getCheckSql(String tableName, String columnName) {
        return "SELECT COUNT(*) FROM " + tableName + 
               " WHERE " + columnName + " IS NULL";
    }
}
```

## 故障排查

### 常见问题

1. **SQL解析失败**
   - 检查SQL语法是否正确
   - 确认数据库类型配置
   - 查看解析器日志

2. **图谱渲染缓慢**
   - 减少查询深度
   - 启用数据抽样
   - 优化数据库查询

3. **质量检查失败**
   - 验证检查SQL语法
   - 检查数据库连接
   - 确认权限配置

### 日志查看
```bash
# 查看应用日志
tail -f logs/bankshield-lineage.log

# 查看错误日志
grep ERROR logs/bankshield-lineage.log
```

## 版本更新

### v1.0.0 (当前版本)
- ✅ 自动血缘发现
- ✅ 可视化图谱
- ✅ 影响分析
- ✅ 质量监控

### 未来版本规划
- **v1.1.0**：AI增强血缘发现
- **v1.2.0**：实时血缘追踪
- **v1.3.0**：跨平台集成

## 支持与贡献

### 获取支持
- 提交Issue到项目仓库
- 查看官方文档
- 参与社区讨论

### 贡献代码
1. Fork项目仓库
2. 创建功能分支
3. 提交代码变更
4. 发起Pull Request

## 许可证

本项目采用MIT许可证，详见LICENSE文件。

## 联系方式

- 项目主页：https://github.com/your-org/bankshield
- 技术支持：support@bankshield.com
- 商务合作：business@bankshield.com