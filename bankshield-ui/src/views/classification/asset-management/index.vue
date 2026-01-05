<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="mb-6 flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">资产管理</h1>
        <p class="mt-1 text-sm text-gray-500">管理数据资产分类分级和敏感信息</p>
      </div>
      <div class="flex gap-3">
        <button @click="handleAdd" class="inline-flex items-center px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors">
          <el-icon class="mr-2"><Plus /></el-icon>
          新增资产
        </button>
        <button @click="handleScan" class="inline-flex items-center px-4 py-2 bg-white text-gray-700 text-sm font-medium rounded-lg border border-gray-300 hover:bg-gray-50 transition-colors">
          <el-icon class="mr-2"><Search /></el-icon>
          扫描资产
        </button>
      </div>
    </div>

    <!-- 图表展示 -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-medium text-gray-900">敏感级别分布</h3>
          <button @click="handleExportLevelChart" class="text-sm text-blue-600 hover:text-blue-700">
            <el-icon class="mr-1"><Download /></el-icon>
            导出
          </button>
        </div>
        <div ref="levelChartRef" style="height: 300px;"></div>
      </div>

      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-medium text-gray-900">资产类型统计</h3>
          <button @click="handleExportTypeChart" class="text-sm text-blue-600 hover:text-blue-700">
            <el-icon class="mr-1"><Download /></el-icon>
            导出
          </button>
        </div>
        <div ref="typeChartRef" style="height: 300px;"></div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0 h-12 w-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-2xl text-blue-600"><Files /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">总资产数</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.total }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0 h-12 w-12 bg-red-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-2xl text-red-600"><Warning /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">高敏感资产</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.high }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0 h-12 w-12 bg-yellow-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-2xl text-yellow-600"><Warning /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">中敏感资产</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.medium }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0 h-12 w-12 bg-green-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-2xl text-green-600"><CircleCheck /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">低敏感资产</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.low }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 资产列表 -->
    <div class="bg-white rounded-lg shadow overflow-hidden">
      <!-- 批量操作工具栏 -->
      <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
        <div class="flex items-center gap-4">
          <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
          <span class="text-sm text-gray-500">已选择 {{ selectedCount }} 项</span>
          <div v-if="hasSelected" class="flex gap-2">
            <button @click="handleBatchDelete" class="text-sm text-red-600 hover:text-red-700">
              <el-icon class="mr-1"><Delete /></el-icon>
              批量删除
            </button>
            <button @click="handleBatchExport" class="text-sm text-blue-600 hover:text-blue-700">
              <el-icon class="mr-1"><Download /></el-icon>
              批量导出
            </button>
          </div>
        </div>
        <div class="flex items-center gap-2">
          <el-input v-model="searchKeyword" placeholder="搜索资产名称" class="w-64" clearable>
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>

      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
              <el-checkbox v-model="selectAll" @change="handleSelectAll" />
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">资产名称</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">资产类型</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">敏感级别</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">数据量</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">所属部门</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">更新时间</th>
            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">操作</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="asset in paginatedAssets" :key="asset.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 whitespace-nowrap">
              <el-checkbox v-model="asset.selected" @change="handleSelect" />
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ asset.name }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ asset.type }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium" :class="getLevelClass(asset.level)">
                {{ getLevelName(asset.level) }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ asset.dataSize }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ asset.department }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ asset.updateTime }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
              <button @click="handleEdit(asset)" class="text-blue-600 hover:text-blue-900 mr-3">编辑</button>
              <button @click="handleClassify(asset)" class="text-yellow-600 hover:text-yellow-900 mr-3">分级</button>
              <button @click="handleDelete(asset)" class="text-red-600 hover:text-red-900">删除</button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 分页组件 -->
      <div class="px-6 py-4 border-t border-gray-200 flex items-center justify-between">
        <div class="flex items-center">
          <span class="text-sm text-gray-700">
            显示第 {{ startIndex }} 到 {{ endIndex }} 条，共 {{ filteredAssets.length }} 条
          </span>
        </div>
        <div class="flex items-center space-x-2">
          <button
            @click="handlePrevious"
            :disabled="currentPage === 1"
            class="px-3 py-1 text-sm border rounded hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            上一页
          </button>
          <div class="flex space-x-1">
            <button
              v-for="page in displayPages"
              :key="page"
              @click="handlePageChange(page)"
              :class="[
                'px-3 py-1 text-sm border rounded',
                page === currentPage
                  ? 'bg-blue-600 text-white border-blue-600'
                  : 'hover:bg-gray-50'
              ]"
            >
              {{ page }}
            </button>
          </div>
          <button
            @click="handleNext"
            :disabled="currentPage === totalPages"
            class="px-3 py-1 text-sm border rounded hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            下一页
          </button>
          <select
            v-model="pageSize"
            @change="handlePageSizeChange"
            class="px-2 py-1 text-sm border rounded"
          >
            <option :value="10">10条/页</option>
            <option :value="20">20条/页</option>
            <option :value="50">50条/页</option>
          </select>
        </div>
      </div>
    </div>

    <!-- 新增资产对话框 -->
    <el-dialog v-model="dialogVisible" title="新增资产" width="500px">
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-form-item label="资产名称">
          <el-input v-model="form.name" placeholder="请输入资产名称" />
        </el-form-item>
        <el-form-item label="资产类型">
          <el-select v-model="form.type" placeholder="请选择资产类型" class="w-full">
            <el-option label="数据库" value="数据库" />
            <el-option label="文件" value="文件" />
            <el-option label="接口" value="接口" />
          </el-select>
        </el-form-item>
        <el-form-item label="敏感级别">
          <el-select v-model="form.level" placeholder="请选择敏感级别" class="w-full">
            <el-option label="高敏感" value="high" />
            <el-option label="中敏感" value="medium" />
            <el-option label="低敏感" value="low" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属部门">
          <el-input v-model="form.department" placeholder="请输入所属部门" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <button @click="dialogVisible = false" class="px-4 py-2 bg-gray-100 text-gray-700 text-sm font-medium rounded-lg hover:bg-gray-200">取消</button>
          <button @click="handleSubmit" class="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700">确定</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { Plus, Search, Files, Warning, CircleCheck, Download, Delete } from '@element-plus/icons-vue'

