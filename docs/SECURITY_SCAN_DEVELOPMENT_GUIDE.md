# 安全扫描与漏洞管理功能开发指南

## 📋 项目信息

**功能模块**: P1-3 安全扫描与漏洞管理  
**开发状态**: 🚧 数据库设计已完成，待实现后端和前端  
**优先级**: 高  
**预计工期**: 3周

---

## ✅ 已完成内容

### 1. 数据库设计 ✅

**SQL脚本**: `@/Users/zhangyanlong/workspaces/BankShield/sql/security_scan.sql` (600行)

**核心表结构** (6张表):
- ✅ `security_scan_task` - 扫描任务表
- ✅ `vulnerability_record` - 漏洞记录表
- ✅ `remediation_plan` - 修复计划表
- ✅ `scan_rule` - 扫描规则表
- ✅ `dependency_component` - 依赖组件表
- ✅ `scan_statistics` - 扫描统计表

**数据库对象**:
- ✅ 3个视图（漏洞统计、任务概览、待修复漏洞）
- ✅ 3个存储过程（更新进度、统计漏洞、生成统计）
- ✅ 12条默认扫描规则（SQL注入、XSS、安全配置）

---

## 📝 待开发内容清单

### 2. 后端开发 ⏳

#### 实体类 (6个)

**文件路径**: `bankshield-api/src/main/java/com/bankshield/api/entity/`

1. **SecurityScanTask.java** - 扫描任务实体
```java
@Data
@TableName("security_scan_task")
public class SecurityScanTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskName;
    private String taskType; // SQL_INJECTION/XSS/DEPENDENCY/CODE_SCAN/FULL_SCAN
    private String scanTarget;
    private String scanScope;
    private String scanConfig; // JSON
    private String scanStatus; // PENDING/RUNNING/COMPLETED/FAILED/CANCELLED
    private Integer progress;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private Integer totalItems;
    private Integer scannedItems;
    private Integer vulnerabilitiesFound;
    private Integer highRiskCount;
    private Integer mediumRiskCount;
    private Integer lowRiskCount;
    private String scanResult;
    private String errorMessage;
    private String createdBy;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
```

2. **VulnerabilityRecord.java** - 漏洞记录实体
```java
@Data
@TableName("vulnerability_record")
public class VulnerabilityRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String vulnCode;
    private String vulnName;
    private String vulnType;
    private String severity; // CRITICAL/HIGH/MEDIUM/LOW/INFO
    private BigDecimal cvssScore;
    private String cveId;
    private String cweId;
    private String description;
    private String location;
    private String affectedComponent;
    private String affectedVersion;
    private String proofOfConcept;
    private String impact;
    private String recommendation;
    private String referenceLinks;
    private String status; // OPEN/IN_PROGRESS/RESOLVED/WONT_FIX/FALSE_POSITIVE
    private String assignedTo;
    private String resolvedBy;
    private LocalDateTime resolvedTime;
    private String resolutionNotes;
    private String verificationStatus;
    private String verifiedBy;
    private LocalDateTime verifiedTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
```

3. **RemediationPlan.java** - 修复计划实体
4. **ScanRule.java** - 扫描规则实体
5. **DependencyComponent.java** - 依赖组件实体
6. **ScanStatistics.java** - 扫描统计实体

#### Mapper接口 (6个)

**文件路径**: `bankshield-api/src/main/java/com/bankshield/api/mapper/`

关键方法示例：
```java
@Mapper
public interface SecurityScanTaskMapper extends BaseMapper<SecurityScanTask> {
    List<SecurityScanTask> selectByStatus(String status);
    List<SecurityScanTask> selectByType(String taskType);
    SecurityScanTask selectWithVulnerabilities(Long id);
}

@Mapper
public interface VulnerabilityRecordMapper extends BaseMapper<VulnerabilityRecord> {
    List<VulnerabilityRecord> selectByTaskId(Long taskId);
    List<VulnerabilityRecord> selectBySeverity(String severity);
    List<VulnerabilityRecord> selectByStatus(String status);
    List<Map<String, Object>> countBySeverity();
    List<Map<String, Object>> countByType();
}
```

#### 安全扫描引擎 (核心组件)

**文件路径**: `bankshield-api/src/main/java/com/bankshield/api/scanner/`

1. **SqlInjectionScanner.java** - SQL注入检测器
```java
@Component
public class SqlInjectionScanner {
    
    public List<VulnerabilityRecord> scan(String target, List<ScanRule> rules) {
        List<VulnerabilityRecord> vulnerabilities = new ArrayList<>();
        
        // 1. 扫描SQL语句
        // 2. 应用检测规则
        // 3. 生成漏洞记录
        
        return vulnerabilities;
    }
    
    private boolean detectSqlInjection(String sql, String pattern) {
        return Pattern.matches(pattern, sql);
    }
}
```

