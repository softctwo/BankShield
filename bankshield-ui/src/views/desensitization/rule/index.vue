<template>
  <div class="desensitization-rule-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="规则名称">
          <el-input v-model="searchForm.ruleName" placeholder="请输入规则名称" clearable />
        </el-form-item>
        <el-form-item label="数据类型">
          <el-select v-model="searchForm.dataType" placeholder="请选择数据类型" clearable>
            <el-option label="手机号" value="PHONE" />
            <el-option label="身份证" value="ID_CARD" />
            <el-option label="银行卡" value="BANK_CARD" />
            <el-option label="邮箱" value="EMAIL" />
            <el-option label="姓名" value="NAME" />
            <el-option label="地址" value="ADDRESS" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" value="ENABLED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button type="success" :icon="Plus" @click="handleAdd">新增规则</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="ruleName" label="规则名称" min-width="150" />
        <el-table-column prop="ruleCode" label="规则编码" min-width="150" />
        <el-table-column prop="dataType" label="数据类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ getDataTypeLabel(row.dataType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="algorithmType" label="算法类型" width="120">
          <template #default="{ row }">
            <el-tag type="success">{{ row.algorithmType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sensitivityLevel" label="敏感级别" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getSensitivityType(row.sensitivityLevel)">
              {{ row.sensitivityLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="ENABLED"
              inactive-value="DISABLED"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" :icon="View" @click="handleView(row)">查看</el-button>
            <el-button type="warning" size="small" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" size="small" :icon="Operation" @click="handleTest(row)">测试</el-button>
            <el-button type="danger" size="small" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
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
      :close-on-click-modal="false"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="120px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="formData.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="规则编码" prop="ruleCode">
          <el-input v-model="formData.ruleCode" placeholder="请输入规则编码" />
        </el-form-item>
        <el-form-item label="数据类型" prop="dataType">
          <el-select v-model="formData.dataType" placeholder="请选择数据类型">
            <el-option label="手机号" value="PHONE" />
            <el-option label="身份证" value="ID_CARD" />
            <el-option label="银行卡" value="BANK_CARD" />
            <el-option label="邮箱" value="EMAIL" />
            <el-option label="姓名" value="NAME" />
            <el-option label="地址" value="ADDRESS" />
          </el-select>
        </el-form-item>
        <el-form-item label="算法类型" prop="algorithmType">
          <el-select v-model="formData.algorithmType" placeholder="请选择算法类型">
            <el-option label="MASK（遮盖）" value="MASK" />
            <el-option label="REPLACE（替换）" value="REPLACE" />
            <el-option label="ENCRYPT（加密）" value="ENCRYPT" />
            <el-option label="HASH（哈希）" value="HASH" />
            <el-option label="GENERALIZE（泛化）" value="GENERALIZE" />
            <el-option label="SHUFFLE（扰乱）" value="SHUFFLE" />
            <el-option label="TRUNCATE（截断）" value="TRUNCATE" />
          </el-select>
        </el-form-item>
        <el-form-item label="算法配置" prop="algorithmConfig">
          <el-input
            v-model="formData.algorithmConfig"
            type="textarea"
            :rows="4"
            placeholder='请输入JSON格式配置，如：{"pattern":"(\\d{3})\\d{4}(\\d{4})","replacement":"$1****$2"}'
          />
        </el-form-item>
        <el-form-item label="敏感级别" prop="sensitivityLevel">
          <el-select v-model="formData.sensitivityLevel" placeholder="请选择敏感级别">
            <el-option label="C1" value="C1" />
            <el-option label="C2" value="C2" />
            <el-option label="C3" value="C3" />
            <el-option label="C4" value="C4" />
            <el-option label="C5" value="C5" />
          </el-select>
        </el-form-item>
        <el-form-item label="应用范围" prop="applyScope">
          <el-input v-model="formData.applyScope" placeholder="请输入应用范围，如：ALL、QUERY、EXPORT" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number v-model="formData.priority" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="规则描述" prop="ruleDescription">
          <el-input v-model="formData.ruleDescription" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="testDialogVisible" title="测试脱敏规则" width="600px">
      <el-form label-width="100px">
        <el-form-item label="测试数据">
          <el-input v-model="testData" placeholder="请输入测试数据" />
        </el-form-item>
        <el-form-item label="脱敏结果">
          <el-input v-model="testResult" readonly />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="testDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleTestSubmit">执行测试</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, Refresh, Plus, View, Edit, Delete, Operation } from '@element-plus/icons-vue'
import * as desensitizationApi from '@/api/desensitization'

interface SearchForm {
  ruleName: string
  dataType: string
  status: string
}

interface RuleData {
  id?: number
  ruleName: string
  ruleCode: string
  dataType: string
  algorithmType: string
  algorithmConfig: string
  sensitivityLevel: string
  applyScope: string
  priority: number
  status: string
  ruleDescription: string
}

const loading = ref(false)
const dialogVisible = ref(false)
const testDialogVisible = ref(false)
const dialogTitle = ref('新增规则')
const formRef = ref<FormInstance>()
const currentRow = ref<RuleData | null>(null)
const testData = ref('')
const testResult = ref('')

const searchForm = reactive<SearchForm>({
  ruleName: '',
  dataType: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref<RuleData[]>([])

const formData = reactive<RuleData>({
  ruleName: '',
  ruleCode: '',
  dataType: '',
  algorithmType: '',
  algorithmConfig: '',
  sensitivityLevel: '',
  applyScope: 'ALL',
  priority: 10,
  status: 'ENABLED',
  ruleDescription: ''
})

const formRules: FormRules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleCode: [{ required: true, message: '请输入规则编码', trigger: 'blur' }],
  dataType: [{ required: true, message: '请选择数据类型', trigger: 'change' }],
  algorithmType: [{ required: true, message: '请选择算法类型', trigger: 'change' }],
  sensitivityLevel: [{ required: true, message: '请选择敏感级别', trigger: 'change' }]
}

const getDataTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    PHONE: '手机号',
    ID_CARD: '身份证',
    BANK_CARD: '银行卡',
    EMAIL: '邮箱',
    NAME: '姓名',
    ADDRESS: '地址'
  }
  return map[type] || type
}

const getSensitivityType = (level: string) => {
  const map: Record<string, string> = {
    C1: 'info',
    C2: 'success',
    C3: 'warning',
    C4: 'danger',
    C5: 'danger'
  }
  return map[level] || 'info'
}

const handleSearch = async () => {
  loading.value = true
  try {
    const res = await desensitizationApi.getRules({
      current: pagination.current,
      size: pagination.size,
      ruleName: searchForm.ruleName,
      dataType: searchForm.dataType,
      status: searchForm.status
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchForm.ruleName = ''
  searchForm.dataType = ''
  searchForm.status = ''
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增规则'
  Object.assign(formData, {
    ruleName: '',
    ruleCode: '',
    dataType: '',
    algorithmType: '',
    algorithmConfig: '',
    sensitivityLevel: '',
    applyScope: 'ALL',
    priority: 10,
    status: 'ENABLED',
    ruleDescription: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row: RuleData) => {
  dialogTitle.value = '编辑规则'
  Object.assign(formData, row)
  currentRow.value = row
  dialogVisible.value = true
}

const handleView = (row: RuleData) => {
  dialogTitle.value = '查看规则'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = (row: RuleData) => {
  ElMessageBox.confirm('确定要删除该规则吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await desensitizationApi.deleteRule(row.id!)
      ElMessage.success('删除成功')
      handleSearch()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const handleStatusChange = async (row: RuleData) => {
  try {
    await desensitizationApi.updateRuleStatus(row.id!, row.status)
    ElMessage.success(`规则已${row.status === 'ENABLED' ? '启用' : '禁用'}`)
  } catch (error) {
    ElMessage.error('状态更新失败')
    row.status = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  }
}

const handleTest = (row: RuleData) => {
  currentRow.value = row
  testData.value = ''
  testResult.value = ''
  testDialogVisible.value = true
}

const handleTestSubmit = async () => {
  if (!testData.value) {
    ElMessage.warning('请输入测试数据')
    return
  }
  if (!currentRow.value) return
  try {
    const res = await desensitizationApi.testRule(currentRow.value.id!, testData.value)
    if (res.code === 200) {
      testResult.value = res.data
      ElMessage.success('测试成功')
    }
  } catch (error) {
    ElMessage.error('测试失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (dialogTitle.value === '新增规则') {
          await desensitizationApi.createRule(formData)
          ElMessage.success('新增成功')
        } else {
          await desensitizationApi.updateRule(formData.id!, formData)
          ElMessage.success('编辑成功')
        }
        dialogVisible.value = false
        handleSearch()
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}

onMounted(() => {
  handleSearch()
})
</script>

<style scoped lang="less">
.desensitization-rule-container {
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
