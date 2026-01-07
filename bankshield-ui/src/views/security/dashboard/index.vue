<template>
  <div class="security-dashboard-screen">
    <!-- 顶部标题栏 -->
    <div class="dashboard-header">
      <div class="header-left">
        <div class="logo">
          <el-icon><Lock /></el-icon>
          <span>BankShield 安全态势感知平台</span>
        </div>
      </div>
      <div class="header-center">
        <div class="time-display">{{ currentTime }}</div>
      </div>
      <div class="header-right">
        <div class="security-level" :class="securityLevelClass">
          <span class="level-label">安全等级</span>
          <span class="level-value">{{ securityLevel }}</span>
        </div>
      </div>
    </div>

    <!-- 主体内容区 -->
    <div class="dashboard-content">
      <!-- 左侧列 -->
      <div class="dashboard-column left-column">
        <!-- 实时威胁统计 -->
        <div class="dashboard-card">
          <div class="card-title">
            <el-icon><WarningFilled /></el-icon>
            <span>实时威胁统计</span>
          </div>
          <div class="threat-stats">
            <div class="threat-item critical">
              <div class="threat-label">严重威胁</div>
              <div class="threat-value">{{ threatStats.critical }}</div>
            </div>
            <div class="threat-item high">
              <div class="threat-label">高危威胁</div>
              <div class="threat-value">{{ threatStats.high }}</div>
            </div>
            <div class="threat-item medium">
              <div class="threat-label">中危威胁</div>
              <div class="threat-value">{{ threatStats.medium }}</div>
            </div>
            <div class="threat-item low">
              <div class="threat-label">低危威胁</div>
              <div class="threat-value">{{ threatStats.low }}</div>
            </div>
          </div>
        </div>

        <!-- 攻击类型分布 -->
        <div class="dashboard-card">
          <div class="card-title">
            <el-icon><PieChart /></el-icon>
            <span>攻击类型分布</span>
          </div>
          <div ref="attackTypeChartRef" class="chart-container"></div>
        </div>

        <!-- 安全事件趋势 -->
        <div class="dashboard-card">
          <div class="card-title">
            <el-icon><TrendCharts /></el-icon>
            <span>安全事件趋势（24小时）</span>
          </div>
          <div ref="eventTrendChartRef" class="chart-container"></div>
        </div>
      </div>

      <!-- 中间列 -->
      <div class="dashboard-column center-column">
        <!-- 地理位置分布 -->
        <div class="dashboard-card map-card">
          <div class="card-title">
            <el-icon><Location /></el-icon>
            <span>攻击来源地理分布</span>
          </div>
          <div ref="mapChartRef" class="map-chart-container"></div>
        </div>

        <!-- 实时安全事件流 -->
        <div class="dashboard-card event-stream-card">
          <div class="card-title">
            <el-icon><List /></el-icon>
            <span>实时安全事件流</span>
          </div>
          <div class="event-stream">
            <div v-for="event in realtimeEvents" :key="event.id" class="event-item" :class="event.level">
              <div class="event-time">{{ event.time }}</div>
              <div class="event-type">{{ event.type }}</div>
              <div class="event-source">{{ event.source }}</div>
              <div class="event-target">{{ event.target }}</div>
              <div class="event-status">
                <el-tag :type="getEventStatusType(event.status)" size="small">{{ event.status }}</el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧列 -->
      <div class="dashboard-column right-column">
        <!-- 系统健康状态 -->
        <div class="dashboard-card">
          <div class="card-title">
            <el-icon><Monitor /></el-icon>
            <span>系统健康状态</span>
          </div>
          <div class="health-stats">
            <div class="health-item">
              <div class="health-label">CPU使用率</div>
              <el-progress :percentage="systemHealth.cpu" :color="getHealthColor(systemHealth.cpu)" />
            </div>
            <div class="health-item">
              <div class="health-label">内存使用率</div>
              <el-progress :percentage="systemHealth.memory" :color="getHealthColor(systemHealth.memory)" />
            </div>
            <div class="health-item">
              <div class="health-label">磁盘使用率</div>
              <el-progress :percentage="systemHealth.disk" :color="getHealthColor(systemHealth.disk)" />
            </div>
            <div class="health-item">
              <div class="health-label">网络流量</div>
              <el-progress :percentage="systemHealth.network" :color="getHealthColor(systemHealth.network)" />
            </div>
          </div>
        </div>

        <!-- TOP攻击源IP -->
        <div class="dashboard-card">
          <div class="card-title">
            <el-icon><DataLine /></el-icon>
            <span>TOP10 攻击源IP</span>
          </div>
          <div class="top-ips">
            <div v-for="(ip, index) in topAttackIPs" :key="ip.ip" class="ip-item">
              <div class="ip-rank">{{ index + 1 }}</div>
              <div class="ip-address">{{ ip.ip }}</div>
              <div class="ip-country">{{ ip.country }}</div>
              <div class="ip-count">{{ ip.count }}</div>
            </div>
          </div>
        </div>

        <!-- 安全评分 -->
        <div class="dashboard-card">
          <div class="card-title">
            <el-icon><Medal /></el-icon>
            <span>综合安全评分</span>
          </div>
          <div ref="scoreGaugeRef" class="chart-container"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { Lock, WarningFilled, PieChart, TrendCharts, Location, List, Monitor, DataLine, Medal } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import 'echarts/extension/bmap/bmap'

