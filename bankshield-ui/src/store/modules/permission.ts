import { defineStore } from 'pinia'
import { getRouters } from '@/api/menu'
import type { RouteRecordRaw } from 'vue-router'

export interface PermissionState {
  routes: RouteRecordRaw[]
  addRoutes: RouteRecordRaw[]
  permissions: string[]
}

export const usePermissionStore = defineStore('permission', {
  state: (): PermissionState => ({
    routes: [],
    addRoutes: [],
    permissions: []
  }),

  actions: {
    /**
     * 生成路由
     */
    async generateRoutes() {
      try {
        const res = await getRouters()
        const { menus, permissions } = res.data
        
        // 保存权限标识
        this.permissions = permissions || []
        
        // 转换菜单为路由
        const accessedRoutes = this.filterAsyncRoutes(menus)
        this.addRoutes = accessedRoutes
        this.routes = accessedRoutes
        
        return accessedRoutes
      } catch (error) {
        console.error('生成路由失败:', error)
        return []
      }
    },

    /**
     * 过滤异步路由
     */
    filterAsyncRoutes(routes: any[]): RouteRecordRaw[] {
      const res: RouteRecordRaw[] = []
      
      routes.forEach(route => {
        const tmp = { ...route }
        
        // 转换路由格式
        const routeRecord: RouteRecordRaw = {
          path: tmp.path,
          name: tmp.name || tmp.path.replace(/\//g, '-'),
          component: this.loadComponent(tmp.component),
          meta: {
            title: tmp.meta?.title || tmp.name,
            icon: tmp.meta?.icon || tmp.icon,
            noCache: tmp.meta?.noCache || false,
            perms: tmp.perms
          }
        }
        
        // 递归处理子路由
        if (tmp.children && tmp.children.length > 0) {
          routeRecord.children = this.filterAsyncRoutes(tmp.children)
        }
        
        res.push(routeRecord)
      })
      
      return res
    },

    /**
     * 动态加载组件
     */
    loadComponent(component: string) {
      if (!component) {
        return () => import('@/views/layout/index.vue')
      }
      
      // 处理布局组件
      if (component === 'Layout') {
        return () => import('@/views/layout/index.vue')
      }
      
      // 动态导入组件
      return () => import(`@/views/${component}.vue`)
    },

    /**
     * 检查权限
     */
    hasPermission(permission: string): boolean {
      return this.permissions.includes(permission)
    },

    /**
     * 检查多个权限（满足任意一个即可）
     */
    hasAnyPermission(permissions: string[]): boolean {
      return permissions.some(permission => this.hasPermission(permission))
    },

    /**
     * 检查多个权限（需要全部满足）
     */
    hasAllPermissions(permissions: string[]): boolean {
      return permissions.every(permission => this.hasPermission(permission))
    }
  }
})