// ECharts实例引用
const levelChartRef = ref<HTMLElement>()
const typeChartRef = ref<HTMLElement>()
let levelChart: echarts.ECharts | null = null
let typeChart: echarts.ECharts | null = null

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')

// 批量操作相关
const selectAll = ref(false)

const dialogVisible = ref(false)
const formRef = ref()

const stats = reactive({
  total: 156,
  high: 23,
  medium: 45,
  low: 88
})

const form = reactive({
  name: '',
  type: '数据库',
  level: 'medium',
  department: ''
})

const assetList = ref([
  { id: 1, name: '客户信息表', type: '数据库', level: 'high', dataSize: '1.2GB', department: '业务部', updateTime: '2024-12-30 10:00:00', selected: false },
  { id: 2, name: '交易记录表', type: '数据库', level: 'high', dataSize: '5.6GB', department: '财务部', updateTime: '2024-12-30 09:30:00', selected: false },
  { id: 3, name: '用户日志文件', type: '文件', level: 'medium', dataSize: '800MB', department: '技术部', updateTime: '2024-12-29 16:20:00', selected: false },
  { id: 4, name: '系统配置文件', type: '文件', level: 'low', dataSize: '50MB', department: '技术部', updateTime: '2024-12-29 14:10:00', selected: false },
  { id: 5, name: '账户余额表', type: '数据库', level: 'high', dataSize: '2.3GB', department: '财务部', updateTime: '2024-12-29 12:00:00', selected: false },
  { id: 6, name: '操作日志', type: '文件', level: 'medium', dataSize: '1.5GB', department: '技术部', updateTime: '2024-12-28 18:30:00', selected: false },
  { id: 7, name: '系统备份', type: '文件', level: 'low', dataSize: '3.2GB', department: '技术部', updateTime: '2024-12-28 15:20:00', selected: false },
  { id: 8, name: '用户权限表', type: '数据库', level: 'medium', dataSize: '200MB', department: '安全部', updateTime: '2024-12-27 11:10:00', selected: false },
  { id: 9, name: '审计报告', type: '文件', level: 'high', dataSize: '500MB', department: '审计部', updateTime: '2024-12-27 09:00:00', selected: false },
  { id: 10, name: '接口文档', type: '接口', level: 'low', dataSize: '50MB', department: '技术部', updateTime: '2024-12-26 16:40:00', selected: false },
  { id: 11, name: '数据字典', type: '数据库', level: 'medium', dataSize: '100MB', department: '技术部', updateTime: '2024-12-26 14:30:00', selected: false },
  { id: 12, name: '监控数据', type: '文件', level: 'low', dataSize: '2.1GB', department: '运维部', updateTime: '2024-12-25 20:15:00', selected: false }
])

