<template>
  <div class="blockchain-explorer">
    <el-card class="header-card">
      <div class="header-content">
        <h2>区块链浏览器</h2>
        <p class="subtitle">基于SM3哈希算法的审计日志区块链</p>
      </div>
    </el-card>

    <!-- 统计信息 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon chain-height">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.chainHeight || 0 }}</div>
              <div class="stat-label">链高度</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total-blocks">
              <el-icon><Grid /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalBlocks || 0 }}</div>
              <div class="stat-label">总区块数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total-logs">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalLogs || 0 }}</div>
              <div class="stat-label">总日志数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon valid-blocks">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.validBlocks || 0 }}</div>
              <div class="stat-label">有效区块</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作栏 -->
    <el-card class="toolbar-card">
      <div class="toolbar">
        <div class="left-actions">
          <el-button type="primary" @click="handleVerifyChain" :icon="Check" :loading="verifying">
            验证区块链
          </el-button>
          <el-button @click="handleRefresh" :icon="Refresh">刷新</el-button>
        </div>
        <div class="right-search">
          <el-input
            v-model="searchBlockIndex"
            placeholder="输入区块索引搜索"
            style="width: 250px"
            @keyup.enter="handleSearchBlock"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearchBlock"></el-button>
            </template>
          </el-input>
        </div>
      </div>
    </el-card>

    <!-- 区块列表 -->
    <el-card class="table-card">
      <el-table :data="blockList" v-loading="loading" stripe>
        <el-table-column prop="blockIndex" label="区块索引" width="100" sortable></el-table-column>
        <el-table-column prop="blockHash" label="区块哈希" width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="hash-text">{{ row.blockHash }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="previousHash" label="前置哈希" width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="hash-text">{{ row.previousHash }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="timestamp" label="时间戳" width="180">
          <template #default="{ row }">
            {{ formatTimestamp(row.timestamp) }}
          </template>
        </el-table-column>
        <el-table-column prop="logCount" label="日志数" width="100" align="center"></el-table-column>
        <el-table-column prop="miner" label="创建者" width="120"></el-table-column>
        <el-table-column prop="isValid" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isValid === 1 ? 'success' : 'danger'">
              {{ row.isValid === 1 ? '有效' : '无效' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)" :icon="View">详情</el-button>
            <el-button link type="success" @click="handleVerifyBlock(row)" :icon="Check">验证</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadBlocks"
          @current-change="loadBlocks"
        />
      </div>
    </el-card>

    <!-- 区块详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="区块详情"
      width="900px"
      @close="handleDetailDialogClose"
    >
      <div v-if="currentBlock" class="block-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="区块索引">{{ currentBlock.blockIndex }}</el-descriptions-item>
          <el-descriptions-item label="创建者">{{ currentBlock.miner }}</el-descriptions-item>
          <el-descriptions-item label="区块哈希" :span="2">
            <span class="hash-text">{{ currentBlock.blockHash }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="前置哈希" :span="2">
            <span class="hash-text">{{ currentBlock.previousHash }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="Merkle根" :span="2">
            <span class="hash-text">{{ currentBlock.merkleRoot }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="时间戳">{{ formatTimestamp(currentBlock.timestamp) }}</el-descriptions-item>
          <el-descriptions-item label="随机数">{{ currentBlock.nonce }}</el-descriptions-item>
          <el-descriptions-item label="日志数量">{{ currentBlock.logCount }}</el-descriptions-item>
          <el-descriptions-item label="区块大小">{{ currentBlock.blockSize || 0 }} 字节</el-descriptions-item>
          <el-descriptions-item label="数字签名" :span="2">
            <span class="hash-text">{{ currentBlock.signature }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentBlock.isValid === 1 ? 'success' : 'danger'">
              {{ currentBlock.isValid === 1 ? '有效' : '无效' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentBlock.createTime }}</el-descriptions-item>
        </el-descriptions>

        <div class="block-actions" style="margin-top: 20px">
          <el-button type="primary" @click="handleVerifyCurrentBlock">验证此区块</el-button>
          <el-button @click="handleViewPreviousBlock" v-if="currentBlock.blockIndex > 0">
            查看前一个区块
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 验证结果对话框 -->
    <el-dialog
      v-model="verifyDialogVisible"
      title="验证结果"
      width="700px"
    >
      <div v-if="verifyResult" class="verify-result">
        <el-result
          :icon="verifyResult.success ? 'success' : 'error'"
          :title="verifyResult.message"
        >
          <template #extra>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="总区块数">{{ verifyResult.totalBlocks }}</el-descriptions-item>
              <el-descriptions-item label="有效区块">{{ verifyResult.validBlocks }}</el-descriptions-item>
              <el-descriptions-item label="无效区块">{{ verifyResult.invalidBlocks }}</el-descriptions-item>
              <el-descriptions-item label="验证耗时">{{ verifyResult.duration }} ms</el-descriptions-item>
            </el-descriptions>

            <div v-if="verifyResult.errors && verifyResult.errors.length > 0" style="margin-top: 20px">
              <h4>错误详情：</h4>
              <el-table :data="verifyResult.errors" border>
                <el-table-column prop="blockIndex" label="区块索引" width="120"></el-table-column>
                <el-table-column prop="blockId" label="区块ID" width="120"></el-table-column>
                <el-table-column prop="error" label="错误信息"></el-table-column>
              </el-table>
            </div>
          </template>
        </el-result>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  TrendCharts, Grid, Document, CircleCheck, Check, Refresh, 
  Search, View 
} from '@element-plus/icons-vue'
import axios from 'axios'
import dayjs from 'dayjs'

interface Block {
  id: number
  blockIndex: number
  blockHash: string
  previousHash: string
  timestamp: number
  nonce: number
  merkleRoot: string
  logCount: number
  blockSize: number
  miner: string
  signature: string
  isValid: number
  createTime: string
}

interface Statistics {
  chainHeight: number
  totalBlocks: number
  validBlocks: number
  invalidBlocks: number
  totalLogs: number
  latestBlockIndex: number
  latestBlockHash: string
  latestBlockTime: number
}

const loading = ref(false)
const verifying = ref(false)
const detailDialogVisible = ref(false)
const verifyDialogVisible = ref(false)
const searchBlockIndex = ref('')

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const blockList = ref<Block[]>([])
const statistics = ref<Statistics>({} as Statistics)
const currentBlock = ref<Block | null>(null)
const verifyResult = ref<any>(null)

const formatTimestamp = (timestamp: number) => {
  if (!timestamp) return '-'
  return dayjs(timestamp).format('YYYY-MM-DD HH:mm:ss')
}

const loadStatistics = async () => {
  try {
    const response = await axios.get('/api/blockchain/statistics')
    if (response.data.code === 200) {
      statistics.value = response.data.data
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

const loadBlocks = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/blockchain/blocks', {
      params: {
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize
      }
    })
    
    if (response.data.code === 200) {
      const data = response.data.data
      blockList.value = data.records
      pagination.total = data.total
    } else {
      ElMessage.error(response.data.message || '加载区块列表失败')
    }
  } catch (error) {
    console.error('加载区块列表失败:', error)
    ElMessage.error('加载区块列表失败')
  } finally {
    loading.value = false
  }
}

const handleRefresh = () => {
  loadStatistics()
  loadBlocks()
  ElMessage.success('刷新成功')
}

const handleSearchBlock = async () => {
  if (!searchBlockIndex.value) {
    ElMessage.warning('请输入区块索引')
    return
  }
  
  try {
    const response = await axios.get(`/api/blockchain/blocks/index/${searchBlockIndex.value}`)
    if (response.data.code === 200) {
      currentBlock.value = response.data.data
      detailDialogVisible.value = true
    } else {
      ElMessage.error(response.data.message || '区块不存在')
    }
  } catch (error) {
    console.error('搜索区块失败:', error)
    ElMessage.error('搜索区块失败')
  }
}

const handleViewDetail = async (row: Block) => {
  try {
    const response = await axios.get(`/api/blockchain/blocks/${row.id}`)
    if (response.data.code === 200) {
      currentBlock.value = response.data.data.block
      detailDialogVisible.value = true
    } else {
      ElMessage.error(response.data.message || '获取区块详情失败')
    }
  } catch (error) {
    console.error('获取区块详情失败:', error)
    ElMessage.error('获取区块详情失败')
  }
}

const handleVerifyBlock = async (row: Block) => {
  try {
    const response = await axios.post(`/api/blockchain/verify/block/${row.id}`)
    if (response.data.code === 200) {
      const result = response.data.data
      if (result.isValid) {
        ElMessage.success('区块验证通过')
      } else {
        ElMessage.error('区块验证失败')
      }
    } else {
      ElMessage.error(response.data.message || '验证失败')
    }
  } catch (error) {
    console.error('验证区块失败:', error)
    ElMessage.error('验证失败')
  }
}

const handleVerifyChain = async () => {
  try {
    await ElMessageBox.confirm('确定要验证整条区块链吗？这可能需要一些时间。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    verifying.value = true
    const response = await axios.post('/api/blockchain/verify/chain')
    
    if (response.data.code === 200) {
      verifyResult.value = response.data.data
      verifyDialogVisible.value = true
    } else {
      ElMessage.error(response.data.message || '验证失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('验证区块链失败:', error)
      ElMessage.error('验证失败')
    }
  } finally {
    verifying.value = false
  }
}

const handleVerifyCurrentBlock = async () => {
  if (!currentBlock.value) return
  await handleVerifyBlock(currentBlock.value)
}

const handleViewPreviousBlock = async () => {
  if (!currentBlock.value || currentBlock.value.blockIndex === 0) return
  
  try {
    const response = await axios.get(`/api/blockchain/blocks/index/${currentBlock.value.blockIndex - 1}`)
    if (response.data.code === 200) {
      currentBlock.value = response.data.data
    } else {
      ElMessage.error('获取前一个区块失败')
    }
  } catch (error) {
    console.error('获取前一个区块失败:', error)
    ElMessage.error('获取前一个区块失败')
  }
}

const handleDetailDialogClose = () => {
  currentBlock.value = null
}

onMounted(() => {
  loadStatistics()
  loadBlocks()
})
</script>

<style scoped lang="less">
.blockchain-explorer {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
  
  .header-content {
    h2 {
      margin: 0 0 10px 0;
      font-size: 24px;
      color: #303133;
    }
    
    .subtitle {
      margin: 0;
      color: #909399;
      font-size: 14px;
    }
  }
}

.stats-row {
  margin-bottom: 20px;
  
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
        font-size: 28px;
        margin-right: 15px;
        
        &.chain-height {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
        }
        
        &.total-blocks {
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          color: white;
        }
        
        &.total-logs {
          background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
          color: white;
        }
        
        &.valid-blocks {
          background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
          color: white;
        }
      }
      
      .stat-info {
        flex: 1;
        
        .stat-value {
          font-size: 28px;
          font-weight: bold;
          color: #303133;
          line-height: 1;
          margin-bottom: 8px;
        }
        
        .stat-label {
          font-size: 14px;
          color: #909399;
        }
      }
    }
  }
}

.toolbar-card {
  margin-bottom: 20px;
  
  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .left-actions {
      display: flex;
      gap: 10px;
    }
  }
}

.table-card {
  .hash-text {
    font-family: 'Courier New', monospace;
    font-size: 12px;
    color: #606266;
  }
  
  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}

.block-detail {
  .hash-text {
    font-family: 'Courier New', monospace;
    font-size: 12px;
    color: #606266;
    word-break: break-all;
  }
  
  .block-actions {
    display: flex;
    gap: 10px;
  }
}

.verify-result {
  h4 {
    margin: 0 0 10px 0;
    color: #303133;
  }
}
</style>
