<template>
  <div class="scan-dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon total">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalTasks || 0 }}</div>
              <div class="stat-label">扫描任务总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon running">
              <el-icon><Loading /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.runningTasks || 0 }}</div>
              <div class="stat-label">运行中任务</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon danger">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalVulnerabilities || 0 }}</div>
              <div class="stat-label">发现漏洞总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon warning">
              <el-icon><CircleClose /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.openVulnerabilities || 0 }}</div>
              <div class="stat-label">未解决漏洞</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>漏洞严重程度分布</span>
          </template>
          <div ref="severityChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>漏洞类型分布</span>
          </template>
          <div ref="typeChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span>漏洞趋势（最近30天）</span>
          </template>
          <div ref="trendChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>最近扫描任务</span>
          </template>
          <el-table :data="recentTasks" style="width: 100%">
            <el-table-column prop="taskName" label="任务名称" />
            <el-table-column prop="scanType" label="扫描类型" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>高危漏洞</span>
          </template>
          <el-table :data="highRiskVulns" style="width: 100%">
            <el-table-column prop="vulnName" label="漏洞名称" />
            <el-table-column prop="severity" label="严重程度" width="100">
              <template #default="{ row }">
                <el-tag :type="getSeverityType(row.severity)">{{ row.severity }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag>{{ row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Document, Loading, Warning, CircleClose } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const statistics = ref<any>({})
const recentTasks = ref<any[]>([])
const highRiskVulns = ref<any[]>([])

const severityChartRef = ref<HTMLElement>()
const typeChartRef = ref<HTMLElement>()
const trendChartRef = ref<HTMLElement>()

let severityChart: echarts.ECharts | null = null
let typeChart: echarts.ECharts | null = null
let trendChart: echarts.ECharts | null = null

const loadDashboardData = async () => {
  try {
    const res = await request.get('/api/security-scan/statistics/dashboard')
    statistics.value = res.data || {}
    
    if (statistics.value.severityDistribution) {
      initSeverityChart(statistics.value.severityDistribution)
    }
  } catch (error) {
    console.error('加载仪表板数据失败:', error)
  }
}

const loadRecentTasks = async () => {
  try {
    const res = await request.get('/api/security-scan/tasks', {
      params: { current: 1, size: 5 }
    })
    recentTasks.value = res.data?.records || []
  } catch (error) {
    console.error('加载最近任务失败:', error)
  }
}

const loadHighRiskVulns = async () => {
  try {
    const res = await request.get('/api/security-scan/vulnerabilities', {
      params: { current: 1, size: 5, severity: 'HIGH' }
    })
    highRiskVulns.value = res.data?.records || []
  } catch (error) {
    console.error('加载高危漏洞失败:', error)
  }
}

const loadVulnerabilityTrend = async () => {
  try {
    const res = await request.get('/api/security-scan/statistics/trend', {
      params: { days: 30 }
    })
    initTrendChart(res.data || [])
  } catch (error) {
    console.error('加载漏洞趋势失败:', error)
  }
}

const loadTopTypes = async () => {
  try {
    const res = await request.get('/api/security-scan/statistics/top-types')
    initTypeChart(res.data || [])
  } catch (error) {
    console.error('加载漏洞类型失败:', error)
  }
}

const initSeverityChart = (data: any[]) => {
  if (!severityChartRef.value) return
  
  severityChart = echarts.init(severityChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      bottom: '0%',
      left: 'center'
    },
    series: [
      {
        name: '严重程度',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: data.map((item: any) => ({
          value: item.count,
          name: item.severity
        }))
      }
    ]
  }
  
  severityChart.setOption(option)
}

const initTypeChart = (data: any[]) => {
  if (!typeChartRef.value) return
  
  typeChart = echarts.init(typeChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: data.map((item: any) => item.vuln_type)
    },
    series: [
      {
        name: '数量',
        type: 'bar',
        data: data.map((item: any) => item.count),
        itemStyle: {
          color: '#409EFF'
        }
      }
    ]
  }
  
  typeChart.setOption(option)
}

const initTrendChart = (data: any[]) => {
  if (!trendChartRef.value) return
  
  trendChart = echarts.init(trendChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['严重', '高危', '中危', '低危']
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
      data: data.map((item: any) => item.date)
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '严重',
        type: 'line',
        data: data.map((item: any) => item.critical),
        itemStyle: { color: '#F56C6C' }
      },
      {
        name: '高危',
        type: 'line',
        data: data.map((item: any) => item.high),
        itemStyle: { color: '#E6A23C' }
      },
      {
        name: '中危',
        type: 'line',
        data: data.map((item: any) => item.medium),
        itemStyle: { color: '#409EFF' }
      },
      {
        name: '低危',
        type: 'line',
        data: data.map((item: any) => item.low),
        itemStyle: { color: '#67C23A' }
      }
    ]
  }
  
  trendChart.setOption(option)
}

const getStatusType = (status: string) => {
  const map: Record<string, any> = {
    PENDING: 'info',
    RUNNING: 'warning',
    SUCCESS: 'success',
    FAILED: 'danger',
    CANCELLED: 'info'
  }
  return map[status] || 'info'
}

const getSeverityType = (severity: string) => {
  const map: Record<string, any> = {
    CRITICAL: 'danger',
    HIGH: 'danger',
    MEDIUM: 'warning',
    LOW: 'success'
  }
  return map[severity] || 'info'
}

onMounted(() => {
  loadDashboardData()
  loadRecentTasks()
  loadHighRiskVulns()
  loadVulnerabilityTrend()
  loadTopTypes()
  
  window.addEventListener('resize', () => {
    severityChart?.resize()
    typeChart?.resize()
    trendChart?.resize()
  })
})

onUnmounted(() => {
  severityChart?.dispose()
  typeChart?.dispose()
  trendChart?.dispose()
})
</script>

<style scoped lang="less">
.scan-dashboard {
  padding: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  
  .stat-icon {
    width: 60px;
    height: 60px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    margin-right: 15px;
    
    &.total {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
    }
    
    &.running {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      color: white;
    }
    
    &.danger {
      background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
      color: white;
    }
    
    &.warning {
      background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
      color: #333;
    }
  }
  
  .stat-content {
    flex: 1;
    
    .stat-value {
      font-size: 28px;
      font-weight: bold;
      color: #303133;
    }
    
    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-top: 5px;
    }
  }
}
</style>
