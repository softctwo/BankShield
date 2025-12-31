<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <!-- 页面标题 -->
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-900">数据大屏</h1>
      <p class="mt-1 text-sm text-gray-500">银行数据安全管理系统总览</p>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0 h-12 w-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-2xl text-blue-600"><Files /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">总资产数量</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.totalAssets }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0 h-12 w-12 bg-red-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-2xl text-red-600"><Warning /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">敏感数据</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.sensitiveData }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0 h-12 w-12 bg-green-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-2xl text-green-600"><Lock /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">已加密数据</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.encryptedData }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0 h-12 w-12 bg-yellow-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-2xl text-yellow-600"><View /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">今日审计</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.todayAudits }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷菜单 -->
    <div class="bg-white rounded-lg shadow p-6 mb-6">
      <h2 class="text-lg font-medium text-gray-900 mb-4">快捷菜单</h2>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <button
          v-for="menu in quickMenus"
          :key="menu.path"
          @click="navigateTo(menu.path)"
          class="flex flex-col items-center p-4 bg-gray-50 rounded-lg hover:bg-blue-50 hover:shadow-md transition-all cursor-pointer"
        >
          <div class="h-12 w-12 rounded-full flex items-center justify-center mb-2" :style="{ backgroundColor: menu.color + '20' }">
            <el-icon class="text-2xl" :style="{ color: menu.color }">
              <component :is="menu.icon" />
            </el-icon>
          </div>
          <span class="text-sm font-medium text-gray-700">{{ menu.name }}</span>
          <span class="text-xs text-gray-500 mt-1">{{ menu.desc }}</span>
        </button>
      </div>
    </div>

    <!-- 数据图表 -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
      <!-- 数据分类统计饼图 -->
      <div class="bg-white rounded-lg shadow p-6">
        <h3 class="text-lg font-medium text-gray-900 mb-4">数据分类统计</h3>
        <div ref="classificationChartRef" style="height: 300px;"></div>
      </div>

      <!-- 安全态势趋势折线图 -->
      <div class="bg-white rounded-lg shadow p-6">
        <h3 class="text-lg font-medium text-gray-900 mb-4">安全态势趋势</h3>
        <div ref="trendChartRef" style="height: 300px;"></div>
      </div>
    </div>

    <!-- 系统状态和告警统计 -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
      <!-- 系统状态仪表盘 -->
      <div class="bg-white rounded-lg shadow p-6">
        <h3 class="text-lg font-medium text-gray-900 mb-4">系统状态监控</h3>
        <div class="grid grid-cols-3 gap-4">
          <div ref="cpuGaugeRef" style="height: 200px;"></div>
          <div ref="memoryGaugeRef" style="height: 200px;"></div>
          <div ref="diskGaugeRef" style="height: 200px;"></div>
        </div>
      </div>

      <!-- 实时告警柱状图 -->
      <div class="bg-white rounded-lg shadow p-6">
        <h3 class="text-lg font-medium text-gray-900 mb-4">告警统计分析</h3>
        <div ref="alertChartRef" style="height: 200px;"></div>
      </div>
    </div>

    <!-- 原有的趋势数据卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-6">
      <div v-for="trend in trendData" :key="trend.name" class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center justify-between">
          <div class="flex items-center">
            <div class="h-10 w-10 rounded-full flex items-center justify-center" :style="{ backgroundColor: trend.color + '20' }">
              <el-icon :style="{ color: trend.color }">
                <component :is="trend.icon" />
              </el-icon>
            </div>
            <div class="ml-3">
              <p class="text-sm font-medium text-gray-900">{{ trend.name }}</p>
              <p class="text-xs text-gray-500">{{ trend.desc }}</p>
            </div>
          </div>
          <div class="text-right">
            <p class="text-lg font-bold" :style="{ color: trend.color }">{{ trend.value }}</p>
            <p class="text-xs" :class="trend.change > 0 ? 'text-red-600' : 'text-green-600'">
              {{ trend.change > 0 ? '+' : '' }}{{ trend.change }}%
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- 实时监控和告警 -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div class="md:col-span-2 bg-white rounded-lg shadow">
        <div class="px-6 py-4 border-b border-gray-200">
          <h2 class="text-lg font-medium text-gray-900">实时告警</h2>
        </div>
        <div class="overflow-hidden">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">时间</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">类型</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">内容</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">级别</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="alert in recentAlerts" :key="alert.id" class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ alert.time }}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ alert.type }}</td>
                <td class="px-6 py-4 text-sm text-gray-500">{{ alert.content }}</td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium" :class="getAlertClass(alert.level)">
                    {{ alert.level }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-medium text-gray-900">系统资源趋势</h3>
          <button @click="handleExportChart" class="text-sm text-blue-600 hover:text-blue-700">
            <el-icon class="mr-1"><Download /></el-icon>
            导出图表
          </button>
        </div>
        <div ref="systemStatusChartRef" style="height: 250px;"></div>
        <div class="grid grid-cols-2 gap-4 mt-4 pt-4 border-t border-gray-200">
          <div class="text-center">
            <p class="text-sm text-gray-500">在线用户</p>
            <p class="text-2xl font-bold text-blue-600">{{ systemStatus.onlineUsers }}</p>
          </div>
          <div class="text-center">
            <p class="text-sm text-gray-500">运行时间</p>
            <p class="text-lg font-medium text-gray-900">{{ systemStatus.uptime }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { Files, Warning, Lock, View, User, Key, DataBoard, Monitor, Setting, TrendCharts, Bell, CircleCheck, Download } from '@element-plus/icons-vue'

const router = useRouter()

// ECharts实例引用
const classificationChartRef = ref<HTMLElement>()
const trendChartRef = ref<HTMLElement>()
const cpuGaugeRef = ref<HTMLElement>()
const memoryGaugeRef = ref<HTMLElement>()
const diskGaugeRef = ref<HTMLElement>()
const alertChartRef = ref<HTMLElement>()
const systemStatusChartRef = ref<HTMLElement>()

let classificationChart: echarts.ECharts | null = null
let trendChart: echarts.ECharts | null = null
let cpuGauge: echarts.ECharts | null = null
let memoryGauge: echarts.ECharts | null = null
let diskGauge: echarts.ECharts | null = null
let alertChart: echarts.ECharts | null = null
let systemStatusChart: echarts.ECharts | null = null

const stats = reactive({
  totalAssets: 1256,
  sensitiveData: 342,
  encryptedData: 1089,
  todayAudits: 156
})

const quickMenus = ref([
  { name: '用户管理', desc: '管理系统用户', path: '/system/user', icon: 'User', color: '#2563eb' },
  { name: '角色管理', desc: '配置角色权限', path: '/system/role', icon: 'Setting', color: '#16a34a' },
  { name: '资产分类', desc: '数据资产管理', path: '/classification/asset-management', icon: 'Files', color: '#ca8a04' },
  { name: '密钥管理', desc: '加密密钥管理', path: '/encrypt/key', icon: 'Key', color: '#dc2626' },
  { name: '审计日志', desc: '查看操作记录', path: '/audit/operation', icon: 'View', color: '#7c3aed' },
  { name: '安全监控', desc: '实时安全监控', path: '/monitor/dashboard', icon: 'Monitor', color: '#0891b2' },
  { name: 'AI分析', desc: '智能数据分析', path: '/ai/analysis', icon: 'TrendCharts', color: '#ea580c' },
  { name: '数据大屏', desc: '数据可视化', path: '/dashboard', icon: 'DataBoard', color: '#8b5cf6' }
])

const classificationData = ref([
  { name: '高敏感数据', value: 342, percentage: 85, color: '#dc2626' },
  { name: '中敏感数据', value: 456, percentage: 65, color: '#ca8a04' },
  { name: '低敏感数据', value: 458, percentage: 45, color: '#16a34a' }
])

const trendData = ref([
  { name: '异常行为', desc: '今日检测', value: 23, change: -12, icon: 'Warning', color: '#dc2626' },
  { name: '安全告警', desc: '待处理', value: 8, change: -25, icon: 'Bell', color: '#ca8a04' },
  { name: '加密任务', desc: '进行中', value: 15, change: 5, icon: 'Lock', color: '#2563eb' },
  { name: '合规检查', desc: '已通过', value: 98, change: 2, icon: 'CircleCheck', color: '#16a34a' }
])

const recentAlerts = ref([
  { id: 1, time: '13:05', type: '登录异常', content: '检测到异常登录尝试', level: '高危' },
  { id: 2, time: '13:03', type: '数据访问', content: '敏感数据访问频率异常', level: '中危' },
  { id: 3, time: '13:00', type: '系统告警', content: 'CPU使用率超过阈值', level: '低危' },
  { id: 4, time: '12:58', type: '权限变更', content: '用户权限被修改', level: '中危' }
])

const systemStatus = reactive({
  cpu: 45,
  memory: 62,
  disk: 38,
  onlineUsers: 45,
  uptime: '15天 8小时'
})

const navigateTo = (path: string) => {
  router.push(path)
}

const getAlertClass = (level: string) => {
  const map: Record<string, string> = {
    '高危': 'bg-red-100 text-red-800',
    '中危': 'bg-yellow-100 text-yellow-800',
    '低危': 'bg-blue-100 text-blue-800'
  }
  return map[level] || 'bg-gray-100 text-gray-800'
}

// 初始化数据分类饼图
const initClassificationChart = () => {
  if (!classificationChartRef.value) return
  classificationChart = echarts.init(classificationChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      bottom: '5%',
      left: 'center'
    },
    series: [
      {
        name: '数据分类',
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
        data: [
          { value: 342, name: '高敏感数据', itemStyle: { color: '#dc2626' } },
          { value: 456, name: '中敏感数据', itemStyle: { color: '#ca8a04' } },
          { value: 458, name: '低敏感数据', itemStyle: { color: '#16a34a' } }
        ]
      }
    ]
  }
  
  classificationChart.setOption(option)
}

// 初始化安全态势趋势折线图
const initTrendChart = () => {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['异常行为', '安全告警', '加密任务', '合规检查'],
      bottom: '5%'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '异常行为',
        type: 'line',
        smooth: true,
        data: [12, 18, 15, 23, 20, 18, 23],
        itemStyle: { color: '#dc2626' }
      },
      {
        name: '安全告警',
        type: 'line',
        smooth: true,
        data: [5, 8, 10, 8, 6, 7, 8],
        itemStyle: { color: '#ca8a04' }
      },
      {
        name: '加密任务',
        type: 'line',
        smooth: true,
        data: [10, 12, 13, 15, 14, 16, 15],
        itemStyle: { color: '#2563eb' }
      },
      {
        name: '合规检查',
        type: 'line',
        smooth: true,
        data: [95, 96, 97, 98, 97, 98, 98],
        itemStyle: { color: '#16a34a' }
      }
    ]
  }
  
  trendChart.setOption(option)
}

