# BankShield 测试补充指南

**目标**: 将单元测试覆盖率提升至85%+

---

## 📊 当前测试覆盖率分析

### 现有测试统计

| 模块 | 测试类数 | 覆盖率估算 | 状态 |
|--------|-----------|-------------|------|
| 用户管理 | 2 | 70% | 需提升 |
| 数据资产 | 1 | 50% | 需提升 |
| 审计管理 | 1 | 60% | 需提升 |
| 监控告警 | 0 | 0% | 需补充 |
| 合规报告 | 0 | 0% | 需补充 |
| 数据血缘 | 5 | 80% | 需微调 |
| 数据脱敏 | 1 | 65% | 需提升 |
| 加密管理 | 5 | 90% | 已达标 |

**整体覆盖率**: 约 **60%**
**目标覆盖率**: **85%+**
**提升空间**: **25%**

---

## 🎯 测试补充计划

### 阶段一：核心业务模块测试补充（15%覆盖）

#### 1.1 监控告警模块测试

**需要测试的类**:
- AlertRuleController
- AlertRecordController
- MonitorService
- AlertEngine

**测试用例清单**:

```java
// AlertRuleControllerTest.java
@DisplayName("告警规则管理测试")
public class AlertRuleControllerTest {

    // 创建告警规则 - 成功
    @Test
    void testCreateAlertRule_Success() {
        // 测试创建CPU使用率告警规则
        // 验证规则已保存到数据库
    }

    // 创建告警规则 - 参数验证
    @Test
    void testCreateAlertRule_ValidationFailed() {
        // 测试必填字段验证
        // 验证threshold必须为数字
        // 验证condition必须是>, <, =之一
    }

    // 更新告警规则 - 成功
    @Test
    void testUpdateAlertRule_Success() {
        // 测试更新告警规则
        // 验证规则已更新
    }

    // 删除告警规则 - 成功
    @Test
    void testDeleteAlertRule_Success() {
        // 测试删除告警规则
        // 验证规则已删除
    }

    // 查询告警规则列表 - 成功
    @Test
    void testGetAlertRulePage_Success() {
        // 测试分页查询
        // 验证返回正确
    }

    // 启用/禁用告警规则 - 成功
    @Test
    void testToggleAlertRule_Success() {
        // 测试切换告警规则状态
        // 验证状态已更新
    }
}

// AlertRecordControllerTest.java
@DisplayName("告警记录管理测试")
public class AlertRecordControllerTest {

    // 查询告警记录 - 成功
    @Test
    void testGetAlertRecords_Success() {
        // 测试查询告警记录
        // 验证分页正确
    }

    // 告警确认 - 成功
    @Test
    void testAcknowledgeAlert_Success() {
        // 测试确认告警
        // 验证状态已更新为已确认
    }

    // 批量确认告警 - 成功
    @Test
    void testBatchAcknowledgeAlerts_Success() {
        // 测试批量确认告警
        // 验证所有告警已确认
    }

    // 查询告警统计 - 成功
    @Test
    void testGetAlertStatistics_Success() {
        // 测试查询告警统计
        // 验证统计数据正确
    }
}

// MonitorServiceTest.java
@DisplayName("监控服务测试")
public class MonitorServiceTest {

    // 收集系统指标 - 成功
    @Test
    void testCollectMetrics_Success() {
        // 测试收集CPU、内存、磁盘等指标
        // 验证指标已存储
    }

    // 检查告警规则 - 触发告警
    @Test
    void testCheckAlertRules_TriggerAlert() {
        // 模拟CPU使用率90%
        // 验证告警已触发
        // 验证告警记录已保存
    }

    // 检查告警规则 - 未触发告警
    @Test
    void testCheckAlertRules_NoTrigger() {
        // 模拟CPU使用率70%
        // 验证未触发告警
    }

    // 发送告警通知 - 成功
    @Test
    void testSendAlertNotification_Success() {
        // 测试发送告警通知
        // 验证邮件/短信/钉钉通知已发送
    }
}
```

#### 1.2 合规报告模块测试

**需要测试的类**:
- ComplianceReportController
- ComplianceReportService

