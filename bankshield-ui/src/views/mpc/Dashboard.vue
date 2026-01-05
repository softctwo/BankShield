<template>
  <div class="mpc-dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #409eff">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalJobs || 0 }}</div>
              <div class="stat-label">总任务数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #67c23a">
              <el-icon><Check /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.successJobs || 0 }}</div>
              <div class="stat-label">成功任务</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #e6a23c">
              <el-icon><Loading /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.runningJobs || 0 }}</div>
              <div class="stat-label">运行中</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #f56c6c">
              <el-icon><Close /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.failedJobs || 0 }}</div>
              <div class="stat-label">失败任务</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>任务类型分布</span>
            </div>
          </template>
          <div class="task-types">
            <div class="task-type-item">
              <span>隐私求交 (PSI)</span>
              <el-tag type="primary">{{ statistics.psiJobs || 0 }}</el-tag>
            </div>
            <div class="task-type-item">
              <span>安全求和</span>
              <el-tag type="success">{{ statistics.sumJobs || 0 }}</el-tag>
            </div>
            <div class="task-type-item">
              <span>联合查询</span>
              <el-tag type="warning">{{ statistics.queryJobs || 0 }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>参与方状态</span>
              <el-button type="primary" size="small" @click="$router.push('/mpc/parties')">
                管理参与方
              </el-button>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="总参与方">
              {{ statistics.totalParties || 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="在线参与方">
              <el-tag type="success">{{ statistics.onlineParties || 0 }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>支持的MPC协议</span>
            </div>
          </template>
          <el-table :data="protocolList" v-loading="protocolLoading">
            <el-table-column prop="name" label="协议名称" />
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column prop="algorithm" label="算法" width="250" />
            <el-table-column prop="security" label="安全模型" width="150">
              <template #default="{ row }">
                <el-tag>{{ row.security }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>快速操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/mpc/psi')">
              <el-icon><Connection /></el-icon>
              执行隐私求交
            </el-button>
            <el-button type="success" @click="$router.push('/mpc/secure-sum')">
              <el-icon><Plus /></el-icon>
              执行安全求和
            </el-button>
            <el-button type="warning" @click="$router.push('/mpc/joint-query')">
              <el-icon><Search /></el-icon>
              执行联合查询
            </el-button>
            <el-button @click="$router.push('/mpc/jobs')">
              <el-icon><List /></el-icon>
              查看任务列表
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Document, Check, Loading, Close, Connection, Plus, Search, List } from '@element-plus/icons-vue'
import * as mpcApi from '@/api/mpc'
import type { ResponseData } from '@/utils/request'

const statistics = ref<any>({})
const protocolList = ref<any[]>([])
const protocolLoading = ref(false)

onMounted(() => {
  loadStatistics()
  loadProtocols()
})

const loadStatistics = async () => {
  try {
    const res = await mpcApi.getStatistics() as ResponseData
    if (res.code === 200) {
      statistics.value = res.data
    }
  } catch (error) {
    console.error('加载统计信息失败', error)
  }
}

const loadProtocols = async () => {
  protocolLoading.value = true
  try {
    const res = await mpcApi.getProtocols() as ResponseData
    if (res.code === 200) {
      const protocols = res.data
      protocolList.value = [
        protocols.psi,
        protocols.secureSum,
        protocols.jointQuery
      ]
    }
  } catch (error) {
    console.error('加载协议信息失败', error)
  } finally {
    protocolLoading.value = false
  }
}
</script>

<style scoped lang="less">
.mpc-dashboard {
  padding: 20px;
}

.stat-card {
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
    font-size: 24px;
    margin-right: 15px;
  }
  
  .stat-content {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-types {
  .task-type-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 0;
    border-bottom: 1px solid #ebeef5;
    
    &:last-child {
      border-bottom: none;
    }
  }
}

.quick-actions {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
  
  .el-button {
    flex: 1;
    min-width: 150px;
  }
}
</style>