// 初始化仪表盘
const initGauges = () => {
  // CPU仪表盘
  if (cpuGaugeRef.value) {
    cpuGauge = echarts.init(cpuGaugeRef.value)
    cpuGauge.setOption({
      series: [
        {
          type: 'gauge',
          startAngle: 180,
          endAngle: 0,
          min: 0,
          max: 100,
          splitNumber: 5,
          axisLine: {
            lineStyle: {
              width: 6,
              color: [
                [0.7, '#16a34a'],
                [0.9, '#ca8a04'],
                [1, '#dc2626']
              ]
            }
          },
          pointer: {
            itemStyle: {
              color: 'auto'
            }
          },
          axisTick: {
            distance: -10,
            length: 5,
            lineStyle: {
              color: '#fff',
              width: 1
            }
          },
          splitLine: {
            distance: -10,
            length: 10,
            lineStyle: {
              color: '#fff',
              width: 2
            }
          },
          axisLabel: {
            color: 'auto',
            distance: 15,
            fontSize: 10
          },
          detail: {
            valueAnimation: true,
            formatter: '{value}%',
            color: 'auto',
            fontSize: 16,
            offsetCenter: [0, '70%']
          },
          title: {
            offsetCenter: [0, '90%'],
            fontSize: 12
          },
          data: [
            {
              value: systemStatus.cpu,
              name: 'CPU'
            }
          ]
        }
      ]
    })
  }

  // 内存仪表盘
  if (memoryGaugeRef.value) {
    memoryGauge = echarts.init(memoryGaugeRef.value)
    memoryGauge.setOption({
      series: [
        {
          type: 'gauge',
          startAngle: 180,
          endAngle: 0,
          min: 0,
          max: 100,
          splitNumber: 5,
          axisLine: {
            lineStyle: {
              width: 6,
              color: [
                [0.7, '#16a34a'],
                [0.9, '#ca8a04'],
                [1, '#dc2626']
              ]
            }
          },
          pointer: {
            itemStyle: {
              color: 'auto'
            }
          },
          axisTick: {
            distance: -10,
            length: 5,
            lineStyle: {
              color: '#fff',
              width: 1
            }
          },
          splitLine: {
            distance: -10,
            length: 10,
            lineStyle: {
              color: '#fff',
              width: 2
            }
          },
          axisLabel: {
            color: 'auto',
            distance: 15,
            fontSize: 10
          },
          detail: {
            valueAnimation: true,
            formatter: '{value}%',
            color: 'auto',
            fontSize: 16,
            offsetCenter: [0, '70%']
          },
          title: {
            offsetCenter: [0, '90%'],
            fontSize: 12
          },
          data: [
            {
              value: systemStatus.memory,
              name: '内存'
            }
          ]
        }
      ]
    })
  }

  // 磁盘仪表盘
  if (diskGaugeRef.value) {
    diskGauge = echarts.init(diskGaugeRef.value)
    diskGauge.setOption({
      series: [
        {
          type: 'gauge',
          startAngle: 180,
          endAngle: 0,
          min: 0,
          max: 100,
          splitNumber: 5,
          axisLine: {
            lineStyle: {
              width: 6,
              color: [
                [0.7, '#16a34a'],
                [0.9, '#ca8a04'],
                [1, '#dc2626']
              ]
            }
          },
          pointer: {
            itemStyle: {
              color: 'auto'
            }
          },
          axisTick: {
            distance: -10,
            length: 5,
            lineStyle: {
              color: '#fff',
              width: 1
            }
          },
          splitLine: {
            distance: -10,
            length: 10,
            lineStyle: {
              color: '#fff',
              width: 2
            }
          },
          axisLabel: {
            color: 'auto',
            distance: 15,
            fontSize: 10
          },
          detail: {
            valueAnimation: true,
            formatter: '{value}%',
            color: 'auto',
            fontSize: 16,
            offsetCenter: [0, '70%']
          },
          title: {
            offsetCenter: [0, '90%'],
            fontSize: 12
          },
          data: [
            {
              value: systemStatus.disk,
              name: '磁盘'
            }
          ]
        }
      ]
    })
  }
}

