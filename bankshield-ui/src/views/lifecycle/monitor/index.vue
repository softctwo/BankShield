<template>
  <div class="lifecycle-monitor">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background: #409eff;">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalPolicies || 0 }}</div>
              <div class="stat-label">总策略数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background: #67c23a;">
              <el-icon><FolderOpened /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalArchived || 0 }}</div>
              <div class="stat-label">已归档数据</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background: #f56c6c;">
              <el-icon><Delete /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalDestroyed || 0 }}</div>
              <div class="stat-label">已销毁数据</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background: #e6a23c;">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.pendingApproval || 0 }}</div>
              <div class="stat-label">待审批</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>待归档数据</span>
              <el-button type="primary" size="small" @click="handleBatchArchive">批量归档</el-button>
            </div>
          </template>
          <el-table 
            :data="pendingArchiveData" 
            v-loading="archiveLoading"
            max-height="400"
            @selection-change="handleArchiveSelectionChange">
            <el-table-column type="selection" width="55" />
            <el-table-column prop="assetName" label="资产名称" />
            <el-table-column prop="assetType" label="类型" width="100" />
            <el-table-column prop="sensitivityLevel" label="级别" width="80">
              <template #default="{ row }">
                <el-tag :type="getLevelType(row.sensitivityLevel)">{{ row.sensitivityLevel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="daysOld" label="天数" width="80" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="handleArchiveSingle(row)">归档</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>待销毁数据</span>
              <el-button type="danger" size="small" @click="handleBatchDestroy">批量销毁</el-button>
            </div>
          </template>
          <el-table 
            :data="pendingDestructionData" 
            v-loading="destructionLoading"
            max-height="400"
            @selection-change="handleDestructionSelectionChange">
            <el-table-column type="selection" width="55" />
            <el-table-column prop="originalTable" label="原始表" />
            <el-table-column prop="assetType" label="类型" width="100" />
            <el-table-column prop="sensitivityLevel" label="级别" width="80">
              <template #default="{ row }">
                <el-tag :type="getLevelType(row.sensitivityLevel)">{{ row.sensitivityLevel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="retentionUntil" label="到期时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.retentionUntil) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button type="danger" size="small" @click="handleDestroySingle(row)">销毁</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>执行记录</span>
          <el-select v-model="selectedPolicyId" placeholder="选择策略" clearable @change="fetchExecutions">
            <el-option 
              v-for="policy in activePolicies" 
              :key="policy.id" 
              :label="policy.policyName" 
              :value="policy.id" />
          </el-select>
        </div>
      </template>
      <el-table :data="executionRecords" v-loading="executionLoading" max-height="400">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="executionType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.executionType === 'ARCHIVE' ? 'success' : 'danger'">
              {{ row.executionType === 'ARCHIVE' ? '归档' : '销毁' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assetName" label="资产名称" />
        <el-table-column prop="executionStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.executionStatus)">
              {{ getStatusText(row.executionStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="affectedCount" label="影响数" width="80" />
        <el-table-column prop="executor" label="执行人" width="100" />
        <el-table-column prop="executionMode" label="模式" width="80">
          <template #default="{ row }">
            {{ row.executionMode === 'AUTO' ? '自动' : '手动' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="执行时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="errorMessage" label="错误信息" show-overflow-tooltip />
      </el-table>
    </el-card>

    <el-dialog v-model="destroyDialogVisible" title="销毁确认" width="500px">
      <el-form :model="destroyForm" label-width="100px">
        <el-form-item label="销毁原因">
          <el-input v-model="destroyForm.reason" type="textarea" :rows="3" placeholder="请输入销毁原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="destroyDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDestroy">确认销毁</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, FolderOpened, Delete, Warning } from '@element-plus/icons-vue'

const archiveLoading = ref(false)
const destructionLoading = ref(false)
const executionLoading = ref(false)
const destroyDialogVisible = ref(false)

const statistics = reactive({
  totalPolicies: 0,
  activePolicies: 0,
  totalArchived: 0,
  totalDestroyed: 0,
  pendingDestruction: 0,
  pendingApproval: 0
})

const pendingArchiveData = ref([])
const pendingDestructionData = ref([])
const executionRecords = ref([])
const activePolicies = ref([])
const selectedPolicyId = ref(null)

const archiveSelection = ref([])
const destructionSelection = ref([])

const destroyForm = reactive({
  reason: '',
  archiveIds: []
})

const getLevelType = (level: string) => {
  const types: Record<string, string> = {
    C1: 'info',
    C2: 'success',
    C3: 'warning',
    C4: 'danger',
    C5: 'danger'
  }
  return types[level] || 'info'
}

const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    PENDING: 'info',
    RUNNING: 'warning',
    SUCCESS: 'success',
    FAILED: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    PENDING: '待执行',
    RUNNING: '执行中',
    SUCCESS: '成功',
    FAILED: '失败'
  }
  return texts[status] || status
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

const fetchStatistics = async () => {
  try {
    const response = await fetch('/api/lifecycle/statistics')
    const result = await response.json()
    if (result.code === 200) {
      Object.assign(statistics, result.data)
    }
  } catch (error) {
    console.error('获取统计信息失败', error)
  }
}

const fetchActivePolicies = async () => {
  try {
    const response = await fetch('/api/lifecycle/policies/active')
    const result = await response.json()
    if (result.code === 200) {
      activePolicies.value = result.data
      if (activePolicies.value.length > 0) {
        selectedPolicyId.value = activePolicies.value[0].id
        fetchPendingArchive(selectedPolicyId.value)
      }
    }
  } catch (error) {
    console.error('获取活跃策略失败', error)
  }
}

const fetchPendingArchive = async (policyId: number) => {
  archiveLoading.value = true
  try {
    const response = await fetch(`/api/lifecycle/pending-archive/${policyId}`)
    const result = await response.json()
    if (result.code === 200) {
      pendingArchiveData.value = result.data
    }
  } catch (error) {
    console.error('获取待归档数据失败', error)
  } finally {
    archiveLoading.value = false
  }
}

const fetchPendingDestruction = async () => {
  destructionLoading.value = true
  try {
    const response = await fetch('/api/lifecycle/pending-destruction')
    const result = await response.json()
    if (result.code === 200) {
      pendingDestructionData.value = result.data
    }
  } catch (error) {
    console.error('获取待销毁数据失败', error)
  } finally {
    destructionLoading.value = false
  }
}

const fetchExecutions = async () => {
  executionLoading.value = true
  try {
    const url = selectedPolicyId.value 
      ? `/api/lifecycle/executions?policyId=${selectedPolicyId.value}`
      : '/api/lifecycle/executions'
    const response = await fetch(url)
    const result = await response.json()
    if (result.code === 200) {
      executionRecords.value = result.data
    }
  } catch (error) {
    console.error('获取执行记录失败', error)
  } finally {
    executionLoading.value = false
  }
}

const handleArchiveSelectionChange = (selection: any[]) => {
  archiveSelection.value = selection
}

const handleDestructionSelectionChange = (selection: any[]) => {
  destructionSelection.value = selection
}

const handleArchiveSingle = async (row: any) => {
  try {
    const response = await fetch(
      `/api/lifecycle/archive/${row.assetId}?policyId=${selectedPolicyId.value}&operator=admin`,
      { method: 'POST' }
    )
    const result = await response.json()
    if (result.code === 200) {
      ElMessage.success('归档成功')
      fetchPendingArchive(selectedPolicyId.value)
      fetchStatistics()
    } else {
      ElMessage.error(result.message || '归档失败')
    }
  } catch (error) {
    ElMessage.error('归档失败')
  }
}

const handleBatchArchive = async () => {
  if (archiveSelection.value.length === 0) {
    ElMessage.warning('请选择要归档的数据')
    return
  }

  try {
    const assetIds = archiveSelection.value.map((item: any) => item.assetId)
    const response = await fetch('/api/lifecycle/archive/batch', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        assetIds,
        policyId: selectedPolicyId.value,
        operator: 'admin'
      })
    })
    const result = await response.json()
    if (result.code === 200) {
      ElMessage.success(`批量归档完成: 成功${result.success}条，失败${result.failed}条`)
      fetchPendingArchive(selectedPolicyId.value)
      fetchStatistics()
    } else {
      ElMessage.error(result.message || '批量归档失败')
    }
  } catch (error) {
    ElMessage.error('批量归档失败')
  }
}

const handleDestroySingle = (row: any) => {
  destroyForm.reason = ''
  destroyForm.archiveIds = [row.id]
  destroyDialogVisible.value = true
}

const handleBatchDestroy = () => {
  if (destructionSelection.value.length === 0) {
    ElMessage.warning('请选择要销毁的数据')
    return
  }

  destroyForm.reason = ''
  destroyForm.archiveIds = destructionSelection.value.map((item: any) => item.id)
  destroyDialogVisible.value = true
}

const confirmDestroy = async () => {
  if (!destroyForm.reason) {
    ElMessage.warning('请输入销毁原因')
    return
  }

  try {
    const response = await fetch('/api/lifecycle/destroy/batch', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        archiveIds: destroyForm.archiveIds,
        reason: destroyForm.reason,
        operator: 'admin'
      })
    })
    const result = await response.json()
    if (result.code === 200) {
      ElMessage.success(`销毁完成: 成功${result.success}条，失败${result.failed}条`)
      destroyDialogVisible.value = false
      fetchPendingDestruction()
      fetchStatistics()
    } else {
      ElMessage.error(result.message || '销毁失败')
    }
  } catch (error) {
    ElMessage.error('销毁失败')
  }
}

onMounted(() => {
  fetchStatistics()
  fetchActivePolicies()
  fetchPendingDestruction()
  fetchExecutions()
  
  setInterval(() => {
    fetchStatistics()
  }, 30000)
})
</script>

<style scoped lang="less">
.lifecycle-monitor {
  padding: 20px;

  .stat-card {
    .stat-item {
      display: flex;
      align-items: center;

      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-size: 28px;
        margin-right: 15px;
      }

      .stat-content {
        flex: 1;

        .stat-value {
          font-size: 28px;
          font-weight: bold;
          color: #303133;
        }

        .stat-label {
          font-size: 14px;
          color: #909399;
          margin-top: 5px;
        }
      }
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
