<template>
  <div class="desensitization-template-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="模板名称">
          <el-input v-model="searchForm.templateName" placeholder="请输入模板名称" clearable />
        </el-form-item>
        <el-form-item label="模板类型">
          <el-select v-model="searchForm.templateType" placeholder="请选择模板类型" clearable>
            <el-option label="表级模板" value="TABLE" />
            <el-option label="字段级模板" value="FIELD" />
            <el-option label="业务模板" value="BUSINESS" />
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
          <el-button type="success" :icon="Plus" @click="handleAdd">新增模板</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="templateName" label="模板名称" min-width="150" />
        <el-table-column prop="templateCode" label="模板编码" min-width="150" />
        <el-table-column prop="templateType" label="模板类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ getTemplateTypeLabel(row.templateType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetTable" label="目标表" min-width="150" />
        <el-table-column prop="ruleCount" label="规则数量" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="success">{{ row.ruleCount || 0 }}</el-tag>
          </template>
        </el-table-column>
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
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="320" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" :icon="View" @click="handleView(row)">查看</el-button>
            <el-button type="warning" size="small" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" size="small" :icon="Operation" @click="handleApply(row)">应用</el-button>
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
      width="900px"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="模板名称" prop="templateName">
              <el-input v-model="formData.templateName" placeholder="请输入模板名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模板编码" prop="templateCode">
              <el-input v-model="formData.templateCode" placeholder="请输入模板编码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="模板类型" prop="templateType">
              <el-select v-model="formData.templateType" placeholder="请选择模板类型">
                <el-option label="表级模板" value="TABLE" />
                <el-option label="字段级模板" value="FIELD" />
                <el-option label="业务模板" value="BUSINESS" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标表" prop="targetTable">
              <el-input v-model="formData.targetTable" placeholder="请输入目标表名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="字段映射" prop="fieldMapping">
          <el-input
            v-model="formData.fieldMapping"
            type="textarea"
            :rows="6"
            placeholder='请输入JSON格式字段映射，如：{"phone":"PHONE_RULE","idCard":"ID_CARD_RULE"}'
          />
        </el-form-item>
        <el-form-item label="模板描述" prop="templateDescription">
          <el-input v-model="formData.templateDescription" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="applyDialogVisible" title="应用脱敏模板" width="600px">
      <el-form label-width="100px">
        <el-form-item label="模板名称">
          <el-input :value="currentTemplate?.templateName" readonly />
        </el-form-item>
        <el-form-item label="目标表">
          <el-input :value="currentTemplate?.targetTable" readonly />
        </el-form-item>
        <el-form-item label="执行方式">
          <el-radio-group v-model="applyMode">
            <el-radio value="IMMEDIATE">立即执行</el-radio>
            <el-radio value="SCHEDULED">定时执行</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="applyMode === 'SCHEDULED'" label="执行时间">
          <el-date-picker
            v-model="scheduleTime"
            type="datetime"
            placeholder="选择执行时间"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleApplySubmit">确定应用</el-button>
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
  templateName: string
  templateType: string
  status: string
}

interface TemplateData {
  id?: number
  templateName: string
  templateCode: string
  templateType: string
  targetTable: string
  fieldMapping: string
  status: string
  templateDescription: string
  ruleCount?: number
  createTime?: string
}

const loading = ref(false)
const dialogVisible = ref(false)
const applyDialogVisible = ref(false)
const dialogTitle = ref('新增模板')
const formRef = ref<FormInstance>()
const currentTemplate = ref<TemplateData | null>(null)
const applyMode = ref('IMMEDIATE')
const scheduleTime = ref('')

const searchForm = reactive<SearchForm>({
  templateName: '',
  templateType: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref<TemplateData[]>([])

const formData = reactive<TemplateData>({
  templateName: '',
  templateCode: '',
  templateType: '',
  targetTable: '',
  fieldMapping: '',
  status: 'ENABLED',
  templateDescription: ''
})

const formRules: FormRules = {
  templateName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  templateCode: [{ required: true, message: '请输入模板编码', trigger: 'blur' }],
  templateType: [{ required: true, message: '请选择模板类型', trigger: 'change' }],
  targetTable: [{ required: true, message: '请输入目标表', trigger: 'blur' }],
  fieldMapping: [{ required: true, message: '请输入字段映射', trigger: 'blur' }]
}

const getTemplateTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    TABLE: '表级模板',
    FIELD: '字段级模板',
    BUSINESS: '业务模板'
  }
  return map[type] || type
}

