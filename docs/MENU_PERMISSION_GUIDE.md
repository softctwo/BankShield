# BankShield 菜单权限使用指南

## 📋 目录

1. [功能概述](#功能概述)
2. [数据库设计](#数据库设计)
3. [后端实现](#后端实现)
4. [前端实现](#前端实现)
5. [使用示例](#使用示例)
6. [常见问题](#常见问题)

---

## 功能概述

BankShield 实现了完整的RBAC（基于角色的访问控制）菜单权限体系，包括：

- ✅ 动态菜单加载
- ✅ 按钮级权限控制
- ✅ 路由权限守卫
- ✅ 权限指令
- ✅ 菜单树形结构
- ✅ 角色权限分配

### 权限层级

```
用户 → 角色 → 菜单/权限
```

---

## 数据库设计

### 1. 菜单表 (sys_menu)

```sql
CREATE TABLE sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    order_num INT DEFAULT 0 COMMENT '显示顺序',
    path VARCHAR(200) COMMENT '路由地址',
    component VARCHAR(255) COMMENT '组件路径',
    menu_type CHAR(1) COMMENT '菜单类型（M目录 C菜单 F按钮）',
    perms VARCHAR(100) COMMENT '权限标识',
    icon VARCHAR(100) COMMENT '菜单图标',
    status CHAR(1) DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
    visible CHAR(1) DEFAULT '0' COMMENT '显示状态（0显示 1隐藏）'
);
```

### 2. 角色菜单关联表 (sys_role_menu)

```sql
CREATE TABLE sys_role_menu (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id)
);
```

### 3. 已初始化的菜单

系统已初始化以下菜单模块：

| 一级菜单 | 二级菜单 | 权限标识 |
|---------|---------|---------|
| 首页 | - | dashboard:view |
| 数据加密 | 加密管理、密钥管理 | encryption:* |
| 访问控制 | 用户管理、角色管理、权限管理 | user:*, role:*, permission:* |
| 审计追踪 | 审计日志、审计分析 | audit:* |
| 数据脱敏 | 脱敏规则、脱敏日志 | desensitization:* |
| 合规性检查 | 合规规则、检查任务、合规报告 | compliance:* |
| 安全态势 | 安全大屏、威胁管理 | security:* |
| 数据血缘 | 血缘追踪、影响分析 | lineage:* |
| 安全扫描 | 扫描任务、漏洞管理 | scan:* |
| 系统管理 | 菜单管理、字典管理、系统配置、日志管理 | system:* |

---

## 后端实现

### 1. 菜单实体类

```java
@Data
@TableName("sys_menu")
public class SysMenu {
    private Long id;
    private String menuName;
    private Long parentId;
    private Integer orderNum;
    private String path;
    private String component;
    private String menuType;  // M目录 C菜单 F按钮
    private String perms;
    private String icon;
    private String status;
    private String visible;
}
```

### 2. 菜单服务接口

```java
public interface SysMenuService {
    // 根据用户ID查询菜单树
    List<Map<String, Object>> getMenuTreeByUserId(Long userId);
    
    // 根据用户ID查询权限标识
    List<String> getPermsByUserId(Long userId);
    
    // 构建菜单树
    List<Map<String, Object>> buildMenuTree(List<SysMenu> menus);
}
```

### 3. API接口

```java
@RestController
@RequestMapping("/api/system/menu")
public class SysMenuController {
    
    // 获取路由信息
    @GetMapping("/getRouters")
    public Result<Map<String, Object>> getRouters() {
        Long userId = getCurrentUserId();
        List<Map<String, Object>> menus = menuService.getMenuTreeByUserId(userId);
        List<String> perms = menuService.getPermsByUserId(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("menus", menus);
        result.put("permissions", perms);
        return Result.success(result);
    }
}
```

---

## 前端实现

### 1. 权限Store

```typescript
// src/store/modules/permission.ts
export const usePermissionStore = defineStore('permission', {
  state: () => ({
    routes: [],
    permissions: []
  }),
  
  actions: {
    // 生成路由
    async generateRoutes() {
      const res = await getRouters()
      const { menus, permissions } = res.data
      this.permissions = permissions
      const accessedRoutes = this.filterAsyncRoutes(menus)
      return accessedRoutes
    },
    
    // 检查权限
    hasPermission(permission: string): boolean {
      return this.permissions.includes(permission)
    }
  }
})
```

### 2. 路由守卫

```typescript
// src/router/permission.ts
router.beforeEach(async (to, from, next) => {
  const hasToken = userStore.token
  
  if (hasToken) {
    if (!userStore.roles.length) {
      // 获取用户信息
      await userStore.getUserInfo()
      
      // 生成路由
      const accessRoutes = await permissionStore.generateRoutes()
      
      // 动态添加路由
      accessRoutes.forEach(route => router.addRoute(route))
      
      next({ ...to, replace: true })
    } else {
      next()
    }
  } else {
    next('/login')
  }
})
```

### 3. 权限指令

```typescript
// src/directives/permission.ts
export default {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value } = binding
    const permissionStore = usePermissionStore()
    
    if (value && value instanceof Array && value.length > 0) {
      const hasPermission = permissionStore.hasAnyPermission(value)
      
      if (!hasPermission) {
        el.parentNode?.removeChild(el)
      }
    }
  }
}
```

---

## 使用示例

### 1. 在Vue组件中使用权限指令

```vue
<template>
  <div>
    <!-- 按钮权限控制 -->
    <el-button v-permission="['user:add']" type="primary">
      新增用户
    </el-button>
    
    <el-button v-permission="['user:edit']" type="warning">
      编辑用户
    </el-button>
    
    <el-button v-permission="['user:delete']" type="danger">
      删除用户
    </el-button>
  </div>
</template>
```

### 2. 在代码中检查权限

```typescript
<script setup lang="ts">
import { usePermissionStore } from '@/store/modules/permission'

const permissionStore = usePermissionStore()

// 检查单个权限
if (permissionStore.hasPermission('user:add')) {
  console.log('有新增用户权限')
}

// 检查多个权限（任意一个）
if (permissionStore.hasAnyPermission(['user:add', 'user:edit'])) {
  console.log('有新增或编辑权限')
}

// 检查多个权限（全部）
if (permissionStore.hasAllPermissions(['user:add', 'user:edit'])) {
  console.log('同时有新增和编辑权限')
}
</script>
```

### 3. 菜单配置示例

```javascript
// 路由配置
{
  path: '/user',
  component: Layout,
  meta: { title: '用户管理', icon: 'user' },
  children: [
    {
      path: 'list',
      component: () => import('@/views/user/list.vue'),
      meta: { 
        title: '用户列表',
        perms: 'user:view'  // 权限标识
      }
    }
  ]
}
```

---

## 常见问题

### Q1: 如何添加新菜单？

**方法1: 通过SQL直接插入**

```sql
INSERT INTO sys_menu VALUES(
    NULL,                    -- id自动生成
    '新功能',                -- 菜单名称
    0,                       -- 父菜单ID（0表示一级菜单）
    10,                      -- 显示顺序
    '/new-feature',          -- 路由地址
    'new-feature/index',     -- 组件路径
    NULL,                    -- 路由参数
    1,                       -- 是否外链
    0,                       -- 是否缓存
    'C',                     -- 菜单类型（C=菜单）
    '0',                     -- 显示状态
    '0',                     -- 菜单状态
    'feature:view',          -- 权限标识
    'star',                  -- 图标
    'admin',                 -- 创建者
    NOW(),                   -- 创建时间
    '',                      -- 更新者
    NULL,                    -- 更新时间
    '新功能模块'             -- 备注
);
```

**方法2: 通过管理界面**

1. 登录系统
2. 进入 "系统管理" → "菜单管理"
3. 点击 "新增" 按钮
4. 填写菜单信息
5. 保存

### Q2: 如何为角色分配菜单权限？

```sql
-- 为角色ID=2分配菜单ID=601的权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 601);
```

### Q3: 权限不生效怎么办？

1. **检查数据库**: 确认菜单和权限已正确配置
2. **检查角色关联**: 确认用户的角色已关联相应菜单
3. **清除缓存**: 重新登录或清除浏览器缓存
4. **检查权限标识**: 确认前端使用的权限标识与数据库一致

### Q4: 如何实现动态菜单？

系统已实现动态菜单，流程如下：

1. 用户登录后，前端调用 `/api/system/menu/getRouters` 获取菜单
2. 后端根据用户角色查询可访问的菜单
3. 前端动态生成路由并添加到路由表
4. 侧边栏根据路由自动渲染菜单

### Q5: 按钮权限如何控制？

**方法1: 使用v-permission指令**

```vue
<el-button v-permission="['user:add']">新增</el-button>
```

**方法2: 在代码中判断**

```typescript
const showAddButton = computed(() => {
  return permissionStore.hasPermission('user:add')
})
```

---

## 初始化步骤

### 1. 执行SQL脚本

```bash
mysql -u root -p bankshield < sql/sys_menu.sql
```

### 2. 重启后端服务

```bash
cd bankshield-api
mvn spring-boot:run
```

### 3. 前端配置

确保已引入权限相关模块：

```typescript
// main.ts
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import './router/permission'  // 引入路由守卫

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
```

### 4. 测试

1. 使用管理员账号登录（拥有所有权限）
2. 查看侧边栏菜单是否正确显示
3. 尝试访问不同的功能模块
4. 使用普通用户账号登录，验证权限限制

---

## 权限标识规范

### 命名规范

```
模块:操作

例如：
user:view    - 查看用户
user:add     - 新增用户
user:edit    - 编辑用户
user:delete  - 删除用户
user:export  - 导出用户
```

### 常用权限标识

| 模块 | 权限标识 | 说明 |
|------|---------|------|
| 用户管理 | user:view | 查看用户 |
| 用户管理 | user:add | 新增用户 |
| 用户管理 | user:edit | 编辑用户 |
| 用户管理 | user:delete | 删除用户 |
| 角色管理 | role:view | 查看角色 |
| 权限管理 | permission:view | 查看权限 |
| 审计日志 | audit:log:view | 查看审计日志 |
| 合规检查 | compliance:rule:view | 查看合规规则 |

---

**文档版本**: v1.0  
**最后更新**: 2025-01-04  
**维护者**: BankShield开发团队
