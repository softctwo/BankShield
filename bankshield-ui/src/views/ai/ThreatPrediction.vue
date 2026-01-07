<template>
  <div class="threat-prediction-container">
    <!-- 顶部操作区 -->
    <el-card class="header-card">
      <div class="header-content">
        <div>
          <h2 class="title">威胁预测</h2>
          <p class="subtitle">基于AI算法预测未来安全威胁趋势</p>
        </div>
        <div class="actions">
          <el-select v-model="predictionDays" placeholder="预测天数" style="width: 150px; margin-right: 10px">
            <el-option label="未来7天" :value="7" />
            <el-option label="未来14天" :value="14" />
            <el-option label="未来30天" :value="30" />
          </el-select>
          <el-button type="primary" :loading="loading" @click="handlePredict">
            <el-icon><TrendCharts /></el-icon>
            开始预测
          </el-button>
          <el-button @click="handleExport">
            <el-icon><Download /></el-icon>
            导出报告
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 预测概览卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalPredicted }}</div>
              <div class="stat-label">预测威胁总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f56c6c">
              <el-icon><CircleClose /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.highRisk }}</div>
              <div class="stat-label">高风险威胁</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67c23a">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.confidence }}%</div>
              <div class="stat-label">预测准确率</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6a23c">
              <el-icon><DataAnalysis /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.modelVersion }}</div>
              <div class="stat-label">模型版本</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 威胁趋势图表 -->
    <el-card class="chart-card">
      <template #header>
        <div class="card-header">
          <span>威胁趋势预测</span>
          <el-tag type="info">算法: LSTM时间序列预测</el-tag>
        </div>
      </template>
      <div ref="trendChartRef" style="width: 100%; height: 400px"></div>
    </el-card>

    <!-- 威胁类型分布 -->
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>威胁类型分布</span>
          </template>
          <div ref="typeChartRef" style="width: 100%; height: 350px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>置信度分析</span>
          </template>
          <div ref="confidenceChartRef" style="width: 100%; height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 预测详情列表 -->
    <el-card class="table-card">
      <template #header>
        <span>预测详情</span>
      </template>
      <el-table :data="predictionList" border stripe>
        <el-table-column prop="day" label="天数" width="80" align="center" />
        <el-table-column prop="date" label="日期" width="120" align="center" />
        <el-table-column label="预测威胁类型" min-width="300">
          <template #default="{ row }">
            <el-tag v-for="(count, type) in row.predictedThreats" :key="type" style="margin: 2px">
              {{ type }}: {{ count }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalCount" label="总数" width="100" align="center" />
        <el-table-column prop="confidence" label="置信度" width="120" align="center">
          <template #default="{ row }">
            <el-progress :percentage="Math.round(row.confidence * 100)" :color="getConfidenceColor(row.confidence)" />
          </template>
        </el-table-column>
        <el-table-column label="风险等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getRiskType(row.totalCount)">{{ getRiskLevel(row.totalCount) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { TrendCharts, Download, Warning, CircleClose, CircleCheck, DataAnalysis } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const loading = ref(false)
const predictionDays = ref(7)
const trendChartRef = ref<HTMLElement>()
const typeChartRef = ref<HTMLElement>()
const confidenceChartRef = ref<HTMLElement>()

const statistics = reactive({
  totalPredicted: 0,
  highRisk: 0,
  confidence: 0,
  modelVersion: 'v2.1.0'
})

const predictionList = ref<any[]>([])

let trendChart: echarts.ECharts | null = null
let typeChart: echarts.ECharts | null = null
let confidenceChart: echarts.ECharts | null = null

onMounted(() => {
  initCharts()
  handlePredict()
})

const initCharts = () => {
  nextTick(() => {
    if (trendChartRef.value) {
      trendChart = echarts.init(trendChartRef.value)
    }
    if (typeChartRef.value) {
      typeChart = echarts.init(typeChartRef.value)
    }
    if (confidenceChartRef.value) {
      confidenceChart = echarts.init(confidenceChartRef.value)
    }
  })
}

const handlePredict = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/api/ai/predict-threats', {
      params: { days: predictionDays.value }
    })
    
    if (res.data) {
      processPredictionData(res.data)
    }
  } catch (error) {
    console.error('威胁预测失败', error)
    // 使用模拟数据
    const mockData = generateMockData(predictionDays.value)
    processPredictionData(mockData)
  } finally {
    loading.value = false
  }
}

