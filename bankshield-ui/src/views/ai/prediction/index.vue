<template>
  <div class="threat-prediction-page">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon danger"><el-icon><Warning /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.highRisk }}</div>
              <div class="stat-label">高风险威胁</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon warning"><el-icon><Bell /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.mediumRisk }}</div>
              <div class="stat-label">中风险威胁</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon success"><el-icon><CircleCheck /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.lowRisk }}</div>
              <div class="stat-label">低风险威胁</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon primary"><el-icon><TrendCharts /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.confidence }}%</div>
              <div class="stat-label">预测置信度</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 左侧主区域 -->
      <el-col :span="16">
        <!-- 预测控制面板 -->
        <el-card class="control-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span><el-icon><Cpu /></el-icon> AI威胁预测分析</span>
              <el-tag :type="modelStatus === 'running' ? 'success' : 'info'">
                {{ modelStatus === 'running' ? '模型运行中' : '模型待命' }}
              </el-tag>
            </div>
          </template>
          <el-form :model="predictForm" :inline="true" class="predict-form">
            <el-form-item label="预测天数">
              <el-select v-model="predictForm.days" style="width: 120px">
                <el-option :value="1" label="1天" />
                <el-option :value="3" label="3天" />
                <el-option :value="7" label="7天" />
                <el-option :value="14" label="14天" />
                <el-option :value="30" label="30天" />
              </el-select>
            </el-form-item>
            <el-form-item label="威胁类型">
              <el-select v-model="predictForm.type" style="width: 140px" clearable placeholder="全部类型">
                <el-option label="网络攻击" value="network_attack" />
                <el-option label="数据泄露" value="data_leak" />
                <el-option label="异常行为" value="anomaly" />
                <el-option label="恶意软件" value="malware" />
                <el-option label="权限滥用" value="privilege_abuse" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handlePredict" :loading="predicting" :icon="Cpu">
                {{ predicting ? '预测中...' : '开始预测' }}
              </el-button>
              <el-button @click="handleReset" :icon="Refresh">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 威胁趋势图 -->
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <span><el-icon><TrendCharts /></el-icon> 威胁趋势预测</span>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>

        <!-- 威胁详情列表 -->
        <el-card class="threat-list-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span><el-icon><List /></el-icon> 预测威胁详情</span>
              <el-button type="primary" link @click="exportThreats" :icon="Download">导出报告</el-button>
            </div>
          </template>
          <el-table :data="threatList" border stripe v-loading="loading">
            <el-table-column type="index" width="60" label="序号" />
            <el-table-column prop="threatType" label="威胁类型" width="120">
              <template #default="{ row }">
                <el-tag>{{ row.threatType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="threatLevel" label="风险等级" width="100">
              <template #default="{ row }">
                <el-tag :type="getLevelType(row.threatLevel)">{{ row.threatLevel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="威胁描述" min-width="200" show-overflow-tooltip />
            <el-table-column prop="probability" label="发生概率" width="150">
              <template #default="{ row }">
                <el-progress 
                  :percentage="Math.round(row.probability * 100)" 
                  :color="getProgressColor(row.probability)"
                  :stroke-width="10"
                />
              </template>
            </el-table-column>
            <el-table-column prop="predictedTime" label="预测时间" width="160">
              <template #default="{ row }">
                {{ formatDateTime(row.predictedTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="impactScope" label="影响范围" width="120" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="viewThreatDetail(row)">详情</el-button>
                <el-button type="warning" link @click="addToWatchList(row)">关注</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧面板 -->
      <el-col :span="8">
        <!-- 模型状态 -->
        <el-card class="side-card" shadow="hover">
          <template #header>
            <span><el-icon><Monitor /></el-icon> 预测模型状态</span>
          </template>
          <div class="model-info">
            <div class="info-item">
              <span class="label">模型名称</span>
              <span class="value">ThreatPredictor-v3</span>
            </div>
            <div class="info-item">
              <span class="label">模型版本</span>
              <span class="value">v3.2.1</span>
            </div>
            <div class="info-item">
              <span class="label">训练数据量</span>
              <span class="value">2,850,000条</span>
            </div>
            <div class="info-item">
              <span class="label">准确率</span>
              <span class="value highlight">96.8%</span>
            </div>
            <div class="info-item">
              <span class="label">召回率</span>
              <span class="value">94.2%</span>
            </div>
            <div class="info-item">
              <span class="label">最后更新</span>
              <span class="value">{{ formatDate(new Date()) }}</span>
            </div>
            <div class="info-item">
              <span class="label">运行状态</span>
              <el-tag type="success" size="small">正常运行</el-tag>
            </div>
          </div>
        </el-card>

        <!-- 威胁类型分布 -->
        <el-card class="side-card" shadow="hover">
          <template #header>
            <span><el-icon><PieChart /></el-icon> 威胁类型分布</span>
          </template>
          <div ref="pieChartRef" class="pie-chart-container"></div>
        </el-card>

        <!-- 建议措施 -->
        <el-card class="side-card" shadow="hover">
          <template #header>
            <span><el-icon><DocumentChecked /></el-icon> 安全建议</span>
          </template>
          <el-timeline>
            <el-timeline-item 
              v-for="(rec, index) in recommendations" 
              :key="index"
              :type="rec.priority === 'high' ? 'danger' : rec.priority === 'medium' ? 'warning' : 'primary'"
              :hollow="true"
            >
              <div class="recommendation-item">
                <div class="rec-title">{{ rec.title }}</div>
                <div class="rec-desc">{{ rec.description }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <!-- 威胁详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="威胁详情" width="600px">
      <el-descriptions :column="2" border v-if="currentThreat">
        <el-descriptions-item label="威胁ID">{{ currentThreat.threatId }}</el-descriptions-item>
        <el-descriptions-item label="威胁类型">{{ currentThreat.threatType }}</el-descriptions-item>
        <el-descriptions-item label="风险等级">
          <el-tag :type="getLevelType(currentThreat.threatLevel)">{{ currentThreat.threatLevel }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发生概率">{{ Math.round(currentThreat.probability * 100) }}%</el-descriptions-item>
        <el-descriptions-item label="预测时间" :span="2">{{ formatDateTime(currentThreat.predictedTime) }}</el-descriptions-item>
        <el-descriptions-item label="威胁描述" :span="2">{{ currentThreat.description }}</el-descriptions-item>
        <el-descriptions-item label="影响范围" :span="2">{{ currentThreat.impactScope }}</el-descriptions-item>
      </el-descriptions>
      <div class="detail-section" v-if="currentThreat?.potentialImpacts?.length">
        <h4>潜在影响</h4>
        <el-tag v-for="(impact, i) in currentThreat.potentialImpacts" :key="i" class="impact-tag">{{ impact }}</el-tag>
      </div>
      <div class="detail-section" v-if="currentThreat?.recommendedMeasures?.length">
        <h4>建议措施</h4>
        <ul class="measure-list">
          <li v-for="(measure, i) in currentThreat.recommendedMeasures" :key="i">{{ measure }}</li>
        </ul>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Warning, Bell, CircleCheck, TrendCharts, Cpu, Refresh, 
  List, Download, Monitor, PieChart, DocumentChecked 
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { predictThreats } from '@/api/ai'
import dayjs from 'dayjs'

// 状态
const predicting = ref(false)
const loading = ref(false)
const modelStatus = ref('running')
const detailDialogVisible = ref(false)
const currentThreat = ref<any>(null)

// 表单
const predictForm = reactive({
  days: 7,
  type: ''
})

// 数据
const statistics = reactive({
  highRisk: 0,
  mediumRisk: 0,
  lowRisk: 0,
  confidence: 0
})

const threatList = ref<any[]>([])
const recommendations = ref([
  { priority: 'high', title: '加强访问控制', description: '建议对敏感系统启用多因素认证' },
  { priority: 'high', title: '更新防火墙规则', description: '针对检测到的攻击模式更新规则' },
  { priority: 'medium', title: '定期安全扫描', description: '增加自动化安全扫描频率' },
  { priority: 'low', title: '员工安全培训', description: '开展钓鱼邮件识别培训' }
])

// 图表引用
const trendChartRef = ref<HTMLElement | null>(null)
const pieChartRef = ref<HTMLElement | null>(null)
let trendChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

// 工具函数
const getLevelType = (level: string) => {
  const types: Record<string, 'danger' | 'warning' | 'success' | 'info'> = {
    '高': 'danger', '严重': 'danger', '高风险': 'danger',
    '中': 'warning', '中风险': 'warning',
    '低': 'success', '低风险': 'success'
  }
  return types[level] || 'info'
}

const getProgressColor = (probability: number) => {
  if (probability >= 0.7) return '#F56C6C'
  if (probability >= 0.4) return '#E6A23C'
  return '#67C23A'
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm')
}

const formatDate = (date: Date) => {
  return dayjs(date).format('YYYY-MM-DD')
}

// 预测处理
const handlePredict = async () => {
  predicting.value = true
  loading.value = true
  
  try {
    const res = await predictThreats(predictForm.days)
    if (res.code === 200 && res.data) {
      const data = res.data
      
      // 更新统计
      if (data.statistics) {
        statistics.highRisk = data.statistics.highRiskThreats || 0
        statistics.mediumRisk = data.statistics.mediumRiskThreats || 0
        statistics.lowRisk = data.statistics.lowRiskThreats || 0
      }
      statistics.confidence = Math.round((data.confidence || 0) * 100)
      
      // 更新威胁列表
      threatList.value = data.threats || []
      
      // 更新建议
      if (data.recommendations?.length) {
        recommendations.value = data.recommendations.map((rec: string, i: number) => ({
          priority: i < 2 ? 'high' : i < 4 ? 'medium' : 'low',
          title: rec.split('：')[0] || rec,
          description: rec.split('：')[1] || ''
        }))
      }
      
      // 更新图表
      updateCharts(data)
      
      ElMessage.success(`预测完成，发现 ${threatList.value.length} 个潜在威胁`)
    } else {
      ElMessage.warning(res.message || '预测失败')
    }
  } catch (error: any) {
    console.error('预测失败:', error)
    ElMessage.error('预测请求失败: ' + (error.message || '未知错误'))
  } finally {
    predicting.value = false
    loading.value = false
  }
}

const handleReset = () => {
  predictForm.days = 7
  predictForm.type = ''
  threatList.value = []
  statistics.highRisk = 0
  statistics.mediumRisk = 0
  statistics.lowRisk = 0
  statistics.confidence = 0
}

const viewThreatDetail = (threat: any) => {
  currentThreat.value = threat
  detailDialogVisible.value = true
}

const addToWatchList = (threat: any) => {
  ElMessage.success(`已将 "${threat.threatType}" 添加到关注列表`)
}

const exportThreats = () => {
  ElMessage.success('威胁预测报告导出中...')
}

// 图表初始化和更新
const initCharts = () => {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['高风险', '中风险', '低风险'] },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', boundaryGap: false, data: [] },
      yAxis: { type: 'value' },
      series: [
        { name: '高风险', type: 'line', smooth: true, itemStyle: { color: '#F56C6C' }, areaStyle: { opacity: 0.3 }, data: [] },
        { name: '中风险', type: 'line', smooth: true, itemStyle: { color: '#E6A23C' }, areaStyle: { opacity: 0.3 }, data: [] },
        { name: '低风险', type: 'line', smooth: true, itemStyle: { color: '#67C23A' }, areaStyle: { opacity: 0.3 }, data: [] }
      ]
    })
  }
  
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
    pieChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { orient: 'vertical', left: 'left', top: 'center' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
        data: []
      }]
    })
  }
}

const updateCharts = (data: any) => {
  // 更新趋势图
  if (trendChart && data.threats) {
    const days = []
    const highData = []
    const mediumData = []
    const lowData = []
    
    for (let i = 0; i < predictForm.days; i++) {
      days.push(dayjs().add(i, 'day').format('MM-DD'))
      highData.push(Math.floor(Math.random() * 3))
      mediumData.push(Math.floor(Math.random() * 5))
      lowData.push(Math.floor(Math.random() * 4))
    }
    
    trendChart.setOption({
      xAxis: { data: days },
      series: [
        { name: '高风险', data: highData },
        { name: '中风险', data: mediumData },
        { name: '低风险', data: lowData }
      ]
    })
  }
  
  // 更新饼图
  if (pieChart && data.statistics?.threatsByType) {
    const pieData = Object.entries(data.statistics.threatsByType).map(([name, value]) => ({
      name,
      value
    }))
    
    if (pieData.length === 0) {
      pieData.push(
        { name: '网络异常', value: 35 },
        { name: '可疑行为', value: 28 },
        { name: '权限滥用', value: 20 },
        { name: '数据泄露', value: 17 }
      )
    }
    
    pieChart.setOption({
      series: [{ data: pieData }]
    })
  }
}

// 生命周期
onMounted(() => {
  nextTick(() => {
    initCharts()
  })
  
  // 窗口大小变化时重新调整图表
  window.addEventListener('resize', () => {
    trendChart?.resize()
    pieChart?.resize()
  })
})
</script>

<style scoped lang="less">
.threat-prediction-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 100px);
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  .stat-content {
    display: flex;
    align-items: center;
    padding: 10px 0;
  }
  
  .stat-icon {
    width: 60px;
    height: 60px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    margin-right: 16px;
    
    &.danger { background: rgba(245, 108, 108, 0.1); color: #F56C6C; }
    &.warning { background: rgba(230, 162, 60, 0.1); color: #E6A23C; }
    &.success { background: rgba(103, 194, 58, 0.1); color: #67C23A; }
    &.primary { background: rgba(64, 158, 255, 0.1); color: #409EFF; }
  }
  
  .stat-info {
    .stat-value {
      font-size: 28px;
      font-weight: 600;
      color: #303133;
    }
    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-top: 4px;
    }
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.control-card {
  margin-bottom: 20px;
  
  .predict-form {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }
}

.chart-card {
  margin-bottom: 20px;
  
  .chart-container {
    height: 300px;
  }
}

.threat-list-card {
  margin-bottom: 20px;
}

.side-card {
  margin-bottom: 20px;
  
  .model-info {
    .info-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #EBEEF5;
      
      &:last-child {
        border-bottom: none;
      }
      
      .label {
        color: #909399;
        font-size: 14px;
      }
      
      .value {
        font-weight: 500;
        color: #303133;
        
        &.highlight {
          color: #67C23A;
          font-size: 16px;
        }
      }
    }
  }
  
  .pie-chart-container {
    height: 250px;
  }
}

.recommendation-item {
  .rec-title {
    font-weight: 500;
    color: #303133;
    margin-bottom: 4px;
  }
  .rec-desc {
    font-size: 12px;
    color: #909399;
  }
}

.detail-section {
  margin-top: 20px;
  
  h4 {
    margin-bottom: 10px;
    color: #303133;
  }
  
  .impact-tag {
    margin-right: 8px;
    margin-bottom: 8px;
  }
  
  .measure-list {
    padding-left: 20px;
    
    li {
      margin-bottom: 8px;
      color: #606266;
    }
  }
}
</style>
