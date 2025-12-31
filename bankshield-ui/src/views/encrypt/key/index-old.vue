<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <!-- 页面头部 -->
    <div class="mb-6 flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">密钥管理</h1>
        <p class="mt-1 text-sm text-gray-500">管理系统加密密钥，支持国密SM2/SM3/SM4算法</p>
      </div>
      <div class="flex gap-3">
        <button @click="handleAdd" class="inline-flex items-center px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors">
          <el-icon class="mr-2"><Plus /></el-icon>
          生成密钥
        </button>
        <button @click="handleExport" class="inline-flex items-center px-4 py-2 bg-white text-gray-700 text-sm font-medium rounded-lg border border-gray-300 hover:bg-gray-50 transition-colors">
          <el-icon class="mr-2"><Download /></el-icon>
          导出密钥
        </button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon sm2">
              <el-icon><Key /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.sm2Keys }}</div>
              <div class="stat-label">SM2密钥</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon sm3">
              <el-icon><Lock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.sm3Keys }}</div>
              <div class="stat-label">SM3密钥</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon sm4">
              <el-icon><Shield /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.sm4Keys }}</div>
              <div class="stat-label">SM4密钥</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon expired">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.expiredKeys }}</div>
              <div class="stat-label">即将过期</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="密钥名称">
          <el-input
            v-model="searchForm.keyName"
            placeholder="请输入密钥名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="算法类型">
          <el-select
            v-model="searchForm.algorithm"
            placeholder="请选择算法类型"
            clearable
            style="width: 150px"
          >
            <el-option label="SM2" value="SM2" />
            <el-option label="SM3" value="SM3" />
            <el-option label="SM4" value="SM4" />
            <el-option label="AES" value="AES" />
            <el-option label="RSA" value="RSA" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="正常" value="active" />
            <el-option label="即将过期" value="expiring" />
            <el-option label="已过期" value="expired" />
            <el-option label="已禁用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">
            搜索
          </el-button>
          <el-button :icon="Refresh" @click="handleReset">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 密钥列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="keyList"
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="keyName" label="密钥名称" min-width="150">
          <template #default="{ row }">
            <div class="key-info">
              <el-icon class="key-icon" :class="getAlgorithmClass(row.algorithm)">
                <Key />
              </el-icon>
              <div>
                <div class="key-name">{{ row.keyName }}</div>
                <div class="key-id">{{ row.keyId }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="algorithm" label="算法类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getAlgorithmTagType(row.algorithm)" size="small">
              {{ row.algorithm }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="keySize" label="密钥长度" width="100" />
        <el-table-column prop="usageCount" label="使用次数" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column prop="expireTime" label="过期时间" width="160">
          <template #default="{ row }">
            <span :class="{ 'text-danger': isExpiring(row.expireTime) }">
              {{ row.expireTime }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              :icon="View"
              @click="handleView(row)"
            >
              查看
            </el-button>
            <el-button
              type="warning"
              size="small"
              :icon="RefreshRight"
              @click="handleRotate(row)"
            >
              轮换
            </el-button>
            <el-button
              type="danger"
              size="small"
              :icon="Delete"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 生成密钥对话框 -->
    <el-dialog
      v-model="generateDialogVisible"
      title="生成新密钥"
      width="600px"
      @close="handleGenerateDialogClose"
    >
      <el-form
        ref="keyFormRef"
        :model="keyForm"
        :rules="keyRules"
        label-width="100px"
      >
        <el-form-item label="密钥名称" prop="keyName">
          <el-input
            v-model="keyForm.keyName"
            placeholder="请输入密钥名称"
          />
        </el-form-item>
        <el-form-item label="算法类型" prop="algorithm">
          <el-radio-group v-model="keyForm.algorithm">
            <el-radio label="SM2">SM2（非对称加密）</el-radio>
            <el-radio label="SM3">SM3（杂凑算法）</el-radio>
            <el-radio label="SM4">SM4（对称加密）</el-radio>
            <el-radio label="AES">AES（对称加密）</el-radio>
            <el-radio label="RSA">RSA（非对称加密）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="密钥长度" prop="keySize">
          <el-select v-model="keyForm.keySize" placeholder="请选择密钥长度">
            <el-option
              v-for="size in getKeySizeOptions(keyForm.algorithm)"
              :key="size.value"
              :label="size.label"
              :value="size.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="有效期" prop="expireDays">
          <el-input-number
            v-model="keyForm.expireDays"
            :min="30"
            :max="3650"
            placeholder="请输入有效期天数"
          />
          <span style="margin-left: 8px; color: #909399;">天</span>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="keyForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入密钥描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="generateDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="generateLoading" @click="handleGenerate">
            生成密钥
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 密钥详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="密钥详情"
      width="800px"
    >
      <div v-if="selectedKey" class="key-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="密钥名称">
            {{ selectedKey.keyName }}
          </el-descriptions-item>
          <el-descriptions-item label="密钥ID">
            {{ selectedKey.keyId }}
          </el-descriptions-item>
          <el-descriptions-item label="算法类型">
            <el-tag :type="getAlgorithmTagType(selectedKey.algorithm)">
              {{ selectedKey.algorithm }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="密钥长度">
            {{ selectedKey.keySize }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ selectedKey.createTime }}
          </el-descriptions-item>
          <el-descriptions-item label="过期时间">
            {{ selectedKey.expireTime }}
          </el-descriptions-item>
          <el-descriptions-item label="使用次数">
            {{ selectedKey.usageCount }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(selectedKey.status)">
              {{ getStatusText(selectedKey.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">
            {{ selectedKey.description || '暂无描述' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <div class="key-actions">
          <el-button type="primary" :icon="Download" @click="handleDownloadKey">
            下载密钥
          </el-button>
          <el-button type="warning" :icon="RefreshRight" @click="handleRotateKey">
            密钥轮换
          </el-button>
          <el-button type="success" :icon="CopyDocument" @click="handleCopyKey">
            复制公钥
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Plus,
  Download,
  Search,
  Refresh,
  Key,
  Lock,
  Shield,
  Warning,
  View,
  RefreshRight,
  Delete,
  CopyDocument
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const generateDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const generateLoading = ref(false)
const keyFormRef = ref<FormInstance>()

// 搜索表单
const searchForm = reactive({
  keyName: '',
  algorithm: '',
  status: ''
})

// 密钥表单
const keyForm = reactive({
  keyName: '',
  algorithm: 'SM2',
  keySize: 256,
  expireDays: 365,
  description: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 统计数据
const stats = ref({
  sm2Keys: 12,
  sm3Keys: 8,
  sm4Keys: 15,
  expiredKeys: 3
})

// 密钥列表
const keyList = ref([
  {
    id: 1,
    keyName: '主数据加密密钥',
    keyId: 'KEY-2024-001',
    algorithm: 'SM2',
    keySize: 256,
    usageCount: 1250,
    createTime: '2024-01-15 10:30:00',
    expireTime: '2025-01-15 10:30:00',
    status: 'active',
    description: '用于主要数据加密的SM2密钥对'
  },
  {
    id: 2,
    keyName: '传输层加密密钥',
    keyId: 'KEY-2024-002',
    algorithm: 'SM4',
    keySize: 128,
    usageCount: 3420,
    createTime: '2024-02-20 14:15:00',
    expireTime: '2025-02-20 14:15:00',
    status: 'active',
    description: '用于数据传输加密的SM4密钥'
  },
  {
    id: 3,
    keyName: '完整性校验密钥',
    keyId: 'KEY-2024-003',
    algorithm: 'SM3',
    keySize: 256,
    usageCount: 890,
    createTime: '2024-03-10 09:20:00',
    expireTime: '2025-03-10 09:20:00',
    status: 'expiring',
    description: '用于数据完整性校验的SM3密钥'
  }
])

// 选中的密钥
const selectedKeys = ref([])
const selectedKey = ref(null)

// 表单验证规则
const keyRules: FormRules = {
  keyName: [
    { required: true, message: '请输入密钥名称', trigger: 'blur' }
  ],
  algorithm: [
    { required: true, message: '请选择算法类型', trigger: 'change' }
  ],
  keySize: [
    { required: true, message: '请选择密钥长度', trigger: 'change' }
  ],
  expireDays: [
    { required: true, message: '请输入有效期', trigger: 'blur' }
  ]
}

// 方法
const handleAdd = () => {
  resetKeyForm()
  generateDialogVisible.value = true
}

const handleView = (row: any) => {
  selectedKey.value = row
  detailDialogVisible.value = true
}

const handleRotate = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要轮换密钥 "${row.keyName}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    ElMessage.success('密钥轮换成功')
  } catch {
    // 用户取消
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除密钥 "${row.keyName}" 吗？此操作不可恢复！`, '警告', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    const index = keyList.value.findIndex(key => key.id === row.id)
    if (index > -1) {
      keyList.value.splice(index, 1)
      pagination.total--
      ElMessage.success('删除成功')
    }
  } catch {
    // 用户取消
  }
}

const handleSearch = () => {
  ElMessage.success('搜索完成')
}

const handleReset = () => {
  Object.assign(searchForm, {
    keyName: '',
    algorithm: '',
    status: ''
  })
  ElMessage.success('重置成功')
}

const handleExport = () => {
  ElMessage.success('导出成功')
}

const handleSelectionChange = (selection: any[]) => {
  selectedKeys.value = selection
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  loadKeyList()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadKeyList()
}

const handleGenerate = async () => {
  if (!keyFormRef.value) return
  
  try {
    const valid = await keyFormRef.value.validate()
    if (!valid) return
    
    generateLoading.value = true
    
    // 模拟生成密钥
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    const newKey = {
      id: Date.now(),
      keyName: keyForm.keyName,
      keyId: `KEY-${new Date().getFullYear()}-${String(keyList.value.length + 1).padStart(3, '0')}`,
      algorithm: keyForm.algorithm,
      keySize: keyForm.keySize,
      usageCount: 0,
      createTime: new Date().toLocaleString(),
      expireTime: new Date(Date.now() + keyForm.expireDays * 24 * 60 * 60 * 1000).toLocaleString(),
      status: 'active',
      description: keyForm.description
    }
    
    keyList.value.unshift(newKey)
    pagination.total++
    
    ElMessage.success('密钥生成成功')
    generateDialogVisible.value = false
  } catch (error) {
    ElMessage.error('密钥生成失败')
  } finally {
    generateLoading.value = false
  }
}

const handleGenerateDialogClose = () => {
  resetKeyForm()
}

const handleDownloadKey = () => {
  ElMessage.success('密钥下载成功')
}

const handleRotateKey = () => {
  ElMessage.success('密钥轮换成功')
}

const handleCopyKey = () => {
  ElMessage.success('公钥复制成功')
}

const resetKeyForm = () => {
  Object.assign(keyForm, {
    keyName: '',
    algorithm: 'SM2',
    keySize: 256,
    expireDays: 365,
    description: ''
  })
  keyFormRef.value?.resetFields()
}

const loadKeyList = () => {
  loading.value = true
  setTimeout(() => {
    pagination.total = keyList.value.length
    loading.value = false
  }, 500)
}

const getAlgorithmClass = (algorithm: string) => {
  const classMap: Record<string, string> = {
    'SM2': 'sm2',
    'SM3': 'sm3',
    'SM4': 'sm4',
    'AES': 'aes',
    'RSA': 'rsa'
  }
  return classMap[algorithm] || ''
}

const getAlgorithmTagType = (algorithm: string) => {
  const typeMap: Record<string, string> = {
    'SM2': 'danger',
    'SM3': 'warning',
    'SM4': 'success',
    'AES': 'primary',
    'RSA': 'info'
  }
  return typeMap[algorithm] || ''
}

const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    'active': 'success',
    'expiring': 'warning',
    'expired': 'danger',
    'disabled': 'info'
  }
  return typeMap[status] || ''
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'active': '正常',
    'expiring': '即将过期',
    'expired': '已过期',
    'disabled': '已禁用'
  }
  return textMap[status] || status
}

const isExpiring = (expireTime: string) => {
  const expire = new Date(expireTime).getTime()
  const now = Date.now()
  const daysUntilExpire = (expire - now) / (1000 * 60 * 60 * 24)
  return daysUntilExpire <= 30 && daysUntilExpire > 0
}

const getKeySizeOptions = (algorithm: string) => {
  const options: Record<string, Array<{ label: string; value: number }>> = {
    'SM2': [{ label: '256位', value: 256 }],
    'SM3': [{ label: '256位', value: 256 }],
    'SM4': [{ label: '128位', value: 128 }, { label: '256位', value: 256 }],
    'AES': [{ label: '128位', value: 128 }, { label: '192位', value: 192 }, { label: '256位', value: 256 }],
    'RSA': [{ label: '2048位', value: 2048 }, { label: '3072位', value: 3072 }, { label: '4096位', value: 4096 }]
  }
  return options[algorithm] || []
}

// 生命周期
onMounted(() => {
  loadKeyList()
})
</script>

<style scoped lang="less">
.key-management {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .header-title {
      h2 {
        margin: 0 0 8px 0;
        color: #303133;
        font-size: 20px;
        font-weight: 600;
      }

      p {
        margin: 0;
        color: #909399;
        font-size: 14px;
      }
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .stats-row {
    margin-bottom: 20px;

    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        padding: 10px 0;

        .stat-icon {
          width: 50px;
          height: 50px;
          border-radius: 10px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 16px;

          .el-icon {
            font-size: 24px;
            color: white;
          }

          &.sm2 {
            background: linear-gradient(135deg, #f56c6c 0%, #e74c3c 100%);
          }

          &.sm3 {
            background: linear-gradient(135deg, #e6a23c 0%, #f39c12 100%);
          }

          &.sm4 {
            background: linear-gradient(135deg, #67c23a 0%, #27ae60 100%);
          }

          &.expired {
            background: linear-gradient(135deg, #909399 0%, #7f8c8d 100%);
          }
        }

        .stat-info {
          flex: 1;

          .stat-number {
            font-size: 24px;
            font-weight: bold;
            color: #303133;
            line-height: 1;
          }

          .stat-label {
            font-size: 12px;
            color: #909399;
            margin-top: 4px;
          }
        }
      }
    }
  }

  .search-card {
    margin-bottom: 20px;

    .search-form {
      margin: 0;
    }
  }

  .table-card {
    .key-info {
      display: flex;
      align-items: center;
      gap: 12px;

      .key-icon {
        font-size: 20px;

        &.sm2 { color: #f56c6c; }
        &.sm3 { color: #e6a23c; }
        &.sm4 { color: #67c23a; }
        &.aes { color: #409eff; }
        &.rsa { color: #909399; }
      }

      .key-name {
        font-weight: 500;
        color: #303133;
      }

      .key-id {
        font-size: 12px;
        color: #909399;
      }
    }

    .text-danger {
      color: #f56c6c;
    }

    .pagination-wrapper {
      display: flex;
      justify-content: center;
      margin-top: 20px;
    }
  }

  .key-detail {
    .key-actions {
      display: flex;
      gap: 12px;
      justify-content: center;
      margin-top: 20px;
    }
  }
}
</style>
