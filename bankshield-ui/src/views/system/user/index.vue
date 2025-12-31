<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <!-- 页面头部 -->
    <div class="mb-6 flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">用户管理</h1>
        <p class="mt-1 text-sm text-gray-500">管理系统用户账号、角色和权限</p>
      </div>
      <div class="flex gap-3">
        <button
          @click="handleAdd"
          class="inline-flex items-center px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors"
        >
          <el-icon class="mr-2"><Plus /></el-icon>
          新增用户
        </button>
        <button
          @click="handleExport"
          class="inline-flex items-center px-4 py-2 bg-white text-gray-700 text-sm font-medium rounded-lg border border-gray-300 hover:bg-gray-50 transition-colors"
        >
          <el-icon class="mr-2"><Download /></el-icon>
          导出
        </button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="mb-6 bg-white rounded-lg shadow p-4">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">用户名</label>
          <input
            v-model="searchForm.username"
            type="text"
            placeholder="请输入用户名"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">角色</label>
          <select
            v-model="searchForm.role"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="">全部</option>
            <option value="admin">管理员</option>
            <option value="audit-admin">审计员</option>
            <option value="operator">操作员</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">状态</label>
          <select
            v-model="searchForm.status"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="">全部</option>
            <option value="active">启用</option>
            <option value="disabled">禁用</option>
          </select>
        </div>
        <div class="flex items-end gap-2">
          <button
            @click="handleSearch"
            class="flex-1 px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors"
          >
            搜索
          </button>
          <button
            @click="handleReset"
            class="px-4 py-2 bg-gray-100 text-gray-700 text-sm font-medium rounded-lg hover:bg-gray-200 transition-colors"
          >
            重置
          </button>
        </div>
      </div>
    </div>

    <!-- 用户列表 -->
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
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">用户</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">邮箱</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">手机号</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">角色</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">状态</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">最后登录</th>
            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="user in userList" :key="user.id" class="hover:bg-gray-50">
            <td class="px-6 py-4">
              <el-checkbox v-model="user.selected" @change="handleSelect" />
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="flex items-center">
                <div class="flex-shrink-0 h-10 w-10">
                  <div class="h-10 w-10 rounded-full bg-blue-100 flex items-center justify-center">
                    <span class="text-blue-600 font-medium">{{ user.username.charAt(0).toUpperCase() }}</span>
                  </div>
                </div>
                <div class="ml-4">
                  <div class="text-sm font-medium text-gray-900">{{ user.username }}</div>
                  <div class="text-sm text-gray-500">{{ user.realName }}</div>
                </div>
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ user.email }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ user.phone }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span
                v-for="role in user.roles"
                :key="role"
                class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium mr-1"
                :class="getRoleClass(role)"
              >
                {{ getRoleName(role) }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span
                class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                :class="user.status === 'active' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'"
              >
                {{ user.status === 'active' ? '启用' : '禁用' }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ user.lastLoginTime }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
              <button @click="handleEdit(user)" class="text-blue-600 hover:text-blue-900 mr-3">编辑</button>
              <button @click="handleResetPassword(user)" class="text-yellow-600 hover:text-yellow-900 mr-3">重置密码</button>
              <button @click="handleDelete(user)" class="text-red-600 hover:text-red-900">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      
      <!-- 分页 -->
      <div class="px-6 py-4 border-t border-gray-200 flex items-center justify-between">
        <div class="text-sm text-gray-500">
          共 {{ pagination.total }} 条记录，当前第 {{ pagination.page }} / {{ Math.ceil(pagination.total / pagination.size) }} 页
        </div>
        <div class="flex gap-2">
          <button 
            @click="handlePageChange(pagination.page - 1)" 
            :disabled="pagination.page === 1"
            class="px-3 py-1 bg-white text-gray-700 text-sm font-medium rounded border border-gray-300 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            上一页
          </button>
          <span class="px-3 py-1 text-sm text-gray-700">{{ pagination.page }} / {{ Math.ceil(pagination.total / pagination.size) }}</span>
          <button 
            @click="handlePageChange(pagination.page + 1)" 
            :disabled="pagination.page >= Math.ceil(pagination.total / pagination.size)"
            class="px-3 py-1 bg-white text-gray-700 text-sm font-medium rounded border border-gray-300 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            下一页
          </button>
        </div>
      </div>
    </div>

    <!-- 用户表单对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="600px">
      <el-form ref="userFormRef" :model="userForm" :rules="userRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="roles">
          <el-checkbox-group v-model="userForm.roles">
            <el-checkbox label="admin">管理员</el-checkbox>
            <el-checkbox label="audit-admin">审计员</el-checkbox>
            <el-checkbox label="operator">操作员</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="disabled">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password />
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
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Download } from '@element-plus/icons-vue'

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const userFormRef = ref<FormInstance>()
const selectAll = ref(false)