const currentTime = ref('')
const securityLevel = ref('正常')
const securityLevelClass = ref('normal')

const threatStats = reactive({
  critical: 0,
  high: 0,
  medium: 0,
  low: 0
})

const systemHealth = reactive({
  cpu: 0,
  memory: 0,
  disk: 0,
  network: 0
})

const realtimeEvents = ref<any[]>([])
const topAttackIPs = ref<any[]>([])

const attackTypeChartRef = ref()
const eventTrendChartRef = ref()
const mapChartRef = ref()
const scoreGaugeRef = ref()

let attackTypeChart: any = null
let eventTrendChart: any = null
let mapChart: any = null
let scoreGaugeChart: any = null

// 更新时间
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

// 模拟实时数据更新
const updateRealtimeData = () => {
  // 更新威胁统计
  threatStats.critical = Math.floor(Math.random() * 10)
  threatStats.high = Math.floor(Math.random() * 20) + 10
  threatStats.medium = Math.floor(Math.random() * 30) + 20
  threatStats.low = Math.floor(Math.random() * 40) + 30

  // 更新系统健康
  systemHealth.cpu = Math.floor(Math.random() * 30) + 40
  systemHealth.memory = Math.floor(Math.random() * 25) + 50
  systemHealth.disk = Math.floor(Math.random() * 20) + 60
  systemHealth.network = Math.floor(Math.random() * 35) + 30

  // 更新安全等级
  const total = threatStats.critical * 10 + threatStats.high * 5 + threatStats.medium * 2 + threatStats.low
  if (total > 200) {
    securityLevel.value = '高危'
    securityLevelClass.value = 'critical'
  } else if (total > 100) {
    securityLevel.value = '警告'
    securityLevelClass.value = 'warning'
  } else {
    securityLevel.value = '正常'
    securityLevelClass.value = 'normal'
  }

  // 更新实时事件流
  const eventTypes = ['SQL注入', 'XSS攻击', 'CSRF攻击', '暴力破解', 'DDoS攻击', '恶意扫描', '异常登录', '数据泄露']
  const statuses = ['已拦截', '已处理', '处理中', '待处理']
  const levels = ['critical', 'high', 'medium', 'low']
  
  if (Math.random() > 0.7) {
    const newEvent = {
      id: Date.now(),
      time: new Date().toLocaleTimeString('zh-CN'),
      type: eventTypes[Math.floor(Math.random() * eventTypes.length)],
      source: `${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}`,
      target: `192.168.1.${Math.floor(Math.random() * 255)}`,
      status: statuses[Math.floor(Math.random() * statuses.length)],
      level: levels[Math.floor(Math.random() * levels.length)]
    }
    realtimeEvents.value.unshift(newEvent)
    if (realtimeEvents.value.length > 10) {
      realtimeEvents.value.pop()
    }
  }
}

// 初始化攻击类型分布图
const initAttackTypeChart = () => {
  if (!attackTypeChart) {
    attackTypeChart = echarts.init(attackTypeChartRef.value)
  }

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#1a1a2e',
          borderWidth: 2
        },
        label: {
          show: true,
          color: '#fff',
          fontSize: 12
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        data: [
          { value: 335, name: 'SQL注入', itemStyle: { color: '#ff6b6b' } },
          { value: 310, name: 'XSS攻击', itemStyle: { color: '#ee5a6f' } },
          { value: 234, name: 'CSRF攻击', itemStyle: { color: '#c44569' } },
          { value: 135, name: '暴力破解', itemStyle: { color: '#f368e0' } },
          { value: 148, name: 'DDoS攻击', itemStyle: { color: '#ff9ff3' } },
          { value: 120, name: '其他', itemStyle: { color: '#54a0ff' } }
        ]
      }
    ]
  }

  attackTypeChart.setOption(option)
}