2. **XssScanner.java** - XSS检测器
```java
@Component
public class XssScanner {
    
    public List<VulnerabilityRecord> scan(String target, List<ScanRule> rules) {
        // 检测HTML输入、输出编码
        // 检测危险标签和事件处理器
        return vulnerabilities;
    }
}
```

3. **DependencyScanner.java** - 依赖漏洞扫描器
```java
@Component
public class DependencyScanner {
    
    public List<VulnerabilityRecord> scanMavenDependencies(String pomPath) {
        // 解析pom.xml
        // 查询NVD数据库
        // 匹配已知漏洞
        return vulnerabilities;
    }
    
    public List<VulnerabilityRecord> scanNpmDependencies(String packageJsonPath) {
        // 解析package.json
        // 查询npm audit
        return vulnerabilities;
    }
}
```

4. **CodeSecurityScanner.java** - 代码安全扫描器
```java
@Component
public class CodeSecurityScanner {
    
    public List<VulnerabilityRecord> scanJavaCode(String sourcePath) {
        // 静态代码分析
        // 检测硬编码密码、不安全的API使用等
        return vulnerabilities;
    }
}
```

5. **ScanEngine.java** - 扫描引擎总控
```java
@Service
public class ScanEngine {
    
    @Autowired
    private SqlInjectionScanner sqlScanner;
    
    @Autowired
    private XssScanner xssScanner;
    
    @Autowired
    private DependencyScanner dependencyScanner;
    
    @Autowired
    private CodeSecurityScanner codeScanner;
    
    public void executeScan(SecurityScanTask task) {
        try {
            task.setScanStatus("RUNNING");
            task.setStartTime(LocalDateTime.now());
            
            List<VulnerabilityRecord> vulnerabilities = new ArrayList<>();
            
            switch (task.getTaskType()) {
                case "SQL_INJECTION":
                    vulnerabilities = sqlScanner.scan(task.getScanTarget(), getRules("SQL_INJECTION"));
                    break;
                case "XSS":
                    vulnerabilities = xssScanner.scan(task.getScanTarget(), getRules("XSS"));
                    break;
                case "DEPENDENCY":
                    vulnerabilities = dependencyScanner.scanMavenDependencies(task.getScanTarget());
                    break;
                case "CODE_SCAN":
                    vulnerabilities = codeScanner.scanJavaCode(task.getScanTarget());
                    break;
                case "FULL_SCAN":
                    // 执行所有扫描
                    break;
            }
            
            // 保存漏洞记录
            saveVulnerabilities(task.getId(), vulnerabilities);
            
            task.setScanStatus("COMPLETED");
            task.setEndTime(LocalDateTime.now());
            task.setVulnerabilitiesFound(vulnerabilities.size());
            
        } catch (Exception e) {
            task.setScanStatus("FAILED");
            task.setErrorMessage(e.getMessage());
        }
    }
}
```

#### Service层 (1个)

**SecurityScanService.java**
```java
public interface SecurityScanService {
    // 扫描任务管理
    IPage<SecurityScanTask> pageTasks(Page<SecurityScanTask> page, String taskType, String status);
    SecurityScanTask createTask(SecurityScanTask task);
    void startScan(Long taskId);
    void stopScan(Long taskId);
    void deleteTask(Long taskId);
    
    // 漏洞管理
    IPage<VulnerabilityRecord> pageVulnerabilities(Page<VulnerabilityRecord> page, 
                                                    String severity, String status);
    VulnerabilityRecord getVulnerabilityDetail(Long id);
    void updateVulnerabilityStatus(Long id, String status);
    void assignVulnerability(Long id, String assignedTo);
    void resolveVulnerability(Long id, String resolutionNotes);
    
    // 修复计划
    RemediationPlan createPlan(RemediationPlan plan);
    void updatePlanStatus(Long id, String status);
    
    // 统计分析
    Map<String, Object> getDashboardStatistics();
    List<Map<String, Object>> getVulnerabilityTrend(int days);
    List<Map<String, Object>> getTopVulnerabilityTypes();
}
```

#### Controller层 (1个)

**SecurityScanController.java** - 提供REST API（约30个接口）

---

### 3. 前端开发 ⏳

#### API接口封装

**文件**: `bankshield-ui/src/api/security-scan.ts`

```typescript
export interface SecurityScanTask {
  id?: number
  taskName: string
  taskType: string
  scanTarget: string
  scanScope: string
  scanStatus: string
  progress: number
  vulnerabilitiesFound: number
  // ...其他字段
}

export interface VulnerabilityRecord {
  id?: number
  vulnCode: string
  vulnName: string
  vulnType: string
  severity: string
  cvssScore: number
  description: string
  location: string
  status: string
  // ...其他字段
}

// API方法
export function getScanTasks(params: any) { }
export function createScanTask(data: SecurityScanTask) { }
export function startScan(taskId: number) { }
export function getVulnerabilities(params: any) { }
export function updateVulnerabilityStatus(id: number, status: string) { }
// ...更多API方法
```

