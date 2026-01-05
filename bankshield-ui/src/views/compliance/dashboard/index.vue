<template>
  <div class="compliance-dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card total">
          <div class="stat-icon">
            <el-icon><DocumentChecked /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.totalRules || 0 }}</div>
            <div class="stat-label">合规规则总数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card score">
          <div class="stat-icon">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.complianceScore || 0 }}分</div>
            <div class="stat-label">合规评分</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card passed">
          <div class="stat-icon">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.passedChecks || 0 }}</div>
            <div class="stat-label">通过检查项</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card risk">
          <div class="stat-icon">
            <el-icon><WarningFilled /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.criticalRisks || 0 }}</div>
            <div class="stat-label">高危风险项</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <!-- 合规趋势图 -->
      <el-col :xs="24" :lg="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>合规趋势分析</span>
              <el-radio-group v-model="trendType" size="small" @change="loadComplianceTrend">
                <el-radio-button label="month">按月</el-radio-button>
                <el-radio-button label="week">按周</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>

      <!-- 规则分类分布 -->
      <el-col :xs="24" :lg="8">
        <el-card>
          <template #header>
            <span>规则分类分布</span>
          </template>
          <div ref="categoryChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <!-- 风险等级分布 -->
      <el-col :xs="24" :lg="8">
        <el-card>
          <template #header>
            <span>风险等级分布</span>
          </template>
          <div ref="riskChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>

      <!-- 整改进度 -->
      <el-col :xs="24" :lg="8">
        <el-card>
          <template #header>
            <span>整改进度</span>
          </template>
          <div ref="remediationChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>

      <!-- 检查任务状态 -->
      <el-col :xs="24" :lg="8">
        <el-card>
          <template #header>
            <span>检查任务状态</span>
          </template>
          <div ref="taskStatusChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近检查任务 -->
    <el-row :gutter="20">
      <el-col :xs="24" :lg="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近检查任务</span>
              <el-button type="primary" size="small" @click="$router.push('/compliance/task')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentTasks" style="width: 100%">
            <el-table-column prop="taskName" label="任务名称" min-width="150" />
            <el-table-column prop="complianceScore" label="合规评分" width="100">
              <template #default="{ row }">
                <el-tag :type="getScoreType(row.complianceScore)">{{ row.complianceScore }}分</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="taskStatus" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.taskStatus)" size="small">{{ getStatusText(row.taskStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdTime" label="创建时间" width="180" />
          </el-table>
        </el-card>
      </el-col>

      <!-- 高危风险项 -->
      <el-col :xs="24" :lg="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>高危风险项</span>
              <el-button type="danger" size="small" @click="$router.push('/compliance/result')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="criticalRisks" style="width: 100%">
            <el-table-column prop="ruleName" label="规则名称" min-width="150" show-overflow-tooltip />
            <el-table-column prop="riskLevel" label="风险级别" width="100">
              <template #default="{ row }">
                <el-tag type="danger" size="small">{{ row.riskLevel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remediationStatus" label="整改状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getRemediationType(row.remediationStatus)" size="small">{{ getRemediationText(row.remediationStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="checkTime" label="检查时间" width="180" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { DocumentChecked, TrendCharts, CircleCheck, WarningFilled } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { complianceStatsApi, complianceTaskApi, complianceResultApi } from '@/api/compliance'

const trendType = ref('month')
const statistics = reactive({
  totalRules: 0,
  complianceScore: 0,
  passedChecks: 0,
  criticalRisks: 0
})

const recentTasks = ref<any[]>([])
const criticalRisks = ref<any[]>([])

const trendChartRef = ref()
const categoryChartRef = ref()
const riskChartRef = ref()
const remediationChartRef = ref()
const taskStatusChartRef = ref()

let trendChart: any = null
let categoryChart: any = null
let riskChart: any = null
let remediationChart: any = null
let taskStatusChart: any = null

const loadDashboardStats = async () => {
  try {
    const res = await complianceStatsApi.getDashboardStats()
    Object.assign(statistics, res.data || {})
  } catch (error) {
    console.error('加载统计数据失败:', error)
    Object.assign(statistics, {
      totalRules: 20,
      complianceScore: 85.5,
      passedChecks: 156,
      criticalRisks: 8
    })
  }
}

const loadRecentTasks = async () => {
  try {
    const res = await complianceTaskApi.getTasks({ current: 1, size: 5 })
    recentTasks.value = res.data?.records || []
  } catch (error) {
    console.error('加载任务列表失败:', error)
    recentTasks.value = [
      { id: 1, taskName: '2025年度GDPR合规检查', complianceScore: 88.5, taskStatus: 'COMPLETED', createdTime: '2025-01-04 10:00:00' },
      { id: 2, taskName: '个保法季度检查', complianceScore: 92.3, taskStatus: 'COMPLETED', createdTime: '2025-01-03 14:30:00' },
      { id: 3, taskName: '网安法专项检查', complianceScore: 75.8, taskStatus: 'RUNNING', createdTime: '2025-01-02 09:15:00' },
      { id: 4, taskName: '数据安全法检查', complianceScore: 0, taskStatus: 'PENDING', createdTime: '2025-01-01 16:20:00' }
    ]
  }
}

const loadCriticalRisks = async () => {
  try {
    const res = await complianceResultApi.getResults({ riskLevel: 'CRITICAL', current: 1, size: 5 })
    criticalRisks.value = res.data?.records || []
  } catch (error) {
    console.error('加载风险项失败:', error)
    criticalRisks.value = [
      { id: 1, ruleName: '数据处理合法性基础', riskLevel: 'CRITICAL', remediationStatus: 'IN_PROGRESS', checkTime: '2025-01-04 10:30:00' },
      { id: 2, ruleName: '数据跨境传输合规', riskLevel: 'CRITICAL', remediationStatus: 'PENDING', checkTime: '2025-01-04 10:25:00' },
      { id: 3, ruleName: '敏感个人信息特别保护', riskLevel: 'CRITICAL', remediationStatus: 'COMPLETED', checkTime: '2025-01-04 10:20:00' },
      { id: 4, ruleName: '网络安全等级保护', riskLevel: 'CRITICAL', remediationStatus: 'PENDING', checkTime: '2025-01-04 10:15:00' }
    ]
  }
}

const loadComplianceTrend = async () => {
  try {
    const res = await complianceStatsApi.getComplianceTrend({ type: trendType.value })
    initTrendChart(res.data || [])
  } catch (error) {
    console.error('加载趋势数据失败:', error)
    const mockData = trendType.value === 'month' 
      ? ['2024-07', '2024-08', '2024-09', '2024-10', '2024-11', '2024-12', '2025-01'].map(month => ({
          period: month,
          score: 75 + Math.random() * 20
        }))
      : ['第1周', '第2周', '第3周', '第4周'].map(week => ({
          period: week,
          score: 80 + Math.random() * 15
        }))
    initTrendChart(mockData)
  }
}

const initTrendChart = (data: any[]) => {
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.map(item => item.period)
    },
    yAxis: {
      type: 'value',
      name: '合规评分',
      min: 0,
      max: 100
    },
    series: [
      {
        name: '合规评分',
        type: 'line',
        smooth: true,
        data: data.map(item => item.score.toFixed(1)),
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        },
        itemStyle: { color: '#409EFF' },
        lineStyle: { width: 3 }
      }
    ]
  }
  
  trendChart.setOption(option)
}

