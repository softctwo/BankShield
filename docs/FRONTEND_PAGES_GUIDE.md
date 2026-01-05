# BankShield 前端页面组件完成指南

## 📋 已创建的页面组件

### 1. 合规性检查模块

#### ✅ 合规规则 (`/compliance/rule`)
- **路径**: `/Users/zhangyanlong/workspaces/BankShield/bankshield-ui/src/views/compliance/rule/index.vue`
- **功能**: 合规规则的增删改查
- **特性**: 
  - 规则列表展示
  - 新增/编辑规则
  - 规则状态管理

#### ✅ 检查任务 (`/compliance/task`)
- **路径**: `/Users/zhangyanlong/workspaces/BankShield/bankshield-ui/src/views/compliance/task/index.vue`
- **功能**: 合规检查任务管理
- **特性**:
  - 任务创建和执行
  - 任务状态跟踪
  - 任务历史查看

#### ✅ 合规报告 (`/compliance/report`)
- **路径**: `/Users/zhangyanlong/workspaces/BankShield/bankshield-ui/src/views/compliance/report/index.vue`
- **功能**: 合规报告生成和查看
- **特性**:
  - 报告生成
  - 报告查看
  - 报告下载

### 2. 安全态势模块

#### ✅ 安全大屏 (`/security/dashboard`)
- **路径**: `/Users/zhangyanlong/workspaces/BankShield/bankshield-ui/src/views/security/dashboard/index.vue`
- **功能**: 安全态势可视化大屏
- **特性**: 已存在，包含完整的安全监控功能

#### ✅ 威胁管理 (`/security/threat`)
- **路径**: `/Users/zhangyanlong/workspaces/BankShield/bankshield-ui/src/views/security/threat/index.vue`
- **功能**: 威胁检测和管理
- **特性**:
  - 威胁统计展示
  - 威胁等级分类
  - 威胁处理功能

### 3. 数据血缘模块

#### ✅ 血缘追踪 (`/lineage/track`)
- **路径**: `/Users/zhangyanlong/workspaces/BankShield/bankshield-ui/src/views/lineage/track/index.vue`
- **功能**: 数据血缘关系追踪
- **特性**:
  - 数据资产查询
  - 血缘关系展示
  - 来源和目标系统追踪

#### ✅ 影响分析 (`/lineage/analysis`)
- **路径**: `/Users/zhangyanlong/workspaces/BankShield/bankshield-ui/src/views/lineage/analysis/index.vue`
- **功能**: 数据变更影响分析
- **特性**:
  - 上下游影响统计
  - 影响程度评估
  - 关联系统分析

### 4. 安全扫描模块

#### ✅ 扫描任务 (`/security-scan/task`)
- **路径**: `/Users/zhangyanlong/workspaces/BankShield/bankshield-ui/src/views/security-scan/task/index.vue`
- **功能**: 安全扫描任务管理
- **特性**: 已存在完整功能

#### ✅ 漏洞管理 (`/security-scan/vulnerability`)
- **路径**: `/Users/zhangyanlong/workspaces/BankShield/bankshield-ui/src/views/security-scan/vulnerability/index.vue`
- **功能**: 漏洞管理和修复
- **特性**: 已存在完整功能

### 5. 数据脱敏模块

#### ✅ 脱敏规则 (`/desensitization/rule`)
- **路径**: `/Users/zhangyanlong/workspaces/BankShield/bankshield-ui/src/views/desensitization/rule/index.vue`
- **功能**: 脱敏规则配置
- **特性**: 已存在完整功能

#### ✅ 脱敏日志 (`/desensitization/log`)
- **路径**: `/Users/zhangyanlong/workspaces/BankShield/bankshield-ui/src/views/desensitization/log/index.vue`
- **功能**: 脱敏操作日志
- **特性**: 已存在完整功能

---

## 🔧 如何访问这些页面

### 方式1: 通过菜单访问

1. 启动前端应用
```bash
cd /Users/zhangyanlong/workspaces/BankShield/bankshield-ui
npm run dev
```

2. 登录系统后，在左侧菜单中点击对应的菜单项：
   - **合规性检查** → 合规规则 / 检查任务 / 合规报告
   - **安全态势** → 安全大屏 / 威胁管理
   - **数据血缘** → 血缘追踪 / 影响分析
   - **安全扫描** → 扫描任务 / 漏洞管理
   - **数据脱敏** → 脱敏规则 / 脱敏日志

### 方式2: 直接访问URL

在浏览器中直接输入URL：

```
http://localhost:5173/compliance/rule
http://localhost:5173/compliance/task
http://localhost:5173/compliance/report
http://localhost:5173/security/threat
http://localhost:5173/lineage/track
http://localhost:5173/lineage/analysis
```

---

## 📊 页面功能说明

