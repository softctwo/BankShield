<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="mb-6 flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">菜单管理</h1>
        <p class="mt-1 text-sm text-gray-500">管理系统菜单和路由配置</p>
      </div>
      <button @click="handleAdd" class="inline-flex items-center px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors">
        <el-icon class="mr-2"><Plus /></el-icon>
        新增菜单
      </button>
    </div>

    <div class="bg-white rounded-lg shadow overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">菜单名称</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">图标</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">路由路径</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">排序</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">操作</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="menu in menuList" :key="menu.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ menu.name }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
              <el-icon><component :is="menu.icon" /></el-icon>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ menu.path }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ menu.sort }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                {{ menu.status === 'active' ? '显示' : '隐藏' }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
              <button @click="handleEdit(menu)" class="text-blue-600 hover:text-blue-900 mr-3">编辑</button>
              <button @click="handleDelete(menu)" class="text-red-600 hover:text-red-900">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑菜单' : '新增菜单'" width="500px">
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-form-item label="菜单名称">
          <el-input v-model="form.name" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="路由路径">
          <el-input v-model="form.path" placeholder="请输入路由路径" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
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
import { Plus, DataBoard, User, Lock } from '@element-plus/icons-vue'

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const form = reactive({
  id: 0,
  name: '',
  icon: 'DataBoard',
  path: '',
  sort: 0,
  status: 'active'
})

const menuList = ref([
  { id: 1, name: '数据大屏', icon: 'DataBoard', path: '/dashboard', sort: 1, status: 'active' },
  { id: 2, name: '用户管理', icon: 'User', path: '/system/user', sort: 2, status: 'active' },
  { id: 3, name: '密钥管理', icon: 'Lock', path: '/encrypt/key', sort: 3, status: 'active' }
])

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: 0, name: '', icon: 'DataBoard', path: '', sort: 0, status: 'active' })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除菜单 "${row.name}" 吗？`, '提示', { type: 'warning' })
    const index = menuList.value.findIndex(m => m.id === row.id)
    if (index > -1) {
      menuList.value.splice(index, 1)
      ElMessage.success('删除成功')
    }
  } catch {}
}

const handleSubmit = () => {
  if (isEdit.value) {
    const index = menuList.value.findIndex(m => m.id === form.id)
    if (index > -1) {
      menuList.value[index] = { ...form }
    }
    ElMessage.success('编辑成功')
  } else {
    menuList.value.push({ id: Date.now(), ...form })
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
}
</script>
