# BankShield 菜单显示问题修复指南

## 问题描述

前端无法看到新增的功能菜单，包括：
- 数据脱敏: 脱敏规则、脱敏日志
- 合规性检查: 合规规则、检查任务、合规报告
- 安全态势: 安全大屏、威胁管理
- 数据血缘: 血缘追踪、影响分析
- 安全扫描: 扫描任务、漏洞管理

## 根本原因

后端菜单实体类字段与数据库表结构不匹配：

| 数据库字段 | 原实体类字段 | 正确字段 |
|-----------|-------------|---------|
| menu_code | - | menuCode |
| sort_order | orderNum | sortOrder |
| menu_type (tinyint) | menuType (String) | menuType (Integer) |
| permission | perms | permission |
| status (tinyint 0/1) | status (String '0'/'1') | status (Integer 0/1) |

## 已修复内容

### 1. 菜单实体类 (SysMenu.java)

**修复前**:
```java
private String menuType;  // 错误：应该是Integer
private Integer orderNum;  // 错误：字段名不匹配
private String perms;      // 错误：字段名不匹配
private String status;     // 错误：应该是Integer
```

**修复后**:
```java
private Integer menuType;   // 0=目录, 1=菜单, 2=按钮
private Integer sortOrder;  // 显示顺序
private String permission;  // 权限标识
private Integer status;     // 0=禁用, 1=启用
private String menuCode;    // 菜单编码
```

### 2. 菜单Mapper (SysMenuMapper.java)

**修复前**:
```java
WHERE m.status = '0' AND m.visible = '0'  // 错误：字符串比较
ORDER BY m.parent_id, m.order_num         // 错误：字段名不匹配
SELECT DISTINCT m.perms                   // 错误：字段名不匹配
```

**修复后**:
```java
WHERE m.status = 1                        // 正确：整数比较
ORDER BY m.parent_id, m.sort_order        // 正确：字段名匹配
SELECT DISTINCT m.permission              // 正确：字段名匹配
```

### 3. 菜单服务实现 (SysMenuServiceImpl.java)

**修复前**:
```java
.sorted(Comparator.comparing(SysMenu::getOrderNum))  // 错误：方法不存在
node.put("perms", menu.getPerms());                  // 错误：方法不存在
```

**修复后**:
```java
.sorted(Comparator.comparing(SysMenu::getSortOrder))  // 正确
node.put("permission", menu.getPermission());         // 正确
```

## 验证步骤

### 1. 验证数据库数据

```sql
-- 查看所有菜单
SELECT id, menu_name, path, parent_id, sort_order, menu_type, status 
FROM sys_menu 
WHERE status = 1 
ORDER BY parent_id, sort_order;

-- 查看用户权限
SELECT COUNT(*) FROM sys_role_menu WHERE role_id = 1;
-- 应该返回: 37

-- 查看具体菜单
SELECT m.id, m.menu_name, m.path, m.parent_id
FROM sys_menu m
INNER JOIN sys_role_menu rm ON m.id = rm.menu_id
WHERE rm.role_id = 1 AND m.status = 1
ORDER BY m.parent_id, m.sort_order;
```

### 2. 测试后端API

```bash
# 获取用户菜单树（userId=1）
curl http://localhost:8080/api/system/menu/user/1

# 获取路由信息
curl http://localhost:8080/api/system/menu/getRouters

# 获取用户权限
curl http://localhost:8080/api/system/menu/perms/1
```

### 3. 重启后端服务

```bash
cd /Users/zhangyanlong/workspaces/BankShield/bankshield-api

# 清理编译
mvn clean

# 重新编译
mvn compile

# 启动服务
mvn spring-boot:run
```

### 4. 前端验证

1. 清除浏览器缓存
2. 重新登录系统
3. 查看侧边栏菜单是否显示所有功能

## 预期结果

登录后应该看到以下菜单结构：

```
首页
数据加密
  ├── 加密管理
  └── 密钥管理
访问控制
  ├── 用户管理
  ├── 角色管理
  └── 权限管理
审计追踪
  ├── 审计日志
  └── 审计分析
数据脱敏
  ├── 脱敏规则
  └── 脱敏日志
合规性检查
  ├── 合规规则
  ├── 检查任务
  └── 合规报告
安全态势
  ├── 安全大屏
  └── 威胁管理
数据血缘
  ├── 血缘追踪
  └── 影响分析
安全扫描
  ├── 扫描任务
  └── 漏洞管理
系统管理
  ├── 菜单管理
  ├── 字典管理
  ├── 系统配置
  └── 日志管理
```

## 如果仍然看不到菜单

### 检查清单

1. **后端服务是否重启**
   ```bash
   # 查看进程
   ps aux | grep bankshield
   
   # 如果有旧进程，先停止
   kill -9 <PID>
   ```

2. **数据库连接是否正常**
   ```bash
   mysql -u root -p3f342bb206 -e "SELECT COUNT(*) FROM sys_menu" bankshield
   ```

3. **用户角色是否正确分配**
   ```sql
   SELECT * FROM sys_user_role WHERE user_id = 1;
   -- 应该返回: user_id=1, role_id=1
   ```

4. **菜单权限是否正确分配**
   ```sql
   SELECT COUNT(*) FROM sys_role_menu WHERE role_id = 1;
   -- 应该返回: 37
   ```

5. **前端是否正确调用API**
   - 打开浏览器开发者工具
   - 查看Network标签
   - 查找 `/api/system/menu/getRouters` 请求
   - 检查响应数据是否包含所有菜单

## 常见问题

### Q1: 编译错误

**错误**: `The method getOrderNum() is undefined for the type SysMenu`

**解决**: 已修复，使用 `getSortOrder()` 替代

### Q2: 菜单查询返回空

**原因**: 用户角色未分配或菜单状态为禁用

**解决**:
```sql
-- 重新分配权限
DELETE FROM sys_role_menu WHERE role_id = 1;
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE status = 1;
```

### Q3: 前端路由404

**原因**: 前端路由配置缺失对应的组件

**解决**: 确保前端 `views` 目录下有对应的组件文件

## 修复文件清单

- ✅ `SysMenu.java` - 菜单实体类
- ✅ `SysMenuMapper.java` - 菜单Mapper
- ✅ `SysMenuServiceImpl.java` - 菜单服务实现

## 下一步

1. 重启后端服务
2. 清除浏览器缓存
3. 重新登录测试
4. 如果还有问题，查看后端日志：`tail -f logs/bankshield.log`

---

**文档版本**: v1.0  
**修复时间**: 2025-01-04  
**状态**: ✅ 已修复