// 初始化事件趋势图
const initEventTrendChart = () => {
  if (!eventTrendChart) {
    eventTrendChart = echarts.init(eventTrendChartRef.value)
  }

  const hours = Array.from({ length: 24 }, (_, i) => `${i}:00`)
  const data = Array.from({ length: 24 }, () => Math.floor(Math.random() * 100) + 50)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: hours,
      axisLine: { lineStyle: { color: '#4a5568' } },
      axisLabel: { color: '#a0aec0' }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#4a5568' } },
      axisLabel: { color: '#a0aec0' },
      splitLine: { lineStyle: { color: '#2d3748' } }
    },
    series: [
      {
        name: '安全事件',
        type: 'line',
        smooth: true,
        data: data,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255, 107, 107, 0.5)' },
            { offset: 1, color: 'rgba(255, 107, 107, 0.05)' }
          ])
        },
        itemStyle: { color: '#ff6b6b' },
        lineStyle: { width: 3 }
      }
    ]
  }

  eventTrendChart.setOption(option)
}

// 初始化地图
const initMapChart = () => {
  if (!mapChart) {
    mapChart = echarts.init(mapChartRef.value)
  }

  const geoCoordMap: Record<string, number[]> = {
    '北京': [116.4074, 39.9042],
    '上海': [121.4737, 31.2304],
    '广州': [113.2644, 23.1291],
    '深圳': [114.0579, 22.5431],
    '杭州': [120.1551, 30.2741],
    '成都': [104.0668, 30.5728],
    '武汉': [114.3055, 30.5931],
    '西安': [108.9398, 34.3416]
  }

  const attackData = [
    { name: '北京', value: 150 },
    { name: '上海', value: 230 },
    { name: '广州', value: 180 },
    { name: '深圳', value: 200 },
    { name: '杭州', value: 120 },
    { name: '成都', value: 90 },
    { name: '武汉', value: 110 },
    { name: '西安', value: 80 }
  ]

  const convertData = (data: any[]) => {
    return data.map(item => ({
      name: item.name,
      value: [...geoCoordMap[item.name], item.value]
    }))
  }

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => {
        if (params.value) {
          return `${params.name}<br/>攻击次数: ${params.value[2]}`
        }
        return params.name
      }
    },
    geo: {
      map: 'china',
      roam: false,
      itemStyle: {
        areaColor: '#1a2332',
        borderColor: '#4a5568'
      },
      emphasis: {
        itemStyle: {
          areaColor: '#2d3748'
        }
      }
    },
    series: [
      {
        type: 'scatter',
        coordinateSystem: 'geo',
        data: convertData(attackData),
        symbolSize: (val: number[]) => val[2] / 10,
        itemStyle: {
          color: '#ff6b6b',
          shadowBlur: 10,
          shadowColor: 'rgba(255, 107, 107, 0.5)'
        },
        emphasis: {
          itemStyle: {
            color: '#ff4757'
          }
        }
      },
      {
        type: 'effectScatter',
        coordinateSystem: 'geo',
        data: convertData(attackData.sort((a, b) => b.value - a.value).slice(0, 3)),
        symbolSize: (val: number[]) => val[2] / 10,
        showEffectOn: 'render',
        rippleEffect: {
          brushType: 'stroke'
        },
        itemStyle: {
          color: '#ff6b6b',
          shadowBlur: 10,
          shadowColor: 'rgba(255, 107, 107, 0.5)'
        }
      }
    ]
  }

  mapChart.setOption(option)
}

// 初始化安全评分仪表盘
const initScoreGauge = () => {
  if (!scoreGaugeChart) {
    scoreGaugeChart = echarts.init(scoreGaugeRef.value)
  }

  const score = 85

  const option = {
    series: [
      {
        type: 'gauge',
        startAngle: 180,
        endAngle: 0,
        min: 0,
        max: 100,
        splitNumber: 10,
        itemStyle: {
          color: '#58D9F9'
        },
        progress: {
          show: true,
          width: 18
        },
        pointer: {
          show: false
        },
        axisLine: {
          lineStyle: {
            width: 18,
            color: [[1, '#2d3748']]
          }
        },
        axisTick: {
          show: false
        },
        splitLine: {
          show: false
        },
        axisLabel: {
          show: false
        },
        detail: {
          valueAnimation: true,
          formatter: '{value}',
          color: '#fff',
          fontSize: 40,
          offsetCenter: [0, '0%']
        },
        data: [{ value: score }]
      }
    ]
  }

  scoreGaugeChart.setOption(option)
}

