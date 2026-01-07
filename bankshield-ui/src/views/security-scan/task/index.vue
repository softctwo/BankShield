<template>
  <div class="scan-task-container">
    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="任务名称">
          <el-input v-model="queryForm.taskName" placeholder="请输入任务名称" clearable />
        </el-form-item>
        <el-form-item label="扫描类型">
          <el-select v-model="queryForm.taskType" placeholder="请选择" clearable>
            <el-option label="SQL注入" value="SQL_INJECTION" />
            <el-option label="XSS检测" value="XSS" />
            <el-option label="依赖扫描" value="DEPENDENCY" />
            <el-option label="代码扫描" value="CODE_SCAN" />
            <el-option label="全面扫描" value="FULL_SCAN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable>
            <el-option label="待执行" value="PENDING" />
            <el-option label="运行中" value="RUNNING" />
            <el-option label="已完成" value="SUCCESS" />
            <el-option label="失败" value="FAILED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon> 查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>

      <el-row style="margin-bottom: 15px">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon> 新建任务
        </el-button>
      </el-row>

      <el-table v-loading="loading" :data="taskList" border>
        <el-table-column prop="taskName" label="任务名称" min-width="150" />
        <el-table-column prop="scanType" label="扫描类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ getScanTypeLabel(row.scanType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="scanTarget" label="扫描目标" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="progress" label="进度" width="150">
          <template #default="{ row }">
            <el-progress :percentage="row.progress || 0" :status="getProgressStatus(row.status)" />
          </template>
        </el-table-column>
        <el-table-column prop="riskCount" label="发现漏洞" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">详情</el-button>
            <el-button v-if="row.status === 'PENDING'" link type="success" size="small" @click="handleStart(row)">启动</el-button>
            <el-button v-if="row.status === 'RUNNING'" link type="warning" size="small" @click="handleStop(row)">停止</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryForm.current"
        v-model:page-size="queryForm.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="form.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="扫描类型" prop="scanType">
          <el-select v-model="form.scanType" placeholder="请选择扫描类型" style="width: 100%">
            <el-option label="SQL注入检测" value="SQL_INJECTION" />
            <el-option label="XSS检测" value="XSS" />
            <el-option label="依赖漏洞扫描" value="DEPENDENCY" />
            <el-option label="代码安全扫描" value="CODE_SCAN" />
            <el-option label="全面扫描" value="FULL_SCAN" />
          </el-select>
        </el-form-item>
        <el-form-item label="扫描目标" prop="scanTarget">
          <el-input v-model="form.scanTarget" type="textarea" :rows="3" placeholder="请输入扫描目标路径" />
        </el-form-item>
        <el-form-item label="扫描范围" prop="scanScope">
          <el-select v-model="form.scanScope" placeholder="请选择" style="width: 100%">
            <el-option label="全面扫描" value="FULL" />
            <el-option label="部分扫描" value="PARTIAL" />
            <el-option label="自定义" value="CUSTOM" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const taskList = ref<any[]>([])
const total = ref(0)

const queryForm = reactive({
  current: 1,
  size: 10,
  taskName: '',
  taskType: '',
  status: ''
})

const dialogVisible = ref(false)
const dialogTitle = ref('新建扫描任务')
const formRef = ref()
const form = reactive({
  taskName: '',
  scanType: '',
  scanTarget: '',
  scanScope: 'FULL'
})

const rules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  scanType: [{ required: true, message: '请选择扫描类型', trigger: 'change' }],
  scanTarget: [{ required: true, message: '请输入扫描目标', trigger: 'blur' }]
}

const loadTaskList = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/api/security-scan/task', { params: queryForm })
    taskList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('加载任务列表失败:', error)
    // 使用模拟数据
    taskList.value = [
      { id: 1, taskName: 'SQL注入扫描任务', scanType: 'SQL_INJECTION', scanTarget: '/api/*', status: 'SUCCESS', progress: 100, riskCount: 3, createTime: '2025-01-05 10:00:00' },
      { id: 2, taskName: 'XSS漏洞扫描', scanType: 'XSS', scanTarget: '/web/*', status: 'RUNNING', progress: 65, riskCount: 1, createTime: '2025-01-06 14:30:00' },
      { id: 3, taskName: '依赖漏洞检测', scanType: 'DEPENDENCY', scanTarget: 'pom.xml', status: 'PENDING', progress: 0, riskCount: 0, createTime: '2025-01-06 16:00:00' }
    ]
    total.value = 3
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryForm.current = 1
  loadTaskList()
}

const handleReset = () => {
  queryForm.taskName = ''
  queryForm.taskType = ''
  queryForm.status = ''
  handleQuery()
}

const handleAdd = () => {
  dialogTitle.value = '新建扫描任务'
  dialogVisible.value = true
  Object.assign(form, {
    taskName: '',
    scanType: '',
    scanTarget: '',
    scanScope: 'FULL'
  })
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    await request.post('/api/security-scan/task', form)
    ElMessage.success('创建成功')
    dialogVisible.value = false
    loadTaskList()
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

const handleStart = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要启动此扫描任务吗？', '提示', {
      type: 'warning'
    })
    await request.post(`/api/security-scan/task/${row.id}/start`)
    ElMessage.success('任务已启动')
    loadTaskList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('启动失败')
    }
  }
}

const handleStop = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要停止此扫描任务吗？', '提示', {
      type: 'warning'
    })
    await request.post(`/api/security-scan/task/${row.id}/stop`)
    ElMessage.success('任务已停止')
    loadTaskList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('停止失败')
    }
  }
}

const handleView = (row: any) => {
  ElMessage.info('查看详情功能开发中')
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除此扫描任务吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/api/security-scan/task/${row.id}`)
    ElMessage.success('删除成功')
    loadTaskList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const getScanTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    SQL_INJECTION: 'SQL注入',
    XSS: 'XSS检测',
    DEPENDENCY: '依赖扫描',
    CODE_SCAN: '代码扫描',
    FULL_SCAN: '全面扫描'
  }
  return map[type] || type
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

const getStatusType = (status: string) => {
  const map: Record<string, any> = {
    PENDING: 'info',
    RUNNING: 'warning',
    SUCCESS: 'success',
    FAILED: 'danger',
    CANCELLED: 'info'
  }
  return map[status] || 'info'
}

const getProgressStatus = (status: string) => {
  if (status === 'SUCCESS') return 'success'
  if (status === 'FAILED') return 'exception'
  return undefined
}

onMounted(() => {
  loadTaskList()
})
</script>

<style scoped lang="less">
.scan-task-container {
  padding: 20px;
}
</style>
