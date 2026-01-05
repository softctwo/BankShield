# 数据生命周期管理功能开发总结

## 📊 项目概览

**功能名称**: 数据生命周期管理  
**开发日期**: 2024-12-31  
**功能状态**: ✅ 已完成  
**优先级**: P0核心功能  

---

## 🎯 功能简介

数据生命周期管理是BankShield系统的核心功能之一，实现了数据从采集、存储、使用、归档到销毁的全生命周期管理。该功能符合金融行业数据管理规范，确保数据在整个生命周期内的安全性和合规性。

### 核心功能

1. **生命周期策略管理**
   - 策略配置（保留期、归档期、销毁期）
   - 策略优先级管理
   - 策略启用/禁用控制

2. **自动归档服务**
   - 基于策略的自动归档
   - 手动归档和批量归档
   - 归档数据完整性验证

3. **安全销毁服务**
   - 逻辑删除、物理删除、覆盖删除
   - 销毁审批流程
   - 销毁记录追踪

4. **定时任务调度**
   - 自动归档任务（每天凌晨2点）
   - 自动销毁任务（每天凌晨3点）
   - 统计任务（每小时）

5. **监控与审计**
   - 实时统计信息
   - 执行记录追踪
   - 待处理数据监控

---

## 🗄️ 数据库设计

### 表结构（6个核心表）

#### 1. lifecycle_policy - 生命周期策略表
```sql
- id: 策略ID
- policy_name: 策略名称
- policy_code: 策略编码
- data_type: 数据类型（DATABASE/FILE/BIGDATA）
- sensitivity_level: 敏感级别（C1-C5）
- retention_days: 保留天数
- archive_enabled: 是否启用归档
- archive_days: 归档天数
- archive_storage: 归档存储位置
- destroy_enabled: 是否启用销毁
- destroy_days: 销毁天数
- destroy_method: 销毁方法（LOGICAL/PHYSICAL/OVERWRITE）
- approval_required: 是否需要审批
- notification_enabled: 是否启用通知
- notification_days: 提前通知天数
- policy_status: 策略状态（ACTIVE/INACTIVE）
- priority: 优先级
```

#### 2. lifecycle_execution - 生命周期执行记录表
```sql
- id: 执行ID
- policy_id: 策略ID
- execution_type: 执行类型（ARCHIVE/DESTROY）
- asset_id: 资产ID
- asset_name: 资产名称
- execution_status: 执行状态（PENDING/RUNNING/SUCCESS/FAILED）
- start_time: 开始时间
- end_time: 结束时间
- duration: 执行耗时
- affected_count: 影响记录数
- error_message: 错误信息
- executor: 执行人
- execution_mode: 执行模式（AUTO/MANUAL）
```

#### 3. archived_data - 归档数据表
```sql
- id: 归档ID
- original_table: 原始表名
- original_id: 原始记录ID
- data_content: 数据内容（JSON格式）
- archive_time: 归档时间
- archive_storage: 归档存储位置
- policy_id: 策略ID
- retention_until: 保留截止日期
- is_destroyed: 是否已销毁
- destroy_time: 销毁时间
```

#### 4. destruction_record - 销毁记录表
```sql
- id: 销毁ID
- asset_id: 资产ID
- destruction_method: 销毁方法
- destruction_reason: 销毁原因
- data_snapshot: 数据快照
- destruction_time: 销毁时间
- approval_status: 审批状态（PENDING/APPROVED/REJECTED）
- approver: 审批人
- approval_time: 审批时间
- verification_hash: 验证哈希（SM3）
```

#### 5. lifecycle_notification - 生命周期通知表
```sql
- id: 通知ID
- notification_type: 通知类型
- policy_id: 策略ID
- notification_content: 通知内容
- recipient: 接收人
- notification_status: 通知状态
- send_time: 发送时间
```

#### 6. lifecycle_statistics - 生命周期统计表
```sql
- id: 统计ID
- stat_date: 统计日期
- total_policies: 总策略数
- active_policies: 活跃策略数
- total_archives: 总归档数
- today_archives: 今日归档数
- total_destructions: 总销毁数
- today_destructions: 今日销毁数
```

### 视图（3个）

1. **v_lifecycle_overview** - 生命周期概览视图
2. **v_pending_archive** - 待归档数据视图
3. **v_pending_destruction** - 待销毁数据视图

