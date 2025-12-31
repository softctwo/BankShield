<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <!-- 页面头部 -->
    <div class="mb-6 flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">角色管理</h1>
        <p class="mt-1 text-sm text-gray-500">管理系统角色和权限配置</p>
      </div>
      <button
        @click="handleAdd"
        class="inline-flex items-center px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors"
      >
        <el-icon class="mr-2"><Plus /></el-icon>
        新增角色
      </button>
    </div>

    <!-- 角色列表 -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 左侧角色列表 -->
      <div class="lg:col-span-2">
        <div class="bg-white rounded-lg shadow overflow-hidden">
          <!-- 批量操作栏 -->
          <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
            <div class="flex items-center gap-4">
              <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
              <span class="text-sm text-gray-500">已选 {{ selectedIds.length }} 条</span>
            </div>
            <div class="flex gap-2">
              <button 
                @click="handleBatchDelete" 
                :disabled="selectedIds.length === 0"
                class="inline-flex items-center px-3 py-1.5 bg-red-600 text-white text-sm font-medium rounded-lg hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                批量删除
              </button>
              <button 
                @click="handleBatchExport" 
                :disabled="selectedIds.length === 0"
                class="inline-flex items-center px-3 py-1.5 bg-green-600 text-white text-sm font-medium rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                批量导出
              </button>
            </div>
          </div>

          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left">
                  <el-checkbox v-model="selectAll" @change="handleSelectAll" />
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">角色名称</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">角色编码</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">描述</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">用户数</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">状态</th>
                <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr
                v-for="role in roleList"
                :key="role.id"
                @click="handleRowClick(role)"
                class="hover:bg-gray-50 cursor-pointer"
                :class="{ 'bg-blue-50': selectedRole?.id === role.id }"
              >
                <td class="px-6 py-4" @click.stop>
                  <el-checkbox v-model="role.selected" @change="handleSelect" />
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <div class="flex-shrink-0 h-10 w-10">
                      <div class="h-10 w-10 rounded-full flex items-center justify-center" :style="{ backgroundColor: role.color + '20' }">
                        <el-icon :style="{ color: role.color }"><UserFilled /></el-icon>
                      </div>
                    </div>
                    <div class="ml-4">
                      <div class="text-sm font-medium text-gray-900">{{ role.name }}</div>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ role.code }}</td>
                <td class="px-6 py-4 text-sm text-gray-500">{{ role.description }}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ role.userCount }}</td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span
                    class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                    :class="role.status === 'active' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'"
                  >
                    {{ role.status === 'active' ? '启用' : '禁用' }}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <button @click.stop="handleEdit(role)" class="text-blue-600 hover:text-blue-900 mr-3">编辑</button>
                  <button @click.stop="handleDelete(role)" class="text-red-600 hover:text-red-900">删除</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 右侧权限配置 -->
      <div class="lg:col-span-1">
        <div class="bg-white rounded-lg shadow p-6 sticky top-6">
          <div v-if="!selectedRole" class="text-center py-12">
            <el-icon class="text-6xl text-gray-300 mb-4"><Lock /></el-icon>
            <p class="text-gray-500">请选择一个角色查看权限</p>
          </div>
          <div v-else>
            <div class="mb-4">
              <h3 class="text-lg font-medium text-gray-900">{{ selectedRole.name }}</h3>
              <p class="mt-1 text-sm text-gray-500">{{ selectedRole.description }}</p>
            </div>
            <div class="border-t border-gray-200 pt-4">
              <div class="flex items-center justify-between mb-4">
                <h4 class="text-sm font-medium text-gray-900">权限配置</h4>
                <button
                  @click="handleSavePermissions"
                  class="text-sm text-blue-600 hover:text-blue-700 font-medium"
                >
                  保存权限
                </button>
              </div>
              <el-tree
                ref="permissionTreeRef"
                :data="permissionTree"
                show-checkbox
                node-key="id"
                :default-checked-keys="selectedPermissions"
                :props="{ label: 'name', children: 'children' }"
                class="permission-tree"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 角色表单对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="500px">
      <el-form ref="roleFormRef" :model="roleForm" :rules="roleRules" label-width="80px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="roleForm.name" placeholder="请输入角色名称" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="roleForm.code" placeholder="请输入角色编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="roleForm.description" type="textarea" :rows="3" placeholder="请输入角色描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="roleForm.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="disabled">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <button
            @click="dialogVisible = false"
            class="px-4 py-2 bg-gray-100 text-gray-700 text-sm font-medium rounded-lg hover:bg-gray-200 transition-colors"
          >
            取消
          </button>
          <button
            @click="handleSubmit"
            :disabled="submitLoading"
            class="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
          >
            {{ submitLoading ? '提交中...' : '确定' }}
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, UserFilled, Lock } from '@element-plus/icons-vue'

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const roleFormRef = ref<FormInstance>()
const permissionTreeRef = ref()
const selectAll = ref(false)

