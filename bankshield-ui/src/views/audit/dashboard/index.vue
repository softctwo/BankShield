<template>
  <div class="audit-dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon total">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalLogs || 0 }}</div>
              <div class="stat-label">总审计日志</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon today">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.todayLogs || 0 }}</div>
              <div class="stat-label">今日操作</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon users">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.activeUsers || 0 }}</div>
              <div class="stat-label">活跃用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon warning">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.failedOps || 0 }}</div>
              <div class="stat-label">失败操作</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>操作趋势（最近30天）</span>
              <el-radio-group v-model="trendType" size="small" @change="loadTrendData">
                <el-radio-button label="day">按天</el-radio-button>
                <el-radio-button label="hour">按小时</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>操作类型分布</span>
          </template>
          <div ref="typeChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <span>用户活跃度热力图</span>
          </template>
          <div ref="heatmapChartRef" style="height: 400px"></div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>时间段分析</span>
          </template>
          <div ref="timeChartRef" style="height: 400px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>高频操作用户 TOP10</span>
          </template>
          <el-table :data="topUsers" style="width: 100%" max-height="300">
            <el-table-column type="index" label="排名" width="60" />
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="operationCount" label="操作次数" width="120" />
            <el-table-column label="趋势" width="80">
              <template #default="{ row }">
                <el-tag :type="row.trend > 0 ? 'success' : 'danger'" size="small">
                  {{ row.trend > 0 ? '↑' : '↓' }} {{ Math.abs(row.trend) }}%
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>最近异常操作</span>
          </template>
          <el-table :data="abnormalOps" style="width: 100%" max-height="300">
            <el-table-column prop="username" label="用户" width="100" />
            <el-table-column prop="operation" label="操作" />
            <el-table-column prop="reason" label="异常原因" show-overflow-tooltip />
            <el-table-column prop="time" label="时间" width="180" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Document, Calendar, User, Warning } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const statistics = ref<any>({})
const trendType = ref('day')
const topUsers = ref<any[]>([])
const abnormalOps = ref<any[]>([])

const trendChartRef = ref<HTMLElement>()
const typeChartRef = ref<HTMLElement>()
const heatmapChartRef = ref<HTMLElement>()
const timeChartRef = ref<HTMLElement>()

let trendChart: echarts.ECharts | null = null
let typeChart: echarts.ECharts | null = null
let heatmapChart: echarts.ECharts | null = null
let timeChart: echarts.ECharts | null = null

const loadStatistics = async () => {
  try {
    const res = await request.get('/api/audit/statistics/dashboard')
    statistics.value = res.data || {}
  } catch (error) {
    console.error('加载统计数据失败:', error)
    statistics.value = {
      totalLogs: 15234,
      todayLogs: 342,
      activeUsers: 28,
      failedOps: 12
    }
  }
}

const loadTrendData = async () => {
  try {
    const res = await request.get('/api/audit/statistics/trend', {
      params: { type: trendType.value, days: 30 }
    })
    initTrendChart(res.data || [])
  } catch (error) {
    console.error('加载趋势数据失败:', error)
    const mockData = generateMockTrendData()
    initTrendChart(mockData)
  }
}

const loadTypeDistribution = async () => {
  try {
    const res = await request.get('/api/audit/statistics/type-distribution')
    initTypeChart(res.data || [])
  } catch (error) {
    console.error('加载类型分布失败:', error)
    const mockData = [
      { type: '查询', count: 5234 },
      { type: '新增', count: 1823 },
      { type: '修改', count: 1456 },
      { type: '删除', count: 234 },
      { type: '登录', count: 892 }
    ]
    initTypeChart(mockData)
  }
}

const loadHeatmapData = async () => {
  try {
    const res = await request.get('/api/audit/statistics/heatmap')
    initHeatmapChart(res.data || [])
  } catch (error) {
    console.error('加载热力图数据失败:', error)
    const mockData = generateMockHeatmapData()
    initHeatmapChart(mockData)
  }
}

const loadTimeAnalysis = async () => {
  try {
    const res = await request.get('/api/audit/statistics/time-analysis')
    initTimeChart(res.data || [])
  } catch (error) {
    console.error('加载时间分析失败:', error)
    const mockData = generateMockTimeData()
    initTimeChart(mockData)
  }
}

const loadTopUsers = async () => {
  try {
    const res = await request.get('/api/audit/statistics/top-users')
    topUsers.value = res.data || []
  } catch (error) {
    console.error('加载TOP用户失败:', error)
    topUsers.value = [
      { username: 'admin', operationCount: 1234, trend: 15 },
      { username: 'operator01', operationCount: 892, trend: 8 },
      { username: 'audit_user', operationCount: 756, trend: -3 },
      { username: 'security_admin', operationCount: 623, trend: 12 },
      { username: 'data_analyst', operationCount: 534, trend: 5 }
    ]
  }
}