#### 页面组件 (4个)

1. **扫描任务管理页面**
   - 文件: `bankshield-ui/src/views/security-scan/task/index.vue`
   - 功能: 创建扫描任务、查看任务列表、启动/停止扫描、查看进度

2. **漏洞列表页面**
   - 文件: `bankshield-ui/src/views/security-scan/vulnerability/index.vue`
   - 功能: 漏洞列表、筛选、详情查看、状态更新、分配处理

3. **漏洞详情页面**
   - 文件: `bankshield-ui/src/views/security-scan/vulnerability/detail.vue`
   - 功能: 详细信息、PoC、修复建议、处理记录

4. **扫描仪表板**
   - 文件: `bankshield-ui/src/views/security-scan/dashboard/index.vue`
   - 功能: 统计图表、漏洞趋势、风险评分、待处理事项

#### 路由配置

**文件**: `bankshield-ui/src/router/modules/security-scan.ts`

```typescript
const securityScanRouter: RouteRecordRaw = {
  path: '/security-scan',
  name: 'SecurityScan',
  redirect: '/security-scan/dashboard',
  meta: { title: '安全扫描', icon: 'Shield' },
  children: [
    {
      path: 'dashboard',
      name: 'ScanDashboard',
      component: () => import('@/views/security-scan/dashboard/index.vue'),
      meta: { title: '扫描仪表板', permission: 'scan:dashboard:view' }
    },
    {
      path: 'task',
      name: 'ScanTask',
      component: () => import('@/views/security-scan/task/index.vue'),
      meta: { title: '扫描任务', permission: 'scan:task:query' }
    },
    {
      path: 'vulnerability',
      name: 'Vulnerability',
      component: () => import('@/views/security-scan/vulnerability/index.vue'),
      meta: { title: '漏洞管理', permission: 'scan:vuln:query' }
    }
  ]
}
```

---

## 🎯 核心功能实现要点

### 1. SQL注入检测

**检测策略**:
- 正则表达式匹配危险模式
- 参数化查询检查
- 输入验证分析

**检测规则**:
- 单引号未转义
- OR 1=1 模式
- UNION SELECT 注入
- SQL注释符（--、#、/*）
- 堆叠查询

### 2. XSS检测

**检测策略**:
- HTML标签检测
- JavaScript事件处理器
- 输出编码检查
- CSP策略验证

**检测规则**:
- `<script>` 标签
- `onerror`、`onload` 等事件
- `javascript:` 伪协议
- `<iframe>` 注入

### 3. 依赖漏洞扫描

**数据源**:
- NVD (National Vulnerability Database)
- Maven Central Repository
- npm audit
- GitHub Advisory Database

**扫描流程**:
1. 解析依赖文件（pom.xml、package.json）
2. 提取组件名称和版本
3. 查询漏洞数据库
4. 匹配已知漏洞
5. 生成漏洞报告

### 4. 代码安全扫描

**检测项**:
- 硬编码密码/密钥
- 不安全的随机数生成
- 弱加密算法（MD5、SHA1、DES）
- SQL拼接
- 不安全的反序列化
- 路径遍历漏洞
- 命令注入

### 5. 漏洞管理流程

```
发现漏洞 → 记录详情 → 风险评估 → 分配处理 → 制定修复计划 → 
实施修复 → 验证修复 → 关闭漏洞
```

---

## 📊 数据流程

### 扫描流程

```
1. 用户创建扫描任务
   ↓
2. 选择扫描类型和目标
   ↓
3. 扫描引擎执行扫描
   ↓
4. 应用检测规则
   ↓
5. 发现并记录漏洞
   ↓
6. 更新任务状态和统计
   ↓
7. 生成扫描报告
```

### 漏洞处理流程

```
1. 漏洞被发现（OPEN）
   ↓
2. 安全团队评估
   ↓
3. 分配给开发人员（IN_PROGRESS）
   ↓
4. 制定修复计划
   ↓
5. 实施修复
   ↓
6. 提交修复验证
   ↓
7. 安全团队验证
   ↓
8. 关闭漏洞（RESOLVED）
```

---

## 🔌 API接口规划

### 扫描任务管理 (10个接口)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/security-scan/tasks` | 分页查询扫描任务 |
| GET | `/api/security-scan/tasks/{id}` | 查询任务详情 |
| POST | `/api/security-scan/tasks` | 创建扫描任务 |
| PUT | `/api/security-scan/tasks/{id}` | 更新任务 |
| DELETE | `/api/security-scan/tasks/{id}` | 删除任务 |
| POST | `/api/security-scan/tasks/{id}/start` | 启动扫描 |
| POST | `/api/security-scan/tasks/{id}/stop` | 停止扫描 |
| GET | `/api/security-scan/tasks/{id}/progress` | 查询进度 |
| GET | `/api/security-scan/tasks/{id}/report` | 生成报告 |
| POST | `/api/security-scan/tasks/{id}/retry` | 重试失败任务 |

