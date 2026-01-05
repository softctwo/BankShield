# 数据脱敏功能菜单配置文档

## 📋 配置概览

**配置时间**: 2024-12-31  
**功能模块**: 数据脱敏引擎  
**菜单数量**: 1个顶级菜单 + 4个功能菜单 + 16个权限按钮

---

## 🎯 菜单结构

### 顶级菜单

```
数据脱敏 (desensitization)
├── 脱敏规则 (desensitization-rule)
│   ├── 查询规则
│   ├── 新增规则
│   ├── 编辑规则
│   ├── 删除规则
│   └── 测试规则
├── 脱敏模板 (desensitization-template)
│   ├── 查询模板
│   ├── 新增模板
│   ├── 编辑模板
│   ├── 删除模板
│   └── 应用模板
├── 脱敏日志 (desensitization-log)
│   ├── 查询日志
│   ├── 导出日志
│   └── 统计分析
└── 脱敏测试 (desensitization-test)
    ├── 单条测试
    └── 批量测试
```

---

## 📊 菜单详细配置

### 1. 顶级菜单 - 数据脱敏

| 属性 | 值 |
|------|-----|
| 菜单名称 | 数据脱敏 |
| 菜单编码 | desensitization |
| 路由路径 | /desensitization |
| 图标 | Hide |
| 排序 | 30 |
| 菜单类型 | 目录 (0) |
| 权限标识 | desensitization |
| 状态 | 启用 (1) |

---

### 2. 功能菜单 - 脱敏规则

| 属性 | 值 |
|------|-----|
| 菜单名称 | 脱敏规则 |
| 菜单编码 | desensitization-rule |
| 路由路径 | rule |
| 组件路径 | /views/desensitization/rule/index.vue |
| 图标 | Document |
| 排序 | 1 |
| 菜单类型 | 菜单 (1) |
| 权限标识 | desensitization:rule:list |
| 状态 | 启用 (1) |

**子权限按钮**:
- ✅ 查询规则 (`desensitization:rule:query`)
- ✅ 新增规则 (`desensitization:rule:add`)
- ✅ 编辑规则 (`desensitization:rule:edit`)
- ✅ 删除规则 (`desensitization:rule:delete`)
- ✅ 测试规则 (`desensitization:rule:test`)

---

### 3. 功能菜单 - 脱敏模板

| 属性 | 值 |
|------|-----|
| 菜单名称 | 脱敏模板 |
| 菜单编码 | desensitization-template |
| 路由路径 | template |
| 组件路径 | /views/desensitization/template/index.vue |
| 图标 | Files |
| 排序 | 2 |
| 菜单类型 | 菜单 (1) |
| 权限标识 | desensitization:template:list |
| 状态 | 启用 (1) |

**子权限按钮**:
- ✅ 查询模板 (`desensitization:template:query`)
- ✅ 新增模板 (`desensitization:template:add`)
- ✅ 编辑模板 (`desensitization:template:edit`)
- ✅ 删除模板 (`desensitization:template:delete`)
- ✅ 应用模板 (`desensitization:template:apply`)

---

### 4. 功能菜单 - 脱敏日志

| 属性 | 值 |
|------|-----|
| 菜单名称 | 脱敏日志 |
| 菜单编码 | desensitization-log |
| 路由路径 | log |
| 组件路径 | /views/desensitization/log/index.vue |
| 图标 | List |
| 排序 | 3 |
| 菜单类型 | 菜单 (1) |
| 权限标识 | desensitization:log:list |
| 状态 | 启用 (1) |

**子权限按钮**:
- ✅ 查询日志 (`desensitization:log:query`)
- ✅ 导出日志 (`desensitization:log:export`)
- ✅ 统计分析 (`desensitization:log:statistics`)

---

### 5. 功能菜单 - 脱敏测试

| 属性 | 值 |
|------|-----|
| 菜单名称 | 脱敏测试 |
| 菜单编码 | desensitization-test |
| 路由路径 | test |
| 组件路径 | /views/desensitization/test/index.vue |
| 图标 | Operation |
| 排序 | 4 |
| 菜单类型 | 菜单 (1) |
| 权限标识 | desensitization:test |
| 状态 | 启用 (1) |

