<template>
  <div class="scan-result-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <el-button link @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h2>扫描结果详情</h2>
        <span class="task-name" v-if="currentTask"> - {{ currentTask.taskName }}</span>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleGenerateReport" :loading="generatingReport">
          <el-icon><Document /></el-icon>
          生成报告
        </el-button>
        <el-button type="warning" @click="showBatchFixDialog = true" :disabled="selectedResults.length === 0">
          <el-icon><Check /></el-icon>
          批量修复
        </el-button>
      </div>
    </div>

    <!-- 统计信息 -->
    <el-row :gutter="20" class="statistics-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon critical">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ riskLevelCounts.CRITICAL || 0 }}</div>
              <div class="stat-label">严重风险</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon high">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ riskLevelCounts.HIGH || 0 }}</div>
              <div class="stat-label">高危风险</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon medium">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ riskLevelCounts.MEDIUM || 0 }}</div>
              <div class="stat-label">中危风险</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon low">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ riskLevelCounts.LOW || 0 }}</div>
              <div class="stat-label">低危风险</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="风险级别">
          <el-select v-model="filterForm.riskLevel" placeholder="请选择风险级别" clearable>
            <el-option label="严重" value="CRITICAL" />
            <el-option label="高危" value="HIGH" />
            <el-option label="中危" value="MEDIUM" />
            <el-option label="低危" value="LOW" />
            <el-option label="信息" value="INFO" />
          </el-select>
        </el-form-item>
        <el-form-item label="修复状态">
          <el-select v-model="filterForm.fixStatus" placeholder="请选择修复状态" clearable>
            <el-option label="未修复" value="UNFIXED" />
            <el-option label="已修复" value="RESOLVED" />
            <el-option label="不修复" value="WONT_FIX" />
          </el-select>
        </el-form-item>
        <el-form-item label="风险类型">
          <el-select v-model="filterForm.riskType" placeholder="请选择风险类型" clearable>
            <el-option-group label="漏洞扫描">
              <el-option label="SQL注入" value="SQL_INJECTION" />
              <el-option label="XSS攻击" value="XSS" />
              <el-option label="CSRF攻击" value="CSRF" />
              <el-option label="目录遍历" value="DIRECTORY_TRAVERSAL" />
              <el-option label="命令注入" value="COMMAND_INJECTION" />
            </el-option-group>
            <el-option-group label="配置检查">
              <el-option label="密码策略" value="PASSWORD_POLICY" />
              <el-option label="会话超时" value="SESSION_TIMEOUT" />
              <el-option label="加密配置" value="ENCRYPTION_CONFIG" />
              <el-option label="文件上传限制" value="FILE_UPLOAD_LIMIT" />
              <el-option label="CORS配置" value="CORS_CONFIG" />
            </el-option-group>
            <el-option-group label="弱密码检测">
              <el-option label="弱密码" value="WEAK_PASSWORD" />
              <el-option label="默认密码" value="DEFAULT_PASSWORD" />
              <el-option label="过期密码" value="EXPIRED_PASSWORD" />
            </el-option-group>
            <el-option-group label="异常行为检测">
              <el-option label="异常登录时间" value="ABNORMAL_LOGIN_TIME" />
              <el-option label="异常IP地址" value="ABNORMAL_IP" />
              <el-option label="高频操作" value="HIGH_FREQUENCY_OPERATION" />
              <el-option label="权限提升" value="PRIVILEGE_ESCALATION" />
              <el-option label="会话异常" value="SESSION_ANOMALY" />
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 结果列表 -->
    <el-card class="result-card">
      <el-table 
        :data="resultList" 
        v-loading="loading"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="riskLevel" label="风险级别" width="100">
          <template #default="{ row }">
            <el-tag 
              :type="getRiskLevelTagType(row.riskLevel)" 
              size="small" 
              effect="dark"
              class="risk-level-tag">
              {{ RISK_LEVEL_COLORS[row.riskLevel] }}
              {{ getRiskLevelText(row.riskLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="riskType" label="风险类型" width="140">
          <template #default="{ row }">
            <el-tag size="small" type="info">
              {{ RISK_TYPE_DESCRIPTIONS[row.riskType] || row.riskType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="riskDescription" label="风险描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="impactScope" label="影响范围" min-width="150" show-overflow-tooltip />
        <el-table-column prop="discoveredTime" label="发现时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.discoveredTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="fixStatus" label="修复状态" width="100">
          <template #default="{ row }">
            <el-tag 
              :type="getFixStatusTagType(row.fixStatus)" 
              size="small">
              {{ getFixStatusText(row.fixStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              link 
              size="small" 
              @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button 
              v-if="row.fixStatus === 'UNFIXED'"
              type="success" 
              link 
              size="small" 
              @click="handleMarkAsFixed(row)">
              <el-icon><Check /></el-icon>
              修复
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange" />
      </div>
    </el-card>

    <!-- 风险详情对话框 -->
    <RiskDetailDialog 
      v-model:visible="showDetailDialog"
      :result="currentResult" />

    <!-- 批量修复对话框 -->
    <BatchFixDialog
      v-model:visible="showBatchFixDialog"
      :selected-results="selectedResults"
      @success="handleBatchFixSuccess" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { scanResultApi, scanTaskApi, scanReportApi } from '@/api/security-scan'
import RiskDetailDialog from './components/RiskDetailDialog.vue'
import BatchFixDialog from './components/BatchFixDialog.vue'
import { RISK_LEVEL_COLORS, RISK_TYPE_DESCRIPTIONS } from '@/types/security-scan'
import type { SecurityScanResult, SecurityScanTask, RiskLevel, RiskType, FixStatus } from '@/types/security-scan'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const resultList = ref<SecurityScanResult[]>([])
const currentTask = ref<SecurityScanTask | null>(null)
const selectedResults = ref<SecurityScanResult[]>([])
const showDetailDialog = ref(false)
const showBatchFixDialog = ref(false)
const currentResult = ref<SecurityScanResult | null>(null)
const generatingReport = ref(false)

const filterForm = reactive({
  riskLevel: '',
  fixStatus: '',
  riskType: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 计算属性
const taskId = computed(() => Number(route.query.taskId))

const riskLevelCounts = computed(() => {
  const counts: Record<string, number> = {}
  resultList.value.forEach(result => {
    counts[result.riskLevel] = (counts[result.riskLevel] || 0) + 1
  })
  return counts
})

// 生命周期
onMounted(() => {
  if (taskId.value) {
    loadTaskDetail()
    loadResults()
  } else {
    ElMessage.error('缺少任务ID参数')
    router.back()
  }
})

// 方法
const loadTaskDetail = async () => {
  try {
    const response = await scanTaskApi.getTaskDetail(taskId.value)
    if (response.data.success) {
      currentTask.value = response.data.data
    }
  } catch (error) {
    console.error('加载任务详情失败:', error)
  }
}

const loadResults = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      taskId: taskId.value,
      riskLevel: filterForm.riskLevel,
      fixStatus: filterForm.fixStatus,
      riskType: filterForm.riskType
    }
    const response = await scanResultApi.getResults(params)
    
    if (response.data.success) {
      resultList.value = response.data.data.records
      pagination.total = response.data.data.total
    }
  } catch (error) {
    ElMessage.error('加载扫描结果失败')
    console.error('加载扫描结果失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadResults()
}

const handleReset = () => {
  filterForm.riskLevel = ''
  filterForm.fixStatus = ''
  filterForm.riskType = ''
  handleSearch()
}

const handleSizeChange = (val: number) => {
  pagination.size = val
  loadResults()
}

const handleCurrentChange = (val: number) => {
  pagination.current = val
  loadResults()
}

const handleSelectionChange = (val: SecurityScanResult[]) => {
  selectedResults.value = val
}

const handleViewDetail = (row: SecurityScanResult) => {
  currentResult.value = row
  showDetailDialog.value = true
}

const handleMarkAsFixed = async (row: SecurityScanResult) => {
  try {
    await ElMessageBox.prompt('请输入修复说明', '标记为已修复', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '请输入修复说明'
    })

    const response = await scanResultApi.markAsFixed(row.id, '')

    if (response.data.success) {
      ElMessage.success('标记为已修复成功')
      loadResults()
    } else {
      ElMessage.error(response.data.message || '标记失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('标记为已修复失败:', error)
      ElMessage.error('标记为已修复失败')
    }
  }
}

const handleBatchFixSuccess = () => {
  showBatchFixDialog.value = false
  loadResults()
}

const handleGenerateReport = async () => {
  try {
    generatingReport.value = true
    const response = await scanReportApi.generateReport(taskId.value)
    
    if (response.data.success) {
      ElMessage.success('扫描报告生成成功')
      // 可以在这里添加下载报告的逻辑
    } else {
      ElMessage.error(response.data.message || '生成报告失败')
    }
  } catch (error) {
    ElMessage.error('生成报告失败')
    console.error('生成报告失败:', error)
  } finally {
    generatingReport.value = false
  }
}

const goBack = () => {
  router.back()
}

// 工具函数
const getRiskLevelTagType = (riskLevel: RiskLevel) => {
  const typeMap: Record<RiskLevel, string> = {
    CRITICAL: 'danger',
    HIGH: 'danger',
    MEDIUM: 'warning',
    LOW: 'info',
    INFO: 'info'
  }
  return typeMap[riskLevel]
}

const getRiskLevelText = (riskLevel: RiskLevel) => {
  const textMap: Record<RiskLevel, string> = {
    CRITICAL: '严重',
    HIGH: '高危',
    MEDIUM: '中危',
    LOW: '低危',
    INFO: '信息'
  }
  return textMap[riskLevel]
}

const getFixStatusTagType = (fixStatus: FixStatus) => {
  const typeMap: Record<FixStatus, string> = {
    UNFIXED: 'danger',
    RESOLVED: 'success',
    WONT_FIX: 'info'
  }
  return typeMap[fixStatus]
}

const getFixStatusText = (fixStatus: FixStatus) => {
  const textMap: Record<FixStatus, string> = {
    UNFIXED: '未修复',
    RESOLVED: '已修复',
    WONT_FIX: '不修复'
  }
  return textMap[fixStatus]
}

const formatDateTime = (datetime: string) => {
  if (!datetime) return ''
  return new Date(datetime).toLocaleString('zh-CN')
}
</script>

<style scoped lang="scss">
.scan-result-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    h2 {
      margin: 0;
      color: #303133;
    }

    .task-name {
      color: #909399;
      font-size: 16px;
    }
  }

  .header-actions {
    display: flex;
    gap: 12px;
  }
}

.statistics-row {
  margin-bottom: 20px;
}

.stat-card {
  .stat-content {
    display: flex;
    align-items: center;
    padding: 10px 0;
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16px;
    font-size: 24px;
    color: white;

    &.critical {
      background: linear-gradient(135deg, #ff0000 0%, #cc0000 100%);
    }

    &.high {
      background: linear-gradient(135deg, #ff6600 0%, #cc5500 100%);
    }

    &.medium {
      background: linear-gradient(135deg, #ff9900 0%, #cc7700 100%);
    }

    &.low {
      background: linear-gradient(135deg, #ffcc00 0%, #cc9900 100%);
    }
  }

  .stat-info {
    flex: 1;
  }

  .stat-number {
    font-size: 24px;
    font-weight: bold;
    color: #303133;
    margin-bottom: 4px;
  }

  .stat-label {
    font-size: 14px;
    color: #909399;
  }
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  .el-form-item {
    margin-bottom: 0;
  }
}

.result-card {
  .risk-level-tag {
    font-weight: bold;
    
    &::before {
      content: attr(data-icon);
      margin-right: 4px;
    }
  }
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>