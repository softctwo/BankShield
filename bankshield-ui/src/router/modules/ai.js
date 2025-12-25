/**
 * AI智能安全分析模块路由配置
 */

const aiRouter = {
  path: '/ai',
  name: 'AI',
  component: () => import('@/views/layout/index.vue'),
  meta: {
    title: 'AI智能分析',
    icon: 'CPU',
    roles: ['admin', 'security-admin']
  },
  children: [
    {
      path: '/ai/dashboard',
      name: 'AIDashboard',
      component: () => import('@/views/ai/AIDashboard.vue'),
      meta: {
        title: 'AI分析大屏',
        icon: 'DataBoard',
        affix: true
      }
    },
    {
      path: '/ai/anomaly-list',
      name: 'AnomalyList',
      component: () => import('@/views/ai/AnomalyList.vue'),
      meta: {
        title: '异常行为管理',
        icon: 'Warning'
      }
    },
    {
      path: '/ai/anomaly-detail/:id',
      name: 'AnomalyDetail',
      component: () => import('@/views/ai/AnomalyDetail.vue'),
      meta: {
        title: '异常行为详情',
        icon: 'View',
        hidden: true
      }
    },
    {
      path: '/ai/alert-management',
      name: 'AlertManagement',
      component: () => import('@/views/ai/AlertManagement.vue'),
      meta: {
        title: '智能告警管理',
        icon: 'Bell'
      }
    },
    {
      path: '/ai/alert-detail/:id',
      name: 'AlertDetail',
      component: () => import('@/views/ai/AlertDetail.vue'),
      meta: {
        title: '告警详情',
        icon: 'View',
        hidden: true
      }
    },
    {
      path: '/ai/prediction',
      name: 'Prediction',
      component: () => import('@/views/ai/Prediction.vue'),
      meta: {
        title: '预测性分析',
        icon: 'TrendCharts'
      }
    },
    {
      path: '/ai/model-management',
      name: 'ModelManagement',
      component: () => import('@/views/ai/ModelManagement.vue'),
      meta: {
        title: 'AI模型管理',
        icon: 'Operation'
      }
    },
    {
      path: '/ai/model-training',
      name: 'ModelTraining',
      component: () => import('@/views/ai/ModelTraining.vue'),
      meta: {
        title: '模型训练',
        icon: 'VideoPlay'
      }
    },
    {
      path: '/ai/behavior-analysis',
      name: 'BehaviorAnalysis',
      component: () => import('@/views/ai/BehaviorAnalysis.vue'),
      meta: {
        title: '行为分析',
        icon: 'UserFilled'
      }
    },
    {
      path: '/ai/threat-intelligence',
      name: 'ThreatIntelligence',
      component: () => import('@/views/ai/ThreatIntelligence.vue'),
      meta: {
        title: '威胁情报',
        icon: 'WarningFilled'
      }
    },
    {
      path: '/ai/config',
      name: 'AIConfig',
      component: () => import('@/views/ai/AIConfig.vue'),
      meta: {
        title: 'AI配置管理',
        icon: 'Setting',
        roles: ['admin']
      }
    }
  ]
}

export default aiRouter