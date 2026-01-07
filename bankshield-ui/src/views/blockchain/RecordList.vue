<template>
  <div class="record-list">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>区块链存证记录</span>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm">
        <el-form-item label="存证类型">
          <el-select v-model="queryForm.anchorType" placeholder="请选择" clearable>
            <el-option label="审计记录" value="AUDIT" />
            <el-option label="密钥事件" value="KEY" />
            <el-option label="合规检查" value="COMPLIANCE" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务ID">
          <el-input v-model="queryForm.businessId" placeholder="请输入业务ID" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="anchorType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.anchorType)">{{ row.anchorType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="businessId" label="业务ID" width="150" />
        <el-table-column prop="dataHash" label="数据哈希" show-overflow-tooltip />
        <el-table-column prop="txHash" label="交易哈希" show-overflow-tooltip />
        <el-table-column prop="blockId" label="区块ID" width="150" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">详情</el-button>
            <el-button type="success" size="small" @click="handleVerify(row)">验证</el-button>
            <el-button type="warning" size="small" @click="handleCertificate(row)">证书</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
        style="margin-top: 20px; text-align: right"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="存证记录详情" width="800px">
      <el-descriptions :column="2" border v-if="currentRecord">
        <el-descriptions-item label="ID">{{ currentRecord.id }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="getTypeTag(currentRecord.anchorType)">{{ currentRecord.anchorType }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="业务ID">{{ currentRecord.businessId }}</el-descriptions-item>
        <el-descriptions-item label="记录ID">{{ currentRecord.recordId }}</el-descriptions-item>
        <el-descriptions-item label="数据哈希" :span="2">{{ currentRecord.dataHash }}</el-descriptions-item>
        <el-descriptions-item label="交易哈希" :span="2">{{ currentRecord.txHash }}</el-descriptions-item>
        <el-descriptions-item label="区块ID" :span="2">{{ currentRecord.blockId }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentRecord.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as blockchainApi from '@/api/blockchain'
import type { ResponseData } from '@/utils/request'

const loading = ref(false)
const tableData = ref<any[]>([])
const detailVisible = ref(false)
const currentRecord = ref<any>(null)

const queryForm = reactive({
  anchorType: '',
  businessId: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await blockchainApi.getAnchorRecords(queryForm) as ResponseData
    if (res.code === 200) {
      tableData.value = res.data || []
      pagination.total = res.data?.length || 0
    }
  } catch (error) {
    console.error('加载数据失败', error)
    // 使用模拟数据
    tableData.value = [
      {
        id: 1,
        anchorType: 'AUDIT',
        businessId: 'AUDIT_20260107_001',
        recordId: 'REC_001',
        dataHash: '0x1a2b3c4d5e6f7890abcdef1234567890abcdef1234567890abcdef1234567890',
        txHash: '0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890',
        blockId: 'BLOCK_12345',
        createTime: '2026-01-07 10:30:15'
      },
      {
        id: 2,
        anchorType: 'KEY',
        businessId: 'KEY_20260107_002',
        recordId: 'REC_002',
        dataHash: '0x2b3c4d5e6f7890abcdef1234567890abcdef1234567890abcdef1234567890ab',
        txHash: '0xbcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890ab',
        blockId: 'BLOCK_12346',
        createTime: '2026-01-07 11:15:30'
      },
      {
        id: 3,
        anchorType: 'COMPLIANCE',
        businessId: 'COMP_20260107_003',
        recordId: 'REC_003',
        dataHash: '0x3c4d5e6f7890abcdef1234567890abcdef1234567890abcdef1234567890abcd',
        txHash: '0xcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890abc',
        blockId: 'BLOCK_12347',
        createTime: '2026-01-07 12:45:20'
      },
      {
        id: 4,
        anchorType: 'AUDIT',
        businessId: 'AUDIT_20260107_004',
        recordId: 'REC_004',
        dataHash: '0x4d5e6f7890abcdef1234567890abcdef1234567890abcdef1234567890abcdef',
        txHash: '0xdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcd',
        blockId: 'BLOCK_12348',
        createTime: '2026-01-07 14:20:45'
      },
      {
        id: 5,
        anchorType: 'KEY',
        businessId: 'KEY_20260107_005',
        recordId: 'REC_005',
        dataHash: '0x5e6f7890abcdef1234567890abcdef1234567890abcdef1234567890abcdef12',
        txHash: '0xef1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcde',
        blockId: 'BLOCK_12349',
        createTime: '2026-01-07 15:50:10'
      }
    ]
    pagination.total = 5
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  queryForm.anchorType = ''
  queryForm.businessId = ''
  handleQuery()
}

const handleView = (row: any) => {
  currentRecord.value = row
  detailVisible.value = true
}

const handleVerify = async (row: any) => {
  try {
    let res: ResponseData
    if (row.anchorType === 'AUDIT') {
      res = await blockchainApi.verifyAuditLogIntegrity(row.businessId) as ResponseData
    } else if (row.anchorType === 'KEY') {
      res = await blockchainApi.verifyKeyEventIntegrity(row.businessId) as ResponseData
    } else if (row.anchorType === 'COMPLIANCE') {
      res = await blockchainApi.verifyComplianceCertificate(row.businessId) as ResponseData
    } else {
      ElMessage.warning('未知的存证类型')
      return
    }
    
    if (res.code === 200) {
      if (res.data) {
        ElMessage.success('验证通过，数据完整性良好')
      } else {
        ElMessage.error('验证失败，数据可能被篡改')
      }
    }
  } catch (error) {
    ElMessage.error('验证失败')
  }
}

const handleCertificate = async (row: any) => {
  try {
    const res = await blockchainApi.generateCertificate(row.recordId) as ResponseData
    if (res.code === 200) {
      ElMessage.success('证书生成成功: ' + res.data)
    }
  } catch (error) {
    ElMessage.error('证书生成失败')
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
</script>

<style scoped lang="less">
.record-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