// 计算属性
const filteredAssets = computed(() => {
  if (!searchKeyword.value) return assetList.value
  return assetList.value.filter(asset => 
    asset.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

const totalPages = computed(() => Math.ceil(filteredAssets.value.length / pageSize.value))

const startIndex = computed(() => (currentPage.value - 1) * pageSize.value + 1)

const endIndex = computed(() => Math.min(currentPage.value * pageSize.value, filteredAssets.value.length))

const paginatedAssets = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredAssets.value.slice(start, end)
})

const displayPages = computed(() => {
  const pages: number[] = []
  const maxDisplay = 5
  let start = Math.max(1, currentPage.value - Math.floor(maxDisplay / 2))
  let end = Math.min(totalPages.value, start + maxDisplay - 1)

  if (end - start < maxDisplay - 1) {
    start = Math.max(1, end - maxDisplay + 1)
  }

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  return pages
})

const selectedCount = computed(() => assetList.value.filter(a => a.selected).length)

const hasSelected = computed(() => selectedCount.value > 0)

const handleAdd = () => {
  Object.assign(form, { name: '', type: '数据库', level: 'medium', department: '' })
  dialogVisible.value = true
}

const handleScan = () => {
  ElMessage.success('资产扫描功能开发中')
}

const handleEdit = (row: any) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleClassify = (row: any) => {
  ElMessage.info(`资产分级: ${row.name}`)
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除资产 "${row.name}" 吗？`, '提示', { type: 'warning' })
    const index = assetList.value.findIndex(a => a.id === row.id)
    if (index > -1) {
      assetList.value.splice(index, 1)
      ElMessage.success('删除成功')
    }
  } catch {}
}

const handleSubmit = () => {
  const newAsset = {
    id: Date.now(),
    ...form,
    dataSize: '0MB',
    updateTime: new Date().toLocaleString('zh-CN'),
    selected: false
  }
  assetList.value.unshift(newAsset)
  ElMessage.success('新增成功')
  dialogVisible.value = false
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
    low: 'bg-green-100 text-green-800'
  }
  return map[level] || 'bg-gray-100 text-gray-800'
}

// 批量操作
const handleSelectAll = () => {
  paginatedAssets.value.forEach(asset => {
    asset.selected = selectAll.value
  })
}

const handleSelect = () => {
  selectAll.value = paginatedAssets.value.every(asset => asset.selected)
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedCount.value} 项吗？`,
      '批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    assetList.value = assetList.value.filter(asset => !asset.selected)
    selectAll.value = false
    ElMessage.success(`成功删除 ${selectedCount.value} 项`)
  } catch {}
}

const handleBatchExport = () => {
  const selectedData = assetList.value.filter(asset => asset.selected)
  ElMessage.success(`正在导出 ${selectedCount.value} 项数据`)
  console.log('导出数据:', selectedData)
}

// 分页操作
const handlePrevious = () => {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

const handleNext = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
  }
}

const handlePageChange = (page: number) => {
  currentPage.value = page
}

const handlePageSizeChange = () => {
  currentPage.value = 1
}

// 初始化敏感级别分布饼图
const initLevelChart = () => {
  if (!levelChartRef.value) return
  levelChart = echarts.init(levelChartRef.value)

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      bottom: '0%',
      left: 'center'
    },
    series: [
      {
        name: '敏感级别',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}\n{c}个'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        data: [
          { value: stats.high, name: '高敏感', itemStyle: { color: '#dc2626' } },
          { value: stats.medium, name: '中敏感', itemStyle: { color: '#ca8a04' } },
          { value: stats.low, name: '低敏感', itemStyle: { color: '#16a34a' } }
        ]
      }
    ]
  }

  levelChart.setOption(option)
}

// 初始化资产类型柱状图
const initTypeChart = () => {
  if (!typeChartRef.value) return
  typeChart = echarts.init(typeChartRef.value)

  const typeStats = {
    '数据库': assetList.value.filter(a => a.type === '数据库').length,
    '文件': assetList.value.filter(a => a.type === '文件').length,
    '接口': assetList.value.filter(a => a.type === '接口').length
  }

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: Object.keys(typeStats),
      axisLabel: {
        interval: 0
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '资产数量',
        type: 'bar',
        data: Object.values(typeStats),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#2563eb' },
            { offset: 1, color: '#3b82f6' }
          ])
        },
        barWidth: '60%'
      }
    ]
  }

  typeChart.setOption(option)
}

// 图表导出
const handleExportLevelChart = () => {
  if (levelChart) {
    const url = levelChart.getDataURL({
      type: 'png',
      pixelRatio: 2,
      backgroundColor: '#fff'
    })
    const link = document.createElement('a')
    link.href = url
    link.download = `敏感级别分布_${new Date().getTime()}.png`
    link.click()
    ElMessage.success('图表导出成功')
  }
}

const handleExportTypeChart = () => {
  if (typeChart) {
    const url = typeChart.getDataURL({
      type: 'png',
      pixelRatio: 2,
      backgroundColor: '#fff'
    })
    const link = document.createElement('a')
    link.href = url
    link.download = `资产类型统计_${new Date().getTime()}.png`
    link.click()
    ElMessage.success('图表导出成功')
  }
}

// 初始化所有图表
const initCharts = () => {
  initLevelChart()
  initTypeChart()
}

// 窗口大小改变时重新调整图表大小
const handleResize = () => {
  levelChart?.resize()
  typeChart?.resize()
}

// 生命周期钩子
onMounted(() => {
  initCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  levelChart?.dispose()
  typeChart?.dispose()
})
</script>