### 存储过程（2个）

1. **sp_auto_archive_data** - 自动归档存储过程
2. **sp_auto_destroy_data** - 自动销毁存储过程

### 默认策略（7条）

1. C5极敏感数据策略：保留7年，5年后归档，7年后覆盖销毁
2. C4高敏感数据策略：保留5年，3年后归档，5年后覆盖销毁
3. C3敏感数据策略：保留3年，2年后归档，3年后物理删除
4. C2内部数据策略：保留2年，1年后归档，2年后逻辑删除
5. C1公开数据策略：保留1年，半年后归档，1年后逻辑删除
6. 审计日志策略：保留5年，1年后归档，5年后物理删除
7. 临时文件策略：保留30天后直接物理删除

---

## 💻 后端开发

### 实体类（4个）

1. **LifecyclePolicy.java** - 生命周期策略实体
2. **LifecycleExecution.java** - 执行记录实体
3. **ArchivedData.java** - 归档数据实体
4. **DestructionRecord.java** - 销毁记录实体

### Mapper接口（4个）

1. **LifecyclePolicyMapper.java**
   - 查询活跃策略
   - 根据数据类型和敏感级别查询策略
   - 查询归档/销毁策略

2. **LifecycleExecutionMapper.java**
   - 根据策略ID/资产ID查询执行记录
   - 查询最近执行记录
   - 统计执行状态

3. **ArchivedDataMapper.java**
   - 根据原始表和ID查询归档数据
   - 查询待销毁数据
   - 统计归档数据

4. **DestructionRecordMapper.java**
   - 查询销毁记录
   - 查询待审批记录
   - 统计销毁数据

### Service服务层（2个接口 + 2个实现）

#### 1. LifecyclePolicyService
```java
- 分页查询策略
- 获取活跃策略
- 创建/更新/删除策略
- 启用/禁用策略
- 调整优先级
```

#### 2. LifecycleManagementService
```java
- 归档单个/批量数据资产
- 自动归档（根据策略）
- 销毁单个/批量归档数据
- 自动销毁（根据策略）
- 获取待归档/待销毁数据
- 审批销毁申请
- 验证归档数据完整性
- 恢复归档数据
- 获取统计信息
```

### Controller API层（1个）

**LifecycleController.java** - 提供23个RESTful API接口

#### 策略管理API（7个）
- `GET /api/lifecycle/policies` - 分页查询策略
- `GET /api/lifecycle/policies/active` - 获取活跃策略
- `GET /api/lifecycle/policies/{id}` - 获取策略详情
- `POST /api/lifecycle/policies` - 创建策略
- `PUT /api/lifecycle/policies/{id}` - 更新策略
- `DELETE /api/lifecycle/policies/{id}` - 删除策略
- `PUT /api/lifecycle/policies/{id}/status` - 切换策略状态

#### 归档管理API（4个）
- `POST /api/lifecycle/archive/{assetId}` - 归档单个资产
- `POST /api/lifecycle/archive/batch` - 批量归档
- `POST /api/lifecycle/archive/auto/{policyId}` - 自动归档
- `GET /api/lifecycle/pending-archive/{policyId}` - 获取待归档数据

#### 销毁管理API（4个）
- `POST /api/lifecycle/destroy/{archiveId}` - 销毁单个归档数据
- `POST /api/lifecycle/destroy/batch` - 批量销毁
- `POST /api/lifecycle/destroy/auto/{policyId}` - 自动销毁
- `GET /api/lifecycle/pending-destruction` - 获取待销毁数据

#### 监控与审计API（8个）
- `GET /api/lifecycle/executions` - 获取执行记录
- `GET /api/lifecycle/archived/{id}` - 获取归档数据详情
- `GET /api/lifecycle/destruction-records` - 获取销毁记录
- `POST /api/lifecycle/destruction-records/{recordId}/approve` - 审批销毁申请
- `GET /api/lifecycle/statistics` - 获取统计信息
- `GET /api/lifecycle/archived/{id}/verify` - 验证归档数据
- `POST /api/lifecycle/archived/{id}/restore` - 恢复归档数据
- `PUT /api/lifecycle/policies/{id}/priority` - 调整优先级

### 定时任务（1个）

