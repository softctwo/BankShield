import { RouteRecordRaw } from 'vue-router'

/**
 * 监控管理路由
 */
const monitorRouter: RouteRecordRaw = {
  path: '/monitor',
  name: 'Monitor',
  component: () => import('@/views/layout/index.vue'),
  redirect: '/monitor/dashboard',
  meta: {
    title: '监控管理',
    icon: 'Monitor',
    roles: ['admin', 'audit-admin', 'system-admin']
  },
  children: [
    {
      path: '/monitor/dashboard',
      name: 'MonitorDashboard',
      component: () => import('@/views/monitor/dashboard/index.vue'),
      meta: {
        title: '监控Dashboard',
        icon: 'DataBoard',
        affix: true
      }
    },
    {
      path: '/monitor/alert-rule',
      name: 'MonitorAlertRule',
      component: () => import('@/views/monitor/alert-rule/index.vue'),
      meta: {
        title: '告警规则管理',
        icon: 'Warning',
        roles: ['admin', 'system-admin']
      }
    },
    {
      path: '/monitor/alert-record',
      name: 'MonitorAlertRecord',
      component: () => import('@/views/monitor/alert-record/index.vue'),
      meta: {
        title: '告警记录管理',
        icon: 'Document',
        roles: ['admin', 'audit-admin']
      }
    },
    {
      path: '/monitor/notification-config',
      name: 'MonitorNotificationConfig',
      component: () => import('@/views/monitor/notification-config/index.vue'),
      meta: {
        title: '通知配置管理',
        icon: 'Message',
        roles: ['admin', 'system-admin']
      }
    },
    {
      path: '/monitor/metrics',
      name: 'MonitorMetrics',
      component: () => import('@/views/monitor/metrics/index.vue'),
      meta: {
        title: '系统监控大屏',
        icon: 'Monitor',
        roles: ['admin', 'system-admin']
      }
    }
  ]
}

export default monitorRouter