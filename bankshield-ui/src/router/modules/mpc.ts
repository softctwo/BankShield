import { RouteRecordRaw } from 'vue-router'

const Layout = () => import('@/views/layout/index.vue')

const mpcRouter: RouteRecordRaw = {
  path: '/mpc',
  component: Layout,
  redirect: '/mpc/dashboard',
  meta: {
    title: '多方安全计算',
    icon: 'Connection'
  },
  children: [
    {
      path: 'dashboard',
      name: 'MpcDashboard',
      component: () => import('@/views/mpc/Dashboard.vue'),
      meta: {
        title: 'MPC概览',
        icon: 'DataBoard'
      }
    },
    {
      path: 'jobs',
      name: 'MpcJobs',
      component: () => import('@/views/mpc/JobList.vue'),
      meta: {
        title: '任务列表',
        icon: 'List'
      }
    }
  ]
}

export default mpcRouter
