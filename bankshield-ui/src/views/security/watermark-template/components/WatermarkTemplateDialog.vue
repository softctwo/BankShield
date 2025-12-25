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
      <!-- 基本信息 -->
      <el-form-item label="模板名称" prop="templateName">
        <el-input
          v-model="formData.templateName"
          placeholder="请输入模板名称"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="水印类型" prop="watermarkType">
        <el-radio-group v-model="formData.watermarkType" @change="handleWatermarkTypeChange">
          <el-radio label="TEXT">文本水印</el-radio>
          <el-radio label="IMAGE">图像水印</el-radio>
          <el-radio label="DATABASE">数据库水印</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="水印内容" prop="watermarkContent">
        <el-input
          v-if="formData.watermarkType === 'TEXT'"
          v-model="formData.watermarkContent"
          type="textarea"
          :rows="3"
          placeholder="请输入水印内容，支持变量：{{TIMESTAMP}}, {{USER}}"
          maxlength="500"
          show-word-limit
        />
        <div v-else-if="formData.watermarkType === 'IMAGE'" class="image-upload-container">
          <el-upload
            ref="uploadRef"
            class="image-uploader"
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleImageChange"
            accept="image/*"
          >
            <img v-if="imagePreview" :src="imagePreview" class="preview-image" />
            <div v-else class="upload-placeholder">
              <el-icon><Plus /></el-icon>
              <span>点击上传水印图片</span>
            </div>
          </el-upload>
          <el-input
            v-model="formData.watermarkContent"
            placeholder="或输入图片Base64编码"
            class="base64-input"
          />
        </div>
        <el-input
          v-else-if="formData.watermarkType === 'DATABASE'"
          v-model="formData.watermarkContent"
          type="textarea"
          :rows="3"
          placeholder="请输入数据库水印内容，支持变量：{{USER}}, {{TIMESTAMP}}, {{DATE}}"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <!-- 文本水印特有设置 -->
      <template v-if="formData.watermarkType === 'TEXT'">
        <el-form-item label="水印位置" prop="watermarkPosition">
          <el-select v-model="formData.watermarkPosition" placeholder="请选择水印位置">
            <el-option label="左上角" value="TOP_LEFT" />
            <el-option label="右上角" value="TOP_RIGHT" />
            <el-option label="左下角" value="BOTTOM_LEFT" />
            <el-option label="右下角" value="BOTTOM_RIGHT" />
            <el-option label="居中" value="CENTER" />
            <el-option label="全屏" value="FULLSCREEN" />
          </el-select>
        </el-form-item>

        <el-form-item label="字体大小" prop="fontSize">
          <el-input-number
            v-model="formData.fontSize"
            :min="8"
            :max="72"
            :step="1"
            controls-position="right"
          />
        </el-form-item>

        <el-form-item label="字体颜色" prop="fontColor">
          <el-color-picker v-model="formData.fontColor" show-alpha />
          <el-input
            v-model="formData.fontColor"
            placeholder="请输入颜色值"
            style="width: 120px; margin-left: 10px"
          />
        </el-form-item>

        <el-form-item label="字体名称" prop="fontFamily">
          <el-select v-model="formData.fontFamily" placeholder="请选择字体">
            <el-option label="Arial" value="Arial" />
            <el-option label="Times New Roman" value="Times New Roman" />
            <el-option label="Helvetica" value="Helvetica" />
            <el-option label="宋体" value="SimSun" />
            <el-option label="微软雅黑" value="Microsoft YaHei" />
          </el-select>
        </el-form-item>
      </template>

      <!-- 图像水印特有设置 -->
      <template v-else-if="formData.watermarkType === 'IMAGE'">
        <el-form-item label="水印位置" prop="watermarkPosition">
          <el-select v-model="formData.watermarkPosition" placeholder="请选择水印位置">
            <el-option label="左上角" value="TOP_LEFT" />
            <el-option label="右上角" value="TOP_RIGHT" />
            <el-option label="左下角" value="BOTTOM_LEFT" />
            <el-option label="右下角" value="BOTTOM_RIGHT" />
            <el-option label="居中" value="CENTER" />
            <el-option label="全屏" value="FULLSCREEN" />
          </el-select>
        </el-form-item>
      </template>

      <!-- 通用设置 -->
      <el-form-item label="透明度" prop="transparency">
        <el-slider
          v-model="formData.transparency"
          :min="0"
          :max="100"
          :step="5"
          show-stops
          show-input
        />
      </el-form-item>

      <el-form-item label="描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入模板描述"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import type { WatermarkTemplate, WatermarkTemplateCreateRequest } from '@/types/watermark'
import { createWatermarkTemplate, updateWatermarkTemplate } from '@/api/watermark'

