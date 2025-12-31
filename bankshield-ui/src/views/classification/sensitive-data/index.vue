<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="mb-6 flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">敏感数据识别</h1>
        <p class="mt-1 text-sm text-gray-500">自动扫描和识别数据库中的敏感信息</p>
      </div>
      <div class="flex gap-3">
        <button @click="handleScan" class="inline-flex items-center px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors">
          <el-icon class="mr-2"><Search /></el-icon>
          开始扫描
        </button>
        <button @click="handleExport" class="inline-flex items-center px-4 py-2 bg-white text-gray-700 text-sm font-medium rounded-lg border border-gray-300 hover:bg-gray-50 transition-colors">
          <el-icon class="mr-2"><Download /></el-icon>
          导出报告
        </button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0 h-12 w-12 bg-red-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-2xl text-red-600"><Warning /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">高敏感字段</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.highSensitive }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0 h-12 w-12 bg-yellow-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-2xl text-yellow-600"><Warning /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">中敏感字段</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.mediumSensitive }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0 h-12 w-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-2xl text-blue-600"><Files /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">已扫描表</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.scannedTables }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0 h-12 w-12 bg-green-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-2xl text-green-600"><CircleCheck /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">识别准确率</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.accuracy }}%</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <div class="bg-white rounded-lg shadow p-6 mb-6">
      <div class="grid grid-cols-1 md:grid-cols-5 gap-4">
        <el-input v-model="searchForm.keyword" placeholder="搜索表名/字段名" clearable>
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="searchForm.dataSource" placeholder="数据源" clearable class="w-full">
          <el-option label="全部" value="" />
          <el-option label="核心业务库" value="core_db" />
          <el-option label="客户信息库" value="customer_db" />
        </el-select>
        <el-select v-model="searchForm.sensitiveLevel" placeholder="敏感级别" clearable class="w-full">
          <el-option label="全部" value="" />
          <el-option label="高敏感" value="high" />
          <el-option label="中敏感" value="medium" />
          <el-option label="低敏感" value="low" />
        </el-select>
        <el-select v-model="searchForm.dataType" placeholder="数据类型" clearable class="w-full">
          <el-option label="全部" value="" />
          <el-option label="身份证号" value="id_card" />
          <el-option label="手机号" value="phone" />
          <el-option label="银行卡号" value="bank_card" />
          <el-option label="邮箱" value="email" />
        </el-select>
        <button @click="handleSearch" class="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700">
          查询
        </button>
      </div>
    </div>

    <!-- 敏感数据列表 -->
    <div class="bg-white rounded-lg shadow">
      <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
        <h2 class="text-lg font-medium text-gray-900">敏感数据列表</h2>
        <div class="flex gap-2">
          <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
          <button @click="handleBatchMask" :disabled="selectedIds.length === 0" class="inline-flex items-center px-3 py-1.5 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed">
            批量脱敏
          </button>
        </div>
      </div>
      <div class="overflow-hidden">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left">
                <el-checkbox v-model="selectAll" @change="handleSelectAll" />
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">表名</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">字段名</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">数据类型</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">敏感级别</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">识别规则</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">样本数据</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
              <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">操作</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="item in sensitiveList" :key="item.id" class="hover:bg-gray-50">
              <td class="px-6 py-4">
                <el-checkbox v-model="item.selected" @change="handleSelect" />
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ item.tableName }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ item.fieldName }}</td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                  {{ item.dataType }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium" :class="getLevelClass(item.level)">
                  {{ getLevelName(item.level) }}
                </span>
              </td>
              <td class="px-6 py-4 text-sm text-gray-500">{{ item.rule }}</td>
              <td class="px-6 py-4 text-sm text-gray-500">{{ item.sample }}</td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium" :class="item.masked ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'">
                  {{ item.masked ? '已脱敏' : '未脱敏' }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                <button @click="handleMask(item)" class="text-blue-600 hover:text-blue-900 mr-3">脱敏</button>
                <button @click="handleIgnore(item)" class="text-gray-600 hover:text-gray-900">忽略</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div class="px-6 py-4 border-t border-gray-200 flex items-center justify-between">
        <div class="text-sm text-gray-500">
          共 {{ pagination.total }} 条，已选 {{ selectedIds.length }} 条
        </div>
        <div class="flex gap-2">
          <button 
            @click="handlePageChange(pagination.current - 1)" 
            :disabled="pagination.current === 1"
            class="px-3 py-1 bg-white text-gray-700 text-sm font-medium rounded border border-gray-300 hover:bg-gray-50 disabled:opacity-50"
          >
            上一页
          </button>
          <span class="px-3 py-1 text-sm text-gray-700">{{ pagination.current }} / {{ Math.ceil(pagination.total / pagination.pageSize) }}</span>
          <button 
            @click="handlePageChange(pagination.current + 1)" 
            :disabled="pagination.current >= Math.ceil(pagination.total / pagination.pageSize)"
            class="px-3 py-1 bg-white text-gray-700 text-sm font-medium rounded border border-gray-300 hover:bg-gray-50 disabled:opacity-50"
          >
            下一页
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Download, Warning, Files, CircleCheck } from '@element-plus/icons-vue'

const selectAll = ref(false)

const stats = reactive({
  highSensitive: 42,
  mediumSensitive: 68,
  scannedTables: 156,
  accuracy: 94.5
})

const searchForm = reactive({
  keyword: '',
  dataSource: '',
  sensitiveLevel: '',
  dataType: ''
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 110
})

const sensitiveList = ref([
  { id: 1, tableName: 'customer_info', fieldName: 'id_card', dataType: '身份证号', level: 'high', rule: '正则匹配', sample: '110101********1234', masked: false, selected: false },
  { id: 2, tableName: 'customer_info', fieldName: 'phone', dataType: '手机号', level: 'high', rule: '正则匹配', sample: '138****5678', masked: true, selected: false },
  { id: 3, tableName: 'account_info', fieldName: 'bank_card', dataType: '银行卡号', level: 'high', rule: '正则匹配', sample: '6222**********90', masked: true, selected: false },
  { id: 4, tableName: 'user_info', fieldName: 'email', dataType: '邮箱', level: 'medium', rule: '正则匹配', sample: 'u***@example.com', masked: false, selected: false },
  { id: 5, tableName: 'employee', fieldName: 'salary', dataType: '薪资', level: 'medium', rule: '字段名匹配', sample: '****', masked: false, selected: false }
])

const selectedIds = computed(() => {
  return sensitiveList.value.filter(item => item.selected).map(item => item.id)
})

const handleSearch = () => {
  ElMessage.success('查询成功')
}

const handleScan = () => {
  ElMessage.success('开始扫描敏感数据...')
}

const handleExport = () => {
  ElMessage.success('导出报告成功')
}

const handleSelectAll = () => {
  sensitiveList.value.forEach(item => {
    item.selected = selectAll.value
  })
}

const handleSelect = () => {
  selectAll.value = sensitiveList.value.every(item => item.selected)
}

const handleBatchMask = () => {
  ElMessage.success(`批量脱敏 ${selectedIds.value.length} 条数据`)
  sensitiveList.value.forEach(item => {
    if (item.selected) {
      item.masked = true
      item.selected = false
    }
  })
  selectAll.value = false
}

const handleMask = (row: any) => {
  row.masked = true
  ElMessage.success(`字段 ${row.fieldName} 已脱敏`)
}

const handleIgnore = (row: any) => {
  const index = sensitiveList.value.findIndex(item => item.id === row.id)
  if (index > -1) {
    sensitiveList.value.splice(index, 1)
    ElMessage.info('已忽略该字段')
  }
}

const handlePageChange = (page: number) => {
  pagination.current = page
}

const getLevelName = (level: string) => {
  const map: Record<string, string> = {
    high: '高敏感',
    medium: '中敏感',
    low: '低敏感'
  }
  return map[level] || level
}

const getLevelClass = (level: string) => {
  const map: Record<string, string> = {
    high: 'bg-red-100 text-red-800',
    medium: 'bg-yellow-100 text-yellow-800',
    low: 'bg-blue-100 text-blue-800'
  }
  return map[level] || 'bg-gray-100 text-gray-800'
}
</script>
