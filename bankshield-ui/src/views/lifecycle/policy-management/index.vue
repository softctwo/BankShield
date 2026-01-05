<template>
  <div class="lifecycle-policy-management">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="策略名称">
          <el-input v-model="searchForm.policyName" placeholder="请输入策略名称" clearable />
        </el-form-item>
        <el-form-item label="策略状态">
          <el-select v-model="searchForm.policyStatus" placeholder="请选择状态" clearable>
            <el-option label="活跃" value="ACTIVE" />
            <el-option label="禁用" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">新增策略</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="策略ID" width="80" />
        <el-table-column prop="policyName" label="策略名称" width="150" />
        <el-table-column prop="policyCode" label="策略编码" width="150" />
        <el-table-column prop="dataType" label="数据类型" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.dataType" type="info">{{ row.dataType }}</el-tag>
            <span v-else>全部</span>
          </template>
        </el-table-column>
        <el-table-column prop="sensitivityLevel" label="敏感级别" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.sensitivityLevel" :type="getLevelType(row.sensitivityLevel)">
              {{ row.sensitivityLevel }}
            </el-tag>
            <span v-else>全部</span>
          </template>
        </el-table-column>
        <el-table-column prop="retentionDays" label="保留天数" width="100" />
        <el-table-column prop="archiveDays" label="归档天数" width="100" />
        <el-table-column prop="destroyDays" label="销毁天数" width="100" />
        <el-table-column prop="priority" label="优先级" width="80" />
        <el-table-column prop="policyStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.policyStatus === 'ACTIVE' ? 'success' : 'danger'">
              {{ row.policyStatus === 'ACTIVE' ? '活跃' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button 
              :type="row.policyStatus === 'ACTIVE' ? 'warning' : 'success'" 
              size="small" 
              @click="handleToggleStatus(row)">
              {{ row.policyStatus === 'ACTIVE' ? '禁用' : '启用' }}
            </el-button>
            <el-button type="info" size="small" @click="handleExecute(row)">执行</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSearch"
        @current-change="handleSearch"
      />
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="handleDialogClose">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="140px">
        <el-form-item label="策略名称" prop="policyName">
          <el-input v-model="formData.policyName" placeholder="请输入策略名称" />
        </el-form-item>
        <el-form-item label="策略编码" prop="policyCode">
          <el-input v-model="formData.policyCode" placeholder="请输入策略编码" />
        </el-form-item>
        <el-form-item label="数据类型">
          <el-select v-model="formData.dataType" placeholder="请选择数据类型" clearable>
            <el-option label="数据库" value="DATABASE" />
            <el-option label="文件" value="FILE" />
            <el-option label="大数据" value="BIGDATA" />
          </el-select>
        </el-form-item>
        <el-form-item label="敏感级别">
          <el-select v-model="formData.sensitivityLevel" placeholder="请选择敏感级别" clearable>
            <el-option label="C1-公开" value="C1" />
            <el-option label="C2-内部" value="C2" />
            <el-option label="C3-敏感" value="C3" />
            <el-option label="C4-高敏感" value="C4" />
            <el-option label="C5-极敏感" value="C5" />
          </el-select>
        </el-form-item>
        <el-form-item label="保留天数" prop="retentionDays">
          <el-input-number v-model="formData.retentionDays" :min="1" :max="10000" />
        </el-form-item>
        <el-form-item label="启用归档">
          <el-switch v-model="formData.archiveEnabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="归档天数" v-if="formData.archiveEnabled === 1">
          <el-input-number v-model="formData.archiveDays" :min="1" :max="10000" />
        </el-form-item>
        <el-form-item label="归档存储位置" v-if="formData.archiveEnabled === 1">
          <el-input v-model="formData.archiveStorage" placeholder="请输入归档存储位置" />
        </el-form-item>
        <el-form-item label="启用销毁">
          <el-switch v-model="formData.destroyEnabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="销毁天数" v-if="formData.destroyEnabled === 1">
          <el-input-number v-model="formData.destroyDays" :min="1" :max="10000" />
        </el-form-item>
        <el-form-item label="销毁方法" v-if="formData.destroyEnabled === 1">
          <el-select v-model="formData.destroyMethod" placeholder="请选择销毁方法">
            <el-option label="逻辑删除" value="LOGICAL" />
            <el-option label="物理删除" value="PHYSICAL" />
            <el-option label="覆盖删除" value="OVERWRITE" />
          </el-select>
        </el-form-item>
        <el-form-item label="需要审批">
          <el-switch v-model="formData.approvalRequired" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="启用通知">
          <el-switch v-model="formData.notificationEnabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="提前通知天数" v-if="formData.notificationEnabled === 1">
          <el-input-number v-model="formData.notificationDays" :min="1" :max="30" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="formData.priority" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="策略描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入策略描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="executeDialogVisible" title="执行策略" width="600px">
      <el-form :model="executeForm" label-width="120px">
        <el-form-item label="执行类型">
          <el-radio-group v-model="executeForm.type">
            <el-radio label="archive">归档</el-radio>
            <el-radio label="destroy">销毁</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="执行模式">
          <el-radio-group v-model="executeForm.mode">
            <el-radio label="auto">自动执行</el-radio>
            <el-radio label="preview">预览</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="executeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleExecuteConfirm">执行</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const executeDialogVisible = ref(false)
const formRef = ref<FormInstance>()

const searchForm = reactive({
  policyName: '',
  policyStatus: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const formData = reactive({
  id: null,
  policyName: '',
  policyCode: '',
  dataType: '',
  sensitivityLevel: '',
  retentionDays: 365,
  archiveEnabled: 1,
  archiveDays: 180,
  archiveStorage: '',
  destroyEnabled: 1,
  destroyDays: 365,
  destroyMethod: 'LOGICAL',
  approvalRequired: 0,
  notificationEnabled: 1,
  notificationDays: 7,
  priority: 0,
  description: ''
})

const executeForm = reactive({
  policyId: null,
  type: 'archive',
  mode: 'auto'
})

const formRules: FormRules = {
  policyName: [{ required: true, message: '请输入策略名称', trigger: 'blur' }],
  policyCode: [{ required: true, message: '请输入策略编码', trigger: 'blur' }],
  retentionDays: [{ required: true, message: '请输入保留天数', trigger: 'blur' }]
}

const getLevelType = (level: string) => {
  const types: Record<string, string> = {
    C1: 'info',
    C2: 'success',
    C3: 'warning',
    C4: 'danger',
    C5: 'danger'
  }
  return types[level] || 'info'
}

const fetchPolicies = async () => {
  loading.value = true
  try {
    const response = await fetch(
      `/api/lifecycle/policies?pageNum=${pagination.pageNum}&pageSize=${pagination.pageSize}&policyName=${searchForm.policyName}&policyStatus=${searchForm.policyStatus}`
    )
    const result = await response.json()
    if (result.code === 200) {
      tableData.value = result.data
      pagination.total = result.total
    } else {
      ElMessage.error(result.message || '查询失败')
    }
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchPolicies()
}

const handleReset = () => {
  searchForm.policyName = ''
  searchForm.policyStatus = ''
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增策略'
  Object.assign(formData, {
    id: null,
    policyName: '',
    policyCode: '',
    dataType: '',
    sensitivityLevel: '',
    retentionDays: 365,
    archiveEnabled: 1,
    archiveDays: 180,
    archiveStorage: '',
    destroyEnabled: 1,
    destroyDays: 365,
    destroyMethod: 'LOGICAL',
    approvalRequired: 0,
    notificationEnabled: 1,
    notificationDays: 7,
    priority: 0,
    description: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑策略'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleToggleStatus = async (row: any) => {
  const newStatus = row.policyStatus === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  try {
    const response = await fetch(`/api/lifecycle/policies/${row.id}/status?status=${newStatus}`, {
      method: 'PUT'
    })
    const result = await response.json()
    if (result.code === 200) {
      ElMessage.success('状态切换成功')
      fetchPolicies()
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该策略吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await fetch(`/api/lifecycle/policies/${row.id}`, {
        method: 'DELETE'
      })
      const result = await response.json()
      if (result.code === 200) {
        ElMessage.success('删除成功')
        fetchPolicies()
      } else {
        ElMessage.error(result.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const handleExecute = (row: any) => {
  executeForm.policyId = row.id
  executeForm.type = 'archive'
  executeForm.mode = 'auto'
  executeDialogVisible.value = true
}

const handleExecuteConfirm = async () => {
  try {
    const url = executeForm.type === 'archive' 
      ? `/api/lifecycle/archive/auto/${executeForm.policyId}`
      : `/api/lifecycle/destroy/auto/${executeForm.policyId}`
    
    const response = await fetch(url, { method: 'POST' })
    const result = await response.json()
    
    if (result.code === 200) {
      ElMessage.success(`执行成功: 成功${result.success}条，失败${result.failed}条`)
      executeDialogVisible.value = false
    } else {
      ElMessage.error(result.message || '执行失败')
    }
  } catch (error) {
    ElMessage.error('执行失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const url = formData.id 
          ? `/api/lifecycle/policies/${formData.id}`
          : '/api/lifecycle/policies'
        const method = formData.id ? 'PUT' : 'POST'
        
        const response = await fetch(url, {
          method,
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(formData)
        })
        const result = await response.json()
        
        if (result.code === 200) {
          ElMessage.success(formData.id ? '更新成功' : '创建成功')
          dialogVisible.value = false
          fetchPolicies()
        } else {
          ElMessage.error(result.message || '操作失败')
        }
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
}

onMounted(() => {
  fetchPolicies()
})
</script>

<style scoped lang="less">
.lifecycle-policy-management {
  padding: 20px;

  .search-card {
    margin-bottom: 20px;
  }

  .table-card {
    .el-pagination {
      margin-top: 20px;
      justify-content: flex-end;
    }
  }
}
</style>
