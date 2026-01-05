<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="mb-6 flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">部门管理</h1>
        <p class="mt-1 text-sm text-gray-500">管理组织架构和部门信息</p>
      </div>
      <div class="flex gap-3">
        <button @click="handleAdd" class="inline-flex items-center px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors">
          <el-icon class="mr-2"><Plus /></el-icon>
          新增部门
        </button>
        <button @click="handleBatchExport" :disabled="selectedIds.length === 0" class="inline-flex items-center px-4 py-2 bg-white text-gray-700 text-sm font-medium rounded-lg border border-gray-300 hover:bg-gray-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
          <el-icon class="mr-2"><Download /></el-icon>
          批量导出 {{ selectedIds.length > 0 ? `(${selectedIds.length})` : '' }}
        </button>
        <button @click="handleBatchEnable" :disabled="selectedIds.length === 0" class="inline-flex items-center px-4 py-2 bg-green-600 text-white text-sm font-medium rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
          <el-icon class="mr-2"><Select /></el-icon>
          批量启用 {{ selectedIds.length > 0 ? `(${selectedIds.length})` : '' }}
        </button>
        <button @click="handleBatchDelete" :disabled="selectedIds.length === 0" class="inline-flex items-center px-4 py-2 bg-red-600 text-white text-sm font-medium rounded-lg hover:bg-red-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
          <el-icon class="mr-2"><Delete /></el-icon>
          批量删除 {{ selectedIds.length > 0 ? `(${selectedIds.length})` : '' }}
        </button>
      </div>
    </div>

    <div class="bg-white rounded-lg shadow overflow-hidden">
      <el-table :data="deptList" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="部门名称" min-width="150" />
        <el-table-column prop="code" label="部门编码" width="120" />
        <el-table-column prop="leader" label="负责人" width="100" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
              {{ row.status === 'active' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
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
import { Plus, Download, Select, Delete } from '@element-plus/icons-vue'

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const selectedIds = ref<number[]>([])

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

const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleBatchExport = async () => {
  try {
    await ElMessageBox.confirm(`确定要导出选中的 ${selectedIds.value.length} 个部门吗？`, '提示', { type: 'warning' })
    ElMessage.success('导出成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('导出失败')
    }
  }
}

const handleBatchEnable = async () => {
  try {
    await ElMessageBox.confirm(`确定要启用选中的 ${selectedIds.value.length} 个部门吗？`, '提示', { type: 'warning' })
    deptList.value.forEach(dept => {
      if (selectedIds.value.includes(dept.id)) {
        dept.status = 'active'
      }
    })
    ElMessage.success('批量启用成功')
    selectedIds.value = []
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个部门吗？此操作不可恢复！`, '警告', { type: 'error' })
    deptList.value = deptList.value.filter(dept => !selectedIds.value.includes(dept.id))
    ElMessage.success('删除成功')
    selectedIds.value = []
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
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
