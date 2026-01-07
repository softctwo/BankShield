import { RouteRecordRaw } from 'vue-router'

const Layout = () => import('@/views/layout/index.vue')

const blockchainRouter: RouteRecordRaw = {
  path: '/blockchain',
  component: Layout,
  redirect: '/blockchain/dashboard',
  meta: {
    title: '区块链存证',
    icon: 'Link'
  },
  children: [
    {
      path: 'dashboard',
      name: 'BlockchainDashboard',
      component: () => import('@/views/blockchain/Dashboard.vue'),
      meta: {
        title: '存证概览',
        icon: 'DataBoard'
      }
    },
    {
      path: 'records',
      name: 'BlockchainRecords',
      component: () => import('@/views/blockchain/RecordList.vue'),
      meta: {
        title: '存证记录',
        icon: 'Document'
      }
    },
    {
      path: 'evidence',
      name: 'BlockchainEvidence',
      component: () => import('@/views/blockchain/evidence/index.vue'),
      meta: {
        title: '存证管理',
        icon: 'Tickets'
      }
    },
    {
      path: 'verify',
      name: 'BlockchainVerify',
      component: () => import('@/views/blockchain/verify/index.vue'),
      meta: {
        title: '存证验证',
        icon: 'CircleCheck'
      }
    }
  ]
}

export default blockchainRouter
