<template>
  <div class="compliance-task-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>检查任务管理</span>
          <el-button type="primary" @click="handleCreate">创建任务</el-button>
        </div>
      </template>
      
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="taskName" label="任务名称" />
        <el-table-column prop="taskType" label="任务类型" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button size="small" @click="handleExecute(row)">执行</el-button>
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const tableData = ref([])

const getStatusType = (status: number) => {
  const types: any = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status: number) => {
  const texts: any = { 0: '待执行', 1: '执行中', 2: '已完成', 3: '失败' }
  return texts[status] || '未知'
}

const handleCreate = () => {
  ElMessage.info('创建任务功能')
}

const handleExecute = (row: any) => {
  ElMessage.info('执行任务: ' + row.taskName)
}

const handleView = (row: any) => {
  ElMessage.info('查看任务: ' + row.taskName)
}

const handleDelete = (row: any) => {
  ElMessage.info('删除任务: ' + row.taskName)
}

onMounted(() => {
  tableData.value = [
    { id: 1, taskName: '日常合规检查', taskType: '定期检查', status: 2, createTime: '2025-01-04 10:00:00' },
    { id: 2, taskName: '数据加密检查', taskType: '专项检查', status: 0, createTime: '2025-01-04 11:00:00' }
  ]
})
</script>

<style scoped>
.compliance-task-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
