<template>
  <div class="page-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>存证管理</span>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新建存证</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="存证类型">
          <el-select v-model="queryParams.type" placeholder="请选择" clearable>
            <el-option label="审计日志" value="audit" />
            <el-option label="操作记录" value="operation" />
            <el-option label="合同文档" value="contract" />
            <el-option label="电子签章" value="signature" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="已上链" value="confirmed" />
            <el-option label="待确认" value="pending" />
            <el-option label="失败" value="failed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="存证标题" min-width="150" />
        <el-table-column prop="type" label="存证类型" width="120">
          <template #default="{ row }"><el-tag>{{ getTypeText(row.type) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="hash" label="区块链哈希" min-width="200" show-overflow-tooltip />
        <el-table-column prop="blockNumber" label="区块号" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="存证时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
            <el-button type="success" link @click="handleVerify(row)">验证</el-button>
            <el-button type="info" link @click="handleDownload(row)">下载</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next, jumper" @size-change="handleQuery" @current-change="handleQuery" class="pagination" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const total = ref(0)
const queryParams = reactive({ type: '', status: '', pageNum: 1, pageSize: 10 })

const tableData = ref([
  { id: 1, title: '审计日志存证-2024010101', type: 'audit', hash: '0x7a8b9c...3d4e5f', blockNumber: 12345678, status: 'confirmed', createTime: '2024-01-01 10:00:00' },
  { id: 2, title: '合同签署存证-合同A', type: 'contract', hash: '0x1a2b3c...7d8e9f', blockNumber: 12345679, status: 'confirmed', createTime: '2024-01-01 11:00:00' },
  { id: 3, title: '操作记录存证-批量导出', type: 'operation', hash: '0x4d5e6f...1a2b3c', blockNumber: 12345680, status: 'pending', createTime: '2024-01-01 12:00:00' },
  { id: 4, title: '电子签章存证-用户认证', type: 'signature', hash: '', blockNumber: 0, status: 'failed', createTime: '2024-01-01 13:00:00' }
])

const getTypeText = (type: string) => {
  const texts: Record<string, string> = { 'audit': '审计日志', 'operation': '操作记录', 'contract': '合同文档', 'signature': '电子签章' }
  return texts[type] || type
}
const getStatusType = (status: string) => {
  const types: Record<string, string> = { 'confirmed': 'success', 'pending': 'warning', 'failed': 'danger' }
  return types[status] || 'info'
}
const getStatusText = (status: string) => {
  const texts: Record<string, string> = { 'confirmed': '已上链', 'pending': '待确认', 'failed': '失败' }
  return texts[status] || status
}

const handleQuery = () => { loading.value = true; setTimeout(() => { loading.value = false; total.value = tableData.value.length }, 500) }
const handleReset = () => { queryParams.type = ''; queryParams.status = ''; handleQuery() }
const handleAdd = () => { ElMessage.info('新建存证') }
const handleView = (row: any) => { ElMessage.info(`查看存证详情：${row.title}`) }
const handleVerify = (row: any) => { ElMessage.success(`存证验证通过：${row.hash}`) }
const handleDownload = (row: any) => { ElMessage.info(`下载存证证书：${row.title}`) }

onMounted(() => { handleQuery() })
</script>

<style scoped lang="less">
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 20px; }
.pagination { margin-top: 20px; justify-content: flex-end; }
</style>