// 初始化TOP攻击源IP
const initTopAttackIPs = () => {
  const countries = ['美国', '俄罗斯', '中国', '日本', '韩国', '德国', '英国', '法国', '印度', '巴西']
  topAttackIPs.value = Array.from({ length: 10 }, (_, i) => ({
    ip: `${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}`,
    country: countries[i],
    count: Math.floor(Math.random() * 500) + 100
  })).sort((a, b) => b.count - a.count)
}

const getEventStatusType = (status: string) => {
  const map: Record<string, any> = {
    '已拦截': 'success',
    '已处理': 'success',
    '处理中': 'warning',
    '待处理': 'danger'
  }
  return map[status] || 'info'
}

const getHealthColor = (percentage: number) => {
  if (percentage > 80) return '#f56c6c'
  if (percentage > 60) return '#e6a23c'
  return '#67c23a'
}

const handleResize = () => {
  attackTypeChart?.resize()
  eventTrendChart?.resize()
  mapChart?.resize()
  scoreGaugeChart?.resize()
}

let timeInterval: any = null
let dataInterval: any = null

onMounted(async () => {
  // 加载中国地图
  try {
    const chinaMap: any = await import('@/assets/china.json')
    echarts.registerMap('china', chinaMap.default || chinaMap)
  } catch (error) {
    console.warn('加载地图失败，使用简化版:', error)
    // 注册一个空地图避免报错
    echarts.registerMap('china', { type: 'FeatureCollection', features: [] } as any)
  }

  updateTime()
  timeInterval = setInterval(updateTime, 1000)

  updateRealtimeData()
  dataInterval = setInterval(updateRealtimeData, 3000)

  initTopAttackIPs()

  setTimeout(() => {
    try {
      initAttackTypeChart()
      initEventTrendChart()
      initMapChart()
      initScoreGauge()
    } catch (err) {
      console.error('初始化图表失败:', err)
    }
  }, 100)

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  clearInterval(timeInterval)
  clearInterval(dataInterval)
  window.removeEventListener('resize', handleResize)
  attackTypeChart?.dispose()
  eventTrendChart?.dispose()
  mapChart?.dispose()
  scoreGaugeChart?.dispose()
})
</script>

<style scoped lang="less">
.security-dashboard-screen {
  width: 100%;
  height: 100vh;
  background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
  overflow: hidden;
  color: #fff;
}

.dashboard-header {
  height: 80px;
  background: rgba(26, 26, 46, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 2px solid #4a5568;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);

  .header-left {
    flex: 1;

    .logo {
      display: flex;
      align-items: center;
      font-size: 24px;
      font-weight: bold;
      color: #58d9f9;

      .el-icon {
        font-size: 32px;
        margin-right: 12px;
      }
    }
  }

  .header-center {
    flex: 1;
    text-align: center;

    .time-display {
      font-size: 28px;
      font-weight: 600;
      color: #fff;
      text-shadow: 0 0 10px rgba(88, 217, 249, 0.5);
    }
  }

  .header-right {
    flex: 1;
    display: flex;
    justify-content: flex-end;

    .security-level {
      padding: 12px 24px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      gap: 12px;
      font-size: 18px;
      font-weight: bold;

      &.normal {
        background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
        box-shadow: 0 0 20px rgba(103, 194, 58, 0.5);
      }

      &.warning {
        background: linear-gradient(135deg, #e6a23c 0%, #f0a020 100%);
        box-shadow: 0 0 20px rgba(230, 162, 60, 0.5);
      }

      &.critical {
        background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
        box-shadow: 0 0 20px rgba(245, 108, 108, 0.5);
        animation: pulse 2s infinite;
      }
    }
  }
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 20px rgba(245, 108, 108, 0.5);
  }
  50% {
    box-shadow: 0 0 40px rgba(245, 108, 108, 0.8);
  }
}

.dashboard-content {
  height: calc(100vh - 80px);
  display: flex;
  gap: 20px;
  padding: 20px;
}

.dashboard-column {
  display: flex;
  flex-direction: column;
  gap: 20px;

  &.left-column {
    flex: 1;
  }

  &.center-column {
    flex: 2;
  }

  &.right-column {
    flex: 1;
  }
}

.dashboard-card {
  background: rgba(26, 26, 46, 0.6);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  border: 1px solid rgba(74, 85, 104, 0.5);
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  flex: 1;
  display: flex;
  flex-direction: column;

  &.map-card {
    flex: 2;
  }

  &.event-stream-card {
    flex: 1;
  }

  .card-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 18px;
    font-weight: bold;
    color: #58d9f9;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 2px solid rgba(88, 217, 249, 0.3);

    .el-icon {
      font-size: 22px;
    }
  }
}