**测试用例清单**:

```java
// ComplianceReportControllerTest.java
@DisplayName("合规报告管理测试")
public class ComplianceReportControllerTest {

    // 生成合规报告 - 周报
    @Test
    void testGenerateComplianceReport_Weekly() {
        // 测试生成本周合规报告
        // 验证报告生成成功
        // 验证报告数据正确
    }

    // 生成合规报告 - 月报
    @Test
    void testGenerateComplianceReport_Monthly() {
        // 测试生成本月合规报告
        // 验证报告生成成功
    }

    // 查询报告列表 - 成功
    @Test
    void testGetReportList_Success() {
        // 测试查询报告列表
        // 验证分页正确
    }

    // 下载报告 - 成功
    @Test
    void testDownloadReport_Success() {
        // 测试下载报告
        // 验证文件可下载
        // 验证文件格式正确（PDF/Excel）
    }

    // 获取报告详情 - 成功
    @Test
    void testGetReportDetail_Success() {
        // 测试获取报告详情
        // 验证报告信息完整
    }

    // 删除报告 - 成功
    @Test
    void testDeleteReport_Success() {
        // 测试删除报告
        // 验证报告已删除
    }
}

// ComplianceReportServiceTest.java
@DisplayName("合规报告服务测试")
public class ComplianceReportServiceTest {

    // 统计敏感数据 - 成功
    @Test
    void testStatisticsSensitiveData_Success() {
        // 测试统计各等级敏感数据数量
        // 验证统计数据正确
    }

    // 统计数据访问 - 成功
    @Test
    void testStatisticsDataAccess_Success() {
        // 测试统计数据访问次数
        // 验证统计数据正确
    }

    // 统计安全事件 - 成功
    @Test
    void testStatisticsSecurityEvents_Success() {
        // 测试统计安全事件
        // 验证统计数据正确
    }

    // 生成报告内容 - 成功
    @Test
    void testGenerateReportContent_Success() {
        // 测试生成报告内容
        // 验证内容格式正确
        // 验证数据完整
    }
}
```

### 阶段二：边缘场景和异常处理测试（5%覆盖）

#### 2.1 边界条件测试

```java
// UserServiceBoundaryTest.java
@DisplayName("用户服务边界测试")
public class UserServiceBoundaryTest {

    // 测试空用户名
    @Test
    void testAddUser_EmptyUsername() {
        User user = User.builder()
                .username("")
                .password("123456")
                .build();

        Result<String> result = userService.addUser(user);

        assertFalse(result.isSuccess());
        assertEquals("用户名不能为空", result.getMessage());
    }

    // 测试超长用户名
    @Test
    void testAddUser_TooLongUsername() {
        User user = User.builder()
                .username("a".repeat(100))
                .password("123456")
                .build();

        Result<String> result = userService.addUser(user);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("用户名长度不能超过"));
    }

    // 测试特殊字符用户名
    @Test
    void testAddUser_SpecialCharacters() {
        User user = User.builder()
                .username("admin<>/\\|")
                .password("123456")
                .build();

        Result<String> result = userService.addUser(user);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("用户名包含非法字符"));
    }

    // 测试弱密码
    @Test
    void testAddUser_WeakPassword() {
        User user = User.builder()
                .username("testuser")
                .password("123")
                .build();

        Result<String> result = userService.addUser(user);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("密码强度不足"));
    }

    // 测试无效邮箱格式
    @Test
    void testAddUser_InvalidEmail() {
        User user = User.builder()
                .username("testuser")
                .password("Test@123456")
                .email("invalid-email")
                .build();

        Result<String> result = userService.addUser(user);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("邮箱格式不正确"));
    }

    // 测试无效手机号
    @Test
    void testAddUser_InvalidPhone() {
        User user = User.builder()
                .username("testuser")
                .password("Test@123456")
                .phone("invalid-phone")
                .build();

        Result<String> result = userService.addUser(user);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("手机号格式不正确"));
    }
}
```

#### 2.2 并发测试

