<template>
  <el-dialog
    :title="dialogTitle"
    v-model="visible"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="loading"
    >
      <el-form-item label="任务名称" prop="taskName">
        <el-input
          v-model="formData.taskName"
          placeholder="请输入任务名称"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="任务类型" prop="taskType">
        <el-radio-group v-model="formData.taskType" @change="handleTaskTypeChange">
          <el-radio label="FILE">文件处理</el-radio>
          <el-radio label="DATABASE">数据库处理</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="水印模板" prop="templateId">
        <el-select 
          v-model="formData.templateId" 
          placeholder="请选择水印模板"
          clearable
          filterable
        >
          <el-option
            v-for="template in availableTemplates"
            :key="template.id"
            :label="template.templateName"
            :value="template.id"
          >
            <span style="float: left">{{ template.templateName }}</span>
            <span style="float: right; color: var(--el-text-color-secondary); font-size: 13px">
              {{ getWatermarkTypeLabel(template.watermarkType) }}
            </span>
          </el-option>
        </el-select>
      </el-form-item>

      <!-- 文件处理特有设置 -->
      <template v-if="formData.taskType === 'FILE'">
        <el-form-item label="上传文件" prop="files">
          <el-upload
            ref="uploadRef"
            class="file-uploader"
            action="#"
            :auto-upload="false"
            :multiple="true"
            :file-list="fileList"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :before-upload="beforeFileUpload"
            accept=".pdf,.doc,.docx,.xls,.xlsx,.jpg,.jpeg,.png,.gif,.bmp"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>选择文件
            </el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持 PDF、Word、Excel、图片等格式，单个文件不超过 50MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </template>

      <!-- 数据库处理特有设置 -->
      <template v-else-if="formData.taskType === 'DATABASE'">
        <el-form-item label="数据源" prop="dataSourceId">
          <el-select 
            v-model="formData.dataSourceId" 
            placeholder="请选择数据源"
            clearable
            filterable
          >
            <el-option
              v-for="source in dataSources"
              :key="source.id"
              :label="source.name"
              :value="source.id"
            >
              <span style="float: left">{{ source.name }}</span>
              <span style="float: right; color: var(--el-text-color-secondary); font-size: 13px">
                {{ source.type }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="目标表" prop="tableName">
          <el-input
            v-model="tableName"
            placeholder="请输入数据库表名"
            clearable
          >
            <template #append>
              <el-button @click="handleLoadTables">
                <el-icon><Search /></el-icon>加载
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="选择表" prop="selectedTables" v-if="availableTables.length > 0">
          <el-select 
            v-model="selectedTables" 
            placeholder="请选择要处理的表"
            multiple
            clearable
          >
            <el-option
              v-for="table in availableTables"
              :key="table"
              :label="table"
              :value="table"
            />
          </el-select>
        </el-form-item>
      </template>

      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="formData.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入任务备注"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">
        创建任务
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import type { WatermarkTaskCreateRequest, WatermarkTemplate, WatermarkTask } from '@/types/watermark'
import { createWatermarkTask } from '@/api/watermark'
import { getWatermarkTemplateList } from '@/api/watermark'

interface Props {
  modelValue: boolean
  task: WatermarkTask | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

interface DataSource {
  id: number
  name: string
  type: string
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 表单引用
const formRef = ref()
const uploadRef = ref()

// 状态
const loading = ref(false)
const fileList = ref<any[]>([])

// 表单数据
const formData = reactive<WatermarkTaskCreateRequest>({
  taskName: '',
  taskType: 'FILE',
  templateId: undefined,
  dataSourceId: undefined,
  createdBy: 'admin', // 应该从用户信息中获取
  remark: ''
})

// 其他数据
const tableName = ref('')
const selectedTables = ref<string[]>([])

// 可用模板
const availableTemplates = ref<WatermarkTemplate[]>([])

// 数据源（模拟数据）
const dataSources = ref<DataSource[]>([
  { id: 1, name: '客户数据库', type: 'MySQL' },
  { id: 2, name: '交易数据库', type: 'PostgreSQL' },
  { id: 3, name: '日志数据库', type: 'MongoDB' }
])

// 可用表（模拟数据）
const availableTables = ref<string[]>([])

// 表单验证规则
const formRules = {
  taskName: [
    { required: true, message: '请输入任务名称', trigger: 'blur' },
    { min: 2, max: 100, message: '任务名称长度为2-100个字符', trigger: 'blur' }
  ],
  taskType: [
    { required: true, message: '请选择任务类型', trigger: 'change' }
  ],
  templateId: [
    { required: true, message: '请选择水印模板', trigger: 'change' }
  ],
  dataSourceId: [
    { required: true, message: '请选择数据源', trigger: 'change' }
  ],
  selectedTables: [
    { required: true, message: '请选择要处理的表', trigger: 'change' }
  ]
}

// 对话框标题
const dialogTitle = computed(() => {
  return '创建水印任务'
})

// 可见性
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 监听任务类型变化，加载对应的模板
watch(
  () => formData.taskType,
  async (newType) => {
    await loadTemplates(newType)
  }
)

// 加载模板列表
const loadTemplates = async (taskType?: string) => {
  try {
    const params = {
      watermarkType: taskType === 'FILE' ? 'TEXT,IMAGE' : 'DATABASE',
      enabled: 1
    }
    
    const res = await getWatermarkTemplateList(params)
    availableTemplates.value = res.list
  } catch (error) {
    console.error('加载模板失败', error)
  }
}

// 任务类型变更
const handleTaskTypeChange = (type: string) => {
  // 重置相关字段
  if (type === 'FILE') {
    formData.dataSourceId = undefined
    selectedTables.value = []
    tableName.value = ''
  } else if (type === 'DATABASE') {
    fileList.value = []
  }
}

// 文件变更
const handleFileChange = (file: any, files: any[]) => {
  fileList.value = files
}

// 文件移除
const handleFileRemove = (file: any, files: any[]) => {
  fileList.value = files
}

// 文件上传前验证
const beforeFileUpload = (file: any) => {
  const isLt50M = file.size / 1024 / 1024 < 50
  if (!isLt50M) {
    ElMessage.error('文件大小不能超过 50MB!')
    return false
  }
  return true
}

// 加载表
const handleLoadTables = async () => {
  if (!formData.dataSourceId) {
    ElMessage.warning('请先选择数据源')
    return
  }
  
  if (!tableName.value.trim()) {
    ElMessage.warning('请输入表名关键字')
    return
  }
  
  try {
    loading.value = true
    
    // 这里应该调用API获取数据库表
    // 模拟数据
    availableTables.value = [
      'customer_info',
      'customer_contact',
      'customer_address',
      'account_info',
      'transaction_record'
    ].filter(table => 
      table.toLowerCase().includes(tableName.value.toLowerCase())
    )
    
    if (availableTables.value.length === 0) {
      ElMessage.info('未找到匹配的表')
    }
    
  } catch (error) {
    ElMessage.error('加载表失败')
  } finally {
    loading.value = false
  }
}

// 获取水印类型标签
const getWatermarkTypeLabel = (type: string) => {
  const map = {
    'TEXT': '文本',
    'IMAGE': '图像',
    'DATABASE': '数据库'
  }
  return map[type as keyof typeof map] || type
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
  formRef.value?.resetFields()
  resetForm()
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    taskName: '',
    taskType: 'FILE',
    templateId: undefined,
    dataSourceId: undefined,
    remark: ''
  })
  fileList.value = []
  selectedTables.value = []
  tableName.value = ''
  availableTables.value = []
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    
    // 验证文件或表的选择
    if (formData.taskType === 'FILE') {
      if (fileList.value.length === 0) {
        ElMessage.warning('请至少选择一个文件')
        return
      }
    } else if (formData.taskType === 'DATABASE') {
      if (selectedTables.value.length === 0) {
        ElMessage.warning('请至少选择一个表')
        return
      }
    }
    
    loading.value = true
    
    // 创建任务
    const result = await createWatermarkTask(formData)
    
    ElMessage.success('任务创建成功')
    emit('success')
    handleClose()
    
  } catch (error: any) {
    if (error?.message) {
      ElMessage.error(error.message)
    }
  } finally {
    loading.value = false
  }
}

// 初始化加载模板
const init = async () => {
  await loadTemplates('FILE')
}

init()
</script>

<style scoped lang="scss">
.file-uploader {
  :deep(.el-upload) {
    width: 100%;
  }
  
  .el-upload__tip {
    color: #909399;
    font-size: 12px;
    margin-top: 8px;
  }
}
</style>