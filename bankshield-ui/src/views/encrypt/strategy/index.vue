<template>
  <div class="page-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>加密策略管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增策略
          </el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="策略名称">
          <el-input v-model="queryParams.name" placeholder="请输入策略名称" clearable />
        </el-form-item>
        <el-form-item label="加密算法">
          <el-select v-model="queryParams.algorithm" placeholder="请选择" clearable>
            <el-option label="SM4" value="SM4" />
            <el-option label="SM2" value="SM2" />
            <el-option label="AES-256" value="AES-256" />
            <el-option label="RSA-2048" value="RSA-2048" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
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
        <el-table-column prop="name" label="策略名称" min-width="150" />
        <el-table-column prop="algorithm" label="加密算法" width="120">
          <template #default="{ row }">
            <el-tag :type="getAlgorithmType(row.algorithm)">{{ row.algorithm }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="keyLength" label="密钥长度" width="100" />
        <el-table-column prop="mode" label="加密模式" width="100" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
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
  algorithm: '',
  status: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10
})

const tableData = ref([
  { id: 1, name: '敏感数据加密策略', algorithm: 'SM4', keyLength: 128, mode: 'CBC', description: '用于敏感数据字段加密', status: 1, createTime: '2024-01-01 10:00:00' },
  { id: 2, name: '文件加密策略', algorithm: 'AES-256', keyLength: 256, mode: 'GCM', description: '用于文件存储加密', status: 1, createTime: '2024-01-02 10:00:00' },
  { id: 3, name: '传输加密策略', algorithm: 'SM2', keyLength: 256, mode: 'ECB', description: '用于数据传输加密', status: 1, createTime: '2024-01-03 10:00:00' },
  { id: 4, name: '备份加密策略', algorithm: 'RSA-2048', keyLength: 2048, mode: 'OAEP', description: '用于备份数据加密', status: 0, createTime: '2024-01-04 10:00:00' }
])

const getAlgorithmType = (algorithm: string) => {
  const types: Record<string, string> = {
    'SM4': 'success',
    'SM2': 'primary',
    'AES-256': 'warning',
    'RSA-2048': 'info'
  }
  return types[algorithm] || 'info'
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
  queryParams.algorithm = ''
  queryParams.status = undefined
  handleQuery()
}

const handleAdd = () => {
  ElMessage.info('新增加密策略')
}

const handleEdit = (row: any) => {
  ElMessage.info(`编辑策略：${row.name}`)
}

const handleView = (row: any) => {
  ElMessage.info(`查看策略详情：${row.name}`)
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定删除策略"${row.name}"吗？`, '提示', {
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
