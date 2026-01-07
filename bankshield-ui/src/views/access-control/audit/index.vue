<template>
  <div class="page-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>访问审计日志</span>
          <el-button type="primary" @click="handleExport">
            <el-icon><Download /></el-icon>导出日志
          </el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="资源类型">
          <el-select v-model="queryParams.resourceType" placeholder="请选择" clearable>
            <el-option label="数据表" value="table" />
            <el-option label="文件" value="file" />
            <el-option label="API" value="api" />
            <el-option label="报表" value="report" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="queryParams.action" placeholder="请选择" clearable>
            <el-option label="读取" value="read" />
            <el-option label="写入" value="write" />
            <el-option label="删除" value="delete" />
            <el-option label="导出" value="export" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="queryParams.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="resourceType" label="资源类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ getResourceTypeText(row.resourceType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="resource" label="访问资源" min-width="200" show-overflow-tooltip />
        <el-table-column prop="action" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getActionType(row.action)">{{ getActionText(row.action) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="result" label="结果" width="80">
          <template #default="{ row }">
            <el-tag :type="row.result === 'success' ? 'success' : 'danger'">
              {{ row.result === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="accessTime" label="访问时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
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
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'

const loading = ref(false)
const total = ref(0)

const queryParams = reactive({
  username: '',
  resourceType: '',
  action: '',
  dateRange: [],
  pageNum: 1,
  pageSize: 10
})

const tableData = ref([
  { id: 1, username: 'admin', ip: '192.168.1.100', resourceType: 'table', resource: 'customer_info', action: 'read', result: 'success', accessTime: '2024-01-01 10:00:00' },
  { id: 2, username: 'user01', ip: '192.168.1.101', resourceType: 'file', resource: '/reports/2024/annual.pdf', action: 'export', result: 'success', accessTime: '2024-01-01 10:05:00' },
  { id: 3, username: 'user02', ip: '192.168.1.102', resourceType: 'api', resource: '/api/user/list', action: 'read', result: 'failed', accessTime: '2024-01-01 10:10:00' },
  { id: 4, username: 'admin', ip: '192.168.1.100', resourceType: 'table', resource: 'transaction_log', action: 'write', result: 'success', accessTime: '2024-01-01 10:15:00' },
  { id: 5, username: 'user03', ip: '192.168.1.103', resourceType: 'report', resource: '月度安全报告', action: 'read', result: 'success', accessTime: '2024-01-01 10:20:00' }
])

const getResourceTypeText = (type: string) => {
  const texts: Record<string, string> = {
    'table': '数据表',
    'file': '文件',
    'api': 'API',
    'report': '报表'
  }
  return texts[type] || type
}

const getActionType = (action: string) => {
  const types: Record<string, string> = {
    'read': 'success',
    'write': 'warning',
    'delete': 'danger',
    'export': 'info'
  }
  return types[action] || 'info'
}

const getActionText = (action: string) => {
  const texts: Record<string, string> = {
    'read': '读取',
    'write': '写入',
    'delete': '删除',
    'export': '导出'
  }
  return texts[action] || action
}

const handleQuery = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    total.value = tableData.value.length
  }, 500)
}

const handleReset = () => {
  queryParams.username = ''
  queryParams.resourceType = ''
  queryParams.action = ''
  queryParams.dateRange = []
  handleQuery()
}

const handleExport = () => {
  ElMessage.success('开始导出审计日志')
}

const handleView = (row: any) => {
  ElMessage.info(`查看访问详情：${row.resource}`)
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
