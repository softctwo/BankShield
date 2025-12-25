export default {
  path: '/system/role-mutex',
  component: () => import('@/layout'),
  name: 'RoleMutex',
  meta: {
    title: '三权分立管理',
    icon: 'el-icon-lock',
    roles: ['SYSTEM_ADMIN', 'AUDIT_ADMIN'] // 只有系统管理员和审计管理员可以访问
  },
  children: [
    {
      path: 'index',
      component: () => import('@/views/system/role-mutex/index'),
      name: 'RoleMutexIndex',
      meta: {
        title: '三权分立管理',
        icon: 'el-icon-lock',
        roles: ['SYSTEM_ADMIN', 'AUDIT_ADMIN']
      }
    }
  ]
}