// 初始化告警柱状图
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
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['登录异常', '数据访问', '系统告警', '权限变更', '配置变更', '网络异常']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '告警数量',
        type: 'bar',
        data: [
          { value: 23, itemStyle: { color: '#dc2626' } },
          { value: 15, itemStyle: { color: '#ca8a04' } },
          { value: 8, itemStyle: { color: '#2563eb' } },
          { value: 12, itemStyle: { color: '#ca8a04' } },
          { value: 5, itemStyle: { color: '#16a34a' } },
          { value: 7, itemStyle: { color: '#2563eb' } }
        ],
        barWidth: '60%'
      }
    ]
  }
  
  alertChart.setOption(option)
}

// 初始化系统状态图表
const initSystemStatusChart = () => {
  if (!systemStatusChartRef.value) return
  systemStatusChart = echarts.init(systemStatusChartRef.value)
  
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
        itemStyle: { color: '#2563eb' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(37, 99, 235, 0.3)' },
            { offset: 1, color: 'rgba(37, 99, 235, 0.05)' }
          ])
        }
      },
      {
        name: '内存',
        type: 'line',
        smooth: true,
        data: [58, 60, 62, 61, 63, 62],
        itemStyle: { color: '#16a34a' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(22, 163, 74, 0.3)' },
            { offset: 1, color: 'rgba(22, 163, 74, 0.05)' }
          ])
        }
      },
      {
        name: '磁盘',
        type: 'line',
        smooth: true,
        data: [36, 37, 38, 38, 37, 38],
        itemStyle: { color: '#ca8a04' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(202, 138, 4, 0.3)' },
            { offset: 1, color: 'rgba(202, 138, 4, 0.05)' }
          ])
        }
      }
    ]
  }
  
  systemStatusChart.setOption(option)
}

