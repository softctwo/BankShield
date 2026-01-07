<template>
  <div class="page-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff;"><el-icon><Document /></el-icon></div>
            <div class="stat-info"><div class="stat-value">{{ stats.totalLogs }}</div><div class="stat-label">总日志数</div></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6a23c;"><el-icon><Warning /></el-icon></div>
            <div class="stat-info"><div class="stat-value">{{ stats.warningCount }}</div><div class="stat-label">告警事件</div></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f56c6c;"><el-icon><CircleClose /></el-icon></div>
            <div class="stat-info"><div class="stat-value">{{ stats.riskCount }}</div><div class="stat-label">高风险事件</div></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67c23a;"><el-icon><CircleCheck /></el-icon></div>
            <div class="stat-info"><div class="stat-value">{{ stats.handleRate }}%</div><div class="stat-label">处理率</div></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 事件趋势图 -->
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header><span>审计事件趋势（近7天）</span></template>
          <div class="chart-placeholder">
            <el-table :data="trendData" border>
              <el-table-column prop="date" label="日期" />
              <el-table-column prop="total" label="总事件" />
              <el-table-column prop="warning" label="告警" />
              <el-table-column prop="risk" label="高风险" />
            </el-table>
          </div>
        </el-card>
      </el-col>
      <!-- 事件类型分布 -->
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header><span>事件类型分布</span></template>
          <div class="type-list">
            <div v-for="item in typeDistribution" :key="item.type" class="type-item">
              <span class="type-name">{{ item.name }}</span>
              <el-progress :percentage="item.percent" :color="item.color" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 高风险用户 -->
    <el-card class="table-card">
      <template #header><span>高风险用户TOP10</span></template>
      <el-table :data="riskUsers" border>
        <el-table-column prop="rank" label="排名" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="department" label="部门" />
        <el-table-column prop="riskScore" label="风险分数" width="120">
          <template #default="{ row }"><el-tag :type="row.riskScore > 80 ? 'danger' : 'warning'">{{ row.riskScore }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="eventCount" label="事件数" width="100" />
        <el-table-column prop="lastEvent" label="最近事件" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { Document, Warning, CircleClose, CircleCheck } from '@element-plus/icons-vue'

const stats = reactive({ totalLogs: 125680, warningCount: 328, riskCount: 45, handleRate: 92.5 })

const trendData = ref([
  { date: '01-01', total: 1580, warning: 45, risk: 8 },
  { date: '01-02', total: 1620, warning: 52, risk: 6 },
  { date: '01-03', total: 1450, warning: 38, risk: 5 },
  { date: '01-04', total: 1720, warning: 61, risk: 12 },
  { date: '01-05', total: 1680, warning: 48, risk: 7 },
  { date: '01-06', total: 1590, warning: 42, risk: 4 },
  { date: '01-07', total: 1650, warning: 55, risk: 9 }
])

const typeDistribution = ref([
  { type: 'login', name: '登录事件', percent: 45, color: '#409eff' },
  { type: 'access', name: '访问事件', percent: 30, color: '#67c23a' },
  { type: 'operation', name: '操作事件', percent: 15, color: '#e6a23c' },
  { type: 'security', name: '安全事件', percent: 10, color: '#f56c6c' }
])

const riskUsers = ref([
  { rank: 1, username: 'user_test01', department: '技术部', riskScore: 95, eventCount: 28, lastEvent: '批量导出敏感数据' },
  { rank: 2, username: 'user_test02', department: '运维部', riskScore: 88, eventCount: 22, lastEvent: '非工作时间登录' },
  { rank: 3, username: 'user_test03', department: '财务部', riskScore: 82, eventCount: 18, lastEvent: '权限越权访问' },
  { rank: 4, username: 'user_test04', department: '人事部', riskScore: 75, eventCount: 15, lastEvent: '频繁登录失败' },
  { rank: 5, username: 'user_test05', department: '市场部', riskScore: 68, eventCount: 12, lastEvent: '异常IP访问' }
])
</script>

<style scoped lang="less">
.page-container { padding: 20px; }
.stat-cards { margin-bottom: 20px; }
.stat-card { .stat-content { display: flex; align-items: center; } .stat-icon { width: 60px; height: 60px; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 28px; margin-right: 16px; } .stat-info { .stat-value { font-size: 28px; font-weight: bold; color: #303133; } .stat-label { font-size: 14px; color: #909399; } } }
.chart-card { margin-bottom: 20px; min-height: 300px; }
.chart-placeholder { padding: 20px 0; }
.type-list { .type-item { margin-bottom: 16px; .type-name { display: block; margin-bottom: 8px; font-size: 14px; } } }
.table-card { margin-top: 20px; }
</style>
