<template>
  <div class="page-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>加密任务管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新建任务
          </el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="任务名称">
          <el-input v-model="queryParams.name" placeholder="请输入任务名称" clearable />
        </el-form-item>
        <el-form-item label="任务状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="待执行" value="pending" />
            <el-option label="执行中" value="running" />
            <el-option label="已完成" value="completed" />
            <el-option label="失败" value="failed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="任务名称" min-width="150" />
        <el-table-column prop="strategy" label="加密策略" width="150" />
        <el-table-column prop="dataSource" label="数据源" width="150" />
        <el-table-column prop="targetTable" label="目标表" width="150" />
        <el-table-column prop="progress" label="进度" width="150">
          <template #default="{ row }">
            <el-progress :percentage="row.progress" :status="getProgressStatus(row.status)" />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleExecute(row)" :disabled="row.status !== 'pending'">执行</el-button>
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
        class="pagination"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const total = ref(0)

const queryParams = reactive({
  name: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

const tableData = ref([
  { id: 1, name: '客户信息加密', strategy: '敏感数据加密策略', dataSource: 'MySQL主库', targetTable: 'customer_info', progress: 100, status: 'completed', createTime: '2024-01-01 10:00:00' },
  { id: 2, name: '交易记录加密', strategy: '敏感数据加密策略', dataSource: 'MySQL主库', targetTable: 'transaction_log', progress: 65, status: 'running', createTime: '2024-01-02 10:00:00' },
  { id: 3, name: '日志文件加密', strategy: '文件加密策略', dataSource: '文件服务器', targetTable: 'logs/', progress: 0, status: 'pending', createTime: '2024-01-03 10:00:00' },
  { id: 4, name: '备份数据加密', strategy: '备份加密策略', dataSource: '备份服务器', targetTable: 'backup/', progress: 30, status: 'failed', createTime: '2024-01-04 10:00:00' }
])

const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    'pending': 'info',
    'running': 'warning',
    'completed': 'success',
    'failed': 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    'pending': '待执行',
    'running': '执行中',
    'completed': '已完成',
    'failed': '失败'
  }
  return texts[status] || status
}

const getProgressStatus = (status: string) => {
  if (status === 'completed') return 'success'
  if (status === 'failed') return 'exception'
  return undefined
}

const handleQuery = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    total.value = tableData.value.length
  }, 500)
}

const handleReset = () => {
  queryParams.name = ''
  queryParams.status = ''
  handleQuery()
}

const handleAdd = () => {
  ElMessage.info('新建加密任务')
}

const handleExecute = (row: any) => {
  ElMessageBox.confirm(`确定执行任务"${row.name}"吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    row.status = 'running'
    ElMessage.success('任务已开始执行')
  }).catch(() => {})
}

const handleView = (row: any) => {
  ElMessage.info(`查看任务详情：${row.name}`)
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定删除任务"${row.name}"吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
  }).catch(() => {})
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped lang="less">
.page-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search-form {
  margin-bottom: 20px;
}
.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
