import router from './index'
import { usePermissionStore } from '@/store/modules/permission'
import { useUserStore } from '@/store/modules/user'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/404', '/401']

router.beforeEach(async (to, from, next) => {
  NProgress.start()
  
  const userStore = useUserStore()
  const permissionStore = usePermissionStore()
  
  // 判断是否已登录
  const hasToken = userStore.token
  
  if (hasToken) {
    if (to.path === '/login') {
      // 已登录，跳转到首页
      next({ path: '/' })
      NProgress.done()
    } else {
      // 判断是否已获取用户信息
      const hasRoles = userStore.roles && userStore.roles.length > 0
      
      if (hasRoles) {
        next()
      } else {
        try {
          // 获取用户信息
          await userStore.getUserInfo()
          
          // 生成可访问的路由
          const accessRoutes = await permissionStore.generateRoutes()
          
          // 动态添加路由
          accessRoutes.forEach(route => {
            router.addRoute(route)
          })
          
          // 确保addRoutes已完成
          next({ ...to, replace: true })
        } catch (error) {
          // 移除token并跳转到登录页
          await userStore.logout()
          next(`/login?redirect=${to.path}`)
          NProgress.done()
        }
      }
    }
  } else {
    // 未登录
    if (whiteList.indexOf(to.path) !== -1) {
      // 在免登录白名单，直接进入
      next()
    } else {
      // 其他没有访问权限的页面将被重定向到登录页面
      next(`/login?redirect=${to.path}`)
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
