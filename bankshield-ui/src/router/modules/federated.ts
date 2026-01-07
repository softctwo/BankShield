import type { RouteRecordRaw } from 'vue-router'
import Layout from '@/views/layout/index.vue'

const federatedRouter: RouteRecordRaw = {
  path: '/federated',
  component: Layout,
  redirect: '/federated/dashboard',
  name: 'Federated',
  meta: {
    title: '联邦学习',
    icon: 'Connection'
  },
  children: [
    {
      path: 'dashboard',
      component: () => import('@/views/federated/FederatedDashboard.vue'),
      name: 'FederatedDashboard',
      meta: { title: '联邦学习概览', icon: 'DataAnalysis' }
    },
    {
      path: 'jobs',
      component: () => import('@/views/federated/FederatedDashboard.vue'),
      name: 'FederatedJobs',
      meta: { title: '任务管理', icon: 'List' }
    },
    {
      path: 'parties',
      component: () => import('@/views/federated/FederatedDashboard.vue'),
      name: 'FederatedParties',
      meta: { title: '参与方管理', icon: 'User' }
    }
  ]
}

export default federatedRouter
