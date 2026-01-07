<template>
  <div class="watermark-extract">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="mb-4">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon size="40" color="#409EFF"><Search /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalExtracts }}</div>
              <div class="stat-label">总提取次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon size="40" color="#67C23A"><CircleCheck /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.successCount }}</div>
              <div class="stat-label">成功提取</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon size="40" color="#E6A23C"><Warning /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.suspiciousCount }}</div>
              <div class="stat-label">可疑文档</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon size="40" color="#F56C6C"><Document /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.leakageCount }}</div>
              <div class="stat-label">泄露追踪</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 水印提取区域 -->
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card class="mb-4">
          <template #header>
            <span>水印提取</span>
          </template>
          <el-upload
            class="upload-area"
            drag
            action="#"
            :auto-upload="false"
            :on-change="handleFileChange"
            accept=".pdf,.doc,.docx,.xls,.xlsx,.png,.jpg,.jpeg"
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              拖拽文件到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 PDF、Word、Excel、图片等格式
              </div>
            </template>
          </el-upload>
          
          <div v-if="selectedFile" class="file-info mt-4">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="文件名">{{ selectedFile.name }}</el-descriptions-item>
              <el-descriptions-item label="大小">{{ formatFileSize(selectedFile.size) }}</el-descriptions-item>
              <el-descriptions-item label="类型">{{ selectedFile.type || '未知' }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="extracting ? 'warning' : 'info'">
                  {{ extracting ? '提取中...' : '待提取' }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
            <el-button type="primary" class="mt-4" :loading="extracting" @click="extractWatermark">
              提取水印
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="mb-4">
          <template #header>
            <span>提取结果</span>
          </template>
          <div v-if="extractResult" class="extract-result">
            <el-result
              :icon="extractResult.found ? 'success' : 'warning'"
              :title="extractResult.found ? '水印提取成功' : '未发现水印'"
            >
              <template #sub-title>
                <span v-if="extractResult.found">已成功从文档中提取水印信息</span>
                <span v-else>该文档可能未嵌入水印或水印已损坏</span>
              </template>
            </el-result>
            
            <el-descriptions v-if="extractResult.found" :column="1" border class="mt-4">
              <el-descriptions-item label="水印类型">
                <el-tag>{{ extractResult.watermarkType }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="嵌入时间">{{ extractResult.embedTime }}</el-descriptions-item>
              <el-descriptions-item label="操作用户">{{ extractResult.userName }}</el-descriptions-item>
              <el-descriptions-item label="用户部门">{{ extractResult.department }}</el-descriptions-item>
              <el-descriptions-item label="原始文档">{{ extractResult.originalDoc }}</el-descriptions-item>
              <el-descriptions-item label="水印内容">
                <el-tag type="danger">{{ extractResult.content }}</el-tag>
              </el-descriptions-item>
            </el-descriptions>
            
            <div v-if="extractResult.found" class="mt-4">
              <el-button type="danger" @click="reportLeakage">上报泄露</el-button>
              <el-button type="warning" @click="markSuspicious">标记可疑</el-button>
            </div>
          </div>
          <el-empty v-else description="请上传文件进行水印提取" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 提取历史 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>提取历史</span>
          <el-button type="primary" link @click="loadHistory">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </template>
      <el-table v-loading="loading" :data="historyList" border stripe>
        <el-table-column prop="fileName" label="文件名" min-width="180" />
        <el-table-column prop="fileType" label="文件类型" width="100" />
        <el-table-column label="提取结果" width="100">
          <template #default="{ row }">
            <el-tag :type="row.found ? 'success' : 'info'">
              {{ row.found ? '已发现' : '未发现' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="watermarkType" label="水印类型" width="120" />
        <el-table-column prop="userName" label="追踪用户" width="120" />
        <el-table-column prop="extractTime" label="提取时间" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
            <el-button v-if="row.found" link type="danger" @click="reportLeakageFromHistory(row)">上报</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="mt-4"
        @size-change="loadHistory"
        @current-change="loadHistory"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, CircleCheck, Warning, Document, UploadFilled, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const extracting = ref(false)
const selectedFile = ref<File | null>(null)
const extractResult = ref<any>(null)
const historyList = ref<any[]>([])
const total = ref(0)

const stats = reactive({
  totalExtracts: 256,
  successCount: 189,
  suspiciousCount: 23,
  leakageCount: 5
})

const queryForm = reactive({
  page: 1,
  size: 10
})

const formatFileSize = (bytes: number) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    '正常': 'success',
    '可疑': 'warning',
    '已上报': 'danger'
  }
  return map[status] || 'info'
}

const handleFileChange = (file: any) => {
  selectedFile.value = file.raw
  extractResult.value = null
}

const extractWatermark = async () => {
  if (!selectedFile.value) return

  extracting.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // 模拟提取结果
    const found = Math.random() > 0.3
    extractResult.value = {
      found,
      watermarkType: found ? '隐形水印' : null,
      embedTime: found ? '2026-01-05 14:30:22' : null,
      userName: found ? '张三' : null,
      department: found ? '风控部' : null,
      originalDoc: found ? '客户信用报告_202601.pdf' : null,
      content: found ? 'DOC-2026010514302-USR0089' : null
    }
    
    ElMessage.success(found ? '水印提取成功' : '未发现水印')
    loadHistory()
  } catch (error) {
    ElMessage.error('提取失败')
  } finally {
    extracting.value = false
  }
}

const reportLeakage = async () => {
  try {
    await ElMessageBox.confirm('确定要上报此文档为泄露文档吗？系统将启动追踪流程。', '上报泄露', { type: 'warning' })
    ElMessage.success('已上报，追踪流程已启动')
  } catch {}
}

const markSuspicious = async () => {
  try {
    await ElMessageBox.confirm('确定要将此文档标记为可疑吗？', '标记可疑', { type: 'warning' })
    ElMessage.success('已标记为可疑')
  } catch {}
}

const loadHistory = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/api/watermark/extract/history', { params: queryForm })
    if (res.code === 200) {
      historyList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    // 使用模拟数据
    historyList.value = [
      { id: 1, fileName: '客户数据报表.xlsx', fileType: 'Excel', found: true, watermarkType: '隐形水印', userName: '李四', extractTime: '2026-01-07 10:23:45', status: '已上报' },
      { id: 2, fileName: '年度财务报告.pdf', fileType: 'PDF', found: true, watermarkType: '可见水印', userName: '王五', extractTime: '2026-01-07 09:15:30', status: '可疑' },
      { id: 3, fileName: '产品说明书.docx', fileType: 'Word', found: false, watermarkType: '-', userName: '-', extractTime: '2026-01-06 16:40:12', status: '正常' },
      { id: 4, fileName: '客户合同.pdf', fileType: 'PDF', found: true, watermarkType: '隐形水印', userName: '赵六', extractTime: '2026-01-06 14:22:08', status: '正常' },
      { id: 5, fileName: '内部通知.png', fileType: '图片', found: true, watermarkType: '数字水印', userName: '孙七', extractTime: '2026-01-05 11:05:33', status: '可疑' }
    ]
    total.value = 5
  } finally {
    loading.value = false
  }
}

const viewDetail = (row: any) => {
  ElMessage.info(`查看详情: ${row.fileName}`)
}

const reportLeakageFromHistory = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要上报文档 "${row.fileName}" 为泄露文档吗？`, '上报泄露', { type: 'warning' })
    ElMessage.success('已上报')
    loadHistory()
  } catch {}
}

onMounted(() => {
  loadHistory()
})
</script>

<style scoped lang="less">
.watermark-extract {
  padding: 20px;
}

.stat-card {
  .stat-content {
    display: flex;
    align-items: center;
    gap: 16px;
  }
  .stat-info {
    .stat-value {
      font-size: 28px;
      font-weight: bold;
      color: #303133;
    }
    .stat-label {
      font-size: 14px;
      color: #909399;
    }
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.upload-area {
  width: 100%;
  :deep(.el-upload-dragger) {
    width: 100%;
  }
}

.file-info {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
}

.extract-result {
  text-align: center;
}

.mb-4 { margin-bottom: 16px; }
.mt-4 { margin-top: 16px; }
</style>
