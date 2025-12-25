# BankShield 登录功能测试指南

## 功能特性

✅ 完整的登录表单（用户名、密码、记住我）
✅ 表单验证（必填字段、长度限制）
✅ 调用后端API进行登录
✅ Token存储（localStorage）
✅ 错误处理（显示错误信息）
✅ 登录成功后跳转到/dashboard
✅ 路由守卫保护需要登录的页面
✅ 用户信息状态管理（Pinia）
✅ 用户退出登录功能

## 项目结构

```
bankshield-ui/
├── src/
│   ├── api/
│   │   ├── auth.ts          # 认证相关API
│   │   └── user.ts          # 用户相关API
│   ├── stores/
│   │   ├── index.ts         # Pinia入口
│   │   └── user.ts          # 用户状态管理
│   ├── types/
│   │   └── user.d.ts        # TypeScript类型定义
│   ├── utils/
│   │   └── request.ts       # Axios请求封装
│   ├── views/
│   │   ├── login/
│   │   │   └── index.vue    # 登录页面
│   │   ├── layout/
│   │   │   └── index.vue    # 布局组件
│   │   ├── dashboard/
│   │   │   └── index.vue    # 仪表板页面
│   │   └── system/
│   │       └── user/
│   │           └── index.vue # 用户管理页面
│   └── router/
│       └── index.ts         # 路由配置
├── .env.development         # 开发环境配置
└── .env.production          # 生产环境配置
```

## API接口说明

### 登录接口
- **地址**: `/auth/login`
- **方法**: POST
- **请求体**: `{ username: string, password: string }`
- **响应格式**: `{ code: number, data: { token: string, user: object }, message: string }`

### 获取用户信息接口
- **地址**: `/auth/userinfo`
- **方法**: GET
- **响应格式**: `{ code: number, data: object, message: string }`

## 状态管理

### User Store (Pinia)
- `token`: 用户认证token
- `userInfo`: 用户信息对象
- `isLogin`: 是否已登录（计算属性）
- `setToken(token)`: 设置token
- `setUserInfo(info)`: 设置用户信息
- `logout()`: 退出登录

## 路由守卫

- 白名单页面：`/login`（无需登录）
- 已登录用户访问登录页会自动跳转到`/dashboard`
- 未登录用户访问需要登录的页面会跳转到`/login`

## 本地存储

- `token`: 存储用户认证token
- `rememberMe`: 记住我选项
- `username`: 记住的用户名

## 表单验证规则

- **用户名**: 必填，长度3-20字符
- **密码**: 必填，长度6-20字符

## 错误处理

- 网络错误：显示"网络连接失败"
- 401错误：显示"登录已过期，请重新登录"并跳转到登录页
- 403错误：显示"没有权限访问"
- 404错误：显示"请求的资源不存在"
- 500错误：显示"服务器内部错误"
- 其他错误：显示后端返回的错误信息

## 使用说明

1. **默认账号**: admin / 123456
2. **记住我功能**: 勾选后会记住用户名
3. **快捷键支持**: 在输入框按Enter键可快速登录
4. **退出登录**: 点击右上角用户名下拉菜单中的"退出登录"

## 开发环境配置

在`.env.development`文件中配置API基础地址：
```
VITE_API_BASE_URL=http://localhost:8080
```

## 生产环境配置

在`.env.production`文件中配置API基础地址：
```
VITE_API_BASE_URL=/api
```

## 测试建议

1. 测试正常登录流程
2. 测试错误的用户名/密码
3. 测试记住我功能
4. 测试路由守卫（直接访问需要登录的页面）
5. 测试退出登录功能
6. 测试token过期处理
7. 测试网络错误处理