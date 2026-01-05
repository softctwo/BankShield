<template>
  <div class="job-list">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>MPC任务列表</span>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm">
        <el-form-item label="任务类型">
          <el-select v-model="queryForm.jobType" placeholder="请选择" clearable>
            <el-option label="隐私求交" value="PSI" />
            <el-option label="安全求和" value="SECURE_SUM" />
            <el-option label="联合查询" value="JOINT_QUERY" />
          </el-select>
        </el-form-item>
        <el-form-item label="任务状态">
          <el-select v-model="queryForm.jobStatus" placeholder="请选择" clearable>
            <el-option label="运行中" value="RUNNING" />
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="jobType" label="任务类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.jobType)">{{ getTypeName(row.jobType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="jobStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.jobStatus)">{{ row.jobStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="partyIds" label="参与方" show-overflow-tooltip />
        <el-table-column prop="params" label="参数" show-overflow-tooltip />
        <el-table-column prop="result" label="结果" show-overflow-tooltip />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">详情</el-button>
            <el-button 
              v-if="row.jobStatus === 'RUNNING'" 
              type="danger" 
              size="small" 
              @click="handleCancel(row)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
        style="margin-top: 20px; text-align: right"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="任务详情" width="800px">
      <el-descriptions :column="2" border v-if="currentJob">
        <el-descriptions-item label="任务ID">{{ currentJob.id }}</el-descriptions-item>
        <el-descriptions-item label="任务类型">
          <el-tag :type="getTypeTag(currentJob.jobType)">{{ getTypeName(currentJob.jobType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="任务状态">
          <el-tag :type="getStatusTag(currentJob.jobStatus)">{{ currentJob.jobStatus }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="参与方数量">{{ currentJob.partyIds?.length || 0 }}</el-descriptions-item>
        <el-descriptions-item label="参数" :span="2">{{ currentJob.params }}</el-descriptions-item>
        <el-descriptions-item label="结果" :span="2">{{ currentJob.result || '-' }}</el-descriptions-item>
        <el-descriptions-item label="开始时间" :span="2">{{ currentJob.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间" :span="2">{{ currentJob.endTime || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as mpcApi from '@/api/mpc'
import type { ResponseData } from '@/utils/request'

const loading = ref(false)
const tableData = ref<any[]>([])
const detailVisible = ref(false)
const currentJob = ref<any>(null)

const queryForm = reactive({
  jobType: '',
  jobStatus: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...queryForm
    }
    const res = await mpcApi.getJobs(params) as ResponseData
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载数据失败', error)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  queryForm.jobType = ''
  queryForm.jobStatus = ''
  handleQuery()
}

const handleView = (row: any) => {
  currentJob.value = row
  detailVisible.value = true
}

const handleCancel = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要取消该任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await mpcApi.cancelJob(row.id) as ResponseData
    if (res.code === 200) {
      ElMessage.success('任务已取消')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消任务失败')
    }
  }
}

const getTypeName = (type: string) => {
  const nameMap: any = {
    PSI: '隐私求交',
    SECURE_SUM: '安全求和',
    JOINT_QUERY: '联合查询'
  }
  return nameMap[type] || type
}

const getTypeTag = (type: string) => {
  const tagMap: any = {
    PSI: 'primary',
    SECURE_SUM: 'success',
    JOINT_QUERY: 'warning'
  }
  return tagMap[type] || 'info'
}

const getStatusTag = (status: string) => {
  const tagMap: any = {
    RUNNING: 'warning',
    SUCCESS: 'success',
    FAILED: 'danger',
    CANCELLED: 'info'
  }
  return tagMap[status] || 'info'
}
</script>

<style scoped lang="less">
.job-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