**LifecycleScheduledJob.java** - 4个定时任务

1. **自动归档任务** - 每天凌晨2点执行
   - 遍历所有启用归档的策略
   - 查找符合归档条件的数据
   - 执行自动归档

2. **自动销毁任务** - 每天凌晨3点执行
   - 遍历所有启用销毁的策略
   - 查找保留期满的归档数据
   - 执行自动销毁（跳过需要审批的）

3. **统计任务** - 每小时执行
   - 更新生命周期统计信息

4. **数据完整性验证任务** - 每天凌晨4点执行
   - 验证归档数据的完整性

---

## 🎨 前端开发

### 页面组件（2个）

#### 1. 策略配置页面 - `/views/lifecycle/policy-management/index.vue`

**功能特性**:
- 策略列表展示（分页、搜索、筛选）
- 新增/编辑策略对话框
- 策略启用/禁用切换
- 策略执行（归档/销毁）
- 策略删除

**UI组件**:
- 搜索表单（策略名称、状态筛选）
- 数据表格（显示策略详情）
- 编辑对话框（完整的策略配置表单）
- 执行对话框（选择执行类型和模式）

**交互功能**:
- 实时查询和分页
- 表单验证
- 状态切换确认
- 删除确认提示
- 执行结果反馈

#### 2. 生命周期监控页面 - `/views/lifecycle/monitor/index.vue`

**功能特性**:
- 统计卡片展示（总策略数、已归档数、已销毁数、待审批数）
- 待归档数据列表（支持单个/批量归档）
- 待销毁数据列表（支持单个/批量销毁）
- 执行记录列表（按策略筛选）
- 自动刷新统计信息（30秒）

**UI组件**:
- 4个统计卡片（带图标和颜色）
- 待归档数据表格（支持多选）
- 待销毁数据表格（支持多选）
- 执行记录表格（显示详细信息）
- 销毁确认对话框（输入销毁原因）

**交互功能**:
- 实时数据刷新
- 批量操作
- 销毁原因填写
- 执行结果反馈
- 策略筛选

---

## 🔧 核心技术特性

### 1. 策略驱动的生命周期管理

- **灵活的策略配置**: 支持按数据类型、敏感级别配置不同的生命周期策略
- **优先级机制**: 多个策略匹配时，按优先级选择最合适的策略
- **动态启用/禁用**: 可随时调整策略状态，不影响已执行的操作

### 2. 自动化归档机制

- **定时自动归档**: 每天自动检查并归档符合条件的数据
- **JSON格式存储**: 归档数据以JSON格式保存，便于恢复和查询
- **归档位置配置**: 支持配置不同的归档存储位置
- **完整性验证**: 使用SM3哈希算法验证归档数据完整性

### 3. 安全销毁机制

- **三种销毁方法**:
  - 逻辑删除：标记为已删除，数据仍保留
  - 物理删除：从数据库中永久删除
  - 覆盖删除：多次覆盖后删除，确保无法恢复

- **审批流程**: 支持配置销毁前需要审批
- **销毁记录**: 完整记录销毁操作，包括数据快照和验证哈希
- **防误删**: 销毁前需要填写原因，重要数据需要审批

### 4. 事务保证

- **原子性操作**: 归档和销毁操作使用事务保证原子性
- **执行记录**: 每次操作都记录执行状态和结果
- **错误处理**: 完善的异常处理和错误信息记录

### 5. 监控与审计

- **实时统计**: 实时统计策略数、归档数、销毁数等关键指标
- **执行追踪**: 完整记录每次归档和销毁操作的详细信息
- **待处理监控**: 实时监控待归档和待销毁的数据
- **审批管理**: 支持销毁申请的审批流程

---

## 📁 文件清单

### SQL脚本（1个）
```
sql/data_lifecycle_management.sql (320行)
```

### Java后端（15个文件）

#### 实体类（4个）
```
entity/LifecyclePolicy.java
entity/LifecycleExecution.java
entity/ArchivedData.java
entity/DestructionRecord.java
```

#### Mapper接口（4个）
```
mapper/LifecyclePolicyMapper.java
mapper/LifecycleExecutionMapper.java
mapper/ArchivedDataMapper.java
mapper/DestructionRecordMapper.java
```

