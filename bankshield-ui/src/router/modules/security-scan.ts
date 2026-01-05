import type { RouteRecordRaw } from 'vue-router'
import Layout from '@/views/layout/index.vue'

const securityScanRouter: RouteRecordRaw = {
  path: '/security-scan',
  component: Layout,
  redirect: '/security-scan/dashboard',
  name: 'SecurityScan',
  meta: {
    title: '安全扫描',
    icon: 'shield'
  },
  children: [
    {
      path: 'dashboard',
      name: 'ScanDashboard',
      component: () => import('@/views/security-scan/dashboard/index.vue'),
      meta: {
        title: '扫描仪表板',
        icon: 'dashboard',
        permission: 'scan:dashboard:view'
      }
    },
    {
      path: 'task',
      name: 'ScanTask',
      component: () => import('@/views/security-scan/task/index.vue'),
      meta: {
        title: '扫描任务',
        icon: 'list',
        permission: 'scan:task:query'
      }
    },
    {
      path: 'vulnerability',
      name: 'Vulnerability',
      component: () => import('@/views/security-scan/vulnerability/index.vue'),
      meta: {
        title: '漏洞管理',
        icon: 'bug',
        permission: 'scan:vuln:query'
      }
    }
  ]
}

export default securityScanRouter
