<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-900">AI智能分析</h1>
      <p class="mt-1 text-sm text-gray-500">基于机器学习的异常行为检测和智能安全分析</p>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0 h-12 w-12 bg-red-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-2xl text-red-600"><Warning /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">异常行为</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.anomalyCount }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0 h-12 w-12 bg-yellow-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-2xl text-yellow-600"><Bell /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">智能告警</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.alertCount }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0 h-12 w-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-2xl text-blue-600"><TrendCharts /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">预测准确率</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.accuracy }}%</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0 h-12 w-12 bg-green-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-2xl text-green-600"><CircleCheck /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">模型状态</p>
            <p class="text-2xl font-bold text-green-600">运行中</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 异常行为检测 -->
    <div class="bg-white rounded-lg shadow mb-6">
      <div class="px-6 py-4 border-b border-gray-200">
        <h2 class="text-lg font-medium text-gray-900">异常行为检测</h2>
      </div>
      <div class="overflow-hidden">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">检测时间</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">异常类型</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">用户</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">行为描述</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">风险等级</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">置信度</th>
              <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">操作</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="anomaly in anomalyList" :key="anomaly.id" class="hover:bg-gray-50">
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ anomaly.time }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ anomaly.type }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ anomaly.user }}</td>
              <td class="px-6 py-4 text-sm text-gray-500">{{ anomaly.description }}</td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium" :class="getRiskClass(anomaly.risk)">
                  {{ getRiskName(anomaly.risk) }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ anomaly.confidence }}%</td>
              <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                <button @click="handleAnalyze(anomaly)" class="text-blue-600 hover:text-blue-900 mr-3">分析</button>
                <button @click="handleBlock(anomaly)" class="text-red-600 hover:text-red-900">阻止</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 威胁预测和模型性能 -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-medium text-gray-900">威胁趋势预测</h3>
          <button @click="handleExportThreatChart" class="text-sm text-blue-600 hover:text-blue-700">
            <el-icon class="mr-1"><Download /></el-icon>
            导出
          </button>
        </div>
        <div ref="threatChartRef" style="height: 300px;"></div>
      </div>

      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-medium text-gray-900">模型性能指标</h3>
          <button @click="handleExportModelChart" class="text-sm text-blue-600 hover:text-blue-700">
            <el-icon class="mr-1"><Download /></el-icon>
            导出
          </button>
        </div>
        <div ref="modelChartRef" style="height: 300px;"></div>
      </div>
    </div>

    <!-- 异常行为热力图 -->
    <div class="bg-white rounded-lg shadow p-6">
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-lg font-medium text-gray-900">异常行为时间分布热力图</h3>
        <button @click="handleExportHeatmapChart" class="text-sm text-blue-600 hover:text-blue-700">
          <el-icon class="mr-1"><Download /></el-icon>
          导出
        </button>
      </div>
      <div ref="heatmapChartRef" style="height: 400px;"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { Warning, Bell, TrendCharts, CircleCheck, Download } from '@element-plus/icons-vue'

// ECharts实例引用
const threatChartRef = ref<HTMLElement>()
const modelChartRef = ref<HTMLElement>()
const heatmapChartRef = ref<HTMLElement>()

let threatChart: echarts.ECharts | null = null
let modelChart: echarts.ECharts | null = null
let heatmapChart: echarts.ECharts | null = null

const stats = reactive({
  anomalyCount: 23,
  alertCount: 15,
  accuracy: 94.5
})

const modelMetrics = reactive({
  accuracy: 94.5,
  precision: 92.3,
  recall: 96.1,
  f1Score: 94.2
})

const anomalyList = ref([
  { id: 1, time: '2024-12-30 12:45:00', type: '异常登录', user: 'user001', description: '非工作时间多次登录尝试', risk: 'high', confidence: 95 },
  { id: 2, time: '2024-12-30 12:40:00', type: '数据访问异常', user: 'user002', description: '短时间内访问大量敏感数据', risk: 'high', confidence: 88 },
  { id: 3, time: '2024-12-30 12:35:00', type: '权限滥用', user: 'user003', description: '访问超出权限范围的数据', risk: 'medium', confidence: 76 },
  { id: 4, time: '2024-12-30 12:30:00', type: '异常操作', user: 'user004', description: '批量删除操作', risk: 'medium', confidence: 82 }
])

const threatPredictions = ref([
  { type: 'SQL注入攻击', probability: 78 },
  { type: 'DDoS攻击', probability: 45 },
  { type: '数据泄露', probability: 62 },
  { type: '权限提升', probability: 35 }
])

const handleAnalyze = (anomaly: any) => {
  ElMessage.info(`分析异常行为: ${anomaly.description}`)
}

const handleBlock = (anomaly: any) => {
  ElMessage.success(`已阻止用户: ${anomaly.user}`)
}

const getRiskName = (risk: string) => {
  const map: Record<string, string> = {
    high: '高危',
    medium: '中危',
    low: '低危'
  }
  return map[risk] || risk
}

const getRiskClass = (risk: string) => {
  const map: Record<string, string> = {
    high: 'bg-red-100 text-red-800',
    medium: 'bg-yellow-100 text-yellow-800',
    low: 'bg-blue-100 text-blue-800'
  }
  return map[risk] || 'bg-gray-100 text-gray-800'
}