#### Service层（4个）
```
service/LifecyclePolicyService.java
service/LifecycleManagementService.java
service/impl/LifecyclePolicyServiceImpl.java
service/impl/LifecycleManagementServiceImpl.java
```

#### Controller层（1个）
```
controller/LifecycleController.java (600行)
```

#### 定时任务（1个）
```
job/LifecycleScheduledJob.java
```

#### 工具类（复用）
```
util/SM3Util.java (用于数据完整性验证)
```

### Vue前端（2个页面）

```
views/lifecycle/policy-management/index.vue (500行)
views/lifecycle/monitor/index.vue (550行)
```

**总计**: 18个文件，约3500+行代码

---

## 🚀 核心业务流程

### 1. 自动归档流程

```
1. 定时任务触发（每天凌晨2点）
   ↓
2. 查询所有启用归档的策略
   ↓
3. 对每个策略：
   - 查找符合归档条件的数据资产
   - 检查是否已归档
   - 创建归档记录
   ↓
4. 执行归档操作：
   - 将数据转为JSON格式
   - 保存到归档表
   - 计算保留截止日期
   - 记录执行结果
   ↓
5. 更新统计信息
```

### 2. 自动销毁流程

```
1. 定时任务触发（每天凌晨3点）
   ↓
2. 查询所有启用销毁的策略
   ↓
3. 对每个策略：
   - 查找保留期满的归档数据
   - 检查是否需要审批
   - 跳过需要审批的数据
   ↓
4. 执行销毁操作：
   - 创建销毁记录
   - 保存数据快照
   - 计算验证哈希（SM3）
   - 标记为已销毁
   - 记录执行结果
   ↓
5. 更新统计信息
```

### 3. 手动归档流程

```
1. 用户选择数据资产
   ↓
2. 选择归档策略
   ↓
3. 系统验证：
   - 资产是否存在
   - 是否已归档
   - 策略是否可用
   ↓
4. 执行归档（同自动归档）
   ↓
5. 返回执行结果
```

### 4. 销毁审批流程

```
1. 创建销毁申请
   ↓
2. 审批状态设为PENDING
   ↓
3. 审批人审批：
   - 填写审批意见
   - 选择批准/拒绝
   ↓
4. 如果批准：
   - 标记归档数据为已销毁
   - 记录销毁时间
   ↓
5. 如果拒绝：
   - 保留归档数据
   - 记录拒绝原因
```

---

## 📊 API接口文档

### 策略管理接口

#### 1. 分页查询策略
```
GET /api/lifecycle/policies
参数:
  - pageNum: 页码（默认1）
  - pageSize: 每页大小（默认10）
  - policyName: 策略名称（可选）
  - policyStatus: 策略状态（可选）
响应:
  - code: 200
  - data: 策略列表
  - total: 总数
```

#### 2. 创建策略
```
POST /api/lifecycle/policies
请求体: LifecyclePolicy对象
响应:
  - code: 200
  - message: 创建成功
  - data: 创建的策略
```

#### 3. 更新策略
```
PUT /api/lifecycle/policies/{id}
请求体: LifecyclePolicy对象
响应:
  - code: 200
  - message: 更新成功
```

### 归档管理接口

#### 4. 归档单个资产
```
POST /api/lifecycle/archive/{assetId}
参数:
  - policyId: 策略ID
  - operator: 操作人（默认admin）
响应:
  - code: 200
  - message: 归档成功
```

#### 5. 批量归档
```
POST /api/lifecycle/archive/batch
请求体:
  - assetIds: 资产ID列表
  - policyId: 策略ID
  - operator: 操作人
响应:
  - code: 200
  - total: 总数
  - success: 成功数
  - failed: 失败数
  - errors: 错误列表
```

### 销毁管理接口

#### 6. 销毁单个归档数据
```
POST /api/lifecycle/destroy/{archiveId}
参数:
  - reason: 销毁原因
  - operator: 操作人
响应:
  - code: 200
  - message: 销毁成功
```

#### 7. 审批销毁申请
```
POST /api/lifecycle/destruction-records/{recordId}/approve
参数:
  - approver: 审批人
  - comment: 审批意见
  - approved: 是否批准
响应:
  - code: 200
  - message: 审批成功
```

### 监控接口

