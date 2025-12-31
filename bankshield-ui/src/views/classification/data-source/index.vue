<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="mb-6 flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">数据源管理</h1>
        <p class="mt-1 text-sm text-gray-500">管理和配置数据库连接信息</p>
      </div>
      <button @click="handleAdd" class="inline-flex items-center px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors">
        <el-icon class="mr-2"><Plus /></el-icon>
        新增数据源
      </button>
    </div>

    <!-- 数据源列表 -->
    <div class="bg-white rounded-lg shadow overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">数据源名称</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">类型</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">主机地址</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">端口</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">数据库</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">操作</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="source in sourceList" :key="source.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ source.name }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium" :class="getTypeClass(source.type)">
                {{ source.type }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ source.host }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ source.port }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ source.database }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium" :class="source.status === 'connected' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'">
                {{ source.status === 'connected' ? '已连接' : '未连接' }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
              <button @click="handleTest(source)" class="text-blue-600 hover:text-blue-900 mr-3">测试</button>
              <button @click="handleEdit(source)" class="text-blue-600 hover:text-blue-900 mr-3">编辑</button>
              <button @click="handleDelete(source)" class="text-red-600 hover:text-red-900">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 新增/编辑数据源对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑数据源' : '新增数据源'" width="600px">
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-form-item label="数据源名称">
          <el-input v-model="form.name" placeholder="请输入数据源名称" />
        </el-form-item>
        <el-form-item label="数据库类型">
          <el-select v-model="form.type" placeholder="请选择数据库类型" class="w-full">
            <el-option label="MySQL" value="MySQL" />
            <el-option label="PostgreSQL" value="PostgreSQL" />
            <el-option label="Oracle" value="Oracle" />
            <el-option label="SQL Server" value="SQL Server" />
          </el-select>
        </el-form-item>
        <el-form-item label="主机地址">
          <el-input v-model="form.host" placeholder="例如：localhost" />
        </el-form-item>
        <el-form-item label="端口">
          <el-input v-model="form.port" placeholder="例如：3306" />
        </el-form-item>
        <el-form-item label="数据库名">
          <el-input v-model="form.database" placeholder="请输入数据库名" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <button @click="dialogVisible = false" class="px-4 py-2 bg-gray-100 text-gray-700 text-sm font-medium rounded-lg hover:bg-gray-200">取消</button>
          <button @click="handleSubmit" class="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700">确定</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const form = reactive({
  name: '',
  type: 'MySQL',
  host: 'localhost',
  port: '3306',
  database: '',
  username: '',
  password: ''
})

const sourceList = ref([
  { id: 1, name: '核心业务库', type: 'MySQL', host: '192.168.1.100', port: '3306', database: 'core_db', status: 'connected' },
  { id: 2, name: '客户信息库', type: 'PostgreSQL', host: '192.168.1.101', port: '5432', database: 'customer_db', status: 'connected' },
  { id: 3, name: '交易记录库', type: 'Oracle', host: '192.168.1.102', port: '1521', database: 'transaction_db', status: 'connected' },
  { id: 4, name: '测试数据库', type: 'MySQL', host: '192.168.1.103', port: '3306', database: 'test_db', status: 'disconnected' }
])

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, { name: '', type: 'MySQL', host: 'localhost', port: '3306', database: '', username: '', password: '' })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleTest = (row: any) => {
  ElMessage.success(`数据源 "${row.name}" 连接测试成功`)
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除数据源 "${row.name}" 吗？`, '提示', { type: 'warning' })
    const index = sourceList.value.findIndex(s => s.id === row.id)
    if (index > -1) {
      sourceList.value.splice(index, 1)
      ElMessage.success('删除成功')
    }
  } catch {}
}

const handleSubmit = () => {
  if (isEdit.value) {
    ElMessage.success('编辑成功')
  } else {
    sourceList.value.unshift({
      id: Date.now(),
      ...form,
      status: 'disconnected'
    })
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
}

const getTypeClass = (type: string) => {
  const map: Record<string, string> = {
    'MySQL': 'bg-blue-100 text-blue-800',
    'PostgreSQL': 'bg-green-100 text-green-800',
    'Oracle': 'bg-red-100 text-red-800',
    'SQL Server': 'bg-yellow-100 text-yellow-800'
  }
  return map[type] || 'bg-gray-100 text-gray-800'
}
</script>