const handleSearch = async () => {
  loading.value = true
  try {
    const res = await desensitizationApi.getTemplates({
      current: pagination.current,
      size: pagination.size,
      templateName: searchForm.templateName,
      templateType: searchForm.templateType,
      status: searchForm.status
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error('查询失败:', error)
    // 使用模拟数据
    tableData.value = [
      { id: 1, templateName: '用户信息脱敏模板', templateCode: 'USER_INFO_TPL', templateType: 'TABLE', targetTable: 'user_info', fieldMapping: '{"phone":"PHONE_MASK","id_card":"ID_CARD_MASK","name":"NAME_MASK"}', status: 'ENABLED', templateDescription: '用户信息表脱敏模板', ruleCount: 3, createTime: '2025-01-05 10:00:00' },
      { id: 2, templateName: '订单信息脱敏模板', templateCode: 'ORDER_INFO_TPL', templateType: 'TABLE', targetTable: 'order_info', fieldMapping: '{"customer_phone":"PHONE_MASK","delivery_address":"ADDRESS_MASK"}', status: 'ENABLED', templateDescription: '订单信息表脱敏模板', ruleCount: 2, createTime: '2025-01-05 11:00:00' },
      { id: 3, templateName: '支付信息脱敏模板', templateCode: 'PAYMENT_INFO_TPL', templateType: 'BUSINESS', targetTable: 'payment_record', fieldMapping: '{"bank_card":"BANK_CARD_MASK","payer_name":"NAME_MASK"}', status: 'ENABLED', templateDescription: '支付记录脱敏模板', ruleCount: 2, createTime: '2025-01-05 14:00:00' },
      { id: 4, templateName: '员工信息脱敏模板', templateCode: 'EMPLOYEE_TPL', templateType: 'TABLE', targetTable: 'employee', fieldMapping: '{"phone":"PHONE_MASK","email":"EMAIL_MASK","id_card":"ID_CARD_MASK"}', status: 'DISABLED', templateDescription: '员工信息表脱敏模板', ruleCount: 3, createTime: '2025-01-04 09:00:00' }
    ]
    pagination.total = 4
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchForm.templateName = ''
  searchForm.templateType = ''
  searchForm.status = ''
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增模板'
  Object.assign(formData, {
    templateName: '',
    templateCode: '',
    templateType: '',
    targetTable: '',
    fieldMapping: '',
    status: 'ENABLED',
    templateDescription: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row: TemplateData) => {
  dialogTitle.value = '编辑模板'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleView = (row: TemplateData) => {
  dialogTitle.value = '查看模板'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = (row: TemplateData) => {
  ElMessageBox.confirm('确定要删除该模板吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await desensitizationApi.deleteTemplate(row.id!)
      ElMessage.success('删除成功')
      handleSearch()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const handleStatusChange = async (row: TemplateData) => {
  try {
    await desensitizationApi.updateTemplateStatus(row.id!, row.status)
    ElMessage.success(`模板已${row.status === 'ENABLED' ? '启用' : '禁用'}`)
  } catch (error) {
    ElMessage.error('状态更新失败')
    row.status = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  }
}

const handleApply = (row: TemplateData) => {
  currentTemplate.value = row
  applyMode.value = 'IMMEDIATE'
  scheduleTime.value = ''
  applyDialogVisible.value = true
}

const handleApplySubmit = async () => {
  if (!currentTemplate.value) return
  try {
    await desensitizationApi.applyTemplate(
      currentTemplate.value.id!,
      applyMode.value === 'SCHEDULED' ? scheduleTime.value : undefined
    )
    ElMessage.success('模板应用成功')
    applyDialogVisible.value = false
  } catch (error) {
    ElMessage.error('模板应用失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (dialogTitle.value === '新增模板') {
          await desensitizationApi.createTemplate(formData)
          ElMessage.success('新增成功')
        } else {
          await desensitizationApi.updateTemplate(formData.id!, formData)
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
.desensitization-template-container {
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
