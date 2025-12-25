<template>
  <div class="ai-dashboard">
    <el-row :gutter="20" class="dashboard-header">
      <el-col :span="24">
        <el-card class="header-card">
          <div class="header-content">
            <h2>AI智能安全分析</h2>
            <p>基于机器学习的异常行为检测、智能告警和预测性安全防护</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="dashboard-stats">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon anomaly">
              <i class="el-icon-warning"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.anomalyCount }}</div>
              <div class="stat-label">今日异常行为</div>
              <div class="stat-change" :class="{ 'increase': stats.anomalyRate > 5, 'decrease': stats.anomalyRate <= 5 }">
                {{ stats.anomalyRate }}%
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon alert">
              <i class="el-icon-bell"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.alertCount }}</div>
              <div class="stat-label">智能告警数量</div>
              <div class="stat-change decrease">
                -{{ stats.falsePositiveReduction }}%
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon prediction">
              <i class="el-icon-data-analysis"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.predictionAccuracy }}%</div>
              <div class="stat-label">预测准确率</div>
              <div class="stat-change increase">
                +{{ stats.accuracyImprovement }}%
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon model">
              <i class="el-icon-cpu"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.modelCount }}</div>
              <div class="stat-label">AI模型数量</div>
              <div class="stat-change">
                {{ stats.activeModels }}/{{ stats.modelCount }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="dashboard-charts">
      <el-col :span="8">
        <el-card>
          <div slot="header" class="chart-header">
            <span>异常行为雷达图</span>
            <el-button type="text" @click="refreshAnomalyRadar">刷新</el-button>
          </div>
          <div class="chart-container">
            <div ref="anomalyRadar" class="chart"></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <div slot="header" class="chart-header">
            <span>智能告警降噪效果</span>
            <el-button type="text" @click="refreshAlertPrecision">刷新</el-button>
          </div>
          <div class="chart-container">
            <div ref="alertPrecision" class="chart"></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <div slot="header" class="chart-header">
            <span>预测性维护趋势</span>
            <el-button type="text" @click="refreshPredictionChart">刷新</el-button>
          </div>
          <div class="chart-container">
            <div ref="predictionChart" class="chart"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="dashboard-tables">
      <el-col :span="12">
        <el-card>
          <div slot="header" class="table-header">
            <span>最新异常行为</span>
            <el-button type="text" @click="viewAllAnomalies">查看全部</el-button>
          </div>
          <el-table :data="recentAnomalies" style="width: 100%">
            <el-table-column prop="userId" label="用户ID" width="80"></el-table-column>
            <el-table-column prop="behaviorType" label="行为类型" width="100">
              <template slot-scope="scope">
                <el-tag :type="getBehaviorTypeTag(scope.row.behaviorType)" size="mini">
                  {{ scope.row.behaviorType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="anomalyScore" label="异常分数" width="100">
              <template slot-scope="scope">
                <span :class="getScoreClass(scope.row.anomalyScore)">
                  {{ scope.row.anomalyScore.toFixed(3) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="anomalyLevel" label="异常等级" width="100">
              <template slot-scope="scope">
                <el-tag :type="getLevelTag(scope.row.anomalyLevel)" size="mini">
                  {{ scope.row.anomalyLevel }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="detectionTime" label="检测时间">
              <template slot-scope="scope">
                {{ formatTime(scope.row.detectionTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div slot="header" class="table-header">
            <span>AI模型状态</span>
            <el-button type="text" @click="viewAllModels">查看全部</el-button>
          </div>
          <el-table :data="modelStatus" style="width: 100%">
            <el-table-column prop="modelName" label="模型名称"></el-table-column>
            <el-table-column prop="modelType" label="模型类型" width="100">
              <template slot-scope="scope">
                <el-tag size="mini">{{ scope.row.modelType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="accuracy" label="准确率" width="80">
              <template slot-scope="scope">
                {{ (scope.row.accuracy * 100).toFixed(1) }}%
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template slot-scope="scope">
                <el-tag :type="getStatusTag(scope.row.status)" size="mini">
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="usageCount" label="使用次数" width="100"></el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getAnomalyRadarData, getAlertPrecisionData, getPredictionData, 
         getRecentAnomalies, getModelStatus, getDashboardStats } from '@/api/ai'

export default {
  name: 'AIDashboard',
  data() {
    return {
      stats: {
        anomalyCount: 0,
        anomalyRate: 0,
        alertCount: 0,
        falsePositiveReduction: 0,
        predictionAccuracy: 0,
        accuracyImprovement: 0,
        modelCount: 0,
        activeModels: 0
      },
      recentAnomalies: [],
      modelStatus: [],
      anomalyRadarChart: null,
      alertPrecisionChart: null,
      predictionChart: null
    }
  },
  mounted() {
    this.initCharts()
    this.loadData()
    // 每30秒刷新一次数据
    this.refreshTimer = setInterval(() => {
      this.loadData()
    }, 30000)
  },
  beforeDestroy() {
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer)
    }
    this.disposeCharts()
  },
  methods: {
    initCharts() {
      // 初始化异常行为雷达图
      this.anomalyRadarChart = echarts.init(this.$refs.anomalyRadar)
      this.initAnomalyRadarChart()
      
      // 初始化告警降噪效果图
      this.alertPrecisionChart = echarts.init(this.$refs.alertPrecision)
      this.initAlertPrecisionChart()
      
      // 初始化预测趋势图
      this.predictionChart = echarts.init(this.$refs.predictionChart)
      this.initPredictionChart()
      
      // 响应式
      window.addEventListener('resize', () => {
        this.anomalyRadarChart.resize()
        this.alertPrecisionChart.resize()
        this.predictionChart.resize()
      })
    },
    
    disposeCharts() {
      if (this.anomalyRadarChart) {
        this.anomalyRadarChart.dispose()
      }
      if (this.alertPrecisionChart) {
        this.alertPrecisionChart.dispose()
      }
      if (this.predictionChart) {
        this.predictionChart.dispose()
      }
    },
    
    initAnomalyRadarChart() {
      const option = {
        title: {
          text: '用户异常行为分析',
          left: 'center',
          textStyle: {
            fontSize: 14
          }
        },
        tooltip: {
          trigger: 'item'
        },
        legend: {
          bottom: 0,
          data: ['当前用户', '正常用户', '异常用户']
        },
        radar: {
          indicator: [
            { name: '登录时间', max: 100 },
            { name: '地理位置', max: 100 },
            { name: '操作频率', max: 100 },
            { name: '权限使用', max: 100 },
            { name: '数据访问', max: 100 }
          ],
          radius: '60%'
        },
        series: [{
          type: 'radar',
          data: [
            {
              value: [85, 92, 78, 95, 88],
              name: '当前用户',
              areaStyle: {
                color: 'rgba(255, 99, 132, 0.2)'
              }
            },
            {
              value: [20, 15, 25, 30, 20],
              name: '正常用户',
              areaStyle: {
                color: 'rgba(54, 162, 235, 0.2)'
              }
            },
            {
              value: [90, 85, 80, 95, 85],
              name: '异常用户',
              areaStyle: {
                color: 'rgba(255, 206, 86, 0.2)'
              }
            }
          ]
        }]
      }
      this.anomalyRadarChart.setOption(option)
    },
    
    initAlertPrecisionChart() {
      const option = {
        title: {
          text: '告警分类准确率',
          left: 'center',
          textStyle: {
            fontSize: 14
          }
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c}%'
        },
        legend: {
          bottom: 0,
          data: ['传统告警', 'AI降噪后']
        },
        xAxis: {
          type: 'category',
          data: ['1月', '2月', '3月', '4月', '5月', '6月']
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            formatter: '{value}%'
          }
        },
        series: [
          {
            name: '传统告警',
            type: 'line',
            data: [75, 78, 72, 76, 74, 77],
            itemStyle: {
              color: '#FF6384'
            }
          },
          {
            name: 'AI降噪后',
            type: 'line',
            data: [92, 94, 91, 95, 93, 96],
            itemStyle: {
              color: '#36A2EB'
            }
          }
        ]
      }
      this.alertPrecisionChart.setOption(option)
    },
    
    initPredictionChart() {
      const option = {
        title: {
          text: '系统资源预测趋势',
          left: 'center',
          textStyle: {
            fontSize: 14
          }
        },
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          bottom: 0,
          data: ['CPU使用率', '内存使用率', '磁盘使用率']
        },
        xAxis: {
          type: 'category',
          data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            formatter: '{value}%'
          }
        },
        series: [
          {
            name: 'CPU使用率',
            type: 'line',
            data: [65, 72, 78, 85, 82, 75, 68],
            areaStyle: {
              color: 'rgba(255, 99, 132, 0.2)'
            }
          },
          {
            name: '内存使用率',
            type: 'line',
            data: [58, 62, 68, 75, 73, 65, 60],
            areaStyle: {
              color: 'rgba(54, 162, 235, 0.2)'
            }
          },
          {
            name: '磁盘使用率',
            type: 'line',
            data: [45, 48, 52, 58, 55, 50, 47],
            areaStyle: {
              color: 'rgba(255, 206, 86, 0.2)'
            }
          }
        ]
      }
      this.predictionChart.setOption(option)
    },
    
    async loadData() {
      try {
        // 加载统计数据
        const statsData = await getDashboardStats()
        this.stats = { ...this.stats, ...statsData }
        
        // 加载最新异常行为
        const anomalyData = await getRecentAnomalies({ limit: 10 })
        this.recentAnomalies = anomalyData
        
        // 加载模型状态
        const modelData = await getModelStatus()
        this.modelStatus = modelData
        
        // 更新图表数据
        this.updateCharts()
        
      } catch (error) {
        this.$message.error('加载数据失败: ' + error.message)
      }
    },
    
    async updateCharts() {
      try {
        // 更新异常行为雷达图
        const radarData = await getAnomalyRadarData()
        if (radarData && this.anomalyRadarChart) {
          this.anomalyRadarChart.setOption({
            series: [{
              data: radarData.seriesData
            }]
          })
        }
        
        // 更新告警降噪效果图
        const precisionData = await getAlertPrecisionData()
        if (precisionData && this.alertPrecisionChart) {
          this.alertPrecisionChart.setOption({
            series: precisionData.seriesData
          })
        }
        
        // 更新预测趋势图
        const predictionData = await getPredictionData()
        if (predictionData && this.predictionChart) {
          this.predictionChart.setOption({
            series: predictionData.seriesData
          })
        }
        
      } catch (error) {
        console.error('更新图表失败:', error)
      }
    },
    
    refreshAnomalyRadar() {
      this.updateCharts()
    },
    
    refreshAlertPrecision() {
      this.updateCharts()
    },
    
    refreshPredictionChart() {
      this.updateCharts()
    },
    
    viewAllAnomalies() {
      this.$router.push('/ai/anomaly-list')
    },
    
    viewAllModels() {
      this.$router.push('/ai/model-management')
    },
    
    // 工具方法
    getBehaviorTypeTag(type) {
      const typeMap = {
        'login': 'primary',
        'access': 'success',
        'download': 'warning',
        'operation': 'info'
      }
      return typeMap[type] || 'info'
    },
    
    getScoreClass(score) {
      if (score > 0.8) return 'score-high'
      if (score > 0.5) return 'score-medium'
      return 'score-low'
    },
    
    getLevelTag(level) {
      const levelMap = {
        '高': 'danger',
        '中': 'warning',
        '低': 'success'
      }
      return levelMap[level] || 'info'
    },
    
    getStatusTag(status) {
      return status === 'active' ? 'success' : 'danger'
    },
    
    formatTime(time) {
      if (!time) return ''
      return this.$moment(time).format('MM-DD HH:mm')
    }
  }
}
</script>

<style lang="scss" scoped>
.ai-dashboard {
  padding: 20px;
  
  .dashboard-header {
    margin-bottom: 20px;
    
    .header-card {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      
      .header-content {
        text-align: center;
        padding: 20px 0;
        
        h2 {
          margin: 0 0 10px 0;
          font-size: 28px;
          font-weight: 300;
        }
        
        p {
          margin: 0;
          font-size: 16px;
          opacity: 0.9;
        }
      }
    }
  }
  
  .dashboard-stats {
    margin-bottom: 20px;
    
    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        padding: 20px 0;
        
        .stat-icon {
          width: 60px;
          height: 60px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 15px;
          font-size: 24px;
          color: white;
          
          &.anomaly {
            background: linear-gradient(135deg, #ff6b6b, #ee5a24);
          }
          
          &.alert {
            background: linear-gradient(135deg, #feca57, #ff9ff3);
          }
          
          &.prediction {
            background: linear-gradient(135deg, #48dbfb, #0abde3);
          }
          
          &.model {
            background: linear-gradient(135deg, #1dd1a1, #10ac84);
          }
        }
        
        .stat-info {
          flex: 1;
          
          .stat-number {
            font-size: 24px;
            font-weight: bold;
            margin-bottom: 5px;
          }
          
          .stat-label {
            font-size: 14px;
            color: #666;
            margin-bottom: 5px;
          }
          
          .stat-change {
            font-size: 12px;
            
            &.increase {
              color: #f56c6c;
              
              &::before {
                content: '↑ ';
              }
            }
            
            &.decrease {
              color: #67c23a;
              
              &::before {
                content: '↓ ';
              }
            }
          }
        }
      }
    }
  }
  
  .dashboard-charts {
    margin-bottom: 20px;
    
    .chart-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .chart-container {
      height: 300px;
      
      .chart {
        width: 100%;
        height: 100%;
      }
    }
  }
  
  .dashboard-tables {
    .table-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .score-high {
      color: #f56c6c;
      font-weight: bold;
    }
    
    .score-medium {
      color: #e6a23c;
      font-weight: bold;
    }
    
    .score-low {
      color: #67c23a;
    }
  }
}
</style>