.threat-stats {
  display: flex;
  flex-direction: column;
  gap: 16px;

  .threat-item {
    padding: 16px;
    border-radius: 8px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    transition: all 0.3s;

    &:hover {
      transform: translateX(5px);
    }

    &.critical {
      background: linear-gradient(135deg, rgba(245, 108, 108, 0.2) 0%, rgba(245, 108, 108, 0.1) 100%);
      border-left: 4px solid #f56c6c;
    }

    &.high {
      background: linear-gradient(135deg, rgba(230, 162, 60, 0.2) 0%, rgba(230, 162, 60, 0.1) 100%);
      border-left: 4px solid #e6a23c;
    }

    &.medium {
      background: linear-gradient(135deg, rgba(64, 158, 255, 0.2) 0%, rgba(64, 158, 255, 0.1) 100%);
      border-left: 4px solid #409eff;
    }

    &.low {
      background: linear-gradient(135deg, rgba(103, 194, 58, 0.2) 0%, rgba(103, 194, 58, 0.1) 100%);
      border-left: 4px solid #67c23a;
    }

    .threat-label {
      font-size: 16px;
      color: #a0aec0;
    }

    .threat-value {
      font-size: 32px;
      font-weight: bold;
      color: #fff;
    }
  }
}

.chart-container {
  flex: 1;
  min-height: 200px;
}

.map-chart-container {
  flex: 1;
  min-height: 400px;
}

.event-stream {
  flex: 1;
  overflow-y: auto;
  
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(88, 217, 249, 0.3);
    border-radius: 3px;
  }

  .event-item {
    display: grid;
    grid-template-columns: 80px 100px 1fr 1fr 80px;
    gap: 12px;
    padding: 12px;
    margin-bottom: 8px;
    border-radius: 6px;
    background: rgba(45, 55, 72, 0.5);
    border-left: 3px solid;
    font-size: 13px;
    transition: all 0.3s;

    &:hover {
      background: rgba(45, 55, 72, 0.8);
      transform: translateX(5px);
    }

    &.critical {
      border-left-color: #f56c6c;
    }

    &.high {
      border-left-color: #e6a23c;
    }

    &.medium {
      border-left-color: #409eff;
    }

    &.low {
      border-left-color: #67c23a;
    }

    .event-time {
      color: #a0aec0;
    }

    .event-type {
      color: #58d9f9;
      font-weight: 600;
    }

    .event-source, .event-target {
      color: #cbd5e0;
      font-family: monospace;
    }
  }
}

.health-stats {
  display: flex;
  flex-direction: column;
  gap: 20px;

  .health-item {
    .health-label {
      font-size: 14px;
      color: #a0aec0;
      margin-bottom: 8px;
    }
  }
}

.top-ips {
  display: flex;
  flex-direction: column;
  gap: 8px;

  .ip-item {
    display: grid;
    grid-template-columns: 30px 1fr 60px 60px;
    gap: 12px;
    padding: 10px;
    border-radius: 6px;
    background: rgba(45, 55, 72, 0.5);
    align-items: center;
    transition: all 0.3s;

    &:hover {
      background: rgba(45, 55, 72, 0.8);
      transform: translateX(5px);
    }

    .ip-rank {
      width: 24px;
      height: 24px;
      border-radius: 50%;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
      font-weight: bold;
    }

    .ip-address {
      color: #58d9f9;
      font-family: monospace;
      font-size: 13px;
    }

    .ip-country {
      color: #a0aec0;
      font-size: 12px;
    }

    .ip-count {
      color: #fff;
      font-weight: bold;
      text-align: right;
    }
  }
}
</style>
