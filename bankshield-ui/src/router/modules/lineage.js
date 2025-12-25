/**
 * 数据血缘增强模块路由配置
 */

const lineageRouter = {
  path: '/lineage',
  name: 'Lineage',
  component: () => import('@/views/layout/index.vue'),
  meta: {
    title: '数据血缘',
    icon: 'Share',
    roles: ['admin', 'data-admin', 'security-admin']
  },
  children: [
    {
      path: '/lineage/enhanced/discovery',
      name: 'DataLineageDiscovery',
      component: () => import('@/views/lineage/DataLineageDiscovery.vue'),
      meta: {
        title: '血缘自动发现',
        icon: 'Search'
      }
    },
    {
      path: '/lineage/enhanced/impact-analysis',
      name: 'DataImpactAnalysis',
      component: () => import('@/views/lineage/DataImpactAnalysis.vue'),
      meta: {
        title: '影响分析',
        icon: 'Warning'
      }
    },
    {
      path: '/lineage/enhanced/data-map',
      name: 'DataMapVisualization',
      component: () => import('@/views/lineage/DataMapVisualization.vue'),
      meta: {
        title: '数据地图',
        icon: 'MapLocation'
      }
    },
    {
      path: '/lineage/enhanced/lineage-graph',
      name: 'LineageGraph',
      component: () => import('@/views/lineage/LineageGraph.vue'),
      meta: {
        title: '血缘关系图',
        icon: 'Connection'
      }
    },
    {
      path: '/lineage/enhanced/traceability',
      name: 'TraceabilityAnalysis',
      component: () => import('@/views/lineage/TraceabilityAnalysis.vue'),
      meta: {
        title: '溯源分析',
        icon: 'TrendCharts'
      }
    },
    {
      path: '/lineage/enhanced/flow-analysis',
      name: 'FlowAnalysis',
      component: () => import('@/views/lineage/FlowAnalysis.vue'),
      meta: {
        title: '流向分析',
        icon: 'DataLine'
      }
    },
    {
      path: '/lineage/enhanced/quality-report',
      name: 'LineageQualityReport',
      component: () => import('@/views/lineage/LineageQualityReport.vue'),
      meta: {
        title: '血缘质量报告',
        icon: 'Document'
      }
    },
    {
      path: '/lineage/enhanced/config',
      name: 'LineageConfig',
      component: () => import('@/views/lineage/LineageConfig.vue'),
      meta: {
        title: '血缘配置',
        icon: 'Setting',
        roles: ['admin']
      }
    }
  ]
}

export default lineageRouter