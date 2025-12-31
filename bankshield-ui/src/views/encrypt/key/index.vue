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
        <button @click="handleExport" class="inline-flex items-center px-4 py-2 bg-white text-gray-700 text-sm font-medium rounded-lg border border-gray-300 hover:bg-gray-50 transition-colors">
          <el-icon class="mr-2"><Download /></el-icon>
          导出密钥
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
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">密钥名称</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">算法类型</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">密钥长度</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">创建时间</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">过期时间</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">操作</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="key in keyList" :key="key.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ key.name }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium" :class="getAlgorithmClass(key.algorithm)">
                {{ key.algorithm }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ key.length }} bits</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ key.createTime }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ key.expireTime }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium" :class="key.status === 'active' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'">
                {{ key.status === 'active' ? '正常' : '已过期' }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
              <button @click="handleView(key)" class="text-blue-600 hover:text-blue-900 mr-3">查看</button>
              <button @click="handleRotate(key)" class="text-yellow-600 hover:text-yellow-900 mr-3">轮换</button>
              <button @click="handleDelete(key)" class="text-red-600 hover:text-red-900">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
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
import { Plus, Download, Key, Lock, Warning } from '@element-plus/icons-vue'

const dialogVisible = ref(false)
const formRef = ref()

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

const handleExport = () => {
  ElMessage.success('密钥导出功能开发中')
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

const getAlgorithmClass = (algorithm: string) => {
  const map: Record<string, string> = {
    SM2: 'bg-blue-100 text-blue-800',
    SM3: 'bg-green-100 text-green-800',
    SM4: 'bg-yellow-100 text-yellow-800'
  }
  return map[algorithm] || 'bg-gray-100 text-gray-800'
}
</script>
