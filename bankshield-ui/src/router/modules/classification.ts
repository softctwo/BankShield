import { RouteRecordRaw } from 'vue-router'
import Layout from '@/views/layout/index.vue'

const classificationRouter: RouteRecordRaw = {
  path: '/classification',
  component: Layout,
  redirect: '/classification/asset-management',
  name: 'Classification',
  meta: {
    title: '数据分类分级',
    icon: 'DataAnalysis',
    roles: ['admin', 'data-manager']
  },
  children: [
    {
      path: 'asset-management',
      name: 'AssetManagement',
      component: () => import('@/views/classification/asset-management/index.vue'),
      meta: {
        title: '资产管理',
        icon: 'Coin',
        roles: ['admin', 'data-manager']
      }
    },
    {
      path: 'asset-map',
      name: 'AssetMap',
      component: () => import('@/views/classification/asset-map/index.vue'),
      meta: {
        title: '资产地图',
        icon: 'MapLocation',
        roles: ['admin', 'data-manager']
      }
    },
    {
      path: 'review',
      name: 'Review',
      component: () => import('@/views/classification/review/index.vue'),
      meta: {
        title: '资产审核',
        icon: 'Check',
        roles: ['admin', 'data-auditor']
      }
    },
    {
      path: 'data-source',
      name: 'DataSource',
      component: () => import('@/views/classification/data-source/index.vue'),
      meta: {
        title: '数据源管理',
        icon: 'Connection',
        roles: ['admin', 'data-manager']
      }
    },
    {
      path: 'lineage',
      name: 'Lineage',
      component: () => import('@/views/classification/lineage/index.vue'),
      meta: {
        title: '血缘分析',
        icon: 'Share',
        roles: ['admin', 'data-manager']
      }
    },
    {
      path: 'template',
      name: 'Template',
      component: () => import('@/views/classification/template/index.vue'),
      meta: {
        title: '敏感数据模板',
        icon: 'Document',
        roles: ['admin', 'data-manager']
      }
    },
    {
      path: 'masking-rule',
      name: 'MaskingRule',
      component: () => import('@/views/classification/masking-rule/index.vue'),
      meta: {
        title: '脱敏规则管理',
        icon: 'Hide',
        roles: ['admin', 'data-manager']
      }
    },
    {
      path: 'sensitive-data',
      name: 'SensitiveData',
      component: () => import('@/views/classification/sensitive-data/index.vue'),
      meta: {
        title: '敏感数据',
        icon: 'Warning',
        roles: ['admin', 'data-manager']
      }
    }
  ]
}

export default classificationRouter