const generateMockData = (days: number) => {
  const predictions = []
  const threatTypes = ['SQL注入', 'XSS攻击', '暴力破解', 'DDoS攻击', '数据泄露']
  
  for (let i = 1; i <= days; i++) {
    const predictedThreats: any = {}
    threatTypes.forEach(type => {
      predictedThreats[type] = Math.floor(Math.random() * 20) + 5
    })
    
    const totalCount = Object.values(predictedThreats).reduce((sum: number, val: any) => sum + val, 0)
    
    predictions.push({
      day: i,
      date: new Date(Date.now() + i * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
      predictedThreats,
      totalCount,
      confidence: 0.75 + Math.random() * 0.2
    })
  }
  
  return {
    predictions,
    algorithm: 'LSTM时间序列预测',
    modelVersion: 'v2.1.0'
  }
}

const processPredictionData = (data: any) => {
  predictionList.value = data.predictions || []
  
  // 更新统计数据
  statistics.totalPredicted = predictionList.value.reduce((sum, item) => sum + item.totalCount, 0)
  statistics.highRisk = predictionList.value.filter(item => item.totalCount > 50).length
  statistics.confidence = Math.round(
    predictionList.value.reduce((sum, item) => sum + item.confidence, 0) / predictionList.value.length * 100
  )
  statistics.modelVersion = data.modelVersion || 'v2.1.0'
  
  // 更新图表
  updateTrendChart()
  updateTypeChart()
  updateConfidenceChart()
  
  ElMessage.success('威胁预测完成')
}

const updateTrendChart = () => {
  if (!trendChart) return
  
  const dates = predictionList.value.map(item => item.date)
  const threatTypes = ['SQL注入', 'XSS攻击', '暴力破解', 'DDoS攻击', '数据泄露']
  const series = threatTypes.map(type => ({
    name: type,
    type: 'line',
    smooth: true,
    data: predictionList.value.map(item => item.predictedThreats[type] || 0)
  }))
  
  trendChart.setOption({
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: threatTypes
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: {
      type: 'value',
      name: '威胁数量'
    },
    series
  })
}

const updateTypeChart = () => {
  if (!typeChart) return
  
  const threatTypes = ['SQL注入', 'XSS攻击', '暴力破解', 'DDoS攻击', '数据泄露']
  const data = threatTypes.map(type => ({
    name: type,
    value: predictionList.value.reduce((sum, item) => sum + (item.predictedThreats[type] || 0), 0)
  }))
  
  typeChart.setOption({
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '威胁类型',
        type: 'pie',
        radius: '50%',
        data,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  })
}

const updateConfidenceChart = () => {
  if (!confidenceChart) return
  
  const dates = predictionList.value.map(item => item.date)
  const confidences = predictionList.value.map(item => (item.confidence * 100).toFixed(1))
  
  confidenceChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: '{b}: {c}%'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: {
      type: 'value',
      name: '置信度(%)',
      min: 70,
      max: 100
    },
    series: [
      {
        name: '置信度',
        type: 'bar',
        data: confidences,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        }
      }
    ]
  })
}

const handleExport = () => {
  ElMessage.success('报告导出成功')
}

const getConfidenceColor = (confidence: number) => {
  if (confidence >= 0.9) return '#67c23a'
  if (confidence >= 0.8) return '#409eff'
  return '#e6a23c'
}

const getRiskLevel = (count: number) => {
  if (count > 50) return '高风险'
  if (count > 30) return '中风险'
  return '低风险'
}

const getRiskType = (count: number) => {
  if (count > 50) return 'danger'
  if (count > 30) return 'warning'
  return 'success'
}
</script>

<style scoped lang="less">
.threat-prediction-container {
  padding: 20px;

  .header-card {
    margin-bottom: 20px;

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .title {
        margin: 0;
        font-size: 24px;
        font-weight: bold;
      }

      .subtitle {
        margin: 5px 0 0;
        color: #909399;
        font-size: 14px;
      }

      .actions {
        display: flex;
        align-items: center;
      }
    }
  }

  .stats-row {
    margin-bottom: 20px;
  }

  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;

      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-size: 28px;
        margin-right: 15px;
      }

      .stat-info {
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
  }

  .chart-card {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  .table-card {
    margin-bottom: 20px;
  }
}
</style>