const searchForm = reactive({
  username: '',
  role: '',
  status: ''
})

const userForm = reactive({
  id: 0,
  username: '',
  realName: '',
  email: '',
  phone: '',
  roles: [] as string[],
  status: 'active',
  password: ''
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 3
})

const userList = ref([
  {
    id: 1,
    username: 'admin',
    realName: '系统管理员',
    email: 'admin@bankshield.com',
    phone: '13800138000',
    roles: ['admin'],
    status: 'active',
    lastLoginTime: '2024-12-30 10:30:00',
    selected: false
  },
  {
    id: 2,
    username: 'audit',
    realName: '审计员',
    email: 'audit@bankshield.com',
    phone: '13800138001',
    roles: ['audit-admin'],
    status: 'active',
    lastLoginTime: '2024-12-30 09:15:00',
    selected: false
  },
  {
    id: 3,
    username: 'operator',
    realName: '操作员',
    email: 'operator@bankshield.com',
    phone: '13800138002',
    roles: ['operator'],
    status: 'active',
    lastLoginTime: '2024-12-29 16:45:00',
    selected: false
  }
])

const selectedIds = computed(() => {
  return userList.value.filter(user => user.selected).map(user => user.id)
})

const userRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  roles: [{ required: true, message: '请选择角色', trigger: 'change' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(userForm, {
    id: 0,
    username: '',
    realName: '',
    email: '',
    phone: '',
    roles: [],
    status: 'active',
    password: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  Object.assign(userForm, { ...row, password: '' })
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const index = userList.value.findIndex(u => u.id === row.id)
    if (index > -1) {
      userList.value.splice(index, 1)
      ElMessage.success('删除成功')
    }
  } catch {}
}

const handleResetPassword = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要重置用户 "${row.username}" 的密码吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.success('密码重置成功，新密码为：123456')
  } catch {}
}

const handleSearch = () => {
  ElMessage.success('搜索完成')
}

const handleReset = () => {
  Object.assign(searchForm, { username: '', role: '', status: '' })
  ElMessage.success('重置成功')
}

const handleExport = () => {
  ElMessage.success('导出成功')
}

const handleSelectAll = () => {
  userList.value.forEach(user => {
    user.selected = selectAll.value
  })
}

const handleSelect = () => {
  selectAll.value = userList.value.every(user => user.selected)
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个用户吗？`, '批量删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userList.value = userList.value.filter(user => !user.selected)
    selectAll.value = false
    ElMessage.success(`成功删除 ${selectedIds.value.length} 个用户`)
  } catch {}
}

const handleBatchExport = () => {
  const selectedUsers = userList.value.filter(user => user.selected)
  ElMessage.success(`成功导出 ${selectedUsers.length} 个用户数据`)
}

const handlePageChange = (page: number) => {
  pagination.page = page
  ElMessage.info(`切换到第 ${page} 页`)
}

const handleSubmit = async () => {
  if (!userFormRef.value) return
  try {
    await userFormRef.value.validate()
    submitLoading.value = true
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    if (isEdit.value) {
      const index = userList.value.findIndex(u => u.id === userForm.id)
      if (index > -1) {
        userList.value[index] = { ...userForm, lastLoginTime: userList.value[index].lastLoginTime, selected: false }
      }
      ElMessage.success('编辑成功')
    } else {
      userList.value.push({
        id: Date.now(),
        ...userForm,
        lastLoginTime: '从未登录',
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

const getRoleName = (role: string) => {
  const map: Record<string, string> = {
    admin: '管理员',
    'audit-admin': '审计员',
    operator: '操作员'
  }
  return map[role] || role
}

const getRoleClass = (role: string) => {
  const map: Record<string, string> = {
    admin: 'bg-red-100 text-red-800',
    'audit-admin': 'bg-yellow-100 text-yellow-800',
    operator: 'bg-blue-100 text-blue-800'
  }
  return map[role] || 'bg-gray-100 text-gray-800'
}
</script>
