<template>
  <el-dialog
    title="密钥详情"
    :visible.sync="visible"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div v-loading="loading" class="key-detail">
      <!-- 基本信息 -->
      <el-card class="detail-card" header="基本信息">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-item">
              <label>密钥ID：</label>
              <span>{{ keyInfo.id }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <label>密钥名称：</label>
              <span>{{ keyInfo.keyName }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <label>密钥类型：</label>
              <el-tag size="small">{{ getKeyTypeLabel(keyInfo.keyType) }}</el-tag>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <label>密钥长度：</label>
              <span>{{ keyInfo.keyLength }} 位</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <label>密钥用途：</label>
              <span>{{ getKeyUsageLabel(keyInfo.keyUsage) }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <label>密钥状态：</label>
              <el-tag
                :type="getKeyStatusColor(keyInfo.keyStatus)"
                size="small"
              >
                {{ getKeyStatusLabel(keyInfo.keyStatus) }}
              </el-tag>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <label>创建人：</label>
              <span>{{ keyInfo.createdBy }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <label>创建时间：</label>
              <span>{{ formatDateTime(keyInfo.createTime) }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <label>过期时间：</label>
              <span :class="{ 'text-warning': isExpiringSoon(keyInfo.expireTime) }">
                {{ formatDateTime(keyInfo.expireTime) }}
              </span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <label>轮换周期：</label>
              <span>{{ keyInfo.rotationCycle }} 天</span>
            </div>
          </el-col>
          <el-col :span="12" v-if="keyInfo.lastRotationTime">
            <div class="detail-item">
              <label>上次轮换时间：</label>
              <span>{{ formatDateTime(keyInfo.lastRotationTime) }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <label>轮换次数：</label>
              <span>{{ keyInfo.rotationCount }} 次</span>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 密钥指纹 -->
      <el-card class="detail-card" header="密钥指纹" v-if="keyInfo.keyFingerprint">
        <div class="fingerprint-container">
          <el-input
            v-model="keyInfo.keyFingerprint"
            readonly
            type="textarea"
            :rows="2"
          />
          <el-button
            type="text"
            size="small"
            @click="copyToClipboard(keyInfo.keyFingerprint)"
            icon="el-icon-copy-document"
          >
            复制
          </el-button>
        </div>
      </el-card>

      <!-- 描述信息 -->
      <el-card class="detail-card" header="描述信息" v-if="keyInfo.description">
        <div class="description-content">
          {{ keyInfo.description }}
        </div>
      </el-card>

      <!-- 轮换历史 -->
      <el-card class="detail-card" header="轮换历史" v-if="rotationHistory.length > 0">
        <el-timeline>
          <el-timeline-item
            v-for="(item, index) in rotationHistory"
            :key="index"
            :timestamp="formatDateTime(item.rotationTime)"
            :type="item.rotationStatus === 'SUCCESS' ? 'success' : 'danger'"
            placement="top"
          >
            <el-card shadow="never">
              <div class="rotation-item">
                <div class="rotation-info">
                  <span class="rotation-reason">{{ item.rotationReason }}</span>
                  <span class="rotation-operator">操作员：{{ item.rotatedBy }}</span>
                </div>
                <div class="rotation-keys">
                  <span>旧密钥ID：{{ item.oldKeyId }}</span>
                  <span>新密钥ID：{{ item.newKeyId }}</span>
                </div>
                <div v-if="item.failureReason" class="rotation-error">
                  失败原因：{{ item.failureReason }}
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </el-card>

      <!-- 使用统计 -->
      <el-card class="detail-card" header="使用统计">
        <div class="usage-stats">
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-label">今日使用次数</div>
                <div class="stat-value">{{ usageStats.todayCount || 0 }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-label">本周使用次数</div>
                <div class="stat-value">{{ usageStats.weekCount || 0 }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-label">本月使用次数</div>
                <div class="stat-value">{{ usageStats.monthCount || 0 }}</div>
              </div>
            </el-col>
          </el-row>
        </div>
        <div class="chart-container" v-if="usageChartData.length > 0">
          <div ref="usageChart" style="height: 300px;"></div>
        </div>
      </el-card>
    </div>

    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">关 闭</el-button>
      <el-button type="primary" @click="handleViewUsage">
        <i class="el-icon-data-line"></i> 查看详细统计
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import { getKeyDetail, getKeyUsageStatistics } from '@/api/key'
import { 
  getKeyStatusColor, 
  getKeyStatusLabel, 
  getKeyTypeLabel, 
  getKeyUsageLabel 
} from '@/api/key'
import { formatDateTime, isExpiringSoon } from '@/utils/format'
import * as echarts from 'echarts'

export default {
  name: 'KeyDetailDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    keyId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      loading: false,
      keyInfo: {},
      rotationHistory: [],
      usageStats: {
        todayCount: 0,
        weekCount: 0,
        monthCount: 0
      },
      usageChartData: [],
      chartInstance: null
    }
  },
  watch: {
    visible(val) {
      if (val && this.keyId) {
        this.loadKeyDetail()
        this.loadUsageStatistics()
      }
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.initChart()
    })
  },
  beforeDestroy() {
    if (this.chartInstance) {
      this.chartInstance.dispose()
    }
  },
  methods: {
    // 加载密钥详情
    async loadKeyDetail() {
      this.loading = true
      try {
        const response = await getKeyDetail(this.keyId)
        if (response.code === 200) {
          this.keyInfo = response.data
          // 加载轮换历史（这里模拟数据，实际应该从API获取）
          this.loadRotationHistory()
        }
      } catch (error) {
        this.$message.error('加载密钥详情失败')
        console.error('加载密钥详情失败:', error)
      } finally {
        this.loading = false
      }
    },

    // 加载轮换历史（模拟数据）
    loadRotationHistory() {
      // TODO: 实际应该从API获取轮换历史
      this.rotationHistory = [
        {
          oldKeyId: 1001,
          newKeyId: this.keyId,
          rotationTime: '2024-01-15 10:30:00',
          rotationReason: '定期轮换',
          rotatedBy: 'admin',
          rotationStatus: 'SUCCESS'
        }
      ]
    },

    // 加载使用统计
    async loadUsageStatistics() {
      try {
        const endTime = new Date()
        const startTime = new Date()
        startTime.setDate(startTime.getDate() - 30) // 最近30天

        const response = await getKeyUsageStatistics(
          this.keyId,
          startTime.toISOString(),
          endTime.toISOString()
        )

        if (response.code === 200) {
          const data = response.data
          this.usageStats = {
            todayCount: this.calculateTodayCount(data.dailyUsage),
            weekCount: this.calculateWeekCount(data.dailyUsage),
            monthCount: this.calculateMonthCount(data.dailyUsage)
          }
          this.usageChartData = data.dailyUsage || []
          this.updateChart()
        }
      } catch (error) {
        console.error('加载使用统计失败:', error)
      }
    },

    // 计算今日使用次数
    calculateTodayCount(dailyUsage) {
      const today = new Date().toISOString().split('T')[0]
      const todayData = dailyUsage.find(item => item.date === today)
      return todayData ? todayData.count : 0
    },

    // 计算本周使用次数
    calculateWeekCount(dailyUsage) {
      const weekAgo = new Date()
      weekAgo.setDate(weekAgo.getDate() - 7)
      return dailyUsage
        .filter(item => new Date(item.date) >= weekAgo)
        .reduce((sum, item) => sum + item.count, 0)
    },

    // 计算本月使用次数
    calculateMonthCount(dailyUsage) {
      const monthAgo = new Date()
      monthAgo.setDate(monthAgo.getDate() - 30)
      return dailyUsage
        .filter(item => new Date(item.date) >= monthAgo)
        .reduce((sum, item) => sum + item.count, 0)
    },

    // 初始化图表
    initChart() {
      if (this.$refs.usageChart) {
        this.chartInstance = echarts.init(this.$refs.usageChart)
      }
    },

    // 更新图表
    updateChart() {
      if (!this.chartInstance || this.usageChartData.length === 0) {
        return
      }

      const option = {
        title: {
          text: '最近30天使用量趋势',
          left: 'center',
          textStyle: {
            fontSize: 14
          }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'line'
          }
        },
        xAxis: {
          type: 'category',
          data: this.usageChartData.map(item => item.date),
          axisLabel: {
            formatter: function(value) {
              return value.split('-').slice(1).join('/')
            }
          }
        },
        yAxis: {
          type: 'value',
          name: '使用次数'
        },
        series: [{
          name: '使用次数',
          type: 'line',
          data: this.usageChartData.map(item => item.count),
          smooth: true,
          lineStyle: {
            color: '#409EFF',
            width: 2
          },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [{
                offset: 0, color: 'rgba(64, 158, 255, 0.3)'
              }, {
                offset: 1, color: 'rgba(64, 158, 255, 0.1)'
              }]
            }
          }
        }]
      }

      this.chartInstance.setOption(option)
    },

    // 复制到剪贴板
    copyToClipboard(text) {
      if (navigator.clipboard) {
        navigator.clipboard.writeText(text).then(() => {
          this.$message.success('已复制到剪贴板')
        }).catch(() => {
          this.$message.error('复制失败')
        })
      } else {
        // 兼容性处理
        const textarea = document.createElement('textarea')
        textarea.value = text
        document.body.appendChild(textarea)
        textarea.select()
        document.execCommand('copy')
        document.body.removeChild(textarea)
        this.$message.success('已复制到剪贴板')
      }
    },

    // 查看详细统计
    handleViewUsage() {
      // 跳转到详细统计页面
      this.$router.push({
        path: '/encrypt/key/usage',
        query: { keyId: this.keyId }
      })
      this.handleClose()
    },

    // 关闭对话框
    handleClose() {
      this.$emit('update:visible', false)
      this.keyInfo = {}
      this.rotationHistory = []
      this.usageStats = {}
      this.usageChartData = []
    },

    // 工具方法
    formatDateTime,
    isExpiringSoon,
    getKeyStatusColor,
    getKeyStatusLabel,
    getKeyTypeLabel,
    getKeyUsageLabel
  }
}
</script>

<style lang="scss" scoped>
.key-detail {
  .detail-card {
    margin-bottom: 20px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .detail-item {
    padding: 8px 0;
    display: flex;
    align-items: center;

    label {
      font-weight: bold;
      color: #606266;
      margin-right: 8px;
      min-width: 80px;
    }

    span {
      color: #303133;
    }

    .text-warning {
      color: #e6a23c;
      font-weight: bold;
    }
  }

  .fingerprint-container {
    display: flex;
    align-items: center;
    gap: 10px;

    .el-textarea {
      flex: 1;
    }
  }

  .description-content {
    line-height: 1.6;
    color: #606266;
    padding: 10px;
    background-color: #f5f7fa;
    border-radius: 4px;
  }

  .rotation-item {
    .rotation-info {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;

      .rotation-reason {
        font-weight: bold;
        color: #303133;
      }

      .rotation-operator {
        color: #909399;
        font-size: 12px;
      }
    }

    .rotation-keys {
      display: flex;
      gap: 20px;
      margin-bottom: 8px;
      font-size: 12px;
      color: #606266;
    }

    .rotation-error {
      color: #f56c6c;
      font-size: 12px;
      background-color: #fef0f0;
      padding: 5px;
      border-radius: 3px;
      margin-top: 5px;
    }
  }

  .usage-stats {
    margin-bottom: 20px;

    .stat-item {
      text-align: center;
      padding: 15px;
      background-color: #f5f7fa;
      border-radius: 4px;

      .stat-label {
        font-size: 14px;
        color: #606266;
        margin-bottom: 8px;
      }

      .stat-value {
        font-size: 24px;
        font-weight: bold;
        color: #409EFF;
      }
    }
  }

  .chart-container {
    margin-top: 20px;
  }
}

.dialog-footer {
  text-align: right;
}
</style>