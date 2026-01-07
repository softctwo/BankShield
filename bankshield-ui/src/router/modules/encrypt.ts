import { RouteRecordRaw } from 'vue-router'

/**
 * 密钥管理路由
 */
const encryptRouter: RouteRecordRaw = {
  path: '/encrypt',
  component: () => import('@/views/layout/index.vue'),
  name: 'Encrypt',
  meta: {
    title: '加密管理',
    icon: 'Lock',
    roles: ['SECURITY_ADMIN']
  },
  children: [
    {
      path: 'key',
      component: () => import('@/views/encrypt/key/index.vue'),
      name: 'EncryptKey',
      meta: {
        title: '密钥管理',
        icon: 'Key',
        roles: ['SECURITY_ADMIN']
      }
    },
    {
      path: 'key/usage',
      component: () => import('@/views/encrypt/key/usage.vue'),
      name: 'EncryptKeyUsage',
      meta: {
        title: '密钥使用统计',
        icon: 'DataAnalysis',
        roles: ['SECURITY_ADMIN']
      }
    },
    {
      path: 'strategy',
      component: () => import('@/views/encrypt/strategy/index.vue'),
      name: 'EncryptStrategy',
      meta: {
        title: '加密策略',
        icon: 'Setting',
        roles: ['SECURITY_ADMIN']
      }
    },
    {
      path: 'task',
      component: () => import('@/views/encrypt/task/index.vue'),
      name: 'EncryptTask',
      meta: {
        title: '加密任务',
        icon: 'List',
        roles: ['SECURITY_ADMIN']
      }
    }
  ]
}

export default encryptRouter