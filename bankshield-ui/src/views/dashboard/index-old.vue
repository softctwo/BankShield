<template>
  <div class="dashboard-container">
    <!-- 顶部统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon security">
                <el-icon><Lock /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.totalAssets }}</div>
                <div class="stat-label">总资产数量</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon sensitive">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.sensitiveData }}</div>
                <div class="stat-label">敏感数据</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon encrypted">
                <el-icon><Key /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.encryptedData }}</div>
                <div class="stat-label">已加密数据</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon audit">
                <el-icon><View /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.todayAudits }}</div>
                <div class="stat-label">今日审计</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <!-- 数据分类统计 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>数据分类统计</span>
                <el-button type="text" @click="refreshClassificationChart">
                  <el-icon><Refresh /></el-icon>
                </el-button>
              </div>
            </template>
            <div ref="classificationChart" class="chart-container"></div>
          </el-card>
        </el-col>
        
        <!-- 安全态势趋势 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>安全态势趋势</span>
                <el-button type="text" @click="refreshTrendChart">
                  <el-icon><Refresh /></el-icon>
                </el-button>
              </div>
            </template>
            <div ref="trendChart" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 实时监控和告警 -->
    <div class="monitor-section">
      <el-row :gutter="20">
        <!-- 实时监控 -->
        <el-col :span="16">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>实时监控</span>
                <el-switch
                  v-model="monitoringEnabled"
                  active-text="监控中"
                  inactive-text="已暂停"
                  @change="toggleMonitoring"
                />
              </div>
            </template>
            <div class="monitor-content">
              <div class="monitor-item" v-for="item in monitorData" :key="item.id">
                <div class="monitor-icon" :class="item.status">
                  <el-icon>
                    <component :is="getMonitorIcon(item.status)" />
                  </el-icon>
                </div>
                <div class="monitor-info">
                  <div class="monitor-title">{{ item.title }}</div>
                  <div class="monitor-desc">{{ item.description }}</div>
                  <div class="monitor-time">{{ item.time }}</div>
                </div>
                <div class="monitor-actions">
                  <el-button size="small" type="primary" @click="handleMonitor(item)">
                    查看详情
                  </el-button>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <!-- 告警信息 -->
        <el-col :span="8">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>告警信息</span>
                <el-badge :value="alerts.length" class="alert-badge">
                  <el-icon><Bell /></el-icon>
                </el-badge>
              </div>
            </template>
            <div class="alert-content">
              <div class="alert-item" v-for="alert in alerts" :key="alert.id" :class="alert.level">
                <div class="alert-icon">
                  <el-icon>
                    <component :is="getAlertIcon(alert.level)" />
                  </el-icon>
                </div>
                <div class="alert-info">
                  <div class="alert-title">{{ alert.title }}</div>
                  <div class="alert-time">{{ alert.time }}</div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 快速操作 -->
    <div class="quick-actions">
      <el-card>
        <template #header>
          <span>快速操作</span>
        </template>
        <div class="actions-grid">
          <div class="action-item" @click="navigateTo('/classification/asset')">
            <el-icon class="action-icon"><Folder /></el-icon>
            <div class="action-title">资产分类</div>
            <div class="action-desc">管理数据资产分类</div>
          </div>
          <div class="action-item" @click="navigateTo('/encrypt/key')">
            <el-icon class="action-icon"><Key /></el-icon>
            <div class="action-title">密钥管理</div>
            <div class="action-desc">管理加密密钥</div>
          </div>
          <div class="action-item" @click="navigateTo('/audit/log')">
            <el-icon class="action-icon"><Document /></el-icon>
            <div class="action-title">审计日志</div>
            <div class="action-desc">查看操作记录</div>
          </div>
          <div class="action-item" @click="navigateTo('/monitor/security')">
            <el-icon class="action-icon"><Monitor /></el-icon>
            <div class="action-title">安全监控</div>
            <div class="action-desc">实时安全监控</div>
          </div>
          <div class="action-item" @click="navigateTo('/ai/analysis')">
            <el-icon class="action-icon"><TrendCharts /></el-icon>
            <div class="action-title">AI分析</div>
            <div class="action-desc">智能数据分析</div>
          </div>
          <div class="action-item" @click="navigateTo('/system/user')">
            <el-icon class="action-icon"><User /></el-icon>
            <div class="action-title">用户管理</div>
            <div class="action-desc">管理系统用户</div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import {
  Lock,
  Warning,
  Key,
  View,
  Refresh,
  Bell,
  Folder,
  Document,
  Monitor,
  TrendCharts,
  User,
  SuccessFilled,
  WarningFilled,
  CircleCloseFilled
} from '@element-plus/icons-vue'

const router = useRouter()

// 响应式数据
const stats = ref({
  totalAssets: 1248,
  sensitiveData: 326,
  encryptedData: 892,
  todayAudits: 45
})

