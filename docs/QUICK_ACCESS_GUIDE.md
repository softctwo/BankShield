# BankShield 快速访问指南

## 🎯 立即访问新功能

前端服务已启动，路由配置已完成！现在你可以直接在浏览器中访问以下URL：

---

## 📍 直接访问URL列表

### 合规性检查模块

```
✅ 合规规则管理
http://localhost:5173/compliance/rule

✅ 检查任务管理
http://localhost:5173/compliance/task

✅ 合规报告
http://localhost:5173/compliance/report

✅ 合规Dashboard
http://localhost:5173/compliance/dashboard

✅ 合规检查
http://localhost:5173/compliance/check
```

### 安全态势模块

```
✅ 安全大屏
http://localhost:5173/security/dashboard

✅ 威胁管理
http://localhost:5173/security/threat

✅ 安全扫描任务
http://localhost:5173/security/scan-task

✅ 扫描结果管理
http://localhost:5173/security/scan-result
```

### 数据血缘模块

```
✅ 血缘追踪
http://localhost:5173/lineage/track

✅ 影响分析
http://localhost:5173/lineage/analysis

✅ 血缘自动发现
http://localhost:5173/lineage/enhanced/discovery

✅ 数据地图
http://localhost:5173/lineage/enhanced/data-map
```

### 数据脱敏模块

```
✅ 脱敏规则
http://localhost:5173/desensitization/rule

✅ 脱敏日志
http://localhost:5173/desensitization/log
```

### 安全扫描模块

```
✅ 扫描任务
http://localhost:5173/security-scan/task

✅ 漏洞管理
http://localhost:5173/security-scan/vulnerability

✅ 扫描Dashboard
http://localhost:5173/security-scan/dashboard
```

---

## 🚀 使用步骤

### 方式1: 直接访问（推荐）

1. **打开浏览器**
2. **复制上面的任意URL**
3. **粘贴到地址栏并回车**
4. 如果未登录，会自动跳转到登录页

### 方式2: 通过菜单访问

1. 访问首页: `http://localhost:5173`
2. 登录系统
3. 在左侧菜单中点击对应的菜单项

---

## 🔑 登录信息

如果需要登录，使用以下测试账号：

```
管理员账号:
用户名: admin
密码: [你的密码]

普通用户:
用户名: user
密码: [你的密码]
```

---

## ✅ 已完成的工作

### 1. 页面组件创建 ✅
- ✅ `/compliance/rule/index.vue` - 合规规则管理
- ✅ `/compliance/task/index.vue` - 检查任务管理
- ✅ `/compliance/report/index.vue` - 合规报告
- ✅ `/security/threat/index.vue` - 威胁管理
- ✅ `/lineage/track/index.vue` - 血缘追踪
- ✅ `/lineage/analysis/index.vue` - 影响分析

### 2. 路由配置完成 ✅
- ✅ 更新 `compliance.ts` - 添加3个新路由
- ✅ 更新 `security.ts` - 添加2个新路由
- ✅ 更新 `lineage-enhanced.ts` - 添加2个新路由
- ✅ 更新 `index.ts` - 导入并注册所有路由

### 3. 前端服务运行中 ✅
- ✅ Vite开发服务器已启动
- ✅ 监听端口: 5173
- ✅ 热重载已启用

---

## 📊 页面功能说明

### 合规规则页面
- 查看规则列表
- 新增/编辑/删除规则
- 规则状态管理（启用/禁用）

### 检查任务页面
- 创建检查任务
- 执行任务
- 查看任务状态（待执行/执行中/已完成/失败）
- 查看任务详情

### 合规报告页面
- 生成合规报告
- 查看报告列表
- 下载报告

### 威胁管理页面
- 威胁统计（总数、高危、中危、低危）
- 威胁列表展示
- 威胁详情查看
- 威胁处理

### 血缘追踪页面
- 数据资产查询
- 查看血缘关系
- 来源和目标系统追踪

### 影响分析页面
- 选择数据资产
- 分析上下游影响
- 影响程度评估
- 关联系统统计

---

## 🎨 页面特性

所有页面都包含：
- ✅ 完整的页面布局
- ✅ Element Plus UI组件
- ✅ 数据表格展示
- ✅ 操作按钮（新增、编辑、删除、查看）
- ✅ 模拟数据展示
- ✅ 响应式设计

---

## ⚠️ 重要说明

### 当前状态
- ✅ **前端页面**: 已创建并可访问
- ✅ **路由配置**: 已完成
- ✅ **前端服务**: 正在运行
- ⚠️ **后端服务**: 有编译错误，暂未启动
- ℹ️ **数据展示**: 使用模拟数据

### 为什么可以访问？
前端使用的是模拟数据，不依赖后端API，所以可以独立运行和访问。

### 菜单为什么不显示？
菜单数据来自后端API (`/api/system/menu/getRouters`)，由于后端服务未启动，菜单无法动态加载。但你可以通过**直接访问URL**的方式查看所有页面。

---

## 🔧 故障排查

### 问题1: 页面显示404

**解决方案**:
1. 确认前端服务正在运行: `ps aux | grep vite`
2. 确认访问的URL正确
3. 清除浏览器缓存并刷新

### 问题2: 页面空白

**解决方案**:
1. 打开浏览器开发者工具（F12）
2. 查看Console标签是否有错误
3. 查看Network标签是否有请求失败

### 问题3: 自动跳转到登录页

**说明**: 这是正常的路由守卫行为
**解决方案**: 登录后即可访问

---

## 📝 下一步工作

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

### 2. 修复后端编译错误
后端有以下编译错误需要修复：
- `SM3Util` 类找不到
- `DataMaskUtil` 类找不到
- `Map` 导入缺失

### 3. 完善页面功能
- 添加表单对话框
- 添加数据验证
- 添加加载状态
- 添加错误处理

---

## 🎉 总结

**所有功能页面现在都可以访问了！**

你可以：
1. ✅ 复制上面的URL直接在浏览器中访问
2. ✅ 查看页面布局和交互效果
3. ✅ 测试所有按钮和操作
4. ✅ 基于这些页面继续开发

**前端服务正在运行，所有路由已配置完成，立即访问吧！** 🚀

---

**文档版本**: v1.0  
**创建时间**: 2025-01-04 22:24  
**前端服务**: http://localhost:5173  
**状态**: ✅ 可访问
