<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="mb-6 flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">部门管理</h1>
        <p class="mt-1 text-sm text-gray-500">管理组织架构和部门信息</p>
      </div>
      <button
        @click="handleAdd"
        class="inline-flex items-center px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors"
      >
        <el-icon class="mr-2"><Plus /></el-icon>
        新增部门
      </button>
    </div>

    <div class="bg-white rounded-lg shadow overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">部门名称</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">部门编码</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">负责人</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">联系电话</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">操作</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="dept in deptList" :key="dept.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="flex items-center">
                <div class="text-sm font-medium text-gray-900">{{ dept.name }}</div>
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ dept.code }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ dept.leader }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ dept.phone }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                {{ dept.status === 'active' ? '启用' : '禁用' }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
              <button @click="handleEdit(dept)" class="text-blue-600 hover:text-blue-900 mr-3">编辑</button>
              <button @click="handleDelete(dept)" class="text-red-600 hover:text-red-900">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑部门' : '新增部门'" width="500px">
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-form-item label="部门名称">
          <el-input v-model="form.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="部门编码">
          <el-input v-model="form.code" placeholder="请输入部门编码" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="form.leader" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
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
  id: 0,
  name: '',
  code: '',
  leader: '',
  phone: '',
  status: 'active'
})

const deptList = ref([
  { id: 1, name: '技术部', code: 'TECH', leader: '张三', phone: '13800138001', status: 'active' },
  { id: 2, name: '财务部', code: 'FIN', leader: '李四', phone: '13800138002', status: 'active' },
  { id: 3, name: '人事部', code: 'HR', leader: '王五', phone: '13800138003', status: 'active' }
])

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: 0, name: '', code: '', leader: '', phone: '', status: 'active' })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除部门 "${row.name}" 吗？`, '提示', { type: 'warning' })
    const index = deptList.value.findIndex(d => d.id === row.id)
    if (index > -1) {
      deptList.value.splice(index, 1)
      ElMessage.success('删除成功')
    }
  } catch {}
}

const handleSubmit = () => {
  if (isEdit.value) {
    const index = deptList.value.findIndex(d => d.id === form.id)
    if (index > -1) {
      deptList.value[index] = { ...form }
    }
    ElMessage.success('编辑成功')
  } else {
    deptList.value.push({ id: Date.now(), ...form })
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
}
</script>
