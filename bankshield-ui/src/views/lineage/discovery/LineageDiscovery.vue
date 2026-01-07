<template>
  <div class="lineage-discovery-container">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalTasks || 0 }}</div>
              <div class="stat-label">任务总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon running">
              <el-icon><Loading /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.runningTasks || 0 }}</div>
              <div class="stat-label">运行中</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon success">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.completedTasks || 0 }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon flows">
              <el-icon><Connection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalDiscoveredFlows || 0 }}</div>
              <div class="stat-label">发现血缘数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作区域 -->
    <el-card class="filter-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="任务状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="待执行" value="PENDING" />
            <el-option label="运行中" value="RUNNING" />
            <el-option label="已完成" value="SUCCESS" />
            <el-option label="失败" value="FAILED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="发现策略">
          <el-select v-model="queryParams.strategy" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="s in strategyOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
      <div class="action-bar">
        <el-button type="primary" @click="handleCreateTask">
          <el-icon><Plus /></el-icon>
          创建发现任务
        </el-button>
        <el-button type="success" @click="handleQuickDiscover">
          <el-icon><MagicStick /></el-icon>
          快速发现
        </el-button>
        <el-button @click="fetchStatistics">
          <el-icon><DataAnalysis /></el-icon>
          刷新统计
        </el-button>
      </div>
    </el-card>

    <!-- 任务列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>发现任务列表</span>
          <el-button text @click="fetchTasks">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>
      </template>
      <el-table v-loading="loading" :data="taskList" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="taskName" label="任务名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="dataSourceName" label="数据源" width="150">
          <template #default="{ row }">
            {{ row.dataSourceName || `数据源#${row.dataSourceId}` }}
          </template>
        </el-table-column>
        <el-table-column prop="discoveryStrategy" label="发现策略" width="130" align="center">
          <template #default="{ row }">
            <el-tag :type="getStrategyTagType(row.discoveryStrategy)">
              {{ getStrategyLabel(row.discoveryStrategy) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="discoveredFlowsCount" label="发现数量" width="100" align="center">
          <template #default="{ row }">
            <span class="flow-count">{{ row.discoveredFlowsCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160" align="center">
          <template #default="{ row }">
            {{ row.startTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="160" align="center">
          <template #default="{ row }">
            {{ row.endTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button 
              v-if="row.status === 'RUNNING'" 
              type="warning" 
              link 
              @click="handleCancelTask(row)"
            >
              <el-icon><VideoPause /></el-icon>
              取消
            </el-button>
            <el-button 
              v-if="row.status === 'FAILED'" 
              type="success" 
              link 
              @click="handleRetryTask(row)"
            >
              <el-icon><RefreshRight /></el-icon>
              重试
            </el-button>
            <el-button 
              v-if="['SUCCESS', 'FAILED', 'CANCELLED'].includes(row.status)" 
              type="danger" 
              link 
              @click="handleDeleteTask(row)"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchTasks"
          @current-change="fetchTasks"
        />
      </div>
    </el-card>

    <!-- 创建任务对话框 -->
    <el-dialog v-model="createDialogVisible" title="创建血缘发现任务" width="600px" :close-on-click-modal="false">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="createForm.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="数据源" prop="dataSourceId">
          <el-select v-model="createForm.dataSourceId" placeholder="请选择数据源" style="width: 100%">
            <el-option 
              v-for="ds in dataSourceList" 
              :key="ds.id" 
              :label="ds.name" 
              :value="ds.id"
            >
              <span>{{ ds.name }}</span>
              <span style="color: #999; margin-left: 10px">{{ ds.type }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="发现策略" prop="discoveryStrategy">
          <el-select v-model="createForm.discoveryStrategy" placeholder="请选择发现策略" style="width: 100%">
            <el-option v-for="s in strategyOptions" :key="s.value" :label="s.label" :value="s.value">
              <div>
                <div>{{ s.label }}</div>
                <div style="font-size: 12px; color: #999">{{ s.description }}</div>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="扫描配置">
          <el-checkbox v-model="createForm.config.scanTables">扫描表</el-checkbox>
          <el-checkbox v-model="createForm.config.scanViews">扫描视图</el-checkbox>
          <el-checkbox v-model="createForm.config.scanProcedures">扫描存储过程</el-checkbox>
          <el-checkbox v-model="createForm.config.scanTriggers">扫描触发器</el-checkbox>
        </el-form-item>
        <el-form-item label="置信度阈值">
          <el-slider v-model="createForm.config.confidenceThreshold" :min="0" :max="1" :step="0.1" show-stops />
        </el-form-item>
        <el-form-item label="最大深度">
          <el-input-number v-model="createForm.config.maxDepth" :min="1" :max="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreateTask" :loading="submitLoading">创建并执行</el-button>
      </template>
    </el-dialog>

    <!-- 任务详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="任务详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="任务ID">{{ currentTask.id }}</el-descriptions-item>
        <el-descriptions-item label="任务名称">{{ currentTask.taskName }}</el-descriptions-item>
        <el-descriptions-item label="数据源">
          {{ currentTask.dataSourceName || `数据源#${currentTask.dataSourceId}` }}
        </el-descriptions-item>
        <el-descriptions-item label="发现策略">
          <el-tag :type="getStrategyTagType(currentTask.discoveryStrategy)">
            {{ getStrategyLabel(currentTask.discoveryStrategy) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentTask.status)">
            {{ getStatusLabel(currentTask.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发现血缘数">
          <span class="flow-count-large">{{ currentTask.discoveredFlowsCount || 0 }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentTask.startTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ currentTask.endTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentTask.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="currentTask.errorMessage" label="错误信息" :span="2">
          <el-text type="danger">{{ currentTask.errorMessage }}</el-text>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentTask.config" label="配置信息" :span="2">
          <el-text>{{ currentTask.config }}</el-text>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button v-if="currentTask.status === 'RUNNING'" type="warning" @click="handleCancelTask(currentTask)">
          取消任务
        </el-button>
        <el-button v-if="currentTask.status === 'FAILED'" type="primary" @click="handleRetryTask(currentTask)">
          重新执行
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  Document, Loading, CircleCheck, Connection, Search, Refresh,
  Plus, MagicStick, DataAnalysis, View, VideoPause, RefreshRight, Delete
} from '@element-plus/icons-vue'
import {
  getDiscoveryStatistics,
  getRecentDiscoveryTasks,
  createDiscoveryTask,
  getDiscoveryTaskStatus,
  cancelDiscoveryTask,
  deleteDiscoveryTask,
  retryDiscoveryTask,
  getDataSourceList,
  autoDiscoverLineage
} from '@/api/lineage'
import type { DiscoveryTask, DiscoveryStatistics, DataSourceItem } from '@/api/lineage'

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  status: '',
  strategy: ''
})

// 统计数据
const statistics = ref<DiscoveryStatistics>({
  totalTasks: 0,
  runningTasks: 0,
  completedTasks: 0,
  failedTasks: 0,
  totalDiscoveredFlows: 0
})

// 任务列表
const taskList = ref<DiscoveryTask[]>([])
const loading = ref(false)
const total = ref(0)

// 数据源列表
const dataSourceList = ref<DataSourceItem[]>([])

// 创建任务对话框
const createDialogVisible = ref(false)
const submitLoading = ref(false)
const createFormRef = ref<FormInstance>()
const createForm = reactive({
  taskName: '',
  dataSourceId: null as number | null,
  discoveryStrategy: 'SQL_PARSE',
  config: {
    scanTables: true,
    scanViews: true,
    scanProcedures: true,
    scanTriggers: false,
    confidenceThreshold: 0.5,
    maxDepth: 5
  }
})

const createRules: FormRules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  dataSourceId: [{ required: true, message: '请选择数据源', trigger: 'change' }],
  discoveryStrategy: [{ required: true, message: '请选择发现策略', trigger: 'change' }]
}

// 详情对话框
const detailDialogVisible = ref(false)
const currentTask = ref<DiscoveryTask>({} as DiscoveryTask)

// 定时刷新
let refreshTimer: ReturnType<typeof setInterval> | null = null

// 策略选项
const strategyOptions = [
  { value: 'SQL_PARSE', label: 'SQL解析', description: '解析SQL语句提取血缘关系' },
  { value: 'LOG_ANALYSIS', label: '日志分析', description: '分析SQL执行日志发现血缘' },
  { value: 'METADATA_CRAWL', label: '元数据爬取', description: '爬取数据库元数据分析血缘' },
  { value: 'ML_INFERENCE', label: '智能推断', description: '使用机器学习推断血缘关系' },
  { value: 'ALL', label: '综合发现', description: '使用所有策略进行综合发现' }
]

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const res: any = await getDiscoveryStatistics()
    if (res.data?.code === 200) {
      statistics.value = res.data.data || statistics.value
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

// 获取任务列表
const fetchTasks = async () => {
  loading.value = true
  try {
    const res: any = await getRecentDiscoveryTasks(50)
    if (res.data?.code === 200) {
      let tasks = res.data.data || []
      // 过滤
      if (queryParams.status) {
        tasks = tasks.filter((t: DiscoveryTask) => t.status === queryParams.status)
      }
      if (queryParams.strategy) {
        tasks = tasks.filter((t: DiscoveryTask) => t.discoveryStrategy === queryParams.strategy)
      }
      // 分页
      total.value = tasks.length
      const start = (queryParams.page - 1) * queryParams.size
      taskList.value = tasks.slice(start, start + queryParams.size)
    }
  } catch (error) {
    console.error('获取任务列表失败', error)
  } finally {
    loading.value = false
  }
}

// 获取数据源列表
const fetchDataSources = async () => {
  try {
    const res: any = await getDataSourceList()
    if (res.data?.code === 200) {
      dataSourceList.value = res.data.data || []
    }
  } catch (error) {
    // 使用模拟数据
    dataSourceList.value = [
      { id: 1, name: '生产数据库', type: 'MySQL', status: 'ACTIVE' },
      { id: 2, name: '分析数据库', type: 'PostgreSQL', status: 'ACTIVE' },
      { id: 3, name: '数据仓库', type: 'Hive', status: 'ACTIVE' }
    ]
  }
}

// 查询
const handleQuery = () => {
  queryParams.page = 1
  fetchTasks()
}

// 重置
const resetQuery = () => {
  queryParams.status = ''
  queryParams.strategy = ''
  handleQuery()
}

// 创建任务
const handleCreateTask = () => {
  createForm.taskName = `血缘发现任务_${new Date().toLocaleString().replace(/[\/\s:]/g, '')}`
  createForm.dataSourceId = null
  createForm.discoveryStrategy = 'SQL_PARSE'
  createForm.config = {
    scanTables: true,
    scanViews: true,
    scanProcedures: true,
    scanTriggers: false,
    confidenceThreshold: 0.5,
    maxDepth: 5
  }
  createDialogVisible.value = true
}

// 提交创建任务
const submitCreateTask = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const res: any = await createDiscoveryTask(
          createForm.taskName,
          createForm.dataSourceId!,
          createForm.discoveryStrategy,
          createForm.config
        )
        if (res.data?.code === 200) {
          ElMessage.success('任务创建成功，已开始执行')
          createDialogVisible.value = false
          fetchTasks()
          fetchStatistics()
          // 启动定时刷新
          startAutoRefresh()
        } else {
          ElMessage.error(res.data?.msg || '创建任务失败')
        }
      } catch (error) {
        ElMessage.error('创建任务失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 快速发现
const handleQuickDiscover = async () => {
  try {
    await ElMessageBox.confirm('确定要执行快速血缘发现吗？这将自动分析所有已配置的数据源。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    loading.value = true
    const res: any = await autoDiscoverLineage()
    if (res.data?.code === 200) {
      ElMessage.success('快速发现任务已启动')
      fetchTasks()
      fetchStatistics()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('启动快速发现失败')
    }
  } finally {
    loading.value = false
  }
}

// 查看详情
const handleViewDetail = async (task: DiscoveryTask) => {
  try {
    const res: any = await getDiscoveryTaskStatus(task.id)
    if (res.data?.code === 200) {
      currentTask.value = res.data.data
    } else {
      currentTask.value = task
    }
  } catch (error) {
    currentTask.value = task
  }
  detailDialogVisible.value = true
}

// 取消任务
const handleCancelTask = async (task: DiscoveryTask) => {
  try {
    await ElMessageBox.confirm('确定要取消该任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res: any = await cancelDiscoveryTask(task.id)
    if (res.data?.code === 200) {
      ElMessage.success('任务已取消')
      fetchTasks()
      fetchStatistics()
      detailDialogVisible.value = false
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消任务失败')
    }
  }
}

// 重试任务
const handleRetryTask = async (task: DiscoveryTask) => {
  try {
    const res: any = await retryDiscoveryTask(task.id)
    if (res.data?.code === 200) {
      ElMessage.success('任务已重新启动')
      fetchTasks()
      fetchStatistics()
      detailDialogVisible.value = false
      startAutoRefresh()
    }
  } catch (error) {
    ElMessage.error('重试任务失败')
  }
}

// 删除任务
const handleDeleteTask = async (task: DiscoveryTask) => {
  try {
    await ElMessageBox.confirm('确定要删除该任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res: any = await deleteDiscoveryTask(task.id)
    if (res.data?.code === 200) {
      ElMessage.success('任务已删除')
      fetchTasks()
      fetchStatistics()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除任务失败')
    }
  }
}

// 自动刷新
const startAutoRefresh = () => {
  if (refreshTimer) return
  refreshTimer = setInterval(() => {
    const hasRunning = taskList.value.some(t => t.status === 'RUNNING')
    if (hasRunning) {
      fetchTasks()
      fetchStatistics()
    } else {
      stopAutoRefresh()
    }
  }, 5000)
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 辅助方法
const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'info',
    RUNNING: 'warning',
    SUCCESS: 'success',
    FAILED: 'danger',
    CANCELLED: 'info'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待执行',
    RUNNING: '运行中',
    SUCCESS: '已完成',
    FAILED: '失败',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

const getStrategyTagType = (strategy: string) => {
  const map: Record<string, string> = {
    SQL_PARSE: '',
    LOG_ANALYSIS: 'success',
    METADATA_CRAWL: 'warning',
    ML_INFERENCE: 'danger',
    ALL: 'info'
  }
  return map[strategy] || 'info'
}

const getStrategyLabel = (strategy: string) => {
  const item = strategyOptions.find(s => s.value === strategy)
  return item?.label || strategy
}

onMounted(() => {
  fetchStatistics()
  fetchTasks()
  fetchDataSources()
  // 如果有运行中的任务，启动自动刷新
  setTimeout(() => {
    if (taskList.value.some(t => t.status === 'RUNNING')) {
      startAutoRefresh()
    }
  }, 1000)
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped lang="less">
.lineage-discovery-container {
  padding: 20px;
}

.stat-cards {
  margin-bottom: 16px;
}

.stat-card {
  .stat-content {
    display: flex;
    align-items: center;
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12px;

    .el-icon {
      font-size: 24px;
      color: #fff;
    }

    &.total {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    &.running {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    }

    &.success {
      background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
    }

    &.flows {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    }
  }

  .stat-info {
    .stat-value {
      font-size: 24px;
      font-weight: 600;
      color: #303133;
    }

    .stat-label {
      font-size: 12px;
      color: #909399;
      margin-top: 4px;
    }
  }
}

.filter-card {
  margin-bottom: 16px;

  .action-bar {
    display: flex;
    gap: 8px;
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #ebeef5;
  }
}

.table-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .pagination-container {
    display: flex;
    justify-content: flex-end;
    padding: 16px 0 0;
  }

  .flow-count {
    font-weight: 600;
    color: #409eff;
  }
}

.flow-count-large {
  font-size: 20px;
  font-weight: 600;
  color: #409eff;
}
</style>
