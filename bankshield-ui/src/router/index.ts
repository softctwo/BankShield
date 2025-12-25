import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

// 路由懒加载优化
const Login = () => import('@/views/login/index.vue')
const Layout = () => import('@/views/layout/index.vue')
const Dashboard = () => import('@/views/dashboard/index.vue')
const UserManagement = () => import('@/views/system/user/index.vue')
const RoleManagement = () => import('@/views/system/role/index.vue')
const DeptManagement = () => import('@/views/system/dept/index.vue')
const MenuManagement = () => import('@/views/system/menu/index.vue')
const SeparationRoleManagement = () => import('@/views/separation/role-management/index.vue')
const SeparationPermissionAudit = () => import('@/views/separation/permission-audit/index.vue')
const SeparationOperationLog = () => import('@/views/separation/operation-log/index.vue')

// 导入路由模块（这些模块内部也使用懒加载）
import classificationRouter from './modules/classification'
import auditRouter from './modules/audit'
import monitorRouter from './modules/monitor'
import encryptRouter from './modules/encrypt'
import watermarkRouter from './modules/watermark'
import aiRouter from './modules/ai'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: {
      title: '登录',
      noCache: true
    }
  },
  {
    path: '/layout',
    name: 'Layout',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: {
          title: '数据大屏',
          icon: 'DataBoard',
          affix: true,
          noCache: false
        }
      },
      {
        path: '/user',
        name: 'User',
        component: UserManagement,
        meta: {
          title: '用户管理',
          icon: 'User',
          roles: ['admin'],
          noCache: false
        }
      },
      // 系统管理模块
      {
        path: '/system/role',
        name: 'SystemRole',
        component: RoleManagement,
        meta: {
          title: '角色管理',
          icon: 'Avatar',
          roles: ['admin'],
          noCache: false
        }
      },
      {
        path: '/system/dept',
        name: 'SystemDept',
        component: DeptManagement,
        meta: {
          title: '部门管理',
          icon: 'OfficeBuilding',
          roles: ['admin'],
          noCache: false
        }
      },
      {
        path: '/system/menu',
        name: 'SystemMenu',
        component: MenuManagement,
        meta: {
          title: '菜单管理',
          icon: 'Menu',
          roles: ['admin'],
          noCache: false
        }
      },
      // 三权分立管理模块
      {
        path: '/separation/role-management',
        name: 'SeparationRoleManagement',
        component: SeparationRoleManagement,
        meta: {
          title: '三权角色管理',
          icon: 'UserFilled',
          roles: ['admin'],
          noCache: false
        }
      },
      {
        path: '/separation/permission-audit',
        name: 'SeparationPermissionAudit',
        component: SeparationPermissionAudit,
        meta: {
          title: '权限审计',
          icon: 'View',
          roles: ['admin', 'audit-admin'],
          noCache: false
        }
      },
      {
        path: '/separation/operation-log',
        name: 'SeparationOperationLog',
        component: SeparationOperationLog,
        meta: {
          title: '操作日志',
          icon: 'Document',
          roles: ['admin', 'audit-admin'],
          noCache: false
        }
      }
    ]
  },
  // 分类分级路由
  classificationRouter,
  // 审计管理路由
  auditRouter,
  // 监控管理路由
  monitorRouter,
  // 密钥管理路由
  encryptRouter,
  // 水印管理路由
  watermarkRouter,
  // AI智能分析路由
  aiRouter
]

const router = createRouter({
  history: createWebHistory('/'),
  routes,
  // 路由滚动行为
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 不需要登录的页面白名单
  const whiteList = ['/login']
  
  // 判断是否需要登录
  const isLogin = userStore.isLogin
  
  if (whiteList.includes(to.path)) {
    // 如果已登录且访问登录页，跳转到dashboard
    if (to.path === '/login' && isLogin) {
      next('/dashboard')
    } else {
      next()
    }
  } else {
    // 需要登录的页面
    if (isLogin) {
      next()
    } else {
      ElMessage.warning('请先登录')
      next('/login')
    }
  }
})

// 路由后置钩子（用于性能监控和预加载）
router.afterEach((to, from) => {
  // 记录页面访问性能
  if (window.performance && window.performance.mark) {
    window.performance.mark('route-changed')
  }
  
  // 预加载下一个可能访问的模块
  preloadNextRoute(to)
})

/**
 * 预加载下一个可能访问的路由
 */
function preloadNextRoute(currentRoute: any) {
  // 根据当前路由预加载相关模块
  const routeMapping: Record<string, string[]> = {
    '/dashboard': ['/user', '/system/role', '/system/dept'],
    '/user': ['/system/role', '/system/dept'],
    '/system/role': ['/system/menu', '/system/dept'],
    '/system/dept': ['/system/menu', '/user'],
    '/classification/asset': ['/classification/sensitive', '/masking/rule']
  }
  
  const routesToPreload = routeMapping[currentRoute.path] || []
  
  routesToPreload.forEach(routePath => {
    // 使用动态导入进行预加载
    const route = router.getRoutes().find(r => r.path === routePath)
    if (route && route.components) {
      // 预加载组件
      if (typeof route.components.default === 'function') {
        (route.components.default as Function)()
      }
    }
  })
}

export default router