```java
// UserServiceConcurrencyTest.java
@DisplayName("用户服务并发测试")
public class UserServiceConcurrencyTest {

    @Test
    void testConcurrentAddUser_NoDuplicate() {
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicBoolean hasError = new AtomicBoolean(false);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    User user = User.builder()
                            .username("concurrent_user_" + index)
                            .password("Test@123456")
                            .build();

                    Result<String> result = userService.addUser(user);

                    if (!result.isSuccess()) {
                        hasError.set(true);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // 验证所有用户都添加成功
        assertFalse(hasError.get(), "并发添加用户时出现错误");
    }

    @Test
    void testConcurrentUpdateUser_NoConflict() {
        Long userId = 1L;
        int threadCount = 5;
        CountDownLatch latch = new CountDownLatch(threadCount);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    User user = new User();
                    user.setId(userId);
                    user.setName("并发更新测试" + i);

                    userService.updateUser(user);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // 验证所有更新都成功（最终结果应该是最后一次更新）
        User finalUser = userService.getUserById(userId);
        assertNotNull(finalUser);
    }
}
```

### 阶段三：集成测试补充（5%覆盖）

#### 3.1 完整业务流程测试

```java
// CompleteBusinessFlowTest.java
@DisplayName("完整业务流程测试")
public class CompleteBusinessFlowTest {

    @Test
    @Order(1)
    @DisplayName("完整的数据资产发现到审核流程")
    void testCompleteAssetDiscoveryAndReviewFlow() {
        // Step 1: 启动资产发现
        Long taskId = startAssetDiscovery(dataSourceId);
        assertNotNull(taskId);

        // Step 2: 等待扫描完成
        waitForScanComplete(taskId);

        // Step 3: 人工标注资产
        Long assetId = manualClassifyAsset(taskId, 3, operatorId);

        // Step 4: 提交审核
        submitForReview(assetId, 3, "测试");

        // Step 5: 审核通过
        approveAsset(assetId, true, "审核通过", reviewerId);

        // Step 6: 验证资产状态
        DataAsset asset = getAssetDetail(assetId);
        assertEquals("APPROVED", asset.getStatus());
    }

    @Test
    @Order(2)
    @DisplayName("完整的用户权限管理流程")
    void testCompleteUserPermissionFlow() {
        // Step 1: 创建角色
        Long roleId = createRole("测试角色", "TEST_ROLE");

        // Step 2: 分配权限给角色
        assignPermissionsToRole(roleId, Arrays.asList(1L, 2L, 3L));

        // Step 3: 创建用户
        Long userId = createUser("testuser", "123456");

        // Step 4: 分配角色给用户
        assignRoleToUser(userId, roleId);

        // Step 5: 验证用户权限
        List<Permission> permissions = getUserPermissions(userId);
        assertTrue(permissions.containsAll(Arrays.asList(1L, 2L, 3L)));

        // Step 6: 撤销角色
        revokeRoleFromUser(userId, roleId);

        // Step 7: 验证权限已撤销
        permissions = getUserPermissions(userId);
        assertTrue(permissions.isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("完整的审计日志流程")
    void testCompleteAuditLogFlow() {
        // Step 1: 用户登录
        login("testuser", "123456");

        // Step 2: 查询数据
        queryUserList();

        // Step 3: 修改数据
        updateUser(userId, newUserData);

        // Step 4: 删除数据
        deleteUser(anotherUserId);

        // Step 5: 查询审计日志
        List<AuditLog> logs = getAuditLogs(userId);

        // Step 6: 验证所有操作都有审计记录
        assertTrue(containsLogOperation(logs, "LOGIN"));
        assertTrue(containsLogOperation(logs, "QUERY"));
        assertTrue(containsLogOperation(logs, "UPDATE"));
        assertTrue(containsLogOperation(logs, "DELETE"));

        // Step 7: 验证审计日志已上链
        AuditLog lastLog = logs.get(logs.size() - 1);
        assertNotNull(lastLog.getBlockchainHash());
    }
}
```

### 阶段四：性能测试补充（5%覆盖）

#### 4.1 API性能测试

