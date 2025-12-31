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
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="role-list-card">
          <el-table
            v-loading="loading"
            :data="roleList"
            stripe
            style="width: 100%"
            @row-click="handleRowClick"
          >
            <el-table-column prop="name" label="角色名称" min-width="120">
              <template #default="{ row }">
                <div class="role-name">
                  <el-icon class="role-icon" :style="{ color: row.color }">
                    <UserFilled />
                  </el-icon>
                  <span>{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="code" label="角色编码" min-width="120" />
            <el-table-column prop="description" label="描述" min-width="200" />
            <el-table-column prop="userCount" label="用户数量" width="100" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 'active' ? 'success' : 'danger'" size="small">
                  {{ row.status === 'active' ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  size="small"
                  :icon="Edit"
                  @click.stop="handleEdit(row)"
                >
                  编辑
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  :icon="Delete"
                  @click.stop="handleDelete(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="permission-card">
          <template #header>
            <div class="card-header">
              <span>权限配置</span>
              <el-button
                v-if="selectedRole"
                type="primary"
                size="small"
                @click="handleSavePermissions"
              >
                保存权限
              </el-button>
            </div>
          </template>
          
          <div v-if="!selectedRole" class="no-role-selected">
            <el-empty description="请选择一个角色查看权限" />
          </div>
          
          <div v-else class="permission-content">
            <div class="role-info">
              <h4>{{ selectedRole.name }}</h4>
              <p>{{ selectedRole.description }}</p>
            </div>
            
            <el-divider />
            
            <div class="permission-tree">
              <el-tree
                ref="permissionTreeRef"
                :data="permissionTree"
                show-checkbox
                node-key="id"
                :default-checked-keys="selectedPermissions"
                :props="{ label: 'name', children: 'children' }"
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 角色表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form
        ref="roleFormRef"
        :model="roleForm"
        :rules="roleRules"
        label-width="80px"
      >
        <el-form-item label="角色名称" prop="name">
          <el-input
            v-model="roleForm.name"
            placeholder="请输入角色名称"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input
            v-model="roleForm.code"
            placeholder="请输入角色编码"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="roleForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入角色描述"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="roleForm.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="disabled">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Plus,
  Edit,
  Delete,
  UserFilled
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const roleFormRef = ref<FormInstance>()
const permissionTreeRef = ref()

// 角色表单
const roleForm = reactive({
  name: '',
  code: '',
  description: '',
  status: 'active'
})

// 角色列表
const roleList = ref([
  {
    id: 1,
    name: '系统管理员',
    code: 'admin',
    description: '拥有系统所有权限',
    userCount: 1,
    status: 'active',
    color: '#f56c6c',
    permissions: ['*']
  },
  {
    id: 2,
    name: '审计员',
    code: 'audit-admin',
    description: '负责系统审计和日志查看',
    userCount: 2,
    status: 'active',
    color: '#e6a23c',
    permissions: ['audit:view', 'log:view', 'report:view']
  },
  {
    id: 3,
    name: '操作员',
    code: 'operator',
    description: '负责日常数据操作',
    userCount: 5,
    status: 'active',
    color: '#409eff',
    permissions: ['data:view', 'data:edit', 'data:export']
  },
  {
    id: 4,
    name: '数据管理员',
    code: 'data-manager',
    description: '负责数据分类和资产管理',
    userCount: 3,
    status: 'active',
    color: '#67c23a',
    permissions: ['asset:manage', 'classification:manage', 'template:manage']
  }
])

// 选中的角色
const selectedRole = ref(null)
const selectedPermissions = ref([])

// 权限树数据
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
      { id: 'role:add', name: '角色新增' },
      { id: 'role:edit', name: '角色编辑' },
      { id: 'role:delete', name: '角色删除' }
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
      { id: 'data:export', name: '数据导出' },
      { id: 'data:import', name: '数据导入' }
    ]
  },
  {
    id: 'asset',
    name: '资产管理',
    children: [
      { id: 'asset:view', name: '资产查看' },
      { id: 'asset:manage', name: '资产管理' },
      { id: 'classification:manage', name: '分类管理' },
      { id: 'template:manage', name: '模板管理' }
    ]
  }
])

// 表单验证规则
const roleRules: FormRules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_-]+$/, message: '角色编码只能包含字母、数字、下划线和横线', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入角色描述', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => isEdit.value ? '编辑角色' : '新增角色')

// 方法
const handleAdd = () => {
  isEdit.value = false
  resetForm()
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
    
    const index = roleList.value.findIndex(role => role.id === row.id)
    if (index > -1) {
      roleList.value.splice(index, 1)
      ElMessage.success('删除成功')
    }
  } catch {
    // 用户取消
  }
}

const handleRowClick = (row: any) => {
  selectedRole.value = row
  selectedPermissions.value = row.permissions
  nextTick(() => {
    permissionTreeRef.value?.setCheckedKeys(row.permissions)
  })
}

const handleSavePermissions = async () => {
  if (!selectedRole.value) return
  
  try {
    const checkedKeys = permissionTreeRef.value.getCheckedKeys()
    const role = roleList.value.find(r => r.id === selectedRole.value.id)
    if (role) {
      role.permissions = checkedKeys
      ElMessage.success('权限保存成功')
    }
  } catch (error) {
    ElMessage.error('权限保存失败')
  }
}

const handleSubmit = async () => {
  if (!roleFormRef.value) return
  
  try {
    const valid = await roleFormRef.value.validate()
    if (!valid) return
    
    submitLoading.value = true
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    if (isEdit.value) {
      const index = roleList.value.findIndex(role => role.id === roleForm.id)
      if (index > -1) {
        roleList.value[index] = { ...roleForm }
      }
      ElMessage.success('编辑成功')
    } else {
      const newRole = {
        id: Date.now(),
        ...roleForm,
        userCount: 0,
        color: '#909399',
        permissions: []
      }
      roleList.value.push(newRole)
      ElMessage.success('新增成功')
    }
    
    dialogVisible.value = false
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

const handleDialogClose = () => {
  resetForm()
}

const resetForm = () => {
  Object.assign(roleForm, {
    name: '',
    code: '',
    description: '',
    status: 'active'
  })
  roleFormRef.value?.resetFields()
}

// 生命周期
onMounted(() => {
  // 默认选择第一个角色
  if (roleList.value.length > 0) {
    handleRowClick(roleList.value[0])
  }
})
</script>

<style scoped lang="less">
.role-management {
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

  .role-list-card {
    .role-name {
      display: flex;
      align-items: center;
      gap: 8px;

      .role-icon {
        font-size: 16px;
      }
    }
  }

  .permission-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .no-role-selected {
      height: 400px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .permission-content {
      .role-info {
        h4 {
          margin: 0 0 8px 0;
          color: #303133;
        }

        p {
          margin: 0;
          color: #909399;
          font-size: 14px;
        }
      }

      .permission-tree {
        max-height: 300px;
        overflow-y: auto;
      }
    }
  }
}
</style>