**子权限按钮**:
- ✅ 单条测试 (`desensitization:test:single`)
- ✅ 批量测试 (`desensitization:test:batch`)

---

## 🔑 权限标识说明

### 菜单类型

- **0**: 目录（顶级菜单，不对应具体页面）
- **1**: 菜单（对应具体页面）
- **2**: 按钮（页面内的操作按钮）

### 权限命名规范

格式：`模块:功能:操作`

示例：
- `desensitization:rule:list` - 脱敏规则列表查看
- `desensitization:rule:add` - 脱敏规则新增
- `desensitization:template:edit` - 脱敏模板编辑
- `desensitization:log:export` - 脱敏日志导出

---

## 📁 前端页面路径

需要创建以下Vue页面文件：

```
bankshield-ui/src/views/desensitization/
├── rule/
│   └── index.vue          # 脱敏规则管理页面
├── template/
│   └── index.vue          # 脱敏模板管理页面
├── log/
│   └── index.vue          # 脱敏日志查询页面
└── test/
    └── index.vue          # 脱敏测试工具页面
```

---

## 🚀 使用说明

### 1. 菜单已自动加载

菜单配置已经添加到数据库的 `sys_menu` 表中，系统启动后会自动加载。

### 2. 权限分配

管理员需要在**系统管理 > 角色管理**中为相应角色分配菜单权限：

1. 进入角色管理页面
2. 选择需要配置的角色
3. 在菜单权限树中勾选"数据脱敏"相关菜单
4. 保存配置

### 3. 菜单显示

用户登录后，如果其角色拥有"数据脱敏"菜单权限，则会在左侧导航栏看到：

```
📊 数据大屏
⚙️ 系统管理
📈 数据分类分级
🔒 数据脱敏          ← 新增菜单
   ├── 📄 脱敏规则
   ├── 📁 脱敏模板
   ├── 📋 脱敏日志
   └── 🔧 脱敏测试
```

---

## 🔧 SQL脚本

菜单配置SQL脚本位置：
```
/Users/zhangyanlong/workspaces/BankShield/sql/desensitization_menu.sql
```

如需重新执行：
```bash
mysql -u root -p bankshield < sql/desensitization_menu.sql
```

---

## 📝 后续工作

### 待开发前端页面

1. **脱敏规则管理页面** (约400行)
   - 规则列表展示
   - 新增/编辑规则对话框
   - 规则测试功能
   - 规则启用/禁用
   - 优先级调整

2. **脱敏模板管理页面** (约350行)
   - 模板列表展示
   - 新增/编辑模板对话框
   - 模板应用功能
   - 模板启用/禁用

3. **脱敏日志查询页面** (约300行)
   - 日志列表展示
   - 条件筛选
   - 统计图表
   - 导出功能

4. **脱敏测试工具页面** (约250行)
   - 单条数据测试
   - 批量数据测试
   - 测试结果展示
   - 规则选择

**总计**: 约1,300行前端代码

---

## ✅ 配置验证

### 验证步骤

1. **检查数据库**:
```sql
SELECT id, parent_id, menu_name, menu_code, path, icon, sort_order, menu_type, permission
FROM sys_menu
WHERE menu_code LIKE 'desensitization%' OR menu_name = '数据脱敏'
ORDER BY parent_id, sort_order;
```

2. **重启后端服务**:
```bash
./scripts/start.sh --stop
./scripts/start.sh --dev --skip-db
```

3. **登录系统验证**:
   - 访问 http://localhost:3000
   - 使用管理员账号登录
   - 检查左侧导航栏是否显示"数据脱敏"菜单

---

## 🎊 配置完成

数据脱敏功能菜单已成功配置！

**配置内容**:
- ✅ 1个顶级菜单
- ✅ 4个功能菜单
- ✅ 16个权限按钮
- ✅ 完整的权限控制体系

**下一步**:
1. 开发前端页面（4个Vue组件）
2. 配置路由
3. 实现页面功能
4. 测试菜单权限

---

**文档版本**: v1.0  
**更新日期**: 2024-12-31 15:30  
**状态**: ✅ 菜单配置完成

---

**© 2024 BankShield. All Rights Reserved.**
