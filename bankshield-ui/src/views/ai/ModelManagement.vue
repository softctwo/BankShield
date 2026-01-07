<template>
  <div class="model-management">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="mb-4">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon size="40" color="#409EFF"><Cpu /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalModels }}</div>
              <div class="stat-label">总模型数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon size="40" color="#67C23A"><CircleCheck /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.deployedModels }}</div>
              <div class="stat-label">已部署</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon size="40" color="#E6A23C"><Loading /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.trainingModels }}</div>
              <div class="stat-label">训练中</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon size="40" color="#909399"><TrendCharts /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.avgAccuracy }}%</div>
              <div class="stat-label">平均准确率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作栏 -->
    <el-card class="mb-4">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="模型类型">
          <el-select v-model="queryForm.modelType" placeholder="请选择" clearable style="width: 150px">
            <el-option label="异常检测" value="ANOMALY_DETECTION" />
            <el-option label="威胁预测" value="THREAT_PREDICTION" />
            <el-option label="行为分析" value="BEHAVIOR_ANALYSIS" />
            <el-option label="风险评分" value="RISK_SCORING" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="已部署" value="DEPLOYED" />
            <el-option label="已训练" value="TRAINED" />
            <el-option label="训练中" value="TRAINING" />
            <el-option label="已弃用" value="DEPRECATED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadModels">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="success" @click="handleCreateModel">创建模型</el-button>
          <el-button type="warning" @click="handleCreateTrainingJob">创建训练任务</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 模型列表 -->
    <el-card>
      <template #header>
        <span>AI模型列表</span>
      </template>
      <el-table v-loading="loading" :data="modelList" border stripe>
        <el-table-column prop="modelName" label="模型名称" min-width="150" />
        <el-table-column prop="modelType" label="模型类型" width="140">
          <template #default="{ row }">
            <el-tag>{{ getModelTypeLabel(row.modelType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="algorithm" label="算法" width="140" />
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="accuracy" label="准确率" width="100">
          <template #default="{ row }">
            <el-progress :percentage="(row.accuracy * 100).toFixed(1)" :stroke-width="10" />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="usageCount" label="使用次数" width="100" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'TRAINED'" link type="success" @click="handleDeploy(row)">部署</el-button>
            <el-button v-if="row.status === 'DEPLOYED'" link type="warning" @click="handleUndeploy(row)">下线</el-button>
            <el-button link type="info" @click="handleExport(row)">导出</el-button>
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
        @size-change="loadModels"
        @current-change="loadModels"
      />
    </el-card>

    <!-- 创建模型对话框 -->
    <el-dialog v-model="createDialogVisible" title="创建AI模型" width="500px">
      <el-form :model="modelForm" label-width="100px">
        <el-form-item label="模型名称" required>
          <el-input v-model="modelForm.modelName" placeholder="请输入模型名称" />
        </el-form-item>
        <el-form-item label="模型类型" required>
          <el-select v-model="modelForm.modelType" placeholder="请选择" style="width: 100%">
            <el-option label="异常检测" value="ANOMALY_DETECTION" />
            <el-option label="威胁预测" value="THREAT_PREDICTION" />
            <el-option label="行为分析" value="BEHAVIOR_ANALYSIS" />
            <el-option label="风险评分" value="RISK_SCORING" />
          </el-select>
        </el-form-item>
        <el-form-item label="算法" required>
          <el-select v-model="modelForm.algorithm" placeholder="请选择" style="width: 100%">
            <el-option label="Isolation Forest" value="ISOLATION_FOREST" />
            <el-option label="LSTM" value="LSTM" />
            <el-option label="XGBoost" value="XGBOOST" />
            <el-option label="Random Forest" value="RANDOM_FOREST" />
            <el-option label="DNN" value="DNN" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="modelForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreateModel">确定</el-button>
      </template>
    </el-dialog>

    <!-- 导出对话框 -->
    <el-dialog v-model="exportDialogVisible" title="导出模型" width="400px">
      <el-form label-width="100px">
        <el-form-item label="导出格式">
          <el-radio-group v-model="exportFormat">
            <el-radio label="ONNX">ONNX</el-radio>
            <el-radio label="PMML">PMML</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exportDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitExport">导出</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Cpu, CircleCheck, Loading, TrendCharts } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const modelList = ref<any[]>([])
const total = ref(0)
const createDialogVisible = ref(false)
const exportDialogVisible = ref(false)
const exportFormat = ref('ONNX')
const currentModel = ref<any>(null)

const stats = reactive({
  totalModels: 8,
  deployedModels: 5,
  trainingModels: 2,
  avgAccuracy: 91
})

const queryForm = reactive({
  page: 1,
  size: 10,
  modelType: '',
  status: ''
})

const modelForm = reactive({
  modelName: '',
  modelType: '',
  algorithm: '',
  description: ''
})

const getModelTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    ANOMALY_DETECTION: '异常检测',
    THREAT_PREDICTION: '威胁预测',
    BEHAVIOR_ANALYSIS: '行为分析',
    RISK_SCORING: '风险评分'
  }
  return map[type] || type
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    DEPLOYED: 'success',
    TRAINED: 'info',
    TRAINING: 'warning',
    DEPRECATED: 'danger'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    DEPLOYED: '已部署',
    TRAINED: '已训练',
    TRAINING: '训练中',
    DEPRECATED: '已弃用'
  }
  return map[status] || status
}