interface Props {
  modelValue: boolean
  template: WatermarkTemplate | null
  mode: 'add' | 'edit'
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 表单引用
const formRef = ref()
const uploadRef = ref()

// 状态
const loading = ref(false)
const imagePreview = ref('')

// 表单数据
const formData = reactive<WatermarkTemplateCreateRequest>({
  templateName: '',
  watermarkType: 'TEXT',
  watermarkContent: '',
  watermarkPosition: 'BOTTOM_RIGHT',
  transparency: 30,
  fontSize: 12,
  fontColor: '#CCCCCC',
  fontFamily: 'Arial',
  description: '',
  createdBy: 'admin' // 应该从用户信息中获取
})

// 表单验证规则
const formRules = {
  templateName: [
    { required: true, message: '请输入模板名称', trigger: 'blur' },
    { min: 2, max: 100, message: '模板名称长度为2-100个字符', trigger: 'blur' }
  ],
  watermarkType: [
    { required: true, message: '请选择水印类型', trigger: 'change' }
  ],
  watermarkContent: [
    { required: true, message: '请输入水印内容', trigger: 'blur' }
  ],
  watermarkPosition: [
    { required: true, message: '请选择水印位置', trigger: 'change' }
  ],
  transparency: [
    { required: true, message: '请设置透明度', trigger: 'blur' },
    { type: 'number', min: 0, max: 100, message: '透明度范围为0-100', trigger: 'blur' }
  ],
  fontSize: [
    { required: true, message: '请设置字体大小', trigger: 'blur' },
    { type: 'number', min: 8, max: 72, message: '字体大小范围为8-72', trigger: 'blur' }
  ],
  fontColor: [
    { required: true, message: '请选择字体颜色', trigger: 'change' }
  ],
  fontFamily: [
    { required: true, message: '请选择字体', trigger: 'change' }
  ]
}

// 对话框标题
const dialogTitle = computed(() => {
  return props.mode === 'add' ? '新增水印模板' : '编辑水印模板'
})

// 可见性
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 监听模板变化
watch(
  () => props.template,
  (newTemplate) => {
    if (newTemplate) {
      // 编辑模式，填充表单数据
      Object.assign(formData, {
        templateName: newTemplate.templateName,
        watermarkType: newTemplate.watermarkType,
        watermarkContent: newTemplate.watermarkContent,
        watermarkPosition: newTemplate.watermarkPosition,
        transparency: newTemplate.transparency,
        fontSize: newTemplate.fontSize,
        fontColor: newTemplate.fontColor,
        fontFamily: newTemplate.fontFamily,
        description: newTemplate.description
      })
      
      // 如果是图像水印，设置预览
      if (newTemplate.watermarkType === 'IMAGE' && newTemplate.watermarkContent) {
        imagePreview.value = newTemplate.watermarkContent
      }
    } else {
      // 新增模式，重置表单
      resetForm()
    }
  },
  { immediate: true }
)

// 水印类型变更
const handleWatermarkTypeChange = (type: string) => {
  // 重置相关字段
  if (type === 'TEXT') {
    formData.watermarkPosition = 'BOTTOM_RIGHT'
    formData.fontSize = 12
    formData.fontColor = '#CCCCCC'
    formData.fontFamily = 'Arial'
  } else if (type === 'IMAGE') {
    formData.watermarkPosition = 'TOP_LEFT'
    formData.transparency = 30
    imagePreview.value = ''
  } else if (type === 'DATABASE') {
    // 数据库水印不需要位置和透明度设置
    formData.watermarkPosition = undefined
    formData.transparency = 0
  }
}

// 图片变更
const handleImageChange = (file: any) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    imagePreview.value = e.target?.result as string
    formData.watermarkContent = e.target?.result as string
  }
  reader.readAsDataURL(file.raw)
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    templateName: '',
    watermarkType: 'TEXT',
    watermarkContent: '',
    watermarkPosition: 'BOTTOM_RIGHT',
    transparency: 30,
    fontSize: 12,
    fontColor: '#CCCCCC',
    fontFamily: 'Arial',
    description: ''
  })
  imagePreview.value = ''
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
  formRef.value?.resetFields()
  resetForm()
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    loading.value = true
    
    let result
    if (props.mode === 'add') {
      result = await createWatermarkTemplate(formData)
    } else {
      result = await updateWatermarkTemplate({
        ...formData,
        id: props.template!.id,
        enabled: props.template?.enabled ?? true
      })
    }
    
    ElMessage.success(props.mode === 'add' ? '创建成功' : '更新成功')
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
</script>

<style scoped lang="scss">
.image-upload-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.image-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
    
    &:hover {
      border-color: var(--el-color-primary);
    }
  }
}

.upload-placeholder {
  width: 120px;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c939d;
  font-size: 12px;
  
  .el-icon {
    font-size: 24px;
    margin-bottom: 8px;
  }
}

.preview-image {
  width: 120px;
  height: 120px;
  object-fit: contain;
}

.base64-input {
  width: 400px;
}

.color-picker-container {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>