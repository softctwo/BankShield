<template>
  <div class="rule-management">
    <el-card class="header-card">
      <div class="header-content">
        <h2>数据分级规则管理</h2>
        <p class="subtitle">管理5级分类标准（C1-C5）的自动分级规则</p>
      </div>
    </el-card>

    <!-- 操作栏 -->
    <el-card class="toolbar-card">
      <div class="toolbar">
        <div class="left-actions">
          <el-button type="primary" @click="handleAdd" :icon="Plus">新增规则</el-button>
          <el-button @click="handleAutoClassify" :icon="MagicStick">执行自动分级</el-button>
          <el-button @click="handleTestMatch" :icon="Search">测试匹配</el-button>
        </div>
        <div class="right-filters">
          <el-select v-model="filters.ruleStatus" placeholder="规则状态" clearable @change="loadRules">
            <el-option label="全部" value=""></el-option>
            <el-option label="启用" value="ACTIVE"></el-option>
            <el-option label="禁用" value="INACTIVE"></el-option>
          </el-select>
        </div>
      </div>
    </el-card>

    <!-- 规则列表 -->
    <el-card class="table-card">
      <el-table :data="ruleList" v-loading="loading" stripe>
        <el-table-column prop="ruleCode" label="规则编码" width="150"></el-table-column>
        <el-table-column prop="ruleName" label="规则名称" width="180"></el-table-column>
        <el-table-column prop="dataType" label="数据类型" width="120"></el-table-column>
        <el-table-column prop="fieldPattern" label="字段匹配" width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="contentPattern" label="内容匹配" width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="sensitivityLevel" label="敏感级别" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.sensitivityLevel)">{{ row.sensitivityLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100" sortable>
          <template #default="{ row }">
            <el-tag type="info">{{ row.priority }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ruleStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.ruleStatus === 'ACTIVE' ? 'success' : 'danger'">
              {{ row.ruleStatus === 'ACTIVE' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)" :icon="Edit">编辑</el-button>
            <el-button link type="warning" @click="handleToggleStatus(row)" :icon="Switch">
              {{ row.ruleStatus === 'ACTIVE' ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)" :icon="Delete">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadRules"
          @current-change="loadRules"
        />
      </div>
    </el-card>

    <!-- 新增/编辑规则对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      @close="handleDialogClose"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="120px">
        <el-form-item label="规则编码" prop="ruleCode">
          <el-input v-model="formData.ruleCode" placeholder="请输入规则编码，如：RULE_ID_CARD"></el-input>
        </el-form-item>
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="formData.ruleName" placeholder="请输入规则名称"></el-input>
        </el-form-item>
        <el-form-item label="数据类型" prop="dataType">
          <el-select v-model="formData.dataType" placeholder="请选择数据类型" clearable>
            <el-option label="个人信息" value="PERSONAL"></el-option>
            <el-option label="金融数据" value="FINANCIAL"></el-option>
            <el-option label="安全数据" value="SECURITY"></el-option>
            <el-option label="内部数据" value="INTERNAL"></el-option>
            <el-option label="公开数据" value="PUBLIC"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="字段匹配" prop="fieldPattern">
          <el-input
            v-model="formData.fieldPattern"
            placeholder="正则表达式，如：(id_card|idcard|身份证)"
            type="textarea"
            :rows="2"
          ></el-input>
        </el-form-item>
        <el-form-item label="内容匹配" prop="contentPattern">
          <el-input
            v-model="formData.contentPattern"
            placeholder="正则表达式，如：\\d{17}[0-9Xx]"
            type="textarea"
            :rows="2"
          ></el-input>
        </el-form-item>
        <el-form-item label="敏感级别" prop="sensitivityLevel">
          <el-select v-model="formData.sensitivityLevel" placeholder="请选择敏感级别">
            <el-option label="C1 - 公开数据" value="C1"></el-option>
            <el-option label="C2 - 内部数据" value="C2"></el-option>
            <el-option label="C3 - 敏感数据" value="C3"></el-option>
            <el-option label="C4 - 高敏感数据" value="C4"></el-option>
            <el-option label="C5 - 极敏感数据" value="C5"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number v-model="formData.priority" :min="0" :max="100" placeholder="数字越大优先级越高"></el-input-number>
        </el-form-item>
        <el-form-item label="规则描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入规则描述"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 测试匹配对话框 -->
    <el-dialog v-model="testDialogVisible" title="测试规则匹配" width="600px">
      <el-form label-width="100px">
        <el-form-item label="字段名">
          <el-input v-model="testData.fieldName" placeholder="请输入字段名，如：id_card"></el-input>
        </el-form-item>
        <el-form-item label="字段内容">
          <el-input v-model="testData.content" placeholder="请输入字段内容，如：110101199001011234"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleTestSubmit">测试</el-button>
        </el-form-item>
        <el-form-item label="匹配结果" v-if="testResult">
          <el-alert
            :title="testResult.matched ? `匹配成功：${testResult.matchedLevel}` : '未匹配到任何规则'"
            :type="testResult.matched ? 'success' : 'warning'"
            :closable="false"
          ></el-alert>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Switch, MagicStick, Search } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import axios from 'axios'

interface Rule {
  id?: number
  ruleCode: string
  ruleName: string
  dataType: string
  fieldPattern: string
  contentPattern: string
  sensitivityLevel: string
  priority: number
  ruleStatus: string
  description: string
}

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const testDialogVisible = ref(false)
const dialogTitle = ref('新增规则')
const formRef = ref<FormInstance>()

const filters = reactive({
  ruleStatus: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const ruleList = ref<Rule[]>([])

const formData = reactive<Rule>({
  ruleCode: '',
  ruleName: '',
  dataType: '',
  fieldPattern: '',
  contentPattern: '',
  sensitivityLevel: '',
  priority: 50,
  ruleStatus: 'ACTIVE',
  description: ''
})

const testData = reactive({
  fieldName: '',
  content: ''
})

const testResult = ref<any>(null)

const formRules: FormRules = {
  ruleCode: [{ required: true, message: '请输入规则编码', trigger: 'blur' }],
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  sensitivityLevel: [{ required: true, message: '请选择敏感级别', trigger: 'change' }],
  priority: [{ required: true, message: '请输入优先级', trigger: 'blur' }]
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

const loadRules = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/classification/rules', {
      params: {
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
        ruleStatus: filters.ruleStatus
      }
    })
    
    if (response.data.code === 200) {
      ruleList.value = response.data.data.records
      pagination.total = response.data.data.total
    } else {
      ElMessage.error(response.data.message || '加载规则列表失败')
    }
  } catch (error) {
    console.error('加载规则列表失败:', error)
    ElMessage.error('加载规则列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增规则'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: Rule) => {
  dialogTitle.value = '编辑规则'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = async (row: Rule) => {
  try {
    await ElMessageBox.confirm('确定要删除该规则吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await axios.delete(`/api/classification/rules/${row.id}`)
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      loadRules()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除规则失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleToggleStatus = async (row: Rule) => {
  const newStatus = row.ruleStatus === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  try {
    const response = await axios.put(`/api/classification/rules/${row.id}/status`, null, {
      params: { status: newStatus }
    })
    
    if (response.data.code === 200) {
      ElMessage.success('状态更新成功')
      loadRules()
    } else {
      ElMessage.error(response.data.message || '状态更新失败')
    }
  } catch (error) {
    console.error('状态更新失败:', error)
    ElMessage.error('状态更新失败')
  }
}

const handleAutoClassify = async () => {
  try {
    await ElMessageBox.confirm('确定要执行自动分级吗？这将对所有未分级的资产进行分级。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    loading.value = true
    const response = await axios.post('/api/classification/auto-classify')
    
    if (response.data.code === 200) {
      ElMessage.success(response.data.data.message)
    } else {
      ElMessage.error(response.data.message || '自动分级失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('自动分级失败:', error)
      ElMessage.error('自动分级失败')
    }
  } finally {
    loading.value = false
  }
}

const handleTestMatch = () => {
  testData.fieldName = ''
  testData.content = ''
  testResult.value = null
  testDialogVisible.value = true
}

const handleTestSubmit = async () => {
  if (!testData.fieldName) {
    ElMessage.warning('请输入字段名')
    return
  }
  
  try {
    const response = await axios.post('/api/classification/test-match', null, {
      params: {
        fieldName: testData.fieldName,
        content: testData.content
      }
    })
    
    if (response.data.code === 200) {
      testResult.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '测试失败')
    }
  } catch (error) {
    console.error('测试匹配失败:', error)
    ElMessage.error('测试失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      let response
      if (formData.id) {
        response = await axios.put(`/api/classification/rules/${formData.id}`, formData)
      } else {
        response = await axios.post('/api/classification/rules', formData)
      }
      
      if (response.data.code === 200) {
        ElMessage.success(formData.id ? '更新成功' : '创建成功')
        dialogVisible.value = false
        loadRules()
      } else {
        ElMessage.error(response.data.message || '操作失败')
      }
    } catch (error) {
      console.error('提交失败:', error)
      ElMessage.error('操作失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  resetForm()
}

const resetForm = () => {
  Object.assign(formData, {
    id: undefined,
    ruleCode: '',
    ruleName: '',
    dataType: '',
    fieldPattern: '',
    contentPattern: '',
    sensitivityLevel: '',
    priority: 50,
    ruleStatus: 'ACTIVE',
    description: ''
  })
}

onMounted(() => {
  loadRules()
})
</script>

<style scoped lang="less">
.rule-management {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
  
  .header-content {
    h2 {
      margin: 0 0 10px 0;
      font-size: 24px;
      color: #303133;
    }
    
    .subtitle {
      margin: 0;
      color: #909399;
      font-size: 14px;
    }
  }
}

.toolbar-card {
  margin-bottom: 20px;
  
  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .left-actions {
      display: flex;
      gap: 10px;
    }
    
    .right-filters {
      display: flex;
      gap: 10px;
    }
  }
}

.table-card {
  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
