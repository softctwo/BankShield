<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-900">数据血缘追踪</h1>
      <p class="mt-1 text-sm text-gray-500">追踪数据流向和依赖关系，实现全链路数据治理</p>
    </div>

    <!-- 搜索筛选区 -->
    <div class="bg-white rounded-lg shadow p-6 mb-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <el-input v-model="searchForm.keyword" placeholder="搜索表名/字段名" clearable>
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="searchForm.dataSource" placeholder="选择数据源" clearable class="w-full">
          <el-option label="全部数据源" value="" />
          <el-option label="核心业务库" value="core_db" />
          <el-option label="客户信息库" value="customer_db" />
          <el-option label="交易记录库" value="transaction_db" />
        </el-select>
        <el-select v-model="searchForm.lineageType" placeholder="血缘类型" clearable class="w-full">
          <el-option label="全部类型" value="" />
          <el-option label="表级血缘" value="table" />
          <el-option label="字段级血缘" value="field" />
        </el-select>
        <button @click="handleSearch" class="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700">
          <el-icon class="mr-1"><Search /></el-icon>
          查询
        </button>
      </div>
    </div>

    <!-- 血缘列表 -->
    <div class="bg-white rounded-lg shadow mb-6">
      <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
        <h2 class="text-lg font-medium text-gray-900">血缘关系列表</h2>
        <div class="flex gap-2">
          <button @click="handleExport" class="inline-flex items-center px-3 py-1.5 bg-white text-gray-700 text-sm font-medium rounded-lg border border-gray-300 hover:bg-gray-50">
            <el-icon class="mr-1"><Download /></el-icon>
            导出
          </button>
          <button @click="handleRefresh" class="inline-flex items-center px-3 py-1.5 bg-white text-gray-700 text-sm font-medium rounded-lg border border-gray-300 hover:bg-gray-50">
            <el-icon class="mr-1"><Refresh /></el-icon>
            刷新
          </button>
        </div>
      </div>
      <div class="overflow-hidden">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">源表</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">源字段</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">目标表</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">目标字段</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">转换规则</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">更新时间</th>
              <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">操作</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="item in lineageList" :key="item.id" class="hover:bg-gray-50">
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ item.sourceTable }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ item.sourceField }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ item.targetTable }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ item.targetField }}</td>
              <td class="px-6 py-4 text-sm text-gray-500">{{ item.transform }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ item.updateTime }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                <button @click="handleViewGraph(item)" class="text-blue-600 hover:text-blue-900 mr-3">查看血缘图</button>
                <button @click="handleViewDetail(item)" class="text-blue-600 hover:text-blue-900">详情</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- 分页 -->
      <div class="px-6 py-4 border-t border-gray-200 flex items-center justify-between">
        <div class="text-sm text-gray-500">
          共 {{ pagination.total }} 条记录，当前第 {{ pagination.current }} / {{ Math.ceil(pagination.total / pagination.pageSize) }} 页
        </div>
        <div class="flex gap-2">
          <button 
            @click="handlePageChange(pagination.current - 1)" 
            :disabled="pagination.current === 1"
            class="px-3 py-1 bg-white text-gray-700 text-sm font-medium rounded border border-gray-300 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            上一页
          </button>
          <button 
            @click="handlePageChange(pagination.current + 1)" 
            :disabled="pagination.current >= Math.ceil(pagination.total / pagination.pageSize)"
            class="px-3 py-1 bg-white text-gray-700 text-sm font-medium rounded border border-gray-300 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            下一页
          </button>
        </div>
      </div>
    </div>

    <!-- 血缘图可视化对话框 -->
    <el-dialog v-model="graphDialogVisible" title="数据血缘图" width="80%" top="5vh">
      <div class="bg-gray-50 rounded-lg p-4" style="height: 600px;">
        <div class="text-center text-gray-500 pt-40">
          <el-icon class="text-6xl mb-4"><Share /></el-icon>
          <p class="text-lg">血缘图可视化</p>
          <p class="text-sm mt-2">{{ selectedLineage?.sourceTable }} → {{ selectedLineage?.targetTable }}</p>
          <div class="mt-6 flex items-center justify-center gap-4">
            <div class="flex items-center">
              <div class="w-4 h-4 bg-blue-600 rounded mr-2"></div>
              <span class="text-sm">源表</span>
            </div>
            <div class="flex items-center">
              <div class="w-4 h-4 bg-green-600 rounded mr-2"></div>
              <span class="text-sm">目标表</span>
            </div>
            <div class="flex items-center">
              <div class="w-4 h-4 bg-yellow-600 rounded mr-2"></div>
              <span class="text-sm">中间表</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="血缘详情" width="600px">
      <div v-if="selectedLineage" class="space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <div>
            <p class="text-sm text-gray-500 mb-1">源表</p>
            <p class="text-sm font-medium text-gray-900">{{ selectedLineage.sourceTable }}</p>
          </div>
          <div>
            <p class="text-sm text-gray-500 mb-1">源字段</p>
            <p class="text-sm font-medium text-gray-900">{{ selectedLineage.sourceField }}</p>
          </div>
          <div>
            <p class="text-sm text-gray-500 mb-1">目标表</p>
            <p class="text-sm font-medium text-gray-900">{{ selectedLineage.targetTable }}</p>
          </div>
          <div>
            <p class="text-sm text-gray-500 mb-1">目标字段</p>
            <p class="text-sm font-medium text-gray-900">{{ selectedLineage.targetField }}</p>
          </div>
        </div>
        <div>
          <p class="text-sm text-gray-500 mb-1">转换规则</p>
          <p class="text-sm text-gray-900 bg-gray-50 p-3 rounded">{{ selectedLineage.transform }}</p>
        </div>
        <div>
          <p class="text-sm text-gray-500 mb-1">影响范围</p>
          <div class="flex flex-wrap gap-2 mt-2">
            <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
              下游表: 3个
            </span>
            <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
              影响字段: 8个
            </span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Download, Refresh, Share } from '@element-plus/icons-vue'

const graphDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const selectedLineage = ref<any>(null)

const searchForm = reactive({
  keyword: '',
  dataSource: '',
  lineageType: ''
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 45
})

const lineageList = ref([
  { id: 1, sourceTable: 'customer_info', sourceField: 'customer_id', targetTable: 'order_info', targetField: 'customer_id', transform: '直接映射', updateTime: '2024-12-30 10:00:00' },
  { id: 2, sourceTable: 'customer_info', sourceField: 'phone', targetTable: 'customer_contact', targetField: 'mobile', transform: '字段重命名', updateTime: '2024-12-30 09:30:00' },
  { id: 3, sourceTable: 'order_info', sourceField: 'amount', targetTable: 'order_summary', targetField: 'total_amount', transform: 'SUM聚合', updateTime: '2024-12-29 16:20:00' },
  { id: 4, sourceTable: 'transaction_log', sourceField: 'trans_time', targetTable: 'daily_report', targetField: 'report_date', transform: 'DATE转换', updateTime: '2024-12-29 14:10:00' },
  { id: 5, sourceTable: 'user_account', sourceField: 'balance', targetTable: 'account_summary', targetField: 'total_balance', transform: 'SUM聚合', updateTime: '2024-12-29 12:00:00' }
])

const handleSearch = () => {
  ElMessage.success('查询成功')
}

const handleExport = () => {
  ElMessage.success('导出成功')
}

const handleRefresh = () => {
  ElMessage.success('刷新成功')
}

const handleViewGraph = (row: any) => {
  selectedLineage.value = row
  graphDialogVisible.value = true
}

const handleViewDetail = (row: any) => {
  selectedLineage.value = row
  detailDialogVisible.value = true
}

const handlePageChange = (page: number) => {
  pagination.current = page
  ElMessage.info(`切换到第 ${page} 页`)
}
</script>
