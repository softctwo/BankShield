<template>
  <div class="dashboard-container">
    <!-- 标题 -->
    <div class="dashboard-header">
      <h1>🤖 AI智能安全 - 强化学习训练监控</h1>
      <div class="status-indicator">
        <span class="status-dot" :class="trainingStatus"></span>
        {{ trainingStatusText }}
      </div>
    </div>

    <!-- 关键指标卡片 -->
    <div class="metrics-row">
      <div class="metric-card">
        <div class="metric-value">{{ metrics.episode }}</div>
        <div class="metric-label">训练轮次</div>
        <div class="metric-trend trend-up">+{{ metrics.episodeGrowth }}</div>
      </div>
      
      <div class="metric-card">
        <div class="metric-value">{{ metrics.accuracy }}%</div>
        <div class="metric-label">检测准确率</div>
        <div class="metric-trend" :class="accuracyTrendClass">
          {{ metrics.accuracyGrowth > 0 ? '+' : '' }}{{ metrics.accuracyGrowth }}%
        </div>
      </div>
      
      <div class="metric-card">
        <div class="metric-value">{{ metrics.responseTime }}ms</div>
        <div class="metric-label">响应时间</div>
        <div class="metric-trend trend-down">-{{ metrics.responseTimeGrowth }}ms</div>
      </div>
      
      <div class="metric-card">
        <div class="metric-value">{{ metrics.anomalies }}</div>
        <div class="metric-label">异常检测</div>
        <div class="metric-trend">今日</div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <!-- DQN训练曲线 -->
      <div class="chart-card">
        <h3>📈 DQN训练曲线（损失值）</h3>
        <div class="chart-container" ref="dqnChart"></div>
        <div class="chart-info">
          <span>当前损失: {{ metrics.currentLoss.toFixed(4) }}</span>
          <span>ε-greedy: {{ metrics.epsilon.toFixed(2) }}</span>
        </div>
      </div>

      <!-- 威胁雷达图 -->
      <div class="chart-card">
        <h3>🎯 威胁雷达图（实时）</h3>
        <div class="radar-container" ref="radarChart"></div>
        <div class="radar-legend">
          <div class="legend-item">
            <span class="legend-color" style="background: #ff6b6b;"></span>
            当前威胁
          </div>
          <div class="legend-item">
            <span class="legend-color" style="background: #4ecdc4;"></span>
            基线水平
          </div>
        </div>
      </div>
    </div>

    <!-- 异常检测详情 -->
    <div class="anomaly-section">
      <h2>🚨 异常行为实时监控</h2>
      <el-table :data="anomalies" style="width: 100%" max-height="400">
        <el-table-column prop="timestamp" label="时间" width="180">
          <template slot-scope="scope">
            {{ formatTime(scope.row.timestamp) }}
          </template>
        </el-table-column>
        <el-table-column prop="userId" label="用户" width="120" />
        <el-table-column prop="ip" label="IP地址" width="150" />
        <el-table-column prop="type" label="异常类型" width="120">
          <template slot-scope="scope">
            <el-tag :type="getTagType(scope.row.type)" size="small">
              {{ scope.row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="异常分数" width="120">
          <template slot-scope="scope">
            <el-progress :percentage="scope.row.score * 100" 
                        :color="getProgressColor(scope.row.score)" />
          </template>
        </el-table-column>
        <el-table-column prop="action" label="自动响应" width="100">
          <template slot-scope="scope">
            <el-button v-if="scope.row.action" size="mini" type="text">
              {{ scope.row.action }}
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 智能预测 -->
    <div class="prediction-section">
      <h2>🔮 智能威胁预测（未来7天）</h2>
      <div class="pred-container">
        <div class="pred-item" v-for="(pred, idx) in predictions" :key="idx">
          <div class="pred-day">Day {{ idx + 1 }}</div>
          <div class="pred-risk" :class="pred.riskClass">
            {{ pred.riskLevel }}
          </div>
          <div class="pred-conf">置信度: {{ pred.confidence }}%</div>
          <div class="pred-trend">
            <el-icon :class="pred.trendIcon"></el-icon>
            {{ pred.trend }}
          </div>
        </div>
      </div>
    </div>

    <!-- 训练控制面板 -->
    <div class="control-panel">
      <h3>⚙️ 训练控制</h3>
      <div class="control-buttons">
        <el-button type="primary" @click="startTraining" :loading="isTraining">
          {{ isTraining ? '训练中...' : '开始训练' }}
        </el-button>
        <el-button @click="pauseTraining" :disabled="!isTraining">
          暂停训练
        </el-button>
        <el-button type="success" @click="refreshData">
          刷新数据
        </el-button>
        <el-button type="warning" @click="resetModel">
          重置模型
        </el-button>
      </div>
      
      <div class="hyper-params">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="学习率">0.001</el-descriptions-item>
          <el-descriptions-item label="Batch Size">32</el-descriptions-item>
          <el-descriptions-item label="ε-greedy">{{ metrics.epsilon.toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="经验回放">10,000</el-descriptions-item>
          <el-descriptions-item label="网络结构">128-256-128</el-descriptions-item>
          <el-descriptions-item label="激活函数">ReLU</el-descriptions-item>
        </el-descriptions>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';

export default {
  name: 'DQNTrainingDashboard',
  data() {
    return {
      trainingStatus: 'active',
      isTraining: false,
      metrics: {
        episode: 1247,
        accuracy: 95.4,
        responseTime: 45,
        anomalies: 23,
        episodeGrowth: 127,
        accuracyGrowth: 2.3,
        responseTimeGrowth: 12,
        currentLoss: 0.0342,
        epsilon: 0.15
      },
      anomalies: [
        { timestamp: Date.now() - 3600000, userId: 'user_12847', ip: '192.168.15.102', type: 'LOGIN_ANOMALY', score: 0.87, action: '封锁IP' },
        { timestamp: Date.now() - 7200000, userId: 'user_09382', ip: '10.0.25.18', type: 'FREQUENCY_ANOMALY', score: 0.72, action: '限流' },
        { timestamp: Date.now() - 10800000, userId: 'user_20394', ip: '192.168.7.45', type: 'GEO_ANOMALY', score: 0.91, action: 'MFA强制' },
        { timestamp: Date.now() - 14400000, userId: 'user_84721', ip: '10.0.8.92', type: 'PERMISSION_ANOMALY', score: 0.65, action: '权限降低' }
      ],
      predictions: [
        { riskLevel: 'LOW', riskClass: 'risk-low', confidence: 85, trend: '下降', trendIcon: 'el-icon-bottom' },
        { riskLevel: 'MEDIUM', riskClass: 'risk-medium', confidence: 72, trend: '平稳', trendIcon: 'el-icon-minus' },
        { riskLevel: 'MEDIUM', riskClass: 'risk-medium', confidence: 68, trend: '上升', trendIcon: 'el-icon-top' },
        { riskLevel: 'HIGH', riskClass: 'risk-high', confidence: 53, trend: '上升', trendIcon: 'el-icon-top' },
        { riskLevel: 'MEDIUM', riskClass: 'risk-medium', confidence: 71, trend: '下降', trendIcon: 'el-icon-bottom' },
        { riskLevel: 'LOW', riskClass: 'risk-low', confidence: 89, trend: '平稳', trendIcon: 'el-icon-minus' },
        { riskLevel: 'LOW', riskClass: 'risk-low', confidence: 92, trend: '下降', trendIcon: 'el-icon-bottom' }
      ],
      dqnChart: null,
      radarChart: null
    }
  },
  computed: {
    trainingStatusText() {
      return this.isTraining ? '训练中' : '监控中';
    },
    accuracyTrendClass() {
      return this.metrics.accuracyGrowth >= 0 ? 'trend-up' : 'trend-down';
    }
  },
  mounted() {
    this.initCharts();
    this.startMonitoring();
  },
  beforeDestroy() {
    if (this.dqnChart) this.dqnChart.dispose();
    if (this.radarChart) this.radarChart.dispose();
  },
  methods: {
    initCharts() {
      // DQN训练曲线
      this.dqnChart = echarts.init(this.$refs.dqnChart);
      const dqnOption = {
        tooltip: { trigger: 'axis' },
        legend: { data: ['损失值'] },
        xAxis: { type: 'category', data: this.generateTimeSeries(100) },
        yAxis: { type: 'value', name: '损失值' },
        series: [{
          name: '损失值',
          type: 'line',
          data: this.generateLossData(),
          smooth: true,
          lineStyle: { color: '#ff6b6b' },
          areaStyle: { color: 'rgba(255, 107, 107, 0.1)' }
        }],
        grid: { left: '10%', right: '10%', top: '15%', bottom: '15%' }
      };
      this.dqnChart.setOption(dqnOption);

      // 威胁雷达图
      this.radarChart = echarts.init(this.$refs.radarChart);
      const radarOption = {
        tooltip: {},
        radar: {
          indicator: [
            { name: '异常登录', max: 100 },
            { name: '访问频率', max: 100 },
            { name: '地理位置', max: 100 },
            { name: '权限使用', max: 100 },
            { name: '数据访问', max: 100 },
            { name: '时间异常', max: 100 }
          ],
          radius: '70%'
        },
        series: [{
          type: 'radar',
          data: [
            {
              value: [85, 72, 91, 65, 78, 45],
              name: '当前威胁',
              areaStyle: { color: 'rgba(255, 107, 107, 0.3)' },
              lineStyle: { color: '#ff6b6b' }
            },
            {
              value: [45, 38, 25, 48, 35, 52],
              name: '基线水平',
              areaStyle: { color: 'rgba(78, 205, 196, 0.2)' },
              lineStyle: { color: '#4ecdc4' }
            }
          ]
        }]
      };
      this.radarChart.setOption(radarOption);
    },
    generateTimeSeries(count) {
      return Array.from({ length: count }, (_, i) => `T${i + 1}`);
    },
    generateLossData() {
      return Array.from({ length: 100 }, (_, i) => {
        return 0.5 * Math.exp(-i * 0.05) + Math.random() * 0.05 + 0.02;
      });
    },
    startMonitoring() {
      // 模拟实时数据更新
      setInterval(() => {
        if (Math.random() > 0.7) {
          this.addNewAnomaly();
        }
      }, 60000);
    },
    addNewAnomaly() {
      const types = ['LOGIN_ANOMALY', 'FREQUENCY_ANOMALY', 'GEO_ANOMALY', 'PERMISSION_ANOMALY', 'DATA_ANOMALY'];
      const actions = ['封锁IP', '限流', 'MFA强制', '权限降低', '审计增强'];
      
      const newAnomaly = {
        timestamp: Date.now(),
        userId: 'user_' + Math.floor(Math.random() * 90000 + 10000),
        ip: '192.168.' + Math.floor(Math.random() * 255) + '.' + Math.floor(Math.random() * 255),
        type: types[Math.floor(Math.random() * types.length)],
        score: Math.random() * 0.5 + 0.5,
        action: actions[Math.floor(Math.random() * actions.length)]
      };
      
      this.anomalies.unshift(newAnomaly);
      this.anomalies = this.anomalies.slice(0, 50);
      
      this.metrics.anomalies++;
    },
    formatTime(timestamp) {
      return new Date(timestamp).toLocaleTimeString();
    },
    getTagType(type) {
      const map = {
        LOGIN_ANOMALY: 'danger',
        FREQUENCY_ANOMALY: 'warning',
        GEO_ANOMALY: 'danger',
        PERMISSION_ANOMALY: 'warning',
        DATA_ANOMALY: 'danger'
      };
      return map[type] || 'info';
    },
    getProgressColor(score) {
      if (score >= 0.8) return '#ff6b6b';
      if (score >= 0.6) return '#ffa726';
      return '#66bb6a';
    },
    startTraining() {
      this.isTraining = true;
      this.$message.success('DQN训练已启动');
      
      // 模拟训练过程
      const interval = setInterval(() => {
        if (!this.isTraining) {
          clearInterval(interval);
          return;
        }
        
        this.metrics.episode++;
        this.metrics.epsilon = Math.max(0.01, this.metrics.epsilon * 0.995);
        this.metrics.currentLoss *= (0.99 + Math.random() * 0.01);
        
        // 更新图表
        this.updateDQNChart();
      }, 3000);
    },
    pauseTraining() {
      this.isTraining = false;
      this.$message.info('训练已暂停');
    },
    refreshData() {
      this.$message.success('数据已刷新');
      this.metrics.episodeGrowth = Math.floor(Math.random() * 200);
      this.metrics.accuracyGrowth = (Math.random() * 3).toFixed(1);
      this.metrics.responseTimeGrowth = Math.floor(Math.random() * 20);
    },
    resetModel() {
      this.$confirm('确定要重置模型吗？这将清除所有训练数据。', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.metrics.episode = 0;
        this.metrics.accuracy = 85.0;
        this.metrics.currentLoss = 0.5;
        this.metrics.epsilon = 1.0;
        this.$message.success('模型已重置');
      });
    },
    updateDQNChart() {
      const option = this.dqnChart.getOption();
      option.series[0].data.shift();
      option.series[0].data.push(this.metrics.currentLoss);
      this.dqnChart.setOption(option);
    }
  }
};
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.dashboard-header h1 {
  margin: 0;
  color: #303133;
  font-size: 24px;
}

.status-indicator {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #606266;
}

.status-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 8px;
}

.status-dot.active {
  background: #67c23a;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}

.metrics-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.metric-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
  transition: transform 0.3s;
}

.metric-card:hover {
  transform: translateY(-5px);
}

.metric-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.metric-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.metric-trend {
  font-size: 12px;
  font-weight: bold;
}

.metric-trend.trend-up {
  color: #67c23a;
}

.metric-trend.trend-down {
  color: #f56c6c;
}

.charts-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.chart-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chart-card h3 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 18px;
}

.chart-container {
  width: 100%;
  height: 300px;
}

.chart-info {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #606266;
}

.radar-container {
  width: 100%;
  height: 300px;
}

.radar-legend {
  margin-top: 15px;
  display: flex;
  gap: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #606266;
}

.legend-color {
  width: 20px;
  height: 3px;
  margin-right: 8px;
  border-radius: 2px;
}

.anomaly-section, .prediction-section, .control-panel {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.anomaly-section h2, .prediction-section h2, .control-panel h3 {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 20px;
}

.control-buttons {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.pred-container {
  display: flex;
  gap: 15px;
  overflow-x: auto;
  padding: 10px 0;
}

.pred-item {
  min-width: 120px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
  text-align: center;
  transition: all 0.3s;
}

.pred-item:hover {
  background: #e6f7ff;
  transform: scale(1.05);
}

.pred-day {
  font-weight: bold;
  color: #606266;
  margin-bottom: 8px;
}

.pred-risk {
  padding: 5px 10px;
  border-radius: 15px;
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 8px;
}

.pred-risk.risk-low {
  background: #67c23a;
  color: white;
}

.pred-risk.risk-medium {
  background: #e6a23c;
  color: white;
}

.pred-risk.risk-high {
  background: #f56c6c;
  color: white;
}

.pred-conf {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.pred-trend {
  font-size: 12px;
  color: #606266;
}

.hyper-params {
  margin-top: 20px;
}
</style>
