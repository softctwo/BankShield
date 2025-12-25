import { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

/**
 * 获取有权限的路由
 */
export function getPermissionRoutes(): RouteRecordRaw[] {
  const userStore = useUserStore()
  const allRoutes = getAllRoutes()
  
  return filterRoutesByPermission(allRoutes, userStore)
}

/**
 * 获取所有路由配置
 */
function getAllRoutes(): RouteRecordRaw[] {
  // 这里应该从路由配置中获取所有路由
  // 为了演示，我们返回一些示例路由
  return [
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('@/views/dashboard/index.vue'),
      meta: {
        title: '数据大屏',
        icon: 'DataBoard',
        affix: true
      }
    },
    {
      path: '/system',
      name: 'System',
      redirect: '/system/user',
      meta: {
        title: '系统管理',
        icon: 'Setting',
        roles: ['admin']
      },
      children: [
        {
          path: '/system/user',
          name: 'User',
          component: () => import('@/views/system/user/index.vue'),
          meta: {
            title: '用户管理',
            icon: 'User',
            roles: ['admin']
          }
        },
        {
          path: '/system/role',
          name: 'Role',
          component: () => import('@/views/system/role/index.vue'),
          meta: {
            title: '角色管理',
            icon: 'Avatar',
            roles: ['admin']
          }
        },
        {
          path: '/system/dept',
          name: 'Dept',
          component: () => import('@/views/system/dept/index.vue'),
          meta: {
            title: '部门管理',
            icon: 'OfficeBuilding',
            roles: ['admin']
          }
        },
        {
          path: '/system/menu',
          name: 'Menu',
          component: () => import('@/views/system/menu/index.vue'),
          meta: {
            title: '菜单管理',
            icon: 'Menu',
            roles: ['admin']
          }
        }
      ]
    },
    {
      path: '/classification',
      name: 'Classification',
      redirect: '/classification/asset-management',
      meta: {
        title: '数据分类分级',
        icon: 'DataAnalysis',
        roles: ['admin', 'data-manager']
      },
      children: [
        {
          path: '/classification/asset-management',
          name: 'AssetManagement',
          component: () => import('@/views/classification/asset-management/index.vue'),
          meta: {
            title: '资产管理',
            icon: 'Coin',
            roles: ['admin', 'data-manager']
          }
        },
        {
          path: '/classification/asset-map',
          name: 'AssetMap',
          component: () => import('@/views/classification/asset-map/index.vue'),
          meta: {
            title: '资产地图',
            icon: 'MapLocation',
            roles: ['admin', 'data-manager']
          }
        },
        {
          path: '/classification/review',
          name: 'Review',
          component: () => import('@/views/classification/review/index.vue'),
          meta: {
            title: '资产审核',
            icon: 'Check',
            roles: ['admin', 'data-auditor']
          }
        },
        {
          path: '/classification/data-source',
          name: 'DataSource',
          component: () => import('@/views/classification/data-source/index.vue'),
          meta: {
            title: '数据源管理',
            icon: 'Connection',
            roles: ['admin', 'data-manager']
          }
        },
        {
          path: '/classification/lineage',
          name: 'Lineage',
          component: () => import('@/views/classification/lineage/index.vue'),
          meta: {
            title: '血缘分析',
            icon: 'Share',
            roles: ['admin', 'data-manager']
          }
        },
        {
          path: '/classification/template',
          name: 'Template',
          component: () => import('@/views/classification/template/index.vue'),
          meta: {
            title: '敏感数据模板',
            icon: 'Document',
            roles: ['admin', 'data-manager']
          }
        }
      ]
    },
    {
      path: '/separation',
      name: 'Separation',
      redirect: '/separation/role-management',
      meta: {
        title: '三权分立管理',
        icon: 'Key',
        roles: ['admin']
      },
      children: [
        {
          path: '/separation/role-management',
          name: 'SeparationRoleManagement',
          component: () => import('@/views/separation/role-management/index.vue'),
          meta: {
            title: '三权角色管理',
            icon: 'UserFilled',
            roles: ['admin']
          }
        },
        {
          path: '/separation/permission-audit',
          name: 'PermissionAudit',
          component: () => import('@/views/separation/permission-audit/index.vue'),
          meta: {
            title: '权限审计',
            icon: 'View',
            roles: ['admin']
          }
        },
        {
          path: '/separation/operation-log',
          name: 'OperationLog',
          component: () => import('@/views/separation/operation-log/index.vue'),
          meta: {
            title: '操作日志',
            icon: 'Document',
            roles: ['admin']
          }
        }
      ]
    }
  ]
}

/**
 * 根据权限过滤路由
 */
function filterRoutesByPermission(routes: RouteRecordRaw[], userStore: any): RouteRecordRaw[] {
  const filteredRoutes: RouteRecordRaw[] = []

  routes.forEach(route => {
    // 检查路由是否需要权限
    if (route.meta?.roles) {
      // 检查用户是否有权限访问该路由
      const roles = route.meta.roles as string[]
      const hasRole = roles.some((role: string) => userStore.hasRole(role))
      if (!hasRole) {
        return // 跳过没有权限的路由
      }
    }
    
    // 深拷贝路由对象
    const newRoute = { ...route }
    
    // 如果路由有子路由，递归过滤
    if (route.children) {
      newRoute.children = filterRoutesByPermission(route.children, userStore)
      // 如果过滤后没有子路由，且当前路由不是叶子节点，则跳过该路由
      if (newRoute.children.length === 0 && route.redirect) {
        return
      }
    }
    
    filteredRoutes.push(newRoute)
  })
  
  return filteredRoutes
}

/**
 * 检查是否有权限访问某个路由
 */
export function hasRoutePermission(route: RouteRecordRaw): boolean {
  const userStore = useUserStore()

  if (!route.meta?.roles) {
    return true // 不需要权限的路由默认可以访问
  }

  const roles = route.meta.roles as string[]
  return roles.some((role: string) => userStore.hasRole(role))
}