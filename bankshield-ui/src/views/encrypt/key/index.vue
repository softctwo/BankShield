<template>
  <div class="min-h-screen bg-gray-50 p-6">
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
        <button @click="handleBatchExport" :disabled="selectedIds.length === 0" class="inline-flex items-center px-4 py-2 bg-white text-gray-700 text-sm font-medium rounded-lg border border-gray-300 hover:bg-gray-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
          <el-icon class="mr-2"><Download /></el-icon>
          批量导出 {{ selectedIds.length > 0 ? `(${selectedIds.length})` : '' }}
        </button>
        <button @click="handleBatchRotate" :disabled="selectedIds.length === 0" class="inline-flex items-center px-4 py-2 bg-yellow-600 text-white text-sm font-medium rounded-lg hover:bg-yellow-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
          <el-icon class="mr-2"><Refresh /></el-icon>
          批量轮换 {{ selectedIds.length > 0 ? `(${selectedIds.length})` : '' }}
        </button>
        <button @click="handleBatchDelete" :disabled="selectedIds.length === 0" class="inline-flex items-center px-4 py-2 bg-red-600 text-white text-sm font-medium rounded-lg hover:bg-red-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
          <el-icon class="mr-2"><Delete /></el-icon>
          批量删除 {{ selectedIds.length > 0 ? `(${selectedIds.length})` : '' }}
        </button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0 h-12 w-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-2xl text-blue-600"><Key /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">SM2密钥</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.sm2Keys }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0 h-12 w-12 bg-green-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-2xl text-green-600"><Lock /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">SM3密钥</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.sm3Keys }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0 h-12 w-12 bg-yellow-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-2xl text-yellow-600"><Lock /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">SM4密钥</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.sm4Keys }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0 h-12 w-12 bg-red-100 rounded-lg flex items-center justify-center">
            <el-icon class="text-2xl text-red-600"><Warning /></el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">即将过期</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.expiring }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 密钥列表 -->
    <div class="bg-white rounded-lg shadow overflow-hidden">
      <el-table :data="keyList" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="密钥名称" min-width="150" />
        <el-table-column prop="algorithm" label="算法类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getAlgorithmTagType(row.algorithm)" size="small">{{ row.algorithm }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="length" label="密钥长度" width="100">
          <template #default="{ row }">{{ row.length }} bits</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="expireTime" label="过期时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'danger'" size="small">
              {{ row.status === 'active' ? '正常' : '已过期' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button link type="warning" size="small" @click="handleRotate(row)">轮换</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 生成密钥对话框 -->
    <el-dialog v-model="dialogVisible" title="生成密钥" width="500px">
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-form-item label="密钥名称">
          <el-input v-model="form.name" placeholder="请输入密钥名称" />
        </el-form-item>
        <el-form-item label="算法类型">
          <el-select v-model="form.algorithm" placeholder="请选择算法类型" class="w-full">
            <el-option label="SM2（非对称加密）" value="SM2" />
            <el-option label="SM3（哈希算法）" value="SM3" />
            <el-option label="SM4（对称加密）" value="SM4" />
          </el-select>
        </el-form-item>
        <el-form-item label="密钥长度">
          <el-select v-model="form.length" placeholder="请选择密钥长度" class="w-full">
            <el-option label="128 bits" :value="128" />
            <el-option label="256 bits" :value="256" />
          </el-select>
        </el-form-item>
        <el-form-item label="有效期">
          <el-select v-model="form.validity" placeholder="请选择有效期" class="w-full">
            <el-option label="1年" :value="365" />
            <el-option label="2年" :value="730" />
            <el-option label="3年" :value="1095" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <button @click="dialogVisible = false" class="px-4 py-2 bg-gray-100 text-gray-700 text-sm font-medium rounded-lg hover:bg-gray-200">取消</button>
          <button @click="handleSubmit" class="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700">生成</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, Key, Lock, Warning, Refresh, Delete } from '@element-plus/icons-vue'

const dialogVisible = ref(false)
const formRef = ref()
const selectedIds = ref<number[]>([])

const stats = reactive({
  sm2Keys: 12,
  sm3Keys: 8,
  sm4Keys: 15,
  expiring: 3
})

const form = reactive({
  name: '',
  algorithm: 'SM2',
  length: 256,
  validity: 365
})

const keyList = ref([
  { id: 1, name: 'SM2-PROD-001', algorithm: 'SM2', length: 256, createTime: '2024-01-15 10:00:00', expireTime: '2025-01-15 10:00:00', status: 'active' },
  { id: 2, name: 'SM3-HASH-001', algorithm: 'SM3', length: 256, createTime: '2024-02-20 14:30:00', expireTime: '2025-02-20 14:30:00', status: 'active' },
  { id: 3, name: 'SM4-ENC-001', algorithm: 'SM4', length: 128, createTime: '2024-03-10 09:15:00', expireTime: '2025-03-10 09:15:00', status: 'active' },
  { id: 4, name: 'SM2-TEST-001', algorithm: 'SM2', length: 256, createTime: '2023-12-01 11:00:00', expireTime: '2024-12-01 11:00:00', status: 'expired' }
])

const handleAdd = () => {
  Object.assign(form, { name: '', algorithm: 'SM2', length: 256, validity: 365 })
  dialogVisible.value = true
}

const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleBatchExport = async () => {
  try {
    await ElMessageBox.confirm(`确定要导出选中的 ${selectedIds.value.length} 个密钥吗？`, '提示', { type: 'warning' })
    ElMessage.success('密钥导出成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('导出失败')
    }
  }
}

const handleBatchRotate = async () => {
  try {
    await ElMessageBox.confirm(`确定要轮换选中的 ${selectedIds.value.length} 个密钥吗？`, '提示', { type: 'warning' })
    ElMessage.success('密钥轮换成功')
    selectedIds.value = []
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('轮换失败')
    }
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个密钥吗？此操作不可恢复！`, '警告', { type: 'error' })
    keyList.value = keyList.value.filter(key => !selectedIds.value.includes(key.id))
    ElMessage.success('删除成功')
    selectedIds.value = []
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleView = (row: any) => {
  ElMessage.info(`查看密钥: ${row.name}`)
}

const handleRotate = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要轮换密钥 "${row.name}" 吗？`, '提示', { type: 'warning' })
    ElMessage.success('密钥轮换成功')
  } catch {}
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除密钥 "${row.name}" 吗？`, '提示', { type: 'warning' })
    const index = keyList.value.findIndex(k => k.id === row.id)
    if (index > -1) {
      keyList.value.splice(index, 1)
      ElMessage.success('删除成功')
    }
  } catch {}
}

const handleSubmit = () => {
  const newKey = {
    id: Date.now(),
    name: form.name,
    algorithm: form.algorithm,
    length: form.length,
    createTime: new Date().toLocaleString('zh-CN'),
    expireTime: new Date(Date.now() + form.validity * 24 * 60 * 60 * 1000).toLocaleString('zh-CN'),
    status: 'active'
  }
  keyList.value.unshift(newKey)
  ElMessage.success('密钥生成成功')
  dialogVisible.value = false
}

const getAlgorithmTagType = (algorithm: string) => {
  const map: Record<string, any> = {
    SM2: 'primary',
    SM3: 'success',
    SM4: 'warning'
  }
  return map[algorithm] || ''
}
</script>
