<template>
  <div class="access-policy-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="策略名称">
          <el-input v-model="queryForm.policyName" placeholder="请输入策略名称" clearable />
        </el-form-item>
        <el-form-item label="策略类型">
          <el-select v-model="queryForm.policyType" placeholder="请选择策略类型" clearable>
            <el-option label="RBAC" value="RBAC" />
            <el-option label="ABAC" value="ABAC" />
            <el-option label="混合" value="HYBRID" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" value="ENABLED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>访问策略列表</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增策略</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="policyList" border stripe>
        <el-table-column prop="policyCode" label="策略编码" width="180" />
        <el-table-column prop="policyName" label="策略名称" width="200" />
        <el-table-column prop="policyType" label="策略类型" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.policyType === 'RBAC'" type="success">RBAC</el-tag>
            <el-tag v-else-if="row.policyType === 'ABAC'" type="warning">ABAC</el-tag>
            <el-tag v-else type="info">混合</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="priority" label="优先级" width="100" align="center" />
        <el-table-column prop="effect" label="效果" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.effect === 'ALLOW'" type="success">允许</el-tag>
            <el-tag v-else type="danger">拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="ENABLED"
              inactive-value="DISABLED"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="handleViewRules(row)">规则</el-button>
            <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryForm.current"
        v-model:page-size="queryForm.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="策略编码" prop="policyCode">
          <el-input v-model="form.policyCode" placeholder="请输入策略编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="策略名称" prop="policyName">
          <el-input v-model="form.policyName" placeholder="请输入策略名称" />
        </el-form-item>
        <el-form-item label="策略类型" prop="policyType">
          <el-select v-model="form.policyType" placeholder="请选择策略类型">
            <el-option label="RBAC" value="RBAC" />
            <el-option label="ABAC" value="ABAC" />
            <el-option label="混合" value="HYBRID" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number v-model="form.priority" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="效果" prop="effect">
          <el-radio-group v-model="form.effect">
            <el-radio label="ALLOW">允许</el-radio>
            <el-radio label="DENY">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="ENABLED">启用</el-radio>
            <el-radio label="DISABLED">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 规则列表对话框 -->
    <el-dialog v-model="rulesDialogVisible" title="访问规则" width="80%">
      <el-button type="primary" :icon="Plus" @click="handleAddRule" style="margin-bottom: 10px">
        新增规则
      </el-button>
      <el-table :data="ruleList" border stripe>
        <el-table-column prop="ruleCode" label="规则编码" width="150" />
        <el-table-column prop="ruleName" label="规则名称" width="180" />
        <el-table-column prop="ruleType" label="规则类型" width="120" />
        <el-table-column prop="priority" label="优先级" width="100" align="center" />
        <el-table-column prop="mfaRequired" label="需要MFA" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.mfaRequired" type="warning">是</el-tag>
            <el-tag v-else type="info">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'ENABLED'" type="success">启用</el-tag>
            <el-tag v-else type="info">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="handleEditRule(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDeleteRule(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, View } from '@element-plus/icons-vue'
import {
  getPolicies,
  createPolicy,
  updatePolicy,
  deletePolicy,
  updatePolicyStatus,
  getPolicyRules,
  type AccessPolicy
} from '@/api/access-control'

const loading = ref(false)
const policyList = ref<AccessPolicy[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref()
const rulesDialogVisible = ref(false)
const ruleList = ref([])
const currentPolicyId = ref<number>()

const queryForm = reactive({
  current: 1,
  size: 10,
  policyName: '',
  policyType: '',
  status: ''
})

const form = reactive<AccessPolicy>({
  policyCode: '',
  policyName: '',
  policyType: 'RBAC',
  description: '',
  priority: 50,
  effect: 'ALLOW',
  status: 'ENABLED'
})

const rules = {
  policyCode: [{ required: true, message: '请输入策略编码', trigger: 'blur' }],
  policyName: [{ required: true, message: '请输入策略名称', trigger: 'blur' }],
  policyType: [{ required: true, message: '请选择策略类型', trigger: 'change' }],
  priority: [{ required: true, message: '请输入优先级', trigger: 'blur' }],
  effect: [{ required: true, message: '请选择效果', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

onMounted(() => {
  handleQuery()
})

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await getPolicies(queryForm)
    if (res.code === 200) {
      policyList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryForm.policyName = ''
  queryForm.policyType = ''
  queryForm.status = ''
  queryForm.current = 1
  handleQuery()
}

const handleAdd = () => {
  dialogTitle.value = '新增访问策略'
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = (row: AccessPolicy) => {
  dialogTitle.value = '编辑访问策略'
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = async (row: AccessPolicy) => {
  try {
    await ElMessageBox.confirm('确定要删除该策略吗？', '提示', {
      type: 'warning'
    })
    await deletePolicy(row.id!)
    ElMessage.success('删除成功')
    handleQuery()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleStatusChange = async (row: AccessPolicy) => {
  try {
    await updatePolicyStatus(row.id!, row.status)
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
    row.status = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  }
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    if (isEdit.value) {
      await updatePolicy(form.id!, form)
      ElMessage.success('更新成功')
    } else {
      await createPolicy(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    handleQuery()
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '新增失败')
  }
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    policyCode: '',
    policyName: '',
    policyType: 'RBAC',
    description: '',
    priority: 50,
    effect: 'ALLOW',
    status: 'ENABLED'
  })
}

const handleViewRules = async (row: AccessPolicy) => {
  currentPolicyId.value = row.id
  try {
    const res = await getPolicyRules(row.id!)
    if (res.code === 200) {
      ruleList.value = res.data
      rulesDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('查询规则失败')
  }
}

const handleAddRule = () => {
  ElMessage.info('规则管理功能开发中...')
}

const handleEditRule = (row: any) => {
  ElMessage.info('规则编辑功能开发中...')
}

const handleDeleteRule = (row: any) => {
  ElMessage.info('规则删除功能开发中...')
}
</script>

<style scoped lang="less">
.access-policy-container {
  padding: 20px;

  .search-card {
    margin-bottom: 20px;
  }

  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  .el-pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }
}
</style>