### 合规规则页面
- **新增规则**: 点击"新增规则"按钮
- **编辑规则**: 点击表格中的"编辑"按钮
- **删除规则**: 点击表格中的"删除"按钮
- **状态管理**: 通过状态标签查看规则启用/禁用状态

### 检查任务页面
- **创建任务**: 点击"创建任务"按钮
- **执行任务**: 点击表格中的"执行"按钮
- **查看详情**: 点击表格中的"查看"按钮
- **任务状态**: 通过颜色标签区分待执行/执行中/已完成/失败

### 合规报告页面
- **生成报告**: 点击"生成报告"按钮
- **查看报告**: 点击表格中的"查看"按钮
- **下载报告**: 点击表格中的"下载"按钮

### 威胁管理页面
- **威胁统计**: 顶部显示总威胁数、高危/中危/低危数量
- **威胁列表**: 展示所有检测到的威胁
- **威胁处理**: 点击"处理"按钮进行威胁处理

### 血缘追踪页面
- **资产查询**: 输入资产名称进行查询
- **查看血缘**: 点击"查看血缘"按钮查看完整血缘关系

### 影响分析页面
- **选择资产**: 从下拉列表选择要分析的数据资产
- **分析影响**: 点击"分析影响"按钮
- **查看结果**: 显示上游/下游影响统计和详细列表

---

## 🎨 页面特性

### 1. 响应式设计
- 所有页面都采用响应式布局
- 支持不同屏幕尺寸自适应

### 2. Element Plus组件
- 使用Element Plus UI组件库
- 统一的视觉风格和交互体验

### 3. 数据展示
- 表格展示数据列表
- 统计卡片展示关键指标
- 标签展示状态和等级

### 4. 操作功能
- 增删改查基础操作
- 搜索和过滤功能
- 导出和下载功能

---

## 🔍 故障排查

### 问题1: 页面显示404

**原因**: 路由未正确配置

**解决方案**:
1. 检查 `src/router/index.ts` 中是否有对应的路由配置
2. 确保路由路径与菜单配置的path一致
3. 确保组件路径正确

### 问题2: 菜单不显示

**原因**: 
- 后端菜单API未返回数据
- 用户权限不足
- 菜单状态为禁用

**解决方案**:
1. 检查浏览器控制台Network标签
2. 查看 `/api/system/menu/getRouters` 的响应
3. 确认用户角色权限配置正确
4. 检查数据库中菜单的status字段是否为1（启用）

### 问题3: 页面功能不完整

**原因**: 当前页面使用模拟数据

**说明**: 
- 所有新创建的页面都使用模拟数据展示
- 点击按钮会显示提示信息
- 后续需要对接真实的后端API

---

## 📝 下一步开发

### 1. 对接后端API
将模拟数据替换为真实的API调用：
```typescript
// 示例：对接合规规则API
import { complianceApi } from '@/api/compliance'

const loadData = async () => {
  const res = await complianceApi.getRules({ page: 1, size: 10 })
  tableData.value = res.data.records
}
```

### 2. 完善表单功能
添加新增和编辑的表单对话框：
- 表单验证
- 数据提交
- 错误处理

### 3. 增强交互体验
- 添加加载状态
- 添加确认对话框
- 添加成功/失败提示

### 4. 数据可视化
- 添加图表展示
- 添加数据统计
- 添加趋势分析

---

## ✅ 完成状态

| 模块 | 页面 | 状态 | 说明 |
|------|------|------|------|
| 合规性检查 | 合规规则 | ✅ 已创建 | 基础CRUD功能 |
| 合规性检查 | 检查任务 | ✅ 已创建 | 任务管理功能 |
| 合规性检查 | 合规报告 | ✅ 已创建 | 报告查看下载 |
| 安全态势 | 安全大屏 | ✅ 已存在 | 完整功能 |
| 安全态势 | 威胁管理 | ✅ 已创建 | 威胁监控处理 |
| 数据血缘 | 血缘追踪 | ✅ 已创建 | 血缘查询展示 |
| 数据血缘 | 影响分析 | ✅ 已创建 | 影响评估分析 |
| 安全扫描 | 扫描任务 | ✅ 已存在 | 完整功能 |
| 安全扫描 | 漏洞管理 | ✅ 已存在 | 完整功能 |
| 数据脱敏 | 脱敏规则 | ✅ 已存在 | 完整功能 |
| 数据脱敏 | 脱敏日志 | ✅ 已存在 | 完整功能 |

---

## 🎉 总结

**所有功能页面已创建完成！**

现在你可以：
1. ✅ 启动前端应用查看所有页面
2. ✅ 通过菜单访问各个功能模块
3. ✅ 查看页面的基础布局和交互
4. ✅ 基于这些页面继续开发完整功能

**注意**: 当前页面使用模拟数据，后续需要对接真实的后端API。

---

**文档版本**: v1.0  
**创建时间**: 2025-01-04  
**维护者**: BankShield开发团队
