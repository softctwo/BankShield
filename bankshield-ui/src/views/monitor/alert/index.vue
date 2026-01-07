<template>
  <div class="page-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card class="stat-card critical"><div class="stat-value">{{ stats.critical }}</div><div class="stat-label">严重告警</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card warning"><div class="stat-value">{{ stats.warning }}</div><div class="stat-label">警告告警</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card info"><div class="stat-value">{{ stats.info }}</div><div class="stat-label">信息告警</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card resolved"><div class="stat-value">{{ stats.resolved }}</div><div class="stat-label">已处理</div></el-card>
      </el-col>
    </el-row>

    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>告警列表</span>
          <div>
            <el-button type="success" @click="handleBatchResolve" :disabled="!selectedRows.length">批量处理</el-button>
            <el-button type="primary" @click="handleExport">导出告警</el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="告警级别">
          <el-select v-model="queryParams.level" placeholder="请选择" clearable>
            <el-option label="严重" value="critical" />
            <el-option label="警告" value="warning" />
            <el-option label="信息" value="info" />
          </el-select>
        </el-form-item>
        <el-form-item label="告警类型">
          <el-select v-model="queryParams.type" placeholder="请选择" clearable>
            <el-option label="安全威胁" value="security" />
            <el-option label="性能异常" value="performance" />
            <el-option label="系统故障" value="system" />
            <el-option label="业务异常" value="business" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="未处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已处理" value="resolved" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="level" label="级别" width="80">
          <template #default="{ row }"><el-tag :type="getLevelType(row.level)">{{ getLevelText(row.level) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }"><el-tag type="info">{{ getTypeText(row.type) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="title" label="告警标题" min-width="200" />
        <el-table-column prop="source" label="来源" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }"><el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createTime" label="告警时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
            <el-button type="success" link @click="handleResolve(row)" v-if="row.status !== 'resolved'">处理</el-button>
            <el-button type="warning" link @click="handleIgnore(row)" v-if="row.status === 'pending'">忽略</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next, jumper" @size-change="handleQuery" @current-change="handleQuery" class="pagination" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const total = ref(0)
const selectedRows = ref<any[]>([])
const stats = reactive({ critical: 5, warning: 12, info: 28, resolved: 156 })
const queryParams = reactive({ level: '', type: '', status: '', pageNum: 1, pageSize: 10 })

const tableData = ref([
  { id: 1, level: 'critical', type: 'security', title: '检测到SQL注入攻击', source: 'WAF', status: 'pending', createTime: '2024-01-01 10:00:00' },
  { id: 2, level: 'warning', type: 'performance', title: 'CPU使用率超过90%', source: '监控系统', status: 'processing', createTime: '2024-01-01 09:30:00' },
  { id: 3, level: 'critical', type: 'security', title: '异常登录尝试（5次失败）', source: '认证系统', status: 'pending', createTime: '2024-01-01 09:00:00' },
  { id: 4, level: 'info', type: 'system', title: '定时任务执行完成', source: '调度系统', status: 'resolved', createTime: '2024-01-01 08:00:00' },
  { id: 5, level: 'warning', type: 'business', title: '交易量异常增长', source: '业务监控', status: 'pending', createTime: '2024-01-01 07:30:00' }
])

const getLevelType = (level: string) => ({ critical: 'danger', warning: 'warning', info: 'info' }[level] || 'info')
const getLevelText = (level: string) => ({ critical: '严重', warning: '警告', info: '信息' }[level] || level)
const getTypeText = (type: string) => ({ security: '安全威胁', performance: '性能异常', system: '系统故障', business: '业务异常' }[type] || type)
const getStatusType = (status: string) => ({ pending: 'danger', processing: 'warning', resolved: 'success' }[status] || 'info')
const getStatusText = (status: string) => ({ pending: '未处理', processing: '处理中', resolved: '已处理' }[status] || status)

const handleQuery = () => { loading.value = true; setTimeout(() => { loading.value = false; total.value = tableData.value.length }, 500) }
const handleReset = () => { queryParams.level = ''; queryParams.type = ''; queryParams.status = ''; handleQuery() }
const handleSelectionChange = (rows: any[]) => { selectedRows.value = rows }
const handleView = (row: any) => { ElMessage.info(`查看告警详情：${row.title}`) }
const handleResolve = (row: any) => { ElMessageBox.confirm('确定处理该告警？', '提示').then(() => { row.status = 'resolved'; ElMessage.success('处理成功') }) }
const handleIgnore = (row: any) => { ElMessageBox.confirm('确定忽略该告警？', '提示').then(() => { row.status = 'resolved'; ElMessage.success('已忽略') }) }
const handleBatchResolve = () => { ElMessage.success(`批量处理${selectedRows.value.length}条告警`) }
const handleExport = () => { ElMessage.success('开始导出告警数据') }

onMounted(() => { handleQuery() })
</script>

<style scoped lang="less">
.page-container { padding: 20px; }
.stat-cards { margin-bottom: 20px; }
.stat-card { text-align: center; padding: 20px; .stat-value { font-size: 36px; font-weight: bold; } .stat-label { font-size: 14px; color: #909399; margin-top: 8px; } &.critical { .stat-value { color: #f56c6c; } } &.warning { .stat-value { color: #e6a23c; } } &.info { .stat-value { color: #409eff; } } &.resolved { .stat-value { color: #67c23a; } } }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 20px; }
.pagination { margin-top: 20px; justify-content: flex-end; }
</style>