const monitoringEnabled = ref(true)
const monitorData = ref([
  {
    id: 1,
    title: '数据库连接异常',
    description: 'MySQL主库连接超时',
    status: 'error',
    time: '2分钟前'
  },
  {
    id: 2,
    title: '敏感数据访问',
    description: '用户张三访问了客户敏感信息',
    status: 'warning',
    time: '5分钟前'
  },
  {
    id: 3,
    title: '加密任务完成',
    description: '批量加密任务已成功完成',
    status: 'success',
    time: '10分钟前'
  }
])

const alerts = ref([
  {
    id: 1,
    title: '检测到异常登录',
    level: 'high',
    time: '刚刚'
  },
  {
    id: 2,
    title: '数据传输异常',
    level: 'medium',
    time: '3分钟前'
  },
  {
    id: 3,
    title: '权限变更通知',
    level: 'low',
    time: '10分钟前'
  }
])

// 图表实例
let classificationChart: echarts.ECharts | null = null
let trendChart: echarts.ECharts | null = null

// 定时器
let monitorTimer: NodeJS.Timeout | null = null

// 初始化图表
const initCharts = () => {
  // 分类统计图表
  const classificationEl = document.querySelector('[ref="classificationChart"]') as HTMLElement
  if (classificationEl) {
    classificationChart = echarts.init(classificationEl)
    const classificationOption = {
      title: {
        text: '数据分类分布',
        left: 'center'
      },
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
              fontSize: '20',
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: [
            { value: 435, name: '个人隐私信息', itemStyle: { color: '#5470c6' } },
            { value: 310, name: '金融数据', itemStyle: { color: '#91cc75' } },
            { value: 234, name: '医疗健康', itemStyle: { color: '#fac858' } },
            { value: 155, name: '企业机密', itemStyle: { color: '#ee6666' } },
            { value: 114, name: '其他', itemStyle: { color: '#73c0de' } }
          ]
        }
      ]
    }
    classificationChart.setOption(classificationOption)
  }

  // 趋势图表
  const trendEl = document.querySelector('[ref="trendChart"]') as HTMLElement
  if (trendEl) {
    trendChart = echarts.init(trendEl)
    const trendOption = {
      title: {
        text: '安全事件趋势',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['安全事件', '已处理', '威胁等级'],
        bottom: '5%'
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
          name: '安全事件',
          type: 'line',
          data: [12, 19, 3, 5, 2, 3, 7],
          itemStyle: { color: '#5470c6' }
        },
        {
          name: '已处理',
          type: 'line',
          data: [8, 15, 2, 4, 1, 2, 5],
          itemStyle: { color: '#91cc75' }
        },
        {
          name: '威胁等级',
          type: 'line',
          data: [3, 6, 1, 2, 1, 1, 3],
          itemStyle: { color: '#ee6666' }
        }
      ]
    }
    trendChart.setOption(trendOption)
  }
}

// 刷新分类图表
const refreshClassificationChart = () => {
  if (classificationChart) {
    // 模拟数据更新
    const newData = [
      { value: Math.floor(Math.random() * 500) + 200, name: '个人隐私信息' },
      { value: Math.floor(Math.random() * 400) + 100, name: '金融数据' },
      { value: Math.floor(Math.random() * 300) + 100, name: '医疗健康' },
      { value: Math.floor(Math.random() * 200) + 50, name: '企业机密' },
      { value: Math.floor(Math.random() * 150) + 50, name: '其他' }
    ]
    classificationChart.setOption({
      series: [{ data: newData }]
    })
    ElMessage.success('分类数据已更新')
  }
}

// 刷新趋势图表
const refreshTrendChart = () => {
  if (trendChart) {
    // 模拟数据更新
    const newEventData = Array.from({ length: 7 }, () => Math.floor(Math.random() * 20) + 5)
    const newProcessedData = Array.from({ length: 7 }, () => Math.floor(Math.random() * 15) + 3)
    const newThreatData = Array.from({ length: 7 }, () => Math.floor(Math.random() * 8) + 1)
    
    trendChart.setOption({
      series: [
        { data: newEventData },
        { data: newProcessedData },
        { data: newThreatData }
      ]
    })
    ElMessage.success('趋势数据已更新')
  }
}

// 切换监控状态
const toggleMonitoring = (enabled: boolean) => {
  if (enabled) {
    startMonitoring()
    ElMessage.success('监控已启动')
  } else {
    stopMonitoring()
    ElMessage.info('监控已暂停')
  }
}

// 开始监控
const startMonitoring = () => {
  monitorTimer = setInterval(() => {
    // 模拟监控数据更新
    if (Math.random() > 0.7) {
      const newMonitor = {
        id: Date.now(),
        title: ['数据访问', '系统异常', '安全告警', '任务完成'][Math.floor(Math.random() * 4)],
        description: '系统自动检测到新的活动',
        status: ['success', 'warning', 'error'][Math.floor(Math.random() * 3)],
        time: '刚刚'
      }
      monitorData.value.unshift(newMonitor)
      if (monitorData.value.length > 5) {
        monitorData.value.pop()
      }
    }
  }, 5000)
}

