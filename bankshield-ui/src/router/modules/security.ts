import { RouteRecordRaw } from 'vue-router'

const securityRoutes: RouteRecordRaw[] = [
  {
    path: '/security',
    component: () => import('@layout/index.vue'),
    name: 'Security',
    meta: {
      title: '安全防护',
      icon: 'Shield',
      roles: ['SECURITY_ADMIN', 'ADMIN']
    },
    children: [
      {
        path: 'scan-task',
        component: () => import('@/views/security/scan-task/index.vue'),
        name: 'ScanTask',
        meta: {
          title: '安全扫描任务',
          icon: 'el-icon-search',
          roles: ['SECURITY_ADMIN']
        }
      },
      {
        path: 'scan-result',
        component: () => import('@/views/security/scan-result/index.vue'),
        name: 'ScanResult',
        meta: {
          title: '扫描结果管理',
          icon: 'el-icon-document-checked',
          roles: ['SECURITY_ADMIN']
        }
      },
      {
        path: 'baseline',
        component: () => import('@/views/security/baseline/index.vue'),
        name: 'Baseline',
        meta: {
          title: '安全基线配置',
          icon: 'el-icon-setting',
          roles: ['SECURITY_ADMIN']
        }
      },
      {
        path: 'rate-limit',
        component: () => import('@/views/security/rate-limit/index.vue'),
        name: 'RateLimit',
        meta: {
          title: '限流规则管理',
          icon: 'el-icon-odometer',
          roles: ['SECURITY_ADMIN']
        }
      },
      {
        path: 'blacklist',
        component: () => import('@/views/security/blacklist/index.vue'),
        name: 'Blacklist',
        meta: {
          title: 'IP黑名单管理',
          icon: 'el-icon-circle-close',
          roles: ['SECURITY_ADMIN']
        }
      },
      {
        path: 'api-audit',
        component: () => import('@/views/security/api-audit/index.vue'),
        name: 'ApiAudit',
        meta: {
          title: 'API审计日志',
          icon: 'el-icon-document',
          roles: ['SECURITY_ADMIN', 'ADMIN']
        }
      }
    ]
  }
]

export default securityRoutes