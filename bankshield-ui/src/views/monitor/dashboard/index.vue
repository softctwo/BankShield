<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-900">监控中心</h1>
      <p class="mt-1 text-sm text-gray-500">实时监控系统运行状态和安全告警</p>
    </div>

    <!-- 实时状态卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0 h-12 w-12 bg-green-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-2xl text-green-600"><CircleCheck /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">系统状态</p>
            <p class="text-2xl font-bold text-green-600">正常</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0 h-12 w-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-2xl text-blue-600"><User /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">在线用户</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.onlineUsers }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0 h-12 w-12 bg-yellow-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-2xl text-yellow-600"><Warning /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">待处理告警</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.pendingAlerts }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0 h-12 w-12 bg-red-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-2xl text-red-600"><Warning /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">今日告警</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.todayAlerts }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 告警记录 -->
    <div class="bg-white rounded-lg shadow mb-6">
      <div class="px-6 py-4 border-b border-gray-200">
        <h2 class="text-lg font-medium text-gray-900">实时告警</h2>
      </div>
      <div class="overflow-hidden">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">告警时间</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">告警类型</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">告警内容</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">严重程度</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
              <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">操作</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="alert in alertList" :key="alert.id" class="hover:bg-gray-50">
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ alert.time }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ alert.type }}</td>
              <td class="px-6 py-4 text-sm text-gray-500">{{ alert.content }}</td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium" :class="getSeverityClass(alert.severity)">
                  {{ getSeverityName(alert.severity) }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium" :class="alert.status === 'pending' ? 'bg-yellow-100 text-yellow-800' : 'bg-green-100 text-green-800'">
                  {{ alert.status === 'pending' ? '待处理' : '已处理' }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                <button v-if="alert.status === 'pending'" @click="handleProcess(alert)" class="text-blue-600 hover:text-blue-900 mr-3">处理</button>
                <button @click="handleView(alert)" class="text-gray-600 hover:text-gray-900">查看</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 监控图表 -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-medium text-gray-900">实时系统监控</h3>
          <button @click="handleExportMonitorChart" class="text-sm text-blue-600 hover:text-blue-700">
            <el-icon class="mr-1"><Download /></el-icon>
            导出
          </button>
        </div>
        <div ref="monitorChartRef" style="height: 300px;"></div>
      </div>

      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-medium text-gray-900">告警趋势分析</h3>
          <button @click="handleExportAlertChart" class="text-sm text-blue-600 hover:text-blue-700">
            <el-icon class="mr-1"><Download /></el-icon>
            导出
          </button>
        </div>
        <div ref="alertChartRef" style="height: 300px;"></div>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-medium text-gray-900">网络流量监控</h3>
          <button @click="handleExportTrafficChart" class="text-sm text-blue-600 hover:text-blue-700">
            <el-icon class="mr-1"><Download /></el-icon>
            导出
          </button>
        </div>
        <div ref="trafficChartRef" style="height: 300px;"></div>
      </div>

      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-medium text-gray-900">安全评分</h3>
        </div>
        <div ref="scoreGaugeRef" style="height: 300px;"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { CircleCheck, User, Warning, Download } from '@element-plus/icons-vue'

// ECharts实例引用
const monitorChartRef = ref<HTMLElement>()
const alertChartRef = ref<HTMLElement>()
const trafficChartRef = ref<HTMLElement>()
const scoreGaugeRef = ref<HTMLElement>()

let monitorChart: echarts.ECharts | null = null
let alertChart: echarts.ECharts | null = null
let trafficChart: echarts.ECharts | null = null
let scoreGauge: echarts.ECharts | null = null

const stats = reactive({
  onlineUsers: 45,
  pendingAlerts: 8,
  todayAlerts: 23
})

const metrics = reactive({
  cpu: 45,
  memory: 62,
  disk: 38
})

const alertList = ref([
  { id: 1, time: '2024-12-30 12:45:00', type: '登录异常', content: '检测到异常登录尝试，IP: 192.168.1.200', severity: 'high', status: 'pending' },
  { id: 2, time: '2024-12-30 12:40:00', type: '数据访问', content: '敏感数据访问频率异常', severity: 'medium', status: 'pending' },
  { id: 3, time: '2024-12-30 12:35:00', type: '系统告警', content: 'CPU使用率超过阈值', severity: 'low', status: 'processed' },
  { id: 4, time: '2024-12-30 12:30:00', type: '权限变更', content: '用户权限被修改', severity: 'medium', status: 'processed' }
])

const handleProcess = (alert: any) => {
  alert.status = 'processed'
  ElMessage.success('告警已处理')
}

const handleView = (alert: any) => {
  ElMessage.info(`查看告警详情: ${alert.content}`)
}

const getSeverityName = (severity: string) => {
  const map: Record<string, string> = {
    high: '高危',
    medium: '中危',
    low: '低危'
  }
  return map[severity] || severity
}

const getSeverityClass = (severity: string) => {
  const map: Record<string, string> = {
    high: 'bg-red-100 text-red-800',
    medium: 'bg-yellow-100 text-yellow-800',
    low: 'bg-blue-100 text-blue-800'
  }
  return map[severity] || 'bg-gray-100 text-gray-800'
}

// 初始化实时系统监控图表
const initMonitorChart = () => {
  if (!monitorChartRef.value) return
  monitorChart = echarts.init(monitorChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['CPU', '内存', '磁盘'],
      bottom: '0%'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '12%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['10分钟前', '8分钟前', '6分钟前', '4分钟前', '2分钟前', '现在']
    },
    yAxis: {
      type: 'value',
      max: 100,
      axisLabel: {
        formatter: '{value}%'
      }
    },
    series: [
      {
        name: 'CPU',
        type: 'line',
        smooth: true,
        data: [42, 45, 48, 46, 44, 45],
        itemStyle: { color: '#2563eb' }
      },
      {
        name: '内存',
        type: 'line',
        smooth: true,
        data: [58, 60, 62, 61, 63, 62],
        itemStyle: { color: '#16a34a' }
      },
      {
        name: '磁盘',
        type: 'line',
        smooth: true,
        data: [36, 37, 38, 38, 37, 38],
        itemStyle: { color: '#ca8a04' }
      }
    ]
  }
  
  monitorChart.setOption(option)
}

// 初始化告警趋势图表
const initAlertChart = () => {
  if (!alertChartRef.value) return
  alertChart = echarts.init(alertChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['高危', '中危', '低危'],
      bottom: '0%'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '12%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '高危',
        type: 'bar',
        stack: 'total',
        data: [5, 8, 6, 7, 9, 4, 5],
        itemStyle: { color: '#dc2626' }
      },
      {
        name: '中危',
        type: 'bar',
        stack: 'total',
        data: [8, 10, 12, 9, 11, 7, 8],
        itemStyle: { color: '#ca8a04' }
      },
      {
        name: '低危',
        type: 'bar',
        stack: 'total',
        data: [12, 15, 14, 16, 13, 10, 12],
        itemStyle: { color: '#2563eb' }
      }
    ]
  }
  
  alertChart.setOption(option)
}

