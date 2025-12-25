import type { RouteRecordRaw } from 'vue-router'

const lineageRoutes: RouteRecordRaw[] = [
  {
    path: '/lineage',
    name: 'Lineage',
    component: () => import('@layout/index.vue'),
    redirect: '/lineage/graph',
    meta: {
      title: '数据血缘',
      icon: 'Connection',
      roles: ['admin', 'data-analyst'],
      sort: 30
    },
    children: [
      {
        path: 'graph',
        name: 'LineageGraph',
        component: () => import('@/views/lineage/graph/LineageGraph.vue'),
        meta: {
          title: '血缘图谱',
          icon: 'Share',
          roles: ['admin', 'data-analyst']
        }
      },
      {
        path: 'impact',
        name: 'ImpactAnalysis',
        component: () => import('@/views/lineage/impact/ImpactAnalysis.vue'),
        meta: {
          title: '影响分析',
          icon: 'TrendCharts',
          roles: ['admin', 'data-analyst']
        }
      },
      {
        path: 'quality',
        name: 'DataQuality',
        component: () => import('@/views/lineage/quality/DataQuality.vue'),
        meta: {
          title: '数据质量',
          icon: 'CircleCheck',
          roles: ['admin', 'data-quality-manager']
        }
      },
      {
        path: 'discovery',
        name: 'LineageDiscovery',
        component: () => import('@/views/lineage/discovery/LineageDiscovery.vue'),
        meta: {
          title: '自动发现',
          icon: 'Search',
          roles: ['admin', 'data-engineer']
        }
      }
    ]
  }
]

export default lineageRoutes