const roleForm = reactive({
  id: 0,
  name: '',
  code: '',
  description: '',
  status: 'active'
})

const roleList = ref([
  {
    id: 1,
    name: '系统管理员',
    code: 'admin',
    description: '拥有系统所有权限',
    userCount: 1,
    status: 'active',
    color: '#f56c6c',
    permissions: ['user:view', 'user:add', 'user:edit', 'user:delete', 'role:view', 'role:add'],
    selected: false
  },
  {
    id: 2,
    name: '审计员',
    code: 'audit-admin',
    description: '负责系统审计和日志查看',
    userCount: 2,
    status: 'active',
    color: '#e6a23c',
    permissions: ['audit:view', 'log:view', 'report:view'],
    selected: false
  },
  {
    id: 3,
    name: '操作员',
    code: 'operator',
    description: '负责日常数据操作',
    userCount: 5,
    status: 'active',
    color: '#409eff',
    permissions: ['data:view', 'data:edit'],
    selected: false
  }
])

const selectedRole = ref<any>(null)
const selectedPermissions = ref<string[]>([])

const selectedIds = computed(() => {
  return roleList.value.filter(role => role.selected).map(role => role.id)
})

const permissionTree = ref([
  {
    id: 'system',
    name: '系统管理',
    children: [
      { id: 'user:view', name: '用户查看' },
      { id: 'user:add', name: '用户新增' },
      { id: 'user:edit', name: '用户编辑' },
      { id: 'user:delete', name: '用户删除' },
      { id: 'role:view', name: '角色查看' },
      { id: 'role:add', name: '角色新增' }
    ]
  },
  {
    id: 'audit',
    name: '审计管理',
    children: [
      { id: 'audit:view', name: '审计查看' },
      { id: 'log:view', name: '日志查看' },
      { id: 'report:view', name: '报表查看' }
    ]
  },
  {
    id: 'data',
    name: '数据管理',
    children: [
      { id: 'data:view', name: '数据查看' },
      { id: 'data:edit', name: '数据编辑' },
      { id: 'data:export', name: '数据导出' }
    ]
  }
])

const roleRules: FormRules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_-]+$/, message: '角色编码只能包含字母、数字、下划线和横线', trigger: 'blur' }
  ],
  description: [{ required: true, message: '请输入角色描述', trigger: 'blur' }]
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(roleForm, {
    id: 0,
    name: '',
    code: '',
    description: '',
    status: 'active'
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  Object.assign(roleForm, row)
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除角色 "${row.name}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const index = roleList.value.findIndex(r => r.id === row.id)
    if (index > -1) {
      roleList.value.splice(index, 1)
      ElMessage.success('删除成功')
    }
  } catch {}
}

const handleRowClick = (row: any) => {
  selectedRole.value = row
  selectedPermissions.value = row.permissions
  nextTick(() => {
    permissionTreeRef.value?.setCheckedKeys(row.permissions)
  })
}

const handleSavePermissions = () => {
  if (!selectedRole.value) return
  const checkedKeys = permissionTreeRef.value.getCheckedKeys()
  const role = roleList.value.find(r => r.id === selectedRole.value.id)
  if (role) {
    role.permissions = checkedKeys
    ElMessage.success('权限保存成功')
  }
}

const handleSelectAll = () => {
  roleList.value.forEach(role => {
    role.selected = selectAll.value
  })
}

const handleSelect = () => {
  selectAll.value = roleList.value.every(role => role.selected)
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个角色吗？`, '批量删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    roleList.value = roleList.value.filter(role => !role.selected)
    selectAll.value = false
    ElMessage.success(`成功删除 ${selectedIds.value.length} 个角色`)
  } catch {}
}

const handleBatchExport = () => {
  const selectedRoles = roleList.value.filter(role => role.selected)
  ElMessage.success(`成功导出 ${selectedRoles.length} 个角色数据`)
}

const handleSubmit = async () => {
  if (!roleFormRef.value) return
  try {
    await roleFormRef.value.validate()
    submitLoading.value = true
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    if (isEdit.value) {
      const index = roleList.value.findIndex(r => r.id === roleForm.id)
      if (index > -1) {
        roleList.value[index] = { ...roleList.value[index], ...roleForm }
      }
      ElMessage.success('编辑成功')
    } else {
      roleList.value.push({
        id: Date.now(),
        ...roleForm,
        userCount: 0,
        color: '#409eff',
        permissions: [],
        selected: false
      })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

// 默认选择第一个角色
if (roleList.value.length > 0) {
  handleRowClick(roleList.value[0])
}
</script>

<style scoped>
.permission-tree {
  max-height: 400px;
  overflow-y: auto;
}
</style>
