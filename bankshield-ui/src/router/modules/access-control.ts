import type { RouteRecordRaw } from 'vue-router'

const accessControlRouter: RouteRecordRaw = {
  path: '/access-control',
  name: 'AccessControl',
  redirect: '/access-control/policy',
  meta: {
    title: '访问控制',
    icon: 'Lock'
  },
  children: [
    {
      path: 'policy',
      name: 'AccessPolicy',
      component: () => import('@/views/access-control/policy/index.vue'),
      meta: {
        title: '访问策略',
        icon: 'Document',
        permission: 'access:policy:query'
      }
    },
    {
      path: 'mfa',
      name: 'MfaConfig',
      component: () => import('@/views/access-control/mfa/index.vue'),
      meta: {
        title: 'MFA配置',
        icon: 'Key',
        permission: 'access:mfa:config'
      }
    },
    {
      path: 'ip',
      name: 'IpControl',
      component: () => import('@/views/access-control/ip/index.vue'),
      meta: {
        title: 'IP访问控制',
        icon: 'Monitor',
        permission: 'access:ip:query'
      }
    }
  ]
}

export default accessControlRouter