// 停止监控
const stopMonitoring = () => {
  if (monitorTimer) {
    clearInterval(monitorTimer)
    monitorTimer = null
  }
}

// 处理监控项
const handleMonitor = (item: any) => {
  ElMessage.info(`查看监控项: ${item.title}`)
}

// 获取监控图标
const getMonitorIcon = (status: string) => {
  const iconMap: Record<string, any> = {
    success: SuccessFilled,
    warning: WarningFilled,
    error: CircleCloseFilled
  }
  return iconMap[status] || WarningFilled
}

// 获取告警图标
const getAlertIcon = (level: string) => {
  const iconMap: Record<string, any> = {
    high: CircleCloseFilled,
    medium: WarningFilled,
    low: SuccessFilled
  }
  return iconMap[level] || WarningFilled
}

// 导航到指定页面
const navigateTo = (path: string) => {
  router.push(path)
}

// 窗口大小改变时重新调整图表
const handleResize = () => {
  classificationChart?.resize()
  trendChart?.resize()
}

onMounted(() => {
  // 延迟初始化图表，确保DOM已渲染
  setTimeout(() => {
    initCharts()
  }, 100)
  
  // 开始监控
  if (monitoringEnabled.value) {
    startMonitoring()
  }
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 清理定时器
  stopMonitoring()
  
  // 销毁图表
  classificationChart?.dispose()
  trendChart?.dispose()
  
  // 移除事件监听
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="less">
.dashboard-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;

  .stats-cards {
    margin-bottom: 20px;

    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        padding: 10px 0;

        .stat-icon {
          width: 60px;
          height: 60px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 16px;

          .el-icon {
            font-size: 28px;
            color: white;
          }

          &.security {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          }

          &.sensitive {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          }

          &.encrypted {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
          }

          &.audit {
            background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
          }
        }

        .stat-info {
          flex: 1;

          .stat-number {
            font-size: 28px;
            font-weight: bold;
            color: #303133;
            line-height: 1;
          }

          .stat-label {
            font-size: 14px;
            color: #909399;
            margin-top: 4px;
          }
        }
      }
    }
  }

  .charts-section {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .chart-container {
      height: 300px;
      width: 100%;
    }
  }

  .monitor-section {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .monitor-content {
      max-height: 400px;
      overflow-y: auto;

      .monitor-item {
        display: flex;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;

        &:last-child {
          border-bottom: none;
        }

        .monitor-icon {
          width: 40px;
          height: 40px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 12px;

          .el-icon {
            font-size: 20px;
            color: white;
          }

          &.success {
            background-color: #67c23a;
          }

          &.warning {
            background-color: #e6a23c;
          }

          &.error {
            background-color: #f56c6c;
          }
        }

        .monitor-info {
          flex: 1;

          .monitor-title {
            font-size: 14px;
            font-weight: 500;
            color: #303133;
            margin-bottom: 4px;
          }

          .monitor-desc {
            font-size: 12px;
            color: #606266;
            margin-bottom: 2px;
          }

          .monitor-time {
            font-size: 12px;
            color: #909399;
          }
        }

        .monitor-actions {
          margin-left: 12px;
        }
      }
    }

    .alert-content {
      max-height: 400px;
      overflow-y: auto;

      .alert-item {
        display: flex;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;

        &:last-child {
          border-bottom: none;
        }

        .alert-icon {
          width: 32px;
          height: 32px;
          border-radius: 6px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 10px;

          .el-icon {
            font-size: 16px;
            color: white;
          }
        }

        .alert-info {
          flex: 1;

          .alert-title {
            font-size: 13px;
            font-weight: 500;
            color: #303133;
            margin-bottom: 2px;
          }

          .alert-time {
            font-size: 11px;
            color: #909399;
          }
        }

        &.high .alert-icon {
          background-color: #f56c6c;
        }

        &.medium .alert-icon {
          background-color: #e6a23c;
        }

        &.low .alert-icon {
          background-color: #67c23a;
        }
      }
    }
  }

  .quick-actions {
    .actions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 16px;

      .action-item {
        padding: 20px;
        border: 1px solid #e4e7ed;
        border-radius: 8px;
        text-align: center;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          border-color: #409eff;
          box-shadow: 0 2px 12px 0 rgba(64, 158, 255, 0.1);
          transform: translateY(-2px);
        }

        .action-icon {
          font-size: 32px;
          color: #409eff;
          margin-bottom: 8px;
        }

        .action-title {
          font-size: 14px;
          font-weight: 500;
          color: #303133;
          margin-bottom: 4px;
        }

        .action-desc {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }
}

.alert-badge {
  .el-icon {
    font-size: 16px;
    color: #409eff;
  }
}
</style>
