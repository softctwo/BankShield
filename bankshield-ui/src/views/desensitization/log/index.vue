<template>
  <div class="desensitization-log-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.userName" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="目标表">
          <el-input v-model="searchForm.targetTable" placeholder="请输入目标表" clearable />
        </el-form-item>
        <el-form-item label="日志类型">
          <el-select v-model="searchForm.logType" placeholder="请选择日志类型" clearable>
            <el-option label="单条脱敏" value="SINGLE" />
            <el-option label="批量脱敏" value="BATCH" />
            <el-option label="模板脱敏" value="TEMPLATE" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILED" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button type="success" :icon="Download" @click="handleExport">导出</el-button>
          <el-button type="info" :icon="DataAnalysis" @click="handleStatistics">统计分析</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="logType" label="日志类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getLogTypeTag(row.logType)">{{ getLogTypeLabel(row.logType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="操作用户" width="120" />
        <el-table-column prop="targetTable" label="目标表" min-width="150" />
        <el-table-column prop="targetField" label="目标字段" min-width="120" />
        <el-table-column prop="algorithmType" label="算法类型" width="100">
          <template #default="{ row }">
            <el-tag type="success">{{ row.algorithmType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="recordCount" label="记录数" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'">
              {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="executionTime" label="执行时长" width="100" align="center">
          <template #default="{ row }">
            {{ row.executionTime }}ms
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="160" />
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" :icon="View" @click="handleView(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSearch"
        @current-change="handleSearch"
      />
    </el-card>

    <el-dialog v-model="detailDialogVisible" title="日志详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="日志类型">{{ getLogTypeLabel(currentLog?.logType) }}</el-descriptions-item>
        <el-descriptions-item label="规则编码">{{ currentLog?.ruleCode }}</el-descriptions-item>
        <el-descriptions-item label="操作用户">{{ currentLog?.userName }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentLog?.userId }}</el-descriptions-item>
        <el-descriptions-item label="目标表">{{ currentLog?.targetTable }}</el-descriptions-item>
        <el-descriptions-item label="目标字段">{{ currentLog?.targetField }}</el-descriptions-item>
        <el-descriptions-item label="算法类型">{{ currentLog?.algorithmType }}</el-descriptions-item>
        <el-descriptions-item label="记录数">{{ currentLog?.recordCount }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentLog?.status === 'SUCCESS' ? 'success' : 'danger'">
            {{ currentLog?.status === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="执行时长">{{ currentLog?.executionTime }}ms</el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ currentLog?.createTime }}</el-descriptions-item>
        <el-descriptions-item label="原始值哈希" :span="2">{{ currentLog?.originalValueHash }}</el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="currentLog?.errorMessage">
          <el-text type="danger">{{ currentLog?.errorMessage }}</el-text>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="statisticsDialogVisible" title="统计分析" width="900px">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">总脱敏次数</div>
            </template>
            <div class="stat-value">{{ statistics.totalCount }}</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">成功次数</div>
            </template>
            <div class="stat-value success">{{ statistics.successCount }}</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">失败次数</div>
            </template>
            <div class="stat-value danger">{{ statistics.failedCount }}</div>
          </el-card>
        </el-col>
      </el-row>
      <el-divider />
      <div ref="chartRef" style="width: 100%; height: 400px;"></div>
      <template #footer>
        <el-button @click="statisticsDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { Search, Refresh, Download, DataAnalysis, View } from '@element-plus/icons-vue'
import * as desensitizationApi from '@/api/desensitization'

interface SearchForm {
  userName: string
  targetTable: string
  logType: string
  status: string
}

interface LogData {
  id?: number
  logType: string
  ruleCode: string
  userId: string
  userName: string
  targetTable: string
  targetField: string
  algorithmType: string
  recordCount: number
  status: string
  executionTime: number
  originalValueHash: string
  errorMessage?: string
  createTime: string
}

const loading = ref(false)
const detailDialogVisible = ref(false)
const statisticsDialogVisible = ref(false)
const currentLog = ref<LogData | null>(null)
const dateRange = ref<[Date, Date] | null>(null)
const chartRef = ref<HTMLElement>()

const searchForm = reactive<SearchForm>({
  userName: '',
  targetTable: '',
  logType: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const statistics = reactive({
  totalCount: 0,
  successCount: 0,
  failedCount: 0
})

const tableData = ref<LogData[]>([])

const getLogTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    SINGLE: '单条脱敏',
    BATCH: '批量脱敏',
    TEMPLATE: '模板脱敏'
  }
  return map[type] || type
}

const getLogTypeTag = (type: string) => {
  const map: Record<string, string> = {
    SINGLE: 'primary',
    BATCH: 'success',
    TEMPLATE: 'warning'
  }
  return map[type] || 'info'
}

const handleSearch = async () => {
  loading.value = true
  try {
    const res = await desensitizationApi.getLogs({
      current: pagination.current,
      size: pagination.size,
      userName: searchForm.userName,
      targetTable: searchForm.targetTable,
      logType: searchForm.logType,
      status: searchForm.status,
      startTime: dateRange.value ? dateRange.value[0].toISOString() : undefined,
      endTime: dateRange.value ? dateRange.value[1].toISOString() : undefined
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error('查询脱敏日志失败', error)
    // 使用模拟数据
    tableData.value = [
      {
        id: 1,
        logType: 'SINGLE',
        ruleCode: 'RULE_PHONE_001',
        userId: 'admin',
        userName: '管理员',
        targetTable: 'customer_info',
        targetField: 'phone',
        algorithmType: 'PHONE_MASK',
        recordCount: 1,
        status: 'SUCCESS',
        executionTime: 125,
        originalValueHash: 'a1b2c3d4e5f6',
        createTime: '2026-01-07 14:30:15'
      },
      {
        id: 2,
        logType: 'BATCH',
        ruleCode: 'RULE_IDCARD_002',
        userId: 'operator',
        userName: '操作员',
        targetTable: 'user_profile',
        targetField: 'id_card',
        algorithmType: 'IDCARD_MASK',
        recordCount: 150,
        status: 'SUCCESS',
        executionTime: 3420,
        originalValueHash: 'b2c3d4e5f6a1',
        createTime: '2026-01-07 13:15:30'
      },
      {
        id: 3,
        logType: 'TEMPLATE',
        ruleCode: 'TEMPLATE_CUSTOMER_001',
        userId: 'admin',
        userName: '管理员',
        targetTable: 'customer_detail',
        targetField: 'email,phone,address',
        algorithmType: 'TEMPLATE',
        recordCount: 500,
        status: 'SUCCESS',
        executionTime: 8750,
        originalValueHash: 'c3d4e5f6a1b2',
        createTime: '2026-01-07 12:00:00'
      },
      {
        id: 4,
        logType: 'SINGLE',
        ruleCode: 'RULE_EMAIL_003',
        userId: 'auditor',
        userName: '审计员',
        targetTable: 'contact_info',
        targetField: 'email',
        algorithmType: 'EMAIL_MASK',
        recordCount: 1,
        status: 'FAILED',
        executionTime: 85,
        originalValueHash: 'd4e5f6a1b2c3',
        errorMessage: '数据格式不正确',
        createTime: '2026-01-07 11:45:20'
      },
      {
        id: 5,
        logType: 'BATCH',
        ruleCode: 'RULE_NAME_004',
        userId: 'operator',
        userName: '操作员',
        targetTable: 'employee_info',
        targetField: 'name',
        algorithmType: 'NAME_MASK',
        recordCount: 80,
        status: 'SUCCESS',
        executionTime: 1850,
        originalValueHash: 'e5f6a1b2c3d4',
        createTime: '2026-01-07 10:30:45'
      }
    ]
    pagination.total = 5
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchForm.userName = ''
  searchForm.targetTable = ''
  searchForm.logType = ''
  searchForm.status = ''
  dateRange.value = null
  handleSearch()
}

const handleView = async (row: LogData) => {
  try {
    const res = await desensitizationApi.getLogById(row.id!)
    if (res.code === 200) {
      currentLog.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('查询日志详情失败')
  }
}

const handleExport = async () => {
  try {
    await desensitizationApi.exportLogs({
      userName: searchForm.userName,
      targetTable: searchForm.targetTable,
      logType: searchForm.logType,
      status: searchForm.status,
      startTime: dateRange.value ? dateRange.value[0].toISOString() : undefined,
      endTime: dateRange.value ? dateRange.value[1].toISOString() : undefined
    })
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const handleStatistics = async () => {
  try {
    const res = await desensitizationApi.getLogStatistics({
      startTime: dateRange.value ? dateRange.value[0].toISOString() : undefined,
      endTime: dateRange.value ? dateRange.value[1].toISOString() : undefined
    })
    if (res.code === 200) {
      statistics.totalCount = res.data.totalCount
      statistics.successCount = res.data.successCount
      statistics.failedCount = res.data.failedCount
      statisticsDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('查询统计数据失败')
  }
}

onMounted(() => {
  handleSearch()
})
</script>

<style scoped lang="less">
.desensitization-log-container {
  padding: 20px;

  .search-card {
    margin-bottom: 20px;
  }

  .table-card {
    .el-pagination {
      margin-top: 20px;
      justify-content: flex-end;
    }
  }

  .card-header {
    font-weight: bold;
  }

  .stat-value {
    font-size: 32px;
    font-weight: bold;
    text-align: center;
    color: #409eff;

    &.success {
      color: #67c23a;
    }

    &.danger {
      color: #f56c6c;
    }
  }
}
</style>