// 初始化网络流量图表
const initTrafficChart = () => {
  if (!trafficChartRef.value) return
  trafficChart = echarts.init(trafficChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['入站流量', '出站流量'],
      bottom: '0%'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '12%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00']
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '{value} MB/s'
      }
    },
    series: [
      {
        name: '入站流量',
        type: 'line',
        smooth: true,
        data: [120, 132, 101, 134, 90, 230, 210],
        itemStyle: { color: '#2563eb' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(37, 99, 235, 0.3)' },
            { offset: 1, color: 'rgba(37, 99, 235, 0.05)' }
          ])
        }
      },
      {
        name: '出站流量',
        type: 'line',
        smooth: true,
        data: [220, 182, 191, 234, 290, 330, 310],
        itemStyle: { color: '#16a34a' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(22, 163, 74, 0.3)' },
            { offset: 1, color: 'rgba(22, 163, 74, 0.05)' }
          ])
        }
      }
    ]
  }
  
  trafficChart.setOption(option)
}

// 初始化安全评分仪表盘
const initScoreGauge = () => {
  if (!scoreGaugeRef.value) return
  scoreGauge = echarts.init(scoreGaugeRef.value)
  
  const option = {
    series: [
      {
        type: 'gauge',
        startAngle: 180,
        endAngle: 0,
        min: 0,
        max: 100,
        splitNumber: 10,
        axisLine: {
          lineStyle: {
            width: 20,
            color: [
              [0.3, '#dc2626'],
              [0.7, '#ca8a04'],
              [1, '#16a34a']
            ]
          }
        },
        pointer: {
          itemStyle: {
            color: 'auto'
          }
        },
        axisTick: {
          distance: -20,
          length: 8,
          lineStyle: {
            color: '#fff',
            width: 2
          }
        },
        splitLine: {
          distance: -20,
          length: 20,
          lineStyle: {
            color: '#fff',
            width: 3
          }
        },
        axisLabel: {
          color: 'auto',
          distance: 30,
          fontSize: 14
        },
        detail: {
          valueAnimation: true,
          formatter: '{value}分',
          color: 'auto',
          fontSize: 30,
          offsetCenter: [0, '70%']
        },
        title: {
          offsetCenter: [0, '90%'],
          fontSize: 16
        },
        data: [
          {
            value: 85,
            name: '安全评分'
          }
        ]
      }
    ]
  }
  
  scoreGauge.setOption(option)
}

// 图表导出功能
const handleExportMonitorChart = () => {
  if (monitorChart) {
    const url = monitorChart.getDataURL({
      type: 'png',
      pixelRatio: 2,
      backgroundColor: '#fff'
    })
    const link = document.createElement('a')
    link.href = url
    link.download = `实时系统监控_${new Date().getTime()}.png`
    link.click()
    ElMessage.success('图表导出成功')
  }
}

const handleExportAlertChart = () => {
  if (alertChart) {
    const url = alertChart.getDataURL({
      type: 'png',
      pixelRatio: 2,
      backgroundColor: '#fff'
    })
    const link = document.createElement('a')
    link.href = url
    link.download = `告警趋势分析_${new Date().getTime()}.png`
    link.click()
    ElMessage.success('图表导出成功')
  }
}

const handleExportTrafficChart = () => {
  if (trafficChart) {
    const url = trafficChart.getDataURL({
      type: 'png',
      pixelRatio: 2,
      backgroundColor: '#fff'
    })
    const link = document.createElement('a')
    link.href = url
    link.download = `网络流量监控_${new Date().getTime()}.png`
    link.click()
    ElMessage.success('图表导出成功')
  }
}

// 初始化所有图表
const initCharts = () => {
  initMonitorChart()
  initAlertChart()
  initTrafficChart()
  initScoreGauge()
}

// 窗口大小改变时重新调整图表大小
const handleResize = () => {
  monitorChart?.resize()
  alertChart?.resize()
  trafficChart?.resize()
  scoreGauge?.resize()
}

// 生命周期钩子
onMounted(() => {
  initCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  monitorChart?.dispose()
  alertChart?.dispose()
  trafficChart?.dispose()
  scoreGauge?.dispose()
})
</script>
