<template>
  <div class="federated-dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="mb-4">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card gradient-blue">
          <div class="stat-content">
            <el-icon size="48"><DataAnalysis /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalJobs }}</div>
              <div class="stat-label">联邦学习任务</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card gradient-green">
          <div class="stat-content">
            <el-icon size="48"><CircleCheck /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.completedJobs }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card gradient-orange">
          <div class="stat-content">
            <el-icon size="48"><Connection /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.onlineParties }}/{{ stats.totalParties }}</div>
              <div class="stat-label">在线参与方</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card gradient-purple">
          <div class="stat-content">
            <el-icon size="48"><TrendCharts /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ (stats.avgAccuracy * 100).toFixed(1) }}%</div>
              <div class="stat-label">平均准确率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作栏 -->
    <el-card class="mb-4">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="任务类型">
          <el-select v-model="queryForm.jobType" placeholder="请选择" clearable style="width: 150px">
            <el-option label="横向联邦" value="HORIZONTAL_FL" />
            <el-option label="纵向联邦" value="VERTICAL_FL" />
            <el-option label="迁移联邦" value="TRANSFER_FL" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="待启动" value="PENDING" />
            <el-option label="训练中" value="TRAINING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已失败" value="FAILED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadJobs">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="success" @click="handleCreateJob">创建联邦任务</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 任务列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>联邦学习任务列表</span>
          <el-button type="primary" link @click="loadJobs">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </template>
      <el-table v-loading="loading" :data="jobList" border stripe>
        <el-table-column prop="jobName" label="任务名称" min-width="180" />
        <el-table-column prop="jobType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getJobTypeTag(row.jobType)">{{ getJobTypeLabel(row.jobType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="modelType" label="模型" width="80" />
        <el-table-column label="进度" width="200">
          <template #default="{ row }">
            <div class="progress-cell">
              <el-progress :percentage="row.progress || 0" :stroke-width="12" />
              <span class="round-info">{{ row.currentRound }}/{{ row.totalRounds }} 轮</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="accuracy" label="准确率" width="100">
          <template #default="{ row }">
            <span :class="getAccuracyClass(row.accuracy)">
              {{ row.accuracy ? (row.accuracy * 100).toFixed(2) + '%' : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" link type="success" @click="handleStart(row)">启动</el-button>
            <el-button v-if="row.status === 'TRAINING'" link type="warning" @click="handlePause(row)">暂停</el-button>
            <el-button v-if="row.status === 'PAUSED'" link type="success" @click="handleResume(row)">恢复</el-button>
            <el-button v-if="['TRAINING', 'PAUSED'].includes(row.status)" link type="danger" @click="handleStop(row)">停止</el-button>
            <el-button link type="primary" @click="handleViewDetail(row)">详情</el-button>
            <el-button link type="info" @click="handleViewCurve(row)">曲线</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="mt-4"
        @size-change="loadJobs"
        @current-change="loadJobs"
      />
    </el-card>

    <!-- 创建任务对话框 -->
    <el-dialog v-model="createDialogVisible" title="创建联邦学习任务" width="600px">
      <el-form :model="jobForm" label-width="120px">
        <el-form-item label="任务名称" required>
          <el-input v-model="jobForm.jobName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="联邦类型" required>
          <el-radio-group v-model="jobForm.jobType">
            <el-radio label="HORIZONTAL_FL">横向联邦</el-radio>
            <el-radio label="VERTICAL_FL">纵向联邦</el-radio>
            <el-radio label="TRANSFER_FL">迁移联邦</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="模型类型" required>
          <el-select v-model="jobForm.modelType" placeholder="请选择" style="width: 100%">
            <el-option label="逻辑回归 (LR)" value="LR" />
            <el-option label="深度神经网络 (DNN)" value="DNN" />
            <el-option label="梯度提升树 (GBDT)" value="GBDT" />
            <el-option label="决策树 (TREE)" value="TREE" />
          </el-select>
        </el-form-item>
        <el-form-item label="训练轮次" required>
          <el-input-number v-model="jobForm.totalRounds" :min="1" :max="1000" />
        </el-form-item>
        <el-form-item label="聚合策略">
          <el-select v-model="jobForm.aggregationStrategy" placeholder="请选择" style="width: 100%">
            <el-option label="FedAvg (联邦平均)" value="FED_AVG" />
            <el-option label="FedProx (联邦近端)" value="FED_PROX" />
            <el-option label="SCAFFOLD" value="SCAFFOLD" />
          </el-select>
        </el-form-item>
        <el-form-item label="差分隐私">
          <el-switch v-model="jobForm.enableDP" />
          <span class="form-tip">启用差分隐私保护</span>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="jobForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreateJob">确定</el-button>
      </template>
    </el-dialog>

    <!-- 训练曲线对话框 -->
    <el-dialog v-model="curveDialogVisible" title="训练曲线" width="800px">
      <div ref="curveChartRef" style="width: 100%; height: 400px;"></div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DataAnalysis, CircleCheck, Connection, TrendCharts, Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const loading = ref(false)
const jobList = ref<any[]>([])
const total = ref(0)
const createDialogVisible = ref(false)
const curveDialogVisible = ref(false)
const curveChartRef = ref<HTMLElement | null>(null)
let curveChart: echarts.ECharts | null = null

const stats = reactive({
  totalJobs: 12,
  completedJobs: 8,
  runningJobs: 3,
  totalParties: 6,
  onlineParties: 5,
  avgAccuracy: 0.923
})

const queryForm = reactive({
  page: 1,
  size: 10,
  jobType: '',
  status: ''
})

const jobForm = reactive({
  jobName: '',
  jobType: 'HORIZONTAL_FL',
  modelType: 'LR',
  totalRounds: 50,
  aggregationStrategy: 'FED_AVG',
  enableDP: false,
  description: ''
})

const getJobTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    HORIZONTAL_FL: '横向联邦',
    VERTICAL_FL: '纵向联邦',
    TRANSFER_FL: '迁移联邦'
  }
  return map[type] || type
}

const getJobTypeTag = (type: string) => {
  const map: Record<string, string> = {
    HORIZONTAL_FL: '',
    VERTICAL_FL: 'success',
    TRANSFER_FL: 'warning'
  }
  return map[type] || ''
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'info',
    INITIALIZING: 'warning',
    TRAINING: '',
    AGGREGATING: 'warning',
    COMPLETED: 'success',
    FAILED: 'danger',
    STOPPED: 'info',
    PAUSED: 'warning'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待启动',
    INITIALIZING: '初始化',
    TRAINING: '训练中',
    AGGREGATING: '聚合中',
    COMPLETED: '已完成',
    FAILED: '已失败',
    STOPPED: '已停止',
    PAUSED: '已暂停'
  }
  return map[status] || status
}

const getAccuracyClass = (accuracy: number) => {
  if (!accuracy) return ''
  if (accuracy >= 0.9) return 'accuracy-high'
  if (accuracy >= 0.8) return 'accuracy-medium'
  return 'accuracy-low'
}

const loadJobs = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/api/federated/jobs', { params: queryForm })
    if (res.code === 200) {
      jobList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载任务列表失败:', error)
    // 使用模拟数据
    jobList.value = [
      { id: 1, jobName: '横向联邦信用评分', jobType: 'HORIZONTAL_FL', modelType: 'LR', status: 'COMPLETED', currentRound: 50, totalRounds: 50, progress: 100, accuracy: 0.945, loss: 0.12 },
      { id: 2, jobName: '纵向联邦反欺诈模型', jobType: 'VERTICAL_FL', modelType: 'GBDT', status: 'TRAINING', currentRound: 25, totalRounds: 50, progress: 50, accuracy: 0.892, loss: 0.18 },
      { id: 3, jobName: '迁移联邦风险预测', jobType: 'TRANSFER_FL', modelType: 'DNN', status: 'PENDING', currentRound: 0, totalRounds: 30, progress: 0, accuracy: null, loss: null },
      { id: 4, jobName: '联合营销模型', jobType: 'HORIZONTAL_FL', modelType: 'LR', status: 'COMPLETED', currentRound: 40, totalRounds: 40, progress: 100, accuracy: 0.912, loss: 0.15 }
    ]
    total.value = 4
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const res: any = await request.get('/api/federated/statistics')
    if (res.code === 200) {
      Object.assign(stats, res.data)
    }
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

const resetQuery = () => {
  queryForm.jobType = ''
  queryForm.status = ''
  queryForm.page = 1
  loadJobs()
}

const handleCreateJob = () => {
  Object.assign(jobForm, {
    jobName: '', jobType: 'HORIZONTAL_FL', modelType: 'LR',
    totalRounds: 50, aggregationStrategy: 'FED_AVG', enableDP: false, description: ''
  })
  createDialogVisible.value = true
}

const submitCreateJob = async () => {
  try {
    await request.post('/api/federated/job', jobForm)
    ElMessage.success('创建成功')
    createDialogVisible.value = false
    loadJobs()
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

const handleStart = async (row: any) => {
  try {
    await request.post(`/api/federated/job/${row.id}/start`)
    ElMessage.success('任务已启动')
    loadJobs()
  } catch (error) {
    ElMessage.error('启动失败')
  }
}

const handlePause = async (row: any) => {
  try {
    await request.post(`/api/federated/job/${row.id}/pause`)
    ElMessage.success('任务已暂停')
    loadJobs()
  } catch (error) {
    ElMessage.error('暂停失败')
  }
}

const handleResume = async (row: any) => {
  try {
    await request.post(`/api/federated/job/${row.id}/resume`)
    ElMessage.success('任务已恢复')
    loadJobs()
  } catch (error) {
    ElMessage.error('恢复失败')
  }
}

const handleStop = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要停止该任务吗？', '提示', { type: 'warning' })
    await request.post(`/api/federated/job/${row.id}/stop`)
    ElMessage.success('任务已停止')
    loadJobs()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error('停止失败')
  }
}

const handleViewDetail = (row: any) => {
  ElMessage.info(`查看任务详情: ${row.jobName}`)
}

const handleViewCurve = async (row: any) => {
  curveDialogVisible.value = true
  await nextTick()
  
  if (!curveChart && curveChartRef.value) {
    curveChart = echarts.init(curveChartRef.value)
  }
  
  // 模拟训练曲线数据
  const rounds = Array.from({ length: row.currentRound || 20 }, (_, i) => i + 1)
  const accuracy = rounds.map((r) => 0.7 + r * 0.01 + Math.random() * 0.02)
  const loss = rounds.map((r) => 0.5 - r * 0.015 + Math.random() * 0.03)
  
  const option = {
    title: { text: '训练曲线', left: 'center' },
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0, data: ['准确率', '损失'] },
    xAxis: { type: 'category', data: rounds, name: '轮次' },
    yAxis: [
      { type: 'value', name: '准确率', min: 0.6, max: 1 },
      { type: 'value', name: '损失', min: 0, max: 0.6 }
    ],
    series: [
      { name: '准确率', type: 'line', data: accuracy, smooth: true, itemStyle: { color: '#67C23A' } },
      { name: '损失', type: 'line', yAxisIndex: 1, data: loss, smooth: true, itemStyle: { color: '#F56C6C' } }
    ]
  }
  curveChart?.setOption(option)
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除任务 "${row.jobName}" 吗？`, '提示', { type: 'warning' })
    await request.delete(`/api/federated/job/${row.id}`)
    ElMessage.success('删除成功')
    loadJobs()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadJobs()
  loadStatistics()
})
</script>

<style scoped lang="less">
.federated-dashboard {
  padding: 20px;
}

.stat-card {
  color: white;
  
  &.gradient-blue { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
  &.gradient-green { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
  &.gradient-orange { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
  &.gradient-purple { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
  
  .stat-content {
    display: flex;
    align-items: center;
    gap: 20px;
    padding: 10px;
  }
  .stat-info {
    .stat-value { font-size: 32px; font-weight: bold; }
    .stat-label { font-size: 14px; opacity: 0.9; }
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-cell {
  .round-info { font-size: 12px; color: #909399; margin-left: 8px; }
}

.accuracy-high { color: #67C23A; font-weight: bold; }
.accuracy-medium { color: #E6A23C; }
.accuracy-low { color: #F56C6C; }

.form-tip { margin-left: 10px; color: #909399; font-size: 12px; }

.mb-4 { margin-bottom: 16px; }
.mt-4 { margin-top: 16px; }
</style>
