import type { RouteRecordRaw } from 'vue-router'

/**
 * 审计管理路由模块
 */
const auditRouter: RouteRecordRaw = {
  path: '/audit',
  name: 'Audit',
  component: () => import('@/views/layout/index.vue'),
  redirect: '/audit/dashboard',
  meta: {
    title: '审计管理',
    icon: 'Document',
    roles: ['admin', 'audit-admin']
  },
  children: [
    {
      path: '/audit/dashboard',
      name: 'AuditDashboard',
      component: () => import('@/views/audit/dashboard/index.vue'),
      meta: {
        title: '审计Dashboard',
        icon: 'DataAnalysis',
        roles: ['admin', 'audit-admin']
      }
    },
    {
      path: '/audit/operation',
      name: 'OperationAudit',
      component: () => import('@/views/audit/operation/index.vue'),
      meta: {
        title: '操作审计',
        icon: 'Document',
        roles: ['admin', 'audit-admin']
      }
    },
    {
      path: '/audit/login',
      name: 'LoginAudit',
      component: () => import('@/views/audit/login/index.vue'),
      meta: {
        title: '登录审计',
        icon: 'Key',
        roles: ['admin', 'audit-admin']
      }
    },
    {
      path: '/audit/security',
      name: 'SecurityAudit',
      component: () => import('@/views/audit/security/index.vue'),
      meta: {
        title: '安全审计',
        icon: 'Warning',
        roles: ['admin', 'audit-admin']
      }
    },
    {
      path: '/audit/analysis',
      name: 'AuditAnalysis',
      component: () => import('@/views/audit/analysis/index.vue'),
      meta: {
        title: '审计分析',
        icon: 'TrendCharts',
        roles: ['admin', 'audit-admin']
      }
    }
  ]
}

export default auditRouter