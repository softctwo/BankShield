import { RouteRecordRaw } from 'vue-router'

const watermarkRouter: RouteRecordRaw = {
  path: '/security',
  component: () => import('@/views/layout/index.vue'),
  name: 'Security',
  meta: {
    title: '安全防护',
    icon: 'Lock',
    roles: ['SECURITY_ADMIN']
  },
  children: [
    {
      path: 'watermark-template',
      name: 'WatermarkTemplate',
      component: () => import('@/views/security/watermark-template/index.vue'),
      meta: {
        title: '水印模板管理',
        icon: 'DocumentCopy',
        roles: ['SECURITY_ADMIN']
      }
    },
    {
      path: 'watermark-task',
      name: 'WatermarkTask',
      component: () => import('@/views/security/watermark-task/index.vue'),
      meta: {
        title: '水印任务管理',
        icon: 'Timer',
        roles: ['SECURITY_ADMIN']
      }
    },
    {
      path: 'watermark-extract',
      name: 'WatermarkExtract',
      component: () => import('@/views/security/watermark-extract/index.vue'),
      meta: {
        title: '水印提取溯源',
        icon: 'Search',
        roles: ['SECURITY_ADMIN']
      }
    }
  ]
}

export default watermarkRouter