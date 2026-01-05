import type { RouteRecordRaw } from 'vue-router'

/**
 * 合规管理路由模块
 */
const complianceRouter: RouteRecordRaw = {
  path: '/compliance',
  name: 'Compliance',
  component: () => import('@/views/layout/index.vue'),
  redirect: '/compliance/dashboard',
  meta: {
    title: '合规管理',
    icon: 'Shield',
    roles: ['AUDIT_ADMIN']
  },
  children: [
    {
      path: '/compliance/dashboard',
      name: 'ComplianceDashboard',
      component: () => import('@/views/compliance/dashboard/index.vue'),
      meta: {
        title: '合规Dashboard',
        icon: 'DataAnalysis',
        roles: ['AUDIT_ADMIN']
      }
    },
    {
      path: '/compliance/check',
      name: 'ComplianceCheck',
      component: () => import('@/views/compliance/check/index.vue'),
      meta: {
        title: '合规检查',
        icon: 'Check',
        roles: ['AUDIT_ADMIN']
      }
    },
    {
      path: '/compliance/report-template',
      name: 'ReportTemplate',
      component: () => import('@/views/compliance/report-template/index.vue'),
      meta: {
        title: '报表模板管理',
        icon: 'Document',
        roles: ['AUDIT_ADMIN']
      }
    },
    {
      path: '/compliance/report-task',
      name: 'ReportTask',
      component: () => import('@/views/compliance/report-task/index.vue'),
      meta: {
        title: '报表生成任务',
        icon: 'Edit',
        roles: ['AUDIT_ADMIN']
      }
    },
    {
      path: '/compliance/check-history',
      name: 'CheckHistory',
      component: () => import('@/views/compliance/check-history/index.vue'),
      meta: {
        title: '检查历史',
        icon: 'Clock',
        roles: ['AUDIT_ADMIN']
      }
    },
    {
      path: '/compliance/rule',
      name: 'ComplianceRule',
      component: () => import('@/views/compliance/rule/index.vue'),
      meta: {
        title: '合规规则',
        icon: 'Document',
        roles: ['AUDIT_ADMIN']
      }
    },
    {
      path: '/compliance/task',
      name: 'ComplianceTask',
      component: () => import('@/views/compliance/task/index.vue'),
      meta: {
        title: '检查任务',
        icon: 'List',
        roles: ['AUDIT_ADMIN']
      }
    },
    {
      path: '/compliance/report',
      name: 'ComplianceReport',
      component: () => import('@/views/compliance/report/index.vue'),
      meta: {
        title: '合规报告',
        icon: 'Tickets',
        roles: ['AUDIT_ADMIN']
      }
    }
  ]
}

export default complianceRouter