#### 8. 获取统计信息
```
GET /api/lifecycle/statistics
响应:
  - code: 200
  - data:
    - totalPolicies: 总策略数
    - activePolicies: 活跃策略数
    - totalArchived: 总归档数
    - totalDestroyed: 总销毁数
    - pendingDestruction: 待销毁数
    - pendingApproval: 待审批数
```

---

## 🎯 功能亮点

### 1. 完整的生命周期管理

从数据采集到最终销毁，覆盖数据的整个生命周期，确保数据在每个阶段都得到适当的管理和保护。

### 2. 灵活的策略配置

支持按数据类型、敏感级别配置不同的生命周期策略，满足不同业务场景的需求。

### 3. 自动化执行

通过定时任务自动执行归档和销毁操作，减少人工干预，提高效率。

### 4. 安全的销毁机制

提供三种销毁方法，支持审批流程，确保重要数据不被误删。

### 5. 完整的审计追踪

记录每次操作的详细信息，包括执行人、执行时间、执行结果等，满足合规要求。

### 6. 数据完整性验证

使用SM3哈希算法验证归档数据的完整性，防止数据被篡改。

### 7. 友好的用户界面

提供直观的策略配置和监控界面，方便用户管理和监控数据生命周期。

---

## 📈 性能优化

### 1. 批量操作

支持批量归档和批量销毁，提高大量数据处理的效率。

### 2. 异步执行

定时任务异步执行，不影响系统正常运行。

### 3. 分页查询

所有列表查询都支持分页，避免一次加载大量数据。

### 4. 索引优化

在关键字段上创建索引，提高查询性能。

---

## 🔒 安全性保障

### 1. 事务保证

归档和销毁操作使用事务，确保数据一致性。

### 2. 审批机制

重要数据销毁前需要审批，防止误操作。

### 3. 完整性验证

使用SM3哈希算法验证归档数据完整性。

### 4. 操作记录

记录所有操作的执行人和执行时间，便于追溯。

---

## 📝 使用说明

### 1. 策略配置

1. 进入"策略配置"页面
2. 点击"新增策略"按钮
3. 填写策略信息：
   - 基本信息：策略名称、编码
   - 适用范围：数据类型、敏感级别
   - 归档配置：保留天数、归档天数、归档位置
   - 销毁配置：销毁天数、销毁方法
   - 其他配置：审批、通知、优先级
4. 点击"确定"保存策略

### 2. 手动归档

1. 进入"生命周期监控"页面
2. 在"待归档数据"列表中选择数据
3. 点击"归档"按钮
4. 系统自动执行归档操作

### 3. 销毁审批

1. 进入"生命周期监控"页面
2. 查看待审批的销毁申请
3. 填写审批意见
4. 选择批准或拒绝

### 4. 查看执行记录

1. 进入"生命周期监控"页面
2. 在"执行记录"列表中查看
3. 可按策略筛选记录

---

## 🎊 开发总结

### 已完成工作

1. ✅ 完整的数据库设计（6表 + 3视图 + 2存储过程）
2. ✅ 完整的后端服务（15个Java文件）
3. ✅ 完整的API接口（23个RESTful接口）
4. ✅ 定时任务调度（4个定时任务）
5. ✅ 前端页面开发（2个Vue页面）
6. ✅ 开发文档编写

### 技术亮点

1. **策略驱动**: 灵活的策略配置，支持多种业务场景
2. **自动化**: 定时任务自动执行归档和销毁
3. **安全性**: 审批流程、完整性验证、操作记录
4. **可追溯**: 完整的执行记录和审计日志
5. **易用性**: 友好的用户界面，简单的操作流程

### 符合标准

- ✅ 符合金融行业数据管理规范
- ✅ 满足数据安全合规要求
- ✅ 支持数据全生命周期管理
- ✅ 提供完整的审计追踪

---

## 🔗 相关文档

- [P0功能完整开发进度报告](./P0_COMPLETE_PROGRESS_REPORT.md)
- [数据分类分级开发总结](./P0_DEVELOPMENT_SUMMARY.md)
- [审计日志防篡改开发总结](./AUDIT_BLOCKCHAIN_SUMMARY.md)
- [项目开发指南](../AGENTS.md)

---

**文档版本**: v1.0  
**更新日期**: 2024-12-31  
**状态**: ✅ 已完成

---

**© 2024 BankShield. All Rights Reserved.**
