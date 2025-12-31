<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-900">登录审计</h1>
      <p class="mt-1 text-sm text-gray-500">查看用户登录日志和安全审计</p>
    </div>

    <div class="mb-6 bg-white rounded-lg shadow p-4">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">用户名</label>
          <input v-model="searchForm.username" type="text" placeholder="请输入用户名" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">登录状态</label>
          <select v-model="searchForm.status" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
            <option value="">全部</option>
            <option value="success">成功</option>
            <option value="fail">失败</option>
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
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">用户名</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">登录IP</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">登录地点</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">浏览器</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作系统</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">登录时间</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="log in loginList" :key="log.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ log.username }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ log.ip }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ log.location }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ log.browser }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ log.os }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ log.loginTime }}</td>
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
  username: '',
  status: '',
  dateRange: []
})

const loginList = ref([
  { id: 1, username: 'admin', ip: '192.168.1.100', location: '北京市', browser: 'Chrome 120', os: 'Windows 10', loginTime: '2024-12-30 10:30:00', status: 'success' },
  { id: 2, username: 'audit', ip: '192.168.1.101', location: '上海市', browser: 'Firefox 121', os: 'macOS 14', loginTime: '2024-12-30 10:25:00', status: 'success' },
  { id: 3, username: 'operator', ip: '192.168.1.102', location: '广州市', browser: 'Safari 17', os: 'macOS 14', loginTime: '2024-12-30 10:20:00', status: 'success' },
  { id: 4, username: 'test', ip: '192.168.1.103', location: '深圳市', browser: 'Chrome 120', os: 'Windows 11', loginTime: '2024-12-30 10:15:00', status: 'fail' },
  { id: 5, username: 'admin', ip: '192.168.1.100', location: '北京市', browser: 'Chrome 120', os: 'Windows 10', loginTime: '2024-12-30 09:00:00', status: 'success' }
])

const handleSearch = () => {
  ElMessage.success('搜索完成')
}

const handleReset = () => {
  Object.assign(searchForm, { username: '', status: '', dateRange: [] })
  ElMessage.success('重置成功')
}
</script>
