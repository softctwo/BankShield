<template>
  <div class="page-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>安全审计日志</span>
          <el-button type="primary" @click="handleExport">
            <el-icon><Download /></el-icon>导出日志
          </el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="事件类型">
          <el-select v-model="queryParams.eventType" placeholder="请选择" clearable>
            <el-option label="登录失败" value="login_failed" />
            <el-option label="权限越权" value="permission_denied" />
            <el-option label="敏感操作" value="sensitive_operation" />
            <el-option label="异常访问" value="abnormal_access" />
          </el-select>
        </el-form-item>
        <el-form-item label="风险等级">
          <el-select v-model="queryParams.riskLevel" placeholder="请选择" clearable>
            <el-option label="高危" value="high" />
            <el-option label="中危" value="medium" />
            <el-option label="低危" value="low" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker v-model="queryParams.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="eventType" label="事件类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getEventTypeColor(row.eventType)">{{ getEventTypeText(row.eventType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="riskLevel" label="风险等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getRiskLevelColor(row.riskLevel)">{{ getRiskLevelText(row.riskLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户" width="120" />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="description" label="事件描述" min-width="250" show-overflow-tooltip />
        <el-table-column prop="eventTime" label="发生时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
            <el-button type="warning" link @click="handleHandle(row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" :page-sizes="[10, 20, 50, 100]" :total="total" layout="total, sizes, prev, pager, next, jumper" @size-change="handleQuery" @current-change="handleQuery" class="pagination" />
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
  eventType: '',
  riskLevel: '',
  dateRange: [],
  pageNum: 1,
  pageSize: 10
})

const tableData = ref([
  { id: 1, eventType: 'login_failed', riskLevel: 'medium', username: 'user01', ip: '192.168.1.100', description: '连续3次登录失败，账户临时锁定', eventTime: '2024-01-01 10:00:00' },
  { id: 2, eventType: 'permission_denied', riskLevel: 'high', username: 'user02', ip: '192.168.1.101', description: '尝试访问未授权资源：/admin/settings', eventTime: '2024-01-01 10:05:00' },
  { id: 3, eventType: 'sensitive_operation', riskLevel: 'high', username: 'admin', ip: '192.168.1.102', description: '批量导出客户敏感数据（10000条）', eventTime: '2024-01-01 10:10:00' },
  { id: 4, eventType: 'abnormal_access', riskLevel: 'medium', username: 'user03', ip: '10.0.0.1', description: '非工作时间访问系统（凌晨3:00）', eventTime: '2024-01-01 03:00:00' },
  { id: 5, eventType: 'login_failed', riskLevel: 'low', username: 'user04', ip: '192.168.1.103', description: '密码错误1次', eventTime: '2024-01-01 10:20:00' }
])

const getEventTypeText = (type: string) => {
  const texts: Record<string, string> = { 'login_failed': '登录失败', 'permission_denied': '权限越权', 'sensitive_operation': '敏感操作', 'abnormal_access': '异常访问' }
  return texts[type] || type
}

const getEventTypeColor = (type: string) => {
  const colors: Record<string, string> = { 'login_failed': 'warning', 'permission_denied': 'danger', 'sensitive_operation': 'danger', 'abnormal_access': 'warning' }
  return colors[type] || 'info'
}

const getRiskLevelText = (level: string) => {
  const texts: Record<string, string> = { 'high': '高危', 'medium': '中危', 'low': '低危' }
  return texts[level] || level
}

const getRiskLevelColor = (level: string) => {
  const colors: Record<string, string> = { 'high': 'danger', 'medium': 'warning', 'low': 'success' }
  return colors[level] || 'info'
}

const handleQuery = () => { loading.value = true; setTimeout(() => { loading.value = false; total.value = tableData.value.length }, 500) }
const handleReset = () => { queryParams.eventType = ''; queryParams.riskLevel = ''; queryParams.dateRange = []; handleQuery() }
const handleExport = () => { ElMessage.success('开始导出安全审计日志') }
const handleView = (row: any) => { ElMessage.info(`查看事件详情：${row.description}`) }
const handleHandle = (row: any) => { ElMessage.info(`处理安全事件：${row.id}`) }

onMounted(() => { handleQuery() })
</script>

<style scoped lang="less">
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 20px; }
.pagination { margin-top: 20px; justify-content: flex-end; }
</style>