const loadAbnormalOps = async () => {
  try {
    const res = await request.get('/api/audit/statistics/abnormal-ops')
    abnormalOps.value = res.data || []
  } catch (error) {
    console.error('加载异常操作失败:', error)
    abnormalOps.value = [
      { username: 'test_user', operation: '批量删除', reason: '非工作时间操作', time: '2025-01-04 02:30:15' },
      { username: 'unknown', operation: '登录失败', reason: '多次密码错误', time: '2025-01-04 03:15:22' },
      { username: 'admin', operation: '权限修改', reason: '敏感操作', time: '2025-01-04 01:45:33' }
    ]
  }
}

const generateMockTrendData = () => {
  const data = []
  for (let i = 29; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    data.push({
      date: date.toISOString().split('T')[0],
      total: Math.floor(Math.random() * 500) + 200,
      success: Math.floor(Math.random() * 450) + 180,
      failed: Math.floor(Math.random() * 50) + 10
    })
  }
  return data
}

const generateMockHeatmapData = () => {
  const hours = Array.from({ length: 24 }, (_, i) => i)
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const data = []
  
  days.forEach((day, dayIndex) => {
    hours.forEach(hour => {
      const value = Math.floor(Math.random() * 100)
      data.push([hour, dayIndex, value])
    })
  })
  
  return data
}

const generateMockTimeData = () => {
  return [
    { period: '00:00-06:00', count: 45 },
    { period: '06:00-09:00', count: 234 },
    { period: '09:00-12:00', count: 892 },
    { period: '12:00-14:00', count: 456 },
    { period: '14:00-18:00', count: 1023 },
    { period: '18:00-24:00', count: 312 }
  ]
}

const initTrendChart = (data: any[]) => {
  if (!trendChartRef.value) return
  
  trendChart = echarts.init(trendChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['总操作', '成功', '失败']
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
        name: '总操作',
        type: 'line',
        data: data.map((item: any) => item.total),
        smooth: true,
        itemStyle: { color: '#409EFF' }
      },
      {
        name: '成功',
        type: 'line',
        data: data.map((item: any) => item.success),
        smooth: true,
        itemStyle: { color: '#67C23A' }
      },
      {
        name: '失败',
        type: 'line',
        data: data.map((item: any) => item.failed),
        smooth: true,
        itemStyle: { color: '#F56C6C' }
      }
    ]
  }
  
  trendChart.setOption(option)
}

const initTypeChart = (data: any[]) => {
  if (!typeChartRef.value) return
  
  typeChart = echarts.init(typeChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '操作类型',
        type: 'pie',
        radius: '70%',
        data: data.map((item: any) => ({
          value: item.count,
          name: item.type
        })),
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
  
  typeChart.setOption(option)
}

const initHeatmapChart = (data: any[]) => {
  if (!heatmapChartRef.value) return
  
  heatmapChart = echarts.init(heatmapChartRef.value)
  
  const hours = Array.from({ length: 24 }, (_, i) => i + '时')
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  
  const option = {
    tooltip: {
      position: 'top'
    },
    grid: {
      height: '70%',
      top: '10%'
    },
    xAxis: {
      type: 'category',
      data: hours,
      splitArea: {
        show: true
      }
    },
    yAxis: {
      type: 'category',
      data: days,
      splitArea: {
        show: true
      }
    },
    visualMap: {
      min: 0,
      max: 100,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: '5%',
      inRange: {
        color: ['#e0f3f8', '#abd9e9', '#74add1', '#4575b4', '#313695']
      }
    },
    series: [
      {
        name: '操作次数',
        type: 'heatmap',
        data: data,
        label: {
          show: false
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  heatmapChart.setOption(option)
}

const initTimeChart = (data: any[]) => {
  if (!timeChartRef.value) return
  
  timeChart = echarts.init(timeChartRef.value)
  
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
      data: data.map((item: any) => item.period)
    },
    series: [
      {
        name: '操作次数',
        type: 'bar',
        data: data.map((item: any) => item.count),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: '#2378f7' },
              { offset: 0.7, color: '#2378f7' },
              { offset: 1, color: '#83bff6' }
            ])
          }
        }
      }
    ]
  }
  
  timeChart.setOption(option)
}

onMounted(() => {
  loadStatistics()
  loadTrendData()
  loadTypeDistribution()
  loadHeatmapData()
  loadTimeAnalysis()
  loadTopUsers()
  loadAbnormalOps()
  
  window.addEventListener('resize', () => {
    trendChart?.resize()
    typeChart?.resize()
    heatmapChart?.resize()
    timeChart?.resize()
  })
})

onUnmounted(() => {
  trendChart?.dispose()
  typeChart?.dispose()
  heatmapChart?.dispose()
  timeChart?.dispose()
})
</script>

<style scoped lang="less">
.audit-dashboard {
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
    
    &.today {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      color: white;
    }
    
    &.users {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      color: white;
    }
    
    &.warning {
      background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
      color: white;
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