### 漏洞管理 (12个接口)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/security-scan/vulnerabilities` | 分页查询漏洞 |
| GET | `/api/security-scan/vulnerabilities/{id}` | 查询漏洞详情 |
| PUT | `/api/security-scan/vulnerabilities/{id}/status` | 更新状态 |
| PUT | `/api/security-scan/vulnerabilities/{id}/assign` | 分配处理人 |
| POST | `/api/security-scan/vulnerabilities/{id}/resolve` | 解决漏洞 |
| POST | `/api/security-scan/vulnerabilities/{id}/verify` | 验证修复 |
| POST | `/api/security-scan/vulnerabilities/{id}/false-positive` | 标记误报 |
| GET | `/api/security-scan/vulnerabilities/statistics` | 漏洞统计 |
| GET | `/api/security-scan/vulnerabilities/trend` | 漏洞趋势 |
| GET | `/api/security-scan/vulnerabilities/top-types` | 高发类型 |
| POST | `/api/security-scan/vulnerabilities/export` | 导出漏洞 |
| POST | `/api/security-scan/vulnerabilities/batch-assign` | 批量分配 |

### 修复计划 (6个接口)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/security-scan/remediation-plans` | 查询修复计划 |
| POST | `/api/security-scan/remediation-plans` | 创建计划 |
| PUT | `/api/security-scan/remediation-plans/{id}` | 更新计划 |
| DELETE | `/api/security-scan/remediation-plans/{id}` | 删除计划 |
| POST | `/api/security-scan/remediation-plans/{id}/approve` | 审批计划 |
| POST | `/api/security-scan/remediation-plans/{id}/complete` | 完成计划 |

### 扫描规则 (5个接口)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/security-scan/rules` | 查询扫描规则 |
| POST | `/api/security-scan/rules` | 创建规则 |
| PUT | `/api/security-scan/rules/{id}` | 更新规则 |
| DELETE | `/api/security-scan/rules/{id}` | 删除规则 |
| PUT | `/api/security-scan/rules/{id}/toggle` | 启用/禁用规则 |

**总计**: 33个API接口

---

## 🚀 技术选型

### 后端技术

- **扫描引擎**: 自研 + 集成第三方工具
- **SQL注入检测**: 正则表达式 + AST分析
- **XSS检测**: OWASP AntiSamy
- **依赖扫描**: OWASP Dependency-Check
- **代码扫描**: SpotBugs + PMD
- **漏洞数据库**: NVD API

### 前端技术

- **图表**: ECharts（漏洞趋势、风险分布）
- **代码高亮**: Prism.js（显示漏洞代码）
- **Markdown**: marked.js（漏洞描述）

---

## 📈 开发计划

### 第1周：核心扫描引擎

- Day 1-2: SQL注入检测器
- Day 3-4: XSS检测器
- Day 5: 单元测试

### 第2周：依赖扫描和漏洞管理

- Day 1-2: 依赖漏洞扫描器
- Day 3-4: 漏洞管理Service和Controller
- Day 5: 集成测试

### 第3周：前端开发和联调

- Day 1-2: 前端页面开发
- Day 3: 前后端联调
- Day 4: 功能测试
- Day 5: 文档编写

---

## ✅ 验收标准

### 功能完整性
- [ ] SQL注入检测准确率 > 90%
- [ ] XSS检测准确率 > 85%
- [ ] 依赖漏洞扫描覆盖主流依赖
- [ ] 漏洞管理流程完整
- [ ] 前端页面功能完整

### 性能指标
- [ ] 单次扫描时间 < 5分钟（中等规模项目）
- [ ] 漏洞查询响应时间 < 200ms
- [ ] 支持并发扫描任务 > 5个

### 安全性
- [ ] 扫描结果加密存储
- [ ] 敏感信息脱敏
- [ ] 权限控制严格

---

## 📝 后续优化方向

1. **AI增强检测**
   - 机器学习模型识别复杂漏洞
   - 自动生成修复建议

2. **集成更多工具**
   - SonarQube集成
   - Checkmarx集成
   - Fortify集成

3. **自动化修复**
   - 简单漏洞自动修复
   - 生成修复补丁

4. **持续监控**
   - 定时自动扫描
   - 实时漏洞告警
   - 漏洞趋势预测

---

**文档版本**: v1.0  
**创建日期**: 2025-01-04  
**状态**: 📋 规划中

---

**© 2025 BankShield. All Rights Reserved.**
