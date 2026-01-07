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
    id: 'dashboard',
    name: '数据大屏',
    children: [
      { id: 'dashboard:view', name: '查看大屏' }
    ]
  },
  {
    id: 'system',
    name: '系统管理',
    children: [
      { id: 'system:user:view', name: '用户查看' },
      { id: 'system:user:add', name: '用户新增' },
      { id: 'system:user:edit', name: '用户编辑' },
      { id: 'system:user:delete', name: '用户删除' },
      { id: 'system:role:view', name: '角色查看' },
      { id: 'system:role:add', name: '角色新增' },
      { id: 'system:role:edit', name: '角色编辑' },
      { id: 'system:role:delete', name: '角色删除' },
      { id: 'system:dept:view', name: '部门查看' },
      { id: 'system:dept:add', name: '部门新增' },
      { id: 'system:menu:view', name: '菜单查看' },
      { id: 'system:menu:add', name: '菜单新增' }
    ]
  },
  {
    id: 'classification',
    name: '数据分类',
    children: [
      { id: 'classification:sensitive:view', name: '敏感数据查看' },
      { id: 'classification:rule:view', name: '分类规则查看' },
      { id: 'classification:rule:add', name: '分类规则新增' },
      { id: 'classification:datasource:view', name: '数据源查看' },
      { id: 'classification:datasource:add', name: '数据源新增' },
      { id: 'classification:asset:view', name: '资产地图查看' }
    ]
  },
  {
    id: 'encrypt',
    name: '数据加密',
    children: [
      { id: 'encrypt:key:view', name: '密钥查看' },
      { id: 'encrypt:key:generate', name: '密钥生成' },
      { id: 'encrypt:key:rotate', name: '密钥轮换' },
      { id: 'encrypt:key:destroy', name: '密钥销毁' },
      { id: 'encrypt:strategy:view', name: '加密策略查看' },
      { id: 'encrypt:strategy:add', name: '加密策略新增' },
      { id: 'encrypt:task:view', name: '加密任务查看' },
      { id: 'encrypt:task:execute', name: '加密任务执行' }
    ]
  },
  {
    id: 'desensitization',
    name: '数据脱敏',
    children: [
      { id: 'desensitization:rule:view', name: '脱敏规则查看' },
      { id: 'desensitization:rule:add', name: '脱敏规则新增' },
      { id: 'desensitization:template:view', name: '脱敏模板查看' },
      { id: 'desensitization:template:add', name: '脱敏模板新增' },
      { id: 'desensitization:log:view', name: '脱敏日志查看' }
    ]
  },
  {
    id: 'access-control',
    name: '访问控制',
    children: [
      { id: 'access:policy:view', name: '策略查看' },
      { id: 'access:policy:add', name: '策略新增' },
      { id: 'access:ip:view', name: 'IP白名单查看' },
      { id: 'access:ip:add', name: 'IP白名单新增' },
      { id: 'access:mfa:view', name: 'MFA配置查看' },
      { id: 'access:mfa:config', name: 'MFA配置管理' }
    ]
  },
  {
    id: 'audit',
    name: '审计追踪',
    children: [
      { id: 'audit:dashboard:view', name: '审计概览查看' },
      { id: 'audit:operation:view', name: '操作审计查看' },
      { id: 'audit:operation:export', name: '操作审计导出' },
      { id: 'audit:login:view', name: '登录审计查看' },
      { id: 'audit:security:view', name: '安全审计查看' }
    ]
  },
  {
    id: 'monitor',
    name: '监控告警',
    children: [
      { id: 'monitor:dashboard:view', name: '监控大屏查看' },
      { id: 'monitor:alert:view', name: '告警规则查看' },
      { id: 'monitor:alert:add', name: '告警规则新增' },
      { id: 'monitor:alert:edit', name: '告警规则编辑' },
      { id: 'monitor:record:view', name: '告警记录查看' }
    ]
  },
  {
    id: 'compliance',
    name: '合规管理',
    children: [
      { id: 'compliance:dashboard:view', name: '合规概览查看' },
      { id: 'compliance:rule:view', name: '合规规则查看' },
      { id: 'compliance:rule:add', name: '合规规则新增' },
      { id: 'compliance:check:view', name: '合规检查查看' },
      { id: 'compliance:check:execute', name: '合规检查执行' },
      { id: 'compliance:report:view', name: '合规报告查看' },
      { id: 'compliance:report:export', name: '合规报告导出' }
    ]
  },
  {
    id: 'lineage',
    name: '数据血缘',
    children: [
      { id: 'lineage:graph:view', name: '血缘图谱查看' },
      { id: 'lineage:discovery:execute', name: '血缘发现执行' },
      { id: 'lineage:map:view', name: '数据地图查看' },
      { id: 'lineage:map:generate', name: '数据地图生成' }
    ]
  },
  {
    id: 'blockchain',
    name: '区块链存证',
    children: [
      { id: 'blockchain:dashboard:view', name: '存证概览查看' },
      { id: 'blockchain:record:view', name: '存证记录查看' },
      { id: 'blockchain:record:add', name: '存证记录新增' },
      { id: 'blockchain:verify:execute', name: '存证验证执行' }
    ]
  },
  {
    id: 'ai',
    name: 'AI智能',
    children: [
      { id: 'ai:dashboard:view', name: 'AI概览查看' },
      { id: 'ai:model:view', name: '模型查看' },
      { id: 'ai:model:add', name: '模型新增' },
      { id: 'ai:model:train', name: '模型训练' },
      { id: 'ai:model:deploy', name: '模型部署' },
      { id: 'ai:behavior:view', name: '行为分析查看' },
      { id: 'ai:anomaly:view', name: '异常检测查看' }
    ]
  },
  {
    id: 'mpc',
    name: 'MPC计算',
    children: [
      { id: 'mpc:dashboard:view', name: 'MPC概览查看' },
      { id: 'mpc:job:view', name: '任务查看' },
      { id: 'mpc:job:create', name: '任务创建' },
      { id: 'mpc:job:execute', name: '任务执行' },
      { id: 'mpc:party:view', name: '参与方查看' },
      { id: 'mpc:party:register', name: '参与方注册' }
    ]
  },
  {
    id: 'federated',
    name: '联邦学习',
    children: [
      { id: 'federated:dashboard:view', name: '联邦概览查看' },
      { id: 'federated:job:view', name: '任务查看' },
      { id: 'federated:job:create', name: '任务创建' },
      { id: 'federated:job:start', name: '任务启动' },
      { id: 'federated:party:view', name: '参与方查看' },
      { id: 'federated:party:register', name: '参与方注册' }
    ]
  },
  {
    id: 'security',
    name: '安全防护',
    children: [
      { id: 'security:scan:view', name: '安全扫描查看' },
      { id: 'security:scan:create', name: '安全扫描创建' },
      { id: 'security:scan:execute', name: '安全扫描执行' },
      { id: 'security:result:view', name: '扫描结果查看' },
      { id: 'security:watermark:view', name: '水印模板查看' },
      { id: 'security:watermark:add', name: '水印模板新增' },
      { id: 'security:watermark:extract', name: '水印提取' }
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
