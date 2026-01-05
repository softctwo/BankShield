<template>
  <div class="blockchain-dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #409eff">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalRecords || 0 }}</div>
              <div class="stat-label">总存证记录</div>
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
              <div class="stat-value">{{ statistics.auditRecords || 0 }}</div>
              <div class="stat-label">审计记录</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #e6a23c">
              <el-icon><Key /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.keyRecords || 0 }}</div>
              <div class="stat-label">密钥记录</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #f56c6c">
              <el-icon><Files /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.complianceRecords || 0 }}</div>
              <div class="stat-label">合规记录</div>
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
              <span>区块链网络状态</span>
              <el-button type="primary" size="small" @click="refreshNetworkStatus">刷新</el-button>
            </div>
          </template>
          <div v-loading="networkLoading">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="连接状态">
                <el-tag :type="networkStatus.connected ? 'success' : 'danger'">
                  {{ networkStatus.connected ? '已连接' : '未连接' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="区块高度">
                {{ networkStatus.blockHeight || 0 }}
              </el-descriptions-item>
              <el-descriptions-item label="节点数量">
                {{ networkStatus.peerCount || 0 }}
              </el-descriptions-item>
              <el-descriptions-item label="通道名称">
                {{ networkStatus.channelName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="最后区块时间" :span="2">
                {{ formatTime(networkStatus.lastBlockTime) }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>合规性检查</span>
            </div>
          </template>
          <div class="compliance-checks">
            <div class="compliance-item" v-for="standard in complianceStandards" :key="standard">
              <span>{{ standard }}</span>
              <el-button type="primary" size="small" @click="checkCompliance(standard)">
                检查
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最近存证记录</span>
              <el-button type="primary" size="small" @click="$router.push('/blockchain/records')">
                查看全部
              </el-button>
            </div>
          </template>
          <el-table :data="recentRecords" v-loading="recordsLoading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="anchorType" label="类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getTypeTag(row.anchorType)">{{ row.anchorType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="businessId" label="业务ID" width="150" />
            <el-table-column prop="txHash" label="交易哈希" show-overflow-tooltip />
            <el-table-column prop="blockId" label="区块ID" width="150" />
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="viewRecord(row)">查看</el-button>
                <el-button type="success" size="small" @click="verifyRecord(row)">验证</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Check, Key, Files } from '@element-plus/icons-vue'
import * as blockchainApi from '@/api/blockchain'
import dayjs from 'dayjs'

const statistics = ref<any>({})
const networkStatus = ref<any>({})
const networkLoading = ref(false)
const recentRecords = ref<any[]>([])
const recordsLoading = ref(false)
const complianceStandards = ['GDPR', 'PCI_DSS', 'SOX']

onMounted(() => {
  loadStatistics()
  loadNetworkStatus()
  loadRecentRecords()
})

const loadStatistics = async () => {
  try {
    const res = await blockchainApi.getStatistics()
    if (res.code === 200) {
      statistics.value = res.data
    }
  } catch (error) {
    console.error('加载统计信息失败', error)
  }
}

const loadNetworkStatus = async () => {
  networkLoading.value = true
  try {
    const res = await blockchainApi.getNetworkStatus()
    if (res.code === 200) {
      networkStatus.value = res.data
    }
  } catch (error) {
    console.error('加载网络状态失败', error)
  } finally {
    networkLoading.value = false
  }
}

const loadRecentRecords = async () => {
  recordsLoading.value = true
  try {
    const res = await blockchainApi.getAnchorRecords()
    if (res.code === 200) {
      recentRecords.value = res.data.slice(0, 10)
    }
  } catch (error) {
    console.error('加载存证记录失败', error)
  } finally {
    recordsLoading.value = false
  }
}

const refreshNetworkStatus = () => {
  loadNetworkStatus()
  ElMessage.success('网络状态已刷新')
}

const checkCompliance = async (standard: string) => {
  try {
    const res = await blockchainApi.checkCompliance(standard)
    if (res.code === 200) {
      const result = res.data
      if (result.compliant) {
        ElMessage.success(`${standard} 合规性检查通过`)
      } else {
        ElMessage.warning(`${standard} 合规性检查未通过`)
      }
    }
  } catch (error) {
    ElMessage.error('合规性检查失败')
  }
}

const viewRecord = (row: any) => {
  console.log('查看记录', row)
}

const verifyRecord = async (row: any) => {
  try {
    let res
    if (row.anchorType === 'AUDIT') {
      res = await blockchainApi.verifyAuditLogIntegrity(row.businessId)
    } else if (row.anchorType === 'KEY') {
      res = await blockchainApi.verifyKeyEventIntegrity(row.businessId)
    } else if (row.anchorType === 'COMPLIANCE') {
      res = await blockchainApi.verifyComplianceCertificate(row.businessId)
    }
    
    if (res && res.code === 200) {
      if (res.data) {
        ElMessage.success('验证通过，数据完整')
      } else {
        ElMessage.error('验证失败，数据可能被篡改')
      }
    }
  } catch (error) {
    ElMessage.error('验证失败')
  }
}

const getTypeTag = (type: string) => {
  const tagMap: any = {
    AUDIT: 'success',
    KEY: 'warning',
    COMPLIANCE: 'danger'
  }
  return tagMap[type] || 'info'
}

const formatTime = (timestamp: number) => {
  if (!timestamp) return '-'
  return dayjs(timestamp).format('YYYY-MM-DD HH:mm:ss')
}
</script>

<style scoped lang="less">
.blockchain-dashboard {
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

.compliance-checks {
  .compliance-item {
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
</style>