const loadModels = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/api/ai/models', { params: queryForm })
    if (res.code === 200) {
      modelList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载模型列表失败:', error)
    // 使用模拟数据
    modelList.value = [
      { id: 1, modelName: '异常检测模型', modelType: 'ANOMALY_DETECTION', algorithm: 'ISOLATION_FOREST', version: 'v1.2', accuracy: 0.945, status: 'DEPLOYED', usageCount: 12580 },
      { id: 2, modelName: '威胁预测模型', modelType: 'THREAT_PREDICTION', algorithm: 'LSTM', version: 'v2.0', accuracy: 0.923, status: 'DEPLOYED', usageCount: 8934 },
      { id: 3, modelName: '行为分析模型', modelType: 'BEHAVIOR_ANALYSIS', algorithm: 'XGBOOST', version: 'v1.5', accuracy: 0.912, status: 'TRAINED', usageCount: 6721 },
      { id: 4, modelName: '风险评分模型', modelType: 'RISK_SCORING', algorithm: 'RANDOM_FOREST', version: 'v1.0', accuracy: 0.887, status: 'DEPLOYED', usageCount: 4523 },
      { id: 5, modelName: '入侵检测模型', modelType: 'ANOMALY_DETECTION', algorithm: 'DNN', version: 'v0.9', accuracy: 0.856, status: 'TRAINING', usageCount: 0 }
    ]
    total.value = 5
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const res: any = await request.get('/api/ai/statistics')
    if (res.code === 200) {
      Object.assign(stats, res.data)
    }
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

const resetQuery = () => {
  queryForm.modelType = ''
  queryForm.status = ''
  queryForm.page = 1
  loadModels()
}

const handleCreateModel = () => {
  Object.assign(modelForm, { modelName: '', modelType: '', algorithm: '', description: '' })
  createDialogVisible.value = true
}

const handleCreateTrainingJob = () => {
  ElMessage.info('创建训练任务功能开发中')
}

const submitCreateModel = async () => {
  try {
    await request.post('/api/ai/model', modelForm)
    ElMessage.success('创建成功')
    createDialogVisible.value = false
    loadModels()
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

const handleViewDetail = (row: any) => {
  ElMessage.info(`查看模型详情: ${row.modelName}`)
}

const handleDeploy = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要部署模型 "${row.modelName}" 吗？`, '提示', { type: 'warning' })
    await request.post(`/api/ai/model/${row.id}/deploy`)
    ElMessage.success('部署成功')
    loadModels()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error('部署失败')
  }
}

const handleUndeploy = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要下线模型 "${row.modelName}" 吗？`, '提示', { type: 'warning' })
    await request.post(`/api/ai/model/${row.id}/undeploy`)
    ElMessage.success('下线成功')
    loadModels()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error('下线失败')
  }
}

const handleExport = (row: any) => {
  currentModel.value = row
  exportFormat.value = 'ONNX'
  exportDialogVisible.value = true
}

const submitExport = async () => {
  try {
    const endpoint = exportFormat.value === 'ONNX' ? 'onnx' : 'pmml'
    await request.post(`/api/ai/model/${currentModel.value.id}/export/${endpoint}`)
    ElMessage.success(`导出${exportFormat.value}格式成功`)
    exportDialogVisible.value = false
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除模型 "${row.modelName}" 吗？`, '提示', { type: 'warning' })
    await request.delete(`/api/ai/model/${row.id}`)
    ElMessage.success('删除成功')
    loadModels()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadModels()
  loadStatistics()
})
</script>

<style scoped lang="less">
.model-management {
  padding: 20px;
}

.stat-card {
  .stat-content {
    display: flex;
    align-items: center;
    gap: 16px;
  }
  .stat-info {
    .stat-value {
      font-size: 28px;
      font-weight: bold;
      color: #303133;
    }
    .stat-label {
      font-size: 14px;
      color: #909399;
    }
  }
}

.mb-4 {
  margin-bottom: 16px;
}

.mt-4 {
  margin-top: 16px;
}
</style>
