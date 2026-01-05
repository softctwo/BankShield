import { RouteRecordRaw } from 'vue-router'

const Layout = () => import('@/views/layout/index.vue')

const desensitizationRouter: RouteRecordRaw = {
  path: '/desensitization',
  component: Layout,
  redirect: '/desensitization/rule',
  name: 'Desensitization',
  meta: {
    title: '数据脱敏',
    icon: 'Hide',
    roles: ['admin', 'security-admin']
  },
  children: [
    {
      path: 'rule',
      name: 'DesensitizationRule',
      component: () => import('@/views/desensitization/rule/index.vue'),
      meta: {
        title: '脱敏规则',
        icon: 'Document',
        roles: ['admin', 'security-admin'],
        permissions: ['desensitization:rule:list']
      }
    },
    {
      path: 'template',
      name: 'DesensitizationTemplate',
      component: () => import('@/views/desensitization/template/index.vue'),
      meta: {
        title: '脱敏模板',
        icon: 'Files',
        roles: ['admin', 'security-admin'],
        permissions: ['desensitization:template:list']
      }
    },
    {
      path: 'log',
      name: 'DesensitizationLog',
      component: () => import('@/views/desensitization/log/index.vue'),
      meta: {
        title: '脱敏日志',
        icon: 'List',
        roles: ['admin', 'security-admin', 'audit-admin'],
        permissions: ['desensitization:log:list']
      }
    },
    {
      path: 'test',
      name: 'DesensitizationTest',
      component: () => import('@/views/desensitization/test/index.vue'),
      meta: {
        title: '脱敏测试',
        icon: 'Operation',
        roles: ['admin', 'security-admin'],
        permissions: ['desensitization:test']
      }
    }
  ]
}

export default desensitizationRouter