// 图表导出功能
const handleExportChart = () => {
  if (systemStatusChart) {
    const url = systemStatusChart.getDataURL({
      type: 'png',
      pixelRatio: 2,
      backgroundColor: '#fff'
    })
    const link = document.createElement('a')
    link.href = url
    link.download = `系统资源趋势_${new Date().getTime()}.png`
    link.click()
    ElMessage.success('图表导出成功')
  }
}

// 实时更新图表数据
let updateTimer: number | null = null
const startRealTimeUpdate = () => {
  updateTimer = window.setInterval(() => {
    // 更新系统状态数据
    systemStatus.cpu = Math.floor(Math.random() * 20) + 40 // 40-60
    systemStatus.memory = Math.floor(Math.random() * 15) + 55 // 55-70
    systemStatus.disk = Math.floor(Math.random() * 10) + 35 // 35-45
    
    // 更新仪表盘
    cpuGauge?.setOption({
      series: [{ data: [{ value: systemStatus.cpu }] }]
    })
    memoryGauge?.setOption({
      series: [{ data: [{ value: systemStatus.memory }] }]
    })
    diskGauge?.setOption({
      series: [{ data: [{ value: systemStatus.disk }] }]
    })
    
    // 更新系统状态折线图
    if (systemStatusChart) {
      const option = systemStatusChart.getOption() as any
      const cpuData = option.series[0].data
      const memoryData = option.series[1].data
      const diskData = option.series[2].data
      
      cpuData.shift()
      cpuData.push(systemStatus.cpu)
      memoryData.shift()
      memoryData.push(systemStatus.memory)
      diskData.shift()
      diskData.push(systemStatus.disk)
      
      systemStatusChart.setOption({
        series: [
          { data: cpuData },
          { data: memoryData },
          { data: diskData }
        ]
      })
    }
  }, 5000) // 每5秒更新一次
}

// 初始化所有图表
const initCharts = () => {
  initClassificationChart()
  initTrendChart()
  initGauges()
  initAlertChart()
  initSystemStatusChart()
  startRealTimeUpdate()
}

// 窗口大小改变时重新调整图表大小
const handleResize = () => {
  classificationChart?.resize()
  trendChart?.resize()
  cpuGauge?.resize()
  memoryGauge?.resize()
  diskGauge?.resize()
  alertChart?.resize()
  systemStatusChart?.resize()
}

// 生命周期钩子
onMounted(() => {
  initCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (updateTimer) {
    clearInterval(updateTimer)
  }
  window.removeEventListener('resize', handleResize)
  classificationChart?.dispose()
  trendChart?.dispose()
  cpuGauge?.dispose()
  memoryGauge?.dispose()
  diskGauge?.dispose()
  alertChart?.dispose()
  systemStatusChart?.dispose()
})
</script>