// 初始化威胁趋势预测图表
const initThreatChart = () => {
  if (!threatChartRef.value) return
  threatChart = echarts.init(threatChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['SQL注入', 'DDoS攻击', '数据泄露', '权限提升'],
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
      max: 100,
      axisLabel: {
        formatter: '{value}%'
      }
    },
    series: [
      {
        name: 'SQL注入',
        type: 'line',
        smooth: true,
        data: [65, 70, 75, 78, 76, 79, 78],
        itemStyle: { color: '#dc2626' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(220, 38, 38, 0.3)' },
            { offset: 1, color: 'rgba(220, 38, 38, 0.05)' }
          ])
        }
      },
      {
        name: 'DDoS攻击',
        type: 'line',
        smooth: true,
        data: [35, 38, 42, 45, 43, 46, 45],
        itemStyle: { color: '#ca8a04' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(202, 138, 4, 0.3)' },
            { offset: 1, color: 'rgba(202, 138, 4, 0.05)' }
          ])
        }
      },
      {
        name: '数据泄露',
        type: 'line',
        smooth: true,
        data: [55, 58, 60, 62, 61, 63, 62],
        itemStyle: { color: '#2563eb' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(37, 99, 235, 0.3)' },
            { offset: 1, color: 'rgba(37, 99, 235, 0.05)' }
          ])
        }
      },
      {
        name: '权限提升',
        type: 'line',
        smooth: true,
        data: [28, 30, 33, 35, 34, 36, 35],
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
  
  threatChart.setOption(option)
}

// 初始化模型性能雷达图
const initModelChart = () => {
  if (!modelChartRef.value) return
  modelChart = echarts.init(modelChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item'
    },
    radar: {
      indicator: [
        { name: '准确率', max: 100 },
        { name: '精确率', max: 100 },
        { name: '召回率', max: 100 },
        { name: 'F1分数', max: 100 },
        { name: 'AUC', max: 100 }
      ],
      radius: '65%'
    },
    series: [
      {
        name: '模型性能',
        type: 'radar',
        data: [
          {
            value: [94.5, 92.3, 96.1, 94.2, 95.8],
            name: '当前模型',
            itemStyle: { color: '#2563eb' },
            areaStyle: {
              color: new echarts.graphic.RadialGradient(0.5, 0.5, 1, [
                { offset: 0, color: 'rgba(37, 99, 235, 0.3)' },
                { offset: 1, color: 'rgba(37, 99, 235, 0.1)' }
              ])
            }
          }
        ]
      }
    ]
  }
  
  modelChart.setOption(option)
}

// 初始化异常行为热力图
const initHeatmapChart = () => {
  if (!heatmapChartRef.value) return
  heatmapChart = echarts.init(heatmapChartRef.value)
  
  // 生成热力图数据
  const hours = ['00', '02', '04', '06', '08', '10', '12', '14', '16', '18', '20', '22']
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const data: any[] = []
  
  days.forEach((day, i) => {
    hours.forEach((hour, j) => {
      data.push([j, i, Math.floor(Math.random() * 50)])
    })
  })
  
  const option = {
    tooltip: {
      position: 'top',
      formatter: (params: any) => {
        return `${days[params.value[1]]} ${hours[params.value[0]]}:00<br/>异常次数: ${params.value[2]}`
      }
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
      max: 50,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: '5%',
      inRange: {
        color: ['#f0fdf4', '#dcfce7', '#86efac', '#4ade80', '#22c55e', '#16a34a', '#15803d', '#166534']
      }
    },
    series: [
      {
        name: '异常次数',
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

// 图表导出功能
const handleExportThreatChart = () => {
  if (threatChart) {
    const url = threatChart.getDataURL({
      type: 'png',
      pixelRatio: 2,
      backgroundColor: '#fff'
    })
    const link = document.createElement('a')
    link.href = url
    link.download = `威胁趋势预测_${new Date().getTime()}.png`
    link.click()
    ElMessage.success('图表导出成功')
  }
}

const handleExportModelChart = () => {
  if (modelChart) {
    const url = modelChart.getDataURL({
      type: 'png',
      pixelRatio: 2,
      backgroundColor: '#fff'
    })
    const link = document.createElement('a')
    link.href = url
    link.download = `模型性能指标_${new Date().getTime()}.png`
    link.click()
    ElMessage.success('图表导出成功')
  }
}

const handleExportHeatmapChart = () => {
  if (heatmapChart) {
    const url = heatmapChart.getDataURL({
      type: 'png',
      pixelRatio: 2,
      backgroundColor: '#fff'
    })
    const link = document.createElement('a')
    link.href = url
    link.download = `异常行为热力图_${new Date().getTime()}.png`
    link.click()
    ElMessage.success('图表导出成功')
  }
}

// 初始化所有图表
const initCharts = () => {
  initThreatChart()
  initModelChart()
  initHeatmapChart()
}

// 窗口大小改变时重新调整图表大小
const handleResize = () => {
  threatChart?.resize()
  modelChart?.resize()
  heatmapChart?.resize()
}

// 生命周期钩子
onMounted(() => {
  initCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  threatChart?.dispose()
  modelChart?.dispose()
  heatmapChart?.dispose()
})
</script>