```java
// APIPerformanceTest.java
@DisplayName("API性能测试")
public class APIPerformanceTest {

    @Test
    @DisplayName("用户管理API性能测试")
    void testUserManagementAPI_Performance() {
        // 测试创建用户性能
        long startTime = System.currentTimeMillis();
        createUser("perftest", "Test@123456");
        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 1000, "创建用户应在1秒内完成");

        // 测试查询用户性能
        startTime = System.currentTimeMillis();
        getUserById(1L);
        duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 500, "查询用户应在500ms内完成");

        // 测试分页查询性能
        startTime = System.currentTimeMillis();
        getUserPage(1, 10);
        duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 1000, "分页查询应在1秒内完成");
    }

    @Test
    @DisplayName("资产管理API性能测试")
    void testAssetManagementAPI_Performance() {
        // 测试资产列表查询性能
        long startTime = System.currentTimeMillis();
        getAssetList(1, 10);
        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 1500, "查询资产列表应在1.5秒内完成");

        // 测试资产详情查询性能
        startTime = System.currentTimeMillis();
        getAssetDetail(1L);
        duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 800, "查询资产详情应在800ms内完成");
    }

    @Test
    @DisplayName("审计日志查询性能测试")
    void testAuditLogQuery_Performance() {
        // 测试大批量审计日志查询性能
        long startTime = System.currentTimeMillis();
        getAuditLogs(1000);
        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 3000, "查询1000条审计日志应在3秒内完成");
    }
}
```

---

## 📝 测试执行计划

### 每日执行

```bash
#!/bin/bash
# scripts/run-daily-tests.sh

# 执行单元测试
echo "执行单元测试..."
mvn test -Dtest="**/*UnitTest.java"

# 生成覆盖率报告
echo "生成覆盖率报告..."
mvn jacoco:report

# 查看覆盖率
echo "查看覆盖率报告..."
open target/site/jacoco/index.html
```

### 每周执行

```bash
#!/bin/bash
# scripts/run-weekly-tests.sh

# 执行所有测试
echo "执行所有测试..."
mvn clean verify

# 检查覆盖率是否达标
echo "检查覆盖率..."
COVERAGE=$(cat target/site/jacoco/index.html | grep -oP '<tfoot>.*<td class="cover">\([^<]*\)%</td>.*</tfoot>' | sed 's/.*cover">\([0-9]*\)%.*/\1/')

echo "当前覆盖率: ${COVERAGE}%"

if (( $(echo "$COVERAGE < 85" | bc -l) )); then
    echo "覆盖率未达标，当前${COVERAGE}%，目标85%"
    exit 1
else
    echo "覆盖率达标: ${COVERAGE}%"
fi
```

---

## 📊 覆盖率提升追踪

| 阶段 | 目标覆盖 | 当前覆盖 | 进展 | 状态 |
|--------|-----------|-----------|------|------|
| 阶段一 | 15% | 10% | 67% | 进行中 |
| 阶段二 | 20% | 15% | 75% | 待开始 |
| 阶段三 | 25% | 20% | 80% | 待开始 |
| 阶段四 | 30% | 25% | 83% | 待开始 |
| **总计** | **85%** | **60%** | **71%** | 进行中 |

---

## 🎯 下一步行动

1. **立即行动**（本周内）:
   - [ ] 补充监控告警模块测试
   - [ ] 补充合规报告模块测试
   - [ ] 运行完整测试套件
   - [ ] 生成覆盖率报告

2. **短期行动**（2周内）:
   - [ ] 补充边界条件测试
   - [ ] 补充并发测试
   - [ ] 补充完整业务流程测试
   - [ ] 执行性能测试

3. **长期行动**（1个月内）:
   - [ ] 建立持续集成测试
   - [ ] 配置自动化测试报告
   - [ ] 设置覆盖率门禁
   - [ ] 优化测试用例

---

## 🔧 工具推荐

- **单元测试**: JUnit 5 + Mockito
- **集成测试**: RestAssured + Testcontainers
- **性能测试**: JMH + k6
- **覆盖率工具**: JaCoCo + SonarQube
- **测试报告**: Allure + JaCoCo

---

**文档版本**: v1.0.0
**最后更新**: 2025-12-25
