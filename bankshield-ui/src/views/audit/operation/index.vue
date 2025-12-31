<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-900">操作审计</h1>
      <p class="mt-1 text-sm text-gray-500">查看系统操作日志和审计记录</p>
    </div>

    <div class="mb-6 bg-white rounded-lg shadow p-4">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">操作人</label>
          <input v-model="searchForm.operator" type="text" placeholder="请输入操作人" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">操作类型</label>
          <select v-model="searchForm.type" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
            <option value="">全部</option>
            <option value="create">新增</option>
            <option value="update">修改</option>
            <option value="delete">删除</option>
            <option value="login">登录</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">时间范围</label>
          <el-date-picker v-model="searchForm.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" class="w-full" />
        </div>
        <div class="flex items-end gap-2">
          <button @click="handleSearch" class="flex-1 px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors">搜索</button>
          <button @click="handleReset" class="px-4 py-2 bg-gray-100 text-gray-700 text-sm font-medium rounded-lg hover:bg-gray-200 transition-colors">重置</button>
        </div>
      </div>
    </div>

    <div class="bg-white rounded-lg shadow overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作人</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作类型</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作模块</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作内容</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">IP地址</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作时间</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="log in auditList" :key="log.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ log.operator }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium" :class="getTypeClass(log.type)">
                {{ getTypeName(log.type) }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ log.module }}</td>
            <td class="px-6 py-4 text-sm text-gray-500">{{ log.content }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ log.ip }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ log.createTime }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium" :class="log.status === 'success' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'">
                {{ log.status === 'success' ? '成功' : '失败' }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const searchForm = reactive({
  operator: '',
  type: '',
  dateRange: []
})

const auditList = ref([
  { id: 1, operator: 'admin', type: 'create', module: '用户管理', content: '新增用户 test001', ip: '192.168.1.100', createTime: '2024-12-30 10:30:00', status: 'success' },
  { id: 2, operator: 'admin', type: 'update', module: '角色管理', content: '修改角色权限', ip: '192.168.1.100', createTime: '2024-12-30 10:25:00', status: 'success' },
  { id: 3, operator: 'audit', type: 'delete', module: '部门管理', content: '删除部门 测试部', ip: '192.168.1.101', createTime: '2024-12-30 10:20:00', status: 'success' },
  { id: 4, operator: 'operator', type: 'login', module: '系统登录', content: '用户登录系统', ip: '192.168.1.102', createTime: '2024-12-30 10:15:00', status: 'success' },
  { id: 5, operator: 'test', type: 'login', module: '系统登录', content: '用户登录失败', ip: '192.168.1.103', createTime: '2024-12-30 10:10:00', status: 'fail' }
])

const handleSearch = () => {
  ElMessage.success('搜索完成')
}

const handleReset = () => {
  Object.assign(searchForm, { operator: '', type: '', dateRange: [] })
  ElMessage.success('重置成功')
}

const getTypeName = (type: string) => {
  const map: Record<string, string> = {
    create: '新增',
    update: '修改',
    delete: '删除',
    login: '登录'
  }
  return map[type] || type
}

const getTypeClass = (type: string) => {
  const map: Record<string, string> = {
    create: 'bg-green-100 text-green-800',
    update: 'bg-blue-100 text-blue-800',
    delete: 'bg-red-100 text-red-800',
    login: 'bg-yellow-100 text-yellow-800'
  }
  return map[type] || 'bg-gray-100 text-gray-800'
}
</script>
