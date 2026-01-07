<template>
  <div class="page-container">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="verify-card">
          <template #header><span>存证验证</span></template>
          <el-form :model="verifyForm" label-width="100px">
            <el-form-item label="验证方式">
              <el-radio-group v-model="verifyForm.method">
                <el-radio value="hash">哈希验证</el-radio>
                <el-radio value="file">文件验证</el-radio>
                <el-radio value="id">存证ID验证</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="verifyForm.method === 'hash'" label="区块链哈希">
              <el-input v-model="verifyForm.hash" placeholder="请输入区块链哈希值" />
            </el-form-item>
            <el-form-item v-if="verifyForm.method === 'file'" label="上传文件">
              <el-upload action="#" :auto-upload="false" :on-change="handleFileChange" :limit="1">
                <el-button type="primary">选择文件</el-button>
              </el-upload>
            </el-form-item>
            <el-form-item v-if="verifyForm.method === 'id'" label="存证ID">
              <el-input v-model="verifyForm.evidenceId" placeholder="请输入存证ID" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleVerify" :loading="verifying">开始验证</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="result-card">
          <template #header><span>验证结果</span></template>
          <div v-if="!verifyResult" class="empty-result">
            <el-icon :size="60" color="#c0c4cc"><Document /></el-icon>
            <p>请输入信息进行验证</p>
          </div>
          <div v-else class="verify-result">
            <div class="result-status" :class="verifyResult.valid ? 'success' : 'failed'">
              <el-icon :size="48"><CircleCheck v-if="verifyResult.valid" /><CircleClose v-else /></el-icon>
              <span>{{ verifyResult.valid ? '验证通过' : '验证失败' }}</span>
            </div>
            <el-descriptions :column="1" border v-if="verifyResult.valid">
              <el-descriptions-item label="存证标题">{{ verifyResult.title }}</el-descriptions-item>
              <el-descriptions-item label="存证类型">{{ verifyResult.type }}</el-descriptions-item>
              <el-descriptions-item label="区块链哈希">{{ verifyResult.hash }}</el-descriptions-item>
              <el-descriptions-item label="区块号">{{ verifyResult.blockNumber }}</el-descriptions-item>
              <el-descriptions-item label="存证时间">{{ verifyResult.createTime }}</el-descriptions-item>
              <el-descriptions-item label="存证人">{{ verifyResult.creator }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="history-card">
      <template #header><span>验证历史</span></template>
      <el-table :data="historyData" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="method" label="验证方式" width="120" />
        <el-table-column prop="input" label="验证输入" min-width="200" show-overflow-tooltip />
        <el-table-column prop="result" label="结果" width="100">
          <template #default="{ row }">
            <el-tag :type="row.result ? 'success' : 'danger'">{{ row.result ? '通过' : '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="verifyTime" label="验证时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, CircleCheck, CircleClose } from '@element-plus/icons-vue'

const verifying = ref(false)
const verifyForm = reactive({ method: 'hash', hash: '', evidenceId: '', file: null as File | null })
const verifyResult = ref<any>(null)

const historyData = ref([
  { id: 1, method: '哈希验证', input: '0x7a8b9c...3d4e5f', result: true, verifyTime: '2024-01-01 10:00:00' },
  { id: 2, method: 'ID验证', input: 'EVD-20240101-001', result: true, verifyTime: '2024-01-01 09:00:00' },
  { id: 3, method: '文件验证', input: 'contract.pdf', result: false, verifyTime: '2024-01-01 08:00:00' }
])

const handleFileChange = (file: any) => { verifyForm.file = file.raw }

const handleVerify = () => {
  verifying.value = true
  setTimeout(() => {
    verifying.value = false
    verifyResult.value = {
      valid: true,
      title: '审计日志存证-2024010101',
      type: '审计日志',
      hash: '0x7a8b9c...3d4e5f',
      blockNumber: 12345678,
      createTime: '2024-01-01 10:00:00',
      creator: 'admin'
    }
    ElMessage.success('验证完成')
  }, 1500)
}

const handleReset = () => {
  verifyForm.hash = ''
  verifyForm.evidenceId = ''
  verifyForm.file = null
  verifyResult.value = null
}
</script>

<style scoped lang="less">
.page-container { padding: 20px; }
.verify-card, .result-card { min-height: 350px; }
.history-card { margin-top: 20px; }
.empty-result { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 200px; color: #909399; p { margin-top: 16px; } }
.verify-result { .result-status { display: flex; flex-direction: column; align-items: center; padding: 20px; margin-bottom: 20px; border-radius: 8px; span { margin-top: 10px; font-size: 18px; font-weight: bold; } &.success { background: #f0f9eb; color: #67c23a; } &.failed { background: #fef0f0; color: #f56c6c; } } }
</style>