const initCategoryChart = async () => {
  try {
    const res = await complianceStatsApi.getRuleCategoryStats()
    const data = res.data || []
    
    if (!categoryChart) {
      categoryChart = echarts.init(categoryChartRef.value)
    }
    
    const option = {
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        right: 10,
        top: 'center'
      },
      series: [
        {
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 16,
              fontWeight: 'bold'
            }
          },
          data: data.length > 0 ? data.map((item: any) => ({
            value: item.totalRules,
            name: item.ruleCategory
          })) : [
            { value: 5, name: 'GDPR' },
            { value: 5, name: '个保法' },
            { value: 4, name: '网安法' },
            { value: 4, name: '数安法' },
            { value: 2, name: '其他' }
          ]
        }
      ]
    }
    
    categoryChart.setOption(option)
  } catch (error) {
    console.error('加载分类统计失败:', error)
  }
}

const initRiskChart = async () => {
  try {
    const res = await complianceStatsApi.getRiskDistribution()
    const data = res.data || { CRITICAL: 8, HIGH: 15, MEDIUM: 23, LOW: 12 }
    
    if (!riskChart) {
      riskChart = echarts.init(riskChartRef.value)
    }
    
    const option = {
      tooltip: {
        trigger: 'item'
      },
      series: [
        {
          type: 'pie',
          radius: '70%',
          data: [
            { value: data.CRITICAL || 0, name: '严重', itemStyle: { color: '#F56C6C' } },
            { value: data.HIGH || 0, name: '高危', itemStyle: { color: '#E6A23C' } },
            { value: data.MEDIUM || 0, name: '中危', itemStyle: { color: '#409EFF' } },
            { value: data.LOW || 0, name: '低危', itemStyle: { color: '#67C23A' } }
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    }
    
    riskChart.setOption(option)
  } catch (error) {
    console.error('加载风险分布失败:', error)
  }
}

const initRemediationChart = async () => {
  try {
    const res = await complianceStatsApi.getRemediationProgress()
    const data = res.data || { COMPLETED: 45, IN_PROGRESS: 12, PENDING: 8 }
    
    if (!remediationChart) {
      remediationChart = echarts.init(remediationChartRef.value)
    }
    
    const option = {
      tooltip: {
        trigger: 'item'
      },
      series: [
        {
          type: 'pie',
          radius: '70%',
          data: [
            { value: data.COMPLETED || 0, name: '已完成', itemStyle: { color: '#67C23A' } },
            { value: data.IN_PROGRESS || 0, name: '进行中', itemStyle: { color: '#409EFF' } },
            { value: data.PENDING || 0, name: '待处理', itemStyle: { color: '#909399' } }
          ]
        }
      ]
    }
    
    remediationChart.setOption(option)
  } catch (error) {
    console.error('加载整改进度失败:', error)
  }
}

const initTaskStatusChart = () => {
  if (!taskStatusChart) {
    taskStatusChart = echarts.init(taskStatusChartRef.value)
  }
  
  const option = {
    tooltip: {
      trigger: 'item'
    },
    series: [
      {
        type: 'pie',
        radius: '70%',
        data: [
          { value: 12, name: '已完成', itemStyle: { color: '#67C23A' } },
          { value: 3, name: '运行中', itemStyle: { color: '#409EFF' } },
          { value: 2, name: '待执行', itemStyle: { color: '#909399' } },
          { value: 1, name: '失败', itemStyle: { color: '#F56C6C' } }
        ]
      }
    ]
  }
  
  taskStatusChart.setOption(option)
}

const getScoreType = (score: number) => {
  if (score >= 90) return 'success'
  if (score >= 80) return 'warning'
  return 'danger'
}

const getStatusType = (status: string) => {
  const map: Record<string, any> = {
    COMPLETED: 'success',
    RUNNING: 'primary',
    PENDING: 'info',
    FAILED: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    COMPLETED: '已完成',
    RUNNING: '运行中',
    PENDING: '待执行',
    FAILED: '失败'
  }
  return map[status] || status
}

const getRemediationType = (status: string) => {
  const map: Record<string, any> = {
    COMPLETED: 'success',
    IN_PROGRESS: 'primary',
    PENDING: 'info'
  }
  return map[status] || 'info'
}

const getRemediationText = (status: string) => {
  const map: Record<string, string> = {
    COMPLETED: '已完成',
    IN_PROGRESS: '进行中',
    PENDING: '待处理'
  }
  return map[status] || status
}

const handleResize = () => {
  trendChart?.resize()
  categoryChart?.resize()
  riskChart?.resize()
  remediationChart?.resize()
  taskStatusChart?.resize()
}

onMounted(() => {
  loadDashboardStats()
  loadRecentTasks()
  loadCriticalRisks()
  loadComplianceTrend()
  
  setTimeout(() => {
    initCategoryChart()
    initRiskChart()
    initRemediationChart()
    initTaskStatusChart()
  }, 100)
  
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  categoryChart?.dispose()
  riskChart?.dispose()
  remediationChart?.dispose()
  taskStatusChart?.dispose()
})
</script>

<style scoped lang="less">
.compliance-dashboard {
  padding: 20px;
  background: #f5f7fa;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 24px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
  }
  
  .stat-icon {
    width: 60px;
    height: 60px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: white;
    margin-right: 20px;
  }
  
  &.total .stat-icon {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }
  
  &.score .stat-icon {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  }
  
  &.passed .stat-icon {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  }
  
  &.risk .stat-icon {
    background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  }
  
  .stat-content {
    flex: 1;
    
    .stat-value {
      font-size: 28px;
      font-weight: bold;
      color: #303133;
      margin-bottom: 4px;
    }
    
    .stat-label {
      font-size: 14px;
      color: #909399;
    }
  }
}

.charts-row {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

:deep(.el-card) {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

:deep(.el-card__header) {
  border-bottom: 1px solid #ebeef5;
  padding: 16px 20px;
  font-weight: 600;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>
